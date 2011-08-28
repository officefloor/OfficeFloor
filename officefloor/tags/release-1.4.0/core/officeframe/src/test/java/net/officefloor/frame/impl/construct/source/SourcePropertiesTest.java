/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2011 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.officefloor.frame.impl.construct.source;

import java.util.Properties;

import net.officefloor.frame.spi.source.SourceProperties;
import net.officefloor.frame.spi.source.UnknownPropertyError;
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
		assertEquals("Should retrieve property", "value",
				this.properties.getProperty("name"));
	}

	/**
	 * Ensure error on unknown property.
	 */
	public void testErrorOnUnknownProperty() {
		try {
			this.properties.getProperty("unknown");
			fail("Should not successful obtain unknown property");
		} catch (UnknownPropertyError ex) {
			assertEquals("Incorrect property", "unknown",
					ex.getUnknownPropertyName());
			assertEquals("Incorrect message", "Unknown property 'unknown'",
					ex.getMessage());
		}
	}

	/**
	 * Ensure not default property if exists.
	 */
	public void testNotDefaultProperty() {
		this.properties.addProperty("name", "value");
		assertEquals("Should use property value", "value",
				this.properties.getProperty("name", "default"));
	}

	/**
	 * Ensure defaults property name.
	 */
	public void testDefaultProperty() {
		assertEquals("Should default property", "default",
				this.properties.getProperty("unknown", "default"));
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