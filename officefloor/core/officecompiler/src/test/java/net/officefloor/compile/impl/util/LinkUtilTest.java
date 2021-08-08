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

package net.officefloor.compile.impl.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.compile.internal.structure.AutoWire;
import net.officefloor.compile.internal.structure.AutoWirer;
import net.officefloor.compile.internal.structure.CompileContext;
import net.officefloor.compile.internal.structure.LinkFlowNode;
import net.officefloor.compile.internal.structure.LinkObjectNode;
import net.officefloor.compile.internal.structure.LinkOfficeNode;
import net.officefloor.compile.internal.structure.LinkPoolNode;
import net.officefloor.compile.internal.structure.LinkStartAfterNode;
import net.officefloor.compile.internal.structure.LinkStartBeforeNode;
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
import net.officefloor.frame.test.MockTestSupport;
import net.officefloor.frame.test.TestSupportExtension;
import net.officefloor.plugin.managedobject.clazz.ClassManagedObjectSource;

/**
 * Tests the {@link LinkUtil}.
 * 
 * @author Daniel Sagenschneider
 */
@ExtendWith(TestSupportExtension.class)
public class LinkUtilTest {

	/**
	 * {@link MockTestSupport}.
	 */
	private final MockTestSupport mocks = new MockTestSupport();

	/**
	 * {@link CompilerIssues}.
	 */
	private MockCompilerIssues issues;

	@BeforeEach
	public void setup() {
		this.issues = new MockCompilerIssues(this.mocks);
	}

	/**
	 * Ensures can traverse {@link LinkFlowNode} instances.
	 */
	@Test
	public void traverseLinkFlowNodes() {

		// Create the links
		TargetLinkNode target = new TargetLinkNode("TARGET");
		LinkNode link = new LinkNode("LINK", target);

		// Obtain the target
		TargetLinkNode retrieved = this.retrieveFlowTarget(link);
		assertSame(target, retrieved, "Incorrect target");
	}

	/**
	 * Ensures can traverse {@link LinkObjectNode} instances.
	 */
	@Test
	public void traverseLinkObjectNodes() {

		// Create the links
		TargetLinkNode target = new TargetLinkNode("TARGET");
		LinkNode link = new LinkNode("LINK", target);

		// Obtain the target
		TargetLinkNode retrieved = this.retrieveObjectTarget(link);
		assertSame(target, retrieved, "Incorrect target");
	}

	/**
	 * Ensures can traverse {@link LinkTeamNode} instances.
	 */
	@Test
	public void traverseLinkTeamNodes() {

		// Create the links
		TargetLinkNode target = new TargetLinkNode("TARGET");
		LinkNode link = new LinkNode("LINK", target);

		// Obtain the target
		TargetLinkNode retrieved = this.retrieveTeamTarget(link);
		assertSame(target, retrieved, "Incorrect target");
	}

	/**
	 * Ensures can traverse {@link LinkOfficeNode} instances.
	 */
	@Test
	public void traverseLinkOfficeNodes() {

		// Create the links
		TargetLinkNode target = new TargetLinkNode("TARGET");
		LinkNode link = new LinkNode("LINK", target);

		// Obtain the target
		TargetLinkNode retrieved = this.retrieveOfficeTarget(link);
		assertSame(target, retrieved, "Incorrect target");
	}

	/**
	 * Ensures can traverse {@link LinkPoolNode} instances.
	 */
	@Test
	public void traverseLinkPoolNodes() {

		// Create the links
		TargetLinkNode target = new TargetLinkNode("TARGET");
		LinkNode link = new LinkNode("LINK", target);

		// Obtain the target
		TargetLinkNode retrieved = this.findPoolTarget(link);
		assertSame(target, retrieved, "Incorrect target");
	}

	/**
	 * Ensure can traverse {@link LinkStartBeforeNode} instances.
	 */
	@Test
	public void travelStartBeforeNodes() {

		// Create the links
		TargetLinkNode target = new TargetLinkNode("TARGET");
		LinkNode link = new LinkNode("LINK", target);

		// Obtain the target
		TargetLinkNode[] retrieved = this.findStartBeforeTargets(link);
		assertEquals(1, retrieved.length, "Incorrect number of targets");
		assertSame(target, retrieved[0], "Incorrect target");
	}

