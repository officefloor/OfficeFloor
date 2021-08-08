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

package net.officefloor.compile.impl.structure;

import java.sql.Connection;

import net.officefloor.compile.internal.structure.Node;
import net.officefloor.compile.internal.structure.OfficeFloorNode;
import net.officefloor.compile.internal.structure.OfficeNode;
import net.officefloor.compile.spi.managedobject.ManagedObjectDependency;
import net.officefloor.compile.spi.managedobject.ManagedObjectFlow;
import net.officefloor.compile.spi.managedobject.ManagedObjectTeam;
import net.officefloor.compile.spi.office.OfficeAdministration;
import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.compile.spi.office.OfficeEscalation;
import net.officefloor.compile.spi.office.OfficeGovernance;
import net.officefloor.compile.spi.office.OfficeInput;
import net.officefloor.compile.spi.office.OfficeManagedObject;
import net.officefloor.compile.spi.office.OfficeManagedObjectDependency;
import net.officefloor.compile.spi.office.OfficeManagedObjectFlow;
import net.officefloor.compile.spi.office.OfficeManagedObjectSource;
import net.officefloor.compile.spi.office.OfficeManagedObjectTeam;
import net.officefloor.compile.spi.office.OfficeObject;
import net.officefloor.compile.spi.office.OfficeOutput;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.spi.office.OfficeSectionFunction;
import net.officefloor.compile.spi.office.OfficeSectionInput;
import net.officefloor.compile.spi.office.OfficeSectionManagedObject;
import net.officefloor.compile.spi.office.OfficeSectionManagedObjectSource;
import net.officefloor.compile.spi.office.OfficeSectionManagedObjectTeam;
import net.officefloor.compile.spi.office.OfficeSectionObject;
import net.officefloor.compile.spi.office.OfficeSectionOutput;
import net.officefloor.compile.spi.office.OfficeStart;
import net.officefloor.compile.spi.office.OfficeSubSection;
import net.officefloor.compile.spi.office.OfficeTeam;
import net.officefloor.compile.spi.office.ResponsibleTeam;
import net.officefloor.compile.spi.office.source.OfficeSource;
import net.officefloor.compile.spi.officefloor.OfficeFloorDeployer;
import net.officefloor.compile.spi.officefloor.source.OfficeFloorSource;
import net.officefloor.compile.spi.section.SectionInput;
import net.officefloor.compile.spi.section.SectionOutput;
import net.officefloor.frame.api.administration.Administration;
import net.officefloor.frame.api.administration.AdministrationFactory;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.function.ManagedFunctionFactory;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.managedobject.source.ManagedObjectFunctionBuilder;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.model.impl.section.SectionModelSectionSource;
import net.officefloor.plugin.administration.clazz.ClassAdministrationSource;
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
	private final OfficeFloorNode officeFloor = this.createMock(OfficeFloorNode.class);

	/**
	 * {@link OfficeNode} to be tested.
	 */
	private final OfficeNode node = new OfficeNodeImpl(OFFICE_NAME, this.officeFloor, this.nodeContext);

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		// Initialise the office
		this.node.initialise(MakerOfficeSource.class.getName(), null, OFFICE_LOCATION);
	}

	/**
	 * Ensure allow {@link OfficeSource} to report issues via the
	 * {@link OfficeArchitect}.
	 */
	public void testAddIssue() {

		// Record adding the issue
		this.recordIssue(OFFICE_NAME, OfficeNodeImpl.class, "TEST_ISSUE");

		// Add the issue
		this.replayMockObjects();
		assertNotNull("Should be provided compile error", this.node.addIssue("TEST_ISSUE"));
		this.verifyMockObjects();
	}

	/**
	 * Ensure allow {@link OfficeFloorSource} to report issues via the
	 * {@link OfficeFloorDeployer}.
	 */
	public void testAddIssueWithCause() {

		final Exception failure = new Exception("cause");

		// Record adding the issue
		this.recordReturn(this.officeFloor, this.officeFloor.getQualifiedName(OFFICE_NAME), OFFICE_NAME);
		this.issues.recordIssue(OFFICE_NAME, OfficeNodeImpl.class, "TEST_ISSUE", failure);

		// Add the issue
		this.replayMockObjects();
		assertNotNull("Should be provided compile error", this.node.addIssue("TEST_ISSUE", failure));
		this.verifyMockObjects();
	}

	/**
	 * Tests adding an {@link OfficeObject}.
	 */
	public void testGetOfficeFloorObject() {
		// Add two different managed objects verifying details
		this.replayMockObjects();
		OfficeObject mo = this.node.addOfficeObject("MO", Connection.class.getName());
		assertNotNull("Must have managed object", mo);
		assertEquals("Incorrect managed object name", "MO", mo.getOfficeObjectName());
		assertNotSame("Should obtain another managed object", mo,
				this.node.addOfficeObject("ANOTHER", String.class.getName()));
		this.verifyMockObjects();
	}

	/**
	 * Ensure issue if add the {@link OfficeObject} twice.
	 */
	public void testGetOfficeFloorObjectTwice() {

		// Record issue in adding the managed object twice
		this.recordIssue("OFFICE.MO", OfficeObjectNodeImpl.class, "Office Object MO already added");

		// Add the managed object twice
		this.replayMockObjects();
		OfficeObject moFirst = this.node.addOfficeObject("MO", Connection.class.getName());
		OfficeObject moSecond = this.node.addOfficeObject("MO", String.class.getName());
		this.verifyMockObjects();

		// Should be the managed object
		assertEquals("Should be same managed object on adding twice", moFirst, moSecond);
	}

	/**
	 * Tests adding an {@link OfficeInput}.
	 */
	public void testAddInput() {
		// Add two different inputs verifying details
		this.replayMockObjects();
		OfficeInput input = this.node.addOfficeInput("INPUT", String.class.getName());
		assertNotNull("Must have input", input);
		assertEquals("Incorrect input name", "INPUT", input.getOfficeInputName());
		assertNotSame("Should obtain another input", input,
				this.node.addOfficeInput("ANOTHER", Integer.class.getName()));
		this.verifyMockObjects();
	}

	/**
	 * Ensure issue if adding the {@link OfficeInput} twice.
	 */
	public void testAddInputTwice() {

		// Record issue in adding the input twice
		this.recordIssue("OFFICE.INPUT", OfficeInputNodeImpl.class, "Office Input INPUT already added");

		// Add the input twice
		this.replayMockObjects();
		OfficeInput inputFirst = this.node.addOfficeInput("INPUT", Integer.class.getName());
		OfficeInput inputSecond = this.node.addOfficeInput("INPUT", Integer.class.getName());
		this.verifyMockObjects();

		// Should be the same input
		assertSame("Should be same input on adding twice", inputFirst, inputSecond);
	}

	/**
	 * Tests adding an {@link OfficeOutput}.
	 */
	public void testAddOutput() {
		// Add two different outputs verifying details
		this.replayMockObjects();
		OfficeOutput output = this.node.addOfficeOutput("OUTPUT", String.class.getName());
		assertNotNull("Must have output", output);
		assertEquals("Incorrect output name", "OUTPUT", output.getOfficeOutputName());
		assertNotSame("Should obtain another output", output,
				this.node.addOfficeOutput("ANOTHER", Integer.class.getName()));
		this.verifyMockObjects();
	}

	/**
	 * Ensure issue if adding the {@link OfficeOutput} twice.
	 */
	public void testAddOutputTwice() {

		// Record issue in adding the output twice
		this.recordIssue("OFFICE.OUTPUT", OfficeOutputNodeImpl.class, "Office Output OUTPUT already added");

		// Add the output twice
		this.replayMockObjects();
		OfficeOutput outputFirst = this.node.addOfficeOutput("OUTPUT", Integer.class.getName());
		OfficeOutput outputSecond = this.node.addOfficeOutput("OUTPUT", Integer.class.getName());
		this.verifyMockObjects();

		// Should be the same output
		assertSame("Should be same output on adding twice", outputFirst, outputSecond);
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
		assertNotSame("Should obtain another team", team, this.node.addOfficeTeam("ANOTHER"));
		this.verifyMockObjects();
	}

	/**
	 * Ensure issue if add the {@link OfficeTeam} twice.
	 */
	public void testGetOfficeTeamTwice() {

		// Record issue in adding the teams twice
		this.recordIssue("OFFICE.TEAM", OfficeTeamNodeImpl.class, "Office Team TEAM already added");

		// Add the team twice
		this.replayMockObjects();
		OfficeTeam teamFirst = this.node.addOfficeTeam("TEAM");
		OfficeTeam teamSecond = this.node.addOfficeTeam("TEAM");
		this.verifyMockObjects();

		// Should be the team
		assertEquals("Should be same managed object on adding twice", teamFirst, teamSecond);
	}

	/**
	 * Tests adding an {@link OfficeSection}.
	 */
	public void testAddSection() {
		// Add two different sections verifying details
		this.replayMockObjects();
		OfficeSection section = this.addSection(this.node, "SECTION", null);
		assertNotNull("Must have section", section);
		assertEquals("Incorrect section name", "SECTION", section.getOfficeSectionName());
		assertNotSame("Should obtain another section", section, this.addSection(this.node, "ANOTHER", null));
		this.verifyMockObjects();
	}

	/**
	 * Ensure issue if add the {@link OfficeSection} twice.
	 */
	public void testAddSectionTwice() {

		// Record issue in adding the section twice
		this.recordIssue("OFFICE.SECTION", SectionNodeImpl.class, "Section SECTION already added");

		// Add the section twice
		this.replayMockObjects();
		OfficeSection sectionFirst = this.addSection(this.node, "SECTION", null);
		OfficeSection sectionSecond = this.addSection(this.node, "SECTION", null);
		this.verifyMockObjects();

		// Should be the same section
		assertEquals("Should be same section on adding twice", sectionFirst, sectionSecond);
	}

	/**
	 * Tests adding an {@link OfficeSection} instance.
	 */
	public void testAddSectionInstance() {
		// Add two different sections verifying details
		this.replayMockObjects();
		OfficeSection section = this.node.addOfficeSection("SECTION", new SectionModelSectionSource(), "location");
		assertNotNull("Must have section", section);
		assertEquals("Incorrect section name", "SECTION", section.getOfficeSectionName());
		assertSame("Should be the same section", section, this.node.getOfficeSection("SECTION"));
		assertNotSame("Should obtain another section", section,
				this.node.addOfficeSection("ANOTHER", new SectionModelSectionSource(), "location"));
		assertSame("Should still be the same section", section, this.node.getOfficeSection("SECTION"));
		this.verifyMockObjects();
	}

	/**
	 * Tests initialising the {@link OfficeSection} after obtaining.
	 */
	public void testInitialiseSectionAfterwards() {
		this.replayMockObjects();
		OfficeSection section = this.node.getOfficeSection("SECTION");
		assertNotNull("Should have section", section);
		assertEquals("Incorrect section name", "SECTION", section.getOfficeSectionName());
		assertSame("Should be same section on initialising", section,
				this.node.addOfficeSection("SECTION", new SectionModelSectionSource(), "location"));
		assertSame("Should still be the same section", section, this.node.getOfficeSection("SECTION"));
		this.verifyMockObjects();
	}

	/**
	 * Ensure issue if add the {@link OfficeSection} instance twice.
	 */
	public void testAddSectionInstanceTwice() {

		// Record issue in adding the section twice
		this.recordIssue("OFFICE.SECTION", SectionNodeImpl.class, "Section SECTION already added");

		// Add the section twice
		this.replayMockObjects();
		OfficeSection sectionFirst = this.node.addOfficeSection("SECTION", new SectionModelSectionSource(), "location");
		OfficeSection sectionSecond = this.node.addOfficeSection("SECTION", new SectionModelSectionSource(),
				"location");
		this.verifyMockObjects();

		// Should be the same section
		assertEquals("Should be same section on adding twice", sectionFirst, sectionSecond);
	}

	/**
	 * Tests adding a {@link OfficeManagedObjectSource}.
	 */
	public void testAddOfficeManagedObjectSource() {
		// Add two different managed objects verifying details
		this.replayMockObjects();
		OfficeManagedObjectSource moSource = this.addManagedObjectSource(this.node, "MO", null);
		assertNotNull("Must have managed object", moSource);
		assertEquals("Incorrect managed object name", "MO", moSource.getOfficeManagedObjectSourceName());
		assertNotSame("Should obtain another managed object", moSource,
				this.addManagedObjectSource(this.node, "ANOTHER", null));
		this.verifyMockObjects();
	}

	/**
	 * Ensure issue if add the {@link OfficeManagedObjectSource} twice.
	 */
	public void testAddOfficeManagedObjectSourceTwice() {

		// Record issue in adding the managed object sources twice
		this.recordIssue("OFFICE.MO", ManagedObjectSourceNodeImpl.class, "Managed Object Source MO already added");

		// Add the managed object source twice
		this.replayMockObjects();
		OfficeManagedObjectSource moSourceFirst = this.addManagedObjectSource(this.node, "MO", null);
		OfficeManagedObjectSource moSourceSecond = this.addManagedObjectSource(this.node, "MO", null);
		this.verifyMockObjects();

		// Should be the same managed object source
		assertEquals("Should be same managed object on adding twice", moSourceFirst, moSourceSecond);
	}

	/**
	 * Tests adding a {@link OfficeManagedObjectSource} instance.
	 */
	public void testAddOfficeManagedObjectSourceInstance() {
		// Add two different managed objects verifying details
		this.replayMockObjects();
		OfficeManagedObjectSource moSource = this.node.addOfficeManagedObjectSource("MO",
				new ClassManagedObjectSource());
		assertNotNull("Must have managed object", moSource);
		assertEquals("Incorrect managed object name", "MO", moSource.getOfficeManagedObjectSourceName());
		assertNotSame("Should obtain another managed object", moSource,
				this.node.addOfficeManagedObjectSource("ANOTHER", new ClassManagedObjectSource()));
		this.verifyMockObjects();
	}

	/**
	 * Ensure issue if add the {@link OfficeManagedObjectSource} instance twice.
	 */
	public void testAddOfficeManagedObjectSourceInstanceTwice() {

		// Record issue in adding the managed object sources twice
		this.recordIssue("OFFICE.MO", ManagedObjectSourceNodeImpl.class, "Managed Object Source MO already added");

		// Add the managed object source twice
		this.replayMockObjects();
		OfficeManagedObjectSource moSourceFirst = this.node.addOfficeManagedObjectSource("MO",
				new ClassManagedObjectSource());
		OfficeManagedObjectSource moSourceSecond = this.node.addOfficeManagedObjectSource("MO",
				new ClassManagedObjectSource());
		this.verifyMockObjects();

		// Should be the same managed object source
		assertEquals("Should be same managed object on adding twice", moSourceFirst, moSourceSecond);
	}

	/**
	 * Tests adding an {@link OfficeManagedObject}.
	 */
	public void testAddOfficeManagedObject() {

		OfficeManagedObjectSource moSource = this.addManagedObjectSource(this.node, "MO", null);

		// Add two different managed objects verifying details
		this.replayMockObjects();
		OfficeManagedObject mo = moSource.addOfficeManagedObject("MO", ManagedObjectScope.PROCESS);
		assertNotNull("Must have managed object", mo);
		assertEquals("Incorrect managed object name", "MO", mo.getOfficeManagedObjectName());
		assertNotSame("Should obtain another managed object", mo,
				moSource.addOfficeManagedObject("ANOTHER", ManagedObjectScope.PROCESS));
		this.verifyMockObjects();
	}

	/**
	 * Ensure issue if add the {@link OfficeManagedObject} twice.
	 */
	public void testAddOfficeManagedObjectTwice() {

		// Record issue in adding the managed objects twice
		this.recordIssue("OFFICE.MO", ManagedObjectNodeImpl.class, "Managed Object MO already added");

		OfficeManagedObjectSource moSource = this.addManagedObjectSource(this.node, "MO_SOURCE", null);

		// Add the managed object twice
		this.replayMockObjects();
		OfficeManagedObject moFirst = moSource.addOfficeManagedObject("MO", ManagedObjectScope.THREAD);
		OfficeManagedObject moSecond = moSource.addOfficeManagedObject("MO", ManagedObjectScope.THREAD);
		this.verifyMockObjects();

		// Should be the same managed object
		assertEquals("Should be same managed object on adding twice", moFirst, moSecond);
	}

	/**
	 * Ensure issue if add the {@link OfficeManagedObject} twice by different
	 * {@link OfficeManagedObjectSource} instances.
	 */
	public void testAddOfficeManagedObjectTwiceByDifferentSources() {

		// Record issue in adding the managed objects twice
		this.recordIssue("OFFICE.MO", ManagedObjectNodeImpl.class, "Managed Object MO already added");

		OfficeManagedObjectSource moSourceOne = this.addManagedObjectSource(this.node, "MO_SOURCE_ONE", null);
		OfficeManagedObjectSource moSourceTwo = this.addManagedObjectSource(this.node, "MO_SOURCE_TWO", null);

		// Add the managed object twice by different sources
		this.replayMockObjects();
		OfficeManagedObject moFirst = moSourceOne.addOfficeManagedObject("MO", ManagedObjectScope.THREAD);
		OfficeManagedObject moSecond = moSourceTwo.addOfficeManagedObject("MO", ManagedObjectScope.THREAD);
		this.verifyMockObjects();

		// Should be the same managed object
		assertEquals("Should be same managed object on adding twice", moFirst, moSecond);
	}

	/**
	 * Tests adding an {@link OfficeEscalation}.
	 */
	public void testAddOfficeEscalation() {
		// Add two different escalations verifying details
		this.replayMockObjects();
		OfficeEscalation escalation = this.node.addOfficeEscalation("java.sql.SQLException");
		assertNotNull("Must have escalation", escalation);
		assertEquals("Incorrect escalation type", "java.sql.SQLException", escalation.getOfficeEscalationType());
		assertNotSame("Should obtain another escalation", escalation,
				this.node.addOfficeEscalation("java.io.IOException"));
		this.verifyMockObjects();
	}

	/**
	 * Ensure issue if add the {@link OfficeEscalation} twice.
	 */
	public void testAddOfficeEscalationTwice() {

		// Record issue in adding the escalation twice
		this.recordIssue("OFFICE.java_sql_SQLException", EscalationNodeImpl.class,
				"Escalation java.sql.SQLException already added");

		// Add the escalation twice
		this.replayMockObjects();
		OfficeEscalation escalationFirst = this.node.addOfficeEscalation("java.sql.SQLException");
		OfficeEscalation escalationSecond = this.node.addOfficeEscalation("java.sql.SQLException");
		this.verifyMockObjects();

		// Should be the same escalation
		assertEquals("Should be same escalation on adding twice", escalationFirst, escalationSecond);
	}

	/**
	 * Tests adding an {@link OfficeStart}.
	 */
	public void testAddOfficeStart() {
		// Add two different escalations verifying details
		this.replayMockObjects();
		OfficeStart start = this.node.addOfficeStart("START");
		assertNotNull("Must have start", start);
		assertEquals("Incorrect start name", "START", start.getOfficeStartName());
		assertNotSame("Should obtain another start", start, this.node.addOfficeStart("ANOTHER"));
		this.verifyMockObjects();
	}

	/**
	 * Ensure issue if add the {@link OfficeStart} twice.
	 */
	public void testAddOfficeStartTwice() {

		// Record issue in adding the start twice
		this.recordIssue("OFFICE.START", OfficeStartNodeImpl.class, "Office Start START already added");

		// Add the start twice
		this.replayMockObjects();
		OfficeStart startFirst = this.node.addOfficeStart("START");
		OfficeStart startSecond = this.node.addOfficeStart("START");
		this.verifyMockObjects();

		// Should be the same start
		assertEquals("Should be same start on adding twice", startFirst, startSecond);
	}

	/**
	 * Tests adding an {@link OfficeGovernance}.
	 */
	public void testAddOfficeGovernance() {
		// Add two different governances verifying details
		this.replayMockObjects();
		OfficeGovernance gov = this.addGovernance(this.node, "GOVERNANCE", null);
		assertNotNull("Must have governance", gov);
		assertEquals("Incorrect governance name", "GOVERNANCE", gov.getOfficeGovernanceName());
		assertNotSame("Should obtain another governance", gov, this.addGovernance(this.node, "ANOTHER", null));
		this.verifyMockObjects();
	}

	/**
	 * Ensure issue if add the {@link OfficeGovernance} twice.
	 */
	public void testAddOfficeGovernanceTwice() {

		// Record issue in adding the governance twice
		this.recordIssue("OFFICE.GOVERNANCE", GovernanceNodeImpl.class, "Governance GOVERNANCE already added");

		// Add the governance twice
		this.replayMockObjects();
		OfficeGovernance govFirst = this.addGovernance(this.node, "GOVERNANCE", null);
		OfficeGovernance govSecond = this.addGovernance(this.node, "GOVERNANCE", null);
		this.verifyMockObjects();

		// Should be the same governance
		assertEquals("Should be same governance on adding twice", govFirst, govSecond);
	}

	/**
	 * Tests adding an {@link OfficeGovernance} instance.
	 */
	public void testAddOfficeGovernanceInstance() {
		// Add two different governances verifying details
		this.replayMockObjects();
		OfficeGovernance gov = this.node.addOfficeGovernance("GOVERNANCE", new ClassGovernanceSource());
		assertNotNull("Must have governance", gov);
		assertEquals("Incorrect governance name", "GOVERNANCE", gov.getOfficeGovernanceName());
		assertNotSame("Should obtain another governance", gov,
				this.node.addOfficeGovernance("ANOTHER", new ClassGovernanceSource()));
		this.verifyMockObjects();
	}

	/**
	 * Ensure issue if add the {@link OfficeGovernance} instance twice.
	 */
	public void testAddOfficeGovernanceInstanceTwice() {

		// Record issue in adding the governance twice
		this.recordIssue("OFFICE.GOVERNANCE", GovernanceNodeImpl.class, "Governance GOVERNANCE already added");

		// Add the governance twice
		this.replayMockObjects();
		OfficeGovernance govFirst = this.node.addOfficeGovernance("GOVERNANCE", new ClassGovernanceSource());
		OfficeGovernance govSecond = this.node.addOfficeGovernance("GOVERNANCE", new ClassGovernanceSource());
		this.verifyMockObjects();

		// Should be the same governance
		assertEquals("Should be same governance on adding twice", govFirst, govSecond);
	}

	/**
	 * Tests adding an {@link OfficeAdministration}.
	 */
	public void testAddOfficeAdministration() {
		final AdministrationFactory<?, ?, ?> factory = this.createMock(AdministrationFactory.class);

		// Add two different administrations verifying details
		this.replayMockObjects();
		OfficeAdministration admin = this.addAdministration(this.node, "ADMIN", Connection.class, factory, null);
		assertNotNull("Must have administration", admin);
		assertEquals("Incorrect administration name", "ADMIN", admin.getOfficeAdministrationName());
		assertNotSame("Should obtain another administration", admin,
				this.addAdministration(this.node, "ANOTHER", Connection.class, factory, null));
		this.verifyMockObjects();
	}

	/**
	 * Ensure issue if add the {@link OfficeAdministration} twice.
	 */
	public void testAddOfficeAdministrationTwice() {
		final AdministrationFactory<?, ?, ?> factory = this.createMock(AdministrationFactory.class);

		// Record issue in adding the administration twice
		this.recordIssue("OFFICE.ADMIN", AdministrationNodeImpl.class, "Administration ADMIN already added");

		// Add the administration twice
		this.replayMockObjects();
		OfficeAdministration adminFirst = this.addAdministration(this.node, "ADMIN", Connection.class, factory, null);
		OfficeAdministration adminSecond = this.addAdministration(this.node, "ADMIN", Connection.class, factory, null);
		this.verifyMockObjects();

		// Should be the same administration
		assertEquals("Should be same administration on adding twice", adminFirst, adminSecond);
	}

	/**
	 * Tests adding an {@link OfficeAdministration} instance.
	 */
	public void testAddOfficeAdministrationInstance() {
		// Add two different administrations verifying details
		this.replayMockObjects();
		OfficeAdministration admin = this.node.addOfficeAdministration("ADMIN", new ClassAdministrationSource());
		assertNotNull("Must have administration", admin);
		assertEquals("Incorrect administration name", "ADMIN", admin.getOfficeAdministrationName());
		assertNotSame("Should obtain another administration", admin,
				this.node.addOfficeAdministration("ANOTHER", new ClassAdministrationSource()));
		this.verifyMockObjects();
	}

	/**
	 * Ensure issue if add the {@link OfficeAdministration} instance twice.
	 */
	public void testAddOfficeAdministrationInstanceTwice() {

		// Record issue in adding the administration twice
		this.recordIssue("OFFICE.ADMIN", AdministrationNodeImpl.class, "Administration ADMIN already added");

		// Add the administration twice
		this.replayMockObjects();
		OfficeAdministration adminFirst = this.node.addOfficeAdministration("ADMIN", new ClassAdministrationSource());
		OfficeAdministration adminSecond = this.node.addOfficeAdministration("ADMIN", new ClassAdministrationSource());
		this.verifyMockObjects();

		// Should be the same administration
		assertEquals("Should be same administration on adding twice", adminFirst, adminSecond);
	}

	/**
	 * Ensure can link {@link OfficeInput} to the {@link SectionInput}.
	 */
	public void testLinkInputToSectionInput() {

		// Record already being linked
		this.recordIssue("OFFICE.INPUT", OfficeInputNodeImpl.class, "Office Input INPUT linked more than once");

		this.replayMockObjects();

		// Link
		OfficeInput input = this.node.addOfficeInput("INPUT", Integer.class.getName());
		OfficeSection section = this.addSection(this.node, "SECTION", null);
		OfficeSectionInput sectionInput = section.getOfficeSectionInput("SECTION_INPUT");
		assertEquals("Incorrect office section input", "SECTION_INPUT", sectionInput.getOfficeSectionInputName());

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
		this.recordIssue("OFFICE.SECTION.SECTION_OUTPUT", SectionOutputNodeImpl.class,
				"Section Output SECTION_OUTPUT linked more than once");

		this.replayMockObjects();

		// Link
		OfficeSection section = this.addSection(this.node, "SECTION", null);
		OfficeSectionOutput sectionOutput = section.getOfficeSectionOutput("SECTION_OUTPUT");
		OfficeOutput output = this.node.addOfficeOutput("OUTPUT", Integer.class.getName());

		this.node.link(sectionOutput, output);
		assertFlowLink("section output -> output", sectionOutput, output);

		// Ensure only can link once
		this.node.link(sectionOutput, this.node.addOfficeOutput("ANOTHER", Character.class.getName()));
		assertFlowLink("Can only link once", sectionOutput, output);

		this.verifyMockObjects();
	}

	/**
	 * Ensure can link the {@link ResponsibleTeam} to an {@link OfficeTeam}.
	 */
	public void testLinkFunctionTeamToOfficeTeam() {

		// Record already being linked
		this.recordIssue("OFFICE.SECTION.FUNCTION.TEAM", ResponsibleTeamNodeImpl.class,
				"Responsible Team TEAM linked more than once");

		this.replayMockObjects();

		// Add section with function
		OfficeSection section = this.addSection(this.node, "SECTION", null);
		OfficeSectionFunction function = section.getOfficeSectionFunction("FUNCTION");
		assertEquals("Incorrect office function", "FUNCTION", function.getOfficeFunctionName());

		// Link
		ResponsibleTeam team = function.getResponsibleTeam();
		OfficeTeam officeTeam = this.node.addOfficeTeam("TEAM");
		this.node.link(team, officeTeam);
		assertTeamLink("function team -> office team", team, officeTeam);

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
		this.recordIssue("OFFICE.GOVERNANCE", GovernanceNodeImpl.class, "Governance GOVERNANCE linked more than once");

		this.replayMockObjects();

		// Link
		OfficeGovernance gov = this.addGovernance(this.node, "GOVERNANCE", null);
		OfficeTeam officeTeam = this.node.addOfficeTeam("OFFICE_TEAM");
		this.node.link(gov, officeTeam);
		assertTeamLink("governance -> office team", gov, officeTeam);

		// Ensure only can link once
		this.node.link(gov, this.node.addOfficeTeam("ANOTHER"));
		assertTeamLink("Can only link once", gov, officeTeam);

		this.verifyMockObjects();
	}

	/**
	 * Ensures can link {@link OfficeAdministration} to the {@link OfficeTeam}.
	 */
	public void testLinkOfficeAdministrationToOfficeTeam() {
		final AdministrationFactory<?, ?, ?> factory = this.createMock(AdministrationFactory.class);

		// Record already being linked
		this.recordIssue("OFFICE.ADMIN", AdministrationNodeImpl.class, "Administration ADMIN linked more than once");

		this.replayMockObjects();

		// Link
		OfficeAdministration admin = this.addAdministration(this.node, "ADMIN", Connection.class, factory, null);
		OfficeTeam officeTeam = this.node.addOfficeTeam("OFFICE_TEAM");
		this.node.link(admin, officeTeam);
		assertTeamLink("administration -> office team", admin, officeTeam);

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
		OfficeGovernance governance = this.addGovernance(this.node, "GOVERNANCE", null);
		section.addGovernance(governance);
		// TODO test that governance specified

		// May have many governances
		OfficeGovernance another = this.addGovernance(this.node, "ANOTHER", null);
		section.addGovernance(another);

		this.verifyMockObjects();
	}

	/**
	 * Ensure can specify {@link OfficeGovernance} for a {@link OfficeSubSection}.
	 */
	public void testLinkOfficeGovernanceForOfficeSubSection() {

		this.replayMockObjects();

		// Add section with sub-section
		OfficeSection section = this.addSection(this.node, "SECTION", null);
		OfficeSubSection subSection = section.getOfficeSubSection("SUB_SECTION");

		// Link
		OfficeGovernance governance = this.addGovernance(this.node, "GOVERNANCE", null);
		subSection.addGovernance(governance);
		// TODO test that governance specified

		// May have many governances
		OfficeGovernance another = this.addGovernance(this.node, "ANOTHER", null);
		subSection.addGovernance(another);

		this.verifyMockObjects();
	}

	/**
	 * Ensure can specify {@link OfficeGovernance} for a specific
	 * {@link OfficeSectionFunction}.
	 */
	public void testLinkOfficeGovernanceForOfficeFunction() {

		this.replayMockObjects();

		// Add section with function
		OfficeSection section = this.addSection(this.node, "SECTION", null);
		OfficeSectionFunction function = section.getOfficeSectionFunction("FUNCTION");

		// Link
		OfficeGovernance governance = this.addGovernance(this.node, "GOVERNANCE", null);
		function.addGovernance(governance);
		// TODO test that governance specified

		// May have many governances
		OfficeGovernance another = this.addGovernance(this.node, "ANOTHER", null);
		function.addGovernance(another);

		this.verifyMockObjects();
	}

	/**
	 * Ensure can specify pre {@link OfficeAdministration} for the
	 * {@link OfficeSectionFunction}.
	 */
	public void testLinkPreAdministrationForOfficeFunction() {
		final AdministrationFactory<?, ?, ?> factory = this.createMock(AdministrationFactory.class);

		this.replayMockObjects();

		// Add section with function
		OfficeSection section = this.addSection(this.node, "SECTION", null);
		OfficeSectionFunction function = section.getOfficeSectionFunction("FUNCTION");

		// Link
		OfficeAdministration administration = this.addAdministration(this.node, "ADMINISTRATION", Connection.class,
				factory, null);
		assertEquals("Incorrect administration name", "ADMINISTRATION", administration.getOfficeAdministrationName());
		function.addPreAdministration(administration);
		// TODO test that pre administration specified

		// May have many pre administration
		function.addPreAdministration(this.addAdministration(this.node, "ANOTHER", Connection.class, factory, null));

		this.verifyMockObjects();
	}

	/**
	 * Ensure can specify post {@link OfficeAdministration} for the
	 * {@link OfficeSectionFunction}.
	 */
	public void testLinkPostAdministrationForOfficeFunction() {
		final AdministrationFactory<?, ?, ?> factory = this.createMock(AdministrationFactory.class);

		this.replayMockObjects();

		// Add section with function
		OfficeSection section = this.addSection(this.node, "SECTION", null);
		OfficeSectionFunction function = section.getOfficeSectionFunction("FUNCTION");

		// Link
		OfficeAdministration administration = this.addAdministration(this.node, "ADMINISTRATION_A", Connection.class,
				factory, null);
		function.addPostAdministration(administration);
		// TODO test that post administration specified

		// May have many post administrations
		function.addPostAdministration(
				this.addAdministration(this.node, "ADMINISTRATION_B", Connection.class, factory, null));

		this.verifyMockObjects();
	}

	/**
	 * Ensure can specify pre-load {@link Administration} for {@link OfficeObject}.
	 */
	public void testLinkPreLoadAdministrationForOfficeObject() {
		final AdministrationFactory<?, ?, ?> factory = this.createMock(AdministrationFactory.class);

		this.replayMockObjects();

		// Add office object
		OfficeObject object = this.node.addOfficeObject("MO", Connection.class.getName());

		// Link
		OfficeAdministration administration = this.addAdministration(this.node, "ADMINISTRATION_A", Connection.class,
				factory, null);
		object.addPreLoadAdministration(administration);
		// TODO test that pre-load administration specified

		// May have many pre-load administrations
		object.addPreLoadAdministration(
				this.addAdministration(this.node, "ADMINISTRATION_B", Connection.class, factory, null));

		this.verifyMockObjects();
	}

	/**
	 * Ensure can specify pre-load {@link Administration} for
	 * {@link OfficeManagedObject}.
	 */
	public void testLinkPreLoadAdministrationForOfficeManagedObject() {
		final AdministrationFactory<?, ?, ?> factory = this.createMock(AdministrationFactory.class);

		this.replayMockObjects();

		// Add office managed object
		OfficeManagedObjectSource moSource = this.addManagedObjectSource(this.node, "MO", null);
		OfficeManagedObject mo = moSource.addOfficeManagedObject("MO", ManagedObjectScope.PROCESS);

		// Link
		OfficeAdministration administration = this.addAdministration(this.node, "ADMINISTRATION_A", Connection.class,
				factory, null);
		mo.addPreLoadAdministration(administration);
		// TODO test that pre-load administration specified

		// May have many pre-load administrations
		mo.addPreLoadAdministration(
				this.addAdministration(this.node, "ADMINISTRATION_B", Connection.class, factory, null));

		this.verifyMockObjects();
	}

	/**
	 * Ensure can specify pre-load {@link Administration} for
	 * {@link OfficeSectionManagedObject}.
	 */
	public void testLinkPreLoadAdministrationForOfficeSectionManagedObject() {
		final AdministrationFactory<?, ?, ?> factory = this.createMock(AdministrationFactory.class);

		this.replayMockObjects();

		// Add office section managed object
		OfficeSection section = this.addSection(this.node, "SECTION", null);
		OfficeSectionManagedObject mo = section.getOfficeSectionManagedObject("MO");

		// Link
		OfficeAdministration administration = this.addAdministration(this.node, "ADMINISTRATION_A", Connection.class,
				factory, null);
		mo.addPreLoadAdministration(administration);
		// TODO test that pre-load administration specified

		// May have many pre-load administrations
		mo.addPreLoadAdministration(
				this.addAdministration(this.node, "ADMINISTRATION_B", Connection.class, factory, null));

		this.verifyMockObjects();
	}

	/**
	 * Ensure can govern {@link OfficeSectionManagedObject}.
	 */
	public void testGovernOfficeSectionManagedObject() {
		this.replayMockObjects();

		// Add section with governance
		OfficeSection section = this.addSection(this.node, "SECTION", null);
		OfficeSectionManagedObject mo = section.getOfficeSectionManagedObject("MO");

		// Link
		OfficeGovernance governance = this.addGovernance(this.node, "GOVERNANCE", null);
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
		OfficeGovernance governance = this.addGovernance(this.node, "GOVERNANCE", null);
		OfficeManagedObjectSource moSource = this.addManagedObjectSource(this.node, "MO_SOURCE", null);
		OfficeManagedObject mo = moSource.addOfficeManagedObject("MO", ManagedObjectScope.THREAD);
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
		OfficeGovernance governance = this.addGovernance(this.node, "GOVERNANCE", null);
		OfficeObject mo = this.node.addOfficeObject("MO", Connection.class.getName());
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
		final AdministrationFactory<?, ?, ?> factory = this.createMock(AdministrationFactory.class);

		this.replayMockObjects();

		// Add section with managed object
		OfficeSection section = this.addSection(this.node, "SECTION", null);
		OfficeSectionManagedObject mo = section.getOfficeSectionManagedObject("MO");

		// Link
		OfficeAdministration administration = this.addAdministration(this.node, "ADMINISTRATION", Connection.class,
				factory, null);
		administration.administerManagedObject(mo);
		// TODO test that administering the section managed object

		// Should be able to administer the managed object twice
		administration.administerManagedObject(mo);

		this.verifyMockObjects();
	}

	/**
	 * Ensure can administer {@link OfficeManagedObject}.
	 */
	public void testAdministerOfficeManagedObject() {
		final AdministrationFactory<?, ?, ?> factory = this.createMock(AdministrationFactory.class);

		this.replayMockObjects();

		// Link
		OfficeAdministration administration = this.addAdministration(this.node, "ADMINISTRATION", Connection.class,
				factory, null);
		OfficeManagedObjectSource moSource = this.addManagedObjectSource(this.node, "MO_SOURCE", null);
		OfficeManagedObject mo = moSource.addOfficeManagedObject("MO", ManagedObjectScope.THREAD);
		administration.administerManagedObject(mo);
		// TODO test that administering the section managed object

		// Should be able to administer the managed object twice
		administration.administerManagedObject(mo);

		this.verifyMockObjects();
	}

	/**
	 * Ensure can administer {@link OfficeObject}.
	 */
	public void testAdministerOfficeObject() {
		final AdministrationFactory<?, ?, ?> factory = this.createMock(AdministrationFactory.class);

		this.replayMockObjects();

		// Link
		OfficeAdministration administration = this.addAdministration(this.node, "ADMINISTRATION", Connection.class,
				factory, null);
		OfficeObject mo = this.node.addOfficeObject("MO", Connection.class.getName());
		administration.administerManagedObject(mo);
		// TODO test that administering the section managed object

		// Should be able to administer the managed object twice
		administration.administerManagedObject(mo);

		this.verifyMockObjects();
	}

	/**
	 * Ensure can link {@link OfficeSectionObject} to the
	 * {@link OfficeManagedObject}.
	 */
	public void testLinkOfficeSectionObjectToOfficeManagedObject() {

		// Record already being linked
		this.recordIssue("OFFICE.SECTION.SECTION_OBJECT", SectionObjectNodeImpl.class,
				"Section Object SECTION_OBJECT linked more than once");

		this.replayMockObjects();

		// Add section with section object
		OfficeSection section = this.addSection(this.node, "SECTION", null);
		OfficeSectionObject sectionObject = section.getOfficeSectionObject("SECTION_OBJECT");
		assertEquals("Incorrect section object", "SECTION_OBJECT", sectionObject.getOfficeSectionObjectName());

		// Link
		OfficeManagedObjectSource moSource = this.addManagedObjectSource(this.node, "MO_SOURCE", null);
		OfficeManagedObject mo = moSource.addOfficeManagedObject("MO", ManagedObjectScope.PROCESS);
		this.node.link(sectionObject, mo);
		assertObjectLink("section object -> office managed object", sectionObject, mo);

		// Ensure only can link once
		this.node.link(sectionObject, moSource.addOfficeManagedObject("ANOTHER", ManagedObjectScope.PROCESS));
		assertObjectLink("Can only link once", sectionObject, mo);

		this.verifyMockObjects();
	}

	/**
	 * Ensure can link {@link OfficeSectionObject} to the {@link OfficeObject}.
	 */
	public void testLinkOfficeSectionObjectToOfficeObject() {

		// Record already being linked
		this.recordIssue("OFFICE.SECTION.SECTION_OBJECT", SectionObjectNodeImpl.class,
				"Section Object SECTION_OBJECT linked more than once");

		this.replayMockObjects();

		// Add section with section object
		OfficeSection section = this.addSection(this.node, "SECTION", null);
		OfficeSectionObject sectionObject = section.getOfficeSectionObject("SECTION_OBJECT");

		// Link
		OfficeObject mo = this.node.addOfficeObject("MO", Connection.class.getName());
		this.node.link(sectionObject, mo);
		assertObjectLink("section object -> office floor managed object", sectionObject, mo);

		// Ensure only can link once
		this.node.link(sectionObject, this.node.addOfficeObject("ANOTHER", Connection.class.getName()));
		assertObjectLink("Can only link once", sectionObject, mo);

		this.verifyMockObjects();
	}

	/**
	 * Ensures can link {@link OfficeManagedObjectSource} {@link ManagedObjectTeam}
	 * to the {@link OfficeTeam}.
	 */
	public void testLinkOfficeManagedObjectSourceTeamToOfficeTeam() {

		// Record already being linked
		this.recordIssue("OFFICE.MO.TEAM", ManagedObjectTeamNodeImpl.class,
				"Managed Object Source Team TEAM linked more than once");

		this.replayMockObjects();

		// Link
		OfficeManagedObjectSource moSource = this.addManagedObjectSource(this.node, "MO", null);
		OfficeManagedObjectTeam team = moSource.getOfficeManagedObjectTeam("TEAM");
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

		final ManagedFunctionFactory<None, None> functionFactory = this.createMock(ManagedFunctionFactory.class);

		// Record already being linked
		this.recordIssue("OFFICE.SECTION.MO_SOURCE.TEAM", ManagedObjectTeamNodeImpl.class,
				"Managed Object Source Team TEAM linked more than once");

		this.replayMockObjects();

		// Add section with managed object requiring a team
		OfficeSection section = this.addSection(this.node, "SECTION", new SectionMaker() {
			@Override
			public void make(SectionMakerContext context) {
				context.addManagedObjectSource("MO_SOURCE", new ManagedObjectMaker() {
					@Override
					public void make(ManagedObjectMakerContext context) {
						ManagedObjectFunctionBuilder<None, None> function = context.getContext()
								.getManagedObjectSourceContext().addManagedFunction("FUNCTION", functionFactory);
						function.setResponsibleTeam("TEAM");
					}
				});
			}
		});
		OfficeSectionManagedObjectSource moSource = section.getOfficeSectionManagedObjectSource("MO_SOURCE");
		assertNotNull("Should have managed object source", moSource);
		assertEquals("Incorrect section managed object source", "MO_SOURCE",
				moSource.getOfficeSectionManagedObjectSourceName());
		OfficeSectionManagedObjectTeam team = moSource.getOfficeSectionManagedObjectTeam("TEAM");
		assertEquals("Incorrect section managed object source team", "TEAM", team.getManagedObjectTeamName());

		// Link
		OfficeTeam officeTeam = this.node.addOfficeTeam("OFFICE_TEAM");
		this.node.link(team, officeTeam);
		assertTeamLink("managed object source team -> office team", team, officeTeam);

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
		this.recordIssue("OFFICE.MO.DEPENDENCY", ManagedObjectDependencyNodeImpl.class,
				"Managed Object Dependency DEPENDENCY linked more than once");

		this.replayMockObjects();

		// Link
		OfficeManagedObjectSource moSource = this.addManagedObjectSource(this.node, "MO_SOURCE", null);
		OfficeManagedObject mo = moSource.addOfficeManagedObject("MO", ManagedObjectScope.THREAD);
		OfficeManagedObjectDependency dependency = mo.getOfficeManagedObjectDependency("DEPENDENCY");
		OfficeManagedObjectSource moSourceTarget = this.addManagedObjectSource(this.node, "MO_SOURCE_TARGET", null);
		OfficeManagedObject moTarget = moSourceTarget.addOfficeManagedObject("MO_TARGET", ManagedObjectScope.THREAD);
		this.node.link(dependency, moTarget);
		assertObjectLink("managed object dependency -> office managed object", dependency, moTarget);

		// Ensure only can link once
		this.node.link(dependency, moSourceTarget.addOfficeManagedObject("ANOTHER", ManagedObjectScope.THREAD));
		assertObjectLink("Can only link once", dependency, moTarget);

		this.verifyMockObjects();
	}

	/**
	 * Ensures can link {@link ManagedObjectDependency} to the {@link OfficeObject}.
	 */
	public void testLinkManagedObjectDependencyToOfficeObject() {

		// Record already being linked
		this.recordIssue("OFFICE.MO.DEPENDENCY", ManagedObjectDependencyNodeImpl.class,
				"Managed Object Dependency DEPENDENCY linked more than once");

		this.replayMockObjects();

		// Link
		OfficeManagedObjectSource moSource = this.addManagedObjectSource(this.node, "MO_SOURCE", null);
		OfficeManagedObject mo = moSource.addOfficeManagedObject("MO", ManagedObjectScope.FUNCTION);
		OfficeManagedObjectDependency dependency = mo.getOfficeManagedObjectDependency("DEPENDENCY");
		OfficeObject moTarget = this.node.addOfficeObject("MO_TARGET", Connection.class.getName());
		this.node.link(dependency, moTarget);
		assertObjectLink("managed object dependency -> office floor managed object", dependency, moTarget);

		// Ensure only can link once
		this.node.link(dependency, this.node.addOfficeObject("ANOTHER", String.class.getName()));
		assertObjectLink("Can only link once", dependency, moTarget);

		this.verifyMockObjects();
	}

	/**
	 * Ensure can link Input {@link ManagedObjectDependency} to the
	 * {@link OfficeManagedObject}.
	 */
	public void testLinkInputManagedObjectDependencyToOfficeManagedObject() {

		// Record already being linked
		this.recordIssue("OFFICE.MO_SOURCE.DEPENDENCY", ManagedObjectDependencyNodeImpl.class,
				"Managed Object Dependency DEPENDENCY linked more than once");

		this.replayMockObjects();

		// Link
		OfficeManagedObjectSource moSource = this.addManagedObjectSource(this.node, "MO_SOURCE", null);
		OfficeManagedObjectDependency dependency = moSource.getInputOfficeManagedObjectDependency("DEPENDENCY");
		OfficeManagedObjectSource moSourceTarget = this.addManagedObjectSource(this.node, "MO_SOURCE_TARGET", null);
		OfficeManagedObject moTarget = moSourceTarget.addOfficeManagedObject("MO_TARGET", ManagedObjectScope.PROCESS);
		this.node.link(dependency, moTarget);
		assertObjectLink("input managed object dependency -> office floor managed object", dependency, moTarget);

		// Ensure only can link once
		this.node.link(dependency, moSourceTarget.addOfficeManagedObject("ANOTHER", ManagedObjectScope.PROCESS));
		assertObjectLink("Can only link once", dependency, moTarget);

		this.verifyMockObjects();
	}

	/**
	 * Ensure can link Input {@link ManagedObjectDependency} to the
	 * {@link OfficeObject}.
	 */
	public void testLinkInputManagedObjectDependencyToOfficeObject() {

		// Record already being linked
		this.recordIssue("OFFICE.MO_SOURCE.DEPENDENCY", ManagedObjectDependencyNodeImpl.class,
				"Managed Object Dependency DEPENDENCY linked more than once");

		this.replayMockObjects();

		// Link
		OfficeManagedObjectSource moSource = this.addManagedObjectSource(this.node, "MO_SOURCE", null);
		OfficeManagedObjectDependency dependency = moSource.getInputOfficeManagedObjectDependency("DEPENDENCY");
		OfficeObject moTarget = this.node.addOfficeObject("MO_TARGET", Connection.class.getName());
		this.node.link(dependency, moTarget);
		assertObjectLink("input managed object dependency -> office floor managed object", dependency, moTarget);

		// Ensure only can link once
		this.node.link(dependency, this.node.addOfficeObject("ANOTHER", Connection.class.getName()));
		assertObjectLink("Can only link once", dependency, moTarget);

		this.verifyMockObjects();
	}

	/**
	 * Ensures can link {@link ManagedObjectFlow} to the {@link OfficeSectionInput}.
	 */
	public void testLinkManagedObjectFlowToOfficeSectionInput() {

		// Record already being linked
		this.recordIssue("OFFICE.MO.FLOW", ManagedObjectFlowNodeImpl.class,
				"Managed Object Source Flow FLOW linked more than once");

		this.replayMockObjects();

		// Add section with section inputs
		OfficeSection section = this.addSection(this.node, "SECTION", null);
		// Obtain section input (should be ordered)
		OfficeSectionInput sectionInput = section.getOfficeSectionInput("SECTION_INPUT");
		assertEquals("Incorrect office section input", "SECTION_INPUT", sectionInput.getOfficeSectionInputName());

		// Link
		OfficeManagedObjectSource moSource = this.addManagedObjectSource(this.node, "MO", null);
		OfficeManagedObjectFlow flow = moSource.getOfficeManagedObjectFlow("FLOW");
		this.node.link(flow, sectionInput);
		assertFlowLink("managed object source flow -> section input", flow, sectionInput);

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
		this.recordIssue("OFFICE.OUTPUT_SECTION.SECTION_OUTPUT", SectionOutputNodeImpl.class,
				"Section Output SECTION_OUTPUT linked more than once");

		this.replayMockObjects();

		// Add section with section outputs
		OfficeSection outputSection = this.addSection(this.node, "OUTPUT_SECTION", null);
		OfficeSectionOutput sectionOutput = outputSection.getOfficeSectionOutput("SECTION_OUTPUT");
		assertEquals("Incorrect office section input", "SECTION_OUTPUT", sectionOutput.getOfficeSectionOutputName());

		// Add section with section inputs
		OfficeSection inputSection = this.addSection(this.node, "INPUT_SECTION", null);
		OfficeSectionInput sectionInput = inputSection.getOfficeSectionInput("SECTION_INPUT");
		assertEquals("Incorrect office section input", "SECTION_INPUT", sectionInput.getOfficeSectionInputName());

		// Link
		this.node.link(sectionOutput, sectionInput);
		assertFlowLink("section output -> section input", sectionOutput, sectionInput);

		// Ensure only can link once
		this.node.link(sectionOutput, inputSection.getOfficeSectionInput("ANOTHER"));
		assertFlowLink("Can only link once", sectionOutput, sectionInput);

		this.verifyMockObjects();
	}

	/**
	 * Ensures can link {@link OfficeEscalation} to the {@link OfficeSectionInput}.
	 */
	public void testLinkOfficeEscalationToOfficeSectionInput() {

		// Record already being linked
		this.recordIssue("OFFICE.java_sql_SQLException", EscalationNodeImpl.class,
				"Escalation java.sql.SQLException linked more than once");

		this.replayMockObjects();

		// Add escalation
		OfficeEscalation escalation = this.node.addOfficeEscalation("java.sql.SQLException");
		assertEquals("Incorrect office escalation", "java.sql.SQLException", escalation.getOfficeEscalationType());

		// Add section with section inputs
		OfficeSection inputSection = this.addSection(this.node, "INPUT_SECTION", null);
		OfficeSectionInput sectionInput = inputSection.getOfficeSectionInput("SECTION_INPUT");
		assertEquals("Incorrect office section input", "SECTION_INPUT", sectionInput.getOfficeSectionInputName());

		// Link
		this.node.link(escalation, sectionInput);
		assertFlowLink("escalation -> section input", escalation, sectionInput);

		// Ensure only can link once
		this.node.link(escalation, inputSection.getOfficeSectionInput("ANOTHER"));
		assertFlowLink("Can only link once", escalation, sectionInput);

		this.verifyMockObjects();
	}

	/**
	 * Ensures can link {@link OfficeStart} to the {@link OfficeSectionInput}.
	 */
	public void testLinkOfficeStartToOfficeSectionInput() {

		// Record already being linked
		this.recordIssue("OFFICE.START", OfficeStartNodeImpl.class, "Office Start START linked more than once");

		this.replayMockObjects();

		// Add start
		OfficeStart start = this.node.addOfficeStart("START");
		assertEquals("Incorrect office start", "START", start.getOfficeStartName());

		// Add section with section inputs
		OfficeSection inputSection = this.addSection(this.node, "INPUT_SECTION", null);
		OfficeSectionInput sectionInput = inputSection.getOfficeSectionInput("SECTION_INPUT");
		assertEquals("Incorrect office section input", "SECTION_INPUT", sectionInput.getOfficeSectionInputName());

		// Link
		this.node.link(start, sectionInput);
		assertFlowLink("start -> section input", start, sectionInput);

		// Ensure only can link once
		this.node.link(start, inputSection.getOfficeSectionInput("ANOTHER"));
		assertFlowLink("Can only link once", start, sectionInput);

		this.verifyMockObjects();
	}

	/**
	 * Records the issue with qualified name resolution.
	 * 
	 * @param name     Expected name.
	 * @param nodeType Expected {@link Node} {@link Class}.
	 * @param message  Expected message.
	 */
	private void recordIssue(String name, Class<? extends Node> nodeType, String message) {
		this.recordReturn(this.officeFloor, this.officeFloor.getQualifiedName(OFFICE_NAME), OFFICE_NAME);
		this.issues.recordIssue(name, nodeType, message);
	}

}
