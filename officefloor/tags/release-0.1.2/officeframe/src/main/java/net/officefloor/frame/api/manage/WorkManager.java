/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2010 Daniel Sagenschneider
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

import net.officefloor.frame.api.execute.FlowFuture;
import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.spi.managedobject.ManagedObject;

/**
 * Interface to manage a particular type of {@link Work}.
 * 
 * @author Daniel Sagenschneider
 */
public interface WorkManager {

	/**
	 * Invokes a new instance of {@link Work} which is done within the
	 * {@link Office}.
	 * 
	 * @param parameter
	 *            Parameter for the first {@link Task} of the {@link Work}.
	 * @throws NoInitialTaskException
	 *             If {@link Work} does not have an initial {@link Task}.
	 */
	FlowFuture invokeWork(Object parameter) throws NoInitialTaskException;

	/**
	 * <p>
	 * Obtains a {@link ManagedObject} within the {@link Work} scope by the
	 * input name.
	 * <p>
	 * As {@link ManagedObject} instances may be dependent on other
	 * {@link ManagedObject} instances, they require the {@link Work} to specify
	 * these dependencies.
	 * 
	 * @param managedObjectName
	 *            Name of the {@link ManagedObject}.
	 * @return {@link ManagedObject} for the input name.
	 * @throws Throwable
	 *             If fails to obtain the {@link ManagedObject}.
	 */
	ManagedObject getManagedObject(String managedObjectName) throws Throwable;

}