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

package net.officefloor.compile.impl;

import static org.junit.Assert.assertArrayEquals;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.function.Consumer;

import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.compile.internal.structure.AdministrationNode;
import net.officefloor.compile.internal.structure.CompileContext;
import net.officefloor.compile.internal.structure.EscalationNode;
import net.officefloor.compile.internal.structure.ExecutionStrategyNode;
import net.officefloor.compile.internal.structure.ExecutiveNode;
import net.officefloor.compile.internal.structure.FunctionFlowNode;
import net.officefloor.compile.internal.structure.FunctionNamespaceNode;
import net.officefloor.compile.internal.structure.FunctionObjectNode;
import net.officefloor.compile.internal.structure.GovernanceNode;
import net.officefloor.compile.internal.structure.InputManagedObjectNode;
import net.officefloor.compile.internal.structure.ManagedFunctionNode;
import net.officefloor.compile.internal.structure.ManagedObjectDependencyNode;
import net.officefloor.compile.internal.structure.ManagedObjectExecutionStrategyNode;
import net.officefloor.compile.internal.structure.ManagedObjectFlowNode;
import net.officefloor.compile.internal.structure.ManagedObjectFunctionDependencyNode;
import net.officefloor.compile.internal.structure.ManagedObjectNode;
import net.officefloor.compile.internal.structure.ManagedObjectPoolNode;
import net.officefloor.compile.internal.structure.ManagedObjectSourceNode;
import net.officefloor.compile.internal.structure.ManagedObjectTeamNode;
import net.officefloor.compile.internal.structure.ManagingOfficeNode;
import net.officefloor.compile.internal.structure.Node;
import net.officefloor.compile.internal.structure.NodeContext;
import net.officefloor.compile.internal.structure.OfficeFloorNode;
import net.officefloor.compile.internal.structure.OfficeInputNode;
import net.officefloor.compile.internal.structure.OfficeNode;
import net.officefloor.compile.internal.structure.OfficeObjectNode;
import net.officefloor.compile.internal.structure.OfficeOutputNode;
import net.officefloor.compile.internal.structure.OfficeStartNode;
import net.officefloor.compile.internal.structure.OfficeTeamNode;
import net.officefloor.compile.internal.structure.OptionalThreadLocalReceiver;
import net.officefloor.compile.internal.structure.ResponsibleTeamNode;
import net.officefloor.compile.internal.structure.SectionInputNode;
import net.officefloor.compile.internal.structure.SectionNode;
import net.officefloor.compile.internal.structure.SectionObjectNode;
import net.officefloor.compile.internal.structure.SectionOutputNode;
import net.officefloor.compile.internal.structure.SuppliedManagedObjectSourceNode;
import net.officefloor.compile.internal.structure.SupplierNode;
import net.officefloor.compile.internal.structure.SupplierThreadLocalNode;
import net.officefloor.compile.internal.structure.TeamNode;
import net.officefloor.compile.internal.structure.TeamOversightNode;
import net.officefloor.compile.managedobject.ManagedObjectType;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.section.TypeQualification;
import net.officefloor.compile.test.properties.PropertyListUtil;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.source.SourceContext;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.frame.test.MockClockFactory;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.frame.test.ParameterCapture;

/**
 * Tests the {@link OfficeFloorCompiler}.
 *
 * @author Daniel Sagenschneider
 */
public class NodeContextTest extends OfficeFrameTestCase {

	/**
	 * {@link NodeContext}.
	 */
	private final NodeContext context = new OfficeFloorCompilerImpl();

	/**
	 * Mock {@link OfficeFloorNode}.
	 */
	private final OfficeFloorNode officeFloor = this.createMock(OfficeFloorNode.class);

	/**
	 * Mock {@link Office}.
	 */
	private final OfficeNode office = this.createMock(OfficeNode.class);

	/**
	 * Mock {@link SectionNode}.
	 */
	private final SectionNode section = this.createMock(SectionNode.class);

	/**
	 * Mock {@link ManagedFunctionNode}.
	 */
	private final ManagedFunctionNode function = this.createMock(ManagedFunctionNode.class);

	/**
	 * Mock {@link ManagedObjectSourceNode}.
	 */
	private final ManagedObjectSourceNode managedObjectSource = this.createMock(ManagedObjectSourceNode.class);

	/**
	 * Mock {@link ExecutiveNode}.
	 */
	private final ExecutiveNode executive = this.createMock(ExecutiveNode.class);

	/**
	 * Ensure can determine additional profiles.
	 */
	public void testAdditionalProfiles() throws Exception {

		// Record additional profiles
		this.recordReturn(this.office, this.office.getAdditionalProfiles(), new String[] { "additional" });
		this.replayMockObjects();

		// Ensure no office (OfficeFloor)
		assertProfiles(this.context.additionalProfiles(null));

		// Ensure additional properties from Office
		assertProfiles(this.context.additionalProfiles(this.office), "additional");

		// Verify
		this.verifyMockObjects();
	}

	/**
	 * Asserts the profiles.
	 * 
	 * @param actualProfiles   Actual profiles.
	 * @param expectedProfiles Expected profiles.
	 */
	private static void assertProfiles(String[] actualProfiles, String... expectedProfiles) {
		assertEquals("Incorrect number of profiles", expectedProfiles.length, actualProfiles.length);
		for (int i = 0; i < expectedProfiles.length; i++) {
			assertEquals("Incorrect profile " + i, expectedProfiles[i], actualProfiles[i]);
		}
	}

	/**
	 * Ensure can override {@link PropertyList}.
	 */
	public void testOverrideProperties() throws Exception {

		// Create the properties
		PropertyList original = OfficeFloorCompiler.newPropertyList();
		original.addProperty("name").setValue("value");
		original.addProperty("override").setValue("ORIGINAL");

		// Create the Office override properties
		PropertyList office = OfficeFloorCompiler.newPropertyList();
		office.addProperty("OFFICE.different.prefix").setValue("NOT_INCLUDED");
		office.addProperty("OFFICE.qualified.prefix.override").setValue("OFFICE_OVERRIDE");

		// Record the office overrides
		this.recordReturn(this.office, this.office.getOverridePropertyList(), office);
		this.recordReturn(this.office, this.office.getOverridePropertyList(), office);
		this.replayMockObjects();

		final String qualifiedPrefix = "OFFICE.qualified.prefix";

		// Ensure not override properties
		PropertyList noOverrides = this.context.overrideProperties(this.function, qualifiedPrefix, original);
		PropertyListUtil.assertPropertyValues(noOverrides, "name", "value", "override", "ORIGINAL");

		// Ensure override via office overrides
		PropertyList officeOverriden = this.context.overrideProperties(this.function, qualifiedPrefix, this.office,
				original);
		PropertyListUtil.assertPropertyValues(officeOverriden, "name", "value", "override", "OFFICE_OVERRIDE");

		// Ensure override via directory (takes precedence over office overrides)
		File propertiesDirectory = this.findFile(this.getClass(), qualifiedPrefix + ".properties").getParentFile();
		((OfficeFloorCompiler) this.context).setOverridePropertiesDirectory(propertiesDirectory);
		PropertyList dirOverriden = this.context.overrideProperties(this.function, qualifiedPrefix, this.office,
				original);
		PropertyListUtil.assertPropertyValues(dirOverriden, "name", "value", "override", "DIRECTORY_OVERRIDE");

		// Verify
		this.verifyMockObjects();
	}

