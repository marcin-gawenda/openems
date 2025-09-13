package io.openems.edge.deye.gridtied;

import static io.openems.edge.bridge.modbus.api.ElementToChannelConverter.INVERT_IF_TRUE;
import static io.openems.edge.bridge.modbus.api.ElementToChannelConverter.SUBTRACT;
import static io.openems.edge.bridge.modbus.api.ElementToChannelConverter.DIVIDE;
import static io.openems.edge.bridge.modbus.api.ElementToChannelConverter.SCALE_FACTOR_1;
import static io.openems.edge.bridge.modbus.api.ElementToChannelConverter.SCALE_FACTOR_2;
import static io.openems.edge.bridge.modbus.api.ElementToChannelConverter.SCALE_FACTOR_3;
import static io.openems.edge.bridge.modbus.api.ElementToChannelConverter.SCALE_FACTOR_MINUS_1;
import static io.openems.edge.bridge.modbus.api.ElementToChannelConverter.SCALE_FACTOR_MINUS_2;
import static io.openems.edge.bridge.modbus.api.ElementToChannelConverter.chain;

import java.util.Optional;
import java.util.function.Consumer;

import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.component.annotations.ReferencePolicyOption;
import org.osgi.service.metatype.annotations.Designate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.openems.common.exceptions.OpenemsException;
import io.openems.common.types.ChannelAddress;
import io.openems.common.types.MeterType;
import io.openems.common.types.OpenemsType;
import io.openems.edge.battery.api.Battery;
import io.openems.edge.bridge.modbus.BridgeModbusRtuOverTcpImpl;
import io.openems.edge.bridge.modbus.api.AbstractOpenemsModbusComponent;
import io.openems.edge.bridge.modbus.api.BridgeModbus;

import io.openems.edge.bridge.modbus.api.ModbusComponent;
import io.openems.edge.bridge.modbus.api.ModbusProtocol;
import io.openems.edge.common.channel.Channel;
import io.openems.edge.common.channel.IntegerReadChannel;
import io.openems.edge.common.channel.value.Value;
import io.openems.edge.common.component.OpenemsComponent;
import io.openems.edge.meter.api.ElectricityMeter;
import io.openems.edge.bridge.modbus.api.element.DummyRegisterElement;
import io.openems.edge.bridge.modbus.api.element.SignedWordElement;
import io.openems.edge.bridge.modbus.api.element.StringWordElement;
import io.openems.edge.bridge.modbus.api.element.UnsignedDoublewordElement;
import io.openems.edge.bridge.modbus.api.element.UnsignedWordElement;
import io.openems.edge.bridge.modbus.api.element.WordOrder;
import io.openems.edge.bridge.modbus.api.task.FC3ReadRegistersTask;
import io.openems.edge.common.taskmanager.Priority;
import io.openems.edge.common.type.TypeUtils;
import io.openems.edge.deye.hybrid.DeyeHybridInverter;
import io.openems.edge.timedata.api.Timedata;
import io.openems.edge.timedata.api.TimedataProvider;


@Designate(ocd = Config.class, factory = true)
@Component(//
		name = "io.openems.edge.deye.gridtied", //
		immediate = true, //
		configurationPolicy = ConfigurationPolicy.REQUIRE //
)
public class DeyeGridTiedInverterImpl extends AbstractOpenemsModbusComponent implements DeyeGridTiedInverter, ElectricityMeter, TimedataProvider, ModbusComponent, OpenemsComponent {

	private final static Logger log = LoggerFactory.getLogger(DeyeGridTiedInverterImpl.class);
	
	@Reference
	private ConfigurationAdmin cm;

	
	@Reference(policy = ReferencePolicy.DYNAMIC, policyOption = ReferencePolicyOption.GREEDY, cardinality = ReferenceCardinality.OPTIONAL)
	private volatile Timedata timedata;
	
