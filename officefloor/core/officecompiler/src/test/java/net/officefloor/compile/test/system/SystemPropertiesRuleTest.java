package net.officefloor.compile.test.system;

import net.officefloor.compile.test.system.AbstractSystemRule.ContextRunnable;

/**
 * Tests the {@link SystemPropertiesRule}.
 * 
 * @author Daniel Sagenschneider
 */
public class SystemPropertiesRuleTest extends AbstractSystemRuleTest {

	@Override
	protected String get(String name) {
		return System.getProperty(name);
	}

	@Override
	protected void set(String name, String value) {
		System.setProperty(name, value);
	}

	@Override
	protected void clear(String name) {
		System.clearProperty(name);
	}

	@Override
	protected void doTest(ContextRunnable<Exception> logic, String nameOne, String valueOne, String nameTwo,
			String valueTwo) throws Exception {
		new SystemPropertiesRule(nameOne, valueOne).property(nameTwo, valueTwo).run(logic);
	}

}