	/**
	 * Ensure reset the root {@link SourceContext} on configuring relevant
	 * {@link OfficeFloorCompiler}.
	 */
	public void testResetRootSourceContext() {

		// Obtain the compiler
		OfficeFloorCompiler compiler = (OfficeFloorCompiler) this.context;

		// Load the root source context
		SourceContext initialContext = this.context.getRootSourceContext();

		// Add profile and ensure available in source context
		compiler.addProfile("CHANGE");
		SourceContext profileContext = this.context.getRootSourceContext();
		assertNotSame("Should be new context for profile", initialContext, profileContext);
		assertTrue("Should have profile", profileContext.getProfiles().contains("CHANGE"));

		// Change clock
		final Long currentTime = 10L;
		compiler.setClockFactory(new MockClockFactory(currentTime));
		SourceContext clockContext = this.context.getRootSourceContext();
		assertNotSame("Should be new context for clock", profileContext, clockContext);
		assertEquals("Incorrect clock", currentTime, clockContext.getClock((time) -> time).getTime());

		// Add resource
		compiler.addResources((resourceName) -> new ByteArrayInputStream(resourceName.getBytes()));
		SourceContext resourceContext = this.context.getRootSourceContext();
		assertNotSame("Should be new context for resource", resourceContext, clockContext);
		assertContents(new StringReader("TEST"), new InputStreamReader(resourceContext.getResource("TEST")));
	}

	/**
	 * Ensure create {@link SectionNode} within {@link OfficeNode}.
	 */
	public void testCreateSectionNode_withinOffice() {
		this.recordReturn(this.office, this.office.getOfficeFloorNode(), this.officeFloor);
		this.recordReturn(this.office, this.office.getOfficeFloorNode(), this.officeFloor);
		this.recordReturn(this.office, this.office.getQualifiedName("SECTION"), "OFFICE.SECTION");
		SectionNode node = this.doTest(() -> {
			SectionNode section = this.context.createSectionNode("SECTION", this.office);
			assertNode(section, "SECTION", "Section", "[NOT INITIALISED]", this.office);
			assertChildren(section, section.getOfficeSectionInput("INPUT"), section.getOfficeSectionOutput("OUTPUT"),
					section.getOfficeSectionObject("OBJECT"), section.getOfficeSubSection("SUB_SECTION"),
					section.getOfficeSectionFunction("FUNCTION"), section.getOfficeSectionManagedObjectSource("MOS"),
					section.getOfficeSectionManagedObject("MO"));
			assertEquals("Incorrect qualified name", "OFFICE.SECTION.QUALIFIED", section.getQualifiedName("QUALIFIED"));
			return section;
		});

		// Ensure section node valid
		assertEquals("Incorrect section name", "SECTION", node.getOfficeSectionName());
		assertSame("Incorrect office node", this.office, node.getOfficeNode());
		assertNull("Should not have parent section", node.getParentSectionNode());

		// Ensure provide location
		assertInitialise(node, (n) -> n.initialise("ExampleSectionSource", null, "LOCATION"));
		assertEquals("Incorrect location", "ExampleSectionSource(LOCATION)", node.getLocation());
	}

	/**
	 * Ensure create {@link SectionNode} within another {@link SectionNode}.
	 */
	public void testCreateSectionNode_withinSection() {

		// Record creating the section
		this.recordReturn(section, section.getOfficeNode(), this.office);

		// Ensure section node valid
		SectionNode node = this.doTest(() -> this.context.createSectionNode("SECTION", this.section));
		assertNode(node, "SECTION", "Section", "[NOT INITIALISED]", this.section);
		assertSame("Incorrect office", this.office, node.getOfficeNode());
		assertSame("Incorrect parent section", this.section, node.getParentSectionNode());
		assertInitialise(node, (n) -> n.initialise("ExampleSectionSource", null, "LOCATION"));
		assertEquals("Incorrect initialised location", "ExampleSectionSource(LOCATION)", node.getLocation());

		// Ensure correct child nodes
		assertChildren(node, node.getSubSectionInput("INPUT"), node.getSubSectionOutput("OUTPUT"),
				node.getSubSectionObject("OBJECT"));
	}

	/**
	 * Ensure create {@link AdministrationNode}.
	 */
	public void testCreateAdministrationNode() {
		AdministrationNode node = this
				.doTest(() -> this.context.createAdministrationNode("ADMINISTRATION", this.office));
		assertNode(node, "ADMINISTRATION", "Administration", null, this.office);
		assertEquals("Incorrect administrator name", "ADMINISTRATION", node.getOfficeAdministrationName());
		assertInitialise(node, (n) -> n.initialise("ExampleAdministratorSource", null));
	}

	/**
	 * Ensure create {@link EscalationNode}.
	 */
	public void testCreateEscalationNode() {
		EscalationNode node = this
				.doTest(() -> this.context.createEscalationNode("java.sql.SQLException", this.office));
		assertNode(node, "java.sql.SQLException", "Escalation", null, this.office);
		assertEquals("Incorrect escalation type", "java.sql.SQLException", node.getOfficeEscalationType());
		assertInitialise(node, (n) -> n.initialise());
	}

	/**
	 * Ensure create {@link GovernanceNode}.
	 */
	public void testCreateGovernanceNode() {
		GovernanceNode node = this.doTest(() -> this.context.createGovernanceNode("GOVERNANCE", this.office));
		assertNode(node, "GOVERNANCE", "Governance", null, this.office);
		assertEquals("Incorrect governance name", "GOVERNANCE", node.getOfficeGovernanceName());
		assertInitialise(node, (n) -> n.initialise("ExampleGovernanceSource", null));
	}

	/**
	 * Ensure create {@link InputManagedObjectNode}.
	 */
	public void testCreateInputManagedObjectNode() {
		InputManagedObjectNode node = this
				.doTest(() -> this.context.createInputManagedNode("INPUT", String.class.getName(), this.officeFloor));
		assertNode(node, "INPUT", "Input Managed Object", null, this.officeFloor);
		assertEquals("Incorrect bound managed object name", "INPUT", node.getBoundManagedObjectName());
		assertEquals("Incorrect input object type", String.class.getName(), node.getInputObjectType());
		assertInitialise(node, (n) -> n.initialise());
	}

	/*
	 * Ensure create {@link ManagedObjectDependencyNode} for a {@link
	 * ManagedObject}.
	 */
	public void testCreateManagedObjectDependencyNode_forManagedObject() {
		ManagedObjectNode managedObject = this.createMock(ManagedObjectNode.class);
		this.recordReturn(managedObject, managedObject.getManagedObjectSourceNode(), this.managedObjectSource);
		ManagedObjectDependencyNode node = this
				.doTest(() -> this.context.createManagedObjectDependencyNode("DEPENDENCY", managedObject));
		assertNode(node, "DEPENDENCY", "Managed Object Dependency", null, managedObject);
		assertEquals("Incorrect managed object dependency name", "DEPENDENCY", node.getManagedObjectDependencyName());
		assertInitialise(node, (n) -> n.initialise());
	}

	/*
	 * Ensure create {@link ManagedObjectDependencyNode} for a {@link
	 * InputManagedObject}.
	 */
	public void testCreateManagedObjectDependencyNode_forInputManagedObject() {
		ManagedObjectSourceNode mos = this.createMock(ManagedObjectSourceNode.class);
		ManagedObjectDependencyNode node = this
				.doTest(() -> this.context.createManagedObjectDependencyNode("DEPENDENCY", mos));
		assertNode(node, "DEPENDENCY", "Managed Object Dependency", null, mos);
		assertEquals("Incorrect managed object dependency name", "DEPENDENCY", node.getManagedObjectDependencyName());
		assertInitialise(node, (n) -> n.initialise());
	}

