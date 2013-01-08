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

import net.officefloor.compile.internal.structure.LinkFlowNode;
import net.officefloor.compile.internal.structure.LinkObjectNode;
import net.officefloor.compile.internal.structure.LinkOfficeNode;
import net.officefloor.compile.internal.structure.LinkTeamNode;
import net.officefloor.compile.issues.CompilerIssues;
import net.officefloor.compile.issues.CompilerIssues.LocationType;
import net.officefloor.frame.api.build.OfficeFloorIssues.AssetType;
import net.officefloor.frame.internal.structure.Asset;
import net.officefloor.frame.test.OfficeFrameTestCase;

/**
 * Tests the {@link LinkUtil}.
 * 
 * @author Daniel Sagenschneider
 */
public class LinkUtilTest extends OfficeFrameTestCase {

	/**
	 * {@link LocationType}.
	 */
	private final static LocationType LOCATION_TYPE = LocationType.SECTION;

	/**
	 * Location.
	 */
	private final static String LOCATION = "SECTION_LOCATION";

	/**
	 * {@link AssetType}.
	 */
	private final static AssetType ASSET_TYPE = AssetType.TASK;

	/**
	 * Name of the {@link Asset}.
	 */
	private final static String ASSET_NAME = "TASK";

	/**
	 * {@link CompilerIssues}.
	 */
	private final CompilerIssues issues = this.createMock(CompilerIssues.class);

	/**
	 * Ensures can traverse {@link LinkFlowNode} instances.
	 */
	public void testTraverseLinkFlowNodes() {

		// Create the links
		TargetLinkNode target = new TargetLinkNode();
		LinkNode link = new LinkNode(target);

		// Obtain the target
		TargetLinkNode retrieved = this.retrieveFlowTarget(link,
				"TaskFlow FLOW");
		assertEquals("Incorrect target", target, retrieved);
	}

	/**
	 * Ensures can traverse {@link LinkObjectNode} instances.
	 */
	public void testTraverseLinkObjectNodes() {

		// Create the links
		TargetLinkNode target = new TargetLinkNode();
		LinkNode link = new LinkNode(target);

		// Obtain the target
		TargetLinkNode retrieved = this.retrieveObjectTarget(link,
				"TaskObject OBJECT");
		assertEquals("Incorrect target", target, retrieved);
	}

	/**
	 * Ensures can traverse {@link LinkTeamNode} instances.
	 */
	public void testTraverseLinkTeamNodes() {

		// Create the links
		TargetLinkNode target = new TargetLinkNode();
		LinkNode link = new LinkNode(target);

		// Obtain the target
		TargetLinkNode retrieved = this.retrieveTeamTarget(link,
				"Office team TEAM");
		assertEquals("Incorrect target", target, retrieved);
	}

	/**
	 * Ensures can traverse {@link LinkOfficeNode} instances.
	 */
	public void testTraverseLinkOfficeNodes() {

		// Create the links
		TargetLinkNode target = new TargetLinkNode();
		LinkNode link = new LinkNode(target);

		// Obtain the target
		TargetLinkNode retrieved = this.retrieveOfficeTarget(link,
				"Office OFFICE");
		assertEquals("Incorrect target", target, retrieved);
	}

	/**
	 * Ensures no issue is raised if no target is found and <code>null</code>
	 * returned.
	 */
	public void testFindNoTarget() {

		// Create the links
		LinkNode link = new LinkNode(null);
		for (int i = 0; i < 10; i++) {
			link = new LinkNode(link);
		}

		// Find the target
		TargetLinkNode found = this.findFlowTarget(link, "TaskFlow FLOW");
		assertNull("Should not find target", found);
	}

	/**
	 * Ensures able to find the target.
	 */
	public void testFindTarget() {

		// Create the links
		TargetLinkNode target = new TargetLinkNode();
		LinkNode link = new LinkNode(target);
		for (int i = 0; i < 10; i++) {
			link = new LinkNode(link);
		}

		// Find the target
		TargetLinkNode found = this.findFlowTarget(link, "TaskFlow FLOW");
		assertEquals("Incorrect target", target, found);
	}

