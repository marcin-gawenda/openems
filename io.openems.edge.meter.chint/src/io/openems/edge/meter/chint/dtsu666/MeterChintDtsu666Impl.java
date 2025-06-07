package io.openems.edge.meter.chint.dtsu666;

import static io.openems.edge.bridge.modbus.api.ElementToChannelConverter.INVERT_IF_TRUE;
import static io.openems.edge.bridge.modbus.api.ElementToChannelConverter.SCALE_FACTOR_1;
import static io.openems.edge.bridge.modbus.api.ElementToChannelConverter.SCALE_FACTOR_2;
import static io.openems.edge.bridge.modbus.api.ElementToChannelConverter.SCALE_FACTOR_3;
import static io.openems.edge.bridge.modbus.api.ElementToChannelConverter.SCALE_FACTOR_MINUS_1;
import static io.openems.edge.bridge.modbus.api.ElementToChannelConverter.SCALE_FACTOR_MINUS_2;
import static io.openems.edge.bridge.modbus.api.ElementToChannelConverter.SCALE_FACTOR_MINUS_3;
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
import io.openems.edge.bridge.modbus.api.AbstractOpenemsModbusComponent;
import io.openems.edge.bridge.modbus.api.BridgeModbus;

import io.openems.edge.bridge.modbus.api.ModbusComponent;
import io.openems.edge.bridge.modbus.api.ModbusProtocol;
import io.openems.edge.common.channel.value.Value;
import io.openems.edge.common.component.OpenemsComponent;
import io.openems.edge.meter.api.ElectricityMeter;
import io.openems.edge.bridge.modbus.api.element.DummyRegisterElement;
import io.openems.edge.bridge.modbus.api.element.FloatDoublewordElement;
import io.openems.edge.bridge.modbus.api.element.UnsignedDoublewordElement;
import io.openems.edge.bridge.modbus.api.element.UnsignedWordElement;
import io.openems.edge.bridge.modbus.api.task.FC3ReadRegistersTask;
import io.openems.edge.bridge.modbus.api.task.FC4ReadInputRegistersTask;
import io.openems.edge.common.taskmanager.Priority;
import io.openems.edge.timedata.api.Timedata;
import io.openems.edge.timedata.api.TimedataProvider;


/**
 * Consumption: energy taken from the grid (registered by the grid meter as consumed by the metered system)
 * Production: energy send to the grid (registered by the grid meter as fed-in from the metered system to the grid)
 */
@Designate(ocd = Config.class, factory = true)
@Component(//
		name = "io.openems.edge.meter.chint.dtsu666", //
		immediate = true, //
		configurationPolicy = ConfigurationPolicy.REQUIRE //
)
public class MeterChintDtsu666Impl extends AbstractOpenemsModbusComponent implements MeterChintDtsu666, ElectricityMeter, TimedataProvider, ModbusComponent, OpenemsComponent {

	@Reference
	private ConfigurationAdmin cm;

	private boolean invert;
	
	@Reference(policy = ReferencePolicy.DYNAMIC, policyOption = ReferencePolicyOption.GREEDY, cardinality = ReferenceCardinality.OPTIONAL)
	private volatile Timedata timedata;
	
	@Reference(policy = ReferencePolicy.STATIC, policyOption = ReferencePolicyOption.GREEDY, cardinality = ReferenceCardinality.MANDATORY)
	protected void setModbus(BridgeModbus modbus) {
		super.setModbus(modbus);
	}

	private MeterType meterType = MeterType.GRID;

	public MeterChintDtsu666Impl() {
		super(//
				OpenemsComponent.ChannelId.values(), //
				ModbusComponent.ChannelId.values(), //
				ElectricityMeter.ChannelId.values(), //				
				MeterChintDtsu666.ChannelId.values() //
		);
	}

