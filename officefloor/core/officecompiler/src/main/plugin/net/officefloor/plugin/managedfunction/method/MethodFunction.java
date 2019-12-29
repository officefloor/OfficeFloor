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
package net.officefloor.plugin.managedfunction.method;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.logging.Logger;

import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.function.AsynchronousFlow;
import net.officefloor.frame.api.function.FlowCallback;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.function.ManagedFunctionContext;
import net.officefloor.frame.api.manage.InvalidParameterTypeException;
import net.officefloor.frame.api.manage.UnknownFunctionException;

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
	 * {@link MethodObjectInstanceFactory}. Will be <code>null</code> if static
	 * {@link Method}.
	 */
	private final MethodObjectInstanceFactory methodObjectInstanceFactory;

	/**
	 * Method to invoke for this {@link ManagedFunction}.
	 */
	private final Method method;

	/**
	 * {@link MethodParameterFactory} instances.
	 */
	private final MethodParameterFactory[] parameterFactories;

	/**
	 * {@link MethodReturnTranslator} or <code>null</code>.
	 */
	private final MethodReturnTranslator<Object, Object> returnTranslator;

	/**
	 * Initiate.
	 * 
	 * @param methodObjectInstanceFactory {@link MethodObjectInstanceFactory}. Will
	 *                                    be <code>null</code> if static
	 *                                    {@link Method}.
	 * @param method                      Method to invoke for this
	 *                                    {@link ManagedFunction}.
	 * @param parameterFactories          {@link MethodParameterFactory} instances.
	 * @param returnTranslator            {@link MethodReturnTranslator} or
	 *                                    <code>null</code>.
	 */
	public MethodFunction(MethodObjectInstanceFactory methodObjectInstanceFactory, Method method,
			MethodParameterFactory[] parameterFactories, MethodReturnTranslator<Object, Object> returnTranslator) {
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
			params[i] = this.parameterFactories[i].createParameter(runContext);
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