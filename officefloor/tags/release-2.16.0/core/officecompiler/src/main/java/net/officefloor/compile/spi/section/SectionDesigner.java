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
import net.officefloor.compile.section.SectionInputType;
import net.officefloor.compile.section.SectionObjectType;
import net.officefloor.compile.section.SectionOutputType;
import net.officefloor.compile.spi.section.source.SectionSource;
import net.officefloor.compile.spi.work.source.WorkSource;
import net.officefloor.compile.work.TaskEscalationType;
import net.officefloor.frame.api.build.OfficeFloorIssues.AssetType;
import net.officefloor.frame.internal.structure.Asset;
import net.officefloor.frame.internal.structure.FlowInstigationStrategyEnum;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;

/**
 * Designer to design the {@link SectionNode}.
 * 
 * @author Daniel Sagenschneider
 */
public interface SectionDesigner {

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
	 *            <code>true</code> if only {@link TaskEscalationType} instances
	 *            are using the {@link SectionOutputType}.
	 * @return {@link SectionOutput} for linking.
	 */
	SectionOutput addSectionOutput(String outputName, String argumentType,
			boolean isEscalationOnly);

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
	 * Adds a {@link SectionWork} to the {@link SectionNode} being built.
	 * 
	 * @param workName
	 *            Name of the {@link SectionWork}.
	 * @param workSourceClassName
	 *            Fully qualified class name of the {@link WorkSource}. This
	 *            allows adding the {@link SectionWork} without having to worry
	 *            if the {@link WorkSource} is available on the class path.
	 * @return {@link SectionWork}.
	 */
	SectionWork addSectionWork(String workName, String workSourceClassName);

	/**
	 * Adds a {@link SectionWork} to the {@link SectionNode} being built.
	 * 
	 * @param workName
	 *            Name of the {@link SectionWork}.
	 * @param workSource
	 *            {@link WorkSource} instance to use.
	 * @return {@link SectionWork}.
	 */
	SectionWork addSectionWork(String workName, WorkSource<?> workSource);

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
	SectionManagedObjectSource addSectionManagedObjectSource(
			String managedObjectSourceName, String managedObjectSourceClassName);

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
	SectionManagedObjectSource addSectionManagedObjectSource(
			String managedObjectSourceName,
			ManagedObjectSource<?, ?> managedObjectSource);

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
	SubSection addSubSection(String subSectionName,
			String sectionSourceClassName, String location);

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
	SubSection addSubSection(String subSectionName,
			SectionSource sectionSource, String location);

	/**
	 * Links the {@link SectionInput} to be undertaken by the
	 * {@link SectionTask}.
	 * 
	 * @param sectionInput
	 *            {@link SectionInput}.
	 * @param task
	 *            {@link SectionTask}.
	 */
	void link(SectionInput sectionInput, SectionTask task);

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
	 * Links the {@link TaskFlow} to be undertaken by the {@link SectionTask}.
	 * 
	 * @param taskFlow
	 *            {@link TaskFlow}.
	 * @param task
	 *            {@link SectionTask}.
	 * @param instigationStrategy
	 *            {@link FlowInstigationStrategyEnum}.
	 */
	void link(TaskFlow taskFlow, SectionTask task,
			FlowInstigationStrategyEnum instigationStrategy);

	/**
	 * Links the {@link TaskFlow} to be undertaken by the
	 * {@link SubSectionInput}.
	 * 
	 * @param taskFlow
	 *            {@link TaskFlow}.
	 * @param subSectionInput
	 *            {@link SectionTask}.
	 * @param instigationStrategy
	 *            {@link FlowInstigationStrategyEnum}.
	 */
	void link(TaskFlow taskFlow, SubSectionInput subSectionInput,
			FlowInstigationStrategyEnum instigationStrategy);

	/**
	 * Links the {@link TaskFlow} to be undertaken by the {@link SectionOutput}.
	 * 
	 * @param taskFlow
	 *            {@link TaskFlow}.
	 * @param sectionOutput
	 *            {@link SectionOutput}.
	 * @param instigationStrategy
	 *            {@link FlowInstigationStrategyEnum}.
	 */
	void link(TaskFlow taskFlow, SectionOutput sectionOutput,
			FlowInstigationStrategyEnum instigationStrategy);