	/**
	 * Ensures able to find the furtherest target.
	 */
	public void testFindFurtherestTarget() {

		// Create extra links without target
		LinkNode link = new LinkNode(null);
		for (int i = 0; i < 10; i++) {
			link = new LinkNode(link);
		}
		TargetLinkNode target = new TargetLinkNode(link);
		link = new LinkNode(target);
		// Provide various possible links to the furtherest target
		for (int i = 0; i < 20; i++) {
			link = ((i % 2 == 0) ? new LinkNode(link)
					: new TargetLinkNode(link));
		}

		// Find the target
		this.replayMockObjects();
		TargetLinkNode found = LinkUtil.findFurtherestTarget(link,
				TargetLinkNode.class, "TaskFlow FLOW", LOCATION_TYPE, LOCATION,
				ASSET_TYPE, ASSET_NAME, this.issues);
		this.verifyMockObjects();
		assertEquals("Incorrect target", target, found);
	}

	/**
	 * Ensure can handle <code>null</code> starting link.
	 */
	public void testNullStartingLink() {

		// Record not retrieving target
		this.record_issue("TaskFlow FLOW is not linked to a "
				+ TargetLinkNode.class.getSimpleName());

		// Attempt to obtain the target (from null)
		TargetLinkNode retrieved = this.retrieveFlowTarget(null,
				"TaskFlow FLOW");
		assertNull("Should not retrieve target", retrieved);
	}

	/**
	 * Ensures can retrieve the link on many steps.
	 */
	public void testRetrieveLinkOnManySteps() {

		// Create the links
		TargetLinkNode target = new TargetLinkNode();
		LinkNode link = new LinkNode(target);
		for (int i = 0; i < 20; i++) {
			link = new LinkNode(link);
		}

		// Obtain the target
		TargetLinkNode retrieved = this.retrieveFlowTarget(link,
				"TaskFlow FLOW");
		assertEquals("Incorrect target", target, retrieved);
	}

	/**
	 * Ensures that at least one link is traversed.
	 */
	public void testMustTraverseOneLink() {

		// Create the targets
		TargetLinkNode target = new TargetLinkNode();
		TargetLinkNode link = new TargetLinkNode(target);

		// Obtain the target
		TargetLinkNode retrieved = this.retrieveFlowTarget(link,
				"TaskFlow FLOW");
		assertEquals("Incorrect target", target, retrieved);
	}

	/**
	 * Ensures issue and returns <code>null</code> if no target.
	 */
	public void testNoTarget() {

		// Create the links without a target
		LinkNode link = new LinkNode(null);
		for (int i = 0; i < 10; i++) {
			link = new LinkNode(link);
		}

		// Record not retrieving target
		this.record_issue("TaskFlow FLOW is not linked to a "
				+ TargetLinkNode.class.getSimpleName());

		// Attempt to obtain the target
		TargetLinkNode retrieved = this.retrieveFlowTarget(link,
				"TaskFlow FLOW");
		assertNull("Should not retrieve target", retrieved);
	}

	/**
	 * Ensures issue on detecting a cycle in the links are returns
	 * <code>null</code>.
	 */
	public void testDetectCycleInLinks() {

		// Create the links with a cycle
		LinkNode loopLink = new LinkNode(null);
		LinkNode link = new LinkNode(loopLink);
		for (int i = 0; i < 10; i++) {
			link = new LinkNode(link);
		}
		loopLink.linkNode(link);
		for (int i = 0; i < 10; i++) {
			link = new LinkNode(link);
		}

		// Record detecting cycle
		this.record_issue("TaskFlow FLOW results in a cycle on linking to a "
				+ TargetLinkNode.class.getSimpleName());

		// Attempt to obtain the target
		TargetLinkNode retrieved = this.retrieveFlowTarget(link,
				"TaskFlow FLOW");
		assertNull("Should not retrieve target", retrieved);
	}

