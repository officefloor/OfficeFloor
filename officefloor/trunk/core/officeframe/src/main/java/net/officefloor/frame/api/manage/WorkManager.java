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

import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.internal.structure.ProcessState;

/**
 * Interface to manage a particular {@link Work}.
 * 
 * @author Daniel Sagenschneider
 */
public interface WorkManager {

	/**
	 * Obtains the parameter type for invoking the {@link Work}.
	 * 
	 * @return Parameter type for invoking the {@link Work}. Will be
	 *         <code>null</code> if no parameter to the {@link Work}.
	 * @throws NoInitialTaskException
	 *             If {@link Work} does not have an initial {@link Task}.
	 */
	Class<?> getWorkParameterType() throws NoInitialTaskException;

	/**
	 * Invokes a new instance of {@link Work} which is done within the
	 * {@link Office}.
	 * 
	 * @param parameter
	 *            Parameter for the first {@link Task} of the {@link Work}.
	 * @return {@link ProcessFuture} to indicate when the {@link ProcessState}
	 *         executing the {@link Work} has completed.
	 * @throws NoInitialTaskException
	 *             If {@link Work} does not have an initial {@link Task}.
	 * @throws InvalidParameterTypeException
	 *             Should the parameter be of incorrect type for the initial
	 *             {@link Task}.
	 */
	ProcessFuture invokeWork(Object parameter) throws NoInitialTaskException,
			InvalidParameterTypeException;

	/**
	 * <p>
	 * Obtains the names of the {@link TaskManager} instances managed by this
	 * {@link WorkManager}.
	 * <p>
	 * This allows to dynamically manage this {@link WorkManager}.
	 * 
	 * @return Names of the {@link TaskManager} instances managed by this
	 *         {@link WorkManager}.
	 */
	String[] getTaskNames();

	/**
	 * Obtains the {@link TaskManager} for the named {@link Task}.
	 * 
	 * @param name
	 *            Name of the {@link Task}.
	 * @return {@link TaskManager} for the named {@link Task}.
	 * @throws UnknownTaskException
	 *             If unknown {@link Task} name.
	 */
	TaskManager getTaskManager(String taskName) throws UnknownTaskException;

}