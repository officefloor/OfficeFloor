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
package net.officefloor.frame.api.manage;

import net.officefloor.frame.api.build.TaskBuilder;
import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.internal.structure.ProcessState;

/**
 * Interface to manage a particular {@link Task}.
 * 
 * @author Daniel Sagenschneider
 */
public interface TaskManager {

	/**
	 * Obtains the differentiator for this {@link Task}.
	 * 
	 * @return Differentiator for this {@link Task}. May be <code>null</code> if
	 *         no differentiator for {@link Task}.
	 * 
	 * @see TaskBuilder#setDifferentiator(Object)
	 */
	Object getDifferentiator();

	/**
	 * Obtains the parameter type for invoking this {@link Task}.
	 * 
	 * @return Parameter type for invoking the {@link Task}. Will be
	 *         <code>null</code> if no parameter to the {@link Task}.
	 */
	Class<?> getParameterType();

	/**
	 * Invokes the {@link Task} which is done within the {@link Office}.
	 * 
	 * @param parameter
	 *            Parameter for the {@link Task}.
	 * @return {@link ProcessFuture} to indicate when the {@link ProcessState}
	 *         executing the {@link Task} has completed.
	 * @throws InvalidParameterTypeException
	 *             Should the parameter be of incorrect type for the
	 *             {@link Task}.
	 */
	ProcessFuture invokeTask(Object parameter)
			throws InvalidParameterTypeException;

}