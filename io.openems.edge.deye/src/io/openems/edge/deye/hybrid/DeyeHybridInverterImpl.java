package io.openems.edge.deye.hybrid;

import static io.openems.edge.bridge.modbus.api.ElementToChannelConverter.INVERT_IF_TRUE;
import static io.openems.edge.bridge.modbus.api.ElementToChannelConverter.SUBTRACT;
import static io.openems.edge.bridge.modbus.api.ElementToChannelConverter.DIVIDE;
import static io.openems.edge.bridge.modbus.api.ElementToChannelConverter.SCALE_FACTOR_1;
import static io.openems.edge.bridge.modbus.api.ElementToChannelConverter.SCALE_FACTOR_2;
import static io.openems.edge.bridge.modbus.api.ElementToChannelConverter.SCALE_FACTOR_3;
import static io.openems.edge.bridge.modbus.api.ElementToChannelConverter.SCALE_FACTOR_MINUS_1;
import static io.openems.edge.bridge.modbus.api.ElementToChannelConverter.SCALE_FACTOR_MINUS_2;
import static io.openems.edge.bridge.modbus.api.ElementToChannelConverter.chain;

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

import io.openems.common.exceptions.OpenemsException;
import io.openems.common.types.MeterType;
import io.openems.edge.battery.api.Battery;
import io.openems.edge.bridge.modbus.api.AbstractOpenemsModbusComponent;
import io.openems.edge.bridge.modbus.api.BridgeModbus;

import io.openems.edge.bridge.modbus.api.ModbusComponent;
import io.openems.edge.bridge.modbus.api.ModbusProtocol;
import io.openems.edge.common.channel.value.Value;
import io.openems.edge.common.component.OpenemsComponent;
import io.openems.edge.meter.api.ElectricityMeter;
import io.openems.edge.bridge.modbus.api.element.DummyRegisterElement;
import io.openems.edge.bridge.modbus.api.element.FloatDoublewordElement;
import io.openems.edge.bridge.modbus.api.element.SignedWordElement;
import io.openems.edge.bridge.modbus.api.element.StringWordElement;
import io.openems.edge.bridge.modbus.api.element.UnsignedDoublewordElement;
import io.openems.edge.bridge.modbus.api.element.UnsignedWordElement;
import io.openems.edge.bridge.modbus.api.task.FC3ReadRegistersTask;
import io.openems.edge.bridge.modbus.api.task.FC4ReadInputRegistersTask;
import io.openems.edge.common.taskmanager.Priority;
import io.openems.edge.timedata.api.Timedata;
import io.openems.edge.timedata.api.TimedataProvider;
import io.openems.edge.ess.api.SymmetricEss;


/**
 * Consumption: energy taken from the grid (registered by the grid meter as consumed by the metered system)
 * Production: energy send to the grid (registered by the grid meter as fed-in from the metered system to the grid)
 */
@Designate(ocd = Config.class, factory = true)
@Component(//
		name = "io.openems.edge.deye.hybrid", //
		immediate = true, //
		configurationPolicy = ConfigurationPolicy.REQUIRE //
)
public class DeyeHybridInverterImpl extends AbstractOpenemsModbusComponent implements DeyeHybridInverter, ElectricityMeter, TimedataProvider, ModbusComponent, OpenemsComponent {

	@Reference
	private ConfigurationAdmin cm;

	
	@Reference(policy = ReferencePolicy.DYNAMIC, policyOption = ReferencePolicyOption.GREEDY, cardinality = ReferenceCardinality.OPTIONAL)
	private volatile Timedata timedata;
	
	@Reference(policy = ReferencePolicy.STATIC, policyOption = ReferencePolicyOption.GREEDY, cardinality = ReferenceCardinality.MANDATORY)
	protected void setModbus(BridgeModbus modbus) {
		super.setModbus(modbus);
	}

	private MeterType meterType = MeterType.GRID;

