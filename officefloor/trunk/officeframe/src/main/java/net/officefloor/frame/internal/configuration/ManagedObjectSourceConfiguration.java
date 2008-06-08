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

import java.util.Properties;

import net.officefloor.frame.api.build.ManagedObjectHandlerBuilder;
import net.officefloor.frame.api.execute.Handler;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.spi.pool.ManagedObjectPool;

/**
 * Configuration of a {@link ManagedObjectSource}.
 * 
 * @author Daniel
 */
public interface ManagedObjectSourceConfiguration<H extends Enum<H>, MS extends ManagedObjectSource<?, H>> {

	/**
	 * Obtains the name of this {@link ManagedObjectSource}.
	 * 
	 * @return Name of this {@link ManagedObjectSource}.
	 */
	String getManagedObjectName();

	/**
	 * Obtains the name of the {@link Office} responsible for managing this
	 * {@link ManagedObject}.
	 * 
	 * @return Name of the {@link Office} responsible for managing this
	 *         {@link ManagedObject}.
	 */
	String getManagingOfficeName();

	/**
	 * Obtains the {@link Class} of the {@link ManagedObjectSource}.
	 * 
	 * @return {@link Class} of the {@link ManagedObjectSource}.
	 * @throws ConfigurationException
	 *             If invalid configuration.
	 */
	Class<MS> getManagedObjectSourceClass() throws ConfigurationException;

	/**
	 * Obtains the properties to initialise the {@link ManagedObjectSource}.
	 * 
	 * @return Properties to initialise the {@link ManagedObjectSource}.
	 */
	Properties getProperties();

	/**
	 * Obtains the {@link ManagedObjectPool} for this
	 * {@link ManagedObjectSource}.
	 * 
	 * @return {@link ManagedObjectPool} for this {@link ManagedObjectSource} or
	 *         <code>null</code> if not to be pooled.
	 */
	ManagedObjectPool getManagedObjectPool();

	/**
	 * Obtains the default timeout for asynchronous operations on the
	 * {@link ManagedObject}.
	 * 
	 * @return Default timeout for asynchronous operations on the
	 *         {@link ManagedObject}.
	 */
	long getDefaultTimeout();

	/**
	 * Obtains the {@link ManagedObjectHandlerBuilder} to allow enhancing.
	 * 
	 * {@link Enum} providing the keys for each {@link Handler}.
	 * 
	 * @return {@link ManagedObjectHandlerBuilder}.
	 * @throws ConfigurationException
	 *             If fails to obtain {@link ManagedObjectHandlerBuilder}.
	 */
	ManagedObjectHandlerBuilder<H> getHandlerBuilder(Class<H> handlerKeys)
			throws ConfigurationException;

	/**
	 * Obtains the {@link HandlerConfiguration} for the
	 * {@link ManagedObjectSource}.
	 * 
	 * @return {@link HandlerConfiguration} for the {@link ManagedObjectSource}.
	 * @throws ConfigurationException
	 *             If invalid configuration.
	 */
	HandlerConfiguration<?, ?>[] getHandlerConfiguration()
			throws ConfigurationException;

}
