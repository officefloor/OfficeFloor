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
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;

/**
 * Meta-data about a particular {@link Handler} for a {@link ManagedObject}.
 * 
 * @author Daniel
 */
public interface ManagedObjectHandlerBuilder {

	/**
	 * <p>
	 * Specifies the type of {@link Handler} required by the
	 * {@link ManagedObjectSource}.
	 * <p>
	 * This method must be invoked by the {@link ManagedObjectSource}
	 * implementation if a {@link Handler} implementation is not to be specified
	 * by the {@link ManagedObjectSource}. Should the
	 * {@link ManagedObjectSource} be providing the {@link Handler}
	 * implementation this method need not be called.
	 * <p>
	 * The {@link Handler} type specified must be the type returned from the
	 * {@link HandlerFactory} registered with the {@link HandlerBuilder}.
	 * 
	 * @param handlerType
	 *            {@link Handler} type.
	 * @throws BuildException
	 *             Indicate failure in building.
	 */
	<H extends Handler<?>> void setHandlerType(Class<H> handlerType)
			throws BuildException;

	/**
	 * Obtains the {@link HandlerBuilder} for a {@link Handler} of the
	 * {@link ManagedObjectSource}.
	 * 
	 * @param flowListingEnum
	 *            Type providing the listing of the {@link Flow} instances for
	 *            the {@link Handler}.
	 * @throws BuildException
	 *             Indicate failure in building.
	 */
	<F extends Enum<F>> HandlerBuilder<F> getHandlerBuilder(
			Class<F> processListingEnum) throws BuildException;

	/**
	 * Obtains the {@link HandlerBuilder} for a {@link Handler} of the
	 * {@link ManagedObjectSource}.
	 * 
	 * @throws BuildException
	 *             Indicate failure in building.
	 */
	HandlerBuilder<Indexed> getHandlerBuilder() throws BuildException;

}
