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

import net.officefloor.compile.impl.util.DoubleKeyMap;
import net.officefloor.model.office.ExternalManagedObjectModel;
import net.officefloor.model.office.OfficeModel;
import net.officefloor.model.office.OfficeRepository;
import net.officefloor.model.office.OfficeSectionInputModel;
import net.officefloor.model.office.OfficeSectionModel;
import net.officefloor.model.office.OfficeSectionObjectModel;
import net.officefloor.model.office.OfficeSectionObjectToExternalManagedObjectModel;
import net.officefloor.model.office.OfficeSectionOutputModel;
import net.officefloor.model.office.OfficeSectionOutputToOfficeSectionInputModel;
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

		// Create the set of office section inputs
		DoubleKeyMap<String, String, OfficeSectionInputModel> inputs = new DoubleKeyMap<String, String, OfficeSectionInputModel>();
		for (OfficeSectionModel section : office.getOfficeSections()) {
			for (OfficeSectionInputModel input : section
					.getOfficeSectionInputs()) {
				inputs.put(section.getOfficeSectionName(), input
						.getOfficeSectionInputName(), input);
			}
		}

		// Connect the outputs to the inputs
		for (OfficeSectionModel section : office.getOfficeSections()) {
			for (OfficeSectionOutputModel output : section
					.getOfficeSectionOutputs()) {
				OfficeSectionOutputToOfficeSectionInputModel conn = output
						.getOfficeSectionInput();
				if (conn != null) {
					OfficeSectionInputModel input = inputs.get(conn
							.getOfficeSectionName(), conn
							.getOfficeSectionInputName());
					if (input != null) {
						conn.setOfficeSectionOutput(output);
						conn.setOfficeSectionInput(input);
						conn.connect();
					}
				}
			}
		}

		// Create the set of external managed objects
		Map<String, ExternalManagedObjectModel> extMos = new HashMap<String, ExternalManagedObjectModel>();
		for (ExternalManagedObjectModel extMo : office
				.getExternalManagedObjects()) {
			extMos.put(extMo.getExternalManagedObjectName(), extMo);
		}

		// Connect the objects to the external managed objects
		for (OfficeSectionModel section : office.getOfficeSections()) {
			for (OfficeSectionObjectModel object : section
					.getOfficeSectionObjects()) {
				OfficeSectionObjectToExternalManagedObjectModel conn = object
						.getExternalManagedObject();
				if (conn != null) {
					ExternalManagedObjectModel extMo = extMos.get(conn
							.getExternalManagedObjectName());
					if (extMo != null) {
						conn.setOfficeSectionObject(object);
						conn.setExternalManagedObject(extMo);
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

		// Specify outputs to inputs
		for (OfficeSectionModel section : office.getOfficeSections()) {
			for (OfficeSectionInputModel input : section
					.getOfficeSectionInputs()) {
				for (OfficeSectionOutputToOfficeSectionInputModel conn : input
						.getOfficeSectionOutputs()) {
					conn.setOfficeSectionName(section.getOfficeSectionName());
					conn.setOfficeSectionInputName(input
							.getOfficeSectionInputName());
				}
			}
		}

		// Specify objects to external managed objects
		for (ExternalManagedObjectModel extMo : office
				.getExternalManagedObjects()) {
			for (OfficeSectionObjectToExternalManagedObjectModel conn : extMo
					.getOfficeSectionObjects()) {
				conn.setExternalManagedObjectName(extMo
						.getExternalManagedObjectName());
			}
		}

		// Store the office into the configuration
		this.modelRepository.store(office, configuration);
	}

}