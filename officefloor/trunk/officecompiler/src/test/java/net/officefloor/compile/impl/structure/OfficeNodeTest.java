/*
 *  Office Floor, Application Server
 *  Copyright (C) 2006 Daniel Sagenschneider
 *
 *  This program is free software; you can redistribute it and/or modify it under the terms 
 *  of the GNU General Public License as published by the Free Software Foundation; either 
 *  version 2 of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with this program; 
 *  if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, 
 *  MA 02111-1307 USA
 */
package net.officefloor.compile.impl.structure;

import java.sql.Connection;

import net.officefloor.compile.internal.structure.OfficeNode;
import net.officefloor.compile.issues.CompilerIssues.LocationType;
import net.officefloor.compile.spi.office.DependentManagedObject;
import net.officefloor.compile.spi.office.ManagedObjectTeam;
import net.officefloor.compile.spi.office.ObjectDependency;
import net.officefloor.compile.spi.office.OfficeAdministrator;
import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.compile.spi.office.OfficeDuty;
import net.officefloor.compile.spi.office.OfficeManagedObject;
import net.officefloor.compile.spi.office.OfficeManagedObjectSource;
import net.officefloor.compile.spi.office.OfficeObject;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.spi.office.OfficeSectionInput;
import net.officefloor.compile.spi.office.OfficeSectionManagedObject;
import net.officefloor.compile.spi.office.OfficeSectionManagedObjectSource;
import net.officefloor.compile.spi.office.OfficeSectionObject;
import net.officefloor.compile.spi.office.OfficeSectionOutput;
import net.officefloor.compile.spi.office.OfficeTask;
import net.officefloor.compile.spi.office.OfficeTeam;
import net.officefloor.compile.spi.office.TaskTeam;
import net.officefloor.compile.spi.office.source.OfficeSource;
import net.officefloor.compile.spi.officefloor.OfficeFloorDeployer;
import net.officefloor.compile.spi.officefloor.source.OfficeFloorSource;
import net.officefloor.compile.spi.section.ManagedObjectDependency;
import net.officefloor.compile.spi.section.ManagedObjectFlow;
import net.officefloor.compile.spi.section.SectionManagedObjectSource;
import net.officefloor.compile.spi.section.SectionObject;
import net.officefloor.compile.spi.section.TaskObject;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.build.TaskFactory;
import net.officefloor.frame.api.build.WorkFactory;
import net.officefloor.frame.api.build.OfficeFloorIssues.AssetType;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectTaskBuilder;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectWorkBuilder;

/**
 * Tests the {@link OfficeArchitect}.
 * 
 * @author Daniel
 */
public class OfficeNodeTest extends AbstractStructureTestCase {

	/**
	 * Name of the {@link Office}.
	 */
	private static final String OFFICE_NAME = "OFFICE";

	/**
	 * Location of the {@link Office} being built.
	 */
	private static final String OFFICE_LOCATION = "OFFICE_LOCATION";

	/**
	 * {@link OfficeNode} to be tested.
	 */
	private final OfficeNode node = new OfficeNodeImpl(OFFICE_NAME,
			MakerOfficeSource.class.getName(), OFFICE_LOCATION,
			this.nodeContext);

	/**
	 * Ensure allow {@link OfficeSource} to report issues via the
	 * {@link OfficeArchitect}.
	 */
	public void testAddIssue() {

		// Record adding the issue
		this.issues.addIssue(LocationType.OFFICE, OFFICE_LOCATION,
				AssetType.ADMINISTRATOR, "SOME ADMINISTRATOR", "TEST_ISSUE");

		// Add the issue
		this.replayMockObjects();
		this.node.addIssue("TEST_ISSUE", AssetType.ADMINISTRATOR,
				"SOME ADMINISTRATOR");
		this.verifyMockObjects();
	}

	/**
	 * Ensure allow {@link OfficeFloorSource} to report issues via the
	 * {@link OfficeFloorDeployer}.
	 */
	public void testAddIssueWithCause() {

		final Exception failure = new Exception("cause");

		// Record adding the issue
		this.issues.addIssue(LocationType.OFFICE, OFFICE_LOCATION,
				AssetType.ADMINISTRATOR, "SOME ADMINISTRATOR", "TEST_ISSUE",
				failure);

		// Add the issue
		this.replayMockObjects();
		this.node.addIssue("TEST_ISSUE", failure, AssetType.ADMINISTRATOR,
				"SOME ADMINISTRATOR");
		this.verifyMockObjects();
	}

