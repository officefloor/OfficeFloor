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
package net.officefloor.compile.impl.util;

import java.util.function.Function;

import net.officefloor.compile.impl.issues.MockCompilerIssues;
import net.officefloor.compile.internal.structure.LinkFlowNode;
import net.officefloor.compile.internal.structure.LinkObjectNode;
import net.officefloor.compile.internal.structure.LinkOfficeNode;
import net.officefloor.compile.internal.structure.LinkTeamNode;
import net.officefloor.compile.internal.structure.Node;
import net.officefloor.compile.issues.CompilerIssues;
import net.officefloor.frame.test.OfficeFrameTestCase;

/**
 * Tests the {@link LinkUtil}.
 * 
 * @author Daniel Sagenschneider
 */
public class LinkUtilTest extends OfficeFrameTestCase {

	/**
	 * {@link CompilerIssues}.
	 */
	private final MockCompilerIssues issues = new MockCompilerIssues(this);

	/**
	 * Ensures can traverse {@link LinkFlowNode} instances.
	 */
	public void testTraverseLinkFlowNodes() {

		// Create the links
		TargetLinkNode target = new TargetLinkNode("TARGET");
		LinkNode link = new LinkNode("LINK", target);

		// Obtain the target
		TargetLinkNode retrieved = this.retrieveFlowTarget(link);
		assertSame("Incorrect target", target, retrieved);
	}

	/**
	 * Ensures can traverse {@link LinkObjectNode} instances.
	 */
	public void testTraverseLinkObjectNodes() {

		// Create the links
		TargetLinkNode target = new TargetLinkNode("TARGET");
		LinkNode link = new LinkNode("LINK", target);

		// Obtain the target
		TargetLinkNode retrieved = this.retrieveObjectTarget(link);
		assertSame("Incorrect target", target, retrieved);
	}

	/**
	 * Ensures can traverse {@link LinkTeamNode} instances.
	 */
	public void testTraverseLinkTeamNodes() {

		// Create the links
		TargetLinkNode target = new TargetLinkNode("TARGET");
		LinkNode link = new LinkNode("LINK", target);

		// Obtain the target
		TargetLinkNode retrieved = this.retrieveTeamTarget(link);
		assertSame("Incorrect target", target, retrieved);
	}

	/**
	 * Ensures can traverse {@link LinkOfficeNode} instances.
	 */
	public void testTraverseLinkOfficeNodes() {

		// Create the links
		TargetLinkNode target = new TargetLinkNode("TARGET");
		LinkNode link = new LinkNode("LINK", target);

		// Obtain the target
		TargetLinkNode retrieved = this.retrieveOfficeTarget(link);
		assertSame("Incorrect target", target, retrieved);
	}

	/**
	 * Ensures no issue is raised if no target is found and <code>null</code>
	 * returned.
	 */
	public void testFindNoTarget() {

		// Create the links
		LinkNode link = new LinkNode("LAST", null);
		for (int i = 0; i < 10; i++) {
			link = new LinkNode(String.valueOf(i), link);
		}

		// Find the target
		TargetLinkNode found = this.findFlowTarget(link);
		assertNull("Should not find target", found);
	}

	/**
	 * Ensures able to find the target.
	 */
	public void testFindTarget() {

		// Create the links
		TargetLinkNode target = new TargetLinkNode("TARGET");
		LinkNode link = new LinkNode("LAST", target);
		for (int i = 0; i < 10; i++) {
			link = new LinkNode(String.valueOf(i), link);
		}

		// Find the target
		TargetLinkNode found = this.findFlowTarget(link);
		assertSame("Incorrect target", target, found);
	}

	/**
	 * Ensure able to find the furtherest target.
	 */
	public void testFindFurtherestFlowTarget() {
		this.doFindFurtherestTargetTest((link) -> LinkUtil
				.findFurtherestTarget((LinkFlowNode) link,
						TargetLinkNode.class, this.issues));
	}

	/**
	 * Ensure able to find the furtherest target.
	 */
	public void testFindFurtherestObjectTarget() {
		this.doFindFurtherestTargetTest((link) -> LinkUtil
				.retrieveFurtherestTarget((LinkObjectNode) link,
						TargetLinkNode.class, this.issues));
	}

