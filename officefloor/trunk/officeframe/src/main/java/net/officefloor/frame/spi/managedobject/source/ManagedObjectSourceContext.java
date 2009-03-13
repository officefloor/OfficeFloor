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

import net.officefloor.frame.api.build.ManagedObjectHandlerBuilder;
import net.officefloor.frame.api.build.WorkBuilder;
import net.officefloor.frame.api.build.WorkFactory;
import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.managedobject.pool.ManagedObjectPool;

/**
 * Context for a {@link ManagedObjectSource}.
 * 
 * @author Daniel
 */
public interface ManagedObjectSourceContext<H extends Enum<H>> {

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
	 * Obtains the {@link ManagedObjectHandlerBuilder}.
	 * 
	 * @return {@link ManagedObjectHandlerBuilder}.
	 */
	ManagedObjectHandlerBuilder<H> getHandlerBuilder();

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
	 * @param workFactory
	 *            {@link WorkFactory} to create the recycle {@link Work}.
	 * @return {@link WorkBuilder} to recycle this {@link ManagedObject}.
	 */
	<W extends Work> ManagedObjectWorkBuilder<W> getRecycleWork(
			WorkFactory<W> workFactory);

	/**
	 * Adds {@link ManagedObjectWorkBuilder} for {@link Work} of the
	 * {@link ManagedObjectSource}.
	 * 
	 * @param workName
	 *            Name of the {@link Work}.
	 * @param workFactory
	 *            {@link WorkFactory} to create the {@link Work}.
	 * @return {@link ManagedObjectWorkBuilder}.
	 */
	<W extends Work> ManagedObjectWorkBuilder<W> addWork(String workName,
			WorkFactory<W> workFactory);

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
	 */
	void addStartupTask(String workName, String taskName);

}
