package io.openems.edge.xindun;

import io.openems.common.channel.AccessMode;
import io.openems.common.channel.PersistencePriority;
import io.openems.common.channel.Unit;
import io.openems.common.types.OpenemsType;
import io.openems.edge.bridge.modbus.api.ModbusComponent;
import io.openems.edge.common.channel.Doc;
import io.openems.edge.common.component.OpenemsComponent;
import io.openems.edge.meter.api.ElectricityMeter;

public interface XindunHfpInverter extends ElectricityMeter, ModbusComponent, OpenemsComponent {

	public enum ChannelId implements io.openems.edge.common.channel.ChannelId {
		
		// device type
		TYPE(Doc.of(OpenemsType.INTEGER)
				.persistencePriority(PersistencePriority.VERY_LOW)), //
		
		// serial number
		SN(Doc.of(OpenemsType.STRING)
				.persistencePriority(PersistencePriority.VERY_LOW)), //
		
		/*
		0x00:Waiting state
		0x01:Grid-connected state
		0x02:Off-grid status
		0x03:Fault status
		0x04:Burn-in status
		0x05:Bypass Status
		0x06:Self-charging status
		*/
		INV_STATUS(Doc.of(OpenemsType.INTEGER)
				.persistencePriority(PersistencePriority.HIGH)),
			
		INV_CURRENT(Doc.of(OpenemsType.INTEGER) //
				.unit(Unit.MILLIAMPERE) //
				.persistencePriority(PersistencePriority.HIGH)), //		
		
		INV_VOLTAGE(Doc.of(OpenemsType.INTEGER) //
				.unit(Unit.MILLIVOLT) //
				.persistencePriority(PersistencePriority.HIGH)), //	
		
		BUS_VOLTAGE(Doc.of(OpenemsType.INTEGER) //
				.unit(Unit.MILLIVOLT) //
				.persistencePriority(PersistencePriority.HIGH)), //	
		
		GRID_VOLTAGE(Doc.of(OpenemsType.INTEGER) //
				.unit(Unit.MILLIVOLT) //
				.persistencePriority(PersistencePriority.HIGH)), //		
		
		LOAD_CURRENT(Doc.of(OpenemsType.INTEGER) //
				.unit(Unit.MILLIAMPERE) //
				.persistencePriority(PersistencePriority.HIGH)), //		
		
		GRID_CURRENT(Doc.of(OpenemsType.INTEGER) //
				.unit(Unit.MILLIAMPERE) //
				.persistencePriority(PersistencePriority.HIGH)), //		
		
		BUS_CURRENT(Doc.of(OpenemsType.INTEGER) //
				.unit(Unit.MILLIAMPERE) //
				.persistencePriority(PersistencePriority.HIGH)), //	
		
		
		INV_TEMP(Doc.of(OpenemsType.INTEGER) //
				.unit(Unit.DEZIDEGREE_CELSIUS) //
				.persistencePriority(PersistencePriority.HIGH)), //
		
		/* section AC-DC */
		INV_IPM_TEMP(Doc.of(OpenemsType.INTEGER) //
				.unit(Unit.DEZIDEGREE_CELSIUS) //
				.persistencePriority(PersistencePriority.HIGH)), //
		
		/* section DC-DC */
		INV_LLC_TEMP(Doc.of(OpenemsType.INTEGER) //
				.unit(Unit.DEZIDEGREE_CELSIUS) //
				.persistencePriority(PersistencePriority.HIGH)), //
		
		
		
		
		
		/* Master inverter error code */
		ERROR_CODE(Doc.of(OpenemsType.INTEGER)
				.persistencePriority(PersistencePriority.HIGH)),
		
		/* Master inverter warning code */
		WARN_CODE(Doc.of(OpenemsType.INTEGER)
				.persistencePriority(PersistencePriority.HIGH)),

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
		
		// TODO MARCIN
		
		/*
		 * 0: No load shedding 
		 * 1: Bus high voltage 
		 * 2: Grid low voltage 
		 * 3: Grid high voltage
		 * 4: High frequency
		 * 5: BOOST high temperature
		 * 6: Inverter high temperature
		 * 7: Environmental high temperature
		 * 8: loading speed
		 * 9: generating reactive power
		 * 10: excessive load
		 * 11: under-frequency loading
		 * 12: Active setting limit
		 * 13: Multi-machine anti-reverse current
		 * 14: Single-machine anti-reverse current
		 * 15: Zero current mode
		 * 16: Aging setting limit
		 * 17: Line impedance limit
		 * 18: Fan abnormality
		 * 19: CT abnormality
		 * 20: LLC over-temperature
		 * 21: Battery discharge setting limit
		 * 22: Power sales setting limit
		 * 23: PV power over range
		 */
		DERATING_MODE(Doc.of(OpenemsType.INTEGER) //				
				.persistencePriority(PersistencePriority.HIGH)), //

