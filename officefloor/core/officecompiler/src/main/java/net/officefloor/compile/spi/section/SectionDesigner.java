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

package net.officefloor.compile.spi.section;

import net.officefloor.compile.internal.structure.SectionNode;
import net.officefloor.compile.issues.SourceIssues;
import net.officefloor.compile.managedfunction.ManagedFunctionEscalationType;
import net.officefloor.compile.section.SectionInputType;
import net.officefloor.compile.section.SectionObjectType;
import net.officefloor.compile.section.SectionOutputType;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionSource;
import net.officefloor.compile.spi.pool.source.ManagedObjectPoolSource;
import net.officefloor.compile.spi.section.source.SectionSource;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.internal.structure.ThreadState;

/**
 * Designer to design the {@link SectionNode}.
 * 
 * @author Daniel Sagenschneider
 */
public interface SectionDesigner extends SourceIssues {

	/**
	 * Adds a {@link SectionInput} to the {@link SectionNode} being built.
	 * 
	 * @param inputName
	 *            Name of the {@link SectionInput}.
	 * @param parameterType
	 *            Parameter type for the {@link SectionInputType}.
	 * @return {@link SectionInput} for linking.
	 */
	SectionInput addSectionInput(String inputName, String parameterType);

	/**
	 * Adds a {@link SectionOutput} to the {@link SectionNode} being built.
	 * 
	 * @param outputName
	 *            Name of the {@link SectionOutput}.
	 * @param argumentType
	 *            Argument type for the {@link SectionOutputType}.
	 * @param isEscalationOnly
	 *            <code>true</code> if only
	 *            {@link ManagedFunctionEscalationType} instances are using the
	 *            {@link SectionOutputType}.
	 * @return {@link SectionOutput} for linking.
	 */
	SectionOutput addSectionOutput(String outputName, String argumentType, boolean isEscalationOnly);

	/**
	 * Adds a {@link SectionObject} to the {@link SectionNode} being built.
	 * 
	 * @param objectName
	 *            Name of the {@link SectionObject}.
	 * @param objectType
	 *            Type required for the {@link SectionObjectType}.
	 * @return {@link SectionObject} for linking.
	 */
	SectionObject addSectionObject(String objectName, String objectType);

	/**
	 * Adds a {@link SectionFunctionNamespace} to the {@link SectionNode} being
	 * built.
	 * 
	 * @param functionNamespaceName
	 *            Name of the {@link SectionFunctionNamespace}.
	 * @param managedFunctionSourceClassName
	 *            Fully qualified class name of the
	 *            {@link ManagedFunctionSource}. This allows adding the
	 *            {@link SectionFunctionNamespace} without having to worry if
	 *            the {@link ManagedFunctionSource} is available on the class
	 *            path.
	 * @return {@link SectionFunctionNamespace}.
	 */
	SectionFunctionNamespace addSectionFunctionNamespace(String functionNamespaceName,
			String managedFunctionSourceClassName);

	/**
	 * Adds a {@link SectionFunctionNamespace} to the {@link SectionNode} being
	 * built.
	 * 
	 * @param functionNamespaceName
	 *            Name of the {@link SectionFunctionNamespace}.
	 * @param managedFunctionSource
	 *            {@link ManagedFunctionSource} instance to use.
	 * @return {@link SectionFunctionNamespace}.
	 */
	SectionFunctionNamespace addSectionFunctionNamespace(String functionNamespaceName,
			ManagedFunctionSource managedFunctionSource);

	/**
	 * Adds a {@link SectionManagedObjectSource} to the {@link SectionNode}
	 * being built.
	 * 
	 * @param managedObjectSourceName
	 *            Name of the {@link SectionManagedObjectSource}.
	 * @param managedObjectSourceClassName
	 *            Fully qualified class name of the {@link ManagedObjectSource}.
	 *            This allows adding the {@link SectionManagedObject} without
	 *            having to worry if the {@link ManagedObjectSource} is
	 *            available on the class path.
	 * @return {@link SectionManagedObject}.
	 */
	SectionManagedObjectSource addSectionManagedObjectSource(String managedObjectSourceName,
			String managedObjectSourceClassName);

	/**
	 * Adds a {@link SectionManagedObjectSource} to the {@link SectionNode}
	 * being built.
	 * 
	 * @param managedObjectSourceName
	 *            Name of the {@link SectionManagedObjectSource}.
	 * @param managedObjectSource
	 *            {@link ManagedObjectSource} instance to use.
	 * @return {@link SectionManagedObject}.
	 */
	SectionManagedObjectSource addSectionManagedObjectSource(String managedObjectSourceName,
			ManagedObjectSource<?, ?> managedObjectSource);

