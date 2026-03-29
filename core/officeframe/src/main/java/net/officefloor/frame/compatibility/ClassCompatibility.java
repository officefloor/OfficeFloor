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

package net.officefloor.frame.compatibility;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * Enables {@link Class} compatibility between Java versions by reflection (so
 * can compile in different Java versions).
 *
 * @author Daniel Sagenschneider
 */
public class ClassCompatibility {

	/**
	 * Error prefix.
	 */
	private static final String ERROR_PREFIX = "Version compatibility issue. ";

	/**
	 * Loads an existing {@link Object}.
	 * 
	 * @param object {@link Object}.
	 * @return {@link ObjectCompatibility} wrapping the {@link Object}.
	 */
	public static ObjectCompatibility object(Object object) {
		Class<?> clazz = object.getClass();
		return new ClassCompatibility(clazz.getName(), clazz.getClassLoader()).wrap(object);
	}

	/**
	 * Loads the {@link Class}.
	 * 
	 * @param className   Name of the {@link Class}.
	 * @param classLoader {@link ClassLoader}.
	 * @return Loaded {@link Class}.
	 */
	private static Class<?> loadClass(String className, ClassLoader classLoader) {
		try {
			return classLoader.loadClass(className);
		} catch (ClassNotFoundException e) {
			throw new IllegalStateException("Version compatiblity issue.  Unknown class " + className);
		}
	}

	/**
	 * {@link Class}.
	 */
	private final Class<?> clazz;

	/**
	 * {@link ClassLoader}.
	 */
	private final ClassLoader classLoader;

	/**
	 * Instantiate.
	 * 
	 * @param className   Name of the {@link Class}.
	 * @param classLoader {@link ClassLoader}.
	 */
	public ClassCompatibility(String className, ClassLoader classLoader) {
		this.clazz = loadClass(className, classLoader);
		this.classLoader = classLoader;
	}

	/**
	 * Undertakes {@link Class} static {@link Method}.
	 * 
	 * @param methodName Name of the static {@link Method}.
	 * @param arguments  Arguments for the {@link Method}.
	 * @return {@link ObjectCompatibility}. Will be <code>null</code> for
	 *         <code>void</code>method.
	 */
	public ObjectCompatibility $(String methodName, Object... arguments) {
		return this.invoke(arguments, (parameterTypes) -> this.clazz.getMethod(methodName, parameterTypes),
				(target, inputArguments) -> {
					target.setAccessible(true);
					Object result = target.invoke(null, inputArguments);
					return ((target.getReturnType() == null) || (void.class.equals(target.getReturnType()))) ? null
							: new ObjectCompatibility(result);
				}, (target, parameterTypes, ex) -> {
					return "Unknown static method " + this.clazz.getName() + "#" + methodName + "(" + String.join(", ",
							Arrays.asList(parameterTypes).stream().map((type) -> type.getName()).toArray(String[]::new))
							+ ")";
				});
	}

	/**
	 * Instantiate new instance of the {@link Object}.
	 * 
	 * @param arguments Arguments for the constructor.
	 * @return {@link ObjectCompatibility} for the constructed {@link Object}.
	 */
	public ObjectCompatibility _new(Object... arguments) {
		return this.invoke(arguments, (parameterTypes) -> this.clazz.getConstructor(parameterTypes),
				(target, inputArguments) -> new ObjectCompatibility(target.newInstance(inputArguments)),
				(target, parameterTypes, ex) -> {
					return "Unknown constructor " + this.clazz.getName() + "(" + String.join(", ",
							Arrays.asList(parameterTypes).stream().map((type) -> type.getName()).toArray(String[]::new))
							+ ")";
				});
	}

	/**
	 * Creates an {@link ArgumentCompatibility} for value.
	 * 
	 * @param value         Value.
	 * @param parameterType {@link Method} parameter type.
	 * @return {@link ArgumentCompatibility}.
	 */
	public ArgumentCompatibility arg(Object value, Class<?> parameterType) {
		return new ArgumentCompatibility(value, parameterType);
	}

	/**
	 * Creates an {@link ArgumentCompatibility} for value.
	 * 
	 * @param value             Value.
	 * @param parameterTypeName Name of the {@link Method} parameter type.
	 * @return {@link ArgumentCompatibility}.
	 */
	public ArgumentCompatibility arg(Object value, String parameterTypeName) {
		return new ArgumentCompatibility(value, loadClass(parameterTypeName, classLoader));
	}

	/**
	 * {@link FunctionalInterface} to locate the {@link Constructor} or
	 * {@link Method}.
	 */
	@FunctionalInterface
	private static interface Locator<T> {
		T locate(Class<?>[] parameterTypes) throws Exception;
	}

	/**
	 * {@link FunctionalInterface} to invoke the target {@link Constructor} or
	 * {@link Method}.
	 */
	@FunctionalInterface
	private static interface Invoker<T> {
		ObjectCompatibility invoke(T target, Object[] arguments) throws Exception;
	}

	/**
	 * {@link FunctionalInterface} to create the error message.
	 */
	@FunctionalInterface
	private static interface ErrorFactory<T> {
		String getMessage(T target, Class<?>[] parameterTypes, Exception exception);
	}

