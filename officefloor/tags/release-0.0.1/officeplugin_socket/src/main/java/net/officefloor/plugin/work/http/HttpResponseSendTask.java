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
package net.officefloor.plugin.work.http;

import java.io.IOException;

import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.execute.TaskContext;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.util.AbstractSingleTask;
import net.officefloor.plugin.socket.server.http.api.HttpResponse;
import net.officefloor.plugin.socket.server.http.api.ServerHttpConnection;

/**
 * {@link Task} to send the {@link HttpResponse}.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpResponseSendTask
		extends
		AbstractSingleTask<Work, HttpResponseSendTask.HttpResponseSendTaskDependencies, None> {

	/**
	 * Dependencies for the {@link HttpResponseSendTask}.
	 */
	public static enum HttpResponseSendTaskDependencies {
		SERVER_HTTP_CONNECTION
	}

	/*
	 * ===================== Task =========================================
	 */

	@Override
	public Object doTask(
			TaskContext<Work, HttpResponseSendTaskDependencies, None> context)
			throws IOException {

		// Obtain the HTTP response
		ServerHttpConnection httpConnetion = (ServerHttpConnection) context
				.getObject(HttpResponseSendTaskDependencies.SERVER_HTTP_CONNECTION);
		HttpResponse httpResponse = httpConnetion.getHttpResponse();

		// Send the HTTP response
		httpResponse.send();

		// Nothing to return
		return null;
	}

}