	/**
	 * Adds an {@link SectionManagedObjectPool}.
	 * 
	 * @param managedObjectPoolName
	 *            Name of the {@link SectionManagedObjectPool}.
	 * @param managedObjectPoolSourceClassName
	 *            Fully qualified class name of the
	 *            {@link ManagedObjectPoolSource}.
	 * @return Added {@link SectionManagedObjectPool}.
	 */
	SectionManagedObjectPool addManagedObjectPool(String managedObjectPoolName,
			String managedObjectPoolSourceClassName);

	/**
	 * Adds a {@link SectionManagedObjectPool}.
	 * 
	 * @param managedObjectPoolName
	 *            Name of the {@link SectionManagedObjectPool}.
	 * @param managedObjectPoolSource
	 *            {@link ManagedObjectPoolSource} instance to use.
	 * @return {@link SectionManagedObjectPool}.
	 */
	SectionManagedObjectPool addManagedObjectPool(String managedObjectPoolName,
			ManagedObjectPoolSource managedObjectPoolSource);

	/**
	 * Adds a {@link SubSection} to the {@link SectionNode} being built.
	 * 
	 * @param subSectionName
	 *            Name of the {@link SubSection}.
	 * @param sectionSourceClassName
	 *            Fully qualified class name of the {@link SectionSource} for
	 *            the {@link SubSection}. This allows adding the
	 *            {@link SubSection} without having to worry if the
	 *            {@link SectionSource} is available on the class path. <b>This
	 *            should be used over attempting to find the
	 *            {@link SectionSource}</b> - as should leave to the compiler to
	 *            find the {@link SectionSource}.
	 * @param location
	 *            Location of the {@link SubSection}.
	 * @return {@link SubSection}.
	 */
	SubSection addSubSection(String subSectionName, String sectionSourceClassName, String location);

	/**
	 * Adds a {@link SubSection} to the {@link SectionNode} being built.
	 * 
	 * @param subSectionName
	 *            Name of the {@link SubSection}.
	 * @param sectionSource
	 *            {@link SectionSource} to enable providing direct instances.
	 *            This should only be used should the {@link SectionSource} want
	 *            to create a {@link SubSection} instance by supplying its own
	 *            instantiated {@link SectionSource} implementation.
	 * @param location
	 *            Location of the {@link SubSection}.
	 * @return {@link SubSection}.
	 */
	SubSection addSubSection(String subSectionName, SectionSource sectionSource, String location);

	/**
	 * Links the {@link SectionManagedObjectSource} to be pooled by the
	 * {@link SectionManagedObjectPool}.
	 * 
	 * @param managedObjectSource
	 *            {@link SectionManagedObjectSource}.
	 * @param managedObjectPool
	 *            {@link SectionManagedObjectPool}.
	 */
	void link(SectionManagedObjectSource managedObjectSource, SectionManagedObjectPool managedObjectPool);

	/**
	 * Links the {@link FunctionFlow} to be undertaken by the
	 * {@link SectionFlowSinkNode}.
	 * 
	 * @param functionFlow
	 *            {@link FunctionFlow}.
	 * @param sectionSinkNode
	 *            {@link SectionFlowSinkNode}.
	 * @param isSpawnThreadState
	 *            Indicates if spawns {@link ThreadState} for {@link Flow}.
	 */
	void link(FunctionFlow functionFlow, SectionFlowSinkNode sectionSinkNode, boolean isSpawnThreadState);

	/**
	 * Links the {@link SectionFlowSourceNode} to undertaken by the
	 * {@link SectionFlowSinkNode}.
	 * 
	 * @param flowSourceNode
	 *            {@link SectionFlowSourceNode}.
	 * @param flowSinkNode
	 *            {@link SectionFlowSinkNode}.
	 */
	void link(SectionFlowSourceNode flowSourceNode, SectionFlowSinkNode flowSinkNode);

	/**
	 * Links the {@link SectionDependencyRequireNode} to be fulfilled by the
	 * {@link SectionDependencyObjectNode}.
	 * 
	 * @param dependencyRequireNode
	 *            {@link SectionDependencyRequireNode}.
	 * @param dependencyObjectNode
	 *            {@link SectionDependencyObjectNode}.
	 */
	void link(SectionDependencyRequireNode dependencyRequireNode, SectionDependencyObjectNode dependencyObjectNode);

}
