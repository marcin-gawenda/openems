package io.openems.edge.aohai.hybrid;

import static io.openems.edge.bridge.modbus.api.ElementToChannelConverter.INVERT_IF_TRUE;
import static io.openems.edge.bridge.modbus.api.ElementToChannelConverter.SUBTRACT;
import static io.openems.edge.bridge.modbus.api.ElementToChannelConverter.DIVIDE;
import static io.openems.edge.bridge.modbus.api.ElementToChannelConverter.MULTIPLY;
import static io.openems.edge.bridge.modbus.api.ElementToChannelConverter.SCALE_FACTOR_1;
import static io.openems.edge.bridge.modbus.api.ElementToChannelConverter.SCALE_FACTOR_2;
import static io.openems.edge.bridge.modbus.api.ElementToChannelConverter.SCALE_FACTOR_3;
import static io.openems.edge.bridge.modbus.api.ElementToChannelConverter.SCALE_FACTOR_MINUS_1;
import static io.openems.edge.bridge.modbus.api.ElementToChannelConverter.SCALE_FACTOR_MINUS_2;
import static io.openems.edge.bridge.modbus.api.ElementToChannelConverter.SCALE_FACTOR_MINUS_3;
import static io.openems.edge.bridge.modbus.api.ElementToChannelConverter.SCALE_FACTOR_MINUS_4_FLOAT;
import static io.openems.edge.bridge.modbus.api.ElementToChannelConverter.chain;

import java.util.function.Consumer;
import java.util.function.Function;

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
import io.openems.edge.bridge.modbus.api.ElementToChannelConverter;
import io.openems.edge.bridge.modbus.api.ModbusComponent;
import io.openems.edge.bridge.modbus.api.ModbusProtocol;
import io.openems.edge.common.channel.Doc;
import io.openems.edge.common.component.OpenemsComponent;
import io.openems.edge.meter.api.ElectricityMeter;
import io.openems.edge.bridge.modbus.api.element.DummyRegisterElement;
import io.openems.edge.bridge.modbus.api.element.FloatDoublewordElement;
import io.openems.edge.bridge.modbus.api.element.SignedDoublewordElement;
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
		name = "io.openems.edge.aohai.hybrid", //
		immediate = true, //
		configurationPolicy = ConfigurationPolicy.REQUIRE //
)
public class AohaiHybridInverterImpl extends AbstractOpenemsModbusComponent implements AohaiHybridInverter, ElectricityMeter, TimedataProvider, ModbusComponent, OpenemsComponent {
	
	@Reference
	private ConfigurationAdmin cm;

	
	@Reference(policy = ReferencePolicy.DYNAMIC, policyOption = ReferencePolicyOption.GREEDY, cardinality = ReferenceCardinality.OPTIONAL)
	private volatile Timedata timedata;
	
	@Reference(policy = ReferencePolicy.STATIC, policyOption = ReferencePolicyOption.GREEDY, cardinality = ReferenceCardinality.MANDATORY)
	protected void setModbus(BridgeModbus modbus) {
		super.setModbus(modbus);
	}

	private MeterType meterType = MeterType.GRID;

