package net.officefloor.compile.test.system;

import net.officefloor.compile.test.system.AbstractSystemRule.ContextRunnable;

/**
 * Tests the {@link EnvironmentRule}.
 * 
 * @author Daniel Sagenschneider
 */
public class EnvironmentRuleTest extends AbstractSystemRuleTest {

	@Override
	protected String get(String name) {
		return System.getenv(name);
	}

	@Override
	protected void set(String name, String value) {
		EnvironmentRule.changeEnvironment((env) -> env.put(name, value));
	}

	@Override
	protected void clear(String name) {
		EnvironmentRule.changeEnvironment((env) -> env.remove(name));
	}

	@Override
	protected void doTest(ContextRunnable<Exception> logic, String nameOne, String valueOne, String nameTwo,
			String valueTwo) throws Exception {
		new EnvironmentRule(nameOne, valueOne).property(nameTwo, valueTwo).run(logic);
	}

}