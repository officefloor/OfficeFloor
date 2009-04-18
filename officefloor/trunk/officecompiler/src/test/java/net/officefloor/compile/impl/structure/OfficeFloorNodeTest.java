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

import net.officefloor.compile.internal.structure.OfficeFloorNode;
import net.officefloor.compile.issues.CompilerIssues;
import net.officefloor.compile.issues.CompilerIssues.LocationType;
import net.officefloor.compile.spi.office.ManagedObjectTeam;
import net.officefloor.compile.spi.office.OfficeObject;
import net.officefloor.compile.spi.office.OfficeTeam;
import net.officefloor.compile.spi.officefloor.DeployedOffice;
import net.officefloor.compile.spi.officefloor.DeployedOfficeInput;
import net.officefloor.compile.spi.officefloor.ManagingOffice;
import net.officefloor.compile.spi.officefloor.OfficeFloorManagedObject;
import net.officefloor.compile.spi.officefloor.OfficeFloorTeam;
import net.officefloor.compile.spi.section.ManagedObjectDependency;
import net.officefloor.compile.spi.section.ManagedObjectFlow;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.model.repository.ConfigurationContext;

/**
 * Tests the {@link OfficeFloorNode}.
 * 
 * @author Daniel
 */
public class OfficeFloorNodeTest extends AbstractStructureTestCase {

	/**
	 * Location of the {@link OfficeFloor}.
	 */
	private static final String OFFICE_FLOOR_LOCATION = "OFFICE_FLOOR";

	/**
	 * {@link ConfigurationContext}.
	 */
	private final ConfigurationContext configurationContext = this
			.createMock(ConfigurationContext.class);

	/**
	 * {@link ClassLoader}.
	 */
	private final ClassLoader classLoader = this.getClass().getClassLoader();

	/**
	 * {@link CompilerIssues}.
	 */
	private final CompilerIssues issues = this.createMock(CompilerIssues.class);

	/**
	 * {@link OfficeFloorNode} implementation.
	 */
	private final OfficeFloorNode node = new OfficeFloorNodeImpl(
			this.configurationContext, this.classLoader, OFFICE_FLOOR_LOCATION,
			this.issues);

	/**
	 * Tests adding an {@link OfficeFloorTeam}.
	 */
	public void testAddOfficeFloorTeam() {
		// Add two different teams verifying details
		this.replayMockObjects();
		OfficeFloorTeam team = this.addTeam(this.node, "TEAM", null);
		assertNotNull("Must have team", team);
		assertEquals("Incorrect team name", "TEAM", team
				.getOfficeFloorTeamName());
		assertNotSame("Should obtain another managed object", team, this
				.addTeam(this.node, "ANOTHER", null));
		this.verifyMockObjects();
	}

	/**
	 * Ensure issue if adding the {@link OfficeFloorTeam} twice.
	 */
	public void testAddOfficeFloorTeamTwice() {

		// Record issue in adding the team twice
		this.record_issue("Office floor team TEAM already added");

		// Add the team twice
		this.replayMockObjects();
		OfficeFloorTeam teamFirst = this.addTeam(this.node, "TEAM", null);
		OfficeFloorTeam teamSecond = this.addTeam(this.node, "TEAM", null);
		this.verifyMockObjects();

		// Should be the same team
		assertEquals("Should be same team on adding twice", teamFirst,
				teamSecond);
	}

	/**
	 * Tests adding an {@link OfficeFloorManagedObject}.
	 */
	public void testAddOfficeFloorManagedObject() {
		// Add two different managed objects verifying details
		this.replayMockObjects();
		OfficeFloorManagedObject mo = this.addManagedObject(this.node, "MO",
				null);
		assertNotNull("Must have managed object", mo);
		assertEquals("Incorrect managed object name", "MO", mo
				.getOfficeFloorManagedObjectName());
		assertNotSame("Should obtain another managed object", mo, this
				.addManagedObject(this.node, "ANOTHER", null));
		this.verifyMockObjects();
	}

