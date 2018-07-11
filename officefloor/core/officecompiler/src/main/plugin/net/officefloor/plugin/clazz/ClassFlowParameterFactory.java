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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;

import net.officefloor.compile.impl.compile.OfficeFloorJavaCompiler;
import net.officefloor.compile.impl.compile.OfficeFloorJavaCompiler.ClassName;
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
	 * {@link FunctionalInterface} to create the flows object.
	 */
	@FunctionalInterface
	private static interface FlowFactory {

		/**
		 * Creates the flows object.
		 * 
		 * @param context {@link FunctionFlowContext}.
		 * @return Flows object.
		 * @throws Exception If fails to create the flows object.
		 */
		Object createFlows(FunctionFlowContext<?> context) throws Exception;
	}

	/**
	 * {@link FlowFactory} to create the parameter from the
	 * {@link FunctionFlowContext}.
	 */
	private final FlowFactory flowFactory;

	/**
	 * {@link ClassFlowMethodMetaData} instances by its {@link Method} name.
	 */
	private final Map<String, ClassFlowMethodMetaData> methodMetaDatas;

	/**
	 * Initiate.
	 * 
	 * @param classLoader     {@link ClassLoader}.
	 * @param flowInterface   {@link FlowInterface} class.
	 * @param methodMetaDatas {@link ClassFlowMethodMetaData} instances by its
	 *                        {@link Method} name.
	 * @throws Exception If fails to create the {@link Proxy}.
	 */
	public ClassFlowParameterFactory(ClassLoader classLoader, Class<?> flowInterface,
			Map<String, ClassFlowMethodMetaData> methodMetaDatas) throws Exception {
		this.methodMetaDatas = methodMetaDatas;

		// Determine if the compiler is available
		OfficeFloorJavaCompiler compiler = OfficeFloorJavaCompiler.newInstance(classLoader);
		if (compiler == null) {

			// Fallback to proxy class
			Class<?>[] interfaces = new Class[] { flowInterface };
			this.flowFactory = (context) -> Proxy.newProxyInstance(classLoader, interfaces,
					new FlowInvocationHandler(context));

		} else {
			// Create compiled implementation (for performance and reduced GC pressure)
			StringWriter sourceBuffer = new StringWriter();
			PrintWriter source = new PrintWriter(sourceBuffer);

			// Create the class name
			ClassName className = compiler.createClassName(flowInterface.getName());

			// Write class definition
			source.println("package " + className.getPackageName() + ";");
			source.println("public class " + className.getClassName() + " implements "
					+ compiler.getSourceName(flowInterface) + "{");

			// Write the constructor
			compiler.writeConstructor(source, className.getClassName(),
					compiler.createField(FunctionFlowContext.class, "context"));

			// Write the flow methods
			for (ClassFlowMethodMetaData metaData : methodMetaDatas.values()) {

				// Write the signature
				source.print("  public ");
				compiler.writeMethodSignature(source, metaData.getMethod());
				source.println(" {");

				// Provide implementation
				source.print("    this.context.doFlow(" + metaData.getFlowIndex() + ", ");
				int parameterIndex = 0;
				source.print(metaData.isParameter() ? "p" + (parameterIndex++) : "null");
				source.print(", ");
				source.print(metaData.isFlowCallback() ? "p" + (parameterIndex++) : "null");
				source.println(");");

				// Complete method
				source.println("  }\n");
			}

			// Complete the class
			source.println("}");
			source.flush();

			// Use the compiled class
			Class<?> clazz = compiler.addSource(className, sourceBuffer.toString()).compile();
			Constructor<?> constructor = clazz.getConstructor(FunctionFlowContext.class);
			this.flowFactory = (context) -> constructor.newInstance(context);
		}
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
	 * @param context {@link FunctionFlowContext}.
	 * @return Parameter.
	 * @throws Exception If fails to create the parameter.
	 */
	public Object createParameter(FunctionFlowContext<?> context) throws Exception {
		return this.flowFactory.createFlows(context);
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
		 * @param functionFlowContext {@link FunctionFlowContext}.
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