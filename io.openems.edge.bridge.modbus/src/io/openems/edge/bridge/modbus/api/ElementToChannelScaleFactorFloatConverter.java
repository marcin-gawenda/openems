package io.openems.edge.bridge.modbus.api;

import io.openems.common.exceptions.InvalidValueException;
import io.openems.edge.bridge.modbus.sunspec.Point.ScaledValuePoint;
import io.openems.edge.common.channel.ChannelId;
import io.openems.edge.common.channel.IntegerReadChannel;
import io.openems.edge.common.component.OpenemsComponent;

/**
 * Converts between Element and Channel by applying a scale factor.
 * It will not try to cast to  short/integer/long
 *
 * <p>
 * (channel = element * 10^scaleFactor)
 *
 * <p>
 * Example: if the Register is in unit [0.1 V] and this converter has a
 * scaleFactor of '2', it converts to unit [1 mV]
 */
public class ElementToChannelScaleFactorFloatConverter extends ElementToChannelConverter {

	private static int getValueOrError(OpenemsComponent component, ChannelId channelId)
			throws InvalidValueException, IllegalArgumentException {
		var channel = (IntegerReadChannel) component.channel(channelId);
		var value = channel.getNextValue().orElse(null);
		if (value != null) {
			return value;
		}
		return channel.value().getOrError();
	}

	public ElementToChannelScaleFactorFloatConverter(OpenemsComponent component, ScaledValuePoint point,
			ChannelId scaleFactorChannel) {
		super(//
				// element -> channel
				value -> {
					if (!point.isDefined(value)) {
						return null;
					}
					try {
						return apply(value, getValueOrError(component, scaleFactorChannel) * -1);
					} catch (InvalidValueException | IllegalArgumentException e) {
						return null;
					}
				}, //

				// channel -> element
				value -> {
					try {
						return apply(value, getValueOrError(component, scaleFactorChannel));
					} catch (InvalidValueException | IllegalArgumentException e) {
						return null;
					}
				});
	}

	public ElementToChannelScaleFactorFloatConverter(int scaleFactor) {
		super(//
				// element -> channel
				value -> apply(value, scaleFactor * -1), //

				// channel -> element
				value -> apply(value, scaleFactor));
	}

	private static Object apply(Object value, int scaleFactor) {
	    float factor = (float) Math.pow(10, scaleFactor * -1);

	    return switch (value) {
	        case null -> null;
	        case Boolean b -> b;
	        case Short s   -> roundTo4(s * factor);
	        case Integer i -> roundTo4(i * factor);
	        case Long l    -> roundTo4(l * factor);
	        case Float f   -> roundTo4(f * factor);
	        case Double d  -> roundTo4((float) (d * factor));
	        case String s  -> s;
	        default -> throw new IllegalArgumentException(
	            "Type [" + value.getClass().getName() + "] not supported by SCALE_FACTOR converter");
	    };
	}

	/**
	 * Round a float to 4 digits after the decimal point.
	 */
	private static Float roundTo4(float value) {
	    return Math.round(value * 10000f) / 10000f;
	}	
}
