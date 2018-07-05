/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package net.officefloor.plugin.clazz;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;

import net.officefloor.frame.api.function.FlowCallback;
import net.officefloor.frame.api.function.FunctionFlowContext;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.plugin.managedfunction.clazz.FlowInterface;
import net.officefloor.plugin.managedfunction.clazz.ManagedFunctionParameterFactory;

/**
 * {@link ManagedFunctionParameterFactory} to obtain the {@link Flow}.
 * 
 * @author Daniel Sagenschneider
 */
public class ClassFlowParameterFactory {

	/**
	 * {@link Constructor} for the {@link Proxy} instance.
	 */
	private final Constructor<?> proxyConstructor;

	/**
	 * {@link ClassFlowMethodMetaData} instances by its {@link Method} name.
	 */
	private final Map<String, ClassFlowMethodMetaData> methodMetaDatas;

	/**
	 * Initiate.
	 * 
	 * @param classLoader
	 *            {@link ClassLoader}.
	 * @param flowInterface
	 *            {@link FlowInterface} class.
	 * @param methodMetaDatas
	 *            {@link ClassFlowMethodMetaData} instances by its {@link Method}
	 *            name.
	 * @throws Exception
	 *             If fails to create the {@link Proxy}.
	 */
	@SuppressWarnings("deprecation")
	public ClassFlowParameterFactory(ClassLoader classLoader, Class<?> flowInterface,
			Map<String, ClassFlowMethodMetaData> methodMetaDatas) throws Exception {
		this.methodMetaDatas = methodMetaDatas;

		// Create the proxy class and obtain the constructor to use
		Class<?> proxyClass = Proxy.getProxyClass(classLoader, flowInterface);
		this.proxyConstructor = proxyClass.getConstructor(InvocationHandler.class);
	}

	/**
	 * Obtains the {@link ClassFlowMethodMetaData}.
	 * 
	 * @return {@link ClassFlowMethodMetaData}.
	 */
	public ClassFlowMethodMetaData[] getFlowMethodMetaData() {
		return this.methodMetaDatas.values().toArray(new ClassFlowMethodMetaData[this.methodMetaDatas.size()]);
	}

	/**
	 * Creates the parameter.
	 * 
	 * @param context
	 *            {@link FunctionFlowContext}.
	 * @return Parameter.
	 * @throws Exception
	 *             If fails to create the parameter.
	 */
	public Object createParameter(FunctionFlowContext<?> context) throws Exception {
		// Return a new instance of the proxy to invoke the flows
		return this.proxyConstructor.newInstance(new Object[] { new FlowInvocationHandler(context) });
	}

	/**
	 * {@link Flow} {@link InvocationHandler}.
	 */
	private class FlowInvocationHandler implements InvocationHandler {

		/**
		 * {@link FunctionFlowContext}.
		 */
		private final FunctionFlowContext<?> functionFlowContext;

		/**
		 * Initiate.
		 * 
		 * @param functionFlowContext
		 *            {@link FunctionFlowContext}.
		 */
		public FlowInvocationHandler(FunctionFlowContext<?> functionFlowContext) {
			this.functionFlowContext = functionFlowContext;
		}

		/*
		 * ================ InvocationHandler ========================
		 */

		@Override
		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

			// Obtain the method meta-data
			String methodName = method.getName();
			ClassFlowMethodMetaData metaData = ClassFlowParameterFactory.this.methodMetaDatas.get(methodName);

			// Obtain the parameter and flow callback
			Object parameter = null;
			FlowCallback flowCallback = null;
			int flowCallbackIndex = 0;
			if (metaData.isParameter()) {
				parameter = args[0];
				flowCallbackIndex = 1;
			}
			if (metaData.isFlowCallback()) {
				flowCallback = (FlowCallback) args[flowCallbackIndex];
			}

			// Invoke the flow
			this.functionFlowContext.doFlow(metaData.getFlowIndex(), parameter, flowCallback);

			// Never returns a value
			return null;
		}
	}

}