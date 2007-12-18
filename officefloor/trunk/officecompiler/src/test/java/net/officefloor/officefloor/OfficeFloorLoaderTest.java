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
package net.officefloor.officefloor;

import java.io.File;

import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.model.officefloor.ManagedObjectSourceModel;
import net.officefloor.model.officefloor.ManagedObjectSourceToOfficeFloorOfficeModel;
import net.officefloor.model.officefloor.OfficeFloorModel;
import net.officefloor.model.officefloor.OfficeFloorOfficeModel;
import net.officefloor.model.officefloor.OfficeManagedObjectModel;
import net.officefloor.model.officefloor.OfficeManagedObjectToManagedObjectSourceModel;
import net.officefloor.model.officefloor.OfficeTeamModel;
import net.officefloor.model.officefloor.OfficeTeamToTeamModel;
import net.officefloor.model.officefloor.PropertyModel;
import net.officefloor.model.officefloor.TeamModel;
import net.officefloor.repository.ConfigurationContext;
import net.officefloor.repository.ConfigurationItem;
import net.officefloor.repository.filesystem.FileSystemConfigurationContext;
import net.officefloor.repository.filesystem.FileSystemConfigurationItem;

/**
 * Tests loading the {@link net.officefloor.officefloor.OfficeFloorLoader}.
 * 
 * @author Daniel
 */
public class OfficeFloorLoaderTest extends OfficeFrameTestCase {

	/**
	 * {@link OfficeFloorLoader} to test.
	 */
	private OfficeFloorLoader officeFloorLoader;

	/**
	 * {@link ConfigurationItem}.
	 */
	private ConfigurationItem configurationItem;

	/*
	 * (non-Javadoc)
	 * 
	 * @see junit.framework.TestCase#setUp()
	 */
	@Override
	protected void setUp() throws Exception {
		// Create the Office Floor Loader
		this.officeFloorLoader = new OfficeFloorLoader();

		// Obtain the configuration
		this.configurationItem = new FileSystemConfigurationItem(this.findFile(
				this.getClass(), "TestOfficeFloor.officefloor.xml"), null);
	}

	/**
	 * Ensure loads the
	 * {@link net.officefloor.model.officefloor.OfficeFloorModel}.
	 */
	public void testLoadOfficeFloor() throws Exception {

		// Load the office floor
		OfficeFloorModel officeFloor = this.officeFloorLoader
				.loadOfficeFloor(this.configurationItem);

		// ----------------------------------------
		// Validate the Office Floor
		// ----------------------------------------

		// Validate the managed objects
		assertList(new String[] { "getId", "getSource" }, officeFloor
				.getManagedObjectSources(), new ManagedObjectSourceModel(
				"MO-ID", "net.officefloor.mo.TestManagedObjectSource", null,
				null, null));
		assertEquals("Incorrect managed object managing office", "OFFICE",
				officeFloor.getManagedObjectSources().get(0)
						.getManagingOffice().getManagingOfficeName());
		assertList(new String[] { "getName", "getValue" }, officeFloor
				.getManagedObjectSources().get(0).getProperties(),
				new PropertyModel("mo prop name", "mo prop value"));

		// Validate the teams
		assertList(new String[] { "getId", "getTeamFactory" }, officeFloor
				.getTeams(), new TeamModel("TEAM-ID",
				"net.officefloor.team.TestTeamFactory", null, null));
		assertList(new String[] { "getName", "getValue" }, officeFloor
				.getTeams().get(0).getProperties(), new PropertyModel(
				"team prop name", "team prop value"));

		// Validate the offices
		assertList(new String[] { "getId", "getName", "getX", "getY" },
				officeFloor.getOffices(), new OfficeFloorOfficeModel("OFFICE",
						"office", null, null, null, 100, 20));
		OfficeFloorOfficeModel office = officeFloor.getOffices().get(0);
		assertList(new String[] { "getManagedObjectName", "getScope" }, office
				.getManagedObjects(), new OfficeManagedObjectModel("MO-NAME",
				"process", null));
		assertList(new String[] { "getTeamName" }, office.getTeams(),
				new OfficeTeamModel("TEAM-NAME", null));
		assertList(new String[] { "getManagingOfficeName" }, officeFloor
				.getOffices().get(0).getResponsibleManagedObjects(),
				new ManagedObjectSourceToOfficeFloorOfficeModel("OFFICE", null,
						null));

		// Validate connections of office
		OfficeManagedObjectToManagedObjectSourceModel moToMos = office
				.getManagedObjects().get(0).getManagedObjectSource();
		assertEquals("Incorrect managed object source link", "MO-ID", moToMos
				.getManagedObjectSourceId());
		OfficeTeamToTeamModel teamToTeam = office.getTeams().get(0).getTeam();
		assertEquals("Incorrect team link", "TEAM-ID", teamToTeam.getTeamId());
	}

	/**
	 * Ensures round trip in loading and storing.
	 */
	public void testLoadAndStore() throws Exception {

		// Load the Office Floor
		OfficeFloorModel officeFloor = this.officeFloorLoader
				.loadOfficeFloor(this.configurationItem);

		// Store the Desk
		File file = File
				.createTempFile("TestOfficeFloor.officefloor.xml", null);
		FileSystemConfigurationItem tempFile = new FileSystemConfigurationItem(
				file, null);
		this.officeFloorLoader.storeOfficeFloor(officeFloor, tempFile);

		// Reload the Office
		OfficeFloorModel reloadedOfficeFloor = this.officeFloorLoader
				.loadOfficeFloor(tempFile);

		// Validate round trip
		assertGraph(officeFloor, reloadedOfficeFloor, "getAdministrators");
	}

	/**
	 * Ensures correctly loads the office.
	 */
	public void testLoadOfficeFloorOffice() throws Exception {

		final String OFFICE_FILE_NAME = "TestOffice.office.xml";

		// Obtain the office file
		File parentOfficeFile = this
				.findFile(this.getClass(), OFFICE_FILE_NAME);

		// Obtain the configuration of the office
		ConfigurationContext context = new FileSystemConfigurationContext(
				parentOfficeFile.getParentFile());
		ConfigurationItem officeConfigItem = context
				.getConfigurationItem(OFFICE_FILE_NAME);

		// Load the office
		OfficeFloorOfficeModel actualOffice = this.officeFloorLoader
				.loadOfficeFloorOffice(officeConfigItem);

		// Create the expected office
		OfficeFloorOfficeModel expectedOffice = new OfficeFloorOfficeModel(
				OFFICE_FILE_NAME, null,
				new OfficeManagedObjectModel[] { new OfficeManagedObjectModel(
						"MO", null, null) }, new OfficeTeamModel[] {
						new OfficeTeamModel("TEAM ONE", null),
						new OfficeTeamModel("TEAM TWO", null) }, null);

		// Validate the office
		assertGraph(expectedOffice, actualOffice);
	}

}
