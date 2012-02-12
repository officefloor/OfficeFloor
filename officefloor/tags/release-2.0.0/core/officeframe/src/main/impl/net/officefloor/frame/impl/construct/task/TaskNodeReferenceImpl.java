/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2011 Daniel Sagenschneider
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

package net.officefloor.frame.impl.construct.task;

import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.internal.configuration.TaskNodeReference;

/**
 * {@link TaskNodeReference} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class TaskNodeReferenceImpl implements TaskNodeReference {

	/**
	 * Name identifying the {@link Work} containing the {@link Task}.
	 */
	private final String workName;

	/**
	 * Name of the {@link Task}.
	 */
	private final String taskName;

	/**
	 * Type of argument to be passed to the referenced {@link Task}.
	 */
	private final Class<?> argumentType;

	/**
	 * Initiate.
	 * 
	 * @param workName
	 *            Name identifying the {@link Work} containing the {@link Task}.
	 * @param taskName
	 *            Name of the {@link Task}.
	 * @param argumentType
	 *            Type of argument to be passed to the referenced {@link Task}.
	 */
	public TaskNodeReferenceImpl(String workName, String taskName,
			Class<?> argumentType) {
		this.workName = workName;
		this.taskName = taskName;
		this.argumentType = argumentType;
	}

	/**
	 * Initiate.
	 * 
	 * @param taskName
	 *            Name of the {@link Task}.
	 * @param argumentType
	 *            Type of argument to be passed to the referenced {@link Task}.
	 */
	public TaskNodeReferenceImpl(String taskName, Class<?> argumentType) {
		this(null, taskName, argumentType);
	}

	/*
	 * =================== TaskNodeReference ==============================
	 */

	@Override
	public String getWorkName() {
		return this.workName;
	}

	@Override
	public String getTaskName() {
		return this.taskName;
	}

	@Override
	public Class<?> getArgumentType() {
		return this.argumentType;
	}

}