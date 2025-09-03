package io.openems.edge.aohai.hybrid;

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

import io.openems.edge.battery.api.*;

public interface AohaiHybridInverter extends ElectricityMeter, ModbusComponent, OpenemsComponent {

	public enum ChannelId implements io.openems.edge.common.channel.ChannelId {
		
		INVERTER_RUN_STATE(Doc.of(OpenemsType.INTEGER)
				.persistencePriority(PersistencePriority.HIGH)), //
		
		INV_VOLTAGE_L1(Doc.of(OpenemsType.INTEGER) //
				.unit(Unit.MILLIVOLT) //
				.persistencePriority(PersistencePriority.HIGH)), //		
		
		INV_VOLTAGE_L2(Doc.of(OpenemsType.INTEGER) //
				.unit(Unit.MILLIVOLT) //
				.persistencePriority(PersistencePriority.HIGH)), //	
		
		INV_VOLTAGE_L3(Doc.of(OpenemsType.INTEGER) //
				.unit(Unit.MILLIVOLT) //
				.persistencePriority(PersistencePriority.HIGH)), //	
		
		/*
		 * A positive current value indicates that the current is flowing to the grid, 
		 * and a negative current value indicates that the current is coming from the grid.
		 * INV_CURRENT_L1-3
		 */		
		INV_CURRENT_L1(Doc.of(OpenemsType.INTEGER) //
				.unit(Unit.MILLIAMPERE) //
				.persistencePriority(PersistencePriority.HIGH)), //
		INV_CURRENT_L2(Doc.of(OpenemsType.INTEGER) //
				.unit(Unit.MILLIAMPERE) //
				.persistencePriority(PersistencePriority.HIGH)), //
		INV_CURRENT_L3(Doc.of(OpenemsType.INTEGER) //
				.unit(Unit.MILLIAMPERE) //
				.persistencePriority(PersistencePriority.HIGH)), //
		
		/**
		 * State of Charge.
		 *
		 * <ul>
		 * <li>Type: Integer
		 * <li>Unit: %
		 * <li>Range: 0..100
		 * </ul>
		 */
		BAT_SOC(Doc.of(OpenemsType.INTEGER) //
				.unit(Unit.PERCENT) //
				.persistencePriority(PersistencePriority.HIGH)), //

		/**
		 * State of Health.
		 *
		 * <ul>
		 * <li>Type: Integer
		 * <li>Unit: %
		 * <li>Range: 0..100
		 * </ul>
		 */
		BAT_SOH(Doc.of(OpenemsType.INTEGER) //
				.unit(Unit.PERCENT) //
				.persistencePriority(PersistencePriority.HIGH)), //

		/**
		 * Voltage of battery.
		 *
		 * <ul>
		 * <li>Type: Integer
		 * <li>Unit: V
		 * </ul>
		 */
		BAT_VOLTAGE(Doc.of(OpenemsType.INTEGER) //
				.unit(Unit.MILLIVOLT) //
				.persistencePriority(PersistencePriority.HIGH)), //
		
		BAT_CURRENT(Doc.of(OpenemsType.INTEGER) //
				.unit(Unit.MILLIAMPERE) //
				.persistencePriority(PersistencePriority.HIGH)), //
		
		BAT_POWER(Doc.of(OpenemsType.INTEGER) //
				.unit(Unit.WATT) //
				.persistencePriority(PersistencePriority.HIGH)), //
		
		BAT_TEMP(Doc.of(OpenemsType.INTEGER) //
				.unit(Unit.DEZIDEGREE_CELSIUS) //
				.persistencePriority(PersistencePriority.HIGH)), //
		
		BAT_CAPACITY(Doc.of(OpenemsType.INTEGER) //
				.unit(Unit.AMPERE_HOURS) //
				.persistencePriority(PersistencePriority.HIGH)), //
		
		BAT_CHARGE_TODAY(Doc.of(OpenemsType.INTEGER) //
				.accessMode(AccessMode.READ_ONLY)
				.unit(Unit.WATT_HOURS) //
				.persistencePriority(PersistencePriority.HIGH)),

