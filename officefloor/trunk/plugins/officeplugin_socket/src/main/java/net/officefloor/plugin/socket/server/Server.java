/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2012 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.officefloor.plugin.socket.server;

import java.io.IOException;

import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.internal.structure.ProcessState;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectExecuteContext;

/**
 * <p>
 * {@link Server} to handle the input.
 * <p>
 * Required to be implemented by the handler provider.
 * 
 * @author Daniel Sagenschneider
 */
public interface Server<CH extends ConnectionHandler> {

	/**
	 * Provides the {@link Server} the {@link ManagedObjectExecuteContext} to
	 * enable it to invoke {@link ProcessState} instances to process input.
	 * 
	 * @param executeContext
	 *            {@link ManagedObjectExecuteContext}.
	 */
	void setManagedObjectExecuteContext(
			ManagedObjectExecuteContext<Indexed> executeContext);

	/**
	 * Creates a {@link ConnectionHandler} for a new {@link Connection}.
	 * 
	 * @param connection
	 *            A new {@link Connection} requiring handling.
	 * @return {@link ConnectionHandler} to handle the new {@link Connection}.
	 */
	CH createConnectionHandler(Connection connection);

	/**
	 * <p>
	 * Starts the processing the request by the {@link Server}.
	 * <p>
	 * To process a stream of input have the {@link ConnectionHandler} process
	 * the request with zero size and use the {@link Connection} passed to the
	 * {@link ConnectionHandler}.
	 * 
	 * @param connectionHandler
	 *            {@link ConnectionHandler} for the {@link Connection}.
	 * @param attachment
	 *            Optional attachment for the request. May be <code>null</code>.
	 * @throws IOException
	 *             If fails to process request.
	 */
	void processRequest(CH connectionHandler, Object attachment)
			throws IOException;

}