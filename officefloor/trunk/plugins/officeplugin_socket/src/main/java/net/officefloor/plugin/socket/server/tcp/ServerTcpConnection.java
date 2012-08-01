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

package net.officefloor.plugin.socket.server.tcp;

import java.io.IOException;

import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.plugin.socket.server.protocol.Connection;
import net.officefloor.plugin.stream.ByteOutputStream;
import net.officefloor.plugin.stream.NioInputStream;

/**
 * TCP connection to be handled by the {@link OfficeFloor}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ServerTcpConnection {

	/**
	 * Obtains the lock that may be <code>synchronized</code> on to reduce
	 * locking overhead of making multiple calls on this
	 * {@link ServerTcpConnection}. In other words, it allows course grained
	 * locking.
	 * 
	 * @return Lock that governs this {@link ServerTcpConnection}.
	 */
	Object getLock();

	/**
	 * Obtains the {@link NioInputStream} that provides access to the data sent
	 * from the client.
	 * 
	 * @return {@link NioInputStream}.
	 */
	NioInputStream getInputStream();

	/**
	 * <p>
	 * Flags for the {@link ManagedObject} to not execute another {@link Task}
	 * until further data is received from the client.
	 * <p>
	 * On calling this the next time a {@link Task} is invoked using this
	 * {@link ManagedObject}, data will be available from the
	 * {@link NioInputStream}.
	 * 
	 * @throws IOException
	 *             If fails to initiate waiting on client.
	 */
	void waitOnClientData() throws IOException;

	/**
	 * <p>
	 * Obtains the {@link ByteOutputStream} to write data back to the client.
	 * <p>
	 * Closing the {@link ByteOutputStream} will result in closing the
	 * {@link Connection}.
	 * 
	 * @return {@link ByteOutputStream}.
	 */
	ByteOutputStream getOutputStream();

}