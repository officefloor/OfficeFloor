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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.AssumptionViolatedException;
import org.junit.Test;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import net.officefloor.frame.test.Closure;

/**
 * Tests the {@link SkipRule}.
 * 
 * @author Daniel Sagenschneider
 */
public class SkipRuleTest {

	/**
	 * Ensure skips running test.
	 */
	@Test
	public void skip() throws Throwable {
		try {
			this.evaluateSkipRule(new SkipRule(true), null);
			fail("Should not be successful");
		} catch (AssumptionViolatedException ex) {
			assertEquals("Incorrect skip reason", "NOT RUNNING TEST", ex.getMessage());
		}
	}

	/**
	 * Ensure provides details with {@link Description}.
	 */
	@Test
	public void skipWithDescription() throws Throwable {
		try {
			this.evaluateSkipRule(new SkipRule(true, "TEST SKIP"),
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
	@Test
	public void notSkip() throws Throwable {
		assertTrue("Should run test", this.evaluateSkipRule(new SkipRule(false, "Not skip"), null));
	}

	/**
	 * Evaluates the {@link SkipRule} indicating whether test was run.
	 * 
	 * @param rule        {@link SkipRule} to use.
	 * @param description {@link Description}.
	 * @return <code>true</code> if test run.
	 */
	private boolean evaluateSkipRule(SkipRule rule, Description description) throws Throwable {
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
