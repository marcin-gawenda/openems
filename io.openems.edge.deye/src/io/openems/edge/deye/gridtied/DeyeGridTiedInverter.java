package io.openems.edge.deye.gridtied;

import io.openems.common.channel.AccessMode;
import io.openems.common.channel.PersistencePriority;
import io.openems.common.channel.Unit;
import io.openems.common.types.OpenemsType;
import io.openems.edge.bridge.modbus.api.ModbusComponent;
import io.openems.edge.common.channel.Doc;
import io.openems.edge.common.channel.IntegerReadChannel;
import io.openems.edge.common.channel.LongReadChannel;
import io.openems.edge.common.component.OpenemsComponent;
import io.openems.edge.deye.enums.InverterStatus;
import io.openems.edge.meter.api.ElectricityMeter;


/*
 * see also https://github.com/kbialek/deye-inverter-mqtt/blob/main/docs/metric_group_string.md
 */
public interface DeyeGridTiedInverter extends ElectricityMeter, ModbusComponent, OpenemsComponent {

	public enum ChannelId implements io.openems.edge.common.channel.ChannelId {
		
		/**
		 * Represents the state of the inverter.
		 */
		INV_STATUS(Doc.of(InverterStatus.values())
				.persistencePriority(PersistencePriority.HIGH)), //
		
		// energy exported to Grid today
		E_GRID_TODAY(Doc.of(OpenemsType.INTEGER) //
				.accessMode(AccessMode.READ_ONLY)
				.unit(Unit.WATT_HOURS) //
				.persistencePriority(PersistencePriority.HIGH)),
	
		// total energy exported to Grid
		E_GRID_TOTAL(Doc.of(OpenemsType.LONG) //
				.accessMode(AccessMode.READ_ONLY)
				.unit(Unit.KILOWATT_HOURS) //
				.persistencePriority(PersistencePriority.HIGH)),

		// reactive energy exported to Grid today
		RE_GRID_TODAY(Doc.of(OpenemsType.INTEGER) //
				.accessMode(AccessMode.READ_ONLY)
				.unit(Unit.WATT_HOURS) //
				.persistencePriority(PersistencePriority.VERY_LOW)), // always 0
		
		// total reactive energy exported to Grid
		RE_GRID_TOTAL(Doc.of(OpenemsType.LONG) //
				.accessMode(AccessMode.READ_ONLY)
				.unit(Unit.KILOWATT_HOURS) //
				.persistencePriority(PersistencePriority.VERY_LOW)), // always 0		
				
		GRID_VOLTAGE_L1(Doc.of(OpenemsType.INTEGER) //
				.unit(Unit.MILLIVOLT) //
				.persistencePriority(PersistencePriority.HIGH)), //		

		GRID_VOLTAGE_L2(Doc.of(OpenemsType.INTEGER) //
				.unit(Unit.MILLIVOLT) //
				.persistencePriority(PersistencePriority.HIGH)), //		

		GRID_VOLTAGE_L3(Doc.of(OpenemsType.INTEGER) //
				.unit(Unit.MILLIVOLT) //
				.persistencePriority(PersistencePriority.HIGH)), //	
		
		GRID_CURRENT_L1(Doc.of(OpenemsType.INTEGER) //
				.unit(Unit.MILLIAMPERE) //
				.persistencePriority(PersistencePriority.HIGH)), //		
		
		GRID_CURRENT_L2(Doc.of(OpenemsType.INTEGER) //
				.unit(Unit.MILLIAMPERE) //
				.persistencePriority(PersistencePriority.HIGH)), //		
		
		GRID_CURRENT_L3(Doc.of(OpenemsType.INTEGER) //
				.unit(Unit.MILLIAMPERE) //
				.persistencePriority(PersistencePriority.HIGH)), //
		
		GRID_FREQUENCY(Doc.of(OpenemsType.INTEGER) //
				.unit(Unit.MILLIHERTZ) //
				.persistencePriority(PersistencePriority.HIGH)),
		
