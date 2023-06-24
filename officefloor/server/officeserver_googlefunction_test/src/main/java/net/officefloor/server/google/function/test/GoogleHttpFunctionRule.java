package net.officefloor.server.google.function.test;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import com.google.cloud.functions.HttpFunction;

public class GoogleHttpFunctionRule extends AbstractGoogleHttpFunctionJUnit implements TestRule {

	/**
	 * Instantiate.
	 * 
	 * @param httpFunctionClass {@link HttpFunction} {@link Class}.
	 */
	public GoogleHttpFunctionRule(Class<?> httpFunctionClass) {
		super(httpFunctionClass);
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
				GoogleHttpFunctionRule rule = GoogleHttpFunctionRule.this;

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
