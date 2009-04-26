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
package net.officefloor.model.impl.office;

import java.util.HashMap;
import java.util.Map;

import net.officefloor.model.office.OfficeModel;
import net.officefloor.model.office.OfficeRepository;
import net.officefloor.model.office.OfficeSectionModel;
import net.officefloor.model.office.OfficeSectionResponsibilityModel;
import net.officefloor.model.office.OfficeSectionResponsibilityToOfficeTeamModel;
import net.officefloor.model.office.OfficeTeamModel;
import net.officefloor.model.repository.ConfigurationItem;
import net.officefloor.model.repository.ModelRepository;

/**
 * {@link OfficeRepository} implementation.
 * 
 * @author Daniel
 */
public class OfficeRepositoryImpl implements OfficeRepository {

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
	public OfficeRepositoryImpl(ModelRepository modelRepository) {
		this.modelRepository = modelRepository;
	}

	/*
	 * ===================== OfficeRepository ============================
	 */

	@Override
	public OfficeModel retrieveOffice(ConfigurationItem configuration)
			throws Exception {

		// Load the office from the configuration
		OfficeModel office = this.modelRepository.retrieve(new OfficeModel(),
				configuration);

		// Create the set of office teams
		Map<String, OfficeTeamModel> teams = new HashMap<String, OfficeTeamModel>();
		for (OfficeTeamModel team : office.getOfficeTeams()) {
			teams.put(team.getOfficeTeamName(), team);
		}

		// Connect the responsibilities to the teams
		for (OfficeSectionModel section : office.getOfficeSections()) {
			for (OfficeSectionResponsibilityModel responsibility : section
					.getOfficeSectionResponsibilities()) {
				OfficeSectionResponsibilityToOfficeTeamModel conn = responsibility
						.getOfficeTeam();
				if (conn != null) {
					OfficeTeamModel team = teams.get(conn.getOfficeTeamName());
					if (team != null) {
						conn.setOfficeSectionResponsibility(responsibility);
						conn.setOfficeTeam(team);
						conn.connect();
					}
				}
			}
		}

		// Return the office
		return office;
	}

	@Override
	public void storeOffice(OfficeModel office, ConfigurationItem configuration)
			throws Exception {

		// Specify responsibility to team
		for (OfficeTeamModel team : office.getOfficeTeams()) {
			for (OfficeSectionResponsibilityToOfficeTeamModel conn : team
					.getOfficeSectionResponsibilities()) {
				conn.setOfficeTeamName(team.getOfficeTeamName());
			}
		}

		// Store the office into the configuration
		this.modelRepository.store(office, configuration);
	}

}