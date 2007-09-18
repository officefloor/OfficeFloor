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
package net.officefloor.frame.internal.configuration;

/**
 * Configuration of a
 * {@link net.officefloor.frame.spi.managedobject.ManagedObject} for use.
 * 
 * @author Daniel
 */
public interface ManagedObjectConfiguration {

	/**
	 * Obtains the Id of the
	 * {@link net.officefloor.frame.spi.managedobject.ManagedObject}.
	 * 
	 * @return Id of the
	 *         {@link net.officefloor.frame.spi.managedobject.ManagedObject}.
	 */
	String getManagedObjectId();

	/**
	 * Obtains the name that the {@link net.officefloor.frame.api.execute.Work}
	 * and its {@link net.officefloor.frame.api.execute.Task} refer to this
	 * {@link net.officefloor.frame.spi.managedobject.ManagedObject}.
	 * 
	 * @return Local name.
	 */
	String getManagedObjectName();

	/**
	 * Obtains the timeout for operations on the
	 * {@link net.officefloor.frame.spi.managedobject.ManagedObject}.
	 * 
	 * @return Timeout for operations on the
	 *         {@link net.officefloor.frame.spi.managedobject.ManagedObject}.
	 */
	long getTimeout();

	/**
	 * Obtains the listing of {@link ManagedObjectDependencyConfiguration}
	 * instances.
	 * 
	 * @return {@link ManagedObjectDependencyConfiguration} instances.
	 */
	ManagedObjectDependencyConfiguration<?>[] getDependencyConfiguration();

}