	@Reference(policy = ReferencePolicy.STATIC, policyOption = ReferencePolicyOption.GREEDY, cardinality = ReferenceCardinality.MANDATORY)
	protected void setModbus(BridgeModbus modbus) {
		super.setModbus(modbus);
	}

	private MeterType meterType = MeterType.PRODUCTION;

	public DeyeGridTiedInverterImpl() {
		super(//
				OpenemsComponent.ChannelId.values(), //
				ModbusComponent.ChannelId.values(), //
				ElectricityMeter.ChannelId.values(), //				
				DeyeGridTiedInverter.ChannelId.values() //
		);
		
		calculatePower(getPowerPv1Channel(), getVoltagePv1Channel(), getCurrentPv1Channel()); 
		calculatePower(getPowerPv2Channel(), getVoltagePv2Channel(), getCurrentPv2Channel());
		calculatePowerPv();		
		calculateGridPower("L1", getGridPowerL1Channel(), getGridVoltageL1Channel(), getGridCurrentL1Channel());
		calculateGridPower("L2", getGridPowerL2Channel(), getGridVoltageL2Channel(), getGridCurrentL2Channel());
		calculateGridPower("L3", getGridPowerL3Channel(), getGridVoltageL3Channel(), getGridCurrentL3Channel());
		fixGridReactivePower();
	}

	@Activate
	private void activate(ComponentContext context, Config config) throws OpenemsException {
		this.meterType = config.type();
		
		if(super.activate(context, config.id(), config.alias(), config.enabled(), config.modbusUnitId(), this.cm, "Modbus",
				config.modbus_id())) {
			return;
		}		
	}

	@Override
	@Deactivate
	protected void deactivate() {
		super.deactivate();
	}

	@Override
	public MeterType getMeterType() {
		return this.meterType;
	}
	
