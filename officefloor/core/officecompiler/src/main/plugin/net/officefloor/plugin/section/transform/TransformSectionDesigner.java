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
package net.officefloor.plugin.section.transform;

import java.util.HashMap;
import java.util.Map;

import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionSource;
import net.officefloor.compile.spi.section.ManagedObjectDependency;
import net.officefloor.compile.spi.section.ManagedObjectFlow;
import net.officefloor.compile.spi.section.SectionDesigner;
import net.officefloor.compile.spi.section.SectionInput;
import net.officefloor.compile.spi.section.SectionManagedObject;
import net.officefloor.compile.spi.section.SectionManagedObjectSource;
import net.officefloor.compile.spi.section.SectionObject;
import net.officefloor.compile.spi.section.SectionOutput;
import net.officefloor.compile.spi.section.SectionTask;
import net.officefloor.compile.spi.section.SectionWork;
import net.officefloor.compile.spi.section.SubSection;
import net.officefloor.compile.spi.section.SubSectionInput;
import net.officefloor.compile.spi.section.SubSectionObject;
import net.officefloor.compile.spi.section.SubSectionOutput;
import net.officefloor.compile.spi.section.TaskFlow;
import net.officefloor.compile.spi.section.TaskObject;
import net.officefloor.compile.spi.section.source.SectionSource;
import net.officefloor.frame.internal.structure.FlowInstigationStrategyEnum;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;

/**
 * {@link SectionDesigner} to intercept configuration to provide access to the
 * {@link SectionSource} configured items.
 * 
 * @author Daniel Sagenschneider
 */
public class TransformSectionDesigner implements SectionDesigner {

	/**
	 * Registers the item.
	 * 
	 * @param itemName
	 *            Name of the item.
	 * @param items
	 *            {@link Map} of item instances by their name.
	 * @param item
	 *            Item.
	 * @return Registered item.
	 */
	private static <T> T register(String itemName, Map<String, T> items, T item) {
		items.put(itemName, item);
		return item;
	}

	/**
	 * Delegate {@link SectionDesigner}.
	 */
	private final SectionDesigner delegate;

	/**
	 * {@link SectionInput} instances by their name.
	 */
	private final Map<String, SectionInput> inputs = new HashMap<String, SectionInput>();

	/**
	 * {@link SectionOutput} instances by their name.
	 */
	private final Map<String, SectionOutput> outputs = new HashMap<String, SectionOutput>();

	/**
	 * {@link SectionObject} instances by their name.
	 */
	private final Map<String, SectionObject> objects = new HashMap<String, SectionObject>();

	/**
	 * {@link SubSection} instances by their name.
	 */
	private final Map<String, SubSection> subSections = new HashMap<String, SubSection>();

	/**
	 * Initiate.
	 * 
	 * @param delegate
	 *            Delegate {@link SectionDesigner}.
	 */
	public TransformSectionDesigner(SectionDesigner delegate) {
		this.delegate = delegate;
	}

	/**
	 * Obtains the {@link SectionInput}.
	 * 
	 * @param inputName
	 *            Name of the {@link SectionInput}.
	 * @return {@link SectionInput}.
	 */
	public SectionInput getSectionInput(String inputName) {
		return this.inputs.get(inputName);
	}

	/**
	 * Obtains the {@link SectionOutput}.
	 * 
	 * @param outputName
	 *            Name of the {@link SectionOutput}.
	 * @return {@link SectionOutput}.
	 */
	public SectionOutput getSectionOutput(String outputName) {
		return this.outputs.get(outputName);
	}

	/**
	 * Obtains the {@link SubSection}.
	 * 
	 * @param subSectionName
	 *            Name of the {@link SubSection}.
	 * @return {@link SubSection}.
	 */
	public SubSection getSubSection(String subSectionName) {
		return this.subSections.get(subSectionName);
	}

	/*
	 * ==================== SectionDesigner ========================
	 */

	@Override
	public SectionInput addSectionInput(String inputName, String parameterType) {
		return register(inputName, this.inputs,
				this.delegate.addSectionInput(inputName, parameterType));
	}

	@Override
	public SectionOutput addSectionOutput(String outputName,
			String argumentType, boolean isEscalationOnly) {
		return register(outputName, this.outputs,
				this.delegate.addSectionOutput(outputName, argumentType,
						isEscalationOnly));
	}

	@Override
	public SectionObject addSectionObject(String objectName, String objectType) {
		return register(objectName, this.objects,
				this.delegate.addSectionObject(objectName, objectType));
	}

	@Override
	public SectionWork addSectionWork(String workName,
			String workSourceClassName) {
		return this.delegate.addSectionWork(workName, workSourceClassName);
	}

	@Override
	public SectionWork addSectionWork(String workName, ManagedFunctionSource<?> workSource) {
		return this.delegate.addSectionWork(workName, workSource);
	}