	/*
	 * Ensure create {@link ManagedObjectFunctionDependencyNode} for a {@link
	 * ManagedObjectSource}.
	 */
	public void testCreateManagedObjectFunctionDependencyNode() {
		ManagedObjectSourceNode mos = this.createMock(ManagedObjectSourceNode.class);
		ManagedObjectFunctionDependencyNode node = this
				.doTest(() -> this.context.createManagedObjectFunctionDependencyNode("DEPENDENCY", mos));
		assertNode(node, "DEPENDENCY", "Managed Object Function Dependency", null, mos);
		assertEquals("Incorrect managed object dependency name", "DEPENDENCY", node.getManagedObjectDependencyName());
		assertInitialise(node, (n) -> n.initialise());
	}

	/**
	 * Ensure create {@link ManagedObjectFlowNode}.
	 */
	public void testCreateManagedObjectFlowNode() {
		ManagedObjectFlowNode node = this
				.doTest(() -> this.context.createManagedObjectFlowNode("FLOW", this.managedObjectSource));
		assertNode(node, "FLOW", "Managed Object Source Flow", null, this.managedObjectSource);
		assertEquals("Incorrect managed object flow name", "FLOW", node.getManagedObjectFlowName());
		assertInitialise(node, (n) -> n.initialise());
	}

	/**
	 * Ensure can create {@link ManagedObjectNode} within {@link SectionNode}.
	 */
	public void testCreateManagedObjectPoolNode_withinSection() {
		this.recordReturn(this.section, this.section.getOfficeNode(), this.office);
		this.recordReturn(this.office, this.office.getOfficeFloorNode(), this.officeFloor);
		ManagedObjectPoolNode node = this.doTest(() -> this.context.createManagedObjectPoolNode("POOL", this.section));

		// Ensure uninitialised that provides values
		assertNode(node, "POOL", "Managed Object Pool", null, this.section);
		assertEquals("Incorrect section name", "POOL", node.getSectionManagedObjectPoolName());
		assertEquals("Incorrect office name", "POOL", node.getOfficeManagedObjectPoolName());
		assertEquals("Incorrect OfficeFloor name", "POOL", node.getOfficeFloorManagedObjectPoolName());

		// Initialise
		assertInitialise(node, (n) -> n.initialise("ExampleManageObjectPoolSource", null));
	}

	/**
	 * Ensure can create {@link ManagedObjectNode} within {@link OfficeNode}.
	 */
	public void testCreateManagedObjectPoolNode_withinOffice() {
		this.recordReturn(this.office, this.office.getOfficeFloorNode(), this.officeFloor);
		ManagedObjectPoolNode node = this.doTest(() -> this.context.createManagedObjectPoolNode("POOL", this.office));

		// Ensure uninitialised that provides values
		assertNode(node, "POOL", "Managed Object Pool", null, this.office);
		assertEquals("Incorrect section name", "POOL", node.getSectionManagedObjectPoolName());
		assertEquals("Incorrect office name", "POOL", node.getOfficeManagedObjectPoolName());
		assertEquals("Incorrect OfficeFloor name", "POOL", node.getOfficeFloorManagedObjectPoolName());

		// Initialise
		assertInitialise(node, (n) -> n.initialise("ExampleManageObjectPoolSource", null));
	}

	/**
	 * Ensure can create {@link ManagedObjectNode} within {@link OfficeFloorNode}.
	 */
	public void testCreateManagedObjectPoolNode_withinOfficeFloor() {
		ManagedObjectPoolNode node = this
				.doTest(() -> this.context.createManagedObjectPoolNode("POOL", this.officeFloor));

		// Ensure uninitialised that provides values
		assertNode(node, "POOL", "Managed Object Pool", null, this.officeFloor);
		assertEquals("Incorrect section name", "POOL", node.getSectionManagedObjectPoolName());
		assertEquals("Incorrect office name", "POOL", node.getOfficeManagedObjectPoolName());
		assertEquals("Incorrect OfficeFloor name", "POOL", node.getOfficeFloorManagedObjectPoolName());

		// Initialise
		assertInitialise(node, (n) -> n.initialise("ExampleManageObjectPoolSource", null));
	}

	/**
	 * Ensure can create {@link ManagedObjectNode} within {@link SectionNode}.
	 */
	public void testCreateManagedObjectNode_withinSection() {
		this.recordReturn(this.section, this.section.getOfficeNode(), this.office);
		this.recordReturn(this.office, this.office.getOfficeFloorNode(), this.officeFloor);
		final CompileContext compileContext = this.context.createCompileContext();
		this.recordReturn(this.managedObjectSource, this.managedObjectSource.getSectionNode(), this.section);
		this.recordReturn(this.section, this.section.getOfficeNode(), this.office);
		this.recordReturn(this.office, this.office.getOfficeFloorNode(), this.officeFloor);
		this.recordReturn(this.section, this.section.getQualifiedName("MO"), "OFFICE.SECTION.MO");
		ManagedObjectType<?> managedObjectType = this.createMock(ManagedObjectType.class);
		this.recordReturn(this.managedObjectSource, this.managedObjectSource.loadManagedObjectType(compileContext),
				managedObjectType);
		this.recordReturn(managedObjectType, managedObjectType.getObjectType(), String.class);
		ManagedObjectNode node = this.doTest(() -> {
			ManagedObjectNode mo = this.context.createManagedObjectNode("MO", this.section);

			// Ensure uninitialised that provides values
			assertNode(mo, "MO", "Managed Object", null, this.section);
			assertEquals("Incorrect office name", "MO", mo.getOfficeManagedObjectName());
			assertEquals("Incorrect section name", "MO", mo.getSectionManagedObjectName());
			assertEquals("Incorrect administerable name", "MO", mo.getAdministerableManagedObjectName());
			assertEquals("Incorrect dependency name", "MO", mo.getDependentManagedObjectName());
			assertEquals("Incorrect governerable name", "MO", mo.getGovernerableManagedObjectName());
			assertEquals("Incorrect OfficeFloor name", "MO", mo.getOfficeFloorManagedObjectName());
			assertNull("Should not have bound name until initialised", mo.getBoundManagedObjectName());

			// Initialise, as must obtain details from managed object source
			assertInitialise(mo, (n) -> n.initialise(ManagedObjectScope.THREAD, this.managedObjectSource));
			assertEquals("Incorrect bound name", "OFFICE.SECTION.MO", mo.getBoundManagedObjectName());

			// Validate default type
			assertTypeQualifications(mo.getTypeQualifications(compileContext), null, String.class.getName());

			return mo;
		});
		assertNode(node, "MO", "Managed Object", null, this.section);

		// Validate type qualifications
		node.addTypeQualification("QUALIFIER", "TYPE");
		assertTypeQualifications(node.getTypeQualifications(compileContext), "QUALIFIER", "TYPE");

		// Validate children
		assertChildren(node, node.getSectionManagedObjectDependency("DEPENDENCY"));
	}