	/**
	 * Tests adding an {@link OfficeObject}.
	 */
	public void testGetOfficeFloorManagedObject() {
		// Add two different managed objects verifying details
		this.replayMockObjects();
		OfficeObject mo = this.node.addOfficeObject("MO", Connection.class
				.getName());
		assertNotNull("Must have managed object", mo);
		assertEquals("Incorrect managed object name", "MO", mo
				.getOfficeObjectName());
		assertNotSame("Should obtain another managed object", mo, this.node
				.addOfficeObject("ANOTHER", String.class.getName()));
		this.verifyMockObjects();
	}

	/**
	 * Ensure issue if add the {@link OfficeObject} twice.
	 */
	public void testGetOfficeFloorManagedObjectTwice() {

		// Record issue in adding the managed object twice
		this.record_issue("Object MO already added");

		// Add the managed object twice
		this.replayMockObjects();
		OfficeObject moFirst = this.node.addOfficeObject("MO", Connection.class
				.getName());
		OfficeObject moSecond = this.node.addOfficeObject("MO", String.class
				.getName());
		this.verifyMockObjects();

		// Should be the managed object
		assertEquals("Should be same managed object on adding twice", moFirst,
				moSecond);
	}

	/**
	 * Tests adding an {@link OfficeTeam}.
	 */
	public void testGetOfficeTeam() {
		// Add two different teams verifying details
		this.replayMockObjects();
		OfficeTeam team = this.node.addOfficeTeam("TEAM");
		assertNotNull("Must have team", team);
		assertEquals("Incorrect team name", "TEAM", team.getOfficeTeamName());
		assertNotSame("Should obtain another team", team, this.node
				.addOfficeTeam("ANOTHER"));
		this.verifyMockObjects();
	}

	/**
	 * Ensure issue if add the {@link OfficeTeam} twice.
	 */
	public void testGetOfficeTeamTwice() {

		// Record issue in adding the teams twice
		this.record_issue("Team TEAM already added");

		// Add the team twice
		this.replayMockObjects();
		OfficeTeam teamFirst = this.node.addOfficeTeam("TEAM");
		OfficeTeam teamSecond = this.node.addOfficeTeam("TEAM");
		this.verifyMockObjects();

		// Should be the team
		assertEquals("Should be same managed object on adding twice",
				teamFirst, teamSecond);
	}

	/**
	 * Tests adding an {@link OfficeSection}.
	 */
	public void testAddSection() {
		// Add two different sections verifying details
		this.replayMockObjects();
		OfficeSection section = this.addSection(this.node, "SECTION", null);
		assertNotNull("Must have section", section);
		assertEquals("Incorrect section name", "SECTION", section
				.getOfficeSectionName());
		assertNotSame("Should obtain another section", section, this
				.addSection(this.node, "ANOTHER", null));
		this.verifyMockObjects();
	}

	/**
	 * Ensure issue if add the {@link OfficeSection} twice.
	 */
	public void testAddSectionTwice() {

		// Record issue in adding the section twice
		this.record_issue("Section SECTION already added");

		// Add the section twice
		this.replayMockObjects();
		OfficeSection sectionFirst = this
				.addSection(this.node, "SECTION", null);
		OfficeSection sectionSecond = this.addSection(this.node, "SECTION",
				null);
		this.verifyMockObjects();

		// Should be the same section
		assertEquals("Should be same section on adding twice", sectionFirst,
				sectionSecond);
	}

	/**
	 * Tests adding a {@link OfficeManagedObjectSource}.
	 */
	public void testAddOfficeManagedObjectSource() {
		// Add two different managed objects verifying details
		this.replayMockObjects();
		OfficeManagedObjectSource moSource = this.addManagedObjectSource(
				this.node, "MO", null);
		assertNotNull("Must have managed object", moSource);
		assertEquals("Incorrect managed object name", "MO", moSource
				.getOfficeManagedObjectSourceName());
		assertNotSame("Should obtain another managed object", moSource, this
				.addManagedObjectSource(this.node, "ANOTHER", null));
		this.verifyMockObjects();
	}

	/**
	 * Ensure issue if add the {@link OfficeManagedObjectSource} twice.
	 */
	public void testAddOfficeManagedObjectSourceTwice() {

		// Record issue in adding the managed object sources twice
		this.record_issue("Managed object source MO already added");

		// Add the managed object source twice
		this.replayMockObjects();
		OfficeManagedObjectSource moSourceFirst = this.addManagedObjectSource(
				this.node, "MO", null);
		OfficeManagedObjectSource moSourceSecond = this.addManagedObjectSource(
				this.node, "MO", null);
		this.verifyMockObjects();

		// Should be the same managed object source
		assertEquals("Should be same managed object on adding twice",
				moSourceFirst, moSourceSecond);
	}

