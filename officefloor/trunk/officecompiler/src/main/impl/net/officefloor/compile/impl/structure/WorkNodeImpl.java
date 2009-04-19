/*
 *  Office Floor, Application Server
 *  Copyright (C) 2006 Daniel Sagenschneider
 *
 *  This program is free software; you can redistribute it and/or modify it under the terms 
 *  of the GNU General Public License as published by the Free Software Foundation; either 
 *  version 2 of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with this program; 
 *  if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, 
 *  MA 02111-1307 USA
 */
package net.officefloor.compile.impl.structure;

import java.util.Map;

import net.officefloor.compile.impl.properties.PropertyListImpl;
import net.officefloor.compile.internal.structure.NodeContext;
import net.officefloor.compile.internal.structure.TaskNode;
import net.officefloor.compile.internal.structure.WorkNode;
import net.officefloor.compile.issues.CompilerIssues.LocationType;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.spi.section.SectionTask;
import net.officefloor.compile.spi.section.SectionWork;
import net.officefloor.compile.spi.work.source.WorkSource;
import net.officefloor.frame.api.build.OfficeFloorIssues.AssetType;

/**
 * {@link WorkNode} implementation.
 * 
 * @author Daniel
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
	 * {@link NodeContext}.
	 */
	private final NodeContext context;

	/**
	 * Map of {@link TaskNode} instances for the {@link OfficeSection}
	 * containing this {@link SectionWork} by their {@link SectionTask} names.
	 */
	private final Map<String, TaskNode> sectionTaskNodes;

	/**
	 * Class name of the {@link WorkSource}.
	 */
	private final String workSourceClassName;

	/**
	 * {@link WorkSource} instance.
	 */
	private WorkSource<?> workSource;

	/**
	 * Initiate.
	 * 
	 * @param workName
	 *            Name of this {@link SectionWork}.
	 * @param workSourceClassName
	 *            Class name of the {@link WorkSource}.
	 * @param workSource
	 *            {@link WorkSource} instance. May be <code>null</code>.
	 * @param sectionLocation
	 *            Location of the {@link OfficeSection} containing this
	 *            {@link WorkNode}.
	 * @param sectionTaskNodes
	 *            Map of {@link TaskNode} instances for the
	 *            {@link OfficeSection} containing this {@link SectionWork} by
	 *            their {@link SectionTask} names.
	 * @param context
	 *            {@link NodeContext}.
	 */
	public WorkNodeImpl(String workName, String workSourceClassName,
			WorkSource<?> workSource, String sectionLocation,
			Map<String, TaskNode> sectionTaskNodes, NodeContext context) {
		this.workName = workName;
		this.workSourceClassName = workSourceClassName;
		this.workSource = workSource;
		this.sectionLocation = sectionLocation;
		this.sectionTaskNodes = sectionTaskNodes;
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
					this.sectionLocation, this.context);
			this.sectionTaskNodes.put(taskName, task);
		} else {
			// Section task already added
			this.addIssue("Section task " + taskName + " already added");
		}
		return task;
	}

}