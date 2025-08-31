package io.openems.edge.deye.enums;

import io.openems.common.types.OptionsEnum;

public enum InverterState implements OptionsEnum {	
	UNDEFINED(-1, "Undefined"), //
	STANDBY(0, "Standby"), //
	SELFCHECK(1, "Selfcheck"), //
	NORMAL(2, "Normal"), //
	ALARM(3, "Alarm"), //
	FAULT(4, "Fault");

	private final int value;
	private final String name;

	private InverterState(int value, String name) {
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
