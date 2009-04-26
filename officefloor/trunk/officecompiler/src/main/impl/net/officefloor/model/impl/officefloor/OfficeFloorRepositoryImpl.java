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
package net.officefloor.model.impl.officefloor;

import java.util.HashMap;
import java.util.Map;

import net.officefloor.model.officefloor.DeployedOfficeModel;
import net.officefloor.model.officefloor.DeployedOfficeTeamModel;
import net.officefloor.model.officefloor.DeployedOfficeTeamToOfficeFloorTeamModel;
import net.officefloor.model.officefloor.OfficeFloorModel;
import net.officefloor.model.officefloor.OfficeFloorRepository;
import net.officefloor.model.officefloor.OfficeFloorTeamModel;
import net.officefloor.model.repository.ConfigurationItem;
import net.officefloor.model.repository.ModelRepository;

/**
 * {@link OfficeFloorRepository} implementation.
 * 
 * @author Daniel
 */
public class OfficeFloorRepositoryImpl implements OfficeFloorRepository {

	/**
	 * {@link ModelRepository}.
	 */
	private final ModelRepository modelRepository;

	/**
	 * Initiate.
	 * 
	 * @param modelRepository
	 *            {@link ModelRepository}.
	 */
	public OfficeFloorRepositoryImpl(ModelRepository modelRepository) {
		this.modelRepository = modelRepository;
	}

	/*
	 * ===================== OfficeFloorRepository =============================
	 */

	@Override
	public OfficeFloorModel retrieveOfficeFloor(ConfigurationItem configuration)
			throws Exception {

		// Load the office floor from configuration
		OfficeFloorModel officeFloor = this.modelRepository.retrieve(
				new OfficeFloorModel(), configuration);

		// Create the set of office floor teams
		Map<String, OfficeFloorTeamModel> teams = new HashMap<String, OfficeFloorTeamModel>();
		for (OfficeFloorTeamModel team : officeFloor.getOfficeFloorTeams()) {
			teams.put(team.getOfficeFloorTeamName(), team);
		}

		// Connect the office teams to the office floor teams
		for (DeployedOfficeModel office : officeFloor.getDeployedOffices()) {
			for (DeployedOfficeTeamModel officeTeam : office
					.getDeployedOfficeTeams()) {
				DeployedOfficeTeamToOfficeFloorTeamModel conn = officeTeam
						.getOfficeFloorTeam();
				if (conn != null) {
					OfficeFloorTeamModel officeFloorTeam = teams.get(conn
							.getOfficeFloorTeamName());
					if (officeFloorTeam != null) {
						conn.setDeployedOfficeTeam(officeTeam);
						conn.setOfficeFloorTeam(officeFloorTeam);
						conn.connect();
					}
				}
			}
		}

		// Return the office floor
		return officeFloor;
	}

	@Override
	public void storeOfficeFloor(OfficeFloorModel officeFloor,
			ConfigurationItem configuration) throws Exception {

		// Specify office teams to office floor teams
		for (OfficeFloorTeamModel officeFloorTeam : officeFloor
				.getOfficeFloorTeams()) {
			for (DeployedOfficeTeamToOfficeFloorTeamModel conn : officeFloorTeam
					.getDeployedOfficeTeams()) {
				conn.setOfficeFloorTeamName(officeFloorTeam
						.getOfficeFloorTeamName());
			}
		}

		// Store the office floor
		this.modelRepository.store(officeFloor, configuration);
	}

}