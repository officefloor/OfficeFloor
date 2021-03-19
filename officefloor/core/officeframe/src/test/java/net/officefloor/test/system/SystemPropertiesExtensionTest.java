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
 * Tests the {@link SystemPropertiesExtension}.
 * 
 * @author Daniel Sagenschneider
 */
public class SystemPropertiesExtensionTest extends AbstractTestExternalOverride {

	/**
	 * {@link SystemPropertiesExtension} to change system property for test.
	 */
	@RegisterExtension
	public final SystemPropertiesExtension systemProperties = new SystemPropertiesExtension("java.home", "OVERRIDE");

	/**
	 * Ensure override system property.
	 */
	@Test
	public void ensureExtesionWorksInJUnit5() {
		assertEquals("OVERRIDE", System.getProperty("java.home"), "Should override environment");
	}

	/**
	 * Ensure resets environment after test.
	 */
	@AfterAll
	public static void ensureRuleResetsSystemProperty() {
		assertNotEquals("OVERRIDE", System.getProperty("java.home"), "Should reset environment");
	}

	/**
	 * Ensure can override system property.
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
		return System.getProperty(name);
	}

	@Override
	protected void set(String name, String value) {
		System.setProperty(name, value);
	}

	@Override
	protected void clear(String name) {
		System.clearProperty(name);
	}

	@Override
	protected void runOverride(ContextRunnable<Exception> logic, String nameOne, String valueOne, String nameTwo,
			String valueTwo) throws Exception {
		new SystemPropertiesExtension(nameOne, valueOne).property(nameTwo, valueTwo).run(logic);
	}

}
