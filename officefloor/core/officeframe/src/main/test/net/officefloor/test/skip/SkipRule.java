/*-
 * #%L
 * OfficeFrame
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
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

package net.officefloor.test.skip;

import org.junit.Assume;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

/**
 * {@link TestRule} to skip test under particular conditions.
 * 
 * @author Daniel Sagenschneider
 */
public class SkipRule implements TestRule {

	/**
	 * Condition for skipping.
	 */
	public static interface SkipCondition {

		/**
		 * Indicates whether to skip.
		 * 
		 * @return <code>true</code> to skip test.
		 */
		boolean isSkip();

		/**
		 * Obtains message indicating reason for skipping.
		 * 
		 * @return Message indicating reason for skipping.
		 */
		String getSkipMessage();
	}

	/**
	 * {@link SkipCondition}.
	 */
	private final SkipCondition condition;

	/**
	 * Instantiate with {@link SkipCondition}.
	 * 
	 * @param condition {@link SkipCondition}.
	 */
	public SkipRule(SkipCondition condition) {
		this.condition = condition;
	}

	/**
	 * Instantiate.
	 * 
	 * @param isSkip Indicates whether to skip.
	 */
	public SkipRule(boolean isSkip) {
		this(isSkip, null);
	}

	/**
	 * Instantiate.
	 * 
	 * @param isSkip  Indicates whether to skip.
	 * @param message Skip message.
	 */
	public SkipRule(boolean isSkip, String message) {
		this(new SkipCondition() {

			@Override
			public boolean isSkip() {
				return isSkip;
			}

			@Override
			public String getSkipMessage() {
				return message;
			}
		});
	}

	/*
	 * ================= TestRule =======================
	 */

	@Override
	public Statement apply(Statement base, Description description) {
		return new Statement() {
			@Override
			public void evaluate() throws Throwable {

				// Determine if skip
				if (SkipRule.this.condition.isSkip()) {

					// Log skipping the test
					String skipMessage = SkipRule.this.condition.getSkipMessage();

					// Determine test to skip
					String testDetail = "NOT RUNNING TEST" + ((description == null) ? ""
							: (" " + description.getClassName() + "." + description.getMethodName() + "()"));

					// Flag to skip
					String message = testDetail + (skipMessage == null ? "" : ": " + skipMessage);
					Assume.assumeTrue(message, false);
				}

				// Undertake the test
				base.evaluate();
			}
		};
	}

}