		BUS1_VOLTAGE(Doc.of(OpenemsType.INTEGER) //
				.unit(Unit.MILLIVOLT) //
				.persistencePriority(PersistencePriority.HIGH)), //	

		/* always 0 */
		BUS2_VOLTAGE(Doc.of(OpenemsType.INTEGER) //
				.unit(Unit.MILLIVOLT) //
				.persistencePriority(PersistencePriority.LOW)), //	

			
		/*
		 * in unit of 0.0001, e.g. returned -9998 = -0.9998
		 */
		GRID_COS_PHI(Doc.of(OpenemsType.INTEGER)
				.accessMode(AccessMode.READ_ONLY)				
				.persistencePriority(PersistencePriority.HIGH)), //	
		
		GRID_FREQUENCY(Doc.of(OpenemsType.INTEGER) //
				.unit(Unit.MILLIHERTZ) //
				.persistencePriority(PersistencePriority.HIGH)),
		
		EPS_VOLTAGE_L1(Doc.of(OpenemsType.INTEGER) //
				.unit(Unit.MILLIVOLT) //
				.persistencePriority(PersistencePriority.HIGH)), //		
		
		EPS_VOLTAGE_L2(Doc.of(OpenemsType.INTEGER) //
				.unit(Unit.MILLIVOLT) //
				.persistencePriority(PersistencePriority.HIGH)), //	
		
		EPS_VOLTAGE_L3(Doc.of(OpenemsType.INTEGER) //
				.unit(Unit.MILLIVOLT) //
				.persistencePriority(PersistencePriority.HIGH)), //	
		
		EPS_CURRENT_L1(Doc.of(OpenemsType.INTEGER) //
				.unit(Unit.MILLIAMPERE) //
				.persistencePriority(PersistencePriority.HIGH)), //		
		
		EPS_CURRENT_L2(Doc.of(OpenemsType.INTEGER) //
				.unit(Unit.MILLIAMPERE) //
				.persistencePriority(PersistencePriority.HIGH)), //		
		
		EPS_CURRENT_L3(Doc.of(OpenemsType.INTEGER) //
				.unit(Unit.MILLIAMPERE) //
				.persistencePriority(PersistencePriority.HIGH)), //	
		
		EPS_FREQUENCY(Doc.of(OpenemsType.INTEGER) //
				.unit(Unit.MILLIHERTZ) //
				.persistencePriority(PersistencePriority.HIGH)),
		
		MPPT_CNT(Doc.of(OpenemsType.INTEGER) //
				.accessMode(AccessMode.READ_ONLY)
				.persistencePriority(PersistencePriority.LOW)),
		
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
		
		/*
		 * 0: Load priority
		 * 1: Battery priority
		 * 2: Grid priority
		 */
		INV_PRIORITY(Doc.of(OpenemsType.INTEGER) //
				.persistencePriority(PersistencePriority.HIGH)), //
		
		
		ISO_RESISTANCE(Doc.of(OpenemsType.INTEGER) //
				.unit(Unit.KILOOHM) //
				.persistencePriority(PersistencePriority.HIGH)), //
		
		/* Leakage Current */
		GFCI(Doc.of(OpenemsType.INTEGER) //
				.unit(Unit.MILLIAMPERE) //
				.persistencePriority(PersistencePriority.HIGH)), //		
		
		
		
		
		/* current value */
		BAT_CHARGE_POWER(Doc.of(OpenemsType.INTEGER) //
				.unit(Unit.WATT) //
				.persistencePriority(PersistencePriority.HIGH)), //

		
		
		// review BAT
		/*
		 * 0: Lead-acid batteries
		 * 1: Lithium batteries
		 * 2: User defined 1
		 * 3: User defined 2
		 * 4: User Defined 3
		 */
		BAT_TYPE(Doc.of(OpenemsType.INTEGER) //
				.persistencePriority(PersistencePriority.HIGH)), //
		