	/**
	 * Undertakes finding the furtherest target.
	 * 
	 * @param finder
	 *            Finds the furtherest target.
	 */
	private void doFindFurtherestTargetTest(
			Function<Node, TargetLinkNode> finder) {

		// Create extra links without target
		LinkNode link = new LinkNode("EXTRA_LINK", null);
		for (int i = 0; i < 10; i++) {
			link = new LinkNode("EXTRA_" + i, link);
		}
		TargetLinkNode target = new TargetLinkNode("FURTHEST_TARGET", link);
		link = new LinkNode("BEFORE_TARGET", target);
		// Provide various possible links to the furtherest target
		for (int i = 0; i < 20; i++) {
			link = ((i % 2 == 0) ? new LinkNode("LINK_" + i, link)
					: new TargetLinkNode("TARGET_" + i, link));
		}

		// Find the target
		this.replayMockObjects();
		TargetLinkNode found = finder.apply(link);
		this.verifyMockObjects();
		assertSame("Incorrect target", target, found);
	}

	/**
	 * Ensure issue if not find a node on retrieving furtherest node.
	 */
	public void testNotFindFurtherestNode() {

		// Create extra links without target
		LinkNode link = new LinkNode("LINK", null);
		for (int i = 0; i < 10; i++) {
			link = new LinkNode("LINK_" + i, link);
		}

		// Record issue in not finding target
		this.issues.recordIssue(
				"LINK",
				LinkNode.class,
				"Breaks linking chain to a "
						+ TargetLinkNode.class.getSimpleName());

		// Find the target
		this.replayMockObjects();
		TargetLinkNode target = LinkUtil.retrieveFurtherestTarget(link,
				TargetLinkNode.class, this.issues);
		this.verifyMockObjects();
		assertNull("Should not find target", target);
	}

	/**
	 * Ensure can handle <code>null</code> starting link.
	 */
	public void testNullStartingLink() {
		try {
			this.retrieveFlowTarget(null);
			fail("Should not be successful if null initial node");
		} catch (IllegalArgumentException ex) {
			assertEquals("Incorrect cause",
					"No starting link to find TargetLinkNode", ex.getMessage());
		}
	}

	/**
	 * Ensures can retrieve the link on many steps.
	 */
	public void testRetrieveLinkOnManySteps() {

		// Create the links
		TargetLinkNode target = new TargetLinkNode("TARGET");
		LinkNode link = new LinkNode("LINK", target);
		for (int i = 0; i < 20; i++) {
			link = new LinkNode("LINK_" + i, link);
		}

		// Obtain the target
		TargetLinkNode retrieved = this.retrieveFlowTarget(link);
		assertSame("Incorrect target", target, retrieved);
	}

	/**
	 * Ensures that at least one link is traversed.
	 */
	public void testMustTraverseOneLink() {

		// Create the targets
		TargetLinkNode target = new TargetLinkNode("TARGET");
		TargetLinkNode link = new TargetLinkNode("ORIGIN", target);

		// Obtain the target
		TargetLinkNode retrieved = this.retrieveFlowTarget(link);
		assertSame("Incorrect target", target, retrieved);
	}

	/**
	 * Ensures issue and returns <code>null</code> if no target.
	 */
	public void testNoTarget() {

		// Create the links without a target
		LinkNode link = new LinkNode("LINK", null);
		for (int i = 0; i < 10; i++) {
			link = new LinkNode("LINK_" + i, link);
		}

		// Record not retrieving target
		this.issues.recordIssue(
				"LINK",
				LinkNode.class,
				"Breaks linking chain to a "
						+ TargetLinkNode.class.getSimpleName());

		// Attempt to obtain the target
		TargetLinkNode retrieved = this.retrieveFlowTarget(link);
		assertNull("Should not retrieve target", retrieved);
	}

	/**
	 * Ensure issue on detecting cycle to itself.
	 */
	public void testDetectCycleToSelf() {

		// Create the cycle to itself
		LinkNode loopLink = new LinkNode("LOOP", null);
		loopLink.linkNode(loopLink);

		// Record detecting cycle
		this.issues.recordIssue("LOOP", LinkNode.class,
				"LOOP results in a cycle on linking to a "
						+ TargetLinkNode.class.getSimpleName());

		// Attempt to obtain the target
		TargetLinkNode retrieved = this.retrieveFlowTarget(loopLink);
		assertNull("Should not retrieve target", retrieved);

	}

