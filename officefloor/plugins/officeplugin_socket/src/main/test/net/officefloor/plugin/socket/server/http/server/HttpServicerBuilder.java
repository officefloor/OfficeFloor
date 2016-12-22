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
package net.officefloor.plugin.socket.server.http.server;

import net.officefloor.frame.api.execute.ManagedFunction;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;
import net.officefloor.plugin.socket.server.http.ServerHttpConnection;

/**
 * Builder to construct the HTTP service {@link ManagedFunction}.
 *
 * @author Daniel Sagenschneider
 */
public interface HttpServicerBuilder {

	/**
	 * Builds the {@link ServerHttpConnection} servicer {@link ManagedFunction}.
	 *
	 * @param managedObjectName
	 *            Name that the {@link ManagedObject} for the
	 *            {@link ServerHttpConnection} is registered under.
	 * @param server
	 *            {@link MockHttpServer} to construct the servicer {@link ManagedFunction}
	 *            instances.
	 * @return {@link HttpServicerTask} to link the {@link ManagedObjectSource}
	 *         to service the {@link ServerHttpConnection}.
	 * @throws Exception
	 *             If fails to build the servicer {@link ManagedFunction}.
	 */
	HttpServicerTask buildServicer(String managedObjectName,
			MockHttpServer server) throws Exception;

}