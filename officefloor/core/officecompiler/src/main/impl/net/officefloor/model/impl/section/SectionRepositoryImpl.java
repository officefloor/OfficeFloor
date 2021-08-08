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

package net.officefloor.model.impl.section;

import java.util.HashMap;
import java.util.Map;

import net.officefloor.compile.impl.util.DoubleKeyMap;
import net.officefloor.configuration.ConfigurationItem;
import net.officefloor.configuration.WritableConfigurationItem;
import net.officefloor.model.repository.ModelRepository;
import net.officefloor.model.section.ExternalFlowModel;
import net.officefloor.model.section.ExternalManagedObjectModel;
import net.officefloor.model.section.FunctionEscalationModel;
import net.officefloor.model.section.FunctionEscalationToExternalFlowModel;
import net.officefloor.model.section.FunctionEscalationToFunctionModel;
import net.officefloor.model.section.FunctionEscalationToSubSectionInputModel;
import net.officefloor.model.section.FunctionFlowModel;
import net.officefloor.model.section.FunctionFlowToExternalFlowModel;
import net.officefloor.model.section.FunctionFlowToFunctionModel;
import net.officefloor.model.section.FunctionFlowToSubSectionInputModel;
import net.officefloor.model.section.FunctionModel;
import net.officefloor.model.section.FunctionNamespaceModel;
import net.officefloor.model.section.FunctionToNextExternalFlowModel;
import net.officefloor.model.section.FunctionToNextFunctionModel;
import net.officefloor.model.section.FunctionToNextSubSectionInputModel;
import net.officefloor.model.section.ManagedFunctionModel;
import net.officefloor.model.section.ManagedFunctionObjectModel;
import net.officefloor.model.section.ManagedFunctionObjectToExternalManagedObjectModel;
import net.officefloor.model.section.ManagedFunctionObjectToSectionManagedObjectModel;
import net.officefloor.model.section.ManagedFunctionToFunctionModel;
import net.officefloor.model.section.SectionManagedObjectDependencyModel;
import net.officefloor.model.section.SectionManagedObjectDependencyToExternalManagedObjectModel;
import net.officefloor.model.section.SectionManagedObjectDependencyToSectionManagedObjectModel;
import net.officefloor.model.section.SectionManagedObjectModel;
import net.officefloor.model.section.SectionManagedObjectPoolModel;
import net.officefloor.model.section.SectionManagedObjectSourceFlowModel;
import net.officefloor.model.section.SectionManagedObjectSourceFlowToExternalFlowModel;
import net.officefloor.model.section.SectionManagedObjectSourceFlowToFunctionModel;
import net.officefloor.model.section.SectionManagedObjectSourceFlowToSubSectionInputModel;
import net.officefloor.model.section.SectionManagedObjectSourceModel;
import net.officefloor.model.section.SectionManagedObjectSourceToSectionManagedObjectPoolModel;
import net.officefloor.model.section.SectionManagedObjectToSectionManagedObjectSourceModel;
import net.officefloor.model.section.SectionModel;
import net.officefloor.model.section.SectionRepository;
import net.officefloor.model.section.SubSectionInputModel;
import net.officefloor.model.section.SubSectionModel;
import net.officefloor.model.section.SubSectionObjectModel;
import net.officefloor.model.section.SubSectionObjectToExternalManagedObjectModel;
import net.officefloor.model.section.SubSectionObjectToSectionManagedObjectModel;
import net.officefloor.model.section.SubSectionOutputModel;
import net.officefloor.model.section.SubSectionOutputToExternalFlowModel;
import net.officefloor.model.section.SubSectionOutputToFunctionModel;
import net.officefloor.model.section.SubSectionOutputToSubSectionInputModel;

/**
 * {@link SectionRepository} implementation.
 *
 * @author Daniel Sagenschneider
 */
public class SectionRepositoryImpl implements SectionRepository {

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
	public SectionRepositoryImpl(ModelRepository modelRepository) {
		this.modelRepository = modelRepository;
	}

	/*
	 * ===================== SectionRepository ==============================
	 */

