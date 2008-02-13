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

import net.officefloor.frame.api.execute.Handler;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.managedobject.extension.ManagedObjectExtensionInterfaceMetaData;

/**
 * Meta-data of the
 * {@link net.officefloor.frame.spi.managedobject.source.ManagedObjectSource}.
 * 
 * @author Daniel
 */
public interface ManagedObjectSourceMetaData<D extends Enum<D>, H extends Enum<H>> {

	/**
	 * <p>
	 * Obtains the {@link Class} of the {@link ManagedObject} returned from
	 * {@link ManagedObjectSource#getManagedObject()}.
	 * </p>
	 * <p>
	 * This is to enable coupled configuration rather than specifying in a
	 * possibly unrelated configuration file.
	 * </p>
	 * <p>
	 * Note this does not prevent the configuration passed to the
	 * {@link #init(Properties, ResourceLocator, ManagedObjectPoolFactory)}
	 * method to specify this. {@link Class} must however be the same given the
	 * same configuration.
	 * </p>
	 * 
	 * @return {@link Class} of the {@link ManagedObject} returned from the
	 *         {@link #getManagedObject()}.
	 */
	Class<? extends ManagedObject> getManagedObjectClass();

	/**
	 * <p>
	 * Obtains the {@link Class} of the object being managed by the
	 * {@link LifeCycleManagedObject} returned from {@link #getManagedObject()}.
	 * </p>
	 * <p>
	 * This is to enable coupled configuration rather than specifying in a
	 * possibly unrelated configuration file.
	 * </p>
	 * <p>
	 * Note this does not prevent the configuration passed to the
	 * {@link #init(Properties, ResourceLocator, ManagedObjectPoolFactory)}
	 * method to specify this. {@link Class} must however be the same given the
	 * same configuration.
	 * </p>
	 * 
	 * @return The {@link Class} of the object being managed by the
	 *         {@link ManagedObject} returned from {@link #getManagedObject()}.
	 */
	Class<?> getObjectClass();

	/**
	 * <p>
	 * Obtains the {@link Enum} specifying the dependencies for the
	 * {@link net.officefloor.frame.spi.managedobject.ManagedObject}.
	 * </p>
	 * <p>
	 * If there are no dependencies return <code>null</code>.
	 * </p>
	 * 
	 * @return {@link Enum} specifying the dependencies for the
	 *         {@link net.officefloor.frame.spi.managedobject.ManagedObject}.
	 */
	Class<D> getDependencyKeys();

	/**
	 * Obtains the list of {@link ManagedObjectDependencyMetaData} instances
	 * should this {@link ManagedObjectSource} provide a
	 * {@link net.officefloor.frame.spi.managedobject.CoordinatingManagedObject}.
	 * 
	 * @return Description of the dependencies for this
	 *         {@link ManagedObjectSource}.
	 * @see #getDependencyKeys()
	 */
	ManagedObjectDependencyMetaData getDependencyMetaData(D key);

	/**
	 * <p>
	 * Obtains the {@link Enum} specifying the
	 * {@link net.officefloor.frame.api.execute.Handler} instances required.
	 * </p>
	 * <p>
	 * If there are no {@link net.officefloor.frame.api.execute.Handler}
	 * instances required then return <code>null</code>.
	 * </p>
	 * 
	 * @return {@link Enum} specifying the
	 *         {@link net.officefloor.frame.api.execute.Handler} instances
	 *         required.
	 */
	Class<H> getHandlerKeys();

	/**
	 * Obtains the {@link Class} type that the
	 * {@link net.officefloor.frame.api.execute.Handler} for the specified key
	 * must implement.
	 * 
	 * @param key
	 *            Key identifying the
	 *            {@link net.officefloor.frame.api.execute.Handler}.
	 * @return {@link Class} type that the
	 *         {@link net.officefloor.frame.api.execute.Handler} for the
	 *         specified key must implement.
	 */
	Class<? extends Handler<?>> getHandlerType(H key);

	/**
	 * Obtains the meta-data regarding the extension interfaces that this
	 * {@link ManagedObject} implements.
	 * 
	 * @return Meta-data regarding the extension interfaces that this
	 *         {@link ManagedObject} implements.
	 */
	ManagedObjectExtensionInterfaceMetaData<?>[] getExtensionInterfacesMetaData();
}
