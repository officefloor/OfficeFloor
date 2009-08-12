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
package net.officefloor.plugin.work.http.file;

import java.io.ByteArrayOutputStream;

import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.execute.TaskContext;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.socket.server.http.HttpRequest;
import net.officefloor.plugin.socket.server.http.HttpResponse;
import net.officefloor.plugin.socket.server.http.ServerHttpConnection;
import net.officefloor.plugin.socket.server.http.parse.UsAsciiUtil;
import net.officefloor.plugin.socket.server.http.protocol.HttpStatus;
import net.officefloor.plugin.stream.OutputBufferStream;
import net.officefloor.plugin.work.http.file.HttpFileTask.HttpFileTaskDependencies;

/**
 * Tests the {@link HttpFileTask}.
 *
 * @author Daniel Sagenschneider
 */
public class HttpFileTaskTest extends OfficeFrameTestCase {

	/**
	 * Mock {@link TaskContext}.
	 */
	@SuppressWarnings("unchecked")
	private TaskContext<HttpFileTask, HttpFileTaskDependencies, None> taskContext = this
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
	 * Mock {@link HttpResponse}.
	 */
	private HttpResponse httpResponse = this.createMock(HttpResponse.class);

	/**
	 * Mock body.
	 */
	private ByteArrayOutputStream body = new ByteArrayOutputStream();

	/**
	 * Validates not finding file.
	 */
	public void testNoFile() throws Throwable {
		this.doTest("FileNotAvailable.txt", "FileNotAvailable.txt", false);
	}

	/**
	 * Validates able to load a file.
	 */
	public void testLoadHtmlFile() throws Throwable {
		this.doTest("index.html", "index.html", true);
	}

	/**
	 * Validates able load file with parameters on path.
	 */
	public void testLoadHtmlWithParameters() throws Throwable {
		this.doTest("index.html?name=value", "index.html", true);
	}

	/**
	 * Validates able load default index page.
	 */
	public void testDefaultIndexPage() throws Throwable {
		this.doTest("", "index.html", true);
	}

	/**
	 * Do the test.
	 *
	 * @param fileName
	 *            Name of the file.
	 * @param isFileAvailable
	 *            Flag indicating if the file is available.
	 */
	private void doTest(String path, String fileName, boolean isFileAvailable)
			throws Throwable {

		// Mocks
		OutputBufferStream bodyOutputBufferStream = this
				.createMock(OutputBufferStream.class);

		// Obtain the package prefix and default index file name
		String packagePrefix = this.getClass().getPackage().getName().replace(
				'.', '/');
		String defaultIndexFileName = "index.html";

		// Create the HTTP file task
		HttpFileTask task = new HttpFileTask(packagePrefix,
				defaultIndexFileName);

		// Record actions on mocks
		this.recordReturn(this.taskContext, this.taskContext
				.getObject(HttpFileTaskDependencies.SERVER_HTTP_CONNECTION),
				this.connection);
		this.recordReturn(this.connection, this.connection.getHttpRequest(),
				this.httpRequest);
		this.recordReturn(this.httpRequest, this.httpRequest.getRequestURI(),
				path);
		this.recordReturn(this.connection, this.connection.getHttpResponse(),
				this.httpResponse);
		if (!isFileAvailable) {
			// Indicate file not available
			this.httpResponse.setStatus(HttpStatus._404);
		}
		this.recordReturn(this.httpResponse, this.httpResponse.getBody(),
				bodyOutputBufferStream);
		this.recordReturn(bodyOutputBufferStream, bodyOutputBufferStream
				.getOutputStream(), this.body);
		this.httpResponse.send();

		// Replay mocks
		this.replayMockObjects();

		// Execute the task
		task.doTask(this.taskContext);

		// Verify mocks
		this.verifyMockObjects();

		// Verify the body
		if (isFileAvailable) {
			// File is available
			UsAsciiUtil.assertEquals("File Available", this.body.toByteArray());
		} else {
			// File not available
			UsAsciiUtil.assertEquals("Can not find resource " + path, this.body
					.toByteArray());
		}
	}
}
