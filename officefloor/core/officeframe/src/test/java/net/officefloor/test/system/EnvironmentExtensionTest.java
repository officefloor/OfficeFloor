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

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import net.officefloor.test.system.AbstractExternalOverride.ContextRunnable;

/**
 * Tests the {@link EnvironmentExtension}.
 * 
 * @author Daniel Sagenschneider
 */
public class EnvironmentExtensionTest extends AbstractTestExternalOverride {

	/**
	 * {@link EnvironmentExtension} to change environment variable for test.
	 */
	@RegisterExtension
	public final EnvironmentExtension environment = new EnvironmentExtension("HOST", "OVERRIDE");

	/**
	 * Ensure override environment.
	 */
	@Test
	public void ensureExtensionWorksInJUnit5() {
		assertEquals("OVERRIDE", System.getenv().get("HOST"), "Should override environment");
	}

	/**
	 * Ensure resets environment after test.
	 */
	@AfterAll
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
	 * ================== AbstractTestExternalOverride ===================
	 */

	@Override
	protected String get(String name) {
		return System.getenv(name);
	}

	@Override
	protected void set(String name, String value) {
		EnvironmentExtension.changeEnvironment((env) -> env.put(name, value));
	}

	@Override
	protected void clear(String name) {
		EnvironmentExtension.changeEnvironment((env) -> env.remove(name));
	}

	@Override
	protected void runOverride(ContextRunnable<Exception> logic, String nameOne, String valueOne, String nameTwo,
			String valueTwo) throws Exception {
		new EnvironmentExtension(nameOne, valueOne).property(nameTwo, valueTwo).run(logic);
	}

}
