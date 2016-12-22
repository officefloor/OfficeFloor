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

import net.officefloor.frame.api.build.ManagedFunctionBuilder;
import net.officefloor.frame.api.execute.ManagedFunction;
import net.officefloor.frame.internal.structure.ProcessCompletionListener;

/**
 * Interface to manage a particular {@link ManagedFunction}.
 * 
 * @author Daniel Sagenschneider
 */
public interface TaskManager {

	/**
	 * Obtains the differentiator for this {@link ManagedFunction}.
	 * 
	 * @return Differentiator for this {@link ManagedFunction}. May be <code>null</code> if
	 *         no differentiator for {@link ManagedFunction}.
	 * 
	 * @see ManagedFunctionBuilder#setDifferentiator(Object)
	 */
	Object getDifferentiator();

	/**
	 * Obtains the parameter type for invoking this {@link ManagedFunction}.
	 * 
	 * @return Parameter type for invoking the {@link ManagedFunction}. Will be
	 *         <code>null</code> if no parameter to the {@link ManagedFunction}.
	 */
	Class<?> getParameterType();

	/**
	 * Invokes the {@link ManagedFunction} which is done within the {@link Office}.
	 * 
	 * @param parameter
	 *            Parameter for the {@link ManagedFunction}.
	 * @param completionListener
	 *            {@link ProcessCompletionListener}.
	 * @throws InvalidParameterTypeException
	 *             Should the parameter be of incorrect type for the
	 *             {@link ManagedFunction}.
	 */
	void invokeTask(Object parameter, ProcessCompletionListener completionListener)
			throws InvalidParameterTypeException;

}