	/**
	 * Ensures issue on detecting a cycle in the links.
	 */
	public void testDetectCycleInLinks() {

		// Create the links with a cycle
		LinkNode loopLink = new LinkNode("LOOP", null);
		LinkNode link = new LinkNode("LINK", loopLink);
		for (int i = 0; i < 10; i++) {
			link = new LinkNode("CYCLE_" + i, link);
		}
		loopLink.linkNode(link);
		for (int i = 0; i < 10; i++) {
			link = new LinkNode("LINK_" + i, link);
		}

		// Record detecting cycle
		this.issues.recordIssue("LOOP", LinkNode.class,
				"LOOP results in a cycle on linking to a "
						+ TargetLinkNode.class.getSimpleName());

		// Attempt to obtain the target
		TargetLinkNode retrieved = this.retrieveFlowTarget(link);
		assertNull("Should not retrieve target", retrieved);
	}

	/**
	 * Ensures issue on detecting a cycle in the furtherest links.
	 */
	public void testDetectCycleInFurtherestLinks() {

		// Create extra links without target
		LinkNode loopLink = new LinkNode("LOOP", null);
		LinkNode link = new LinkNode("EXTRA_LINK", loopLink);
		for (int i = 0; i < 10; i++) {
			link = new LinkNode("EXTRA_" + i, link);
		}
		TargetLinkNode target = new TargetLinkNode("FURTHEST_TARGET", link);
		link = new LinkNode("BEFORE_TARGET", target);
		for (int i = 0; i < 20; i++) {
			link = ((i % 2 == 0) ? new LinkNode("LINK_" + i, link)
					: new TargetLinkNode("TARGET_" + i, link));
		}
		loopLink.linkNode(link);

		// Record detecting cycle
		this.issues.recordIssue("LOOP", LinkNode.class,
				"LOOP results in a cycle on linking to a "
						+ TargetLinkNode.class.getSimpleName());

		// Attempt to obtain the target
		this.replayMockObjects();
		TargetLinkNode found = LinkUtil.findFurtherestTarget(link,
				TargetLinkNode.class, this.issues);
		this.verifyMockObjects();
		assertNull("Should not retrieve target", found);
	}

	/**
	 * Ensures issue on detecting a cycle in the furtherest links when target
	 * link.
	 */
	public void testDetectCycleWhenTargetInFurtherestLinks() {

		// Create extra links without target
		TargetLinkNode loopLink = new TargetLinkNode("LOOP", null);
		LinkNode link = new LinkNode("EXTRA_LINK", loopLink);
		for (int i = 0; i < 10; i++) {
			link = new LinkNode("EXTRA_" + i, link);
		}
		TargetLinkNode target = new TargetLinkNode("FURTHEST_TARGET", link);
		link = new LinkNode("BEFORE_TARGET", target);
		for (int i = 0; i < 20; i++) {
			link = ((i % 2 == 0) ? new LinkNode("LINK_" + i, link)
					: new TargetLinkNode("TARGET_" + i, link));
		}
		loopLink.linkNode(link);

		// Record detecting cycle
		this.issues.recordIssue("LOOP", TargetLinkNode.class,
				"LOOP results in a cycle on linking to a "
						+ TargetLinkNode.class.getSimpleName());

		// Attempt to obtain the target
		this.replayMockObjects();
		TargetLinkNode found = LinkUtil.findFurtherestTarget(link,
				TargetLinkNode.class, this.issues);
		this.verifyMockObjects();
		assertNull("Should not retrieve target", found);
	}

	/**
	 * Ensure can successfully link {@link LinkFlowNode}.
	 */
	public void testLinkFlowNode() {
		LinkNode node = new LinkNode("NODE", null);
		LinkNode linkNode = new LinkNode("LINK", null);
		LinkFlowNode[] loadedNode = new LinkFlowNode[1];
		LinkUtil.linkFlowNode(node, linkNode, this.issues,
				(link) -> loadedNode[0] = link);
		assertSame("Ensure link loaded", linkNode, loadedNode[0]);
	}