	/**
	 * Tests adding an {@link OfficeManagedObject}.
	 */
	public void testAddOfficeManagedObject() {

		OfficeManagedObjectSource moSource = this.addManagedObjectSource(
				this.node, "MO", null);

		// Add two different managed objects verifying details
		this.replayMockObjects();
		OfficeManagedObject mo = moSource.addOfficeManagedObject("MO",
				ManagedObjectScope.PROCESS);
		assertNotNull("Must have managed object", mo);
		assertEquals("Incorrect managed object name", "MO", mo
				.getOfficeManagedObjectName());
		assertNotSame("Should obtain another managed object", mo, moSource
				.addOfficeManagedObject("ANOTHER", ManagedObjectScope.PROCESS));
		this.verifyMockObjects();
	}

	/**
	 * Ensure issue if add the {@link OfficeManagedObject} twice.
	 */
	public void testAddOfficeManagedObjectTwice() {

		// Record issue in adding the managed objects twice
		this.issues.addIssue(LocationType.OFFICE, OFFICE_LOCATION,
				AssetType.MANAGED_OBJECT, "MO",
				"Office managed object MO already added");

		OfficeManagedObjectSource moSource = this.addManagedObjectSource(
				this.node, "MO_SOURCE", null);

		// Add the managed object twice
		this.replayMockObjects();
		OfficeManagedObject moFirst = moSource.addOfficeManagedObject("MO",
				ManagedObjectScope.THREAD);
		OfficeManagedObject moSecond = moSource.addOfficeManagedObject("MO",
				ManagedObjectScope.THREAD);
		this.verifyMockObjects();

		// Should be the same managed object
		assertEquals("Should be same managed object on adding twice", moFirst,
				moSecond);
	}

	/**
	 * Ensure issue if add the {@link OfficeManagedObject} twice by different
	 * {@link OfficeManagedObjectSource} instances.
	 */
	public void testAddOfficeManagedObjectTwiceByDifferentSources() {

		// Record issue in adding the managed objects twice
		this.issues.addIssue(LocationType.OFFICE, OFFICE_LOCATION,
				AssetType.MANAGED_OBJECT, "MO",
				"Office managed object MO already added");

		OfficeManagedObjectSource moSourceOne = this.addManagedObjectSource(
				this.node, "MO_SOURCE_ONE", null);
		OfficeManagedObjectSource moSourceTwo = this.addManagedObjectSource(
				this.node, "MO_SOURCE_TWO", null);

		// Add the managed object twice by different sources
		this.replayMockObjects();
		OfficeManagedObject moFirst = moSourceOne.addOfficeManagedObject("MO",
				ManagedObjectScope.THREAD);
		OfficeManagedObject moSecond = moSourceTwo.addOfficeManagedObject("MO",
				ManagedObjectScope.THREAD);
		this.verifyMockObjects();

		// Should be the same managed object
		assertEquals("Should be same managed object on adding twice", moFirst,
				moSecond);
	}

	/**
	 * Tests adding an {@link OfficeAdministrator}.
	 */
	public void testAddOfficeAdministrator() {
		// Add two different administrators verifying details
		this.replayMockObjects();
		OfficeAdministrator admin = this.addAdministrator(this.node, "ADMIN",
				null);
		assertNotNull("Must have administrator", admin);
		assertEquals("Incorrect administrator name", "ADMIN", admin
				.getOfficeAdministratorName());
		assertNotSame("Should obtain another administrator", admin, this
				.addAdministrator(this.node, "ANOTHER", null));
		this.verifyMockObjects();
	}

	/**
	 * Ensure issue if add the {@link OfficeManagedObject} twice.
	 */
	public void testAddOfficeAdministratorTwice() {

		// Record issue in adding the administrator twice
		this.record_issue("Administrator ADMIN already added");

		// Add the administrator twice
		this.replayMockObjects();
		OfficeAdministrator adminFirst = this.addAdministrator(this.node,
				"ADMIN", null);
		OfficeAdministrator adminSecond = this.addAdministrator(this.node,
				"ADMIN", null);
		this.verifyMockObjects();

		// Should be the same administrator
		assertEquals("Should be same administrator on adding twice",
				adminFirst, adminSecond);
	}

