/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2009 Daniel Sagenschneider
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
package net.officefloor.plugin.socket.server.http.file.source;

import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.execute.TaskContext;
import net.officefloor.frame.util.AbstractSingleTask;
import net.officefloor.plugin.socket.server.http.HttpRequest;
import net.officefloor.plugin.socket.server.http.ServerHttpConnection;
import net.officefloor.plugin.socket.server.http.file.HttpFile;
import net.officefloor.plugin.socket.server.http.file.HttpFileLocator;

/**
 * {@link Task} to locate a {@link HttpFile} via a {@link HttpFileLocator}.
 *
 * @author Daniel Sagenschneider
 */
public class HttpFileLocatorTask
		extends
		AbstractSingleTask<HttpFileLocatorTask, Indexed, HttpFileLocatorTask.HttpFileLocatorTaskFlows> {

	/**
	 * Enum of flows for the {@link HttpFileLocatorTask}.
	 */
	public static enum HttpFileLocatorTaskFlows {
		HTTP_FILE_NOT_FOUND
	}

	/**
	 * {@link HttpFileLocator}.
	 */
	private final HttpFileLocator httpFileLocator;

	/**
	 * Initiate.
	 *
	 * @param httpFileLocator
	 *            {@link HttpFileLocator}.
	 */
	private HttpFileLocatorTask(HttpFileLocator httpFileLocator) {
		this.httpFileLocator = httpFileLocator;
	}

	/*
	 * =========================== Task =====================================
	 */

	@Override
	public Object doTask(
			TaskContext<HttpFileLocatorTask, Indexed, HttpFileLocatorTaskFlows> context)
			throws Throwable {

		// Obtain the HTTP request
		ServerHttpConnection connection = (ServerHttpConnection) context
				.getObject(0);
		HttpRequest request = connection.getHttpRequest();

		// Locate the HTTP file
		String requestUriPath = request.getRequestURI();
		HttpFile httpFile = this.httpFileLocator.locateHttpFile(requestUriPath);

		// Handle based on whether exists
		if (!httpFile.isExist()) {
			// Not exists, so invoke flow to handle
			context.doFlow(HttpFileLocatorTaskFlows.HTTP_FILE_NOT_FOUND,
					httpFile);
		}

		// Return the HTTP file
		return httpFile;
	}

}