	/**
	 * Ensure issue if duplicate {@link LinkFlowNode} loaded.
	 */
	public void testDuplicateFlowLinkLoaded() {
		LinkNode existingLink = new LinkNode("EXISTING", null);
		LinkNode node = new LinkNode("NODE", existingLink);
		LinkNode newLinkNode = new LinkNode("LINK", null);

		// Issue in linking
		this.issues.recordIssue("NODE", LinkNode.class,
				"Link NODE linked more than once");

		// Undertake link
		this.replayMockObjects();
		LinkUtil.linkFlowNode(node, newLinkNode, this.issues, (link) -> {
			fail("Should not link node");
		});
		this.verifyMockObjects();
	}

	/**
	 * Ensure can successfully link {@link LinkObjectNode}.
	 */
	public void testLinkObjectNode() {
		LinkNode node = new LinkNode("NODE", null);
		LinkNode linkNode = new LinkNode("LINK", null);
		LinkObjectNode[] loadedNode = new LinkObjectNode[1];
		LinkUtil.linkObjectNode(node, linkNode, this.issues,
				(link) -> loadedNode[0] = link);
		assertSame("Ensure link loaded", linkNode, loadedNode[0]);
	}

	/**
	 * Ensure issue if duplicate {@link LinkObjectNode} loaded.
	 */
	public void testDuplicateObjectLinkLoaded() {
		LinkNode existingLink = new LinkNode("EXISTING", null);
		LinkNode node = new LinkNode("NODE", existingLink);
		LinkNode newLinkNode = new LinkNode("LINK", null);

		// Issue in linking
		this.issues.recordIssue("NODE", LinkNode.class,
				"Link NODE linked more than once");

		// Undertake link
		this.replayMockObjects();
		LinkUtil.linkObjectNode(node, newLinkNode, this.issues, (link) -> {
			fail("Should not link node");
		});
		this.verifyMockObjects();
	}

	/**
	 * Retrieves the {@link TargetLinkNode}.
	 * 
	 * @param link
	 *            Starting {@link LinkFlowNode}.
	 * @return {@link TargetLinkNode}.
	 */
	private TargetLinkNode retrieveFlowTarget(LinkFlowNode link) {
		this.replayMockObjects();
		TargetLinkNode retrieved = LinkUtil.retrieveTarget(link,
				TargetLinkNode.class, this.issues);
		this.verifyMockObjects();
		return retrieved;
	}

	/**
	 * Finds the {@link TargetLinkNode}.
	 * 
	 * @param link
	 *            Starting {@link LinkFlowNode}.
	 * @return {@link TargetLinkNode} or <code>null</code> if not found.
	 */
	private TargetLinkNode findFlowTarget(LinkFlowNode link) {
		this.replayMockObjects();
		TargetLinkNode found = LinkUtil.findTarget(link, TargetLinkNode.class,
				this.issues);
		this.verifyMockObjects();
		return found;
	}

	/**
	 * Retrieves the {@link TargetLinkNode}.
	 * 
	 * @param link
	 *            Starting {@link LinkObjectNode}.
	 * @return {@link TargetLinkNode}.
	 */
	private TargetLinkNode retrieveObjectTarget(LinkObjectNode link) {
		this.replayMockObjects();
		TargetLinkNode retrieved = LinkUtil.retrieveTarget(link,
				TargetLinkNode.class, this.issues);
		this.verifyMockObjects();
		return retrieved;
	}

	/**
	 * Retrieves the {@link TargetLinkNode}.
	 * 
	 * @param link
	 *            Starting {@link LinkTeamNode}.
	 * @return {@link TargetLinkNode}.
	 */
	private TargetLinkNode retrieveTeamTarget(LinkTeamNode link) {
		this.replayMockObjects();
		TargetLinkNode retrieved = LinkUtil.retrieveTarget(link,
				TargetLinkNode.class, this.issues);
		this.verifyMockObjects();
		return retrieved;
	}

