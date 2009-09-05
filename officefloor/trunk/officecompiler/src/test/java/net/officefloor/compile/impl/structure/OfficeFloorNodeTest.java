/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2009 Daniel Sagenschneider
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

import net.officefloor.compile.internal.structure.OfficeFloorNode;
import net.officefloor.compile.issues.CompilerIssues.LocationType;
import net.officefloor.compile.spi.office.ManagedObjectTeam;
import net.officefloor.compile.spi.office.OfficeObject;
import net.officefloor.compile.spi.office.OfficeTeam;
import net.officefloor.compile.spi.officefloor.DeployedOffice;
import net.officefloor.compile.spi.officefloor.DeployedOfficeInput;
import net.officefloor.compile.spi.officefloor.ManagingOffice;
import net.officefloor.compile.spi.officefloor.OfficeFloorDeployer;
import net.officefloor.compile.spi.officefloor.OfficeFloorInputManagedObject;
import net.officefloor.compile.spi.officefloor.OfficeFloorManagedObject;
import net.officefloor.compile.spi.officefloor.OfficeFloorManagedObjectSource;
import net.officefloor.compile.spi.officefloor.OfficeFloorTeam;
import net.officefloor.compile.spi.officefloor.source.OfficeFloorSource;
import net.officefloor.compile.spi.section.ManagedObjectDependency;
import net.officefloor.compile.spi.section.ManagedObjectFlow;
import net.officefloor.frame.api.build.OfficeFloorIssues.AssetType;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.internal.structure.ManagedObjectScope;

/**
 * Tests the {@link OfficeFloorNode}.
 *
 * @author Daniel Sagenschneider
 */
public class OfficeFloorNodeTest extends AbstractStructureTestCase {

	/**
	 * Location of the {@link OfficeFloor}.
	 */
	private static final String OFFICE_FLOOR_LOCATION = "OFFICE_FLOOR";

	/**
	 * {@link OfficeFloorNode} implementation.
	 */
	private final OfficeFloorNode node = new OfficeFloorNodeImpl(
			OFFICE_FLOOR_LOCATION, this.nodeContext);

	/**
	 * Ensure allow {@link OfficeFloorSource} to report issues via the
	 * {@link OfficeFloorDeployer}.
	 */
	public void testAddIssue() {

		// Record adding the issue
		this.issues.addIssue(LocationType.OFFICE_FLOOR, OFFICE_FLOOR_LOCATION,
				AssetType.MANAGED_OBJECT, "SOME MANAGED OBJECT", "TEST_ISSUE");

		// Add the issue
		this.replayMockObjects();
		this.node.addIssue("TEST_ISSUE", AssetType.MANAGED_OBJECT,
				"SOME MANAGED OBJECT");
		this.verifyMockObjects();
	}

	/**
	 * Ensure allow {@link OfficeFloorSource} to report issues via the
	 * {@link OfficeFloorDeployer}.
	 */
	public void testAddIssueWithCause() {

		final Exception failure = new Exception("cause");

		// Record adding the issue
		this.issues.addIssue(LocationType.OFFICE_FLOOR, OFFICE_FLOOR_LOCATION,
				AssetType.MANAGED_OBJECT, "SOME MANAGED OBJECT", "TEST_ISSUE",
				failure);

		// Add the issue
		this.replayMockObjects();
		this.node.addIssue("TEST_ISSUE", failure, AssetType.MANAGED_OBJECT,
				"SOME MANAGED OBJECT");
		this.verifyMockObjects();
	}

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
	 * Tests adding an {@link OfficeFloorManagedObjectSource}.
	 */
	public void testAddOfficeFloorManagedObjectSource() {
		// Add two different managed object sources verifying details
		this.replayMockObjects();
		OfficeFloorManagedObjectSource moSource = this.addManagedObjectSource(
				this.node, "MO", null);
		assertNotNull("Must have managed object source", moSource);
		assertEquals("Incorrect managed object source name", "MO", moSource
				.getOfficeFloorManagedObjectSourceName());
		assertNotSame("Should obtain another managed object source", moSource,
				this.addManagedObjectSource(this.node, "ANOTHER", null));
		this.verifyMockObjects();
	}