	@Activate
	private void activate(ComponentContext context, Config config) throws OpenemsException {
		this.meterType = config.type();
		this.invert = config.invert();
		
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
				new FC4ReadInputRegistersTask(0x2000, Priority.HIGH, //
						m(MeterChintDtsu666.ChannelId.VOLTAGE_L1_L2, new FloatDoublewordElement(0x2000), SCALE_FACTOR_2), //
						m(MeterChintDtsu666.ChannelId.VOLTAGE_L2_L3, new FloatDoublewordElement(0x2002), SCALE_FACTOR_2), //
						m(MeterChintDtsu666.ChannelId.VOLTAGE_L3_L1, new FloatDoublewordElement(0x2004), SCALE_FACTOR_2), //
						m(ElectricityMeter.ChannelId.VOLTAGE_L1, new FloatDoublewordElement(0x2006), SCALE_FACTOR_2), //
						m(ElectricityMeter.ChannelId.VOLTAGE_L2, new FloatDoublewordElement(0x2008), SCALE_FACTOR_2), //
						m(ElectricityMeter.ChannelId.VOLTAGE_L3, new FloatDoublewordElement(0x200A), SCALE_FACTOR_2), //						
						m(ElectricityMeter.ChannelId.CURRENT_L1, new FloatDoublewordElement(0x200C)), //
						m(ElectricityMeter.ChannelId.CURRENT_L2, new FloatDoublewordElement(0x200E)), //
						m(ElectricityMeter.ChannelId.CURRENT_L3, new FloatDoublewordElement(0x2010)), //
						m(ElectricityMeter.ChannelId.ACTIVE_POWER, new FloatDoublewordElement(0x2012),
								chain(INVERT_IF_TRUE(this.invert), SCALE_FACTOR_MINUS_1)),						
						m(ElectricityMeter.ChannelId.ACTIVE_POWER_L1, new FloatDoublewordElement(0x2014), 
								chain(INVERT_IF_TRUE(this.invert), SCALE_FACTOR_MINUS_1)),
						m(ElectricityMeter.ChannelId.ACTIVE_POWER_L2, new FloatDoublewordElement(0x2016),
								chain(INVERT_IF_TRUE(this.invert), SCALE_FACTOR_MINUS_1)),
						m(ElectricityMeter.ChannelId.ACTIVE_POWER_L3, new FloatDoublewordElement(0x2018),
								chain(INVERT_IF_TRUE(this.invert), SCALE_FACTOR_MINUS_1)),
						m(ElectricityMeter.ChannelId.REACTIVE_POWER, new FloatDoublewordElement(0x201A),
								chain(INVERT_IF_TRUE(this.invert), SCALE_FACTOR_MINUS_1)),												
						m(ElectricityMeter.ChannelId.REACTIVE_POWER_L1, new FloatDoublewordElement(0x201C),
								chain(INVERT_IF_TRUE(this.invert), SCALE_FACTOR_MINUS_1)),
						m(ElectricityMeter.ChannelId.REACTIVE_POWER_L2, new FloatDoublewordElement(0x201E),
								chain(INVERT_IF_TRUE(this.invert), SCALE_FACTOR_MINUS_1)),
						m(ElectricityMeter.ChannelId.REACTIVE_POWER_L3, new FloatDoublewordElement(0x2020),
								chain(INVERT_IF_TRUE(this.invert), SCALE_FACTOR_MINUS_1)),
						m(MeterChintDtsu666.ChannelId.APPARENT_POWER, new FloatDoublewordElement(0x2022), SCALE_FACTOR_MINUS_1), //
						m(MeterChintDtsu666.ChannelId.APPARENT_POWER_L1, new FloatDoublewordElement(0x2024), SCALE_FACTOR_MINUS_1), //
						m(MeterChintDtsu666.ChannelId.APPARENT_POWER_L2, new FloatDoublewordElement(0x2026), SCALE_FACTOR_MINUS_1), //
						m(MeterChintDtsu666.ChannelId.APPARENT_POWER_L3, new FloatDoublewordElement(0x2028), SCALE_FACTOR_MINUS_1), //
						m(MeterChintDtsu666.ChannelId.COS_PHI, new FloatDoublewordElement(0x202A), SCALE_FACTOR_MINUS_3), //
						m(MeterChintDtsu666.ChannelId.COS_PHI_L1, new FloatDoublewordElement(0x202C), SCALE_FACTOR_MINUS_3), //
						m(MeterChintDtsu666.ChannelId.COS_PHI_L2, new FloatDoublewordElement(0x202E), SCALE_FACTOR_MINUS_3), //
						m(MeterChintDtsu666.ChannelId.COS_PHI_L3, new FloatDoublewordElement(0x2030), SCALE_FACTOR_MINUS_3), //
						new DummyRegisterElement(0x2032, 0x2043), //
						m(ElectricityMeter.ChannelId.FREQUENCY, new FloatDoublewordElement(0x2044), SCALE_FACTOR_MINUS_2) //							


//						m(this.invert ? ElectricityMeter.ChannelId.ACTIVE_PRODUCTION_ENERGY : ElectricityMeter.ChannelId.ACTIVE_CONSUMPTION_ENERGY, 
//								new FloatDoublewordElement(0x2034), SCALE_FACTOR_3), //
//						m(this.invert ? ElectricityMeter.ChannelId.ACTIVE_CONSUMPTION_ENERGY : ElectricityMeter.ChannelId.ACTIVE_PRODUCTION_ENERGY, 
//								new FloatDoublewordElement(0x2036), SCALE_FACTOR_3), //
//						m(this.invert ? MeterChintDtsu666.ChannelId.REACTIVE_PRODUCTION_ENERGY : MeterChintDtsu666.ChannelId.REACTIVE_CONSUMPTION_ENERGY, 
//								new FloatDoublewordElement(0x2038), SCALE_FACTOR_3), //
//						m(this.invert ? MeterChintDtsu666.ChannelId.REACTIVE_CONSUMPTION_ENERGY : MeterChintDtsu666.ChannelId.REACTIVE_PRODUCTION_ENERGY, 
//								new FloatDoublewordElement(0x203A), SCALE_FACTOR_3) // 
					)
				);