		/* active power on all phases  calculated as the sum of phases power */
		GRID_POWER(Doc.of(OpenemsType.LONG) //
				.unit(Unit.WATT) //
				.persistencePriority(PersistencePriority.HIGH)), //	
		
		/* apparent power on all phases */
		GRID_APPARENT_POWER(Doc.of(OpenemsType.LONG) //
				.unit(Unit.VOLT_AMPERE) //
				.persistencePriority(PersistencePriority.HIGH)), //	

		/* calculated */
		GRID_POWER_L1(Doc.of(OpenemsType.INTEGER) //
				.unit(Unit.WATT) //
				.persistencePriority(PersistencePriority.HIGH)), //		
		
		/* calculated */
		GRID_POWER_L2(Doc.of(OpenemsType.INTEGER) //
				.unit(Unit.WATT) //
				.persistencePriority(PersistencePriority.HIGH)), //	
		
		/* calculated */
		GRID_POWER_L3(Doc.of(OpenemsType.INTEGER) //
				.unit(Unit.WATT) //
				.persistencePriority(PersistencePriority.HIGH)), //			
		
		/* reactive power on all phases */
		GRID_REACTIVE_POWER(Doc.of(OpenemsType.LONG) //
				.accessMode(AccessMode.READ_ONLY)
				.unit(Unit.VOLT_AMPERE_REACTIVE)
				.persistencePriority(PersistencePriority.HIGH)), // 
		
		GRID_COS_PHI(Doc.of(OpenemsType.INTEGER)
				.accessMode(AccessMode.READ_ONLY)
				.persistencePriority(PersistencePriority.HIGH)), //	
		
		DC_TRANS_TEMP(Doc.of(OpenemsType.INTEGER) //
				.unit(Unit.DEZIDEGREE_CELSIUS) //
				.persistencePriority(PersistencePriority.HIGH)), //
		
		IGBT_TEMP(Doc.of(OpenemsType.INTEGER) //
				.unit(Unit.DEZIDEGREE_CELSIUS) //
				.persistencePriority(PersistencePriority.HIGH)), //		
		
		HEAT_SINK_TEMP(Doc.of(OpenemsType.INTEGER) //
				.unit(Unit.DEZIDEGREE_CELSIUS) //
				.persistencePriority(PersistencePriority.HIGH)), //		
		
		/* Leakage Current */
		GFCI(Doc.of(OpenemsType.INTEGER) //
				.unit(Unit.MILLIAMPERE) //
				.persistencePriority(PersistencePriority.HIGH)), //	
				
		// energy generated from PV all strings
		E_PV_TODAY(Doc.of(OpenemsType.INTEGER) //
				.accessMode(AccessMode.READ_ONLY)
				.unit(Unit.WATT_HOURS) //
				.persistencePriority(PersistencePriority.HIGH)),	

		// total energy generated from PV all strings
		E_PV_TOTAL(Doc.of(OpenemsType.LONG) //
				.accessMode(AccessMode.READ_ONLY)
				.unit(Unit.KILOWATT_HOURS) //
				.persistencePriority(PersistencePriority.LOW)),	
		
		// calculated sum of PVx power
		POWER_PV(Doc.of(OpenemsType.INTEGER) //
				.accessMode(AccessMode.READ_ONLY)
				.unit(Unit.WATT) //
				.persistencePriority(PersistencePriority.HIGH)),
		
		/* calculated */
		POWER_PV1(Doc.of(OpenemsType.INTEGER) //
				.accessMode(AccessMode.READ_ONLY)
				.unit(Unit.WATT) //
				.persistencePriority(PersistencePriority.HIGH)),

		/* calculated */
		POWER_PV2(Doc.of(OpenemsType.INTEGER) //
				.accessMode(AccessMode.READ_ONLY)
				.unit(Unit.WATT) //
				.persistencePriority(PersistencePriority.HIGH)),

		CURRENT_PV1(Doc.of(OpenemsType.INTEGER) //
				.accessMode(AccessMode.READ_ONLY)
				.unit(Unit.MILLIAMPERE) //
				.persistencePriority(PersistencePriority.HIGH)),
		
