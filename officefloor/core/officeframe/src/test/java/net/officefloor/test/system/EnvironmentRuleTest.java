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