		if (!this.invert) {	// TODO verify inversion is correct
			modbusProtocol.addTask(new FC4ReadInputRegistersTask(0x101E, Priority.HIGH,				
					m(ElectricityMeter.ChannelId.ACTIVE_CONSUMPTION_ENERGY, new FloatDoublewordElement(0x101E), SCALE_FACTOR_3), //
					m(ElectricityMeter.ChannelId.ACTIVE_CONSUMPTION_ENERGY_L1, new FloatDoublewordElement(0x1020), SCALE_FACTOR_3), //
					m(ElectricityMeter.ChannelId.ACTIVE_CONSUMPTION_ENERGY_L2, new FloatDoublewordElement(0x1022), SCALE_FACTOR_3), //
					m(ElectricityMeter.ChannelId.ACTIVE_CONSUMPTION_ENERGY_L3, new FloatDoublewordElement(0x1024), SCALE_FACTOR_3), //
					m(MeterChintDtsu666.ChannelId.ACTIVE_CONSUMPTION_ENERGY_NET, new FloatDoublewordElement(0x1026), SCALE_FACTOR_3), //
					m(ElectricityMeter.ChannelId.ACTIVE_PRODUCTION_ENERGY, new FloatDoublewordElement(0x1028), SCALE_FACTOR_3), //
					m(ElectricityMeter.ChannelId.ACTIVE_PRODUCTION_ENERGY_L1, new FloatDoublewordElement(0x102A), SCALE_FACTOR_3), //
					m(ElectricityMeter.ChannelId.ACTIVE_PRODUCTION_ENERGY_L2, new FloatDoublewordElement(0x102C), SCALE_FACTOR_3), //
					m(ElectricityMeter.ChannelId.ACTIVE_PRODUCTION_ENERGY_L3, new FloatDoublewordElement(0x102E), SCALE_FACTOR_3), //
					m(MeterChintDtsu666.ChannelId.ACTIVE_PRODUCTION_ENERGY_NET, new FloatDoublewordElement(0x1030), SCALE_FACTOR_3) //
					)
				);
		}
		
		
		// Reactive energy by quadrants
		if (this.invert) {	// TODO verify inversion is correct						
			modbusProtocol.addTask(new FC4ReadInputRegistersTask(0x4032, Priority.HIGH,
//					m(MeterChintDtsu666.ChannelId.APPARENT_ENERGY, new UnsignedDoublewordElement(0x010E), SCALE_FACTOR_1), //
					m(MeterChintDtsu666.ChannelId.REACTIVE_CONSUMPTION_ENERGY_INDUCTIVE_Q_I, new FloatDoublewordElement(0x4032), SCALE_FACTOR_3), //
					new DummyRegisterElement(0x4034, 0x403B), //
					m(MeterChintDtsu666.ChannelId.REACTIVE_CONSUMPTION_ENERGY_CAPACITIVE_Q_I_I, new FloatDoublewordElement(0x403C), SCALE_FACTOR_3), //
					new DummyRegisterElement(0x403E, 0x4045), //
					m(MeterChintDtsu666.ChannelId.REACTIVE_PRODUCTION_ENERGY_CAPACITIVE_Q_I_I_I, new FloatDoublewordElement(0x4046), SCALE_FACTOR_3), //
					new DummyRegisterElement(0x4048, 0x404F), //
					m(MeterChintDtsu666.ChannelId.REACTIVE_PRODUCTION_ENERGY_CAPACITIVE_Q_I_V, new FloatDoublewordElement(0x4050), SCALE_FACTOR_3) //				 
					)
			);
		} else {
			modbusProtocol.addTask(new FC4ReadInputRegistersTask(0x4032, Priority.HIGH,
//					m(MeterChintDtsu666.ChannelId.APPARENT_ENERGY, new UnsignedDoublewordElement(0x010E), SCALE_FACTOR_1), //
					m(MeterChintDtsu666.ChannelId.REACTIVE_CONSUMPTION_ENERGY_INDUCTIVE_Q_I, new FloatDoublewordElement(0x4032), SCALE_FACTOR_3), //
					new DummyRegisterElement(0x4034, 0x403B), //
					m(MeterChintDtsu666.ChannelId.REACTIVE_CONSUMPTION_ENERGY_CAPACITIVE_Q_I_I, new FloatDoublewordElement(0x403C), SCALE_FACTOR_3), //
					new DummyRegisterElement(0x403E, 0x4045), //
					m(MeterChintDtsu666.ChannelId.REACTIVE_PRODUCTION_ENERGY_CAPACITIVE_Q_I_I_I, new FloatDoublewordElement(0x4046), SCALE_FACTOR_3), //
					new DummyRegisterElement(0x4048, 0x404F), //
					m(MeterChintDtsu666.ChannelId.REACTIVE_PRODUCTION_ENERGY_CAPACITIVE_Q_I_V, new FloatDoublewordElement(0x4050), SCALE_FACTOR_3) //				 
					)
				);			
		}
		
		
		return modbusProtocol;
	}

	@Override
	public String debugLog() {
		return "L:" + this.getActivePower().asString() + ", U1: " + this.getVoltageL1();
	}
	
	@Override
	public Timedata getTimedata() {
		return this.timedata;
	}
}
