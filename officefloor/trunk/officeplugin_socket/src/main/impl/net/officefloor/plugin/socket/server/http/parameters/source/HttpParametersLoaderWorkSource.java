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
package net.officefloor.plugin.socket.server.http.parameters.source;

import java.util.HashMap;
import java.util.Map;

import net.officefloor.compile.spi.work.source.TaskTypeBuilder;
import net.officefloor.compile.spi.work.source.WorkSource;
import net.officefloor.compile.spi.work.source.WorkSourceContext;
import net.officefloor.compile.spi.work.source.WorkTypeBuilder;
import net.officefloor.compile.spi.work.source.impl.AbstractWorkSource;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.execute.TaskContext;
import net.officefloor.frame.util.AbstractSingleTask;
import net.officefloor.plugin.socket.server.http.HttpRequest;
import net.officefloor.plugin.socket.server.http.ServerHttpConnection;
import net.officefloor.plugin.socket.server.http.parameters.HttpParametersLoadException;
import net.officefloor.plugin.socket.server.http.parameters.HttpParametersLoader;
import net.officefloor.plugin.socket.server.http.parameters.HttpParametersLoaderImpl;

/**
 * {@link WorkSource} to load the {@link HttpRequest} parameters onto a
 * dependency Object.
 *
 * @author Daniel Sagenschneider
 */
/*
 * TODO provide testing (HttpParametersLoaderWorkSource)
 */
public class HttpParametersLoaderWorkSource
		extends
		AbstractWorkSource<HttpParametersLoaderWorkSource.HttpParametersLoaderTask> {

	/**
	 * Property to obtain the fully qualified type name of the Object to have
	 * parameters loaded on it.
	 */
	public static final String PROPERTY_TYPE_NAME = "type.name";

	/**
	 * Property to obtain whether the {@link HttpParametersLoader} is case
	 * sensitive in matching parameter names.
	 */
	public static final String PROPERTY_CASE_SENSITIVE = "case.sensitive";

	/**
	 * {@link HttpParametersLoader}.
	 */
	@SuppressWarnings("unchecked")
	private final HttpParametersLoader loader = new HttpParametersLoaderImpl<Object>();

	/*
	 * ===================== AbstractWorkSource ===========================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
		context.addProperty(PROPERTY_TYPE_NAME);
	}

	@Override
	@SuppressWarnings("unchecked")
	public void sourceWork(
			WorkTypeBuilder<HttpParametersLoaderTask> workTypeBuilder,
			WorkSourceContext context) throws Exception {

		// Obtain the type
		String typeName = context.getProperty(PROPERTY_TYPE_NAME);
		Class<?> type = context.getClassLoader().loadClass(typeName);

		// Obtain whether case sensitive (true by default)
		boolean isCaseSensitive = Boolean.parseBoolean(context.getProperty(
				PROPERTY_CASE_SENSITIVE, Boolean.toString(true)));

		// Create the alias mappings
		Map<String, String> aliasMappings = new HashMap<String, String>();
		// TODO load alias mappings from properties

		// Initialise the loader
		this.loader.init(type, aliasMappings, isCaseSensitive);

		// Create the task to load the HTTP parameters
		HttpParametersLoaderTask task = new HttpParametersLoaderTask();

		// Build the work
		workTypeBuilder.setWorkFactory(task);

		// Build the task
		TaskTypeBuilder<HttpParametersLoaderDependencies, None> taskBuilder = workTypeBuilder
				.addTaskType("LOADER", task,
						HttpParametersLoaderDependencies.class, None.class);
		taskBuilder.addObject(ServerHttpConnection.class).setKey(
				HttpParametersLoaderDependencies.SERVER_HTTP_CONNECTION);
		taskBuilder.addObject(type).setKey(
				HttpParametersLoaderDependencies.OBJECT);
		taskBuilder.setReturnType(type);
		taskBuilder.addEscalation(HttpParametersLoadException.class);
	}

	/**
	 * {@link Task} to load the {@link HttpRequest} parameters onto a dependency
	 * Object.
	 */
	public class HttpParametersLoaderTask
			extends
			AbstractSingleTask<HttpParametersLoaderTask, HttpParametersLoaderDependencies, None> {

		/*
		 * =========================== Task ===============================
		 */

		@Override
		@SuppressWarnings("unchecked")
		public Object doTask(
				TaskContext<HttpParametersLoaderTask, HttpParametersLoaderDependencies, None> context)
				throws HttpParametersLoadException {

			// Obtain the dependencies
			ServerHttpConnection connection = (ServerHttpConnection) context
					.getObject(HttpParametersLoaderDependencies.SERVER_HTTP_CONNECTION);
			Object object = context
					.getObject(HttpParametersLoaderDependencies.OBJECT);

			// Load the parameters onto the object
			HttpParametersLoaderWorkSource.this.loader.loadParameters(
					connection.getHttpRequest(), object);

			// Return the object
			return object;
		}
	}

}