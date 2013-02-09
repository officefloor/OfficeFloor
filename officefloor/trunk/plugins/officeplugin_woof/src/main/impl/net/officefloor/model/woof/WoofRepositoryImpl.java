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
package net.officefloor.model.woof;

import java.util.HashMap;
import java.util.Map;

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

		// Create the set of Templates
		Map<String, WoofTemplateModel> templates = new HashMap<String, WoofTemplateModel>();
		for (WoofTemplateModel template : woof.getWoofTemplates()) {
			templates.put(template.getWoofTemplateName(), template);
		}

		// Create the set of Access Inputs
		Map<String, WoofAccessInputModel> accessInputs = new HashMap<String, WoofAccessInputModel>();
		WoofAccessModel access = woof.getWoofAccess();
		if (access != null) {
			for (WoofAccessInputModel input : access.getInputs()) {
				accessInputs.put(input.getWoofAccessInputName(), input);
			}
		}

		// Create the set of Resources
		Map<String, WoofResourceModel> resources = new HashMap<String, WoofResourceModel>();
		for (WoofResourceModel resource : woof.getWoofResources()) {
			resources.put(resource.getWoofResourceName(), resource);
		}

		// Connect Template Outputs
		for (WoofTemplateModel template : woof.getWoofTemplates()) {
			for (WoofTemplateOutputModel templateOutput : template.getOutputs()) {

				// Connect Template Output to Section Input
				WoofTemplateOutputToWoofSectionInputModel connSection = templateOutput
						.getWoofSectionInput();
				if (connSection != null) {
					WoofSectionInputModel sectionInput = sectionInputs.get(
							connSection.getSectionName(),
							connSection.getInputName());
					if (sectionInput != null) {
						connSection.setWoofTemplateOutput(templateOutput);
						connSection.setWoofSectionInput(sectionInput);
						connSection.connect();
					}
				}

				// Connect Template Output to Template
				WoofTemplateOutputToWoofTemplateModel connTemplate = templateOutput
						.getWoofTemplate();
				if (connTemplate != null) {
					WoofTemplateModel target = templates.get(connTemplate
							.getTemplateName());
					if (target != null) {
						connTemplate.setWoofTemplateOutput(templateOutput);
						connTemplate.setWoofTemplate(target);
						connTemplate.connect();
					}
				}

				// Connect Template Output to Access Input
				WoofTemplateOutputToWoofAccessInputModel connAccess = templateOutput
						.getWoofAccessInput();
				if (connAccess != null) {
					WoofAccessInputModel accessInput = accessInputs
							.get(connAccess.getInputName());
					if (accessInput != null) {
						connAccess.setWoofTemplateOutput(templateOutput);
						connAccess.setWoofAccessInput(accessInput);
						connAccess.connect();
					}
				}

				// Connect Template Output to Resource
				WoofTemplateOutputToWoofResourceModel connResource = templateOutput
						.getWoofResource();
				if (connResource != null) {
					WoofResourceModel resource = resources.get(connResource
							.getResourceName());
					if (resource != null) {
						connResource.setWoofTemplateOutput(templateOutput);
						connResource.setWoofResource(resource);
						connResource.connect();
					}
				}
			}
		}

		// Connect Section Outputs
		for (WoofSectionModel section : woof.getWoofSections()) {
			for (WoofSectionOutputModel sectionOutput : section.getOutputs()) {

				// Connect Section Output to Section Input
				WoofSectionOutputToWoofSectionInputModel connSection = sectionOutput
						.getWoofSectionInput();
				if (connSection != null) {
					WoofSectionInputModel sectionInput = sectionInputs.get(
							connSection.getSectionName(),
							connSection.getInputName());
					if (sectionInput != null) {
						connSection.setWoofSectionOutput(sectionOutput);
						connSection.setWoofSectionInput(sectionInput);
						connSection.connect();
					}
				}

				// Connect Section Output to Template
				WoofSectionOutputToWoofTemplateModel connTemplate = sectionOutput
						.getWoofTemplate();
				if (connTemplate != null) {
					WoofTemplateModel target = templates.get(connTemplate
							.getTemplateName());
					if (target != null) {
						connTemplate.setWoofSectionOutput(sectionOutput);
						connTemplate.setWoofTemplate(target);
						connTemplate.connect();
					}
				}

				// Connection Section Output to Access Input
				WoofSectionOutputToWoofAccessInputModel connAccess = sectionOutput
						.getWoofAccessInput();
				if (connAccess != null) {
					WoofAccessInputModel accessInput = accessInputs
							.get(connAccess.getInputName());
					if (accessInput != null) {
						connAccess.setWoofSectionOutput(sectionOutput);
						connAccess.setWoofAccessInput(accessInput);
						connAccess.connect();
					}
				}

				// Connect Section Output to Resource
				WoofSectionOutputToWoofResourceModel connResource = sectionOutput
						.getWoofResource();
				if (connResource != null) {
					WoofResourceModel resource = resources.get(connResource
							.getResourceName());
					if (resource != null) {
						connResource.setWoofSectionOutput(sectionOutput);
						connResource.setWoofResource(resource);
						connResource.connect();
					}
				}
			}
		}

		// Connect access (if available)
		if (access != null) {

			// Connect the Access Outputs
			for (WoofAccessOutputModel accessOutput : access.getOutputs()) {

				// Connect Access Output to Section Input
				WoofAccessOutputToWoofSectionInputModel connSection = accessOutput
						.getWoofSectionInput();
				if (connSection != null) {
					WoofSectionInputModel sectionInput = sectionInputs.get(
							connSection.getSectionName(),
							connSection.getInputName());
					if (sectionInput != null) {
						connSection.setWoofAccessOutput(accessOutput);
						connSection.setWoofSectionInput(sectionInput);
						connSection.connect();
					}
				}

				// Connect Access Output to Template
				WoofAccessOutputToWoofTemplateModel connTemplate = accessOutput
						.getWoofTemplate();
				if (connTemplate != null) {
					WoofTemplateModel template = templates.get(connTemplate
							.getTemplateName());
					if (template != null) {
						connTemplate.setWoofAccessOutput(accessOutput);
						connTemplate.setWoofTemplate(template);
						connTemplate.connect();
					}
				}

				// Connect Access Output to Resource
				WoofAccessOutputToWoofResourceModel connResource = accessOutput
						.getWoofResource();
				if (connResource != null) {
					WoofResourceModel resource = resources.get(connResource
							.getResourceName());
					if (resource != null) {
						connResource.setWoofAccessOutput(accessOutput);
						connResource.setWoofResource(resource);
						connResource.connect();
					}
				}
			}
		}

		// Connect Exceptions
		for (WoofExceptionModel exception : woof.getWoofExceptions()) {

			// Connect Exception to Section Input
			WoofExceptionToWoofSectionInputModel connSection = exception
					.getWoofSectionInput();
			if (connSection != null) {
				WoofSectionInputModel sectionInput = sectionInputs.get(
						connSection.getSectionName(),
						connSection.getInputName());
				if (sectionInput != null) {
					connSection.setWoofException(exception);
					connSection.setWoofSectionInput(sectionInput);
					connSection.connect();
				}
			}

			// Connect Exception to Template
			WoofExceptionToWoofTemplateModel connTemplate = exception
					.getWoofTemplate();
			if (connTemplate != null) {
				WoofTemplateModel target = templates.get(connTemplate
						.getTemplateName());
				if (target != null) {
					connTemplate.setWoofException(exception);
					connTemplate.setWoofTemplate(target);
					connTemplate.connect();
				}
			}

			// Connect Exception to Resource
			WoofExceptionToWoofResourceModel connResource = exception
					.getWoofResource();
			if (connResource != null) {
				WoofResourceModel resource = resources.get(connResource
						.getResourceName());
				if (resource != null) {
					connResource.setWoofException(exception);
					connResource.setWoofResource(resource);
					connResource.connect();
				}
			}
		}

		// Connect Starts
		for (WoofStartModel start : woof.getWoofStarts()) {

			// Connect Start to Section Input
			WoofStartToWoofSectionInputModel connSection = start
					.getWoofSectionInput();
			if (connSection != null) {
				WoofSectionInputModel sectionInput = sectionInputs.get(
						connSection.getSectionName(),
						connSection.getInputName());
				if (sectionInput != null) {
					connSection.setWoofStart(start);
					connSection.setWoofSectionInput(sectionInput);
					connSection.connect();
				}
			}
		}

		// Return the WoOF
		return woof;
	}

	@Override
	public void storeWoOF(WoofModel woof, ConfigurationItem configuration)
			throws Exception {

		// Specify section inputs
		for (WoofSectionModel section : woof.getWoofSections()) {
			for (WoofSectionInputModel input : section.getInputs()) {

				// Specify section inputs for section output
				for (WoofSectionOutputToWoofSectionInputModel conn : input
						.getWoofSectionOutputs()) {
					conn.setSectionName(section.getWoofSectionName());
					conn.setInputName(input.getWoofSectionInputName());
				}

				// Specify section inputs for template output
				for (WoofTemplateOutputToWoofSectionInputModel conn : input
						.getWoofTemplateOutputs()) {
					conn.setSectionName(section.getWoofSectionName());
					conn.setInputName(input.getWoofSectionInputName());
				}

				// Specify section inputs for access output
				for (WoofAccessOutputToWoofSectionInputModel conn : input
						.getWoofAccessOutputs()) {
					conn.setSectionName(section.getWoofSectionName());
					conn.setInputName(input.getWoofSectionInputName());
				}

				// Specify section inputs for exception
				for (WoofExceptionToWoofSectionInputModel conn : input
						.getWoofExceptions()) {
					conn.setSectionName(section.getWoofSectionName());
					conn.setInputName(input.getWoofSectionInputName());
				}

				// Specify section inputs for start
				for (WoofStartToWoofSectionInputModel conn : input
						.getWoofStarts()) {
					conn.setSectionName(section.getWoofSectionName());
					conn.setInputName(input.getWoofSectionInputName());
				}
			}
		}

		// Specify templates
		for (WoofTemplateModel template : woof.getWoofTemplates()) {

			// Specify templates for section output
			for (WoofSectionOutputToWoofTemplateModel conn : template
					.getWoofSectionOutputs()) {
				conn.setTemplateName(template.getWoofTemplateName());
			}

			// Specify templates for template output
			for (WoofTemplateOutputToWoofTemplateModel conn : template
					.getWoofTemplateOutputs()) {
				conn.setTemplateName(template.getWoofTemplateName());
			}

			// Specify templates for access output
			for (WoofAccessOutputToWoofTemplateModel conn : template
					.getWoofAccessOutputs()) {
				conn.setTemplateName(template.getWoofTemplateName());
			}

			// Specify templates for exception
			for (WoofExceptionToWoofTemplateModel conn : template
					.getWoofExceptions()) {
				conn.setTemplateName(template.getWoofTemplateName());
			}
		}

		// Specify access inputs (if access available)
		WoofAccessModel access = woof.getWoofAccess();
		if (access != null) {
			for (WoofAccessInputModel input : access.getInputs()) {

				// Specify access input for section output
				for (WoofSectionOutputToWoofAccessInputModel conn : input
						.getWoofSectionOutputs()) {
					conn.setInputName(input.getWoofAccessInputName());
				}

				// Specify access input for template output
				for (WoofTemplateOutputToWoofAccessInputModel conn : input
						.getWoofTemplateOutputs()) {
					conn.setInputName(input.getWoofAccessInputName());
				}
			}
		}

		// Specify resources
		for (WoofResourceModel resource : woof.getWoofResources()) {

			// Specify resources for section output
			for (WoofSectionOutputToWoofResourceModel conn : resource
					.getWoofSectionOutputs()) {
				conn.setResourceName(resource.getWoofResourceName());
			}

			// Specify resources for template output
			for (WoofTemplateOutputToWoofResourceModel conn : resource
					.getWoofTemplateOutputs()) {
				conn.setResourceName(resource.getWoofResourceName());
			}

			// Specify resources for access output
			for (WoofAccessOutputToWoofResourceModel conn : resource
					.getWoofAccessOutputs()) {
				conn.setResourceName(resource.getWoofResourceName());
			}

			// Specify resources for exception
			for (WoofExceptionToWoofResourceModel conn : resource
					.getWoofExceptions()) {
				conn.setResourceName(resource.getWoofResourceName());
			}
		}

		// Store the WoOF
		this.modelRepository.store(woof, configuration);
	}

}