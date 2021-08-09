/*-
 * #%L
 * OfficeFrame
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

package net.officefloor.frame.test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import net.officefloor.frame.api.administration.Administration;
import net.officefloor.frame.api.build.DependencyMappingBuilder;
import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.build.ManagedFunctionBuilder;
import net.officefloor.frame.api.build.OfficeBuilder;
import net.officefloor.frame.api.function.AsynchronousFlow;
import net.officefloor.frame.api.function.FlowCallback;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.function.ManagedFunctionContext;
import net.officefloor.frame.api.function.StaticManagedFunction;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.managedobject.AsynchronousContext;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.frame.internal.structure.ThreadState;

/**
 * Reflective {@link ManagedFunctionBuilder}.
 * 
 * @author Daniel Sagenschneider
 */
public class ReflectiveFunctionBuilder extends StaticManagedFunction<Indexed, Indexed> {

	/**
	 * {@link ConstructTestSupport}.
	 */
	private final ConstructTestSupport constructTestSupport;

	/**
	 * Object should the method not be <code>static</code>.
	 */
	private final Object object;

	/**
	 * {@link Method} to invoke.
	 */
	private final Method method;

	/**
	 * Types for the parameters of the {@link Method}.
	 */
	private final Class<?>[] parameterTypes;

	/**
	 * {@link ManagedFunctionBuilder}.
	 */
	private final ManagedFunctionBuilder<Indexed, Indexed> functionBuilder;

	/**
	 * {@link ParameterFactory} instances for the method.
	 */
	private final ParameterFactory[] parameterFactories;

	/**
	 * Next index to specify the {@link ParameterFactory}.
	 */
	private int parameterIndex = 0;

	/**
	 * Next index to specify object.
	 */
	private int objectIndex = 0;

	/**
	 * Next index to specify flow.
	 */
	private int flowIndex = 0;

	/**
	 * Initiate.
	 *
	 * @param <C>                  {@link ManagedFunction} {@link Method} containing
	 *                             {@link Class} type.
	 * @param clazz                {@link Class}.
	 * @param object               Object should the method not be
	 *                             <code>static</code>. May be <code>null</code> if
	 *                             <code>static</code> {@link Method} of the
	 *                             {@link Class}.
	 * @param methodName           Name of the {@link Method} to invoke.
	 * @param officeBuilder        {@link OfficeBuilder}.
	 * @param constructTestSupport {@link ConstructTestSupport}.
	 */
	public <C> ReflectiveFunctionBuilder(Class<C> clazz, C object, String methodName, OfficeBuilder officeBuilder,
			ConstructTestSupport constructTestSupport) {
		this.object = object;
		this.constructTestSupport = constructTestSupport;

		// Obtain the method
		Method functionMethod = null;
		for (Method method : clazz.getMethods()) {
			if (method.getName().equals(methodName)) {
				functionMethod = method;
			}
		}
		if (functionMethod == null) {
			Assertions.fail("No method '" + methodName + "' on class " + clazz.getName());
		}
		this.method = functionMethod;

		// Add the function
		this.functionBuilder = officeBuilder.addManagedFunction(methodName, this);

		// Create the parameter factories for the method
		this.parameterTypes = this.method.getParameterTypes();
		this.parameterFactories = new ParameterFactory[this.parameterTypes.length];
	}

	/**
	 * Obtains the {@link ManagedFunctionBuilder}.
	 * 
	 * @return {@link ManagedFunctionBuilder}.
	 */
	public ManagedFunctionBuilder<Indexed, Indexed> getBuilder() {
		return this.functionBuilder;
	}

	/**
	 * Builds the {@link ManagedFunctionContext}.
	 */
	public void buildManagedFunctionContext() {

		// Ensure parameter is ManagedFunctionContext
		Class<?> parameterType = this.parameterTypes[this.parameterIndex];
		Assertions.assertTrue(ManagedFunctionContext.class.isAssignableFrom(parameterType),
				"Parameter " + this.parameterIndex + " must be " + ManagedFunctionContext.class.getSimpleName());

		// Link ManagedFunctionContext
		this.parameterFactories[this.parameterIndex] = new ManagedFunctionContextParameterFactory();

		// Set for next parameter
		this.parameterIndex++;
	}

