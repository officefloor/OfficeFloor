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
package net.officefloor.repository;

import net.officefloor.model.officefloor.ManagedObjectSourceModel;
import net.officefloor.model.officefloor.ManagedObjectSourceToOfficeFloorOfficeModel;
import net.officefloor.model.officefloor.OfficeFloorModel;
import net.officefloor.model.officefloor.OfficeFloorOfficeModel;
import net.officefloor.model.officefloor.OfficeManagedObjectModel;
import net.officefloor.model.officefloor.OfficeTeamModel;
import net.officefloor.model.officefloor.PropertyModel;
import net.officefloor.model.officefloor.TeamModel;

/**
 * Tests the storing and retrieving of the
 * {@link net.officefloor.model.officefloor.OfficeFloorModel}.
 * 
 * @author Daniel
 */
public class OfficeFloorModelTest extends AbstractModelTestCase {

	/**
	 * Ensures able to store and retrieve.
	 */
	public void testStoreAndRetrieve() throws Exception {

		// Create the Office Floor Model
		OfficeFloorModel of = new OfficeFloorModel(
				new ManagedObjectSourceModel[] { new ManagedObjectSourceModel(
						"MO-ID", "SOURCE",
						new ManagedObjectSourceToOfficeFloorOfficeModel(
								"OFFICE", null, null),
						new PropertyModel[] { new PropertyModel(
								"PROPERTY NAME", "PROPERTY VALUE") }, null) },
				new TeamModel[] { new TeamModel("TEAM", "TEAM-FACTORY",
						new PropertyModel[] { new PropertyModel("PROP NAME",
								"PROP VALUE") }, null) },
				new OfficeFloorOfficeModel[] { new OfficeFloorOfficeModel(
						"OFFICE", "office",
						new OfficeManagedObjectModel[] { new OfficeManagedObjectModel(
								"MO-NAME", null) }, new OfficeTeamModel[0],
						null) });

		// Configuration Item
		ConfigurationItem configItem = this.getConfigurationContext()
				.getConfigurationItem("OfficeFloor.officefloor.xml");

		// Store the Office Floor
		this.getModelRepository().store(of, configItem);

		// Retrieve the Office Floor
		OfficeFloorModel retrieved = this.getModelRepository().retrieve(
				new OfficeFloorModel(), configItem);

		// Validate contains all information
		ManagedObjectSourceModel mos = retrieved.getManagedObjectSources().get(
				0);
		assertEquals("Incorrect Managed Object Id", "MO-ID", mos.getId());
		assertEquals("Incorrect Managed Object Source", "SOURCE", mos
				.getSource());
		assertEquals("Incorrect Managed Object managing Office", "OFFICE", mos
				.getManagingOffice().getManagingOfficeName());
		PropertyModel moProp = mos.getProperties().get(0);
		assertEquals("Incorrect Managed Object property name", "PROPERTY NAME",
				moProp.getName());
		assertEquals("Incorrect Managed Object property value",
				"PROPERTY VALUE", moProp.getValue());
		TeamModel team = retrieved.getTeams().get(0);
		assertEquals("Incorrect Team name", "TEAM", team.getId());
		assertEquals("Incorrect Team factory", "TEAM-FACTORY", team
				.getTeamFactory());
		PropertyModel teamProp = team.getProperties().get(0);
		assertEquals("Incorrect team property name", "PROP NAME", teamProp
				.getName());
		assertEquals("Incorrect team property value", "PROP VALUE", teamProp
				.getValue());
		OfficeFloorOfficeModel office = retrieved.getOffices().get(0);
		assertEquals("Incorrect Office Name", "OFFICE", office.getId());
		OfficeManagedObjectModel officeMo = office.getManagedObjects().get(0);
		assertEquals("Incorrect MO link Name", "MO-NAME", officeMo
				.getManagedObjectName());
	}
}