	/**
	 * Invokes the target.
	 * 
	 * @param arguments    Arguments for the target.
	 * @param locator      {@link Locator}.
	 * @param invoker      {@link Invoker}.
	 * @param errorFactory {@link ErrorFactory}.
	 * @return {@link ObjectCompatibility} for result. May be <code>null</code>.
	 */
	private <T> ObjectCompatibility invoke(Object[] arguments, Locator<T> locator, Invoker<T> invoker,
			ErrorFactory<T> errorFactory) {

		// Create the list of parameter types and input arguments
		Class<?>[] parameterTypes = new Class[arguments.length];
		Object[] inputArguments = new Object[arguments.length];
		for (int i = 0; i < arguments.length; i++) {
			Object argument = arguments[i];
			Class<?> parameterType;
			Object inputArgument;
			if (argument instanceof ArgumentCompatibility) {
				ArgumentCompatibility compatibility = (ArgumentCompatibility) argument;
				parameterType = compatibility.parameterType;
				inputArgument = compatibility.object;
			} else {
				parameterType = argument.getClass();
				inputArgument = argument;
			}
			parameterTypes[i] = parameterType;
			inputArguments[i] = inputArgument;
		}

		// Find the target
		T target;
		try {
			target = locator.locate(parameterTypes);
		} catch (Exception ex) {
			throw new IllegalStateException(ERROR_PREFIX + errorFactory.getMessage(null, parameterTypes, ex), ex);
		}

		// Invoke the target
		ObjectCompatibility result;
		try {
			result = invoker.invoke(target, inputArguments);
		} catch (InvocationTargetException ex) {
			throw new CompatibilityInvocationException(ex.getCause());
		} catch (Exception ex) {
			throw new IllegalStateException(ERROR_PREFIX + errorFactory.getMessage(target, parameterTypes, ex), ex);
		}

		// Return the compatibility result
		return result;
	}

	/**
	 * Wraps the {@link ObjectCompatibility}.
	 * 
	 * @param object {@link Object}.
	 * @return {@link ObjectCompatibility}.
	 */
	private ObjectCompatibility wrap(Object object) {
		return new ObjectCompatibility(object);
	}

	/**
	 * Provides compatibility for an {@link Object}.
	 */
	public class ObjectCompatibility {

		/**
		 * Object.
		 */
		private final Object object;

		/**
		 * Instantiate.
		 * 
		 * @param object Object.
		 */
		private ObjectCompatibility(Object object) {
			this.object = object;
		}

		/**
		 * Undertakes {@link Object} instance {@link Method}.
		 * 
		 * @param methodName Name of the instance {@link Method}.
		 * @param arguments  Arguments for the {@link Method}.
		 * @return {@link ObjectCompatibility}. Will be <code>null</code> for
		 *         <code>void</code>method.
		 */
		public ObjectCompatibility $(String methodName, Object... arguments) {
			return ClassCompatibility.this.invoke(arguments,
					(parameterTypes) -> this.object.getClass().getMethod(methodName, parameterTypes),
					(target, inputArguments) -> {
						Object result = target.invoke(this.object, inputArguments);
						return ((target.getReturnType() == null) || (void.class.equals(target.getReturnType()))) ? null
								: new ObjectCompatibility(result);
					}, (target, parameterTypes, ex) -> {
						return "Unknown method " + this.object.getClass().getName() + "#" + methodName + "("
								+ String.join(", ", Arrays.asList(parameterTypes).stream().map((type) -> type.getName())
										.toArray(String[]::new))
								+ ")";
					});
		}

		/**
		 * Creates an {@link ArgumentCompatibility} for this {@link Object}.
		 * 
		 * @param parameterType {@link Method} parameter type.
		 * @return {@link ArgumentCompatibility}.
		 */
		public ArgumentCompatibility arg(Class<?> parameterType) {
			return new ArgumentCompatibility(this.object, parameterType);
		}

		/**
		 * Creates an {@link ArgumentCompatibility} for this {@link Object}.
		 * 
		 * @param parameterTypeName Name of the {@link Method} parameter type.
		 * @return {@link ArgumentCompatibility}.
		 */
		public ArgumentCompatibility arg(String parameterTypeName) {
			return new ArgumentCompatibility(this.object,
					loadClass(parameterTypeName, ClassCompatibility.this.classLoader));
		}

		/**
		 * Obtains the object value.
		 * 
		 * @param type Type expected.
		 * @return Object value.
		 */
		@SuppressWarnings("unchecked")
		public <T> T get(Class<? extends T> type) {
			if (this.object == null) {
				return null;
			}
			if (!type.isAssignableFrom(this.object.getClass())) {
				throw new IllegalStateException(ERROR_PREFIX + "Return value not of type " + type.getName()
						+ " (but was " + this.object.getClass().getName() + ")");
			}
			return (T) this.object;
		}
	}

	/**
	 * <p>
	 * Argument compatibility.
	 * <p>
	 * This allows specifying the {@link Method} parameter type should it be less
	 * specific than the argument {@link Object}.
	 */
	public static class ArgumentCompatibility {

		/**
		 * Object.
		 */
		private final Object object;

		/**
		 * Parameter type for the {@link Method}.
		 */
		private Class<?> parameterType;

		/**
		 * Instantiate.
		 * 
		 * @param object             Object.
		 * @param methodArgumentType Parameter type for the {@link Method}.
		 */
		private ArgumentCompatibility(Object object, Class<?> methodArgumentType) {
			this.object = object;
			this.parameterType = methodArgumentType;
		}
	}

	/**
	 * Indicates failure from {@link Method}.
	 */
	public static class CompatibilityInvocationException extends RuntimeException {

		/**
		 * Serial version UID.
		 */
		private static final long serialVersionUID = 1L;

		/**
		 * Instantiate.
		 * 
		 * @param cause {@link Throwable} cause.
		 */
		private CompatibilityInvocationException(Throwable cause) {
			super(cause.getMessage(), cause);
		}
	}

}
