/*-
 * #%L
 * OfficeFrame
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package net.officefloor.frame.test;

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