	/**
	 * Ensure issue if adding the {@link OfficeFloorManagedObjectSource} twice.
	 */
	public void testAddOfficeFloorManagedObjectSourceTwice() {

		// Record issue in adding the managed object source twice
		this
				.record_issue("Office floor managed object source MO already added");

		// Add the managed object twice
		this.replayMockObjects();
		OfficeFloorManagedObjectSource moSourceFirst = this
				.addManagedObjectSource(this.node, "MO", null);
		OfficeFloorManagedObjectSource moSourceSecond = this
				.addManagedObjectSource(this.node, "MO", null);
		this.verifyMockObjects();

		// Should be the same managed object source
		assertEquals("Should be same managed object source on adding twice",
				moSourceFirst, moSourceSecond);
	}

	/**
	 * Tests adding an {@link OfficeFloorInputManagedObject}.
	 */
	public void testAddOfficeFloorInputManagedObject() {
		// Add two different input managed objects verifying details
		this.replayMockObjects();
		OfficeFloorInputManagedObject inputMo = this.node
				.addInputManagedObject("INPUT");
		assertNotNull("Must have input managed object", inputMo);
		assertEquals("Incorrect input managed object name", "INPUT", inputMo
				.getOfficeFloorInputManagedObjectName());
		assertNotSame("Should obtain another input managed object", inputMo,
				this.node.addInputManagedObject("ANOTHER"));
		this.verifyMockObjects();
	}

	/**
	 * Ensures issue if adding the {@link OfficeFloorInputManagedObject} twice.
	 */
	public void testAddOfficeFloorInputManagedObjectTwice() {

		// Record issue in adding twice
		this
				.record_issue("Office floor input managed object INPUT already added");

		// Add the input managed object twice
		this.replayMockObjects();
		OfficeFloorInputManagedObject moFirst = this.node
				.addInputManagedObject("INPUT");
		OfficeFloorInputManagedObject moSecond = this.node
				.addInputManagedObject("INPUT");
		this.verifyMockObjects();

		// Should be the same input managed object
		assertEquals("Should be same input managed object on adding twice",
				moFirst, moSecond);
	}

	/**
	 * Tests adding an {@link OfficeFloorManagedObject}.
	 */
	public void testAddOfficeFloorManagedObject() {
		// Add two different managed objects verifying details
		OfficeFloorManagedObjectSource moSource = this.addManagedObjectSource(
				this.node, "MO_SOURCE", null);
		this.replayMockObjects();
		OfficeFloorManagedObject mo = moSource.addOfficeFloorManagedObject(
				"MO", ManagedObjectScope.THREAD);
		assertNotNull("Must have managed object", mo);
		assertEquals("Incorrect managed object name", "MO", mo
				.getOfficeFloorManagedObjectName());
		assertNotSame("Should obtain another managed object", mo, moSource
				.addOfficeFloorManagedObject("ANOTHER",
						ManagedObjectScope.THREAD));
		this.verifyMockObjects();
	}

	/**
	 * Ensure issue if adding the {@link OfficeFloorManagedObject} twice.
	 */
	public void testAddOfficeFloorManagedObjectTwice() {

		// Record issue in adding the managed object twice
		this.issues.addIssue(LocationType.OFFICE_FLOOR, OFFICE_FLOOR_LOCATION,
				AssetType.MANAGED_OBJECT, "MO",
				"Office floor managed object MO already added");

		OfficeFloorManagedObjectSource moSource = this.addManagedObjectSource(
				this.node, "MO_SOURCE", null);

		// Add the managed object twice
		this.replayMockObjects();
		OfficeFloorManagedObject moFirst = moSource
				.addOfficeFloorManagedObject("MO", ManagedObjectScope.WORK);
		OfficeFloorManagedObject moSecond = moSource
				.addOfficeFloorManagedObject("MO", ManagedObjectScope.WORK);
		this.verifyMockObjects();

		// Should be the same managed object
		assertEquals("Should be same managed object on adding twice", moFirst,
				moSecond);
	}

