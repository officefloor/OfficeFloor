/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2012 Daniel Sagenschneider
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

import java.io.FileNotFoundException;
import java.io.IOException;

import net.officefloor.compile.spi.work.source.TaskTypeBuilder;
import net.officefloor.compile.spi.work.source.WorkSource;
import net.officefloor.compile.spi.work.source.WorkSourceContext;
import net.officefloor.compile.spi.work.source.WorkTypeBuilder;
import net.officefloor.compile.spi.work.source.impl.AbstractWorkSource;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.execute.TaskContext;
import net.officefloor.frame.util.AbstractSingleTask;
import net.officefloor.plugin.socket.server.http.HttpResponse;
import net.officefloor.plugin.socket.server.http.ServerHttpConnection;
import net.officefloor.plugin.web.http.location.HttpApplicationLocationMangedObject;
import net.officefloor.plugin.web.http.resource.AbstractHttpFile;
import net.officefloor.plugin.web.http.resource.FileExtensionHttpFileDescriber;
import net.officefloor.plugin.web.http.resource.HttpFile;
import net.officefloor.plugin.web.http.resource.HttpResource;
import net.officefloor.plugin.web.http.resource.HttpResourceFactory;

/**
 * {@link WorkSource} for always sending a particular {@link HttpFile}.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpFileWorkSource extends
		AbstractWorkSource<HttpFileWorkSource.SendHttpFileTask> {

	/**
	 * Dependency keys for the {@link ClasspathHttpFileTask}.
	 */
	public static enum DependencyKeys {
		SERVER_HTTP_CONNECTION
	}

	/**
	 * Property to obtain the path to the resource on the class path.
	 */
	public static final String PROPERTY_RESOURCE_PATH = "resource.path";

	/**
	 * {@link Task} name for writing the {@link HttpFile}.
	 */
	public static final String TASK_HTTP_FILE = "file";

	/*
	 * ========================== WorkSource ================================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
		context.addProperty(PROPERTY_RESOURCE_PATH, "Resource Path");
	}

	@Override
	public void sourceWork(WorkTypeBuilder<SendHttpFileTask> workTypeBuilder,
			WorkSourceContext context) throws Exception {

		// Obtain the properties
		String resourcePath = context.getProperty(PROPERTY_RESOURCE_PATH);

		// Create the class path HTTP resource factory
		HttpResourceFactory httpResourceFactory = SourceHttpResourceFactory
				.createHttpResourceFactory(context);

		// Add the file extension HTTP file describer by file extension
		FileExtensionHttpFileDescriber describer = new FileExtensionHttpFileDescriber();
		describer.loadDefaultDescriptions();
		describer.loadDescriptions(context.getProperties());
		httpResourceFactory.addHttpFileDescriber(describer);

		// Ensure resource path canonical
		if (!(resourcePath.startsWith("/"))) {
			resourcePath = "/" + resourcePath;
		}
		resourcePath = HttpApplicationLocationMangedObject
				.transformToCanonicalPath(resourcePath);

		// Obtain the HTTP file
		HttpResource resource = httpResourceFactory
				.createHttpResource(resourcePath);
		if (!(resource instanceof HttpFile)) {
			throw new FileNotFoundException("Can not find resource '"
					+ resourcePath + "'");
		}
		HttpFile file = (HttpFile) resource;

		// Create the factory for the task
		SendHttpFileTask factory = new SendHttpFileTask(file);

		// Register the task information
		workTypeBuilder.setWorkFactory(factory);
		TaskTypeBuilder<DependencyKeys, None> task = workTypeBuilder
				.addTaskType(TASK_HTTP_FILE, factory, DependencyKeys.class,
						None.class);
		task.addObject(ServerHttpConnection.class).setKey(
				DependencyKeys.SERVER_HTTP_CONNECTION);
		task.addEscalation(IOException.class);
	}

	/**
	 * {@link Task} to send the {@link HttpFile}.
	 */
	public static class SendHttpFileTask extends
			AbstractSingleTask<SendHttpFileTask, DependencyKeys, None> {

		/**
		 * {@link HttpFile}.
		 */
		private final HttpFile file;

		/**
		 * Initiate.
		 * 
		 * @param file
		 *            {@link HttpFile}.
		 */
		public SendHttpFileTask(HttpFile file) {
			this.file = file;
		}

		/*
		 * ========================= Task =================================
		 */

		@Override
		public Object doTask(
				TaskContext<SendHttpFileTask, DependencyKeys, None> context)
				throws IOException {

			// Obtain the response
			ServerHttpConnection connection = (ServerHttpConnection) context
					.getObject(DependencyKeys.SERVER_HTTP_CONNECTION);
			HttpResponse response = connection.getHttpResponse();

			// Write the HTTP file
			AbstractHttpFile.writeHttpFile(this.file, response);

			// Nothing to return
			return null;
		}
	}

}