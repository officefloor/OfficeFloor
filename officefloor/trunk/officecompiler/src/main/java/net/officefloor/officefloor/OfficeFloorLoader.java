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

import net.officefloor.model.office.OfficeModel;
import net.officefloor.model.officefloor.ManagedObjectSourceModel;
import net.officefloor.model.officefloor.ManagedObjectSourceToOfficeFloorOfficeModel;
import net.officefloor.model.officefloor.OfficeFloorModel;
import net.officefloor.model.officefloor.OfficeFloorOfficeModel;
import net.officefloor.model.officefloor.OfficeManagedObjectModel;
import net.officefloor.model.officefloor.OfficeManagedObjectToManagedObjectSourceModel;
import net.officefloor.model.officefloor.OfficeTeamModel;
import net.officefloor.model.officefloor.OfficeTeamToTeamModel;
import net.officefloor.model.officefloor.TeamModel;
import net.officefloor.office.OfficeLoader;
import net.officefloor.repository.ConfigurationItem;
import net.officefloor.repository.ModelRepository;

/**
 * Loads the {@link net.officefloor.model.officefloor.OfficeFloorModel}.
 * 
 * @author Daniel
 */
public class OfficeFloorLoader {

	/**
	 * {@link ModelRepository}.
	 */
	private final ModelRepository modelRepository;

	/**
	 * Default constructor.
	 */
	public OfficeFloorLoader() {
		this.modelRepository = new ModelRepository();
	}

	/**
	 * Initiate.
	 * 
	 * @param modelRepository
	 *            {@link ModelRepository}.
	 */
	public OfficeFloorLoader(ModelRepository modelRepository) {
		this.modelRepository = modelRepository;
	}

	/**
	 * Loads the {@link OfficeFloorModel} from the configuration.
	 * 
	 * @param configuration
	 *            {@link ConfigurationItem}.
	 * @return Configured {@link OfficeFloorModel}.
	 * @throws Exception
	 *             If fails.
	 */
	public OfficeFloorModel loadOfficeFloor(ConfigurationItem configuration)
			throws Exception {

		// Load the office floor from the configuration
		OfficeFloorModel officeFloor = this.modelRepository.retrieve(
				new OfficeFloorModel(), configuration);

		// Create the registry of offices
		Map<String, OfficeFloorOfficeModel> offices = new HashMap<String, OfficeFloorOfficeModel>();
		for (OfficeFloorOfficeModel office : officeFloor.getOffices()) {
			offices.put(office.getId(), office);
		}

		// Connect the managed object source to its managing office
		for (ManagedObjectSourceModel mos : officeFloor
				.getManagedObjectSources()) {
			ManagedObjectSourceToOfficeFloorOfficeModel conn = mos
					.getManagingOffice();
			if (conn != null) {
				OfficeFloorOfficeModel office = offices.get(conn
						.getManagingOffice());
				if (office != null) {
					conn.setManagedObjectSource(mos);
					conn.setManagingOffice(office);
					conn.connect();
				}
			}
		}

		// Create the registry of managed object sources
		Map<String, ManagedObjectSourceModel> mosRegistry = new HashMap<String, ManagedObjectSourceModel>();
		for (ManagedObjectSourceModel mos : officeFloor
				.getManagedObjectSources()) {
			mosRegistry.put(mos.getId(), mos);
		}

		// Create the registry of teams
		Map<String, TeamModel> teams = new HashMap<String, TeamModel>();
		for (TeamModel team : officeFloor.getTeams()) {
			teams.put(team.getId(), team);
		}

		// Connect the office to managed object sources and teams
		for (OfficeFloorOfficeModel office : officeFloor.getOffices()) {

			// Connect the managed object sources
			for (OfficeManagedObjectModel mo : office.getManagedObjects()) {
				OfficeManagedObjectToManagedObjectSourceModel conn = mo
						.getManagedObjectSource();
				if (conn != null) {
					ManagedObjectSourceModel mos = mosRegistry.get(conn
							.getManagedObjectSourceId());
					if (mos != null) {
						conn.setOfficeManagedObject(mo);
						conn.setManagedObjectSource(mos);
						conn.connect();
					}
				}
			}

			// Connect the teams
			for (OfficeTeamModel officeTeam : office.getTeams()) {
				OfficeTeamToTeamModel conn = officeTeam.getTeam();
				if (conn != null) {
					TeamModel team = teams.get(conn.getTeamId());
					if (team != null) {
						conn.setOfficeTeam(officeTeam);
						conn.setTeam(team);
						conn.connect();
					}
				}
			}
		}

		// Return the office floor
		return officeFloor;
	}

	/**
	 * Stores the {@link OfficeFloorModel}.
	 * 
	 * @param officeFloor
	 *            {@link OfficeFloorModel} to store.
	 * @param configurationItem
	 *            {@link ConfigurationItem} to contain the stored
	 *            {@link OfficeFloorModel}.
	 * @throws Exception
	 *             If fails to store the {@link OfficeFloorModel}.
	 */
	public void storeOfficeFloor(OfficeFloorModel officeFloor,
			ConfigurationItem configurationItem) throws Exception {
		
		// Ensure the team are linked
		for (TeamModel teamModel : officeFloor.getTeams()) {
			for (OfficeTeamToTeamModel conn : teamModel.getOfficeTeams()) {
				conn.setTeamId(teamModel.getId());
			}
		}
		
		// Store the model
		this.modelRepository.store(officeFloor, configurationItem);
	}

	/**
	 * Loads the {@link OfficeFloorOfficeModel} from the input
	 * {@link ConfigurationItem} for a {@link OfficeModel}.
	 * 
	 * @param configurationItem
	 *            {@link OfficeModel} {@link ConfigurationItem}.
	 * @throws Exception
	 *             If fails to load the {@link OfficeFloorOfficeModel}.
	 */
	public OfficeFloorOfficeModel loadOfficeFloorOffice(
			ConfigurationItem configurationItem) throws Exception {

		// Load the Office
		OfficeLoader officeLoader = new OfficeLoader();
		OfficeModel office = officeLoader.loadOffice(configurationItem);

		// Create the office floor office
		OfficeFloorOfficeModel officeFloorOffice = new OfficeFloorOfficeModel();

		// Synchronise the office onto the office floor office
		OfficeToOfficeFloorOfficeSynchroniser
				.synchroniseOfficeOntoOfficeFloorOffice(configurationItem
						.getId(), office, officeFloorOffice);

		// Return the office floor office
		return officeFloorOffice;
	}

}
