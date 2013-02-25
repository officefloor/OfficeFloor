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
package net.officefloor.plugin.jpa;

import javax.persistence.EntityManager;

import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.execute.TaskContext;
import net.officefloor.frame.spi.managedobject.recycle.RecycleManagedObjectParameter;
import net.officefloor.frame.util.AbstractSingleTask;

/**
 * {@link Task} to close the {@link EntityManager}.
 * 
 * @author Daniel Sagenschneider
 */
// START SNIPPET: tutorial
public class CloseEntityManagerTask
		extends
		AbstractSingleTask<CloseEntityManagerTask, CloseEntityManagerTask.CloseEntityManagerDependencies, None> {

	/**
	 * Recycle {@link Task} dependencies to close the {@link EntityManager}.
	 */
	public static enum CloseEntityManagerDependencies {
		MANAGED_OBJECT
	}

	/*
	 * ============================ Task ==========================
	 */

	@Override
	@SuppressWarnings("unchecked")
	public Object doTask(
			TaskContext<CloseEntityManagerTask, CloseEntityManagerDependencies, None> context)
			throws Throwable {

		// Obtain the recycle parameter
		RecycleManagedObjectParameter<JpaEntityManagerManagedObject> parameter = (RecycleManagedObjectParameter<JpaEntityManagerManagedObject>) context
				.getObject(CloseEntityManagerDependencies.MANAGED_OBJECT);

		// Obtain the managed object
		JpaEntityManagerManagedObject managedObject = parameter
				.getManagedObject();

		// Close the Entity Manager
		managedObject.closeEntityManager();

		// Closed, nothing further
		return null;
	}

}
// END SNIPPET: tutorial