	/**
	 * Ensure can create {@link ManagedObjectNode} within {@link OfficeNode}.
	 */
	public void testCreateManagedObjectNode_withinOffice() {
		this.recordReturn(this.office, this.office.getOfficeFloorNode(), this.officeFloor);
		this.recordReturn(this.managedObjectSource, this.managedObjectSource.getSectionNode(), null);
		this.recordReturn(this.managedObjectSource, this.managedObjectSource.getOfficeNode(), this.office);
		this.recordReturn(this.office, this.office.getOfficeFloorNode(), this.officeFloor);
		this.recordReturn(this.office, this.office.getQualifiedName("MO"), "OFFICE.MO");
		ManagedObjectNode node = this.doTest(() -> {
			ManagedObjectNode mo = this.context.createManagedObjectNode("MO", this.office);

			// Validate correct details
			assertNode(mo, "MO", "Managed Object", null, this.office);
			assertEquals("Incorrect section name", "MO", mo.getSectionManagedObjectName());
			assertEquals("Incorrect office name", "MO", mo.getOfficeManagedObjectName());
			assertEquals("Incorrect office section name", "MO", mo.getOfficeSectionManagedObjectName());
			assertNull("Should not have bound name until initialised", mo.getBoundManagedObjectName());
			assertNull("Should not have managed object source until initialised", mo.getManagedObjectSourceNode());

			// Initialise, as gets details from managed object source
			assertInitialise(mo, (n) -> n.initialise(ManagedObjectScope.THREAD, this.managedObjectSource));
			assertEquals("Incorrect bound name", "OFFICE.MO", mo.getBoundManagedObjectName());
			assertEquals("Incorrect managed object source", this.managedObjectSource, mo.getManagedObjectSourceNode());

			return mo;
		});
		assertNode(node, "MO", "Managed Object", null, this.office);
	}

	/**
	 * Ensure can create {@link ManagedObjectNode} within {@link OfficeFloorNode}.
	 */
	public void testCreateManagedObjectNode_withinOfficeFloor() {
		this.recordReturn(this.managedObjectSource, this.managedObjectSource.getSectionNode(), null);
		this.recordReturn(this.managedObjectSource, this.managedObjectSource.getOfficeNode(), null);
		this.recordReturn(this.managedObjectSource, this.managedObjectSource.getOfficeFloorNode(), this.officeFloor);
		ManagedObjectNode node = this.doTest(() -> {
			ManagedObjectNode mo = this.context.createManagedObjectNode("MO", this.officeFloor);

			// Ensure uninitialised that provides values
			assertNode(mo, "MO", "Managed Object", null, this.officeFloor);
			assertEquals("Incorrect section name", "MO", mo.getSectionManagedObjectName());
			assertEquals("Incorrect office name", "MO", mo.getOfficeManagedObjectName());
			assertEquals("Incorrect office section name", "MO", mo.getOfficeSectionManagedObjectName());
			assertNull("Should not have bound name until initialised", mo.getBoundManagedObjectName());

			// Initialise, as gets details from managed object source
			assertInitialise(mo, (n) -> n.initialise(ManagedObjectScope.THREAD, this.managedObjectSource));
			assertEquals("Incorrect bound name", "MO", mo.getBoundManagedObjectName());
			assertEquals("Incorrect managed object source", this.managedObjectSource, mo.getManagedObjectSourceNode());

			return mo;
		});
		assertNode(node, "MO", "Managed Object", null, this.officeFloor);
	}

	/**
	 * Ensure can create {@link ManagedObjectSourceNode} within a
	 * {@link SectionNode}.
	 */
	public void testCreateManagedObjectSourceNode_withinSection() {
		this.recordReturn(this.section, this.section.getOfficeNode(), this.office);
		this.recordReturn(this.office, this.office.getOfficeFloorNode(), this.officeFloor);
		this.recordReturn(this.section, this.section.getQualifiedName("MOS"), "OFFICE.SECTION.MOS");
		ManagedObjectSourceNode node = this.doTest(() -> {
			ManagedObjectSourceNode mos = this.context.createManagedObjectSourceNode("MOS", this.section);
			assertEquals("Incorrect managed object source name", "OFFICE.SECTION.MOS", mos.getQualifiedName());
			return mos;
		});
		assertNode(node, "MOS", "Managed Object Source", null, this.section);
		assertEquals("Incorrect OfficeFloor managed object source name", "MOS",
				node.getOfficeFloorManagedObjectSourceName());
		assertSame("Incorrect containing OfficeFloor", this.officeFloor, node.getOfficeFloorNode());
		assertEquals("Incorrect office managed object source name", "MOS", node.getOfficeManagedObjectSourceName());
		assertSame("Incorrect containing office", this.office, node.getOfficeNode());
		assertEquals("Incorrect office section managed object source name", "MOS",
				node.getOfficeSectionManagedObjectSourceName());
		assertEquals("Incorrect section managed object source name", "MOS", node.getSectionManagedObjectSourceName());
		assertSame("Incorrect parent section", this.section, node.getSectionNode());
		assertInitialise(node, (n) -> n.initialise("ExampleManagedObjectSource", null));

		// Validate children
		assertChildren(node, node.getSectionManagedObjectFlow("FLOW"),
				node.getInputSectionManagedObjectDependency("DEPENDENCY"));
	}

	/**
	 * Ensure can create {@link ManagedObjectSourceNode} within a
	 * {@link OfficeNode}.
	 */
	public void testCreateManagedObjectSourceNode_withinOffice() {
		this.recordReturn(this.office, this.office.getOfficeFloorNode(), this.officeFloor);
		this.recordReturn(this.office, this.office.getQualifiedName("MOS"), "OFFICE.MOS");
		ManagedObjectSourceNode node = this.doTest(() -> {
			ManagedObjectSourceNode mos = this.context.createManagedObjectSourceNode("MOS", this.office);
			assertEquals("Incorrect managed object source name", "OFFICE.MOS", mos.getQualifiedName());
			return mos;
		});
		assertNode(node, "MOS", "Managed Object Source", null, this.office);
		assertEquals("Incorrect OfficeFloor managed object source name", "MOS",
				node.getOfficeFloorManagedObjectSourceName());
		assertSame("Incorrect containing OfficeFloor", this.officeFloor, node.getOfficeFloorNode());
		assertEquals("Incorrect office managed object source name", "MOS", node.getOfficeManagedObjectSourceName());
		assertSame("Incorrect containing office", this.office, node.getOfficeNode());
		assertNull("Should not have office section name, as not contained in section",
				node.getOfficeSectionManagedObjectSourceName());
		assertNull("Should not have section name, as not contained in section",
				node.getSectionManagedObjectSourceName());
		assertNull("Should not have parent section, as not contained in section", node.getSectionNode());
		assertInitialise(node, (n) -> n.initialise("ExampleManagedObjectSource", null));

		// Validate children
		assertChildren(node, node.getSectionManagedObjectFlow("FLOW"), node.getOfficeFloorManagedObjectTeam("TEAM"),
				node.getInputSectionManagedObjectDependency("DEPENDENCY"));
	}

	/**
	 * Ensure can create {@link ManagedObjectSourceNode} from {@link SupplierNode}.
	 */
	public void testCreateManagedObjectSourceNode_fromSupplier_withinOfficeFloor() {
		SuppliedManagedObjectSourceNode suppliedManagedObject = this.createMock(SuppliedManagedObjectSourceNode.class);
		SupplierNode supplier = this.createMock(SupplierNode.class);
		this.recordReturn(suppliedManagedObject, suppliedManagedObject.getSupplierNode(), supplier);
		this.recordReturn(supplier, supplier.getOfficeNode(), null);
		this.recordReturn(supplier, supplier.getOfficeFloorNode(), this.officeFloor);
		this.recordReturn(this.officeFloor, this.officeFloor.getQualifiedName("MOS"), "MOS");
		ManagedObjectSourceNode node = this.doTest(() -> {
			ManagedObjectSourceNode mos = this.context.createManagedObjectSourceNode("MOS", suppliedManagedObject);
			assertEquals("Incorrect managed object source name", "MOS", mos.getQualifiedName());
			return mos;
		});
		assertNode(node, "MOS", "Managed Object Source", null, this.officeFloor);
		assertEquals("Incorrect OfficeFloor managed object source name", "MOS",
				node.getOfficeFloorManagedObjectSourceName());
		assertSame("Incorrect containing OfficeFloor", this.officeFloor, node.getOfficeFloorNode());
		assertNull("Should not have office name, as not contained in office", node.getOfficeManagedObjectSourceName());
		assertNull("Should not have parent office, as not contained in office", node.getOfficeNode());
		assertNull("Should not have office section name, as not contained in section",
				node.getOfficeSectionManagedObjectSourceName());
		assertNull("Should not have section name, as not contained in section",
				node.getSectionManagedObjectSourceName());
		assertNull("Should not have parent section, as not contained in section", node.getSectionNode());
		assertInitialise(node, (n) -> n.initialise("ExampleManagedObjectSource", null));
	}

