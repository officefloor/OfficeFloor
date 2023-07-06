package net.officefloor.server.google.function.test;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import com.google.cloud.functions.HttpFunction;

import net.officefloor.frame.api.manage.OfficeFloor;

public class GoogleHttpFunctionRule extends AbstractGoogleHttpFunctionJUnit<GoogleHttpFunctionRule>
		implements TestRule {

	/**
	 * Instantiate with the Google {@link HttpFunction}.
	 * 
	 * @param httpFunctionClass {@link HttpFunction} {@link Class}.
	 */
	public GoogleHttpFunctionRule(Class<?> httpFunctionClass) {
		super(httpFunctionClass);
	}

	/**
	 * Instantiate using the {@link OfficeFloor} {@link HttpFunction}.
	 */
	public GoogleHttpFunctionRule() {
	}

	/*
	 * ======================= TestRule ======================
	 */

	@Override
	public Statement apply(Statement base, Description description) {
		return new Statement() {

			@Override
			public void evaluate() throws Throwable {

				// Easy access
				GoogleHttpFunctionRule rule = GoogleHttpFunctionRule.this;

				// Open server
				rule.openHttpServer();
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
