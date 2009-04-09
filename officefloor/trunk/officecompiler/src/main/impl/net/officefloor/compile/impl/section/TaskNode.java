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
package net.officefloor.compile.impl.section;

import net.officefloor.compile.spi.section.SectionTask;
import net.officefloor.compile.spi.section.TaskFlow;
import net.officefloor.compile.spi.section.TaskObject;
import net.officefloor.compile.work.TaskType;

/**
 * {@link SectionTask} node.
 * 
 * @author Daniel
 */
public class TaskNode implements SectionTask {

	/**
	 * Name of this {@link SectionTask}.
	 */
	private final String taskName;

	/**
	 * Name of the {@link TaskType} for this {@link SectionTask}.
	 */
	private final String taskTypeName;

	/**
	 * Initiate.
	 * 
	 * @param taskName
	 *            Name of this {@link SectionTask}.
	 * @param taskTypeName
	 *            Name of the {@link TaskType} for this {@link SectionTask}.
	 */
	public TaskNode(String taskName, String taskTypeName) {
		this.taskName = taskName;
		this.taskTypeName = taskTypeName;
	}

	/*
	 * ====================== SectionTask =============================
	 */

	@Override
	public String getSectionTaskName() {
		return this.taskName;
	}

	@Override
	public TaskFlow getTaskEscalation(String taskEscalationName) {
		// TODO Implement
		throw new UnsupportedOperationException(
				"TODO implement SectionTask.getTaskEscalation");
	}

	@Override
	public TaskFlow getTaskFlow(String taskFlowName) {
		// TODO Implement
		throw new UnsupportedOperationException(
				"TODO implement SectionTask.getTaskFlow");
	}

	@Override
	public TaskObject getTaskObject(String taskObjectName) {
		// TODO Implement
		throw new UnsupportedOperationException(
				"TODO implement SectionTask.getTaskObject");
	}

}