	/**
	 * Ensure can create {@link ManagedObjectSourceNode} from {@link SupplierNode}.
	 */
	public void testCreateManagedObjectSourceNode_fromSupplier_withinOffice() {
		SuppliedManagedObjectSourceNode suppliedManagedObject = this.createMock(SuppliedManagedObjectSourceNode.class);
		SupplierNode supplier = this.createMock(SupplierNode.class);
		this.recordReturn(suppliedManagedObject, suppliedManagedObject.getSupplierNode(), supplier);
		this.recordReturn(supplier, supplier.getOfficeNode(), this.office);
		this.recordReturn(supplier, supplier.getOfficeFloorNode(), this.officeFloor);
		this.recordReturn(this.office, this.office.getQualifiedName("MOS"), "OFFICE.MOS");
		ManagedObjectSourceNode node = this.doTest(() -> {
			ManagedObjectSourceNode mos = this.context.createManagedObjectSourceNode("MOS", suppliedManagedObject);
			assertEquals("Incorrect qualified name", "OFFICE.MOS", mos.getQualifiedName());
			return mos;
		});
		assertNode(node, "MOS", "Managed Object Source", null, this.office);
		assertEquals("Incorrect OfficeFloor managed object source name", "MOS",
				node.getOfficeFloorManagedObjectSourceName());
		assertSame("Incorrect containing OfficeFloor", this.officeFloor, node.getOfficeFloorNode());
		assertEquals("Incorrect Office managed object source name", "MOS", node.getOfficeManagedObjectSourceName());
		assertSame("Incorrect containing Office", this.office, node.getOfficeNode());
		assertNull("Should not have office section name, as not contained in section",
				node.getOfficeSectionManagedObjectSourceName());
		assertNull("Should not have section name, as not contained in section",
				node.getSectionManagedObjectSourceName());
		assertNull("Should not have parent section, as not contained in section", node.getSectionNode());
		assertInitialise(node, (n) -> n.initialise("ExampleManagedObjectSource", null));
	}

	/**
	 * Ensure can create {@link ManagedObjectSourceNode} within a
	 * {@link OfficeFloorNode}.
	 */
	public void testCreateManagedObjectSourceNode_withinOfficeFloor() {
		this.recordReturn(this.officeFloor, this.officeFloor.getQualifiedName("MOS"), "MOS");
		ManagedObjectSourceNode node = this.doTest(() -> {
			ManagedObjectSourceNode mos = this.context.createManagedObjectSourceNode("MOS", this.officeFloor);
			assertEquals("Incorrect managed object source name", "MOS", mos.getQualifiedName());
			return mos;
		});
		assertNode(node, "MOS", "Managed Object Source", null, this.officeFloor);
		assertEquals("Incorrect OfficeFloor managed object source name", "MOS",
				node.getOfficeFloorManagedObjectSourceName());
		assertSame("Incorrect containing OfficeFloor", this.officeFloor, node.getOfficeFloorNode());
		assertNull("Should not have office name, as not contained in office", node.getOfficeManagedObjectSourceName());
		assertNull("Should not have parent office, as not contained in office", node.getOfficeNode());
		assertNull("Should not have office section name, as not contained in section",
				node.getOfficeSectionManagedObjectSourceName());
		assertNull("Should not have section name, as not contained in section",
				node.getSectionManagedObjectSourceName());
		assertNull("Should not have parent section, as not contained in section", node.getSectionNode());
		assertInitialise(node, (n) -> n.initialise("ExampleManagedObjectSource", null));

		// Validate children
		assertChildren(node, node.getSectionManagedObjectFlow("FLOW"), node.getOfficeFloorManagedObjectTeam("TEAM"),
				node.getInputSectionManagedObjectDependency("DEPENDENCY"));
	}

	/**
	 * Ensure can create {@link ManagedObjectTeamNode}.
	 */
	public void testCreateManagedObjectTeamNode() {
		ManagedObjectTeamNode node = this
				.doTest(() -> this.context.createManagedObjectTeamNode("TEAM", this.managedObjectSource));
		assertNode(node, "TEAM", "Managed Object Source Team", null, this.managedObjectSource);
		assertEquals("Incorrect managed object team name", "TEAM", node.getManagedObjectTeamName());
		assertInitialise(node, (n) -> n.initialise());
	}

	/**
	 * Ensure can create {@link ManagedObjectExecutionStrategyNode}.
	 */
	public void testCreateManagedObjectExecutionStrategyNode() {
		ManagedObjectExecutionStrategyNode node = this.doTest(() -> this.context
				.createManagedObjectExecutionStrategyNode("EXECUTION_STRATEGY", this.managedObjectSource));
		assertNode(node, "EXECUTION_STRATEGY", "Managed Object Source Execution Strategy", null,
				this.managedObjectSource);
		assertEquals("Incorrect managed object execution strategy name", "EXECUTION_STRATEGY",
				node.getManagedObjectExecutionStrategyName());
		assertInitialise(node, (n) -> n.initialise());
	}

	/**
	 * Ensure can create {@link ManagingOfficeNode}.
	 */
	public void testCreateManagingOfficeNode() {
		ManagingOfficeNode node = this.doTest(() -> {
			ManagingOfficeNode office = this.context.createManagingOfficeNode(this.managedObjectSource);
			assertNode(office, "MANAGING_OFFICE", "Managing Office", null, this.managedObjectSource);
			return office;
		});
		assertInitialise(node, (n) -> n.initialise());
	}

	/**
	 * Ensure can create {@link OfficeFloorNode}.
	 */
	public void testCreateOfficeFloorNode() {
		this.recordReturn(this.managedObjectSource, this.managedObjectSource.getSectionNode(), this.section);
		this.recordReturn(this.section, this.section.getOfficeNode(), this.office);
		this.recordReturn(this.office, this.office.getOfficeFloorNode(), this.officeFloor);
		OfficeFloorNode node = this.doTest(() -> {
			OfficeFloorNode officeFloor = this.context.createOfficeFloorNode("ExampleOfficeFloorSource", null,
					"LOCATION");
			assertNode(officeFloor, OfficeFloorNode.OFFICE_FLOOR_NAME, "OfficeFloor",
					"ExampleOfficeFloorSource(LOCATION)", null);
			assertChildren(officeFloor, officeFloor.addTeam("TEAM", "net.example.ExampleTeamSource"),
					officeFloor.addManagedObjectSource("MOS", "net.example.ExampleManagedObjectSource"),
					officeFloor.addInputManagedObject("INPUT", "java.lang.String"),
					officeFloor.addSupplier("SUPPLIER", "net.example.ExampleSupplierSource"),
					officeFloor.getManagedObjectNode("MO_ONE"),
					officeFloor.addManagedObjectNode("MO_TWO", ManagedObjectScope.THREAD, this.managedObjectSource),
					officeFloor.addDeployedOffice("OFFICE", "net.example.ExampleOfficeSource", "LOCATION"));
			return officeFloor;
		});
		assertInitialise(node, (n) -> n.initialise());
	}

