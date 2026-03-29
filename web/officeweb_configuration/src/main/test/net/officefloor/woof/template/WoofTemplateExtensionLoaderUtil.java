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

import java.io.Reader;
import java.io.StringWriter;

import org.junit.Assert;

import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.compile.impl.properties.PropertyListImpl;
import net.officefloor.compile.issues.CompilerIssues;
import net.officefloor.compile.properties.Property;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.compile.test.issues.FailTestCompilerIssues;
import net.officefloor.compile.test.properties.PropertyListUtil;
import net.officefloor.configuration.ConfigurationContext;
import net.officefloor.configuration.ConfigurationItem;
import net.officefloor.configuration.impl.configuration.MemoryConfigurationContext;
import net.officefloor.frame.api.source.ResourceSource;
import net.officefloor.frame.api.source.SourceContext;
import net.officefloor.frame.api.source.SourceProperties;
import net.officefloor.frame.impl.construct.source.SourceContextImpl;
import net.officefloor.frame.impl.construct.source.SourcePropertiesImpl;
import net.officefloor.frame.test.MockClockFactory;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.model.change.Change;
import net.officefloor.web.build.WebArchitect;
import net.officefloor.web.template.build.WebTemplate;
import net.officefloor.woof.model.woof.WoofChangeIssues;

