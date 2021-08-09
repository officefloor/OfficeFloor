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

package net.officefloor.frame.impl.construct.source;

import java.util.Properties;

import net.officefloor.frame.api.source.SourceProperties;
import net.officefloor.frame.api.source.UnknownPropertyError;
import net.officefloor.frame.test.OfficeFrameTestCase;

/**
 * Tests the {@link SourceProperties}.
 * 
 * @author Daniel Sagenschneider
 */
public class SourcePropertiesTest extends OfficeFrameTestCase {

	/**
	 * {@link SourceProperties} to test.
	 */
	private final SourcePropertiesImpl properties = new SourcePropertiesImpl();

	/**
	 * Ensure can add and get the property.
	 */
	public void testAddAndGetProperty() {
		this.properties.addProperty("name", "value");
		assertEquals("Should retrieve property", "value", this.properties.getProperty("name"));
	}

	/**
	 * Ensure error on unknown property.
	 */
	public void testErrorOnUnknownProperty() {
		try {
			this.properties.getProperty("unknown");
			fail("Should not successful obtain unknown property");
		} catch (UnknownPropertyError ex) {
			assertEquals("Incorrect property", "unknown", ex.getUnknownPropertyName());
			assertEquals("Incorrect message", "Must specify property 'unknown'", ex.getMessage());
		}
	}

	/**
	 * Ensure not default property if exists.
	 */
	public void testNotDefaultProperty() {
		this.properties.addProperty("name", "value");
		assertEquals("Should use property value", "value", this.properties.getProperty("name", "default"));
	}

	/**
	 * Ensure defaults property name.
	 */
	public void testDefaultProperty() {
		assertEquals("Should default property", "default", this.properties.getProperty("unknown", "default"));
	}

	/**
	 * Ensure property names in order as added.
	 */
	public void testPropertyNames() {
		this.properties.addProperty("one", "1");
		this.properties.addProperty("two", "2");
		this.properties.addProperty("three", "3");
		this.properties.addProperty("four", "4");

		// Ensure property names as expected
		String[] names = this.properties.getPropertyNames();
		assertEquals("Incorrect number of property names", 4, names.length);
		assertEquals("Name one", "one", names[0]);
		assertEquals("Name two", "two", names[1]);
		assertEquals("Name three", "three", names[2]);
		assertEquals("Name four", "four", names[3]);
	}

	/**
	 * Ensure properties available.
	 */
	public void testProperties() {
		this.properties.addProperty("A", "one");
		this.properties.addProperty("B", "two");

		// Ensure properties available
		Properties props = this.properties.getProperties();
		assertEquals("Incorrect number of properties", 2, props.size());
		assertEquals("Incorrect A property", "one", props.getProperty("A"));
		assertEquals("Incorrect B property", "two", props.getProperty("B"));
	}

	/**
	 * Initialises from properties.
	 */
	public void testInitialiseFromProperties() {
		this.properties.addProperty("one", "1");
		this.properties.addProperty("two", "2");
		this.properties.addProperty("three", "3");
		this.properties.addProperty("four", "4");

		// Initialise from properties
		SourcePropertiesImpl clone = new SourcePropertiesImpl(this.properties);

		// Ensure copied in the properties (in same order)
		String[] names = clone.getPropertyNames();
		assertEquals("Incorrect number of property names", 4, names.length);
		assertEquals("Name one", "one", names[0]);
		assertEquals("Name two", "two", names[1]);
		assertEquals("Name three", "three", names[2]);
		assertEquals("Name four", "four", names[3]);

		// Ensure values as expected
		assertEquals("Incorrect one", "1", clone.getProperty("one"));
		assertEquals("Incorrect two", "2", clone.getProperty("two"));
		assertEquals("Incorrect three", "3", clone.getProperty("three"));
		assertEquals("Incorrect four", "4", clone.getProperty("four"));
	}

	/**
	 * Ensure able to initialise with <code>null</code> value.
	 */
	public void testInitialiseWithNullValue() {
		this.properties.addProperty("blank", null);

		// Initialise from properties
		SourcePropertiesImpl clone = new SourcePropertiesImpl(this.properties);

		// Ensure null value loaded
		Properties props = clone.getProperties();
		assertTrue("Ensure load null value", props.keySet().contains("blank"));
	}

}
