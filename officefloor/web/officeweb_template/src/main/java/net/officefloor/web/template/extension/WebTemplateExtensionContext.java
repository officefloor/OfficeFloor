/*-
 * #%L
 * Web Template
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

package net.officefloor.web.template.extension;

import net.officefloor.compile.spi.section.SectionDependencyObjectNode;
import net.officefloor.compile.spi.section.SectionDesigner;
import net.officefloor.compile.spi.section.SectionFunction;
import net.officefloor.compile.spi.section.SectionManagedObject;
import net.officefloor.compile.spi.section.SectionObject;
import net.officefloor.compile.spi.section.SectionOutput;
import net.officefloor.compile.spi.section.source.SectionSourceContext;
import net.officefloor.frame.api.source.SourceProperties;
import net.officefloor.plugin.section.clazz.loader.ClassSectionFlow;
import net.officefloor.plugin.section.clazz.loader.ClassSectionManagedFunction;
import net.officefloor.server.http.HttpResponse;
import net.officefloor.web.template.build.WebTemplate;

/**
 * Context for the {@link WebTemplateExtension}.
 * 
 * @author Daniel Sagenschneider
 */
public interface WebTemplateExtensionContext extends SourceProperties {

	/**
	 * Obtains the content of the {@link WebTemplate}.
	 * 
	 * @return Content of the {@link WebTemplate}.
	 */
	String getTemplateContent();

	/**
	 * <p>
	 * Enables overriding the content of the template.
	 * <p>
	 * This need not be called, however is available should the extension wish to
	 * change the template content.
	 * 
	 * @param templateContent Content of the template.
	 */
	void setTemplateContent(String templateContent);

	/**
	 * Obtains the logic class for the {@link WebTemplate}.
	 * 
	 * @return Logic class for the {@link WebTemplate}.
	 */
	Class<?> getLogicClass();

	/**
	 * Flags that the method on the logic class should not have the template
	 * rendered to the {@link HttpResponse} by default on its completion.
	 * 
	 * @param templateClassMethodName Name of the method on the template class to be
	 *                                flagged to not have template rendered on its
	 *                                completion.
	 */
	void flagAsNonRenderTemplateMethod(String templateClassMethodName);

	/**
	 * <p>
	 * Obtains the {@link SectionSourceContext} for the {@link WebTemplate} to be
	 * extended.
	 * <p>
	 * Please be aware that the returned {@link SectionSourceContext} does not
	 * filter the properties. Therefore please use the property methods on this
	 * interface to obtain the extension specific properties.
	 * 
	 * @return {@link SectionSourceContext} for the {@link WebTemplate} to be
	 *         extended.
	 */
	SectionSourceContext getSectionSourceContext();

	/**
	 * Obtains the {@link SectionDesigner} for the {@link WebTemplate} being
	 * extended.
	 * 
	 * @return {@link SectionDesigner} for the {@link WebTemplate} being extended.
	 */
	SectionDesigner getSectionDesigner();

	/**
	 * Obtains the {@link SectionManagedObject} for the template logic object.
	 * 
	 * @return {@link SectionManagedObject}.
	 */
	SectionManagedObject getTemplateLogicObject();

	/**
	 * Obtains the {@link SectionFunction} by the name.
	 * 
	 * @param functionName {@link SectionFunction} name.
	 * @return {@link ClassSectionManagedFunction} or <code>null</code> if no
	 *         {@link SectionFunction} by name.
	 */
	ClassSectionManagedFunction getFunction(String functionName);

	/**
	 * Obtains or creates the {@link SectionObject} for the type name.
	 * 
	 * @param qualifier Qualifier. May be <code>null</code>.
	 * @param typeName  Type name.
	 * @return {@link SectionDependencyObjectNode}.
	 * @throws Exception If fails to obtain dependency.
	 */
	SectionDependencyObjectNode getDependency(String qualifier, String typeName) throws Exception;

	/**
	 * Obtains or creates the {@link SectionOutput}.
	 * 
	 * @param name         {@link SectionOutput} name.
	 * @param argumentType Argument type. May be <code>null</code> if no argument.
	 * @return {@link ClassSectionFlow}.
	 */
	ClassSectionFlow getFlow(String name, String argumentType);

}