	@Override
	protected ModbusProtocol defineModbusProtocol() {
		var modbusProtocol = new ModbusProtocol(this,
				new FC3ReadRegistersTask(0, Priority.HIGH,
						m(DeyeGridTiedInverter.ChannelId.TYPE, new UnsignedWordElement(0)), // YPE: 2:Grid-tied inverter
						new DummyRegisterElement(1, 2),
						m(DeyeGridTiedInverter.ChannelId.SN, new StringWordElement(3, 5)), // SN: 2309132384
						new DummyRegisterElement(8, 58),
//						m(DeyeGridTiedInverter.ChannelId.GRID_COS_PHI, new SignedWordElement(39), SUBTRACT(1000)), // -800 = -0.8, seems to be the value expected for PF mode but not real value
//						new DummyRegisterElement(40, 58),
						m(DeyeGridTiedInverter.ChannelId.INV_STATUS, new UnsignedWordElement(59)), // INV_STATUS: 2 (normal)
						m(DeyeGridTiedInverter.ChannelId.E_GRID_TODAY, new SignedWordElement(60), SCALE_FACTOR_2), // E_GRID_TODAY: 13100 Wh
						new DummyRegisterElement(61, 62),
//						m(DeyeGridTiedInverter.ChannelId.RE_GRID_TODAY, new SignedWordElement(61), SCALE_FACTOR_2), // always 0						
						m(DeyeGridTiedInverter.ChannelId.E_GRID_TOTAL, new UnsignedDoublewordElement(63).wordOrder(WordOrder.LSWMSW), SCALE_FACTOR_MINUS_1), // E_GRID_TOTAL: 24967 kWh
						new DummyRegisterElement(65, 72),
//						m(DeyeGridTiedInverter.ChannelId.RE_GRID_TOTAL, new UnsignedDoublewordElement(65).wordOrder(WordOrder.LSWMSW), SCALE_FACTOR_MINUS_1), // RE_GRID_TOTAL: 0 kWh						
						m(DeyeGridTiedInverter.ChannelId.GRID_VOLTAGE_L1, new UnsignedWordElement(73), SCALE_FACTOR_2), // GRID_VOLTAGE_L1: 233900 mV
						m(DeyeGridTiedInverter.ChannelId.GRID_VOLTAGE_L2, new UnsignedWordElement(74), SCALE_FACTOR_2), // GRID_VOLTAGE_L2: 236100 mV
						m(DeyeGridTiedInverter.ChannelId.GRID_VOLTAGE_L3, new UnsignedWordElement(75), SCALE_FACTOR_2), // GRID_VOLTAGE_L3: 232400 mV
						m(DeyeGridTiedInverter.ChannelId.GRID_CURRENT_L1, new UnsignedWordElement(76), SCALE_FACTOR_2), // GRID_CURRENT_L1: 1370 mA
						m(DeyeGridTiedInverter.ChannelId.GRID_CURRENT_L2, new UnsignedWordElement(77), SCALE_FACTOR_2), // GRID_CURRENT_L2: 1470 mA
						m(DeyeGridTiedInverter.ChannelId.GRID_CURRENT_L3, new UnsignedWordElement(78), SCALE_FACTOR_2), // GRID_CURRENT_L3: 1430 mA
						m(DeyeGridTiedInverter.ChannelId.GRID_FREQUENCY, new UnsignedWordElement(79), SCALE_FACTOR_1), // GRID_FREQUENCY: 49950 mHz
						new DummyRegisterElement(80, 83), // 1019 W
						// GridPower value is weird, better to calculate it always
//						m(DeyeGridTiedInverter.ChannelId.GRID_POWER, new UnsignedDoublewordElement(82).wordOrder(WordOrder.LSWMSW), SCALE_FACTOR_MINUS_1), // GRID_POWER: 1050 W
						m(DeyeGridTiedInverter.ChannelId.GRID_APPARENT_POWER, new UnsignedDoublewordElement(84).wordOrder(WordOrder.LSWMSW), SCALE_FACTOR_MINUS_1), // GRID_APPARENT_POWER: 1097 VA
						new DummyRegisterElement(86, 87), // 1023 W, out active						
//						m(DeyeGridTiedInverter.ChannelId.GRID_POWER2, new UnsignedDoublewordElement(86).wordOrder(WordOrder.LSWMSW), SCALE_FACTOR_MINUS_1), 
						m(DeyeGridTiedInverter.ChannelId.GRID_REACTIVE_POWER, new UnsignedDoublewordElement(88).wordOrder(WordOrder.LSWMSW), SCALE_FACTOR_MINUS_1), // GRID_REACTIVE_POWER: 200 var 
						m(DeyeGridTiedInverter.ChannelId.DC_TRANS_TEMP, new SignedWordElement(90), SUBTRACT(1000)), // DC_TRANS_TEMP: 250 dC
						m(DeyeGridTiedInverter.ChannelId.IGBT_TEMP, new SignedWordElement(91), SUBTRACT(1000)), // IGBT_TEMP: 250 dC
						new DummyRegisterElement(92, 92),
						m(DeyeGridTiedInverter.ChannelId.GRID_COS_PHI, new UnsignedWordElement(93)), // 0 - 1000
						new DummyRegisterElement(94, 97),
//						m(DeyeGridTiedInverter.ChannelId.E_PV_TOTAL, new UnsignedDoublewordElement(96).wordOrder(WordOrder.LSWMSW), SCALE_FACTOR_MINUS_1), // weird value
						m(DeyeGridTiedInverter.ChannelId.GFCI, new SignedWordElement(98), SCALE_FACTOR_1),
						new DummyRegisterElement(99, 108),
//						m(DeyeGridTiedInverter.ChannelId.E_PV_TODAY, new SignedWordElement(108), SCALE_FACTOR_2), // always 0
						m(DeyeGridTiedInverter.ChannelId.VOLTAGE_PV1, new UnsignedWordElement(109), SCALE_FACTOR_2), // VOLTAGE_PV1: 442100 mV
						m(DeyeGridTiedInverter.ChannelId.CURRENT_PV1, new UnsignedWordElement(110), SCALE_FACTOR_2), // CURRENT_PV1: 2200 mA
						m(DeyeGridTiedInverter.ChannelId.VOLTAGE_PV2, new UnsignedWordElement(111), SCALE_FACTOR_2), //
						m(DeyeGridTiedInverter.ChannelId.CURRENT_PV2, new UnsignedWordElement(112), SCALE_FACTOR_2) //
					)
				);
		
		// 63: TEST_INT: 53048 14hours
		// 92: 4 dC inductance temp ?
		// 95: always 0 ambient temp ?
		// 93: cos phi always 1000
		// 200: same as 60:
		// 205: 0
		// 208: 0
//		var rUInt = 208;
//		modbusProtocol.addTask(new FC3ReadRegistersTask(rUInt, Priority.HIGH,
////		modbusProtocol.addTask(new FC4ReadInputRegistersTask(rUInt, Priority.HIGH,
//				m(DeyeGridTiedInverter.ChannelId.TEST_INT, new UnsignedWordElement(rUInt)) //
//			)
//		);		
//		
//		// 201: same as 63:
//		// 206: 0
//		// 209: 0
//		var rUDword = 209;
//		modbusProtocol.addTask(new FC3ReadRegistersTask(rUDword, Priority.HIGH,
////		modbusProtocol.addTask(new FC4ReadInputRegistersTask(rUInt, Priority.HIGH,
//				m(DeyeGridTiedInverter.ChannelId.TEST_LONG, new UnsignedDoublewordElement(rUDword).wordOrder(WordOrder.LSWMSW)) //
//			)
//		);
		
		return modbusProtocol;
	}
	
