package net.officefloor.server.google.function.mock;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import com.google.cloud.functions.HttpFunction;

import net.officefloor.frame.api.manage.OfficeFloor;

/**
 * {@link TestRule} for Google {@link HttpFunction} execution.
 */
public class MockGoogleHttpFunctionRule extends AbstractMockGoogleHttpFunctionJUnit implements TestRule {

	/**
	 * Instantiate.
	 * 
	 * @param httpFunctionClass {@link HttpFunction} {@link Class}.
	 */
	public MockGoogleHttpFunctionRule(Class<?> httpFunctionClass) {
		super(httpFunctionClass);
	}

	/**
	 * Instantiate using the {@link OfficeFloor} {@link HttpFunction}.
	 */
	public MockGoogleHttpFunctionRule() {
	}

	/*
	 * =================== TestRule ===================
	 */

	@Override
	public Statement apply(Statement base, Description description) {
		return new Statement() {

			@Override
			public void evaluate() throws Throwable {

				// Easy access
				MockGoogleHttpFunctionRule rule = MockGoogleHttpFunctionRule.this;

				// Open server
				rule.openMockHttpServer();
				try {

					// Undertake base functionality
					base.evaluate();

				} finally {
					// Ensure close server
					rule.close();
				}
			}
		};
	}

}
