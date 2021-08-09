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
import net.officefloor.frame.api.source.SourceProperties;
import net.officefloor.frame.test.OfficeFrameTestCase;

/**
 * Tests the {@link PropertiesUtil}.
 * 
 * @author Daniel Sagenschneider
 */
public class PropertiesUtilTest extends OfficeFrameTestCase {

	/**
	 * {@link SourceProperties}.
	 */
	private final SourceProperties source = this.createMock(SourceProperties.class);

	/**
	 * {@link PropertyConfigurable}.
	 */
	private final PropertyConfigurable target = this.createMock(PropertyConfigurable.class);

	/**
	 * {@link PropertyList}.
	 */
	private final PropertyList targetList = this.createMock(PropertyList.class);

	/**
	 * Ensure able to copy a {@link Property}.
	 */
	public void testCopyOneProperty() {
		this.doTest(new String[] { "NAME" }, "NAME", "VALUE");
	}

	/**
	 * Ensure not copy if no value for {@link Property}.
	 */
	public void testNotCopyAsNoValue() {
		this.doTest(new String[] { "NAME" }, "NAME", null);
	}

	/**
	 * Ensure able to copy multiple specified {@link Property} instances.
	 */
	public void testMultipleSpecifiedProperties() {
		this.doTest(new String[] { "ONE", "TWO", "THREE" }, "ONE", "1", "TWO", null, "THREE", "");
	}

	/**
	 * Ensure able to copy all if <code>null</code> array of specified
	 * {@link Property} instances to copy.
	 */
	public void testCopyAllAsNullSpecified() {
		this.doTest(null, "ONE", "1", "TWO", null, "THREE", "");
	}

	/**
	 * Ensure able to copy all if empty array of specified {@link Property}
	 * instances to copy.
	 */
	public void testCopyAllAsNoneSpecified() {
		this.doTest(new String[0], "ONE", "1", "TWO", null, "THREE", "");
	}

	/**
	 * Undertakes the test.
	 * 
	 * @param specifiedPropertyNames Specified {@link Property} instances to copy.
	 * @param propertyNameValues     {@link Property} name value pairs.
	 */
	private void doTest(String[] specifiedPropertyNames, String... propertyNameValues) {

		// Obtain all property names
		String[] allPropertyNames = new String[propertyNameValues.length / 2];
		for (int i = 0; i < propertyNameValues.length; i += 2) {
			allPropertyNames[i / 2] = propertyNameValues[i];
		}

		// Determine if copy all properties
		boolean isAllProperties = (specifiedPropertyNames == null) || (specifiedPropertyNames.length == 0);

		// Record copying the properties
		for (boolean isConfigurable : new boolean[] { true, false }) {
			if (isAllProperties) {
				this.recordReturn(this.source, this.source.getPropertyNames(), allPropertyNames);
			}
			for (int i = 0; i < propertyNameValues.length; i += 2) {
				String name = propertyNameValues[i];
				String value = propertyNameValues[i + 1];

				// Record obtaining the property
				this.recordReturn(this.source, this.source.getProperty(name, null), value);

				// Record copying property (only if value)
				if (value != null) {
					this.recordProperty(isConfigurable, name, value);
				}
			}
		}

		// Test
		this.replayMockObjects();
		PropertiesUtil.copyProperties(this.source, this.target, specifiedPropertyNames);
		PropertiesUtil.copyProperties(this.source, this.targetList, specifiedPropertyNames);
		this.verifyMockObjects();
	}

	/**
	 * Ensure can copy prefixed {@link Property} instances.
	 */
	public void testCopyPrefixedProperties() {

		// Record the property names
		for (boolean isConfigurable : new boolean[] { true, false }) {
			this.recordReturn(this.source, this.source.getPropertyNames(), new String[] { "ignore.one", "prefix.",
					"prefix.one", "ignore.prefix", "prefix.two", "ignore.again", "prefix.null" });
			this.recordReturn(this.source, this.source.getProperty("prefix.", null), "empty");
			this.recordProperty(isConfigurable, "prefix.", "empty");
			this.recordReturn(this.source, this.source.getProperty("prefix.one", null), "1");
			this.recordProperty(isConfigurable, "prefix.one", "1");
			this.recordReturn(this.source, this.source.getProperty("prefix.two", null), "2");
			this.recordProperty(isConfigurable, "prefix.two", "2");
			this.recordReturn(this.source, this.source.getProperty("prefix.null", null), null);
			this.recordProperty(isConfigurable, "prefix.null", null);
		}

		// Test
		this.replayMockObjects();
		PropertiesUtil.copyPrefixedProperties(this.source, "prefix.", this.target);
		PropertiesUtil.copyPrefixedProperties(this.source, "prefix.", this.targetList);
		this.verifyMockObjects();
	}

	/**
	 * Records the {@link Property} copy.
	 */
	private void recordProperty(boolean isConfigurable, String name, String value) {
		if (isConfigurable) {
			// Copy to configurable
			this.target.addProperty(name, value);
		} else {
			// Copy to properties list
			Property property = this.createMock(Property.class);
			this.recordReturn(this.targetList, this.targetList.addProperty(name), property);
			property.setValue(value);
		}
	}

}
