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

package net.officefloor.test.system;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.junit.AfterClass;
import org.junit.Rule;
import org.junit.Test;

import net.officefloor.test.system.AbstractExternalOverride.ContextRunnable;

/**
 * Tests the {@link EnvironmentRule}.
 * 
 * @author Daniel Sagenschneider
 */
public class EnvironmentRuleTest extends AbstractTestExternalOverride {

	/**
	 * {@link EnvironmentRule} to change environment variable for test.
	 */
	@Rule
	public EnvironmentRule environment = new EnvironmentRule("HOST", "OVERRIDE");

	/**
	 * Ensure override environmental rule.
	 */
	@Test
	public void ensureRuleWorksInJUnit4() {
		assertEquals("OVERRIDE", System.getenv().get("HOST"), "Should override environment");
	}

	/**
	 * Ensure resets environment after test.
	 */
	@AfterClass
	public static void ensureRuleResetsEnvironment() {
		assertNotEquals("OVERRIDE", System.getenv().get("HOST"), "Should reset environment");
	}

	/**
	 * Ensure can override environment.
	 */
	@Test
	public void testOverride() throws Exception {
		this.doOverrideTest();
	}

	/*
	 * ================== AbstractSystemRuleTest ===================
	 */

	@Override
	protected String get(String name) {
		return System.getenv(name);
	}

	@Override
	protected void set(String name, String value) {
		EnvironmentRule.changeEnvironment((env) -> env.put(name, value));
	}

	@Override
	protected void clear(String name) {
		EnvironmentRule.changeEnvironment((env) -> env.remove(name));
	}

	@Override
	protected void runOverride(ContextRunnable<Exception> logic, String nameOne, String valueOne, String nameTwo,
			String valueTwo) throws Exception {
		new EnvironmentRule(nameOne, valueOne).property(nameTwo, valueTwo).run(logic);
	}

}
