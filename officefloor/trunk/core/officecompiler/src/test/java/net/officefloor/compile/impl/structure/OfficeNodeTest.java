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

import java.sql.Connection;

import net.officefloor.compile.internal.structure.OfficeFloorNode;
import net.officefloor.compile.internal.structure.OfficeNode;
import net.officefloor.compile.spi.office.ManagedObjectTeam;
import net.officefloor.compile.spi.office.OfficeAdministrator;
import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.compile.spi.office.OfficeDuty;
import net.officefloor.compile.spi.office.OfficeEscalation;
import net.officefloor.compile.spi.office.OfficeGovernance;
import net.officefloor.compile.spi.office.OfficeInput;
import net.officefloor.compile.spi.office.OfficeManagedObject;
import net.officefloor.compile.spi.office.OfficeManagedObjectSource;
import net.officefloor.compile.spi.office.OfficeObject;
import net.officefloor.compile.spi.office.OfficeOutput;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.spi.office.OfficeSectionInput;
import net.officefloor.compile.spi.office.OfficeSectionManagedObject;
import net.officefloor.compile.spi.office.OfficeSectionManagedObjectSource;
import net.officefloor.compile.spi.office.OfficeSectionObject;
import net.officefloor.compile.spi.office.OfficeSectionOutput;
import net.officefloor.compile.spi.office.OfficeStart;
import net.officefloor.compile.spi.office.OfficeSubSection;
import net.officefloor.compile.spi.office.OfficeSectionTask;
import net.officefloor.compile.spi.office.OfficeTeam;
import net.officefloor.compile.spi.office.TaskTeam;
import net.officefloor.compile.spi.office.source.OfficeSource;
import net.officefloor.compile.spi.officefloor.OfficeFloorDeployer;
import net.officefloor.compile.spi.officefloor.source.OfficeFloorSource;
import net.officefloor.compile.spi.section.ManagedObjectDependency;
import net.officefloor.compile.spi.section.ManagedObjectFlow;
import net.officefloor.compile.spi.section.SectionInput;
import net.officefloor.compile.spi.section.SectionOutput;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.build.TaskFactory;
import net.officefloor.frame.api.build.WorkFactory;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectTaskBuilder;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectWorkBuilder;
import net.officefloor.model.impl.section.SectionModelSectionSource;
import net.officefloor.plugin.administrator.clazz.ClassAdministratorSource;
import net.officefloor.plugin.governance.clazz.ClassGovernanceSource;
import net.officefloor.plugin.managedobject.clazz.ClassManagedObjectSource;

