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
package net.officefloor.woof.model.woof;

import java.util.HashMap;
import java.util.Map;

import net.officefloor.compile.impl.util.DoubleKeyMap;
import net.officefloor.configuration.ConfigurationItem;
import net.officefloor.configuration.WritableConfigurationItem;
import net.officefloor.model.repository.ModelRepository;
import net.officefloor.woof.model.woof.WoofExceptionModel;
import net.officefloor.woof.model.woof.WoofExceptionToWoofResourceModel;
import net.officefloor.woof.model.woof.WoofExceptionToWoofSectionInputModel;
import net.officefloor.woof.model.woof.WoofExceptionToWoofTemplateModel;
import net.officefloor.woof.model.woof.WoofModel;
import net.officefloor.woof.model.woof.WoofRepository;
import net.officefloor.woof.model.woof.WoofResourceModel;
import net.officefloor.woof.model.woof.WoofSectionInputModel;
import net.officefloor.woof.model.woof.WoofSectionModel;
import net.officefloor.woof.model.woof.WoofSectionOutputModel;
import net.officefloor.woof.model.woof.WoofSectionOutputToWoofResourceModel;
import net.officefloor.woof.model.woof.WoofSectionOutputToWoofSectionInputModel;
import net.officefloor.woof.model.woof.WoofSectionOutputToWoofSecurityModel;
import net.officefloor.woof.model.woof.WoofSectionOutputToWoofTemplateModel;
import net.officefloor.woof.model.woof.WoofSecurityModel;
import net.officefloor.woof.model.woof.WoofSecurityOutputModel;
import net.officefloor.woof.model.woof.WoofSecurityOutputToWoofResourceModel;
import net.officefloor.woof.model.woof.WoofSecurityOutputToWoofSectionInputModel;
import net.officefloor.woof.model.woof.WoofSecurityOutputToWoofTemplateModel;
import net.officefloor.woof.model.woof.WoofStartModel;
import net.officefloor.woof.model.woof.WoofStartToWoofSectionInputModel;
import net.officefloor.woof.model.woof.WoofTemplateModel;
import net.officefloor.woof.model.woof.WoofTemplateOutputModel;
import net.officefloor.woof.model.woof.WoofTemplateOutputToWoofResourceModel;
import net.officefloor.woof.model.woof.WoofTemplateOutputToWoofSectionInputModel;
import net.officefloor.woof.model.woof.WoofTemplateOutputToWoofSecurityModel;
import net.officefloor.woof.model.woof.WoofTemplateOutputToWoofTemplateModel;

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
	public void retrieveWoof(WoofModel woof, ConfigurationItem configuration) throws Exception {

		// Load the WoOF from the configuration
		this.modelRepository.retrieve(woof, configuration);

		// Create the set of Section Inputs
		DoubleKeyMap<String, String, WoofSectionInputModel> sectionInputs = new DoubleKeyMap<String, String, WoofSectionInputModel>();
		for (WoofSectionModel section : woof.getWoofSections()) {
			for (WoofSectionInputModel input : section.getInputs()) {
				sectionInputs.put(section.getWoofSectionName(), input.getWoofSectionInputName(), input);
			}
		}

		// Create the set of Templates
		Map<String, WoofTemplateModel> templates = new HashMap<String, WoofTemplateModel>();
		for (WoofTemplateModel template : woof.getWoofTemplates()) {
			templates.put(template.getApplicationPath(), template);
		}

		// Create the set of Securities
		Map<String, WoofSecurityModel> securities = new HashMap<String, WoofSecurityModel>();
		for (WoofSecurityModel security : woof.getWoofSecurities()) {
			securities.put(security.getHttpSecurityName(), security);
		}

		// Create the set of Resources
		Map<String, WoofResourceModel> resources = new HashMap<String, WoofResourceModel>();
		for (WoofResourceModel resource : woof.getWoofResources()) {
			resources.put(resource.getResourcePath(), resource);
		}

		// Connect Template Outputs
		for (WoofTemplateModel template : woof.getWoofTemplates()) {
			for (WoofTemplateOutputModel templateOutput : template.getOutputs()) {

				// Connect Template Output to Section Input
				WoofTemplateOutputToWoofSectionInputModel connSection = templateOutput.getWoofSectionInput();
				if (connSection != null) {
					WoofSectionInputModel sectionInput = sectionInputs.get(connSection.getSectionName(),
							connSection.getInputName());
					if (sectionInput != null) {
						connSection.setWoofTemplateOutput(templateOutput);
						connSection.setWoofSectionInput(sectionInput);
						connSection.connect();
					}
				}

				// Connect Template Output to Template
				WoofTemplateOutputToWoofTemplateModel connTemplate = templateOutput.getWoofTemplate();
				if (connTemplate != null) {
					WoofTemplateModel target = templates.get(connTemplate.getApplicationPath());
					if (target != null) {
						connTemplate.setWoofTemplateOutput(templateOutput);
						connTemplate.setWoofTemplate(target);
						connTemplate.connect();
					}
				}

				// Connect Template Output to Security
				WoofTemplateOutputToWoofSecurityModel connAccess = templateOutput.getWoofSecurity();
				if (connAccess != null) {
					WoofSecurityModel security = securities.get(connAccess.getHttpSecurityName());
					if (security != null) {
						connAccess.setWoofTemplateOutput(templateOutput);
						connAccess.setWoofSecurity(security);
						connAccess.connect();
					}
				}

				// Connect Template Output to Resource
				WoofTemplateOutputToWoofResourceModel connResource = templateOutput.getWoofResource();
				if (connResource != null) {
					WoofResourceModel resource = resources.get(connResource.getResourcePath());
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
				WoofSectionOutputToWoofSectionInputModel connSection = sectionOutput.getWoofSectionInput();
				if (connSection != null) {
					WoofSectionInputModel sectionInput = sectionInputs.get(connSection.getSectionName(),
							connSection.getInputName());
					if (sectionInput != null) {
						connSection.setWoofSectionOutput(sectionOutput);
						connSection.setWoofSectionInput(sectionInput);
						connSection.connect();
					}
				}

				// Connect Section Output to Template
				WoofSectionOutputToWoofTemplateModel connTemplate = sectionOutput.getWoofTemplate();
				if (connTemplate != null) {
					WoofTemplateModel target = templates.get(connTemplate.getApplicationPath());
					if (target != null) {
						connTemplate.setWoofSectionOutput(sectionOutput);
						connTemplate.setWoofTemplate(target);
						connTemplate.connect();
					}
				}

				// Connection Section Output to Security
				WoofSectionOutputToWoofSecurityModel connSecurity = sectionOutput.getWoofSecurity();
				if (connSecurity != null) {
					WoofSecurityModel security = securities.get(connSecurity.getHttpSecurityName());
					if (security != null) {
						connSecurity.setWoofSectionOutput(sectionOutput);
						connSecurity.setWoofSecurity(security);
						connSecurity.connect();
					}
				}

				// Connect Section Output to Resource
				WoofSectionOutputToWoofResourceModel connResource = sectionOutput.getWoofResource();
				if (connResource != null) {
					WoofResourceModel resource = resources.get(connResource.getResourcePath());
					if (resource != null) {
						connResource.setWoofSectionOutput(sectionOutput);
						connResource.setWoofResource(resource);
						connResource.connect();
					}
				}
			}
		}

		// Connect securities
		for (WoofSecurityModel security : woof.getWoofSecurities()) {

			// Connect the Security Outputs
			for (WoofSecurityOutputModel securityOutput : security.getOutputs()) {

				// Connect Security Output to Section Input
				WoofSecurityOutputToWoofSectionInputModel connSection = securityOutput.getWoofSectionInput();
				if (connSection != null) {
					WoofSectionInputModel sectionInput = sectionInputs.get(connSection.getSectionName(),
							connSection.getInputName());
					if (sectionInput != null) {
						connSection.setWoofSecurityOutput(securityOutput);
						connSection.setWoofSectionInput(sectionInput);
						connSection.connect();
					}
				}

				// Connect Security Output to Template
				WoofSecurityOutputToWoofTemplateModel connTemplate = securityOutput.getWoofTemplate();
				if (connTemplate != null) {
					WoofTemplateModel template = templates.get(connTemplate.getApplicationPath());
					if (template != null) {
						connTemplate.setWoofSecurityOutput(securityOutput);
						connTemplate.setWoofTemplate(template);
						connTemplate.connect();
					}
				}

				// Connect Security Output to Resource
				WoofSecurityOutputToWoofResourceModel connResource = securityOutput.getWoofResource();
				if (connResource != null) {
					WoofResourceModel resource = resources.get(connResource.getResourcePath());
					if (resource != null) {
						connResource.setWoofSecurityOutput(securityOutput);
						connResource.setWoofResource(resource);
						connResource.connect();
					}
				}
			}
		}

		// Connect Exceptions
		for (WoofExceptionModel exception : woof.getWoofExceptions()) {

			// Connect Exception to Section Input
			WoofExceptionToWoofSectionInputModel connSection = exception.getWoofSectionInput();
			if (connSection != null) {
				WoofSectionInputModel sectionInput = sectionInputs.get(connSection.getSectionName(),
						connSection.getInputName());
				if (sectionInput != null) {
					connSection.setWoofException(exception);
					connSection.setWoofSectionInput(sectionInput);
					connSection.connect();
				}
			}

			// Connect Exception to Template
			WoofExceptionToWoofTemplateModel connTemplate = exception.getWoofTemplate();
			if (connTemplate != null) {
				WoofTemplateModel target = templates.get(connTemplate.getApplicationPath());
				if (target != null) {
					connTemplate.setWoofException(exception);
					connTemplate.setWoofTemplate(target);
					connTemplate.connect();
				}
			}

			// Connect Exception to Resource
			WoofExceptionToWoofResourceModel connResource = exception.getWoofResource();
			if (connResource != null) {
				WoofResourceModel resource = resources.get(connResource.getResourcePath());
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
			WoofStartToWoofSectionInputModel connSection = start.getWoofSectionInput();
			if (connSection != null) {
				WoofSectionInputModel sectionInput = sectionInputs.get(connSection.getSectionName(),
						connSection.getInputName());
				if (sectionInput != null) {
					connSection.setWoofStart(start);
					connSection.setWoofSectionInput(sectionInput);
					connSection.connect();
				}
			}
		}
	}

	@Override
	public void storeWoof(WoofModel woof, WritableConfigurationItem configuration) throws Exception {

		// Specify section inputs
		for (WoofSectionModel section : woof.getWoofSections()) {
			for (WoofSectionInputModel input : section.getInputs()) {

				// Specify section inputs for section output
				for (WoofSectionOutputToWoofSectionInputModel conn : input.getWoofSectionOutputs()) {
					conn.setSectionName(section.getWoofSectionName());
					conn.setInputName(input.getWoofSectionInputName());
				}

				// Specify section inputs for template output
				for (WoofTemplateOutputToWoofSectionInputModel conn : input.getWoofTemplateOutputs()) {
					conn.setSectionName(section.getWoofSectionName());
					conn.setInputName(input.getWoofSectionInputName());
				}

				// Specify section inputs for security output
				for (WoofSecurityOutputToWoofSectionInputModel conn : input.getWoofSecurityOutputs()) {
					conn.setSectionName(section.getWoofSectionName());
					conn.setInputName(input.getWoofSectionInputName());
				}

				// Specify section inputs for exception
				for (WoofExceptionToWoofSectionInputModel conn : input.getWoofExceptions()) {
					conn.setSectionName(section.getWoofSectionName());
					conn.setInputName(input.getWoofSectionInputName());
				}

				// Specify section inputs for start
				for (WoofStartToWoofSectionInputModel conn : input.getWoofStarts()) {
					conn.setSectionName(section.getWoofSectionName());
					conn.setInputName(input.getWoofSectionInputName());
				}
			}
		}

		// Specify templates
		for (WoofTemplateModel template : woof.getWoofTemplates()) {

			// Specify templates for section output
			for (WoofSectionOutputToWoofTemplateModel conn : template.getWoofSectionOutputs()) {
				conn.setApplicationPath(template.getApplicationPath());
			}

			// Specify templates for template output
			for (WoofTemplateOutputToWoofTemplateModel conn : template.getWoofTemplateOutputs()) {
				conn.setApplicationPath(template.getApplicationPath());
			}

			// Specify templates for security output
			for (WoofSecurityOutputToWoofTemplateModel conn : template.getWoofSecurityOutputs()) {
				conn.setApplicationPath(template.getApplicationPath());
			}

			// Specify templates for exception
			for (WoofExceptionToWoofTemplateModel conn : template.getWoofExceptions()) {
				conn.setApplicationPath(template.getApplicationPath());
			}
		}

		// Specify security inputs (if security available)
		for (WoofSecurityModel security : woof.getWoofSecurities()) {

			// Specify access input for section output
			for (WoofSectionOutputToWoofSecurityModel conn : security.getWoofSectionOutputs()) {
				conn.setHttpSecurityName(security.getHttpSecurityName());
			}

			// Specify access input for template output
			for (WoofTemplateOutputToWoofSecurityModel conn : security.getWoofTemplateOutputs()) {
				conn.setHttpSecurityName(security.getHttpSecurityName());
			}
		}

		// Specify resources
		for (WoofResourceModel resource : woof.getWoofResources()) {

			// Specify resources for section output
			for (WoofSectionOutputToWoofResourceModel conn : resource.getWoofSectionOutputs()) {
				conn.setResourcePath(resource.getResourcePath());
			}

			// Specify resources for template output
			for (WoofTemplateOutputToWoofResourceModel conn : resource.getWoofTemplateOutputs()) {
				conn.setResourcePath(resource.getResourcePath());
			}

			// Specify resources for security output
			for (WoofSecurityOutputToWoofResourceModel conn : resource.getWoofSecurityOutputs()) {
				conn.setResourcePath(resource.getResourcePath());
			}

			// Specify resources for exception
			for (WoofExceptionToWoofResourceModel conn : resource.getWoofExceptions()) {
				conn.setResourcePath(resource.getResourcePath());
			}
		}

		// Store the WoOF
		this.modelRepository.store(woof, configuration);
	}

}