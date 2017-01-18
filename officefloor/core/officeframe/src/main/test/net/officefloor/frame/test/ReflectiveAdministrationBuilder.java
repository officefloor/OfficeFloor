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

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import org.junit.Assert;

import junit.framework.TestCase;
import net.officefloor.frame.api.administration.Administration;
import net.officefloor.frame.api.administration.AdministrationContext;
import net.officefloor.frame.api.administration.AdministrationFactory;
import net.officefloor.frame.api.build.AdministrationBuilder;
import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.build.ManagedFunctionBuilder;
import net.officefloor.frame.api.function.FlowCallback;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.internal.structure.Flow;

/**
 * Reflective {@link AdministrationBuilder}.
 *
 * @author Daniel Sagenschneider
 */
public class ReflectiveAdministrationBuilder
		implements AdministrationFactory<Object, Indexed, Indexed>, Administration<Object, Indexed, Indexed> {

	/**
	 * {@link AbstractOfficeConstructTestCase}.
	 */
	private final AbstractOfficeConstructTestCase testCase;

	/**
	 * {@link ManagedFunctionBuilder}.
	 */
	private final ManagedFunctionBuilder<?, ?> managedFunctionBuilder;

	/**
	 * Object should the {@link Method} for the {@link AdministrationDuty} not
	 * be static.
	 */
	private final Object object;

	/**
	 * {@link Method} for this {@link Administration}.
	 */
	private final Method method;

	/**
	 * Types for the parameters of the {@link Method}.
	 */
	private final Class<?>[] parameterTypes;

	/**
	 * {@link AdministrationBuilder} for this
	 * {@link ReflectiveAdministrationBuilder}.
	 */
	private final AdministrationBuilder<Indexed, Indexed> administrationBuilder;

	/**
	 * {@link ParameterFactory} instances for the method.
	 */
	private final ParameterFactory[] parameterFactories;

	/**
	 * Next index to specify the {@link ParameterFactory}.
	 */
	private int parameterIndex = 0;

	/**
	 * Next index to specify flow.
	 */
	private int flowIndex = 0;

	/**
	 * Instantiate.
	 * 
	 * @param clazz
	 *            {@link Class} to determine the {@link Method} instances for
	 *            the {@link AdministrationDuty} instances.
	 * @param object
	 *            Optional {@link Object} for non-static methods.
	 * @param methodName
	 *            Name of the {@link Method} to invoke.
	 * @param managedFunctionBuilder
	 *            {@link ManagedFunctionBuilder}.
	 * @param testCase
	 *            {@link AbstractOfficeConstructTestCase}.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public <C> ReflectiveAdministrationBuilder(Class<C> clazz, C object, String methodName, boolean isPreNotPost,
			ManagedFunctionBuilder<?, ?> managedFunctionBuilder, AbstractOfficeConstructTestCase testCase) {
		this.object = object;
		this.managedFunctionBuilder = managedFunctionBuilder;
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

		// Obtain the parameters for the method
		this.parameterTypes = this.method.getParameterTypes();

		// The first parameter is always the extension array
		Assert.assertTrue("Should have at least one parameter being the extension array",
				this.parameterTypes.length >= 1);
		Class<?> extensionArrayType = this.parameterTypes[0];
		Assert.assertTrue("First parameter should be extension array", extensionArrayType.isArray());
		Class extensionInterface = extensionArrayType.getComponentType();

		// Load the parameter factory for the extensions
		this.parameterFactories = new ParameterFactory[this.parameterTypes.length];
		this.parameterFactories[this.parameterIndex++] = new ExtensionsParameterFactory();

		// Construct this administration
		if (isPreNotPost) {
			this.administrationBuilder = this.managedFunctionBuilder.preAdminister(methodName, extensionInterface,
					this);
		} else {
			this.administrationBuilder = this.managedFunctionBuilder.postAdminister(methodName, extensionInterface,
					this);
		}
	}

	/**
	 * Builds the flow.
	 * 
	 * @param functionName
	 *            {@link ManagedFunction} name.
	 * @param argumentType
	 *            Type of argument passed to the {@link Flow}.
	 */
	public void buildFlow(String functionName, Class<?> argumentType) {

		// Link in the flow and allow for invocation
		this.administrationBuilder.linkFlow(this.flowIndex, functionName, argumentType);
		this.parameterFactories[this.parameterIndex] = new ReflectiveFlowParameterFactory(this.flowIndex);

		// Set for next flow and parameter
		this.flowIndex++;
		this.parameterIndex++;
	}

	/*
	 * ======================== AdmnistrationFactory =========================
	 */

	@Override
	public Administration<Object, Indexed, Indexed> createAdministration() throws Throwable {
		return this;
	}

	/*
	 * =========================== Administration =============================
	 */

	@Override
	public void administer(AdministrationContext<Object, Indexed, Indexed> context) throws Throwable {

		// Create the parameters
		Object[] parameters = new Object[this.method.getParameterTypes().length];
		for (int i = 0; i < parameters.length; i++) {
			parameters[i] = this.parameterFactories[i].createParamater(context);
		}

		// Record invoking method
		this.testCase.recordReflectiveFunctionMethodInvoked(this.method.getName());

		// Invoke the method on object
		try {
			this.method.invoke(this.object, parameters);
		} catch (InvocationTargetException ex) {
			// Throw cause of exception
			throw ex.getCause();
		}
	}

	/**
	 * Interface for a factory to create the parameter from the
	 * {@link AdministrationContext}.
	 */
	private interface ParameterFactory {
		Object createParamater(AdministrationContext<Object, Indexed, Indexed> context);
	}

	/**
	 * {@link ParameterFactory} to obtain the extenion interfaces.
	 */
	private class ExtensionsParameterFactory implements ParameterFactory {
		public Object createParamater(AdministrationContext<Object, Indexed, Indexed> context) {
			List<?> extensionInterfaces = context.getExtensionInterfaces();
			Object[] array = (Object[]) Array.newInstance(
					ReflectiveAdministrationBuilder.this.parameterTypes[0].getComponentType(),
					extensionInterfaces.size());
			return extensionInterfaces.toArray(array);
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
		public Object createParamater(AdministrationContext<Object, Indexed, Indexed> context) {
			return new ReflectiveFlow() {
				@Override
				public void doFlow(Object parameter, FlowCallback callback) {
					context.doFlow(ReflectiveFlowParameterFactory.this.index, parameter, callback);
				}
			};
		}
	}

}