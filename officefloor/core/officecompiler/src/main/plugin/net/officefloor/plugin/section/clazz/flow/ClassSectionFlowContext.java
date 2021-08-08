/*-
 * #%L
 * OfficeCompiler
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

package net.officefloor.plugin.section.clazz.flow;

import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionSource;
import net.officefloor.compile.spi.section.SectionFlowSinkNode;
import net.officefloor.compile.spi.section.SectionFunction;
import net.officefloor.compile.spi.section.SectionFunctionNamespace;
import net.officefloor.compile.spi.section.SubSection;
import net.officefloor.compile.spi.section.SubSectionOutput;
import net.officefloor.compile.spi.section.source.SectionSource;
import net.officefloor.compile.spi.section.source.SectionSourceContext;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.plugin.section.clazz.loader.ClassSectionFlow;
import net.officefloor.plugin.section.clazz.loader.ClassSectionFunctionNamespace;
import net.officefloor.plugin.section.clazz.loader.ClassSectionManagedFunction;
import net.officefloor.plugin.section.clazz.loader.ClassSectionSubSection;

/**
 * {@link Flow} context for {@link Class} section.
 * 
 * @author Daniel Sagenschneider
 */
public interface ClassSectionFlowContext {

	/**
	 * Adds a {@link SectionFunctionNamespace}.
	 * 
	 * @param namespaceName                  Hit to use as name of
	 *                                       {@link SectionFunctionNamespace}. May
	 *                                       alter to keep name unique.
	 * @param managedFunctionSourceClassName {@link ManagedFunctionSource}
	 *                                       {@link Class} name.
	 * @param properties                     {@link PropertyList} for the
	 *                                       {@link SectionFunctionNamespace}.
	 * @return {@link ClassSectionFunctionNamespace}.
	 */
	ClassSectionFunctionNamespace addFunctionNamespace(String namespaceName, String managedFunctionSourceClassName,
			PropertyList properties);

	/**
	 * Adds a {@link SectionFunctionNamespace}.
	 * 
	 * @param namespaceName         Hit to use as name of
	 *                              {@link SectionFunctionNamespace}. May alter to
	 *                              keep name unique.
	 * @param managedFunctionSource {@link ManagedFunctionSource}.
	 * @param properties            {@link PropertyList} for the
	 *                              {@link SectionFunctionNamespace}.
	 * @return {@link ClassSectionFunctionNamespace}.
	 */
	ClassSectionFunctionNamespace addFunctionNamespace(String namespaceName,
			ManagedFunctionSource managedFunctionSource, PropertyList properties);

	/**
	 * Obtains the {@link ClassSectionManagedFunction}.
	 * 
	 * @param functionName Name of the {@link SectionFunction}.
	 * @return {@link ClassSectionManagedFunction} or <code>null</code> if no
	 *         {@link ClassSectionManagedFunction} by name.
	 */
	ClassSectionManagedFunction getFunction(String functionName);

	/**
	 * Creates the {@link ClassSectionSubSectionOutputLink}.
	 * 
	 * @param subSectionOutputName Name of {@link SubSectionOutput}.
	 * @param linkName             Name of handling {@link SectionFlowSinkNode}.
	 * @return {@link ClassSectionSubSectionOutputLink}.
	 */
	ClassSectionSubSectionOutputLink createSubSectionOutputLink(String subSectionOutputName, String linkName);

	/**
	 * Gets or creates the {@link SubSection}.
	 * 
	 * @param sectionName            Hint to use as name of {@link SubSection}. May
	 *                               alter to keep name unique.
	 * @param sectionSourceClassName Name of {@link SectionSource} {@link Class}.
	 * @param sectionLocation        Location of the {@link SubSection}.
	 * @param properties             {@link PropertyList} for the
	 *                               {@link SubSection}.
	 * @param configuredLinks        {@link ClassSectionSubSectionOutputLink}
	 *                               instances.
	 * @return {@link ClassSectionSubSection}.
	 */
	ClassSectionSubSection getOrCreateSubSection(String sectionName, String sectionSourceClassName,
			String sectionLocation, PropertyList properties, ClassSectionSubSectionOutputLink... configuredLinks);

	/**
	 * Gets or creates the {@link SubSection}.
	 * 
	 * @param sectionName     Hint to use as name of {@link SubSection}. May alter
	 *                        to keep name unique.
	 * @param sectionSource   {@link SectionSource}.
	 * @param sectionLocation Location of the {@link SubSection}.
	 * @param properties      {@link PropertyList} for the {@link SubSection}.
	 * @param configuredLinks {@link ClassSectionSubSectionOutputLink} instances.
	 * @return {@link ClassSectionSubSection}.
	 */
	ClassSectionSubSection getOrCreateSubSection(String sectionName, SectionSource sectionSource,
			String sectionLocation, PropertyList properties, ClassSectionSubSectionOutputLink... configuredLinks);

	/**
	 * Obtains the {@link ClassSectionFlow}.
	 * 
	 * @param flowName     Name of {@link Flow}.
	 * @param argumentType Fully qualified type of argument. May be
	 *                     <code>null</code> for no argument.
	 * @return {@link ClassSectionFlow}.
	 */
	ClassSectionFlow getFlow(String flowName, String argumentType);

	/**
	 * Obtains the {@link SectionSourceContext}.
	 * 
	 * @return {@link SectionSourceContext}.
	 */
	SectionSourceContext getSourceContext();

}