	/**
	 * Ensure can link the {@link TaskTeam} to an {@link OfficeTeam}.
	 */
	public void testLinkTaskTeamToOfficeTeam() {

		final WorkFactory<Work> workFactory = this.createMockWorkFactory();
		final TaskFactory<Work, ?, ?> taskFactory = this
				.createMockTaskFactory();

		// Record already being linked
		this.record_issue("Team for task TASK already assigned");

		this.replayMockObjects();

		// Add section with task
		OfficeSection section = this.addSection(this.node, "SECTION",
				new SectionMaker() {
					@Override
					public void make(SectionMakerContext context) {
						context.addTask("WORK", workFactory, "TASK",
								taskFactory, null);
					}
				});
		assertEquals("Incorrect number of section tasks", 1, section
				.getOfficeTasks().length);
		OfficeTask task = section.getOfficeTasks()[0];
		assertEquals("Incorrect office task", "TASK", task.getOfficeTaskName());

		// Link
		TaskTeam team = task.getTeamResponsible();
		OfficeTeam officeTeam = this.node.addOfficeTeam("TEAM");
		this.node.link(team, officeTeam);
		assertTeamLink("task team -> office team", team, officeTeam);

		// Ensure only can link once
		this.node.link(team, this.node.addOfficeTeam("ANOTHER"));
		assertTeamLink("Can only link once", team, officeTeam);

		this.verifyMockObjects();
	}

	/**
	 * Ensure can specify pre {@link OfficeDuty} for the {@link OfficeTask}.
	 */
	public void testLinkPreOfficeDutyForOfficeTask() {

		final WorkFactory<Work> workFactory = this.createMockWorkFactory();
		final TaskFactory<Work, ?, ?> taskFactory = this
				.createMockTaskFactory();

		this.replayMockObjects();

		// Add section with task
		OfficeSection section = this.addSection(this.node, "SECTION",
				new SectionMaker() {
					@Override
					public void make(SectionMakerContext context) {
						context.addTask("WORK", workFactory, "TASK",
								taskFactory, null);
					}
				});
		OfficeTask task = section.getOfficeTasks()[0];

		// Link
		OfficeAdministrator administrator = this.addAdministrator(this.node,
				"ADMINISTRATOR", null);
		assertEquals("Incorrect administrator name", "ADMINISTRATOR",
				administrator.getOfficeAdministratorName());
		OfficeDuty duty = administrator.getDuty("DUTY");
		assertEquals("Incorrect duty name", "DUTY", duty.getOfficeDutyName());
		task.addPreTaskDuty(duty);
		// TODO test that pre duty specified

		// May have many pre duties
		task.addPreTaskDuty(administrator.getDuty("ANOTHER"));

		this.verifyMockObjects();
	}

	/**
	 * Ensure can specify post {@link OfficeDuty} for the {@link OfficeTask}.
	 */
	public void testLinkPostOfficeDutyForOfficeTask() {

		final WorkFactory<Work> workFactory = this.createMockWorkFactory();
		final TaskFactory<Work, ?, ?> taskFactory = this
				.createMockTaskFactory();

		this.replayMockObjects();

		// Add section with task
		OfficeSection section = this.addSection(this.node, "SECTION",
				new SectionMaker() {
					@Override
					public void make(SectionMakerContext context) {
						context.addTask("WORK", workFactory, "TASK",
								taskFactory, null);
					}
				});
		OfficeTask task = section.getOfficeTasks()[0];

		// Link
		OfficeAdministrator administrator = this.addAdministrator(this.node,
				"ADMINISTRATOR", null);
		OfficeDuty duty = administrator.getDuty("DUTY");
		task.addPostTaskDuty(duty);
		// TODO test that post duty specified

		// May have many post duties
		task.addPostTaskDuty(administrator.getDuty("ANOTHER"));

		this.verifyMockObjects();
	}

	/**
	 * Ensure can administer {@link OfficeSectionManagedObject}.
	 */
	public void testAdministerOfficeSectionManagedObject() {
		this.replayMockObjects();

		// Add section with managed object
		OfficeSection section = this.addSection(this.node, "SECTION",
				new SectionMaker() {
					@Override
					public void make(SectionMakerContext context) {
						SectionManagedObjectSource moSource = context
								.addManagedObjectSource("MO_SOURCE", null);
						moSource.addSectionManagedObject("MO",
								ManagedObjectScope.WORK);
					}
				});
		OfficeSectionManagedObject mo = section
				.getOfficeSectionManagedObjectSources()[0]
				.getOfficeSectionManagedObjects()[0];

		// Link
		OfficeAdministrator administrator = this.addAdministrator(this.node,
				"ADMINISTRATOR", null);
		administrator.administerManagedObject(mo);
		// TODO test that administering the section managed object

		// Should be able to administer the managed object twice
		administrator.administerManagedObject(mo);

		this.verifyMockObjects();
	}

