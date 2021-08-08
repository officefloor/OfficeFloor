/*-
 * #%L
 * OfficeCompiler
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

package net.officefloor.compile.impl.properties;

import net.officefloor.compile.properties.Property;
import net.officefloor.compile.properties.PropertyConfigurable;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.frame.test.OfficeFrameTestCase;

/**
 * Tests the {@link PropertyList}.
 * 
 * @author Daniel Sagenschneider
 */
public class PropertyListTest extends OfficeFrameTestCase {

	/**
	 * {@link PropertyList} to test.
	 */
	private final PropertyList list = new PropertyListImpl();

	/**
	 * Ensure able to get {@link Property} value by convenience method.
	 */
	public void testGetPropertyValue() {

		// Ensure get default if empty
		assertNull("Should not have value",
				this.list.getPropertyValue("test", null));
		assertEquals("Should get default value", "DEFAULT",
				this.list.getPropertyValue("test", "DEFAULT"));

		// Add property with no value and ensure get default value
		Property property = this.list.addProperty("test");
		assertEquals("Should not have property value", "DEFAULT",
				this.list.getPropertyValue("test", "DEFAULT"));

		// Provide value to property and ensure get value
		property.setValue("VALUE");
		assertEquals("Should get value", "VALUE",
				this.list.getPropertyValue("test", "DEFAULT"));
	}

	/**
	 * Ensure can remove the {@link Property}.
	 */
	public void testRemoveProperty() {

		// Add the property
		Property property = this.list.addProperty("test");
		assertEquals("Ensure property added", property,
				this.list.getProperty("test"));

		// Remove the property
		this.list.removeProperty(property);
		assertNull("Should no longer have property",
				this.list.getProperty("test"));
	}

	/**
	 * Ensure can load {@link PropertyConfigurable} with the
	 * {@link PropertyList}.
	 */
	public void testConfigurePropertyConfigurable() {

		// Record adding properties
		PropertyConfigurable configurable = this
				.createMock(PropertyConfigurable.class);
		configurable.addProperty("one", "1");
		configurable.addProperty("two", "2");
		configurable.addProperty("three", "3");

		// Test
		this.replayMockObjects();

		// Add properties
		this.list.addProperty("one").setValue("1");
		this.list.addProperty("two").setValue("2");
		this.list.addProperty("three").setValue("3");

		// Load the properties to property configurable
		this.list.configureProperties(configurable);

		// Validate
		this.verifyMockObjects();
	}

}