	private void calculatePower(IntegerReadChannel powerChannel, IntegerReadChannel voltageChannel, IntegerReadChannel currentChannel) {		
		final Consumer<Value<Integer>> calculate = ignore -> {
			Optional<Integer> voltageOpt = voltageChannel.getNextValue().asOptional();
			Optional<Integer> currentOpt = currentChannel.getNextValue().asOptional();
			
			if (voltageOpt.isPresent() && currentOpt.isPresent()) {
			    Integer power = (int) ((voltageOpt.get() / 1_000.0) * (currentOpt.get() / 1_000.0)); // mV * mA -> W
				this.logDebug(log, "calculatePower: " + power);
				powerChannel.setNextValue(power);
			} else {
				this.logDebug(log, "calculatePower: one of volt or current is null");
				powerChannel.setNextValue(null);
			}
		};
		
		voltageChannel.onSetNextValue(calculate);
		currentChannel.onSetNextValue(calculate);		
	}
	
	private void calculatePowerPv() {		
		final Consumer<Value<Integer>> calculate = ignore -> {
			Integer power = TypeUtils.sum(getPowerPv1Channel().getNextValue().get(), getPowerPv2Channel().getNextValue().get());			    
			this.logDebug(log, "calculatePowerPv: " + power);			
			getPowerPvChannel().setNextValue(power);
		};
		
		getPowerPv1Channel().onSetNextValue(calculate);
		getPowerPv2Channel().onSetNextValue(calculate);		
	}
	
	
	private void calculateGridPower(String phase, IntegerReadChannel powerChannel, IntegerReadChannel voltageChannel, IntegerReadChannel currentChannel) {		
		final Consumer<Value<Integer>> calculate = ignore -> {
			Optional<Integer> voltageOpt = voltageChannel.getNextValue().asOptional();
			Optional<Integer> currentOpt = currentChannel.getNextValue().asOptional();
			Optional<Integer> cosPhiOpt = getGridCosPhiChannel().getNextValue().asOptional();
			
			float cosPhi = 1.0f;
			if (cosPhiOpt.isPresent()) {
				if (cosPhiOpt.get() == 0) {
					this.logDebug(log, "fixCosPhi: was 0, setting to 1000");
					getGridCosPhiChannel().setNextValue(1000);	
				} else {
					cosPhi = cosPhiOpt.get() / 1_000.0f;
					if (voltageOpt.isPresent() && currentOpt.isPresent()) {
					    Integer power = (int) ((voltageOpt.get() / 1_000.0) * (currentOpt.get() / 1_000.0) * cosPhi); // mV * mA -> W, cos(phi) per phase is not available 
						this.logDebug(log, "calculateGridPower: phase: " + phase + " power: " + power);
						powerChannel.setNextValue(power);
						// GridPower value is weird, better to calculate it always
							// getGridPowerChannel().getNextValue().isDefined() && getGridPowerChannel().getNextValue().get() == 0				
						if (getGridPowerL1Channel().getNextValue().isDefined() 
							&& getGridPowerL2Channel().getNextValue().isDefined()
							&& getGridPowerL3Channel().getNextValue().isDefined()) { // 
							getGridPowerChannel().setNextValue(
									getGridPowerL1Channel().getNextValue().get()
									+ getGridPowerL2Channel().getNextValue().get()
									+ getGridPowerL3Channel().getNextValue().get()
								);
							this.logDebug(log, "calculateGridPower: total grid power was 0, setting to sum of L1, L2, L3");
						}						
					} else {
						this.logDebug(log, "calculateGridPower: one of Voltage or Current is null for phase: " + phase);
						powerChannel.setNextValue(null);
					}
				}				
			}
		};
		
		voltageChannel.onSetNextValue(calculate);
		currentChannel.onSetNextValue(calculate);
		getGridCosPhiChannel().onSetNextValue(calculate);
	}
	
