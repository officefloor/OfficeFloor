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
import net.officefloor.compile.impl.util.DoubleKeyMap;
import net.officefloor.compile.spi.section.ManagedObjectDependency;
import net.officefloor.compile.spi.section.ManagedObjectFlow;
import net.officefloor.compile.spi.section.SectionDesigner;
import net.officefloor.compile.spi.section.SectionInput;
import net.officefloor.compile.spi.section.SectionManagedObject;
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
import net.officefloor.frame.api.build.OfficeFloorIssues.AssetType;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.model.impl.repository.ModelRepositoryImpl;
import net.officefloor.model.impl.repository.inputstream.InputStreamConfigurationItem;
import net.officefloor.model.section.ExternalFlowModel;
import net.officefloor.model.section.ExternalManagedObjectModel;
import net.officefloor.model.section.PropertyModel;
import net.officefloor.model.section.SectionChanges;
import net.officefloor.model.section.SectionManagedObjectDependencyModel;
import net.officefloor.model.section.SectionManagedObjectDependencyToExternalManagedObjectModel;
import net.officefloor.model.section.SectionManagedObjectDependencyToSectionManagedObjectModel;
import net.officefloor.model.section.SectionManagedObjectModel;
import net.officefloor.model.section.SectionManagedObjectSourceFlowModel;
import net.officefloor.model.section.SectionManagedObjectSourceFlowToExternalFlowModel;
import net.officefloor.model.section.SectionManagedObjectSourceFlowToSubSectionInputModel;
import net.officefloor.model.section.SectionManagedObjectSourceModel;
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
public class SectionModelSectionSource extends AbstractSectionSource implements
		SectionSourceService<SectionModelSectionSource> {

	/*
	 * ==================== SectionSourceService ============================
	 */

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
	public void sourceSection(SectionDesigner designer,
			SectionSourceContext context) throws Exception {

		// Obtain the configuration to the section
		InputStream configuration = context.getResource(context
				.getSectionLocation());
		if (configuration == null) {
			// Must have configuration
			throw new FileNotFoundException("Can not find section '"
					+ context.getSectionLocation() + "'");
		}

		// Retrieve the section model
		SectionModel section = new SectionRepositoryImpl(
				new ModelRepositoryImpl())
				.retrieveSection(new InputStreamConfigurationItem(configuration));

		// Add the external flows as outputs from the section, keeping registry
		Map<String, SectionOutput> sectionOutputs = new HashMap<String, SectionOutput>();
		for (ExternalFlowModel extFlow : section.getExternalFlows()) {

			// Determine if all connected sub section outputs are escalation
			boolean isEscalationOnly = true;
			for (SubSectionOutputToExternalFlowModel conn : extFlow
					.getSubSectionOutputs()) {
				if (!conn.getSubSectionOutput().getEscalationOnly()) {
					// Connected to output which is not escalation only
					isEscalationOnly = false;
				}
			}

			// Obtain the section output name
			String sectionOutputName = extFlow.getExternalFlowName();

			// Add the output and register it
			SectionOutput sectionOutput = designer.addSectionOutput(
					sectionOutputName, extFlow.getArgumentType(),
					isEscalationOnly);
			sectionOutputs.put(sectionOutputName, sectionOutput);
		}

		// Add the external managed objects as objects, keeping registry
		Map<String, SectionObject> sectionObjects = new HashMap<String, SectionObject>();
		for (ExternalManagedObjectModel extMo : section
				.getExternalManagedObjects()) {

			// Obtain the section object name
			String sectionObjectName = extMo.getExternalManagedObjectName();

			// Add the section object and register it
			SectionObject sectionObject = designer.addSectionObject(
					sectionObjectName, extMo.getObjectType());
			sectionObjects.put(sectionObjectName, sectionObject);
		}

		// Add the managed object sources, keeping registry of them
		Map<String, SectionManagedObjectSource> managedObjectSources = new HashMap<String, SectionManagedObjectSource>();
		for (SectionManagedObjectSourceModel mosModel : section
				.getSectionManagedObjectSources()) {

			// Add the managed object source
			String mosName = mosModel.getSectionManagedObjectSourceName();
			SectionManagedObjectSource mos = designer
					.addSectionManagedObjectSource(mosName,
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
					designer.addIssue("Invalid timeout value: " + timeoutValue,
							AssetType.MANAGED_OBJECT, mosName);
				}
			}

			// Register the managed object source
			managedObjectSources.put(mosName, mos);
		}

		// Add the managed objects, keeping registry of them
		Map<String, SectionManagedObject> managedObjects = new HashMap<String, SectionManagedObject>();
		for (SectionManagedObjectModel moModel : section
				.getSectionManagedObjects()) {

			// Obtain the managed object details
			String managedObjectName = moModel.getSectionManagedObjectName();
			ManagedObjectScope managedObjectScope = this.getManagedObjectScope(
					moModel.getManagedObjectScope(), designer,
					managedObjectName);

			// Obtain the managed object source for the managed object
			SectionManagedObjectSource moSource = null;
			SectionManagedObjectToSectionManagedObjectSourceModel moToSource = moModel
					.getSectionManagedObjectSource();
			if (moToSource != null) {
				SectionManagedObjectSourceModel moSourceModel = moToSource
						.getSectionManagedObjectSource();
				if (moSourceModel != null) {
					moSource = managedObjectSources.get(moSourceModel
							.getSectionManagedObjectSourceName());
				}
			}
			if (moSource == null) {
				continue; // must have managed object source
			}

			// Add the managed object and also register it
			SectionManagedObject managedObject = moSource
					.addSectionManagedObject(managedObjectName,
							managedObjectScope);
			managedObjects.put(managedObjectName, managedObject);
		}

		// Link managed object dependencies to managed objects
		for (SectionManagedObjectModel moModel : section
				.getSectionManagedObjects()) {

			// Obtain the managed object
			SectionManagedObject mo = managedObjects.get(moModel
					.getSectionManagedObjectName());
			if (mo == null) {
				continue; // should always have managed object
			}

			// Link managed object dependencies to managed object
			for (SectionManagedObjectDependencyModel dependencyModel : moModel
					.getSectionManagedObjectDependencies()) {

				// Obtain the managed object dependency
				ManagedObjectDependency dependency = mo
						.getManagedObjectDependency(dependencyModel
								.getSectionManagedObjectDependencyName());

				// Link the dependency to managed object
				SectionManagedObject linkedManagedObject = null;
				SectionManagedObjectDependencyToSectionManagedObjectModel dependencyToMo = dependencyModel
						.getSectionManagedObject();
				if (dependencyToMo != null) {
					SectionManagedObjectModel linkedMoModel = dependencyToMo
							.getSectionManagedObject();
					if (linkedMoModel != null) {
						linkedManagedObject = managedObjects.get(linkedMoModel
								.getSectionManagedObjectName());
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
					ExternalManagedObjectModel extMo = dependencyToExtMo
							.getExternalManagedObject();
					if (extMo != null) {
						linkedObject = sectionObjects.get(extMo
								.getExternalManagedObjectName());
					}
				}
				if (linkedObject != null) {
					// Link dependency to section object
					designer.link(dependency, linkedObject);
				}
			}
		}

		// Add the sub sections, keeping registry of sub sections/inputs
		Map<String, SubSection> subSections = new HashMap<String, SubSection>();
		DoubleKeyMap<String, String, SubSectionInput> subSectionInputs = new DoubleKeyMap<String, String, SubSectionInput>();
		for (SubSectionModel subSectionModel : section.getSubSections()) {

			// Add the sub section and register
			String subSectionName = subSectionModel.getSubSectionName();
			SubSection subSection = designer.addSubSection(subSectionName,
					subSectionModel.getSectionSourceClassName(),
					subSectionModel.getSectionLocation());
			subSections.put(subSectionName, subSection);
			for (PropertyModel property : subSectionModel.getProperties()) {
				subSection.addProperty(property.getName(), property.getValue());
			}

			// Add the sub section inputs and register
			for (SubSectionInputModel inputModel : subSectionModel
					.getSubSectionInputs()) {
				String inputName = inputModel.getSubSectionInputName();
				SubSectionInput input = subSection
						.getSubSectionInput(inputName);
				subSectionInputs.put(subSectionName, inputName, input);

				// Determine if public input to section
				if (inputModel.getIsPublic()) {
					// Obtain the public name for the input
					String publicInputName = inputModel.getPublicInputName();
					publicInputName = (CompileUtil.isBlank(publicInputName) ? inputName
							: publicInputName);

					// Add the public input
					SectionInput sectionInput = designer.addSectionInput(
							publicInputName, inputModel.getParameterType());

					// Link the section input to sub section input
					designer.link(sectionInput, input);
				}
			}
		}

		// Add the sub section outputs/objects now that all links available
		for (SubSectionModel subSectionModel : section.getSubSections()) {

			// Obtain the sub section
			String subSectionName = subSectionModel.getSubSectionName();
			SubSection subSection = subSections.get(subSectionName);

			// Add the sub section outputs
			for (SubSectionOutputModel outputModel : subSectionModel
					.getSubSectionOutputs()) {

				// Add the sub section output
				String outputName = outputModel.getSubSectionOutputName();
				SubSectionOutput output = subSection
						.getSubSectionOutput(outputName);

				// Determine if link to a sub section input
				SubSectionInput linkedInput = null;
				SubSectionOutputToSubSectionInputModel outputToInput = outputModel
						.getSubSectionInput();
				if (outputToInput != null) {
					SubSectionInputModel inputModel = outputToInput
							.getSubSectionInput();
					if (inputModel != null) {
						SubSectionModel linkedSubSection = this
								.getSubSectionForInput(section, inputModel);
						linkedInput = subSectionInputs.get(
								linkedSubSection.getSubSectionName(),
								inputModel.getSubSectionInputName());
					}
				}
				if (linkedInput != null) {
					// Link the output to the input
					designer.link(output, linkedInput);
				}

				// Determine if link to section output
				SectionOutput linkedOutput = null;
				SubSectionOutputToExternalFlowModel outputToExternalFlow = outputModel
						.getExternalFlow();
				if (outputToExternalFlow != null) {
					ExternalFlowModel externalFlow = outputToExternalFlow
							.getExternalFlow();
					if (externalFlow != null) {
						linkedOutput = sectionOutputs.get(externalFlow
								.getExternalFlowName());
					}
				}
				if (linkedOutput != null) {
					// Link the output to the section output
					designer.link(output, linkedOutput);
				}
			}

			// Add the sub section objects
			for (SubSectionObjectModel objectModel : subSectionModel
					.getSubSectionObjects()) {

				// Add the sub section object
				String objectName = objectModel.getSubSectionObjectName();
				SubSectionObject object = subSection
						.getSubSectionObject(objectName);

				// Determine if link to a managed object
				SectionManagedObject linkedMo = null;
				SubSectionObjectToSectionManagedObjectModel objectToMo = objectModel
						.getSectionManagedObject();
				if (objectToMo != null) {
					SectionManagedObjectModel moModel = objectToMo
							.getSectionManagedObject();
					if (moModel != null) {
						linkedMo = managedObjects.get(moModel
								.getSectionManagedObjectName());
					}
				}
				if (linkedMo != null) {
					// Link the object to the managed object
					designer.link(object, linkedMo);
				}

				// Determine if link to external managed object
				SectionObject linkedObject = null;
				SubSectionObjectToExternalManagedObjectModel objectToExtMo = objectModel
						.getExternalManagedObject();
				if (objectToExtMo != null) {
					ExternalManagedObjectModel extMo = objectToExtMo
							.getExternalManagedObject();
					if (extMo != null) {
						linkedObject = sectionObjects.get(extMo
								.getExternalManagedObjectName());
					}
				}
				if (linkedObject != null) {
					// Link the object to section object
					designer.link(object, linkedObject);
				}
			}
		}

		// Link managed object source flow to sub section input/external flow
		for (SectionManagedObjectSourceModel mosModel : section
				.getSectionManagedObjectSources()) {

			// Obtain the managed object source
			SectionManagedObjectSource mos = managedObjectSources.get(mosModel
					.getSectionManagedObjectSourceName());
			if (mos == null) {
				continue; // should always have
			}

			// Link mos flow to sub section input/external flow
			for (SectionManagedObjectSourceFlowModel mosFlowModel : mosModel
					.getSectionManagedObjectSourceFlows()) {

				// Obtain the managed object source flow
				ManagedObjectFlow mosFlow = mos
						.getManagedObjectFlow(mosFlowModel
								.getSectionManagedObjectSourceFlowName());

				// Link managed object source flow to sub section input
				SubSectionInput linkedInput = null;
				SectionManagedObjectSourceFlowToSubSectionInputModel flowToInput = mosFlowModel
						.getSubSectionInput();
				if (flowToInput != null) {
					SubSectionInputModel inputModel = flowToInput
							.getSubSectionInput();
					if (inputModel != null) {
						SubSectionModel sectionModel = this
								.getSubSectionForInput(section, inputModel);
						if (sectionModel != null) {
							linkedInput = subSectionInputs.get(
									sectionModel.getSubSectionName(),
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
				SectionManagedObjectSourceFlowToExternalFlowModel flowToExtFlow = mosFlowModel
						.getExternalFlow();
				if (flowToExtFlow != null) {
					ExternalFlowModel extFlow = flowToExtFlow.getExternalFlow();
					if (extFlow != null) {
						linkedOutput = sectionOutputs.get(extFlow
								.getExternalFlowName());
					}
				}
				if (linkedOutput != null) {
					// Link managed object source flow to section output
					designer.link(mosFlow, linkedOutput);
				}
			}
		}
	}

	/**
	 * Obtains the {@link SubSectionModel} containing the input
	 * {@link SubSectionInputModel}.
	 * 
	 * @param section
	 *            {@link SectionModel}.
	 * @param input
	 *            {@link SubSectionInputModel}.
	 * @return {@link SubSectionModel}.
	 */
	private SubSectionModel getSubSectionForInput(SectionModel section,
			SubSectionInputModel input) {

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
	private ManagedObjectScope getManagedObjectScope(String managedObjectScope,
			SectionDesigner designer, String managedObjectName) {

		// Obtain the managed object scope
		if (SectionChanges.PROCESS_MANAGED_OBJECT_SCOPE
				.equals(managedObjectScope)) {
			return ManagedObjectScope.PROCESS;
		} else if (SectionChanges.THREAD_MANAGED_OBJECT_SCOPE
				.equals(managedObjectScope)) {
			return ManagedObjectScope.THREAD;
		} else if (SectionChanges.WORK_MANAGED_OBJECT_SCOPE
				.equals(managedObjectScope)) {
			return ManagedObjectScope.WORK;
		}

		// Unknown scope if at this point
		designer.addIssue("Unknown managed object scope " + managedObjectScope,
				AssetType.MANAGED_OBJECT, managedObjectName);
		return null;
	}

}