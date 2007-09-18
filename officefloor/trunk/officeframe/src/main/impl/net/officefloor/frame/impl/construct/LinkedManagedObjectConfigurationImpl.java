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
package net.officefloor.frame.impl.construct;

import net.officefloor.frame.internal.configuration.LinkedManagedObjectConfiguration;

/**
 * Implementation of
 * {@link net.officefloor.frame.internal.configuration.LinkedManagedObjectConfiguration}.
 * 
 * @author Daniel
 */
class LinkedManagedObjectConfigurationImpl implements
		LinkedManagedObjectConfiguration {

	/**
	 * Id of the {@link net.officefloor.frame.spi.managedobject.ManagedObject}.
	 */
	private final String managedObjectId;

	/**
	 * Name of the {@link net.officefloor.frame.spi.managedobject.ManagedObject}.
	 */
	private final String managedObjectName;

	/**
	 * Initiate.
	 * 
	 * @param managedObjectName
	 *            Id of the
	 *            {@link net.officefloor.frame.spi.managedobject.ManagedObject}.
	 * @param managedObjectId
	 *            Name of the
	 *            {@link net.officefloor.frame.spi.managedobject.ManagedObject}.
	 */
	public LinkedManagedObjectConfigurationImpl(String managedObjectName,
			String managedObjectId) {
		this.managedObjectName = managedObjectName;
		this.managedObjectId = managedObjectId;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.internal.configuration.ProcessManagedObjectConfiguration#getManagedObjectId()
	 */
	public String getManagedObjectId() {
		return this.managedObjectId;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.internal.configuration.ProcessManagedObjectConfiguration#getManagedObjectName()
	 */
	public String getManagedObjectName() {
		return this.managedObjectName;
	}

}