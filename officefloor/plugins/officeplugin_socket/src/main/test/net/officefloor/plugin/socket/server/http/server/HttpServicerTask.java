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
import net.officefloor.frame.api.execute.Work;
import net.officefloor.plugin.socket.server.http.ServerHttpConnection;

/**
 * Reference to a {@link ManagedFunction} to be invoked to service the
 * {@link ServerHttpConnection}.
 *
 * @author Daniel Sagenschneider
 */
public class HttpServicerTask {

	/**
	 * Name of the {@link Work}.
	 */
	public final String workName;

	/**
	 * Name of the {@link ManagedFunction}.
	 */
	public final String taskName;

	/**
	 * Initiate.
	 *
	 * @param workName
	 *            Name of the {@link Work}.
	 * @param taskName
	 *            Name of the {@link ManagedFunction}.
	 */
	public HttpServicerTask(String workName, String taskName) {
		this.workName = workName;
		this.taskName = taskName;
	}

}