	/**
	 * Ensure issue if adding the {@link OfficeFloorManagedObject} twice.
	 */
	public void testAddOfficeFloorManagedObjectTwice() {

		// Record issue in adding the managed object twice
		this.record_issue("Office floor managed object MO already added");

		// Add the managed object twice
		this.replayMockObjects();
		OfficeFloorManagedObject moFirst = this.addManagedObject(this.node,
				"MO", null);
		OfficeFloorManagedObject moSecond = this.addManagedObject(this.node,
				"MO", null);
		this.verifyMockObjects();

		// Should be the same managed object
		assertEquals("Should be same managed object on adding twice", moFirst,
				moSecond);
	}

	/**
	 * Tests adding a {@link DeployedOffice}.
	 */
	public void testDeployOffice() {
		// Add two different offices verifying details
		this.replayMockObjects();
		DeployedOffice office = this.addDeployedOffice(this.node, "OFFICE",
				null);
		assertNotNull("Must have office", office);
		assertEquals("Incorrect office name", "OFFICE", office
				.getDeployedOfficeName());
		assertNotSame("Should obtain another office", office, this
				.addDeployedOffice(this.node, "ANOTHER", null));
		this.verifyMockObjects();
	}

	/**
	 * Ensure issue if adding the {@link DeployedOffice} twice.
	 */
	public void testAddDeployedOfficeTwice() {

		// Record issue in adding the office twice
		this.record_issue("Office OFFICE already deployed");

		// Add the office twice
		this.replayMockObjects();
		DeployedOffice officeFirst = this.addDeployedOffice(this.node,
				"OFFICE", null);
		DeployedOffice officeSecond = this.addDeployedOffice(this.node,
				"OFFICE", null);
		this.verifyMockObjects();

		// Should be the same office
		assertEquals("Should be same office on adding twice", officeFirst,
				officeSecond);
	}

	/**
	 * Ensure can link {@link ManagingOffice} to the {@link DeployedOffice}.
	 */
	public void testLinkManagingOfficeToDeployedOffice() {

		// Record already being linked
		this
				.record_issue("Managing office for managed object MO linked more than once");

		this.replayMockObjects();

		// Link
		OfficeFloorManagedObject mo = this.addManagedObject(this.node, "MO",
				null);
		ManagingOffice managingOffice = mo.getManagingOffice();
		DeployedOffice office = this.addDeployedOffice(this.node, "OFFICE",
				null);
		this.node.link(managingOffice, office);
		assertOfficeLink("managing office -> deployed office", managingOffice,
				office);

		// Ensure only can link once
		this.node.link(managingOffice, this.addDeployedOffice(this.node,
				"ANOTHER", null));
		assertOfficeLink("Can only link once", managingOffice, office);

		this.verifyMockObjects();
	}

	/**
	 * Ensure can link {@link OfficeFloorManagedObject}
	 * {@link ManagedObjectTeam} to the {@link OfficeFloorTeam}.
	 */
	public void testLinkManagedObjectTeamToOfficeFloorTeam() {

		// Record already being linked
		this.record_issue("TEAM already assigned");

		this.replayMockObjects();

		// Link
		OfficeFloorManagedObject mo = this.addManagedObject(this.node, "MO",
				null);
		ManagedObjectTeam team = mo.getManagedObjectTeam("TEAM");
		OfficeFloorTeam officeFloorTeam = this.addTeam(this.node,
				"OFFICE_FLOOR_TEAM", null);
		this.node.link(team, officeFloorTeam);
		assertTeamLink("managed object team -> office floor team", team,
				officeFloorTeam);

		// Ensure only can link once
		this.node.link(team, this.addTeam(this.node, "ANOTHER", null));
		assertTeamLink("Can only link once", team, officeFloorTeam);

		this.verifyMockObjects();
	}

