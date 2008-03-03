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
package net.officefloor.frame.spi.managedobject.source;

import java.util.Properties;

import net.officefloor.frame.api.build.BuildException;
import net.officefloor.frame.api.build.ManagedObjectHandlersBuilder;
import net.officefloor.frame.api.build.WorkBuilder;
import net.officefloor.frame.api.build.WorkFactory;
import net.officefloor.frame.api.execute.Handler;
import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.pool.ManagedObjectPool;

/**
 * Context for a {@link ManagedObjectSource}.
 * 
 * @author Daniel
 */
public interface ManagedObjectSourceContext {

	/**
	 * Obtains a required property value.
	 * 
	 * @param name
	 *            Name of the property.
	 * @return Value of the property.
	 * @throws ManagedObjectSourceUnknownPropertyError
	 *             If property was not configured. Let this propagate as the
	 *             framework will handle it.
	 */
	String getProperty(String name)
			throws ManagedObjectSourceUnknownPropertyError;

	/**
	 * Obtains the property value or subsequently the default value.
	 * 
	 * @param name
	 *            Name of the property.
	 * @param defaultValue
	 *            Default value if property not specified.
	 * @return Value of the property or the the default value if not specified.
	 */
	String getProperty(String name, String defaultValue);

	/**
	 * Properties to configure the {@link ManagedObjectSource}.
	 * 
	 * @return Properties specific for the {@link ManagedObjectSource}.
	 */
	Properties getProperties();

	/**
	 * <p>
	 * Should this {@link ManagedObjectSource}require to obtain various
	 * resources to initialise. A possible example of a resource would be an XML
	 * configuration file specific to the {@link ManagedObjectSource}.
	 * <p>
	 * This is only valid to call during the
	 * {@link ManagedObjectSource#init(ManagedObjectSourceContext)} method.
	 * 
	 * @return {@link ResourceLocator}.
	 */
	ResourceLocator getResourceLocator();

	/**
	 * Obtains the {@link ManagedObjectHandlersBuilder}.
	 * 
	 * @param handlerKeys
	 *            {@link Enum} providing the keys for each {@link Handler}.
	 * @return {@link ManagedObjectHandlersBuilder}.
	 * @throws BuildException
	 *             If fails to obtain {@link ManagedObjectHandlersBuilder}.
	 */
	<H extends Enum<H>> ManagedObjectHandlersBuilder<H> getHandlerBuilder(
			Class<H> handlerKeys) throws BuildException;

	/**
	 * <p>
	 * Invoking this method during the
	 * {@link ManagedObjectSource#init(ManagedObjectSourceContext)} will create
	 * recycle functionality for the {@link ManagedObject} to be returned to a
	 * {@link ManagedObjectPool}.
	 * <p>
	 * The initial {@link Task} will be used as the recycle starting point for
	 * this {@link ManagedObject}.
	 * 
	 * @param typeOfWork
	 *            {@link Class} of the {@link Work}.
	 * @return {@link WorkBuilder} to recycle this {@link ManagedObject}.
	 * @throws BuildException
	 *             If fails to obtain the recycle
	 *             {@link ManagedObjectWorkBuilder}.
	 */
	<W extends Work> ManagedObjectWorkBuilder<W> getRecycleWork(
			Class<W> typeOfWork) throws BuildException;

	/**
	 * Adds {@link ManagedObjectWorkBuilder} for {@link Work} of the
	 * {@link ManagedObjectSource}.
	 * 
	 * @param workName
	 *            Name of the {@link Work}.
	 * @param typeOfWork
	 *            Type of the {@link Work}.
	 * @param factory
	 *            {@link WorkFactory}.
	 * @return {@link ManagedObjectWorkBuilder}.
	 * @throws BuildException
	 *             If fails to add the {@link Work}.
	 */
	<W extends Work> ManagedObjectWorkBuilder<W> addWork(String workName,
			Class<W> typeOfWork) throws BuildException;

	/**
	 * <p>
	 * Adds a {@link Task} to invoke on start up of the {@link Office}.
	 * <p>
	 * The {@link Task} must be registered by this {@link ManagedObjectSource}.
	 * 
	 * @param workName
	 *            Name of {@link Work} containing the {@link Task}.
	 * @param taskName
	 *            Name of {@link Task} on the {@link Work}.
	 * @throws BuildException
	 *             If fails to register startup {@link Task}.
	 */
	void addStartupTask(String workName, String taskName) throws BuildException;

}
