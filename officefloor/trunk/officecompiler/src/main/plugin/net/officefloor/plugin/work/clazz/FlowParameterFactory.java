/*
 *  Office Floor, Application Server
 *  Copyright (C) 2006 Daniel Sagenschneider
 *
 *  This program is free software; you can redistribute it and/or modify it under the terms 
 *  of the GNU General Public License as published by the Free Software Foundation; either 
 *  version 2 of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with this program; 
 *  if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, 
 *  MA 02111-1307 USA
 */
package net.officefloor.plugin.work.clazz;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;

import net.officefloor.frame.api.execute.FlowFuture;
import net.officefloor.frame.api.execute.TaskContext;
import net.officefloor.frame.internal.structure.Flow;

/**
 * {@link ParameterFactory} to obtain the {@link Flow}.
 * 
 * @author Daniel
 */
public class FlowParameterFactory implements ParameterFactory {

	/**
	 * {@link Constructor} for the {@link Proxy} instance.
	 */
	private final Constructor<?> proxyConstructor;

	/**
	 * {@link FlowMethodMetaData} instances by its {@link Method} name.
	 */
	private final Map<String, FlowMethodMetaData> methodMetaDatas;

	/**
	 * Initiate.
	 * 
	 * @param classLoader
	 *            {@link ClassLoader}.
	 * @param flowInterface
	 *            {@link FlowInterface} class.
	 * @param methodMetaDatas
	 *            {@link FlowMethodMetaData} instances by its {@link Method}
	 *            name.
	 */
	public FlowParameterFactory(ClassLoader classLoader,
			Class<?> flowInterface,
			Map<String, FlowMethodMetaData> methodMetaDatas) throws Exception {
		this.methodMetaDatas = methodMetaDatas;

		// Create the proxy class and obtain the constructor to use
		Class<?> proxyClass = Proxy.getProxyClass(classLoader, flowInterface);
		this.proxyConstructor = proxyClass
				.getConstructor(InvocationHandler.class);
	}

	/*
	 * ==================== ParameterFactory ========================
	 */

	@Override
	public Object createParameter(TaskContext<?, ?, ?> context)
			throws Exception {
		// Return a new instance of the proxy to invoke the flows
		return this.proxyConstructor
				.newInstance(new Object[] { new FlowInvocationHandler(context) });
	}

	/**
	 * {@link Flow} {@link InvocationHandler}.
	 */
	private class FlowInvocationHandler implements InvocationHandler {

		/**
		 * {@link TaskContext}.
		 */
		private final TaskContext<?, ?, ?> taskContext;

		/**
		 * Initiate.
		 * 
		 * @param taskContext
		 *            {@link TaskContext}.
		 */
		public FlowInvocationHandler(TaskContext<?, ?, ?> taskContext) {
			this.taskContext = taskContext;
		}

		/*
		 * ================ InvocationHandler ========================
		 */

		@Override
		public Object invoke(Object proxy, Method method, Object[] args)
				throws Throwable {

			// Obtain the method meta-data
			String methodName = method.getName();
			FlowMethodMetaData metaData = FlowParameterFactory.this.methodMetaDatas
					.get(methodName);

			// Obtain the parameter
			Object parameter = (metaData.isParameter() ? args[0] : null);

			// Invoke the flow
			FlowFuture flowFuture = this.taskContext.doFlow(metaData
					.getFlowIndex(), parameter);

			// Return the flow future if required
			return (metaData.isReturnFlowFuture() ? flowFuture : null);
		}
	}

}