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
import net.officefloor.frame.api.execute.Work;

/**
 * Provides configuration of a {@link net.officefloor.frame.api.execute.Work}
 * item.
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
	 * Obtains the {@link WorkFactory} to create the
	 * {@link net.officefloor.frame.api.execute.Work} to be done.
	 * 
	 * @return {@link WorkFactory} to create the
	 *         {@link net.officefloor.frame.api.execute.Work} to be done.
	 * @throws ConfigurationException
	 *             If invalid configuration.
	 */
	WorkFactory<W> getWorkFactory() throws ConfigurationException;

	/**
	 * Obtains the {@link net.officefloor.frame.internal.structure.ProcessState}
	 * bound {@link net.officefloor.frame.spi.managedobject.ManagedObject}
	 * instances for this {@link Work}.
	 * 
	 * @return Listing of
	 *         {@link net.officefloor.frame.spi.managedobject.ManagedObject}
	 *         instances for this {@link Work}.
	 * @throws ConfigurationException
	 *             If invalid configuration.
	 */
	LinkedManagedObjectConfiguration[] getProcessManagedObjectConfiguration()
			throws ConfigurationException;

	/**
	 * Obtains the {@link Work} bound
	 * {@link net.officefloor.frame.spi.managedobject.ManagedObject} instances
	 * for this {@link Work}.
	 * 
	 * @return Listing of the
	 *         {@link net.officefloor.frame.spi.managedobject.ManagedObject}
	 *         instances for this {@link Work}.
	 * @throws ConfigurationException
	 *             If invalid configuration.
	 */
	ManagedObjectConfiguration[] getManagedObjectConfiguration()
			throws ConfigurationException;

	/**
	 * Obtains the
	 * {@link net.officefloor.frame.spi.administration.Administrator} instances
	 * for this {@link Work}.
	 * 
	 * @return Listing of the
	 *         {@link net.officefloor.frame.spi.administration.Administrator}
	 *         instances for this {@link Work}.
	 * @throws ConfigurationException
	 *             If invalid configuration.
	 */
	WorkAdministratorConfiguration[] getAdministratorConfiguration()
			throws ConfigurationException;

	/**
	 * Obtains the name of the initial
	 * {@link net.officefloor.frame.api.execute.Task} of the
	 * {@link net.officefloor.frame.api.execute.Work}.
	 * 
	 * @return Name of the initial
	 *         {@link net.officefloor.frame.api.execute.Task} of the
	 *         {@link net.officefloor.frame.api.execute.Work}.
	 */
	String getInitialTaskName();

	/**
	 * Obtains the configuration for the
	 * {@link net.officefloor.frame.api.execute.Task} instances for the
	 * {@link Work}.
	 * 
	 * @return Configuration for the
	 *         {@link net.officefloor.frame.api.execute.Task} instances of the
	 *         {@link Work}.
	 * @throws ConfigurationException
	 *             If invalid configuration.
	 */
	TaskConfiguration<?, W, ?, ?>[] getTaskConfiguration()
			throws ConfigurationException;

}
