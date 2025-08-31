package io.openems.edge.meter.elecnova.dts1946;

import static io.openems.edge.bridge.modbus.api.ElementToChannelConverter.INVERT_IF_TRUE;
import static io.openems.edge.bridge.modbus.api.ElementToChannelConverter.SCALE_FACTOR_1;
import static io.openems.edge.bridge.modbus.api.ElementToChannelConverter.SCALE_FACTOR_3;
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
import io.openems.edge.bridge.modbus.api.AbstractOpenemsModbusComponent;
import io.openems.edge.bridge.modbus.api.BridgeModbus;

import io.openems.edge.bridge.modbus.api.ModbusComponent;
import io.openems.edge.bridge.modbus.api.ModbusProtocol;
import io.openems.edge.common.channel.value.Value;
import io.openems.edge.common.component.OpenemsComponent;
import io.openems.edge.meter.api.ElectricityMeter;
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
		name = "io.openems.edge.meter.elecnova.dts1946", //
		immediate = true, //
		configurationPolicy = ConfigurationPolicy.REQUIRE //
)
public class MeterElecnovaDts1946Impl extends AbstractOpenemsModbusComponent implements MeterElecnovaDts1946, ElectricityMeter, TimedataProvider, ModbusComponent, OpenemsComponent {

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

