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
import net.officefloor.model.section.ExternalFlowModel;
import net.officefloor.model.section.ExternalManagedObjectModel;
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
import net.officefloor.model.section.SectionRepository;
import net.officefloor.model.section.SubSectionInputModel;
import net.officefloor.model.section.SubSectionModel;
import net.officefloor.model.section.SubSectionObjectModel;
import net.officefloor.model.section.SubSectionObjectToExternalManagedObjectModel;
import net.officefloor.model.section.SubSectionObjectToSectionManagedObjectModel;
import net.officefloor.model.section.SubSectionOutputModel;
import net.officefloor.model.section.SubSectionOutputToExternalFlowModel;
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
	public SectionModel retrieveSection(ConfigurationItem configuration)
			throws Exception {

		// Load the section from the configuration
		SectionModel section = this.modelRepository.retrieve(
				new SectionModel(), configuration);

		// Create the map of inputs
		DoubleKeyMap<String, String, SubSectionInputModel> inputs = new DoubleKeyMap<String, String, SubSectionInputModel>();
		for (SubSectionModel subSection : section.getSubSections()) {
			for (SubSectionInputModel input : subSection.getSubSectionInputs()) {
				inputs.put(subSection.getSubSectionName(), input
						.getSubSectionInputName(), input);
			}
		}

		// Connect the outputs to the inputs
		for (SubSectionModel subSection : section.getSubSections()) {
			for (SubSectionOutputModel output : subSection
					.getSubSectionOutputs()) {
				SubSectionOutputToSubSectionInputModel conn = output
						.getSubSectionInput();
				if (conn != null) {
					SubSectionInputModel input = inputs
							.get(conn.getSubSectionName(), conn
									.getSubSectionInputName());
					if (input != null) {
						conn.setSubSectionOutput(output);
						conn.setSubSectionInput(input);
						conn.connect();
					}
				}
			}
		}

		// Connect the managed object source flows to the inputs
		for (SectionManagedObjectSourceModel mos : section
				.getSectionManagedObjectSources()) {
			for (SectionManagedObjectSourceFlowModel mosFlow : mos
					.getSectionManagedObjectSourceFlows()) {
				SectionManagedObjectSourceFlowToSubSectionInputModel conn = mosFlow
						.getSubSectionInput();
				if (conn != null) {
					SubSectionInputModel input = inputs
							.get(conn.getSubSectionName(), conn
									.getSubSectionInputName());
					if (input != null) {
						conn.setSectionManagedObjectSourceFlow(mosFlow);
						conn.setSubSectionInput(input);
						conn.connect();
					}
				}
			}
		}

		// Create the map of external flows
		Map<String, ExternalFlowModel> externalFlows = new HashMap<String, ExternalFlowModel>();
		for (ExternalFlowModel externalFlow : section.getExternalFlows()) {
			externalFlows.put(externalFlow.getExternalFlowName(), externalFlow);
		}

		// Connect the outputs to the external flows
		for (SubSectionModel subSection : section.getSubSections()) {
			for (SubSectionOutputModel output : subSection
					.getSubSectionOutputs()) {
				SubSectionOutputToExternalFlowModel conn = output
						.getExternalFlow();
				if (conn != null) {
					ExternalFlowModel externalFlow = externalFlows.get(conn
							.getExternalFlowName());
					if (externalFlow != null) {
						conn.setSubSectionOutput(output);
						conn.setExternalFlow(externalFlow);
						conn.connect();
					}
				}
			}
		}

		// Connect the managed object source flows to the external flows
		for (SectionManagedObjectSourceModel mos : section
				.getSectionManagedObjectSources()) {
			for (SectionManagedObjectSourceFlowModel mosFlow : mos
					.getSectionManagedObjectSourceFlows()) {
				SectionManagedObjectSourceFlowToExternalFlowModel conn = mosFlow
						.getExternalFlow();
				if (conn != null) {
					ExternalFlowModel externalFlow = externalFlows.get(conn
							.getExternalFlowName());
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
		for (SectionManagedObjectSourceModel mos : section
				.getSectionManagedObjectSources()) {
			managedObjectSources.put(mos.getSectionManagedObjectSourceName(),
					mos);
		}

		// Connect the managed objects to their managed object sources
		for (SectionManagedObjectModel mo : section.getSectionManagedObjects()) {
			SectionManagedObjectToSectionManagedObjectSourceModel conn = mo
					.getSectionManagedObjectSource();
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

		// Create the map of external managed objects
		Map<String, ExternalManagedObjectModel> externalMos = new HashMap<String, ExternalManagedObjectModel>();
		for (ExternalManagedObjectModel externalMo : section
				.getExternalManagedObjects()) {
			externalMos.put(externalMo.getExternalManagedObjectName(),
					externalMo);
		}

		// Connect the objects to external managed objects
		for (SubSectionModel subSection : section.getSubSections()) {
			for (SubSectionObjectModel object : subSection
					.getSubSectionObjects()) {
				SubSectionObjectToExternalManagedObjectModel conn = object
						.getExternalManagedObject();
				if (conn != null) {
					ExternalManagedObjectModel externalMo = externalMos
							.get(conn.getExternalManagedObjectName());
					if (externalMo != null) {
						conn.setSubSectionObject(object);
						conn.setExternalManagedObject(externalMo);
						conn.connect();
					}
				}
			}
		}

		// Create the map of managed objects
		Map<String, SectionManagedObjectModel> managedObjects = new HashMap<String, SectionManagedObjectModel>();
		for (SectionManagedObjectModel mo : section.getSectionManagedObjects()) {
			managedObjects.put(mo.getSectionManagedObjectName(), mo);
		}

		// Connect the objects to managed objects
		for (SubSectionModel subSection : section.getSubSections()) {
			for (SubSectionObjectModel object : subSection
					.getSubSectionObjects()) {
				SubSectionObjectToSectionManagedObjectModel conn = object
						.getSectionManagedObject();
				if (conn != null) {
					SectionManagedObjectModel mo = managedObjects.get(conn
							.getSectionManagedObjectName());
					if (mo != null) {
						conn.setSubSectionObject(object);
						conn.setSectionManagedObject(mo);
						conn.connect();
					}
				}
			}
		}

		// Connect the dependencies to the external managed objects
		for (SectionManagedObjectModel mo : section.getSectionManagedObjects()) {
			for (SectionManagedObjectDependencyModel dependency : mo
					.getSectionManagedObjectDependencies()) {
				SectionManagedObjectDependencyToExternalManagedObjectModel conn = dependency
						.getExternalManagedObject();
				if (conn != null) {
					ExternalManagedObjectModel extMo = externalMos.get(conn
							.getExternalManagedObjectName());
					if (extMo != null) {
						conn.setSectionManagedObjectDependency(dependency);
						conn.setExternalManagedObject(extMo);
						conn.connect();
					}
				}
			}
		}

		// Connect the dependencies to the managed objects
		for (SectionManagedObjectModel mo : section.getSectionManagedObjects()) {
			for (SectionManagedObjectDependencyModel dependency : mo
					.getSectionManagedObjectDependencies()) {
				SectionManagedObjectDependencyToSectionManagedObjectModel conn = dependency
						.getSectionManagedObject();
				if (conn != null) {
					SectionManagedObjectModel dependentMo = managedObjects
							.get(conn.getSectionManagedObjectName());
					if (dependentMo != null) {
						conn.setSectionManagedObjectDependency(dependency);
						conn.setSectionManagedObject(dependentMo);
						conn.connect();
					}
				}
			}
		}

		// Return the section
		return section;
	}

	@Override
	public void storeSection(SectionModel section,
			ConfigurationItem configuration) throws Exception {

		// Specify output to input
		for (SubSectionModel subSection : section.getSubSections()) {
			for (SubSectionInputModel input : subSection.getSubSectionInputs()) {
				for (SubSectionOutputToSubSectionInputModel conn : input
						.getSubSectionOutputs()) {
					conn.setSubSectionName(subSection.getSubSectionName());
					conn.setSubSectionInputName(input.getSubSectionInputName());
				}
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

		// Specify output to external flow
		for (ExternalFlowModel extFlow : section.getExternalFlows()) {
			for (SubSectionOutputToExternalFlowModel conn : extFlow
					.getSubSectionOutputs()) {
				conn.setExternalFlowName(extFlow.getExternalFlowName());
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
		for (SectionManagedObjectSourceModel mos : section
				.getSectionManagedObjectSources()) {
			for (SectionManagedObjectToSectionManagedObjectSourceModel conn : mos
					.getSectionManagedObjects()) {
				conn.setSectionManagedObjectSourceName(mos
						.getSectionManagedObjectSourceName());
			}
		}

		// Specify object to external managed object
		for (ExternalManagedObjectModel extMo : section
				.getExternalManagedObjects()) {
			for (SubSectionObjectToExternalManagedObjectModel conn : extMo
					.getSubSectionObjects()) {
				conn.setExternalManagedObjectName(extMo
						.getExternalManagedObjectName());
			}
		}

		// Specify object to managed object
		for (SectionManagedObjectModel mo : section.getSectionManagedObjects()) {
			for (SubSectionObjectToSectionManagedObjectModel conn : mo
					.getSubSectionObjects()) {
				conn.setSectionManagedObjectName(mo
						.getSectionManagedObjectName());
			}
		}

		// Specify dependency to external managed object
		for (ExternalManagedObjectModel extMo : section
				.getExternalManagedObjects()) {
			for (SectionManagedObjectDependencyToExternalManagedObjectModel conn : extMo
					.getDependentSectionManagedObjects()) {
				conn.setExternalManagedObjectName(extMo
						.getExternalManagedObjectName());
			}
		}

		// Specify dependency to managed object
		for (SectionManagedObjectModel mo : section.getSectionManagedObjects()) {
			for (SectionManagedObjectDependencyToSectionManagedObjectModel conn : mo
					.getDependentSectionManagedObjects()) {
				conn.setSectionManagedObjectName(mo
						.getSectionManagedObjectName());
			}
		}

		// Store the section into the configuration
		this.modelRepository.store(section, configuration);
	}

}