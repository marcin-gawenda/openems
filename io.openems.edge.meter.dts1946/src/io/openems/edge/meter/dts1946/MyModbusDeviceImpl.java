package io.openems.edge.meter.dts1946;

import static io.openems.edge.bridge.modbus.api.ElementToChannelConverter.INVERT_IF_TRUE;
import static io.openems.edge.bridge.modbus.api.ElementToChannelConverter.SCALE_FACTOR_3;
import static io.openems.edge.bridge.modbus.api.ElementToChannelConverter.chain;

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
import io.openems.edge.common.channel.Doc;
import io.openems.edge.common.component.OpenemsComponent;
import io.openems.edge.meter.api.ElectricityMeter;
import io.openems.edge.bridge.modbus.api.element.FloatDoublewordElement;
import io.openems.edge.bridge.modbus.api.task.FC4ReadInputRegistersTask;
import io.openems.edge.common.taskmanager.Priority;


@Designate(ocd = Config.class, factory = true)
@Component(//
		name = "io.openems.edge.meter.dts1946", //
		immediate = true, //
		configurationPolicy = ConfigurationPolicy.REQUIRE //
)
public class MyModbusDeviceImpl extends AbstractOpenemsModbusComponent implements MyModbusDevice, ElectricityMeter, ModbusComponent, OpenemsComponent {

	@Reference
	private ConfigurationAdmin cm;

	private boolean invert;
	
	@Reference(policy = ReferencePolicy.STATIC, policyOption = ReferencePolicyOption.GREEDY, cardinality = ReferenceCardinality.MANDATORY)
	protected void setModbus(BridgeModbus modbus) {
		super.setModbus(modbus);
	}

	private Config config = null;
	private MeterType meterType = MeterType.GRID;

	public MyModbusDeviceImpl() {
		super(//
				OpenemsComponent.ChannelId.values(), //
				ElectricityMeter.ChannelId.values(), //
				ModbusComponent.ChannelId.values(), //
				MyModbusDevice.ChannelId.values() //
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
		this.config = config;
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
	protected ModbusProtocol defineModbusProtocol() { // throws OpenemsException {
		// TODO implement ModbusProtocol
		return new ModbusProtocol(this, //
				new FC4ReadInputRegistersTask(0, Priority.LOW, // Priority.HIGH
						m(ElectricityMeter.ChannelId.VOLTAGE_L1, new FloatDoublewordElement(0), SCALE_FACTOR_3), //
						m(ElectricityMeter.ChannelId.VOLTAGE_L2, new FloatDoublewordElement(2), SCALE_FACTOR_3), //
						m(ElectricityMeter.ChannelId.VOLTAGE_L3, new FloatDoublewordElement(4), SCALE_FACTOR_3), //
						m(MyModbusDevice.ChannelId.VOLTAGE_L1_L2, new FloatDoublewordElement(6), SCALE_FACTOR_3), //
						m(MyModbusDevice.ChannelId.VOLTAGE_L2_L3, new FloatDoublewordElement(8), SCALE_FACTOR_3), //
						m(MyModbusDevice.ChannelId.VOLTAGE_L3_L1, new FloatDoublewordElement(10), SCALE_FACTOR_3), //
						m(ElectricityMeter.ChannelId.CURRENT_L1, new FloatDoublewordElement(12), SCALE_FACTOR_3), //
						m(ElectricityMeter.ChannelId.CURRENT_L2, new FloatDoublewordElement(14), SCALE_FACTOR_3), //
						m(ElectricityMeter.ChannelId.CURRENT_L3, new FloatDoublewordElement(16), SCALE_FACTOR_3), //
						m(ElectricityMeter.ChannelId.ACTIVE_POWER_L1, new FloatDoublewordElement(18), 
								chain(INVERT_IF_TRUE(this.invert), SCALE_FACTOR_3)),
						m(ElectricityMeter.ChannelId.ACTIVE_POWER_L2, new FloatDoublewordElement(20),
								chain(INVERT_IF_TRUE(this.invert), SCALE_FACTOR_3)),
						m(ElectricityMeter.ChannelId.ACTIVE_POWER_L3, new FloatDoublewordElement(22),
								chain(INVERT_IF_TRUE(this.invert), SCALE_FACTOR_3)),
						m(ElectricityMeter.ChannelId.ACTIVE_POWER, new FloatDoublewordElement(24),
								chain(INVERT_IF_TRUE(this.invert), SCALE_FACTOR_3))
						)
				);
	}

	@Override
	public String debugLog() {
		return "L:" + this.getActivePower().asString() + ", U-L1: " + this.getVoltageL1();
	}
}