	/**
	 * Ensure can administer {@link OfficeManagedObject}.
	 */
	public void testAdministerOfficeManagedObject() {
		this.replayMockObjects();

		// Link
		OfficeAdministrator administrator = this.addAdministrator(this.node,
				"ADMINISTRATOR", null);
		OfficeManagedObjectSource moSource = this.addManagedObjectSource(
				this.node, "MO_SOURCE", null);
		OfficeManagedObject mo = moSource.addOfficeManagedObject("MO",
				ManagedObjectScope.THREAD);
		administrator.administerManagedObject(mo);
		// TODO test that administering the section managed object

		// Should be able to administer the managed object twice
		administrator.administerManagedObject(mo);

		this.verifyMockObjects();
	}

	/**
	 * Ensure can administer {@link OfficeObject}.
	 */
	public void testAdministerOfficeObject() {
		this.replayMockObjects();

		// Link
		OfficeAdministrator administrator = this.addAdministrator(this.node,
				"ADMINISTRATOR", null);
		OfficeObject mo = this.node.addOfficeObject("MO", Connection.class
				.getName());
		administrator.administerManagedObject(mo);
		// TODO test that administering the section managed object

		// Should be able to administer the managed object twice
		administrator.administerManagedObject(mo);

		this.verifyMockObjects();
	}

	/**
	 * Ensure can link {@link OfficeSectionObject} to the
	 * {@link OfficeManagedObject}.
	 */
	public void testLinkOfficeSectionObjectToOfficeManagedObject() {

		// Record already being linked
		this
				.record_issue("Office section object SECTION_OBJECT linked more than once");

		this.replayMockObjects();

		// Add section with section object
		OfficeSection section = this.addSection(this.node, "SECTION",
				new SectionMaker() {
					@Override
					public void make(SectionMakerContext context) {
						context.getBuilder().addSectionObject("SECTION_OBJECT",
								Connection.class.getName());
					}
				});
		assertEquals("Incorrect number of section objects", 1, section
				.getOfficeSectionObjects().length);
		OfficeSectionObject sectionObject = section.getOfficeSectionObjects()[0];
		assertEquals("Incorrect section object", "SECTION_OBJECT",
				sectionObject.getOfficeSectionObjectName());

		// Link
		OfficeManagedObjectSource moSource = this.addManagedObjectSource(
				this.node, "MO_SOURCE", null);
		OfficeManagedObject mo = moSource.addOfficeManagedObject("MO",
				ManagedObjectScope.PROCESS);
		this.node.link(sectionObject, mo);
		assertObjectLink("section object -> office managed object",
				sectionObject, mo);

		// Ensure only can link once
		this.node.link(sectionObject, moSource.addOfficeManagedObject(
				"ANOTHER", ManagedObjectScope.PROCESS));
		assertObjectLink("Can only link once", sectionObject, mo);

		this.verifyMockObjects();
	}

	/**
	 * Ensure can link {@link OfficeSectionObject} to the {@link OfficeObject}.
	 */
	public void testLinkOfficeSectionObjectToOfficeObject() {

		// Record already being linked
		this
				.record_issue("Office section object SECTION_OBJECT linked more than once");

		this.replayMockObjects();

		// Add section with section object
		OfficeSection section = this.addSection(this.node, "SECTION",
				new SectionMaker() {
					@Override
					public void make(SectionMakerContext context) {
						context.getBuilder().addSectionObject("SECTION_OBJECT",
								Connection.class.getName());
					}
				});
		OfficeSectionObject sectionObject = section.getOfficeSectionObjects()[0];

		// Link
		OfficeObject mo = this.node.addOfficeObject("MO", Connection.class
				.getName());
		this.node.link(sectionObject, mo);
		assertObjectLink("section object -> office floor managed object",
				sectionObject, mo);

		// Ensure only can link once
		this.node.link(sectionObject, this.node.addOfficeObject("ANOTHER",
				Connection.class.getName()));
		assertObjectLink("Can only link once", sectionObject, mo);

		this.verifyMockObjects();
	}

