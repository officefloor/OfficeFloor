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
import java.util.HashMap;
import java.util.Map;

import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.execute.TaskContext;
import net.officefloor.frame.util.AbstractSingleTask;

import com.google.gwt.user.client.rpc.IncompatibleRemoteServiceException;
import com.google.gwt.user.server.rpc.RPCRequest;

/**
 * {@link Task} to handle GWT Service.
 * 
 * @author Daniel Sagenschneider
 */
public class GwtServiceTask
		extends
		AbstractSingleTask<GwtServiceTask, GwtServiceTask.Dependencies, Indexed> {

	/**
	 * Dependencies for the {@link GwtServiceTask}.
	 */
	public static enum Dependencies {
		SERVER_GWT_RPC_CONNECTION
	}

	/**
	 * Mapping of {@link Method} name to flow index.
	 */
	private final Map<String, Integer> methodToFlow;

	/**
	 * Initiate.
	 * 
	 * @param metaData
	 *            {@link GwtAsyncMethodMetaData} instances.
	 */
	public GwtServiceTask(GwtAsyncMethodMetaData[] metaData) {
		this.methodToFlow = new HashMap<String, Integer>(metaData.length);
		for (int i = 0; i < metaData.length; i++) {
			GwtAsyncMethodMetaData async = metaData[i];
			this.methodToFlow.put(async.getMethodName(), new Integer(i));
		}
	}

	/*
	 * ======================== Task =============================
	 */

	@Override
	public Object doTask(
			TaskContext<GwtServiceTask, Dependencies, Indexed> context)
			throws Throwable {

		// Obtain the request
		ServerGwtRpcConnection<?> connection = (ServerGwtRpcConnection<?>) context
				.getObject(Dependencies.SERVER_GWT_RPC_CONNECTION);

		// Obtain the GWT request
		RPCRequest rpc = connection.getRpcRequest();

		// Obtain the flow index
		String methodName = rpc.getMethod().getName();
		Integer flowIndex = this.methodToFlow.get(methodName);
		if (flowIndex == null) {
			// Indicate unknown GWT service method
			connection.onFailure(new IncompatibleRemoteServiceException(
					"Unknown service method '" + methodName + "(...)'"));
			return null; // No further processing
		}

		// Obtain the parameter value
		Object[] parameters = rpc.getParameters();
		Object parameter = (parameters.length == 0 ? null : parameters[0]);

		// Invoke flow to service request
		context.doFlow(flowIndex.intValue(), parameter);

		// Processing by flows
		return null;
	}

}