	@Override
	public SectionManagedObjectSource addSectionManagedObjectSource(
			String managedObjectSourceName, String managedObjectSourceClassName) {
		return this.delegate.addSectionManagedObjectSource(
				managedObjectSourceName, managedObjectSourceClassName);
	}

	@Override
	public SectionManagedObjectSource addSectionManagedObjectSource(
			String managedObjectSourceName,
			ManagedObjectSource<?, ?> managedObjectSource) {
		return this.delegate.addSectionManagedObjectSource(
				managedObjectSourceName, managedObjectSource);
	}

	@Override
	public SubSection addSubSection(String subSectionName,
			String sectionSourceClassName, String location) {
		return register(subSectionName, this.subSections,
				this.delegate.addSubSection(subSectionName,
						sectionSourceClassName, location));
	}

	@Override
	public SubSection addSubSection(String subSectionName,
			SectionSource sectionSource, String location) {
		return register(subSectionName, this.subSections,
				this.delegate.addSubSection(subSectionName, sectionSource,
						location));
	}

	@Override
	public void link(SectionInput sectionInput, SectionTask task) {
		this.delegate.link(sectionInput, task);
	}

	@Override
	public void link(SectionInput sectionInput, SubSectionInput subSectionInput) {
		this.delegate.link(sectionInput, subSectionInput);
	}

	@Override
	public void link(SectionInput sectionInput, SectionOutput sectionOutput) {
		this.delegate.link(sectionInput, sectionOutput);
	}

	@Override
	public void link(TaskFlow taskFlow, SectionTask task,
			FlowInstigationStrategyEnum instigationStrategy) {
		this.delegate.link(taskFlow, task, instigationStrategy);
	}

	@Override
	public void link(TaskFlow taskFlow, SubSectionInput subSectionInput,
			FlowInstigationStrategyEnum instigationStrategy) {
		this.delegate.link(taskFlow, subSectionInput, instigationStrategy);
	}

	@Override
	public void link(TaskFlow taskFlow, SectionOutput sectionOutput,
			FlowInstigationStrategyEnum instigationStrategy) {
		this.delegate.link(taskFlow, sectionOutput, instigationStrategy);
	}

	@Override
	public void link(SectionTask task, SectionTask nextTask) {
		this.delegate.link(task, nextTask);
	}

	@Override
	public void link(SectionTask task, SubSectionInput subSectionInput) {
		this.delegate.link(task, subSectionInput);
	}

	@Override
	public void link(SectionTask task, SectionOutput sectionOutput) {
		this.delegate.link(task, sectionOutput);
	}

	@Override
	public void link(SubSectionOutput subSectionOutput, SectionTask task) {
		this.delegate.link(subSectionOutput, task);
	}

	@Override
	public void link(SubSectionOutput subSectionOutput,
			SubSectionInput subSectionInput) {
		this.delegate.link(subSectionOutput, subSectionInput);
	}

	@Override
	public void link(SubSectionOutput subSectionOutput,
			SectionOutput sectionOutput) {
		this.delegate.link(subSectionOutput, sectionOutput);
	}

	@Override
	public void link(ManagedObjectFlow managedObjectFlow, SectionTask task) {
		this.delegate.link(managedObjectFlow, task);
	}

	@Override
	public void link(ManagedObjectFlow managedObjectFlow,
			SubSectionInput subSectionInput) {
		this.delegate.link(managedObjectFlow, subSectionInput);
	}

	@Override
	public void link(ManagedObjectFlow managedObjectFlow,
			SectionOutput sectionOutput) {
		this.delegate.link(managedObjectFlow, sectionOutput);
	}

	@Override
	public void link(TaskObject taskObject, SectionObject sectionObject) {
		this.delegate.link(taskObject, sectionObject);
	}

	@Override
	public void link(TaskObject taskObject,
			SectionManagedObject sectionManagedObject) {
		this.delegate.link(taskObject, sectionManagedObject);
	}

	@Override
	public void link(SubSectionObject subSectionObject,
			SectionObject sectionObject) {
		this.delegate.link(subSectionObject, sectionObject);
	}

	@Override
	public void link(SubSectionObject subSectionObject,
			SectionManagedObject sectionManagedObject) {
		this.delegate.link(subSectionObject, sectionManagedObject);
	}

	@Override
	public void link(ManagedObjectDependency dependency,
			SectionObject sectionObject) {
		this.delegate.link(dependency, sectionObject);
	}

	@Override
	public void link(ManagedObjectDependency dependency,
			SectionManagedObject sectionManagedObject) {
		this.delegate.link(dependency, sectionManagedObject);
	}

	@Override
	public void addIssue(String issueDescription) {
		this.delegate.addIssue(issueDescription);
	}

	@Override
	public void addIssue(String issueDescription, Throwable cause) {
		this.delegate.addIssue(issueDescription, cause);
	}

}