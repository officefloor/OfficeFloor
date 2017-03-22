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
package net.officefloor.model.impl.section;

import java.util.HashMap;
import java.util.Map;

import net.officefloor.compile.impl.util.DoubleKeyMap;
import net.officefloor.model.repository.ConfigurationItem;
import net.officefloor.model.repository.ModelRepository;
import net.officefloor.model.section.DeskManagedObjectDependencyModel;
import net.officefloor.model.section.DeskManagedObjectDependencyToDeskManagedObjectModel;
import net.officefloor.model.section.DeskManagedObjectDependencyToExternalManagedObjectModel;
import net.officefloor.model.section.DeskManagedObjectModel;
import net.officefloor.model.section.DeskManagedObjectSourceFlowModel;
import net.officefloor.model.section.DeskManagedObjectSourceFlowToExternalFlowModel;
import net.officefloor.model.section.DeskManagedObjectSourceFlowToFunctionModel;
import net.officefloor.model.section.DeskManagedObjectSourceModel;
import net.officefloor.model.section.DeskManagedObjectToDeskManagedObjectSourceModel;
import net.officefloor.model.section.DeskModel;
import net.officefloor.model.section.DeskRepository;
import net.officefloor.model.section.ExternalFlowModel;
import net.officefloor.model.section.ExternalManagedObjectModel;
import net.officefloor.model.section.FunctionEscalationModel;
import net.officefloor.model.section.FunctionEscalationToExternalFlowModel;
import net.officefloor.model.section.FunctionEscalationToFunctionModel;
import net.officefloor.model.section.FunctionFlowModel;
import net.officefloor.model.section.FunctionFlowToExternalFlowModel;
import net.officefloor.model.section.FunctionFlowToFunctionModel;
import net.officefloor.model.section.FunctionModel;
import net.officefloor.model.section.FunctionNamespaceModel;
import net.officefloor.model.section.FunctionToNextExternalFlowModel;
import net.officefloor.model.section.FunctionToNextFunctionModel;
import net.officefloor.model.section.ManagedFunctionModel;
import net.officefloor.model.section.ManagedFunctionObjectModel;
import net.officefloor.model.section.ManagedFunctionObjectToDeskManagedObjectModel;
import net.officefloor.model.section.ManagedFunctionObjectToExternalManagedObjectModel;
import net.officefloor.model.section.ManagedFunctionToFunctionModel;

/**
 * {@link DeskRepository} implementation.
 *
 * @author Daniel Sagenschneider
 */
public class DeskRepositoryImpl implements DeskRepository {

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
	public DeskRepositoryImpl(ModelRepository modelRepository) {
		this.modelRepository = modelRepository;
	}

	/*
	 * ================== DeskRepository ==========================
	 */

