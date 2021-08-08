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
import net.officefloor.frame.api.administration.AdministrationContext;
import net.officefloor.frame.api.administration.AdministrationFactory;
import net.officefloor.frame.api.administration.GovernanceManager;
import net.officefloor.frame.api.build.AdministrationBuilder;
import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.build.ManagedFunctionBuilder;
import net.officefloor.frame.api.function.AsynchronousFlow;
import net.officefloor.frame.api.function.FlowCallback;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.governance.Governance;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.internal.structure.FlowMetaData;
import net.officefloor.frame.internal.structure.ThreadState;

/**
 * Reflective {@link AdministrationBuilder}.
 *
 * @author Daniel Sagenschneider
 */
public class ReflectiveAdministrationBuilder {

	/**
	 * {@link ConstructTestSupport}.
	 */
	private final ConstructTestSupport constructTestSupport;

	/**
	 * {@link ManagedFunctionBuilder}.
	 */
	private final ManagedFunctionBuilder<?, ?> managedFunctionBuilder;

	/**
	 * Object should the {@link Method} for the {@link AdministrationDuty} not be
	 * static.
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
	 * Next index to specify {@link Flow}.
	 */
	private int flowIndex = 0;

	/**
	 * Next index to specify {@link Governance}.
	 */
	private int governanceIndex = 0;

	/**
	 * Instantiate.
	 *
	 * @param <C>                    {@link Administration} {@link Class} type.
	 * @param clazz                  {@link Class} to determine the {@link Method}
	 *                               instances for the {@link Administration}
	 *                               instances.
	 * @param object                 Optional {@link Object} for non-static methods.
	 * @param methodName             Name of the {@link Method} to invoke.
	 * @param isPreNotPost           Indicates if pre-administration (otherwise
	 *                               post-administration).
	 * @param managedFunctionBuilder {@link ManagedFunctionBuilder}.
	 * @param constructTestSupport   {@link ConstructTestSupport}.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public <C> ReflectiveAdministrationBuilder(Class<C> clazz, C object, String methodName, boolean isPreNotPost,
			ManagedFunctionBuilder<?, ?> managedFunctionBuilder, ConstructTestSupport constructTestSupport) {
		this.object = object;
		this.managedFunctionBuilder = managedFunctionBuilder;
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

		// Obtain the parameters for the method
		this.parameterTypes = this.method.getParameterTypes();

		// The first parameter is always the extension array
		Assertions.assertTrue(this.parameterTypes.length >= 1,
				"Should have at least one parameter being the extension array");
		Class<?> extensionArrayType = this.parameterTypes[0];
		Assertions.assertTrue(extensionArrayType.isArray(), "First parameter should be extension array");
		Class extensionInterface = extensionArrayType.getComponentType();

		// Load the parameter factory for the extensions
		this.parameterFactories = new ParameterFactory[this.parameterTypes.length];
		this.parameterFactories[this.parameterIndex++] = new ExtensionsParameterFactory();

		// Construct this administration
		if (isPreNotPost) {
			this.administrationBuilder = this.managedFunctionBuilder.preAdminister(methodName, extensionInterface,
					new ReflectiveAdministration());
		} else {
			this.administrationBuilder = this.managedFunctionBuilder.postAdminister(methodName, extensionInterface,
					new ReflectiveAdministration());
		}
	}

	/**
	 * Obtains the {@link AdministrationBuilder}.
	 * 
	 * @return {@link AdministrationBuilder}.
	 */
	public AdministrationBuilder<Indexed, Indexed> getBuilder() {
		return this.administrationBuilder;
	}

	/**
	 * Builds the {@link Administration} of the {@link ManagedObject}.
	 * 
	 * @param managedObjectName Name of the {@link ManagedObject} to administer.
	 */
	public void administerManagedObject(String managedObjectName) {
		this.administrationBuilder.administerManagedObject(managedObjectName);
	}

	/**
	 * Builds the {@link AdministrationContext} as parameter.
	 */
	public void buildAdministrationContext() {

		// Link the administration context
		this.parameterFactories[this.parameterIndex] = (context) -> context;

		// Set for next parameter
		this.parameterIndex++;
	}

