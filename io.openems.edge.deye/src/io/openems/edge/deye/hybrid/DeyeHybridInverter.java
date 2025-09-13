package io.openems.edge.deye.hybrid;

import io.openems.common.channel.AccessMode;
import io.openems.common.channel.PersistencePriority;
import io.openems.common.channel.Unit;
import io.openems.common.types.OpenemsType;
import io.openems.edge.bridge.modbus.api.ModbusComponent;
import io.openems.edge.common.channel.Doc;
import io.openems.edge.common.channel.IntegerReadChannel;
import io.openems.edge.common.channel.value.Value;
import io.openems.edge.common.component.OpenemsComponent;
import io.openems.edge.deye.enums.DeviceType;
import io.openems.edge.deye.enums.InverterStatus;
import io.openems.edge.deye.gridtied.DeyeGridTiedInverter.ChannelId;
import io.openems.edge.meter.api.ElectricityMeter;

import io.openems.edge.battery.api.*;

public interface DeyeHybridInverter extends ElectricityMeter, ModbusComponent, OpenemsComponent {

	public enum ChannelId implements io.openems.edge.common.channel.ChannelId {
		
		// device type
		TYPE(Doc.of(DeviceType.values())
				.persistencePriority(PersistencePriority.VERY_LOW)), //
		
		// serial number
		SN(Doc.of(OpenemsType.STRING)
				.persistencePriority(PersistencePriority.VERY_LOW)), //
		
		/**
		 * Represents the state of the inverter.
		 */
		INV_STATUS(Doc.of(InverterStatus.values())
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
			
		// energy exported to Grid today
		E_GRID_SELL_TODAY(Doc.of(OpenemsType.INTEGER) //
				.accessMode(AccessMode.READ_ONLY)
				.unit(Unit.WATT_HOURS) //
				.persistencePriority(PersistencePriority.HIGH)),		

		// energy imported from Grid today
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
		
		
		DC_TRANS_TEMP(Doc.of(OpenemsType.INTEGER) //
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
				.persistencePriority(PersistencePriority.HIGH)), //
		
		GRID_APPARENT_POWER(Doc.of(OpenemsType.INTEGER) //
				.accessMode(AccessMode.READ_ONLY)
				.unit(Unit.VOLT_AMPERE)
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
				.persistencePriority(PersistencePriority.HIGH)), //

		GRID_EXT_APPARENT_POWER(Doc.of(OpenemsType.INTEGER) //
				.accessMode(AccessMode.READ_ONLY)
				.unit(Unit.VOLT_AMPERE)
				.persistencePriority(PersistencePriority.HIGH)), //

		GRID_COS_PHI(Doc.of(OpenemsType.INTEGER)
				.accessMode(AccessMode.READ_ONLY)
				.persistencePriority(PersistencePriority.HIGH)), //		
				
		INV_OUT_POWER(Doc.of(OpenemsType.INTEGER) //
				.unit(Unit.WATT) //
				.persistencePriority(PersistencePriority.HIGH)), //	
		
		INV_OUT_REACTIVE_POWER(Doc.of(OpenemsType.INTEGER) //
				.unit(Unit.VOLT_AMPERE_REACTIVE) //
				.persistencePriority(PersistencePriority.HIGH)), //	
		
		INV_OUT_APPARENT_POWER(Doc.of(OpenemsType.INTEGER) //
				.unit(Unit.VOLT_AMPERE) //
				.persistencePriority(PersistencePriority.HIGH)), //	
		
		UPS_LOAD_POWER(Doc.of(OpenemsType.INTEGER) //
				.unit(Unit.WATT) //
				.persistencePriority(PersistencePriority.HIGH)), //	

		LOAD_POWER(Doc.of(OpenemsType.INTEGER) //
				.unit(Unit.WATT) //
				.persistencePriority(PersistencePriority.HIGH)), //	

		LOAD_REACTIVE_POWER(Doc.of(OpenemsType.INTEGER) //
				.unit(Unit.VOLT_AMPERE_REACTIVE) //
				.persistencePriority(PersistencePriority.HIGH)), //	
		
		LOAD_APPARENT_POWER(Doc.of(OpenemsType.INTEGER) //
				.unit(Unit.VOLT_AMPERE) //
				.persistencePriority(PersistencePriority.HIGH)), //	
		
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
	
		
	public default IntegerReadChannel getPowerPv1Channel() {
		return this.channel(ChannelId.POWER_PV1);
	}
		
	public default IntegerReadChannel getPowerPv2Channel() {
		return this.channel(ChannelId.POWER_PV2);
	}	
	
	public default IntegerReadChannel getPowerPvChannel() {
		return this.channel(ChannelId.POWER_PV);
	}
}
