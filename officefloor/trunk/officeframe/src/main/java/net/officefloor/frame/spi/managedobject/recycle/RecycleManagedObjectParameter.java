/*
 * Office Floor, Application Server
 * Copyright (C) 2005-2009 Daniel Sagenschneider
 *
 * This program is free software; you can redistribute it and/or modify it under the terms
 * of the GNU General Public License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program;
 * if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston,
 * MA 02111-1307 USA
 */
package net.officefloor.frame.spi.managedobject.recycle;

import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.spi.managedobject.ManagedObject;

/**
 * Parameter to the recycle {@link Task}.
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

}
