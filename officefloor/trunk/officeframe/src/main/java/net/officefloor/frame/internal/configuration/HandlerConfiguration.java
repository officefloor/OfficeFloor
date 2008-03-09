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
import net.officefloor.frame.api.execute.Handler;
import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;

/**
 * Configuration of a {@link Handler}.
 * 
 * @author Daniel
 */
public interface HandlerConfiguration<H extends Enum<H>, F extends Enum<F>> {

	/**
	 * Obtains the key linking this {@link Handler} to the
	 * {@link ManagedObjectSource}.
	 * 
	 * @return Key linking this {@link Handler} to the
	 *         {@link ManagedObjectSource}.
	 */
	H getHandlerKey();

	/**
	 * Obtains the {@link HandlerFactory} for the {@link Handler}.
	 * 
	 * @return {@link HandlerFactory}.
	 */
	HandlerFactory<F> getHandlerFactory();

	/**
	 * Obtains the configuration for the processes invoked by the
	 * {@link Handler}.
	 * 
	 * @return {@link HandlerFlowConfiguration} specifying the first
	 *         {@link Task} of the linked process.
	 */
	HandlerFlowConfiguration<F>[] getLinkedProcessConfiguration();

}
