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
import java.nio.channels.SocketChannel;

/**
 * <p>
 * Handler for a {@link Connection}.
 * <p>
 * Required to be implemented by the handler provider.
 * 
 * @author Daniel Sagenschneider
 */
public interface ConnectionHandler {

	/**
	 * Handles a read from the {@link SocketChannel}.
	 * 
	 * @param context
	 *            {@link ReadContext}.
	 * @throws IOException
	 *             If fails to obtain data from the {@link ReadContext}.
	 */
	void handleRead(ReadContext context) throws IOException;

	/**
	 * Handles a potential write to the {@link SocketChannel}.
	 * 
	 * @param context
	 *            {@link WriteContext}.
	 * @throws IOException
	 *             If fails to handle write.
	 */
	void handleWrite(WriteContext context) throws IOException;

	/**
	 * Handles a {@link Connection} being idled.
	 * 
	 * @param context
	 *            {@link IdleContext}.
	 * @throws IOException
	 *             If fails to handle idle {@link Connection}. Possibly from
	 *             attempting to close it.
	 */
	void handleIdleConnection(IdleContext context) throws IOException;

}