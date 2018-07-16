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
package net.officefloor.plugin.managedobject.clazz;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;

import net.officefloor.compile.impl.compile.OfficeFloorJavaCompiler;
import net.officefloor.compile.impl.compile.OfficeFloorJavaCompiler.ClassName;
import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.function.FlowCallback;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.source.ManagedObjectExecuteContext;
import net.officefloor.frame.internal.structure.ProcessState;

/**
 * Meta-data for a process interface.
 * 
 * @author Daniel Sagenschneider
 */
public class ProcessMetaData {

	/**
	 * {@link FunctionalInterface} to create the flows object.
	 */
	@FunctionalInterface
	private static interface FlowFactory {

		/**
		 * Creates the flows object.
		 * 
		 * @param managedObject {@link ManagedObject}.
		 * @return Flows object.
		 * @throws Exception If fails to create the flows object.
		 */
		Object createFlows(ManagedObject managedObject) throws Exception;
	}

	/**
	 * {@link Field} to receive the injected process interface.
	 */
	public final Field field;

	/**
	 * {@link ProcessMetaData} for the {@link Method} instances of the process
	 * interface.
	 */
	private final Map<String, ProcessMethodMetaData> methodMetaData;

	/**
	 * {@link FlowFactory}.
	 */
	private final FlowFactory flowFactory;

	/**
	 * {@link ManagedObjectExecuteContext}.
	 */
	private final ManagedObjectExecuteContext<Indexed> executeContext;

	/**
	 * Initiate.
	 * 
	 * @param field          {@link Field} receiving the injected process interface.
	 * @param methodMetaData {@link ProcessMetaData} for the {@link Method}
	 *                       instances of the process interface.
	 * @param classLoader    {@link ClassLoader}.
	 * @param executeContext {@link ManagedObjectExecuteContext}.
	 * @throws Exception If fails to create the proxy for the process interface.
	 */
	public ProcessMetaData(Field field, Map<String, ProcessMethodMetaData> methodMetaData, ClassLoader classLoader,
			ManagedObjectExecuteContext<Indexed> executeContext) throws Exception {
		this.field = field;
		this.methodMetaData = methodMetaData;
		this.executeContext = executeContext;

		// Determine if the compiler is available
		OfficeFloorJavaCompiler compiler = OfficeFloorJavaCompiler.newInstance(classLoader);
		if (compiler == null) {

			// Compiler not available so fallback to proxy
			Class<?>[] interfaces = new Class[] { field.getType() };
			this.flowFactory = (managedObject) -> Proxy.newProxyInstance(classLoader, interfaces,
					new ProcessInvocationHandler(managedObject));
		} else {
			// Create compiled implementation (for performance and reduced GC pressure)
			StringWriter sourceBuffer = new StringWriter();
			PrintWriter source = new PrintWriter(sourceBuffer);

			// Obtain the flow interface type
			Class<?> flowInterface = this.field.getType();

			// Create the class name
			ClassName className = compiler.createClassName(flowInterface.getName());

			// Write class definition
			source.println("package " + className.getPackageName() + ";");
			source.println("public class " + className.getClassName() + " implements "
					+ compiler.getSourceName(flowInterface) + "{");

			// Write the constructor
			compiler.writeConstructor(source, className.getClassName(),
					compiler.createField(ManagedObjectExecuteContext.class, "context"),
					compiler.createField(ManagedObject.class, "managedObject"));

			// Write the flow methods
			for (Method method : flowInterface.getMethods()) {

				// Obtain the meta-data for the method
				ProcessMethodMetaData metaData = methodMetaData.get(method.getName());

				// Write the signature
				source.print("  public ");
				compiler.writeMethodSignature(source, method);
				source.println(" {");

				// Provide implementation
				source.print("    this.context.invokeProcess(" + metaData.getProcessIndex() + ", ");
				int parameterIndex = 0;
				source.print(metaData.isParameter() ? "p" + (parameterIndex++) : "null");
				source.print(", this.managedObject, 0, ");
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
			Constructor<?> constructor = clazz.getConstructor(ManagedObjectExecuteContext.class, ManagedObject.class);
			this.flowFactory = (managedObject) -> constructor.newInstance(this.executeContext, managedObject);
		}
	}

	/**
	 * Creates the implementation of the process interface field type for the
	 * {@link ManagedObject} to be injected into the object.
	 * 
	 * @param managedObject {@link ManagedObject} for the invoked
	 *                      {@link ProcessState}.
	 * @return Implementation to be injected into the object.
	 * @throws Exception If fails to instantiate the process interface
	 *                   implementation.
	 */
	public Object createProcessInterfaceImplementation(ManagedObject managedObject) throws Exception {
		return this.flowFactory.createFlows(managedObject);
	}

	/**
	 * {@link InvocationHandler} for the process interface implementation.
	 */
	private class ProcessInvocationHandler implements InvocationHandler {

		/**
		 * {@link ManagedObject}.
		 */
		private final ManagedObject managedObject;

		/**
		 * Initiate.
		 * 
		 * @param managedObject {@link ManagedObject} for invoked {@link ProcessState}.
		 */
		public ProcessInvocationHandler(ManagedObject managedObject) {
			this.managedObject = managedObject;
		}

		/*
		 * ============= InvocationHandler =================================
		 */

		@Override
		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

			// Obtain the meta-data for the method
			ProcessMethodMetaData metaData = ProcessMetaData.this.methodMetaData.get(method.getName());

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

			// Invoke the process
			ProcessMetaData.this.executeContext.invokeProcess(metaData.getProcessIndex(), parameter, this.managedObject,
					0, flowCallback);

			// Never returns a value
			return null;
		}
	}

}