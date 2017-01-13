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
package net.officefloor.frame.api.managedobject.recycle;

import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.managedobject.ManagedObject;

/**
 * Parameter to the recycle {@link ManagedFunction}.
 * 
 * @author Daniel Sagenschneider
 */
public interface RecycleManagedObjectParameter<MO extends ManagedObject> {

	/**
	 * Obtains the {@link ManagedObject} being recycled.
	 * 
	 * @return {@link ManagedObject} being recycled.
	 */
	MO getManagedObject();

	/**
	 * <p>
	 * Invoked at the end of recycling to re-use the {@link ManagedObject}.
	 * </p>
	 * Should this method not be invoked, the {@link ManagedObject} will be
	 * destroyed.
	 * 
	 * @param managedObject
	 *            {@link ManagedObject} that has been recycled and ready for
	 *            re-use.
	 */
	void reuseManagedObject(MO managedObject);

	/**
	 * Obtains possible {@link CleanupEscalation} instances that occurred in
	 * cleaning up previous {@link ManagedObject} instances.
	 * 
	 * @return Possible {@link CleanupEscalation} instances.
	 */
	CleanupEscalation[] getCleanupEscalations();

}