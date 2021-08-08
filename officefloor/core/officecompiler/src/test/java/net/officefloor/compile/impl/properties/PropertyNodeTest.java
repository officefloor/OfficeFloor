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

import net.officefloor.compile.impl.structure.PropertyNode;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.frame.test.OfficeFrameTestCase;

/**
 * Tests the {@link PropertyNode}.
 *
 * @author Daniel Sagenschneider
 */
public class PropertyNodeTest extends OfficeFrameTestCase {

	/**
	 * Ensure able to construct with values.
	 */
	public void testSimpleConstructor() {
		PropertyNode node = new PropertyNode("NAME", "LABEL", "DEFAULT_VALUE");
		assertNode(node, "NAME", "LABEL", "DEFAULT_VALUE");
	}

	/**
	 * Ensure able to construct from {@link PropertyList}.
	 */
	public void testConstructProperties() {

		// Create the populated property list
		PropertyList properties = new PropertyListImpl();
		properties.addProperty("ONE", "A").setValue("1");
		properties.addProperty("TWO", "B").setValue("2");

		// Construct the nodes
		PropertyNode[] nodes = PropertyNode.constructPropertyNodes(properties);

		// Validate correct construction
		assertEquals("Incorrect number of nodes", 2, nodes.length);
		assertNode(nodes[0], "ONE", "A", "1");
		assertNode(nodes[1], "TWO", "B", "2");
	}

	/**
	 * Asserts the {@link PropertyNode} to have correct values.
	 * 
	 * @param node
	 *            {@link PropertyNode} to test.
	 * @param expectedName
	 *            Expected name.
	 * @param expectedLabel
	 *            Expected label.
	 * @param expectedDefaultValue
	 *            Expected default value.
	 */
	private static void assertNode(PropertyNode node, String expectedName,
			String expectedLabel, String expectedDefaultValue) {
		assertEquals("Incorrect name", expectedName, node.getName());
		assertEquals("Incorrect label", expectedLabel, node.getLabel());
		assertEquals("Incorrect default value", expectedDefaultValue,
				node.getDefaultValue());
	}

}
