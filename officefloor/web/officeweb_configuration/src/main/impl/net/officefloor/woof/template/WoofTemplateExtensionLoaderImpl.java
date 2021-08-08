/*-
 * #%L
 * Web configuration
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

package net.officefloor.woof.template;

import java.lang.reflect.InvocationTargetException;

import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.compile.OfficeFloorCompilerRunnable;
import net.officefloor.compile.impl.properties.PropertyListImpl;
import net.officefloor.compile.impl.properties.PropertyListSourceProperties;
import net.officefloor.compile.impl.util.CompileUtil;
import net.officefloor.compile.issues.CompilerIssues;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.configuration.ConfigurationContext;
import net.officefloor.configuration.impl.ConfigurationSourceContextImpl;
import net.officefloor.frame.api.source.SourceContext;
import net.officefloor.frame.api.source.SourceProperties;
import net.officefloor.model.change.Change;
import net.officefloor.model.impl.change.ConflictImpl;
import net.officefloor.model.impl.change.NoChange;
import net.officefloor.web.build.WebArchitect;
import net.officefloor.web.template.build.WebTemplate;
import net.officefloor.woof.model.woof.WoofChangeIssues;
import net.officefloor.woof.model.woof.WoofTemplateExtensionChangeContextImpl;
import net.officefloor.woof.model.woof.WoofTemplateExtensionModel;

/**
 * {@link WoofTemplateExtensionLoader} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class WoofTemplateExtensionLoaderImpl implements WoofTemplateExtensionLoader {

	/*
	 * =============== WoofTemplateExtensionLoader =================
	 */

	public static class LoadSpecification implements OfficeFloorCompilerRunnable<PropertyList> {
		@Override
		public PropertyList run(OfficeFloorCompiler compiler, Object[] parameters) throws Exception {

			// Obtain the parameters
			String woofTemplateExtensionSourceClassName = (String) parameters[0];
			CompilerIssues issues = (CompilerIssues) parameters[1];

			// Instantiate the woof template extension source
			SourceContext sourceContext = compiler.createRootSourceContext();
			WoofTemplateExtensionSource extensionSource = CompileUtil.newInstance(woofTemplateExtensionSourceClassName,
					WoofTemplateExtensionSource.class, null, sourceContext, compiler, issues);
			if (extensionSource == null) {
				return null; // failed to instantiate
			}

			// Obtain the specification
			WoofTemplateExtensionSourceSpecification specification;
			try {
				specification = extensionSource.getSpecification();
			} catch (Throwable ex) {
				issues.addIssue(compiler,
						"Failed to obtain " + WoofTemplateExtensionSourceSpecification.class.getSimpleName() + " from "
								+ woofTemplateExtensionSourceClassName,
						ex);
				return null; // failed to obtain
			}

			// Ensure have specification
			if (specification == null) {
				issues.addIssue(compiler, "No " + WoofTemplateExtensionSourceSpecification.class.getSimpleName()
						+ " returned from " + woofTemplateExtensionSourceClassName);
				return null; // no specification obtained
			}

			// Obtain the properties
			WoofTemplateExtensionSourceProperty[] extensionSourceProperties;
			try {
				extensionSourceProperties = specification.getProperties();
			} catch (Throwable ex) {
				issues.addIssue(compiler,
						"Failed to obtain " + WoofTemplateExtensionSourceProperty.class.getSimpleName()
								+ " instances from " + WoofTemplateExtensionSourceSpecification.class.getSimpleName()
								+ " for " + woofTemplateExtensionSourceClassName,
						ex);
				return null; // failed to obtain properties
			}

			// Load the extension source properties into a property list
			PropertyList propertyList = new PropertyListImpl();
			if (extensionSourceProperties != null) {
				for (int i = 0; i < extensionSourceProperties.length; i++) {
					WoofTemplateExtensionSourceProperty extensionProperty = extensionSourceProperties[i];

					// Ensure have the extension source property
					if (extensionProperty == null) {
						issues.addIssue(compiler,
								WoofTemplateExtensionSourceProperty.class.getSimpleName() + " " + i + " is null from "
										+ WoofTemplateExtensionSourceSpecification.class.getSimpleName() + " for "
										+ woofTemplateExtensionSourceClassName);
						return null; // must have complete property details
					}

					// Obtain the property name
					String name;
					try {
						name = extensionProperty.getName();
					} catch (Throwable ex) {
						issues.addIssue(compiler,
								"Failed to get name for " + WoofTemplateExtensionSourceProperty.class.getSimpleName()
										+ " " + i + " from "
										+ WoofTemplateExtensionSourceSpecification.class.getSimpleName() + " for "
										+ woofTemplateExtensionSourceClassName,
								ex);
						return null; // must have complete property details
					}
					if (CompileUtil.isBlank(name)) {
						issues.addIssue(compiler,
								WoofTemplateExtensionSourceProperty.class.getSimpleName() + " " + i
										+ " provided blank name from "
										+ WoofTemplateExtensionSourceSpecification.class.getSimpleName() + " for "
										+ woofTemplateExtensionSourceClassName);
						return null; // must have complete property details
					}

					// Obtain the property label
					String label;
					try {
						label = extensionProperty.getLabel();
					} catch (Throwable ex) {
						issues.addIssue(compiler,
								"Failed to get label for " + WoofTemplateExtensionSourceProperty.class.getSimpleName()
										+ " " + i + " (" + name + ") from "
										+ WoofTemplateExtensionSourceSpecification.class.getSimpleName() + " for "
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
	public PropertyList loadSpecification(String woofTemplateExtensionSourceClassName, ClassLoader classLoader,
			CompilerIssues issues) {

		// Create and configure the compiler
		OfficeFloorCompiler compiler = OfficeFloorCompiler.newOfficeFloorCompiler(classLoader);
		compiler.setCompilerIssues(issues);

		// Load the specification
		PropertyList properties = null;
		try {
			properties = compiler.run(LoadSpecification.class, woofTemplateExtensionSourceClassName, issues);

		} catch (Exception ex) {
			issues.addIssue(compiler, "Failed to load specification", ex);
		}

		// Return the properties
		return properties;
	}

	/**
	 * {@link OfficeFloorCompilerRunnable} to refactor the
	 * {@link WoofTemplateExtensionSource}.
	 */
	public static class RefactorTemplateExtension implements OfficeFloorCompilerRunnable<Change<?>> {

		/*
		 * ================ OfficeFloorCompilerRunnable ==============
		 */

		@Override
		public Change<?> run(OfficeFloorCompiler compiler, Object[] parameters) throws Exception {

			// Obtain the parameters
			String woofTemplateExtensionSourceClassName = (String) parameters[0];
			String oldUri = (String) parameters[1];
			SourceProperties oldProperties = (SourceProperties) parameters[2];
			String newUri = (String) parameters[3];
			SourceProperties newProperties = (SourceProperties) parameters[4];
			ConfigurationContext configurationContext = (ConfigurationContext) parameters[5];
			SourceContext sourceContext = (SourceContext) parameters[6];
			WoofChangeIssues issues = (WoofChangeIssues) parameters[7];

			// Wrap issues to report this particular extension failing
			WoofChangeIssues extensionIssues = new ExtensionWoofChangeIssues((oldUri == null ? newUri : oldUri),
					woofTemplateExtensionSourceClassName, issues);

			// Construct the context
			WoofTemplateExtensionChangeContext context = new WoofTemplateExtensionChangeContextImpl(true, sourceContext,
					oldUri, oldProperties, newUri, newProperties, configurationContext, extensionIssues);

			// Attempt to create the extension change
			Change<?> extensionChange = null;
			try {

				// Construct the source
				Class<?> extensionSourceClass = sourceContext.loadClass(woofTemplateExtensionSourceClassName);
				WoofTemplateExtensionSource source = (WoofTemplateExtensionSource) extensionSourceClass
						.getDeclaredConstructor().newInstance();

				// Create potential change to to refactor extension
				extensionChange = source.createConfigurationChange(context);

			} catch (Throwable ex) {
				// Handle invocation target
				if (ex instanceof InvocationTargetException) {
					ex = ex.getCause();
				}

				// Provide conflict indicating failure of extension
				extensionChange = createFailureChange(woofTemplateExtensionSourceClassName, oldUri, newUri, ex);
			}

			// Return the extension change
			return extensionChange;
		}
	}

	/**
	 * {@link WoofChangeIssues} to provide reporting of issues for
	 * {@link WoofTemplateExtensionSource}.
	 */
	private static class ExtensionWoofChangeIssues implements WoofChangeIssues {

		/**
		 * Message prefix.
		 */
		private final String messagePrefix;

		/**
		 * Delegate {@link WoofChangeIssues}.
		 */
		private final WoofChangeIssues delegate;

		/**
		 * Initiate.
		 * 
		 * @param applicationPath                      {@link WebTemplate} application
		 *                                             path.
		 * @param woofTemplateExtensionSourceClassName {@link WoofTemplateExtensionSource}
		 *                                             class name.
		 * @param delegate                             {@link WoofChangeIssues}
		 *                                             delegate.
		 */
		public ExtensionWoofChangeIssues(String applicationPath, String woofTemplateExtensionSourceClassName,
				WoofChangeIssues delegate) {
			this.messagePrefix = "Template " + applicationPath + " Extension " + woofTemplateExtensionSourceClassName
					+ ": ";
			this.delegate = delegate;
		}

		/*
		 * ================ WoofChangeIssues ===================
		 */

		@Override
		public void addIssue(String message) {
			this.delegate.addIssue(this.messagePrefix + message);
		}

		@Override
		public void addIssue(String message, Throwable cause) {
			this.delegate.addIssue(this.messagePrefix + message, cause);
		}
	}

	@Override
	public Change<?> refactorTemplateExtension(String woofTemplateExtensionSourceClassName, String oldUri,
			SourceProperties oldProperties, String newUri, SourceProperties newProperties,
			ConfigurationContext configurationContext, SourceContext sourceContext, WoofChangeIssues issues) {

		// Create and configure the compiler
		OfficeFloorCompiler compiler = OfficeFloorCompiler.newOfficeFloorCompiler(sourceContext.getClassLoader());

		// Load the change
		Change<?> change = null;
		try {
			change = compiler.run(RefactorTemplateExtension.class, woofTemplateExtensionSourceClassName, oldUri,
					oldProperties, newUri, newProperties, configurationContext, sourceContext, issues);

		} catch (Exception ex) {
			// Provide conflict indicating failure of extension
			change = createFailureChange(woofTemplateExtensionSourceClassName, oldUri, newUri, ex);
		}

		// Return the change
		return change;
	}

	/**
	 * Creates the {@link NoChange} for failure of refactor {@link Change}.
	 * 
	 * @param woofTemplateExtensionSourceClassName {@link WoofTemplateExtensionSource}
	 *                                             class name.
	 * @param oldApplicationPath                   Old URI.
	 * @param newApplicationPath                   New URI.
	 * @param ex                                   Cause.
	 * @return {@link NoChange}.
	 */
	private static NoChange<?> createFailureChange(String woofTemplateExtensionSourceClassName,
			String oldApplicationPath, String newApplicationPath, Throwable ex) {
		return new NoChange<WoofTemplateExtensionModel>(
				new WoofTemplateExtensionModel(woofTemplateExtensionSourceClassName),
				"Refactor extension " + woofTemplateExtensionSourceClassName,
				new ConflictImpl(
						"Extension " + woofTemplateExtensionSourceClassName + " on template "
								+ (oldApplicationPath != null ? oldApplicationPath : newApplicationPath)
								+ " prevented change as " + ex.getMessage() + " [" + ex.getClass().getName() + "]",
						ex));
	}

	@Override
	public void extendTemplate(WoofTemplateExtensionSource extensionSource, PropertyList properties,
			String templatePath, WebTemplate template, OfficeArchitect officeArchitect, WebArchitect webArchitect,
			SourceContext sourceContext) throws WoofTemplateExtensionException {

		// Create the context for the extension source
		WoofTemplateExtensionSourceContext extensionSourceContext = new WoofTemplateExtensionServiceContextImpl(
				templatePath, template, officeArchitect, webArchitect, properties, sourceContext);

		// Extend the template
		try {
			extensionSource.extendTemplate(extensionSourceContext);

		} catch (Throwable ex) {
			// Indicate failure to extend template
			throw new WoofTemplateExtensionException("Failed loading Template Extension "
					+ extensionSource.getClass().getName() + ". " + ex.getMessage(), ex);
		}
	}

	/**
	 * {@link WoofTemplateExtensionSourceContext} implementation.
	 */
	private static class WoofTemplateExtensionServiceContextImpl extends ConfigurationSourceContextImpl
			implements WoofTemplateExtensionSourceContext {

		/**
		 * URL path to the {@link WebTemplate}.
		 */
		private final String applicationPath;

		/**
		 * {@link WebTemplate}.
		 */
		private final WebTemplate template;

		/**
		 * {@link OfficeArchitect}.
		 */
		private final OfficeArchitect officeArchitect;

		/**
		 * {@link WebArchitect}.
		 */
		private final WebArchitect webArchitect;

		/**
		 * Initiate.
		 * 
		 * @param applicationPath Application path to the {@link WebTemplate}.
		 * @param template        {@link WebTemplate}.
		 * @param officeArchitect {@link OfficeArchitect}.
		 * @param webArchitect    {@link WebArchitect}.
		 * @param properties      {@link PropertyList}.
		 * @param classLoader     {@link ClassLoader}.
		 */
		public WoofTemplateExtensionServiceContextImpl(String applicationPath, WebTemplate template,
				OfficeArchitect officeArchitect, WebArchitect webArchitect, PropertyList properties,
				SourceContext sourceContext) {
			super(sourceContext.getName(), sourceContext.isLoadingType(), sourceContext, null,
					new PropertyListSourceProperties(properties));
			this.applicationPath = applicationPath;
			this.template = template;
			this.officeArchitect = officeArchitect;
			this.webArchitect = webArchitect;
		}

		/*
		 * ============= WoofTemplateExtensionServiceContext ================
		 */

		@Override
		public String getApplicationPath() {
			return this.applicationPath;
		}

		@Override
		public WebTemplate getTemplate() {
			return this.template;
		}

		@Override
		public OfficeArchitect getOfficeArchitect() {
			return this.officeArchitect;
		}

		@Override
		public WebArchitect getWebArchitect() {
			return this.webArchitect;
		}
	}

}
