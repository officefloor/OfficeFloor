/*-
 * #%L
 * OfficeCompiler
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

package net.officefloor.plugin.clazz.method;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.Executor;
import java.util.logging.Logger;

import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.function.AsynchronousFlow;
import net.officefloor.frame.api.function.FlowCallback;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.function.ManagedFunctionContext;
import net.officefloor.frame.api.manage.InvalidParameterTypeException;
import net.officefloor.frame.api.manage.UnknownFunctionException;
import net.officefloor.plugin.clazz.dependency.ClassDependencyFactory;

/**
 * {@link ManagedFunction} to invoke a {@link Method}.
 * 
 * @author Daniel Sagenschneider
 */
public class MethodFunction implements ManagedFunction<Indexed, Indexed> {

	/**
	 * Invokes the {@link Method} as the {@link ManagedFunction} directly on the
	 * {@link Object}.
	 * 
	 * @param instance   Instance. May be <code>null</code> if static
	 *                   {@link Method}.
	 * @param method     {@link Method}.
	 * @param parameters Parameters.
	 * @return {@link Method} return value.
	 * @throws Throwable Failure invoking the {@link Method}.
	 */
	public static Object invokeMethod(Object instance, Method method, Object[] parameters) throws Throwable {

		// Invoke the function
		try {
			return method.invoke(instance, parameters);
		} catch (InvocationTargetException ex) {
			// Propagate failure of function
			throw ex.getCause();
		} catch (IllegalArgumentException ex) {

			// Provide detail of illegal argument
			StringBuilder message = new StringBuilder();
			message.append("Function failure invoking ");
			message.append(method.getName());
			message.append("(");
			boolean isFirst = true;
			for (Class<?> parameterType : method.getParameterTypes()) {
				if (isFirst) {
					isFirst = false;
				} else {
					message.append(", ");
				}
				message.append(parameterType.getName());
			}
			message.append(") with arguments ");
			isFirst = true;
			for (Object parameter : parameters) {
				if (isFirst) {
					isFirst = false;
				} else {
					message.append(", ");
				}
				message.append(parameter == null ? "null" : parameter.getClass().getName());
			}

			// Propagate illegal argument issue
			throw new IllegalArgumentException(message.toString());
		}
	}

	/**
	 * {@link MethodObjectFactory}. Will be <code>null</code> if static
	 * {@link Method}.
	 */
	private final MethodObjectFactory methodObjectInstanceFactory;

	/**
	 * Method to invoke for this {@link ManagedFunction}.
	 */
	private final Method method;

	/**
	 * {@link ClassDependencyFactory} instances.
	 */
	private final ClassDependencyFactory[] parameterFactories;

	/**
	 * {@link MethodReturnTranslator} or <code>null</code>.
	 */
	private final MethodReturnTranslator<Object, Object> returnTranslator;

	/**
	 * Initiate.
	 * 
	 * @param methodObjectInstanceFactory {@link MethodObjectFactory}. Will
	 *                                    be <code>null</code> if static
	 *                                    {@link Method}.
	 * @param method                      Method to invoke for this
	 *                                    {@link ManagedFunction}.
	 * @param parameterFactories          {@link ClassDependencyFactory} instances.
	 * @param returnTranslator            {@link MethodReturnTranslator} or
	 *                                    <code>null</code>.
	 */
	public MethodFunction(MethodObjectFactory methodObjectInstanceFactory, Method method,
			ClassDependencyFactory[] parameterFactories, MethodReturnTranslator<Object, Object> returnTranslator) {
		this.method = method;
		this.methodObjectInstanceFactory = methodObjectInstanceFactory;
		this.parameterFactories = parameterFactories;
		this.returnTranslator = returnTranslator;
	}

	/**
	 * Returns the {@link Method} for the {@link ManagedFunction}.
	 * 
	 * @return {@link Method} for the {@link ManagedFunction}.
	 */
	public Method getMethod() {
		return this.method;
	}

	/*
	 * ========================= ManagedFunction =========================
	 */

	@Override
	public void execute(ManagedFunctionContext<Indexed, Indexed> context) throws Throwable {

		// Obtain the instance to invoke the method on (null if static method)
		Object instance = (this.methodObjectInstanceFactory == null) ? null
				: this.methodObjectInstanceFactory.createInstance(context);

		// May inject context, so need to wrap translating next value
		ManagedFunctionContext<Indexed, Indexed> runContext = (this.returnTranslator != null)
				? new TranslateManagedFunctionContext(context)
				: context;

		// Create the listing of parameters
		Object[] params = new Object[this.parameterFactories.length];
		for (int i = 0; i < params.length; i++) {
			params[i] = this.parameterFactories[i].createDependency(runContext);
		}

		// Invoke the method as the function
		Object returnValue = invokeMethod(instance, this.method, params);

		// Determine if translate return value
		if (returnValue != null) {
			runContext.setNextFunctionArgument(returnValue);
		}
	}

	/**
	 * {@link ManagedFunctionContext} to translate the return value.
	 */
	private class TranslateManagedFunctionContext implements ManagedFunctionContext<Indexed, Indexed> {

		/**
		 * {@link ManagedFunctionContext} delegate.
		 */
		private final ManagedFunctionContext<Indexed, Indexed> delegate;

		/**
		 * Instantiate.
		 * 
		 * @param delegate {@link ManagedFunctionContext} delegate.
		 */
		private TranslateManagedFunctionContext(ManagedFunctionContext<Indexed, Indexed> delegate) {
			this.delegate = delegate;
		}

		/*
		 * ======================= ManagedFunctionContext ============================
		 */

		@Override
		public Logger getLogger() {
			return this.delegate.getLogger();
		}

		@Override
		public void doFlow(Indexed key, Object parameter, FlowCallback callback) {
			this.delegate.doFlow(key, parameter, callback);
		}

		@Override
		public void doFlow(int flowIndex, Object parameter, FlowCallback callback) {
			this.delegate.doFlow(flowIndex, parameter, callback);
		}

		@Override
		public AsynchronousFlow createAsynchronousFlow() {
			return this.delegate.createAsynchronousFlow();
		}

		@Override
		public Executor getExecutor() {
			return this.delegate.getExecutor();
		}

		@Override
		public Object getObject(Indexed key) {
			return this.delegate.getObject(key);
		}

		@Override
		public Object getObject(int dependencyIndex) {
			return this.delegate.getObject(dependencyIndex);
		}

		@Override
		public void doFlow(String functionName, Object parameter, FlowCallback callback)
				throws UnknownFunctionException, InvalidParameterTypeException {
			this.delegate.doFlow(functionName, parameter, callback);
		}

		@Override
		public void setNextFunctionArgument(Object argument) throws Exception {

			// Translate the return value
			MethodFunction.this.returnTranslator.translate(new MethodReturnTranslatorContext<Object, Object>() {

				@Override
				public Object getReturnValue() {
					return argument;
				}

				@Override
				public void setTranslatedReturnValue(Object value) throws Exception {
					TranslateManagedFunctionContext.this.delegate.setNextFunctionArgument(value);
				}

				@Override
				public ManagedFunctionContext<?, ?> getManagedFunctionContext() {
					return TranslateManagedFunctionContext.this.delegate;
				}
			});
		}
	}

}
