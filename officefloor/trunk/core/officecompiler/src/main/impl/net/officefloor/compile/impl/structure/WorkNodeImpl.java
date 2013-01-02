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
import java.util.Map;

import net.officefloor.compile.impl.properties.PropertyListImpl;
import net.officefloor.compile.internal.structure.NodeContext;
import net.officefloor.compile.internal.structure.SectionNode;
import net.officefloor.compile.internal.structure.TaskNode;
import net.officefloor.compile.internal.structure.WorkNode;
import net.officefloor.compile.issues.CompilerIssues.LocationType;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.spi.section.SectionTask;
import net.officefloor.compile.spi.section.SectionWork;
import net.officefloor.compile.spi.work.source.WorkSource;
import net.officefloor.compile.work.WorkLoader;
import net.officefloor.compile.work.WorkType;
import net.officefloor.frame.api.build.OfficeBuilder;
import net.officefloor.frame.api.build.WorkBuilder;
import net.officefloor.frame.api.build.OfficeFloorIssues.AssetType;
import net.officefloor.frame.api.manage.Office;

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
	private final PropertyList propertyList = new PropertyListImpl();

	/**
	 * Location of the {@link OfficeSection} containing this {@link WorkNode}.
	 */
	private final String sectionLocation;

	/**
	 * {@link OfficeSection} containing this {@link WorkNode}.
	 */
	private final SectionNode section;

	/**
	 * {@link NodeContext}.
	 */
	private final NodeContext context;

	/**
	 * Map of {@link TaskNode} instances for the {@link OfficeSection}
	 * containing this {@link SectionWork} by their {@link SectionTask} names.
	 */
	private final Map<String, TaskNode> sectionTaskNodes;

	/**
	 * Listing of {@link TaskNode} instances for this {@link SectionWork}.
	 */
	private final List<TaskNode> workTaskNodes = new LinkedList<TaskNode>();

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
	 * Flag indicating if within the {@link Office} context.
	 */
	private boolean isWithinOfficeContext = false;

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
	 * Initiate.
	 * 
	 * @param workName
	 *            Name of this {@link SectionWork}.
	 * @param workSourceClassName
	 *            Class name of the {@link WorkSource}.
	 * @param sectionLocation
	 *            Location of the {@link OfficeSection} containing this
	 *            {@link WorkNode}.
	 * @param sectionTaskNodes
	 *            Map of {@link TaskNode} instances for the
	 *            {@link OfficeSection} containing this {@link SectionWork} by
	 *            their {@link SectionTask} names.
	 * @param section
	 *            {@link OfficeSection} containing this {@link WorkNode}.
	 * @param context
	 *            {@link NodeContext}.
	 */
	public WorkNodeImpl(String workName, String workSourceClassName,
			String sectionLocation, Map<String, TaskNode> sectionTaskNodes,
			SectionNode section, NodeContext context) {
		this.workName = workName;
		this.workSourceClassName = workSourceClassName;
		this.workSource = null;
		this.sectionLocation = sectionLocation;
		this.sectionTaskNodes = sectionTaskNodes;
		this.section = section;
		this.context = context;
	}

	/**
	 * Initiate.
	 * 
	 * @param workName
	 *            Name of this {@link SectionWork}.
	 * @param workSource
	 *            {@link WorkSource} instance to use.
	 * @param sectionLocation
	 *            Location of the {@link OfficeSection} containing this
	 *            {@link WorkNode}.
	 * @param sectionTaskNodes
	 *            Map of {@link TaskNode} instances for the
	 *            {@link OfficeSection} containing this {@link SectionWork} by
	 *            their {@link SectionTask} names.
	 * @param section
	 *            {@link OfficeSection} containing this {@link WorkNode}.
	 * @param context
	 *            {@link NodeContext}.
	 */
	public WorkNodeImpl(String workName, WorkSource<?> workSource,
			String sectionLocation, Map<String, TaskNode> sectionTaskNodes,
			SectionNode section, NodeContext context) {
		this.workName = workName;
		this.workSourceClassName = null;
		this.workSource = workSource;
		this.sectionLocation = sectionLocation;
		this.sectionTaskNodes = sectionTaskNodes;
		this.section = section;
		this.context = context;
	}

	/**
	 * Adds an issue regarding the {@link OfficeSection} being built.
	 * 
	 * @param issueDescription
	 *            Description of the issue.
	 */
	private void addIssue(String issueDescription) {
		this.context.getCompilerIssues().addIssue(LocationType.SECTION,
				this.sectionLocation, AssetType.WORK, this.workName,
				issueDescription);
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
		TaskNode task = this.sectionTaskNodes.get(taskName);
		if (task == null) {
			// Add the section task
			task = new TaskNodeImpl(taskName, taskTypeName,
					this.sectionLocation, this, this.context);
			this.sectionTaskNodes.put(taskName, task);
			this.workTaskNodes.add(task);
		} else {
			// Section task already added
			this.addIssue("Section task " + taskName + " already added");
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
	public void addOfficeContext(String officeLocation) {
		// Do not need office location, only that to know within Office context
		this.isWithinOfficeContext = true;
	}

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

		// Ensure within office context
		if (!this.isWithinOfficeContext) {
			throw new IllegalStateException("Office context has not been added");
		}

		// Determine if work type already loaded
		if (this.isWorkTypeLoaded) {
			return this.workType;
		}

		// Flag work type loaded (as now attempting to load it)
		this.isWorkTypeLoaded = true;

		// Obtain the work source class
		Class<? extends WorkSource> workSourceClass = this.context
				.getWorkSourceClass(this.workSourceClassName,
						this.sectionLocation, this.workName);
		if (workSourceClass == null) {
			return null; // must obtain work source class
		}

		// Load the work type
		WorkLoader workLoader = this.context.getWorkLoader(
				this.sectionLocation, this.workName);
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
		for (TaskNode task : this.workTaskNodes) {
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