/**
 * Tests the {@link OfficeArchitect}.
 * 
 * @author Daniel Sagenschneider
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
	 * Mock {@link OfficeFloorNode}.
	 */
	private final OfficeFloorNode officeFloor = this
			.createMock(OfficeFloorNode.class);

	/**
	 * {@link OfficeNode} to be tested.
	 */
	private final OfficeNode node = new OfficeNodeImpl(OFFICE_NAME,
			this.officeFloor, this.nodeContext);

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		// Initialise the office
		this.node.initialise(MakerOfficeSource.class.getName(), null,
				OFFICE_LOCATION);
	}

	/**
	 * Ensure allow {@link OfficeSource} to report issues via the
	 * {@link OfficeArchitect}.
	 */
	public void testAddIssue() {

		// Record adding the issue
		this.issues
				.recordIssue(OFFICE_NAME, OfficeNodeImpl.class, "TEST_ISSUE");

		// Add the issue
		this.replayMockObjects();
		this.node.addIssue("TEST_ISSUE");
		this.verifyMockObjects();
	}

	/**
	 * Ensure allow {@link OfficeFloorSource} to report issues via the
	 * {@link OfficeFloorDeployer}.
	 */
	public void testAddIssueWithCause() {

		final Exception failure = new Exception("cause");

		// Record adding the issue
		this.issues.recordIssue(OFFICE_NAME, OfficeNodeImpl.class,
				"TEST_ISSUE", failure);

		// Add the issue
		this.replayMockObjects();
		this.node.addIssue("TEST_ISSUE", failure);
		this.verifyMockObjects();
	}

	/**
	 * Tests adding an {@link OfficeObject}.
	 */
	public void testGetOfficeFloorObject() {
		// Add two different managed objects verifying details
		this.replayMockObjects();
		OfficeObject mo = this.node.addOfficeObject("MO",
				Connection.class.getName());
		assertNotNull("Must have managed object", mo);
		assertEquals("Incorrect managed object name", "MO",
				mo.getOfficeObjectName());
		assertNotSame("Should obtain another managed object", mo,
				this.node.addOfficeObject("ANOTHER", String.class.getName()));
		this.verifyMockObjects();
	}

	/**
	 * Ensure issue if add the {@link OfficeObject} twice.
	 */
	public void testGetOfficeFloorObjectTwice() {

		// Record issue in adding the managed object twice
		this.issues.recordIssue("MO", OfficeObjectNodeImpl.class,
				"Office Object MO already added");

		// Add the managed object twice
		this.replayMockObjects();
		OfficeObject moFirst = this.node.addOfficeObject("MO",
				Connection.class.getName());
		OfficeObject moSecond = this.node.addOfficeObject("MO",
				String.class.getName());
		this.verifyMockObjects();

		// Should be the managed object
		assertEquals("Should be same managed object on adding twice", moFirst,
				moSecond);
	}

	/**
	 * Tests adding an {@link OfficeInput}.
	 */
	public void testAddInput() {
		// Add two different inputs verifying details
		this.replayMockObjects();
		OfficeInput input = this.node.addOfficeInput("INPUT",
				String.class.getName());
		assertNotNull("Must have input", input);
		assertEquals("Incorrect input name", "INPUT",
				input.getOfficeInputName());
		assertNotSame("Should obtain another input", input,
				this.node.addOfficeInput("ANOTHER", Integer.class.getName()));
		this.verifyMockObjects();
	}

	/**
	 * Ensure issue if adding the {@link OfficeInput} twice.
	 */
	public void testAddInputTwice() {

		// Record issue in adding the input twice
		this.issues.recordIssue("INPUT", OfficeInputNodeImpl.class,
				"Office Input INPUT already added");

		// Add the input twice
		this.replayMockObjects();
		OfficeInput inputFirst = this.node.addOfficeInput("INPUT",
				Integer.class.getName());
		OfficeInput inputSecond = this.node.addOfficeInput("INPUT",
				Integer.class.getName());
		this.verifyMockObjects();

		// Should be the same input
		assertSame("Should be same input on adding twice", inputFirst,
				inputSecond);
	}

	/**
	 * Tests adding an {@link OfficeOutput}.
	 */
	public void testAddOutput() {
		// Add two different outputs verifying details
		this.replayMockObjects();
		OfficeOutput output = this.node.addOfficeOutput("OUTPUT",
				String.class.getName());
		assertNotNull("Must have output", output);
		assertEquals("Incorrect output name", "OUTPUT",
				output.getOfficeOutputName());
		assertNotSame("Should obtain another output", output,
				this.node.addOfficeOutput("ANOTHER", Integer.class.getName()));
		this.verifyMockObjects();
	}

	/**
	 * Ensure issue if adding the {@link OfficeOutput} twice.
	 */
	public void testAddOutputTwice() {

		// Record issue in adding the output twice
		this.issues.recordIssue("OUTPUT", OfficeOutputNodeImpl.class,
				"Office Output OUTPUT already added");

		// Add the output twice
		this.replayMockObjects();
		OfficeOutput outputFirst = this.node.addOfficeOutput("OUTPUT",
				Integer.class.getName());
		OfficeOutput outputSecond = this.node.addOfficeOutput("OUTPUT",
				Integer.class.getName());
		this.verifyMockObjects();

		// Should be the same output
		assertSame("Should be same output on adding twice", outputFirst,
				outputSecond);
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
		assertNotSame("Should obtain another team", team,
				this.node.addOfficeTeam("ANOTHER"));
		this.verifyMockObjects();
	}

	/**
	 * Ensure issue if add the {@link OfficeTeam} twice.
	 */
	public void testGetOfficeTeamTwice() {

		// Record issue in adding the teams twice
		this.issues.recordIssue("TEAM", OfficeTeamNodeImpl.class,
				"Office Team TEAM already added");

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
		assertEquals("Incorrect section name", "SECTION",
				section.getOfficeSectionName());
		assertNotSame("Should obtain another section", section,
				this.addSection(this.node, "ANOTHER", null));
		this.verifyMockObjects();
	}

	/**
	 * Ensure issue if add the {@link OfficeSection} twice.
	 */
	public void testAddSectionTwice() {

		// Record issue in adding the section twice
		this.issues.recordIssue("SECTION", SectionNodeImpl.class,
				"Section SECTION already added");

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
	 * Tests adding an {@link OfficeSection} instance.
	 */
	public void testAddSectionInstance() {
		// Add two different sections verifying details
		this.replayMockObjects();
		OfficeSection section = this.node.addOfficeSection("SECTION",
				new SectionModelSectionSource(), "location");
		assertNotNull("Must have section", section);
		assertEquals("Incorrect section name", "SECTION",
				section.getOfficeSectionName());
		assertNotSame("Should obtain another section", section,
				this.node.addOfficeSection("ANOTHER",
						new SectionModelSectionSource(), "location"));
		this.verifyMockObjects();
	}

	/**
	 * Ensure issue if add the {@link OfficeSection} instance twice.
	 */
	public void testAddSectionInstanceTwice() {

		// Record issue in adding the section twice
		this.issues.recordIssue("SECTION", SectionNodeImpl.class,
				"Section SECTION already added");

		// Add the section twice
		this.replayMockObjects();
		OfficeSection sectionFirst = this.node.addOfficeSection("SECTION",
				new SectionModelSectionSource(), "location");
		OfficeSection sectionSecond = this.node.addOfficeSection("SECTION",
				new SectionModelSectionSource(), "location");
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
		assertEquals("Incorrect managed object name", "MO",
				moSource.getOfficeManagedObjectSourceName());
		assertNotSame("Should obtain another managed object", moSource,
				this.addManagedObjectSource(this.node, "ANOTHER", null));
		this.verifyMockObjects();
	}

	/**
	 * Ensure issue if add the {@link OfficeManagedObjectSource} twice.
	 */
	public void testAddOfficeManagedObjectSourceTwice() {

		// Record issue in adding the managed object sources twice
		this.issues.recordIssue("MO", ManagedObjectSourceNodeImpl.class,
				"Managed Object Source MO already added");

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
	 * Tests adding a {@link OfficeManagedObjectSource} instance.
	 */
	public void testAddOfficeManagedObjectSourceInstance() {
		// Add two different managed objects verifying details
		this.replayMockObjects();
		OfficeManagedObjectSource moSource = this.node
				.addOfficeManagedObjectSource("MO",
						new ClassManagedObjectSource());
		assertNotNull("Must have managed object", moSource);
		assertEquals("Incorrect managed object name", "MO",
				moSource.getOfficeManagedObjectSourceName());
		assertNotSame("Should obtain another managed object", moSource,
				this.node.addOfficeManagedObjectSource("ANOTHER",
						new ClassManagedObjectSource()));
		this.verifyMockObjects();
	}

	/**
	 * Ensure issue if add the {@link OfficeManagedObjectSource} instance twice.
	 */
	public void testAddOfficeManagedObjectSourceInstanceTwice() {

		// Record issue in adding the managed object sources twice
		this.issues.recordIssue("MO", ManagedObjectSourceNodeImpl.class,
				"Managed Object Source MO already added");

		// Add the managed object source twice
		this.replayMockObjects();
		OfficeManagedObjectSource moSourceFirst = this.node
				.addOfficeManagedObjectSource("MO",
						new ClassManagedObjectSource());
		OfficeManagedObjectSource moSourceSecond = this.node
				.addOfficeManagedObjectSource("MO",
						new ClassManagedObjectSource());
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
		assertEquals("Incorrect managed object name", "MO",
				mo.getOfficeManagedObjectName());
		assertNotSame("Should obtain another managed object", mo,
				moSource.addOfficeManagedObject("ANOTHER",
						ManagedObjectScope.PROCESS));
		this.verifyMockObjects();
	}

	/**
	 * Ensure issue if add the {@link OfficeManagedObject} twice.
	 */
	public void testAddOfficeManagedObjectTwice() {

		// Record issue in adding the managed objects twice
		this.issues.recordIssue("MO", ManagedObjectNodeImpl.class,
				"Managed Object MO already added");

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
		this.issues.recordIssue("MO", ManagedObjectNodeImpl.class,
				"Managed Object MO already added");

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
	 * Tests adding an {@link OfficeEscalation}.
	 */
	public void testAddOfficeEscalation() {
		// Add two different escalations verifying details
		this.replayMockObjects();
		OfficeEscalation escalation = this.node
				.addOfficeEscalation("java.sql.SQLException");
		assertNotNull("Must have escalation", escalation);
		assertEquals("Incorrect escalation type", "java.sql.SQLException",
				escalation.getOfficeEscalationType());
		assertNotSame("Should obtain another escalation", escalation,
				this.node.addOfficeEscalation("java.io.IOException"));
		this.verifyMockObjects();
	}

	/**
	 * Ensure issue if add the {@link OfficeEscalation} twice.
	 */
	public void testAddOfficeEscalationTwice() {

		// Record issue in adding the escalation twice
		this.issues.recordIssue("java.sql.SQLException",
				EscalationNodeImpl.class,
				"Escalation java.sql.SQLException already added");

		// Add the escalation twice
		this.replayMockObjects();
		OfficeEscalation escalationFirst = this.node
				.addOfficeEscalation("java.sql.SQLException");
		OfficeEscalation escalationSecond = this.node
				.addOfficeEscalation("java.sql.SQLException");
		this.verifyMockObjects();

		// Should be the same escalation
		assertEquals("Should be same escalation on adding twice",
				escalationFirst, escalationSecond);
	}

	/**
	 * Tests adding an {@link OfficeStart}.
	 */
	public void testAddOfficeStart() {
		// Add two different escalations verifying details
		this.replayMockObjects();
		OfficeStart start = this.node.addOfficeStart("START");
		assertNotNull("Must have start", start);
		assertEquals("Incorrect start name", "START",
				start.getOfficeStartName());
		assertNotSame("Should obtain another start", start,
				this.node.addOfficeStart("ANOTHER"));
		this.verifyMockObjects();
	}

	/**
	 * Ensure issue if add the {@link OfficeStart} twice.
	 */
	public void testAddOfficeStartTwice() {

		// Record issue in adding the start twice
		this.issues.recordIssue("START", OfficeStartNodeImpl.class,
				"Office Start START already added");

		// Add the start twice
		this.replayMockObjects();
		OfficeStart startFirst = this.node.addOfficeStart("START");
		OfficeStart startSecond = this.node.addOfficeStart("START");
		this.verifyMockObjects();

		// Should be the same start
		assertEquals("Should be same start on adding twice", startFirst,
				startSecond);
	}

	/**
	 * Tests adding an {@link OfficeGovernance}.
	 */
	public void testAddOfficeGovernance() {
		// Add two different governances verifying details
		this.replayMockObjects();
		OfficeGovernance gov = this
				.addGovernance(this.node, "GOVERNANCE", null);
		assertNotNull("Must have governance", gov);
		assertEquals("Incorrect governance name", "GOVERNANCE",
				gov.getOfficeGovernanceName());
		assertNotSame("Should obtain another governance", gov,
				this.addAdministrator(this.node, "ANOTHER", null));
		this.verifyMockObjects();
	}

	/**
	 * Ensure issue if add the {@link OfficeGovernance} twice.
	 */
	public void testAddOfficeGovernanceTwice() {

		// Record issue in adding the governance twice
		this.issues.recordIssue("GOVERNANCE", GovernanceNodeImpl.class,
				"Governance GOVERNANCE already added");

		// Add the governance twice
		this.replayMockObjects();
		OfficeGovernance govFirst = this.addGovernance(this.node, "GOVERNANCE",
				null);
		OfficeGovernance govSecond = this.addGovernance(this.node,
				"GOVERNANCE", null);
		this.verifyMockObjects();

		// Should be the same governance
		assertEquals("Should be same governance on adding twice", govFirst,
				govSecond);
	}

	/**
	 * Tests adding an {@link OfficeGovernance} instance.
	 */
	public void testAddOfficeGovernanceInstance() {
		// Add two different governances verifying details
		this.replayMockObjects();
		OfficeGovernance gov = this.node.addOfficeGovernance("GOVERNANCE",
				new ClassGovernanceSource());
		assertNotNull("Must have governance", gov);
		assertEquals("Incorrect governance name", "GOVERNANCE",
				gov.getOfficeGovernanceName());
		assertNotSame("Should obtain another governance", gov,
				this.node.addOfficeGovernance("ANOTHER",
						new ClassGovernanceSource()));
		this.verifyMockObjects();
	}

	/**
	 * Ensure issue if add the {@link OfficeGovernance} instance twice.
	 */
	public void testAddOfficeGovernanceInstanceTwice() {

		// Record issue in adding the governance twice
		this.issues.recordIssue("GOVERNANCE", GovernanceNodeImpl.class,
				"Governance GOVERNANCE already added");

		// Add the governance twice
		this.replayMockObjects();
		OfficeGovernance govFirst = this.node.addOfficeGovernance("GOVERNANCE",
				new ClassGovernanceSource());
		OfficeGovernance govSecond = this.node.addOfficeGovernance(
				"GOVERNANCE", new ClassGovernanceSource());
		this.verifyMockObjects();

		// Should be the same governance
		assertEquals("Should be same governance on adding twice", govFirst,
				govSecond);
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
		assertEquals("Incorrect administrator name", "ADMIN",
				admin.getOfficeAdministratorName());
		assertNotSame("Should obtain another administrator", admin,
				this.addAdministrator(this.node, "ANOTHER", null));
		this.verifyMockObjects();
	}

	/**
	 * Ensure issue if add the {@link OfficeAdministrator} twice.
	 */
	public void testAddOfficeAdministratorTwice() {

		// Record issue in adding the administrator twice
		this.issues.recordIssue("ADMIN", AdministratorNodeImpl.class,
				"Administrator ADMIN already added");

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
	 * Tests adding an {@link OfficeAdministrator} instance.
	 */
	public void testAddOfficeAdministratorInstance() {
		// Add two different administrators verifying details
		this.replayMockObjects();
		OfficeAdministrator admin = this.node.addOfficeAdministrator("ADMIN",
				new ClassAdministratorSource());
		assertNotNull("Must have administrator", admin);
		assertEquals("Incorrect administrator name", "ADMIN",
				admin.getOfficeAdministratorName());
		assertNotSame("Should obtain another administrator", admin,
				this.node.addOfficeAdministrator("ANOTHER",
						new ClassAdministratorSource()));
		this.verifyMockObjects();
	}

	/**
	 * Ensure issue if add the {@link OfficeAdministrator} instance twice.
	 */
	public void testAddOfficeAdministratorInstanceTwice() {

		// Record issue in adding the administrator twice
		this.issues.recordIssue("ADMIN", AdministratorNodeImpl.class,
				"Administrator ADMIN already added");

		// Add the administrator twice
		this.replayMockObjects();
		OfficeAdministrator adminFirst = this.node.addOfficeAdministrator(
				"ADMIN", new ClassAdministratorSource());
		OfficeAdministrator adminSecond = this.node.addOfficeAdministrator(
				"ADMIN", new ClassAdministratorSource());
		this.verifyMockObjects();

		// Should be the same administrator
		assertEquals("Should be same administrator on adding twice",
				adminFirst, adminSecond);
	}

	/**
	 * Ensure can link {@link OfficeInput} to the {@link SectionInput}.
	 */
	public void testLinkInputToSectionInput() {

		// Record already being linked
		this.issues.recordIssue("INPUT", OfficeInputNodeImpl.class,
				"Office Input INPUT linked more than once");

		this.replayMockObjects();

		// Link
		OfficeInput input = this.node.addOfficeInput("INPUT",
				Integer.class.getName());
		OfficeSection section = this.addSection(this.node, "SECTION", null);
		OfficeSectionInput sectionInput = section
				.getOfficeSectionInput("SECTION_INPUT");
		assertEquals("Incorrect office section input", "SECTION_INPUT",
				sectionInput.getOfficeSectionInputName());

		this.node.link(input, sectionInput);
		assertFlowLink("input -> section input", input, sectionInput);

		// Ensure only can link once
		this.node.link(input, section.getOfficeSectionInput("ANOTHER"));
		assertFlowLink("Can only link once", input, sectionInput);

		this.verifyMockObjects();
	}

	/**
	 * Ensure can link {@link SectionOutput} to a {@link OfficeOutput}.
	 */
	public void testLinkSectionOutputToOutput() {

		// Record already being linked
		this.issues.recordIssue("SECTION_OUTPUT", SectionOutputNodeImpl.class,
				"Section Output SECTION_OUTPUT linked more than once");

		this.replayMockObjects();

		// Link
		OfficeSection section = this.addSection(this.node, "SECTION", null);
		OfficeSectionOutput sectionOutput = section
				.getOfficeSectionOutput("SECTION_OUTPUT");
		OfficeOutput output = this.node.addOfficeOutput("OUTPUT",
				Integer.class.getName());

		this.node.link(sectionOutput, output);
		assertFlowLink("section output -> output", sectionOutput, output);

		// Ensure only can link once
		this.node
				.link(sectionOutput,
						this.node.addOfficeOutput("ANOTHER",
								Character.class.getName()));
		assertFlowLink("Can only link once", sectionOutput, output);

		this.verifyMockObjects();
	}

	/**
	 * Ensure can link {@link OfficeInput} to {@link OfficeOutput} for
	 * synchronous response.
	 */
	public void testLinkInputToOutput() {

		// Record already being linked
		this.issues.recordIssue("INPUT", OfficeInputNodeImpl.class,
				"Input INPUT linked more than once");

		this.replayMockObjects();

		// Link
		OfficeInput input = this.node.addOfficeInput("INPUT",
				Integer.class.getName());
		OfficeOutput output = this.node.addOfficeOutput("OUTPUT",
				Long.class.getName());
		this.node.link(input, output);
		assertSynchronousLink("input -> output", input, output);

		// Ensure only can link once
		this.node
				.link(input,
						this.node.addOfficeOutput("ANOHTER",
								Character.class.getName()));
		assertSynchronousLink("input -> output", input, output);

		this.verifyMockObjects();
	}

	/**
	 * Ensure can link {@link OfficeOutput} to {@link OfficeInput} for
	 * synchronous request.
	 */
	public void testLinkOutputToInput() {

		// Record already being linked
		this.issues.recordIssue("OUTPUT", OfficeOutputNodeImpl.class,
				"Output OUTPUT linked more than once");

		this.replayMockObjects();

		// Link
		OfficeOutput output = this.node.addOfficeOutput("OUTPUT",
				Long.class.getName());
		OfficeInput input = this.node.addOfficeInput("INPUT",
				Integer.class.getName());
		this.node.link(output, input);
		assertSynchronousLink("output -> input", output, input);

		// Ensure only can link once
		this.node.link(output,
				this.node.addOfficeInput("ANOTHER", Character.class.getName()));
		assertSynchronousLink("output -> input", output, input);

		this.verifyMockObjects();
	}

	/**
	 * Ensure can link the {@link TaskTeam} to an {@link OfficeTeam}.
	 */
	public void testLinkTaskTeamToOfficeTeam() {

		// Record already being linked
		this.issues.recordIssue("Team for task TASK", TaskTeamNodeImpl.class,
				"Task Team Team for task TASK linked more than once");

		this.replayMockObjects();

		// Add section with task
		OfficeSection section = this.addSection(this.node, "SECTION", null);
		OfficeSectionTask task = section.getOfficeSectionTask("TASK");
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
	 * Ensures can link {@link OfficeGovernance} to the {@link OfficeTeam}.
	 */
	public void testLinkOfficeGovernanceToOfficeTeam() {

		// Record already being linked
		this.issues.recordIssue("GOVERNANCE", GovernanceNodeImpl.class,
				"Governance GOVERNANCE linked more than once");

		this.replayMockObjects();

		// Link
		OfficeGovernance gov = this
				.addGovernance(this.node, "GOVERNANCE", null);
		OfficeTeam officeTeam = this.node.addOfficeTeam("OFFICE_TEAM");
		this.node.link(gov, officeTeam);
		assertTeamLink("governance -> office team", gov, officeTeam);

		// Ensure only can link once
		this.node.link(gov, this.node.addOfficeTeam("ANOTHER"));
		assertTeamLink("Can only link once", gov, officeTeam);

		this.verifyMockObjects();
	}

	/**
	 * Ensures can link {@link OfficeAdministrator} to the {@link OfficeTeam}.
	 */
	public void testLinkOfficeAdministratorToOfficeTeam() {

		// Record already being linked
		this.issues.recordIssue("ADMIN", AdministratorNodeImpl.class,
				"Administrator ADMIN linked more than once");

		this.replayMockObjects();

		// Link
		OfficeAdministrator admin = this.addAdministrator(this.node, "ADMIN",
				null);
		OfficeTeam officeTeam = this.node.addOfficeTeam("OFFICE_TEAM");
		this.node.link(admin, officeTeam);
		assertTeamLink("administrator -> office team", admin, officeTeam);

		// Ensure only can link once
		this.node.link(admin, this.node.addOfficeTeam("ANOTHER"));
		assertTeamLink("Can only link once", admin, officeTeam);

		this.verifyMockObjects();
	}

	/**
	 * Ensure can specify {@link OfficeGovernance} for a {@link OfficeSection}.
	 */
	public void testLinkOfficeGovernanceForOfficeSection() {

		this.replayMockObjects();

		// Add section
		OfficeSection section = this.addSection(this.node, "SECTION", null);

		// Link
		OfficeGovernance governance = this.addGovernance(this.node,
				"GOVERNANCE", null);
		section.addGovernance(governance);
		// TODO test that governance specified

		// May have many governances
		OfficeGovernance another = this.addGovernance(this.node, "ANOTHER",
				null);
		section.addGovernance(another);

		this.verifyMockObjects();
	}

	/**
	 * Ensure can specify {@link OfficeGovernance} for a
	 * {@link OfficeSubSection}.
	 */
	public void testLinkOfficeGovernanceForOfficeSubSection() {

		this.replayMockObjects();

		// Add section with sub-section
		OfficeSection section = this.addSection(this.node, "SECTION", null);
		OfficeSubSection subSection = section
				.getOfficeSubSection("SUB_SECTION");

		// Link
		OfficeGovernance governance = this.addGovernance(this.node,
				"GOVERNANCE", null);
		subSection.addGovernance(governance);
		// TODO test that governance specified

		// May have many governances
		OfficeGovernance another = this.addGovernance(this.node, "ANOTHER",
				null);
		subSection.addGovernance(another);

		this.verifyMockObjects();
	}

	/**
	 * Ensure can specify {@link OfficeGovernance} for a specific
	 * {@link OfficeSectionTask}.
	 */
	public void testLinkOfficeGovernanceForOfficeTask() {

		this.replayMockObjects();

		// Add section with task
		OfficeSection section = this.addSection(this.node, "SECTION", null);
		OfficeSectionTask task = section.getOfficeSectionTask("TASK");

		// Link
		OfficeGovernance governance = this.addGovernance(this.node,
				"GOVERNANCE", null);
		task.addGovernance(governance);
		// TODO test that governance specified

		// May have many governances
		OfficeGovernance another = this.addGovernance(this.node, "ANOTHER",
				null);
		task.addGovernance(another);

		this.verifyMockObjects();
	}

	/**
	 * Ensure can specify pre {@link OfficeDuty} for the {@link OfficeSectionTask}.
	 */
	public void testLinkPreOfficeDutyForOfficeTask() {

		this.replayMockObjects();

		// Add section with task
		OfficeSection section = this.addSection(this.node, "SECTION", null);
		OfficeSectionTask task = section.getOfficeSectionTask("TASK");

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
	 * Ensure can specify post {@link OfficeDuty} for the {@link OfficeSectionTask}.
	 */
	public void testLinkPostOfficeDutyForOfficeTask() {

		this.replayMockObjects();

		// Add section with task
		OfficeSection section = this.addSection(this.node, "SECTION", null);
		OfficeSectionTask task = section.getOfficeSectionTask("TASK");

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
	 * Ensure can govern {@link OfficeSectionManagedObject}.
	 */
	public void testGovernOfficeSectionManagedObject() {
		this.replayMockObjects();

		// Add section with governance
		OfficeSection section = this.addSection(this.node, "SECTION", null);
		OfficeSectionManagedObject mo = section
				.getOfficeSectionManagedObject("MO");

		// Link
		OfficeGovernance governance = this.addGovernance(this.node,
				"GOVERNANCE", null);
		governance.governManagedObject(mo);
		// TODO test that governing the section managed object

		// Should be able to govern the managed object twice
		governance.governManagedObject(mo);

		this.verifyMockObjects();
	}

	/**
	 * Ensure can govern {@link OfficeManagedObject}.
	 */
	public void testGovernOfficeManagedObject() {
		this.replayMockObjects();

		// Link
		OfficeGovernance governance = this.addGovernance(this.node,
				"GOVERNANCE", null);
		OfficeManagedObjectSource moSource = this.addManagedObjectSource(
				this.node, "MO_SOURCE", null);
		OfficeManagedObject mo = moSource.addOfficeManagedObject("MO",
				ManagedObjectScope.THREAD);
		governance.governManagedObject(mo);
		// TODO test that governering the section managed object

		// Should be able to govern the managed object twice
		governance.governManagedObject(mo);

		this.verifyMockObjects();
	}

	/**
	 * Ensure can govern {@link OfficeObject}.
	 */
	public void testGovernOfficeObject() {
		this.replayMockObjects();

		// Link
		OfficeGovernance governance = this.addGovernance(this.node,
				"GOVERNANCE", null);
		OfficeObject mo = this.node.addOfficeObject("MO",
				Connection.class.getName());
		governance.governManagedObject(mo);
		// TODO test that governering the office object

		// Should be able to govern the office object twice
		governance.governManagedObject(mo);

		this.verifyMockObjects();
	}

	/**
	 * Ensure can administer {@link OfficeSectionManagedObject}.
	 */
	public void testAdministerOfficeSectionManagedObject() {
		this.replayMockObjects();

		// Add section with managed object
		OfficeSection section = this.addSection(this.node, "SECTION", null);
		OfficeSectionManagedObject mo = section
				.getOfficeSectionManagedObject("MO");

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
		OfficeObject mo = this.node.addOfficeObject("MO",
				Connection.class.getName());
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
		this.issues.recordIssue("SECTION_OBJECT", SectionObjectNodeImpl.class,
				"Section Object SECTION_OBJECT linked more than once");

		this.replayMockObjects();

		// Add section with section object
		OfficeSection section = this.addSection(this.node, "SECTION", null);
		OfficeSectionObject sectionObject = section
				.getOfficeSectionObject("SECTION_OBJECT");
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
		this.issues.recordIssue("SECTION_OBJECT", SectionObjectNodeImpl.class,
				"Section Object SECTION_OBJECT linked more than once");

		this.replayMockObjects();

		// Add section with section object
		OfficeSection section = this.addSection(this.node, "SECTION", null);
		OfficeSectionObject sectionObject = section
				.getOfficeSectionObject("SECTION_OBJECT");

		// Link
		OfficeObject mo = this.node.addOfficeObject("MO",
				Connection.class.getName());
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
	 * Ensures can link {@link OfficeManagedObjectSource}
	 * {@link ManagedObjectTeam} to the {@link OfficeTeam}.
	 */
	public void testLinkOfficeManagedObjectSourceTeamToOfficeTeam() {

		// Record already being linked
		this.issues.recordIssue("TEAM", ManagedObjectTeamNodeImpl.class,
				"Managed Object Source Team TEAM linked more than once");

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
		this.issues.recordIssue("TEAM", ManagedObjectTeamNodeImpl.class,
				"Managed Object Source Team TEAM linked more than once");

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
		OfficeSectionManagedObjectSource moSource = section
				.getOfficeSectionManagedObjectSource("MO_SOURCE");
		assertNotNull("Should have managed object source", moSource);
		assertEquals("Incorrect section managed object source", "MO_SOURCE",
				moSource.getOfficeSectionManagedObjectSourceName());
		ManagedObjectTeam team = moSource
				.getOfficeSectionManagedObjectTeam("TEAM");
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
	 * {@link OfficeManagedObject}.
	 */
	public void testLinkManagedObjectDependencyToOfficeManagedObject() {

		// Record already being linked
		this.issues.recordIssue("DEPENDENCY",
				ManagedObjectDependencyNodeImpl.class,
				"Managed Object Dependency DEPENDENCY linked more than once");

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
	 * Ensures can link {@link ManagedObjectDependency} to the
	 * {@link OfficeObject}.
	 */
	public void testLinkManagedObjectDependencyToOfficeObject() {

		// Record already being linked
		this.issues.recordIssue("DEPENDENCY",
				ManagedObjectDependencyNodeImpl.class,
				"Managed Object Dependency DEPENDENCY linked more than once");

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
		this.node.link(dependency,
				this.node.addOfficeObject("ANOTHER", String.class.getName()));
		assertObjectLink("Can only link once", dependency, moTarget);

		this.verifyMockObjects();
	}

	/**
	 * Ensure can link Input {@link ManagedObjectDependency} to the
	 * {@link OfficeManagedObject}.
	 */
	public void testLinkInputManagedObjectDependencyToOfficeManagedObject() {

		// Record already being linked
		this.issues.recordIssue("DEPENDENCY",
				ManagedObjectDependencyNodeImpl.class,
				"Managed Object Dependency DEPENDENCY linked more than once");

		this.replayMockObjects();

		// Link
		OfficeManagedObjectSource moSource = this.addManagedObjectSource(
				this.node, "MO_SOURCE", null);
		ManagedObjectDependency dependency = moSource
				.getInputManagedObjectDependency("DEPENDENCY");
		OfficeManagedObjectSource moSourceTarget = this.addManagedObjectSource(
				this.node, "MO_SOURCE_TARGET", null);
		OfficeManagedObject moTarget = moSourceTarget.addOfficeManagedObject(
				"MO_TARGET", ManagedObjectScope.PROCESS);
		this.node.link(dependency, moTarget);
		assertObjectLink(
				"input managed object dependency -> office floor managed object",
				dependency, moTarget);

		// Ensure only can link once
		this.node.link(dependency, moSourceTarget.addOfficeManagedObject(
				"ANOTHER", ManagedObjectScope.PROCESS));
		assertObjectLink("Can only link once", dependency, moTarget);

		this.verifyMockObjects();
	}

	/**
	 * Ensure can link Input {@link ManagedObjectDependency} to the
	 * {@link OfficeObject}.
	 */
	public void testLinkInputManagedObjectDependencyToOfficeObject() {

		// Record already being linked
		this.issues.recordIssue("DEPENDENCY",
				ManagedObjectDependencyNodeImpl.class,
				"Managed Object Dependency DEPENDENCY linked more than once");

		this.replayMockObjects();

		// Link
		OfficeManagedObjectSource moSource = this.addManagedObjectSource(
				this.node, "MO_SOURCE", null);
		ManagedObjectDependency dependency = moSource
				.getInputManagedObjectDependency("DEPENDENCY");
		OfficeObject moTarget = this.node.addOfficeObject("MO_TARGET",
				Connection.class.getName());
		this.node.link(dependency, moTarget);
		assertObjectLink(
				"input managed object dependency -> office floor managed object",
				dependency, moTarget);

		// Ensure only can link once
		this.node.link(dependency, this.node.addOfficeObject("ANOTHER",
				Connection.class.getName()));
		assertObjectLink("Can only link once", dependency, moTarget);

		this.verifyMockObjects();
	}

	/**
	 * Ensures can link {@link ManagedObjectFlow} to the
	 * {@link OfficeSectionInput}.
	 */
	public void testLinkManagedObjectFlowToOfficeSectionInput() {

		// Record already being linked
		this.issues.recordIssue("FLOW", ManagedObjectFlowNodeImpl.class,
				"Managed Object Source Flow FLOW linked more than once");

		this.replayMockObjects();

		// Add section with section inputs
		OfficeSection section = this.addSection(this.node, "SECTION", null);
		// Obtain section input (should be ordered)
		OfficeSectionInput sectionInput = section
				.getOfficeSectionInput("SECTION_INPUT");
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
		this.node.link(flow, section.getOfficeSectionInput("ANOTHER"));
		assertFlowLink("Can only link once", flow, sectionInput);

		this.verifyMockObjects();
	}

	/**
	 * Ensures can link {@link OfficeSectionOutput} to the
	 * {@link OfficeSectionInput}.
	 */
	public void testLinkOfficeSectionOutputToOfficeSectionInput() {

		// Record already being linked
		this.issues.recordIssue("SECTION_OUTPUT", SectionOutputNodeImpl.class,
				"Section Output SECTION_OUTPUT linked more than once");

		this.replayMockObjects();

		// Add section with section outputs
		OfficeSection outputSection = this.addSection(this.node,
				"OUTPUT_SECTION", null);
		OfficeSectionOutput sectionOutput = outputSection
				.getOfficeSectionOutput("SECTION_OUTPUT");
		assertEquals("Incorrect office section input", "SECTION_OUTPUT",
				sectionOutput.getOfficeSectionOutputName());

		// Add section with section inputs
		OfficeSection inputSection = this.addSection(this.node,
				"INPUT_SECTION", null);
		OfficeSectionInput sectionInput = inputSection
				.getOfficeSectionInput("SECTION_INPUT");
		assertEquals("Incorrect office section input", "SECTION_INPUT",
				sectionInput.getOfficeSectionInputName());

		// Link
		this.node.link(sectionOutput, sectionInput);
		assertFlowLink("section output -> section input", sectionOutput,
				sectionInput);

		// Ensure only can link once
		this.node.link(sectionOutput,
				inputSection.getOfficeSectionInput("ANOTHER"));
		assertFlowLink("Can only link once", sectionOutput, sectionInput);

		this.verifyMockObjects();
	}

	/**
	 * Ensures can link {@link OfficeEscalation} to the
	 * {@link OfficeSectionInput}.
	 */
	public void testLinkOfficeEscalationToOfficeSectionInput() {

		// Record already being linked
		this.issues.recordIssue("java.sql.SQLException",
				EscalationNodeImpl.class,
				"Escalation java.sql.SQLException linked more than once");

		this.replayMockObjects();

		// Add escalation
		OfficeEscalation escalation = this.node
				.addOfficeEscalation("java.sql.SQLException");
		assertEquals("Incorrect office escalation", "java.sql.SQLException",
				escalation.getOfficeEscalationType());

		// Add section with section inputs
		OfficeSection inputSection = this.addSection(this.node,
				"INPUT_SECTION", null);
		OfficeSectionInput sectionInput = inputSection
				.getOfficeSectionInput("SECTION_INPUT");
		assertEquals("Incorrect office section input", "SECTION_INPUT",
				sectionInput.getOfficeSectionInputName());

		// Link
		this.node.link(escalation, sectionInput);
		assertFlowLink("escalation -> section input", escalation, sectionInput);

		// Ensure only can link once
		this.node.link(escalation,
				inputSection.getOfficeSectionInput("ANOTHER"));
		assertFlowLink("Can only link once", escalation, sectionInput);

		this.verifyMockObjects();
	}

	/**
	 * Ensures can link {@link OfficeStart} to the {@link OfficeSectionInput}.
	 */
	public void testLinkOfficeStartToOfficeSectionInput() {

		// Record already being linked
		this.issues.recordIssue("START", OfficeStartNodeImpl.class,
				"Office Start START linked more than once");

		this.replayMockObjects();

		// Add start
		OfficeStart start = this.node.addOfficeStart("START");
		assertEquals("Incorrect office start", "START",
				start.getOfficeStartName());

		// Add section with section inputs
		OfficeSection inputSection = this.addSection(this.node,
				"INPUT_SECTION", null);
		OfficeSectionInput sectionInput = inputSection
				.getOfficeSectionInput("SECTION_INPUT");
		assertEquals("Incorrect office section input", "SECTION_INPUT",
				sectionInput.getOfficeSectionInputName());

		// Link
		this.node.link(start, sectionInput);
		assertFlowLink("start -> section input", start, sectionInput);

		// Ensure only can link once
		this.node.link(start, inputSection.getOfficeSectionInput("ANOTHER"));
		assertFlowLink("Can only link once", start, sectionInput);

		this.verifyMockObjects();
	}

}