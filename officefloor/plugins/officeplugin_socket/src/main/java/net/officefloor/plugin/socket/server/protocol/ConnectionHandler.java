/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2013 Daniel Sagenschneider
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
package net.officefloor.plugin.socket.server.protocol;

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
	 * <p>
	 * Handles a heart beat on {@link Connection}.
	 * <p>
	 * Typical use of the heart beat is to allow the {@link ConnectionHandler}
	 * to close an idle {@link Connection}.
	 * 
	 * @param context
	 *            {@link HeartBeatContext}.
	 * @throws IOException
	 *             If fails to handle heart beat. Possibly from attempting to
	 *             close it.
	 */
	void handleHeartbeat(HeartBeatContext context) throws IOException;

}