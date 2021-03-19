/*-
 * #%L
 * OfficeCompiler
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package net.officefloor.model.impl.section;

import java.util.HashMap;
import java.util.Map;

import net.officefloor.compile.SectionSourceService;
import net.officefloor.compile.SectionSourceServiceFactory;
import net.officefloor.compile.impl.util.CompileUtil;
import net.officefloor.compile.impl.util.DoubleKeyMap;
import net.officefloor.compile.spi.section.FunctionFlow;
import net.officefloor.compile.spi.section.FunctionObject;
import net.officefloor.compile.spi.section.SectionDesigner;
import net.officefloor.compile.spi.section.SectionFunction;
import net.officefloor.compile.spi.section.SectionFunctionNamespace;
import net.officefloor.compile.spi.section.SectionInput;
import net.officefloor.compile.spi.section.SectionManagedObject;
import net.officefloor.compile.spi.section.SectionManagedObjectDependency;
import net.officefloor.compile.spi.section.SectionManagedObjectFlow;
import net.officefloor.compile.spi.section.SectionManagedObjectPool;
import net.officefloor.compile.spi.section.SectionManagedObjectSource;
import net.officefloor.compile.spi.section.SectionObject;
import net.officefloor.compile.spi.section.SectionOutput;
import net.officefloor.compile.spi.section.SubSection;
import net.officefloor.compile.spi.section.SubSectionInput;
import net.officefloor.compile.spi.section.SubSectionObject;
import net.officefloor.compile.spi.section.SubSectionOutput;
import net.officefloor.compile.spi.section.source.SectionSource;
import net.officefloor.compile.spi.section.source.SectionSourceContext;
import net.officefloor.compile.spi.section.source.impl.AbstractSectionSource;
import net.officefloor.configuration.ConfigurationItem;
import net.officefloor.frame.api.source.ServiceContext;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.model.impl.repository.ModelRepositoryImpl;
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
import net.officefloor.model.section.ManagedFunctionObjectToExternalManagedObjectModel;
import net.officefloor.model.section.ManagedFunctionObjectToSectionManagedObjectModel;
import net.officefloor.model.section.ManagedFunctionToFunctionModel;
import net.officefloor.model.section.PropertyModel;
import net.officefloor.model.section.SectionChanges;
import net.officefloor.model.section.SectionManagedObjectDependencyModel;
import net.officefloor.model.section.SectionManagedObjectDependencyToExternalManagedObjectModel;
import net.officefloor.model.section.SectionManagedObjectDependencyToSectionManagedObjectModel;
import net.officefloor.model.section.SectionManagedObjectModel;
import net.officefloor.model.section.SectionManagedObjectPoolModel;
import net.officefloor.model.section.SectionManagedObjectSourceFlowModel;
import net.officefloor.model.section.SectionManagedObjectSourceFlowToExternalFlowModel;
import net.officefloor.model.section.SectionManagedObjectSourceFlowToSubSectionInputModel;
import net.officefloor.model.section.SectionManagedObjectSourceModel;
import net.officefloor.model.section.SectionManagedObjectSourceToSectionManagedObjectPoolModel;
import net.officefloor.model.section.SectionManagedObjectToSectionManagedObjectSourceModel;
import net.officefloor.model.section.SectionModel;
import net.officefloor.model.section.SubSectionInputModel;
import net.officefloor.model.section.SubSectionModel;
import net.officefloor.model.section.SubSectionObjectModel;
import net.officefloor.model.section.SubSectionObjectToExternalManagedObjectModel;
import net.officefloor.model.section.SubSectionObjectToSectionManagedObjectModel;
import net.officefloor.model.section.SubSectionOutputModel;
import net.officefloor.model.section.SubSectionOutputToExternalFlowModel;
import net.officefloor.model.section.SubSectionOutputToSubSectionInputModel;

/**
 * {@link SectionSource} for a {@link SectionModel}.
 * 
 * @author Daniel Sagenschneider
 */
