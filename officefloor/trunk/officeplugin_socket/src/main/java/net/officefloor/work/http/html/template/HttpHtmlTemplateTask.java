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
package net.officefloor.work.http.html.template;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.build.TaskFactory;
import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.execute.TaskContext;
import net.officefloor.model.task.TaskFactoryManufacturer;
import net.officefloor.plugin.socket.server.http.api.HttpResponse;
import net.officefloor.plugin.socket.server.http.api.ServerHttpConnection;
import net.officefloor.work.http.HttpException;

/**
 * HTTP HTML Template {@link Task}.
 * 
 * @author Daniel
 */
public class HttpHtmlTemplateTask implements TaskFactoryManufacturer,
		TaskFactory<Object, HttpHtmlTemplateWork, Indexed, None>,
		Task<Object, HttpHtmlTemplateWork, Indexed, None> {

	/**
	 * Flag indicating if bean is required.
	 */
	private final boolean isRequireBean;

	/**
	 * {@link HttpHtmlTemplateContentWriter} instances.
	 */
	private final HttpHtmlTemplateContentWriter[] contents;

	/**
	 * Initiate.
	 * 
	 * @param isRequireBean
	 *            Flag indicating if bean is required.
	 * @param contents
	 *            {@link HttpHtmlTemplateContentWriter} instances.
	 */
	public HttpHtmlTemplateTask(boolean isRequireBean,
			HttpHtmlTemplateContentWriter[] contents) {
		this.isRequireBean = isRequireBean;
		this.contents = contents;
	}

	/*
	 * ================= Task =====================================
	 */

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.officefloor.model.task.TaskFactoryManufacturer#createTaskFactory()
	 */
	@Override
	public TaskFactory<?, ?, ?, ?> createTaskFactory() {
		return this;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.officefloor.frame.api.build.TaskFactory#createTask(net.officefloor
	 * .frame.api.execute.Work)
	 */
	@Override
	public Task<Object, HttpHtmlTemplateWork, Indexed, None> createTask(
			HttpHtmlTemplateWork work) {
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
	public Object doTask(
			TaskContext<Object, HttpHtmlTemplateWork, Indexed, None> context)
			throws IOException, HttpException {

		// Obtain the HTTP response
		ServerHttpConnection connection = (ServerHttpConnection) context
				.getObject(0);
		HttpResponse response = connection.getHttpResponse();

		// Obtain the bean and writer if required
		Object bean = null;
		Writer writer = null;
		if (this.isRequireBean) {
			bean = context.getParameter();
			writer = new OutputStreamWriter(response.getBody());
		}

		// Write the contents to the response
		for (HttpHtmlTemplateContentWriter content : this.contents) {
			content.writeContent(bean, writer, response);
		}

		// Nothing to return
		return null;
	}

}
