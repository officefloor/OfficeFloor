/*-
 * #%L
 * OfficeCompiler
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

package net.officefloor.model.impl.office;

import java.util.HashMap;
import java.util.Map;

import net.officefloor.compile.impl.util.CompileUtil;
import net.officefloor.compile.impl.util.DoubleKeyMap;
import net.officefloor.configuration.ConfigurationItem;
import net.officefloor.configuration.WritableConfigurationItem;
import net.officefloor.frame.api.administration.Administration;
import net.officefloor.frame.api.governance.Governance;
import net.officefloor.frame.api.team.Team;
import net.officefloor.model.office.AdministrationEscalationModel;
import net.officefloor.model.office.AdministrationEscalationToOfficeSectionInputModel;
import net.officefloor.model.office.AdministrationFlowModel;
import net.officefloor.model.office.AdministrationFlowToOfficeSectionInputModel;
import net.officefloor.model.office.AdministrationModel;
import net.officefloor.model.office.AdministrationToExternalManagedObjectModel;
import net.officefloor.model.office.AdministrationToOfficeManagedObjectModel;
import net.officefloor.model.office.AdministrationToOfficeSectionManagedObjectModel;
import net.officefloor.model.office.AdministrationToOfficeTeamModel;
import net.officefloor.model.office.ExternalManagedObjectModel;
import net.officefloor.model.office.ExternalManagedObjectToPreLoadAdministrationModel;
import net.officefloor.model.office.GovernanceEscalationModel;
import net.officefloor.model.office.GovernanceEscalationToOfficeSectionInputModel;
import net.officefloor.model.office.GovernanceFlowModel;
import net.officefloor.model.office.GovernanceFlowToOfficeSectionInputModel;
import net.officefloor.model.office.GovernanceModel;
import net.officefloor.model.office.GovernanceToExternalManagedObjectModel;
import net.officefloor.model.office.GovernanceToOfficeManagedObjectModel;
import net.officefloor.model.office.GovernanceToOfficeSectionManagedObjectModel;
import net.officefloor.model.office.GovernanceToOfficeTeamModel;
import net.officefloor.model.office.OfficeEscalationModel;
import net.officefloor.model.office.OfficeEscalationToOfficeSectionInputModel;
import net.officefloor.model.office.OfficeFunctionModel;
import net.officefloor.model.office.OfficeFunctionToGovernanceModel;
import net.officefloor.model.office.OfficeFunctionToOfficeTeamModel;
import net.officefloor.model.office.OfficeFunctionToPostAdministrationModel;
import net.officefloor.model.office.OfficeFunctionToPreAdministrationModel;
import net.officefloor.model.office.OfficeInputManagedObjectDependencyModel;
import net.officefloor.model.office.OfficeInputManagedObjectDependencyToExternalManagedObjectModel;
import net.officefloor.model.office.OfficeInputManagedObjectDependencyToOfficeManagedObjectModel;
import net.officefloor.model.office.OfficeManagedObjectDependencyModel;
import net.officefloor.model.office.OfficeManagedObjectDependencyToExternalManagedObjectModel;
import net.officefloor.model.office.OfficeManagedObjectDependencyToOfficeManagedObjectModel;
import net.officefloor.model.office.OfficeManagedObjectFunctionDependencyModel;
import net.officefloor.model.office.OfficeManagedObjectFunctionDependencyToExternalManagedObjectModel;
import net.officefloor.model.office.OfficeManagedObjectFunctionDependencyToOfficeManagedObjectModel;
import net.officefloor.model.office.OfficeManagedObjectModel;
import net.officefloor.model.office.OfficeManagedObjectPoolModel;
import net.officefloor.model.office.OfficeManagedObjectSourceFlowModel;
import net.officefloor.model.office.OfficeManagedObjectSourceFlowToOfficeSectionInputModel;
import net.officefloor.model.office.OfficeManagedObjectSourceModel;
import net.officefloor.model.office.OfficeManagedObjectSourceStartAfterOfficeManagedObjectSourceModel;
import net.officefloor.model.office.OfficeManagedObjectSourceStartBeforeOfficeManagedObjectSourceModel;
import net.officefloor.model.office.OfficeManagedObjectSourceTeamModel;
import net.officefloor.model.office.OfficeManagedObjectSourceTeamToOfficeTeamModel;
import net.officefloor.model.office.OfficeManagedObjectSourceToOfficeManagedObjectPoolModel;
import net.officefloor.model.office.OfficeManagedObjectSourceToOfficeSupplierModel;
import net.officefloor.model.office.OfficeManagedObjectToOfficeManagedObjectSourceModel;
import net.officefloor.model.office.OfficeManagedObjectToPreLoadAdministrationModel;
import net.officefloor.model.office.OfficeModel;
import net.officefloor.model.office.OfficeRepository;
import net.officefloor.model.office.OfficeSectionInputModel;
import net.officefloor.model.office.OfficeSectionManagedObjectModel;
import net.officefloor.model.office.OfficeSectionManagedObjectToPreLoadAdministrationModel;
import net.officefloor.model.office.OfficeSectionModel;
import net.officefloor.model.office.OfficeSectionObjectModel;
import net.officefloor.model.office.OfficeSectionObjectToExternalManagedObjectModel;
import net.officefloor.model.office.OfficeSectionObjectToOfficeManagedObjectModel;
import net.officefloor.model.office.OfficeSectionOutputModel;
import net.officefloor.model.office.OfficeSectionOutputToOfficeSectionInputModel;
import net.officefloor.model.office.OfficeStartModel;
import net.officefloor.model.office.OfficeStartToOfficeSectionInputModel;
import net.officefloor.model.office.OfficeSubSectionModel;
import net.officefloor.model.office.OfficeSubSectionToGovernanceModel;
import net.officefloor.model.office.OfficeSupplierModel;
import net.officefloor.model.office.OfficeSupplierThreadLocalModel;
import net.officefloor.model.office.OfficeSupplierThreadLocalToExternalManagedObjectModel;
import net.officefloor.model.office.OfficeSupplierThreadLocalToOfficeManagedObjectModel;
import net.officefloor.model.office.OfficeTeamModel;
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
	 * @param modelRepository {@link ModelRepository}.
	 */
	public OfficeRepositoryImpl(ModelRepository modelRepository) {
		this.modelRepository = modelRepository;
	}

	/*
	 * ===================== OfficeRepository ============================
	 */

	@Override
	public void retrieveOffice(OfficeModel office, ConfigurationItem configuration) throws Exception {

		// Load the office from the configuration
		this.modelRepository.retrieve(office, configuration);

		// Create the set of managed object sources
		Map<String, OfficeManagedObjectSourceModel> managedObjectSources = new HashMap<String, OfficeManagedObjectSourceModel>();
		for (OfficeManagedObjectSourceModel managedObjectSource : office.getOfficeManagedObjectSources()) {
			managedObjectSources.put(managedObjectSource.getOfficeManagedObjectSourceName(), managedObjectSource);
		}

		// Create the set of office managed objects
		Map<String, OfficeManagedObjectModel> managedObjects = new HashMap<String, OfficeManagedObjectModel>();
		for (OfficeManagedObjectModel managedObject : office.getOfficeManagedObjects()) {
			managedObjects.put(managedObject.getOfficeManagedObjectName(), managedObject);
		}

		// Create the set of office managed object pools
		Map<String, OfficeManagedObjectPoolModel> managedObjectPools = new HashMap<>();
		for (OfficeManagedObjectPoolModel managedObjectPool : office.getOfficeManagedObjectPools()) {
			managedObjectPools.put(managedObjectPool.getOfficeManagedObjectPoolName(), managedObjectPool);
		}

		// Create the set of office suppliers
		Map<String, OfficeSupplierModel> suppliers = new HashMap<>();
		for (OfficeSupplierModel supplier : office.getOfficeSuppliers()) {
			suppliers.put(supplier.getOfficeSupplierName(), supplier);
		}

		// Create the set of external managed objects
		Map<String, ExternalManagedObjectModel> extMos = new HashMap<String, ExternalManagedObjectModel>();
		for (ExternalManagedObjectModel extMo : office.getExternalManagedObjects()) {
			extMos.put(extMo.getExternalManagedObjectName(), extMo);
		}

		// Create the set of office section inputs
		DoubleKeyMap<String, String, OfficeSectionInputModel> inputs = new DoubleKeyMap<String, String, OfficeSectionInputModel>();
		for (OfficeSectionModel section : office.getOfficeSections()) {
			for (OfficeSectionInputModel input : section.getOfficeSectionInputs()) {
				inputs.put(section.getOfficeSectionName(), input.getOfficeSectionInputName(), input);
			}
		}

		// Create the map of administrations
		Map<String, AdministrationModel> administrations = new HashMap<String, AdministrationModel>();
		for (AdministrationModel admin : office.getAdministrations()) {
			String administrationName = admin.getAdministrationName();
			administrations.put(administrationName, admin);
		}

		// Create the map of governances
		Map<String, GovernanceModel> governances = new HashMap<String, GovernanceModel>();
		for (GovernanceModel gov : office.getGovernances()) {
			String governanceName = gov.getGovernanceName();
			governances.put(governanceName, gov);
		}

		// Create the set of office teams
		Map<String, OfficeTeamModel> teams = new HashMap<String, OfficeTeamModel>();
		for (OfficeTeamModel team : office.getOfficeTeams()) {
			teams.put(team.getOfficeTeamName(), team);
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

		// Connect the managed object sources to their corresponding pools
		for (OfficeManagedObjectSourceModel managedObjectSource : office.getOfficeManagedObjectSources()) {
			OfficeManagedObjectSourceToOfficeManagedObjectPoolModel conn = managedObjectSource
					.getOfficeManagedObjectPool();
			if (conn != null) {
				OfficeManagedObjectPoolModel managedObjectPool = managedObjectPools
						.get(conn.getOfficeManagedObjectPoolName());
				if (managedObjectPool != null) {
					conn.setOfficeManagedObjectSource(managedObjectSource);
					conn.setOfficeManagedObjectPool(managedObjectPool);
					conn.connect();
				}
			}
		}

		// Connection the managed object source to start before
		for (OfficeManagedObjectSourceModel managedObjectSource : office.getOfficeManagedObjectSources()) {
			for (OfficeManagedObjectSourceStartBeforeOfficeManagedObjectSourceModel conn : managedObjectSource
					.getStartBeforeEarliers()) {

				// Obtain the name
				String startBeforeName = conn.getOfficeManagedObjectSourceName();
				if (CompileUtil.isBlank(startBeforeName)) {
					// Start before type
					conn.setStartEarlier(managedObjectSource);

				} else {
					// Undertake connection
					OfficeManagedObjectSourceModel startLater = managedObjectSources.get(startBeforeName);
					if (startLater != null) {
						conn.setStartEarlier(managedObjectSource);
						conn.setStartLater(startLater);
						conn.connect();
					}
				}
			}
		}

		// Connection the managed object source to start after
		for (OfficeManagedObjectSourceModel managedObjectSource : office.getOfficeManagedObjectSources()) {
			for (OfficeManagedObjectSourceStartAfterOfficeManagedObjectSourceModel conn : managedObjectSource
					.getStartAfterLaters()) {

				// Obtain the name
				String startAfterName = conn.getOfficeManagedObjectSourceName();
				if (CompileUtil.isBlank(startAfterName)) {
					// Start after type
					conn.setStartLater(managedObjectSource);

				} else {
					// Undertake connection
					OfficeManagedObjectSourceModel startEarlier = managedObjectSources.get(startAfterName);
					if (startEarlier != null) {
						conn.setStartLater(managedObjectSource);
						conn.setStartEarlier(startEarlier);
						conn.connect();
					}
				}
			}
		}

		// Connect the managed object sources to their corresponding suppliers
		for (OfficeManagedObjectSourceModel managedObjectSource : office.getOfficeManagedObjectSources()) {
			OfficeManagedObjectSourceToOfficeSupplierModel conn = managedObjectSource.getOfficeSupplier();
			if (conn != null) {
				OfficeSupplierModel supplier = suppliers.get(conn.getOfficeSupplierName());
				if (supplier != null) {
					conn.setOfficeManagedObjectSource(managedObjectSource);
					conn.setOfficeSupplier(supplier);
					conn.connect();
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

		// Connect supplier thread locals to external managed objects
		for (OfficeSupplierModel supplier : office.getOfficeSuppliers()) {
			for (OfficeSupplierThreadLocalModel threadLocal : supplier.getOfficeSupplierThreadLocals()) {
				OfficeSupplierThreadLocalToExternalManagedObjectModel conn = threadLocal.getExternalManagedObject();
				if (conn != null) {
					ExternalManagedObjectModel extMo = extMos.get(conn.getExternalManagedObjectName());
					if (extMo != null) {
						conn.setOfficeSupplierThreadLocal(threadLocal);
						conn.setExternalManagedObject(extMo);
						conn.connect();
					}
				}
			}
		}

		// Connect supplier thread locals to managed objects
		for (OfficeSupplierModel supplier : office.getOfficeSuppliers()) {
			for (OfficeSupplierThreadLocalModel threadLocal : supplier.getOfficeSupplierThreadLocals()) {
				OfficeSupplierThreadLocalToOfficeManagedObjectModel conn = threadLocal.getOfficeManagedObject();
				if (conn != null) {
					OfficeManagedObjectModel mo = managedObjects.get(conn.getOfficeManagedObjectName());
					if (mo != null) {
						conn.setOfficeSupplierThreadLocal(threadLocal);
						conn.setOfficeManagedObject(mo);
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

		// Connect function managed object dependencies to external managed object
		for (OfficeManagedObjectSourceModel mos : office.getOfficeManagedObjectSources()) {
			for (OfficeManagedObjectFunctionDependencyModel dependency : mos
					.getOfficeManagedObjectFunctionDependencies()) {
				OfficeManagedObjectFunctionDependencyToExternalManagedObjectModel conn = dependency
						.getExternalManagedObject();
				if (conn != null) {
					ExternalManagedObjectModel extMo = extMos.get(conn.getExternalManagedObjectName());
					if (extMo != null) {
						conn.setOfficeManagedObjectFunctionDependency(dependency);
						conn.setExternalManagedObject(extMo);
						conn.connect();
					}
				}
			}
		}

		// Connect function managed object dependencies to managed object
		for (OfficeManagedObjectSourceModel mos : office.getOfficeManagedObjectSources()) {
			for (OfficeManagedObjectFunctionDependencyModel dependency : mos
					.getOfficeManagedObjectFunctionDependencies()) {
				OfficeManagedObjectFunctionDependencyToOfficeManagedObjectModel conn = dependency
						.getOfficeManagedObject();
				if (conn != null) {
					OfficeManagedObjectModel mo = managedObjects.get(conn.getOfficeManagedObjectName());
					if (mo != null) {
						conn.setOfficeManagedObjectFunctionDependency(dependency);
						conn.setOfficeManagedObject(mo);
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

		// Connect the section objects to the external managed objects
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

		// Connect the section objects to the office managed objects
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

		// Connect administration flow to section input
		for (AdministrationModel admin : office.getAdministrations()) {
			for (AdministrationFlowModel adminFlow : admin.getAdministrationFlows()) {
				AdministrationFlowToOfficeSectionInputModel conn = adminFlow.getOfficeSectionInput();
				if (conn != null) {
					OfficeSectionInputModel input = inputs.get(conn.getOfficeSectionName(),
							conn.getOfficeSectionInputName());
					if (input != null) {
						conn.setAdministrationFlow(adminFlow);
						conn.setOfficeSectionInput(input);
						conn.connect();
					}
				}
			}
		}

		// Connect administration escalation to section input
		for (AdministrationModel admin : office.getAdministrations()) {
			for (AdministrationEscalationModel adminEscalation : admin.getAdministrationEscalations()) {
				AdministrationEscalationToOfficeSectionInputModel conn = adminEscalation.getOfficeSectionInput();
				if (conn != null) {
					OfficeSectionInputModel input = inputs.get(conn.getOfficeSectionName(),
							conn.getOfficeSectionInputName());
					if (input != null) {
						conn.setAdministrationEscalation(adminEscalation);
						conn.setOfficeSectionInput(input);
						conn.connect();
					}
				}
			}
		}

		// Connect the external managed objects to administrations
		for (ExternalManagedObjectModel extMo : office.getExternalManagedObjects()) {
			for (AdministrationToExternalManagedObjectModel conn : extMo.getAdministrations()) {
				AdministrationModel admin = administrations.get(conn.getAdministrationName());
				if (admin != null) {
					conn.setExternalManagedObject(extMo);
					conn.setAdministration(admin);
					conn.connect();
				}
			}
		}

		// Connect pre-load administration for external managed objects
		for (ExternalManagedObjectModel extMo : office.getExternalManagedObjects()) {
			for (ExternalManagedObjectToPreLoadAdministrationModel conn : extMo.getPreLoadAdministrations()) {
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
			for (AdministrationToOfficeManagedObjectModel conn : mo.getAdministrations()) {
				AdministrationModel admin = administrations.get(conn.getAdministrationName());
				if (admin != null) {
					conn.setOfficeManagedObject(mo);
					conn.setAdministration(admin);
					conn.connect();
				}
			}
		}

		// Connect pre-load administration for external managed objects
		for (OfficeManagedObjectModel mo : office.getOfficeManagedObjects()) {
			for (OfficeManagedObjectToPreLoadAdministrationModel conn : mo.getPreLoadAdministrations()) {
				AdministrationModel admin = administrations.get(conn.getAdministrationName());
				if (admin != null) {
					conn.setOfficeManagedObject(mo);
					conn.setAdministration(admin);
					conn.connect();
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

		// Connect governance flow to section input
		for (GovernanceModel governance : office.getGovernances()) {
			for (GovernanceFlowModel govFlow : governance.getGovernanceFlows()) {
				GovernanceFlowToOfficeSectionInputModel conn = govFlow.getOfficeSectionInput();
				if (conn != null) {
					OfficeSectionInputModel input = inputs.get(conn.getOfficeSectionName(),
							conn.getOfficeSectionInputName());
					if (input != null) {
						conn.setGovernanceFlow(govFlow);
						conn.setOfficeSectionInput(input);
						conn.connect();
					}
				}
			}
		}

		// Connect governance escalation to section input
		for (GovernanceModel governance : office.getGovernances()) {
			for (GovernanceEscalationModel govEscalation : governance.getGovernanceEscalations()) {
				GovernanceEscalationToOfficeSectionInputModel conn = govEscalation.getOfficeSectionInput();
				if (conn != null) {
					OfficeSectionInputModel input = inputs.get(conn.getOfficeSectionName(),
							conn.getOfficeSectionInputName());
					if (input != null) {
						conn.setGovernanceEscalation(govEscalation);
						conn.setOfficeSectionInput(input);
						conn.connect();
					}
				}
			}
		}

		// Connect the external managed objects to governances
		for (ExternalManagedObjectModel extMo : office.getExternalManagedObjects()) {
			for (GovernanceToExternalManagedObjectModel conn : extMo.getGovernances()) {
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
			for (GovernanceToOfficeManagedObjectModel conn : mo.getGovernances()) {
				GovernanceModel gov = governances.get(conn.getGovernanceName());
				if (gov != null) {
					conn.setOfficeManagedObject(mo);
					conn.setGovernance(gov);
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

		// Connect the sub sections
		for (OfficeSectionModel section : office.getOfficeSections()) {
			this.connectSubSections(section.getOfficeSubSection(), teams, administrations, governances);
		}
	}

	/**
	 * Connects the {@link OfficeSubSectionModel} instances.
	 * 
	 * @param subSection      {@link OfficeSubSectionModel}.
	 * @param teams           Map of {@link OfficeTeamModel} instances by
	 *                        {@link Team} name.
	 * @param administrations Map of {@link AdministrationModel} instances by
	 *                        {@link Administration} name.
	 * @param governances     Map of {@link GovernanceModel} instances by
	 *                        {@link Governance} name.
	 */
	private void connectSubSections(OfficeSubSectionModel subSection, Map<String, OfficeTeamModel> teams,
			Map<String, AdministrationModel> administrations, Map<String, GovernanceModel> governances) {

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

		// Connect the sub section responsibilities to the teams
		for (OfficeFunctionModel function : subSection.getOfficeFunctions()) {
			OfficeFunctionToOfficeTeamModel conn = function.getOfficeTeam();
			if (conn != null) {
				OfficeTeamModel team = teams.get(conn.getOfficeTeamName());
				if (team != null) {
					conn.setOfficeFunction(function);
					conn.setOfficeTeam(team);
					conn.connect();
				}
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

		// Connection administration of sub section managed objects
		for (OfficeSectionManagedObjectModel mo : subSection.getOfficeSectionManagedObjects()) {
			for (AdministrationToOfficeSectionManagedObjectModel conn : mo.getAdministrations()) {
				AdministrationModel admin = administrations.get(conn.getAdministrationName());
				if (admin != null) {
					conn.setOfficeSectionManagedObject(mo);
					conn.setAdministration(admin);
					conn.connect();
				}
			}
		}

		// Connect pre-load administration for sub section managed objects
		for (OfficeSectionManagedObjectModel mo : subSection.getOfficeSectionManagedObjects()) {
			for (OfficeSectionManagedObjectToPreLoadAdministrationModel conn : mo.getPreLoadAdministrations()) {
				AdministrationModel admin = administrations.get(conn.getAdministrationName());
				if (admin != null) {
					conn.setOfficeSectionManagedObject(mo);
					conn.setAdministration(admin);
					conn.connect();
				}
			}
		}

		// Connect the section managed objects to governance
		for (OfficeSectionManagedObjectModel mo : subSection.getOfficeSectionManagedObjects()) {
			for (GovernanceToOfficeSectionManagedObjectModel conn : mo.getGovernances()) {
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
			this.connectSubSections(subSubSection, teams, administrations, governances);
		}
	}

	@Override
	public void storeOffice(OfficeModel office, WritableConfigurationItem configuration) throws Exception {

		// Specify managed objects to their corresponding sources
		for (OfficeManagedObjectSourceModel mos : office.getOfficeManagedObjectSources()) {
			for (OfficeManagedObjectToOfficeManagedObjectSourceModel conn : mos.getOfficeManagedObjects()) {
				conn.setOfficeManagedObjectSourceName(mos.getOfficeManagedObjectSourceName());
			}
		}

		// Specify managed object sources to their corresponding pools
		for (OfficeManagedObjectPoolModel pool : office.getOfficeManagedObjectPools()) {
			for (OfficeManagedObjectSourceToOfficeManagedObjectPoolModel conn : pool.getOfficeManagedObjectSources()) {
				conn.setOfficeManagedObjectPoolName(pool.getOfficeManagedObjectPoolName());
			}
		}

		// Specify start befores for the managed object sources
		for (OfficeManagedObjectSourceModel moSource : office.getOfficeManagedObjectSources()) {
			for (OfficeManagedObjectSourceStartBeforeOfficeManagedObjectSourceModel conn : moSource
					.getStartBeforeLaters()) {
				conn.setOfficeManagedObjectSourceName(moSource.getOfficeManagedObjectSourceName());
			}
		}

		// Specify start afters for the managed object sources
		for (OfficeManagedObjectSourceModel moSource : office.getOfficeManagedObjectSources()) {
			for (OfficeManagedObjectSourceStartAfterOfficeManagedObjectSourceModel conn : moSource
					.getStartAfterEarliers()) {
				conn.setOfficeManagedObjectSourceName(moSource.getOfficeManagedObjectSourceName());
			}
		}

		// Specify managed object sources to their corresponding suppliers
		for (OfficeSupplierModel supplier : office.getOfficeSuppliers()) {
			for (OfficeManagedObjectSourceToOfficeSupplierModel conn : supplier.getOfficeManagedObjectSources()) {
				conn.setOfficeSupplierName(supplier.getOfficeSupplierName());
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

		// Specify external managed objects to supplier thread locals
		for (ExternalManagedObjectModel extMo : office.getExternalManagedObjects()) {
			for (OfficeSupplierThreadLocalToExternalManagedObjectModel conn : extMo
					.getDependentOfficeSupplierThreadLocals()) {
				conn.setExternalManagedObjectName(extMo.getExternalManagedObjectName());
			}
		}

		// Specify managed objects to supplier thread locals
		for (OfficeManagedObjectModel mo : office.getOfficeManagedObjects()) {
			for (OfficeSupplierThreadLocalToOfficeManagedObjectModel conn : mo
					.getDependentOfficeSupplierThreadLocals()) {
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

		// Specify external managed objects to function dependencies
		for (ExternalManagedObjectModel extMo : office.getExternalManagedObjects()) {
			for (OfficeManagedObjectFunctionDependencyToExternalManagedObjectModel conn : extMo
					.getDependentOfficeManagedObjectFunctionObjects()) {
				conn.setExternalManagedObjectName(extMo.getExternalManagedObjectName());
			}
		}

		// Specify external managed objects to pre-load administration
		for (AdministrationModel admin : office.getAdministrations()) {
			for (ExternalManagedObjectToPreLoadAdministrationModel conn : admin.getPreLoadExternalManagedObjects()) {
				conn.setAdministrationName(admin.getAdministrationName());
			}
		}

		// Specify managed objects to input dependencies
		for (OfficeManagedObjectModel mo : office.getOfficeManagedObjects()) {
			for (OfficeInputManagedObjectDependencyToOfficeManagedObjectModel conn : mo
					.getDependentOfficeInputManagedObjects()) {
				conn.setOfficeManagedObjectName(mo.getOfficeManagedObjectName());
			}
		}

		// Specify managed objects to function dependencies
		for (OfficeManagedObjectModel mo : office.getOfficeManagedObjects()) {
			for (OfficeManagedObjectFunctionDependencyToOfficeManagedObjectModel conn : mo
					.getDependentOfficeManagedObjectFunctionObjects()) {
				conn.setOfficeManagedObjectName(mo.getOfficeManagedObjectName());
			}
		}

		// Specify managed objects to pre-load administration
		for (AdministrationModel admin : office.getAdministrations()) {
			for (OfficeManagedObjectToPreLoadAdministrationModel conn : admin.getPreLoadOfficeManagedObjects()) {
				conn.setAdministrationName(admin.getAdministrationName());
			}
		}

		// Specify managed object source teams to office teams
		for (OfficeTeamModel team : office.getOfficeTeams()) {
			for (OfficeManagedObjectSourceTeamToOfficeTeamModel conn : team.getOfficeManagedObjectSourceTeams()) {
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

		// Specify escalation to inputs
		for (OfficeSectionModel section : office.getOfficeSections()) {
			for (OfficeSectionInputModel input : section.getOfficeSectionInputs()) {
				for (OfficeEscalationToOfficeSectionInputModel conn : input.getOfficeEscalations()) {
					conn.setOfficeSectionName(section.getOfficeSectionName());
					conn.setOfficeSectionInputName(input.getOfficeSectionInputName());
				}
			}
		}

		// Specify office teams to office functions
		for (OfficeTeamModel team : office.getOfficeTeams()) {
			for (OfficeFunctionToOfficeTeamModel conn : team.getOfficeFunctions()) {
				conn.setOfficeTeamName(team.getOfficeTeamName());
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

		// Specify administration flows to section inputs
		for (OfficeSectionModel section : office.getOfficeSections()) {
			for (OfficeSectionInputModel input : section.getOfficeSectionInputs()) {
				for (AdministrationFlowToOfficeSectionInputModel conn : input.getAdministrationFlows()) {
					conn.setOfficeSectionName(section.getOfficeSectionName());
					conn.setOfficeSectionInputName(input.getOfficeSectionInputName());
				}
			}
		}

		// Specify administration escalations to section inputs
		for (OfficeSectionModel section : office.getOfficeSections()) {
			for (OfficeSectionInputModel input : section.getOfficeSectionInputs()) {
				for (AdministrationEscalationToOfficeSectionInputModel conn : input.getAdministrationEscalations()) {
					conn.setOfficeSectionName(section.getOfficeSectionName());
					conn.setOfficeSectionInputName(input.getOfficeSectionInputName());
				}
			}
		}

		// Specify external managed objects to administrations
		for (AdministrationModel admin : office.getAdministrations()) {
			for (AdministrationToExternalManagedObjectModel conn : admin.getAdministeredExternalManagedObjects()) {
				conn.setAdministrationName(admin.getAdministrationName());
			}
		}

		// Specify managed objects to administrators
		for (AdministrationModel admin : office.getAdministrations()) {
			for (AdministrationToOfficeManagedObjectModel conn : admin.getAdministeredOfficeManagedObjects()) {
				conn.setAdministrationName(admin.getAdministrationName());
			}
		}

		// Specify administrations to team
		for (OfficeTeamModel team : office.getOfficeTeams()) {
			for (AdministrationToOfficeTeamModel conn : team.getAdministrations()) {
				conn.setOfficeTeamName(team.getOfficeTeamName());
			}
		}

		// Specify governance flows to section inputs
		for (OfficeSectionModel section : office.getOfficeSections()) {
			for (OfficeSectionInputModel input : section.getOfficeSectionInputs()) {
				for (GovernanceFlowToOfficeSectionInputModel conn : input.getGovernanceFlows()) {
					conn.setOfficeSectionName(section.getOfficeSectionName());
					conn.setOfficeSectionInputName(input.getOfficeSectionInputName());
				}
			}
		}

		// Specify governance escalations to section inputs
		for (OfficeSectionModel section : office.getOfficeSections()) {
			for (OfficeSectionInputModel input : section.getOfficeSectionInputs()) {
				for (GovernanceEscalationToOfficeSectionInputModel conn : input.getGovernanceEscalations()) {
					conn.setOfficeSectionName(section.getOfficeSectionName());
					conn.setOfficeSectionInputName(input.getOfficeSectionInputName());
				}
			}
		}

		// Specify governances to sub sections
		for (GovernanceModel governance : office.getGovernances()) {
			for (OfficeSubSectionToGovernanceModel conn : governance.getOfficeSubSections()) {
				conn.setGovernanceName(governance.getGovernanceName());
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
			for (GovernanceToOfficeSectionManagedObjectModel conn : governance.getOfficeSectionManagedObjects()) {
				conn.setGovernanceName(governance.getGovernanceName());
			}
		}

		// Specify administration of section managed objects
		for (AdministrationModel admin : office.getAdministrations()) {
			for (AdministrationToOfficeSectionManagedObjectModel conn : admin
					.getAdministeredOfficeSectionManagedObjects()) {
				conn.setAdministrationName(admin.getAdministrationName());
			}
		}

		// Specify section managed objects to pre-load administration
		for (AdministrationModel admin : office.getAdministrations()) {
			for (OfficeSectionManagedObjectToPreLoadAdministrationModel conn : admin
					.getPreLoadOfficeSectionManagedObjects()) {
				conn.setAdministrationName(admin.getAdministrationName());
			}
		}

		// Specify external managed objects to governances
		for (GovernanceModel gov : office.getGovernances()) {
			for (GovernanceToExternalManagedObjectModel conn : gov.getExternalManagedObjects()) {
				conn.setGovernanceName(gov.getGovernanceName());
			}
		}

		// Specify managed objects to governances
		for (GovernanceModel gov : office.getGovernances()) {
			for (GovernanceToOfficeManagedObjectModel conn : gov.getOfficeManagedObjects()) {
				conn.setGovernanceName(gov.getGovernanceName());
			}
		}

		// Specify governances to team
		for (OfficeTeamModel team : office.getOfficeTeams()) {
			for (GovernanceToOfficeTeamModel conn : team.getGovernances()) {
				conn.setOfficeTeamName(team.getOfficeTeamName());
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

		// Specify section objects to external managed objects
		for (ExternalManagedObjectModel extMo : office.getExternalManagedObjects()) {
			for (OfficeSectionObjectToExternalManagedObjectModel conn : extMo.getOfficeSectionObjects()) {
				conn.setExternalManagedObjectName(extMo.getExternalManagedObjectName());
			}
		}

		// Specify section objects to office managed objects
		for (OfficeManagedObjectModel mo : office.getOfficeManagedObjects()) {
			for (OfficeSectionObjectToOfficeManagedObjectModel conn : mo.getOfficeSectionObjects()) {
				conn.setOfficeManagedObjectName(mo.getOfficeManagedObjectName());
			}
		}

		// Store the office into the configuration
		this.modelRepository.store(office, configuration);
	}

}
