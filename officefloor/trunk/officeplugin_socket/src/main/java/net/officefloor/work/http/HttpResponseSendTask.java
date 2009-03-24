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
package net.officefloor.work.http;

import java.io.IOException;

import net.officefloor.compile.util.AbstractSingleTaskWork;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.execute.TaskContext;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.plugin.socket.server.http.api.HttpResponse;
import net.officefloor.plugin.socket.server.http.api.ServerHttpConnection;

/**
 * {@link Task} to send the {@link HttpResponse}.
 * 
 * @author Daniel
 */
public class HttpResponseSendTask
		extends
		AbstractSingleTaskWork<Work, HttpResponseSendTask.HttpResponseSendTaskDependencies, None> {

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