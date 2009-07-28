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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.execute.TaskContext;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.util.AbstractSingleTask;
import net.officefloor.plugin.socket.server.http.HttpRequest;
import net.officefloor.plugin.socket.server.http.HttpResponse;
import net.officefloor.plugin.socket.server.http.ServerHttpConnection;
import net.officefloor.plugin.socket.server.http.source.HttpStatus;
import net.officefloor.plugin.work.http.HttpException;

/**
 * {@link Work} and {@link Task} for serving {@link File} content as response.
 *
 * @author Daniel Sagenschneider
 */
public class HttpFileTask
		extends
		AbstractSingleTask<HttpFileTask, HttpFileTask.HttpFileTaskDependencies, None> {

	/**
	 * Dependencies for the {@link HttpFileTask}.
	 */
	public static enum HttpFileTaskDependencies {
		SERVER_HTTP_CONNECTION
	}

	/**
	 * Package to prefix to paths to find the file.
	 */
	private final String packagePrefix;

	/**
	 * Default index file name. Typically <code>index.html</code>.
	 */
	private final String defaultIndexFileName;

	/**
	 * Initialise.
	 *
	 * @param packagePrefix
	 *            Package to prefix to paths to find the file.
	 * @param defaultIndexFileName
	 *            Default index file name. Typically <code>index.html</code>.
	 */
	public HttpFileTask(String packagePrefix, String defaultIndexFileName) {
		// Ensure package prefix ends with '/'
		this.packagePrefix = (packagePrefix.endsWith("/") ? packagePrefix
				: packagePrefix + "/");
		this.defaultIndexFileName = defaultIndexFileName;
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
		String path = request.getRequestURI();
		int parameterStart = path.indexOf('?');
		if (parameterStart > 0) {
			// Obtain the path minus the parameters
			path = path.substring(0, parameterStart);
		}

		// Keep track of original request path (for possible file not found)
		String requestPath = path;

		// Determine if file (file expected to have extension)
		boolean isFile = (path.indexOf('.') > 0);

		// Prefix the package onto the path
		path = this.packagePrefix + path;

		// Obtain the class loader
		ClassLoader classLoader = Thread.currentThread()
				.getContextClassLoader();

		// Obtain the content based on whether directory or file
		InputStream content;
		if (isFile) {
			// Obtain the file
			content = classLoader.getResourceAsStream(path);
		} else {
			// Obtain default index file from the directory
			String directoryPath = (path.endsWith("/") ? path : path + "/")
					+ this.defaultIndexFileName;
			content = classLoader.getResourceAsStream(directoryPath);
		}

		// Obtain the response
		HttpResponse response = connection.getHttpResponse();

		// Handle if not find file
		if (content == null) {
			// Item not found
			response.setStatus(HttpStatus._404); // not found
			new OutputStreamWriter(response.getBody().getOutputStream())
					.append("Can not find resource " + requestPath).flush();
		} else {

			// TODO cache file and append as ByteBuffer

			// Return the file content as response
			OutputStream responseBody = response.getBody().getOutputStream();
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