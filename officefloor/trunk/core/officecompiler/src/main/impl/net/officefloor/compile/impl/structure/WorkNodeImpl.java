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
package net.officefloor.compile.impl.structure;

import java.util.LinkedList;
import java.util.List;

import net.officefloor.compile.internal.structure.Node;
import net.officefloor.compile.internal.structure.NodeContext;
import net.officefloor.compile.internal.structure.SectionNode;
import net.officefloor.compile.internal.structure.TaskNode;
import net.officefloor.compile.internal.structure.WorkNode;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.spi.section.SectionTask;
import net.officefloor.compile.spi.section.SectionWork;
import net.officefloor.compile.spi.work.source.WorkSource;
import net.officefloor.compile.work.WorkLoader;
import net.officefloor.compile.work.WorkType;
import net.officefloor.frame.api.build.OfficeBuilder;
import net.officefloor.frame.api.build.WorkBuilder;

/**
 * {@link WorkNode} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class WorkNodeImpl implements WorkNode {

	/**
	 * Name of this {@link SectionWork}.
	 */
	private final String workName;

	/**
	 * {@link PropertyList}.
	 */
	private final PropertyList propertyList;

	/**
	 * {@link OfficeSection} containing this {@link WorkNode}.
	 */
	private final SectionNode section;

	/**
	 * {@link NodeContext}.
	 */
	private final NodeContext context;

	/**
	 * Listing of {@link TaskNode} instances for this {@link SectionWork}.
	 */
	private final List<TaskNode> taskNodes = new LinkedList<TaskNode>();

	/**
	 * Class name of the {@link WorkSource}.
	 */
	private final String workSourceClassName;

	/**
	 * {@link WorkSource} instance to use. If this is specified it overrides
	 * using the {@link Class} name.
	 */
	@SuppressWarnings("unused")
	private final WorkSource<?> workSource;

	/**
	 * Initial {@link SectionTask} for this {@link SectionWork}.
	 */
	private SectionTask initialTask = null;

	/**
	 * {@link WorkType} for this {@link WorkNode}.
	 */
	private WorkType<?> workType = null;

	/**
	 * Flag indicating if the {@link WorkType} is loaded (or at least attempted
	 * to be loaded).
	 */
	private boolean isWorkTypeLoaded = false;

	/**
	 * Instantiate.
	 * 
	 * @param workName
	 *            Name of this {@link SectionWork}.
	 * @param workSourceClassName
	 *            Class name of the {@link WorkSource}.
	 * @param workSource
	 *            Optional instantiated {@link WorkSource}. May be
	 *            <code>null</code>.
	 * @param section
	 *            {@link OfficeSection} containing this {@link WorkNode}.
	 * @param context
	 *            {@link NodeContext}.
	 */
	public WorkNodeImpl(String workName, String workSourceClassName,
			WorkSource<?> workSource, SectionNode section, NodeContext context) {
		this.workName = workName;
		this.workSourceClassName = workSourceClassName;
		this.workSource = workSource;
		this.section = section;
		this.context = context;

		// Create additional objects
		this.propertyList = this.context.createPropertyList();
	}

	/*
	 * ======================== Node ============================
	 */

	@Override
	public String getNodeName() {
		// TODO implement Node.getNodeName
		throw new UnsupportedOperationException(
				"TODO implement Node.getNodeName");

	}

	@Override
	public String getNodeType() {
		// TODO implement Node.getNodeType
		throw new UnsupportedOperationException(
				"TODO implement Node.getNodeType");

	}

	@Override
	public String getLocation() {
		// TODO implement Node.getLocation
		throw new UnsupportedOperationException(
				"TODO implement Node.getLocation");

	}

	@Override
	public Node getParentNode() {
		// TODO implement Node.getParentNode
		throw new UnsupportedOperationException(
				"TODO implement Node.getParentNode");

	}

	/*
	 * ======================== SectionWork ============================
	 */

	@Override
	public String getSectionWorkName() {
		return this.workName;
	}

	@Override
	public void addProperty(String name, String value) {
		this.propertyList.addProperty(name).setValue(value);
	}

	@Override
	public SectionTask addSectionTask(String taskName, String taskTypeName) {
		// Obtain and return the section task for the name
		TaskNode task = this.section.getTaskNode(taskName);
		if (task == null) {
			// Add the section task
			task = this.section.createTaskNode(taskName, taskTypeName, this);
			this.taskNodes.add(task);
		} else {
			// Section task already added
			this.context.getCompilerIssues().addIssue(this,
					"Section task " + taskName + " already added");
		}
		return task;
	}

	@Override
	public void setInitialTask(SectionTask task) {
		this.initialTask = task;
	}

	/*
	 * ===================== WorkNode ===================================
	 */

	@Override
	public SectionNode getSectionNode() {
		return this.section;
	}

	@Override
	public String getQualifiedWorkName() {
		return this.section.getSectionQualifiedName(this.workName);
	}

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public WorkType<?> getWorkType() {

		// Determine if work type already loaded
		if (this.isWorkTypeLoaded) {
			return this.workType;
		}

		// Flag work type loaded (as now attempting to load it)
		this.isWorkTypeLoaded = true;

		// Obtain the work source class
		Class<? extends WorkSource> workSourceClass = this.context
				.getWorkSourceClass(this.workSourceClassName, this);
		if (workSourceClass == null) {
			return null; // must obtain work source class
		}

		// Load the work type
		WorkLoader workLoader = this.context.getWorkLoader(this);
		this.workType = workLoader.loadWorkType(workSourceClass,
				this.propertyList);

		// Return the work type
		return this.workType;
	}

	@Override
	@SuppressWarnings("rawtypes")
	public void buildWork(OfficeBuilder builder) {

		// Obtain the work type
		WorkType<?> workType = this.getWorkType();
		if (workType == null) {
			return; // must have WorkType to build work
		}

		// Obtain the fully qualified work name
		String fullyQualifiedWorkName = this.section
				.getSectionQualifiedName(this.workName);

		// Build the work
		WorkBuilder workBuilder = builder.addWork(fullyQualifiedWorkName,
				workType.getWorkFactory());

		// Build the tasks
		for (TaskNode task : this.taskNodes) {
			task.buildTask(workBuilder);
		}

		// Determine if initial task for work
		if (this.initialTask != null) {
			// Specify initial task for work
			String initialTaskName = this.initialTask.getSectionTaskName();
			workBuilder.setInitialTask(initialTaskName);
		}
	}

}