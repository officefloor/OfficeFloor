/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2011 Daniel Sagenschneider
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
package net.officefloor.compile.impl;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * {@link InvocationHandler} to enable type compatibility between interface
 * loaded in one {@link ClassLoader} and implementation in another. This is
 * overcomes the assignable issues while still allowing invocation (via
 * reflection internally).
 * 
 * @author Daniel Sagenschneider
 */
public class TypeAdapter implements InvocationHandler {

	/**
	 * Invokes the method expecting no {@link Exception}.
	 * 
	 * @param implementation
	 *            Implementation.
	 * @param methodName
	 *            Name of the method.
	 * @param arguments
	 *            Arguments for the method.
	 * @param paramTypes
	 *            Parameter types for the method. May be <code>null</code>.
	 * @param clientClassLoader
	 *            {@link ClassLoader} of the client.
	 * @param implClassLoader
	 *            {@link ClassLoader} of the implementation.
	 * @return Return value from the {@link Method} invocation.
	 */
	public static Object invokeNoExceptionMethod(Object implementation,
			String methodName, Object[] arguments, Class<?>[] paramTypes,
			ClassLoader clientClassLoader, ClassLoader implClassLoader) {
		try {

			// Invoke the method
			return invokeMethod(implementation, methodName, arguments,
					paramTypes, clientClassLoader, implClassLoader);

			// Provide best attempt to propagate failure
		} catch (RuntimeException ex) {
			throw (RuntimeException) ex;
		} catch (Error ex) {
			throw (Error) ex;
		} catch (Throwable ex) {
			// Incompatibility in throws
			throw OfficeFloorVersionIncompatibilityException
					.newTypeIncompatibilityException(implementation,
							methodName, paramTypes);
		}
	}

	/**
	 * Invokes the implementing method.
	 * 
	 * @param implementation
	 *            Implementation.
	 * @param methodName
	 *            Name of the method.
	 * @param arguments
	 *            Arguments for the method.
	 * @param paramTypes
	 *            Parameter types for the method.
	 * @param clientClassLoader
	 *            {@link ClassLoader} of the client.
	 * @param implClassLoader
	 *            {@link ClassLoader} of the implementation.
	 * @return Return value from the {@link Method} invocation.
	 * @throws Throwable
	 *             If fails to invoke the {@link Method}.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static Object invokeMethod(Object implementation, String methodName,
			Object[] arguments, Class<?>[] paramTypes,
			ClassLoader clientClassLoader, ClassLoader implClassLoader)
			throws Throwable {

		// Ensure have arguments
		if (arguments == null) {
			arguments = new Object[paramTypes.length];
		}

		// Transform the parameter types for implementation
		for (int i = 0; i < paramTypes.length; i++) {
			paramTypes[i] = translateClass(paramTypes[i], implClassLoader);
		}

		// Obtain the implementation class
		Class<?> implementationClass = implementation.getClass();

		// Obtain the implementing method
		Method method = null;
		try {
			method = implementationClass.getMethod(methodName, paramTypes);

		} catch (NoSuchMethodException ex) {
			// No method, so not compatible
			throw OfficeFloorVersionIncompatibilityException
					.newTypeIncompatibilityException(implementation,
							methodName, paramTypes);
		}

		// Determine if must adapt arguments
		Class<?>[] methodParamTypes = method.getParameterTypes();
		for (int i = 0; i < arguments.length; i++) {
			Class<?> methodParamType = methodParamTypes[i];

			// Obtain the overriding argument
			Object argument = arguments[i];
			Object overridingArgument = null;

			// Determine if a proxy
			if ((argument != null) && (Proxy.isProxyClass(argument.getClass()))) {
				InvocationHandler handler = Proxy
						.getInvocationHandler(argument);
				if (handler instanceof TypeAdapter) {
					TypeAdapter adapter = (TypeAdapter) handler;

					// Use implementation as argument
					overridingArgument = adapter.implementation;
				}
			}

			// If not proxied implementation, adapt argument for invocation
			if (overridingArgument == null) {
				if (methodParamType.isInterface()) {
					// Adapt argument (need to be available for Class Loader)
					overridingArgument = createProxy(argument, implClassLoader,
							clientClassLoader, methodParamType);

				} else if (argument != null) {
					// Determine if can ignore argument
					if (Class.class.getName().equals(methodParamType.getName())) {
						// Translate class
						Class<?> classArgument = (Class<?>) argument;
						overridingArgument = translateClass(classArgument,
								implClassLoader);

					} else if (methodParamType.isEnum()) {
						// Transform enumeration
						Enum<?> argumentEnum = (Enum<?>) argument;
						overridingArgument = Enum.valueOf(
								(Class) methodParamType, argumentEnum.name());

					} else if ((String.class.getName().equals(methodParamType
							.getName()))
							|| (String.class.getName().equals(argument
									.getClass().getName()))
							|| (methodParamType.isPrimitive())) {
						// Maintain argument
						overridingArgument = argument;

					} else {
						// Ignore non-important argument
						overridingArgument = null;
					}
				}
			}

			// Specify the overridden argument value
			arguments[i] = overridingArgument;
		}

		// Invoke the method
		Object returnValue;
		try {
			method.setAccessible(true);
			returnValue = method.invoke(implementation, arguments);

		} catch (InvocationTargetException ex) {
			// Propagate method failure
			throw ex.getCause();

		} catch (Exception ex) {
			// Not compatible
			throw OfficeFloorVersionIncompatibilityException
					.newTypeIncompatibilityException(implementation,
							methodName, paramTypes);
		}

		// Determine if return is an interface
		Class<?> returnType = method.getReturnType();
		if (returnType != null) {
			if (Class.class.getName().equals(returnType.getName())) {
				// Transform class for return
				returnValue = translateClass((Class<?>) returnValue,
						clientClassLoader);

			} else if (returnType.isInterface()) {
				// Adapt the return (need to be available for Class Loader)
				returnValue = createProxy(returnValue, clientClassLoader,
						implClassLoader, returnType);

			} else if (returnType.isArray()) {
				// Array return type, so must adapt each element
				Class<?> componentType = returnType.getComponentType();
				if (componentType.isInterface()) {
					Object[] returnArray = (Object[]) returnValue;

					// Array of interfaces requiring adapting
					componentType = translateClass(componentType,
							clientClassLoader);
					Object[] array = (Object[]) Array.newInstance(
							componentType, returnArray.length);

					// Adapt the array elements
					for (int i = 0; i < array.length; i++) {
						Object returnElement = returnArray[i];
						if (returnElement != null) {
							array[i] = createProxy(returnElement,
									clientClassLoader, implClassLoader,
									componentType);
						}
					}

					// Override return value with adapted array
					returnValue = array;
				}

			} else if (Object.class.getName().equals(returnType.getName())) {
				// Non-specific return so use implementation interfaces
				if (returnValue != null) {
					Class<?>[] interfaces = returnValue.getClass()
							.getInterfaces();
					if (interfaces.length > 0) {
						// Provide proxy for interfaces of implementation
						returnValue = createProxy(returnValue,
								clientClassLoader, implClassLoader, interfaces);
					}
				}
			}
		}

		// Return the value
		return returnValue;
	}

	/**
	 * Translates the {@link Class} for use with the {@link ClassLoader}.
	 * 
	 * @param clazz
	 *            {@link Class}.
	 * @param classLoader
	 *            {@link ClassLoader}.
	 * @return Translated {@link Class}.
	 * @throws ClassNotFoundException
	 *             If fails to obtain translated {@link Class}.
	 */
	private static Class<?> translateClass(Class<?> clazz,
			ClassLoader classLoader) throws ClassNotFoundException {

		// Do not translate if primitive
		if (clazz.isPrimitive()) {
			return clazz;
		}

		// Translate class
		return classLoader.loadClass(clazz.getName());
	}

