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

import net.officefloor.compile.impl.properties.PropertyListSourceProperties;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.frame.impl.construct.source.SourceContextImpl;
import net.officefloor.frame.spi.source.SourceContext;
import net.officefloor.frame.spi.source.SourceProperties;
import net.officefloor.model.change.Change;
import net.officefloor.model.impl.change.NoChange;
import net.officefloor.model.repository.ConfigurationContext;
import net.officefloor.model.woof.WoofTemplateExtensionChangeContextImpl;
import net.officefloor.model.woof.WoofTemplateExtensionModel;
import net.officefloor.plugin.web.http.application.HttpTemplateAutoWireSection;
import net.officefloor.plugin.web.http.application.WebAutoWireApplication;

/**
 * {@link WoofTemplateExtensionLoader} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class WoofTemplateExtensionLoaderImpl implements
		WoofTemplateExtensionLoader {

	/*
	 * =============== WoofTemplateExtensionLoader =================
	 */

	@Override
	public PropertyList loadSpecification(
			Class<? extends WoofTemplateExtensionSource> woofTemplateExtensionSourceClass) {
		// TODO implement WoofTemplateExtensionLoader.loadSpecification
		throw new UnsupportedOperationException(
				"TODO implement WoofTemplateExtensionLoader.loadSpecification");
	}

	@Override
	public Change<?> refactorTemplateExtension(
			String woofTemplateExtensionSourceClassName, String oldUri,
			SourceProperties oldProperties, String newUri,
			SourceProperties newProperties,
			ConfigurationContext configurationContext,
			SourceContext sourceContext) {

		// Construct the context
		WoofTemplateExtensionChangeContext context = new WoofTemplateExtensionChangeContextImpl(
				true, sourceContext, oldUri, oldProperties, newUri,
				newProperties, configurationContext);

		// Attempt to create the extension change
		Change<?> extensionChange = null;
		try {

			// Construct the source
			Class<?> extensionSourceClass = sourceContext
					.loadClass(woofTemplateExtensionSourceClassName);
			WoofTemplateExtensionSource source = (WoofTemplateExtensionSource) extensionSourceClass
					.newInstance();

			// Create potential change to to refactor extension
			extensionChange = source.createConfigurationChange(context);

		} catch (Throwable ex) {
			// Provide conflict indicating failure of extension
			extensionChange = new NoChange<WoofTemplateExtensionModel>(
					new WoofTemplateExtensionModel(
							woofTemplateExtensionSourceClassName),
					"Refactor extension "
							+ woofTemplateExtensionSourceClassName,
					"Extension " + woofTemplateExtensionSourceClassName
							+ " on template "
							+ (oldUri != null ? oldUri : newUri)
							+ " prevented change as " + ex.getMessage() + " ["
							+ ex.getClass().getName() + "]");
		}

		// Return the extension change
		return extensionChange;
	}

	@Override
	public void extendTemplate(String extensionSourceClassName,
			PropertyList properties, HttpTemplateAutoWireSection template,
			WebAutoWireApplication application, SourceContext sourceContext)
			throws WoofTemplateExtensionException {

		// Create the context for the extension source
		WoofTemplateExtensionSourceContext extensionSourceContext = new WoofTemplateExtensionServiceContextImpl(
				template, application, properties, sourceContext);

		// Load the extension
		try {

			// Instantiate the extension source
			WoofTemplateExtensionSource extensionSource = (WoofTemplateExtensionSource) sourceContext
					.loadClass(extensionSourceClassName).newInstance();

			// Extend the template
			extensionSource.extendTemplate(extensionSourceContext);

		} catch (Throwable ex) {
			// Indicate failure to extend template
			throw new WoofTemplateExtensionException(
					"Failed loading Template Extension "
							+ extensionSourceClassName + ". " + ex.getMessage(),
					ex);
		}
	}

	/**
	 * {@link WoofTemplateExtensionSourceContext} implementation.
	 */
	private static class WoofTemplateExtensionServiceContextImpl extends
			SourceContextImpl implements WoofTemplateExtensionSourceContext {

		/**
		 * {@link HttpTemplateAutoWireSection}.
		 */
		private final HttpTemplateAutoWireSection template;

		/**
		 * {@link WebAutoWireApplication}.
		 */
		private final WebAutoWireApplication application;

		/**
		 * Initiate.
		 * 
		 * @param template
		 *            {@link HttpTemplateAutoWireSection}.
		 * @param application
		 *            {@link WebAutoWireApplication}.
		 * @param properties
		 *            {@link PropertyList}.
		 * @param classLoader
		 *            {@link ClassLoader}.
		 */
		public WoofTemplateExtensionServiceContextImpl(
				HttpTemplateAutoWireSection template,
				WebAutoWireApplication application, PropertyList properties,
				SourceContext sourceContext) {
			super(sourceContext.isLoadingType(), sourceContext,
					new PropertyListSourceProperties(properties));
			this.template = template;
			this.application = application;
		}

		/*
		 * ============= WoofTemplateExtensionServiceContext ================
		 */

		@Override
		public HttpTemplateAutoWireSection getTemplate() {
			return this.template;
		}

		@Override
		public WebAutoWireApplication getWebApplication() {
			return this.application;
		}
	}

}