/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2012 Daniel Sagenschneider
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
package net.officefloor.launch.woof;

import java.io.InputStream;

import net.officefloor.model.impl.repository.ModelRepositoryImpl;
import net.officefloor.model.impl.repository.inputstream.InputStreamConfigurationItem;
import net.officefloor.model.woof.PropertyModel;
import net.officefloor.model.woof.WoofChanges;
import net.officefloor.model.woof.WoofModel;
import net.officefloor.model.woof.WoofRepository;
import net.officefloor.model.woof.WoofRepositoryImpl;
import net.officefloor.model.woof.WoofSectionInputModel;
import net.officefloor.model.woof.WoofSectionModel;
import net.officefloor.model.woof.WoofTemplateExtensionModel;
import net.officefloor.model.woof.WoofTemplateModel;
import net.officefloor.plugin.woof.gwt.GwtWoofTemplateExtensionService;

/**
 * Loads the {@link WoofDevelopmentConfiguration} from the project.
 * 
 * @author Daniel Sagenschneider
 */
public class WoofDevelopmentConfigurationLoader {

	/**
	 * Loads the {@link WoofDevelopmentConfiguration} from the WoOF
	 * configuration file.
	 * 
	 * @param woofModelConfiguration
	 *            {@link InputStream} to WoOF configuration file (typically
	 *            <code>application.woof</code>). {@link InputStream} to enable
	 *            loading from {@link ClassLoader}.
	 * @return {@link WoofDevelopmentConfiguration}.
	 * @throws Exception
	 *             If fails to load the {@link WoofDevelopmentConfiguration}.
	 */
	public static WoofDevelopmentConfiguration loadConfiguration(
			InputStream woofModelConfiguration) throws Exception {

		// Retrieve the WoOF model
		WoofRepository woofRepository = new WoofRepositoryImpl(
				new ModelRepositoryImpl());
		WoofModel woof = woofRepository
				.retrieveWoOF(new InputStreamConfigurationItem(
						woofModelConfiguration));

		// Load the WoOF development configuration
		WoofDevelopmentConfiguration configuration = new WoofDevelopmentConfiguration();

		// Load template configuration
		for (WoofTemplateModel template : woof.getWoofTemplates()) {

			// Include the template URI
			String templateUri = template.getUri();
			if ((templateUri != null) && (templateUri.trim().length() > 0)) {
				// Template URI provided so include
				String startupUrl = transformUriToStartupUrl(templateUri);
				configuration.addStartupUrl(startupUrl);
			}

			// Include GWT modules
			for (WoofTemplateExtensionModel extension : template
					.getExtensions()) {

				// Determine if GWT extension
				String extensionAlias = extension.getExtensionClassName();
				if (!(GwtWoofTemplateExtensionService.EXTENSION_ALIAS
						.equals(extensionAlias))) {
					continue; // ignore non-GWT extension
				}

				// Obtain the GWT module from properties
				for (PropertyModel property : extension.getProperties()) {
					if (WoofChanges.PROPERTY_GWT_MODULE_PATH.equals(property
							.getName())) {
						String gwtModuleName = transformGwtModulePathToName(property
								.getValue());
						configuration.addGwtModuleName(gwtModuleName);
					}
				}
			}
		}

		// Load section configuration
		for (WoofSectionModel section : woof.getWoofSections()) {
			for (WoofSectionInputModel sectionInput : section.getInputs()) {

				// Include the section input URI
				String sectionInputUri = sectionInput.getUri();
				if ((sectionInputUri != null)
						&& (sectionInputUri.trim().length() > 0)) {
					// Section INput URI provided so include
					String startupUrl = transformUriToStartupUrl(sectionInputUri);
					configuration.addStartupUrl(startupUrl);
				}
			}
		}

		// Return the development configuration
		return configuration;
	}

	/**
	 * Transforms the URI to the startup URL.
	 * 
	 * @param uri
	 *            URI to transform.
	 * @return Startup URL.
	 */
	private static String transformUriToStartupUrl(String uri) {
		return (uri.startsWith("/") ? uri : "/" + uri);
	}

	/**
	 * Transforms the GWT module path to GWT module name.
	 * 
	 * @param gwtModulePath
	 *            GWT module path.
	 * @return GWT module name.
	 */
	private static String transformGwtModulePathToName(String gwtModulePath) {
		String moduleName = gwtModulePath;

		// Strip off extension
		final String EXTENSION = ".gwt.xml";
		if (moduleName.endsWith(EXTENSION)) {
			moduleName = moduleName.substring(0, moduleName.length()
					- EXTENSION.length());
		}

		// Transform path into package naming
		moduleName = moduleName.replace('/', '.');

		// Return the module name
		return moduleName;
	}

	/**
	 * All access via static methods.
	 */
	private WoofDevelopmentConfigurationLoader() {
	}

}