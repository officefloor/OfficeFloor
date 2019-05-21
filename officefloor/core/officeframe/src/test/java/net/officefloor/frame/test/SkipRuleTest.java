/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2019 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package net.officefloor.frame.test;

import org.junit.AssumptionViolatedException;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

/**
 * Tests the {@link SkipRule}.
 * 
 * @author Daniel Sagenschneider
 */
public class SkipRuleTest extends OfficeFrameTestCase {

	/**
	 * Ensure skips running test.
	 */
	public void testSkip() throws Throwable {
		try {
			this.evaluteSkipRule(new SkipRule(true), null);
			fail("Should not be successful");
		} catch (AssumptionViolatedException ex) {
			assertEquals("Incorrect skip reason", "NOT RUNNING TEST", ex.getMessage());
		}
	}

	/**
	 * Ensure provides details with {@link Description}.
	 */
	public void testSkipWithDescription() throws Throwable {
		try {
			this.evaluteSkipRule(new SkipRule(true, "TEST SKIP"),
					Description.createTestDescription(SkipRuleTest.class, "testMethod"));
			fail("Should not be successful");
		} catch (AssumptionViolatedException ex) {
			assertEquals("Incorrect skip reason",
					"NOT RUNNING TEST " + SkipRuleTest.class.getName() + ".testMethod(): TEST SKIP", ex.getMessage());
		}
	}

	/**
	 * Ensure not skip test.
	 */
	public void testNotSkip() throws Throwable {
		assertTrue("Should run test", this.evaluteSkipRule(new SkipRule(false, "Not skip"), null));
	}

	/**
	 * Evaluates the {@link SkipRule} indicating whether test was run.
	 * 
	 * @param rule        {@link SkipRule} to use.
	 * @param description {@link Description}.
	 * @return <code>true</code> if test run.
	 */
	private boolean evaluteSkipRule(SkipRule rule, Description description) throws Throwable {
		Closure<Boolean> isRun = new Closure<>(false);
		rule.apply(new Statement() {

			@Override
			public void evaluate() throws Throwable {
				isRun.value = true;
			}
		}, description).evaluate();
		return isRun.value;
	}

}