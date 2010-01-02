/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2010 Daniel Sagenschneider
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

import java.io.File;
import java.io.IOException;

import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.execute.TaskContext;
import net.officefloor.frame.util.AbstractSingleTask;
import net.officefloor.plugin.socket.server.http.HttpRequest;
import net.officefloor.plugin.socket.server.http.ServerHttpConnection;
import net.officefloor.plugin.socket.server.http.file.HttpFile;
import net.officefloor.plugin.socket.server.http.file.HttpFileDescriber;
import net.officefloor.plugin.socket.server.http.file.HttpFileFactory;
import net.officefloor.plugin.socket.server.http.file.InvalidHttpRequestUriException;

/**
 * {@link Task} to locate a {@link HttpFile} via a {@link HttpFileFactory}.
 *
 * @author Daniel Sagenschneider
 */
public class HttpFileFactoryTask
		extends
		AbstractSingleTask<HttpFileFactoryTask, Indexed, HttpFileFactoryTask.HttpFileFactoryTaskFlows> {

	/**
	 * Enum of flows for the {@link HttpFileFactoryTask}.
	 */
	public static enum HttpFileFactoryTaskFlows {
		HTTP_FILE_NOT_FOUND
	}

	/**
	 * {@link HttpFileFactory}.
	 */
	private final HttpFileFactory httpFileFactory;

	/**
	 * Index to obtain the context directory from the {@link TaskContext}.
	 */
	private final int contextDirectoryIndex;

	/**
	 * Initiate.
	 *
	 * @param httpFileFactory
	 *            {@link HttpFileFactory}.
	 * @param contextDirectoryIndex
	 *            Index to obtain the context directory from the
	 *            {@link TaskContext}.
	 */
	public HttpFileFactoryTask(HttpFileFactory httpFileFactory,
			int contextDirectoryIndex) {
		this.httpFileFactory = httpFileFactory;
		this.contextDirectoryIndex = contextDirectoryIndex;
	}

	/*
	 * =========================== Task =====================================
	 */

	@Override
	public Object doTask(
			TaskContext<HttpFileFactoryTask, Indexed, HttpFileFactoryTaskFlows> context)
			throws IOException, InvalidHttpRequestUriException {

		// Obtain the HTTP request
		ServerHttpConnection connection = (ServerHttpConnection) context
				.getObject(0);
		HttpRequest request = connection.getHttpRequest();

		// TODO obtain creation specific describers
		HttpFileDescriber[] describers = null;

		// Obtain context directory
		File contextDirectory = null;
		if (this.contextDirectoryIndex >= 0) {
			contextDirectory = (File) context
					.getObject(this.contextDirectoryIndex);
		}

		// Create the HTTP file
		String requestUriPath = request.getRequestURI();
		HttpFile httpFile = this.httpFileFactory.createHttpFile(
				contextDirectory, requestUriPath, describers);

		// Handle based on whether exists
		if (!httpFile.isExist()) {
			// Not exists, so invoke flow to handle
			context.doFlow(HttpFileFactoryTaskFlows.HTTP_FILE_NOT_FOUND,
					httpFile);
		}

		// Return the HTTP file
		return httpFile;
	}

}