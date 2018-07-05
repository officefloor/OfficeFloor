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
package net.officefloor.frame.api.managedobject.source;

import net.officefloor.frame.api.managedobject.ManagedObject;

/**
 * User interested in using the {@link ManagedObject}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ManagedObjectUser {

	/**
	 * <p>
	 * Specifies the {@link ManagedObject} to be used.
	 * <p>
	 * This will be called by the
	 * {@link ManagedObjectSource#sourceManagedObject(ManagedObjectUser)} method
	 * to provide the {@link ManagedObject} to this {@link ManagedObjectUser}.
	 * 
	 * @param managedObject
	 *            {@link ManagedObject} to be used.
	 */
	void setManagedObject(ManagedObject managedObject);

	/**
	 * Indicates failure to obtain the {@link ManagedObject}.
	 * 
	 * @param cause
	 *            Cause of the failure.
	 */
	void setFailure(Throwable cause);

}