	/**
	 * Ensure can travel {@link LinkStartAfterNode} instances.
	 */
	@Test
	public void travelStartAfterNodes() {

		// Create the links
		TargetLinkNode target = new TargetLinkNode("TARGET");
		LinkNode link = new LinkNode("LINK", target);

		// Obtain the target
		TargetLinkNode[] retrieved = this.findStartAfterTargets(link);
		assertEquals(1, retrieved.length, "Incorrect number of targets");
		assertSame(target, retrieved[0], "Incorrect target");
	}

	/**
	 * Ensures no issue is raised if no target is found and <code>null</code>
	 * returned.
	 */
	@Test
	public void findNoTarget() {

		// Create the links
		LinkNode link = new LinkNode("LAST", null);
		for (int i = 0; i < 10; i++) {
			link = new LinkNode(String.valueOf(i), link);
		}

		// Find the target
		TargetLinkNode found = this.findFlowTarget(link);
		assertNull(found, "Should not find target");
	}

	/**
	 * Ensures no issue is raised if no targets is found and empty array returned.
	 */
	@Test
	public void findNoTargets() {

		// Create the links
		LinkNode link = new LinkNode("LAST", null);
		for (int i = 0; i < 10; i++) {
			link = new LinkNode(String.valueOf(i), link);
		}

		// Find the targets
		TargetLinkNode[] found = this.findStartBeforeTargets(link);
		assertEquals(0, found.length, "Should not find targets");
	}

	/**
	 * Ensures able to find the target.
	 */
	@Test
	public void findTarget() {

		// Create the links
		TargetLinkNode target = new TargetLinkNode("TARGET");
		LinkNode link = new LinkNode("LAST", target);
		for (int i = 0; i < 10; i++) {
			link = new LinkNode(String.valueOf(i), link);
		}

		// Find the target
		TargetLinkNode found = this.findFlowTarget(link);
		assertSame(target, found, "Incorrect target");
	}

	/**
	 * Ensure able to find the furtherest target.
	 */
	@Test
	public void findFurtherestFlowTarget() {
		this.doFindFurtherestTargetTest(
				(link) -> LinkUtil.findFurtherestTarget((LinkFlowNode) link, TargetLinkNode.class, this.issues));
	}

	/**
	 * Ensure able to find the furtherest target.
	 */
	@Test
	public void findFurtherestObjectTarget() {
		this.doFindFurtherestTargetTest(
				(link) -> LinkUtil.retrieveFurtherestTarget((LinkObjectNode) link, TargetLinkNode.class, this.issues));
	}

	/**
	 * Undertakes finding the furtherest target.
	 * 
	 * @param finder Finds the furtherest target.
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
		this.mocks.replayMockObjects();
		TargetLinkNode found = finder.apply(link);
		this.mocks.verifyMockObjects();
		assertSame(target, found, "Incorrect target");
	}

	/**
	 * Ensure issue if not find a node on retrieving furtherest node.
	 */
	@Test
	public void notFindFurtherestNode() {

		// Create extra links without target
		LinkNode link = new LinkNode("LINK", null);
		for (int i = 0; i < 10; i++) {
			link = new LinkNode("LINK_" + i, link);
		}

		// Record issue in not finding target
		this.issues.recordIssue("LINK", LinkNode.class,
				"Link LINK is not linked to a " + TargetLinkNode.class.getSimpleName());

		// Find the target
		this.mocks.replayMockObjects();
		TargetLinkNode target = LinkUtil.retrieveFurtherestTarget(link, TargetLinkNode.class, this.issues);
		this.mocks.verifyMockObjects();
		assertNull(target, "Should not find target");
	}

	/**
	 * Ensure can handle <code>null</code> starting link.
	 */
	@Test
	public void nullStartingLinkForTarget() {
		try {
			this.retrieveFlowTarget(null);
			fail("Should not be successful if null initial node");
		} catch (IllegalArgumentException ex) {
			assertEquals("No starting link to find TargetLinkNode", ex.getMessage(), "Incorrect cause");
		}
	}