	@Override
	public DeskModel retrieveDesk(ConfigurationItem configuration) throws Exception {

		// Load the desk from the configuration
		DeskModel desk = this.modelRepository.retrieve(new DeskModel(), configuration);

		// Create the set of managed object sources
		Map<String, DeskManagedObjectSourceModel> managedObjectSources = new HashMap<String, DeskManagedObjectSourceModel>();
		for (DeskManagedObjectSourceModel mos : desk.getDeskManagedObjectSources()) {
			managedObjectSources.put(mos.getDeskManagedObjectSourceName(), mos);
		}

		// Connect the managed objects to their managed object sources
		for (DeskManagedObjectModel mo : desk.getDeskManagedObjects()) {
			DeskManagedObjectToDeskManagedObjectSourceModel conn = mo.getDeskManagedObjectSource();
			if (conn != null) {
				DeskManagedObjectSourceModel mos = managedObjectSources.get(conn.getDeskManagedObjectSourceName());
				if (mos != null) {
					conn.setDeskManagedObject(mo);
					conn.setDeskManagedObjectSource(mos);
					conn.connect();
				}
			}
		}

		// Create the set of external managed objects
		Map<String, ExternalManagedObjectModel> externalManagedObjects = new HashMap<String, ExternalManagedObjectModel>();
		for (ExternalManagedObjectModel mo : desk.getExternalManagedObjects()) {
			externalManagedObjects.put(mo.getExternalManagedObjectName(), mo);
		}

		// Connect the managed function objects to external managed objects
		for (FunctionNamespaceModel namespace : desk.getFunctionNamespaces()) {
			for (ManagedFunctionModel managedFunction : namespace.getManagedFunctions()) {
				for (ManagedFunctionObjectModel managedFunctionObject : managedFunction.getManagedFunctionObjects()) {
					// Obtain the connection
					ManagedFunctionObjectToExternalManagedObjectModel conn = managedFunctionObject
							.getExternalManagedObject();
					if (conn != null) {
						// Obtain the external managed object
						ExternalManagedObjectModel extMo = externalManagedObjects
								.get(conn.getExternalManagedObjectName());
						if (extMo != null) {
							// Connect
							conn.setManagedFunctionObject(managedFunctionObject);
							conn.setExternalManagedObject(extMo);
							conn.connect();
						}
					}
				}
			}
		}

		// Create the set of managed objects
		Map<String, DeskManagedObjectModel> managedObjects = new HashMap<String, DeskManagedObjectModel>();
		for (DeskManagedObjectModel mo : desk.getDeskManagedObjects()) {
			managedObjects.put(mo.getDeskManagedObjectName(), mo);
		}

		// Connect the managed function objects to managed objects
		for (FunctionNamespaceModel namespace : desk.getFunctionNamespaces()) {
			for (ManagedFunctionModel managedFunction : namespace.getManagedFunctions()) {
				for (ManagedFunctionObjectModel object : managedFunction.getManagedFunctionObjects()) {
					ManagedFunctionObjectToDeskManagedObjectModel conn = object.getDeskManagedObject();
					if (conn != null) {
						DeskManagedObjectModel mo = managedObjects.get(conn.getDeskManagedObjectName());
						if (mo != null) {
							conn.setManagedFunctionObject(object);
							conn.setDeskManagedObject(mo);
							conn.connect();
						}
					}
				}
			}
		}

		// Connect the dependencies to external managed objects
		for (DeskManagedObjectModel mo : desk.getDeskManagedObjects()) {
			for (DeskManagedObjectDependencyModel dependency : mo.getDeskManagedObjectDependencies()) {
				DeskManagedObjectDependencyToExternalManagedObjectModel conn = dependency.getExternalManagedObject();
				if (conn != null) {
					ExternalManagedObjectModel extMo = externalManagedObjects.get(conn.getExternalManagedObjectName());
					if (extMo != null) {
						conn.setDeskManagedObjectDependency(dependency);
						conn.setExternalManagedObject(extMo);
						conn.connect();
					}
				}
			}
		}

		// Connect the dependencies to managed objects
		for (DeskManagedObjectModel mo : desk.getDeskManagedObjects()) {
			for (DeskManagedObjectDependencyModel dependency : mo.getDeskManagedObjectDependencies()) {
				DeskManagedObjectDependencyToDeskManagedObjectModel conn = dependency.getDeskManagedObject();
				if (conn != null) {
					DeskManagedObjectModel dependentMo = managedObjects.get(conn.getDeskManagedObjectName());
					if (dependentMo != null) {
						conn.setDeskManagedObjectDependency(dependency);
						conn.setDeskManagedObject(dependentMo);
						conn.connect();
					}
				}
			}
		}

		// Create the set of external flows
		Map<String, ExternalFlowModel> externalFlows = new HashMap<String, ExternalFlowModel>();
		for (ExternalFlowModel flow : desk.getExternalFlows()) {
			externalFlows.put(flow.getExternalFlowName(), flow);
		}

		// Connect the managed object source flows to external flows
		for (DeskManagedObjectSourceModel mos : desk.getDeskManagedObjectSources()) {
			for (DeskManagedObjectSourceFlowModel mosFlow : mos.getDeskManagedObjectSourceFlows()) {
				DeskManagedObjectSourceFlowToExternalFlowModel conn = mosFlow.getExternalFlow();
				if (conn != null) {
					ExternalFlowModel extFlow = externalFlows.get(conn.getExternalFlowName());
					if (extFlow != null) {
						conn.setDeskManagedObjectSourceFlow(mosFlow);
						conn.setExternalFlow(extFlow);
						conn.connect();
					}
				}
			}
		}

		// Connect the function flows to external flow
		for (FunctionModel function : desk.getFunctions()) {
			for (FunctionFlowModel flow : function.getFunctionFlows()) {
				// Obtain the connection
				FunctionFlowToExternalFlowModel conn = flow.getExternalFlow();
				if (conn != null) {
					// Obtain the external flow
					ExternalFlowModel extFlow = externalFlows.get(conn.getExternalFlowName());
					if (extFlow != null) {
						// Connect
						conn.setFunctionFlow(flow);
						conn.setExternalFlow(extFlow);
						conn.connect();
					}
				}
			}
		}

		// Connect the functions to next external flow
		for (FunctionModel function : desk.getFunctions()) {
			// Obtain the connection
			FunctionToNextExternalFlowModel conn = function.getNextExternalFlow();
			if (conn != null) {
				// Obtain the external flow
				ExternalFlowModel extFlow = externalFlows.get(conn.getExternalFlowName());
				if (extFlow != null) {
					// Connect
					conn.setPreviousFunction(function);
					conn.setNextExternalFlow(extFlow);
					conn.connect();
				}
			}
		}

		// Create the set of functions
		Map<String, FunctionModel> functions = new HashMap<String, FunctionModel>();
		for (FunctionModel function : desk.getFunctions()) {
			functions.put(function.getFunctionName(), function);
		}

		// Connect the managed object source flows to functions
		for (DeskManagedObjectSourceModel mos : desk.getDeskManagedObjectSources()) {
			for (DeskManagedObjectSourceFlowModel mosFlow : mos.getDeskManagedObjectSourceFlows()) {
				DeskManagedObjectSourceFlowToFunctionModel conn = mosFlow.getFunction();
				if (conn != null) {
					FunctionModel function = functions.get(conn.getFunctionName());
					if (function != null) {
						conn.setDeskManagedObjectSourceFlow(mosFlow);
						conn.setFunction(function);
						conn.connect();
					}
				}
			}
		}

		// Connect the function flows to functions
		for (FunctionModel function : desk.getFunctions()) {
			for (FunctionFlowModel flow : function.getFunctionFlows()) {
				// Obtain the connection
				FunctionFlowToFunctionModel conn = flow.getFunction();
				if (conn != null) {
					// Obtain the function
					FunctionModel flowFunction = functions.get(conn.getFunctionName());
					if (flowFunction != null) {
						// Connect
						conn.setFunctionFlow(flow);
						conn.setFunction(flowFunction);
						conn.connect();
					}
				}
			}
		}

		// Connect the escalation to functions
		for (FunctionModel function : desk.getFunctions()) {
			for (FunctionEscalationModel escalation : function.getFunctionEscalations()) {
				// Obtain the connection
				FunctionEscalationToFunctionModel conn = escalation.getFunction();
				if (conn != null) {
					// Obtain the handling function
					FunctionModel escalationFunction = functions.get(conn.getFunctionName());
					if (escalationFunction != null) {
						// Connect
						conn.setEscalation(escalation);
						conn.setFunction(escalationFunction);
						conn.connect();
					}
				}
			}
		}

		// Connect the escalation to external flows
		for (FunctionModel function : desk.getFunctions()) {
			for (FunctionEscalationModel escalation : function.getFunctionEscalations()) {
				// Obtain the connection
				FunctionEscalationToExternalFlowModel conn = escalation.getExternalFlow();
				if (conn != null) {
					// Obtain the external flow
					ExternalFlowModel externalFlow = externalFlows.get(conn.getExternalFlowName());
					if (externalFlow != null) {
						// Connect
						conn.setFunctionEscalation(escalation);
						conn.setExternalFlow(externalFlow);
						conn.connect();
					}
				}
			}
		}

		// Connect the function to its next function
		for (FunctionModel previous : desk.getFunctions()) {
			// Obtain the connection
			FunctionToNextFunctionModel conn = previous.getNextFunction();
			if (conn != null) {
				// Obtain the next function
				FunctionModel next = functions.get(conn.getNextFunctionName());
				if (next != null) {
					// Connect
					conn.setPreviousFunction(previous);
					conn.setNextFunction(next);
					conn.connect();
				}
			}
		}

		// Create the set of managed functions
		DoubleKeyMap<String, String, ManagedFunctionModel> managedFunctionRegistry = new DoubleKeyMap<String, String, ManagedFunctionModel>();
		for (FunctionNamespaceModel namespace : desk.getFunctionNamespaces()) {
			for (ManagedFunctionModel managedFunction : namespace.getManagedFunctions()) {
				managedFunctionRegistry.put(namespace.getFunctionNamespaceName(),
						managedFunction.getManagedFunctionName(), managedFunction);
			}
		}

		// Connect the functions to their managed functions
		for (FunctionModel function : desk.getFunctions()) {
			// Obtain the managed function
			ManagedFunctionModel managedFunction = managedFunctionRegistry.get(function.getFunctionNamespaceName(),
					function.getFunctionName());
			if (managedFunction != null) {
				// Connect
				new ManagedFunctionToFunctionModel(function, managedFunction).connect();
			}
		}

		// Return the desk
		return desk;
	}