	/**
	 * Builds the {@link Flow}.
	 * 
	 * @param functionName       {@link ManagedFunction} name.
	 * @param argumentType       Type of argument passed to the {@link Flow}.
	 * @param isSpawnThreadState Indicates whether to spawn a {@link ThreadState}.
	 */
	public void buildFlow(String functionName, Class<?> argumentType, boolean isSpawnThreadState) {

		// Link in the flow and allow for invocation
		this.administrationBuilder.linkFlow(this.flowIndex, functionName, argumentType, isSpawnThreadState);
		this.parameterFactories[this.parameterIndex] = new ReflectiveFlowParameterFactory(this.flowIndex);

		// Set for next flow and parameter
		this.flowIndex++;
		this.parameterIndex++;
	}

	/**
	 * Builds the {@link Governance}.
	 * 
	 * @param governanceName Name of the {@link Governance}.
	 */
	public void buildGovernance(String governanceName) {

		// Link in the governance and allow for invocation
		this.administrationBuilder.linkGovernance(this.governanceIndex, governanceName);
		this.parameterFactories[this.parameterIndex] = new ReflectiveGovernanceParameterFactory(this.governanceIndex);

		// Set for next governance and parameter
		this.governanceIndex++;
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
	 * {@link Administration}.
	 */
	private class ReflectiveAdministration
			implements AdministrationFactory<Object, Indexed, Indexed>, Administration<Object, Indexed, Indexed> {

		/*
		 * =================== AdmnistrationFactory ===================
		 */

		@Override
		public Administration<Object, Indexed, Indexed> createAdministration() throws Throwable {
			return this;
		}

		/*
		 * ====================== Administration ======================
		 */

		@Override
		public void administer(AdministrationContext<Object, Indexed, Indexed> context) throws Throwable {

			// Create the parameters
			Object[] parameters = new Object[ReflectiveAdministrationBuilder.this.method.getParameterTypes().length];
			for (int i = 0; i < parameters.length; i++) {
				parameters[i] = ReflectiveAdministrationBuilder.this.parameterFactories[i].createParamater(context);
			}

			// Record invoking method
			ReflectiveAdministrationBuilder.this.constructTestSupport
					.recordReflectiveFunctionMethodInvoked(ReflectiveAdministrationBuilder.this.method.getName());

			// Invoke the method on object
			try {
				ReflectiveAdministrationBuilder.this.method.invoke(ReflectiveAdministrationBuilder.this.object,
						parameters);
			} catch (InvocationTargetException ex) {
				// Throw cause of exception
				throw ex.getCause();
			}
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
			return context.getExtensions();
		}
	}

	/**
	 * {@link ParameterFactory} to obtain the {@link ReflectiveFlow}.
	 */
	private class ReflectiveFlowParameterFactory implements ParameterFactory {

		/**
		 * Index of the flow.
		 */
		private final int index;

		/**
		 * Initiate.
		 * 
		 * @param index Index of the {@link FlowMetaData}.
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

	/**
	 * {@link ParameterFactory} to obtain {@link GovernanceManager}.
	 */
	private class ReflectiveGovernanceParameterFactory implements ParameterFactory {

		/**
		 * Index of the flow.
		 */
		private final int index;

		/**
		 * Initiate.
		 * 
		 * @param index Index of the {@link Governance}.
		 */
		public ReflectiveGovernanceParameterFactory(int index) {
			this.index = index;
		}

		@Override
		public Object createParamater(AdministrationContext<Object, Indexed, Indexed> context) {
			return context.getGovernance(this.index);
		}
	}

	/**
	 * {@link ParameterFactory} to obtain {@link AsynchronousFlow}.
	 */
	private class AsynchronousFlowParameterFactory implements ParameterFactory {

		@Override
		public Object createParamater(AdministrationContext<Object, Indexed, Indexed> context) {
			return context.createAsynchronousFlow();
		}
	}

}