	/**
	 * Ensure can handle <code>null</code> starting link.
	 */
	@Test
	public void nullStartingLinkForTargets() {
		try {
			this.findStartBeforeTargets(null);
			fail("Should not be successful if null initial node");
		} catch (IllegalArgumentException ex) {
			assertEquals("No starting link to find TargetLinkNode instances", ex.getMessage(), "Incorrect cause");
		}
	}

	/**
	 * Ensures can retrieve the link on many steps.
	 */
	@Test
	public void retrieveLinkOnManySteps() {

		// Create the links
		TargetLinkNode target = new TargetLinkNode("TARGET");
		LinkNode link = new LinkNode("LINK", target);
		for (int i = 0; i < 20; i++) {
			link = new LinkNode("LINK_" + i, link);
		}

		// Obtain the target
		TargetLinkNode retrieved = this.retrieveFlowTarget(link);
		assertSame(target, retrieved, "Incorrect target");
	}

	/**
	 * Ensures that at least one link is traversed.
	 */
	@Test
	public void mustTraverseOneLink() {

		// Create the targets
		TargetLinkNode target = new TargetLinkNode("TARGET");
		TargetLinkNode link = new TargetLinkNode("ORIGIN", target);

		// Obtain the target
		TargetLinkNode retrieved = this.retrieveFlowTarget(link);
		assertSame(target, retrieved, "Incorrect target");
	}

	/**
	 * Ensures issue and returns <code>null</code> if no target.
	 */
	@Test
	public void noTarget() {

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
		assertNull(retrieved, "Should not retrieve target");
	}

	/**
	 * Ensure issue on detecting cycle to itself.
	 */
	@Test
	public void detectCycleToSelfForTarget() {

		// Create the cycle to itself
		LinkNode loopLink = new LinkNode("LOOP", null);
		loopLink.linkNode(loopLink);

		// Record detecting cycle
		this.issues.recordIssue("LOOP", LinkNode.class,
				"LOOP results in a cycle on linking to a " + TargetLinkNode.class.getSimpleName());

		// Attempt to obtain the target
		TargetLinkNode retrieved = this.retrieveFlowTarget(loopLink);
		assertNull(retrieved, "Should not retrieve target");
	}

	/**
	 * Ensure issue on detecting cycle to itself.
	 */
	@Test
	public void detectCycleToSelfForTargets() {

		// Create the cycle to itself
		LinkNode loopLink = new LinkNode("LOOP", null);
		loopLink.linkNode(loopLink);

		// Record detecting cycle
		this.issues.recordIssue("LOOP", LinkNode.class,
				"LOOP results in a cycle on linking to a " + TargetLinkNode.class.getSimpleName());

		// Attempt to obtain the target
		TargetLinkNode[] retrieved = this.findStartBeforeTargets(loopLink);
		assertEquals(0, retrieved.length, "Should not retrieve target");
	}

	/**
	 * Ensures issue on detecting a cycle in the links.
	 */
	@Test
	public void detectCycleInLinksForTarget() {

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
		assertNull(retrieved, "Should not retrieve target");
	}

	/**
	 * Ensures issue on detecting a cycle in the links.
	 */
	@Test
	public void detectCycleInLinksForTargets() {

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
		TargetLinkNode[] retrieved = this.findStartBeforeTargets(loopLink);
		assertEquals(0, retrieved.length, "Should not retrieve target");
	}

	/**
	 * Ensures issue on detecting a cycle in the furtherest links.
	 */
	@Test
	public void detectCycleInFurtherestLinks() {

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
		this.mocks.replayMockObjects();
		TargetLinkNode found = LinkUtil.findFurtherestTarget(link, TargetLinkNode.class, this.issues);
		this.mocks.verifyMockObjects();
		assertNull(found, "Should not retrieve target");
	}

	/**
	 * Ensures issue on detecting a cycle in the furtherest links when target link.
	 */
	@Test
	public void detectCycleWhenTargetInFurtherestLinks() {

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
		this.mocks.replayMockObjects();
		TargetLinkNode found = LinkUtil.findFurtherestTarget(link, TargetLinkNode.class, this.issues);
		this.mocks.verifyMockObjects();
		assertNull(found, "Should not retrieve target");
	}

