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

import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.compile.OfficeFloorCompilerRunnable;
import net.officefloor.compile.impl.properties.PropertyListImpl;
import net.officefloor.compile.impl.properties.PropertyListSourceProperties;
import net.officefloor.compile.impl.util.CompileUtil;
import net.officefloor.compile.issues.CompilerIssues;
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

	public static class LoadSpecification implements
			OfficeFloorCompilerRunnable<PropertyList> {
		@Override
		public PropertyList run(OfficeFloorCompiler compiler,
				Object[] parameters) throws Exception {

			// Obtain the parameters
			String woofTemplateExtensionSourceClassName = (String) parameters[0];
			CompilerIssues issues = (CompilerIssues) parameters[1];

			// Instantiate the woof template extension source
			SourceContext sourceContext = new SourceContextImpl(true,
					compiler.getClassLoader());
			WoofTemplateExtensionSource extensionSource = CompileUtil
					.newInstance(woofTemplateExtensionSourceClassName,
							WoofTemplateExtensionSource.class, null,
							sourceContext, null, null, null, null, issues);
			if (extensionSource == null) {
				return null; // failed to instantiate
			}

			// Obtain the specification
			WoofTemplateExtensionSourceSpecification specification;
			try {
				specification = extensionSource.getSpecification();
			} catch (Throwable ex) {
				issues.addIssue(
						null,
						null,
						null,
						null,
						"Failed to obtain "
								+ WoofTemplateExtensionSourceSpecification.class
										.getSimpleName() + " from "
								+ woofTemplateExtensionSourceClassName, ex);
				return null; // failed to obtain
			}

			// Ensure have specification
			if (specification == null) {
				issues.addIssue(
						null,
						null,
						null,
						null,
						"No "
								+ WoofTemplateExtensionSourceSpecification.class
										.getSimpleName() + " returned from "
								+ woofTemplateExtensionSourceClassName);
				return null; // no specification obtained
			}

			// Obtain the properties
			WoofTemplateExtensionSourceProperty[] extensionSourceProperties;
			try {
				extensionSourceProperties = specification.getProperties();
			} catch (Throwable ex) {
				issues.addIssue(
						null,
						null,
						null,
						null,
						"Failed to obtain "
								+ WoofTemplateExtensionSourceProperty.class
										.getSimpleName()
								+ " instances from "
								+ WoofTemplateExtensionSourceSpecification.class
										.getSimpleName() + " for "
								+ woofTemplateExtensionSourceClassName, ex);
				return null; // failed to obtain properties
			}

			// Load the extension source properties into a property list
			PropertyList propertyList = new PropertyListImpl();
			if (extensionSourceProperties != null) {
				for (int i = 0; i < extensionSourceProperties.length; i++) {
					WoofTemplateExtensionSourceProperty extensionProperty = extensionSourceProperties[i];

					// Ensure have the extension source property
					if (extensionProperty == null) {
						issues.addIssue(
								null,
								null,
								null,
								null,
								WoofTemplateExtensionSourceProperty.class
										.getSimpleName()
										+ " "
										+ i
										+ " is null from "
										+ WoofTemplateExtensionSourceSpecification.class
												.getSimpleName()
										+ " for "
										+ woofTemplateExtensionSourceClassName);
						return null; // must have complete property details
					}

					// Obtain the property name
					String name;
					try {
						name = extensionProperty.getName();
					} catch (Throwable ex) {
						issues.addIssue(
								null,
								null,
								null,
								null,
								"Failed to get name for "
										+ WoofTemplateExtensionSourceProperty.class
												.getSimpleName()
										+ " "
										+ i
										+ " from "
										+ WoofTemplateExtensionSourceSpecification.class
												.getSimpleName() + " for "
										+ woofTemplateExtensionSourceClassName,
								ex);
						return null; // must have complete property details
					}
					if (CompileUtil.isBlank(name)) {
						issues.addIssue(
								null,
								null,
								null,
								null,
								WoofTemplateExtensionSourceProperty.class
										.getSimpleName()
										+ " "
										+ i
										+ " provided blank name from "
										+ WoofTemplateExtensionSourceSpecification.class
												.getSimpleName()
										+ " for "
										+ woofTemplateExtensionSourceClassName);
						return null; // must have complete property details
					}

					// Obtain the property label
					String label;
					try {
						label = extensionProperty.getLabel();
					} catch (Throwable ex) {
						issues.addIssue(
								null,
								null,
								null,
								null,
								"Failed to get label for "
										+ WoofTemplateExtensionSourceProperty.class
												.getSimpleName()
										+ " "
										+ i
										+ " ("
										+ name
										+ ") from "
										+ WoofTemplateExtensionSourceSpecification.class
												.getSimpleName() + " for "
										+ woofTemplateExtensionSourceClassName,
								ex);
						return null; // must have complete property details
					}

					// Add to the properties
					propertyList.addProperty(name, label);
				}
			}

			// Return the property list
			return propertyList;
		}
	}

	@Override
	public PropertyList loadSpecification(
			String woofTemplateExtensionSourceClassName,
			ClassLoader classLoader, CompilerIssues issues) {

		// Create and configure the compiler
		OfficeFloorCompiler compiler = OfficeFloorCompiler
				.newOfficeFloorCompiler(classLoader);
		compiler.setCompilerIssues(issues);

		// Load the specification
		PropertyList properties = null;
		try {
			properties = compiler.run(LoadSpecification.class,
					woofTemplateExtensionSourceClassName, issues);

		} catch (Exception ex) {
			issues.addIssue(null, null, null, null,
					"Failed to load specification", ex);
		}

		// Return the properties
		return properties;
	}

	/**
	 * {@link OfficeFloorCompilerRunnable} to refactor the
	 * {@link WoofTemplateExtensionSource}.
	 */
	public static class RefactorTemplateExtension implements
			OfficeFloorCompilerRunnable<Change<?>> {

		/*
		 * ================ OfficeFloorCompilerRunnable ==============
		 */

		@Override
		public Change<?> run(OfficeFloorCompiler compiler, Object[] parameters)
				throws Exception {

			// Obtain the parameters
			String woofTemplateExtensionSourceClassName = (String) parameters[0];
			String oldUri = (String) parameters[1];
			SourceProperties oldProperties = (SourceProperties) parameters[2];
			String newUri = (String) parameters[3];
			SourceProperties newProperties = (SourceProperties) parameters[4];
			ConfigurationContext configurationContext = (ConfigurationContext) parameters[5];
			SourceContext sourceContext = (SourceContext) parameters[6];

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
				extensionChange = createFailureChange(
						woofTemplateExtensionSourceClassName, oldUri, newUri,
						ex);
			}

			// Return the extension change
			return extensionChange;
		}
	}

	@Override
	public Change<?> refactorTemplateExtension(
			String woofTemplateExtensionSourceClassName, String oldUri,
			SourceProperties oldProperties, String newUri,
			SourceProperties newProperties,
			ConfigurationContext configurationContext,
			SourceContext sourceContext) {

		// Create and configure the compiler
		OfficeFloorCompiler compiler = OfficeFloorCompiler
				.newOfficeFloorCompiler(sourceContext.getClassLoader());

		// Load the change
		Change<?> change = null;
		try {
			change = compiler.run(RefactorTemplateExtension.class,
					woofTemplateExtensionSourceClassName, oldUri,
					oldProperties, newUri, newProperties, configurationContext,
					sourceContext);

		} catch (Exception ex) {
			// Provide conflict indicating failure of extension
			change = createFailureChange(woofTemplateExtensionSourceClassName,
					oldUri, newUri, ex);
		}

		// Return the change
		return change;
	}

	/**
	 * Creates the {@link NoChange} for failure of refactor {@link Change}.
	 * 
	 * @param woofTemplateExtensionSourceClassName
	 *            {@link WoofTemplateExtensionSource} class name.
	 * @param oldUri
	 *            Old URI.
	 * @param newUri
	 *            New URI.
	 * @param ex
	 *            Cause.
	 * @return {@link NoChange}.
	 */
	private static NoChange<?> createFailureChange(
			String woofTemplateExtensionSourceClassName, String oldUri,
			String newUri, Throwable ex) {
		return new NoChange<WoofTemplateExtensionModel>(
				new WoofTemplateExtensionModel(
						woofTemplateExtensionSourceClassName),
				"Refactor extension " + woofTemplateExtensionSourceClassName,
				"Extension " + woofTemplateExtensionSourceClassName
						+ " on template " + (oldUri != null ? oldUri : newUri)
						+ " prevented change as " + ex.getMessage() + " ["
						+ ex.getClass().getName() + "]");
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