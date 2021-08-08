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

import net.officefloor.frame.test.OfficeFrameTestCase;

/**
 * Tests the retrieving the values via the {@link ValueRetriever}.
 * 
 * @author Daniel Sagenschneider
 */
public class RetrieveValueTest extends OfficeFrameTestCase {

	/**
	 * Root object.
	 */
	private final RootObject object = this.createMock(RootObject.class);

	/**
	 * Case sensitive.
	 */
	private boolean isCaseSensitive = true;

	/**
	 * Ensure can obtain simple property.
	 */
	public void testSimpleProperty() {
		this.recordReturn(this.object, this.object.getValue(), "TEST");
		this.doTest("Value", "TEST");
	}

	/**
	 * Ensure can obtain simple property via case insensitive name.
	 */
	public void testCaseInsensitiveSimpleProperty() {
		this.isCaseSensitive = false;
		this.recordReturn(this.object, this.object.getValue(), "UPPER");
		this.recordReturn(this.object, this.object.getValue(), "lower");
		this.doTest("VALUE", "UPPER", "value", "lower");
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
		final PropertyObject property = this.createMock(PropertyObject.class);
		this.recordReturn(this.object, this.object.getProperty(), property);
		this.doTest("Property", property);
	}

	/**
	 * Ensure can obtain property following object graph.
	 */
	public void testGraphProperty() {
		final PropertyObject property = this.createMock(PropertyObject.class);
		this.recordReturn(this.object, this.object.getProperty(), property);
		this.recordReturn(property, property.getText(), "TEST");
		this.doTest("Property.Text", "TEST");
	}

	/**
	 * Ensure can obtain case insensitive property following object graph.
	 */
	public void testCaseInsensitiveGraphProperty() {
		this.isCaseSensitive = false;
		final PropertyObject property = this.createMock(PropertyObject.class);
		this.recordReturn(this.object, this.object.getProperty(), property);
		this.recordReturn(property, property.getText(), "Upper");
		this.recordReturn(this.object, this.object.getProperty(), property);
		this.recordReturn(property, property.getText(), "Lower");
		this.doTest("PROPERTY.TEXT", "Upper", "property.text", "Lower");
	}

	/**
	 * Ensure no value for unknown graph property.
	 */
	public void testUnknownGraphProperty() {
		final PropertyObject property = this.createMock(PropertyObject.class);
		this.recordReturn(this.object, this.object.getProperty(), property);
		this.doTest("Property.Unkown", null);
	}

	/**
	 * Ensure able to obtain recursive property.
	 */
	public void testRecursiveProperty() {
		final PropertyObject one = this.createMock(PropertyObject.class);
		final PropertyObject two = this.createMock(PropertyObject.class);
		final PropertyObject three = this.createMock(PropertyObject.class);
		this.recordReturn(this.object, this.object.getProperty(), one);
		this.recordReturn(one, one.getRecursive(), two);
		this.recordReturn(two, two.getRecursive(), three);
		this.recordReturn(three, three.getText(), "TEST");
		this.doTest("Property.Recursive.Recursive.Text", "TEST");
	}

	/**
	 * Does the test.
	 * 
	 * @param nameValuePairs
	 *            Name value pairs expected to be retrieved.
	 */
	private void doTest(Object... nameValuePairs) {
		try {
			// Create the value retriever
			ValueRetrieverSource source = new ValueRetrieverSource(!this.isCaseSensitive);
			ValueRetriever<RootObject> retriever = source.sourceValueRetriever(RootObject.class);

			// Test
			this.replayMockObjects();
			for (int i = 0; i < nameValuePairs.length; i += 2) {
				String name = nameValuePairs[i].toString();
				Object expectedValue = nameValuePairs[i + 1];

				// Retrieve the value
				Object actualValue = retriever.retrieveValue(this.object, name);

				// Ensure expected value
				assertEquals("Incorrect value for property '" + name + "'", expectedValue, actualValue);
			}
			this.verifyMockObjects();
		} catch (Exception ex) {
			throw fail(ex);
		}
	}

}
