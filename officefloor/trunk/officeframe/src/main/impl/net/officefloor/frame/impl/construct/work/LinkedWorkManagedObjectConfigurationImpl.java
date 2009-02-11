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
package net.officefloor.frame.impl.construct.work;

import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.internal.configuration.LinkedWorkManagedObjectConfiguration;
import net.officefloor.frame.internal.structure.ProcessState;
import net.officefloor.frame.internal.structure.ThreadState;
import net.officefloor.frame.spi.managedobject.ManagedObject;

/**
 * {@link LinkedWorkManagedObjectConfiguration} implementation.
 * 
 * @author Daniel
 */
public class LinkedWorkManagedObjectConfigurationImpl implements
		LinkedWorkManagedObjectConfiguration {

	/**
	 * Name of {@link ManagedObject} within {@link Work}.
	 */
	private final String workManagedObjectName;

	/**
	 * Name of {@link ThreadState} or {@link ProcessState} bound
	 * {@link ManagedObject}.
	 */
	private final String boundManagedObjectName;

	/**
	 * Initiate.
	 * 
	 * @param workManagedObjectName
	 *            Name of {@link ManagedObject} within {@link Work}.
	 * @param boundManagedObjectName
	 *            Name of {@link ThreadState} or {@link ProcessState} bound
	 *            {@link ManagedObject}.
	 */
	public LinkedWorkManagedObjectConfigurationImpl(
			String workManagedObjectName, String boundManagedObjectName) {
		super();
		this.workManagedObjectName = workManagedObjectName;
		this.boundManagedObjectName = boundManagedObjectName;
	}

	/*
	 * =============== LinkedWorkManagedObjectConfiguration ==============
	 */

	@Override
	public String getWorkManagedObjectName() {
		return this.workManagedObjectName;
	}

	@Override
	public String getBoundManagedObjectName() {
		return this.boundManagedObjectName;
	}

}
