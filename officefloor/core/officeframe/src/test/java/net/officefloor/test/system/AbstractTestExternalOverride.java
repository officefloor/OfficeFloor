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
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.function.Function;

import net.officefloor.test.system.AbstractExternalOverride.ContextRunnable;

/**
 * Abstract logic to override external {@link System#getProperties()} or
 * environment variables.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class AbstractTestExternalOverride {

	/**
	 * Obtains the value.
	 * 
	 * @param name Name of value.
	 * @return Value.
	 */
	protected abstract String get(String name);

	/**
	 * Specifies the value.
	 * 
	 * @param name  Name for value.
	 * @param value Value.
	 */
	protected abstract void set(String name, String value);

	/**
	 * Clears the value.
	 * 
	 * @param name Name of value.
	 */
	protected abstract void clear(String name);

	/**
	 * Undertakes the test.
	 * 
	 * @param logic    Logic to be wrapped with changes.
	 * @param nameOne  First name.
	 * @param valueOne First value.
	 * @param nameTwo  Second name.
	 * @param valueTwo Second value.
	 */
	protected abstract void runOverride(ContextRunnable<Exception> logic, String nameOne, String valueOne,
			String nameTwo, String valueTwo) throws Exception;

	/**
	 * Ensure correctly setting/unsetting properties.
	 */
	public void doOverrideTest() throws Exception {

		// Determine an empty property name
		Function<String, String> emptyPropertyName = (prefix) -> {
			String name = prefix;
			int index = 0;
			String value = "check";
			do {
				value = this.get(name);
				if (value != null) {
					index++;
					name = prefix + index;
				}
			} while (value != null);
			return name;
		};

		// Find non-configured properties
		String propertyOneName = emptyPropertyName.apply("PropertyOne");
		String propertyTwoName = emptyPropertyName.apply("PropertyTwo");
		try {

			// Provide one property value
			final String originalValue = "ORIGINAL";
			this.set(propertyOneName, originalValue);
			assertEquals(originalValue, this.get(propertyOneName),
					"INVALID TEST: failed to set value " + propertyOneName);

			// Run rule to ensure appropriately sets value
			final String overrideValue = "OVERRIDE";
			final String setValue = "SET";
			this.runOverride(() -> {
				assertEquals(overrideValue, this.get(propertyOneName), "Incorrect override value");
				assertEquals(setValue, this.get(propertyTwoName), "Incorrect set value");
			}, propertyOneName, overrideValue, propertyTwoName, setValue);

			// Ensure unsets
			assertEquals(originalValue, this.get(propertyOneName), "Incorrect unset value");
			assertNull(this.get(propertyTwoName), "Should clear value");

		} finally {
			// Ensure reset
			this.clear(propertyOneName);
			this.clear(propertyTwoName);
		}
	}

}
