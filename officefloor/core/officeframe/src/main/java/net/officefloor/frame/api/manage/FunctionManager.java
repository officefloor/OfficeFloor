/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package net.officefloor.frame.api.manage;

import net.officefloor.frame.api.function.FlowCallback;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.internal.structure.ProcessState;

/**
 * Interface to manage a particular {@link ManagedFunction}.
 * 
 * @author Daniel Sagenschneider
 */
public interface FunctionManager {

	/**
	 * Obtains the annotations for this {@link ManagedFunction}.
	 * 
	 * @return Annotations for this {@link ManagedFunction}.
	 */
	Object[] getAnnotations();

	/**
	 * Obtains the parameter type for invoking this {@link ManagedFunction}.
	 * 
	 * @return Parameter type for invoking the {@link ManagedFunction}. Will be
	 *         <code>null</code> if no parameter to the {@link ManagedFunction}.
	 */
	Class<?> getParameterType();

	/**
	 * Invokes the {@link ManagedFunction} which is executed within a new
	 * {@link ProcessState} of the {@link Office}.
	 * 
	 * @param parameter
	 *            Parameter for the {@link ManagedFunction}.
	 * @param callback
	 *            Optional {@link FlowCallback}. May be <code>null</code>.
	 * @throws InvalidParameterTypeException
	 *             Should the parameter be of incorrect type for the
	 *             {@link ManagedFunction}.
	 */
	void invokeProcess(Object parameter, FlowCallback callback) throws InvalidParameterTypeException;

}