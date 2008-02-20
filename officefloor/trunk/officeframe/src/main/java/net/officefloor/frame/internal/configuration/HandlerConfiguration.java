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

import net.officefloor.frame.api.build.HandlerFactory;

/**
 * Configuration of a {@link net.officefloor.frame.api.execute.Handler}.
 * 
 * @author Daniel
 */
public interface HandlerConfiguration<H extends Enum<H>, F extends Enum<F>> {

	/**
	 * Obtains the key linking this
	 * {@link net.officefloor.frame.api.execute.Handler} to the
	 * {@link net.officefloor.frame.spi.managedobject.source.ManagedObjectSource}.
	 * 
	 * @return Key linking this
	 *         {@link net.officefloor.frame.api.execute.Handler} to the
	 *         {@link net.officefloor.frame.spi.managedobject.source.ManagedObjectSource}.
	 */
	H getHandlerKey();

	/**
	 * Obtains the {@link HandlerFactory} for the
	 * {@link net.officefloor.frame.api.execute.Handler}.
	 * 
	 * @return {@link HandlerFactory}.
	 */
	HandlerFactory<F> getHandlerFactory();

	/**
	 * Obtains the configuration for the processes invoked by the
	 * {@link net.officefloor.frame.api.execute.Handler}.
	 * 
	 * @return {@link HandlerFlowConfiguration} specifying the first
	 *         {@link net.officefloor.frame.api.execute.Task} of the linked
	 *         process.
	 */
	HandlerFlowConfiguration<F>[] getLinkedProcessConfiguration();

}
