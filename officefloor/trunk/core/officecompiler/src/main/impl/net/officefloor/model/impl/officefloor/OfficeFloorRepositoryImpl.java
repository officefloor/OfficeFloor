/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2012 Daniel Sagenschneider
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

package net.officefloor.model.impl.officefloor;

import java.util.HashMap;
import java.util.Map;

import net.officefloor.compile.impl.util.TripleKeyMap;
import net.officefloor.model.officefloor.DeployedOfficeInputModel;
import net.officefloor.model.officefloor.DeployedOfficeModel;
import net.officefloor.model.officefloor.DeployedOfficeObjectModel;
import net.officefloor.model.officefloor.DeployedOfficeObjectToOfficeFloorInputManagedObjectModel;
import net.officefloor.model.officefloor.DeployedOfficeObjectToOfficeFloorManagedObjectModel;
import net.officefloor.model.officefloor.DeployedOfficeTeamModel;
import net.officefloor.model.officefloor.DeployedOfficeTeamToOfficeFloorTeamModel;
import net.officefloor.model.officefloor.OfficeFloorInputManagedObjectModel;
import net.officefloor.model.officefloor.OfficeFloorInputManagedObjectToBoundOfficeFloorManagedObjectSourceModel;
import net.officefloor.model.officefloor.OfficeFloorManagedObjectDependencyModel;
import net.officefloor.model.officefloor.OfficeFloorManagedObjectDependencyToOfficeFloorInputManagedObjectModel;
import net.officefloor.model.officefloor.OfficeFloorManagedObjectDependencyToOfficeFloorManagedObjectModel;
import net.officefloor.model.officefloor.OfficeFloorManagedObjectModel;
import net.officefloor.model.officefloor.OfficeFloorManagedObjectSourceFlowModel;
import net.officefloor.model.officefloor.OfficeFloorManagedObjectSourceFlowToDeployedOfficeInputModel;
import net.officefloor.model.officefloor.OfficeFloorManagedObjectSourceInputDependencyModel;
import net.officefloor.model.officefloor.OfficeFloorManagedObjectSourceInputDependencyToOfficeFloorManagedObjectModel;
import net.officefloor.model.officefloor.OfficeFloorManagedObjectSourceModel;
import net.officefloor.model.officefloor.OfficeFloorManagedObjectSourceTeamModel;
import net.officefloor.model.officefloor.OfficeFloorManagedObjectSourceTeamToOfficeFloorTeamModel;
import net.officefloor.model.officefloor.OfficeFloorManagedObjectSourceToDeployedOfficeModel;
import net.officefloor.model.officefloor.OfficeFloorManagedObjectSourceToOfficeFloorInputManagedObjectModel;
import net.officefloor.model.officefloor.OfficeFloorManagedObjectSourceToOfficeFloorSupplierModel;
import net.officefloor.model.officefloor.OfficeFloorManagedObjectToOfficeFloorManagedObjectSourceModel;
import net.officefloor.model.officefloor.OfficeFloorModel;
import net.officefloor.model.officefloor.OfficeFloorRepository;
import net.officefloor.model.officefloor.OfficeFloorSupplierModel;
import net.officefloor.model.officefloor.OfficeFloorTeamModel;
import net.officefloor.model.repository.ConfigurationItem;
import net.officefloor.model.repository.ModelRepository;

