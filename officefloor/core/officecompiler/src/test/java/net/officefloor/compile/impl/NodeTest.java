/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2019 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package net.officefloor.compile.impl;

import net.officefloor.compile.internal.structure.Node;
import net.officefloor.frame.test.OfficeFrameTestCase;

/**
 * Tests the generic {@link Node} functionality.
 * 
 * @author Daniel Sagenschneider
 */
public class NodeTest extends OfficeFrameTestCase {

	/**
	 * Ensure correct qualify naming.
	 */
	public void test_qualify() {

		// No name
		assertEquals("", Node.qualify());
		assertEquals("", Node.qualify((String[]) null));

		// Null details
		assertEquals("ONE.[null].[null]", Node.qualify("ONE", null, null));
		assertEquals("ONE.[null].THREE", Node.qualify("ONE", null, "THREE"));
		assertEquals("TWO.THREE", Node.qualify(null, "TWO", "THREE"));
		assertEquals("THREE", Node.qualify(null, null, "THREE"));

		// Blank
		assertEquals("ONE.[ ].[]", Node.qualify("ONE", " ", ""));
		assertEquals("ONE.[].THREE", Node.qualify("ONE", "", "THREE"));
		assertEquals("TWO.THREE", Node.qualify("", "TWO", "THREE"));
		assertEquals("THREE", Node.qualify("", "", "THREE"));
	}

	/**
	 * Ensure correct root {@link Node} naming.
	 */
	public void testRootNodeNaming() {
		this.doQualifyTest("root", "root", "root.child", "root");
		this.doQualifyTest("unsafe.name", "unsafe_name", "unsafe_name.child", "unsafe.name");
	}

	/**
	 * Ensure correct parent {@link Node} naming.
	 */
	public void testParentNodeNaming() {
		this.doQualifyTest("child", "parent.child", "parent.child.child", "parent", "child");
		this.doQualifyTest("child", "unsafe_name.child", "unsafe_name.child.child", "unsafe.name", "child");
	}

	/**
	 * Ensure handle deep {@link Node} naming.
	 */
	public void testDeepNodeNaming() {
		this.doQualifyTest("leaf", "one.two.three.leaf", "one.two.three.leaf.child", "one", "two", "three", "leaf");
	}

	/**
	 * Ensures correct qualified {@link Node} names.
	 * 
	 * @param name               Name.
	 * @param qualifiedName      Qualified names.
	 * @param qualifiedChildName Qualified child name.
	 * @param names              Names to qualify {@link Node}.
	 */
	private void doQualifyTest(String name, String qualifiedName, String qualifiedChildName, String... names) {
		Node root = leaf(names);
		assertEquals("Incorrect name", name, root.getNodeName());
		assertEquals("Incorrect qualified name", qualifiedName, root.getQualifiedName());
		assertEquals("Incorrect qualified child name", qualifiedChildName, root.getQualifiedName("child"));
	}

	/**
	 * Returns the leaf {@link Node} on chain of {@link Node} instances.
	 * 
	 * @param names Names.
	 * @return Leaf {@link Node}.
	 */
	private static Node leaf(String... names) {
		Node leaf = null;
		Node[] children = new Node[0];
		for (int i = names.length - 1; i >= 0; i--) {
			String name = names[i];
			Node node = n(name, children);
			if (leaf == null) {
				leaf = node;
			}
			children = new Node[] { node };
		}
		return leaf;
	}

	/**
	 * Convenience method to create a {@link NamedNode}.
	 * 
	 * @param name     Name.
	 * @param children Child {@link Node} instances.
	 * @return {@link Node}.
	 */
	private static Node n(String name, Node... children) {
		return new NamedNode(name, children);
	}

	/**
	 * Named {@link Node} for testing qualified naming.
	 */
	private static class NamedNode implements Node {

		/**
		 * Parent {@link Node}.
		 */
		private Node parent = null;

		/**
		 * Name.
		 */
		private final String name;

		/**
		 * Child {@link Node} instances.
		 */
		private final Node[] children;

		/**
		 * Instantiate.
		 * 
		 * @param name     Name of this {@link Node}.
		 * @param children Child {@link Node} instances.
		 */
		private NamedNode(String name, Node[] children) {
			this.name = name;
			this.children = children;

			// Specify as parent of children
			for (Node child : children) {
				((NamedNode) child).parent = this;
			}
		}

		/*
		 * ==================== Node ===========================
		 */

		@Override
		public String getNodeName() {
			return this.name;
		}

		@Override
		public String getNodeType() {
			fail("Should not require node type");
			return null;
		}

		@Override
		public String getLocation() {
			fail("Should not require node location");
			return null;
		}

		@Override
		public Node getParentNode() {
			return this.parent;
		}

		@Override
		public boolean isInitialised() {
			fail("Should not check if initialised");
			return false;
		}

		@Override
		public Node[] getChildNodes() {
			return this.children;
		}
	}

}