	@Override
	public void retrieveSection(SectionModel section, ConfigurationItem configuration) throws Exception {

		// Load the section from the configuration
		this.modelRepository.retrieve(section, configuration);

		// Create the set of functions
		Map<String, FunctionModel> functions = new HashMap<String, FunctionModel>();
		for (FunctionModel function : section.getFunctions()) {
			functions.put(function.getFunctionName(), function);
		}

		// Create the map of inputs
		DoubleKeyMap<String, String, SubSectionInputModel> inputs = new DoubleKeyMap<String, String, SubSectionInputModel>();
		for (SubSectionModel subSection : section.getSubSections()) {
			for (SubSectionInputModel input : subSection.getSubSectionInputs()) {
				inputs.put(subSection.getSubSectionName(), input.getSubSectionInputName(), input);
			}
		}

		// Create the map of external flows
		Map<String, ExternalFlowModel> externalFlows = new HashMap<String, ExternalFlowModel>();
		for (ExternalFlowModel externalFlow : section.getExternalFlows()) {
			externalFlows.put(externalFlow.getExternalFlowName(), externalFlow);
		}

		// Connect the function to its next function
		for (FunctionModel previous : section.getFunctions()) {
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

		// Connect the function to its next input
		for (FunctionModel function : section.getFunctions()) {
			FunctionToNextSubSectionInputModel conn = function.getNextSubSectionInput();
			if (conn != null) {
				SubSectionInputModel input = inputs.get(conn.getSubSectionName(), conn.getSubSectionInputName());
				if (input != null) {
					conn.setPreviousFunction(function);
					conn.setNextSubSectionInput(input);
					conn.connect();
				}
			}
		}

		// Connect the functions to next external flow
		for (FunctionModel function : section.getFunctions()) {
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

		// Connect the function flows to functions
		for (FunctionModel function : section.getFunctions()) {
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

		// Connect the function flows to inputs
		for (FunctionModel function : section.getFunctions()) {
			for (FunctionFlowModel flow : function.getFunctionFlows()) {
				FunctionFlowToSubSectionInputModel conn = flow.getSubSectionInput();
				if (conn != null) {
					SubSectionInputModel input = inputs.get(conn.getSubSectionName(), conn.getSubSectionInputName());
					if (input != null) {
						conn.setFunctionFlow(flow);
						conn.setSubSectionInput(input);
						conn.connect();
					}
				}
			}
		}

		// Connect the function flows to external flow
		for (FunctionModel function : section.getFunctions()) {
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

		// Connect the escalation to functions
		for (FunctionModel function : section.getFunctions()) {
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

		// Connect the escalation to inputs
		for (FunctionModel function : section.getFunctions()) {
			for (FunctionEscalationModel escalation : function.getFunctionEscalations()) {
				FunctionEscalationToSubSectionInputModel conn = escalation.getSubSectionInput();
				if (conn != null) {
					SubSectionInputModel input = inputs.get(conn.getSubSectionName(), conn.getSubSectionInputName());
					if (input != null) {
						conn.setFunctionEscalation(escalation);
						conn.setSubSectionInput(input);
						conn.connect();
					}
				}
			}
		}

		// Connect the escalation to external flows
		for (FunctionModel function : section.getFunctions()) {
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

		// Connect the outputs to functions
		for (SubSectionModel subSection : section.getSubSections()) {
			for (SubSectionOutputModel output : subSection.getSubSectionOutputs()) {
				SubSectionOutputToFunctionModel conn = output.getFunction();
				if (conn != null) {
					FunctionModel function = functions.get(conn.getFunctionName());
					if (function != null) {
						conn.setSubSectionOutput(output);
						conn.setFunction(function);
						conn.connect();
					}
				}
			}
		}

		// Connect the outputs to the inputs
		for (SubSectionModel subSection : section.getSubSections()) {
			for (SubSectionOutputModel output : subSection.getSubSectionOutputs()) {
				SubSectionOutputToSubSectionInputModel conn = output.getSubSectionInput();
				if (conn != null) {
					SubSectionInputModel input = inputs.get(conn.getSubSectionName(), conn.getSubSectionInputName());
					if (input != null) {
						conn.setSubSectionOutput(output);
						conn.setSubSectionInput(input);
						conn.connect();
					}
				}
			}
		}

		// Connect the outputs to the external flows
		for (SubSectionModel subSection : section.getSubSections()) {
			for (SubSectionOutputModel output : subSection.getSubSectionOutputs()) {
				SubSectionOutputToExternalFlowModel conn = output.getExternalFlow();
				if (conn != null) {
					ExternalFlowModel externalFlow = externalFlows.get(conn.getExternalFlowName());
					if (externalFlow != null) {
						conn.setSubSectionOutput(output);
						conn.setExternalFlow(externalFlow);
						conn.connect();
					}
				}
			}
		}

		// Connect the managed object source flows to functions
		for (SectionManagedObjectSourceModel mos : section.getSectionManagedObjectSources()) {
			for (SectionManagedObjectSourceFlowModel mosFlow : mos.getSectionManagedObjectSourceFlows()) {
				SectionManagedObjectSourceFlowToFunctionModel conn = mosFlow.getFunction();
				if (conn != null) {
					FunctionModel function = functions.get(conn.getFunctionName());
					if (function != null) {
						conn.setSectionManagedObjectSourceFlow(mosFlow);
						conn.setFunction(function);
						conn.connect();
					}
				}
			}
		}

		// Connect the managed object source flows to the inputs
		for (SectionManagedObjectSourceModel mos : section.getSectionManagedObjectSources()) {
			for (SectionManagedObjectSourceFlowModel mosFlow : mos.getSectionManagedObjectSourceFlows()) {
				SectionManagedObjectSourceFlowToSubSectionInputModel conn = mosFlow.getSubSectionInput();
				if (conn != null) {
					SubSectionInputModel input = inputs.get(conn.getSubSectionName(), conn.getSubSectionInputName());
					if (input != null) {
						conn.setSectionManagedObjectSourceFlow(mosFlow);
						conn.setSubSectionInput(input);
						conn.connect();
					}
				}
			}
		}

		// Connect the managed object source flows to the external flows
		for (SectionManagedObjectSourceModel mos : section.getSectionManagedObjectSources()) {
			for (SectionManagedObjectSourceFlowModel mosFlow : mos.getSectionManagedObjectSourceFlows()) {
				SectionManagedObjectSourceFlowToExternalFlowModel conn = mosFlow.getExternalFlow();
				if (conn != null) {
					ExternalFlowModel externalFlow = externalFlows.get(conn.getExternalFlowName());
					if (externalFlow != null) {
						conn.setSectionManagedObjectSourceFlow(mosFlow);
						conn.setExternalFlow(externalFlow);
						conn.connect();
					}
				}
			}
		}

		// Create the map of managed object sources
		Map<String, SectionManagedObjectSourceModel> managedObjectSources = new HashMap<String, SectionManagedObjectSourceModel>();
		for (SectionManagedObjectSourceModel mos : section.getSectionManagedObjectSources()) {
			managedObjectSources.put(mos.getSectionManagedObjectSourceName(), mos);
		}

		// Connect the managed objects to their managed object sources
		for (SectionManagedObjectModel mo : section.getSectionManagedObjects()) {
			SectionManagedObjectToSectionManagedObjectSourceModel conn = mo.getSectionManagedObjectSource();
			if (conn != null) {
				SectionManagedObjectSourceModel mos = managedObjectSources
						.get(conn.getSectionManagedObjectSourceName());
				if (mos != null) {
					conn.setSectionManagedObject(mo);
					conn.setSectionManagedObjectSource(mos);
					conn.connect();
				}
			}
		}

		// Create the map of managed object pools
		Map<String, SectionManagedObjectPoolModel> pools = new HashMap<>();
		for (SectionManagedObjectPoolModel pool : section.getSectionManagedObjectPools()) {
			pools.put(pool.getSectionManagedObjectPoolName(), pool);
		}

		// Connect the managed object sources to their managed object pools
		for (SectionManagedObjectSourceModel mos : section.getSectionManagedObjectSources()) {
			SectionManagedObjectSourceToSectionManagedObjectPoolModel conn = mos.getSectionManagedObjectPool();
			if (conn != null) {
				SectionManagedObjectPoolModel pool = pools.get(conn.getSectionManagedObjectPoolName());
				if (pool != null) {
					conn.setSectionManagedObjectSource(mos);
					conn.setSectionManagedObjectPool(pool);
					conn.connect();
				}
			}
		}

		// Create the map of external managed objects
		Map<String, ExternalManagedObjectModel> externalMos = new HashMap<String, ExternalManagedObjectModel>();
		for (ExternalManagedObjectModel externalMo : section.getExternalManagedObjects()) {
			externalMos.put(externalMo.getExternalManagedObjectName(), externalMo);
		}

		// Connect the objects to external managed objects
		for (SubSectionModel subSection : section.getSubSections()) {
			for (SubSectionObjectModel object : subSection.getSubSectionObjects()) {
				SubSectionObjectToExternalManagedObjectModel conn = object.getExternalManagedObject();
				if (conn != null) {
					ExternalManagedObjectModel externalMo = externalMos.get(conn.getExternalManagedObjectName());
					if (externalMo != null) {
						conn.setSubSectionObject(object);
						conn.setExternalManagedObject(externalMo);
						conn.connect();
					}
				}
			}
		}

		// Create the set of managed functions
		DoubleKeyMap<String, String, ManagedFunctionModel> managedFunctionRegistry = new DoubleKeyMap<String, String, ManagedFunctionModel>();
		for (FunctionNamespaceModel namespace : section.getFunctionNamespaces()) {
			for (ManagedFunctionModel managedFunction : namespace.getManagedFunctions()) {
				managedFunctionRegistry.put(namespace.getFunctionNamespaceName(),
						managedFunction.getManagedFunctionName(), managedFunction);
			}
		}

		// Connect the functions to their managed functions
		for (FunctionModel function : section.getFunctions()) {
			// Obtain the managed function
			ManagedFunctionModel managedFunction = managedFunctionRegistry.get(function.getFunctionNamespaceName(),
					function.getManagedFunctionName());
			if (managedFunction != null) {
				// Connect
				new ManagedFunctionToFunctionModel(function, managedFunction).connect();
			}
		}

		// Create the map of managed objects
		Map<String, SectionManagedObjectModel> managedObjects = new HashMap<String, SectionManagedObjectModel>();
		for (SectionManagedObjectModel mo : section.getSectionManagedObjects()) {
			managedObjects.put(mo.getSectionManagedObjectName(), mo);
		}

		// Connect the objects to managed objects
		for (SubSectionModel subSection : section.getSubSections()) {
			for (SubSectionObjectModel object : subSection.getSubSectionObjects()) {
				SubSectionObjectToSectionManagedObjectModel conn = object.getSectionManagedObject();
				if (conn != null) {
					SectionManagedObjectModel mo = managedObjects.get(conn.getSectionManagedObjectName());
					if (mo != null) {
						conn.setSubSectionObject(object);
						conn.setSectionManagedObject(mo);
						conn.connect();
					}
				}
			}
		}

		// Connect the managed function objects to managed objects
		for (FunctionNamespaceModel namespace : section.getFunctionNamespaces()) {
			for (ManagedFunctionModel managedFunction : namespace.getManagedFunctions()) {
				for (ManagedFunctionObjectModel object : managedFunction.getManagedFunctionObjects()) {
					ManagedFunctionObjectToSectionManagedObjectModel conn = object.getSectionManagedObject();
					if (conn != null) {
						SectionManagedObjectModel mo = managedObjects.get(conn.getSectionManagedObjectName());
						if (mo != null) {
							conn.setManagedFunctionObject(object);
							conn.setSectionManagedObject(mo);
							conn.connect();
						}
					}
				}
			}
		}

		// Connect the dependencies to the external managed objects
		for (SectionManagedObjectModel mo : section.getSectionManagedObjects()) {
			for (SectionManagedObjectDependencyModel dependency : mo.getSectionManagedObjectDependencies()) {
				SectionManagedObjectDependencyToExternalManagedObjectModel conn = dependency.getExternalManagedObject();
				if (conn != null) {
					ExternalManagedObjectModel extMo = externalMos.get(conn.getExternalManagedObjectName());
					if (extMo != null) {
						conn.setSectionManagedObjectDependency(dependency);
						conn.setExternalManagedObject(extMo);
						conn.connect();
					}
				}
			}
		}

		// Connect the managed function objects to external managed objects
		for (FunctionNamespaceModel namespace : section.getFunctionNamespaces()) {
			for (ManagedFunctionModel managedFunction : namespace.getManagedFunctions()) {
				for (ManagedFunctionObjectModel managedFunctionObject : managedFunction.getManagedFunctionObjects()) {
					// Obtain the connection
					ManagedFunctionObjectToExternalManagedObjectModel conn = managedFunctionObject
							.getExternalManagedObject();
					if (conn != null) {
						// Obtain the external managed object
						ExternalManagedObjectModel extMo = externalMos.get(conn.getExternalManagedObjectName());
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

		// Connect the dependencies to the managed objects
		for (SectionManagedObjectModel mo : section.getSectionManagedObjects()) {
			for (SectionManagedObjectDependencyModel dependency : mo.getSectionManagedObjectDependencies()) {
				SectionManagedObjectDependencyToSectionManagedObjectModel conn = dependency.getSectionManagedObject();
				if (conn != null) {
					SectionManagedObjectModel dependentMo = managedObjects.get(conn.getSectionManagedObjectName());
					if (dependentMo != null) {
						conn.setSectionManagedObjectDependency(dependency);
						conn.setSectionManagedObject(dependentMo);
						conn.connect();
					}
				}
			}
		}
	}

	@Override
	public void storeSection(SectionModel section, WritableConfigurationItem configuration) throws Exception {

		// Specify function flow to function
		for (FunctionModel function : section.getFunctions()) {
			for (FunctionFlowModel flow : function.getFunctionFlows()) {
				FunctionFlowToFunctionModel conn = flow.getFunction();
				if (conn != null) {
					conn.setFunctionName(conn.getFunction().getFunctionName());
				}
			}
		}

		// Specify function flow to input
		for (SubSectionModel subSection : section.getSubSections()) {
			for (SubSectionInputModel input : subSection.getSubSectionInputs()) {
				for (FunctionFlowToSubSectionInputModel conn : input.getFunctionFlows()) {
					conn.setSubSectionName(subSection.getSubSectionName());
					conn.setSubSectionInputName(input.getSubSectionInputName());
				}
			}
		}

		// Specify function flow to external flow
		for (FunctionModel function : section.getFunctions()) {
			for (FunctionFlowModel flow : function.getFunctionFlows()) {
				FunctionFlowToExternalFlowModel conn = flow.getExternalFlow();
				if (conn != null) {
					conn.setExternalFlowName(conn.getExternalFlow().getExternalFlowName());
				}
			}
		}

		// Specify function to next functions
		for (FunctionModel function : section.getFunctions()) {
			FunctionToNextFunctionModel conn = function.getNextFunction();
			if (conn != null) {
				conn.setNextFunctionName(conn.getNextFunction().getFunctionName());
			}
		}

		// Specify function to next input
		for (SubSectionModel subSection : section.getSubSections()) {
			for (SubSectionInputModel input : subSection.getSubSectionInputs()) {
				for (FunctionToNextSubSectionInputModel conn : input.getPreviousFunctions()) {
					conn.setSubSectionName(subSection.getSubSectionName());
					conn.setSubSectionInputName(input.getSubSectionInputName());
				}
			}
		}

		// Specify function next to external flow
		for (FunctionModel function : section.getFunctions()) {
			FunctionToNextExternalFlowModel conn = function.getNextExternalFlow();
			if (conn != null) {
				conn.setExternalFlowName(conn.getNextExternalFlow().getExternalFlowName());
			}
		}

		// Specify function escalation to function
		for (FunctionModel function : section.getFunctions()) {
			for (FunctionEscalationModel functionEscalation : function.getFunctionEscalations()) {
				FunctionEscalationToFunctionModel conn = functionEscalation.getFunction();
				if (conn != null) {
					conn.setFunctionName(conn.getFunction().getFunctionName());
				}
			}
		}

		// Specify function escalation to input
		for (SubSectionModel subSection : section.getSubSections()) {
			for (SubSectionInputModel input : subSection.getSubSectionInputs()) {
				for (FunctionEscalationToSubSectionInputModel conn : input.getFunctionEscalations()) {
					conn.setSubSectionName(subSection.getSubSectionName());
					conn.setSubSectionInputName(input.getSubSectionInputName());
				}
			}
		}

		// Specify function escalation to external function
		for (FunctionModel function : section.getFunctions()) {
			for (FunctionEscalationModel functionEscalation : function.getFunctionEscalations()) {
				FunctionEscalationToExternalFlowModel conn = functionEscalation.getExternalFlow();
				if (conn != null) {
					conn.setExternalFlowName(conn.getExternalFlow().getExternalFlowName());
				}
			}
		}

		// Specify output to function
		for (FunctionModel function : section.getFunctions()) {
			for (SubSectionOutputToFunctionModel conn : function.getSubSectionOutputs()) {
				conn.setFunctionName(function.getFunctionName());
			}
		}

		// Specify output to input
		for (SubSectionModel subSection : section.getSubSections()) {
			for (SubSectionInputModel input : subSection.getSubSectionInputs()) {
				for (SubSectionOutputToSubSectionInputModel conn : input.getSubSectionOutputs()) {
					conn.setSubSectionName(subSection.getSubSectionName());
					conn.setSubSectionInputName(input.getSubSectionInputName());
				}
			}
		}

		// Specify output to external flow
		for (ExternalFlowModel extFlow : section.getExternalFlows()) {
			for (SubSectionOutputToExternalFlowModel conn : extFlow.getSubSectionOutputs()) {
				conn.setExternalFlowName(extFlow.getExternalFlowName());
			}
		}

		// Specify managed object source flow to its function
		for (FunctionModel function : section.getFunctions()) {
			for (SectionManagedObjectSourceFlowToFunctionModel conn : function.getSectionManagedObjectSourceFlows()) {
				conn.setFunctionName(function.getFunctionName());
			}
		}

		// Specify managed object source flow to input
		for (SubSectionModel subSection : section.getSubSections()) {
			for (SubSectionInputModel input : subSection.getSubSectionInputs()) {
				for (SectionManagedObjectSourceFlowToSubSectionInputModel conn : input
						.getSectionManagedObjectSourceFlows()) {
					conn.setSubSectionName(subSection.getSubSectionName());
					conn.setSubSectionInputName(input.getSubSectionInputName());
				}
			}
		}

		// Specify managed object source flow to external flow
		for (ExternalFlowModel extFlow : section.getExternalFlows()) {
			for (SectionManagedObjectSourceFlowToExternalFlowModel conn : extFlow
					.getSectionManagedObjectSourceFlows()) {
				conn.setExternalFlowName(extFlow.getExternalFlowName());
			}
		}

		// Specify managed objects to their managed object sources
		for (SectionManagedObjectSourceModel mos : section.getSectionManagedObjectSources()) {
			for (SectionManagedObjectToSectionManagedObjectSourceModel conn : mos.getSectionManagedObjects()) {
				conn.setSectionManagedObjectSourceName(mos.getSectionManagedObjectSourceName());
			}
		}

		// Specify managed objects to their managed object pools
		for (SectionManagedObjectPoolModel pool : section.getSectionManagedObjectPools()) {
			for (SectionManagedObjectSourceToSectionManagedObjectPoolModel conn : pool
					.getSectionManagedObjectSources()) {
				conn.setSectionManagedObjectPoolName(pool.getSectionManagedObjectPoolName());
			}
		}

		// Specify managed function object to managed object
		for (SectionManagedObjectModel mo : section.getSectionManagedObjects()) {
			for (ManagedFunctionObjectToSectionManagedObjectModel conn : mo.getManagedFunctionObjects()) {
				conn.setSectionManagedObjectName(mo.getSectionManagedObjectName());
			}
		}

		// Specify managed function object to external managed object
		for (FunctionNamespaceModel namespace : section.getFunctionNamespaces()) {
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

		// Specify object to external managed object
		for (ExternalManagedObjectModel extMo : section.getExternalManagedObjects()) {
			for (SubSectionObjectToExternalManagedObjectModel conn : extMo.getSubSectionObjects()) {
				conn.setExternalManagedObjectName(extMo.getExternalManagedObjectName());
			}
		}

		// Specify object to managed object
		for (SectionManagedObjectModel mo : section.getSectionManagedObjects()) {
			for (SubSectionObjectToSectionManagedObjectModel conn : mo.getSubSectionObjects()) {
				conn.setSectionManagedObjectName(mo.getSectionManagedObjectName());
			}
		}

		// Specify dependency to external managed object
		for (ExternalManagedObjectModel extMo : section.getExternalManagedObjects()) {
			for (SectionManagedObjectDependencyToExternalManagedObjectModel conn : extMo
					.getDependentSectionManagedObjects()) {
				conn.setExternalManagedObjectName(extMo.getExternalManagedObjectName());
			}
		}

		// Specify dependency to managed object
		for (SectionManagedObjectModel mo : section.getSectionManagedObjects()) {
			for (SectionManagedObjectDependencyToSectionManagedObjectModel conn : mo
					.getDependentSectionManagedObjects()) {
				conn.setSectionManagedObjectName(mo.getSectionManagedObjectName());
			}
		}

		// Store the section into the configuration
		this.modelRepository.store(section, configuration);
	}

}
