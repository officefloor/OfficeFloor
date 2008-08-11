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
package net.officefloor.work.http.file;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.build.TaskFactory;
import net.officefloor.frame.api.build.WorkFactory;
import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.execute.TaskContext;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.api.execute.WorkContext;
import net.officefloor.model.task.TaskFactoryManufacturer;
import net.officefloor.plugin.socket.server.http.HttpStatus;
import net.officefloor.plugin.socket.server.http.api.HttpRequest;
import net.officefloor.plugin.socket.server.http.api.HttpResponse;
import net.officefloor.plugin.socket.server.http.api.ServerHttpConnection;

/**
 * {@link Work} and {@link Task} for serving {@link File} content as response.
 * 
 * @author Daniel
 */
public class HttpFileTask implements WorkFactory<HttpFileTask>, Work,
		TaskFactoryManufacturer,
		TaskFactory<Object, HttpFileTask, Indexed, Indexed>,
		Task<Object, HttpFileTask, Indexed, Indexed> {

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.api.build.WorkFactory#createWork()
	 */
	@Override
	public HttpFileTask createWork() {
		return this;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.api.execute.Work#setWorkContext(net.officefloor.frame.api.execute.WorkContext)
	 */
	@Override
	public void setWorkContext(WorkContext context) throws Exception {
		// Nothing to initialise
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.model.task.TaskFactoryManufacturer#createTaskFactory()
	 */
	@Override
	public TaskFactory<?, ?, ?, ?> createTaskFactory() {
		return this;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.api.build.TaskFactory#createTask(net.officefloor.frame.api.execute.Work)
	 */
	@Override
	public Task<Object, HttpFileTask, Indexed, Indexed> createTask(
			HttpFileTask work) {
		return this;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.api.execute.Task#doTask(net.officefloor.frame.api.execute.TaskContext)
	 */
	@Override
	public Object doTask(
			TaskContext<Object, HttpFileTask, Indexed, Indexed> context)
			throws Throwable {

		// Obtain the request to route it
		ServerHttpConnection connection = (ServerHttpConnection) context
				.getObject(0);
		HttpRequest request = connection.getHttpRequest();

		// Obtain the path from the request
		String path = request.getPath();
		int parameterStart = path.indexOf('?');
		if (parameterStart > 0) {
			// Obtain the path minus the parameters
			path = path.substring(0, (parameterStart - 1));
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
					"<html><body>Can not find resource " + path
							+ "</body></html>").flush();
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
