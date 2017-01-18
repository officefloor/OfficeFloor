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
package net.officefloor.frame.test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import junit.framework.TestCase;
import net.officefloor.frame.api.build.DependencyMappingBuilder;
import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.build.ManagedFunctionBuilder;
import net.officefloor.frame.api.build.OfficeBuilder;
import net.officefloor.frame.api.function.FlowCallback;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.function.ManagedFunctionContext;
import net.officefloor.frame.api.function.ManagedFunctionFactory;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.frame.internal.structure.ThreadState;

/**
 * Reflective {@link ManagedFunctionBuilder}.
 * 
 * @author Daniel Sagenschneider
 */
public class ReflectiveFunctionBuilder
		implements ManagedFunctionFactory<Indexed, Indexed>, ManagedFunction<Indexed, Indexed> {

	/**
	 * {@link AbstractOfficeConstructTestCase}.
	 */
	private AbstractOfficeConstructTestCase testCase;

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
	 * {@link OfficeBuilder}.
	 */
	private final OfficeBuilder officeBuilder;

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
	 * @param clazz
	 *            {@link Class}.
	 * @param object
	 *            Object should the method not be <code>static</code>. May be
	 *            <code>null</code> if <code>static</code> {@link Method} of the
	 *            {@link Class}.
	 * @param methodName
	 *            Name of the {@link Method} to invoke.
	 * @param officeBuilder
	 *            {@link OfficeBuilder}.
	 * @param testCase
	 *            {@link AbstractOfficeConstructTestCase}.
	 */
	public <C> ReflectiveFunctionBuilder(Class<C> clazz, C object, String methodName, OfficeBuilder officeBuilder,
			AbstractOfficeConstructTestCase testCase) {
		this.object = object;
		this.officeBuilder = officeBuilder;
		this.testCase = testCase;

		// Obtain the method
		Method functionMethod = null;
		for (Method method : clazz.getMethods()) {
			if (method.getName().equals(methodName)) {
				functionMethod = method;
			}
		}
		if (functionMethod == null) {
			TestCase.fail("No method '" + methodName + "' on class " + clazz.getName());
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
		TestCase.assertTrue(
				"Parameter " + this.parameterIndex + " must be " + ManagedFunctionContext.class.getSimpleName(),
				ManagedFunctionContext.class.isAssignableFrom(parameterType));

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
	 * Links the {@link ManagedObject} to the {@link ManagedFunction}.
	 * 
	 * @param scopeManagedObjectName
	 *            Scope name of the {@link ManagedObject}.
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
	 * @param officeManagedObjectName
	 *            {@link Office} name of the {@link ManagedObject}.
	 * @param managedObjectScope
	 *            {@link ManagedObjectScope} for the {@link ManagedObject}.
	 * @return {@link DependencyMappingBuilder}.
	 */
	public DependencyMappingBuilder buildObject(String officeManagedObjectName, ManagedObjectScope managedObjectScope) {

		// Build the managed object based on scope
		DependencyMappingBuilder mappingBuilder;
		switch (managedObjectScope) {
		case FUNCTION:
			mappingBuilder = this.functionBuilder.addManagedObject(officeManagedObjectName, officeManagedObjectName);
			break;

		case THREAD:
			mappingBuilder = this.officeBuilder.addThreadManagedObject(officeManagedObjectName,
					officeManagedObjectName);
			break;

		case PROCESS:
			mappingBuilder = this.officeBuilder.addProcessManagedObject(officeManagedObjectName,
					officeManagedObjectName);
			break;

		default:
			TestCase.fail("Unknown managed object scope " + managedObjectScope);
			return null;
		}

		// Link to object to function
		this.buildObject(officeManagedObjectName);

		// Return the dependency mapping builder
		return mappingBuilder;
	}

	/**
	 * Builds the flow.
	 * 
	 * @param functionName
	 *            {@link ManagedFunction} name.
	 * @param argumentType
	 *            Type of argument passed to the {@link Flow}.
	 * @param isSpawnThreadState
	 *            <code>true</code> to spawn in {@link ThreadState}.
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
	 * @param functionName
	 *            {@link ManagedFunction} name.
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

	/*
	 * ======================== ManagedFunctionFactory ========================
	 */

	@Override
	public ManagedFunction<Indexed, Indexed> createManagedFunction() {
		return this;
	}

	/*
	 * =========================== ManagedFunction ===========================
	 */

	@Override
	public Object execute(ManagedFunctionContext<Indexed, Indexed> context) throws Throwable {

		// Create the parameters
		Object[] parameters = new Object[this.method.getParameterTypes().length];
		for (int i = 0; i < parameters.length; i++) {
			parameters[i] = this.parameterFactories[i].createParamater(context);
		}

		// Record invoking method
		this.testCase.recordReflectiveFunctionMethodInvoked(this.method.getName());

		// Invoke the method on object to get return
		Object returnValue;
		try {
			returnValue = this.method.invoke(this.object, parameters);
		} catch (InvocationTargetException ex) {
			// Throw cause of exception
			throw ex.getCause();
		}

		// Return the value from method
		return returnValue;
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
		 * @param index
		 *            Index of the object.
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