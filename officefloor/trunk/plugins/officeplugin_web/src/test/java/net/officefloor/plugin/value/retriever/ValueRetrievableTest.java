/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2010 Daniel Sagenschneider
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
package net.officefloor.plugin.value.retriever;

import net.officefloor.frame.test.OfficeFrameTestCase;

/**
 * Tests determine if value is retrievable via the {@link ValueRetriever}.
 * 
 * @author Daniel Sagenschneider
 */
public class ValueRetrievableTest extends OfficeFrameTestCase {

	/**
	 * Case sensitive.
	 */
	private boolean isCaseSensitive = true;

	/**
	 * Ensure can retrieve simple property.
	 */
	public void testSimpleProperty() {
		this.doTest("Value", true);
	}

	/**
	 * Ensure can retrieve simple property via case insensitive name.
	 */
	public void testCaseInsensitiveSimpleProperty() {
		this.isCaseSensitive = false;
		this.doTest("VALUE", true);
		this.doTest("value", true);
	}

	/**
	 * Ensure no value on unknown property.
	 */
	public void testUnknownProperty() {
		this.doTest("Unknown", false);
	}

	/**
	 * Ensure able to obtain object <code>toString</code> value.
	 */
	public void testObjectValue() {
		this.doTest("Property", true);
	}

	/**
	 * Ensure can obtain property following object graph.
	 */
	public void testGraphProperty() {
		this.doTest("Property.Text", true);
	}

	/**
	 * Ensure can obtain case insensitive property following object graph.
	 */
	public void testCaseInsensitiveGraphProperty() {
		this.isCaseSensitive = false;
		this.doTest("PROPERTY.TEXT", true);
		this.doTest("property.text", true);
	}

	/**
	 * Ensure no value for unknown graph property.
	 */
	public void testUnknownGraphProperty() {
		this.doTest("Property.Unkown", false);
	}

	/**
	 * Does the test.
	 * 
	 * @param name
	 *            Name of property to check is available.
	 * @param isExpectRetrievable
	 *            Indicates if expect to be able to retrieve the value.
	 */
	private void doTest(String name, boolean isExpectRetrievable) {
		try {
			// Create the value retriever
			ValueRetrieverSource source = new ValueRetrieverSourceImpl();
			source.init(!this.isCaseSensitive);
			ValueRetriever<RootObject> retriever = source
					.sourceValueRetriever(RootObject.class);

			// Test
			boolean isActualRetrievable = retriever.isValueRetrievable(name);
			assertEquals("Incorrectly determined if retrievable for property '"
					+ name + "'", isExpectRetrievable, isActualRetrievable);

		} catch (Exception ex) {
			throw fail(ex);
		}
	}

}