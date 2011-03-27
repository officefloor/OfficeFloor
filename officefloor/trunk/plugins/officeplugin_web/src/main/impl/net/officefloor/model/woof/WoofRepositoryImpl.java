/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2011 Daniel Sagenschneider
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
package net.officefloor.model.woof;

import net.officefloor.compile.impl.util.DoubleKeyMap;
import net.officefloor.model.repository.ConfigurationItem;
import net.officefloor.model.repository.ModelRepository;

/**
 * {@link WoofRepository} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class WoofRepositoryImpl implements WoofRepository {

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
	public WoofRepositoryImpl(ModelRepository modelRepository) {
		this.modelRepository = modelRepository;
	}

	/*
	 * ======================= WoofRepository ==========================
	 */

	@Override
	public WoofModel retrieveWoOF(ConfigurationItem configuration)
			throws Exception {

		// Load the WoOF from the configuration
		WoofModel woof = this.modelRepository.retrieve(new WoofModel(),
				configuration);

		// Create the set of Section Inputs
		DoubleKeyMap<String, String, WoofSectionInputModel> sectionInputs = new DoubleKeyMap<String, String, WoofSectionInputModel>();
		for (WoofSectionModel section : woof.getWoofSections()) {
			for (WoofSectionInputModel input : section.getInputs()) {
				sectionInputs.put(section.getWoofSectionName(),
						input.getWoofSectionInputName(), input);
			}
		}

		// Connect Template Output to Section Input
		for (WoofTemplateModel template : woof.getWoofTemplates()) {
			for (WoofTemplateOutputModel templateOutput : template.getOutputs()) {
				WoofTemplateOutputToWoofSectionInputModel conn = templateOutput
						.getWoofSectionInput();
				if (conn != null) {
					WoofSectionInputModel sectionInput = sectionInputs.get(
							conn.getSectionName(), conn.getInputName());
					if (sectionInput != null) {
						conn.setWoofTemplateOutput(templateOutput);
						conn.setWoofSectionInput(sectionInput);
						conn.connect();
					}
				}
			}
		}

		// Return the WoOF
		return woof;
	}

	@Override
	public void storeWoOF(WoofModel woof, ConfigurationItem configuration)
			throws Exception {

		// Specify section inputs for template output
		for (WoofSectionModel section : woof.getWoofSections()) {
			for (WoofSectionInputModel input : section.getInputs()) {
				for (WoofTemplateOutputToWoofSectionInputModel conn : input
						.getWoofTemplateOutputs()) {
					conn.setSectionName(section.getWoofSectionName());
					conn.setInputName(input.getWoofSectionInputName());
				}
			}
		}

		// Store the WoOF
		this.modelRepository.store(woof, configuration);
	}

}