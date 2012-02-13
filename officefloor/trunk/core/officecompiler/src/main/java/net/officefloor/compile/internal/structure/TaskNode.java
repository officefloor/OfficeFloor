/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2012 Daniel Sagenschneider
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

package net.officefloor.compile.internal.structure;

import net.officefloor.compile.spi.office.OfficeTask;
import net.officefloor.compile.spi.section.SectionTask;
import net.officefloor.compile.work.TaskType;
import net.officefloor.frame.api.build.WorkBuilder;
import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.api.manage.Office;

/**
 * {@link SectionTask} node.
 * 
 * @author Daniel Sagenschneider
 */
public interface TaskNode extends SectionTask, OfficeTask, LinkFlowNode {

	/**
	 * <p>
	 * Adds context of the {@link Office} containing this {@link OfficeTask}.
	 * <p>
	 * The {@link TaskType} can not be added until the {@link Office} context is
	 * provided.
	 * 
	 * @param officeLocation
	 *            Location of the {@link Office}.
	 */
	void addOfficeContext(String officeLocation);

	/**
	 * Obtains the {@link WorkNode} containing this {@link TaskNode}.
	 * 
	 * @return {@link WorkNode} containing this {@link TaskNode}.
	 */
	WorkNode getWorkNode();

	/**
	 * Obtains the {@link TaskType} for this {@link TaskNode}.
	 * 
	 * @return {@link TaskType} for this {@link TaskNode}. May be
	 *         <code>null</code> if can not determine {@link TaskType}.
	 */
	TaskType<?, ?, ?> getTaskType();

	/**
	 * Builds the {@link Task} for this {@link TaskNode}.
	 * 
	 * @param workBuilder
	 *            {@link WorkBuilder} for the {@link Work} of this {@link Task}.
	 */
	void buildTask(WorkBuilder<?> workBuilder);

}