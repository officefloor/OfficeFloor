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

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import net.officefloor.compile.SectionSourceService;
import net.officefloor.compile.impl.util.CompileUtil;
import net.officefloor.compile.spi.section.FunctionFlow;
import net.officefloor.compile.spi.section.FunctionObject;
import net.officefloor.compile.spi.section.ManagedObjectDependency;
import net.officefloor.compile.spi.section.ManagedObjectFlow;
import net.officefloor.compile.spi.section.SectionDesigner;
import net.officefloor.compile.spi.section.SectionFunction;
import net.officefloor.compile.spi.section.SectionFunctionNamespace;
import net.officefloor.compile.spi.section.SectionInput;
import net.officefloor.compile.spi.section.SectionManagedObject;
import net.officefloor.compile.spi.section.SectionManagedObjectSource;
import net.officefloor.compile.spi.section.SectionObject;
import net.officefloor.compile.spi.section.SectionOutput;
import net.officefloor.compile.spi.section.source.SectionSource;
import net.officefloor.compile.spi.section.source.SectionSourceContext;
import net.officefloor.compile.spi.section.source.impl.AbstractSectionSource;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.model.impl.repository.ModelRepositoryImpl;
import net.officefloor.model.impl.repository.inputstream.InputStreamConfigurationItem;
import net.officefloor.model.section.DeskChanges;
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
import net.officefloor.model.section.PropertyModel;
import net.officefloor.model.section.SectionManagedObjectModel;

/**
 * {@link SectionSource} for a {@link DeskModel}.
 * 
 * @author Daniel Sagenschneider
 */