	/**
	 * Ensure can successfully link {@link LinkFlowNode}.
	 */
	@Test
	public void linkFlowNode() {
		LinkNode node = new LinkNode("NODE", null);
		LinkNode linkNode = new LinkNode("LINK", null);
		LinkFlowNode[] loadedNode = new LinkFlowNode[1];
		LinkUtil.linkFlowNode(node, linkNode, this.issues, (link) -> loadedNode[0] = link);
		assertSame(linkNode, loadedNode[0], "Ensure link loaded");
	}

	/**
	 * Ensure issue if duplicate {@link LinkFlowNode} loaded.
	 */
	@Test
	public void duplicateFlowLinkLoaded() {
		LinkNode existingLink = new LinkNode("EXISTING", null);
		LinkNode node = new LinkNode("NODE", existingLink);
		LinkNode newLinkNode = new LinkNode("LINK", null);

		// Issue in linking
		this.issues.recordIssue("NODE", LinkNode.class, "Link NODE linked more than once");

		// Undertake link
		this.mocks.replayMockObjects();
		LinkUtil.linkFlowNode(node, newLinkNode, this.issues, (link) -> {
			fail("Should not link node");
		});
		this.mocks.verifyMockObjects();
	}

	/**
	 * Ensure can successfully link {@link LinkObjectNode}.
	 */
	@Test
	public void linkObjectNode() {
		LinkNode node = new LinkNode("NODE", null);
		LinkNode linkNode = new LinkNode("LINK", null);
		LinkObjectNode[] loadedNode = new LinkObjectNode[1];
		LinkUtil.linkObjectNode(node, linkNode, this.issues, (link) -> loadedNode[0] = link);
		assertSame(linkNode, loadedNode[0], "Ensure link loaded");
	}

	/**
	 * Ensure issue if duplicate {@link LinkObjectNode} loaded.
	 */
	@Test
	public void duplicateObjectLinkLoaded() {
		LinkNode existingLink = new LinkNode("EXISTING", null);
		LinkNode node = new LinkNode("NODE", existingLink);
		LinkNode newLinkNode = new LinkNode("LINK", null);

		// Issue in linking
		this.issues.recordIssue("NODE", LinkNode.class, "Link NODE linked more than once");

		// Undertake link
		this.mocks.replayMockObjects();
		LinkUtil.linkObjectNode(node, newLinkNode, this.issues, (link) -> {
			fail("Should not link node");
		});
		this.mocks.verifyMockObjects();
	}

	/**
	 * Ensure can {@link AutoWire} the {@link ManagedObjectNode} and ensure its
	 * {@link ManagedObjectSourceNode} is managed by an {@link Office}.
	 */
	@Test
	public void autoWireObjectNode() {

		// Mocks
		@SuppressWarnings("unchecked")
		final AutoWirer<LinkObjectNode> autoWirer = this.mocks.createMock(AutoWirer.class);
		final CompileContext compileContext = this.mocks.createMock(CompileContext.class);
		final ManagedObjectType<?> managedObjectType = this.mocks.createMock(ManagedObjectType.class);

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
		ManagedObjectNode managedObject = context.createManagedObjectNode("MO", officeFloor);
		managedObject.initialise(ManagedObjectScope.THREAD, managedObjectSource);

		// Record the auto-wiring
		this.mocks.recordReturn(compileContext, compileContext.getOrLoadManagedObjectType(managedObjectSource),
				managedObjectType);
		this.mocks.recordReturn(managedObjectType, managedObjectType.getDependencyTypes(),
				new ManagedObjectDependencyType[0]);

		// Link the auto-wire object
		this.mocks.replayMockObjects();
		LinkUtil.linkAutoWireObjectNode(node, managedObject, office, autoWirer, compileContext, this.issues,
				(link) -> node.linkObjectNode(link));
		this.mocks.verifyMockObjects();

		// Ensure the node is linked to the managed object
		assertEquals(managedObject, node.getLinkedObjectNode(), "Node should be linked to managed object");

		// Ensure the managed object source is also managed by the office
		ManagingOfficeNode managingOffice = (ManagingOfficeNode) managedObjectSource.getManagingOffice();
		assertEquals(office, managingOffice.getLinkedOfficeNode(), "Managed object source should be managed by office");
	}

