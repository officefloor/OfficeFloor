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

import java.util.Map;

import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.section.SectionType;
import net.officefloor.compile.spi.section.source.SectionSource;
import net.officefloor.model.change.Change;

/**
 * Changes that can be made to a {@link WoofModel}.
 * 
 * @author Daniel Sagenschneider
 */
public interface WoofChanges {

	/**
	 * Adds a {@link WoofTemplateModel}.
	 * 
	 * @param templateName
	 *            Name of the {@link WoofTemplateModel}.
	 * @param templatePath
	 *            Path to the template file.
	 * @param templateLogicClass
	 *            Name of the logic {@link Class} for the template.
	 * @param sectionType
	 *            {@link SectionType} for the {@link WoofTemplateModel}.
	 * @param uri
	 *            URI to the {@link WoofTemplateModel}. May be <code>null</code>
	 *            if private {@link WoofTemplateModel}.
	 * @return {@link Change} to add the {@link WoofTemplateModel}.
	 */
	Change<WoofTemplateModel> addTemplate(String templateName,
			String templatePath, String templateLogicClass,
			SectionType sectionType, String uri);

	/**
	 * Removes the {@link WoofTemplateModel}.
	 * 
	 * @param template
	 *            {@link WoofTemplateModel} to remove.
	 * @return {@link Change} to remove the {@link WoofTemplateModel}.
	 */
	Change<WoofTemplateModel> removeTemplate(WoofTemplateModel template);

	/**
	 * Adds a {@link WoofSectionModel}.
	 * 
	 * @param sectionName
	 *            Name of the {@link WoofSectionModel}.
	 * @param sectionSourceClassName
	 *            {@link SectionSource} class name.
	 * @param sectionLocation
	 *            Location of the section.
	 * @param properties
	 *            {@link PropertyList}.
	 * @param sectionType
	 *            {@link SectionType} for the {@link WoofSectionModel}.
	 * @param inputToUri
	 *            Mapping on input name to URI.
	 * @return {@link Change} to add the {@link WoofSectionModel}.
	 */
	Change<WoofSectionModel> addSection(String sectionName,
			String sectionSourceClassName, String sectionLocation,
			PropertyList properties, SectionType sectionType,
			Map<String, String> inputToUri);

	/**
	 * Removes the {@link WoofSectionModel}.
	 * 
	 * @param section
	 *            {@link WoofSectionModel} to remove.
	 * @return {@link Change} to remove the {@link WoofSectionModel}.
	 */
	Change<WoofSectionModel> removeSection(WoofSectionModel section);

	/**
	 * Adds a {@link WoofResourceModel}.
	 * 
	 * @param resourceName
	 *            Name of the {@link WoofResourceModel}.
	 * @param resourcePath
	 *            Path to the resource.
	 * @return {@link Change} to add the {@link WoofResourceModel}.
	 */
	Change<WoofResourceModel> addResource(String resourceName,
			String resourcePath);

	/**
	 * Removes the {@link WoofResourceModel}.
	 * 
	 * @param resource
	 *            {@link WoofResourceModel} to remove.
	 * @return {@link Change} to remove the {@link WoofResourceModel}.
	 */
	Change<WoofResourceModel> removeResource(WoofResourceModel resource);

	/**
	 * Adds a {@link WoofExceptionModel}.
	 * 
	 * @param exceptionClassName
	 *            {@link Throwable} class name.
	 * @return {@link Change} to add the {@link WoofExceptionModel}.
	 */
	Change<WoofExceptionModel> addException(String exceptionClassName);

	/**
	 * Removes the {@link WoofExceptionModel}.
	 * 
	 * @param exception
	 *            {@link WoofExceptionModel} to remove.
	 * @return {@link Change} to remove the {@link WoofExceptionModel}.
	 */
	Change<WoofExceptionModel> removeException(WoofExceptionModel exception);

}