public class DeskModelSectionSource extends AbstractSectionSource
		implements SectionSourceService<DeskModelSectionSource> {

	/*
	 * =================== SectionSourceService ===============================
	 */

	@Override
	public String getSectionSourceAlias() {
		return "DESK";
	}

	@Override
	public Class<DeskModelSectionSource> getSectionSourceClass() {
		return DeskModelSectionSource.class;
	}

	/*
	 * ================= SectionSource ===========================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
		// No specification required
	}

	@Override
	public void sourceSection(SectionDesigner designer, SectionSourceContext context) throws Exception {

		// Obtain the configuration to the desk
		InputStream configuration = context.getResource(context.getSectionLocation());
		if (configuration == null) {
			// Must have configuration
			throw new FileNotFoundException("Can not find desk '" + context.getSectionLocation() + "'");
		}

		// Retrieve the desk model
		DeskModel desk = new DeskRepositoryImpl(new ModelRepositoryImpl())
				.retrieveDesk(new InputStreamConfigurationItem(configuration));

		// Add the external flows as outputs, keeping registry of the outputs
		Map<String, SectionOutput> sectionOutputs = new HashMap<String, SectionOutput>();
		for (ExternalFlowModel extFlow : desk.getExternalFlows()) {

			// Determine if escalation only (only function escalation)
			boolean isEscalationOnly = ((extFlow.getPreviousFunctions().size() == 0)
					&& (extFlow.getFunctionFlows().size() == 0) && (extFlow.getFunctionEscalations().size() > 0));

			// Add the section output and register
			String sectionOutputName = extFlow.getExternalFlowName();
			SectionOutput sectionOutput = designer.addSectionOutput(sectionOutputName, extFlow.getArgumentType(),
					isEscalationOnly);
			sectionOutputs.put(sectionOutputName, sectionOutput);
		}

		// Add the external managed objects as objects, keeping registry of them
		Map<String, SectionObject> sectionObjects = new HashMap<String, SectionObject>();
		for (ExternalManagedObjectModel extMo : desk.getExternalManagedObjects()) {
			String sectionObjectName = extMo.getExternalManagedObjectName();
			SectionObject sectionObject = designer.addSectionObject(sectionObjectName, extMo.getObjectType());
			sectionObjects.put(sectionObjectName, sectionObject);
		}

		// Add the managed object sources, keeping registry of them
		Map<String, SectionManagedObjectSource> managedObjectSources = new HashMap<String, SectionManagedObjectSource>();
		for (DeskManagedObjectSourceModel mosModel : desk.getDeskManagedObjectSources()) {

			// Add the managed object source
			String mosName = mosModel.getDeskManagedObjectSourceName();
			SectionManagedObjectSource mos = designer.addSectionManagedObjectSource(mosName,
					mosModel.getManagedObjectSourceClassName());
			for (PropertyModel property : mosModel.getProperties()) {
				mos.addProperty(property.getName(), property.getValue());
			}

			// Provide timeout
			String timeoutValue = mosModel.getTimeout();
			if (!CompileUtil.isBlank(timeoutValue)) {
				try {
					mos.setTimeout(Long.valueOf(timeoutValue));
				} catch (NumberFormatException ex) {
					designer.addIssue(
							"Invalid timeout value: " + timeoutValue + " for managed object source " + mosName);
				}
			}

			// Register the managed object source
			managedObjectSources.put(mosName, mos);
		}

		// Add the managed objects, keeping registry of them
		Map<String, SectionManagedObject> managedObjects = new HashMap<String, SectionManagedObject>();
		for (DeskManagedObjectModel moModel : desk.getDeskManagedObjects()) {

			// Obtain the managed object details
			String managedObjectName = moModel.getDeskManagedObjectName();
			ManagedObjectScope managedObjectScope = this.getManagedObjectScope(moModel.getManagedObjectScope(),
					designer, managedObjectName);

			// Obtain the managed object source for the managed object
			SectionManagedObjectSource moSource = null;
			DeskManagedObjectToDeskManagedObjectSourceModel moToSource = moModel.getDeskManagedObjectSource();
			if (moToSource != null) {
				DeskManagedObjectSourceModel moSourceModel = moToSource.getDeskManagedObjectSource();
				if (moSourceModel != null) {
					moSource = managedObjectSources.get(moSourceModel.getDeskManagedObjectSourceName());
				}
			}
			if (moSource == null) {
				continue; // must have managed object source
			}

			// Add the managed object and also register it
			SectionManagedObject managedObject = moSource.addSectionManagedObject(managedObjectName,
					managedObjectScope);
			managedObjects.put(managedObjectName, managedObject);
		}

		// Link managed object dependencies to managed objects/external objects
		for (DeskManagedObjectModel moModel : desk.getDeskManagedObjects()) {

			// Obtain the managed object
			SectionManagedObject managedObject = managedObjects.get(moModel.getDeskManagedObjectName());
			if (managedObject == null) {
				continue; // should always have
			}

			// Link dependencies to managed object/external object
			for (DeskManagedObjectDependencyModel dependencyModel : moModel.getDeskManagedObjectDependencies()) {

				// Obtain the dependency
				ManagedObjectDependency dependency = managedObject
						.getManagedObjectDependency(dependencyModel.getDeskManagedObjectDependencyName());

				// Link dependency to managed object
				SectionManagedObject linkedManagedObject = null;
				DeskManagedObjectDependencyToDeskManagedObjectModel dependencyToMo = dependencyModel
						.getDeskManagedObject();
				if (dependencyToMo != null) {
					DeskManagedObjectModel linkedMoModel = dependencyToMo.getDeskManagedObject();
					if (linkedMoModel != null) {
						linkedManagedObject = managedObjects.get(linkedMoModel.getDeskManagedObjectName());
					}
				}
				if (linkedManagedObject != null) {
					// Link dependency to managed object
					designer.link(dependency, linkedManagedObject);
				}

				// Link dependency to external managed object
				SectionObject linkedObject = null;
				DeskManagedObjectDependencyToExternalManagedObjectModel dependencyToExtMo = dependencyModel
						.getExternalManagedObject();
				if (dependencyToExtMo != null) {
					ExternalManagedObjectModel extMoModel = dependencyToExtMo.getExternalManagedObject();
					if (extMoModel != null) {
						linkedObject = sectionObjects.get(extMoModel.getExternalManagedObjectName());
					}
				}
				if (linkedObject != null) {
					// Link dependency to external managed object
					designer.link(dependency, linkedObject);
				}
			}
		}

		// Add the functions, keeping registry of the functions
		Map<String, SectionFunction> functions = new HashMap<String, SectionFunction>();
		for (FunctionNamespaceModel namespaceModel : desk.getFunctionNamespaces()) {

			// Add the namespace
			SectionFunctionNamespace namespace = designer.addSectionFunctionNamespace(
					namespaceModel.getFunctionNamespaceName(), namespaceModel.getManagedFunctionSourceClassName());
			for (PropertyModel property : namespaceModel.getProperties()) {
				namespace.addProperty(property.getName(), property.getValue());
			}

			// Add the functions for the namespace
			for (ManagedFunctionModel managedFunctionModel : namespaceModel.getManagedFunctions()) {
				for (ManagedFunctionToFunctionModel conn : managedFunctionModel.getFunctions()) {
					FunctionModel functionModel = conn.getFunction();
					if (functionModel != null) {
						// Add the function for the namespace and register
						String functionName = functionModel.getFunctionName();
						SectionFunction function = namespace.addSectionFunction(functionName,
								functionModel.getManagedFunctionName());
						functions.put(functionName, function);
					}
				}
			}
		}

		// Link the flows/objects/escalations (as all links registered)
		for (FunctionModel functionModel : desk.getFunctions()) {

			// Obtain the function for the function model
			String functionName = functionModel.getFunctionName();
			SectionFunction function = functions.get(functionName);
			if (function == null) {
				continue; // function not linked to namespace
			}

			// Obtain the managed function for the function
			ManagedFunctionModel managedFunctionModel = null;
			ManagedFunctionToFunctionModel managedFunctionToFunction = functionModel.getManagedFunction();
			if (managedFunctionToFunction != null) {
				managedFunctionModel = managedFunctionToFunction.getManagedFunction();
			}
			if (managedFunctionModel != null) {
				// Link in the objects for the managed functions
				for (ManagedFunctionObjectModel managedFunctionObjectModel : managedFunctionModel
						.getManagedFunctionObjects()) {

					// Obtain the function object
					String objectName = managedFunctionObjectModel.getObjectName();
					FunctionObject functionObject = function.getFunctionObject(objectName);

					// Determine if object is a parameter
					if (managedFunctionObjectModel.getIsParameter()) {
						functionObject.flagAsParameter();
						continue; // flagged as parameter
					}

					// Determine if link object to external managed object
					SectionObject linkedSectionObject = null;
					ManagedFunctionObjectToExternalManagedObjectModel objectToExtMo = managedFunctionObjectModel
							.getExternalManagedObject();
					if (objectToExtMo != null) {
						ExternalManagedObjectModel linkedExtMo = objectToExtMo.getExternalManagedObject();
						if (linkedExtMo != null) {
							// Obtain the linked section object
							linkedSectionObject = sectionObjects.get(linkedExtMo.getExternalManagedObjectName());
						}
					}
					if (linkedSectionObject != null) {
						// Link the object to its section object
						designer.link(functionObject, linkedSectionObject);
					}

					// Determine if link object to managed object
					SectionManagedObject linkedManagedObject = null;
					ManagedFunctionObjectToDeskManagedObjectModel objectToMo = managedFunctionObjectModel
							.getDeskManagedObject();
					if (objectToMo != null) {
						DeskManagedObjectModel linkedMo = objectToMo.getDeskManagedObject();
						if (linkedMo != null) {
							linkedManagedObject = managedObjects.get(linkedMo.getDeskManagedObjectName());
						}
					}
					if (linkedManagedObject != null) {
						// Link the object to its managed object
						designer.link(functionObject, linkedManagedObject);
					}
				}
			}

			// Link in the flows for the function
			for (FunctionFlowModel functionFlowModel : functionModel.getFunctionFlows()) {

				// Obtain the function flow
				String flowName = functionFlowModel.getFlowName();
				FunctionFlow functionFlow = function.getFunctionFlow(flowName);

				// Determine if link flow to another function
				SectionFunction linkedFunction = null;
				boolean isSpawnThreadState = false;
				FunctionFlowToFunctionModel flowToFunction = functionFlowModel.getFunction();
				if (flowToFunction != null) {
					FunctionModel linkedFunctionModel = flowToFunction.getFunction();
					if (linkedFunctionModel != null) {
						// Obtain the linked function and whether spawn
						linkedFunction = functions.get(linkedFunctionModel.getFunctionName());
						isSpawnThreadState = flowToFunction.getIsSpawnThreadState();
					}
				}
				if (linkedFunction != null) {
					// Link the flow to its function
					designer.link(functionFlow, linkedFunction, isSpawnThreadState);
					continue;
				}

				// Determine if link flow to external flow
				SectionOutput linkedSectionOutput = null;
				FunctionFlowToExternalFlowModel flowToExtFlow = functionFlowModel.getExternalFlow();
				if (flowToExtFlow != null) {
					ExternalFlowModel linkedExtFlow = flowToExtFlow.getExternalFlow();
					if (linkedExtFlow != null) {
						// Obtain the linked flow and instigation strategy
						linkedSectionOutput = sectionOutputs.get(linkedExtFlow.getExternalFlowName());
						isSpawnThreadState = flowToExtFlow.getIsSpawnThreadState();
					}
				}
				if (linkedSectionOutput != null) {
					// Link the flow to section output
					designer.link(functionFlow, linkedSectionOutput, isSpawnThreadState);
				}
			}

			// Determine if link function to next function
			SectionFunction nextFunction = null;
			FunctionToNextFunctionModel functionToNextFunction = functionModel.getNextFunction();
			if (functionToNextFunction != null) {
				FunctionModel nextFunctionModel = functionToNextFunction.getNextFunction();
				if (nextFunctionModel != null) {
					nextFunction = functions.get(nextFunctionModel.getFunctionName());
				}
			}
			if (nextFunction != null) {
				// Link the function to its next function
				designer.link(function, nextFunction);
			}

			// Determine if link function to next external flow
			SectionOutput nextSectionOutput = null;
			FunctionToNextExternalFlowModel functionToNextExtFlow = functionModel.getNextExternalFlow();
			if (functionToNextExtFlow != null) {
				ExternalFlowModel nextExtFlow = functionToNextExtFlow.getNextExternalFlow();
				if (nextExtFlow != null) {
					nextSectionOutput = sectionOutputs.get(nextExtFlow.getExternalFlowName());
				}
			}
			if (nextSectionOutput != null) {
				// Link the function to its next section output
				designer.link(function, nextSectionOutput);
			}

			// Link in the escalations for the function
			for (FunctionEscalationModel functionEscalationModel : functionModel.getFunctionEscalations()) {

				// Obtain the function escalation
				String escalationTypeName = functionEscalationModel.getEscalationType();
				FunctionFlow functionEscalation = function.getFunctionEscalation(escalationTypeName);

				// Determine if link escalation to another function
				SectionFunction linkedFunction = null;
				FunctionEscalationToFunctionModel escalationToFunction = functionEscalationModel.getFunction();
				if (escalationToFunction != null) {
					FunctionModel linkedFunctionModel = escalationToFunction.getFunction();
					if (linkedFunctionModel != null) {
						linkedFunction = functions.get(linkedFunctionModel.getFunctionName());
					}
				}
				if (linkedFunction != null) {
					// Link the escalation to its function
					designer.link(functionEscalation, linkedFunction, false);
				}

				// Determine if link escalation to section output
				SectionOutput linkedSectionOutput = null;
				FunctionEscalationToExternalFlowModel escalationToExtFlow = functionEscalationModel.getExternalFlow();
				if (escalationToExtFlow != null) {
					ExternalFlowModel linkedExtFlow = escalationToExtFlow.getExternalFlow();
					if (linkedExtFlow != null) {
						linkedSectionOutput = sectionOutputs.get(linkedExtFlow.getExternalFlowName());
					}
				}
				if (linkedSectionOutput != null) {
					// Link the escalation to its section output
					designer.link(functionEscalation, linkedSectionOutput, false);
				}
			}
		}

		// Add the public functions as inputs and link to functions
		for (FunctionModel function : desk.getFunctions()) {
			if (function.getIsPublic()) {

				// Obtain the managed function
				ManagedFunctionModel managedFunction = null;
				ManagedFunctionToFunctionModel conn = function.getManagedFunction();
				if (conn != null) {
					managedFunction = conn.getManagedFunction();
				}
				if (managedFunction == null) {
					designer.addIssue("Function " + function.getFunctionName() + " not linked to a managed function");
					continue; // must have managed function
				}

				// Determine the parameter type from the managed function
				String parameterType = null;
				for (ManagedFunctionObjectModel managedFunctionObject : managedFunction.getManagedFunctionObjects()) {
					if (managedFunctionObject.getIsParameter()) {
						// TODO handle two parameters to function
						parameterType = managedFunctionObject.getObjectType();
					}
				}

				// Add the section input and register
				String functionName = function.getFunctionName();
				SectionInput sectionInput = designer.addSectionInput(functionName, parameterType);

				// Obtain the section function and link input to function
				SectionFunction sectionFunction = functions.get(functionName);
				designer.link(sectionInput, sectionFunction);
			}
		}

		// Link managed object source flows to functions/section outputs
		for (DeskManagedObjectSourceModel mosModel : desk.getDeskManagedObjectSources()) {

			// Obtain the managed object source
			SectionManagedObjectSource mos = managedObjectSources.get(mosModel.getDeskManagedObjectSourceName());
			if (mos == null) {
				continue; // should always have
			}

			// Link flows to functions/section outputs
			for (DeskManagedObjectSourceFlowModel mosFlowModel : mosModel.getDeskManagedObjectSourceFlows()) {

				// Obtain the managed object source flow
				ManagedObjectFlow mosFlow = mos.getManagedObjectFlow(mosFlowModel.getDeskManagedObjectSourceFlowName());

				// Link managed object source flow to function
				SectionFunction linkedFunction = null;
				DeskManagedObjectSourceFlowToFunctionModel flowToFunction = mosFlowModel.getFunction();
				if (flowToFunction != null) {
					FunctionModel functionModel = flowToFunction.getFunction();
					if (functionModel != null) {
						linkedFunction = functions.get(functionModel.getFunctionName());
					}
				}
				if (linkedFunction != null) {
					// Link managed object source flow to function
					designer.link(mosFlow, linkedFunction);
				}

				// Link managed object source flow to external flow
				SectionOutput linkedOutput = null;
				DeskManagedObjectSourceFlowToExternalFlowModel flowToOutput = mosFlowModel.getExternalFlow();
				if (flowToOutput != null) {
					ExternalFlowModel extOutput = flowToOutput.getExternalFlow();
					if (extOutput != null) {
						linkedOutput = sectionOutputs.get(extOutput.getExternalFlowName());
					}
				}
				if (linkedOutput != null) {
					// Link managed object source flow to external flow
					designer.link(mosFlow, linkedOutput);
				}
			}
		}
	}

	/**
	 * Obtains the {@link ManagedObjectScope} from the managed object scope
	 * name.
	 * 
	 * @param managedObjectScope
	 *            Name of the {@link ManagedObjectScope}.
	 * @param designer
	 *            {@link SectionDesigner}.
	 * @param managedObjectName
	 *            Name of the {@link SectionManagedObjectModel}.
	 * @return {@link ManagedObjectScope} or <code>null</code> with issue
	 *         reported to the {@link SectionDesigner}.
	 */
	private ManagedObjectScope getManagedObjectScope(String managedObjectScope, SectionDesigner designer,
			String managedObjectName) {

		// Obtain the managed object scope
		if (DeskChanges.PROCESS_MANAGED_OBJECT_SCOPE.equals(managedObjectScope)) {
			return ManagedObjectScope.PROCESS;
		} else if (DeskChanges.THREAD_MANAGED_OBJECT_SCOPE.equals(managedObjectScope)) {
			return ManagedObjectScope.THREAD;
		} else if (DeskChanges.FUNCTION_MANAGED_OBJECT_SCOPE.equals(managedObjectScope)) {
			return ManagedObjectScope.FUNCTION;
		}

		// Unknown scope if at this point
		designer.addIssue(
				"Unknown managed object scope " + managedObjectScope + " for managed object " + managedObjectName);
		return null;
	}

}