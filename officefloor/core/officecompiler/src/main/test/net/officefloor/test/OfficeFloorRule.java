/*-
 * #%L
 * OfficeCompiler
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

package net.officefloor.test;

import static org.junit.Assert.fail;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import net.officefloor.frame.api.manage.OfficeFloor;

/**
 * {@link TestRule} for running {@link OfficeFloor} around tests.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeFloorRule extends AbstractOfficeFloorJUnit implements TestRule {

	/**
	 * Test instance.
	 */
	private final Object testInstance;

	/**
	 * Instantiate for no dependency injection of test.
	 */
	public OfficeFloorRule() {
		this(null);
	}

	/**
	 * Instantiate to dependency inject into the test instance.
	 * 
	 * @param testInstance Test instance.
	 */
	public OfficeFloorRule(Object testInstance) {
		this.testInstance = testInstance;
	}

	/*
	 * =============== AbstractOfficeFloorJUnit ==============
	 */

	@Override
	protected void doFail(String message) {
		fail(message);
	}

	@Override
	protected Error doFail(Throwable cause) {
		String message = cause.getMessage();
		fail(message != null ? message : cause.toString());
		return null; // should not return
	}

	/*
	 * ================== TestRule ============================
	 */

	@Override
	public Statement apply(Statement base, Description description) {
		return new Statement() {

			@Override
			public void evaluate() throws Throwable {

				// Easy access to rule
				OfficeFloorRule rule = OfficeFloorRule.this;

				// Create and compile the OfficeFloor
				rule.beforeAll();
				try {

					// Undertake dependency injection
					if (rule.testInstance != null) {
						rule.beforeEach(rule.testInstance);
					}

					// Run the test
					base.evaluate();

				} finally {
					// Ensure close and clear the OfficeFloor
					rule.afterAll();
				}
			}
		};
	}

}