	/**
	 * Ensure can link {@link OfficeFloorManagedObject}
	 * {@link ManagedObjectDependency} to the {@link OfficeFloorManagedObject}.
	 */
	public void testLinkManagedObjectDependencyToOfficeFloorManagedObject() {

		// Record already being linked
		this
				.record_issue("Managed object dependency DEPENDENCY linked more than once");

		this.replayMockObjects();

		// Link
		OfficeFloorManagedObject mo = this.addManagedObject(this.node, "MO",
				null);
		ManagedObjectDependency dependency = mo
				.getManagedObjectDependency("DEPENDENCY");
		OfficeFloorManagedObject moTarget = this.addManagedObject(this.node,
				"MO_TARGET", null);
		this.node.link(dependency, moTarget);
		assertObjectLink(
				"managed object dependency -> office floor managed object",
				dependency, moTarget);

		// Ensure only can link once
		this.node.link(dependency, this.addManagedObject(this.node, "ANOTHER",
				null));
		assertObjectLink("Can only link once", dependency, moTarget);

		this.verifyMockObjects();
	}

	/**
	 * Ensure can link {@link OfficeFloorManagedObject}
	 * {@link ManagedObjectFlow} to the {@link DeployedOfficeInput}.
	 */
	public void testLinkManagedObjectFlowToDeployedOfficeInput() {

		// Record already being linked
		this.record_issue("Managed object flow FLOW linked more than once");

		this.replayMockObjects();

		// Link
		OfficeFloorManagedObject mo = this.addManagedObject(this.node, "MO",
				null);
		ManagedObjectFlow flow = mo.getManagedObjectFlow("FLOW");
		DeployedOffice office = this.addDeployedOffice(this.node, "OFFICE",
				null);
		DeployedOfficeInput input = office.getDeployedOfficeInput("SECTION",
				"INPUT");
		this.node.link(flow, input);
		assertFlowLink("managed object flow -> office input", flow, input);

		// Ensure only can link once
		this.node.link(flow, office
				.getDeployedOfficeInput("SECTION", "ANOTHER"));
		assertFlowLink("Can only link once", flow, input);

		this.verifyMockObjects();
	}

	/**
	 * Ensure can link {@link OfficeTeam} to the {@link OfficeFloorTeam}.
	 */
	public void testLinkOfficeTeamToOfficeFloorTeam() {

		// Record already being linked
		this.record_issue("TEAM already assigned");

		this.replayMockObjects();

		// Link
		DeployedOffice office = this.addDeployedOffice(this.node, "OFFICE",
				null);
		OfficeTeam team = office.getDeployedOfficeTeam("TEAM");
		OfficeFloorTeam officeFloorTeam = this.addTeam(this.node,
				"OFFICE_FLOOR_TEAM", null);
		this.node.link(team, officeFloorTeam);
		assertTeamLink("office team -> office floor team", team,
				officeFloorTeam);

		// Ensure only can link once
		this.node.link(team, this.addTeam(this.node, "ANOTHER", null));
		assertTeamLink("Can only link once", team, officeFloorTeam);

		this.verifyMockObjects();
	}

	/**
	 * Ensure can link {@link OfficeObject} to the
	 * {@link OfficeFloorManagedObject}.
	 */
	public void testLinkOfficeRequiredManagedObjectToOfficeFloorManagedObject() {

		// Record already being linked
		this.record_issue("Office object OBJECT linked more than once");

		this.replayMockObjects();

		// Link
		DeployedOffice office = this.addDeployedOffice(this.node, "OFFICE",
				null);
		OfficeObject object = office
				.getDeployedOfficeObject("OBJECT");
		OfficeFloorManagedObject mo = this.addManagedObject(this.node,
				"MO_TARGET", null);
		this.node.link(object, mo);
		assertObjectLink(
				"office required object -> office floor managed object",
				object, mo);

		// Ensure only can link once
		this.node.link(object, this
				.addManagedObject(this.node, "ANOTHER", null));
		assertObjectLink("Can only link once", object, mo);

		this.verifyMockObjects();
	}

	/**
	 * Records adding an issue.
	 * 
	 * @param issueDescription
	 *            Description of the issue.
	 */
	private void record_issue(String issueDescription) {
		this.issues.addIssue(LocationType.OFFICE_FLOOR, OFFICE_FLOOR_LOCATION,
				null, null, issueDescription);
	}

}