	public AohaiHybridInverterImpl() {
		super(//
				OpenemsComponent.ChannelId.values(), //
				ModbusComponent.ChannelId.values(), //
				ElectricityMeter.ChannelId.values(), //	
				AohaiHybridInverter.ChannelId.values() //
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
				new FC4ReadInputRegistersTask(0, Priority.HIGH,
						m(AohaiHybridInverter.ChannelId.INVERTER_RUN_STATE, new UnsignedWordElement(0)), // INVERTER_RUN_STATE: 1 (Grid-connected)
						new DummyRegisterElement(1,1),
						m(AohaiHybridInverter.ChannelId.INV_VOLTAGE_L1, new UnsignedWordElement(2), SCALE_FACTOR_2), // INV_VOLTAGE_L1: 231100 mV
						m(AohaiHybridInverter.ChannelId.INV_VOLTAGE_L2, new UnsignedWordElement(3), SCALE_FACTOR_2), // INV_VOLTAGE_L2: 234500 mV						
						m(AohaiHybridInverter.ChannelId.INV_VOLTAGE_L3, new UnsignedWordElement(4), SCALE_FACTOR_2), // INV_VOLTAGE_L3: 236200 mV
						m(AohaiHybridInverter.ChannelId.INV_CURRENT_L1, new SignedWordElement(5), SCALE_FACTOR_2), // INV_CURRENT_L1: 700 mA
						m(AohaiHybridInverter.ChannelId.INV_CURRENT_L2, new SignedWordElement(6), SCALE_FACTOR_2), // INV_CURRENT_L2: -700 mA						
						m(AohaiHybridInverter.ChannelId.INV_CURRENT_L3, new SignedWordElement(7), SCALE_FACTOR_2), // INV_CURRENT_L3: 700 mA
						new DummyRegisterElement(8,37),
						// TODO MARCIN
						m(AohaiHybridInverter.ChannelId.DERATING_MODE_FLAG, new UnsignedWordElement(38)), // DERATING_MODE_FLAG: 0
						new DummyRegisterElement(39,39),
						new DummyRegisterElement(40,41),
//						m(AohaiHybridInverter.ChannelId.BUS_VOLTAGE_POSITIVE, new UnsignedWordElement(40), SCALE_FACTOR_2), // 0
//						m(AohaiHybridInverter.ChannelId.BUS_VOLTAGE_NEGATIVE, new UnsignedWordElement(41), SCALE_FACTOR_2), // 0				
						m(AohaiHybridInverter.ChannelId.GRID_VOLTAGE_L1, new UnsignedWordElement(42), SCALE_FACTOR_2), // GRID_VOLTAGE_L1: 233900 mV
						m(AohaiHybridInverter.ChannelId.GRID_CURRENT_L1, new SignedWordElement(43), SCALE_FACTOR_2), // GRID_CURRENT_L1: 1370 mA				
						m(AohaiHybridInverter.ChannelId.GRID_VOLTAGE_L2, new UnsignedWordElement(44), SCALE_FACTOR_2), // GRID_VOLTAGE_L2: 236100 mV
						m(AohaiHybridInverter.ChannelId.GRID_CURRENT_L2, new SignedWordElement(45), SCALE_FACTOR_2), // GRID_CURRENT_L2: 1470 mA
						m(AohaiHybridInverter.ChannelId.GRID_VOLTAGE_L3, new UnsignedWordElement(46), SCALE_FACTOR_2), // GRID_VOLTAGE_L3: 232400 mV
						m(AohaiHybridInverter.ChannelId.GRID_CURRENT_L3, new SignedWordElement(47), SCALE_FACTOR_2), // GRID_CURRENT_L3: 1430 mA
						new DummyRegisterElement(48,50),
//						m(AohaiHybridInverter.ChannelId.GRID_VOLTAGE_L1_L2, new UnsignedWordElement(48), SCALE_FACTOR_2), // GRID_VOLTAGE_L1_L2: 0 V - ?
//						m(AohaiHybridInverter.ChannelId.GRID_VOLTAGE_L2_L3, new UnsignedWordElement(49), SCALE_FACTOR_2), // GRID_VOLTAGE_L2_L3: 0 V - ?
//						m(AohaiHybridInverter.ChannelId.GRID_VOLTAGE_L3_L1, new UnsignedWordElement(50), SCALE_FACTOR_2), // GRID_VOLTAGE_L3_L1: 0 V - ?
						m(AohaiHybridInverter.ChannelId.GRID_FREQUENCY, new UnsignedWordElement(51), SCALE_FACTOR_1), // GRID_FREQUENCY: 49950 mHz
						m(AohaiHybridInverter.ChannelId.GRID_COS_PHI, new SignedWordElement(52), SCALE_FACTOR_MINUS_4_FLOAT ), // GRID_COS_PHI: 1.0
//						m(AohaiHybridInverter.ChannelId.GRID_COS_PHI_1, new SignedWordElement(52), SCALE_FACTOR_MINUS_2), // GRID_COS_PHI: 1.0
//						m(AohaiHybridInverter.ChannelId.GRID_COS_PHI, new SignedWordElement(52)) // GRID_COS_PHI: 1.0
//						m(AohaiHybridInverter.ChannelId.GRID_COS_PHI_INT, new SignedWordElement(52), MULTIPLY(0.0001d)) // GRID_COS_PHI: 1.0
						//SCALE_FACTOR_MINUS_3
//						m(AohaiHybridInverter.ChannelId.GRID_COS_PHI, new SignedWordElement(52), DIVIDE(10000)) // GRID_COS_PHI: 1.0				
						new DummyRegisterElement(53,53), // RealOPPercent: 0				
						m(AohaiHybridInverter.ChannelId.EPS_FREQUENCY, new UnsignedWordElement(54), SCALE_FACTOR_1), // EPS_FREQUENCY: 49950 mHz
						m(AohaiHybridInverter.ChannelId.EPS_VOLTAGE_L1, new UnsignedWordElement(55), SCALE_FACTOR_2), // EPS_VOLTAGE_L1: 233900 mV
						m(AohaiHybridInverter.ChannelId.EPS_CURRENT_L1, new UnsignedWordElement(56), SCALE_FACTOR_2), // EPS_CURRENT_L1: 1370 mA				
						m(AohaiHybridInverter.ChannelId.EPS_VOLTAGE_L2, new UnsignedWordElement(57), SCALE_FACTOR_2), // EPS_VOLTAGE_L2: 236100 mV
						m(AohaiHybridInverter.ChannelId.EPS_CURRENT_L2, new SignedWordElement(58), SCALE_FACTOR_2), // EPS_CURRENT_L2: 1470 mA
						m(AohaiHybridInverter.ChannelId.EPS_VOLTAGE_L3, new UnsignedWordElement(59), SCALE_FACTOR_2), // EPS_VOLTAGE_L3: 232400 mV
						m(AohaiHybridInverter.ChannelId.EPS_CURRENT_L3, new SignedWordElement(60), SCALE_FACTOR_2), // EPS_CURRENT_L3: 1430 mA
						new DummyRegisterElement(61,62), 				
						m(AohaiHybridInverter.ChannelId.MPPT_CNT, new UnsignedWordElement(63)), // Number of PV paths: 2
						m(AohaiHybridInverter.ChannelId.VOLTAGE_PV1, new UnsignedWordElement(64), SCALE_FACTOR_2), // VOLTAGE_PV1: 574100 mV
						m(AohaiHybridInverter.ChannelId.CURRENT_PV1, new UnsignedWordElement(65), SCALE_FACTOR_2), // CURRENT_PV1: 1000 mA
						m(AohaiHybridInverter.ChannelId.VOLTAGE_PV2, new UnsignedWordElement(66), SCALE_FACTOR_2), //
						m(AohaiHybridInverter.ChannelId.CURRENT_PV2, new UnsignedWordElement(67), SCALE_FACTOR_2) //				
					)
				);
		
		modbusProtocol.addTask(new FC4ReadInputRegistersTask(125, Priority.HIGH,
				m(AohaiHybridInverter.ChannelId.INV_PRIORITY, new UnsignedWordElement(125)), // 
				m(AohaiHybridInverter.ChannelId.BAT_TYPE, new UnsignedWordElement(126)), // offset 1000, BAT_TEMP: 160 dC 
				m(AohaiHybridInverter.ChannelId.BAT_VOLTAGE, new UnsignedWordElement(127), SCALE_FACTOR_2), // BAT_VOLTAGE: 421100 mV
				m(AohaiHybridInverter.ChannelId.BAT_SOC, new UnsignedWordElement(128)), // BAT_SOC: 63 %
				m(AohaiHybridInverter.ChannelId.BAT_VOLTAGE_DSP, new UnsignedWordElement(129), SCALE_FACTOR_2), // BAT_VOLTAGE: 418100 mV
				m(AohaiHybridInverter.ChannelId.BMS_STATUS, new UnsignedWordElement(130)), // BMS_STATUS: 2
				new DummyRegisterElement(131,140),
				m(AohaiHybridInverter.ChannelId.BAT_CURRENT, new SignedWordElement(141), SCALE_FACTOR_1), // BAT_CURRENT: 2860 mA
				m(AohaiHybridInverter.ChannelId.BAT_TEMP, new SignedWordElement(142)), // offset 1000, BAT_TEMP: 313 dC
				new DummyRegisterElement(143,150),
				m(AohaiHybridInverter.ChannelId.BAT_CYCLE_CNT, new UnsignedWordElement(151)), // BAT_SOC: 63 %
				m(AohaiHybridInverter.ChannelId.BAT_SOH, new UnsignedWordElement(152)) // BAT_SOC: 63 %				
			)
		);	

		// 314 always 0
		// 318 always 0
		// 320 always 0
		
		modbusProtocol.addTask(new FC4ReadInputRegistersTask(250, Priority.HIGH,
				m(AohaiHybridInverter.ChannelId.POWER_PV, new UnsignedDoublewordElement(250), SCALE_FACTOR_MINUS_1), // 
				m(AohaiHybridInverter.ChannelId.POWER_PV1, new UnsignedDoublewordElement(252), SCALE_FACTOR_MINUS_1), // in doc mistake, unit is 1W, POWER_PV1: 60 W
				m(AohaiHybridInverter.ChannelId.POWER_PV2, new UnsignedDoublewordElement(254), SCALE_FACTOR_MINUS_1), // POWER_PV2: 0 W
//				m(AohaiHybridInverter.ChannelId.POWER_PV3, new UnsignedDoublewordElement(256), SCALE_FACTOR_MINUS_1), // POWER_PV3: 0 W
//				m(AohaiHybridInverter.ChannelId.POWER_PV4, new UnsignedDoublewordElement(258), SCALE_FACTOR_MINUS_1) // POWER_PV4: 0 W 
				new DummyRegisterElement(256,283),
				m(AohaiHybridInverter.ChannelId.INV_APPARENT_POWER, new UnsignedDoublewordElement(284), SCALE_FACTOR_MINUS_1), // INV_APPARENT_POWER: 96 VA ?
				m(AohaiHybridInverter.ChannelId.INV_POWER, new SignedDoublewordElement(286), SCALE_FACTOR_MINUS_1), // INV_POWER: -52 W <> SUM ?
				m(AohaiHybridInverter.ChannelId.INV_REACTIVE_POWER, new SignedDoublewordElement(288), SCALE_FACTOR_MINUS_1), // INV_REACTIVE_POWER: -10 var ?
				m(AohaiHybridInverter.ChannelId.INV_APPARENT_POWER_L1, new UnsignedDoublewordElement(290), SCALE_FACTOR_MINUS_1), // 0 ?
				m(AohaiHybridInverter.ChannelId.INV_POWER_L1, new SignedDoublewordElement(292), SCALE_FACTOR_MINUS_1), // INV_POWER_L1: -14 W
				m(AohaiHybridInverter.ChannelId.INV_REACTIVE_POWER_L1, new SignedDoublewordElement(294), SCALE_FACTOR_MINUS_1), // 0 ?
				m(AohaiHybridInverter.ChannelId.INV_APPARENT_POWER_L2, new UnsignedDoublewordElement(296), SCALE_FACTOR_MINUS_1), // 0 ?			
				m(AohaiHybridInverter.ChannelId.INV_POWER_L2, new SignedDoublewordElement(298), SCALE_FACTOR_MINUS_1), // INV_POWER_L2: -30 W
				m(AohaiHybridInverter.ChannelId.INV_REACTIVE_POWER_L2, new SignedDoublewordElement(300), SCALE_FACTOR_MINUS_1), // 0 ?
				m(AohaiHybridInverter.ChannelId.INV_APPARENT_POWER_L3, new UnsignedDoublewordElement(302), SCALE_FACTOR_MINUS_1), // 0 ?
				m(AohaiHybridInverter.ChannelId.INV_POWER_L3, new SignedDoublewordElement(304), SCALE_FACTOR_MINUS_1), // INV_POWER_L3: -18 W
				m(AohaiHybridInverter.ChannelId.INV_REACTIVE_POWER_L3, new SignedDoublewordElement(306), SCALE_FACTOR_MINUS_1), // 0 ?
				new DummyRegisterElement(308,321),
				m(AohaiHybridInverter.ChannelId.GRID_POWER_L1, new UnsignedDoublewordElement(322), SCALE_FACTOR_MINUS_1),
				m(AohaiHybridInverter.ChannelId.GRID_POWER_L2, new UnsignedDoublewordElement(324), SCALE_FACTOR_MINUS_1),
				m(AohaiHybridInverter.ChannelId.GRID_POWER_L3, new UnsignedDoublewordElement(326), SCALE_FACTOR_MINUS_1),
				m(AohaiHybridInverter.ChannelId.GRID_POWER, new UnsignedDoublewordElement(328), SCALE_FACTOR_MINUS_1),
				new DummyRegisterElement(330,331), 
				m(AohaiHybridInverter.ChannelId.EPS_APPARENT_POWER_L1, new UnsignedDoublewordElement(332), SCALE_FACTOR_MINUS_1),
				m(AohaiHybridInverter.ChannelId.EPS_POWER_L1, new UnsignedDoublewordElement(334), SCALE_FACTOR_MINUS_1),
				m(AohaiHybridInverter.ChannelId.EPS_APPARENT_POWER_L2, new UnsignedDoublewordElement(336), SCALE_FACTOR_MINUS_1),
				m(AohaiHybridInverter.ChannelId.EPS_POWER_L2, new UnsignedDoublewordElement(338), SCALE_FACTOR_MINUS_1),
				m(AohaiHybridInverter.ChannelId.EPS_APPARENT_POWER_L3, new UnsignedDoublewordElement(340), SCALE_FACTOR_MINUS_1),
				m(AohaiHybridInverter.ChannelId.EPS_POWER_L3, new UnsignedDoublewordElement(342), SCALE_FACTOR_MINUS_1),
				new DummyRegisterElement(344,348),
				m(AohaiHybridInverter.ChannelId.BAT_DISCHARGE_POWER, new UnsignedDoublewordElement(349), SCALE_FACTOR_MINUS_1),
				m(AohaiHybridInverter.ChannelId.BAT_CHARGE_POWER, new UnsignedDoublewordElement(351), SCALE_FACTOR_MINUS_1),
				m(AohaiHybridInverter.ChannelId.BAT_AC_CHARGE_POWER, new UnsignedDoublewordElement(353), SCALE_FACTOR_MINUS_1)
			)
		);
		
		// 344 always 0
		// 355 always 0
		
		modbusProtocol.addTask(new FC4ReadInputRegistersTask(379, Priority.HIGH,
				m(AohaiHybridInverter.ChannelId.E_PV_TODAY, new UnsignedDoublewordElement(379), SCALE_FACTOR_2), // E_PV_TODAY: 17800 Wh
				m(AohaiHybridInverter.ChannelId.E_PV_TOTAL, new UnsignedDoublewordElement(381), SCALE_FACTOR_2), // E_PV_TOTAL: 723600 Wh 
				m(AohaiHybridInverter.ChannelId.BAT_CHARGE_TODAY, new UnsignedDoublewordElement(383), SCALE_FACTOR_2), // BAT_CHARGE_TODAY: 19100 Wh
				m(AohaiHybridInverter.ChannelId.BAT_CHARGE_TOTAL, new UnsignedDoublewordElement(385), SCALE_FACTOR_2),
				m(AohaiHybridInverter.ChannelId.BAT_DISCHARGE_TODAY, new UnsignedDoublewordElement(387), SCALE_FACTOR_2), // BAT_DISCHARGE_TODAY: 28800 Wh
				m(AohaiHybridInverter.ChannelId.BAT_DISCHARGE_TOTAL, new UnsignedDoublewordElement(389), SCALE_FACTOR_2), 
				m(AohaiHybridInverter.ChannelId.BAT_AC_CHARGE_TODAY, new UnsignedDoublewordElement(391), SCALE_FACTOR_2), // BAT_CHARGE_TODAY: 19100 Wh
				m(AohaiHybridInverter.ChannelId.BAT_AC_CHARGE_TOTAL, new UnsignedDoublewordElement(393), SCALE_FACTOR_2),
				new DummyRegisterElement(395,412),
				m(AohaiHybridInverter.ChannelId.E_GRID_SELL_TOTAL, new UnsignedDoublewordElement(413), SCALE_FACTOR_2), //
				new DummyRegisterElement(415,416),
				m(AohaiHybridInverter.ChannelId.E_GRID_BUY_TOTAL, new UnsignedDoublewordElement(417), SCALE_FACTOR_2) //	
			)
		);
		
//		var rUInt = 52;
//		modbusProtocol.addTask(new FC4ReadInputRegistersTask(rUInt, Priority.HIGH,
//				m(AohaiHybridInverter.ChannelId.TEST_U_INT, new SignedWordElement(rUInt)) //
//			)
//		);	
	
//		var rDouble = 52;
//		modbusProtocol.addTask(new FC4ReadInputRegistersTask(rDouble, Priority.HIGH,
//				m(AohaiHybridInverter.ChannelId.TEST_DOUBLE, new SignedWordElement(rDouble), DIVIDE(10000.0)) //
//			)
//		);
		
		// 375 89
		// zeros: 411, 415
//		var rLong = 429;
//		modbusProtocol.addTask(new FC4ReadInputRegistersTask(rLong, Priority.HIGH,
//				m(AohaiHybridInverter.ChannelId.TEST_LONG, new UnsignedDoublewordElement(rLong), SCALE_FACTOR_2) //
//			)
//		);
				
		return modbusProtocol;
	}
	
