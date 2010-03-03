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

import java.io.IOException;

import net.officefloor.compile.spi.work.source.TaskFlowTypeBuilder;
import net.officefloor.compile.spi.work.source.TaskTypeBuilder;
import net.officefloor.compile.spi.work.source.WorkSource;
import net.officefloor.compile.spi.work.source.WorkSourceContext;
import net.officefloor.compile.spi.work.source.WorkTypeBuilder;
import net.officefloor.compile.spi.work.source.impl.AbstractWorkSource;
import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.execute.TaskContext;
import net.officefloor.plugin.socket.server.http.ServerHttpConnection;
import net.officefloor.plugin.socket.server.http.file.ClasspathHttpFileFactory;
import net.officefloor.plugin.socket.server.http.file.FileExtensionHttpFileDescriber;
import net.officefloor.plugin.socket.server.http.file.HttpFile;
import net.officefloor.plugin.socket.server.http.file.HttpFileFactory;
import net.officefloor.plugin.socket.server.http.file.HttpFileCreationListener;
import net.officefloor.plugin.socket.server.http.file.InvalidHttpRequestUriException;

/**
 * {@link WorkSource} to locate a {@link HttpFile} on the class path.
 * 
 * @author Daniel Sagenschneider
 */
public class ClasspathHttpFileFactoryWorkSource
		extends
		AbstractWorkSource<HttpFileFactoryTask<ClasspathHttpFileFactoryWorkSource.HttpFileFactoryTaskFlows>> {

	/**
	 * Enum of flows for the {@link HttpFileFactoryTask}.
	 */
	public static enum HttpFileFactoryTaskFlows {
		HTTP_FILE_NOT_FOUND
	}

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

	/*
	 * ==================== AbstractWorkSource =============================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
		context.addProperty(PROPERTY_CLASSPATH_PREFIX);
		context.addProperty(PROPERTY_DEFAULT_FILE_NAME);
	}

	@Override
	public void sourceWork(
			WorkTypeBuilder<HttpFileFactoryTask<HttpFileFactoryTaskFlows>> workTypeBuilder,
			WorkSourceContext context) throws Exception {

		// Obtain the properties
		String classpathPrefix = context.getProperty(PROPERTY_CLASSPATH_PREFIX);
		String defaultFileName = context
				.getProperty(PROPERTY_DEFAULT_FILE_NAME);

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

		// Create the handler for file not found
		HttpFileCreationListener<HttpFileFactoryTaskFlows> fileNotFoundHandler = new HttpFileCreationListener<HttpFileFactoryTaskFlows>() {
			@Override
			public void httpFileCreated(HttpFile httpFile,
					ServerHttpConnection connection,
					TaskContext<?, ?, HttpFileFactoryTaskFlows> context)
					throws IOException {
				// Determine if the file exists
				if (!httpFile.isExist()) {
					// Invoke flow indicating file not found
					context.doFlow(
							HttpFileFactoryTaskFlows.HTTP_FILE_NOT_FOUND, null);
				}
			}
		};

		// Create the HTTP file factory task
		HttpFileFactoryTask<HttpFileFactoryTaskFlows> task = new HttpFileFactoryTask<HttpFileFactoryTaskFlows>(
				httpFileFactory, fileNotFoundHandler, -1);

		// Load the type information of the work
		workTypeBuilder.setWorkFactory(task);

		// Load the task to create the HTTP file
		TaskTypeBuilder<Indexed, HttpFileFactoryTaskFlows> taskTypeBuilder = workTypeBuilder
				.addTaskType("FindFile", task, Indexed.class,
						HttpFileFactoryTaskFlows.class);
		taskTypeBuilder.addObject(ServerHttpConnection.class).setLabel(
				"SERVER_HTTP_CONNECTION");
		TaskFlowTypeBuilder<HttpFileFactoryTaskFlows> flowTypeBuilder = taskTypeBuilder
				.addFlow();
		flowTypeBuilder.setKey(HttpFileFactoryTaskFlows.HTTP_FILE_NOT_FOUND);
		flowTypeBuilder.setArgumentType(HttpFile.class);
		taskTypeBuilder.setReturnType(HttpFile.class);
		taskTypeBuilder.addEscalation(IOException.class);
		taskTypeBuilder.addEscalation(InvalidHttpRequestUriException.class);
	}

}