		BAT_DISCHARGE_TODAY(Doc.of(OpenemsType.INTEGER) //
				.accessMode(AccessMode.READ_ONLY)
				.unit(Unit.WATT_HOURS) //
				.persistencePriority(PersistencePriority.HIGH)),

		ACTIVE_ENERGY_GEN_TODAY(Doc.of(OpenemsType.INTEGER) //
				.accessMode(AccessMode.READ_ONLY)
				.unit(Unit.WATT_HOURS) //
				.persistencePriority(PersistencePriority.HIGH)),
		
		REACTIVE_ENERGY_GEN_TODAY(Doc.of(OpenemsType.INTEGER) //
				.accessMode(AccessMode.READ_ONLY)
				.unit(Unit.WATT_HOURS) //
				.persistencePriority(PersistencePriority.HIGH)),		
		
		E_GRID_SELL_TODAY(Doc.of(OpenemsType.INTEGER) //
				.accessMode(AccessMode.READ_ONLY)
				.unit(Unit.WATT_HOURS) //
				.persistencePriority(PersistencePriority.HIGH)),		

		E_GRID_BUY_TODAY(Doc.of(OpenemsType.INTEGER) //
				.accessMode(AccessMode.READ_ONLY)
				.unit(Unit.WATT_HOURS) //
				.persistencePriority(PersistencePriority.HIGH)),		

		// energy supplied to Load
		E_LOAD_TODAY(Doc.of(OpenemsType.INTEGER) //
				.accessMode(AccessMode.READ_ONLY)
				.unit(Unit.WATT_HOURS) //
				.persistencePriority(PersistencePriority.HIGH)),			

		// energy generated from PV all strings
		E_PV_TODAY(Doc.of(OpenemsType.INTEGER) //
				.accessMode(AccessMode.READ_ONLY)
				.unit(Unit.WATT_HOURS) //
				.persistencePriority(PersistencePriority.HIGH)),	
		
		// energy generated from PV1
		E_PV1_TODAY(Doc.of(OpenemsType.INTEGER) //
				.accessMode(AccessMode.READ_ONLY)
				.unit(Unit.WATT_HOURS) //
				.persistencePriority(PersistencePriority.HIGH)),	

		// energy generated from PV2
		E_PV2_TODAY(Doc.of(OpenemsType.INTEGER) //
				.accessMode(AccessMode.READ_ONLY)
				.unit(Unit.WATT_HOURS) //
				.persistencePriority(PersistencePriority.HIGH)),	

		// energy generated from PV4
		E_PV3_TODAY(Doc.of(OpenemsType.INTEGER) //
				.accessMode(AccessMode.READ_ONLY)
				.unit(Unit.WATT_HOURS) //
				.persistencePriority(PersistencePriority.HIGH)),	

		// energy generated from PV4
		E_PV4_TODAY(Doc.of(OpenemsType.INTEGER) //
				.accessMode(AccessMode.READ_ONLY)
				.unit(Unit.WATT_HOURS) //
				.persistencePriority(PersistencePriority.HIGH)),
		
		// sum of PVx power
		POWER_PV(Doc.of(OpenemsType.INTEGER) //
				.accessMode(AccessMode.READ_ONLY)
				.unit(Unit.WATT) //
				.persistencePriority(PersistencePriority.HIGH)),
		
		POWER_PV1(Doc.of(OpenemsType.INTEGER) //
				.accessMode(AccessMode.READ_ONLY)
				.unit(Unit.WATT) //
				.persistencePriority(PersistencePriority.HIGH)),

		POWER_PV2(Doc.of(OpenemsType.INTEGER) //
				.accessMode(AccessMode.READ_ONLY)
				.unit(Unit.WATT) //
				.persistencePriority(PersistencePriority.HIGH)),

		POWER_PV3(Doc.of(OpenemsType.INTEGER) //
				.accessMode(AccessMode.READ_ONLY)
				.unit(Unit.WATT) //
				.persistencePriority(PersistencePriority.HIGH)),