	/**
	 * Ensure can create {@link OfficeInputNode}.
	 */
	public void testCreateOfficeInputNode() {
		OfficeInputNode node = this.doTest(() -> this.context.createOfficeInputNode("INPUT", this.office));
		assertNode(node, "INPUT", "Office Input", null, this.office);
		assertEquals("Incorrect office input name", "INPUT", node.getOfficeInputName());
		assertInitialise(node, (n) -> n.initialise("java.lang.String"));
	}

	/**
	 * Ensure can create {@link OfficeNode}.
	 */
	public void testCreateOfficeNode() {
		this.recordReturn(this.managedObjectSource, this.managedObjectSource.getSectionNode(), this.section);
		this.recordReturn(this.section, this.section.getOfficeNode(), this.office);
		this.recordReturn(this.office, this.office.getOfficeFloorNode(), this.officeFloor);
		this.recordReturn(this.officeFloor, this.officeFloor.getQualifiedName("OFFICE"), "OFFICE");
		OfficeNode node = this.doTest(() -> {
			OfficeNode office = this.context.createOfficeNode("OFFICE", this.officeFloor);
			assertNode(office, "OFFICE", "Office", "[NOT INITIALISED]", this.officeFloor);
			assertChildren(office, office.addOfficeInput("INPUT", null), office.addOfficeOutput("OUTPUT", null),
					office.getDeployedOfficeObject("OBJECT"),
					office.addOfficeSection("SECTION", "net.example.ExampleSectionSource", "LOCATION"),
					office.getDeployedOfficeTeam("TEAM_ONE"), office.addOfficeTeam("TEAM_TWO"),
					office.addSupplier("SUPPLIER", "net.example.ExampleSupplierSource"),
					office.getManagedObjectNode("MO_ONE"),
					office.addManagedObjectNode("MO_TWO", ManagedObjectScope.THREAD, this.managedObjectSource),
					office.addOfficeManagedObjectSource("MOS", "net.example.ExampleManagedObjectSource"),
					office.addOfficeGovernance("GOVERNANCE", "net.example.ExampleGovernanceSource"),
					office.addOfficeAdministration("ADMINISTRATION", "net.example.ExampleAdministrationSource"),
					office.addOfficeEscalation("net.example.ExampleEscalation"), office.addOfficeStart("START"));
			assertEquals("Incorrect qualified name", "OFFICE.name", office.getQualifiedName("name"));
			return office;
		});
		assertEquals("Incorrect office name", "OFFICE", node.getDeployedOfficeName());
		assertInitialise(node, (n) -> n.initialise("ExampleOfficeSource", null, "LOCATION"));
		assertEquals("Should be initialised with location", "ExampleOfficeSource(LOCATION)", node.getLocation());
	}

	/**
	 * Ensure can create {@link OfficeObjectNode}.
	 */
	public void testCreateOfficeObjectNode() {
		OfficeObjectNode node = this.doTest(() -> this.context.createOfficeObjectNode("OBJECT", this.office));
		assertNode(node, "OBJECT", "Office Object", null, this.office);
		assertEquals("Incorrect administerable object name", "OBJECT", node.getAdministerableManagedObjectName());
		assertEquals("Incorrect dependent object name", "OBJECT", node.getDependentManagedObjectName());
		assertEquals("Incorrect governerable object name", "OBJECT", node.getGovernerableManagedObjectName());
		assertEquals("Incorrect office object name", "OBJECT", node.getOfficeObjectName());
		assertInitialise(node, (n) -> n.initialise("java.lang.Integer"));
	}

	/**
	 * Ensure can create {@link OfficeOutputNode}.
	 */
	public void testCreateOfficeOutputNode() {
		OfficeOutputNode node = this.doTest(() -> this.context.createOfficeOutputNode("OUTPUT", this.office));
		assertNode(node, "OUTPUT", "Office Output", null, this.office);
		assertEquals("Incorrect output name", "OUTPUT", node.getOfficeOutputName());
		assertInitialise(node, (n) -> n.initialise("java.lang.Character"));
	}

	/**
	 * Ensure can create {@link OfficeStartNode}.
	 */
	public void testCreateOfficeStartNode() {
		OfficeStartNode node = this.doTest(() -> this.context.createOfficeStartNode("START", this.office));
		assertNode(node, "START", "Office Start", null, this.office);
		assertEquals("Incorrect start name", "START", node.getOfficeStartName());
		assertInitialise(node, (n) -> n.initialise());
	}

	/**
	 * Ensure can create {@link OfficeTeamNode}.
	 */
	public void testCreateOfficeTeamNode() {
		OfficeTeamNode node = this.doTest(() -> this.context.createOfficeTeamNode("TEAM", this.office));
		assertNode(node, "TEAM", "Office Team", null, this.office);
		assertInitialise(node, (n) -> n.initialise());
		assertTypeQualifications(node.getTypeQualifications());
		node.addTypeQualification("QUALIFIED", "TYPE");
		assertTypeQualifications(node.getTypeQualifications(), "QUALIFIED", "TYPE");
	}

	/**
	 * Ensure can create {@link SectionInputNode}.
	 */
	public void testCreateSectionInputNode() {
		this.recordReturn(this.section, this.section.getOfficeNode(), this.office);
		SectionInputNode node = this.doTest(() -> {
			SectionInputNode input = this.context.createSectionInputNode("INPUT", this.section);
			assertEquals("Incorrect office", this.office, input.getDeployedOffice());
			return input;
		});
		assertNode(node, "INPUT", "Section Input", null, this.section);
		assertEquals("Incorrect section", this.section, node.getOfficeSection());
		assertEquals("Incorrect deployed input name", "INPUT", node.getDeployedOfficeInputName());
		assertEquals("Incorrect section input name", "INPUT", node.getOfficeSectionInputName());
		assertInitialise(node, (n) -> n.initialise("java.lang.Short"));
	}

	/**
	 * Ensure can create {@link SectionObjectNode}.
	 */
	public void testCreateSectionObjectNode() {
		SectionObjectNode node = this.doTest(() -> this.context.createSectionObjectNode("OBJECT", this.section));
		assertNode(node, "OBJECT", "Section Object", null, this.section);
		assertEquals("Incorrect section", this.section, node.getOfficeSection());
		assertEquals("Incorrect office section object name", "OBJECT", node.getOfficeSectionObjectName());
		assertEquals("Incorrect section object name", "OBJECT", node.getSectionObjectName());
		assertEquals("Incorrect sub section object name", "OBJECT", node.getSubSectionObjectName());
		assertInitialise(node, (n) -> n.initialise("java.lang.Byte"));
	}

	/**
	 * Ensure can create {@link SectionOutputNode}.
	 */
	public void testCreateSectionOutputNode() {
		SectionOutputNode node = this.doTest(() -> this.context.createSectionOutputNode("OUTPUT", this.section));
		assertNode(node, "OUTPUT", "Section Output", null, this.section);
		assertEquals("Incorrect section", this.section, node.getOfficeSection());
		assertEquals("Incorrect office section output name", "OUTPUT", node.getOfficeSectionOutputName());
		assertEquals("Incorrect section output name", "OUTPUT", node.getSectionOutputName());
		assertEquals("Incorrect sub section output name", "OUTPUT", node.getSubSectionOutputName());
		assertInitialise(node, (n) -> n.initialise("java.lang.Float", true));
	}

