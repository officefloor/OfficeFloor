/*
 *  Office Floor, Application Server
 *  Copyright (C) 2006 Daniel Sagenschneider
 *
 *  This program is free software; you can redistribute it and/or modify it under the terms 
 *  of the GNU General Public License as published by the Free Software Foundation; either 
 *  version 2 of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with this program; 
 *  if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, 
 *  MA 02111-1307 USA
 */
package net.officefloor.frame.spi.managedobject.source;

import net.officefloor.frame.spi.managedobject.ManagedObject;

/**
 * User interested in using the
 * {@link net.officefloor.frame.spi.managedobject.ManagedObject}.
 * 
 * @author Daniel
 */
public interface ManagedObjectUser {

	/**
	 * <p>
	 * Specifies the {@link ManagedObject} to be used.
	 * </p>
	 * <p>
	 * This will be called by the
	 * {@link ManagedObjectSource#attainManagedObject(ManagedObjectUser)} method
	 * to provide the {@link ManagedObject} to this {@link ManagedObjectUser}.
	 * </p>
	 * 
	 * @param managedObject
	 *            {@link ManagedObject} to be used.
	 */
	void setManagedObject(ManagedObject managedObject);

	/**
	 * <p>
	 * Indicates failure to obtain the {@link ManagedObject}.
	 * </p>
	 * 
	 * @param cause
	 *            Cause of the failure.
	 */
	void setFailure(Throwable cause);
}
