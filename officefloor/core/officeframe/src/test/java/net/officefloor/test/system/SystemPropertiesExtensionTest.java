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
