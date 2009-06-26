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
package net.officefloor.model.impl.office;

import java.util.HashMap;
import java.util.Map;

import net.officefloor.compile.impl.util.DoubleKeyMap;
import net.officefloor.frame.spi.administration.Administrator;
import net.officefloor.frame.spi.administration.Duty;
import net.officefloor.model.office.AdministratorModel;
import net.officefloor.model.office.AdministratorToOfficeTeamModel;
import net.officefloor.model.office.DutyModel;
import net.officefloor.model.office.ExternalManagedObjectModel;
import net.officefloor.model.office.ExternalManagedObjectToAdministratorModel;
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
import net.officefloor.model.office.OfficeSubSectionModel;
import net.officefloor.model.office.OfficeTaskModel;
import net.officefloor.model.office.OfficeTaskToPostDutyModel;
import net.officefloor.model.office.OfficeTaskToPreDutyModel;
import net.officefloor.model.office.OfficeTeamModel;
import net.officefloor.model.repository.ConfigurationItem;
import net.officefloor.model.repository.ModelRepository;

/**
 * {@link OfficeRepository} implementation.
 * 
 * @author Daniel Sagenschneider
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

		// Connect the administrators to the teams
		for (AdministratorModel admin : office.getOfficeAdministrators()) {
			AdministratorToOfficeTeamModel conn = admin.getOfficeTeam();
			if (conn != null) {
				OfficeTeamModel team = teams.get(conn.getOfficeTeamName());
				if (team != null) {
					conn.setAdministrator(admin);
					conn.setOfficeTeam(team);
					conn.connect();
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

		// Create the map of administrators and duties
		Map<String, AdministratorModel> administrators = new HashMap<String, AdministratorModel>();
		DoubleKeyMap<String, String, DutyModel> duties = new DoubleKeyMap<String, String, DutyModel>();
		for (AdministratorModel admin : office.getOfficeAdministrators()) {
			String administratorName = admin.getAdministratorName();
			administrators.put(administratorName, admin);
			for (DutyModel duty : admin.getDuties()) {
				duties.put(administratorName, duty.getDutyName(), duty);
			}
		}

		// Connect tasks to duties
		for (OfficeSectionModel section : office.getOfficeSections()) {
			this.connectTasksToDuties(section.getOfficeSubSection(), duties);
		}

		// Connect the external managed objects to administrators
		for (ExternalManagedObjectModel extMo : office
				.getExternalManagedObjects()) {
			for (ExternalManagedObjectToAdministratorModel conn : extMo
					.getAdministrators()) {
				AdministratorModel admin = administrators.get(conn
						.getAdministratorName());
				if (admin != null) {
					conn.setExternalManagedObject(extMo);
					conn.setAdministrator(admin);
					conn.connect();
				}
			}
		}

		// Return the office
		return office;
	}

	/**
	 * Connects the {@link OfficeTaskModel} to {@link DutyModel} instances.
	 * 
	 * @param subSection
	 *            {@link OfficeSubSectionModel}.
	 * @param duties
	 *            Map of {@link DutyModel} instances by {@link Administrator}
	 *            then {@link Duty} name.
	 */
	private void connectTasksToDuties(OfficeSubSectionModel subSection,
			DoubleKeyMap<String, String, DutyModel> duties) {

		// Ensure have the sub section
		if (subSection == null) {
			return;
		}

		// Connect the sub section tasks to pre duties
		for (OfficeTaskModel task : subSection.getOfficeTasks()) {
			for (OfficeTaskToPreDutyModel conn : task.getPreDuties()) {
				DutyModel duty = duties.get(conn.getAdministratorName(), conn
						.getDutyName());
				if (duty != null) {
					conn.setOfficeTask(task);
					conn.setDuty(duty);
					conn.connect();
				}
			}
		}

		// Connect the sub section tasks to post duties
		for (OfficeTaskModel task : subSection.getOfficeTasks()) {
			for (OfficeTaskToPostDutyModel conn : task.getPostDuties()) {
				DutyModel duty = duties.get(conn.getAdministratorName(), conn
						.getDutyName());
				if (duty != null) {
					conn.setOfficeTask(task);
					conn.setDuty(duty);
					conn.connect();
				}
			}
		}

		// Connect task to duties for further sub sections
		for (OfficeSubSectionModel subSubSection : subSection
				.getOfficeSubSections()) {
			this.connectTasksToDuties(subSubSection, duties);
		}
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

		// Specify administrators to team
		for (OfficeTeamModel team : office.getOfficeTeams()) {
			for (AdministratorToOfficeTeamModel conn : team.getAdministrators()) {
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

		// Specify pre duties to office tasks
		for (AdministratorModel admin : office.getOfficeAdministrators()) {
			for (DutyModel duty : admin.getDuties()) {
				for (OfficeTaskToPreDutyModel conn : duty.getPreOfficeTasks()) {
					conn.setAdministratorName(admin.getAdministratorName());
					conn.setDutyName(duty.getDutyName());
				}
			}
		}

		// Specify post duties to office tasks
		for (AdministratorModel admin : office.getOfficeAdministrators()) {
			for (DutyModel duty : admin.getDuties()) {
				for (OfficeTaskToPostDutyModel conn : duty.getPostOfficeTasks()) {
					conn.setAdministratorName(admin.getAdministratorName());
					conn.setDutyName(duty.getDutyName());
				}
			}
		}

		// Specify external managed objects to administrators
		for (AdministratorModel admin : office.getOfficeAdministrators()) {
			for (ExternalManagedObjectToAdministratorModel conn : admin
					.getExternalManagedObjects()) {
				conn.setAdministratorName(admin.getAdministratorName());
			}
		}

		// Store the office into the configuration
		this.modelRepository.store(office, configuration);
	}

}