	@Override
	public String debugLog() {
		return "\n\tid: " + this.getUnitId()		
//				+ ", L:" //+ this.getActivePower().asString() //				
				+ ", INVERTER_RUN_STATE: " + this.channel(AohaiHybridInverter.ChannelId.INVERTER_RUN_STATE).value().asString()
//				+ ", INV_VOLTAGE_L1: " + this.channel(AohaiHybridInverter.ChannelId.INV_VOLTAGE_L1).value().asString()
//				+ ", INV_VOLTAGE_L2: " + this.channel(AohaiHybridInverter.ChannelId.INV_VOLTAGE_L2).value().asString()
//				+ ", INV_VOLTAGE_L3: " + this.channel(AohaiHybridInverter.ChannelId.INV_VOLTAGE_L3).value().asString()
//				+ ", INV_CURRENT_L1: " + this.channel(AohaiHybridInverter.ChannelId.INV_CURRENT_L1).value().asString()
//				+ ", INV_CURRENT_L2: " + this.channel(AohaiHybridInverter.ChannelId.INV_CURRENT_L2).value().asString()
//				+ ", INV_CURRENT_L3: " + this.channel(AohaiHybridInverter.ChannelId.INV_CURRENT_L3).value().asString()
				+ ", DERATING_MODE_FLAG: " + this.channel(AohaiHybridInverter.ChannelId.DERATING_MODE_FLAG).value().asString()
//				+ ", BUS_VOLTAGE_POSITIVE: " + this.channel(AohaiHybridInverter.ChannelId.BUS_VOLTAGE_POSITIVE).value().asString()
//				+ ", BUS_VOLTAGE_NEGATIVE: " + this.channel(AohaiHybridInverter.ChannelId.BUS_VOLTAGE_NEGATIVE).value().asString()				
//				+ ", GRID_VOLTAGE_L1: " + this.channel(AohaiHybridInverter.ChannelId.GRID_VOLTAGE_L1).value().asString()
//				+ ", GRID_VOLTAGE_L2: " + this.channel(AohaiHybridInverter.ChannelId.GRID_VOLTAGE_L2).value().asString()
//				+ ", GRID_VOLTAGE_L3: " + this.channel(AohaiHybridInverter.ChannelId.GRID_VOLTAGE_L3).value().asString()
//				+ ", GRID_VOLTAGE_L1_L2: " + this.channel(AohaiHybridInverter.ChannelId.GRID_VOLTAGE_L1_L2).value().asString()
//				+ ", GRID_VOLTAGE_L2_L3: " + this.channel(AohaiHybridInverter.ChannelId.GRID_VOLTAGE_L2_L3).value().asString()
//				+ ", GRID_VOLTAGE_L3_L1: " + this.channel(AohaiHybridInverter.ChannelId.GRID_VOLTAGE_L3_L1).value().asString()
//				+ ", GRID_CURRENT_L1: " + this.channel(AohaiHybridInverter.ChannelId.GRID_CURRENT_L1).value().asString()
//				+ ", GRID_CURRENT_L2: " + this.channel(AohaiHybridInverter.ChannelId.GRID_CURRENT_L2).value().asString()
//				+ ", GRID_CURRENT_L3: " + this.channel(AohaiHybridInverter.ChannelId.GRID_CURRENT_L3).value().asString()
//				+ ", GRID_FREQUENCY: " + this.channel(AohaiHybridInverter.ChannelId.GRID_FREQUENCY).value().asString()
				+ ", GRID_COS_PHI: " + this.channel(AohaiHybridInverter.ChannelId.GRID_COS_PHI).value().asString()
//				+ ", EPS_FREQUENCY: " + this.channel(AohaiHybridInverter.ChannelId.EPS_FREQUENCY).value().asString()				
//				+ ", EPS_VOLTAGE_L1: " + this.channel(AohaiHybridInverter.ChannelId.EPS_VOLTAGE_L1).value().asString()
//				+ ", EPS_VOLTAGE_L2: " + this.channel(AohaiHybridInverter.ChannelId.EPS_VOLTAGE_L2).value().asString()
//				+ ", EPS_VOLTAGE_L3: " + this.channel(AohaiHybridInverter.ChannelId.EPS_VOLTAGE_L3).value().asString()
//				+ ", EPS_CURRENT_L1: " + this.channel(AohaiHybridInverter.ChannelId.EPS_CURRENT_L1).value().asString()
//				+ ", EPS_CURRENT_L2: " + this.channel(AohaiHybridInverter.ChannelId.EPS_CURRENT_L2).value().asString()
//				+ ", EPS_CURRENT_L3: " + this.channel(AohaiHybridInverter.ChannelId.EPS_CURRENT_L3).value().asString()
//				+ ", MPPT_CNT: " + this.channel(AohaiHybridInverter.ChannelId.MPPT_CNT).value().asString()
//				+ ", VOLTAGE_PV1: " + this.channel(AohaiHybridInverter.ChannelId.VOLTAGE_PV1).value().asString()
//				+ ", CURRENT_PV1: " + this.channel(AohaiHybridInverter.ChannelId.CURRENT_PV1).value().asString()
//				+ ", VOLTAGE_PV2: " + this.channel(AohaiHybridInverter.ChannelId.VOLTAGE_PV2).value().asString()
//				+ ", CURRENT_PV2: " + this.channel(AohaiHybridInverter.ChannelId.CURRENT_PV2).value().asString()
//				+ ", INV_PRIORITY: " + this.channel(AohaiHybridInverter.ChannelId.INV_PRIORITY).value().asString()
//				+ ", BMS_STATUS: " + this.channel(AohaiHybridInverter.ChannelId.BMS_STATUS).value().asString()
//				+ ", BAT_TYPE: " + this.channel(AohaiHybridInverter.ChannelId.BAT_TYPE).value().asString()
//				+ ", BAT_SOC: " + this.channel(AohaiHybridInverter.ChannelId.BAT_SOC).value().asString()
//				+ ", BAT_VOLTAGE: " + this.channel(AohaiHybridInverter.ChannelId.BAT_VOLTAGE).value().asString()				
//				+ ", BAT_VOLTAGE_DSP: " + this.channel(AohaiHybridInverter.ChannelId.BAT_VOLTAGE_DSP).value().asString()
//				+ ", BAT_CURRENT: " + this.channel(AohaiHybridInverter.ChannelId.BAT_CURRENT).value().asString()
				+ ", BAT_TEMP: " + this.channel(AohaiHybridInverter.ChannelId.BAT_TEMP).value().asString()
//				+ ", BAT_SOH: " + this.channel(AohaiHybridInverter.ChannelId.BAT_SOH).value().asString()
//				+ ", BAT_CYCLE_CNT: " + this.channel(AohaiHybridInverter.ChannelId.BAT_CYCLE_CNT).value().asString()
				+ ", POWER_PV: " + this.channel(AohaiHybridInverter.ChannelId.POWER_PV).value().asString()
//				+ ", POWER_PV1: " + this.channel(AohaiHybridInverter.ChannelId.POWER_PV1).value().asString() 
//				+ ", POWER_PV2: " + this.channel(AohaiHybridInverter.ChannelId.POWER_PV2).value().asString()
//				+ ", POWER_PV3: " + this.channel(AohaiHybridInverter.ChannelId.POWER_PV3).value().asString()
//				+ ", POWER_PV4: " + this.channel(AohaiHybridInverter.ChannelId.POWER_PV4).value().asString()
				+ ", GRID_POWER: " + this.channel(AohaiHybridInverter.ChannelId.GRID_POWER).value().asString()
				+ ", GRID_POWER_L1: " + this.channel(AohaiHybridInverter.ChannelId.GRID_POWER_L1).value().asString()
				+ ", GRID_POWER_L2: " + this.channel(AohaiHybridInverter.ChannelId.GRID_POWER_L2).value().asString()
				+ ", GRID_POWER_L3: " + this.channel(AohaiHybridInverter.ChannelId.GRID_POWER_L3).value().asString()
				+ ", EPS_POWER_L1: " + this.channel(AohaiHybridInverter.ChannelId.EPS_POWER_L1).value().asString()
//				+ ", EPS_POWER_L2: " + this.channel(AohaiHybridInverter.ChannelId.EPS_POWER_L2).value().asString()
//				+ ", EPS_POWER_L3: " + this.channel(AohaiHybridInverter.ChannelId.EPS_POWER_L3).value().asString()
//				+ ", EPS_APPARENT_POWER_L1: " + this.channel(AohaiHybridInverter.ChannelId.EPS_APPARENT_POWER_L1).value().asString()
//				+ ", EPS_APPARENT_POWER_L2: " + this.channel(AohaiHybridInverter.ChannelId.EPS_APPARENT_POWER_L2).value().asString()
//				+ ", EPS_APPARENT_POWER_L3: " + this.channel(AohaiHybridInverter.ChannelId.EPS_APPARENT_POWER_L3).value().asString()
//				+ ", INV_POWER: " + this.channel(AohaiHybridInverter.ChannelId.INV_POWER).value().asString()
				+ ", INV_POWER_L1: " + this.channel(AohaiHybridInverter.ChannelId.INV_POWER_L1).value().asString()
//				+ ", INV_POWER_L2: " + this.channel(AohaiHybridInverter.ChannelId.INV_POWER_L2).value().asString()
//				+ ", INV_POWER_L3: " + this.channel(AohaiHybridInverter.ChannelId.INV_POWER_L3).value().asString()
//				+ ", INV_APPARENT_POWER: " + this.channel(AohaiHybridInverter.ChannelId.INV_APPARENT_POWER).value().asString()
//				+ ", INV_APPARENT_POWER_L1: " + this.channel(AohaiHybridInverter.ChannelId.INV_APPARENT_POWER_L1).value().asString()
//				+ ", INV_APPARENT_POWER_L2: " + this.channel(AohaiHybridInverter.ChannelId.INV_APPARENT_POWER_L2).value().asString()
//				+ ", INV_APPARENT_POWER_L3: " + this.channel(AohaiHybridInverter.ChannelId.INV_APPARENT_POWER_L3).value().asString()
//				+ ", INV_REACTIVE_POWER: " + this.channel(AohaiHybridInverter.ChannelId.INV_REACTIVE_POWER).value().asString()
//				+ ", INV_REACTIVE_POWER_L1: " + this.channel(AohaiHybridInverter.ChannelId.INV_REACTIVE_POWER_L1).value().asString()
//				+ ", INV_REACTIVE_POWER_L2: " + this.channel(AohaiHybridInverter.ChannelId.INV_REACTIVE_POWER_L2).value().asString()
//				+ ", INV_REACTIVE_POWER_L3: " + this.channel(AohaiHybridInverter.ChannelId.INV_REACTIVE_POWER_L3).value().asString()
//				+ ", BAT_DISCHARGE_POWER: " + this.channel(AohaiHybridInverter.ChannelId.BAT_DISCHARGE_POWER).value().asString()
//				+ ", BAT_CHARGE_POWER: " + this.channel(AohaiHybridInverter.ChannelId.BAT_CHARGE_POWER).value().asString()
//				+ ", BAT_AC_CHARGE_POWER: " + this.channel(AohaiHybridInverter.ChannelId.BAT_AC_CHARGE_POWER).value().asString()
				+ ", E_PV_TODAY: " + this.channel(AohaiHybridInverter.ChannelId.E_PV_TODAY).value().asString()
//				+ ", E_PV_TOTAL: " + this.channel(AohaiHybridInverter.ChannelId.E_PV_TOTAL).value().asString()
//				+ ", BAT_CHARGE_TODAY: " + this.channel(AohaiHybridInverter.ChannelId.BAT_CHARGE_TODAY).value().asString()
//				+ ", BAT_AC_CHARGE_TODAY: " + this.channel(AohaiHybridInverter.ChannelId.BAT_AC_CHARGE_TODAY).value().asString()
//				+ ", BAT_CHARGE_TOTAL: " + this.channel(AohaiHybridInverter.ChannelId.BAT_CHARGE_TOTAL).value().asString()
//				+ ", BAT_AC_CHARGE_TOTAL: " + this.channel(AohaiHybridInverter.ChannelId.BAT_AC_CHARGE_TOTAL).value().asString()
//				+ ", BAT_DISCHARGE_TODAY: " + this.channel(AohaiHybridInverter.ChannelId.BAT_DISCHARGE_TODAY).value().asString()
//				+ ", BAT_DISCHARGE_TOTAL: " + this.channel(AohaiHybridInverter.ChannelId.BAT_DISCHARGE_TOTAL).value().asString()
//				+ ", E_GRID_SELL_TOTAL: " + this.channel(AohaiHybridInverter.ChannelId.E_GRID_SELL_TOTAL).value().asString()
//				+ ", E_GRID_BUY_TOTAL: " + this.channel(AohaiHybridInverter.ChannelId.E_GRID_BUY_TOTAL).value().asString()
				
//				+ ", TEST_DOUBLE: " + this.channel(AohaiHybridInverter.ChannelId.TEST_DOUBLE).value().asString()
//				+ ", TEST_LONG: " + this.channel(AohaiHybridInverter.ChannelId.TEST_LONG).value().asString()
//				+ ", TEST_U_INT: " + this.channel(AohaiHybridInverter.ChannelId.TEST_U_INT).value().asString()				
				;

	}
	
	
	@Override
	public Timedata getTimedata() {
		return this.timedata;
	}
}
