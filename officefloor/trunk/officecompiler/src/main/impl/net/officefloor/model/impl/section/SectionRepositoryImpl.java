/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2009 Daniel Sagenschneider
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
import net.officefloor.model.section.SectionModel;
import net.officefloor.model.section.SectionRepository;
import net.officefloor.model.section.SubSectionInputModel;
import net.officefloor.model.section.SubSectionModel;
import net.officefloor.model.section.SubSectionObjectModel;
import net.officefloor.model.section.SubSectionObjectToExternalManagedObjectModel;
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

		// Create the map of external flows
		Map<String, ExternalFlowModel> externalFlows = new HashMap<String, ExternalFlowModel>();
		for (ExternalFlowModel externalFlow : section.getExternalFlows()) {
			externalFlows.put(externalFlow.getExternalFlowName(), externalFlow);
		}

		// Connect the outputs to the inputs
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

		// Specify output to external flow
		for (ExternalFlowModel extFlow : section.getExternalFlows()) {
			for (SubSectionOutputToExternalFlowModel conn : extFlow
					.getSubSectionOutputs()) {
				conn.setExternalFlowName(extFlow.getExternalFlowName());
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

		// Store the section into the configuration
		this.modelRepository.store(section, configuration);
	}

}