/**
 * {@link OfficeFloorRepository} implementation.
 * 
 * @author Daniel Sagenschneider
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

		// Create the set of office floor suppliers
		Map<String, OfficeFloorSupplierModel> suppliers = new HashMap<String, OfficeFloorSupplierModel>();
		for (OfficeFloorSupplierModel supplier : officeFloor
				.getOfficeFloorSuppliers()) {
			suppliers.put(supplier.getOfficeFloorSupplierName(), supplier);
		}

		// Connect the managed object sources to their suppliers
		for (OfficeFloorManagedObjectSourceModel managedObjectSource : officeFloor
				.getOfficeFloorManagedObjectSources()) {
			OfficeFloorManagedObjectSourceToOfficeFloorSupplierModel conn = managedObjectSource
					.getOfficeFloorSupplier();
			if (conn != null) {
				OfficeFloorSupplierModel supplier = suppliers.get(conn
						.getOfficeFloorSupplierName());
				if (supplier != null) {
					conn.setOfficeFloorManagedObjectSource(managedObjectSource);
					conn.setOfficeFloorSupplier(supplier);
					conn.connect();
				}
			}
		}

		// Create the set of office floor managed object sources
		Map<String, OfficeFloorManagedObjectSourceModel> managedObjectSources = new HashMap<String, OfficeFloorManagedObjectSourceModel>();
		for (OfficeFloorManagedObjectSourceModel managedObjectSource : officeFloor
				.getOfficeFloorManagedObjectSources()) {
			managedObjectSources
					.put(managedObjectSource
							.getOfficeFloorManagedObjectSourceName(),
							managedObjectSource);
		}

		// Connect the managed objects to their managed object sources
		for (OfficeFloorManagedObjectModel managedObject : officeFloor
				.getOfficeFloorManagedObjects()) {
			OfficeFloorManagedObjectToOfficeFloorManagedObjectSourceModel conn = managedObject
					.getOfficeFloorManagedObjectSource();
			if (conn != null) {
				OfficeFloorManagedObjectSourceModel moSource = managedObjectSources
						.get(conn.getOfficeFloorManagedObjectSourceName());
				if (moSource != null) {
					conn.setOfficeFloorManagedObject(managedObject);
					conn.setOfficeFloorManagedObjectSource(moSource);
					conn.connect();
				}
			}
		}

		// Create the set of offices
		Map<String, DeployedOfficeModel> offices = new HashMap<String, DeployedOfficeModel>();
		for (DeployedOfficeModel office : officeFloor.getDeployedOffices()) {
			offices.put(office.getDeployedOfficeName(), office);
		}

		// Connect the office floor managed object source to its managing office
		for (OfficeFloorManagedObjectSourceModel moSource : officeFloor
				.getOfficeFloorManagedObjectSources()) {
			OfficeFloorManagedObjectSourceToDeployedOfficeModel conn = moSource
					.getManagingOffice();
			if (conn != null) {
				DeployedOfficeModel office = offices.get(conn
						.getManagingOfficeName());
				if (office != null) {
					conn.setOfficeFloorManagedObjectSource(moSource);
					conn.setManagingOffice(office);
					conn.connect();
				}
			}
		}

		// Create the set of input managed objects
		Map<String, OfficeFloorInputManagedObjectModel> inputManagedObjects = new HashMap<String, OfficeFloorInputManagedObjectModel>();
		for (OfficeFloorInputManagedObjectModel inputManagedObject : officeFloor
				.getOfficeFloorInputManagedObjects()) {
			inputManagedObjects.put(
					inputManagedObject.getOfficeFloorInputManagedObjectName(),
					inputManagedObject);
		}

		// Connect the managed object source to its input managed object
		for (OfficeFloorManagedObjectSourceModel moSource : officeFloor
				.getOfficeFloorManagedObjectSources()) {
			OfficeFloorManagedObjectSourceToOfficeFloorInputManagedObjectModel conn = moSource
					.getOfficeFloorInputManagedObject();
			if (conn != null) {
				OfficeFloorInputManagedObjectModel inputMo = inputManagedObjects
						.get(conn.getOfficeFloorInputManagedObjectName());
				if (inputMo != null) {
					conn.setOfficeFloorManagedObjectSource(moSource);
					conn.setOfficeFloorInputManagedObject(inputMo);
					conn.connect();
				}
			}
		}

		// Connect the input managed object to its bound managed object source
		for (OfficeFloorInputManagedObjectModel inputMo : officeFloor
				.getOfficeFloorInputManagedObjects()) {
			OfficeFloorInputManagedObjectToBoundOfficeFloorManagedObjectSourceModel conn = inputMo
					.getBoundOfficeFloorManagedObjectSource();
			if (conn != null) {
				OfficeFloorManagedObjectSourceModel boundMoSource = managedObjectSources
						.get(conn.getOfficeFloorManagedObjectSourceName());
				if (boundMoSource != null) {
					conn.setBoundOfficeFloorInputManagedObject(inputMo);
					conn.setBoundOfficeFloorManagedObjectSource(boundMoSource);
					conn.connect();
				}
			}
		}

		// Create the set of office inputs
		TripleKeyMap<String, String, String, DeployedOfficeInputModel> officeInputs = new TripleKeyMap<String, String, String, DeployedOfficeInputModel>();
		for (DeployedOfficeModel office : officeFloor.getDeployedOffices()) {
			for (DeployedOfficeInputModel officeInput : office
					.getDeployedOfficeInputs()) {
				officeInputs.put(office.getDeployedOfficeName(),
						officeInput.getSectionName(),
						officeInput.getSectionInputName(), officeInput);
			}
		}

		// Connect the managed object source flows to their office inputs
		for (OfficeFloorManagedObjectSourceModel moSource : officeFloor
				.getOfficeFloorManagedObjectSources()) {
			for (OfficeFloorManagedObjectSourceFlowModel flow : moSource
					.getOfficeFloorManagedObjectSourceFlows()) {
				OfficeFloorManagedObjectSourceFlowToDeployedOfficeInputModel conn = flow
						.getDeployedOfficeInput();
				if (conn != null) {
					DeployedOfficeInputModel officeInput = officeInputs.get(
							conn.getDeployedOfficeName(),
							conn.getSectionName(), conn.getSectionInputName());
					if (officeInput != null) {
						conn.setOfficeFloorManagedObjectSoruceFlow(flow);
						conn.setDeployedOfficeInput(officeInput);
						conn.connect();
					}
				}
			}
		}

		// Create the set of office floor managed objects
		Map<String, OfficeFloorManagedObjectModel> managedObjects = new HashMap<String, OfficeFloorManagedObjectModel>();
		for (OfficeFloorManagedObjectModel managedObject : officeFloor
				.getOfficeFloorManagedObjects()) {
			managedObjects.put(managedObject.getOfficeFloorManagedObjectName(),
					managedObject);
		}

		// Connect the input dependencies to the office floor managed objects
		for (OfficeFloorManagedObjectSourceModel moSource : officeFloor
				.getOfficeFloorManagedObjectSources()) {
			for (OfficeFloorManagedObjectSourceInputDependencyModel inputDependency : moSource
					.getOfficeFloorManagedObjectSourceInputDependencies()) {
				OfficeFloorManagedObjectSourceInputDependencyToOfficeFloorManagedObjectModel conn = inputDependency
						.getOfficeFloorManagedObject();
				if (conn != null) {
					OfficeFloorManagedObjectModel managedObject = managedObjects
							.get(conn.getOfficeFloorManagedObjectName());
					if (managedObject != null) {
						conn.setOfficeFloorManagedObjectDependency(inputDependency);
						conn.setOfficeFloorManagedObject(managedObject);
						conn.connect();
					}
				}
			}
		}

		// Connect the office objects to the office floor managed objects
		for (DeployedOfficeModel office : officeFloor.getDeployedOffices()) {
			for (DeployedOfficeObjectModel officeObject : office
					.getDeployedOfficeObjects()) {
				DeployedOfficeObjectToOfficeFloorManagedObjectModel conn = officeObject
						.getOfficeFloorManagedObject();
				if (conn != null) {
					OfficeFloorManagedObjectModel managedObject = managedObjects
							.get(conn.getOfficeFloorManagedObjectName());
					if (managedObject != null) {
						conn.setDeployedOfficeObject(officeObject);
						conn.setOfficeFloorManagedObject(managedObject);
						conn.connect();
					}
				}
			}
		}

		// Connect the office objects to the office floor input managed objects
		for (DeployedOfficeModel office : officeFloor.getDeployedOffices()) {
			for (DeployedOfficeObjectModel officeObject : office
					.getDeployedOfficeObjects()) {
				DeployedOfficeObjectToOfficeFloorInputManagedObjectModel conn = officeObject
						.getOfficeFloorInputManagedObject();
				if (conn != null) {
					OfficeFloorInputManagedObjectModel inputMo = inputManagedObjects
							.get(conn.getOfficeFloorInputManagedObjectName());
					if (inputMo != null) {
						conn.setDeployedOfficeObject(officeObject);
						conn.setOfficeFloorInputManagedObject(inputMo);
						conn.connect();
					}
				}
			}
		}

		// Connect the dependencies to the office floor managed objects
		for (OfficeFloorManagedObjectModel managedObject : officeFloor
				.getOfficeFloorManagedObjects()) {
			for (OfficeFloorManagedObjectDependencyModel dependency : managedObject
					.getOfficeFloorManagedObjectDependencies()) {
				OfficeFloorManagedObjectDependencyToOfficeFloorManagedObjectModel conn = dependency
						.getOfficeFloorManagedObject();
				if (conn != null) {
					OfficeFloorManagedObjectModel dependentManagedObject = managedObjects
							.get(conn.getOfficeFloorManagedObjectName());
					if (dependentManagedObject != null) {
						conn.setOfficeFloorManagedObjectDependency(dependency);
						conn.setOfficeFloorManagedObject(dependentManagedObject);
						conn.connect();
					}
				}
			}
		}

		// Connect the dependencies to the office floor input managed objects
		for (OfficeFloorManagedObjectModel managedObject : officeFloor
				.getOfficeFloorManagedObjects()) {
			for (OfficeFloorManagedObjectDependencyModel dependency : managedObject
					.getOfficeFloorManagedObjectDependencies()) {
				OfficeFloorManagedObjectDependencyToOfficeFloorInputManagedObjectModel conn = dependency
						.getOfficeFloorInputManagedObject();
				if (conn != null) {
					OfficeFloorInputManagedObjectModel dependentManagedObject = inputManagedObjects
							.get(conn.getOfficeFloorInputManagedObjectName());
					if (dependentManagedObject != null) {
						conn.setOfficeFloorManagedObjectDependency(dependency);
						conn.setOfficeFloorInputManagedObject(dependentManagedObject);
						conn.connect();
					}
				}
			}
		}

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

		// Connect the office floor managed object teams to office floor teams
		for (OfficeFloorManagedObjectSourceModel mos : officeFloor
				.getOfficeFloorManagedObjectSources()) {
			for (OfficeFloorManagedObjectSourceTeamModel mosTeam : mos
					.getOfficeFloorManagedObjectSourceTeams()) {
				OfficeFloorManagedObjectSourceTeamToOfficeFloorTeamModel conn = mosTeam
						.getOfficeFloorTeam();
				if (conn != null) {
					OfficeFloorTeamModel team = teams.get(conn
							.getOfficeFloorTeamName());
					if (team != null) {
						conn.setOfficeFloorManagedObjectSourceTeam(mosTeam);
						conn.setOfficeFloorTeam(team);
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

		// Specify the suppliers for the managed object sources
		for (OfficeFloorSupplierModel supplier : officeFloor
				.getOfficeFloorSuppliers()) {
			for (OfficeFloorManagedObjectSourceToOfficeFloorSupplierModel conn : supplier
					.getOfficeFloorManagedObjectSources()) {
				conn.setOfficeFloorSupplierName(supplier
						.getOfficeFloorSupplierName());
			}
		}

		// Specify managed object sources for the managed objects
		for (OfficeFloorManagedObjectSourceModel moSource : officeFloor
				.getOfficeFloorManagedObjectSources()) {
			for (OfficeFloorManagedObjectToOfficeFloorManagedObjectSourceModel conn : moSource
					.getOfficeFloorManagedObjects()) {
				conn.setOfficeFloorManagedObjectSourceName(moSource
						.getOfficeFloorManagedObjectSourceName());
			}
		}

		// Specify managing offices for the office floor managed objects
		for (DeployedOfficeModel office : officeFloor.getDeployedOffices()) {
			for (OfficeFloorManagedObjectSourceToDeployedOfficeModel conn : office
					.getOfficeFloorManagedObjectSources()) {
				conn.setManagingOfficeName(office.getDeployedOfficeName());
			}
		}

		// Specify input managed objects for office floor managed object sources
		for (OfficeFloorInputManagedObjectModel inputManagedObject : officeFloor
				.getOfficeFloorInputManagedObjects()) {
			for (OfficeFloorManagedObjectSourceToOfficeFloorInputManagedObjectModel conn : inputManagedObject
					.getOfficeFloorManagedObjectSources()) {
				conn.setOfficeFloorInputManagedObjectName(inputManagedObject
						.getOfficeFloorInputManagedObjectName());
			}
		}

		// Specify bound managed object source for input managed objects
		for (OfficeFloorManagedObjectSourceModel moSource : officeFloor
				.getOfficeFloorManagedObjectSources()) {
			for (OfficeFloorInputManagedObjectToBoundOfficeFloorManagedObjectSourceModel conn : moSource
					.getBoundOfficeFloorInputManagedObjects()) {
				conn.setOfficeFloorManagedObjectSourceName(moSource
						.getOfficeFloorManagedObjectSourceName());
			}
		}

		// Specify input dependencies for office floor managed object sources
		for (OfficeFloorManagedObjectModel managedObject : officeFloor
				.getOfficeFloorManagedObjects()) {
			for (OfficeFloorManagedObjectSourceInputDependencyToOfficeFloorManagedObjectModel conn : managedObject
					.getDependentOfficeFloorManagedObjectSourceInputs()) {
				conn.setOfficeFloorManagedObjectName(managedObject
						.getOfficeFloorManagedObjectName());
			}
		}

		// Specify office input for office floor managed object source flows
		for (DeployedOfficeModel office : officeFloor.getDeployedOffices()) {
			for (DeployedOfficeInputModel officeInput : office
					.getDeployedOfficeInputs()) {
				for (OfficeFloorManagedObjectSourceFlowToDeployedOfficeInputModel conn : officeInput
						.getOfficeFloorManagedObjectSourceFlows()) {
					conn.setDeployedOfficeName(office.getDeployedOfficeName());
					conn.setSectionName(officeInput.getSectionName());
					conn.setSectionInputName(officeInput.getSectionInputName());
				}
			}
		}

		// Specify office objects to office floor managed objects
		for (OfficeFloorManagedObjectModel managedObject : officeFloor
				.getOfficeFloorManagedObjects()) {
			for (DeployedOfficeObjectToOfficeFloorManagedObjectModel conn : managedObject
					.getDeployedOfficeObjects()) {
				conn.setOfficeFloorManagedObjectName(managedObject
						.getOfficeFloorManagedObjectName());
			}
		}

		// Specify office objects to office floor input managed objects
		for (OfficeFloorInputManagedObjectModel inputMo : officeFloor
				.getOfficeFloorInputManagedObjects()) {
			for (DeployedOfficeObjectToOfficeFloorInputManagedObjectModel conn : inputMo
					.getDeployedOfficeObjects()) {
				conn.setOfficeFloorInputManagedObjectName(inputMo
						.getOfficeFloorInputManagedObjectName());
			}
		}

		// Specify dependencies to office floor managed objects
		for (OfficeFloorManagedObjectModel managedObject : officeFloor
				.getOfficeFloorManagedObjects()) {
			for (OfficeFloorManagedObjectDependencyToOfficeFloorManagedObjectModel conn : managedObject
					.getDependentOfficeFloorManagedObjects()) {
				conn.setOfficeFloorManagedObjectName(managedObject
						.getOfficeFloorManagedObjectName());
			}
		}

		// Specify dependencies to office floor input managed objects
		for (OfficeFloorInputManagedObjectModel inputManagedObject : officeFloor
				.getOfficeFloorInputManagedObjects()) {
			for (OfficeFloorManagedObjectDependencyToOfficeFloorInputManagedObjectModel conn : inputManagedObject
					.getDependentOfficeFloorManagedObjects()) {
				conn.setOfficeFloorInputManagedObjectName(inputManagedObject
						.getOfficeFloorInputManagedObjectName());
			}
		}

		// Specify office teams to office floor teams
		for (OfficeFloorTeamModel officeFloorTeam : officeFloor
				.getOfficeFloorTeams()) {
			for (DeployedOfficeTeamToOfficeFloorTeamModel conn : officeFloorTeam
					.getDeployedOfficeTeams()) {
				conn.setOfficeFloorTeamName(officeFloorTeam
						.getOfficeFloorTeamName());
			}
		}

		// Specify managed object source teams to office floor teams
		for (OfficeFloorTeamModel officeFloorTeam : officeFloor
				.getOfficeFloorTeams()) {
			for (OfficeFloorManagedObjectSourceTeamToOfficeFloorTeamModel conn : officeFloorTeam
					.getOfficeFloorManagedObjectSourceTeams()) {
				conn.setOfficeFloorTeamName(officeFloorTeam
						.getOfficeFloorTeamName());
			}
		}

		// Store the office floor
		this.modelRepository.store(officeFloor, configuration);
	}

}