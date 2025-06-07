package io.openems.edge.meter.chint.dtsu666;

import org.junit.Test;

import io.openems.edge.common.test.AbstractComponentTest.TestCase;
import io.openems.edge.meter.chint.dtsu666.MeterChintDtsu666Impl;
import io.openems.edge.bridge.modbus.test.DummyModbusBridge;
import io.openems.edge.common.test.ComponentTest;
import io.openems.edge.common.test.DummyConfigurationAdmin;

public class MeterChintDtsu666ImplTest {

	@Test
	public void test() throws Exception {
		new ComponentTest(new MeterChintDtsu666Impl()) //
				.addReference("cm", new DummyConfigurationAdmin()) //
				.addReference("setModbus", new DummyModbusBridge("modbus0")) //
				.activate(MyConfig.create() //
						.setId("component0") //
						.setModbusId("modbus0") //
						.build()) //
				.next(new TestCase()) //
				.deactivate();
	}

}
