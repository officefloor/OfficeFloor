/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2013 Daniel Sagenschneider
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
package net.officefloor.plugin.gwt.service;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

import net.officefloor.compile.spi.work.source.TaskFlowTypeBuilder;
import net.officefloor.compile.spi.work.source.TaskTypeBuilder;
import net.officefloor.compile.spi.work.source.WorkSource;
import net.officefloor.compile.spi.work.source.WorkSourceContext;
import net.officefloor.compile.spi.work.source.WorkTypeBuilder;
import net.officefloor.compile.spi.work.source.impl.AbstractWorkSource;
import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.execute.Task;
import net.officefloor.plugin.gwt.service.GwtServiceTask.Dependencies;

/**
 * {@link WorkSource} for a GWT Service.
 * 
 * @author Daniel Sagenschneider
 */
public class GwtServiceWorkSource extends AbstractWorkSource<GwtServiceTask> {

	/**
	 * Name of property specifying the GWT Async service interface.
	 */
	public static final String PROPERTY_GWT_ASYNC_SERVICE_INTERFACE = "gwt.async.service.interface";

	/**
	 * Name of the {@link Task} for servicing the GWT request.
	 */
	public static final String SERVICE_TASK_NAME = "service";

	/*
	 * ====================== WorkSource ===========================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
		context.addProperty(PROPERTY_GWT_ASYNC_SERVICE_INTERFACE, "Interface");
	}

	@Override
	public void sourceWork(WorkTypeBuilder<GwtServiceTask> workTypeBuilder,
			WorkSourceContext context) throws Exception {

		// Load the GWT async service interface
		String gwtAsyncServiceInterfaceName = context
				.getProperty(PROPERTY_GWT_ASYNC_SERVICE_INTERFACE);
		Class<?> gwtAsyncServiceInterface = context
				.loadClass(gwtAsyncServiceInterfaceName);

		// Obtain the methods (sorted by name for consistency)
		Method[] methods = gwtAsyncServiceInterface.getMethods();
		Arrays.sort(methods, new Comparator<Method>() {
			@Override
			public int compare(Method a, Method b) {
				return String.CASE_INSENSITIVE_ORDER.compare(a.getName(),
						b.getName());
			}
		});

		// Create the array of async meta-data
		GwtAsyncMethodMetaData[] asyncMetaData = new GwtAsyncMethodMetaData[methods.length];
		Set<String> uniqueMethodNames = new HashSet<String>();
		for (int i = 0; i < asyncMetaData.length; i++) {
			Method method = methods[i];

			// Obtain the GWT async meta-data
			GwtAsyncMethodMetaData metaData = new GwtAsyncMethodMetaData(method);
			String error = metaData.getError();
			if (error != null) {
				throw new IllegalArgumentException("Invalid async method "
						+ method.getDeclaringClass().getSimpleName() + "."
						+ method.getName() + ": " + error);
			}

			// Determine if duplicate method name
			String methodName = metaData.getMethodName();
			if (uniqueMethodNames.contains(methodName)) {
				throw new IllegalArgumentException(
						"Duplicate GWT Service Async method name '"
								+ method.getDeclaringClass().getSimpleName()
								+ "."
								+ methodName
								+ "(...)'. "
								+ "Method names must be unique per GWT service interface.");
			}
			uniqueMethodNames.add(methodName);

			// Register the async meta-data
			asyncMetaData[i] = metaData;
		}

		// Create the factory
		GwtServiceTask factory = new GwtServiceTask(asyncMetaData);

		// Register the work
		workTypeBuilder.setWorkFactory(factory);

		// Register the task
		TaskTypeBuilder<Dependencies, Indexed> task = workTypeBuilder
				.addTaskType(SERVICE_TASK_NAME, factory, Dependencies.class,
						Indexed.class);
		task.addObject(ServerGwtRpcConnection.class).setKey(
				Dependencies.SERVER_GWT_RPC_CONNECTION);

		// Load the output flows
		for (GwtAsyncMethodMetaData metaData : asyncMetaData) {
			TaskFlowTypeBuilder<Indexed> flow = task.addFlow();

			// Provide method name as label
			flow.setLabel(metaData.getMethodName());

			// Provide argument type (if provided)
			Class<?> argumentType = metaData.getParameterType();
			if (argumentType != null) {
				flow.setArgumentType(argumentType);
			}
		}
	}
}