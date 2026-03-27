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

import net.officefloor.compile.issues.CompilerIssues;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.configuration.ConfigurationContext;
import net.officefloor.frame.api.source.SourceContext;
import net.officefloor.frame.api.source.SourceProperties;
import net.officefloor.model.change.Change;
import net.officefloor.web.build.WebArchitect;
import net.officefloor.web.template.build.WebTemplate;
import net.officefloor.woof.model.woof.WoofChangeIssues;
import net.officefloor.woof.model.woof.WoofTemplateModel;

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
	 * @param woofTemplateExtensionSourceClassName
	 *            {@link WoofTemplateExtensionSource} class name.
	 * @param classLoader
	 *            {@link ClassLoader} to use in loading the specification.
	 * @param issues
	 *            {@link CompilerIssues} to report any issues in attempting to
	 *            obtain the {@link PropertyList}.
	 * @return {@link PropertyList} of the
	 *         {@link WoofTemplateExtensionSourceProperty} instances of the
	 *         {@link WoofTemplateExtensionSourceSpecification} or
	 *         <code>null</code> if issue, which is reported to the
	 *         {@link CompilerIssues}.
	 */
	PropertyList loadSpecification(String woofTemplateExtensionSourceClassName, ClassLoader classLoader,
			CompilerIssues issues);

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
	 * @param issues
	 *            {@link WoofChangeIssues}.
	 * @return {@link Change} to refactor the
	 *         {@link WoofTemplateExtensionSource} for the
	 *         {@link WoofTemplateModel}.
	 */
	Change<?> refactorTemplateExtension(String woofTemplateExtensionSourceClassName, String oldUri,
			SourceProperties oldProperties, String newUri, SourceProperties newProperties,
			ConfigurationContext configurationContext, SourceContext sourceContext, WoofChangeIssues issues);

	/**
	 * Extends the {@link WebTemplate} with the
	 * {@link WoofTemplateExtensionSource}.
	 * 
	 * @param extensionSource
	 *            {@link WoofTemplateExtensionSource}.
	 * @param properties
	 *            {@link PropertyList} to configure the
	 *            {@link WoofTemplateExtensionSource}.
	 * @param applicationPath
	 *            Application path to the {@link WebTemplate}.
	 * @param template
	 *            {@link WebTemplate} to be extended.
	 * @param officeArchitect
	 *            {@link OfficeArchitect}.
	 * @param webArchitect
	 *            {@link WebArchitect}.
	 * @param sourceContext
	 *            {@link SourceContext}.
	 * @throws WoofTemplateExtensionException
	 *             If fails to extend the {@link WebTemplate}.
	 */
	void extendTemplate(WoofTemplateExtensionSource extensionSource, PropertyList properties, String applicationPath,
			WebTemplate template, OfficeArchitect officeArchitect, WebArchitect webArchitect,
			SourceContext sourceContext) throws WoofTemplateExtensionException;

}