		BMS_STATUS(Doc.of(OpenemsType.INTEGER) //				
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

		BAT_CYCLE_CNT(Doc.of(OpenemsType.INTEGER) //
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
		
		BAT_VOLTAGE_DSP(Doc.of(OpenemsType.INTEGER) //
				.unit(Unit.MILLIVOLT) //
				.persistencePriority(PersistencePriority.HIGH)), //
		
		BAT_CURRENT(Doc.of(OpenemsType.INTEGER) //
				.unit(Unit.MILLIAMPERE) //
				.persistencePriority(PersistencePriority.HIGH)), //
		

		/* current value */
		BAT_AC_CHARGE_POWER(Doc.of(OpenemsType.LONG) //
				.unit(Unit.WATT) //
				.persistencePriority(PersistencePriority.HIGH)), //

		/* current value */
		BAT_DISCHARGE_POWER(Doc.of(OpenemsType.LONG) //
				.unit(Unit.WATT) //
				.persistencePriority(PersistencePriority.HIGH)), //

		BAT_TEMP(Doc.of(OpenemsType.INTEGER) //
				.unit(Unit.DEZIDEGREE_CELSIUS) //
				.persistencePriority(PersistencePriority.HIGH)), //
		
		/* not changing often */
		BAT_CAPACITY(Doc.of(OpenemsType.INTEGER) //
				.unit(Unit.AMPERE_HOURS) //
				.persistencePriority(PersistencePriority.LOW)), //
				
		/* energy sent today to battery */
		BAT_CHARGE_TODAY(Doc.of(OpenemsType.LONG) //
				.accessMode(AccessMode.READ_ONLY)
				.unit(Unit.WATT_HOURS) //
				.persistencePriority(PersistencePriority.HIGH)),
		
		/* total energy sent to battery */
		BAT_CHARGE_TOTAL(Doc.of(OpenemsType.LONG) //
				.accessMode(AccessMode.READ_ONLY)
				.unit(Unit.WATT_HOURS) //
				.persistencePriority(PersistencePriority.LOW)),
		
		/* energy sent today to battery from grid */
		BAT_AC_CHARGE_TODAY(Doc.of(OpenemsType.LONG) //
				.accessMode(AccessMode.READ_ONLY)
				.unit(Unit.WATT_HOURS) //
				.persistencePriority(PersistencePriority.HIGH)),
		
		/* total energy sent to battery from grid */
		BAT_AC_CHARGE_TOTAL(Doc.of(OpenemsType.LONG) //
				.accessMode(AccessMode.READ_ONLY)
				.unit(Unit.WATT_HOURS) //
				.persistencePriority(PersistencePriority.LOW)),
		
		/* energy taken today from battery */
		BAT_DISCHARGE_TODAY(Doc.of(OpenemsType.LONG) //
				.accessMode(AccessMode.READ_ONLY)
				.unit(Unit.WATT_HOURS) //
				.persistencePriority(PersistencePriority.HIGH)),
		
		/* total energy taken from battery */
		BAT_DISCHARGE_TOTAL(Doc.of(OpenemsType.LONG) //
				.accessMode(AccessMode.READ_ONLY)
				.unit(Unit.WATT_HOURS) //
				.persistencePriority(PersistencePriority.LOW)),

		// energy generated from PV all strings
		E_PV_TODAY(Doc.of(OpenemsType.LONG) //
				.accessMode(AccessMode.READ_ONLY)
				.unit(Unit.WATT_HOURS) //
				.persistencePriority(PersistencePriority.HIGH)),
		
		// total energy generated from PV all strings
		E_PV_TOTAL(Doc.of(OpenemsType.LONG) //
				.accessMode(AccessMode.READ_ONLY)
				.unit(Unit.WATT_HOURS) //
				.persistencePriority(PersistencePriority.LOW)),	
		
		// total energy exported to Grid
		E_GRID_SELL_TOTAL(Doc.of(OpenemsType.LONG) //
				.accessMode(AccessMode.READ_ONLY)
				.unit(Unit.WATT_HOURS) //
				.persistencePriority(PersistencePriority.HIGH)),		

		// total energy imported from Grid
		E_GRID_BUY_TOTAL(Doc.of(OpenemsType.INTEGER) //
				.accessMode(AccessMode.READ_ONLY)
				.unit(Unit.KILOWATT_HOURS) //
				.persistencePriority(PersistencePriority.HIGH)),
		
		POWER_PV(Doc.of(OpenemsType.LONG) //
				.accessMode(AccessMode.READ_ONLY)
				.unit(Unit.WATT) //
				.persistencePriority(PersistencePriority.HIGH)),
		
		POWER_PV1(Doc.of(OpenemsType.LONG) //
				.accessMode(AccessMode.READ_ONLY)
				.unit(Unit.WATT) //
				.persistencePriority(PersistencePriority.HIGH)),

		POWER_PV2(Doc.of(OpenemsType.LONG) //
				.accessMode(AccessMode.READ_ONLY)
				.unit(Unit.WATT) //
				.persistencePriority(PersistencePriority.HIGH)),

		POWER_PV3(Doc.of(OpenemsType.LONG) //
				.accessMode(AccessMode.READ_ONLY)
				.unit(Unit.WATT) //
				.persistencePriority(PersistencePriority.HIGH)),

		POWER_PV4(Doc.of(OpenemsType.LONG) //
				.accessMode(AccessMode.READ_ONLY)
				.unit(Unit.WATT) //
				.persistencePriority(PersistencePriority.HIGH)),

		GRID_POWER(Doc.of(OpenemsType.INTEGER) //
				.unit(Unit.WATT) //
				.persistencePriority(PersistencePriority.HIGH)), //		

	

		// TODO SUM
		EPS_POWER(Doc.of(OpenemsType.LONG) //
				.unit(Unit.WATT) //
				.persistencePriority(PersistencePriority.HIGH)), //		

		EPS_POWER_L1(Doc.of(OpenemsType.LONG) //
				.unit(Unit.WATT) //
				.persistencePriority(PersistencePriority.HIGH)), //		
		
		EPS_POWER_L2(Doc.of(OpenemsType.LONG) //
				.unit(Unit.WATT) //
				.persistencePriority(PersistencePriority.HIGH)), //	
		
		EPS_POWER_L3(Doc.of(OpenemsType.LONG) //
				.unit(Unit.WATT) //
				.persistencePriority(PersistencePriority.HIGH)), //		
		
		// TODO SUM
		EPS_APPARENT_POWER(Doc.of(OpenemsType.LONG) //
				.unit(Unit.VOLT_AMPERE) //
				.persistencePriority(PersistencePriority.HIGH)), //		

		EPS_APPARENT_POWER_L1(Doc.of(OpenemsType.LONG) //
				.unit(Unit.VOLT_AMPERE) //
				.persistencePriority(PersistencePriority.HIGH)), //		
		
		EPS_APPARENT_POWER_L2(Doc.of(OpenemsType.LONG) //
				.unit(Unit.VOLT_AMPERE) //
				.persistencePriority(PersistencePriority.HIGH)), //	
		
		EPS_APPARENT_POWER_L3(Doc.of(OpenemsType.LONG) //
				.unit(Unit.VOLT_AMPERE) //
				.persistencePriority(PersistencePriority.HIGH)), //	
		
		// TODO SUM
		INV_POWER(Doc.of(OpenemsType.LONG) //
				.unit(Unit.WATT) //
				.persistencePriority(PersistencePriority.HIGH)), //		

		INV_POWER_L1(Doc.of(OpenemsType.LONG) //
				.unit(Unit.WATT) //
				.persistencePriority(PersistencePriority.HIGH)), //		
		
		INV_POWER_L2(Doc.of(OpenemsType.LONG) //
				.unit(Unit.WATT) //
				.persistencePriority(PersistencePriority.HIGH)), //	
		
		INV_POWER_L3(Doc.of(OpenemsType.LONG) //
				.unit(Unit.WATT) //
				.persistencePriority(PersistencePriority.HIGH)), //		
		
		// TODO SUM
		INV_APPARENT_POWER(Doc.of(OpenemsType.LONG) //
				.unit(Unit.VOLT_AMPERE) //
				.persistencePriority(PersistencePriority.HIGH)), //		

		INV_APPARENT_POWER_L1(Doc.of(OpenemsType.LONG) //
				.unit(Unit.VOLT_AMPERE) //
				.persistencePriority(PersistencePriority.HIGH)), //		
		
		INV_APPARENT_POWER_L2(Doc.of(OpenemsType.LONG) //
				.unit(Unit.VOLT_AMPERE) //
				.persistencePriority(PersistencePriority.HIGH)), //	
		
		INV_APPARENT_POWER_L3(Doc.of(OpenemsType.LONG) //
				.unit(Unit.VOLT_AMPERE) //
				.persistencePriority(PersistencePriority.HIGH)), //	
		
		INV_REACTIVE_POWER(Doc.of(OpenemsType.LONG) //
				.unit(Unit.VOLT_AMPERE_REACTIVE) //
				.persistencePriority(PersistencePriority.HIGH)), //		

		INV_REACTIVE_POWER_L1(Doc.of(OpenemsType.LONG) //
				.unit(Unit.VOLT_AMPERE_REACTIVE) //
				.persistencePriority(PersistencePriority.HIGH)), //		
		
		INV_REACTIVE_POWER_L2(Doc.of(OpenemsType.LONG) //
				.unit(Unit.VOLT_AMPERE_REACTIVE) //
				.persistencePriority(PersistencePriority.HIGH)), //	
		
		INV_REACTIVE_POWER_L3(Doc.of(OpenemsType.LONG) //
				.unit(Unit.VOLT_AMPERE_REACTIVE) //
				.persistencePriority(PersistencePriority.HIGH)), //			
		
		

		/*
		 * used during implementation for quick test 
		 */				
		TEST_U_INT(Doc.of(OpenemsType.INTEGER) //
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

}
