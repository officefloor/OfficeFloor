/*-
 * #%L
 * OfficeCompiler
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

package net.officefloor.compile.impl.properties;

import net.officefloor.compile.properties.Property;
import net.officefloor.compile.properties.PropertyConfigurable;
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
	private final SourceProperties source = this
			.createMock(SourceProperties.class);

	/**
	 * {@link PropertyConfigurable}.
	 */
	private final PropertyConfigurable target = this
			.createMock(PropertyConfigurable.class);

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
		this.doTest(new String[] { "ONE", "TWO", "THREE" }, "ONE", "1", "TWO",
				null, "THREE", "");
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
	 * @param specifiedPropertyNames
	 *            Specified {@link Property} instances to copy.
	 * @param propertyNameValues
	 *            {@link Property} name value pairs.
	 */
	private void doTest(String[] specifiedPropertyNames,
			String... propertyNameValues) {

		// Determine if copy all properties
		if ((specifiedPropertyNames == null)
				|| (specifiedPropertyNames.length == 0)) {
			// Record copy all properties
			String[] allPropertyNames = new String[propertyNameValues.length / 2];
			for (int i = 0; i < propertyNameValues.length; i += 2) {
				allPropertyNames[i / 2] = propertyNameValues[i];
			}
			this.recordReturn(this.source, this.source.getPropertyNames(),
					allPropertyNames);
		}

		// Record copying the properties
		this.recordCopyProperties(propertyNameValues);

		// Test
		this.replayMockObjects();
		PropertiesUtil.copyProperties(this.source, this.target,
				specifiedPropertyNames);
		this.verifyMockObjects();
	}

	/**
	 * Records copying the {@link Property} instances.
	 * 
	 * @param propertyNameValues
	 *            {@link Property} name value pairs.
	 */
	private void recordCopyProperties(String... propertyNameValues) {
		for (int i = 0; i < propertyNameValues.length; i += 2) {
			String name = propertyNameValues[i];
			String value = propertyNameValues[i + 1];

			// Record obtaining the property
			this.recordReturn(this.source, this.source.getProperty(name, null),
					value);

			// Record copying property (only if value)
			if (value != null) {
				this.target.addProperty(name, value);
			}
		}
	}

	/**
	 * Ensure can copy prefixed {@link Property} instances.
	 */
	public void testCopyPrefixedProperties() {

		// Record the property names
		this.recordReturn(this.source, this.source.getPropertyNames(),
				new String[] { "ignore.one", "prefix.", "prefix.one",
						"ignore.prefix", "prefix.two", "ignore.again",
						"prefix.null" });
		this.recordReturn(this.source,
				this.source.getProperty("prefix.", null), "empty");
		this.target.addProperty("prefix.", "empty");
		this.recordReturn(this.source,
				this.source.getProperty("prefix.one", null), "1");
		this.target.addProperty("prefix.one", "1");
		this.recordReturn(this.source,
				this.source.getProperty("prefix.two", null), "2");
		this.target.addProperty("prefix.two", "2");
		this.recordReturn(this.source,
				this.source.getProperty("prefix.null", null), null);
		this.target.addProperty("prefix.null", null);

		// Test
		this.replayMockObjects();
		PropertiesUtil.copyPrefixedProperties(this.source, "prefix.",
				this.target);
		this.verifyMockObjects();
	}

}