	/**
	 * Builds the parameter.
	 */
	public void buildParameter() {

		// Obtain the type of the parameter
		Class<?> parameterType = this.parameterTypes[this.parameterIndex];

		// Link parameter and setup to return
		this.functionBuilder.linkParameter(this.objectIndex, parameterType);
		this.parameterFactories[this.parameterIndex] = new ObjectParameterFactory(this.objectIndex);

		// Set for next object and parameter
		this.objectIndex++;
		this.parameterIndex++;
	}

	/**
	 * Builds {@link AsynchronousFlow}.
	 */
	public void buildAsynchronousFlow() {

		// Ensure parameter is AsynchronousFlow
		Class<?> parameterType = this.parameterTypes[this.parameterIndex];
		Assertions.assertTrue(AsynchronousFlow.class.isAssignableFrom(parameterType),
				"Parameter " + this.parameterIndex + " must be " + AsynchronousFlow.class.getSimpleName());

		// Link AsynchronousFlow
		this.parameterFactories[this.parameterIndex] = new AsynchronousFlowParameterFactory();

		// Set for next parameter
		this.parameterIndex++;
	}

	/**
	 * Links the {@link ManagedObject} to the {@link ManagedFunction}.
	 * 
	 * @param scopeManagedObjectName Scope name of the {@link ManagedObject}.
	 */
	public void buildObject(String scopeManagedObjectName) {

		// Obtain the type of the object
		Class<?> objectType = this.parameterTypes[this.parameterIndex];

		// Link to task and setup to return
		this.functionBuilder.linkManagedObject(this.objectIndex, scopeManagedObjectName, objectType);
		this.parameterFactories[this.parameterIndex] = new ObjectParameterFactory(this.objectIndex);

		// Set for next object and parameter
		this.objectIndex++;
		this.parameterIndex++;
	}

	/**
	 * Builds the {@link ManagedObjectScope} bound {@link ManagedObject}.
	 * 
	 * @param officeManagedObjectName {@link Office} name of the
	 *                                {@link ManagedObject}.
	 * @param managedObjectScope      {@link ManagedObjectScope} for the
	 *                                {@link ManagedObject}.
	 * @return {@link DependencyMappingBuilder}.
	 */
	public DependencyMappingBuilder buildObject(String officeManagedObjectName, ManagedObjectScope managedObjectScope) {

		// Build the managed object based on scope
		DependencyMappingBuilder mappingBuilder = this.constructTestSupport.bindManagedObject(officeManagedObjectName,
				managedObjectScope, this.functionBuilder);

		// Link to object to function
		this.buildObject(officeManagedObjectName);

		// Return the dependency mapping builder
		return mappingBuilder;
	}

	/**
	 * Builds the flow.
	 * 
	 * @param functionName       {@link ManagedFunction} name.
	 * @param argumentType       Type of argument passed to the {@link Flow}.
	 * @param isSpawnThreadState <code>true</code> to spawn in {@link ThreadState}.
	 */
	public void buildFlow(String functionName, Class<?> argumentType, boolean isSpawnThreadState) {

		// Link in the flow and allow for invocation
		this.functionBuilder.linkFlow(this.flowIndex, functionName, argumentType, isSpawnThreadState);
		this.parameterFactories[this.parameterIndex] = new ReflectiveFlowParameterFactory(this.flowIndex);

		// Set for next flow and parameter
		this.flowIndex++;
		this.parameterIndex++;
	}

	/**
	 * Specifies the next {@link ManagedFunction} using the return type of the
	 * {@link Method} as the argument type.
	 * 
	 * @param functionName {@link ManagedFunction} name.
	 */
	public void setNextFunction(String functionName) {

		// Obtain the method return type
		Class<?> returnType = this.method.getReturnType();
		if (returnType == Void.class) {
			returnType = null; // null if no argument type
		}

		// Specify the next function
		this.functionBuilder.setNextFunction(functionName, returnType);
	}