	/**
	 * Links the {@link SectionTask} with the next {@link SectionTask} to be
	 * undertaken.
	 * 
	 * @param task
	 *            {@link SectionTask}.
	 * @param nextTask
	 *            Next {@link SectionTask}.
	 */
	void link(SectionTask task, SectionTask nextTask);

	/**
	 * Links the {@link SectionTask} with the next {@link SubSectionInput} to be
	 * undertaken.
	 * 
	 * @param task
	 *            {@link SectionTask}.
	 * @param subSectionInput
	 *            Next {@link SubSectionInput}.
	 */
	void link(SectionTask task, SubSectionInput subSectionInput);

	/**
	 * Links the {@link SectionTask} with the next {@link SectionOutput} to be
	 * undertaken.
	 * 
	 * @param task
	 *            {@link SectionTask}.
	 * @param sectionOutput
	 *            Next {@link SectionOutput}.
	 */
	void link(SectionTask task, SectionOutput sectionOutput);

	/**
	 * Links the {@link SubSectionOutput} to be undertaken by the
	 * {@link SectionTask}.
	 * 
	 * @param subSectionOutput
	 *            {@link SubSectionOutput}.
	 * @param task
	 *            {@link SectionTask}.
	 */
	void link(SubSectionOutput subSectionOutput, SectionTask task);

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
	 * {@link SectionTask}.
	 * 
	 * @param managedObjectFlow
	 *            {@link ManagedObjectFlow}.
	 * @param task
	 *            {@link SectionTask}.
	 */
	void link(ManagedObjectFlow managedObjectFlow, SectionTask task);

	/**
	 * Links the {@link ManagedObjectFlow} to be undertaken by the
	 * {@link SubSectionInput}.
	 * 
	 * @param managedObjectFlow
	 *            {@link ManagedObjectFlow}.
	 * @param subSectionInput
	 *            {@link SubSectionInput}.
	 */
	void link(ManagedObjectFlow managedObjectFlow,
			SubSectionInput subSectionInput);

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
	 * Links the {@link TaskObject} to be the {@link SectionObject}.
	 * 
	 * @param taskObject
	 *            {@link TaskObject}.
	 * @param sectionObject
	 *            {@link SectionObject}.
	 */
	void link(TaskObject taskObject, SectionObject sectionObject);

	/**
	 * Links the {@link TaskObject} to be the {@link SectionManagedObject}.
	 * 
	 * @param taskObject
	 *            {@link TaskObject}.
	 * @param sectionManagedObject
	 *            {@link SectionManagedObject}.
	 */
	void link(TaskObject taskObject, SectionManagedObject sectionManagedObject);

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
	void link(SubSectionObject subSectionObject,
			SectionManagedObject sectionManagedObject);

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
	void link(ManagedObjectDependency dependency,
			SectionManagedObject sectionManagedObject);

	/**
	 * <p>
	 * Allows the {@link SectionSource} to add an issue in attempting to design
	 * the {@link SectionNode}.
	 * <p>
	 * This is available to report invalid configuration but continue to design
	 * the rest of the {@link SectionNode}.
	 * 
	 * @param issueDescription
	 *            Description of the issue.
	 * @param assetType
	 *            {@link AssetType}. May be <code>null</code> if
	 *            {@link SectionNode} in general.
	 * @param assetName
	 *            Name of the {@link Asset}. May be <code>null</code> if
	 *            {@link SectionNode} in general.
	 */
	void addIssue(String issueDescription, AssetType assetType, String assetName);

	/**
	 * <p>
	 * Allows the {@link SectionSource} to add an issue along with its cause in
	 * attempting to design the {@link SectionNode}.
	 * <p>
	 * This is available to report an issue and continue designing the
	 * {@link SectionNode} rather than throwing the {@link Exception} which
	 * results in an incomplete design.
	 * 
	 * @param issueDescription
	 *            Description of the issue.
	 * @param cause
	 *            Cause of the issue.
	 * @param assetType
	 *            {@link AssetType}. May be <code>null</code> if
	 *            {@link SectionNode} in general.
	 * @param assetName
	 *            Name of the {@link Asset}. May be <code>null</code> if
	 *            {@link SectionNode} in general.
	 */
	void addIssue(String issueDescription, Throwable cause,
			AssetType assetType, String assetName);

}