	public MeterElecnovaDts1946Impl() {
		super(//
				OpenemsComponent.ChannelId.values(), //
				ModbusComponent.ChannelId.values(), //
				ElectricityMeter.ChannelId.values(), //				
				MeterElecnovaDts1946.ChannelId.values() //
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
				new FC4ReadInputRegistersTask(0x0000, Priority.HIGH, //
						m(ElectricityMeter.ChannelId.VOLTAGE_L1, new FloatDoublewordElement(0x0000), SCALE_FACTOR_3), //
						m(ElectricityMeter.ChannelId.VOLTAGE_L2, new FloatDoublewordElement(0x0002), SCALE_FACTOR_3), //
						m(ElectricityMeter.ChannelId.VOLTAGE_L3, new FloatDoublewordElement(0x0004), SCALE_FACTOR_3), //
						m(MeterElecnovaDts1946.ChannelId.VOLTAGE_L1_L2, new FloatDoublewordElement(0x0006), SCALE_FACTOR_3), //
						m(MeterElecnovaDts1946.ChannelId.VOLTAGE_L2_L3, new FloatDoublewordElement(0x0008), SCALE_FACTOR_3), //
						m(MeterElecnovaDts1946.ChannelId.VOLTAGE_L3_L1, new FloatDoublewordElement(0x000A), SCALE_FACTOR_3), //
						m(ElectricityMeter.ChannelId.CURRENT_L1, new FloatDoublewordElement(0x000C), SCALE_FACTOR_3), //
						m(ElectricityMeter.ChannelId.CURRENT_L2, new FloatDoublewordElement(0x000E), SCALE_FACTOR_3), //
						m(ElectricityMeter.ChannelId.CURRENT_L3, new FloatDoublewordElement(0x0010), SCALE_FACTOR_3), //
						m(ElectricityMeter.ChannelId.ACTIVE_POWER_L1, new FloatDoublewordElement(0x0012), 
								chain(INVERT_IF_TRUE(this.invert), SCALE_FACTOR_3)),
						m(ElectricityMeter.ChannelId.ACTIVE_POWER_L2, new FloatDoublewordElement(0x0014),
								chain(INVERT_IF_TRUE(this.invert), SCALE_FACTOR_3)),
						m(ElectricityMeter.ChannelId.ACTIVE_POWER_L3, new FloatDoublewordElement(0x0016),
								chain(INVERT_IF_TRUE(this.invert), SCALE_FACTOR_3)),
						m(ElectricityMeter.ChannelId.ACTIVE_POWER, new FloatDoublewordElement(0x0018),
								chain(INVERT_IF_TRUE(this.invert), SCALE_FACTOR_3)),
						m(ElectricityMeter.ChannelId.REACTIVE_POWER_L1, new FloatDoublewordElement(0x001A),
								chain(INVERT_IF_TRUE(this.invert), SCALE_FACTOR_3)),
						m(ElectricityMeter.ChannelId.REACTIVE_POWER_L2, new FloatDoublewordElement(0x001C),
								chain(INVERT_IF_TRUE(this.invert), SCALE_FACTOR_3)),
						m(ElectricityMeter.ChannelId.REACTIVE_POWER_L3, new FloatDoublewordElement(0x001E),
								chain(INVERT_IF_TRUE(this.invert), SCALE_FACTOR_3)),
						m(ElectricityMeter.ChannelId.REACTIVE_POWER, new FloatDoublewordElement(0x0020),
								chain(INVERT_IF_TRUE(this.invert), SCALE_FACTOR_3)),						
						m(MeterElecnovaDts1946.ChannelId.APPARENT_POWER_L1, new FloatDoublewordElement(0x0022), SCALE_FACTOR_3), //
						m(MeterElecnovaDts1946.ChannelId.APPARENT_POWER_L2, new FloatDoublewordElement(0x0024), SCALE_FACTOR_3), //
						m(MeterElecnovaDts1946.ChannelId.APPARENT_POWER_L3, new FloatDoublewordElement(0x0026), SCALE_FACTOR_3), //
						m(MeterElecnovaDts1946.ChannelId.APPARENT_POWER, new FloatDoublewordElement(0x0028), SCALE_FACTOR_3), //						
						m(MeterElecnovaDts1946.ChannelId.COS_PHI_L1, new FloatDoublewordElement(0x002A)), //
						m(MeterElecnovaDts1946.ChannelId.COS_PHI_L2, new FloatDoublewordElement(0x002C)), //
						m(MeterElecnovaDts1946.ChannelId.COS_PHI_L3, new FloatDoublewordElement(0x002E)), //
						m(MeterElecnovaDts1946.ChannelId.COS_PHI, new FloatDoublewordElement(0x0030)), //						
						m(ElectricityMeter.ChannelId.FREQUENCY, new FloatDoublewordElement(0x0032)), //
						m(this.invert ? ElectricityMeter.ChannelId.ACTIVE_PRODUCTION_ENERGY : ElectricityMeter.ChannelId.ACTIVE_CONSUMPTION_ENERGY, 
								new FloatDoublewordElement(0x0034), SCALE_FACTOR_3), //
						m(this.invert ? ElectricityMeter.ChannelId.ACTIVE_CONSUMPTION_ENERGY : ElectricityMeter.ChannelId.ACTIVE_PRODUCTION_ENERGY, 
								new FloatDoublewordElement(0x0036), SCALE_FACTOR_3), //
						m(this.invert ? MeterElecnovaDts1946.ChannelId.REACTIVE_PRODUCTION_ENERGY : MeterElecnovaDts1946.ChannelId.REACTIVE_CONSUMPTION_ENERGY, 
								new FloatDoublewordElement(0x0038), SCALE_FACTOR_3), //
						m(this.invert ? MeterElecnovaDts1946.ChannelId.REACTIVE_CONSUMPTION_ENERGY : MeterElecnovaDts1946.ChannelId.REACTIVE_PRODUCTION_ENERGY, 
								new FloatDoublewordElement(0x003A), SCALE_FACTOR_3) // 
					)
				);

		// Reactive energy by quadrants
		if (this.invert) {	// TODO verify inversion is correct						
			modbusProtocol.addTask(new FC4ReadInputRegistersTask(0x010E, Priority.HIGH,
				m(MeterElecnovaDts1946.ChannelId.APPARENT_ENERGY, new UnsignedDoublewordElement(0x010E), SCALE_FACTOR_1), //
				m(MeterElecnovaDts1946.ChannelId.REACTIVE_CONSUMPTION_ENERGY_INDUCTIVE_Q_I, new UnsignedDoublewordElement(0x0110), SCALE_FACTOR_1), //
				m(MeterElecnovaDts1946.ChannelId.REACTIVE_CONSUMPTION_ENERGY_CAPACITIVE_Q_I_I, new UnsignedDoublewordElement(0x0112), SCALE_FACTOR_1), //
				m(MeterElecnovaDts1946.ChannelId.REACTIVE_PRODUCTION_ENERGY_INDUCTIVE_Q_I_I_I, new UnsignedDoublewordElement(0x0114), SCALE_FACTOR_1), //
				m(MeterElecnovaDts1946.ChannelId.REACTIVE_PRODUCTION_ENERGY_CAPACITIVE_Q_I_V, new UnsignedDoublewordElement(0x0116), SCALE_FACTOR_1) //				 
				)
			);
		} else {
			modbusProtocol.addTask(new FC4ReadInputRegistersTask(0x010E, Priority.HIGH,
					m(MeterElecnovaDts1946.ChannelId.APPARENT_ENERGY, new UnsignedDoublewordElement(0x010E), SCALE_FACTOR_1), //
					m(MeterElecnovaDts1946.ChannelId.REACTIVE_CONSUMPTION_ENERGY_INDUCTIVE_Q_I, new UnsignedDoublewordElement(0x0110), SCALE_FACTOR_1), //
					m(MeterElecnovaDts1946.ChannelId.REACTIVE_CONSUMPTION_ENERGY_CAPACITIVE_Q_I_I, new UnsignedDoublewordElement(0x0112), SCALE_FACTOR_1), //
					m(MeterElecnovaDts1946.ChannelId.REACTIVE_PRODUCTION_ENERGY_INDUCTIVE_Q_I_I_I, new UnsignedDoublewordElement(0x0114), SCALE_FACTOR_1), //
					m(MeterElecnovaDts1946.ChannelId.REACTIVE_PRODUCTION_ENERGY_CAPACITIVE_Q_I_V, new UnsignedDoublewordElement(0x0116), SCALE_FACTOR_1) //				 
					)
				);			
		}
		
		modbusProtocol.addTask(new FC4ReadInputRegistersTask(0x05FA, Priority.HIGH,
				m(MeterElecnovaDts1946.ChannelId.T_H_DU1, new UnsignedWordElement(0x05FA), SCALE_FACTOR_MINUS_2), //
				m(MeterElecnovaDts1946.ChannelId.T_H_DU2, new UnsignedWordElement(0x05FB), SCALE_FACTOR_MINUS_2), //
				m(MeterElecnovaDts1946.ChannelId.T_H_DU3, new UnsignedWordElement(0x05FC), SCALE_FACTOR_MINUS_2), //
				m(MeterElecnovaDts1946.ChannelId.T_H_DI1, new UnsignedWordElement(0x05FD), SCALE_FACTOR_MINUS_2), //
				m(MeterElecnovaDts1946.ChannelId.T_H_DI2, new UnsignedWordElement(0x05FE), SCALE_FACTOR_MINUS_2), //
				m(MeterElecnovaDts1946.ChannelId.T_H_DI3, new UnsignedWordElement(0x05FF), SCALE_FACTOR_MINUS_2) //
				)
			);
		
		return modbusProtocol;
	}

	@Override
	public String debugLog() {
		return "\n\tid: " + this.getUnitId()				
				+ ", L: " + this.getActivePower().asString() //
				+ ", U1: " + this.getVoltageL1()
		;
	}
	
	@Override
	public Timedata getTimedata() {
		return this.timedata;
	}
}