	/**
	 * Ensure can create {@link SupplierNode} within {@link OfficeFloor}.
	 */
	public void testCreateSupplierNode_withinOfficeFloor() {
		this.recordReturn(this.officeFloor, this.officeFloor.getQualifiedName("SUPPLIER"), "SUPPLIER");
		ParameterCapture<SuppliedManagedObjectSourceNode> suppliedManagedObjectNode = new ParameterCapture<>();
		this.recordReturn(this.officeFloor,
				this.officeFloor.addManagedObjectSource(this.param("MOS"), suppliedManagedObjectNode.capture()),
				this.managedObjectSource);
		SupplierNode node = this.doTest(() -> {
			SupplierNode supplier = this.context.createSupplierNode("SUPPLIER", this.officeFloor);
			assertNode(supplier, "SUPPLIER", "Supplier", null, this.officeFloor);
			assertEquals("Incorrect qualified name", "SUPPLIER.QUALIFIED", supplier.getQualifiedName("QUALIFIED"));
			supplier.getOfficeFloorManagedObjectSource("MOS", null, "TYPE");
			assertChildren(supplier, supplier.getOfficeFloorSupplierThreadLocal("QUALIFIER", Object.class.getName()),
					suppliedManagedObjectNode.getValue());
			return supplier;
		});

		assertEquals("Incorrect supplier name", "SUPPLIER", node.getOfficeFloorSupplierName());
		assertInitialise(node, (n) -> n.initialise("ExampleSupplierSource", null));
	}

	/**
	 * Ensure can create {@link SupplierNode} within {@link Office}.
	 */
	public void testCreateSupplierNode_withinOffice() {
		this.recordReturn(this.office, this.office.getOfficeFloorNode(), this.officeFloor);
		this.recordReturn(this.office, this.office.getQualifiedName("SUPPLIER"), "OFFICE.SUPPLIER");
		ParameterCapture<SuppliedManagedObjectSourceNode> suppliedManagedObjectNode = new ParameterCapture<>();
		this.recordReturn(this.office,
				this.office.addManagedObjectSource(this.param("MOS"), suppliedManagedObjectNode.capture()),
				this.managedObjectSource);
		SupplierNode node = this.doTest(() -> {
			SupplierNode supplier = this.context.createSupplierNode("SUPPLIER", this.office);
			assertNode(supplier, "SUPPLIER", "Supplier", null, this.office);
			assertEquals("Incorrect qualified name", "OFFICE.SUPPLIER.QUALIFIED",
					supplier.getQualifiedName("QUALIFIED"));
			supplier.getOfficeManagedObjectSource("MOS", null, "TYPE");
			assertChildren(supplier, suppliedManagedObjectNode.getValue());
			return supplier;
		});

		assertEquals("Incorrect supplier name", "SUPPLIER", node.getOfficeSupplierName());
		assertInitialise(node, (n) -> n.initialise("ExampleSupplierSource", null));
	}

	/**
	 * Ensure can create {@link SupplierThreadLocalNode} within {@link OfficeFloor}.
	 */
	public void testCreateSupplierThreadLocalNode_withinOfficeFloor() {
		SupplierNode supplier = this.createMock(SupplierNode.class);
		final String expectedName = "QUALIFIER-java.lang.Object";
		this.recordReturn(supplier, supplier.getQualifiedName(Node.escape(expectedName)),
				Node.qualify("SUPPLIER", Node.escape(expectedName)));
		SupplierThreadLocalNode node = this.doTest(() -> {
			SupplierThreadLocalNode threadLocal = this.context.createSupplierThreadLocalNode("QUALIFIER",
					Object.class.getName(), supplier);
			assertNode(threadLocal, expectedName, "Supplier Thread Local", null, supplier);
			assertEquals("Incorrect qualified name", "SUPPLIER.QUALIFIER-java_lang_Object",
					threadLocal.getQualifiedName());
			return threadLocal;
		});
		assertSame("Incorrect supplier", supplier, node.getSupplierNode());
		assertEquals("Incorrect OfficeFloor name", expectedName, node.getOfficeFloorSupplierThreadLocalName());
		assertEquals("Incorrect qualfiifer", "QUALIFIER", node.getQualifier());
		assertEquals("Incorrect type", "java.lang.Object", node.getType());
		assertInitialise(node, (n) -> n.initialise(this.createMock(OptionalThreadLocalReceiver.class)));
	}

	/**
	 * Ensure can create {@link SupplierThreadLocalNode} within {@link Office}.
	 */
	public void testCreateSupplierThreadLocalNode_withinOffice() {
		SupplierNode supplier = this.createMock(SupplierNode.class);
		final String expectedName = "QUALIFIER-java.lang.Object";
		this.recordReturn(supplier, supplier.getQualifiedName(Node.escape(expectedName)),
				"OFFICE." + Node.escape(expectedName));
		SupplierThreadLocalNode node = this.doTest(() -> {
			SupplierThreadLocalNode threadLocal = this.context.createSupplierThreadLocalNode("QUALIFIER",
					Object.class.getName(), supplier);
			assertNode(threadLocal, expectedName, "Supplier Thread Local", null, supplier);
			assertEquals("Incorrect qualified name", "OFFICE.QUALIFIER-java_lang_Object",
					threadLocal.getQualifiedName());
			return threadLocal;
		});
		assertSame("Incorrect supplier", supplier, node.getSupplierNode());
		assertEquals("Incorrect Office name", expectedName, node.getOfficeSupplierThreadLocalName());
		assertEquals("Incorrect qualfiifer", "QUALIFIER", node.getQualifier());
		assertEquals("Incorrect type", "java.lang.Object", node.getType());
		assertInitialise(node, (n) -> n.initialise(this.createMock(OptionalThreadLocalReceiver.class)));
	}

	/**
	 * Ensure can create {@link SuppliedManagedObjectSourceNode} within
	 * {@link OfficeFloor}.
	 */
	public void testCreateSuppliedManagedObjectNode_withinOfficeFloor() {
		SupplierNode supplier = this.createMock(SupplierNode.class);
		final String expectedName = "QUALIFIER-" + Object.class.getName();
		SuppliedManagedObjectSourceNode node = this.doTest(() -> {
			SuppliedManagedObjectSourceNode mos = this.context.createSuppliedManagedObjectSourceNode("QUALIFIER",
					Object.class.getName(), supplier);
			assertNode(mos, expectedName, "Supplied Managed Object Source", null, supplier);
			return mos;
		});
		assertSame("Incorrect supplier", supplier, node.getSupplierNode());
		assertInitialise(node, (n) -> n.initialise());
	}

	/**
	 * Ensure can create {@link SuppliedManagedObjectSourceNode} within
	 * {@link Office}.
	 */
	public void testCreateSuppliedManagedObjectNode_withinOffice() {
		SupplierNode supplier = this.createMock(SupplierNode.class);
		final String expectedName = "QUALIFIER-" + Object.class.getName();
		SuppliedManagedObjectSourceNode node = this.doTest(() -> {
			SuppliedManagedObjectSourceNode mos = this.context.createSuppliedManagedObjectSourceNode("QUALIFIER",
					Object.class.getName(), supplier);
			assertNode(mos, expectedName, "Supplied Managed Object Source", null, supplier);
			return mos;
		});
		assertSame("Incorrect supplier", supplier, node.getSupplierNode());
		assertInitialise(node, (n) -> n.initialise());
	}

