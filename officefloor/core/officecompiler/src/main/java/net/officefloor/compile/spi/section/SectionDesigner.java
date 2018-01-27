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
package net.officefloor.compile.spi.section;

import net.officefloor.compile.internal.structure.SectionNode;
import net.officefloor.compile.issues.SourceIssues;
import net.officefloor.compile.managedfunction.ManagedFunctionEscalationType;
import net.officefloor.compile.section.SectionInputType;
import net.officefloor.compile.section.SectionObjectType;
import net.officefloor.compile.section.SectionOutputType;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionSource;
import net.officefloor.compile.spi.managedobject.ManagedObjectDependency;
import net.officefloor.compile.spi.managedobject.ManagedObjectFlow;
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
	 * @param workName
	 *            Name of the {@link SectionFunctionNamespace}.
	 * @param workSourceClassName
	 *            Fully qualified class name of the
	 *            {@link ManagedFunctionSource}. This allows adding the
	 *            {@link SectionFunctionNamespace} without having to worry if
	 *            the {@link ManagedFunctionSource} is available on the class
	 *            path.
	 * @return {@link SectionFunctionNamespace}.
	 */
	SectionFunctionNamespace addSectionFunctionNamespace(String workName, String workSourceClassName);

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
	 * Links the {@link SectionInput} to be undertaken by the
	 * {@link SectionFunction}.
	 * 
	 * @param sectionInput
	 *            {@link SectionInput}.
	 * @param task
	 *            {@link SectionFunction}.
	 */
	void link(SectionInput sectionInput, SectionFunction task);

	/**
	 * Links the {@link SectionInput} to be undertaken by the
	 * {@link SubSectionInput}.
	 * 
	 * @param sectionInput
	 *            {@link SectionInput}.
	 * @param subSectionInput
	 *            {@link SubSectionInput}.
	 */
	void link(SectionInput sectionInput, SubSectionInput subSectionInput);

	/**
	 * Links the {@link SubSectionInput} to be undertaken by the
	 * {@link SectionOutput}.
	 * 
	 * @param sectionInput
	 *            {@link SectionInput}.
	 * @param sectionOutput
	 *            {@link SectionOutput}.
	 */
	void link(SectionInput sectionInput, SectionOutput sectionOutput);

	/**
	 * Links the {@link FunctionFlow} to be undertaken by the
	 * {@link SectionFunction}.
	 * 
	 * @param functionFlow
	 *            {@link FunctionFlow}.
	 * @param function
	 *            {@link SectionFunction}.
	 * @param isSpawnThreadState
	 *            Indicates if spawns {@link ThreadState} for {@link Flow}.
	 */
	void link(FunctionFlow functionFlow, SectionFunction task, boolean isSpawnThreadState);

	/**
	 * Links the {@link FunctionFlow} to be undertaken by the
	 * {@link SubSectionInput}.
	 * 
	 * @param functionFlow
	 *            {@link FunctionFlow}.
	 * @param subSectionInput
	 *            {@link SectionFunction}.
	 * @param isSpawnThreadState
	 *            Indicates if spawns {@link ThreadState} for {@link Flow}.
	 */
	void link(FunctionFlow functionFlow, SubSectionInput subSectionInput, boolean isSpawnThreadState);

	/**
	 * Links the {@link FunctionFlow} to be undertaken by the
	 * {@link SectionOutput}.
	 * 
	 * @param functionFlow
	 *            {@link FunctionFlow}.
	 * @param sectionOutput
	 *            {@link SectionOutput}.
	 * @param isSpawnThreadState
	 *            Indicates if spawns {@link ThreadState} for {@link Flow}.
	 */
	void link(FunctionFlow functionFlow, SectionOutput sectionOutput, boolean isSpawnThreadState);

	/**
	 * Links the {@link SectionFunction} with the next {@link SectionFunction}
	 * to be undertaken.
	 * 
	 * @param task
	 *            {@link SectionFunction}.
	 * @param nextTask
	 *            Next {@link SectionFunction}.
	 */
	void link(SectionFunction task, SectionFunction nextTask);

	/**
	 * Links the {@link SectionFunction} with the next {@link SubSectionInput}
	 * to be undertaken.
	 * 
	 * @param task
	 *            {@link SectionFunction}.
	 * @param subSectionInput
	 *            Next {@link SubSectionInput}.
	 */
	void link(SectionFunction task, SubSectionInput subSectionInput);

	/**
	 * Links the {@link SectionFunction} with the next {@link SectionOutput} to
	 * be undertaken.
	 * 
	 * @param task
	 *            {@link SectionFunction}.
	 * @param sectionOutput
	 *            Next {@link SectionOutput}.
	 */
	void link(SectionFunction task, SectionOutput sectionOutput);

	/**
	 * Links the {@link SubSectionOutput} to be undertaken by the
	 * {@link SectionFunction}.
	 * 
	 * @param subSectionOutput
	 *            {@link SubSectionOutput}.
	 * @param task
	 *            {@link SectionFunction}.
	 */
	void link(SubSectionOutput subSectionOutput, SectionFunction task);

	/**
	 * Links the {@link SubSectionOutput} to be undertaken by the
	 * {@link SubSectionInput}.
	 * 
	 * @param subSectionOutput
	 *            {@link SubSectionOutput}.
	 * @param subSectionInput
	 *            {@link SubSectionInput}.
	 */
	void link(SubSectionOutput subSectionOutput, SubSectionInput subSectionInput);

	/**
	 * Links the {@link SubSectionOutput} to be undertaken by the
	 * {@link SectionOutput}.
	 * 
	 * @param subSectionOutput
	 *            {@link SubSectionOutput}.
	 * @param sectionOutput
	 *            {@link SectionOutput}.
	 */
	void link(SubSectionOutput subSectionOutput, SectionOutput sectionOutput);

	/**
	 * Links the {@link ManagedObjectFlow} to be undertaken by the
	 * {@link SectionFunction}.
	 * 
	 * @param managedObjectFlow
	 *            {@link ManagedObjectFlow}.
	 * @param task
	 *            {@link SectionFunction}.
	 */
	void link(ManagedObjectFlow managedObjectFlow, SectionFunction task);

	/**
	 * Links the {@link ManagedObjectFlow} to be undertaken by the
	 * {@link SubSectionInput}.
	 * 
	 * @param managedObjectFlow
	 *            {@link ManagedObjectFlow}.
	 * @param subSectionInput
	 *            {@link SubSectionInput}.
	 */
	void link(ManagedObjectFlow managedObjectFlow, SubSectionInput subSectionInput);

	/**
	 * Links the {@link ManagedObjectFlow} to be undertaken by the
	 * {@link SectionOutput}.
	 * 
	 * @param managedObjectFlow
	 *            {@link ManagedObjectFlow}.
	 * @param sectionOutput
	 *            {@link SectionOutput}.
	 */
	void link(ManagedObjectFlow managedObjectFlow, SectionOutput sectionOutput);

	/**
	 * Links the {@link FunctionObject} to be the {@link SectionObject}.
	 * 
	 * @param taskObject
	 *            {@link FunctionObject}.
	 * @param sectionObject
	 *            {@link SectionObject}.
	 */
	void link(FunctionObject taskObject, SectionObject sectionObject);

	/**
	 * Links the {@link FunctionObject} to be the {@link SectionManagedObject}.
	 * 
	 * @param taskObject
	 *            {@link FunctionObject}.
	 * @param sectionManagedObject
	 *            {@link SectionManagedObject}.
	 */
	void link(FunctionObject taskObject, SectionManagedObject sectionManagedObject);

	/**
	 * Links the {@link SubSectionObject} to be the {@link SectionObject}.
	 * 
	 * @param subSectionObject
	 *            {@link SubSectionObject}.
	 * @param sectionObject
	 *            {@link SectionObject}.
	 */
	void link(SubSectionObject subSectionObject, SectionObject sectionObject);

	/**
	 * Links {@link SubSectionObject} to be the {@link SectionManagedObject}.
	 * 
	 * @param subSectionObject
	 *            {@link SubSectionObject}.
	 * @param sectionManagedObject
	 *            {@link SectionManagedObject}.
	 */
	void link(SubSectionObject subSectionObject, SectionManagedObject sectionManagedObject);

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
	 * Links {@link ManagedObjectDependency} to be the {@link SectionObject}.
	 * 
	 * @param dependency
	 *            {@link ManagedObjectDependency}.
	 * @param sectionObject
	 *            {@link SectionObject}.
	 */
	void link(ManagedObjectDependency dependency, SectionObject sectionObject);

	/**
	 * Links the {@link ManagedObjectDependency} to be the
	 * {@link SectionManagedObject}.
	 * 
	 * @param dependency
	 *            {@link ManagedObjectDependency}.
	 * @param sectionManagedObject
	 *            {@link SectionManagedObject}.
	 */
	void link(ManagedObjectDependency dependency, SectionManagedObject sectionManagedObject);

}