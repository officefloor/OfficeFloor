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
package net.officefloor.plugin.woof;

import net.officefloor.model.repository.ConfigurationContext;
import net.officefloor.model.repository.ConfigurationItem;
import net.officefloor.model.woof.PropertyModel;
import net.officefloor.model.woof.WoofModel;
import net.officefloor.model.woof.WoofRepository;
import net.officefloor.model.woof.WoofSectionModel;
import net.officefloor.model.woof.WoofTemplateModel;
import net.officefloor.plugin.autowire.AutoWireSection;
import net.officefloor.plugin.web.http.server.WebAutoWireApplication;

/**
 * {@link WoofLoader} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class WoofLoaderImpl implements WoofLoader {

	/**
	 * {@link ClassLoader}.
	 */
	private final ClassLoader classLoader;

	/**
	 * {@link ConfigurationContext}.
	 */
	private final ConfigurationContext context;

	/**
	 * {@link WoofRepository}.
	 */
	private final WoofRepository repository;

	/**
	 * Initiate.
	 * 
	 * @param classLoader
	 *            {@link ClassLoader}.
	 * @param context
	 *            {@link ConfigurationContext}.
	 * @param repository
	 *            {@link WoofRepository}.
	 */
	public WoofLoaderImpl(ClassLoader classLoader,
			ConfigurationContext context, WoofRepository repository) {
		this.classLoader = classLoader;
		this.context = context;
		this.repository = repository;
	}

	/*
	 * ======================= WoofLoader ===========================
	 */

	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void loadWoofConfiguration(String woofLocation,
			WebAutoWireApplication application) throws Exception {

		// Obtain the woof configuration
		ConfigurationItem configuration = this.context
				.getConfigurationItem(woofLocation);

		// Load the WoOF model
		WoofModel woof = this.repository.retrieveWoOF(configuration);

		// Configure the HTTP templates
		for (WoofTemplateModel templateModel : woof.getWoofTemplates()) {

			// Obtain template details
			String templatePath = templateModel.getTemplatePath();
			String templateClassName = templateModel.getTemplateClassName();
			String uri = templateModel.getUri();

			// Obtain the template logic class
			Class<?> templateLogicClass = this.classLoader
					.loadClass(templateClassName);

			// Configure the template
			application.addHttpTemplate(templatePath, templateLogicClass, uri);
		}

		// Configure the sections
		for (WoofSectionModel sectionModel : woof.getWoofSections()) {

			// Obtain the section details
			String sectionName = sectionModel.getWoofSectionName();
			String sectionSourceClassName = sectionModel
					.getSectionSourceClassName();
			String sectionLocation = sectionModel.getSectionLocation();

			// Obtain the section source class
			Class sectionSourceClass = this.classLoader
					.loadClass(sectionSourceClassName);

			// Configure the section
			AutoWireSection section = application.addSection(sectionName,
					sectionSourceClass, sectionLocation);
			for (PropertyModel property : sectionModel.getProperties()) {
				section.addProperty(property.getName(), property.getValue());
			}
		}
	}

}