	/**
	 * Ensure can create {@link FunctionFlowNode}.
	 */
	public void testCreateFunctionFlowNode() {
		FunctionFlowNode node = this.doTest(() -> this.context.createFunctionFlowNode("FLOW", true, this.function));
		assertNode(node, "FLOW", "Function Flow", null, this.function);
		assertFalse("Escalation does not spawn thread state", node.isSpawnThreadState());
		assertEquals("Incorrect function flow name", "FLOW", node.getFunctionFlowName());
		assertInitialise(node, (n) -> n.initialise());
	}

	/**
	 * Ensure can create {@link ManagedFunctionNode}.
	 */
	public void testCreateManagedFunctionNode() {
		FunctionNamespaceNode namespace = this.createMock(FunctionNamespaceNode.class);
		ManagedFunctionNode node = this.doTest(() -> this.context.createFunctionNode("FUNCTION", this.section));
		assertNode(node, "FUNCTION", "Managed Function", null, this.section);
		assertEquals("Incorrct office function name", "FUNCTION", node.getOfficeFunctionName());
		assertEquals("Incorrect section function name", "FUNCTION", node.getSectionFunctionName());
		assertNull("Should not have namespace", node.getFunctionNamespaceNode());
		assertInitialise(node, (n) -> n.initialise("TYPE", namespace));
		assertSame("Incorrect namespace", namespace, node.getFunctionNamespaceNode());
		assertChildren(node, node.getFunctionFlow("FLOW"), node.getFunctionEscalation("ESCALATION"),
				node.getFunctionObject("OBJECT"));
	}

	/**
	 * Ensure can create {@link FunctionObjectNode}.
	 */
	public void testCreateFunctionObjectNode() {
		FunctionObjectNode node = this.doTest(() -> this.context.createFunctionObjectNode("OBJECT", this.function));
		assertNode(node, "OBJECT", "Function Object", null, this.function);
		assertEquals("Incorrect function object name", "OBJECT", node.getFunctionObjectName());
		assertInitialise(node, (n) -> n.initialise());
	}

	/**
	 * Ensure can create {@link ResponsibleTeamNode}.
	 */
	public void testCreateResponsibleTeamNode() {
		ResponsibleTeamNode node = this.doTest(() -> this.context.createResponsibleTeamNode("TEAM", this.function));
		assertNode(node, "TEAM", "Responsible Team", null, this.function);
		assertInitialise(node, (n) -> n.initialise());
	}

	/**
	 * Ensure can create {@link TeamNode}.
	 */
	public void testCreateTeamNode() {
		TeamNode node = this.context.createTeamNode("TEAM", this.officeFloor);
		assertNode(node, "TEAM", "Team", null, this.officeFloor);
		assertEquals("TEAM", node.getOfficeFloorTeamName());
		assertInitialise(node, (n) -> n.initialise("ExampleTeamSource", null));
	}

	/**
	 * Ensure can create {@link ExecutiveNode}.
	 */
	public void testCreateExecutiveNode() {
		ExecutiveNode node = this.context.createExecutiveNode(this.officeFloor);
		assertNode(node, "Executive", "Executive", null, this.officeFloor);
		assertEquals("Executive", node.getOfficeFloorExecutiveName());
		assertInitialise(node, (n) -> n.initialise("ExampleExecutiveSource", null));

		// Validate children
		assertChildren(node, node.getOfficeFloorExecutionStrategy("EXECUTION_STRATEGY"),
				node.getOfficeFloorTeamOversight("TEAM_OVERSIGHT"));
	}

	/**
	 * Ensure can create {@link ExecutionStrategyNode}.
	 */
	public void testCreateExecutionStrategyNode() {
		ExecutionStrategyNode node = this.context.createExecutionStrategyNode("EXECUTION_STRATEGY", this.executive);
		assertNode(node, "EXECUTION_STRATEGY", "Execution Strategy", null, this.executive);
		assertEquals("EXECUTION_STRATEGY", node.getOfficeFloorExecutionStratgyName());
		assertInitialise(node, (n) -> n.initialise());
	}

	/**
	 * Ensure can create {@link TeamOversightNode}.
	 */
	public void testCreateTeamOversightNode() {
		TeamOversightNode node = this.context.createTeamOversightNode("TEAM_OVERSIGHT", this.executive);
		assertNode(node, "TEAM_OVERSIGHT", "Team Oversight", null, this.executive);
		assertEquals("TEAM_OVERSIGHT", node.getOfficeFloorTeamOversightName());
		assertInitialise(node, (n) -> n.initialise());
	}

	/**
	 * Ensure can create {@link FunctionNamespaceNode}.
	 */
	public void testCreateFunctionNamespaceNode() {
		FunctionNamespaceNode node = this.doTest(() -> {
			return this.context.createFunctionNamespaceNode("NAMESPACE", this.section);
		});
		assertNode(node, "NAMESPACE", "Function Namespace", null, this.section);
		assertSame("Incorrect section", this.section, node.getSectionNode());
		assertEquals("Incorrect section namespace name", "NAMESPACE", node.getSectionFunctionNamespaceName());
		assertInitialise(node, (n) -> n.initialise("ExampleManagedFunctionSource", null));
	}

	/**
	 * Asserts the {@link Node} is correct.
	 * 
	 * @param node     {@link Node} to validate.
	 * @param name     Expected name.
	 * @param type     Expected type.
	 * @param location Expected location.
	 * @param parent   Parent {@link Node}.
	 */
	private static void assertNode(Node node, String name, String type, String location, Node parent) {
		assertEquals("Incorrect node name", name, node.getNodeName());
		assertEquals("Incorrect node type", type, node.getNodeType());
		assertEquals("Incorrect node location", location, node.getLocation());
		assertSame("Incorrect node parent", parent, node.getParentNode());
		assertChildren(node); // ensure no children
	}

	/**
	 * Asserts the {@link TypeQualification} for the {@link ManagedObjectNode}.
	 * 
	 * @param typeQualifications {@link TypeQualification} instances.
	 * @param qualifierTypePairs Pairings of expected qualifier and types.
	 */
	private static void assertTypeQualifications(TypeQualification[] typeQualifications, String... qualifierTypePairs) {
		assertEquals("Incorrect number of type qualifications", (qualifierTypePairs.length / 2),
				typeQualifications.length);
		for (int i = 0; i < qualifierTypePairs.length; i += 2) {
			int index = i / 2;
			TypeQualification qualification = typeQualifications[index];
			assertEquals("Incorrect qualification " + index, qualifierTypePairs[i], qualification.getQualifier());
			assertEquals("Incorrect type " + index, qualifierTypePairs[i + 1], qualification.getType());
		}
	}

	/**
	 * Asserts the correct children {@link Node} instances.
	 * 
	 * @param node     {@link Node} to check its children.
	 * @param children Child {@link Node} instances.
	 */
	private static void assertChildren(Node node, Object... children) {
		assertArrayEquals("Incorrect children", node.getChildNodes(), children);
	}

	/**
	 * Asserts the {@link Node} is initialised.
	 * 
	 * @param node        {@link Node} to validate initialising.
	 * @param initialiser {@link Consumer} to initialise the {@link Node}.
	 */
	private static <N extends Node> void assertInitialise(N node, Consumer<N> initialiser) {
		assertFalse("Should not be initialised", node.isInitialised());
		initialiser.accept(node);
		assertTrue("Should now be initialised", node.isInitialised());
	}

}