	/**
	 * Ensure can load all {@link AutoWire} instances for the
	 * {@link LinkObjectNode}.
	 */
	@Test
	public void loadAllObjectAutoWires() {

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
		this.mocks.replayMockObjects();
		LinkUtil.loadAllObjectAutoWires(direct, allAutoWires, compileContext, issues);
		this.mocks.verifyMockObjects();

		// Ensure correct number of auto wires
		assertEquals(6, allAutoWires.size(), "Incorrect number of auto-wires");
		assertTrue(allAutoWires.contains(new AutoWire("DIRECT", Object.class)), "No direct auto wire");
		assertTrue(allAutoWires.contains(new AutoWire("DEPENDENCY", Object.class)), "No dependency auto wire");
		assertTrue(allAutoWires.contains(new AutoWire("TRANSITIVE", Object.class)), "No transitive auto wire");
		assertTrue(allAutoWires.contains(new AutoWire("OBJECT_DEPENDENCY", Object.class)),
				"No Office Object implementing auto wire");
		assertTrue(allAutoWires.contains(new AutoWire("OFFICE_OBJECT", Object.class)), "No OfficeObject auto wire");
		assertTrue(allAutoWires.contains(new AutoWire("CYCLE", Object.class)), "No cycle auto wire");
	}

	/**
	 * Retrieves the {@link TargetLinkNode}.
	 * 
	 * @param link Starting {@link LinkFlowNode}.
	 * @return {@link TargetLinkNode}.
	 */
	private TargetLinkNode retrieveFlowTarget(LinkFlowNode link) {
		this.mocks.replayMockObjects();
		TargetLinkNode retrieved = LinkUtil.retrieveTarget(link, TargetLinkNode.class, this.issues);
		this.mocks.verifyMockObjects();
		return retrieved;
	}

	/**
	 * Finds the {@link TargetLinkNode}.
	 * 
	 * @param link Starting {@link LinkFlowNode}.
	 * @return {@link TargetLinkNode} or <code>null</code> if not found.
	 */
	private TargetLinkNode findFlowTarget(LinkFlowNode link) {
		this.mocks.replayMockObjects();
		TargetLinkNode found = LinkUtil.findTarget(link, TargetLinkNode.class, this.issues);
		this.mocks.verifyMockObjects();
		return found;
	}

	/**
	 * Retrieves the {@link TargetLinkNode}.
	 * 
	 * @param link Starting {@link LinkObjectNode}.
	 * @return {@link TargetLinkNode}.
	 */
	private TargetLinkNode retrieveObjectTarget(LinkObjectNode link) {
		this.mocks.replayMockObjects();
		TargetLinkNode retrieved = LinkUtil.retrieveTarget(link, TargetLinkNode.class, this.issues);
		this.mocks.verifyMockObjects();
		return retrieved;
	}

	/**
	 * Retrieves the {@link TargetLinkNode}.
	 * 
	 * @param link Starting {@link LinkTeamNode}.
	 * @return {@link TargetLinkNode}.
	 */
	private TargetLinkNode retrieveTeamTarget(LinkTeamNode link) {
		this.mocks.replayMockObjects();
		TargetLinkNode retrieved = LinkUtil.findTarget(link, TargetLinkNode.class, this.issues);
		this.mocks.verifyMockObjects();
		return retrieved;
	}

	/**
	 * Retrieves the {@link TargetLinkNode}.
	 * 
	 * @param link Starting {@link LinkOfficeNode}.
	 * @return {@link TargetLinkNode}.
	 */
	private TargetLinkNode retrieveOfficeTarget(LinkOfficeNode link) {
		this.mocks.replayMockObjects();
		TargetLinkNode retrieved = LinkUtil.retrieveTarget(link, TargetLinkNode.class, this.issues);
		this.mocks.verifyMockObjects();
		return retrieved;
	}

	/**
	 * Finds the {@link TargetLinkNode}.
	 * 
	 * @param link Starting {@link LinkPoolNode}.
	 * @return {@link TargetLinkNode} or <code>null</code> if target not found.
	 */
	private TargetLinkNode findPoolTarget(LinkPoolNode link) {
		this.mocks.replayMockObjects();
		TargetLinkNode retrieved = LinkUtil.findTarget(link, TargetLinkNode.class, this.issues);
		this.mocks.verifyMockObjects();
		return retrieved;
	}

