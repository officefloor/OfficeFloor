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

import java.util.HashMap;
import java.util.Map;

import net.officefloor.model.office.ExternalManagedObjectModel;
import net.officefloor.model.office.ExternalTeamModel;
import net.officefloor.model.office.OfficeModel;
import net.officefloor.model.officefloor.OfficeFloorOfficeModel;
import net.officefloor.model.officefloor.OfficeManagedObjectModel;
import net.officefloor.model.officefloor.OfficeTeamModel;

/**
 * Synchronises the {@link OfficeModel} onto the {@link OfficeFloorOfficeModel}.
 * 
 * @author Daniel
 */
public class OfficeToOfficeFloorOfficeSynchroniser {

	/**
	 * Synchronises the {@link OfficeModel} onto the
	 * {@link OfficeFloorOfficeModel}.
	 * 
	 * @param officeId
	 *            Id of the {@link OfficeModel}.
	 * @param office
	 *            {@link OfficeModel}.
	 * @param officeFloorOffice
	 *            {@link OfficeFloorOfficeModel}.
	 */
	public static void synchroniseOfficeOntoOfficeFloorOffice(String officeId,
			OfficeModel office, OfficeFloorOfficeModel officeFloorOffice) {

		// Specify the id of Office
		officeFloorOffice.setId(officeId);

		// Create the map of existing managed objects
		Map<String, OfficeManagedObjectModel> existingManagedObjects = new HashMap<String, OfficeManagedObjectModel>();
		for (OfficeManagedObjectModel mo : officeFloorOffice
				.getManagedObjects()) {
			existingManagedObjects.put(mo.getManagedObjectName(), mo);
		}

		// Synchronise the managed objects
		for (ExternalManagedObjectModel mo : office.getExternalManagedObjects()) {
			String moName = mo.getName();
			if (existingManagedObjects.containsKey(moName)) {
				// Remove managed object (so not removed later)
				existingManagedObjects.remove(moName);
			} else {
				// Create the new managed object
				OfficeManagedObjectModel newMo = new OfficeManagedObjectModel(
						moName, null);
				officeFloorOffice.addManagedObject(newMo);
			}
		}

		// Remove the old managed objects
		for (OfficeManagedObjectModel mo : existingManagedObjects.values()) {
			officeFloorOffice.removeManagedObject(mo);
		}

		// Create the map of existing teams
		Map<String, OfficeTeamModel> existingTeams = new HashMap<String, OfficeTeamModel>();
		for (OfficeTeamModel team : officeFloorOffice.getTeams()) {
			existingTeams.put(team.getTeamName(), team);
		}

		// Synchronise the teams
		for (ExternalTeamModel team : office.getExternalTeams()) {
			String teamName = team.getName();
			if (existingTeams.containsKey(teamName)) {
				// Remove team (so not removed later)
				existingTeams.remove(teamName);
			} else {
				// Create the new team
				OfficeTeamModel newTeam = new OfficeTeamModel(teamName, null);
				officeFloorOffice.addTeam(newTeam);
			}
		}

		// Remove the old teams
		for (OfficeTeamModel team : existingTeams.values()) {
			officeFloorOffice.removeTeam(team);
		}
	}

	/**
	 * Access via static methods.
	 */
	private OfficeToOfficeFloorOfficeSynchroniser() {
	}

}
