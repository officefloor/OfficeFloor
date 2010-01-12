/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2010 Daniel Sagenschneider
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
package net.officefloor.plugin.socket.server.http.file.source;

import java.io.FileNotFoundException;
import java.io.IOException;

import net.officefloor.compile.spi.work.source.TaskTypeBuilder;
import net.officefloor.compile.spi.work.source.WorkSource;
import net.officefloor.compile.spi.work.source.WorkSourceContext;
import net.officefloor.compile.spi.work.source.WorkTypeBuilder;
import net.officefloor.compile.spi.work.source.impl.AbstractWorkSource;
import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.execute.TaskContext;
import net.officefloor.plugin.socket.server.http.HttpResponse;
import net.officefloor.plugin.socket.server.http.ServerHttpConnection;
import net.officefloor.plugin.socket.server.http.file.ClasspathHttpFileFactory;
import net.officefloor.plugin.socket.server.http.file.FileExtensionHttpFileDescriber;
import net.officefloor.plugin.socket.server.http.file.HttpFile;
import net.officefloor.plugin.socket.server.http.file.HttpFileFactory;
import net.officefloor.plugin.socket.server.http.file.HttpFileCreationListener;
import net.officefloor.plugin.socket.server.http.file.InvalidHttpRequestUriException;
import net.officefloor.plugin.socket.server.http.protocol.HttpStatus;
import net.officefloor.plugin.stream.OutputBufferStream;

/**
 * <p>
 * {@link WorkSource} to send a {@link HttpFile}.
 * <p>
 * This provides common handling of sending a {@link HttpFile} by:
 * <ol>
 * <li>Creating the {@link HttpFile}</li>
 * <li>Sending the {@link HttpFile}</li>
 * <li>Sending a &quot;not found&quot; response if the {@link HttpFile} is not
 * found</li>
 * <li>Propagating failures in sending the {@link HttpFile}</li>
 * </ol>
 * 
 * @author Daniel Sagenschneider
 */
public class ClasspathHttpFileSenderWorkSource extends
		AbstractWorkSource<HttpFileFactoryTask<None>> {

	/**
	 * Property to obtain the class path prefix on the request URI path to
	 * locate the {@link HttpFile}.
	 */
	public static final String PROPERTY_CLASSPATH_PREFIX = "classpath.prefix";

	/**
	 * Property to obtain the default file name should the request URI path
	 * resolve to a directory.
	 */
	public static final String PROPERTY_DEFAULT_FILE_NAME = "default.file.name";

	/**
	 * Property to obtain the path to the file containing the &quot;file not
	 * found&quot; response.
	 */
	public static final String PROPERTY_FILE_NOT_FOUND_CONTENT_PATH = "file.not.found.content.path";

	/*
	 * ============================ WorkSource ================================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
		context.addProperty(PROPERTY_CLASSPATH_PREFIX);
		context.addProperty(PROPERTY_DEFAULT_FILE_NAME);
		context.addProperty(PROPERTY_FILE_NOT_FOUND_CONTENT_PATH);
	}

	@Override
	public void sourceWork(
			WorkTypeBuilder<HttpFileFactoryTask<None>> workTypeBuilder,
			WorkSourceContext context) throws Exception {

		// Obtain the properties
		String classpathPrefix = context.getProperty(PROPERTY_CLASSPATH_PREFIX);
		String defaultFileName = context
				.getProperty(PROPERTY_DEFAULT_FILE_NAME);
		String fileNotFoundContentPath = context
				.getProperty(PROPERTY_FILE_NOT_FOUND_CONTENT_PATH);

		// Obtain the class loader to use to find files
		ClassLoader classLoader = context.getClassLoader();

		// Create the class path HTTP file factory
		HttpFileFactory httpFileFactory = new ClasspathHttpFileFactory(
				classLoader, classpathPrefix, defaultFileName);

		// Add the file extension HTTP file describer by file extension
		FileExtensionHttpFileDescriber describer = new FileExtensionHttpFileDescriber();
		describer.loadDefaultDescriptions();
		describer.loadDescriptions(context.getProperties());
		httpFileFactory.addHttpFileDescriber(describer);

		// Obtain the file not found content
		final HttpFile fileNotFoundContent = ClasspathHttpFileFactory
				.createHttpFile(classLoader, fileNotFoundContentPath);
		if (!fileNotFoundContent.isExist()) {
			// Must have file not found content
			throw new FileNotFoundException(
					"Can not obtain file not found content: "
							+ fileNotFoundContentPath);
		}

		// Create the HTTP file creation listener
		HttpFileCreationListener<None> httpFileCreationListener = new HttpFileCreationListener<None>() {
			@Override
			public void httpFileCreated(HttpFile httpFile,
					ServerHttpConnection connection,
					TaskContext<?, ?, None> context) throws IOException {

				// Obtain the response to write
				HttpResponse response = connection.getHttpResponse();
				OutputBufferStream body = response.getBody();

				// Determine if HTTP file exists
				if (httpFile.isExist()) {
					// Send the HTTP File
					body.append(httpFile.getContents());

					// Specify found status
					response.setStatus(HttpStatus.SC_OK);

				} else {
					// File not found so write file not found content
					body.append(fileNotFoundContent.getContents());

					// Specify not found status
					response.setStatus(HttpStatus.SC_NOT_FOUND);
				}

				// Send the response
				response.send();
			}
		};

		// Create the HTTP file factory task
		HttpFileFactoryTask<None> task = new HttpFileFactoryTask<None>(
				httpFileFactory, httpFileCreationListener, -1);

		// Load the type information of the work
		workTypeBuilder.setWorkFactory(task);

		// Load the task to send the HTTP file
		TaskTypeBuilder<Indexed, None> taskTypeBuilder = workTypeBuilder
				.addTaskType("SendFile", task, Indexed.class, None.class);
		taskTypeBuilder.addObject(ServerHttpConnection.class).setLabel(
				"SERVER_HTTP_CONNECTION");
		taskTypeBuilder.addEscalation(IOException.class);
		taskTypeBuilder.addEscalation(InvalidHttpRequestUriException.class);
	}

}