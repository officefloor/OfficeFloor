/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2011 Daniel Sagenschneider
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
import net.officefloor.frame.spi.governance.Governance;
import net.officefloor.model.office.AdministratorModel;
import net.officefloor.model.office.AdministratorToOfficeTeamModel;
import net.officefloor.model.office.DutyModel;
import net.officefloor.model.office.ExternalManagedObjectModel;
import net.officefloor.model.office.ExternalManagedObjectToAdministratorModel;
import net.officefloor.model.office.ExternalManagedObjectToOfficeGovernanceModel;
import net.officefloor.model.office.OfficeEscalationModel;
import net.officefloor.model.office.OfficeEscalationToOfficeSectionInputModel;
import net.officefloor.model.office.OfficeGovernanceModel;
import net.officefloor.model.office.OfficeGovernanceToOfficeTeamModel;
import net.officefloor.model.office.OfficeInputManagedObjectDependencyModel;
import net.officefloor.model.office.OfficeInputManagedObjectDependencyToExternalManagedObjectModel;
import net.officefloor.model.office.OfficeInputManagedObjectDependencyToOfficeManagedObjectModel;
import net.officefloor.model.office.OfficeManagedObjectDependencyModel;
import net.officefloor.model.office.OfficeManagedObjectDependencyToExternalManagedObjectModel;
import net.officefloor.model.office.OfficeManagedObjectDependencyToOfficeManagedObjectModel;
import net.officefloor.model.office.OfficeManagedObjectModel;
import net.officefloor.model.office.OfficeManagedObjectSourceFlowModel;
import net.officefloor.model.office.OfficeManagedObjectSourceFlowToOfficeSectionInputModel;
import net.officefloor.model.office.OfficeManagedObjectSourceModel;
import net.officefloor.model.office.OfficeManagedObjectSourceTeamModel;
import net.officefloor.model.office.OfficeManagedObjectSourceTeamToOfficeTeamModel;
import net.officefloor.model.office.OfficeManagedObjectToAdministratorModel;
import net.officefloor.model.office.OfficeManagedObjectToOfficeGovernanceModel;
import net.officefloor.model.office.OfficeManagedObjectToOfficeManagedObjectSourceModel;
import net.officefloor.model.office.OfficeModel;
import net.officefloor.model.office.OfficeRepository;
import net.officefloor.model.office.OfficeSectionInputModel;
import net.officefloor.model.office.OfficeSectionManagedObjectModel;
import net.officefloor.model.office.OfficeSectionManagedObjectToOfficeGovernanceModel;
import net.officefloor.model.office.OfficeSectionModel;
import net.officefloor.model.office.OfficeSectionObjectModel;
import net.officefloor.model.office.OfficeSectionObjectToExternalManagedObjectModel;
import net.officefloor.model.office.OfficeSectionObjectToOfficeManagedObjectModel;
import net.officefloor.model.office.OfficeSectionOutputModel;
import net.officefloor.model.office.OfficeSectionOutputToOfficeSectionInputModel;
import net.officefloor.model.office.OfficeSectionResponsibilityModel;
import net.officefloor.model.office.OfficeSectionResponsibilityToOfficeTeamModel;
import net.officefloor.model.office.OfficeSubSectionModel;
import net.officefloor.model.office.OfficeSubSectionToOfficeGovernanceModel;
import net.officefloor.model.office.OfficeTaskModel;
import net.officefloor.model.office.OfficeTaskToOfficeGovernanceModel;
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

		// Connect the managed object source teams to the office teams
		for (OfficeManagedObjectSourceModel mos : office
				.getOfficeManagedObjectSources()) {
			for (OfficeManagedObjectSourceTeamModel moTeam : mos
					.getOfficeManagedObjectSourceTeams()) {
				OfficeManagedObjectSourceTeamToOfficeTeamModel conn = moTeam
						.getOfficeTeam();
				if (conn != null) {
					OfficeTeamModel team = teams.get(conn.getOfficeTeamName());
					if (team != null) {
						conn.setOfficeManagedObjectSourceTeam(moTeam);
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

		// Connect the governances to the teams
		for (OfficeGovernanceModel gov : office.getOfficeGovernances()) {
			OfficeGovernanceToOfficeTeamModel conn = gov.getOfficeTeam();
			if (conn != null) {
				OfficeTeamModel team = teams.get(conn.getOfficeTeamName());
				if (team != null) {
					conn.setOfficeGovernance(gov);
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
				inputs.put(section.getOfficeSectionName(),
						input.getOfficeSectionInputName(), input);
			}
		}

		// Connect the outputs to the inputs
		for (OfficeSectionModel section : office.getOfficeSections()) {
			for (OfficeSectionOutputModel output : section
					.getOfficeSectionOutputs()) {
				OfficeSectionOutputToOfficeSectionInputModel conn = output
						.getOfficeSectionInput();
				if (conn != null) {
					OfficeSectionInputModel input = inputs.get(
							conn.getOfficeSectionName(),
							conn.getOfficeSectionInputName());
					if (input != null) {
						conn.setOfficeSectionOutput(output);
						conn.setOfficeSectionInput(input);
						conn.connect();
					}
				}
			}
		}

		// Connect the managed object source flows to the inputs
		for (OfficeManagedObjectSourceModel mos : office
				.getOfficeManagedObjectSources()) {
			for (OfficeManagedObjectSourceFlowModel flow : mos
					.getOfficeManagedObjectSourceFlows()) {
				OfficeManagedObjectSourceFlowToOfficeSectionInputModel conn = flow
						.getOfficeSectionInput();
				if (conn != null) {
					OfficeSectionInputModel input = inputs.get(
							conn.getOfficeSectionName(),
							conn.getOfficeSectionInputName());
					if (input != null) {
						conn.setOfficeManagedObjectSourceFlow(flow);
						conn.setOfficeSectionInput(input);
						conn.connect();
					}
				}
			}
		}

		// Connect the escalations to the inputs
		for (OfficeEscalationModel escalation : office.getOfficeEscalations()) {
			OfficeEscalationToOfficeSectionInputModel conn = escalation
					.getOfficeSectionInput();
			if (conn != null) {
				OfficeSectionInputModel sectionInput = inputs.get(
						conn.getOfficeSectionName(),
						conn.getOfficeSectionInputName());
				if (sectionInput != null) {
					conn.setOfficeEscalation(escalation);
					conn.setOfficeSectionInput(sectionInput);
					conn.connect();
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

		// Create the set of office managed objects
		Map<String, OfficeManagedObjectModel> managedObjects = new HashMap<String, OfficeManagedObjectModel>();
		for (OfficeManagedObjectModel managedObject : office
				.getOfficeManagedObjects()) {
			managedObjects.put(managedObject.getOfficeManagedObjectName(),
					managedObject);
		}

		// Connect the objects to the office managed objects
		for (OfficeSectionModel section : office.getOfficeSections()) {
			for (OfficeSectionObjectModel object : section
					.getOfficeSectionObjects()) {
				OfficeSectionObjectToOfficeManagedObjectModel conn = object
						.getOfficeManagedObject();
				if (conn != null) {
					OfficeManagedObjectModel mo = managedObjects.get(conn
							.getOfficeManagedObjectName());
					if (mo != null) {
						conn.setOfficeSectionObject(object);
						conn.setOfficeManagedObject(mo);
						conn.connect();
					}
				}
			}
		}

		// Connect to managed object dependencies to external managed objects
		for (OfficeManagedObjectModel mo : office.getOfficeManagedObjects()) {
			for (OfficeManagedObjectDependencyModel dependency : mo
					.getOfficeManagedObjectDependencies()) {
				OfficeManagedObjectDependencyToExternalManagedObjectModel conn = dependency
						.getExternalManagedObject();
				if (conn != null) {
					ExternalManagedObjectModel extMo = extMos.get(conn
							.getExternalManagedObjectName());
					if (extMo != null) {
						conn.setOfficeManagedObjectDependency(dependency);
						conn.setExternalManagedObject(extMo);
						conn.connect();
					}
				}
			}
		}

		// Connect to managed object dependencies to managed object
		for (OfficeManagedObjectModel mo : office.getOfficeManagedObjects()) {
			for (OfficeManagedObjectDependencyModel dependency : mo
					.getOfficeManagedObjectDependencies()) {
				OfficeManagedObjectDependencyToOfficeManagedObjectModel conn = dependency
						.getOfficeManagedObject();
				if (conn != null) {
					OfficeManagedObjectModel dependentMo = managedObjects
							.get(conn.getOfficeManagedObjectName());
					if (dependentMo != null) {
						conn.setOfficeManagedObjectDependency(dependency);
						conn.setOfficeManagedObject(dependentMo);
						conn.connect();
					}
				}
			}
		}

		// Connect input managed object dependencies to external managed object
		for (OfficeManagedObjectSourceModel mos : office
				.getOfficeManagedObjectSources()) {
			for (OfficeInputManagedObjectDependencyModel dependency : mos
					.getOfficeInputManagedObjectDependencies()) {
				OfficeInputManagedObjectDependencyToExternalManagedObjectModel conn = dependency
						.getExternalManagedObject();
				if (conn != null) {
					ExternalManagedObjectModel extMo = extMos.get(conn
							.getExternalManagedObjectName());
					if (extMo != null) {
						conn.setOfficeInputManagedObjectDependency(dependency);
						conn.setExternalManagedObject(extMo);
						conn.connect();
					}
				}
			}
		}

		// Connect input managed object dependencies to managed object
		for (OfficeManagedObjectSourceModel mos : office
				.getOfficeManagedObjectSources()) {
			for (OfficeInputManagedObjectDependencyModel dependency : mos
					.getOfficeInputManagedObjectDependencies()) {
				OfficeInputManagedObjectDependencyToOfficeManagedObjectModel conn = dependency
						.getOfficeManagedObject();
				if (conn != null) {
					OfficeManagedObjectModel mo = managedObjects.get(conn
							.getOfficeManagedObjectName());
					if (mo != null) {
						conn.setOfficeInputManagedObjectDependency(dependency);
						conn.setOfficeManagedObject(mo);
						conn.connect();
					}
				}
			}
		}

		// Create the set of managed object sources
		Map<String, OfficeManagedObjectSourceModel> managedObjectSources = new HashMap<String, OfficeManagedObjectSourceModel>();
		for (OfficeManagedObjectSourceModel managedObjectSource : office
				.getOfficeManagedObjectSources()) {
			managedObjectSources.put(
					managedObjectSource.getOfficeManagedObjectSourceName(),
					managedObjectSource);
		}

		// Connect the managed objects to their corresponding sources
		for (OfficeManagedObjectModel managedObject : office
				.getOfficeManagedObjects()) {
			OfficeManagedObjectToOfficeManagedObjectSourceModel conn = managedObject
					.getOfficeManagedObjectSource();
			if (conn != null) {
				OfficeManagedObjectSourceModel managedObjectSource = managedObjectSources
						.get(conn.getOfficeManagedObjectSourceName());
				if (managedObjectSource != null) {
					conn.setOfficeManagedObject(managedObject);
					conn.setOfficeManagedObjectSource(managedObjectSource);
					conn.connect();
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

		// Connect the managed objects to administrators
		for (OfficeManagedObjectModel mo : office.getOfficeManagedObjects()) {
			for (OfficeManagedObjectToAdministratorModel conn : mo
					.getAdministrators()) {
				AdministratorModel admin = administrators.get(conn
						.getAdministratorName());
				if (admin != null) {
					conn.setOfficeManagedObject(mo);
					conn.setAdministrator(admin);
					conn.connect();
				}
			}
		}

		// Create the map of governances
		Map<String, OfficeGovernanceModel> governances = new HashMap<String, OfficeGovernanceModel>();
		for (OfficeGovernanceModel gov : office.getOfficeGovernances()) {
			String governanceName = gov.getOfficeGovernanceName();
			governances.put(governanceName, gov);
		}

		// Connect the external managed objects to governances
		for (ExternalManagedObjectModel extMo : office
				.getExternalManagedObjects()) {
			for (ExternalManagedObjectToOfficeGovernanceModel conn : extMo
					.getOfficeGovernances()) {
				OfficeGovernanceModel gov = governances.get(conn
						.getOfficeGovernanceName());
				if (gov != null) {
					conn.setExternalManagedObject(extMo);
					conn.setOfficeGovernance(gov);
					conn.connect();
				}
			}
		}

		// Connect the managed objects to governances
		for (OfficeManagedObjectModel mo : office.getOfficeManagedObjects()) {
			for (OfficeManagedObjectToOfficeGovernanceModel conn : mo
					.getOfficeGovernances()) {
				OfficeGovernanceModel gov = governances.get(conn
						.getOfficeGovernanceName());
				if (gov != null) {
					conn.setOfficeManagedObject(mo);
					conn.setOfficeGovernance(gov);
					conn.connect();
				}
			}
		}

		// Connect the sub sections
		for (OfficeSectionModel section : office.getOfficeSections()) {
			this.connectSubSections(section.getOfficeSubSection(), duties,
					governances);
		}

		// Return the office
		return office;
	}

	/**
	 * Connects the {@link OfficeSubSectionModel} instances.
	 * 
	 * @param subSection
	 *            {@link OfficeSubSectionModel}.
	 * @param duties
	 *            Map of {@link DutyModel} instances by {@link Administrator}
	 *            then {@link Duty} name.
	 * @param governances
	 *            Map of {@link OfficeGovernanceModel} instances by
	 *            {@link Governance} name.
	 */
	private void connectSubSections(OfficeSubSectionModel subSection,
			DoubleKeyMap<String, String, DutyModel> duties,
			Map<String, OfficeGovernanceModel> governances) {

		// Ensure have the sub section
		if (subSection == null) {
			return;
		}

		// Connect sub section to governances
		for (OfficeSubSectionToOfficeGovernanceModel conn : subSection
				.getOfficeGovernances()) {
			OfficeGovernanceModel governance = governances.get(conn
					.getOfficeGovernanceName());
			if (governance != null) {
				conn.setOfficeSubSection(subSection);
				conn.setOfficeGovernance(governance);
				conn.connect();
			}
		}

		// Connect the sub section tasks to pre duties
		for (OfficeTaskModel task : subSection.getOfficeTasks()) {
			for (OfficeTaskToPreDutyModel conn : task.getPreDuties()) {
				DutyModel duty = duties.get(conn.getAdministratorName(),
						conn.getDutyName());
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
				DutyModel duty = duties.get(conn.getAdministratorName(),
						conn.getDutyName());
				if (duty != null) {
					conn.setOfficeTask(task);
					conn.setDuty(duty);
					conn.connect();
				}
			}
		}

		// Connect the sub section tasks to governance
		for (OfficeTaskModel task : subSection.getOfficeTasks()) {
			for (OfficeTaskToOfficeGovernanceModel conn : task
					.getOfficeGovernances()) {
				OfficeGovernanceModel governance = governances.get(conn
						.getOfficeGovernanceName());
				if (governance != null) {
					conn.setOfficeTask(task);
					conn.setOfficeGovernance(governance);
					conn.connect();
				}
			}
		}

		// Connect the section managed objects to governance
		for (OfficeSectionManagedObjectModel mo : subSection
				.getOfficeSectionManagedObjects()) {
			for (OfficeSectionManagedObjectToOfficeGovernanceModel conn : mo
					.getOfficeGovernances()) {
				OfficeGovernanceModel governance = governances.get(conn
						.getOfficeGovernanceName());
				if (governance != null) {
					conn.setOfficeSectionManagedObject(mo);
					conn.setOfficeGovernance(governance);
					conn.connect();
				}
			}
		}

		// Connect task to duties for further sub sections
		for (OfficeSubSectionModel subSubSection : subSection
				.getOfficeSubSections()) {
			this.connectSubSections(subSubSection, duties, governances);
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

		// Specify managed object source teams to office teams
		for (OfficeTeamModel team : office.getOfficeTeams()) {
			for (OfficeManagedObjectSourceTeamToOfficeTeamModel conn : team
					.getOfficeManagedObjectSourceTeams()) {
				conn.setOfficeTeamName(team.getOfficeTeamName());
			}
		}

		// Specify administrators to team
		for (OfficeTeamModel team : office.getOfficeTeams()) {
			for (AdministratorToOfficeTeamModel conn : team.getAdministrators()) {
				conn.setOfficeTeamName(team.getOfficeTeamName());
			}
		}

		// Specify governances to team
		for (OfficeTeamModel team : office.getOfficeTeams()) {
			for (OfficeGovernanceToOfficeTeamModel conn : team
					.getOfficeGovernances()) {
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

		// Specify managed object source flows to inputs
		for (OfficeSectionModel section : office.getOfficeSections()) {
			for (OfficeSectionInputModel input : section
					.getOfficeSectionInputs()) {
				for (OfficeManagedObjectSourceFlowToOfficeSectionInputModel conn : input
						.getOfficeManagedObjectSourceFlows()) {
					conn.setOfficeSectionName(section.getOfficeSectionName());
					conn.setOfficeSectionInputName(input
							.getOfficeSectionInputName());
				}
			}
		}

		// Specify escalation to inputs
		for (OfficeSectionModel section : office.getOfficeSections()) {
			for (OfficeSectionInputModel input : section
					.getOfficeSectionInputs()) {
				for (OfficeEscalationToOfficeSectionInputModel conn : input
						.getOfficeEscalations()) {
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

		// Specify objects to office managed objects
		for (OfficeManagedObjectModel mo : office.getOfficeManagedObjects()) {
			for (OfficeSectionObjectToOfficeManagedObjectModel conn : mo
					.getOfficeSectionObjects()) {
				conn.setOfficeManagedObjectName(mo.getOfficeManagedObjectName());
			}
		}

		// Specify managed objects to their corresponding sources
		for (OfficeManagedObjectSourceModel mos : office
				.getOfficeManagedObjectSources()) {
			for (OfficeManagedObjectToOfficeManagedObjectSourceModel conn : mos
					.getOfficeManagedObjects()) {
				conn.setOfficeManagedObjectSourceName(mos
						.getOfficeManagedObjectSourceName());
			}
		}

		// Specify external managed objects to dependencies
		for (ExternalManagedObjectModel extMo : office
				.getExternalManagedObjects()) {
			for (OfficeManagedObjectDependencyToExternalManagedObjectModel conn : extMo
					.getDependentOfficeManagedObjects()) {
				conn.setExternalManagedObjectName(extMo
						.getExternalManagedObjectName());
			}
		}

		// Specify managed objects to dependencies
		for (OfficeManagedObjectModel mo : office.getOfficeManagedObjects()) {
			for (OfficeManagedObjectDependencyToOfficeManagedObjectModel conn : mo
					.getDependentOfficeManagedObjects()) {
				conn.setOfficeManagedObjectName(mo.getOfficeManagedObjectName());
			}
		}

		// Specify external managed objects to input dependencies
		for (ExternalManagedObjectModel extMo : office
				.getExternalManagedObjects()) {
			for (OfficeInputManagedObjectDependencyToExternalManagedObjectModel conn : extMo
					.getDependentOfficeInputManagedObjects()) {
				conn.setExternalManagedObjectName(extMo
						.getExternalManagedObjectName());
			}
		}

		// Specify managed objects to input dependencies
		for (OfficeManagedObjectModel mo : office.getOfficeManagedObjects()) {
			for (OfficeInputManagedObjectDependencyToOfficeManagedObjectModel conn : mo
					.getDependentOfficeInputManagedObjects()) {
				conn.setOfficeManagedObjectName(mo.getOfficeManagedObjectName());
			}
		}

		// Specify governances to sub sections
		for (OfficeGovernanceModel governance : office.getOfficeGovernances()) {
			for (OfficeSubSectionToOfficeGovernanceModel conn : governance
					.getOfficeSubSections()) {
				conn.setOfficeGovernanceName(governance
						.getOfficeGovernanceName());
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

		// Specify governances to office tasks
		for (OfficeGovernanceModel governance : office.getOfficeGovernances()) {
			for (OfficeTaskToOfficeGovernanceModel conn : governance
					.getOfficeTasks()) {
				conn.setOfficeGovernanceName(governance
						.getOfficeGovernanceName());
			}
		}

		// Specify governance to section managed objects
		for (OfficeGovernanceModel governance : office.getOfficeGovernances()) {
			for (OfficeSectionManagedObjectToOfficeGovernanceModel conn : governance
					.getOfficeSectionManagedObjects()) {
				conn.setOfficeGovernanceName(governance
						.getOfficeGovernanceName());
			}
		}

		// Specify external managed objects to administrators
		for (AdministratorModel admin : office.getOfficeAdministrators()) {
			for (ExternalManagedObjectToAdministratorModel conn : admin
					.getExternalManagedObjects()) {
				conn.setAdministratorName(admin.getAdministratorName());
			}
		}

		// Specify managed objects to administrators
		for (AdministratorModel admin : office.getOfficeAdministrators()) {
			for (OfficeManagedObjectToAdministratorModel conn : admin
					.getOfficeManagedObjects()) {
				conn.setAdministratorName(admin.getAdministratorName());
			}
		}

		// Specify external managed objects to governances
		for (OfficeGovernanceModel gov : office.getOfficeGovernances()) {
			for (ExternalManagedObjectToOfficeGovernanceModel conn : gov
					.getExternalManagedObjects()) {
				conn.setOfficeGovernanceName(gov.getOfficeGovernanceName());
			}
		}

		// Specify managed objects to governances
		for (OfficeGovernanceModel gov : office.getOfficeGovernances()) {
			for (OfficeManagedObjectToOfficeGovernanceModel conn : gov
					.getOfficeManagedObjects()) {
				conn.setOfficeGovernanceName(gov.getOfficeGovernanceName());
			}
		}

		// Store the office into the configuration
		this.modelRepository.store(office, configuration);
	}

}