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

import net.officefloor.frame.api.OfficeFrame;
import net.officefloor.frame.api.build.ManagedObjectBuilder;
import net.officefloor.frame.api.build.OfficeBuilder;
import net.officefloor.frame.api.build.WorkBuilder;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.spi.pool.ManagedObjectPool;

/**
 * Context for a
 * {@link net.officefloor.frame.spi.managedobject.source.ManagedObjectSource}.
 * 
 * @author Daniel
 */
public interface ManagedObjectSourceContext {

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
	 * <p>
	 * Invoking this method during the
	 * {@link ManagedObjectSource#init(ManagedObjectSourceContext)} will create
	 * recycle functionality for the
	 * {@link net.officefloor.frame.spi.managedobject.ManagedObject} to be
	 * returned to a {@link ManagedObjectPool}.
	 * <p>
	 * The initial {@link net.officefloor.frame.api.execute.Task} will be used
	 * as the recycle starting point for this
	 * {@link net.officefloor.frame.spi.managedobject.ManagedObject}.
	 * 
	 * @param W
	 *            Type of the {@link Work}.
	 * @param typeOfWork
	 *            {@link Class} of the {@link Work}.
	 * @return {@link WorkBuilder} to recycle this
	 *         {@link net.officefloor.frame.spi.managedobject.ManagedObject}.
	 */
	<W extends Work> WorkBuilder<W> getRecycleWorkBuilder(Class<W> typeOfWork);

	/**
	 * <p>
	 * Obtains the {@link ManagedObjectBuilder} that built this
	 * {@link net.officefloor.frame.spi.managedobject.ManagedObject}.
	 * <p>
	 * It is available to allow the {@link ManagedObjectSource} to provide
	 * configuration.
	 * 
	 * @return {@link ManagedObjectBuilder}.
	 */
	ManagedObjectBuilder<?> getManagedObjectBuilder();

	/**
	 * Obtains the {@link OfficeBuilder} building the office that the
	 * {@link ManagedObjectSource} is being used within.
	 * 
	 * @return {@link OfficeBuilder}.
	 */
	OfficeBuilder getOfficeBuilder();

	/**
	 * <p>
	 * Obtains the {@link OfficeFrame}.
	 * <p>
	 * If another Office is to be registered, a
	 * {@link net.officefloor.frame.spi.team.Team} should be registered with the
	 * {@link #getOfficeBuilder()} {@link OfficeBuilder} to start/stop the
	 * created office.
	 * 
	 * @return {@link OfficeFrame}.
	 */
	OfficeFrame getOfficeFrame();

}
