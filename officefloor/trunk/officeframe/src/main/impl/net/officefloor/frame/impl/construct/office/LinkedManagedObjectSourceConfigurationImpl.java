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
package net.officefloor.frame.impl.construct.office;

import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.internal.configuration.LinkedManagedObjectSourceConfiguration;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;

/**
 * {@link LinkedManagedObjectSourceConfiguration} implementation.
 * 
 * @author Daniel
 */
public class LinkedManagedObjectSourceConfigurationImpl implements
		LinkedManagedObjectSourceConfiguration {

	/**
	 * {@link Office} name of the {@link ManagedObject}.
	 */
	private final String officeManagedObjectName;

	/**
	 * {@link OfficeFloor} name of the {@link ManagedObjectSource}.
	 */
	private final String officeFloorManagedObjectSourceName;

	/**
	 * Initiate.
	 * 
	 * @param officeManagedObjectName
	 *            {@link Office} name of the {@link ManagedObject}.
	 * @param officeFloorManagedObjectSourceName
	 *            {@link OfficeFloor} name of the {@link ManagedObjectSource}.
	 */
	public LinkedManagedObjectSourceConfigurationImpl(String officeManagedObjectName,
			String officeFloorManagedObjectSourceName) {
		this.officeManagedObjectName = officeManagedObjectName;
		this.officeFloorManagedObjectSourceName = officeFloorManagedObjectSourceName;
	}

	/*
	 * ================ LinkedManagedObjectSourceConfiguration ================
	 */

	@Override
	public String getOfficeManagedObjectName() {
		return this.officeManagedObjectName;
	}

	@Override
	public String getOfficeFloorManagedObjectSourceName() {
		return this.officeFloorManagedObjectSourceName;
	}

}