	public DeyeHybridInverterImpl() {
		super(//
				OpenemsComponent.ChannelId.values(), //
				ModbusComponent.ChannelId.values(), //
				ElectricityMeter.ChannelId.values(), //				
				DeyeHybridInverter.ChannelId.values() //
		);
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
		var modbusProtocol = new ModbusProtocol(this, //
//				new FC3ReadRegistersTask(1, Priority.LOW,
//						m(SymmetricEss.ChannelId.GRID_MODE, new UnsignedWordElement(1)), new DummyRegisterElement(2),
//						m(DeyeHybridInverter.ChannelId.SERIAL_NUMBER, new StringWordElement(3, 5))),

				new FC3ReadRegistersTask(500, Priority.HIGH,
						m(DeyeHybridInverter.ChannelId.INVERTER_RUN_STATE, new UnsignedWordElement(500)) // INVERTER_RUN_STATE: 2 (normal)
//						m(DeyeHybridInverter.ChannelId.ACTIVE_ENERGY_GEN_TODAY, new SignedWordElement(501), SCALE_FACTOR_2), // always 0
//						m(DeyeHybridInverter.ChannelId.REACTIVE_ENERGY_GEN_TODAY, new UnsignedWordElement(502), SCALE_FACTOR_2) // always 0				
					)
				);
		
		modbusProtocol.addTask(new FC3ReadRegistersTask(514, Priority.HIGH,
				m(DeyeHybridInverter.ChannelId.BAT_CHARGE_TODAY, new UnsignedWordElement(514), SCALE_FACTOR_2), // BAT_CHARGE_TODAY: 19100 Wh
				m(DeyeHybridInverter.ChannelId.BAT_DISCHARGE_TODAY, new UnsignedWordElement(515), SCALE_FACTOR_2), // BAT_DISCHARGE_TODAY: 28800 Wh
				new DummyRegisterElement(516, 519),
				m(DeyeHybridInverter.ChannelId.E_GRID_BUY_TODAY, new UnsignedWordElement(520), SCALE_FACTOR_2), // E_GRID_BUY_TODAY: 0 Wh
				m(DeyeHybridInverter.ChannelId.E_GRID_SELL_TODAY, new UnsignedWordElement(521), SCALE_FACTOR_2), // E_GRID_SELL_TODAY: 39800 Wh
				new DummyRegisterElement(522, 525),
				m(DeyeHybridInverter.ChannelId.E_LOAD_TODAY, new UnsignedWordElement(526), SCALE_FACTOR_2), // E_LOAD_TODAY: 600 Wh
				new DummyRegisterElement(527, 528),
				m(DeyeHybridInverter.ChannelId.E_PV_TODAY, new UnsignedWordElement(529), SCALE_FACTOR_2) // E_PV_TODAY: 36300 Wh
//				m(DeyeHybridInverter.ChannelId.E_PV1_TODAY, new UnsignedWordElement(530), SCALE_FACTOR_2), // always 0
//				m(DeyeHybridInverter.ChannelId.E_PV2_TODAY, new UnsignedWordElement(531), SCALE_FACTOR_2), // always 0
//				m(DeyeHybridInverter.ChannelId.E_PV3_TODAY, new UnsignedWordElement(532), SCALE_FACTOR_2), // always 0
//				m(DeyeHybridInverter.ChannelId.E_PV4_TODAY, new UnsignedWordElement(533), SCALE_FACTOR_2) // always 0						
			)
		);
		modbusProtocol.addTask(new FC3ReadRegistersTask(540, Priority.HIGH,
				m(DeyeHybridInverter.ChannelId.DC_TRANSFORMER_TEMP, new SignedWordElement(540), SUBTRACT(1000)), // DC_TRANSFORMER_TEMP: 250 dC
				m(DeyeHybridInverter.ChannelId.HEAT_SINK_TEMP, new SignedWordElement(541), SUBTRACT(1000)) // HEAT_SINK_TEMP: 270 dC	
			)
		);	
				
		modbusProtocol.addTask(new FC3ReadRegistersTask(586, Priority.HIGH,
				m(DeyeHybridInverter.ChannelId.BAT_TEMP, new SignedWordElement(586), SUBTRACT(1000)), // offset 1000, BAT_TEMP: 160 dC 
				m(DeyeHybridInverter.ChannelId.BAT_VOLTAGE, new UnsignedWordElement(587), SCALE_FACTOR_2), // BAT_VOLTAGE: 421000 mV
				m(DeyeHybridInverter.ChannelId.BAT_SOC, new SignedWordElement(588)), // BAT_SOC: 63 %
				new DummyRegisterElement(589),
				m(DeyeHybridInverter.ChannelId.BAT_POWER, new SignedWordElement(590), SCALE_FACTOR_1), // BAT_POWER: 1200 W
				m(DeyeHybridInverter.ChannelId.BAT_CURRENT, new SignedWordElement(591), SCALE_FACTOR_1) // BAT_CURRENT: 2860 mA			 
//				m(DeyeHybridInverter.ChannelId.BAT_CAPACITY, new UnsignedWordElement(592)) // BAT_CAPACITY: 200 Ah (420 V * 200 Ah = 84 kWh, >> 20 kWh ?)
			)
		);	
		
		modbusProtocol.addTask(new FC3ReadRegistersTask(598, Priority.HIGH,
				m(DeyeHybridInverter.ChannelId.GRID_VOLTAGE_L1, new UnsignedWordElement(598), SCALE_FACTOR_2), // GRID_VOLTAGE_L1: 233900 mV
				m(DeyeHybridInverter.ChannelId.GRID_VOLTAGE_L2, new UnsignedWordElement(599), SCALE_FACTOR_2), // GRID_VOLTAGE_L2: 236100 mV
				m(DeyeHybridInverter.ChannelId.GRID_VOLTAGE_L3, new UnsignedWordElement(600), SCALE_FACTOR_2), // GRID_VOLTAGE_L3: 232400 mV
//				m(DeyeHybridInverter.ChannelId.GRID_VOLTAGE_L1_L2, new UnsignedWordElement(601), SCALE_FACTOR_MINUS_1), // GRID_VOLTAGE_L1_L2: 6516 V - ?
//				m(DeyeHybridInverter.ChannelId.GRID_VOLTAGE_L2_L3, new UnsignedWordElement(602), SCALE_FACTOR_MINUS_1), // GRID_VOLTAGE_L2_L3: 6552 V -?
//				m(DeyeHybridInverter.ChannelId.GRID_VOLTAGE_L3_L1, new UnsignedWordElement(603), SCALE_FACTOR_MINUS_1) // GRID_VOLTAGE_L3_L1: 0 V - ?
				new DummyRegisterElement(601, 603),
				m(DeyeHybridInverter.ChannelId.GRID_POWER_L1, new SignedWordElement(604)), // GRID_POWER_L1: -292 W 
				m(DeyeHybridInverter.ChannelId.GRID_POWER_L2, new SignedWordElement(605)), // GRID_POWER_L2: -314 W
				m(DeyeHybridInverter.ChannelId.GRID_POWER_L3, new SignedWordElement(606)), // GRID_POWER_L3: -309 W
				m(DeyeHybridInverter.ChannelId.GRID_POWER, new SignedWordElement(607)), // GRID_POWER: -915 W
				m(DeyeHybridInverter.ChannelId.GRID_REACTIVE_POWER, new SignedWordElement(608)), // GRID_APPARENT_POWER: 0 VA - reactive?
				m(DeyeHybridInverter.ChannelId.GRID_FREQUENCY, new UnsignedWordElement(609), SCALE_FACTOR_1), // GRID_FREQUENCY: 49950 mHz
				m(DeyeHybridInverter.ChannelId.GRID_CURRENT_L1, new SignedWordElement(610), SCALE_FACTOR_1), // GRID_CURRENT_L1: 1370 mA
				m(DeyeHybridInverter.ChannelId.GRID_CURRENT_L2, new SignedWordElement(611), SCALE_FACTOR_1), // GRID_CURRENT_L2: 1470 mA
				m(DeyeHybridInverter.ChannelId.GRID_CURRENT_L3, new SignedWordElement(612), SCALE_FACTOR_1), // GRID_CURRENT_L3: 1430 mA
				m(DeyeHybridInverter.ChannelId.GRID_EXT_CURRENT_L1, new SignedWordElement(613), SCALE_FACTOR_1), // GRID_EXT_CURRENT_L1: 10 mA - ?
				m(DeyeHybridInverter.ChannelId.GRID_EXT_CURRENT_L2, new SignedWordElement(614), SCALE_FACTOR_1), // GRID_EXT_CURRENT_L2: 50 mA - ?
				m(DeyeHybridInverter.ChannelId.GRID_EXT_CURRENT_L3, new SignedWordElement(615), SCALE_FACTOR_1), // GRID_EXT_CURRENT_L3: 30 mA - ?
				m(DeyeHybridInverter.ChannelId.GRID_EXT_POWER_L1, new SignedWordElement(616)), // GRID_EXT_POWER_L1: 1 W
				m(DeyeHybridInverter.ChannelId.GRID_EXT_POWER_L2, new SignedWordElement(617)), // GRID_EXT_POWER_L2: 2 W
				m(DeyeHybridInverter.ChannelId.GRID_EXT_POWER_L3, new SignedWordElement(618)), // GRID_EXT_POWER_L3: 1 W
				m(DeyeHybridInverter.ChannelId.GRID_EXT_POWER, new SignedWordElement(619)), // GRID_EXT_POWER: 4 W
				m(DeyeHybridInverter.ChannelId.GRID_EXT_REACTIVE_POWER, new SignedWordElement(620)) // GRID_EXT_APPARENT_POWER: 0 VA
//				m(DeyeHybridInverter.ChannelId.GRID_COS_PHI, new SignedWordElement(621), DIVIDE(1000)) // GRID_COS_PHI: -75.0
//				m(DeyeHybridInverter.ChannelId.GRID_COS_PHI, new SignedWordElement(621)) // GRID_COS_PHI: -75.0
				 
			)
		);			
		
//		modbusProtocol.addTask(new FC3ReadRegistersTask(621, Priority.HIGH,
//				m(DeyeHybridInverter.ChannelId.GRID_COS_PHI_INT, new SignedWordElement(621), SCALE_FACTOR_MINUS_2)
////				m(DeyeHybridInverter.ChannelId.GRID_COS_PHI_INT, new SignedWordElement(621), DIVIDE(1000)) 
//			)
//		);
		
		// GRID_POWER: -905 W, GRID_REACTIVE_POWER: 0 var, 
		// INV_OUT_POWER: 950 W, INV_OUT_REACTIVE_POWER: 950 var, 
		// UPS_LOAD_POWER: 45 W, LOAD_POWER: 45 W, LOAD_REACTIVE_POWER: 45 var
		modbusProtocol.addTask(new FC3ReadRegistersTask(636, Priority.HIGH,
				m(DeyeHybridInverter.ChannelId.INV_OUT_POWER, new SignedWordElement(636)),
				m(DeyeHybridInverter.ChannelId.INV_OUT_REACTIVE_POWER, new SignedWordElement(637)),
				new DummyRegisterElement(638, 642),
				m(DeyeHybridInverter.ChannelId.UPS_LOAD_POWER, new UnsignedWordElement(643)),
//				m(DeyeHybridInverter.ChannelId.UPS_LOAD_POWER, new SignedWordElement(644)),
				new DummyRegisterElement(644, 652),
				m(DeyeHybridInverter.ChannelId.LOAD_POWER, new SignedWordElement(653)),
				m(DeyeHybridInverter.ChannelId.LOAD_REACTIVE_POWER, new SignedWordElement(654)) 
				)
		);

		
		
		modbusProtocol.addTask(new FC3ReadRegistersTask(672, Priority.HIGH,
//				m(DeyeHybridInverter.ChannelId.POWER_PV, new UnsignedWordElement(671)), // TODO: calculate the sum
				m(DeyeHybridInverter.ChannelId.POWER_PV1, new UnsignedWordElement(672), SCALE_FACTOR_1), // in doc mistake, unit is 1W, POWER_PV1: 60 W
				m(DeyeHybridInverter.ChannelId.POWER_PV2, new UnsignedWordElement(673), SCALE_FACTOR_1), // POWER_PV2: 0 W
				m(DeyeHybridInverter.ChannelId.POWER_PV3, new UnsignedWordElement(674), SCALE_FACTOR_1), // POWER_PV3: 0 W
				m(DeyeHybridInverter.ChannelId.POWER_PV4, new UnsignedWordElement(675), SCALE_FACTOR_1), // POWER_PV4: 0 W 
				m(DeyeHybridInverter.ChannelId.VOLTAGE_PV1, new UnsignedWordElement(676), SCALE_FACTOR_2), // VOLTAGE_PV1: 574100 mV
				m(DeyeHybridInverter.ChannelId.CURRENT_PV1, new UnsignedWordElement(677), SCALE_FACTOR_2), // CURRENT_PV1: 1000 mA
				m(DeyeHybridInverter.ChannelId.VOLTAGE_PV2, new UnsignedWordElement(678), SCALE_FACTOR_2), //
				m(DeyeHybridInverter.ChannelId.CURRENT_PV2, new UnsignedWordElement(679), SCALE_FACTOR_2), //
				m(DeyeHybridInverter.ChannelId.VOLTAGE_PV3, new UnsignedWordElement(680), SCALE_FACTOR_2), //
				m(DeyeHybridInverter.ChannelId.CURRENT_PV3, new UnsignedWordElement(681), SCALE_FACTOR_2), //
				m(DeyeHybridInverter.ChannelId.VOLTAGE_PV4, new UnsignedWordElement(682), SCALE_FACTOR_2), //
				m(DeyeHybridInverter.ChannelId.CURRENT_PV4, new UnsignedWordElement(683), SCALE_FACTOR_2) //				
			)
		);		
						
		return modbusProtocol;
	}
	
