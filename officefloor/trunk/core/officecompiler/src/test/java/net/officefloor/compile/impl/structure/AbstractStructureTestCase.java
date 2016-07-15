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
package net.officefloor.compile.impl.structure;

import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;
import net.officefloor.autowire.spi.supplier.source.SupplierSource;
import net.officefloor.autowire.spi.supplier.source.SupplierSourceContext;
import net.officefloor.autowire.spi.supplier.source.impl.AbstractSupplierSource;
import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.compile.impl.properties.PropertyListImpl;
import net.officefloor.compile.internal.structure.InputManagedObjectNode;
import net.officefloor.compile.internal.structure.LinkFlowNode;
import net.officefloor.compile.internal.structure.LinkObjectNode;
import net.officefloor.compile.internal.structure.LinkOfficeNode;
import net.officefloor.compile.internal.structure.LinkSynchronousNode;
import net.officefloor.compile.internal.structure.LinkTeamNode;
import net.officefloor.compile.internal.structure.ManagedObjectSourceNode;
import net.officefloor.compile.internal.structure.NodeContext;
import net.officefloor.compile.issues.CompilerIssues;
import net.officefloor.compile.properties.Property;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.section.OfficeSectionType;
import net.officefloor.compile.section.SectionLoader;
import net.officefloor.compile.spi.governance.source.GovernanceSource;
import net.officefloor.compile.spi.governance.source.impl.AbstractGovernanceSource;
import net.officefloor.compile.spi.office.OfficeAdministrator;
import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.compile.spi.office.OfficeGovernance;
import net.officefloor.compile.spi.office.OfficeManagedObjectSource;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.spi.office.source.OfficeSource;
import net.officefloor.compile.spi.office.source.OfficeSourceContext;
import net.officefloor.compile.spi.office.source.OfficeSourceSpecification;
import net.officefloor.compile.spi.officefloor.DeployedOffice;
import net.officefloor.compile.spi.officefloor.OfficeFloorDeployer;
import net.officefloor.compile.spi.officefloor.OfficeFloorManagedObjectSource;
import net.officefloor.compile.spi.officefloor.OfficeFloorSupplier;
import net.officefloor.compile.spi.officefloor.OfficeFloorTeam;
import net.officefloor.compile.spi.officefloor.source.OfficeFloorSource;
import net.officefloor.compile.spi.officefloor.source.OfficeFloorSourceContext;
import net.officefloor.compile.spi.officefloor.source.OfficeFloorSourceSpecification;
import net.officefloor.compile.spi.officefloor.source.RequiredProperties;
import net.officefloor.compile.spi.section.SectionDesigner;
import net.officefloor.compile.spi.section.SectionManagedObject;
import net.officefloor.compile.spi.section.SectionManagedObjectSource;
import net.officefloor.compile.spi.section.SectionTask;
import net.officefloor.compile.spi.section.SectionWork;
import net.officefloor.compile.spi.section.SubSection;
import net.officefloor.compile.spi.section.TaskFlow;
import net.officefloor.compile.spi.section.TaskObject;
import net.officefloor.compile.spi.section.source.SectionSource;
import net.officefloor.compile.spi.section.source.SectionSourceContext;
import net.officefloor.compile.spi.section.source.SectionSourceSpecification;
import net.officefloor.compile.spi.work.source.TaskEscalationTypeBuilder;
import net.officefloor.compile.spi.work.source.TaskFlowTypeBuilder;
import net.officefloor.compile.spi.work.source.TaskObjectTypeBuilder;
import net.officefloor.compile.spi.work.source.TaskTypeBuilder;
import net.officefloor.compile.spi.work.source.WorkSource;
import net.officefloor.compile.spi.work.source.WorkSourceContext;
import net.officefloor.compile.spi.work.source.WorkSourceSpecification;
import net.officefloor.compile.spi.work.source.WorkTypeBuilder;
import net.officefloor.compile.work.TaskEscalationType;
import net.officefloor.compile.work.TaskFlowType;
import net.officefloor.compile.work.TaskObjectType;
import net.officefloor.compile.work.TaskType;
import net.officefloor.frame.api.build.GovernanceFactory;
import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.build.TaskFactory;
import net.officefloor.frame.api.build.WorkFactory;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.spi.TestSource;
import net.officefloor.frame.spi.administration.Administrator;
import net.officefloor.frame.spi.administration.Duty;
import net.officefloor.frame.spi.administration.source.AdministratorSource;
import net.officefloor.frame.spi.administration.source.impl.AbstractAdministratorSource;
import net.officefloor.frame.spi.governance.Governance;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.managedobject.extension.ExtensionInterfaceFactory;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.spi.managedobject.source.impl.AbstractAsyncManagedObjectSource.MetaDataContext;
import net.officefloor.frame.spi.managedobject.source.impl.AbstractManagedObjectSource;
import net.officefloor.frame.spi.source.ResourceSource;
import net.officefloor.frame.spi.team.Job;
import net.officefloor.frame.spi.team.Team;
import net.officefloor.frame.spi.team.TeamIdentifier;
import net.officefloor.frame.spi.team.source.TeamSource;
import net.officefloor.frame.spi.team.source.TeamSourceContext;
import net.officefloor.frame.spi.team.source.TeamSourceSpecification;
import net.officefloor.frame.test.OfficeFrameTestCase;

