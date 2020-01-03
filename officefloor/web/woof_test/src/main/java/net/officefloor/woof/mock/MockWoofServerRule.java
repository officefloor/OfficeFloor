package net.officefloor.woof.mock;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

/**
 * {@link TestRule} for running the {@link MockWoofServer}.
 * 
 * @author Daniel Sagenschneider
 */
public class MockWoofServerRule extends MockWoofServer implements TestRule {

	/**
	 * {@link MockWoofServerConfigurer} instances.
	 */
	private final MockWoofServerConfigurer[] configurers;

	/**
	 * Instantiate.
	 * 
	 * @param configurers {@link MockWoofServerConfigurer} instances.
	 */
	public MockWoofServerRule(MockWoofServerConfigurer... configurers) {

		// Ensure always have at least one configurer to load WoOF
		this.configurers = configurers != null ? configurers
				: new MockWoofServerConfigurer[] { (woofContext, compiler) -> {
				} };
	}

	/**
	 * =============== MockWoofServer =====================
	 */

	@Override
	public MockWoofServerRule timeout(int timeout) {
		super.timeout(timeout);
		return this;
	}

	/*
	 * =================== TestRule =======================
	 */

	@Override
	public Statement apply(Statement base, Description description) {
		return new Statement() {

			@Override
			public void evaluate() throws Throwable {
				try (MockWoofServer server = MockWoofServer.open(MockWoofServerRule.this,
						MockWoofServerRule.this.configurers)) {
					base.evaluate();
				}
			}
		};
	}

}