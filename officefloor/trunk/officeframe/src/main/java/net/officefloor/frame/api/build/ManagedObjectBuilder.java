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
package net.officefloor.frame.api.build;

import net.officefloor.frame.api.execute.Handler;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.spi.pool.ManagedObjectPool;

/**
 * Meta-data of a {@link net.officefloor.frame.spi.managedobject.ManagedObject}.
 * 
 * @author Daniel
 */
public interface ManagedObjectBuilder {

	/**
	 * Specifies the {@link Class} of the {@link ManagedObjectSource}.
	 * 
	 * @param <S>
	 *            {@link ManagedObjectSource} type.
	 * @param managedObjectSourceClass
	 *            {@link Class} of the {@link ManagedObjectSource}.
	 * @throws BuildException
	 *             Indicate failure in building.
	 */
	<S extends ManagedObjectSource> void setManagedObjectSourceClass(
			Class<S> managedObjectSourceClass) throws BuildException;

	/**
	 * Specifies a property for the {@link ManagedObjectSource}.
	 * 
	 * @param name
	 *            Name of property.
	 * @param value
	 *            Value of property.
	 * @throws BuildException
	 *             Indicate failure in building.
	 */
	void addProperty(String name, String value) throws BuildException;

	/**
	 * Specifies the {@link ManagedObjectPool} for this
	 * {@link net.officefloor.frame.spi.managedobject.ManagedObject}.
	 * 
	 * @param pool
	 *            {@link ManagedObjectPool} for this
	 *            {@link net.officefloor.frame.spi.managedobject.ManagedObject}.
	 */
	void setManagedObjectPool(ManagedObjectPool pool);

	/**
	 * Specifies the default timeout for asynchronous operations on the
	 * {@link net.officefloor.frame.spi.managedobject.ManagedObject}.
	 * 
	 * @param timeout
	 *            Default timeout.
	 * @throws BuildException
	 *             Indicate failure in building.
	 */
	void setDefaultTimeout(long timeout) throws BuildException;

	/**
	 * Specifies the {@link net.officefloor.frame.api.manage.Office} to manage
	 * this {@link net.officefloor.frame.spi.managedobject.ManagedObject}.
	 * 
	 * @param officeName
	 *            Name of the {@link net.officefloor.frame.api.manage.Office}.
	 * @throws BuildException
	 *             Indicate failure in building.
	 */
	void setManagingOffice(String officeName) throws BuildException;

	/**
	 * Obtains the {@link ManagedObjectHandlerBuilder}.
	 * 
	 * @param handlerKeys
	 *            {@link Enum} providing the keys for each {@link Handler}.
	 * @return {@link ManagedObjectHandlerBuilder}.
	 * @throws BuildException
	 *             If fails to obtain {@link ManagedObjectHandlerBuilder}.
	 */
	<H extends Enum<H>> ManagedObjectHandlerBuilder<H> getManagedObjectHandlerBuilder(
			Class<H> handlerKeys) throws BuildException;

}