	@Override
	public void storeDesk(DeskModel desk, ConfigurationItem configuration) throws Exception {

		// Specify managed object to its managed object source
		for (DeskManagedObjectSourceModel mos : desk.getDeskManagedObjectSources()) {
			for (DeskManagedObjectToDeskManagedObjectSourceModel conn : mos.getDeskManagedObjects()) {
				conn.setDeskManagedObjectSourceName(mos.getDeskManagedObjectSourceName());
			}
		}

		// Specify managed object source flow to its external flow
		for (ExternalFlowModel extFlow : desk.getExternalFlows()) {
			for (DeskManagedObjectSourceFlowToExternalFlowModel conn : extFlow.getDeskManagedObjectSourceFlows()) {
				conn.setExternalFlowName(extFlow.getExternalFlowName());
			}
		}

		// Specify managed object source flow to its function
		for (FunctionModel function : desk.getFunctions()) {
			for (DeskManagedObjectSourceFlowToFunctionModel conn : function.getDeskManagedObjectSourceFlows()) {
				conn.setFunctionName(function.getFunctionName());
			}
		}

		// Specify managed function object to external managed object
		for (FunctionNamespaceModel namespace : desk.getFunctionNamespaces()) {
			for (ManagedFunctionModel managedFunction : namespace.getManagedFunctions()) {
				for (ManagedFunctionObjectModel managedFunctionObject : managedFunction.getManagedFunctionObjects()) {
					ManagedFunctionObjectToExternalManagedObjectModel conn = managedFunctionObject
							.getExternalManagedObject();
					if (conn != null) {
						conn.setExternalManagedObjectName(
								conn.getExternalManagedObject().getExternalManagedObjectName());
					}
				}
			}
		}

		// Specify managed function object to managed object
		for (DeskManagedObjectModel mo : desk.getDeskManagedObjects()) {
			for (ManagedFunctionObjectToDeskManagedObjectModel conn : mo.getManagedFunctionObjects()) {
				conn.setDeskManagedObjectName(mo.getDeskManagedObjectName());
			}
		}

		// Specify dependency to external managed object
		for (ExternalManagedObjectModel extMo : desk.getExternalManagedObjects()) {
			for (DeskManagedObjectDependencyToExternalManagedObjectModel conn : extMo
					.getDependentDeskManagedObjects()) {
				conn.setExternalManagedObjectName(extMo.getExternalManagedObjectName());
			}
		}

		// Specify dependency to managed object
		for (DeskManagedObjectModel mo : desk.getDeskManagedObjects()) {
			for (DeskManagedObjectDependencyToDeskManagedObjectModel conn : mo.getDependentDeskManagedObjects()) {
				conn.setDeskManagedObjectName(mo.getDeskManagedObjectName());
			}
		}

		// Specify function flow to external flow
		for (FunctionModel function : desk.getFunctions()) {
			for (FunctionFlowModel flow : function.getFunctionFlows()) {
				FunctionFlowToExternalFlowModel conn = flow.getExternalFlow();
				if (conn != null) {
					conn.setExternalFlowName(conn.getExternalFlow().getExternalFlowName());
				}
			}
		}

		// Specify function next to external flow
		for (FunctionModel function : desk.getFunctions()) {
			FunctionToNextExternalFlowModel conn = function.getNextExternalFlow();
			if (conn != null) {
				conn.setExternalFlowName(conn.getNextExternalFlow().getExternalFlowName());
			}
		}

		// Specify function flow to function
		for (FunctionModel function : desk.getFunctions()) {
			for (FunctionFlowModel flow : function.getFunctionFlows()) {
				FunctionFlowToFunctionModel conn = flow.getFunction();
				if (conn != null) {
					conn.setFunctionName(conn.getFunction().getFunctionName());
				}
			}
		}

		// Specify function escalation to function
		for (FunctionModel function : desk.getFunctions()) {
			for (FunctionEscalationModel functionEscalation : function.getFunctionEscalations()) {
				FunctionEscalationToFunctionModel conn = functionEscalation.getFunction();
				if (conn != null) {
					conn.setFunctionName(conn.getFunction().getFunctionName());
				}
			}
		}

		// Specify function escalation to external function
		for (FunctionModel function : desk.getFunctions()) {
			for (FunctionEscalationModel functionEscalation : function.getFunctionEscalations()) {
				FunctionEscalationToExternalFlowModel conn = functionEscalation.getExternalFlow();
				if (conn != null) {
					conn.setExternalFlowName(conn.getExternalFlow().getExternalFlowName());
				}
			}
		}

		// Specify next functions
		for (FunctionModel function : desk.getFunctions()) {
			FunctionToNextFunctionModel conn = function.getNextFunction();
			if (conn != null) {
				conn.setNextFunctionName(conn.getNextFunction().getFunctionName());
			}
		}

		// Stores the desk
		this.modelRepository.store(desk, configuration);
	}

}