	/**
	 * Ensure can have {@link ObjectDependency} linked to an
	 * {@link OfficeManagedObject}.
	 */
	public void testLinkObjectDependencyToOfficeManagedObject() {

		final WorkFactory<Work> workFactory = this.createMockWorkFactory();
		final TaskFactory<Work, ?, ?> taskFactory = this
				.createMockTaskFactory();

		// Record not linked on first attempt to retrieve dependent
		this
				.record_issue("TaskObject OBJECT is not linked to a DependentManagedObject");

		this.replayMockObjects();

		// Add section with section object
		OfficeSection section = this.addSection(this.node, "SECTION",
				new SectionMaker() {
					@Override
					public void make(SectionMakerContext context) {
						// Add managed object and link to section output
						TaskObject object = context.addTaskObject("WORK",
								workFactory, "TASK", taskFactory, "OBJECT",
								Connection.class);
						SectionObject sectionObject = context.getBuilder()
								.addSectionObject("SECTION_OBJECT",
										Connection.class.getName());
						context.getBuilder().link(object, sectionObject);
					}
				});
		OfficeSectionObject sectionObject = section.getOfficeSectionObjects()[0];

		// Obtain the object dependency and ensure not linked at this stage
		ObjectDependency dependency = section.getOfficeTasks()[0]
				.getObjectDependencies()[0];
		assertEquals("Incorrect dependency", "OBJECT", dependency
				.getObjectDependencyName());
		DependentManagedObject dependent = dependency
				.getDependentManagedObject();
		assertNull("Should not yet be linked to a managed object", dependent);

		// Link section object to an office managed object
		OfficeManagedObjectSource moSource = this.addManagedObjectSource(
				this.node, "MO_SOURCE", null);
		OfficeManagedObject mo = moSource.addOfficeManagedObject("MO",
				ManagedObjectScope.WORK);
		this.node.link(sectionObject, mo);

		// Ensure task object linked to office managed object
		assertEquals("Incorrect linked managed object", mo, dependency
				.getDependentManagedObject());

		this.verifyMockObjects();
	}

	/**
	 * Ensure can have {@link ObjectDependency} linked to an
	 * {@link OfficeObject}.
	 */
	public void testLinkObjectDependencyToOfficeFloorManagedObject() {

		final WorkFactory<Work> workFactory = this.createMockWorkFactory();
		final TaskFactory<Work, ?, ?> taskFactory = this
				.createMockTaskFactory();

		// Record not linked on first attempt to retrieve dependent
		this
				.record_issue("TaskObject OBJECT is not linked to a DependentManagedObject");

		this.replayMockObjects();

		// Add section with section object
		OfficeSection section = this.addSection(this.node, "SECTION",
				new SectionMaker() {
					@Override
					public void make(SectionMakerContext context) {
						// Add managed object and link to section output
						TaskObject object = context.addTaskObject("WORK",
								workFactory, "TASK", taskFactory, "OBJECT",
								Connection.class);
						SectionObject sectionObject = context.getBuilder()
								.addSectionObject("SECTION_OBJECT",
										Connection.class.getName());
						context.getBuilder().link(object, sectionObject);
					}
				});
		OfficeSectionObject sectionObject = section.getOfficeSectionObjects()[0];

		// Obtain the object dependency and ensure not linked at this stage
		ObjectDependency dependency = section.getOfficeTasks()[0]
				.getObjectDependencies()[0];
		assertEquals("Incorrect dependency", "OBJECT", dependency
				.getObjectDependencyName());
		assertNull("Should not yet be linked to managed object", dependency
				.getDependentManagedObject());

		// Link section object to an office floor managed object
		OfficeObject mo = this.node.addOfficeObject("MO", Connection.class
				.getName());
		this.node.link(sectionObject, mo);

		// Ensure task object linked to office floor managed object
		assertEquals("Incorrect linked managed object", mo, dependency
				.getDependentManagedObject());

		this.verifyMockObjects();
	}