	/**
	 * Retrieves the {@link TargetLinkNode}.
	 * 
	 * @param link
	 *            Starting {@link LinkOfficeNode}.
	 * @return {@link TargetLinkNode}.
	 */
	private TargetLinkNode retrieveOfficeTarget(LinkOfficeNode link) {
		this.replayMockObjects();
		TargetLinkNode retrieved = LinkUtil.retrieveTarget(link,
				TargetLinkNode.class, this.issues);
		this.verifyMockObjects();
		return retrieved;
	}

	/**
	 * Link node for testing.
	 */
	private class LinkNode implements LinkFlowNode, LinkObjectNode,
			LinkTeamNode, LinkOfficeNode {

		/**
		 * Name of this {@link Node}.
		 */
		private final String nodeName;

		/**
		 * Linked node.
		 */
		private LinkNode linkedNode;

		/**
		 * Flags if this {@link LinkNode} will cause a cycle.
		 */
		private boolean isTraversed = false;

		/**
		 * Initiate.
		 * 
		 * @param nodeName
		 *            Name of this {@link Node}.
		 * @param linkedNode
		 *            Linked node.
		 */
		public LinkNode(String nodeName, LinkNode linkedNode) {
			this.nodeName = nodeName;
			this.linkedNode = linkedNode;
		}

		/**
		 * Links this {@link LinkNode} to the input {@link LinkNode}.
		 * 
		 * @param linkedNode
		 *            Linked {@link LinkNode}.
		 */
		public void linkNode(LinkNode linkedNode) {
			this.linkedNode = linkedNode;
		}

		/**
		 * Obtains the linked {@link LinkNode} validating not in a cycle.
		 * 
		 * @return Linked {@link LinkNode}.
		 */
		private LinkNode getLinkedNode() {

			// Ensure not traverse if this link causes a cycle
			if (this.isTraversed) {
				fail("Should not traverse when in a cycle");
			}

			// Return the linked node
			this.isTraversed = true;
			return this.linkedNode;
		}

		/*
		 * ================= Node =============================
		 */

		@Override
		public String getNodeName() {
			return this.nodeName;
		}

		@Override
		public String getNodeType() {
			return "Link";
		}

		@Override
		public String getLocation() {
			fail("Should not require location");
			return null;
		}

		@Override
		public Node getParentNode() {
			fail("Should not require parent");
			return null;
		}

		/*
		 * ============= LinkFlowNode =========================
		 */

		@Override
		public LinkFlowNode getLinkedFlowNode() {
			return this.getLinkedNode();
		}

		@Override
		public boolean linkFlowNode(LinkFlowNode node) {
			fail("Should not be linking, only following links");
			return false;
		}

		/*
		 * ================ LinkObjectNode ===============================
		 */

		@Override
		public LinkObjectNode getLinkedObjectNode() {
			return this.getLinkedNode();
		}

		@Override
		public boolean linkObjectNode(LinkObjectNode node) {
			fail("Should not be linking, only following links");
			return false;
		}

		/*
		 * =============== LinkTeamNode ==================================
		 */

		@Override
		public LinkTeamNode getLinkedTeamNode() {
			return this.getLinkedNode();
		}

		@Override
		public boolean linkTeamNode(LinkTeamNode node) {
			fail("Should not be linking, only following links");
			return false;
		}

		/*
		 * ================ LinkOfficeNode ===============================
		 */

		@Override
		public LinkOfficeNode getLinkedOfficeNode() {
			return this.getLinkedNode();
		}

		@Override
		public boolean linkOfficeNode(LinkOfficeNode node) {
			fail("Should not be linking, only following links");
			return false;
		}
	}

	/**
	 * Target {@link LinkNode} attempting to obtain.
	 */
	private class TargetLinkNode extends LinkNode {

		/**
		 * Initiate without being linked to a {@link LinkNode}.
		 * 
		 * @param nodeName
		 *            Name of this {@link Node}.
		 */
		public TargetLinkNode(String nodeName) {
			super(nodeName, null);
		}

		/**
		 * Initiate.
		 * 
		 * @param nodeName
		 *            Name of this {@link Node}.
		 * @param linkedNode
		 *            Linked node.
		 */
		public TargetLinkNode(String nodeName, LinkNode linkedNode) {
			super(nodeName, linkedNode);
		}
	}

}