	/**
	 * Ensure issue if adding the {@link OfficeFloorManagedObject} twice by
	 * different {@link OfficeFloorManagedObjectSource} instances.
	 */
	public void testAddOfficeFloorManagedObjectTwiceByDifferentSources() {

		// Record issue in adding the managed object twice
		this.issues.addIssue(LocationType.OFFICE_FLOOR, OFFICE_FLOOR_LOCATION,
				AssetType.MANAGED_OBJECT, "MO",
				"Office floor managed object MO already added");

		OfficeFloorManagedObjectSource moSourceOne = this
				.addManagedObjectSource(this.node, "MO_SOURCE_ONE", null);
		OfficeFloorManagedObjectSource moSourceTwo = this
				.addManagedObjectSource(this.node, "MO_SOURCE_TWO", null);

		// Add the managed object twice by different sources
		this.replayMockObjects();
		OfficeFloorManagedObject moFirst = moSourceOne
				.addOfficeFloorManagedObject("MO", ManagedObjectScope.WORK);
		OfficeFloorManagedObject moSecond = moSourceTwo
				.addOfficeFloorManagedObject("MO", ManagedObjectScope.WORK);
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
				.record_issue("Managing office for managed object source MO linked more than once");

		this.replayMockObjects();

		// Link
		OfficeFloorManagedObjectSource moSource = this.addManagedObjectSource(
				this.node, "MO", null);
		ManagingOffice managingOffice = moSource.getManagingOffice();
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
	 * Ensure can link {@link OfficeFloorManagedObjectSource} to an
	 * {@link OfficeFloorInputManagedObject}.
	 */
	public void testLinkOfficeFloorManagedObjectSourceToOfficeFloorInputManagedObject() {

		// Record already being linked
		this
				.record_issue("Managed object source MO_SOURCE already linked to an input managed object");

		this.replayMockObjects();

		// Link
		OfficeFloorManagedObjectSource moSource = this.addManagedObjectSource(
				this.node, "MO_SOURCE", null);
		OfficeFloorInputManagedObject inputMo = this.node
				.addInputManagedObject("INPUT");
		this.node.link(moSource, inputMo);
		assertSourceInput(
				"office floor managed object source -> office floor input managed object",
				moSource, inputMo);

		// Ensure can link only once
		this.node.link(moSource, this.node.addInputManagedObject("ANOTHER"));
		assertSourceInput("Can only link once", moSource, inputMo);

		this.verifyMockObjects();
	}

	/**
	 * Ensure can link {@link OfficeFloorManagedObjectSource}
	 * {@link ManagedObjectTeam} to the {@link OfficeFloorTeam}.
	 */
	public void testLinkManagedObjectTeamToOfficeFloorTeam() {

		// Record already being linked
		this.record_issue("TEAM already assigned");

		this.replayMockObjects();

		// Link
		OfficeFloorManagedObjectSource moSource = this.addManagedObjectSource(
				this.node, "MO", null);
		ManagedObjectTeam team = moSource.getManagedObjectTeam("TEAM");
		OfficeFloorTeam officeFloorTeam = this.addTeam(this.node,
				"OFFICE_FLOOR_TEAM", null);
		this.node.link(team, officeFloorTeam);
		assertTeamLink("managed object source team -> office floor team", team,
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
		OfficeFloorManagedObjectSource moSource = this.addManagedObjectSource(
				this.node, "MO_SOURCE", null);
		OfficeFloorManagedObject mo = moSource.addOfficeFloorManagedObject(
				"MO", ManagedObjectScope.PROCESS);
		ManagedObjectDependency dependency = mo
				.getManagedObjectDependency("DEPENDENCY");
		OfficeFloorManagedObjectSource moSourceTarget = this
				.addManagedObjectSource(this.node, "MO_SOURCE_TARGET", null);
		OfficeFloorManagedObject moTarget = moSourceTarget
				.addOfficeFloorManagedObject("MO_TARGET",
						ManagedObjectScope.PROCESS);
		this.node.link(dependency, moTarget);
		assertObjectLink(
				"managed object dependency -> office floor managed object",
				dependency, moTarget);

		// Ensure only can link once
		this.node.link(dependency, moSourceTarget.addOfficeFloorManagedObject(
				"ANOTHER", ManagedObjectScope.PROCESS));
		assertObjectLink("Can only link once", dependency, moTarget);

		this.verifyMockObjects();
	}

	/**
	 * Ensure can link {@link OfficeFloorManagedObject}
	 * {@link ManagedObjectDependency} to the
	 * {@link OfficeFloorInputManagedObject}.
	 */
	public void testLinkManagedObjectDependencyToOfficeFloorInputManagedObject() {

		// Record already being linked
		this
				.record_issue("Managed object dependency DEPENDENCY linked more than once");

		this.replayMockObjects();

		// Link
		OfficeFloorManagedObjectSource moSource = this.addManagedObjectSource(
				this.node, "MO_SOURCE", null);
		OfficeFloorManagedObject mo = moSource.addOfficeFloorManagedObject(
				"MO", ManagedObjectScope.PROCESS);
		ManagedObjectDependency dependency = mo
				.getManagedObjectDependency("DEPENDENCY");
		OfficeFloorInputManagedObject inputMoTarget = this.node
				.addInputManagedObject("INPUT_TARGET");
		this.node.link(dependency, inputMoTarget);
		assertObjectLink(
				"managed object dependency -> office floor input managed object",
				dependency, inputMoTarget);

		// Ensure only can link once
		this.node.link(dependency, this.node.addInputManagedObject("ANOTHER"));
		assertObjectLink("Can only link once", dependency, inputMoTarget);

		this.verifyMockObjects();
	}

	/**
	 * Ensure can link {@link OfficeFloorManagedObjectSource}
	 * {@link ManagedObjectFlow} to the {@link DeployedOfficeInput}.
	 */
	public void testLinkManagedObjectFlowToDeployedOfficeInput() {

		// Record already being linked
		this
				.record_issue("Managed object source flow FLOW linked more than once");

		this.replayMockObjects();

		// Link
		OfficeFloorManagedObjectSource moSource = this.addManagedObjectSource(
				this.node, "MO", null);
		ManagedObjectFlow flow = moSource.getManagedObjectFlow("FLOW");
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
	public void testLinkOfficeObjectToOfficeFloorManagedObject() {

		// Record already being linked
		this.record_issue("Office object OBJECT linked more than once");

		this.replayMockObjects();

		// Link
		DeployedOffice office = this.addDeployedOffice(this.node, "OFFICE",
				null);
		OfficeObject object = office.getDeployedOfficeObject("OBJECT");
		OfficeFloorManagedObjectSource moSource = this.addManagedObjectSource(
				this.node, "MO_SOURCE_TARGET", null);
		OfficeFloorManagedObject mo = moSource.addOfficeFloorManagedObject(
				"MO_TARGET", ManagedObjectScope.THREAD);
		this.node.link(object, mo);
		assertObjectLink(
				"office required object -> office floor managed object",
				object, mo);

		// Ensure only can link once
		this.node.link(object, moSource.addOfficeFloorManagedObject("ANOTHER",
				ManagedObjectScope.THREAD));
		assertObjectLink("Can only link once", object, mo);

		this.verifyMockObjects();
	}

	/**
	 * Ensure can link {@link OfficeObject} to the
	 * {@link OfficeFloorInputManagedObject}.
	 */
	public void testLinkOfficeObjectToOfficeFloorInputManagedObject() {

		// Record already being linked
		this.record_issue("Office object OBJECT linked more than once");

		this.replayMockObjects();

		// Link
		DeployedOffice office = this.addDeployedOffice(this.node, "OFFICE",
				null);
		OfficeObject object = office.getDeployedOfficeObject("OBJECT");
		OfficeFloorInputManagedObject inputMo = this.node
				.addInputManagedObject("INPUT_TARGET");
		this.node.link(object, inputMo);
		assertObjectLink(
				"office required object -> office floor input managed object",
				object, inputMo);

		// Ensure only can link once
		this.node.link(object, this.node.addInputManagedObject("ANOTHER"));
		assertObjectLink("Can only link once", object, inputMo);

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