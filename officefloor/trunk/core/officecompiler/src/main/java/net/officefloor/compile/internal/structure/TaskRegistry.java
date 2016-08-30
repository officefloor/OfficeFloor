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
package net.officefloor.compile.internal.structure;

import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.execute.Work;

/**
 * Registry of the {@link TaskNode} instances.
 *
 * @author Daniel Sagenschneider
 */
public interface TaskRegistry {

	/**
	 * Obtains the {@link TaskNode} from the registry.
	 * 
	 * @param taskName
	 *            Name of the {@link TaskNode} to obtain.
	 * @return {@link TaskNode} or <code>null</code> if no {@link TaskNode}
	 *         registered.
	 */
	TaskNode getTaskNode(String taskName);

	/**
	 * Creates a new {@link TaskNode} and registers it.
	 * 
	 * @param taskName
	 *            Name of the {@link TaskNode}.
	 * @param taskTypeName
	 *            Type name of the {@link Task} within the {@link Work}.
	 * @param work
	 *            Parent {@link WorkNode}.
	 * @return {@link TaskNode}.
	 */
	TaskNode createTaskNode(String taskName, String taskTypeName, WorkNode work);

}