	@Override
	public String debugLog() {
		return "\n\tid: " + this.getUnitId()
//				+ ", L:" //+ this.getActivePower().asString() //				
//				+ ", INVERTER_RUN_STATE: " + this.channel(DeyeHybridInverter.ChannelId.INVERTER_RUN_STATE).value().asString()
//				+ ", ACTIVE_ENERGY_GEN_TODAY: " + this.channel(DeyeHybridInverter.ChannelId.ACTIVE_ENERGY_GEN_TODAY).value().asString()
//				+ ", REACTIVE_ENERGY_GEN_TODAY: " + this.channel(DeyeHybridInverter.ChannelId.REACTIVE_ENERGY_GEN_TODAY).value().asString()
//				+ ", BAT_CHARGE_TODAY: " + this.channel(DeyeHybridInverter.ChannelId.BAT_CHARGE_TODAY).value().asString()
//				+ ", BAT_DISCHARGE_TODAY: " + this.channel(DeyeHybridInverter.ChannelId.BAT_DISCHARGE_TODAY).value().asString()
//				+ ", E_GRID_SELL_TODAY: " + this.channel(DeyeHybridInverter.ChannelId.E_GRID_SELL_TODAY).value().asString()
//				+ ", E_GRID_BUY_TODAY: " + this.channel(DeyeHybridInverter.ChannelId.E_GRID_BUY_TODAY).value().asString()
//				+ ", E_LOAD_TODAY: " + this.channel(DeyeHybridInverter.ChannelId.E_LOAD_TODAY).value().asString()
//				+ ", E_PV_TODAY: " + this.channel(DeyeHybridInverter.ChannelId.E_PV_TODAY).value().asString()
//				+ ", E_PV1_TODAY: " + this.channel(DeyeHybridInverter.ChannelId.E_PV1_TODAY).value().asString()
//				+ ", E_PV2_TODAY: " + this.channel(DeyeHybridInverter.ChannelId.E_PV2_TODAY).value().asString()
//				+ ", E_PV3_TODAY: " + this.channel(DeyeHybridInverter.ChannelId.E_PV3_TODAY).value().asString()
//				+ ", E_PV4_TODAY: " + this.channel(DeyeHybridInverter.ChannelId.E_PV4_TODAY).value().asString()				
//				+ ", BAT_TEMP: " + this.channel(DeyeHybridInverter.ChannelId.BAT_TEMP).value().asString()
//				+ ", BAT_VOLTAGE: " + this.channel(DeyeHybridInverter.ChannelId.BAT_VOLTAGE).value().asString()
				+ ", BAT_SOC: " + this.channel(DeyeHybridInverter.ChannelId.BAT_SOC).value().asString()
//				+ ", BAT_POWER: " + this.channel(DeyeHybridInverter.ChannelId.BAT_POWER).value().asString()
//				+ ", BAT_CURRENT: " + this.channel(DeyeHybridInverter.ChannelId.BAT_CURRENT).value().asString()
//				+ ", BAT_CAPACITY: " + this.channel(DeyeHybridInverter.ChannelId.BAT_CAPACITY).value().asString()				
//				+ ", POWER_PV: " + this.channel(DeyeHybridInverter.ChannelId.POWER_PV).value().asString()
				+ ", POWER_PV1: " + this.channel(DeyeHybridInverter.ChannelId.POWER_PV1).value().asString() 
				+ ", POWER_PV2: " + this.channel(DeyeHybridInverter.ChannelId.POWER_PV2).value().asString()
//				+ ", POWER_PV3: " + this.channel(DeyeHybridInverter.ChannelId.POWER_PV3).value().asString()
//				+ ", POWER_PV4: " + this.channel(DeyeHybridInverter.ChannelId.POWER_PV4).value().asString()
//				+ ", VOLTAGE_PV1: " + this.channel(DeyeHybridInverter.ChannelId.VOLTAGE_PV1).value().asString()
//				+ ", CURRENT_PV1: " + this.channel(DeyeHybridInverter.ChannelId.CURRENT_PV1).value().asString()
//				+ ", DC_TRANSFORMER_TEMP: " + this.channel(DeyeHybridInverter.ChannelId.DC_TRANSFORMER_TEMP).value().asString()
//				+ ", HEAT_SINK_TEMP: " + this.channel(DeyeHybridInverter.ChannelId.HEAT_SINK_TEMP).value().asString()
//				+ ", GRID_VOLTAGE_L1: " + this.channel(DeyeHybridInverter.ChannelId.GRID_VOLTAGE_L1).value().asString()
//				+ ", GRID_VOLTAGE_L2: " + this.channel(DeyeHybridInverter.ChannelId.GRID_VOLTAGE_L2).value().asString()
//				+ ", GRID_VOLTAGE_L3: " + this.channel(DeyeHybridInverter.ChannelId.GRID_VOLTAGE_L3).value().asString()
//				+ ", GRID_VOLTAGE_L1_L2: " + this.channel(DeyeHybridInverter.ChannelId.GRID_VOLTAGE_L1_L2).value().asString()
//				+ ", GRID_VOLTAGE_L2_L3: " + this.channel(DeyeHybridInverter.ChannelId.GRID_VOLTAGE_L2_L3).value().asString()
//				+ ", GRID_VOLTAGE_L3_L1: " + this.channel(DeyeHybridInverter.ChannelId.GRID_VOLTAGE_L3_L1).value().asString()
				+ ", GRID_POWER_L1: " + this.channel(DeyeHybridInverter.ChannelId.GRID_POWER_L1).value().asString()
				+ ", GRID_POWER_L2: " + this.channel(DeyeHybridInverter.ChannelId.GRID_POWER_L2).value().asString()
				+ ", GRID_POWER_L3: " + this.channel(DeyeHybridInverter.ChannelId.GRID_POWER_L3).value().asString()
				+ ", GRID_POWER: " + this.channel(DeyeHybridInverter.ChannelId.GRID_POWER).value().asString()
				+ ", GRID_REACTIVE_POWER: " + this.channel(DeyeHybridInverter.ChannelId.GRID_REACTIVE_POWER).value().asString()
//				+ ", GRID_FREQUENCY: " + this.channel(DeyeHybridInverter.ChannelId.GRID_FREQUENCY).value().asString()
//				+ ", GRID_CURRENT_L1: " + this.channel(DeyeHybridInverter.ChannelId.GRID_CURRENT_L1).value().asString()
//				+ ", GRID_CURRENT_L2: " + this.channel(DeyeHybridInverter.ChannelId.GRID_CURRENT_L2).value().asString()
//				+ ", GRID_CURRENT_L3: " + this.channel(DeyeHybridInverter.ChannelId.GRID_CURRENT_L3).value().asString()
//				+ ", GRID_EXT_CURRENT_L1: " + this.channel(DeyeHybridInverter.ChannelId.GRID_EXT_CURRENT_L1).value().asString()
//				+ ", GRID_EXT_CURRENT_L2: " + this.channel(DeyeHybridInverter.ChannelId.GRID_EXT_CURRENT_L2).value().asString()
//				+ ", GRID_EXT_CURRENT_L3: " + this.channel(DeyeHybridInverter.ChannelId.GRID_EXT_CURRENT_L3).value().asString()
//				+ ", GRID_EXT_POWER_L1: " + this.channel(DeyeHybridInverter.ChannelId.GRID_EXT_POWER_L1).value().asString()
//				+ ", GRID_EXT_POWER_L2: " + this.channel(DeyeHybridInverter.ChannelId.GRID_EXT_POWER_L2).value().asString()
//				+ ", GRID_EXT_POWER_L3: " + this.channel(DeyeHybridInverter.ChannelId.GRID_EXT_POWER_L3).value().asString()
//				+ ", GRID_EXT_POWER: " + this.channel(DeyeHybridInverter.ChannelId.GRID_EXT_POWER).value().asString()
//				+ ", GRID_EXT_REACTIVE_POWER: " + this.channel(DeyeHybridInverter.ChannelId.GRID_EXT_REACTIVE_POWER).value().asString()
//				+ ", GRID_COS_PHI: " + this.channel(DeyeHybridInverter.ChannelId.GRID_COS_PHI).value().asString()
//				+ ", GRID_COS_PHI_INT: " + this.channel(DeyeHybridInverter.ChannelId.GRID_COS_PHI_INT).value().asString()				
				+ ", INV_OUT_POWER: " + this.channel(DeyeHybridInverter.ChannelId.INV_OUT_POWER).value().asString()
				+ ", INV_OUT_REACTIVE_POWER: " + this.channel(DeyeHybridInverter.ChannelId.INV_OUT_REACTIVE_POWER).value().asString()
				+ ", UPS_LOAD_POWER: " + this.channel(DeyeHybridInverter.ChannelId.UPS_LOAD_POWER).value().asString()
				+ ", LOAD_POWER: " + this.channel(DeyeHybridInverter.ChannelId.LOAD_POWER).value().asString()
				+ ", LOAD_REACTIVE_POWER: " + this.channel(DeyeHybridInverter.ChannelId.LOAD_REACTIVE_POWER).value().asString()
				
				
				;

	}
	
	
	@Override
	public Timedata getTimedata() {
		return this.timedata;
	}
}
