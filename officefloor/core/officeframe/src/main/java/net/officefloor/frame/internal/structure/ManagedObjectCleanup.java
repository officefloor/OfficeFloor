/*-
 * #%L
 * OfficeFrame
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package net.officefloor.frame.internal.structure;

import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.pool.ManagedObjectPool;

/**
 * Manages the clean up of {@link ManagedObject} instances.
 *
 * @author Daniel Sagenschneider
 */
public interface ManagedObjectCleanup {

	/**
	 * Creates the clean up {@link FunctionState}.
	 * 
	 * @param recycleFlowMetaData
	 *            {@link FlowMetaData} to recycle the {@link ManagedObject}.
	 * @param objectType
	 *            Type of the object from the {@link ManagedObject}.
	 * @param managedObject
	 *            {@link ManagedObject} to be cleaned up.
	 * @param managedObjectPool
	 *            Optional {@link ManagedObjectPool} to return the
	 *            {@link ManagedObject}. May be <code>null</code>.
	 * @return {@link FunctionState} to clean up the {@link ManagedObject}.
	 */
	FunctionState cleanup(FlowMetaData recycleFlowMetaData, Class<?> objectType, ManagedObject managedObject,
			ManagedObjectPool managedObjectPool);

}