		POWER_PV4(Doc.of(OpenemsType.INTEGER) //
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
		
		CURRENT_PV3(Doc.of(OpenemsType.INTEGER) //
				.accessMode(AccessMode.READ_ONLY)
				.unit(Unit.MILLIAMPERE) //
				.persistencePriority(PersistencePriority.HIGH)),
		
		VOLTAGE_PV3(Doc.of(OpenemsType.INTEGER) //
				.accessMode(AccessMode.READ_ONLY)
				.unit(Unit.MILLIVOLT) //
				.persistencePriority(PersistencePriority.HIGH)),
		
		CURRENT_PV4(Doc.of(OpenemsType.INTEGER) //
				.accessMode(AccessMode.READ_ONLY)
				.unit(Unit.MILLIAMPERE) //
				.persistencePriority(PersistencePriority.HIGH)),
		
		VOLTAGE_PV4(Doc.of(OpenemsType.INTEGER) //
				.accessMode(AccessMode.READ_ONLY)
				.unit(Unit.MILLIVOLT) //
				.persistencePriority(PersistencePriority.HIGH)),
		
		
		DC_TRANSFORMER_TEMP(Doc.of(OpenemsType.INTEGER) //
				.unit(Unit.DEZIDEGREE_CELSIUS) //
				.persistencePriority(PersistencePriority.HIGH)), //
		
		HEAT_SINK_TEMP(Doc.of(OpenemsType.INTEGER) //
				.unit(Unit.DEZIDEGREE_CELSIUS) //
				.persistencePriority(PersistencePriority.HIGH)), //		
		
		GRID_VOLTAGE_L1(Doc.of(OpenemsType.INTEGER) //
				.unit(Unit.MILLIVOLT) //
				.persistencePriority(PersistencePriority.HIGH)), //		

		GRID_VOLTAGE_L2(Doc.of(OpenemsType.INTEGER) //
				.unit(Unit.MILLIVOLT) //
				.persistencePriority(PersistencePriority.HIGH)), //		

		GRID_VOLTAGE_L3(Doc.of(OpenemsType.INTEGER) //
				.unit(Unit.MILLIVOLT) //
				.persistencePriority(PersistencePriority.HIGH)), //		
		
		GRID_VOLTAGE_L1_L2(Doc.of(OpenemsType.INTEGER) //
				.accessMode(AccessMode.READ_ONLY)
				.unit(Unit.VOLT)
				.persistencePriority(PersistencePriority.MEDIUM)), //
		GRID_VOLTAGE_L2_L3(Doc.of(OpenemsType.INTEGER) //
				.accessMode(AccessMode.READ_ONLY)
				.unit(Unit.VOLT)
				.persistencePriority(PersistencePriority.MEDIUM)), //
		GRID_VOLTAGE_L3_L1(Doc.of(OpenemsType.INTEGER) //
				.accessMode(AccessMode.READ_ONLY)
				.unit(Unit.VOLT)
				.persistencePriority(PersistencePriority.MEDIUM)), //	
		
		GRID_POWER_L1(Doc.of(OpenemsType.INTEGER) //
				.unit(Unit.WATT) //
				.persistencePriority(PersistencePriority.HIGH)), //		
		
		GRID_POWER_L2(Doc.of(OpenemsType.INTEGER) //
				.unit(Unit.WATT) //
				.persistencePriority(PersistencePriority.HIGH)), //	
		
		GRID_POWER_L3(Doc.of(OpenemsType.INTEGER) //
				.unit(Unit.WATT) //
				.persistencePriority(PersistencePriority.HIGH)), //			
		
		GRID_POWER(Doc.of(OpenemsType.INTEGER) //
				.unit(Unit.WATT) //
				.persistencePriority(PersistencePriority.HIGH)), //			
		
		GRID_FREQUENCY(Doc.of(OpenemsType.INTEGER) //
				.unit(Unit.MILLIHERTZ) //
				.persistencePriority(PersistencePriority.HIGH)),
		
		GRID_REACTIVE_POWER(Doc.of(OpenemsType.INTEGER) //
				.accessMode(AccessMode.READ_ONLY)
				.unit(Unit.VOLT_AMPERE_REACTIVE)
				.persistencePriority(PersistencePriority.MEDIUM)), //
		
		GRID_CURRENT_L1(Doc.of(OpenemsType.INTEGER) //
				.unit(Unit.MILLIAMPERE) //
				.persistencePriority(PersistencePriority.HIGH)), //		
		
		GRID_CURRENT_L2(Doc.of(OpenemsType.INTEGER) //
				.unit(Unit.MILLIAMPERE) //
				.persistencePriority(PersistencePriority.HIGH)), //		
		
		GRID_CURRENT_L3(Doc.of(OpenemsType.INTEGER) //
				.unit(Unit.MILLIAMPERE) //
				.persistencePriority(PersistencePriority.HIGH)), //		
		
		// EXT: probably it is measured by CT
		
		GRID_EXT_CURRENT_L1(Doc.of(OpenemsType.INTEGER) //
				.unit(Unit.MILLIAMPERE) //
				.persistencePriority(PersistencePriority.HIGH)), //		
		
		GRID_EXT_CURRENT_L2(Doc.of(OpenemsType.INTEGER) //
				.unit(Unit.MILLIAMPERE) //
				.persistencePriority(PersistencePriority.HIGH)), //		
		
		GRID_EXT_CURRENT_L3(Doc.of(OpenemsType.INTEGER) //
				.unit(Unit.MILLIAMPERE) //
				.persistencePriority(PersistencePriority.HIGH)), //		
		
		GRID_EXT_POWER_L1(Doc.of(OpenemsType.INTEGER) //
				.unit(Unit.WATT) //
				.persistencePriority(PersistencePriority.HIGH)), //		
		
		GRID_EXT_POWER_L2(Doc.of(OpenemsType.INTEGER) //
				.unit(Unit.WATT) //
				.persistencePriority(PersistencePriority.HIGH)), //	
		
		GRID_EXT_POWER_L3(Doc.of(OpenemsType.INTEGER) //
				.unit(Unit.WATT) //
				.persistencePriority(PersistencePriority.HIGH)), //			
		
		GRID_EXT_POWER(Doc.of(OpenemsType.INTEGER) //
				.unit(Unit.WATT) //
				.persistencePriority(PersistencePriority.HIGH)), //		
		
		GRID_EXT_REACTIVE_POWER(Doc.of(OpenemsType.INTEGER) //
				.accessMode(AccessMode.READ_ONLY)
				.unit(Unit.VOLT_AMPERE_REACTIVE)
				.persistencePriority(PersistencePriority.MEDIUM)), //
		
		GRID_COS_PHI(Doc.of(OpenemsType.FLOAT)
				.accessMode(AccessMode.READ_ONLY)
				.persistencePriority(PersistencePriority.HIGH)), //		
		
		// TODO not needed?
		GRID_COS_PHI_INT(Doc.of(OpenemsType.INTEGER)
				.accessMode(AccessMode.READ_ONLY)
				.persistencePriority(PersistencePriority.HIGH)), //		
		
		
		INV_OUT_POWER(Doc.of(OpenemsType.INTEGER) //
				.unit(Unit.WATT) //
				.persistencePriority(PersistencePriority.HIGH)), //	
		
		INV_OUT_REACTIVE_POWER(Doc.of(OpenemsType.INTEGER) //
				.unit(Unit.VOLT_AMPERE_REACTIVE) //
				.persistencePriority(PersistencePriority.MEDIUM)), //	
		
		UPS_LOAD_POWER(Doc.of(OpenemsType.INTEGER) //
				.unit(Unit.WATT) //
				.persistencePriority(PersistencePriority.HIGH)), //	

		LOAD_POWER(Doc.of(OpenemsType.INTEGER) //
				.unit(Unit.WATT) //
				.persistencePriority(PersistencePriority.HIGH)), //	

		LOAD_REACTIVE_POWER(Doc.of(OpenemsType.INTEGER) //
				.unit(Unit.VOLT_AMPERE_REACTIVE) //
				.persistencePriority(PersistencePriority.MEDIUM)) //	
		
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
