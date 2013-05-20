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
package net.officefloor.plugin.woof.template;

import net.officefloor.compile.issues.CompilerIssues;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.frame.spi.source.SourceContext;
import net.officefloor.frame.spi.source.SourceProperties;
import net.officefloor.model.change.Change;
import net.officefloor.model.repository.ConfigurationContext;
import net.officefloor.model.woof.WoofTemplateModel;
import net.officefloor.plugin.web.http.application.HttpTemplateAutoWireSection;
import net.officefloor.plugin.web.http.application.WebAutoWireApplication;

/**
 * Loads the extension from the {@link WoofTemplateExtensionSource}.
 * 
 * @author Daniel Sagenschneider
 */
public interface WoofTemplateExtensionLoader {

	/**
	 * Loads and returns the {@link PropertyList} from the
	 * {@link WoofTemplateExtensionSourceSpecification} for the
	 * {@link WoofTemplateExtensionSource}.
	 * 
	 * @param woofTemplateExtensionSourceClass
	 *            {@link WoofTemplateExtensionSource} class.
	 * @return {@link PropertyList} of the
	 *         {@link WoofTemplateExtensionSourceProperty} instances of the
	 *         {@link WoofTemplateExtensionSourceSpecification} or
	 *         <code>null</code> if issue, which is reported to the
	 *         {@link CompilerIssues}.
	 */
	PropertyList loadSpecification(
			Class<? extends WoofTemplateExtensionSource> woofTemplateExtensionSourceClass);

	/**
	 * Refactors the {@link WoofTemplateExtensionSource} for the
	 * {@link WoofTemplateModel}.
	 * 
	 * @param woofTemplateExtensionSourceClassName
	 *            {@link WoofTemplateExtensionSource} class name.
	 * @param oldUri
	 *            Old URI. May be <code>null</code> if adding
	 *            {@link WoofTemplateExtensionSource}.
	 * @param oldProperties
	 *            Old {@link SourceProperties}.
	 * @param newUri
	 *            New URI. May be <code>null</code> if removing the
	 *            {@link WoofTemplateExtensionSource}.
	 * @param newProperties
	 *            New {@link SourceProperties}.
	 * @param configurationContext
	 *            {@link ConfigurationContext}.
	 * @param sourceContext
	 *            {@link SourceContext}.
	 * @return {@link Change} to refactor the
	 *         {@link WoofTemplateExtensionSource} for the
	 *         {@link WoofTemplateModel}.
	 */
	Change<?> refactorTemplateExtension(
			String woofTemplateExtensionSourceClassName, String oldUri,
			SourceProperties oldProperties, String newUri,
			SourceProperties newProperties,
			ConfigurationContext configurationContext,
			SourceContext sourceContext);

	/**
	 * Extends the {@link HttpTemplateAutoWireSection} with the
	 * {@link WoofTemplateExtensionSource}.
	 * 
	 * @param extensionSourceClassName
	 *            Name of the {@link WoofTemplateExtensionSource} class name.
	 * @param properties
	 *            {@link PropertyList} to configure the
	 *            {@link WoofTemplateExtensionSource}.
	 * @param template
	 *            {@link HttpTemplateAutoWireSection} to be extended.
	 * @param application
	 *            {@link WebAutoWireApplication}.
	 * @param sourceContext
	 *            {@link SourceContext}.
	 * @throws WoofTemplateExtensionException
	 *             If fails to extend the {@link HttpTemplateAutoWireSection}.
	 */
	void extendTemplate(String extensionSourceClassName,
			PropertyList properties, HttpTemplateAutoWireSection template,
			WebAutoWireApplication application, SourceContext sourceContext)
			throws WoofTemplateExtensionException;

}