	/*
	 * observing weird values GRID_REACTIVE_POWER: 429496350 var
	 */
	private void fixGridReactivePower() {		
		final Consumer<Value<Long>> calculate = ignore -> {
			Optional<Long> valOpt = getGridReactivePowerChannel().getNextValue().asOptional();			
			if (valOpt.isPresent() && valOpt.get() > 100_000) {
				this.logInfo(log, "fixGridReactivePower: was: " + valOpt.get() + " [var], setting to 0");
				getGridReactivePowerChannel().setNextValue(0);
			}
		};
		
		getGridReactivePowerChannel().onSetNextValue(calculate);
	}
	
	@Override
	public String debugLog() {		
		return "\n\tid: " + this.getUnitId()
//				+ ", L:" //+ this.getActivePower().asString() //
				+ ", TYPE: " + this.channel(DeyeGridTiedInverter.ChannelId.TYPE).value().asString()
				+ ", SN: " + this.channel(DeyeGridTiedInverter.ChannelId.SN).value().asString()		
				+ ", INV_STATUS: " + this.channel(DeyeGridTiedInverter.ChannelId.INV_STATUS).value().asString()
				+ ", E_GRID_TODAY: " + this.channel(DeyeGridTiedInverter.ChannelId.E_GRID_TODAY).value().asString()
//				+ ", RE_GRID_TODAY: " + this.channel(DeyeGridTiedInverter.ChannelId.RE_GRID_TODAY).value().asString()
//				+ ", E_GRID_TOTAL: " + this.channel(DeyeGridTiedInverter.ChannelId.E_GRID_TOTAL).value().asString()
//				+ ", RE_GRID_TOTAL: " + this.channel(DeyeGridTiedInverter.ChannelId.RE_GRID_TOTAL).value().asString()
				+ ", GRID_VOLTAGE_L1: " + this.channel(DeyeGridTiedInverter.ChannelId.GRID_VOLTAGE_L1).value().asString()
				+ ", GRID_VOLTAGE_L2: " + this.channel(DeyeGridTiedInverter.ChannelId.GRID_VOLTAGE_L2).value().asString()
				+ ", GRID_VOLTAGE_L3: " + this.channel(DeyeGridTiedInverter.ChannelId.GRID_VOLTAGE_L3).value().asString()				
				+ ", GRID_CURRENT_L1: " + this.channel(DeyeGridTiedInverter.ChannelId.GRID_CURRENT_L1).value().asString()
				+ ", GRID_CURRENT_L2: " + this.channel(DeyeGridTiedInverter.ChannelId.GRID_CURRENT_L2).value().asString()
				+ ", GRID_CURRENT_L3: " + this.channel(DeyeGridTiedInverter.ChannelId.GRID_CURRENT_L3).value().asString()	
//				+ ", GRID_FREQUENCY: " + this.channel(DeyeGridTiedInverter.ChannelId.GRID_FREQUENCY).value().asString()
				+ ", GRID_POWER: " + this.channel(DeyeGridTiedInverter.ChannelId.GRID_POWER).value().asString()
//				+ ", GRID_POWER2: " + this.channel(DeyeGridTiedInverter.ChannelId.GRID_POWER2).value().asString()
//				+ ", GRID_POWER_L1: " + this.channel(DeyeGridTiedInverter.ChannelId.GRID_POWER_L1).value().asString()
//				+ ", GRID_POWER_L2: " + this.channel(DeyeGridTiedInverter.ChannelId.GRID_POWER_L2).value().asString()
//				+ ", GRID_POWER_L3: " + this.channel(DeyeGridTiedInverter.ChannelId.GRID_POWER_L3).value().asString()
//				+ ", GRID_APPARENT_POWER: " + this.channel(DeyeGridTiedInverter.ChannelId.GRID_APPARENT_POWER).value().asString()
//				+ ", GRID_REACTIVE_POWER: " + this.channel(DeyeGridTiedInverter.ChannelId.GRID_REACTIVE_POWER).value().asString()
//				+ ", DC_TRANS_TEMP: " + this.channel(DeyeGridTiedInverter.ChannelId.DC_TRANS_TEMP).value().asString()
//				+ ", IGBT_TEMP: " + this.channel(DeyeGridTiedInverter.ChannelId.IGBT_TEMP).value().asString()
				+ ", GRID_COS_PHI: " + this.channel(DeyeGridTiedInverter.ChannelId.GRID_COS_PHI).value().asString()
//				+ ", E_PV_TODAY: " + this.channel(DeyeGridTiedInverter.ChannelId.E_PV_TODAY).value().asString()
//				+ ", E_PV_TOTAL: " + this.channel(DeyeGridTiedInverter.ChannelId.E_PV_TOTAL).value().asString()
//				+ ", GFCI: " + this.channel(DeyeGridTiedInverter.ChannelId.GFCI).value().asString()
//				+ ", VOLTAGE_PV1: " + this.channel(DeyeGridTiedInverter.ChannelId.VOLTAGE_PV1).value().asString()
//				+ ", CURRENT_PV1: " + this.channel(DeyeGridTiedInverter.ChannelId.CURRENT_PV1).value().asString()
//				+ ", VOLTAGE_PV2: " + this.channel(DeyeGridTiedInverter.ChannelId.VOLTAGE_PV2).value().asString()
//				+ ", CURRENT_PV2: " + this.channel(DeyeGridTiedInverter.ChannelId.CURRENT_PV2).value().asString()
//				+ ", POWER_PV1: " + this.channel(DeyeGridTiedInverter.ChannelId.POWER_PV1).value().asString() 
//				+ ", POWER_PV2: " + this.channel(DeyeGridTiedInverter.ChannelId.POWER_PV2).value().asString()
				+ ", POWER_PV: " + this.channel(DeyeGridTiedInverter.ChannelId.POWER_PV).value().asString()
				
				// just for testing
//				+ ", TEST_INT: " + this.channel(DeyeGridTiedInverter.ChannelId.TEST_INT).value().asString()
//				+ ", TEST_LONG: " + this.channel(DeyeGridTiedInverter.ChannelId.TEST_LONG).value().asString()
				;

	}
	
	@Override
	public Timedata getTimedata() {
		return this.timedata;
	}
}
