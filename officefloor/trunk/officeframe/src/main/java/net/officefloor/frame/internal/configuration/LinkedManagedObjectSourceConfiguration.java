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
package net.officefloor.frame.internal.configuration;

import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;

/**
 * Configuration linking in a {@link ManagedObjectSource}.
 * 
 * @author Daniel Sagenschneider
 */
public interface LinkedManagedObjectSourceConfiguration {

	/**
	 * Obtains the name of the {@link OfficeFloor} {@link ManagedObjectSource}
	 * instance.
	 * 
	 * @return Name of the {@link OfficeFloor} {@link ManagedObjectSource}
	 *         instance.
	 */
	String getOfficeFloorManagedObjectSourceName();

	/**
	 * Obtains the name that the {@link ManagedObject} is registered within the
	 * {@link Office}.
	 * 
	 * @return Name that the {@link ManagedObject} is registered within the
	 *         {@link Office}.
	 */
	String getOfficeManagedObjectName();

}