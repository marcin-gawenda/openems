package io.openems.edge.meter.elecnova;

import org.osgi.service.event.EventHandler;

import io.openems.common.channel.Unit;
import io.openems.common.types.OpenemsType;
import io.openems.edge.bridge.modbus.api.ModbusComponent;
import io.openems.edge.common.channel.Doc;
import io.openems.edge.common.component.OpenemsComponent;
import io.openems.edge.meter.api.ElectricityMeter;

public interface MyModbusDevice extends ElectricityMeter, ModbusComponent, OpenemsComponent {

	public enum ChannelId implements io.openems.edge.common.channel.ChannelId {
		VOLTAGE_L1_L2(Doc.of(OpenemsType.INTEGER) //
				.unit(Unit.VOLT)), //
		VOLTAGE_L2_L3(Doc.of(OpenemsType.INTEGER) //
				.unit(Unit.VOLT)), //
		VOLTAGE_L3_L1(Doc.of(OpenemsType.INTEGER) //
				.unit(Unit.VOLT)), //
		;

		private final Doc doc;

		private ChannelId(Doc doc) {
			this.doc = doc;
		}

		@Override
		public Doc doc() {
			return this.doc;
		}
	}

}