/**
 * Utility functions for testing a {@link WoofTemplateExtensionSource}
 * implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class WoofTemplateExtensionLoaderUtil {

	/**
	 * Validates the {@link WoofTemplateExtensionSourceSpecification} for the
	 * {@link WoofTemplateExtensionSource}.
	 * 
	 * @param <S>                              {@link WoofTemplateExtensionSource}
	 *                                         type.
	 * @param woofTemplateExtensionSourceClass {@link WoofTemplateExtensionSource}
	 *                                         class.
	 * @param propertyNameLabels               Listing of name/label pairs for the
	 *                                         {@link Property} instances.
	 * @return Loaded {@link PropertyList}.
	 */
	public static <S extends WoofTemplateExtensionSource> PropertyList validateSpecification(
			Class<S> woofTemplateExtensionSourceClass, String... propertyNameLabels) {
		return validateSpecification(woofTemplateExtensionSourceClass, null, propertyNameLabels);
	}

	/**
	 * Validates the {@link WoofTemplateExtensionSourceSpecification} for the
	 * {@link WoofTemplateExtensionSource}.
	 * 
	 * @param <S>                              {@link WoofTemplateExtensionSource}
	 *                                         type.
	 * @param woofTemplateExtensionSourceClass {@link WoofTemplateExtensionSource}
	 *                                         class.
	 * @param classLoader                      {@link ClassLoader}. May be
	 *                                         <code>null</code>.
	 * @param propertyNameLabels               Listing of name/label pairs for the
	 *                                         {@link Property} instances.
	 * @return Loaded {@link PropertyList}.
	 */
	public static <S extends WoofTemplateExtensionSource> PropertyList validateSpecification(
			Class<S> woofTemplateExtensionSourceClass, ClassLoader classLoader, String... propertyNameLabels) {

		// Load the specification
		PropertyList propertyList = getWoofTemplateExtensionLoader()
				.loadSpecification(woofTemplateExtensionSourceClass.getName(), classLoader, getCompilerIssues());

		// Verify the properties
		PropertyListUtil.validatePropertyNameLabels(propertyList, propertyNameLabels);

		// Return the property list
		return propertyList;
	}

	/**
	 * Creates the {@link SourceProperties}.
	 * 
	 * @param propertyNameValues Property name/value pairs.
	 * @return {@link SourceProperties}.
	 */
	public static SourceProperties createSourceProperties(String... propertyNameValues) {
		return new SourcePropertiesImpl(propertyNameValues);
	}

	/**
	 * Obtains the {@link WoofChangeIssues}.
	 * 
	 * @return {@link WoofChangeIssues}.
	 */
	public static WoofChangeIssues getWoofChangeIssues() {
		final OfficeFloorCompiler compiler = OfficeFloorCompiler.newOfficeFloorCompiler(null);
		final CompilerIssues issues = getCompilerIssues();
		return new WoofChangeIssues() {

			@Override
			public void addIssue(String message, Throwable cause) {
				issues.addIssue(compiler, message, cause);
			}

			@Override
			public void addIssue(String message) {
				issues.addIssue(compiler, message);
			}
		};
	}

	/**
	 * Creates the {@link Change} for refactoring.
	 * 
	 * @param <S>                              {@link WoofTemplateExtensionSource}
	 *                                         type.
	 * @param woofTemplateExtensionSourceClass {@link WoofTemplateExtensionSource}
	 *                                         class.
	 * @param oldUri                           Old URI.
	 * @param oldProperties                    Old {@link SourceProperties}.
	 * @param newUri                           New URI.
	 * @param newProperties                    New {@link SourceProperties}.
	 * @return {@link Change} for refactoring. May be <code>null</code>.
	 */
	public static <S extends WoofTemplateExtensionSource> Change<?> refactorTemplateExtension(
			Class<S> woofTemplateExtensionSourceClass, String oldUri, SourceProperties oldProperties, String newUri,
			SourceProperties newProperties) {
		return refactorTemplateExtension(woofTemplateExtensionSourceClass, oldUri, oldProperties, newUri, newProperties,
				null, null);
	}

	/**
	 * Creates the {@link Change} for refactoring.
	 * 
	 * @param <S>                              {@link WoofTemplateExtensionSource}
	 *                                         type.
	 * @param woofTemplateExtensionSourceClass {@link WoofTemplateExtensionSource}
	 *                                         class.
	 * @param oldUri                           Old URI.
	 * @param oldProperties                    Old {@link SourceProperties}.
	 * @param newUri                           New URI.
	 * @param newProperties                    New {@link SourceProperties}.
	 * @param configurationContext             {@link ConfigurationContext}. May be
	 *                                         <code>null</code>.
	 * @param classLoader                      {@link ClassLoader}.
	 * @param resourceSources                  {@link ResourceSource} instances.
	 * @return {@link Change} for refactoring. May be <code>null</code>.
	 */
	public static <S extends WoofTemplateExtensionSource> Change<?> refactorTemplateExtension(
			Class<S> woofTemplateExtensionSourceClass, String oldUri, SourceProperties oldProperties, String newUri,
			SourceProperties newProperties, ConfigurationContext configurationContext, ClassLoader classLoader,
			ResourceSource... resourceSources) {

		// Ensure have configuration context
		if (configurationContext == null) {
			configurationContext = new MemoryConfigurationContext();
		}

		// Create the source context
		SourceContext sourceContext = getSourceContext(classLoader, resourceSources);

		// Obtain the extension source class name
		String extensionSourceClassName = woofTemplateExtensionSourceClass.getName();

		// Obtain the WoOF change issues
		WoofChangeIssues issues = getWoofChangeIssues();

		// Load the change
		Change<?> change = getWoofTemplateExtensionLoader().refactorTemplateExtension(extensionSourceClassName, oldUri,
				oldProperties, newUri, newProperties, configurationContext, sourceContext, issues);

		// Return the change
		return change;
	}

	/**
	 * Convenience method to validate the {@link ConfigurationItem}.
	 * 
	 * @param context  {@link ConfigurationContext}.
	 * @param location Location of the {@link ConfigurationItem}.
	 * @param content  Expected content of the {@link ConfigurationItem}. May be
	 *                 <code>null</code> to indicate no expecting
	 *                 {@link ConfigurationItem}.
	 * @throws Exception If failure in accessing {@link ConfigurationItem}.
	 */
	public static void validateConfigurationItem(ConfigurationContext context, String location, String content)
			throws Exception {

		// Obtain the configuration item
		ConfigurationItem item = context.getConfigurationItem(location, null);

		// Determine if expecting the configuration item
		if (content == null) {
			Assert.assertNull("Should be no configuration item at location " + location, item);
			return;
		}

		// Ensure have the configuration item
		Assert.assertNotNull("Should have configuration item for location " + location, item);

		// Load the content of the configuration item
		Reader reader = item.getReader();
		StringWriter buffer = new StringWriter();
		for (int character = reader.read(); character != -1; character = reader.read()) {
			buffer.write(character);
		}

		// Ensure correct content
		OfficeFrameTestCase.assertXmlEquals("Incorrect content for configuration item at location " + location, content,
				buffer.toString());
	}

	/**
	 * Convenience method to validate the {@link ConfigurationItem}.
	 * 
	 * @param context  {@link ConfigurationContext}.
	 * @param location Location of the {@link ConfigurationItem}.
	 * @param content  Expected content of the {@link ConfigurationItem}. May be
	 *                 <code>null</code> to indicate no expecting
	 *                 {@link ConfigurationItem}.
	 * @throws Exception If fails accessing the {@link ConfigurationItem}.
	 */
	public static void validateConfigurationItem(ConfigurationContext context, String location, Reader content)
			throws Exception {

		// Obtain the content as string
		StringWriter buffer = null;
		if (content != null) {
			// Load the content into a buffer
			buffer = new StringWriter();
			for (int character = content.read(); character != -1; character = content.read()) {
				buffer.write(character);
			}
		}
		String stringContent = (buffer == null ? null : buffer.toString());

		// Validate the configuration item
		validateConfigurationItem(context, location, stringContent);
	}

	/**
	 * Undertakes the extending of the {@link WebTemplate} by the
	 * {@link WoofTemplateExtensionSource}.
	 * 
	 * @param <S>                  {@link WoofTemplateExtensionSource} type.
	 * @param extensionSourceClass {@link WoofTemplateExtensionSource} class.
	 * @param applicationPath      Application path to the {@link WebTemplate}.
	 * @param template             {@link WebTemplate}.
	 * @param officeArchitect      {@link OfficeArchitect}.
	 * @param webArchitect         {@link WebArchitect}.
	 * @param propertyNameValues   {@link Property} name/value pairs.
	 * @throws Exception If fails to extend {@link WebTemplate}.
	 */
	public static <S extends WoofTemplateExtensionSource> void extendTemplate(Class<S> extensionSourceClass,
			String applicationPath, WebTemplate template, OfficeArchitect officeArchitect, WebArchitect webArchitect,
			String... propertyNameValues) throws Exception {
		extendTemplate(extensionSourceClass, applicationPath, template, officeArchitect, webArchitect, null, null,
				propertyNameValues);
	}

	/**
	 * Undertakes the extending of the {@link WebTemplate} by the
	 * {@link WoofTemplateExtensionSource}.
	 * 
	 * @param <S>                  {@link WoofTemplateExtensionSource} type.
	 * @param extensionSourceClass {@link WoofTemplateExtensionSource} class.
	 * @param templatePath         URL path to the {@link WebTemplate}.
	 * @param template             {@link WebTemplate}.
	 * @param officeArchitect      {@link OfficeArchitect}.
	 * @param webArchitect         {@link WebArchitect}.
	 * @param classLoader          {@link ClassLoader}. May be <code>null</code>.
	 * @param resourceSources      {@link ResourceSource} instances. May be
	 *                             <code>null</code>.
	 * @param propertyNameValues   {@link Property} name/value pairs.
	 * @throws Exception If fails to extend {@link WebTemplate}.
	 */
	public static <S extends WoofTemplateExtensionSource> void extendTemplate(Class<S> extensionSourceClass,
			String templatePath, WebTemplate template, OfficeArchitect officeArchitect, WebArchitect webArchitect,
			ClassLoader classLoader, ResourceSource[] resourceSources, String... propertyNameValues) throws Exception {

		// Obtains the source context
		SourceContext sourceContext = getSourceContext(classLoader, resourceSources);

		// Create the properties
		PropertyList properties = new PropertyListImpl(propertyNameValues);

		// Undertake the extension of the template
		getWoofTemplateExtensionLoader().extendTemplate(extensionSourceClass.getDeclaredConstructor().newInstance(),
				properties, templatePath, template, officeArchitect, webArchitect, sourceContext);
	}

	/**
	 * Obtains the {@link WoofTemplateExtensionLoader}.
	 * 
	 * @return {@link WoofTemplateExtensionLoader}.
	 */
	private static WoofTemplateExtensionLoader getWoofTemplateExtensionLoader() {
		return new WoofTemplateExtensionLoaderImpl();
	}

	/**
	 * Obtains the {@link SourceContext}.
	 * 
	 * @param classLoader     {@link ClassLoader}. May be <code>null</code>.
	 * @param resourceSources {@link ResourceSource} instances. May be
	 *                        <code>null</code>.
	 * @return {@link SourceContext}.
	 */
	private static SourceContext getSourceContext(ClassLoader classLoader, ResourceSource[] resourceSources) {

		// Ensure have class loader
		if (classLoader == null) {
			classLoader = Thread.currentThread().getContextClassLoader();
		}

		// Ensure have resource sources
		if (resourceSources == null) {
			resourceSources = new ResourceSource[0];
		}

		// Return the source context
		return new SourceContextImpl(WoofTemplateExtensionLoaderUtil.class.getName(), false, null, classLoader,
				new MockClockFactory(), resourceSources);
	}

	/**
	 * Obtains the {@link CompilerIssues}.
	 * 
	 * @return {@link CompilerIssues}.
	 */
	private static CompilerIssues getCompilerIssues() {
		return new FailTestCompilerIssues();
	}

	/**
	 * All access via static methods.
	 */
	private WoofTemplateExtensionLoaderUtil() {
	}

}
