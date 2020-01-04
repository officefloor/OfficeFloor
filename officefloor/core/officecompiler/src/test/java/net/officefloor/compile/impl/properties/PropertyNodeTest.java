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