	/**
	 * Ensures can link {@link ManagedObjectDependency} to the
	 * {@link OfficeManagedObject}.
	 */
	public void testLinkManagedObjectDependencyToOfficeManagedObject() {

		// Record already being linked
		this
				.record_issue("Managed object dependency DEPENDENCY linked more than once");

		this.replayMockObjects();

		// Link
		OfficeManagedObjectSource moSource = this.addManagedObjectSource(
				this.node, "MO_SOURCE", null);
		OfficeManagedObject mo = moSource.addOfficeManagedObject("MO",
				ManagedObjectScope.THREAD);
		ManagedObjectDependency dependency = mo
				.getManagedObjectDependency("DEPENDENCY");
		OfficeManagedObjectSource moSourceTarget = this.addManagedObjectSource(
				this.node, "MO_SOURCE_TARGET", null);
		OfficeManagedObject moTarget = moSourceTarget.addOfficeManagedObject(
				"MO_TARGET", ManagedObjectScope.THREAD);
		this.node.link(dependency, moTarget);
		assertObjectLink("managed object dependency -> office managed object",
				dependency, moTarget);

		// Ensure only can link once
		this.node.link(dependency, moSourceTarget.addOfficeManagedObject(
				"ANOTHER", ManagedObjectScope.THREAD));
		assertObjectLink("Can only link once", dependency, moTarget);

		this.verifyMockObjects();
	}

	/**
	 * Ensures can link {@link OfficeManagedObjectSource}
	 * {@link ManagedObjectTeam} to the {@link OfficeTeam}.
	 */
	public void testLinkOfficeManagedObjectSourceTeamToOfficeTeam() {

		// Record already being linked
		this.record_issue("TEAM already assigned");

		this.replayMockObjects();

		// Link
		OfficeManagedObjectSource moSource = this.addManagedObjectSource(
				this.node, "MO", null);
		ManagedObjectTeam team = moSource.getManagedObjectTeam("TEAM");
		OfficeTeam officeTeam = this.node.addOfficeTeam("OFFICE_TEAM");
		this.node.link(team, officeTeam);
		assertTeamLink("managed object team -> office team", team, officeTeam);

		// Ensure only can link once
		this.node.link(team, this.node.addOfficeTeam("ANOTHER"));
		assertTeamLink("Can only link once", team, officeTeam);

		this.verifyMockObjects();
	}

	/**
	 * Ensures can link {@link OfficeSectionManagedObjectSource}
	 * {@link ManagedObjectTeam} to the {@link OfficeTeam}.
	 */
	@SuppressWarnings("unchecked")
	public void testLinkOfficeSectionManagedObjectSourceTeamToOfficeTeam() {

		final WorkFactory<Work> workFactory = this
				.createMock(WorkFactory.class);
		final TaskFactory<Work, None, None> taskFactory = this
				.createMock(TaskFactory.class);

		// Record already being linked
		this.record_issue("TEAM already assigned");

		this.replayMockObjects();

		// Add section with managed object requiring a team
		OfficeSection section = this.addSection(this.node, "SECTION",
				new SectionMaker() {
					@Override
					public void make(SectionMakerContext context) {
						context.addManagedObjectSource("MO_SOURCE",
								new ManagedObjectMaker() {
									@Override
									public void make(
											ManagedObjectMakerContext context) {
										ManagedObjectWorkBuilder<Work> work = context
												.getContext()
												.getManagedObjectSourceContext()
												.addWork("WORK", workFactory);
										ManagedObjectTaskBuilder<None, None> task = work
												.addTask("TASK", taskFactory);
										task.setTeam("TEAM");
									}
								});
					}
				});
		assertEquals("Incorrect number of section managed object sources", 1,
				section.getOfficeSectionManagedObjectSources().length);
		OfficeSectionManagedObjectSource moSource = section
				.getOfficeSectionManagedObjectSources()[0];
		assertEquals("Incorrect section managed object source", "MO_SOURCE",
				moSource.getOfficeSectionManagedObjectSourceName());
		assertEquals("Incorrect number of section managed object source teams",
				1, moSource.getOfficeSectionManagedObjectTeams().length);
		ManagedObjectTeam team = moSource.getOfficeSectionManagedObjectTeams()[0];
		assertEquals("Incorrect section managed object source team", "TEAM",
				team.getManagedObjectTeamName());

		// Link
		OfficeTeam officeTeam = this.node.addOfficeTeam("OFFICE_TEAM");
		this.node.link(team, officeTeam);
		assertTeamLink("managed object source team -> office team", team,
				officeTeam);

		// Ensure only can link once
		this.node.link(team, this.node.addOfficeTeam("ANOTHER"));
		assertTeamLink("Can only link once", team, officeTeam);

		this.verifyMockObjects();
	}

