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
package net.officefloor.frame.impl.construct.task;

import net.officefloor.frame.internal.configuration.TaskManagedObjectConfiguration;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.frame.spi.managedobject.ManagedObject;

/**
 * {@link TaskManagedObjectConfiguration} implementation.
 * 
 * @author Daniel
 */
public class TaskManagedObjectConfigurationImpl implements
		TaskManagedObjectConfiguration {

	/**
	 * Name of {@link ManagedObject} within the {@link ManagedObjectScope}.
	 */
	protected final String scopeManagedObjectName;

	/**
	 * Initiate.
	 * 
	 * @param scopeManagedObjectName
	 *            Name of {@link ManagedObject} within the
	 *            {@link ManagedObjectScope}.
	 */
	public TaskManagedObjectConfigurationImpl(String scopeManagedObjectName) {
		this.scopeManagedObjectName = scopeManagedObjectName;
	}

	/*
	 * =================== TaskManagedObjectConfiguration =====================
	 */

	@Override
	public String getScopeManagedObjectName() {
		return this.scopeManagedObjectName;
	}

}