		VOLTAGE_PV1(Doc.of(OpenemsType.INTEGER) //
				.accessMode(AccessMode.READ_ONLY)
				.unit(Unit.MILLIVOLT) //
				.persistencePriority(PersistencePriority.HIGH)),

		CURRENT_PV2(Doc.of(OpenemsType.INTEGER) //
				.accessMode(AccessMode.READ_ONLY)
				.unit(Unit.MILLIAMPERE) //
				.persistencePriority(PersistencePriority.HIGH)),
		
		VOLTAGE_PV2(Doc.of(OpenemsType.INTEGER) //
				.accessMode(AccessMode.READ_ONLY)
				.unit(Unit.MILLIVOLT) //
				.persistencePriority(PersistencePriority.HIGH)),
		
		/*
		 * used during implementation for quick test 
		 */				
		TEST_INT(Doc.of(OpenemsType.INTEGER) //
				.persistencePriority(PersistencePriority.VERY_LOW)), //	
		
		TEST_DOUBLE(Doc.of(OpenemsType.DOUBLE) //
				.persistencePriority(PersistencePriority.VERY_LOW)), //	
		
		TEST_LONG(Doc.of(OpenemsType.LONG) //
				.persistencePriority(PersistencePriority.VERY_LOW)), //	
		
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
	
	public default IntegerReadChannel getVoltagePv1Channel() {
		return this.channel(ChannelId.VOLTAGE_PV1);
	}
	
	public default IntegerReadChannel getCurrentPv1Channel() {
		return this.channel(ChannelId.CURRENT_PV1);
	}
	
	public default IntegerReadChannel getPowerPv1Channel() {
		return this.channel(ChannelId.POWER_PV1);
	}
	
	public default IntegerReadChannel getVoltagePv2Channel() {
		return this.channel(ChannelId.VOLTAGE_PV2);
	}
	
	public default IntegerReadChannel getCurrentPv2Channel() {
		return this.channel(ChannelId.CURRENT_PV2);
	}
	
	public default IntegerReadChannel getPowerPv2Channel() {
		return this.channel(ChannelId.POWER_PV2);
	}	
	
	public default IntegerReadChannel getPowerPvChannel() {
		return this.channel(ChannelId.POWER_PV);
	}
	
	public default IntegerReadChannel getGridVoltageL1Channel() {
		return this.channel(ChannelId.GRID_VOLTAGE_L1);
	}
	
	public default IntegerReadChannel getGridCurrentL1Channel() {
		return this.channel(ChannelId.GRID_CURRENT_L1);
	}
	
	public default IntegerReadChannel getGridPowerL1Channel() {
		return this.channel(ChannelId.GRID_POWER_L1);
	}	
	
	public default IntegerReadChannel getGridVoltageL2Channel() {
		return this.channel(ChannelId.GRID_VOLTAGE_L2);
	}
	
	public default IntegerReadChannel getGridCurrentL2Channel() {
		return this.channel(ChannelId.GRID_CURRENT_L2);
	}
	
	public default IntegerReadChannel getGridPowerL2Channel() {
		return this.channel(ChannelId.GRID_POWER_L2);
	}	
	
	public default IntegerReadChannel getGridVoltageL3Channel() {
		return this.channel(ChannelId.GRID_VOLTAGE_L3);
	}
	
	public default IntegerReadChannel getGridCurrentL3Channel() {
		return this.channel(ChannelId.GRID_CURRENT_L3);
	}
	
	public default IntegerReadChannel getGridPowerL3Channel() {
		return this.channel(ChannelId.GRID_POWER_L3);	
	}
	
	public default LongReadChannel getGridPowerChannel() {
		return this.channel(ChannelId.GRID_POWER);	
	}
	
	public default IntegerReadChannel getGridCosPhiChannel() {
		return this.channel(ChannelId.GRID_COS_PHI);
	}
	
	public default LongReadChannel getGridReactivePowerChannel() {
		return this.channel(ChannelId.GRID_REACTIVE_POWER);
	}	
}
