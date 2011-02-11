/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2011 Daniel Sagenschneider
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

package net.officefloor.plugin.web.http.resource.source;

import java.io.IOException;

import net.officefloor.compile.spi.work.source.TaskFlowTypeBuilder;
import net.officefloor.compile.spi.work.source.TaskTypeBuilder;
import net.officefloor.compile.spi.work.source.WorkSource;
import net.officefloor.compile.spi.work.source.WorkSourceContext;
import net.officefloor.compile.spi.work.source.WorkTypeBuilder;
import net.officefloor.compile.spi.work.source.impl.AbstractWorkSource;
import net.officefloor.frame.api.execute.TaskContext;
import net.officefloor.plugin.socket.server.http.ServerHttpConnection;
import net.officefloor.plugin.web.http.resource.ClasspathHttpResourceFactory;
import net.officefloor.plugin.web.http.resource.FileExtensionHttpFileDescriber;
import net.officefloor.plugin.web.http.resource.HttpFile;
import net.officefloor.plugin.web.http.resource.HttpResource;
import net.officefloor.plugin.web.http.resource.HttpResourceCreationListener;
import net.officefloor.plugin.web.http.resource.HttpResourceFactory;
import net.officefloor.plugin.web.http.resource.InvalidHttpRequestUriException;
import net.officefloor.plugin.web.http.resource.source.HttpFileFactoryTask.DependencyKeys;

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

		// TODO allow configuring multiple default file names

		// Create the class path HTTP file factory
		HttpResourceFactory httpFileFactory = ClasspathHttpResourceFactory
				.getHttpResourceFactory(classpathPrefix, defaultFileName);

		// Add the file extension HTTP file describer by file extension
		FileExtensionHttpFileDescriber describer = new FileExtensionHttpFileDescriber();
		describer.loadDefaultDescriptions();
		describer.loadDescriptions(context.getProperties());
		httpFileFactory.addHttpFileDescriber(describer);

		// Create the handler for file not found
		HttpResourceCreationListener<HttpFileFactoryTaskFlows> fileNotFoundHandler = new HttpResourceCreationListener<HttpFileFactoryTaskFlows>() {
			@Override
			public void httpResourceCreated(HttpResource httpResource,
					ServerHttpConnection connection,
					TaskContext<?, ?, HttpFileFactoryTaskFlows> context)
					throws IOException {
				// Determine if the file exists
				if (!httpResource.isExist()) {
					// Invoke flow indicating file not found
					context.doFlow(
							HttpFileFactoryTaskFlows.HTTP_FILE_NOT_FOUND, null);
				}
			}
		};

		// Create the HTTP file factory task
		HttpFileFactoryTask<HttpFileFactoryTaskFlows> task = new HttpFileFactoryTask<HttpFileFactoryTaskFlows>(
				httpFileFactory, fileNotFoundHandler);

		// Load the type information of the work
		workTypeBuilder.setWorkFactory(task);

		// Load the task to create the HTTP file
		TaskTypeBuilder<DependencyKeys, HttpFileFactoryTaskFlows> taskTypeBuilder = workTypeBuilder
				.addTaskType("FindFile", task, DependencyKeys.class,
						HttpFileFactoryTaskFlows.class);
		taskTypeBuilder.addObject(ServerHttpConnection.class).setKey(
				DependencyKeys.SERVER_HTTP_CONNECTION);
		TaskFlowTypeBuilder<HttpFileFactoryTaskFlows> flowTypeBuilder = taskTypeBuilder
				.addFlow();
		flowTypeBuilder.setKey(HttpFileFactoryTaskFlows.HTTP_FILE_NOT_FOUND);
		flowTypeBuilder.setArgumentType(HttpFile.class);
		taskTypeBuilder.setReturnType(HttpFile.class);
		taskTypeBuilder.addEscalation(IOException.class);
		taskTypeBuilder.addEscalation(InvalidHttpRequestUriException.class);
	}

}