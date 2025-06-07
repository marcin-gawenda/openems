package io.openems.edge.meter.chint.dtsu666;

import io.openems.common.channel.AccessMode;
import io.openems.common.channel.PersistencePriority;
import io.openems.common.channel.Unit;
import io.openems.common.types.OpenemsType;
import io.openems.edge.bridge.modbus.api.ModbusComponent;
import io.openems.edge.common.channel.Doc;
import io.openems.edge.common.channel.IntegerReadChannel;
import io.openems.edge.common.channel.value.Value;
import io.openems.edge.common.component.OpenemsComponent;
import io.openems.edge.meter.api.ElectricityMeter;

public interface MeterChintDtsu666 extends ElectricityMeter, ModbusComponent, OpenemsComponent {

	public enum ChannelId implements io.openems.edge.common.channel.ChannelId {
		VOLTAGE_L1_L2(Doc.of(OpenemsType.INTEGER) //
				.accessMode(AccessMode.READ_ONLY).unit(Unit.VOLT).persistencePriority(PersistencePriority.MEDIUM)), //
		VOLTAGE_L2_L3(Doc.of(OpenemsType.INTEGER) //
				.accessMode(AccessMode.READ_ONLY).unit(Unit.VOLT).persistencePriority(PersistencePriority.MEDIUM)), //
		VOLTAGE_L3_L1(Doc.of(OpenemsType.INTEGER) //
				.accessMode(AccessMode.READ_ONLY).unit(Unit.VOLT).persistencePriority(PersistencePriority.MEDIUM)), //

		
		  APPARENT_POWER_L1(Doc.of(OpenemsType.INTEGER) //
		  .accessMode(AccessMode.READ_ONLY) .unit(Unit.VOLT_AMPERE)
		  .persistencePriority(PersistencePriority.MEDIUM)), //
		  APPARENT_POWER_L2(Doc.of(OpenemsType.INTEGER) //
		  .accessMode(AccessMode.READ_ONLY) .unit(Unit.VOLT_AMPERE)
		  .persistencePriority(PersistencePriority.MEDIUM)), //
		  APPARENT_POWER_L3(Doc.of(OpenemsType.INTEGER) //
		  .accessMode(AccessMode.READ_ONLY) .unit(Unit.VOLT_AMPERE)
		  .persistencePriority(PersistencePriority.MEDIUM)), //
		  APPARENT_POWER(Doc.of(OpenemsType.INTEGER) //
		  .accessMode(AccessMode.READ_ONLY) .unit(Unit.VOLT_AMPERE)
		  .persistencePriority(PersistencePriority.MEDIUM)), //
		 
		APPARENT_ENERGY(Doc.of(OpenemsType.LONG) //
				.accessMode(AccessMode.READ_ONLY).unit(Unit.VOLT_AMPERE_HOURS)
				.persistencePriority(PersistencePriority.MEDIUM)), //

		COS_PHI_L1(Doc.of(OpenemsType.FLOAT).accessMode(AccessMode.READ_ONLY)
				.persistencePriority(PersistencePriority.HIGH)), //
		COS_PHI_L2(Doc.of(OpenemsType.FLOAT).accessMode(AccessMode.READ_ONLY)
				.persistencePriority(PersistencePriority.HIGH)), //
		COS_PHI_L3(Doc.of(OpenemsType.FLOAT).accessMode(AccessMode.READ_ONLY)
				.persistencePriority(PersistencePriority.HIGH)), //
		COS_PHI(Doc.of(OpenemsType.FLOAT).accessMode(AccessMode.READ_ONLY)
				.persistencePriority(PersistencePriority.HIGH)), //

		// Reactive energy taken from the grid (registered by the grid meter as consumed
		// by the metered system)
		REACTIVE_CONSUMPTION_ENERGY(Doc.of(OpenemsType.LONG) //
				.accessMode(AccessMode.READ_ONLY).unit(Unit.VOLT_AMPERE_REACTIVE_HOURS) //
				.persistencePriority(PersistencePriority.HIGH)),