	/**
	 * Finds the {@link TargetLinkNode}.
	 * 
	 * @param link Starting {@link LinkStartBeforeNode}.
	 * @return {@link TargetLinkNode} targets.
	 */
	private TargetLinkNode[] findStartBeforeTargets(LinkStartBeforeNode link) {
		this.mocks.replayMockObjects();
		TargetLinkNode[] retrieved = LinkUtil.findTargets(link, TargetLinkNode.class, this.issues);
		this.mocks.verifyMockObjects();
		return retrieved;
	}

	/**
	 * Finds the {@link TargetLinkNode}.
	 * 
	 * @param link Starting {@link LinkStartAfterNode}.
	 * @return {@link TargetLinkNode} targets.
	 */
	private TargetLinkNode[] findStartAfterTargets(LinkStartAfterNode link) {
		this.mocks.replayMockObjects();
		TargetLinkNode[] retrieved = LinkUtil.findTargets(link, TargetLinkNode.class, this.issues);
		this.mocks.verifyMockObjects();
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
		 * @param nodeName   Name of this {@link Node}.
		 * @param linkedNode Linked {@link Node}.
		 */
		public AbstractLinkNode(String nodeName, N linkedNode) {
			this.nodeName = nodeName;
			this.linkedNode = linkedNode;
		}

		/**
		 * Links this {@link Node} to the input {@link Node}.
		 * 
		 * @param linkedNode Linked {@link Node}.
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
			return fail("Should not require location");
		}

		@Override
		public Node getParentNode() {
			return null; // no parent for qualified naming
		}

		@Override
		public Node[] getChildNodes() {
			return fail("Should not require children");
		}

		@Override
		public boolean isInitialised() {
			return fail("Should not require initialisation");
		}
	}

	/**
	 * Link {@link Node} for testing.
	 */
	private class LinkNode extends AbstractLinkNode<LinkNode> implements LinkFlowNode, LinkObjectNode, LinkTeamNode,
			LinkOfficeNode, LinkPoolNode, LinkStartBeforeNode, LinkStartAfterNode {

		/**
		 * Instantiate.
		 * 
		 * @param nodeName   Name of this {@link Node}.
		 * @param linkedNode Linked {@link Node}.
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
			return fail("Should not be linking, only following links");
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
			return fail("Should not be linking, only following links");
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
			return fail("Should not be linking, only following links");
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
			return fail("Should not be linking, only following links");
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
			return fail("Should not be linking, only folling links");
		}

		/*
		 * =============== LinkStartBeforeNode ============================
		 */

		@Override
		public boolean linkStartBeforeNode(LinkStartBeforeNode node) {
			return fail("Should not be linking, only folling links");
		}

		@Override
		public LinkStartBeforeNode[] getLinkedStartBeforeNodes() {
			LinkStartBeforeNode node = this.getLinkedNode();
			return node != null ? new LinkStartBeforeNode[] { node } : new LinkStartBeforeNode[0];
		}

		/*
		 * ================ LinkStartAfterNode ============================
		 */

		@Override
		public boolean linkStartAfterNode(LinkStartAfterNode node) {
			return fail("Should not be linking, only folling links");
		}

		@Override
		public LinkStartAfterNode[] getLinkedStartAfterNodes() {
			LinkStartAfterNode node = this.getLinkedNode();
			return node != null ? new LinkStartAfterNode[] { node } : new LinkStartAfterNode[0];
		}
	}

	/**
	 * Target {@link LinkNode} attempting to obtain.
	 */
	private class TargetLinkNode extends LinkNode {

		/**
		 * Initiate without being linked to a {@link LinkNode}.
		 * 
		 * @param nodeName Name of this {@link Node}.
		 */
		public TargetLinkNode(String nodeName) {
			super(nodeName, null);
		}

		/**
		 * Initiate.
		 * 
		 * @param nodeName   Name of this {@link Node}.
		 * @param linkedNode Linked node.
		 */
		public TargetLinkNode(String nodeName, LinkNode linkedNode) {
			super(nodeName, linkedNode);
		}
	}

}
