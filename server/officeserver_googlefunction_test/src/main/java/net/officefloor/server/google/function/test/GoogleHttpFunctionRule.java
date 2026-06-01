/*-
 * #%L
 * Google Function Testing
 * %%
 * Copyright (C) 2005 - 2026 Daniel Sagenschneider
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

package net.officefloor.server.google.function.test;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import com.google.cloud.functions.HttpFunction;

import net.officefloor.frame.api.manage.OfficeFloor;

/** JUnit 4 rule for Google HTTP function testing. */
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
					rule.teardownHttpFunction();
				}
			}
		};
	}

}