	/**
	 * Retrieves the {@link TargetLinkNode}.
	 * 
	 * @param link
	 *            Starting {@link LinkFlowNode}.
	 * @param startingLinkName
	 *            Name of starting link.
	 * @return {@link TargetLinkNode}.
	 */
	private TargetLinkNode retrieveFlowTarget(LinkFlowNode link,
			String startingLinkName) {
		this.replayMockObjects();
		TargetLinkNode retrieved = LinkUtil.retrieveTarget(link,
				TargetLinkNode.class, startingLinkName, LOCATION_TYPE,
				LOCATION, ASSET_TYPE, ASSET_NAME, this.issues);
		this.verifyMockObjects();
		return retrieved;
	}

	/**
	 * Finds the {@link TargetLinkNode}.
	 * 
	 * @param link
	 *            Starting {@link LinkFlowNode}.
	 * @param startingLinkName
	 *            Name of starting link.
	 * @return {@link TargetLinkNode} or <code>null</code> if not found.
	 */
	private TargetLinkNode findFlowTarget(LinkFlowNode link,
			String startingLinkName) {
		this.replayMockObjects();
		TargetLinkNode found = LinkUtil.findTarget(link, TargetLinkNode.class,
				startingLinkName, LOCATION_TYPE, LOCATION, ASSET_TYPE,
				ASSET_NAME, this.issues);
		this.verifyMockObjects();
		return found;
	}

	/**
	 * Retrieves the {@link TargetLinkNode}.
	 * 
	 * @param link
	 *            Starting {@link LinkObjectNode}.
	 * @param startingLinkName
	 *            Name of starting link.
	 * @return {@link TargetLinkNode}.
	 */
	private TargetLinkNode retrieveObjectTarget(LinkObjectNode link,
			String startingLinkName) {
		this.replayMockObjects();
		TargetLinkNode retrieved = LinkUtil.retrieveTarget(link,
				TargetLinkNode.class, startingLinkName, LOCATION_TYPE,
				LOCATION, ASSET_TYPE, ASSET_NAME, this.issues);
		this.verifyMockObjects();
		return retrieved;
	}

	/**
	 * Retrieves the {@link TargetLinkNode}.
	 * 
	 * @param link
	 *            Starting {@link LinkTeamNode}.
	 * @param startingLinkName
	 *            Name of starting link.
	 * @return {@link TargetLinkNode}.
	 */
	private TargetLinkNode retrieveTeamTarget(LinkTeamNode link,
			String startingLinkName) {
		this.replayMockObjects();
		TargetLinkNode retrieved = LinkUtil.retrieveTarget(link,
				TargetLinkNode.class, startingLinkName, LOCATION_TYPE,
				LOCATION, ASSET_TYPE, ASSET_NAME, this.issues);
		this.verifyMockObjects();
		return retrieved;
	}

	/**
	 * Retrieves the {@link TargetLinkNode}.
	 * 
	 * @param link
	 *            Starting {@link LinkOfficeNode}.
	 * @param startingLinkName
	 *            Name of starting link.
	 * @return {@link TargetLinkNode}.
	 */
	private TargetLinkNode retrieveOfficeTarget(LinkOfficeNode link,
			String startingLinkName) {
		this.replayMockObjects();
		TargetLinkNode retrieved = LinkUtil.retrieveTarget(link,
				TargetLinkNode.class, startingLinkName, LOCATION_TYPE,
				LOCATION, ASSET_TYPE, ASSET_NAME, this.issues);
		this.verifyMockObjects();
		return retrieved;
	}

	/**
	 * Records and issue.
	 * 
	 * @param issueDescription
	 *            Description of the issue.
	 */
	private void record_issue(String issueDescription) {
		this.issues.addIssue(LOCATION_TYPE, LOCATION, ASSET_TYPE, ASSET_NAME,
				issueDescription);
	}

	/**
	 * Link node for testing.
	 */
	private class LinkNode implements LinkFlowNode, LinkObjectNode,
			LinkTeamNode, LinkOfficeNode {

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
		 * @param linkedNode
		 *            Linked node.
		 */
		public LinkNode(LinkNode linkedNode) {
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
		 */
		public TargetLinkNode() {
			super(null);
		}

		/**
		 * Initiate.
		 * 
		 * @param linkedNode
		 *            Linked node.
		 */
		public TargetLinkNode(LinkNode linkedNode) {
			super(linkedNode);
		}
	}
}