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
import net.officefloor.compile.issues.CompilerIssues;
import net.officefloor.compile.issues.CompilerIssues.LocationType;
import net.officefloor.compile.spi.office.ObjectDependency;
import net.officefloor.compile.spi.office.OfficeAdministrator;
import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.compile.spi.office.OfficeFloorManagedObject;
import net.officefloor.compile.spi.office.OfficeManagedObject;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.spi.office.OfficeTeam;
import net.officefloor.frame.api.manage.Office;

/**
 * Tests the {@link OfficeArchitect}.
 * 
 * @author Daniel
 */
public class OfficeNodeTest extends AbstractStructureTestCase {

	/**
	 * Location of the {@link Office} being built.
	 */
	private static final String OFFICE_LOCATION = "OFFICE_LOCATION";

	/**
	 * {@link CompilerIssues}.
	 */
	private final CompilerIssues issues = this.createMock(CompilerIssues.class);

	/**
	 * {@link OfficeNode} to be tested.
	 */
	private final OfficeNode node = new OfficeNodeImpl(OFFICE_LOCATION,
			this.issues);

	/**
	 * Tests adding an {@link OfficeFloorManagedObject}.
	 */
	public void testAddOfficeFloorManagedObject() {
		// Add two different managed objects verifying details
		this.replayMockObjects();
		OfficeFloorManagedObject mo = this.node.addOfficeFloorManagedObject(
				"MO", Connection.class.getName());
		assertNotNull("Must have managed object", mo);
		assertEquals("Incorrect managed object name", "MO", mo
				.getOfficeManagedObjectName());
		assertNotSame("Should obtain another managed object", mo, this.node
				.addOfficeFloorManagedObject("ANOTHER", String.class.getName()));
		this.verifyMockObjects();
	}

	/**
	 * Ensure issue if add the {@link OfficeFloorManagedObject} twice.
	 */
	public void testAddOfficeFloorManagedObjectTwice() {

		// Record issue in adding the managed object twice
		this.record_issue("Office floor managed object MO already added");

		// Add the managed object twice
		this.replayMockObjects();
		OfficeFloorManagedObject moFirst = this.node
				.addOfficeFloorManagedObject("MO", Connection.class.getName());
		OfficeFloorManagedObject moSecond = this.node
				.addOfficeFloorManagedObject("MO", String.class.getName());
		this.verifyMockObjects();

		// Should be the managed object
		assertEquals("Should be same managed object on adding twice", moFirst,
				moSecond);
	}

	/**
	 * Tests adding an {@link OfficeTeam}.
	 */
	public void testAddOfficeTeam() {
		// Add two different teams verifying details
		this.replayMockObjects();
		OfficeTeam team = this.node.addTeam("TEAM");
		assertNotNull("Must have team", team);
		assertEquals("Incorrect team name", "TEAM", team.getOfficeTeamName());
		assertNotSame("Should obtain another team", team, this.node
				.addTeam("ANOTHER"));
		this.verifyMockObjects();
	}

	/**
	 * Ensure issue if add the {@link OfficeTeam} twice.
	 */
	public void testAddOfficeTeamTwice() {

		// Record issue in adding the teams twice
		this.record_issue("Office team TEAM already added");

		// Add the team twice
		this.replayMockObjects();
		OfficeTeam teamFirst = this.node.addTeam("TEAM");
		OfficeTeam teamSecond = this.node.addTeam("TEAM");
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
		assertEquals("Incorrect section name", "section", section
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
		this.record_issue("Office section SECTION already added");

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
	 * Tests adding an {@link OfficeManagedObject}.
	 */
	public void testAddOfficeManagedObject() {
		// Add two different managed objects verifying details
		this.replayMockObjects();
		OfficeManagedObject mo = this.addManagedObject(this.node, "MO", null);
		assertNotNull("Must have managed object", mo);
		assertEquals("Incorrect managed object name", "MO", mo
				.getOfficeManagedObjectName());
		assertNotSame("Should obtain another managed object", mo, this
				.addManagedObject(this.node, "ANOTHER", null));
		this.verifyMockObjects();
	}

	/**
	 * Ensure issue if add the {@link OfficeManagedObject} twice.
	 */
	public void testAddOfficeManagedObjectTwice() {

		// Record issue in adding the managed objects twice
		this.record_issue("Office managed object MO already added");

		// Add the managed object twice
		this.replayMockObjects();
		OfficeManagedObject moFirst = this.addManagedObject(this.node, "MO",
				null);
		OfficeManagedObject moSecond = this.addManagedObject(this.node, "MO",
				null);
		this.verifyMockObjects();

		// Should be the same managed object
		assertEquals("Should be same managed object on adding twice", moFirst,
				moSecond);
	}

	/**
	 * Ensure can have {@link ObjectDependency} linked to an
	 * {@link OfficeManagedObject}.
	 */
	public void testObjectDependencyLinkedToOfficeManagedObject() {
		fail("TODO implement");
	}

	/**
	 * Ensure can have {@link ObjectDependency} linked to an
	 * {@link OfficeFloorManagedObject}.
	 */
	public void testObjectDependencyLinkedToOfficeFloorManagedObject() {
		fail("TODO implement");
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
		this.record_issue("Office administrator ADMIN already added");

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