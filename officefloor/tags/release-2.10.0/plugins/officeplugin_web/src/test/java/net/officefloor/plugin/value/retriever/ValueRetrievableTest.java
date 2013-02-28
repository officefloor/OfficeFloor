/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2013 Daniel Sagenschneider
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

import java.lang.reflect.Method;

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
		this.doTest("Value", String.class);
	}

	/**
	 * Ensure can retrieve simple property via case insensitive name.
	 */
	public void testCaseInsensitiveSimpleProperty() {
		this.isCaseSensitive = false;
		this.doTest("VALUE", String.class);
		this.doTest("value", String.class);
	}

	/**
	 * Ensure no value on unknown property.
	 */
	public void testUnknownProperty() {
		this.doTest("Unknown", null);
	}

	/**
	 * Ensure able to obtain object <code>toString</code> value.
	 */
	public void testObjectValue() {
		this.doTest("Property", PropertyObject.class);
	}

	/**
	 * Ensure can obtain property following object graph.
	 */
	public void testGraphProperty() {
		this.doTest("Property.Text", String.class);
	}

	/**
	 * Ensure can obtain case insensitive property following object graph.
	 */
	public void testCaseInsensitiveGraphProperty() {
		this.isCaseSensitive = false;
		this.doTest("PROPERTY.TEXT", String.class);
		this.doTest("property.text", String.class);
	}

	/**
	 * Ensure no value for unknown graph property.
	 */
	public void testUnknownGraphProperty() {
		this.doTest("Property.Unkown", null);
	}

	/**
	 * Does the test.
	 * 
	 * @param name
	 *            Name of property to check is available.
	 * @param expectedMethodReturnType
	 *            Expected return type from the {@link Method}. May be
	 *            <code>null</code> to indicate method not found.
	 */
	private void doTest(String name, Class<?> expectedMethodReturnType) {
		try {
			// Create the value retriever
			ValueRetrieverSource source = new ValueRetrieverSourceImpl();
			source.init(!this.isCaseSensitive);
			ValueRetriever<RootObject> retriever = source
					.sourceValueRetriever(RootObject.class);

			// Test
			Method method = retriever.getTypeMethod(name);
			if (expectedMethodReturnType == null) {
				assertNull("Not expecting method for " + name, method);
			} else {
				Class<?> returnType = method.getReturnType();
				assertEquals("Incorrect method return type",
						expectedMethodReturnType, returnType);
			}

		} catch (Exception ex) {
			throw fail(ex);
		}
	}

}