	/**
	 * Creates a {@link Proxy}.
	 * 
	 * @param implementation
	 *            Implementation behind the {@link Proxy}.
	 * @param clientClassLoader
	 *            {@link ClassLoader} for the client.
	 * @param implClassLoader
	 *            {@link ClassLoader} for the implementation.
	 * @param interfaceType
	 *            Interfaces for the {@link Proxy}.
	 * @return {@link Proxy}.
	 * @throws ClassNotFoundException
	 *             If fails to load interface type for {@link Proxy}.
	 */
	public static Object createProxy(Object implementation,
			ClassLoader clientClassLoader, ClassLoader implClassLoader,
			Class<?>... interfaceTypes) throws ClassNotFoundException {

		// Ensure interface types can be loaded by client Class Loader
		for (int i = 0; i < interfaceTypes.length; i++) {
			interfaceTypes[i] = translateClass(interfaceTypes[i],
					clientClassLoader);
		}

		// Create the proxy
		Object proxy = Proxy.newProxyInstance(clientClassLoader,
				interfaceTypes, new TypeAdapter(implementation,
						clientClassLoader, implClassLoader));

		// Return the proxy
		return proxy;
	}

	/**
	 * Type implementation.
	 */
	private final Object implementation;

	/**
	 * {@link ClassLoader} of the client.
	 */
	private final ClassLoader clientClassLoader;

	/**
	 * {@link ClassLoader} of the implementation.
	 */
	private final ClassLoader implClassLoader;

	/**
	 * Initiate.
	 * 
	 * @param implementation
	 *            Type implementation.
	 * @param clientClassLoader
	 *            {@link ClassLoader} of the client.
	 * @param implClassLoader
	 *            {@link ClassLoader} of the implementation.
	 */
	public TypeAdapter(Object implementation, ClassLoader clientClassLoader,
			ClassLoader implClassLoader) {
		this.implementation = implementation;
		this.clientClassLoader = clientClassLoader;
		this.implClassLoader = implClassLoader;
	}

	/*
	 * ====================== InvocationHandler ========================
	 */

	@Override
	public Object invoke(Object proxy, Method method, Object[] args)
			throws Throwable {
		return invokeMethod(this.implementation, method.getName(), args,
				method.getParameterTypes(), this.clientClassLoader,
				this.implClassLoader);
	}

}