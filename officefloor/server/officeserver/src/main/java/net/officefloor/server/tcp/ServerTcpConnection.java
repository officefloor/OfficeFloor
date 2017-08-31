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
package net.officefloor.server.tcp;

import java.io.IOException;

import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.server.stream.ServerInputStream;
import net.officefloor.server.stream.ServerOutputStream;

/**
 * TCP connection to be handled by {@link OfficeFloor}.
 * 
 * @author Daniel Sagenschneider
 */
@Deprecated
public interface ServerTcpConnection {

	/**
	 * Obtains the {@link ServerInputStream} that provides access to the data
	 * sent from the client.
	 * 
	 * @return {@link ServerInputStream}.
	 */
	ServerInputStream getInputStream();

	/**
	 * <p>
	 * Flags for the {@link ManagedObject} to not execute another
	 * {@link ManagedFunction} until further data is received from the client.
	 * <p>
	 * On calling this the next time a {@link ManagedFunction} is invoked using
	 * this {@link ManagedObject}, data will be available from the
	 * {@link ServerInputStream}.
	 * 
	 * @return <code>true</code> indicating if will wait on client data.
	 *         <code>false</code> if client data is available and therefore will
	 *         not wait.
	 * @throws IOException
	 *             If fails to initiate waiting on client.
	 */
	boolean waitOnClientData() throws IOException;

	/**
	 * <p>
	 * Obtains the {@link ServerOutputStream} to write data back to the client.
	 * <p>
	 * Closing the {@link ServerOutputStream} will result in closing the
	 * connection.
	 * 
	 * @return {@link ServerOutputStream}.
	 */
	ServerOutputStream getOutputStream();

}