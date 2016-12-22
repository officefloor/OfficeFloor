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

import net.officefloor.compile.issues.CompilerIssue;
import net.officefloor.frame.api.execute.ManagedFunction;
import net.officefloor.frame.api.execute.Work;

/**
 * Registry of the {@link TaskNode} instances.
 *
 * @author Daniel Sagenschneider
 */
public interface TaskRegistry {

	/**
	 * <p>
	 * Obtains the {@link TaskNode} from the registry.
	 * <p>
	 * The returned {@link TaskNode} may or may not be initialised.
	 * 
	 * @param taskName
	 *            Name of the {@link TaskNode} to obtain.
	 * @return {@link TaskNode} from the registry.
	 */
	TaskNode getTaskNode(String taskName);

	/**
	 * <p>
	 * Adds an initialised {@link TaskNode} to the registry.
	 * <p>
	 * Should an {@link TaskNode} already be added by the name, then an issue is
	 * reported to the {@link CompilerIssue}.
	 * 
	 * @param taskName
	 *            Name of the {@link TaskNode}.
	 * @param taskTypeName
	 *            Type name of the {@link ManagedFunction} within the {@link Work}.
	 * @param workNode
	 *            Parent {@link WorkNode}.
	 * @return Initialised {@link TaskNode} by the name.
	 */
	TaskNode addTaskNode(String taskName, String taskTypeName, WorkNode workNode);

}