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

import net.officefloor.frame.api.build.WorkFactory;
import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.internal.structure.ProcessState;
import net.officefloor.frame.internal.structure.ThreadState;
import net.officefloor.frame.spi.administration.Administrator;
import net.officefloor.frame.spi.managedobject.ManagedObject;

/**
 * Provides configuration of {@link Work}.
 * 
 * @author Daniel
 */
public interface WorkConfiguration<W extends Work> {

	/**
	 * Obtains the name of this {@link Work}.
	 * 
	 * @return Name of this {@link Work}.
	 */
	String getWorkName();

	/**
	 * Obtains the {@link WorkFactory} to create the {@link Work}.
	 * 
	 * @return {@link WorkFactory} to create the {@link Work} to be done.
	 */
	WorkFactory<W> getWorkFactory();

	/**
	 * Obtains the {@link ProcessState} and {@link ThreadState} bound
	 * {@link ManagedObject} instances for this {@link Work}.
	 * 
	 * @return Listing of {@link ManagedObject} configuration instances for this
	 *         {@link Work}.
	 */
	LinkedWorkManagedObjectConfiguration[] getLinkedManagedObjectConfiguration();

	/**
	 * Obtains the configuration of the {@link Work} bound {@link ManagedObject}
	 * instances.
	 * 
	 * @return Listing of the {@link ManagedObject} configuration for this
	 *         {@link Work}.
	 */
	ManagedObjectConfiguration<?>[] getManagedObjectConfiguration();

	/**
	 * Obtains the {@link ProcessState} and {@link ThreadState} bound
	 * {@link Administrator} instances for this {@link Work}.
	 * 
	 * @return Listing of {@link Administrator} configuration instances for this
	 *         {@link Work}.
	 */
	LinkedWorkAdministratorConfiguration[] getLinkedAdministratorConfiguration();

	/**
	 * Obtains the configuration of the {@link Work} bound {@link Administrator}
	 * instances.
	 * 
	 * @return Listing of {@link Administrator} configuration this {@link Work}.
	 */
	AdministratorSourceConfiguration<?, ?>[] getAdministratorConfiguration();

	/**
	 * Obtains the name of the initial {@link Task} of the {@link Work}.
	 * 
	 * @return Name of the initial {@link Task} of the {@link Work}.
	 */
	String getInitialTaskName();

	/**
	 * Obtains the configuration for the {@link Task} instances for the
	 * {@link Work}.
	 * 
	 * @return Configuration for the {@link Task} instances of the {@link Work}.
	 */
	TaskConfiguration<?, W, ?, ?>[] getTaskConfiguration();

}