		// Reactive energy send to the grid (registered by the grid meter as fed-in from
		// the metered system to the grid)
		REACTIVE_PRODUCTION_ENERGY(Doc.of(OpenemsType.LONG) //
				.accessMode(AccessMode.READ_ONLY).unit(Unit.VOLT_AMPERE_REACTIVE_HOURS) //
				.persistencePriority(PersistencePriority.HIGH)),

		// Reactive energy 1st quadrant
		REACTIVE_CONSUMPTION_ENERGY_INDUCTIVE_Q_I(Doc.of(OpenemsType.LONG) //
				.accessMode(AccessMode.READ_ONLY).unit(Unit.VOLT_AMPERE_REACTIVE_HOURS)
				.persistencePriority(PersistencePriority.HIGH)), //

		// Reactive energy 2nd quadrant
		REACTIVE_CONSUMPTION_ENERGY_CAPACITIVE_Q_I_I(Doc.of(OpenemsType.LONG) //
				.accessMode(AccessMode.READ_ONLY).unit(Unit.VOLT_AMPERE_REACTIVE_HOURS)
				.persistencePriority(PersistencePriority.HIGH)), //

		// Reactive energy 3rd quadrant
		REACTIVE_PRODUCTION_ENERGY_CAPACITIVE_Q_I_I_I(Doc.of(OpenemsType.LONG) //
				.accessMode(AccessMode.READ_ONLY).unit(Unit.VOLT_AMPERE_REACTIVE_HOURS)
				.persistencePriority(PersistencePriority.HIGH)), //

		// Reactive energy 4th quadrant
		REACTIVE_PRODUCTION_ENERGY_CAPACITIVE_Q_I_V(Doc.of(OpenemsType.LONG) //
				.accessMode(AccessMode.READ_ONLY).unit(Unit.VOLT_AMPERE_REACTIVE_HOURS)
				.persistencePriority(PersistencePriority.HIGH)), //

		T_H_DU1(Doc.of(OpenemsType.INTEGER) //
				.accessMode(AccessMode.READ_ONLY).unit(Unit.PERCENT).persistencePriority(PersistencePriority.MEDIUM)), //
		T_H_DU2(Doc.of(OpenemsType.INTEGER) //
				.accessMode(AccessMode.READ_ONLY).unit(Unit.PERCENT).persistencePriority(PersistencePriority.MEDIUM)), //
		T_H_DU3(Doc.of(OpenemsType.INTEGER) //
				.accessMode(AccessMode.READ_ONLY).unit(Unit.PERCENT).persistencePriority(PersistencePriority.MEDIUM)), //
		T_H_DI1(Doc.of(OpenemsType.INTEGER) //
				.accessMode(AccessMode.READ_ONLY).unit(Unit.PERCENT).persistencePriority(PersistencePriority.MEDIUM)), //
		T_H_DI2(Doc.of(OpenemsType.INTEGER) //
				.accessMode(AccessMode.READ_ONLY).unit(Unit.PERCENT).persistencePriority(PersistencePriority.MEDIUM)), //
		T_H_DI3(Doc.of(OpenemsType.INTEGER) //
				.accessMode(AccessMode.READ_ONLY).unit(Unit.PERCENT).persistencePriority(PersistencePriority.MEDIUM)),

		// Active energy taken from the grid (registered by the grid meter as consumed
		// by the metered system) on phase Lx
		// TODO NET = ????
		ACTIVE_CONSUMPTION_ENERGY_NET(Doc.of(OpenemsType.LONG) //
				.accessMode(AccessMode.READ_ONLY).unit(Unit.CUMULATED_WATT_HOURS) //
				.persistencePriority(PersistencePriority.HIGH)),

		// Active energy send to the grid (registered by the grid meter as fed-in from
		// the metered system to the grid) on phase Lx		
		ACTIVE_PRODUCTION_ENERGY_NET(Doc.of(OpenemsType.LONG) //
				.accessMode(AccessMode.READ_ONLY).unit(Unit.CUMULATED_WATT_HOURS)
				.persistencePriority(PersistencePriority.HIGH)), //
		
		DEBUG_FLOAT(Doc.of(OpenemsType.FLOAT).accessMode(AccessMode.READ_ONLY)
				.persistencePriority(PersistencePriority.HIGH)) //
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