/**
 * Abstract functionality for testing loading the {@link OfficeSection}.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class AbstractStructureTestCase extends OfficeFrameTestCase {

	/**
	 * Location of the top level {@link OfficeSection}.
	 */
	protected static final String SECTION_LOCATION = "SECTION_LOCATION";

	/**
	 * {@link ResourceSource}.
	 */
	protected final ResourceSource resourceSource = this
			.createMock(ResourceSource.class);

	/**
	 * {@link ClassLoader}.
	 */
	protected final ClassLoader classLoader = this.getClass().getClassLoader();

	/**
	 * {@link CompilerIssues}.
	 */
	protected final CompilerIssues issues = this
			.createMock(CompilerIssues.class);

	/**
	 * {@link NodeContext}.
	 */
	protected final NodeContext nodeContext;

	/**
	 * Initiate.
	 */
	public AbstractStructureTestCase() {
		OfficeFloorCompiler compiler = OfficeFloorCompiler
				.newOfficeFloorCompiler(this.classLoader);
		compiler.setCompilerIssues(this.issues);
		compiler.addResources(this.resourceSource);
		this.nodeContext = (NodeContext) compiler;
	}

	@Override
	protected void setUp() throws Exception {
		MakerSectionSource.reset();
		MakerManagedObjectSource.reset();
		MakerSupplierSource.reset();
		MakerWorkSource.reset();
		MakerGovernanceSource.reset();
		MakerAdministratorSource.reset();
		MakerTeamSource.reset();
		MakerOfficeSource.reset(this);
		MakerOfficeFloorSource.reset(this);
	}

	/**
	 * Asserts the {@link LinkFlowNode} source is linked to the target
	 * {@link LinkFlowNode}.
	 * 
	 * @param msg
	 *            Message.
	 * @param linkSource
	 *            Source {@link LinkFlowNode}.
	 * @param linkTarget
	 *            Target {@link LinkFlowNode}.
	 */
	protected static void assertFlowLink(String msg, Object linkSource,
			Object linkTarget) {
		assertTrue(
				msg + ": source must be " + LinkFlowNode.class.getSimpleName(),
				linkSource instanceof LinkFlowNode);
		assertEquals(msg, linkTarget,
				((LinkFlowNode) linkSource).getLinkedFlowNode());
	}

	/**
	 * Asserts the {@link LinkSynchronousNode} source is linked to the target
	 * {@link LinkSynchronousNode}.
	 * 
	 * @param msg
	 *            Message.
	 * @param linkSource
	 *            Source {@link LinkSynchronousNode}.
	 * @param linkTarget
	 *            Target {@link LinkSynchronousNode}.
	 */
	protected static void assertSynchronousLink(String msg, Object linkSource,
			Object linkTarget) {
		assertTrue(
				msg + ": source must be "
						+ LinkSynchronousNode.class.getSimpleName(),
				linkSource instanceof LinkSynchronousNode);
		assertEquals(msg, linkTarget,
				((LinkSynchronousNode) linkSource).getLinkedSynchronousNode());
	}

	/**
	 * Asserts the {@link LinkObjectNode} source is linked to the target
	 * {@link LinkObjectNode}.
	 * 
	 * @param msg
	 *            Message.
	 * @param linkSource
	 *            Source {@link LinkObjectNode}.
	 * @param linkTarget
	 *            Target {@link LinkObjectNode}.
	 */
	protected static void assertObjectLink(String msg, Object linkSource,
			Object linkTarget) {
		assertTrue(
				msg + ": source must be "
						+ LinkObjectNode.class.getSimpleName(),
				linkSource instanceof LinkObjectNode);
		assertEquals(msg, linkTarget,
				((LinkObjectNode) linkSource).getLinkedObjectNode());
	}

	/**
	 * Asserts the {@link LinkTeamNode} source is linked to the target
	 * {@link LinkTeamNode}.
	 * 
	 * @param msg
	 *            Message.
	 * @param linkSource
	 *            Source {@link LinkTeamNode}.
	 * @param linkTarget
	 *            Target {@link LinkTeamNode}.
	 */
	protected static void assertTeamLink(String msg, Object linkSource,
			Object linkTarget) {
		assertTrue(
				msg + ": source must be " + LinkTeamNode.class.getSimpleName(),
				linkSource instanceof LinkTeamNode);
		assertEquals(msg, linkTarget,
				((LinkTeamNode) linkSource).getLinkedTeamNode());
	}

	/**
	 * Asserts the {@link LinkOfficeNode} source is linked to the target
	 * {@link LinkOfficeNode}.
	 * 
	 * @param msg
	 *            Message.
	 * @param linkSource
	 *            Source {@link LinkOfficeNode}.
	 * @param linkTarget
	 *            Target {@link LinkOfficeNode}.
	 */
	protected static void assertOfficeLink(String msg, Object linkSource,
			Object linkTarget) {
		assertTrue(
				msg + ": source must be "
						+ LinkOfficeNode.class.getSimpleName(),
				linkSource instanceof LinkOfficeNode);
		assertEquals(msg, linkTarget,
				((LinkOfficeNode) linkSource).getLinkedOfficeNode());
	}

	/**
	 * Asserts the {@link ManagedObjectSourceNode} is linked to the
	 * {@link InputManagedObjectNode}.
	 * 
	 * @param msg
	 *            Message.
	 * @param managedObjectSourceNode
	 *            {@link ManagedObjectSourceNode}.
	 * @param inputManagedObjectNode
	 *            Linked {@link InputManagedObjectNode}.
	 */
	protected static void assertSourceInput(String msg,
			Object managedObjectSourceNode, Object inputManagedObjectNode) {
		assertTrue(
				msg + ": source must be "
						+ ManagedObjectSourceNode.class.getSimpleName(),
				managedObjectSourceNode instanceof ManagedObjectSourceNode);
		assertEquals(msg, inputManagedObjectNode,
				((ManagedObjectSourceNode) managedObjectSourceNode)
						.getInputManagedObjectNode());
	}

	/**
	 * Creates a mock {@link WorkFactory}.
	 * 
	 * @return Mock {@link WorkFactory}.
	 */
	@SuppressWarnings("unchecked")
	protected WorkFactory<Work> createMockWorkFactory() {
		return this.createMock(WorkFactory.class);
	}

	/**
	 * Creates a mock {@link TaskFactory}.
	 * 
	 * @return Mock {@link TaskFactory}.
	 */
	@SuppressWarnings("unchecked")
	protected TaskFactory<Work, ?, ?> createMockTaskFactory() {
		return this.createMock(TaskFactory.class);
	}

	/**
	 * Loads the {@link OfficeSectionType}.
	 * 
	 * @param sectionName
	 *            Name of the {@link OfficeSection}.
	 * @param maker
	 *            {@link SectionMaker} to make the {@link OfficeSection}.
	 * @return Loaded {@link OfficeSectionType}.
	 */
	protected OfficeSectionType loadOfficeSectionType(String sectionName,
			SectionMaker maker) {
		return this.loadOfficeSectionType(true, sectionName, maker);
	}

	/**
	 * Loads the {@link OfficeSectionType}.
	 * 
	 * @param isHandleMockState
	 *            <code>true</code> for method to handle mock states.
	 * @param sectionName
	 *            Name of the {@link OfficeSection}.
	 * @param maker
	 *            {@link SectionMaker} to make the {@link OfficeSection}.
	 * @return Loaded {@link OfficeSectionType}.
	 */
	protected OfficeSectionType loadOfficeSectionType(
			boolean isHandleMockState, String sectionName, SectionMaker maker) {

		// Register the section maker
		PropertyList propertyList = MakerSectionSource.register(maker);

		// Replay the mocks if handling mock state
		if (isHandleMockState) {
			this.replayMockObjects();
		}

		// Load the section
		OfficeFloorCompiler compiler = OfficeFloorCompiler
				.newOfficeFloorCompiler(this.classLoader);
		compiler.setCompilerIssues(this.issues);
		compiler.addResources(this.resourceSource);
		SectionLoader loader = compiler.getSectionLoader();
		OfficeSectionType sectionType = loader.loadOfficeSectionType(
				sectionName, MakerSectionSource.class, SECTION_LOCATION,
				propertyList);

		// Verify the mocks if handling mock state
		if (isHandleMockState) {
			this.verifyMockObjects();
		}

		// Return the section type
		return sectionType;
	}

	/**
	 * Adds the {@link OfficeSection} to the {@link OfficeArchitect}.
	 * 
	 * @param officeArchitect
	 *            {@link OfficeArchitect}.
	 * @param sectionName
	 *            Name of the {@link OfficeSection}.
	 * @param maker
	 *            {@link SectionMaker}.
	 * @return Added {@link OfficeSection}.
	 */
	protected OfficeSection addSection(OfficeArchitect officeArchitect,
			String sectionName, SectionMaker maker) {

		// Register the section maker
		PropertyList propertyList = MakerSectionSource.register(maker);

		// Add and return the office section
		return officeArchitect.addOfficeSection(sectionName,
				MakerSectionSource.class.getName(), sectionName, propertyList);
	}

	/**
	 * Adds an {@link OfficeManagedObjectSource} to the {@link OfficeArchitect}.
	 * 
	 * @param officeArchitect
	 *            {@link OfficeArchitect}.
	 * @param managedObjectSourceName
	 *            Name of the {@link OfficeManagedObjectSource}.
	 * @param maker
	 *            {@link ManagedObjectMaker}.
	 * @return {@link OfficeManagedObjectSource}.
	 */
	protected OfficeManagedObjectSource addManagedObjectSource(
			OfficeArchitect officeArchitect, String managedObjectSourceName,
			ManagedObjectMaker maker) {

		// Register the managed object maker
		PropertyList propertyList = MakerManagedObjectSource.register(maker);

		// Add and return the managed object source
		OfficeManagedObjectSource moSource = officeArchitect
				.addOfficeManagedObjectSource(managedObjectSourceName,
						MakerManagedObjectSource.class.getName());
		for (Property property : propertyList) {
			moSource.addProperty(property.getName(), property.getValue());
		}
		return moSource;
	}

	/**
	 * Adds an {@link OfficeGovernance} to the {@link OfficeArchitect}.
	 * 
	 * @param officeArchitect
	 *            {@link OfficeArchitect}.
	 * @param governanceName
	 *            Name of the {@link OfficeGovernance}.
	 * @param maker
	 *            {@link GovernanceMaker}.
	 * @return {@link OfficeGovernance}.
	 */
	protected OfficeGovernance addGovernance(OfficeArchitect officeArchitect,
			String governanceName, GovernanceMaker maker) {

		// Register the governance maker
		PropertyList propertyList = MakerGovernanceSource.register(maker);

		// Add and return the governance
		OfficeGovernance gov = officeArchitect.addOfficeGovernance(
				governanceName, MakerGovernanceSource.class.getName());
		for (Property property : propertyList) {
			gov.addProperty(property.getName(), property.getValue());
		}
		return gov;
	}

	/**
	 * Adds an {@link OfficeAdministrator} to the {@link OfficeArchitect}.
	 * 
	 * @param officeArchitect
	 *            {@link OfficeArchitect}.
	 * @param administratorName
	 *            Name of the {@link OfficeAdministrator}.
	 * @param maker
	 *            {@link AdministratorMaker}.
	 * @return {@link OfficeAdministrator}.
	 */
	protected OfficeAdministrator addAdministrator(
			OfficeArchitect officeArchitect, String administratorName,
			AdministratorMaker maker) {

		// Register the administrator maker
		PropertyList propertyList = MakerAdministratorSource.register(maker);

		// Add and return the administrator
		OfficeAdministrator admin = officeArchitect.addOfficeAdministrator(
				administratorName, MakerAdministratorSource.class.getName());
		for (Property property : propertyList) {
			admin.addProperty(property.getName(), property.getValue());
		}
		return admin;
	}

	/**
	 * Adds a simple {@link OfficeAdministrator} to the
	 * {@link OfficeAdministrator} with the supplied extension interface and a
	 * single {@link Duty}.
	 * 
	 * @param officeArchitect
	 *            {@link OfficeArchitect}.
	 * @param administratorName
	 *            Name of the {@link OfficeAdministrator}.
	 * @param extensionInterface
	 *            Extension interface.
	 * @return {@link OfficeAdministrator}.
	 */
	protected OfficeAdministrator addAdministrator(
			OfficeArchitect officeArchitect, String administratorName,
			final Class<?> extensionInterface, final Enum<?> firstDutyKey,
			final Enum<?>... remainingDutyKeys) {

		// Create the simple administrator maker
		AdministratorMaker maker = new AdministratorMaker() {
			@Override
			public void make(AdministratorMakerContext context) {
				// Specify the extension interface
				context.setExtensionInterface(extensionInterface);

				// Specify the duties
				context.addDuty(firstDutyKey); // must be at least one
				for (Enum<?> dutyKey : remainingDutyKeys) {
					context.addDuty(dutyKey);
				}
			}
		};

		// Add and return the administrator
		return this.addAdministrator(officeArchitect, administratorName, maker);
	}

	/**
	 * {@link Duty} key for a simple {@link OfficeAdministrator}.
	 */
	protected enum SimpleDutyKey {
		DUTY
	};

	/**
	 * Adds an {@link OfficeFloorTeam} to the {@link OfficeFloorDeployer}.
	 * 
	 * @param officeFloorDeployer
	 *            {@link OfficeFloorDeployer}.
	 * @param teamName
	 *            Name of the {@link OfficeFloorTeam}.
	 * @param maker
	 *            {@link TeamMaker}.
	 * @return {@link OfficeFloorTeam}.
	 */
	protected OfficeFloorTeam addTeam(OfficeFloorDeployer officeFloorDeployer,
			String teamName, TeamMaker maker) {

		// Register the team maker
		PropertyList propertyList = MakerTeamSource.register(maker);

		// Add and return the team
		OfficeFloorTeam team = officeFloorDeployer.addTeam(teamName,
				MakerTeamSource.class.getName());
		for (Property property : propertyList) {
			team.addProperty(property.getName(), property.getValue());
		}
		return team;
	}

	/**
	 * Adds an {@link OfficeFloorManagedObjectSource} to the
	 * {@link OfficeFloorDeployer}.
	 * 
	 * @param officeFloorDeployer
	 *            {@link OfficeFloorDeployer}.
	 * @param managedObjectSourceName
	 *            Name of the {@link OfficeFloorManagedObjectSource}.
	 * @param maker
	 *            {@link ManagedObjectMaker}.
	 * @return {@link OfficeFloorManagedObjectSource}.
	 */
	protected OfficeFloorManagedObjectSource addManagedObjectSource(
			OfficeFloorDeployer officeFloorDeployer,
			String managedObjectSourceName, ManagedObjectMaker maker) {

		// Register the managed object maker
		PropertyList propertyList = MakerManagedObjectSource.register(maker);

		// Add and return the managed object source
		OfficeFloorManagedObjectSource moSource = officeFloorDeployer
				.addManagedObjectSource(managedObjectSourceName,
						MakerManagedObjectSource.class.getName());
		for (Property property : propertyList) {
			moSource.addProperty(property.getName(), property.getValue());
		}
		return moSource;
	}

	/**
	 * Adds an {@link OfficeFloorSupplier} to the {@link OfficeFloorDeployer}.
	 * 
	 * @param officeFloorDeployer
	 *            {@link OfficeFloorDeployer}.
	 * @param supplierName
	 *            Name of the {@link OfficeFloorSupplier}.
	 * @param maker
	 *            {@link SupplierMaker}.
	 * @return {@link OfficeFloorSupplier}.
	 */
	protected OfficeFloorSupplier addSupplier(
			OfficeFloorDeployer officeFloorDeployer, String supplierName,
			SupplierMaker maker) {

		// Register the supplier maker
		PropertyList propertyList = MakerSupplierSource.register(maker);

		// Add and return the supplier
		OfficeFloorSupplier supplier = officeFloorDeployer.addSupplier(
				supplierName, MakerSupplierSource.class.getName());
		for (Property property : propertyList) {
			supplier.addProperty(property.getName(), property.getValue());
		}
		return supplier;
	}

	/**
	 * Adds a {@link DeployedOffice}.
	 * 
	 * @param officeFloorDeployer
	 *            {@link OfficeFloorDeployer}.
	 * @param officeName
	 *            Name of the {@link DeployedOffice}. Also used as location of
	 *            the {@link DeployedOffice}.
	 * @param maker
	 *            {@link OfficeMaker}.
	 * @return {@link DeployedOffice}.
	 */
	protected DeployedOffice addDeployedOffice(
			OfficeFloorDeployer officeFloorDeployer, String officeName,
			OfficeMaker maker) {

		// Register the office maker
		PropertyList propertyList = MakerOfficeSource.register(maker);

		// Add and return the deployed office
		DeployedOffice office = officeFloorDeployer.addDeployedOffice(
				officeName, MakerOfficeSource.class.getName(), officeName);
		for (Property property : propertyList) {
			office.addProperty(property.getName(), property.getValue());
		}
		return office;
	}

	/**
	 * Makes the {@link SubSection}.
	 */
	protected static interface SectionMaker {

		/**
		 * Test logic to make the {@link SubSection}.
		 * 
		 * @param context
		 *            {@link SectionMakerContext}.
		 */
		void make(SectionMakerContext context);
	}

	/**
	 * Context for the {@link SectionMaker}.
	 */
	protected static interface SectionMakerContext {

		/**
		 * Obtains the {@link SectionDesigner}.
		 * 
		 * @return {@link SectionDesigner}.
		 */
		SectionDesigner getBuilder();

		/**
		 * Obtains the {@link SectionSourceContext}.
		 * 
		 * @return {@link SectionSourceContext}.
		 */
		SectionSourceContext getContext();

		/**
		 * Adds a {@link SubSection}.
		 * 
		 * @param subSectionName
		 *            Name of the {@link SubSection} which is also used as the
		 *            {@link SubSection} location.
		 * @param maker
		 *            {@link SectionMaker}.
		 * @return Added {@link SubSection}.
		 */
		SubSection addSubSection(String subSectionName, SectionMaker maker);

		/**
		 * Adds a {@link SectionManagedObjectSource}.
		 * 
		 * @param managedObjectSourceName
		 *            Name of the {@link SectionManagedObjectSource}.
		 * @param maker
		 *            {@link ManagedObjectMaker}.
		 * @return Added {@link SectionManagedObjectSource}.
		 */
		SectionManagedObjectSource addManagedObjectSource(
				String managedObjectSourceName, ManagedObjectMaker maker);

		/**
		 * Adds a {@link SectionWork}.
		 * 
		 * @param workName
		 *            Name of the {@link SectionWork}.
		 * @param workFactory
		 *            {@link WorkFactory}.
		 * @param maker
		 *            {@link WorkMaker}.
		 * @return Added {@link SectionWork}.
		 */
		<W extends Work> SectionWork addWork(String workName,
				WorkFactory<W> workFactory, WorkMaker maker);

		/**
		 * Adds a {@link SectionTask}.
		 * 
		 * @param workName
		 *            Name of the {@link SectionWork} for the
		 *            {@link SectionTask}.
		 * @param workFactory
		 *            {@link WorkFactory}.
		 * @param taskName
		 *            Name of the {@link SectionTask}.
		 * @param taskFactory
		 *            {@link TaskFactory}.
		 * @param taskMaker
		 *            {@link TaskMaker} for the {@link SectionTask}.
		 * @return {@link SectionTask}.
		 */
		<W extends Work> SectionTask addTask(String workName,
				WorkFactory<W> workFactory, String taskName,
				TaskFactory<W, ?, ?> taskFactory, TaskMaker taskMaker);

		/**
		 * Adds a {@link TaskFlow}.
		 * 
		 * @param workName
		 *            Name of the {@link SectionWork} for the
		 *            {@link SectionTask}.
		 * @param workFactory
		 *            {@link WorkFactory}.
		 * @param taskName
		 *            Name of the {@link SectionTask}.
		 * @param taskFactory
		 *            {@link TaskFactory}.
		 * @param flowName
		 *            Name of the {@link TaskFlowType}.
		 * @param argumentType
		 *            Argument type.
		 * @return {@link TaskFlow}.
		 */
		<W extends Work> TaskFlow addTaskFlow(String workName,
				WorkFactory<W> workFactory, String taskName,
				TaskFactory<W, ?, ?> taskFactory, String flowName,
				Class<?> argumentType);

		/**
		 * Adds a {@link TaskObject}.
		 * 
		 * @param workName
		 *            Name of the {@link SectionWork} for the
		 *            {@link SectionTask}.
		 * @param workFactory
		 *            {@link WorkFactory}.
		 * @param taskName
		 *            Name of the {@link SectionTask}.
		 * @param taskFactory
		 *            {@link TaskFactory}.
		 * @param objectName
		 *            Name of the {@link TaskObjectType}.
		 * @param objectType
		 *            Object type.
		 * @param typeQualifier
		 *            Type qualifier.
		 * @return {@link TaskObject}.
		 */
		<W extends Work> TaskObject addTaskObject(String workName,
				WorkFactory<W> workFactory, String taskName,
				TaskFactory<W, ?, ?> taskFactory, String objectName,
				Class<?> objectType, String typeQualifier);

		/**
		 * Adds a {@link TaskFlow} for a {@link TaskEscalationType}.
		 * 
		 * @param workName
		 *            Name of the {@link SectionWork} for the
		 *            {@link SectionTask}.
		 * @param workFactory
		 *            {@link WorkFactory}.
		 * @param taskName
		 *            Name of the {@link SectionTask}.
		 * @param taskFactory
		 *            {@link TaskFactory}.
		 * @param escalationName
		 *            Name of the {@link TaskEscalationType}.
		 * @param escalationType
		 *            Escalation type.
		 * @return {@link TaskFlow}.
		 */
		<E extends Throwable, W extends Work> TaskFlow addTaskEscalation(
				String workName, WorkFactory<W> workFactory, String taskName,
				TaskFactory<W, ?, ?> taskFactory, String escalationName,
				Class<E> escalationType);
	}

	/**
	 * Maker {@link SectionSource}.
	 */
	@TestSource
	public static class MakerSectionSource implements SectionSource,
			SectionMakerContext {

		/**
		 * Property name to obtain the {@link SectionMaker} identifier.
		 */
		private static final String MAKER_IDENTIFIER_PROPERTY_NAME = "section.maker";

		/**
		 * {@link SectionMaker} instances by their identifier.
		 */
		private static Map<String, SectionMaker> sectionMakers;

		/**
		 * Resets for another test.
		 */
		public static void reset() {
			sectionMakers = new HashMap<String, SectionMaker>();
		}

		/**
		 * Registers a {@link SectionMaker}.
		 * 
		 * @param maker
		 *            {@link SectionMaker}.
		 * @return {@link PropertyList} to use to load the
		 *         {@link MakerSectionSource}.
		 */
		public static PropertyList register(SectionMaker maker) {

			// Ensure have a maker
			if (maker == null) {
				maker = new SectionMaker() {
					@Override
					public void make(SectionMakerContext context) {
						// Empty section
					}
				};
			}

			// Register the section maker
			String identifier = String.valueOf(sectionMakers.size());
			sectionMakers.put(identifier, maker);

			// Create and return the properties to load
			PropertyList propertyList = new PropertyListImpl(
					MAKER_IDENTIFIER_PROPERTY_NAME, identifier);
			return propertyList;
		}

		/**
		 * {@link SectionDesigner}.
		 */
		private SectionDesigner builder;

		/**
		 * {@link SectionSourceContext}.
		 */
		private SectionSourceContext context;

		/*
		 * ===================== SectionSource =============================
		 */

		@Override
		public SectionSourceSpecification getSpecification() {
			fail("Should not require specification");
			return null;
		}

		@Override
		public void sourceSection(SectionDesigner sectionBuilder,
				SectionSourceContext context) throws Exception {

			// Store details to load
			this.builder = sectionBuilder;
			this.context = context;

			// Obtain the section maker
			String identifier = context
					.getProperty(MAKER_IDENTIFIER_PROPERTY_NAME);
			SectionMaker maker = sectionMakers.get(identifier);

			// Make the section
			maker.make(this);
		}

		/*
		 * ======================= SectionMakerContext ====================
		 */

		@Override
		public SectionDesigner getBuilder() {
			return this.builder;
		}

		@Override
		public SectionSourceContext getContext() {
			return this.context;
		}

		@Override
		public SubSection addSubSection(String subSectionName,
				SectionMaker maker) {

			// Register the section source
			PropertyList properties = MakerSectionSource.register(maker);

			// Return the created sub section (using name as location)
			SubSection subSection = this.builder.addSubSection(subSectionName,
					MakerSectionSource.class.getName(), subSectionName);
			for (Property property : properties) {
				subSection.addProperty(property.getName(), property.getValue());
			}
			return subSection;
		}

		@Override
		public SectionManagedObjectSource addManagedObjectSource(
				String managedObjectSourceName, ManagedObjectMaker maker) {

			// Register the managed object source
			PropertyList propertyList = MakerManagedObjectSource
					.register(maker);

			// Create and return the section managed object
			SectionManagedObjectSource moSource = this.builder
					.addSectionManagedObjectSource(managedObjectSourceName,
							MakerManagedObjectSource.class.getName());
			for (Property property : propertyList) {
				moSource.addProperty(property.getName(), property.getValue());
			}
			return moSource;
		}

		@Override
		public <W extends Work> SectionWork addWork(String workName,
				WorkFactory<W> workFactory, WorkMaker maker) {

			// Register the work maker
			PropertyList properties = MakerWorkSource.register(maker,
					workFactory);

			// Return the created work
			SectionWork work = this.builder.addSectionWork(workName,
					MakerWorkSource.class.getName());
			for (Property property : properties) {
				work.addProperty(property.getName(), property.getValue());
			}
			return work;
		}

		@Override
		public <W extends Work> SectionTask addTask(String workName,
				WorkFactory<W> workFactory, final String taskName,
				final TaskFactory<W, ?, ?> taskFactory,
				final TaskMaker taskMaker) {
			// Create the section work containing the single task type
			WorkMaker workMaker = new WorkMaker() {
				@Override
				public void make(WorkMakerContext context) {
					// Create the task type and make if required
					TaskTypeMaker maker = context
							.addTask(taskName, taskFactory);
					if (taskMaker != null) {
						taskMaker.make(maker);
					}
				}
			};
			SectionWork work = this.addWork(workName, workFactory, workMaker);

			// Return the section task
			return work.addSectionTask(taskName, taskName);
		}

		@Override
		public <W extends Work> TaskFlow addTaskFlow(String workName,
				WorkFactory<W> workFactory, String taskName,
				TaskFactory<W, ?, ?> taskFactory, final String flowName,
				final Class<?> argumentType) {
			// Create the section task containing of a single flow
			TaskMaker taskMaker = new TaskMaker() {
				@Override
				public void make(TaskTypeMaker maker) {
					// Create the flow
					maker.addFlow(flowName, argumentType);
				}
			};
			SectionTask task = this.addTask(workName, workFactory, taskName,
					taskFactory, taskMaker);

			// Return the task flow
			return task.getTaskFlow(flowName);
		}

		@Override
		public <W extends Work> TaskObject addTaskObject(String workName,
				WorkFactory<W> workFactory, String taskName,
				TaskFactory<W, ?, ?> taskFactory, final String objectName,
				final Class<?> objectType, final String typeQualifier) {
			// Create the section task containing of a single object
			TaskMaker taskMaker = new TaskMaker() {
				@Override
				public void make(TaskTypeMaker maker) {
					// Create the object
					maker.addObject(objectName, objectType, typeQualifier);
				}
			};
			SectionTask task = this.addTask(workName, workFactory, taskName,
					taskFactory, taskMaker);

			// Return the task object
			return task.getTaskObject(objectName);
		}

		@Override
		public <E extends Throwable, W extends Work> TaskFlow addTaskEscalation(
				String workName, WorkFactory<W> workFactory, String taskName,
				TaskFactory<W, ?, ?> taskFactory, final String escalationName,
				final Class<E> escalationType) {
			// Create the section task containing of a single escalation
			TaskMaker taskMaker = new TaskMaker() {
				@Override
				public void make(TaskTypeMaker maker) {
					// Create the escalation
					maker.addEscalation(escalationName, escalationType);
				}
			};
			SectionTask task = this.addTask(workName, workFactory, taskName,
					taskFactory, taskMaker);

			// Return the task escalation
			return task.getTaskEscalation(escalationName);
		}
	}

	/**
	 * Makes the {@link SectionManagedObject}.
	 */
	protected static interface ManagedObjectMaker {

		/**
		 * Makes the {@link SectionManagedObject}.
		 * 
		 * @param context
		 *            {@link ManagedObjectMakerContext}.
		 */
		void make(ManagedObjectMakerContext context);
	}

	/**
	 * Context for the {@link ManagedObjectMaker}.
	 */
	protected static interface ManagedObjectMakerContext {

		/**
		 * Obtains the {@link MetaDataContext}.
		 * 
		 * @return {@link MetaDataContext}.
		 */
		MetaDataContext<Indexed, Indexed> getContext();

		/**
		 * Adds an extension interface supported by the {@link ManagedObject}.
		 * 
		 * @param extensionInterface
		 *            Extension interface supported.
		 */
		void addExtensionInterface(Class<?> extensionInterface);
	}

	/**
	 * Maker {@link ManagedObjectSource}.
	 */
	@TestSource
	public static class MakerManagedObjectSource extends
			AbstractManagedObjectSource<Indexed, Indexed> implements
			ManagedObjectMakerContext {

		/**
		 * Name of property holding the identifier for the
		 * {@link ManagedObjectMaker}.
		 */
		private static final String MAKER_IDENTIFIER_PROPERTY_NAME = "managed.object.maker";

		/**
		 * {@link ManagedObjectMaker} instances by their identifiers.
		 */
		private static Map<String, ManagedObjectMaker> managedObjectMakers;

		/**
		 * Clears the {@link ManagedObjectMaker} instances for the next test.
		 */
		public static void reset() {
			managedObjectMakers = new HashMap<String, ManagedObjectMaker>();
		}

		/**
		 * Registers a {@link ManagedObjectMaker}.
		 * 
		 * @param maker
		 *            {@link ManagedObjectMaker}.
		 * @return {@link PropertyList}.
		 */
		public static PropertyList register(ManagedObjectMaker maker) {

			// Ensure have a maker
			if (maker == null) {
				maker = new ManagedObjectMaker() {
					@Override
					public void make(ManagedObjectMakerContext context) {
						// Empty managed object
					}
				};
			}

			// Register the managed object maker
			String identifier = String.valueOf(managedObjectMakers.size());
			managedObjectMakers.put(identifier, maker);

			// Return the property list
			return new PropertyListImpl(MAKER_IDENTIFIER_PROPERTY_NAME,
					identifier);
		}

		/**
		 * {@link MetaDataContext}.
		 */
		private MetaDataContext<Indexed, Indexed> context;

		/*
		 * ================= AbstractManagedObjectSource ======================
		 */

		@Override
		protected void loadSpecification(SpecificationContext context) {
			fail("Should not require specification");
		}

		@Override
		protected void loadMetaData(MetaDataContext<Indexed, Indexed> context)
				throws Exception {

			// Provide default object type
			context.setObjectClass(Object.class);

			// Store details to load
			this.context = context;

			// Obtain the managed object maker
			String identifier = context.getManagedObjectSourceContext()
					.getProperty(MAKER_IDENTIFIER_PROPERTY_NAME);
			ManagedObjectMaker managedObjectMaker = managedObjectMakers
					.get(identifier);

			// Make the managed object
			managedObjectMaker.make(this);
		}

		@Override
		protected ManagedObject getManagedObject() throws Throwable {
			fail("Should not require to source managed object");
			return null;
		}

		/*
		 * ==================== ManagedObjectMakerContext ====================
		 */

		@Override
		public MetaDataContext<Indexed, Indexed> getContext() {
			return this.context;
		}

		@Override
		@SuppressWarnings({ "unchecked", "rawtypes" })
		public void addExtensionInterface(Class<?> extensionInterface) {
			// Add the extension interface
			this.context.addManagedObjectExtensionInterface(extensionInterface,
					new ExtensionInterfaceFactory() {
						@Override
						public Object createExtensionInterface(
								ManagedObject managedObject) {
							fail("Should not require to create extension interface");
							return null;
						}
					});
		}
	}

	/**
	 * Makes the {@link OfficeFloorSupplier}.
	 */
	protected static interface SupplierMaker {

		/**
		 * As per {@link SupplierSource}.
		 * 
		 * @param context
		 *            {@link SupplierSourceContext}.
		 * @throws Exception
		 *             If fails to supply the {@link ManagedObjectSource}
		 *             instances.
		 */
		void supply(SupplierSourceContext context) throws Exception;
	}

	/**
	 * Maker {@link SupplierSource}.
	 */
	@TestSource
	public static class MakerSupplierSource extends AbstractSupplierSource {

		/**
		 * Name of property holding the identifier for the {@link SupplierMaker}
		 * .
		 */
		private static final String MAKER_IDENTIFIER_PROPERTY_NAME = "supplier.maker";

		/**
		 * {@link SupplierMaker} instances by their identifiers.
		 */
		private static Map<String, SupplierMaker> supplierMakers;

		/**
		 * Clears the {@link SupplierMaker} instances for the next test.
		 */
		public static void reset() {
			supplierMakers = new HashMap<String, SupplierMaker>();
		}

		/**
		 * Registers a {@link SupplierMaker}.
		 * 
		 * @param maker
		 *            {@link SupplierMaker}.
		 * @return {@link PropertyList}.
		 */
		public static PropertyList register(SupplierMaker maker) {

			// Ensure have a maker
			if (maker == null) {
				maker = new SupplierMaker() {
					@Override
					public void supply(SupplierSourceContext context)
							throws Exception {
						// No managed objects
					}
				};
			}

			// Register the managed object maker
			String identifier = String.valueOf(supplierMakers.size());
			supplierMakers.put(identifier, maker);

			// Return the property list
			return new PropertyListImpl(MAKER_IDENTIFIER_PROPERTY_NAME,
					identifier);
		}

		/*
		 * ================== SupplierSource =========================
		 */

		@Override
		protected void loadSpecification(SpecificationContext context) {
			fail("Should not require specification");
		}

		@Override
		public void supply(SupplierSourceContext context) throws Exception {

			// Obtain the supplier maker
			String identifier = context
					.getProperty(MAKER_IDENTIFIER_PROPERTY_NAME);
			SupplierMaker maker = supplierMakers.get(identifier);
			assertNotNull("Unknown supplier maker " + identifier, maker);

			// Make the supplier
			maker.supply(context);
		}
	}

	/**
	 * Makes the {@link SectionWork}.
	 */
	protected static interface WorkMaker {

		/**
		 * Makes the {@link SectionWork}.
		 * 
		 * @param context
		 *            {@link WorkMakerContext}.
		 */
		void make(WorkMakerContext context);
	}

	/**
	 * Context for the {@link WorkMaker}.
	 */
	protected static interface WorkMakerContext {

		/**
		 * Obtains the {@link WorkTypeBuilder}.
		 * 
		 * @return {@link WorkTypeBuilder}.
		 */
		WorkTypeBuilder<Work> getBuilder();

		/**
		 * Obtains the {@link WorkSourceContext}.
		 * 
		 * @return {@link WorkSourceContext}.
		 */
		WorkSourceContext getContext();

		/**
		 * Obtains the {@link WorkFactory} used for the {@link SectionWork}.
		 * 
		 * @return {@link WorkFactory} used for the {@link SectionWork}.
		 */
		WorkFactory<Work> getWorkFactory();

		/**
		 * Adds a {@link TaskType}.
		 * 
		 * @param taskTypeName
		 *            Name of the {@link TaskType}.
		 * @param taskFactory
		 *            {@link TaskFactory}.
		 * @return {@link TaskTypeMaker} to make the {@link TaskType}.
		 */
		<W extends Work> TaskTypeMaker addTask(String taskTypeName,
				TaskFactory<W, ?, ?> taskFactory);
	}

	/**
	 * Makes the {@link TaskType}.
	 */
	protected static interface TaskMaker {

		/**
		 * Makes the {@link TaskType}.
		 * 
		 * @param maker
		 *            {@link TaskTypeMaker}.
		 */
		void make(TaskTypeMaker maker);
	}

	/**
	 * Context for the {@link TaskMaker}.
	 */
	protected static interface TaskTypeMaker {

		/**
		 * Adds a {@link TaskFlowType}.
		 * 
		 * @param flowName
		 *            Name of the {@link TaskFlowType}.
		 * @param argumentType
		 *            Argument type.
		 * @return {@link TaskFlowTypeBuilder}.
		 */
		TaskFlowTypeBuilder<?> addFlow(String flowName, Class<?> argumentType);

		/**
		 * Adds a {@link TaskObjectType}.
		 * 
		 * @param objectName
		 *            Name of the {@link TaskObjectType}.
		 * @param objectType
		 *            Object type.
		 * @param typeQualifier
		 *            Type qualifier.
		 * @return {@link TaskObjectTypeBuilder}.
		 */
		TaskObjectTypeBuilder<?> addObject(String objectName,
				Class<?> objectType, String typeQualifier);

		/**
		 * Adds a {@link TaskEscalationTypeBuilder}.
		 * 
		 * @param escalationName
		 *            Name of the {@link TaskEscalationType}.
		 * @param escalationType
		 *            Escalation type.
		 * @return {@link TaskEscalationTypeBuilder}.
		 */
		<E extends Throwable> TaskEscalationTypeBuilder addEscalation(
				String escalationName, Class<E> escalationType);
	}

	/**
	 * Maker {@link WorkSource}.
	 */
	@TestSource
	public static class MakerWorkSource implements WorkSource<Work>,
			WorkMakerContext {

		/**
		 * Property name for the {@link WorkMaker} identifier.
		 */
		public static final String MAKER_IDENTIFIER_PROPERTY_NAME = "work.maker";

		/**
		 * {@link WorkMaker} instances by their identifiers.
		 */
		private static Map<String, WorkMaker> workMakers;

		/**
		 * {@link WorkFactory} instances by their identifiers.
		 */
		private static Map<String, WorkFactory<?>> workFactories;

		/**
		 * Resets for the next load.
		 */
		public static void reset() {
			workMakers = new HashMap<String, WorkMaker>();
			workFactories = new HashMap<String, WorkFactory<?>>();
		}

		/**
		 * Registers a {@link WorkMaker}.
		 * 
		 * @param maker
		 *            {@link WorkMaker}.
		 * @param workFactory
		 *            {@link WorkFactory}.
		 * @return {@link PropertyList}.
		 */
		public static <W extends Work> PropertyList register(WorkMaker maker,
				WorkFactory<W> workFactory) {

			// Ensure have a maker
			if (maker == null) {
				maker = new WorkMaker() {
					@Override
					public void make(WorkMakerContext context) {
						// Empty work
					}
				};
			}

			// Register the work maker and factory
			String identifier = String.valueOf(workMakers.size());
			workMakers.put(identifier, maker);
			workFactories.put(identifier, workFactory);

			// Return the property list
			return new PropertyListImpl(MAKER_IDENTIFIER_PROPERTY_NAME,
					identifier);
		}

		/**
		 * {@link WorkFactory}.
		 */
		@SuppressWarnings("rawtypes")
		private WorkFactory workFactory;

		/**
		 * {@link WorkTypeBuilder}.
		 */
		@SuppressWarnings("rawtypes")
		private WorkTypeBuilder builder;

		/**
		 * {@link WorkSourceContext}.
		 */
		private WorkSourceContext context;

		/*
		 * ==================== WorkSource ==============================
		 */

		@Override
		public WorkSourceSpecification getSpecification() {
			fail("Should not require specification");
			return null;
		}

		@Override
		@SuppressWarnings("unchecked")
		public void sourceWork(WorkTypeBuilder<Work> workTypeBuilder,
				WorkSourceContext context) throws Exception {

			// Obtain the work maker
			String identifier = context
					.getProperty(MAKER_IDENTIFIER_PROPERTY_NAME);
			WorkMaker workMaker = workMakers.get(identifier);

			// Obtain and load the work factory
			this.workFactory = workFactories.get(identifier);
			workTypeBuilder.setWorkFactory(this.workFactory);

			// Store details to load
			this.builder = workTypeBuilder;
			this.context = context;

			// Make the work
			workMaker.make(this);
		}

		/*
		 * ================== WorkMakerContext ===========================
		 */

		@Override
		@SuppressWarnings("unchecked")
		public WorkTypeBuilder<Work> getBuilder() {
			return this.builder;
		}

		@Override
		public WorkSourceContext getContext() {
			return this.context;
		}

		@Override
		@SuppressWarnings("unchecked")
		public WorkFactory<Work> getWorkFactory() {
			return this.workFactory;
		}

		@Override
		@SuppressWarnings({ "unchecked", "rawtypes" })
		public TaskTypeMaker addTask(String taskName, TaskFactory taskFactory) {
			// Create and return the task type maker
			return new TaskTypeMakerImpl(taskName, taskFactory, this.builder);
		}

		/**
		 * {@link TaskTypeMaker} implementation.
		 */
		private class TaskTypeMakerImpl implements TaskTypeMaker {

			/**
			 * {@link TaskFactoryManufacturer}.
			 */
			private final TaskFactory<Work, ?, ?> taskFactory;

			/**
			 * {@link TaskTypeBuilder}.
			 */
			@SuppressWarnings("rawtypes")
			private final TaskTypeBuilder taskTypeBuilder;

			/**
			 * Initiate.
			 * 
			 * @param taskName
			 *            Name of the {@link SectionTask}.
			 * @param taskFactory
			 *            {@link TaskFactory}.
			 * @param workTypeBuilder
			 *            {@link WorkTypeBuilder}.
			 */
			public TaskTypeMakerImpl(String taskName,
					TaskFactory<Work, ?, ?> taskFactory,
					WorkTypeBuilder<Work> workTypeBuilder) {
				this.taskFactory = taskFactory;

				// Create the task type builder
				this.taskTypeBuilder = workTypeBuilder.addTaskType(taskName,
						this.taskFactory, null, null);
			}

			/*
			 * ================ TaskMake =================================
			 */

			@Override
			public TaskFlowTypeBuilder<?> addFlow(String flowName,
					Class<?> argumentType) {
				// Create and return the task flow type builder
				TaskFlowTypeBuilder<?> flowBuilder = this.taskTypeBuilder
						.addFlow();
				flowBuilder.setArgumentType(argumentType);
				flowBuilder.setLabel(flowName);
				return flowBuilder;
			}

			@Override
			@SuppressWarnings("unchecked")
			public TaskObjectTypeBuilder<?> addObject(String objectName,
					Class<?> objectType, String typeQualifier) {
				// Create and return the task object type builder
				TaskObjectTypeBuilder<?> objectBuilder = this.taskTypeBuilder
						.addObject(objectType);
				objectBuilder.setLabel(objectName);
				if (typeQualifier != null) {
					objectBuilder.setTypeQualifier(typeQualifier);
				}
				return objectBuilder;
			}

			@Override
			@SuppressWarnings("unchecked")
			public <E extends Throwable> TaskEscalationTypeBuilder addEscalation(
					String escalationName, Class<E> escalationType) {
				// Create and return the task escalation type builder
				TaskEscalationTypeBuilder escalationBuilder = this.taskTypeBuilder
						.addEscalation(escalationType);
				escalationBuilder.setLabel(escalationName);
				return escalationBuilder;
			}
		}
	}

	/**
	 * Maker of {@link Governance}.
	 */
	protected static interface GovernanceMaker {

		/**
		 * Makes the {@link Governance}.
		 * 
		 * @param context
		 *            {@link GovernanceMakerContext}.
		 */
		void make(GovernanceMakerContext context);
	}

	/**
	 * Context for the {@link GovernanceMaker}.
	 */
	protected static interface GovernanceMakerContext {

		/**
		 * Specifies the extension interface.
		 * 
		 * @param extensionInterface
		 *            Extension interface.
		 */
		void setExtensionInterface(Class<?> extensionInterface);
	}

	/**
	 * Maker {@link GovernanceSource}.
	 */
	@TestSource
	public static class MakerGovernanceSource extends
			AbstractGovernanceSource<Object, Indexed> implements
			GovernanceMakerContext, GovernanceFactory<Object, Indexed> {

		/**
		 * Property name to obtain the {@link GovernanceMaker} identifier.
		 */
		private static final String MAKER_IDENTIFIER_PROPERTY_NAME = "governance.maker";

		/**
		 * {@link GovernanceMaker} instances by their identifiers.
		 */
		private static Map<String, GovernanceMaker> governanceMakers;

		/**
		 * Resets for the next test.
		 */
		public static void reset() {
			governanceMakers = new HashMap<String, GovernanceMaker>();
		}

		/**
		 * Registers a {@link GovernanceMaker}.
		 * 
		 * @param maker
		 *            {@link GovernanceMaker}.
		 * @return {@link PropertyList}.
		 */
		public static PropertyList register(GovernanceMaker maker) {

			// Ensure have a maker
			if (maker == null) {
				maker = new GovernanceMaker() {
					@Override
					public void make(GovernanceMakerContext context) {
						// Empty governance
					}
				};
			}

			// Register the governance maker
			String identifier = String.valueOf(governanceMakers.size());
			governanceMakers.put(identifier, maker);

			// Return the property list
			return new PropertyListImpl(MAKER_IDENTIFIER_PROPERTY_NAME,
					identifier);
		}

		/**
		 * {@link MetaDataContext}.
		 */
		@SuppressWarnings("rawtypes")
		private MetaDataContext metaDataContext;

		/*
		 * ================ AbstractAdministratorSource ========================
		 */

		@Override
		protected void loadSpecification(SpecificationContext context) {
			fail("Should not require specification");
		}

		@Override
		protected void loadMetaData(MetaDataContext<Object, Indexed> context)
				throws Exception {

			// Store details to load
			this.metaDataContext = context;

			// Provide the governance factory
			context.setGovernanceFactory(this);

			// Obtain the governance maker
			String identifier = context.getGovernanceSourceContext()
					.getProperty(MAKER_IDENTIFIER_PROPERTY_NAME);
			GovernanceMaker governanceMaker = governanceMakers.get(identifier);

			// Make the governance
			governanceMaker.make(this);
		}

		/*
		 * ================= GovernanceMakerContext =========================
		 */

		@Override
		@SuppressWarnings("unchecked")
		public void setExtensionInterface(Class<?> extensionInterface) {
			this.metaDataContext.setExtensionInterface(extensionInterface);
		}

		/*
		 * ===================== GovernanceFactory ===========================
		 */

		@Override
		public Governance<Object, Indexed> createGovernance() throws Throwable {
			TestCase.fail("Should not require to create Governance for compiling");
			return null;
		}
	}

	/**
	 * Maker of an {@link Administrator}.
	 */
	protected static interface AdministratorMaker {

		/**
		 * Makes the {@link Administrator}.
		 * 
		 * @param context
		 *            {@link AdministratorMakerContext}.
		 */
		void make(AdministratorMakerContext context);
	}

	/**
	 * Context for the {@link AdministratorMaker}.
	 */
	protected static interface AdministratorMakerContext {

		/**
		 * Specifies the extension interface.
		 * 
		 * @param extensionInterface
		 *            Extension interface.
		 */
		void setExtensionInterface(Class<?> extensionInterface);

		/**
		 * Adds a {@link Duty} for the {@link Administrator}.
		 * 
		 * @param dutyKey
		 *            Key identifying the {@link Duty}.
		 */
		void addDuty(Enum<?> dutyKey);

	}

	/**
	 * Maker {@link AdministratorSource}.
	 */
	@TestSource
	public static class MakerAdministratorSource extends
			AbstractAdministratorSource<Object, Indexed> implements
			AdministratorMakerContext {

		/**
		 * Property name to obtain the {@link AdministratorMaker} identifier.
		 */
		private static final String MAKER_IDENTIFIER_PROPERTY_NAME = "administrator.maker";

		/**
		 * {@link AdministratorMaker} instances by their identifiers.
		 */
		private static Map<String, AdministratorMaker> adminMakers;

		/**
		 * Resets for the next test.
		 */
		public static void reset() {
			adminMakers = new HashMap<String, AdministratorMaker>();
		}

		/**
		 * Registers a {@link AdministratorMaker}.
		 * 
		 * @param maker
		 *            {@link AdministratorMaker}.
		 * @return {@link PropertyList}.
		 */
		public static PropertyList register(AdministratorMaker maker) {

			// Ensure have a maker
			if (maker == null) {
				maker = new AdministratorMaker() {
					@Override
					public void make(AdministratorMakerContext context) {
						// Empty administrator
					}
				};
			}

			// Register the administrator maker
			String identifier = String.valueOf(adminMakers.size());
			adminMakers.put(identifier, maker);

			// Return the property list
			return new PropertyListImpl(MAKER_IDENTIFIER_PROPERTY_NAME,
					identifier);
		}

		/**
		 * {@link MetaDataContext}.
		 */
		@SuppressWarnings("rawtypes")
		private MetaDataContext metaDataContext;

		/*
		 * ================ AbstractAdministratorSource ========================
		 */

		@Override
		protected void loadSpecification(SpecificationContext context) {
			fail("Should not require specification");
		}

		@Override
		protected void loadMetaData(MetaDataContext<Object, Indexed> context)
				throws Exception {

			// Store details to load
			this.metaDataContext = context;

			// Obtain the administrator maker
			String identifier = context.getAdministratorSourceContext()
					.getProperty(MAKER_IDENTIFIER_PROPERTY_NAME);
			AdministratorMaker adminMaker = adminMakers.get(identifier);

			// Make the administrator
			adminMaker.make(this);
		}

		@Override
		public Administrator<Object, Indexed> createAdministrator() {
			fail("Should not require creating an administrator");
			return null;
		}

		/*
		 * ================= AdministratorMakerContext =========================
		 */

		@Override
		@SuppressWarnings("unchecked")
		public void setExtensionInterface(Class<?> extensionInterface) {
			this.metaDataContext.setExtensionInterface(extensionInterface);
		}

		@Override
		@SuppressWarnings("unchecked")
		public void addDuty(Enum<?> dutyKey) {
			this.metaDataContext.addDuty(dutyKey);
		}
	}

	/**
	 * Makes the {@link Team}.
	 */
	protected static interface TeamMaker {

		/**
		 * Makes the {@link Team}.
		 * 
		 * @param context
		 *            {@link TeamMakerContext}.
		 */
		void make(TeamMakerContext context);
	}

	/**
	 * Context for the {@link TeamMaker}.
	 */
	protected static interface TeamMakerContext {
	}

	/**
	 * Maker {@link TeamSource}.
	 */
	@TestSource
	public static class MakerTeamSource implements TeamSource,
			TeamMakerContext, Team {

		/**
		 * Property name to obtain the {@link TeamMaker} identifier.
		 */
		public static final String MAKER_IDENTIFIER_PROPERTY_NAME = "team.maker";

		/**
		 * {@link TeamMaker} instances by their identifiers.
		 */
		private static Map<String, TeamMaker> teamMakers;

		/**
		 * Resets for the next test.
		 */
		public static void reset() {
			teamMakers = new HashMap<String, TeamMaker>();
		}

		/**
		 * Registers a {@link TeamMaker}.
		 * 
		 * @param maker
		 *            {@link TeamMaker}.
		 * @return {@link PropertyList}.
		 */
		public static PropertyList register(TeamMaker maker) {

			// Ensure have a maker
			if (maker == null) {
				maker = new TeamMaker() {
					@Override
					public void make(TeamMakerContext context) {
						// Empty team
					}
				};
			}

			// Register the team maker
			String identifier = String.valueOf(teamMakers.size());
			teamMakers.put(identifier, maker);

			// Return the property list
			return new PropertyListImpl(MAKER_IDENTIFIER_PROPERTY_NAME,
					identifier);
		}

		/*
		 * ==================== TeamSource ================================
		 */

		@Override
		public TeamSourceSpecification getSpecification() {
			fail("Should not require specification");
			return null;
		}

		@Override
		public Team createTeam(TeamSourceContext context) throws Exception {

			// Obtain the team maker
			String identifier = context
					.getProperty(MAKER_IDENTIFIER_PROPERTY_NAME);
			TeamMaker teamMaker = teamMakers.get(identifier);

			// Make the team
			teamMaker.make(this);

			// Return the team
			return this;
		}

		/*
		 * ======================== Team ===================================
		 */

		@Override
		public void startWorking() {
			fail("Should not require the Team");
		}

		@Override
		public void assignJob(Job job, TeamIdentifier assignerTeam) {
			fail("Should not require the Team");
		}

		@Override
		public void stopWorking() {
			fail("Should not require the Team");
		}
	}

	/**
	 * Makes an {@link Office}.
	 */
	protected static interface OfficeMaker {

		/**
		 * Makes an {@link Office}.
		 * 
		 * @param context
		 *            {@link OfficeMakerContext}.
		 */
		void make(OfficeMakerContext context);
	}

	/**
	 * Context for the {@link OfficeMaker}.
	 */
	protected static interface OfficeMakerContext {

		/**
		 * Obtains the {@link OfficeArchitect}.
		 * 
		 * @return {@link OfficeArchitect}.
		 */
		OfficeArchitect getArchitect();

		/**
		 * Adds a {@link OfficeSection} to the {@link Office}.
		 * 
		 * @param sectionName
		 *            Name of the {@link OfficeSection}.
		 * @param sectionMaker
		 *            {@link SectionMaker}.
		 * @return Added {@link OfficeSection}.
		 */
		OfficeSection addSection(String sectionName, SectionMaker sectionMaker);
	}

	/**
	 * Maker {@link OfficeSource}.
	 */
	@TestSource
	public static class MakerOfficeSource implements OfficeSource,
			OfficeMakerContext {

		/**
		 * Property name to obtain the {@link OfficeMaker} identifier.
		 */
		private static final String MAKER_IDENTIFIER_PROPERTY_NAME = "office.maker";

		/**
		 * {@link OfficeMaker} instances by their identifiers.
		 */
		private static Map<String, OfficeMaker> officeMakers;

		/**
		 * {@link AbstractStructureTestCase}.
		 */
		private static AbstractStructureTestCase testCase;

		/**
		 * Resets for the next test.
		 * 
		 * @param testCase
		 *            {@link AbstractStructureTestCase}.
		 */
		public static void reset(AbstractStructureTestCase testCase) {
			officeMakers = new HashMap<String, OfficeMaker>();
			MakerOfficeSource.testCase = testCase;
		}

		/**
		 * Registers a {@link OfficeMaker}.
		 * 
		 * @param maker
		 *            {@link OfficeMaker}.
		 * @return {@link PropertyList}.
		 */
		public static PropertyList register(OfficeMaker maker) {

			// Ensure have a maker
			if (maker == null) {
				maker = new OfficeMaker() {
					@Override
					public void make(OfficeMakerContext context) {
						// Empty office
					}
				};
			}

			// Register the office maker
			String identifier = String.valueOf(officeMakers.size());
			officeMakers.put(identifier, maker);

			// Return the property list
			return new PropertyListImpl(MAKER_IDENTIFIER_PROPERTY_NAME,
					identifier);
		}

		/**
		 * {@link OfficeArchitect}.
		 */
		private OfficeArchitect architect;

		/*
		 * ===================== OfficeSource ================================
		 */

		@Override
		public OfficeSourceSpecification getSpecification() {
			fail("Should not require specification");
			return null;
		}

		@Override
		public void sourceOffice(OfficeArchitect officeArchitect,
				OfficeSourceContext context) throws Exception {

			// Store details to make available
			this.architect = officeArchitect;

			// Obtain the office maker
			String identifier = context
					.getProperty(MAKER_IDENTIFIER_PROPERTY_NAME);
			OfficeMaker officeMaker = officeMakers.get(identifier);

			// Make the office
			officeMaker.make(this);
		}

		/*
		 * ==================== OfficeMakerContext ==========================
		 */

		@Override
		public OfficeArchitect getArchitect() {
			return this.architect;
		}

		@Override
		public OfficeSection addSection(String sectionName,
				SectionMaker sectionMaker) {
			return testCase.addSection(this.architect, sectionName,
					sectionMaker);
		}
	}

	/**
	 * Makes the {@link OfficeFloor}.
	 */
	protected static interface OfficeFloorMaker {

		/**
		 * Makes the {@link OfficeFloor}.
		 * 
		 * @param context
		 *            {@link OfficeFloor}.
		 * @throws Exception
		 *             If fails to make the {@link OfficeFloor}.
		 */
		void make(OfficeFloorMakerContext context) throws Exception;
	}

	/**
	 * Context for the {@link OfficeFloorMaker}.
	 */
	protected static interface OfficeFloorMakerContext {

		/**
		 * Obtains the {@link OfficeFloorDeployer}.
		 * 
		 * @return {@link OfficeFloorDeployer}.
		 */
		OfficeFloorDeployer getDeployer();

		/**
		 * Obtains the {@link OfficeFloorSourceContext}.
		 * 
		 * @return {@link OfficeFloorSourceContext}.
		 */
		OfficeFloorSourceContext getContext();

		/**
		 * Adds an {@link Office}.
		 * 
		 * @param officeName
		 *            Name of the {@link Office}.
		 * @param officeMaker
		 *            {@link OfficeMaker}.
		 * @return Added {@link DeployedOffice}.
		 */
		DeployedOffice addOffice(String officeName, OfficeMaker officeMaker);

		/**
		 * Adds an {@link OfficeFloorTeam}.
		 * 
		 * @param teamName
		 *            Name of the {@link OfficeFloorTeam}.
		 * @param teamMaker
		 *            {@link TeamMaker}.
		 * @return Added {@link OfficeFloorTeam}.
		 */
		OfficeFloorTeam addTeam(String teamName, TeamMaker teamMaker);
	}

	/**
	 * Maker {@link OfficeFloorSource}.
	 */
	public static class MakerOfficeFloorSource implements OfficeFloorSource,
			OfficeFloorMakerContext {

		/**
		 * Property name to obtain the {@link OfficeFloorMaker} identifier.
		 */
		public static final String MAKER_IDENTIFIER_PROPERTY_NAME = "officefloor.maker";

		/**
		 * {@link OfficeFloorMaker} instances by their identifiers.
		 */
		private static Map<String, OfficeFloorMaker> officeFloorMakers;

		/**
		 * {@link AbstractStructureTestCase}.
		 */
		private static AbstractStructureTestCase testCase;

		/**
		 * Failure to instantiate this {@link MakerOfficeFloorSource}.
		 */
		public static RuntimeException instantiateFailure = null;

		/**
		 * Resets for the next test.
		 * 
		 * @param testCase
		 *            Current {@link AbstractStructureTestCase} running.
		 */
		public static void reset(AbstractStructureTestCase testCase) {
			officeFloorMakers = new HashMap<String, OfficeFloorMaker>();
			MakerOfficeFloorSource.testCase = testCase;
			instantiateFailure = null;
		}

		/**
		 * Registers a {@link OfficeFloorMaker}.
		 * 
		 * @param maker
		 *            {@link OfficeFloorMaker}.
		 * @return {@link PropertyList}.
		 */
		public static PropertyList register(OfficeFloorMaker maker) {

			// Ensure have a maker
			if (maker == null) {
				maker = new OfficeFloorMaker() {
					@Override
					public void make(OfficeFloorMakerContext context) {
						// Empty office floor
					}
				};
			}

			// Register the office floor maker
			String identifier = String.valueOf(officeFloorMakers.size());
			officeFloorMakers.put(identifier, maker);

			// Return the property list
			return new PropertyListImpl(MAKER_IDENTIFIER_PROPERTY_NAME,
					identifier);
		}

		/**
		 * {@link OfficeFloorDeployer}.
		 */
		private OfficeFloorDeployer deployer = null;

		/**
		 * {@link OfficeFloorSourceContext}.
		 */
		private OfficeFloorSourceContext context = null;

		/**
		 * Default constructor that may through instantiate failure if specified
		 * to do so.
		 */
		public MakerOfficeFloorSource() {
			if (instantiateFailure != null) {
				throw instantiateFailure;
			}
		}

		/*
		 * ===================== OfficeFloorSource ============================
		 */

		@Override
		public OfficeFloorSourceSpecification getSpecification() {
			fail("Should not require specification");
			return null;
		}

		@Override
		public void specifyConfigurationProperties(
				RequiredProperties requiredProperties,
				OfficeFloorSourceContext context) throws Exception {
			fail("Should not required to specify configuration properties");
		}

		@Override
		public void sourceOfficeFloor(OfficeFloorDeployer deployer,
				OfficeFloorSourceContext context) throws Exception {

			// Store details to make available
			this.deployer = deployer;
			this.context = context;

			// Obtain the office floor maker
			String identifier = context
					.getProperty(MAKER_IDENTIFIER_PROPERTY_NAME);
			OfficeFloorMaker officeFloorMaker = officeFloorMakers
					.get(identifier);

			// Make the office floor
			officeFloorMaker.make(this);
		}

		/*
		 * =================== OfficeFloorMakerContext ========================
		 */

		@Override
		public OfficeFloorDeployer getDeployer() {
			return this.deployer;
		}

		@Override
		public OfficeFloorSourceContext getContext() {
			return this.context;
		}

		@Override
		public DeployedOffice addOffice(String officeName,
				OfficeMaker officeMaker) {
			return testCase.addDeployedOffice(this.deployer, officeName,
					officeMaker);
		}

		@Override
		public OfficeFloorTeam addTeam(String teamName, TeamMaker teamMaker) {
			return testCase.addTeam(this.deployer, teamName, teamMaker);
		}
	}

}