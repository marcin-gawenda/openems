package io.openems.edge.deye.enums;

import io.openems.common.types.OptionsEnum;

public enum DeviceType implements OptionsEnum {	
	UNDEFINED(-1, "Undefined"), //
	GRID_TIED(2, "Grid-tied inverter"), //
	HYBRID_1_PHASE(3, "Hybrid inverter single phase"), //	
	MICROINVERTER(4, "Microinverter"), //
	HYBRID_3_PHASE(5, "Hybrid inverter 3-phase");

	private final int value;
	private final String name;

	private DeviceType(int value, String name) {
		this.value = value;
		this.name = name;
	}

	@Override
	public int getValue() {
		return this.value;
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public OptionsEnum getUndefined() {
		return UNDEFINED;
	}
}
