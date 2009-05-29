/*
 * Office Floor, Application Server
 * Copyright (C) 2005-2009 Daniel Sagenschneider
 *
 * This program is free software; you can redistribute it and/or modify it under the terms
 * of the GNU General Public License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program;
 * if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston,
 * MA 02111-1307 USA
 */
package net.officefloor.plugin.work.http.html.template;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.execute.TaskContext;
import net.officefloor.frame.util.AbstractSingleTask;
import net.officefloor.plugin.socket.server.http.api.HttpResponse;
import net.officefloor.plugin.socket.server.http.api.ServerHttpConnection;
import net.officefloor.plugin.work.http.HttpException;

/**
 * HTTP HTML Template {@link Task}.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpHtmlTemplateTask
		extends
		AbstractSingleTask<HttpHtmlTemplateWork, HttpHtmlTemplateTask.HttpHtmlTemplateTaskDependencies, None> {

	/**
	 * Dependencies for the {@link HttpHtmlTemplateTask}.
	 */
	public static enum HttpHtmlTemplateTaskDependencies {
		SERVER_HTTP_CONNECTION, BEAN
	}

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

	@Override
	public Object doTask(
			TaskContext<HttpHtmlTemplateWork, HttpHtmlTemplateTaskDependencies, None> context)
			throws IOException, HttpException {

		// Obtain the HTTP response
		ServerHttpConnection connection = (ServerHttpConnection) context
				.getObject(HttpHtmlTemplateTaskDependencies.SERVER_HTTP_CONNECTION);
		HttpResponse response = connection.getHttpResponse();

		// Obtain the bean and writer if required
		Object bean = null;
		Writer writer = null;
		if (this.isRequireBean) {
			bean = context.getObject(HttpHtmlTemplateTaskDependencies.BEAN);
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