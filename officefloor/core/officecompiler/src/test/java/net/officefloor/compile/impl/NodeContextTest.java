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
package net.officefloor.compile.impl;

import java.util.function.Consumer;

import net.officefloor.autowire.AutoWire;
import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.compile.internal.structure.AdministratorNode;
import net.officefloor.compile.internal.structure.DutyNode;
import net.officefloor.compile.internal.structure.EscalationNode;
import net.officefloor.compile.internal.structure.GovernanceNode;
import net.officefloor.compile.internal.structure.InputManagedObjectNode;
import net.officefloor.compile.internal.structure.ManagedObjectDependencyNode;
import net.officefloor.compile.internal.structure.ManagedObjectFlowNode;
import net.officefloor.compile.internal.structure.ManagedObjectNode;
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
import net.officefloor.compile.internal.structure.SectionInputNode;
import net.officefloor.compile.internal.structure.SectionNode;
import net.officefloor.compile.internal.structure.SectionObjectNode;
import net.officefloor.compile.internal.structure.SectionOutputNode;
import net.officefloor.compile.internal.structure.SuppliedManagedObjectNode;
import net.officefloor.compile.internal.structure.SupplierNode;
import net.officefloor.compile.internal.structure.TaskFlowNode;
import net.officefloor.compile.internal.structure.TaskNode;
import net.officefloor.compile.internal.structure.TaskObjectNode;
import net.officefloor.compile.internal.structure.TaskTeamNode;
import net.officefloor.compile.internal.structure.TeamNode;
import net.officefloor.compile.internal.structure.WorkNode;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.internal.structure.FlowInstigationStrategyEnum;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.frame.test.OfficeFrameTestCase;

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
	 * Mock {@link TaskNode}.
	 */
	private final TaskNode task = this.createMock(TaskNode.class);

	/**
	 * Mock {@link ManagedObjectSourceNode}.
	 */
	private final ManagedObjectSourceNode managedObjectSource = this.createMock(ManagedObjectSourceNode.class);

	/**
	 * Ensure create {@link SectionNode} within {@link OfficeNode}.
	 */
	public void testCreateSectionNode_withinOffice() {

		SectionNode node = this.doTest(() -> this.context.createSectionNode("SECTION", this.office));

		// Ensure section node valid
		assertNode(node, "SECTION", "Section", "[NOT INITIALISED]", this.office);
		assertEquals("Incorrect section name", "SECTION", node.getOfficeSectionName());
		assertSame("Incorrect office node", this.office, node.getOfficeNode());
		assertNull("Should not have parent section", node.getParentSectionNode());
		assertEquals("Incorrect qualified name", "SECTION.QUALIFIED", node.getSectionQualifiedName("QUALIFIED"));

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
	}

	/**
	 * Ensure create {@link AdministratorNode}.
	 */
	public void testCreateAdministratorNode() {
		AdministratorNode node = this.doTest(() -> this.context.createAdministratorNode("ADMINISTRATOR", this.office));
		assertNode(node, "ADMINISTRATOR", "Administrator", null, this.office);
		assertEquals("Incorrect administrator name", "ADMINISTRATOR", node.getOfficeAdministratorName());
		assertInitialise(node, (n) -> n.initialise("ExampleAdministratorSource", null));
	}

	/**
	 * Ensure can create {@link DutyNode}.
	 */
	public void testCreateDutyNode() {
		AdministratorNode admin = this.createMock(AdministratorNode.class);
		DutyNode node = this.doTest(() -> this.context.createDutyNode("DUTY", admin));
		assertNode(node, "DUTY", "Duty", null, admin);
		assertInitialise(node, (n) -> n.initialise());
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
		InputManagedObjectNode node = this.doTest(() -> this.context.createInputManagedNode("INPUT", this.officeFloor));
		assertNode(node, "INPUT", "Input Managed Object", null, this.officeFloor);
		assertEquals("Incorrect bound managed object name", "INPUT", node.getBoundManagedObjectName());
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
	public void testCreateManagedObjectNode_withinSection() {
		this.recordReturn(this.managedObjectSource, this.managedObjectSource.getSectionNode(), this.section);
		this.recordReturn(this.section, this.section.getOfficeNode(), this.office);
		this.recordReturn(this.office, this.office.getOfficeFloorNode(), this.officeFloor);
		this.recordReturn(this.office, this.office.getDeployedOfficeName(), "OFFICE");
		this.recordReturn(this.section, this.section.getSectionQualifiedName("MO"), "SECTION.MO");
		ManagedObjectNode node = this.doTest(() -> {
			ManagedObjectNode mo = this.context.createManagedObjectNode("MO");

			// Ensure uninitialised that provides values
			assertNode(mo, "MO", "Managed Object", null, null);
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
			return mo;
		});
		assertNode(node, "MO", "Managed Object", null, this.managedObjectSource);
	}

	/**
	 * Ensure can create {@link ManagedObjectNode} within {@link OfficeNode}.
	 */
	public void testCreateManagedObjectNode_withinOffice() {
		this.recordReturn(this.managedObjectSource, this.managedObjectSource.getSectionNode(), null);
		this.recordReturn(this.managedObjectSource, this.managedObjectSource.getOfficeNode(), this.office);
		this.recordReturn(this.office, this.office.getOfficeFloorNode(), this.officeFloor);
		this.recordReturn(this.office, this.office.getDeployedOfficeName(), "OFFICE");
		ManagedObjectNode node = this.doTest(() -> {
			ManagedObjectNode mo = this.context.createManagedObjectNode("MO");

			// Validate correct details
			assertNode(mo, "MO", "Managed Object", null, null);
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
		assertNode(node, "MO", "Managed Object", null, this.managedObjectSource);
	}

	/**
	 * Ensure can create {@link ManagedObjectNode} within
	 * {@link OfficeFloorNode}.
	 */
	public void testCreateManagedObjectNode_withinOfficeFloor() {
		this.recordReturn(this.managedObjectSource, this.managedObjectSource.getSectionNode(), null);
		this.recordReturn(this.managedObjectSource, this.managedObjectSource.getOfficeNode(), null);
		this.recordReturn(this.managedObjectSource, this.managedObjectSource.getOfficeFloorNode(), this.officeFloor);
		ManagedObjectNode node = this.doTest(() -> {
			ManagedObjectNode mo = this.context.createManagedObjectNode("MO");

			// Ensure uninitialised that provides values
			assertNode(mo, "MO", "Managed Object", null, null);
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
		assertNode(node, "MO", "Managed Object", null, this.managedObjectSource);
	}

	/**
	 * Ensure can create {@link ManagedObjectSourceNode} within a
	 * {@link SectionNode}.
	 */
	public void testCreateManagedObjectSourceNode_withinSection() {
		this.recordReturn(this.section, this.section.getOfficeNode(), this.office);
		this.recordReturn(this.office, this.office.getOfficeFloorNode(), this.officeFloor);
		this.recordReturn(this.office, this.office.getDeployedOfficeName(), "OFFICE");
		this.recordReturn(this.section, this.section.getSectionQualifiedName("MOS"), "SECTION.MOS");
		ManagedObjectSourceNode node = this.doTest(() -> {
			ManagedObjectSourceNode mos = this.context.createManagedObjectSourceNode("MOS", this.section);
			assertEquals("Incorrect managed object source name", "OFFICE.SECTION.MOS",
					mos.getManagedObjectSourceName());
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
	}

	/**
	 * Ensure can create {@link ManagedObjectSourceNode} within a
	 * {@link OfficeNode}.
	 */
	public void testCreateManagedObjectSourceNode_withinOffice() {
		this.recordReturn(this.office, this.office.getOfficeFloorNode(), this.officeFloor);
		this.recordReturn(this.office, this.office.getDeployedOfficeName(), "OFFICE");
		ManagedObjectSourceNode node = this.doTest(() -> {
			ManagedObjectSourceNode mos = this.context.createManagedObjectSourceNode("MOS", this.office);
			assertEquals("Incorrect managed object source name", "OFFICE.MOS", mos.getManagedObjectSourceName());
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
	}

	/**
	 * Ensure can create {@link ManagedObjectSourceNode} from
	 * {@link SupplierNode}.
	 */
	public void testCreateManagedObjectSourceNode_fromSupplier() {
		SuppliedManagedObjectNode suppliedManagedObject = this.createMock(SuppliedManagedObjectNode.class);
		SupplierNode supplier = this.createMock(SupplierNode.class);
		this.recordReturn(suppliedManagedObject, suppliedManagedObject.getSupplierNode(), supplier);
		this.recordReturn(supplier, supplier.getOfficeFloorNode(), this.officeFloor);
		ManagedObjectSourceNode node = this.doTest(() -> {
			ManagedObjectSourceNode mos = this.context.createManagedObjectSourceNode("MOS", suppliedManagedObject);
			assertEquals("Incorrect managed object source name", "MOS", mos.getManagedObjectSourceName());
			return mos;
		});
		assertNode(node, "MOS", "Managed Object Source", null, suppliedManagedObject);
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
	 * Ensure can create {@link ManagedObjectSourceNode} within a
	 * {@link OfficeFloorNode}.
	 */
	public void testCreateManagedObjectSourceNode_withinOfficeFloor() {
		ManagedObjectSourceNode node = this.doTest(() -> {
			ManagedObjectSourceNode mos = this.context.createManagedObjectSourceNode("MOS", this.officeFloor);
			assertEquals("Incorrect managed object source name", "MOS", mos.getManagedObjectSourceName());
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
	 * Ensure can create {@link ManagingOfficeNode}.
	 */
	public void testCreateManagingOfficeNode() {
		this.recordReturn(this.managedObjectSource, this.managedObjectSource.getManagedObjectSourceName(), "MOS");
		ManagingOfficeNode node = this.doTest(() -> {
			ManagingOfficeNode office = this.context.createManagingOfficeNode(this.managedObjectSource);
			assertNode(office, "Managing Office for Managed Object Source MOS", "Managing Office", null,
					this.managedObjectSource);
			return office;
		});
		assertInitialise(node, (n) -> n.initialise());
	}

	/**
	 * Ensure can create {@link OfficeFloorNode}.
	 */
	public void testCreateOfficeFloorNode() {
		OfficeFloorNode node = this
				.doTest(() -> this.context.createOfficeFloorNode("ExampleOfficeFloorSource", null, "LOCATION"));
		assertNode(node, OfficeFloorNode.OFFICE_FLOOR_NAME, "OfficeFloor", "ExampleOfficeFloorSource(LOCATION)", null);
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
		OfficeNode node = this.doTest(() -> this.context.createOfficeNode("OFFICE", this.officeFloor));
		assertNode(node, "OFFICE", "Office", "[NOT INITIALISED]", this.officeFloor);
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
	}

	/**
	 * Ensure can create {@link SectionInputNode}.
	 */
	public void testCreateSectionInputNode() {
		SectionInputNode node = this.doTest(() -> {
			SectionInputNode input = this.context.createSectionInputNode("INPUT", this.section);
			return input;
		});
		assertNode(node, "INPUT", "Section Input", null, this.section);
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
		assertEquals("Incorrect office section output name", "OUTPUT", node.getOfficeSectionOutputName());
		assertEquals("Incorrect section output name", "OUTPUT", node.getSectionOutputName());
		assertEquals("Incorrect sub section output name", "OUTPUT", node.getSubSectionOutputName());
		assertInitialise(node, (n) -> n.initialise("java.lang.Float", true));
	}

	/**
	 * Ensure can create {@link SuppliedManagedObjectNode}.
	 */
	public void testCreateSuppliedManagedObjectNode() {
		AutoWire autoWire = new AutoWire("TYPE");
		SupplierNode supplier = this.createMock(SupplierNode.class);
		SuppliedManagedObjectNode node = this
				.doTest(() -> this.context.createSuppliedManagedObjectNode(autoWire, supplier));
		assertNode(node, autoWire.getQualifiedType(), "Supplied Managed Object", null, supplier);
		assertInitialise(node, (n) -> n.initialise());
	}

	/**
	 * Ensure can create {@link SupplierNode}.
	 */
	public void testCreateSupplierNode() {
		SupplierNode node = this
				.doTest(() -> this.context.createSupplierNode("SUPPLIER", "ExampleSupplierSource", this.officeFloor));
		assertNode(node, "SUPPLIER", "Supplier", null, this.officeFloor);
		assertEquals("Incorrect supplier name", "SUPPLIER", node.getOfficeFloorSupplierName());
		assertInitialise(node, (n) -> n.initialise());
	}

	/**
	 * Ensure can create {@link TaskFlowNode}.
	 */
	public void testCreateTaskFlowNode() {
		TaskFlowNode node = this.doTest(() -> this.context.createTaskFlowNode("FLOW", true, this.task));
		assertNode(node, "FLOW", "Task Flow", null, this.task);
		assertEquals("Escalation is sequential", FlowInstigationStrategyEnum.SEQUENTIAL,
				node.getFlowInstigationStrategy());
		assertEquals("Incorrect task flow name", "FLOW", node.getTaskFlowName());
		assertInitialise(node, (n) -> n.initialise());
	}

	/**
	 * Ensure can create {@link TaskNode}.
	 */
	public void testCreateTaskNode() {
		WorkNode work = this.createMock(WorkNode.class);
		TaskNode node = this.doTest(() -> this.context.createTaskNode("TASK"));
		assertNode(node, "TASK", "Task", null, null);
		assertEquals("Incorrct office task name", "TASK", node.getOfficeTaskName());
		assertEquals("Incorrect section task name", "TASK", node.getSectionTaskName());
		assertNull("Should not have work", node.getWorkNode());
		assertInitialise(node, (n) -> n.initialise("TYPE", work));
		assertSame("Incorrect work", work, node.getWorkNode());
	}

	/**
	 * Ensure can create {@link TaskObjectNode}.
	 */
	public void testCreateTaskObjectNode() {
		TaskObjectNode node = this.doTest(() -> this.context.createTaskObjectNode("OBJECT", this.task));
		assertNode(node, "OBJECT", "Task Object", null, this.task);
		assertEquals("Incorrect task object name", "OBJECT", node.getTaskObjectName());
		assertInitialise(node, (n) -> n.initialise());
	}

	/**
	 * Ensure can create {@link TaskTeamNode}.
	 */
	public void testCreateTaskTeamNode() {
		TaskTeamNode node = this.doTest(() -> this.context.createTaskTeamNode("TEAM", this.task));
		assertNode(node, "TEAM", "Task Team", null, this.task);
		assertInitialise(node, (n) -> n.initialise());
	}

	/**
	 * Ensure can create {@link TeamNode}.
	 */
	public void testCreateTeamNode() {
		TeamNode node = this.context.createTeamNode("TEAM", this.officeFloor);
		assertNode(node, "TEAM", "Team", null, this.officeFloor);
		assertEquals("TEAM", node.getOfficeFloorTeamName());
		assertInitialise(node, (n) -> n.initialise("ExampleTeamSource"));
	}

	/**
	 * Ensure can create {@link WorkNode}.
	 */
	public void testCreateWorkNode() {
		this.recordReturn(this.section, this.section.getSectionQualifiedName("WORK"), "SECTION.WORK");
		WorkNode node = this.doTest(() -> {
			WorkNode work = this.context.createWorkNode("WORK", this.section);
			assertEquals("Incorrect qualified work name", "SECTION.WORK", work.getQualifiedWorkName());
			return work;
		});
		assertNode(node, "WORK", "Work", null, this.section);
		assertSame("Incorrect section", this.section, node.getSectionNode());
		assertEquals("Incorrect section work name", "WORK", node.getSectionWorkName());
		assertInitialise(node, (n) -> n.initialise("ExampleWorkSource", null));
	}

	/**
	 * Asserts the {@link Node} is correct.
	 * 
	 * @param node
	 *            {@link Node} to validate.
	 * @param name
	 *            Expected name.
	 * @param type
	 *            Expected type.
	 * @param location
	 *            Expected location.
	 * @param parent
	 *            Parent {@link Node}.
	 */
	private static void assertNode(Node node, String name, String type, String location, Node parent) {
		assertEquals("Incorrect node name", name, node.getNodeName());
		assertEquals("Incorrect node type", type, node.getNodeType());
		assertEquals("Incorrect node location", location, node.getLocation());
		assertSame("Incorrect node parent", parent, node.getParentNode());
	}

	/**
	 * Asserts the {@link Node} is initialised.
	 * 
	 * @param node
	 *            {@link Node} to validate initialising.
	 * @param initialiser
	 *            {@link Consumer} to initialise the {@link Node}.
	 */
	private static <N extends Node> void assertInitialise(N node, Consumer<N> initialiser) {
		assertFalse("Should not be initialised", node.isInitialised());
		initialiser.accept(node);
		assertTrue("Should now be initialised", node.isInitialised());
	}

}