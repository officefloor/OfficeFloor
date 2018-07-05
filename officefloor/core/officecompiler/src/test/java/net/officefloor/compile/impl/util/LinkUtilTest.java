/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
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
package net.officefloor.compile.impl.util;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.compile.internal.structure.AutoWire;
import net.officefloor.compile.internal.structure.AutoWirer;
import net.officefloor.compile.internal.structure.CompileContext;
import net.officefloor.compile.internal.structure.LinkFlowNode;
import net.officefloor.compile.internal.structure.LinkObjectNode;
import net.officefloor.compile.internal.structure.LinkOfficeNode;
import net.officefloor.compile.internal.structure.LinkPoolNode;
import net.officefloor.compile.internal.structure.LinkTeamNode;
import net.officefloor.compile.internal.structure.ManagedObjectNode;
import net.officefloor.compile.internal.structure.ManagedObjectSourceNode;
import net.officefloor.compile.internal.structure.ManagingOfficeNode;
import net.officefloor.compile.internal.structure.Node;
import net.officefloor.compile.internal.structure.NodeContext;
import net.officefloor.compile.internal.structure.OfficeFloorNode;
import net.officefloor.compile.internal.structure.OfficeNode;
import net.officefloor.compile.internal.structure.OfficeObjectNode;
import net.officefloor.compile.issues.CompilerIssues;
import net.officefloor.compile.managedobject.ManagedObjectDependencyType;
import net.officefloor.compile.managedobject.ManagedObjectType;
import net.officefloor.compile.test.issues.MockCompilerIssues;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.managedobject.clazz.ClassManagedObjectSource;

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
	 * Ensures can traverse {@link LinkPoolNode} instances.
	 */
	public void testTraverseLinkPoolNodes() {

		// Create the links
		TargetLinkNode target = new TargetLinkNode("TARGET");
		LinkNode link = new LinkNode("LINK", target);

		// Obtain the target
		TargetLinkNode retrieved = this.findPoolTarget(link);
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
		this.doFindFurtherestTargetTest(
				(link) -> LinkUtil.findFurtherestTarget((LinkFlowNode) link, TargetLinkNode.class, this.issues));
	}

	/**
	 * Ensure able to find the furtherest target.
	 */
	public void testFindFurtherestObjectTarget() {
		this.doFindFurtherestTargetTest(
				(link) -> LinkUtil.retrieveFurtherestTarget((LinkObjectNode) link, TargetLinkNode.class, this.issues));
	}

	/**
	 * Undertakes finding the furtherest target.
	 * 
	 * @param finder
	 *            Finds the furtherest target.
	 */
	private void doFindFurtherestTargetTest(Function<Node, TargetLinkNode> finder) {

		// Create extra links without target
		LinkNode link = new LinkNode("EXTRA_LINK", null);
		for (int i = 0; i < 10; i++) {
			link = new LinkNode("EXTRA_" + i, link);
		}
		TargetLinkNode target = new TargetLinkNode("FURTHEST_TARGET", link);
		link = new LinkNode("BEFORE_TARGET", target);
		// Provide various possible links to the furtherest target
		for (int i = 0; i < 20; i++) {
			link = ((i % 2 == 0) ? new LinkNode("LINK_" + i, link) : new TargetLinkNode("TARGET_" + i, link));
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
		this.issues.recordIssue("LINK", LinkNode.class,
				"Link LINK is not linked to a " + TargetLinkNode.class.getSimpleName());

		// Find the target
		this.replayMockObjects();
		TargetLinkNode target = LinkUtil.retrieveFurtherestTarget(link, TargetLinkNode.class, this.issues);
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
			assertEquals("Incorrect cause", "No starting link to find TargetLinkNode", ex.getMessage());
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
		this.issues.recordIssue("LINK", LinkNode.class,
				"Link LINK is not linked to a " + TargetLinkNode.class.getSimpleName());

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
				"LOOP results in a cycle on linking to a " + TargetLinkNode.class.getSimpleName());

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
				"LOOP results in a cycle on linking to a " + TargetLinkNode.class.getSimpleName());

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
			link = ((i % 2 == 0) ? new LinkNode("LINK_" + i, link) : new TargetLinkNode("TARGET_" + i, link));
		}
		loopLink.linkNode(link);

		// Record detecting cycle
		this.issues.recordIssue("LOOP", LinkNode.class,
				"LOOP results in a cycle on linking to a " + TargetLinkNode.class.getSimpleName());

		// Attempt to obtain the target
		this.replayMockObjects();
		TargetLinkNode found = LinkUtil.findFurtherestTarget(link, TargetLinkNode.class, this.issues);
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
			link = ((i % 2 == 0) ? new LinkNode("LINK_" + i, link) : new TargetLinkNode("TARGET_" + i, link));
		}
		loopLink.linkNode(link);

		// Record detecting cycle
		this.issues.recordIssue("LOOP", TargetLinkNode.class,
				"LOOP results in a cycle on linking to a " + TargetLinkNode.class.getSimpleName());

		// Attempt to obtain the target
		this.replayMockObjects();
		TargetLinkNode found = LinkUtil.findFurtherestTarget(link, TargetLinkNode.class, this.issues);
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
		LinkUtil.linkFlowNode(node, linkNode, this.issues, (link) -> loadedNode[0] = link);
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
		this.issues.recordIssue("NODE", LinkNode.class, "Link NODE linked more than once");

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
		LinkUtil.linkObjectNode(node, linkNode, this.issues, (link) -> loadedNode[0] = link);
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
		this.issues.recordIssue("NODE", LinkNode.class, "Link NODE linked more than once");

		// Undertake link
		this.replayMockObjects();
		LinkUtil.linkObjectNode(node, newLinkNode, this.issues, (link) -> {
			fail("Should not link node");
		});
		this.verifyMockObjects();
	}

	/**
	 * Ensure can {@link AutoWire} the {@link ManagedObjectNode} and ensure its
	 * {@link ManagedObjectSourceNode} is managed by an {@link Office}.
	 */
	public void testAutoWireObjectNode() {

		// Mocks
		@SuppressWarnings("unchecked")
		final AutoWirer<LinkObjectNode> autoWirer = this.createMock(AutoWirer.class);
		final CompileContext compileContext = this.createMock(CompileContext.class);
		final ManagedObjectType<?> managedObjectType = this.createMock(ManagedObjectType.class);

		// Obtain the node context
		NodeContext context = (NodeContext) OfficeFloorCompiler.newOfficeFloorCompiler(null);

		// Create the office
		OfficeFloorNode officeFloor = context.createOfficeFloorNode("net.example.ExampleOfficeFloorSource", null,
				"location");
		OfficeNode office = context.createOfficeNode("OFFICE", officeFloor);

		// Create the source node
		OfficeObjectNode node = context.createOfficeObjectNode("OBJECT", office);

		// Create the managed object within the OfficeFloor
		ManagedObjectSourceNode managedObjectSource = context.createManagedObjectSourceNode("MOS", officeFloor);
		ManagedObjectNode managedObject = context.createManagedObjectNode("MO");
		managedObject.initialise(ManagedObjectScope.THREAD, managedObjectSource);

		// Record the auto-wiring
		this.recordReturn(compileContext, compileContext.getOrLoadManagedObjectType(managedObjectSource),
				managedObjectType);
		this.recordReturn(managedObjectType, managedObjectType.getDependencyTypes(),
				new ManagedObjectDependencyType[0]);

		// Link the auto-wire object
		this.replayMockObjects();
		LinkUtil.linkAutoWireObjectNode(node, managedObject, office, autoWirer, compileContext, this.issues,
				(link) -> node.linkObjectNode(link));
		this.verifyMockObjects();

		// Ensure the node is linked to the managed object
		assertEquals("Node should be linked to managed object", managedObject, node.getLinkedObjectNode());

		// Ensure the managed object source is also managed by the office
		ManagingOfficeNode managingOffice = (ManagingOfficeNode) managedObjectSource.getManagingOffice();
		assertEquals("Managed object source should be managed by office", office, managingOffice.getLinkedOfficeNode());
	}

	/**
	 * Ensure can load all {@link AutoWire} instances for the
	 * {@link LinkObjectNode}.
	 */
	public void testLoadAllObjectAutoWires() {

		// Obtain the node context
		NodeContext context = (NodeContext) OfficeFloorCompiler.newOfficeFloorCompiler(null);

		// Compile context
		final CompileContext compileContext = context.createCompileContext();

		// Create the OfficeFloor and Office
		OfficeFloorNode officeFloor = context.createOfficeFloorNode("net.example.ExampleOfficeFloorSource", null,
				"location");
		OfficeNode office = context.createOfficeNode("OFFICE", officeFloor);

		// Create the direct dependency
		ManagedObjectSourceNode directSource = context.createManagedObjectSourceNode("DIRECT_SOURCE", officeFloor);
		directSource.initialise(ClassManagedObjectSource.class.getName(), null);
		directSource.addProperty(ClassManagedObjectSource.CLASS_NAME_PROPERTY_NAME, Object.class.getName());
		ManagedObjectNode direct = (ManagedObjectNode) directSource.addOfficeFloorManagedObject("DIRECT",
				ManagedObjectScope.PROCESS);
		direct.addTypeQualification("DIRECT", Object.class.getName());

		// Create the dependency
		ManagedObjectSourceNode dependencySource = context.createManagedObjectSourceNode("DEPENDENCY_SOURCE",
				officeFloor);
		dependencySource.initialise(ClassManagedObjectSource.class.getName(), null);
		dependencySource.addProperty(ClassManagedObjectSource.CLASS_NAME_PROPERTY_NAME, Object.class.getName());
		ManagedObjectNode dependency = (ManagedObjectNode) dependencySource.addOfficeFloorManagedObject("DEPENDENCY",
				ManagedObjectScope.PROCESS);
		dependency.addTypeQualification("DEPENDENCY", Object.class.getName());
		((LinkObjectNode) direct.getSectionManagedObjectDependency("dependency")).linkObjectNode(dependency);

		// Create the transitive dependency
		ManagedObjectSourceNode transitiveSource = context.createManagedObjectSourceNode("TRANSITIVE_SOURCE",
				officeFloor);
		transitiveSource.initialise(ClassManagedObjectSource.class.getName(), null);
		transitiveSource.addProperty(ClassManagedObjectSource.CLASS_NAME_PROPERTY_NAME, Object.class.getName());
		ManagedObjectNode transitive = (ManagedObjectNode) transitiveSource.addOfficeFloorManagedObject("TRANSITIVE",
				ManagedObjectScope.PROCESS);
		transitive.addTypeQualification("TRANSITIVE", Object.class.getName());
		((LinkObjectNode) dependency.getSectionManagedObjectDependency("dependency")).linkObjectNode(transitive);

		// Create the ignored Office Object (as linked to managed object)
		OfficeObjectNode linkedOfficeObject = context.createOfficeObjectNode("IGNORED", office);
		((LinkObjectNode) direct.getSectionManagedObjectDependency("implemented")).linkObjectNode(linkedOfficeObject);
		ManagedObjectSourceNode implementingSource = context.createManagedObjectSourceNode("IMPLEMENTING_SOURCE",
				officeFloor);
		implementingSource.initialise(ClassManagedObjectSource.class.getName(), null);
		implementingSource.addProperty(ClassManagedObjectSource.CLASS_NAME_PROPERTY_NAME, Object.class.getName());
		ManagedObjectNode implementing = (ManagedObjectNode) implementingSource
				.addOfficeFloorManagedObject("IMPLEMENTING", ManagedObjectScope.PROCESS);
		implementing.addTypeQualification("OBJECT_DEPENDENCY", Object.class.getName());
		linkedOfficeObject.linkObjectNode(implementing);

		// Create the used Office Object (testing but will error compile)
		OfficeObjectNode officeObject = context.createOfficeObjectNode("NOT_LINKED", office);
		officeObject.initialise(Object.class.getName());
		officeObject.setTypeQualifier("OFFICE_OBJECT");
		((LinkObjectNode) direct.getSectionManagedObjectDependency("object")).linkObjectNode(officeObject);

		// Ensure handle cycle and not infinite loop
		ManagedObjectSourceNode cycleSource = context.createManagedObjectSourceNode("CYCLE_SOURCE", officeFloor);
		cycleSource.initialise(ClassManagedObjectSource.class.getName(), null);
		cycleSource.addProperty(ClassManagedObjectSource.CLASS_NAME_PROPERTY_NAME, Object.class.getName());
		ManagedObjectNode cycle = (ManagedObjectNode) cycleSource.addOfficeFloorManagedObject("CYCLE",
				ManagedObjectScope.PROCESS);
		cycle.addTypeQualification("CYCLE", Object.class.getName());
		((LinkObjectNode) direct.getSectionManagedObjectDependency("cycle")).linkObjectNode(cycle);
		((LinkObjectNode) cycle.getSectionManagedObjectDependency("cycle")).linkObjectNode(direct);

		// Load the auto wire objects
		Set<AutoWire> allAutoWires = new HashSet<>();
		this.replayMockObjects();
		LinkUtil.loadAllObjectAutoWires(direct, allAutoWires, compileContext, issues);
		this.verifyMockObjects();

		// Ensure correct number of auto wires
		assertEquals("Incorrect number of auto-wires", 6, allAutoWires.size());
		assertTrue("No direct auto wire", allAutoWires.contains(new AutoWire("DIRECT", Object.class)));
		assertTrue("No dependency auto wire", allAutoWires.contains(new AutoWire("DEPENDENCY", Object.class)));
		assertTrue("No transitive auto wire", allAutoWires.contains(new AutoWire("TRANSITIVE", Object.class)));
		assertTrue("No Office Object implementing auto wire",
				allAutoWires.contains(new AutoWire("OBJECT_DEPENDENCY", Object.class)));
		assertTrue("No OfficeObject auto wire", allAutoWires.contains(new AutoWire("OFFICE_OBJECT", Object.class)));
		assertTrue("No cycle auto wire", allAutoWires.contains(new AutoWire("CYCLE", Object.class)));
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
		TargetLinkNode retrieved = LinkUtil.retrieveTarget(link, TargetLinkNode.class, this.issues);
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
		TargetLinkNode found = LinkUtil.findTarget(link, TargetLinkNode.class, this.issues);
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
		TargetLinkNode retrieved = LinkUtil.retrieveTarget(link, TargetLinkNode.class, this.issues);
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
		TargetLinkNode retrieved = LinkUtil.findTarget(link, TargetLinkNode.class, this.issues);
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
		TargetLinkNode retrieved = LinkUtil.retrieveTarget(link, TargetLinkNode.class, this.issues);
		this.verifyMockObjects();
		return retrieved;
	}

	/**
	 * Finds the {@link TargetLinkNode}.
	 * 
	 * @param link
	 *            Starting {@link LinkPoolNode}.
	 * @return {@link TargetLinkNode} or <code>null</code> if target not found.
	 */
	private TargetLinkNode findPoolTarget(LinkPoolNode link) {
		this.replayMockObjects();
		TargetLinkNode retrieved = LinkUtil.findTarget(link, TargetLinkNode.class, this.issues);
		this.verifyMockObjects();
		return retrieved;
	}

	/**
	 * Abstract {@link Node} for testing.
	 */
	private class AbstractLinkNode<N extends Node> implements Node {

		/**
		 * Name of this {@link Node}.
		 */
		private final String nodeName;

		/**
		 * Linked node.
		 */
		private N linkedNode;

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
		 *            Linked {@link Node}.
		 */
		public AbstractLinkNode(String nodeName, N linkedNode) {
			this.nodeName = nodeName;
			this.linkedNode = linkedNode;
		}

		/**
		 * Links this {@link Node} to the input {@link Node}.
		 * 
		 * @param linkedNode
		 *            Linked {@link Node}.
		 */
		public void linkNode(N linkedNode) {
			this.linkedNode = linkedNode;
		}

		/**
		 * Obtains the linked {@link Node} validating not in a cycle.
		 * 
		 * @return Linked {@link Node}.
		 */
		protected N getLinkedNode() {

			// Ensure not traverse if this link causes a cycle
			if (this.isTraversed) {
				fail("Should only traverse once, otherwise in a cycle");
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

		@Override
		public Node[] getChildNodes() {
			fail("Should not require children");
			return null;
		}

		@Override
		public boolean isInitialised() {
			fail("Should not require initialisation");
			return false;
		}
	}

	/**
	 * Link {@link Node} for testing.
	 */
	private class LinkNode extends AbstractLinkNode<LinkNode>
			implements LinkFlowNode, LinkObjectNode, LinkTeamNode, LinkOfficeNode, LinkPoolNode {

		/**
		 * Instantiate.
		 * 
		 * @param nodeName
		 *            Name of this {@link Node}.
		 * @param linkedNode
		 *            Linked {@link Node}.
		 */
		public LinkNode(String nodeName, LinkNode linkedNode) {
			super(nodeName, linkedNode);
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

		/*
		 * ================== LinkPoolNode ===============================
		 */

		@Override
		public LinkPoolNode getLinkedPoolNode() {
			return this.getLinkedNode();
		}

		@Override
		public boolean linkPoolNode(LinkPoolNode node) {
			fail("Should not be linking, only folling links");
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