package io.openems.edge.pvmterminal;

import org.junit.Test;

import io.openems.edge.common.test.AbstractComponentTest.TestCase;
import io.openems.edge.common.test.ComponentTest;

public class PVMTerminalClientTest {

	@Test
	public void test() throws Exception {
		new ComponentTest(new PVMTerminalClientImpl()) //
				.activate(MyConfig.create() //
						.setId("component0") //
						.build()) //
				.next(new TestCase()) //
				.deactivate();
	}

}
