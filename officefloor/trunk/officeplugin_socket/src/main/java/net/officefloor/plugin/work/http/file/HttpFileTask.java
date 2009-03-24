/*
 *  Office Floor, Application Server
 *  Copyright (C) 2006 Daniel Sagenschneider
 *
 *  This program is free software; you can redistribute it and/or modify it under the terms 
 *  of the GNU General Public License as published by the Free Software Foundation; either 
 *  version 2 of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with this program; 
 *  if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, 
 *  MA 02111-1307 USA
 */
package net.officefloor.plugin.work.http.file;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import net.officefloor.compile.util.AbstractSingleTaskWork;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.execute.TaskContext;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.plugin.socket.server.http.HttpStatus;
import net.officefloor.plugin.socket.server.http.api.HttpRequest;
import net.officefloor.plugin.socket.server.http.api.HttpResponse;
import net.officefloor.plugin.socket.server.http.api.ServerHttpConnection;
import net.officefloor.plugin.work.http.HttpException;

/**
 * {@link Work} and {@link Task} for serving {@link File} content as response.
 * 
 * @author Daniel
 */
public class HttpFileTask
		extends
		AbstractSingleTaskWork<HttpFileTask, HttpFileTask.HttpFileTaskDependencies, None> {

	/**
	 * Dependencies for the {@link HttpFileTask}.
	 */
	public static enum HttpFileTaskDependencies {
		SERVER_HTTP_CONNECTION
	}

	/*
	 * ================== Task ==========================================
	 */

	@Override
	public Object doTask(
			TaskContext<HttpFileTask, HttpFileTaskDependencies, None> context)
			throws HttpException, IOException {

		// Obtain the HTTP request to obtain path for file
		ServerHttpConnection connection = (ServerHttpConnection) context
				.getObject(HttpFileTaskDependencies.SERVER_HTTP_CONNECTION);
		HttpRequest request = connection.getHttpRequest();

		// Obtain the path from the request
		String path = request.getPath();
		int parameterStart = path.indexOf('?');
		if (parameterStart > 0) {
			// Obtain the path minus the parameters
			path = path.substring(0, parameterStart);
		}

		// Obtain the response
		HttpResponse response = connection.getHttpResponse();

		// Obtain the file content from class path
		InputStream content = context.getClass().getResourceAsStream(path);

		// Handle if not find file
		if (content == null) {
			// Item not found
			response.setStatus(HttpStatus._404); // not found
			new OutputStreamWriter(response.getBody()).append(
					"Can not find resource " + path).flush();
		} else {
			// Return the file content as response
			OutputStream responseBody = response.getBody();
			for (int value = content.read(); value != -1; value = content
					.read()) {
				responseBody.write(value);
			}
		}

		// Send the response
		response.send();

		// Should expect no further processing
		return null;
	}

}