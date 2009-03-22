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

import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.internal.structure.ProcessState;
import net.officefloor.frame.internal.structure.ThreadState;
import net.officefloor.frame.spi.managedobject.ManagedObject;

/**
 * Configuration of a {@link ProcessState} or {@link ThreadState} bound
 * {@link ManagedObject}.
 * 
 * @author Daniel
 */
public interface ManagedObjectConfiguration<D extends Enum<D>> {

	/**
	 * Obtains the name of the {@link ManagedObject} registered within the
	 * {@link Office}.
	 * 
	 * @return Name of the {@link ManagedObject} registered within the
	 *         {@link Office}.
	 */
	String getOfficeManagedObjectName();

	/**
	 * Obtains name of the {@link ManagedObject} bound to either
	 * {@link ProcessState} or {@link ThreadState}.
	 * 
	 * @return Name of the {@link ManagedObject} bound to either
	 *         {@link ProcessState} or {@link ThreadState}.
	 */
	String getBoundManagedObjectName();

	/**
	 * Obtains the listing of {@link ManagedObjectDependencyConfiguration}
	 * instances.
	 * 
	 * @return {@link ManagedObjectDependencyConfiguration} instances.
	 */
	ManagedObjectDependencyConfiguration<D>[] getDependencyConfiguration();

}