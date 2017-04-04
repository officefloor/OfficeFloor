/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2013 Daniel Sagenschneider
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
import net.officefloor.frame.api.administration.Administration;
import net.officefloor.frame.api.governance.Governance;
import net.officefloor.model.office.AdministrationModel;
import net.officefloor.model.office.AdministrationToOfficeTeamModel;
import net.officefloor.model.office.ExternalManagedObjectModel;
import net.officefloor.model.office.ExternalManagedObjectToAdministrationModel;
import net.officefloor.model.office.ExternalManagedObjectToGovernanceModel;
import net.officefloor.model.office.GovernanceModel;
import net.officefloor.model.office.GovernanceToOfficeTeamModel;
import net.officefloor.model.office.OfficeEscalationModel;
import net.officefloor.model.office.OfficeEscalationToOfficeSectionInputModel;
import net.officefloor.model.office.OfficeFunctionModel;
import net.officefloor.model.office.OfficeFunctionToGovernanceModel;
import net.officefloor.model.office.OfficeFunctionToPostAdministrationModel;
import net.officefloor.model.office.OfficeFunctionToPreAdministrationModel;
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
import net.officefloor.model.office.OfficeManagedObjectToAdministrationModel;
import net.officefloor.model.office.OfficeManagedObjectToGovernanceModel;
import net.officefloor.model.office.OfficeManagedObjectToOfficeManagedObjectSourceModel;
import net.officefloor.model.office.OfficeModel;
import net.officefloor.model.office.OfficeRepository;
import net.officefloor.model.office.OfficeSectionInputModel;
import net.officefloor.model.office.OfficeSectionManagedObjectModel;
import net.officefloor.model.office.OfficeSectionManagedObjectToGovernanceModel;
import net.officefloor.model.office.OfficeSectionModel;
import net.officefloor.model.office.OfficeSectionObjectModel;
import net.officefloor.model.office.OfficeSectionObjectToExternalManagedObjectModel;
import net.officefloor.model.office.OfficeSectionObjectToOfficeManagedObjectModel;
import net.officefloor.model.office.OfficeSectionOutputModel;
import net.officefloor.model.office.OfficeSectionOutputToOfficeSectionInputModel;
import net.officefloor.model.office.OfficeSectionResponsibilityModel;
import net.officefloor.model.office.OfficeSectionResponsibilityToOfficeTeamModel;
import net.officefloor.model.office.OfficeStartModel;
import net.officefloor.model.office.OfficeStartToOfficeSectionInputModel;
import net.officefloor.model.office.OfficeSubSectionModel;
import net.officefloor.model.office.OfficeSubSectionToGovernanceModel;
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
	public OfficeModel retrieveOffice(ConfigurationItem configuration) throws Exception {

		// Load the office from the configuration
		OfficeModel office = this.modelRepository.retrieve(new OfficeModel(), configuration);

		// Create the set of office teams
		Map<String, OfficeTeamModel> teams = new HashMap<String, OfficeTeamModel>();
		for (OfficeTeamModel team : office.getOfficeTeams()) {
			teams.put(team.getOfficeTeamName(), team);
		}

		// Connect the responsibilities to the teams
		for (OfficeSectionModel section : office.getOfficeSections()) {
			for (OfficeSectionResponsibilityModel responsibility : section.getOfficeSectionResponsibilities()) {
				OfficeSectionResponsibilityToOfficeTeamModel conn = responsibility.getOfficeTeam();
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
		for (OfficeManagedObjectSourceModel mos : office.getOfficeManagedObjectSources()) {
			for (OfficeManagedObjectSourceTeamModel moTeam : mos.getOfficeManagedObjectSourceTeams()) {
				OfficeManagedObjectSourceTeamToOfficeTeamModel conn = moTeam.getOfficeTeam();
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

		// Connect the administration to the teams
		for (AdministrationModel admin : office.getAdministrations()) {
			AdministrationToOfficeTeamModel conn = admin.getOfficeTeam();
			if (conn != null) {
				OfficeTeamModel team = teams.get(conn.getOfficeTeamName());
				if (team != null) {
					conn.setAdministration(admin);
					conn.setOfficeTeam(team);
					conn.connect();
				}
			}
		}

		// Connect the governances to the teams
		for (GovernanceModel gov : office.getGovernances()) {
			GovernanceToOfficeTeamModel conn = gov.getOfficeTeam();
			if (conn != null) {
				OfficeTeamModel team = teams.get(conn.getOfficeTeamName());
				if (team != null) {
					conn.setGovernance(gov);
					conn.setOfficeTeam(team);
					conn.connect();
				}
			}
		}

		// Create the set of office section inputs
		DoubleKeyMap<String, String, OfficeSectionInputModel> inputs = new DoubleKeyMap<String, String, OfficeSectionInputModel>();
		for (OfficeSectionModel section : office.getOfficeSections()) {
			for (OfficeSectionInputModel input : section.getOfficeSectionInputs()) {
				inputs.put(section.getOfficeSectionName(), input.getOfficeSectionInputName(), input);
			}
		}

		// Connect the starts to the inputs
		for (OfficeStartModel start : office.getOfficeStarts()) {
			OfficeStartToOfficeSectionInputModel conn = start.getOfficeSectionInput();
			if (conn != null) {
				OfficeSectionInputModel input = inputs.get(conn.getOfficeSectionName(),
						conn.getOfficeSectionInputName());
				if (input != null) {
					conn.setOfficeStart(start);
					conn.setOfficeSectionInput(input);
					conn.connect();
				}
			}
		}

		// Connect the outputs to the inputs
		for (OfficeSectionModel section : office.getOfficeSections()) {
			for (OfficeSectionOutputModel output : section.getOfficeSectionOutputs()) {
				OfficeSectionOutputToOfficeSectionInputModel conn = output.getOfficeSectionInput();
				if (conn != null) {
					OfficeSectionInputModel input = inputs.get(conn.getOfficeSectionName(),
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
		for (OfficeManagedObjectSourceModel mos : office.getOfficeManagedObjectSources()) {
			for (OfficeManagedObjectSourceFlowModel flow : mos.getOfficeManagedObjectSourceFlows()) {
				OfficeManagedObjectSourceFlowToOfficeSectionInputModel conn = flow.getOfficeSectionInput();
				if (conn != null) {
					OfficeSectionInputModel input = inputs.get(conn.getOfficeSectionName(),
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
			OfficeEscalationToOfficeSectionInputModel conn = escalation.getOfficeSectionInput();
			if (conn != null) {
				OfficeSectionInputModel sectionInput = inputs.get(conn.getOfficeSectionName(),
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
		for (ExternalManagedObjectModel extMo : office.getExternalManagedObjects()) {
			extMos.put(extMo.getExternalManagedObjectName(), extMo);
		}

		// Connect the objects to the external managed objects
		for (OfficeSectionModel section : office.getOfficeSections()) {
			for (OfficeSectionObjectModel object : section.getOfficeSectionObjects()) {
				OfficeSectionObjectToExternalManagedObjectModel conn = object.getExternalManagedObject();
				if (conn != null) {
					ExternalManagedObjectModel extMo = extMos.get(conn.getExternalManagedObjectName());
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
		for (OfficeManagedObjectModel managedObject : office.getOfficeManagedObjects()) {
			managedObjects.put(managedObject.getOfficeManagedObjectName(), managedObject);
		}

		// Connect the objects to the office managed objects
		for (OfficeSectionModel section : office.getOfficeSections()) {
			for (OfficeSectionObjectModel object : section.getOfficeSectionObjects()) {
				OfficeSectionObjectToOfficeManagedObjectModel conn = object.getOfficeManagedObject();
				if (conn != null) {
					OfficeManagedObjectModel mo = managedObjects.get(conn.getOfficeManagedObjectName());
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
			for (OfficeManagedObjectDependencyModel dependency : mo.getOfficeManagedObjectDependencies()) {
				OfficeManagedObjectDependencyToExternalManagedObjectModel conn = dependency.getExternalManagedObject();
				if (conn != null) {
					ExternalManagedObjectModel extMo = extMos.get(conn.getExternalManagedObjectName());
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
			for (OfficeManagedObjectDependencyModel dependency : mo.getOfficeManagedObjectDependencies()) {
				OfficeManagedObjectDependencyToOfficeManagedObjectModel conn = dependency.getOfficeManagedObject();
				if (conn != null) {
					OfficeManagedObjectModel dependentMo = managedObjects.get(conn.getOfficeManagedObjectName());
					if (dependentMo != null) {
						conn.setOfficeManagedObjectDependency(dependency);
						conn.setOfficeManagedObject(dependentMo);
						conn.connect();
					}
				}
			}
		}

		// Connect input managed object dependencies to external managed object
		for (OfficeManagedObjectSourceModel mos : office.getOfficeManagedObjectSources()) {
			for (OfficeInputManagedObjectDependencyModel dependency : mos.getOfficeInputManagedObjectDependencies()) {
				OfficeInputManagedObjectDependencyToExternalManagedObjectModel conn = dependency
						.getExternalManagedObject();
				if (conn != null) {
					ExternalManagedObjectModel extMo = extMos.get(conn.getExternalManagedObjectName());
					if (extMo != null) {
						conn.setOfficeInputManagedObjectDependency(dependency);
						conn.setExternalManagedObject(extMo);
						conn.connect();
					}
				}
			}
		}

		// Connect input managed object dependencies to managed object
		for (OfficeManagedObjectSourceModel mos : office.getOfficeManagedObjectSources()) {
			for (OfficeInputManagedObjectDependencyModel dependency : mos.getOfficeInputManagedObjectDependencies()) {
				OfficeInputManagedObjectDependencyToOfficeManagedObjectModel conn = dependency.getOfficeManagedObject();
				if (conn != null) {
					OfficeManagedObjectModel mo = managedObjects.get(conn.getOfficeManagedObjectName());
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
		for (OfficeManagedObjectSourceModel managedObjectSource : office.getOfficeManagedObjectSources()) {
			managedObjectSources.put(managedObjectSource.getOfficeManagedObjectSourceName(), managedObjectSource);
		}

		// Connect the managed objects to their corresponding sources
		for (OfficeManagedObjectModel managedObject : office.getOfficeManagedObjects()) {
			OfficeManagedObjectToOfficeManagedObjectSourceModel conn = managedObject.getOfficeManagedObjectSource();
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
		Map<String, AdministrationModel> administrations = new HashMap<String, AdministrationModel>();
		for (AdministrationModel admin : office.getAdministrations()) {
			String administrationName = admin.getAdministrationName();
			administrations.put(administrationName, admin);
		}

		// Connect the external managed objects to administrations
		for (ExternalManagedObjectModel extMo : office.getExternalManagedObjects()) {
			for (ExternalManagedObjectToAdministrationModel conn : extMo.getAdministrations()) {
				AdministrationModel admin = administrations.get(conn.getAdministrationName());
				if (admin != null) {
					conn.setExternalManagedObject(extMo);
					conn.setAdministration(admin);
					conn.connect();
				}
			}
		}

		// Connect the managed objects to administrators
		for (OfficeManagedObjectModel mo : office.getOfficeManagedObjects()) {
			for (OfficeManagedObjectToAdministrationModel conn : mo.getAdministrations()) {
				AdministrationModel admin = administrations.get(conn.getAdministrationName());
				if (admin != null) {
					conn.setOfficeManagedObject(mo);
					conn.setAdministration(admin);
					conn.connect();
				}
			}
		}

		// Create the map of governances
		Map<String, GovernanceModel> governances = new HashMap<String, GovernanceModel>();
		for (GovernanceModel gov : office.getGovernances()) {
			String governanceName = gov.getGovernanceName();
			governances.put(governanceName, gov);
		}

		// Connect the external managed objects to governances
		for (ExternalManagedObjectModel extMo : office.getExternalManagedObjects()) {
			for (ExternalManagedObjectToGovernanceModel conn : extMo.getGovernances()) {
				GovernanceModel gov = governances.get(conn.getGovernanceName());
				if (gov != null) {
					conn.setExternalManagedObject(extMo);
					conn.setGovernance(gov);
					conn.connect();
				}
			}
		}

		// Connect the managed objects to governances
		for (OfficeManagedObjectModel mo : office.getOfficeManagedObjects()) {
			for (OfficeManagedObjectToGovernanceModel conn : mo.getGovernances()) {
				GovernanceModel gov = governances.get(conn.getGovernanceName());
				if (gov != null) {
					conn.setOfficeManagedObject(mo);
					conn.setGovernance(gov);
					conn.connect();
				}
			}
		}

		// Connect the sub sections
		for (OfficeSectionModel section : office.getOfficeSections()) {
			this.connectSubSections(section.getOfficeSubSection(), administrations, governances);
		}

		// Return the office
		return office;
	}

	/**
	 * Connects the {@link OfficeSubSectionModel} instances.
	 * 
	 * @param subSection
	 *            {@link OfficeSubSectionModel}.
	 * @param administrations
	 *            Map of {@link AdministrationModel} instances by
	 *            {@link Administration} name.
	 * @param governances
	 *            Map of {@link GovernanceModel} instances by {@link Governance}
	 *            name.
	 */
	private void connectSubSections(OfficeSubSectionModel subSection, Map<String, AdministrationModel> administrations,
			Map<String, GovernanceModel> governances) {

		// Ensure have the sub section
		if (subSection == null) {
			return;
		}

		// Connect sub section to governances
		for (OfficeSubSectionToGovernanceModel conn : subSection.getGovernances()) {
			GovernanceModel governance = governances.get(conn.getGovernanceName());
			if (governance != null) {
				conn.setOfficeSubSection(subSection);
				conn.setGovernance(governance);
				conn.connect();
			}
		}

		// Connect the sub section functions to pre administration
		for (OfficeFunctionModel function : subSection.getOfficeFunctions()) {
			for (OfficeFunctionToPreAdministrationModel conn : function.getPreAdministrations()) {
				AdministrationModel admin = administrations.get(conn.getAdministrationName());
				if (admin != null) {
					conn.setOfficeFunction(function);
					conn.setAdministration(admin);
					conn.connect();
				}
			}
		}

		// Connect the sub section functions to post administration
		for (OfficeFunctionModel function : subSection.getOfficeFunctions()) {
			for (OfficeFunctionToPostAdministrationModel conn : function.getPostAdministrations()) {
				AdministrationModel admin = administrations.get(conn.getAdministrationName());
				if (admin != null) {
					conn.setOfficeFunction(function);
					conn.setAdministration(admin);
					conn.connect();
				}
			}
		}

		// Connect the sub section functions to governance
		for (OfficeFunctionModel function : subSection.getOfficeFunctions()) {
			for (OfficeFunctionToGovernanceModel conn : function.getGovernances()) {
				GovernanceModel governance = governances.get(conn.getGovernanceName());
				if (governance != null) {
					conn.setOfficeFunction(function);
					conn.setGovernance(governance);
					conn.connect();
				}
			}
		}

		// Connect the section managed objects to governance
		for (OfficeSectionManagedObjectModel mo : subSection.getOfficeSectionManagedObjects()) {
			for (OfficeSectionManagedObjectToGovernanceModel conn : mo.getGovernances()) {
				GovernanceModel governance = governances.get(conn.getGovernanceName());
				if (governance != null) {
					conn.setOfficeSectionManagedObject(mo);
					conn.setGovernance(governance);
					conn.connect();
				}
			}
		}

		// Connect task to duties for further sub sections
		for (OfficeSubSectionModel subSubSection : subSection.getOfficeSubSections()) {
			this.connectSubSections(subSubSection, administrations, governances);
		}
	}

	@Override
	public void storeOffice(OfficeModel office, ConfigurationItem configuration) throws Exception {

		// Specify responsibility to team
		for (OfficeTeamModel team : office.getOfficeTeams()) {
			for (OfficeSectionResponsibilityToOfficeTeamModel conn : team.getOfficeSectionResponsibilities()) {
				conn.setOfficeTeamName(team.getOfficeTeamName());
			}
		}

		// Specify managed object source teams to office teams
		for (OfficeTeamModel team : office.getOfficeTeams()) {
			for (OfficeManagedObjectSourceTeamToOfficeTeamModel conn : team.getOfficeManagedObjectSourceTeams()) {
				conn.setOfficeTeamName(team.getOfficeTeamName());
			}
		}

		// Specify administrations to team
		for (OfficeTeamModel team : office.getOfficeTeams()) {
			for (AdministrationToOfficeTeamModel conn : team.getAdministrations()) {
				conn.setOfficeTeamName(team.getOfficeTeamName());
			}
		}

		// Specify governances to team
		for (OfficeTeamModel team : office.getOfficeTeams()) {
			for (GovernanceToOfficeTeamModel conn : team.getGovernances()) {
				conn.setOfficeTeamName(team.getOfficeTeamName());
			}
		}

		// Specify starts to inputs
		for (OfficeSectionModel section : office.getOfficeSections()) {
			for (OfficeSectionInputModel input : section.getOfficeSectionInputs()) {
				for (OfficeStartToOfficeSectionInputModel conn : input.getOfficeStarts()) {
					conn.setOfficeSectionName(section.getOfficeSectionName());
					conn.setOfficeSectionInputName(input.getOfficeSectionInputName());
				}
			}
		}

		// Specify outputs to inputs
		for (OfficeSectionModel section : office.getOfficeSections()) {
			for (OfficeSectionInputModel input : section.getOfficeSectionInputs()) {
				for (OfficeSectionOutputToOfficeSectionInputModel conn : input.getOfficeSectionOutputs()) {
					conn.setOfficeSectionName(section.getOfficeSectionName());
					conn.setOfficeSectionInputName(input.getOfficeSectionInputName());
				}
			}
		}

		// Specify managed object source flows to inputs
		for (OfficeSectionModel section : office.getOfficeSections()) {
			for (OfficeSectionInputModel input : section.getOfficeSectionInputs()) {
				for (OfficeManagedObjectSourceFlowToOfficeSectionInputModel conn : input
						.getOfficeManagedObjectSourceFlows()) {
					conn.setOfficeSectionName(section.getOfficeSectionName());
					conn.setOfficeSectionInputName(input.getOfficeSectionInputName());
				}
			}
		}

		// Specify escalation to inputs
		for (OfficeSectionModel section : office.getOfficeSections()) {
			for (OfficeSectionInputModel input : section.getOfficeSectionInputs()) {
				for (OfficeEscalationToOfficeSectionInputModel conn : input.getOfficeEscalations()) {
					conn.setOfficeSectionName(section.getOfficeSectionName());
					conn.setOfficeSectionInputName(input.getOfficeSectionInputName());
				}
			}
		}

		// Specify objects to external managed objects
		for (ExternalManagedObjectModel extMo : office.getExternalManagedObjects()) {
			for (OfficeSectionObjectToExternalManagedObjectModel conn : extMo.getOfficeSectionObjects()) {
				conn.setExternalManagedObjectName(extMo.getExternalManagedObjectName());
			}
		}

		// Specify objects to office managed objects
		for (OfficeManagedObjectModel mo : office.getOfficeManagedObjects()) {
			for (OfficeSectionObjectToOfficeManagedObjectModel conn : mo.getOfficeSectionObjects()) {
				conn.setOfficeManagedObjectName(mo.getOfficeManagedObjectName());
			}
		}

		// Specify managed objects to their corresponding sources
		for (OfficeManagedObjectSourceModel mos : office.getOfficeManagedObjectSources()) {
			for (OfficeManagedObjectToOfficeManagedObjectSourceModel conn : mos.getOfficeManagedObjects()) {
				conn.setOfficeManagedObjectSourceName(mos.getOfficeManagedObjectSourceName());
			}
		}

		// Specify external managed objects to dependencies
		for (ExternalManagedObjectModel extMo : office.getExternalManagedObjects()) {
			for (OfficeManagedObjectDependencyToExternalManagedObjectModel conn : extMo
					.getDependentOfficeManagedObjects()) {
				conn.setExternalManagedObjectName(extMo.getExternalManagedObjectName());
			}
		}

		// Specify managed objects to dependencies
		for (OfficeManagedObjectModel mo : office.getOfficeManagedObjects()) {
			for (OfficeManagedObjectDependencyToOfficeManagedObjectModel conn : mo.getDependentOfficeManagedObjects()) {
				conn.setOfficeManagedObjectName(mo.getOfficeManagedObjectName());
			}
		}

		// Specify external managed objects to input dependencies
		for (ExternalManagedObjectModel extMo : office.getExternalManagedObjects()) {
			for (OfficeInputManagedObjectDependencyToExternalManagedObjectModel conn : extMo
					.getDependentOfficeInputManagedObjects()) {
				conn.setExternalManagedObjectName(extMo.getExternalManagedObjectName());
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
		for (GovernanceModel governance : office.getGovernances()) {
			for (OfficeSubSectionToGovernanceModel conn : governance.getOfficeSubSections()) {
				conn.setGovernanceName(governance.getGovernanceName());
			}
		}

		// Specify pre administration to office functions
		for (AdministrationModel admin : office.getAdministrations()) {
			for (OfficeFunctionToPreAdministrationModel conn : admin.getPreOfficeFunctions()) {
				conn.setAdministrationName(admin.getAdministrationName());
			}
		}

		// Specify post administration to office functions
		for (AdministrationModel admin : office.getAdministrations()) {
			for (OfficeFunctionToPostAdministrationModel conn : admin.getPostOfficeFunctions()) {
				conn.setAdministrationName(admin.getAdministrationName());
			}
		}

		// Specify governances to office functions
		for (GovernanceModel governance : office.getGovernances()) {
			for (OfficeFunctionToGovernanceModel conn : governance.getOfficeFunctions()) {
				conn.setGovernanceName(governance.getGovernanceName());
			}
		}

		// Specify governance to section managed objects
		for (GovernanceModel governance : office.getGovernances()) {
			for (OfficeSectionManagedObjectToGovernanceModel conn : governance.getOfficeSectionManagedObjects()) {
				conn.setGovernanceName(governance.getGovernanceName());
			}
		}

		// Specify external managed objects to administrations
		for (AdministrationModel admin : office.getAdministrations()) {
			for (ExternalManagedObjectToAdministrationModel conn : admin.getExternalManagedObjects()) {
				conn.setAdministrationName(admin.getAdministrationName());
			}
		}

		// Specify managed objects to administrators
		for (AdministrationModel admin : office.getAdministrations()) {
			for (OfficeManagedObjectToAdministrationModel conn : admin.getOfficeManagedObjects()) {
				conn.setAdministrationName(admin.getAdministrationName());
			}
		}

		// Specify external managed objects to governances
		for (GovernanceModel gov : office.getGovernances()) {
			for (ExternalManagedObjectToGovernanceModel conn : gov.getExternalManagedObjects()) {
				conn.setGovernanceName(gov.getGovernanceName());
			}
		}

		// Specify managed objects to governances
		for (GovernanceModel gov : office.getGovernances()) {
			for (OfficeManagedObjectToGovernanceModel conn : gov.getOfficeManagedObjects()) {
				conn.setGovernanceName(gov.getGovernanceName());
			}
		}

		// Store the office into the configuration
		this.modelRepository.store(office, configuration);
	}

}