public class SectionModelSectionSource extends AbstractSectionSource
		implements SectionSourceService<SectionModelSectionSource>, SectionSourceServiceFactory {

	/*
	 * ==================== SectionSourceService ============================
	 */

	@Override
	public SectionSourceService<?> createService(ServiceContext context) throws Throwable {
		return this;
	}

	@Override
	public String getSectionSourceAlias() {
		return "SECTION";
	}

	@Override
	public Class<SectionModelSectionSource> getSectionSourceClass() {
		return SectionModelSectionSource.class;
	}

	/*
	 * ================== SectionSource ===========================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
		// No specification
	}

	@Override
	public void sourceSection(SectionDesigner designer, SectionSourceContext context) throws Exception {

		// Retrieve the section model
		ConfigurationItem configuration = context.getConfigurationItem(context.getSectionLocation(), null);
		SectionModel section = new SectionModel();
		new SectionRepositoryImpl(new ModelRepositoryImpl()).retrieveSection(section, configuration);

		// Add the external flows as outputs from the section, keeping registry
		Map<String, SectionOutput> sectionOutputs = new HashMap<String, SectionOutput>();
		for (ExternalFlowModel extFlow : section.getExternalFlows()) {

			// Determine if all connected sub section outputs are escalation
			boolean isEscalationOnly = true;
			for (SubSectionOutputToExternalFlowModel conn : extFlow.getSubSectionOutputs()) {
				if (!conn.getSubSectionOutput().getEscalationOnly()) {
					// Connected to output which is not escalation only
					isEscalationOnly = false;
				}
			}

			// Obtain the section output name
			String sectionOutputName = extFlow.getExternalFlowName();

			// Add the output and register it
			SectionOutput sectionOutput = designer.addSectionOutput(sectionOutputName, extFlow.getArgumentType(),
					isEscalationOnly);
			sectionOutputs.put(sectionOutputName, sectionOutput);
		}

		// Add the external managed objects as objects, keeping registry
		Map<String, SectionObject> sectionObjects = new HashMap<String, SectionObject>();
		for (ExternalManagedObjectModel extMo : section.getExternalManagedObjects()) {

			// Obtain the section object name
			String sectionObjectName = extMo.getExternalManagedObjectName();

			// Add the section object and register it
			SectionObject sectionObject = designer.addSectionObject(sectionObjectName, extMo.getObjectType());
			sectionObjects.put(sectionObjectName, sectionObject);
		}

		// Add the managed object pools, keeping registry of them
		Map<String, SectionManagedObjectPool> sectionManagedObjectPools = new HashMap<>();
		for (SectionManagedObjectPoolModel poolModel : section.getSectionManagedObjectPools()) {

			// Add the managed object pool
			String managedObjectPoolName = poolModel.getSectionManagedObjectPoolName();
			String managedObjectPoolSourceClassName = poolModel.getManagedObjectPoolSourceClassName();
			SectionManagedObjectPool pool = designer.addManagedObjectPool(managedObjectPoolName,
					managedObjectPoolSourceClassName);
			sectionManagedObjectPools.put(managedObjectPoolName, pool);

			// Add properties for the managed object source
			for (PropertyModel property : poolModel.getProperties()) {
				pool.addProperty(property.getName(), property.getValue());
			}
		}

		// Add the managed object sources, keeping registry of them
		Map<String, SectionManagedObjectSource> managedObjectSources = new HashMap<String, SectionManagedObjectSource>();
		for (SectionManagedObjectSourceModel mosModel : section.getSectionManagedObjectSources()) {

			// Add the managed object source
			String mosName = mosModel.getSectionManagedObjectSourceName();
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
					designer.addIssue("Invalid timeout value " + timeoutValue + " for managed object source " + mosName
							+ ". Must be an integer.");
				}
			}

			// Register the managed object source
			managedObjectSources.put(mosName, mos);

			// Determine if pool the managed object
			SectionManagedObjectSourceToSectionManagedObjectPoolModel mosToPool = mosModel
					.getSectionManagedObjectPool();
			if (mosToPool != null) {
				SectionManagedObjectPoolModel poolModel = mosToPool.getSectionManagedObjectPool();
				if (poolModel != null) {
					SectionManagedObjectPool pool = sectionManagedObjectPools
							.get(poolModel.getSectionManagedObjectPoolName());
					designer.link(mos, pool);
				}
			}
		}

		// Add the managed objects, keeping registry of them
		Map<String, SectionManagedObject> managedObjects = new HashMap<String, SectionManagedObject>();
		for (SectionManagedObjectModel moModel : section.getSectionManagedObjects()) {

			// Obtain the managed object details
			String managedObjectName = moModel.getSectionManagedObjectName();
			ManagedObjectScope managedObjectScope = this.getManagedObjectScope(moModel.getManagedObjectScope(),
					designer, managedObjectName);

			// Obtain the managed object source for the managed object
			SectionManagedObjectSource moSource = null;
			SectionManagedObjectToSectionManagedObjectSourceModel moToSource = moModel.getSectionManagedObjectSource();
			if (moToSource != null) {
				SectionManagedObjectSourceModel moSourceModel = moToSource.getSectionManagedObjectSource();
				if (moSourceModel != null) {
					moSource = managedObjectSources.get(moSourceModel.getSectionManagedObjectSourceName());
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

		// Link managed object dependencies to managed objects
		for (SectionManagedObjectModel moModel : section.getSectionManagedObjects()) {

			// Obtain the managed object
			SectionManagedObject mo = managedObjects.get(moModel.getSectionManagedObjectName());
			if (mo == null) {
				continue; // should always have managed object
			}

			// Link managed object dependencies to managed object
			for (SectionManagedObjectDependencyModel dependencyModel : moModel.getSectionManagedObjectDependencies()) {

				// Obtain the managed object dependency
				SectionManagedObjectDependency dependency = mo
						.getSectionManagedObjectDependency(dependencyModel.getSectionManagedObjectDependencyName());

				// Link the dependency to managed object
				SectionManagedObject linkedManagedObject = null;
				SectionManagedObjectDependencyToSectionManagedObjectModel dependencyToMo = dependencyModel
						.getSectionManagedObject();
				if (dependencyToMo != null) {
					SectionManagedObjectModel linkedMoModel = dependencyToMo.getSectionManagedObject();
					if (linkedMoModel != null) {
						linkedManagedObject = managedObjects.get(linkedMoModel.getSectionManagedObjectName());
					}
				}
				if (linkedManagedObject != null) {
					// Link dependency to managed object
					designer.link(dependency, linkedManagedObject);
				}

				// Link the dependency to section object
				SectionObject linkedObject = null;
				SectionManagedObjectDependencyToExternalManagedObjectModel dependencyToExtMo = dependencyModel
						.getExternalManagedObject();
				if (dependencyToExtMo != null) {
					ExternalManagedObjectModel extMo = dependencyToExtMo.getExternalManagedObject();
					if (extMo != null) {
						linkedObject = sectionObjects.get(extMo.getExternalManagedObjectName());
					}
				}
				if (linkedObject != null) {
					// Link dependency to section object
					designer.link(dependency, linkedObject);
				}
			}
		}

		// Add the functions, keeping registry of the functions
		Map<String, SectionFunction> functions = new HashMap<String, SectionFunction>();
		for (FunctionNamespaceModel namespaceModel : section.getFunctionNamespaces()) {

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

		// Add the sub sections, keeping registry of sub sections/inputs
		Map<String, SubSection> subSections = new HashMap<String, SubSection>();
		DoubleKeyMap<String, String, SubSectionInput> subSectionInputs = new DoubleKeyMap<String, String, SubSectionInput>();
		for (SubSectionModel subSectionModel : section.getSubSections()) {

			// Add the sub section and register
			String subSectionName = subSectionModel.getSubSectionName();
			SubSection subSection = designer.addSubSection(subSectionName, subSectionModel.getSectionSourceClassName(),
					subSectionModel.getSectionLocation());
			subSections.put(subSectionName, subSection);
			for (PropertyModel property : subSectionModel.getProperties()) {
				subSection.addProperty(property.getName(), property.getValue());
			}

			// Add the sub section inputs and register
			for (SubSectionInputModel inputModel : subSectionModel.getSubSectionInputs()) {
				String inputName = inputModel.getSubSectionInputName();
				SubSectionInput input = subSection.getSubSectionInput(inputName);
				subSectionInputs.put(subSectionName, inputName, input);

				// Determine if public input to section
				if (inputModel.getIsPublic()) {
					// Obtain the public name for the input
					String publicInputName = inputModel.getPublicInputName();
					publicInputName = (CompileUtil.isBlank(publicInputName) ? inputName : publicInputName);

					// Add the public input
					SectionInput sectionInput = designer.addSectionInput(publicInputName,
							inputModel.getParameterType());

					// Link the section input to sub section input
					designer.link(sectionInput, input);
				}
			}
		}

		// Link the flows/objects/escalations (as all links registered)
		for (FunctionModel functionModel : section.getFunctions()) {

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
					ManagedFunctionObjectToSectionManagedObjectModel objectToMo = managedFunctionObjectModel
							.getSectionManagedObject();
					if (objectToMo != null) {
						SectionManagedObjectModel linkedMo = objectToMo.getSectionManagedObject();
						if (linkedMo != null) {
							linkedManagedObject = managedObjects.get(linkedMo.getSectionManagedObjectName());
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
					continue;
				}

				// TODO determine link flow to sub section input
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

			// TODO determine link next sub section input

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
					continue;
				}

				// TODO determine link escalation to sub section input
			}
		}

		// Add the sub section outputs/objects now that all links available
		for (SubSectionModel subSectionModel : section.getSubSections()) {

			// Obtain the sub section
			String subSectionName = subSectionModel.getSubSectionName();
			SubSection subSection = subSections.get(subSectionName);

			// Add the sub section outputs
			for (SubSectionOutputModel outputModel : subSectionModel.getSubSectionOutputs()) {

				// Add the sub section output
				String outputName = outputModel.getSubSectionOutputName();
				SubSectionOutput output = subSection.getSubSectionOutput(outputName);

				// Determine if link to a sub section input
				SubSectionInput linkedInput = null;
				SubSectionOutputToSubSectionInputModel outputToInput = outputModel.getSubSectionInput();
				if (outputToInput != null) {
					SubSectionInputModel inputModel = outputToInput.getSubSectionInput();
					if (inputModel != null) {
						SubSectionModel linkedSubSection = this.getSubSectionForInput(section, inputModel);
						linkedInput = subSectionInputs.get(linkedSubSection.getSubSectionName(),
								inputModel.getSubSectionInputName());
					}
				}
				if (linkedInput != null) {
					// Link the output to the input
					designer.link(output, linkedInput);
					continue;
				}

				// Determine if link to external flow
				SectionOutput linkedOutput = null;
				SubSectionOutputToExternalFlowModel outputToExternalFlow = outputModel.getExternalFlow();
				if (outputToExternalFlow != null) {
					ExternalFlowModel externalFlow = outputToExternalFlow.getExternalFlow();
					if (externalFlow != null) {
						linkedOutput = sectionOutputs.get(externalFlow.getExternalFlowName());
					}
				}
				if (linkedOutput != null) {
					// Link the output to the section output
					designer.link(output, linkedOutput);
					continue;
				}

				// TODO determine if link to function
			}

			// Add the sub section objects
			for (SubSectionObjectModel objectModel : subSectionModel.getSubSectionObjects()) {

				// Add the sub section object
				String objectName = objectModel.getSubSectionObjectName();
				SubSectionObject object = subSection.getSubSectionObject(objectName);

				// Determine if link to a managed object
				SectionManagedObject linkedMo = null;
				SubSectionObjectToSectionManagedObjectModel objectToMo = objectModel.getSectionManagedObject();
				if (objectToMo != null) {
					SectionManagedObjectModel moModel = objectToMo.getSectionManagedObject();
					if (moModel != null) {
						linkedMo = managedObjects.get(moModel.getSectionManagedObjectName());
					}
				}
				if (linkedMo != null) {
					// Link the object to the managed object
					designer.link(object, linkedMo);
					continue;
				}

				// Determine if link to external managed object
				SectionObject linkedObject = null;
				SubSectionObjectToExternalManagedObjectModel objectToExtMo = objectModel.getExternalManagedObject();
				if (objectToExtMo != null) {
					ExternalManagedObjectModel extMo = objectToExtMo.getExternalManagedObject();
					if (extMo != null) {
						linkedObject = sectionObjects.get(extMo.getExternalManagedObjectName());
					}
				}
				if (linkedObject != null) {
					// Link the object to section object
					designer.link(object, linkedObject);
					continue;
				}
			}
		}

		// Link managed object source flow to sub section input/external flow
		for (SectionManagedObjectSourceModel mosModel : section.getSectionManagedObjectSources()) {

			// Obtain the managed object source
			SectionManagedObjectSource mos = managedObjectSources.get(mosModel.getSectionManagedObjectSourceName());
			if (mos == null) {
				continue; // should always have
			}

			// Link mos flow to sub section input/external flow
			for (SectionManagedObjectSourceFlowModel mosFlowModel : mosModel.getSectionManagedObjectSourceFlows()) {

				// Obtain the managed object source flow
				SectionManagedObjectFlow mosFlow = mos
						.getSectionManagedObjectFlow(mosFlowModel.getSectionManagedObjectSourceFlowName());

				// Link managed object source flow to sub section input
				SubSectionInput linkedInput = null;
				SectionManagedObjectSourceFlowToSubSectionInputModel flowToInput = mosFlowModel.getSubSectionInput();
				if (flowToInput != null) {
					SubSectionInputModel inputModel = flowToInput.getSubSectionInput();
					if (inputModel != null) {
						SubSectionModel sectionModel = this.getSubSectionForInput(section, inputModel);
						if (sectionModel != null) {
							linkedInput = subSectionInputs.get(sectionModel.getSubSectionName(),
									inputModel.getSubSectionInputName());
						}
					}
				}
				if (linkedInput != null) {
					// Link managed object source flow to sub section input
					designer.link(mosFlow, linkedInput);
				}

				// Link managed object source flow to section output
				SectionOutput linkedOutput = null;
				SectionManagedObjectSourceFlowToExternalFlowModel flowToExtFlow = mosFlowModel.getExternalFlow();
				if (flowToExtFlow != null) {
					ExternalFlowModel extFlow = flowToExtFlow.getExternalFlow();
					if (extFlow != null) {
						linkedOutput = sectionOutputs.get(extFlow.getExternalFlowName());
					}
				}
				if (linkedOutput != null) {
					// Link managed object source flow to section output
					designer.link(mosFlow, linkedOutput);
				}
			}
		}

		// Add the public functions as inputs and link to functions
		for (FunctionModel function : section.getFunctions()) {
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
	}

	/**
	 * Obtains the {@link SubSectionModel} containing the input
	 * {@link SubSectionInputModel}.
	 * 
	 * @param section {@link SectionModel}.
	 * @param input   {@link SubSectionInputModel}.
	 * @return {@link SubSectionModel}.
	 */
	private SubSectionModel getSubSectionForInput(SectionModel section, SubSectionInputModel input) {

		// Search the for input (which is contained in the target sub section)
		for (SubSectionModel subSection : section.getSubSections()) {
			for (SubSectionInputModel check : subSection.getSubSectionInputs()) {
				if (check == input) {
					// Found the input and subsequently the containing sub
					// section
					return subSection;
				}
			}
		}

		// No sub section if at this point
		return null;
	}

	/**
	 * Obtains the {@link ManagedObjectScope} from the managed object scope name.
	 * 
	 * @param managedObjectScope Name of the {@link ManagedObjectScope}.
	 * @param designer           {@link SectionDesigner}.
	 * @param managedObjectName  Name of the {@link SectionManagedObjectModel}.
	 * @return {@link ManagedObjectScope} or <code>null</code> with issue reported
	 *         to the {@link SectionDesigner}.
	 */
	private ManagedObjectScope getManagedObjectScope(String managedObjectScope, SectionDesigner designer,
			String managedObjectName) {

		// Obtain the managed object scope
		if (SectionChanges.PROCESS_MANAGED_OBJECT_SCOPE.equals(managedObjectScope)) {
			return ManagedObjectScope.PROCESS;
		} else if (SectionChanges.THREAD_MANAGED_OBJECT_SCOPE.equals(managedObjectScope)) {
			return ManagedObjectScope.THREAD;
		} else if (SectionChanges.FUNCTION_MANAGED_OBJECT_SCOPE.equals(managedObjectScope)) {
			return ManagedObjectScope.FUNCTION;
		}

		// Unknown scope if at this point
		designer.addIssue(
				"Unknown managed object scope " + managedObjectScope + " for managed object " + managedObjectName);
		return null;
	}

}