	/**
	 * Creates pre {@link Administration}.
	 * 
	 * @param methodName Name of {@link Method} for {@link Administration}.
	 * @return {@link ReflectiveAdministrationBuilder}.
	 */
	public ReflectiveAdministrationBuilder preAdminister(String methodName) {
		return this.addAdminster(methodName, true);
	}

	/**
	 * Creates post {@link Administration}.
	 * 
	 * @param methodName Name of {@link Method} for {@link Administration}.
	 * @return {@link ReflectiveAdministrationBuilder}.
	 */
	public ReflectiveAdministrationBuilder postAdminister(String methodName) {
		return this.addAdminster(methodName, false);
	}

	/**
	 * Adds {@link Administration}.
	 * 
	 * @param methodName   Name of the {@link Method}.
	 * @param isPreNotPost <code>true</code> for pre (otherwise <code>false</code>
	 *                     for post).
	 * @return {@link ReflectiveAdministrationBuilder}.
	 */
	private ReflectiveAdministrationBuilder addAdminster(String methodName, boolean isPreNotPost) {
		@SuppressWarnings({ "unchecked", "rawtypes" })
		ReflectiveAdministrationBuilder builder = new ReflectiveAdministrationBuilder((Class) this.object.getClass(),
				this.object, methodName, isPreNotPost, this.functionBuilder, this.constructTestSupport);
		return builder;
	}

	/*
	 * =========================== ManagedFunction ===========================
	 */

	@Override
	public void execute(ManagedFunctionContext<Indexed, Indexed> context) throws Throwable {

		// Create the parameters
		Object[] parameters = new Object[this.method.getParameterTypes().length];
		for (int i = 0; i < parameters.length; i++) {
			parameters[i] = this.parameterFactories[i].createParamater(context);
		}

		// Record invoking method
		this.constructTestSupport.recordReflectiveFunctionMethodInvoked(this.method.getName());

		// Invoke the method on object to get return
		try {
			Object result = this.method.invoke(this.object, parameters);
			if (result != null) {
				context.setNextFunctionArgument(result);
			}
		} catch (InvocationTargetException ex) {
			// Throw cause of exception
			throw ex.getCause();
		}
	}

	/**
	 * Interface for a factory to create the parameter from the
	 * {@link ManagedFunctionContext}.
	 */
	private static interface ParameterFactory {
		Object createParamater(ManagedFunctionContext<Indexed, Indexed> context);
	}

	/**
	 * {@link ParameterFactory} to obtain the {@link ManagedFunctionContext}.
	 */
	private static class ManagedFunctionContextParameterFactory implements ParameterFactory {

		@Override
		public Object createParamater(ManagedFunctionContext<Indexed, Indexed> context) {
			return context;
		}
	}

	/**
	 * {@link ParameterFactory} to obtain the {@link AsynchronousContext}.
	 */
	private static class AsynchronousFlowParameterFactory implements ParameterFactory {

		@Override
		public Object createParamater(ManagedFunctionContext<Indexed, Indexed> context) {
			return context.createAsynchronousFlow();
		}
	}

	/**
	 * {@link ParameterFactory} to obtain a dependency.
	 */
	private static class ObjectParameterFactory implements ParameterFactory {

		/**
		 * Index of the object.
		 */
		private final int index;

		/**
		 * Initiate.
		 * 
		 * @param index Index of the object.
		 */
		public ObjectParameterFactory(int index) {
			this.index = index;
		}

		@Override
		public Object createParamater(ManagedFunctionContext<Indexed, Indexed> context) {
			// Return the managed object
			return context.getObject(index);
		}
	}

	/**
	 * {@link ParameterFactory} to obtain the flow.
	 */
	private class ReflectiveFlowParameterFactory implements ParameterFactory {

		/**
		 * Index of the flow.
		 */
		private final int index;

		/**
		 * Initiate.
		 * 
		 * @param index
		 * 
		 */
		public ReflectiveFlowParameterFactory(int index) {
			this.index = index;
		}

		@Override
		public Object createParamater(final ManagedFunctionContext<Indexed, Indexed> context) {
			return new ReflectiveFlow() {
				@Override
				public void doFlow(Object parameter, FlowCallback callback) {
					context.doFlow(ReflectiveFlowParameterFactory.this.index, parameter, callback);
				}
			};
		}
	}

}
