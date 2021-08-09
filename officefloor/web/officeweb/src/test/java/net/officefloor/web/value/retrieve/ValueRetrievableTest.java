/*-
 * #%L
 * Web Plug-in
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

package net.officefloor.web.value.retrieve;

import java.lang.reflect.Method;

import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.web.HttpPathParameter;

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
			ValueRetrieverSource source = new ValueRetrieverSource(!this.isCaseSensitive);
			ValueRetriever<RootObject> retriever = source.sourceValueRetriever(RootObject.class);

			// Test
			Class<?> valueType = retriever.getValueType(name);
			if (expectedMethodReturnType == null) {
				assertNull("Not expecting value type for " + name, valueType);
			} else {
				assertEquals("Incorrect value type", expectedMethodReturnType, valueType);
			}

		} catch (Exception ex) {
			throw fail(ex);
		}
	}

	/**
	 * Ensure able to obtain annotation.
	 */
	public void testAnnotation() {

		// Create the retriever
		ValueRetrieverSource source = new ValueRetrieverSource(this.isCaseSensitive);
		ValueRetriever<RootObject> retriever = source.sourceValueRetriever(RootObject.class);

		// Ensure have annotation
		HttpPathParameter annotation = retriever.getValueAnnotation("Property.text", HttpPathParameter.class);
		assertEquals("Incorrect annotation", "test", annotation.value());

		// Ensure null if not find annotation
		assertNull("Should not have annotation", retriever.getValueAnnotation("Property", HttpPathParameter.class));
	}

}
