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
package net.officefloor.eclipse.wizard.worksource;

import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.work.source.WorkSource;
import net.officefloor.compile.work.TaskType;
import net.officefloor.compile.work.WorkType;
import net.officefloor.frame.api.execute.Work;

/**
 * Instance of a {@link Work}.
 * 
 * @author Daniel
 */
public class WorkInstance {

	/**
	 * Name of this {@link Work}.
	 */
	private final String workName;

	/**
	 * {@link WorkSource} class name.
	 */
	private final String workSourceClassName;

	/**
	 * {@link PropertyList}.
	 */
	private final PropertyList propertyList;

	/**
	 * {@link WorkType}.
	 */
	private final WorkType<?> workType;

	/**
	 * {@link TaskType} selected.
	 */
	private final TaskType<?, ?, ?>[] taskTypes;

	/**
	 * Names of the selected {@link TaskType} instances.
	 */
	private final String[] taskTypeNames;

	/**
	 * Initiate for public use.
	 * 
	 * @param workName
	 *            Name of the {@link Work}.
	 * @param workSourceClassName
	 *            {@link WorkSource} class name.
	 * @param taskTypeNames
	 *            Names of the {@link TaskType} instances being used on the
	 *            {@link WorkType}.
	 */
	public WorkInstance(String workName, String workSourceClassName,
			String... taskTypeNames) {
		this.workName = workName;
		this.workSourceClassName = workSourceClassName;
		this.propertyList = OfficeFloorCompiler.newPropertyList();
		this.workType = null;
		this.taskTypes = null;
		this.taskTypeNames = (taskTypeNames == null ? new String[0]
				: taskTypeNames);
	}

	/**
	 * Initiate from {@link WorkSourceInstance}.
	 * 
	 * @param workName
	 *            Name of the {@link Work}.
	 * @param workSourceClassName
	 *            {@link WorkSource} class name.
	 * @param propertyList
	 *            {@link PropertyList}.
	 * @param workType
	 *            {@link WorkType}.
	 * @param taskTypes
	 *            {@link TaskType} selected.
	 */
	WorkInstance(String workName, String workSourceClassName,
			PropertyList propertyList, WorkType<?> workType,
			TaskType<?, ?, ?>[] taskTypes) {
		this.workName = workName;
		this.workSourceClassName = workSourceClassName;
		this.propertyList = propertyList;
		this.workType = workType;
		this.taskTypes = taskTypes;

		// Create the listing of task type names
		this.taskTypeNames = new String[this.taskTypes.length];
		for (int i = 0; i < this.taskTypeNames.length; i++) {
			this.taskTypeNames[i] = this.taskTypes[i].getTaskName();
		}
	}

	/**
	 * Obtains the name of the {@link Work}.
	 * 
	 * @return Name of the {@link Work}.
	 */
	public String getWorkName() {
		return this.workName;
	}

	/**
	 * Obtains the {@link WorkSource} class name.
	 * 
	 * @return {@link WorkSource} class name.
	 */
	public String getWorkSourceClassName() {
		return this.workSourceClassName;
	}

	/**
	 * Obtains the {@link PropertyList}.
	 * 
	 * @return {@link PropertyList}.
	 */
	public PropertyList getPropertylist() {
		return this.propertyList;
	}

	/**
	 * Obtains the {@link WorkType}.
	 * 
	 * @return {@link WorkType} if obtained from {@link WorkSourceInstance} or
	 *         <code>null</code> if initiated by <code>public</code>
	 *         constructor.
	 */
	public WorkType<?> getWorkType() {
		return this.workType;
	}

	/**
	 * Obtains the {@link TaskType} instances.
	 * 
	 * @return {@link TaskType} instances if obtained from
	 *         {@link WorkSourceInstance} or <code>null</code> if initiated by
	 *         <code>public</code> constructor.
	 */
	public TaskType<?, ?, ?>[] getTaskTypes() {
		return this.taskTypes;
	}

	/**
	 * Obtains the names of the {@link TaskType} instances being used on the
	 * {@link WorkType}.
	 * 
	 * @return Names of the {@link TaskType} instances being used on the
	 *         {@link WorkType}.
	 */
	public String[] getTaskTypeNames() {
		return this.taskTypeNames;
	}
}