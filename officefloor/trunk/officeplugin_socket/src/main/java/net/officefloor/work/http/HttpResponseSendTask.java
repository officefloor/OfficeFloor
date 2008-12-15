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

import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.execute.TaskContext;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.model.work.TaskEscalationModel;
import net.officefloor.model.work.TaskModel;
import net.officefloor.model.work.TaskObjectModel;
import net.officefloor.plugin.socket.server.http.api.HttpResponse;
import net.officefloor.plugin.socket.server.http.api.ServerHttpConnection;
import net.officefloor.work.AbstractSingleTaskWork;

/**
 * {@link Task} to send the {@link HttpResponse}.
 * 
 * @author Daniel
 */
public class HttpResponseSendTask extends
		AbstractSingleTaskWork<Object, Work, Indexed, None> {

	/**
	 * Creates the {@link TaskModel} for a {@link HttpResponseSendTask}.
	 * 
	 * @return {@link TaskModel} for a {@link HttpResponseSendTask}.
	 */
	public static TaskModel<Indexed, None> createTaskModel() {

		// Create the task model
		TaskModel<Indexed, None> task = new TaskModel<Indexed, None>();
		task.setTaskName("SendHttpResponse");
		task.setTaskFactoryManufacturer(new HttpResponseSendTask());
		TaskObjectModel<Indexed> httpConnection = new TaskObjectModel<Indexed>();
		httpConnection.setObjectType(ServerHttpConnection.class.getName());
		task.addObject(httpConnection);
		TaskEscalationModel ioEscalation = new TaskEscalationModel();
		ioEscalation.setEscalationType(IOException.class.getName());
		task.addEscalation(ioEscalation);

		// Return the task model
		return task;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.officefloor.frame.util.AbstractSingleTask#createTask(net.officefloor
	 * .frame.api.execute.Work)
	 */
	@Override
	public Task<Object, Work, Indexed, None> createTask(Work work) {
		// Overridden to return this as work may not be right type
		return this;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.officefloor.frame.api.execute.Task#doTask(net.officefloor.frame.api
	 * .execute.TaskContext)
	 */
	@Override
	public Object doTask(TaskContext<Object, Work, Indexed, None> context)
			throws IOException {

		// Obtain the HTTP response
		ServerHttpConnection httpConnetion = (ServerHttpConnection) context
				.getObject(0);
		HttpResponse httpResponse = httpConnetion.getHttpResponse();

		// Send the HTTP response
		httpResponse.send();

		// Nothing to return
		return null;
	}

}