	/**
	 * Ensures can link {@link ManagedObjectDependency} to the
	 * {@link OfficeObject}.
	 */
	public void testLinkManagedObjectDependencyToOfficeObject() {

		// Record already being linked
		this
				.record_issue("Managed object dependency DEPENDENCY linked more than once");

		this.replayMockObjects();

		// Link
		OfficeManagedObjectSource moSource = this.addManagedObjectSource(
				this.node, "MO_SOURCE", null);
		OfficeManagedObject mo = moSource.addOfficeManagedObject("MO",
				ManagedObjectScope.WORK);
		ManagedObjectDependency dependency = mo
				.getManagedObjectDependency("DEPENDENCY");
		OfficeObject moTarget = this.node.addOfficeObject("MO_TARGET",
				Connection.class.getName());
		this.node.link(dependency, moTarget);
		assertObjectLink(
				"managed object dependency -> office floor managed object",
				dependency, moTarget);

		// Ensure only can link once
		this.node.link(dependency, this.node.addOfficeObject("ANOTHER",
				String.class.getName()));
		assertObjectLink("Can only link once", dependency, moTarget);

		this.verifyMockObjects();
	}

	/**
	 * Ensures can link {@link ManagedObjectFlow} to the
	 * {@link OfficeSectionInput}.
	 */
	public void testLinkManagedObjectFlowToOfficeSectionInput() {

		// Record already being linked
		this
				.record_issue("Managed object source flow FLOW linked more than once");

		this.replayMockObjects();

		// Add section with section inputs
		OfficeSection section = this.addSection(this.node, "SECTION",
				new SectionMaker() {
					@Override
					public void make(SectionMakerContext context) {
						context.getBuilder().addSectionInput("SECTION_INPUT",
								Object.class.getName());
						context.getBuilder().addSectionInput("ANOTHER",
								String.class.getName());
					}
				});
		// Obtain section input (should be ordered)
		OfficeSectionInput sectionInput = section.getOfficeSectionInputs()[1];
		assertEquals("Incorrect office section input", "SECTION_INPUT",
				sectionInput.getOfficeSectionInputName());

		// Link
		OfficeManagedObjectSource moSource = this.addManagedObjectSource(
				this.node, "MO", null);
		ManagedObjectFlow flow = moSource.getManagedObjectFlow("FLOW");
		this.node.link(flow, sectionInput);
		assertFlowLink("managed object source flow -> section input", flow,
				sectionInput);

		// Ensure only can link once
		this.node.link(flow, section.getOfficeSectionInputs()[0]); // ordered
		assertFlowLink("Can only link once", flow, sectionInput);

		this.verifyMockObjects();
	}

	/**
	 * Ensures can link {@link OfficeSectionOutput} to the
	 * {@link OfficeSectionInput}.
	 */
	public void testLinkOfficeSectionOutputToOfficeSectionInput() {

		// Record already being linked
		this
				.record_issue("Office section output SECTION_OUTPUT linked more than once");

		this.replayMockObjects();

		// Add section with section outputs
		OfficeSection outputSection = this.addSection(this.node,
				"OUTPUT_SECTION", new SectionMaker() {
					@Override
					public void make(SectionMakerContext context) {
						context.getBuilder().addSectionOutput("SECTION_OUTPUT",
								Object.class.getName(), false);
					}
				});
		OfficeSectionOutput sectionOutput = outputSection
				.getOfficeSectionOutputs()[0];
		assertEquals("Incorrect office section input", "SECTION_OUTPUT",
				sectionOutput.getOfficeSectionOutputName());

		// Add section with section inputs
		OfficeSection inputSection = this.addSection(this.node,
				"INPUT_SECTION", new SectionMaker() {
					@Override
					public void make(SectionMakerContext context) {
						context.getBuilder().addSectionInput("SECTION_INPUT",
								Object.class.getName());
						context.getBuilder().addSectionInput("ANOTHER",
								String.class.getName());
					}
				});
		// Should be ordered (so will be second input)
		OfficeSectionInput sectionInput = inputSection.getOfficeSectionInputs()[1];
		assertEquals("Incorrect office section input", "SECTION_INPUT",
				sectionInput.getOfficeSectionInputName());

		// Link
		this.node.link(sectionOutput, sectionInput);
		assertFlowLink("section output -> section input", sectionOutput,
				sectionInput);

		// Ensure only can link once
		this.node.link(sectionOutput, inputSection.getOfficeSectionInputs()[0]); // ordered
		assertFlowLink("Can only link once", sectionOutput, sectionInput);

		this.verifyMockObjects();
	}

	/**
	 * Records adding an issue.
	 * 
	 * @param issueDescription
	 *            Description of the issue.
	 */
	private void record_issue(String issueDescription) {
		this.issues.addIssue(LocationType.OFFICE, OFFICE_LOCATION, null, null,
				issueDescription);
	}

}