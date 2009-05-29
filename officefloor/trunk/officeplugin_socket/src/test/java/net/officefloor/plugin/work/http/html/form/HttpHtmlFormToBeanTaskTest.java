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
package net.officefloor.plugin.work.http.html.form;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.execute.TaskContext;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.socket.server.http.api.HttpRequest;
import net.officefloor.plugin.socket.server.http.api.ServerHttpConnection;
import net.officefloor.plugin.work.http.html.form.HttpHtmlFormToBeanTask;
import net.officefloor.plugin.work.http.html.form.HttpHtmlFormToBeanTask.HttpHtmlFormToBeanTaskDependencies;

/**
 * Tests the {@link HttpHtmlFormToBeanTask}.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpHtmlFormToBeanTaskTest extends OfficeFrameTestCase {

	/**
	 * Mock {@link TaskContext}.
	 */
	@SuppressWarnings("unchecked")
	private TaskContext<HttpHtmlFormToBeanTask, HttpHtmlFormToBeanTaskDependencies, None> taskContext = this
			.createMock(TaskContext.class);

	/**
	 * Mock {@link ServerHttpConnection}.
	 */
	private ServerHttpConnection connection = this
			.createMock(ServerHttpConnection.class);

	/**
	 * Mock {@link HttpRequest}.
	 */
	private HttpRequest httpRequest = this.createMock(HttpRequest.class);

	/**
	 * Ensures able to map from a GET.
	 */
	public void testGet() throws Throwable {
		this.doTest("POST", "/path?name=value", null, "value");
	}

	/**
	 * Ensures able to map from a POST.
	 */
	public void testPost() throws Throwable {
		this.doTest("POST", "/path", "name=value", "value");
	}

	/**
	 * Ensures able to load via alias.
	 */
	public void testAlias() throws Throwable {
		this.doTest("GET", "/path?another=value", null, "value", "another",
				"name");
	}

	/**
	 * Does the test.
	 * 
	 * @param method
	 *            HTTP method.
	 * @param uriPath
	 *            URI path.
	 * @param httpBody
	 *            HTTP body content. May be <code>null</code> if no body
	 *            content.
	 * @param expectedName
	 *            Expected value for the bean property name.
	 * @param aliasMappings
	 *            Mapping of alias name to property name.
	 */
	private void doTest(String method, String uriPath, String httpBody,
			String expectedName, String... aliasMappings) throws Throwable {

		// Obtain the input stream body
		final InputStream body = new ByteArrayInputStream(
				httpBody == null ? new byte[0] : "name=value".getBytes());

		// Create the listing of alias mappings
		Map<String, String> aliases = new HashMap<String, String>(0);
		for (int i = 0; i < aliasMappings.length; i += 2) {
			String alias = aliasMappings[i];
			String propertyName = aliasMappings[i + 1];
			aliases.put(alias, propertyName);
		}

		// Create the task
		HttpHtmlFormToBeanTask task = new HttpHtmlFormToBeanTask(
				HtmlFormBean.class, aliases);

		// Record actions on mocks
		this
				.recordReturn(
						this.taskContext,
						this.taskContext
								.getObject(HttpHtmlFormToBeanTaskDependencies.SERVER_HTTP_CONNECTION),
						this.connection);
		this.recordReturn(this.connection, this.connection.getHttpRequest(),
				this.httpRequest);
		this.recordReturn(this.httpRequest, this.httpRequest.getMethod(),
				method);
		this
				.recordReturn(this.httpRequest, this.httpRequest.getPath(),
						uriPath);
		this.recordReturn(this.httpRequest, this.httpRequest.getBody(), body);

		// Replay mocks
		this.replayMockObjects();

		// Execute the task
		HtmlFormBean bean = (HtmlFormBean) task.doTask(this.taskContext);

		// Verify functionality
		this.verifyMockObjects();

		// Verify properties loaded
		assertEquals("Incorrect value for property name", "value", bean.name);
	}
}
