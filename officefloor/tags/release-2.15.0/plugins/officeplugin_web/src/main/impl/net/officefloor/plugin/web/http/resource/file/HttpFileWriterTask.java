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
package net.officefloor.plugin.web.http.resource.file;

import java.io.IOException;

import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.build.TaskFactory;
import net.officefloor.frame.api.execute.TaskContext;
import net.officefloor.frame.util.AbstractSingleTask;
import net.officefloor.plugin.socket.server.http.HttpResponse;
import net.officefloor.plugin.socket.server.http.ServerHttpConnection;
import net.officefloor.plugin.web.http.resource.AbstractHttpFile;
import net.officefloor.plugin.web.http.resource.HttpFile;

/**
 * {@link TaskFactory} to write a {@link HttpFile} to the {@link HttpResponse}.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpFileWriterTask
		extends
		AbstractSingleTask<HttpFileWriterTask, HttpFileWriterTask.HttpFileWriterTaskDependencies, None> {

	/**
	 * Keys for the dependencies.
	 */
	public static enum HttpFileWriterTaskDependencies {
		HTTP_FILE, SERVER_HTTP_CONNECTION
	}

	/*
	 * ===================== Task ========================================
	 */

	@Override
	public Object doTask(
			TaskContext<HttpFileWriterTask, HttpFileWriterTaskDependencies, None> context)
			throws IOException {

		// Obtain the dependencies
		HttpFile httpFile = (HttpFile) context
				.getObject(HttpFileWriterTaskDependencies.HTTP_FILE);
		ServerHttpConnection connection = (ServerHttpConnection) context
				.getObject(HttpFileWriterTaskDependencies.SERVER_HTTP_CONNECTION);

		// Write the file
		AbstractHttpFile.writeHttpFile(httpFile, connection.getHttpResponse());

		// Return nothing as file written to response
		return null;
	}

}