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
package net.officefloor.plugin.socket.server;

import java.io.IOException;
import java.nio.channels.Selector;

import net.officefloor.plugin.socket.server.protocol.Connection;

/**
 * Manages the {@link Connection} instances.
 * 
 * @author Daniel Sagenschneider
 */
public interface ConnectionManager {

	/**
	 * Opens the {@link Selector} instances for managing the
	 * {@link EstablishedConnection} instances.
	 * 
	 * @throws IOException
	 *             If fails to open all the {@link Selector} instances.
	 */
	void openSocketSelectors() throws IOException;

	/**
	 * Manages the {@link EstablishedConnection}.
	 * 
	 * @param connection
	 *            {@link EstablishedConnection} to be managed.
	 */
	void manageConnection(EstablishedConnection connection);

	/**
	 * Closes the {@link Selector} instances for managing the
	 * {@link EstablishedConnection} instances.
	 */
	void closeSocketSelectors() throws IOException;

}