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

package net.officefloor.compile.impl.adapt;

import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.InaccessibleObjectException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

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
	 * Primitive object types.
	 */
	private static final Set<String> compatibleObjectTypes = new HashSet<String>();

	/**
	 * Required types that always require proxying.
	 */
	private static final Set<String> alwaysProxyRequiredTypes = new HashSet<String>();

	static {
		// Load the compatible object types
		compatibleObjectTypes.add(Boolean.class.getName());
		compatibleObjectTypes.add(Short.class.getName());
		compatibleObjectTypes.add(Character.class.getName());
		compatibleObjectTypes.add(Integer.class.getName());
		compatibleObjectTypes.add(Long.class.getName());
		compatibleObjectTypes.add(Float.class.getName());
		compatibleObjectTypes.add(Double.class.getName());
		compatibleObjectTypes.add(String.class.getName());
		compatibleObjectTypes.add(InputStream.class.getName());

		// Load the always proxy required types
		alwaysProxyRequiredTypes.add(Object.class.getName());
		alwaysProxyRequiredTypes.add(Collection.class.getName());
		alwaysProxyRequiredTypes.add(List.class.getName());
		alwaysProxyRequiredTypes.add(Set.class.getName());
		alwaysProxyRequiredTypes.add(Map.class.getName());
		alwaysProxyRequiredTypes.add(Iterator.class.getName());
	}

	/**
	 * Invokes the method expecting no {@link Exception}.
	 * 
	 * @param implementation    Implementation.
	 * @param methodName        Name of the method.
	 * @param arguments         Arguments for the method.
	 * @param paramTypes        Parameter types for the method. May be
	 *                          <code>null</code>.
	 * @param clientClassLoader {@link ClassLoader} of the client.
	 * @param implClassLoader   {@link ClassLoader} of the implementation.
	 * @return Return value from the {@link Method} invocation.
	 */
	public static Object invokeNoExceptionMethod(Object implementation, String methodName, Object[] arguments,
			Class<?>[] paramTypes, ClassLoader clientClassLoader, ClassLoader implClassLoader) {
		try {

			// Invoke the method
			return invokeMethod(implementation, methodName, arguments, paramTypes, clientClassLoader, implClassLoader);

			// Provide best attempt to propagate failure
		} catch (RuntimeException ex) {
			throw (RuntimeException) ex;
		} catch (Error ex) {
			throw (Error) ex;
		} catch (Throwable ex) {
			// Incompatibility in throws
			throw OfficeFloorVersionIncompatibilityException.newTypeIncompatibilityException(implementation, methodName,
					paramTypes);
		}
	}

	/**
	 * Invokes the implementing method.
	 * 
	 * @param implementation    Implementation.
	 * @param methodName        Name of the method.
	 * @param arguments         Arguments for the method.
	 * @param paramTypes        Parameter types for the method.
	 * @param clientClassLoader {@link ClassLoader} of the client.
	 * @param implClassLoader   {@link ClassLoader} of the implementation.
	 * @return Return value from the {@link Method} invocation.
	 * @throws Throwable If fails to invoke the {@link Method}.
	 */
	public static Object invokeMethod(Object implementation, String methodName, Object[] arguments,
			Class<?>[] paramTypes, ClassLoader clientClassLoader, ClassLoader implClassLoader) throws Throwable {

		// Ensure have arguments
		if (arguments == null) {
			arguments = new Object[paramTypes.length];
		}

		// Transform the parameter types for implementation
		for (int i = 0; i < paramTypes.length; i++) {

			// Adapt the parameter type
			Class<?> paramType = translateClass(paramTypes[i], implClassLoader);
			if (paramType == null) {
				// No adapted parameter type, then must not be compatible method
				throw OfficeFloorVersionIncompatibilityException.newTypeIncompatibilityException(implementation,
						methodName, paramTypes);
			}
			paramTypes[i] = paramType;
		}

		// Obtain the implementation class
		Class<?> implementationClass = implementation.getClass();

		// Obtain the implementing method
		Method method = null;
		try {
			method = implementationClass.getMethod(methodName, paramTypes);

		} catch (NoSuchMethodException ex) {
			// No method, so not compatible
			throw OfficeFloorVersionIncompatibilityException.newTypeIncompatibilityException(implementation, methodName,
					paramTypes);
		}

		// Adapt the arguments
		for (int i = 0; i < arguments.length; i++) {
			arguments[i] = adaptObject(arguments[i], paramTypes[i], clientClassLoader, implClassLoader);
		}

		// Invoke the method
		Object returnValue;
		try {
			method.setAccessible(true);
			returnValue = method.invoke(implementation, arguments);

		} catch (InvocationTargetException ex) {
			// Propagate method failure
			throw (Throwable) adaptObject(ex.getCause(), Throwable.class, clientClassLoader, implClassLoader);

		} catch (InaccessibleObjectException ex) {
			// Need to open method
			throw OfficeFloorVersionIncompatibilityException.newTypeInaccessibleException(ex, implementation,
					methodName, paramTypes);

		} catch (Exception ex) {
			// Not compatible
			throw OfficeFloorVersionIncompatibilityException.newTypeIncompatibilityException(implementation, methodName,
					paramTypes);
		}

		// Obtain the return type
		Class<?> returnType = method.getReturnType();
		if (returnType != null) {
			returnType = translateClass(returnType, clientClassLoader);
		}

		// Adapt return value
		returnValue = adaptObject(returnValue, returnType, implClassLoader, clientClassLoader);

		// Return the value
		return returnValue;
	}

	/**
	 * Adapts the object.
	 * 
	 * @param object            Object to be adapted.
	 * @param requiredType      Required Type.
	 * @param clientClassLoader Client {@link ClassLoader}.
	 * @param implClassLoader   Implementation {@link ClassLoader}.
	 * @return Adapted object.
	 * @throws Exception If fails to adapt the object.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static Object adaptObject(Object object, Class<?> requiredType, ClassLoader clientClassLoader,
			ClassLoader implClassLoader) throws Exception {

		// Null does not need adapting
		if (object == null) {
			return null;
		}

		// Obtain the object class
		Class<?> objectClass = object.getClass();

		// Determine if a proxy
		if (Proxy.isProxyClass(objectClass)) {
			InvocationHandler handler = Proxy.getInvocationHandler(object);
			if (handler instanceof TypeAdapter) {
				TypeAdapter adapter = (TypeAdapter) handler;

				// Use implementation as object
				return adapter.implementation;
			}
		}

		// Determine if compatible object
		if (isCompatibleType(objectClass)) {
			// Maintain value as compatible
			return object;
		}

		// Determine if compatible array
		if ((objectClass.isArray()) && (isCompatibleType(objectClass.getComponentType()))) {
			// Maintain array object (for primitive arrays)
			return object;
		}

		// Transform for class
		if (Class.class.getName().equals(objectClass.getName())) {
			// Translate class
			Class<?> classObject = (Class<?>) object;
			Class<?> adaptedClass = translateClass(classObject, implClassLoader);

			// Return adapted class (otherwise if not on class loader provide class)
			return adaptedClass != null ? adaptedClass : classObject;
		}

		// Transform for enum
		if (objectClass.isEnum()) {
			// Transform enumeration
			Enum<?> enumObject = (Enum<?>) object;
			Class<?> adaptObjectClass = translateClass(objectClass, implClassLoader);
			return Enum.valueOf((Class) adaptObjectClass, enumObject.name());
		}

		// Transform for logger
		if (Logger.class.getName().equals(objectClass.getName())) {
			return object; // use logger as is
		}

		// Transform for InputStream
		if (InputStream.class.getName().equals(requiredType.getName())) {
			// Create adapted InputStream
			Class<?> adaptInputStreamClass = translateClass(InputStreamAdapter.class, implClassLoader);
			Constructor<?> constructor = adaptInputStreamClass.getConstructor(Object.class, ClassLoader.class,
					ClassLoader.class);
			Object instance = constructor.newInstance(object, clientClassLoader, implClassLoader);
			return instance;
		}

		// Determine if throwable
		if (isThrowable(objectClass)) {

			// Only adapt as throwable if method requiring throwable
			if (isThrowable(requiredType)) {

				// Adapt the throwable
				Throwable cause = (Throwable) object;
				Constructor<?> constructor;
				try {
					// Attempt to adapt the actual throwable type
					Class<?> adaptedCauseClass = translateClass(objectClass, implClassLoader);
					constructor = adaptedCauseClass.getConstructor(String.class);
					return constructor.newInstance(cause.getMessage());

				} catch (Throwable ex) {
					// Only provide exception for CompilerIssue.
					// Therefore can adapt the exception with another.
					Class<?> adaptedExceptionClass = translateClass(AdaptedException.class, implClassLoader);
					StringWriter stackTrace = new StringWriter();
					cause.printStackTrace(new PrintWriter(stackTrace));
					constructor = adaptedExceptionClass.getConstructor(String.class, String.class);
					return constructor.newInstance(cause.getMessage(), stackTrace.toString());
				}
			}
		}

		// Transform list
		if (List.class.getName().equals(requiredType.getName())) {

			// Extract values from list
			Class<?> adaptListClass = translateClass(ListAdapter.class, implClassLoader);
			Object[] values = (Object[]) adaptListClass.getMethod("toArray", Object.class).invoke(null, object);

			// Provide adapted list
			List adaptedList = new ArrayList<>();
			for (Object value : values) {
				Class<?> adaptedValueType = translateClass(value.getClass(), implClassLoader);
				adaptedList.add(adaptObject(value, adaptedValueType, clientClassLoader, implClassLoader));
			}
			return adaptedList;
		}

		// Transform possible array
		if (objectClass.isArray()) {
			// Proxy the array
			Class<?> adaptedComponentType = translateClass(objectClass.getComponentType(), implClassLoader);
			Object[] objectArray = (Object[]) object;
			Object[] adaptedArray = (Object[]) Array.newInstance(adaptedComponentType, objectArray.length);

			// Adapt the elements
			for (int i = 0; i < adaptedArray.length; i++) {
				adaptedArray[i] = adaptObject(objectArray[i], adaptedComponentType, clientClassLoader, implClassLoader);
			}

			// Return the adapted array
			return adaptedArray;
		}

		// Adapt by interfaces
		Class<?>[] interfaces = getInterfaces(objectClass);
		return createProxy(object, implClassLoader, clientClassLoader, interfaces);
	}

	/**
	 * Determine if compatible type.
	 * 
	 * @param type Type to check.
	 * @return <code>true</code> if compatible type.
	 */
	private static boolean isCompatibleType(Class<?> type) {
		return (compatibleObjectTypes.contains(type.getName())) || (type.isPrimitive())
				|| (ClassLoader.class.isAssignableFrom(type));
	}

	/**
	 * Translates the {@link Class} for use with the {@link ClassLoader}.
	 * 
	 * @param clazz       {@link Class}.
	 * @param classLoader {@link ClassLoader}.
	 * @return Translated {@link Class}. May be <code>null</code> if {@link Class}
	 *         not available from {@link ClassLoader}.
	 * @throws ClassNotFoundException If fails to obtain translated {@link Class}.
	 */
	private static Class<?> translateClass(Class<?> clazz, ClassLoader classLoader) throws ClassNotFoundException {

		// Do not translate if primitive
		if (clazz.isPrimitive()) {
			return clazz;
		}

		try {

			// Handle if an array
			if (clazz.isArray()) {

				// Array so obtain the component type
				Class<?> componentType = clazz.getComponentType();

				// Determine if compatible array
				if (isCompatibleType(componentType)) {
					return clazz; // compatible array
				}

				// Translate array of object
				Class<?> adaptedComponentType = classLoader.loadClass(componentType.getName());
				return Array.newInstance(adaptedComponentType, 0).getClass();
			}

			// Translate class
			return classLoader.loadClass(clazz.getName());

		} catch (ClassNotFoundException ex) {
			// Class not available from class loader
			return null;
		}
	}

	/**
	 * Obtains the implementing interfaces the {@link Class}.
	 * 
	 * @param clazz {@link Class}.
	 * @return Implementing interfaces.
	 */
	public static Class<?>[] getInterfaces(Class<?> clazz) {

		// Traverse up the inheritance hierarchy to load interfaces
		List<Class<?>> interfaces = new LinkedList<Class<?>>();
		do {

			// Add the interfaces for the class
			interfaces.addAll(Arrays.asList(clazz.getInterfaces()));

			// Move to the super (parent) class
			clazz = clazz.getSuperclass();

		} while (clazz != null);

		// Return the interfaces
		return interfaces.toArray(new Class[interfaces.size()]);
	}

	/**
	 * Determines if the {@link Class} is a {@link Throwable} (i.e. an
	 * {@link Exception}).
	 * 
	 * @param clazz {@link Class} to check.
	 * @return <code>true</code> if is a {@link Throwable}.
	 */
	private static boolean isThrowable(Class<?> clazz) {
		if (clazz == null) {
			return false; // not throwable (as no super throwable)
		} else if (clazz.getName().equals(Throwable.class.getName())) {
			return true; // throwable
		} else {
			// Check super class to determine if throwable
			return isThrowable(clazz.getSuperclass());
		}
	}

	/**
	 * Creates a {@link Proxy}.
	 * 
	 * @param implementation    Implementation behind the {@link Proxy}.
	 * @param clientClassLoader {@link ClassLoader} for the client.
	 * @param implClassLoader   {@link ClassLoader} for the implementation.
	 * @param interfaceTypes    Interfaces for the {@link Proxy}.
	 * @return {@link Proxy}.
	 * @throws ClassNotFoundException If fails to load interface type for
	 *                                {@link Proxy}.
	 */
	public static Object createProxy(Object implementation, ClassLoader clientClassLoader, ClassLoader implClassLoader,
			Class<?>... interfaceTypes) throws ClassNotFoundException {

		// Ensure interface types can be loaded by client Class Loader
		List<Class<?>> interfaces = new LinkedList<Class<?>>();
		for (int i = 0; i < interfaceTypes.length; i++) {

			// Translate the interface
			Class<?> adaptedInterfaceType = translateClass(interfaceTypes[i], clientClassLoader);
			if (adaptedInterfaceType != null) {
				// Have adaption available
				interfaces.add(adaptedInterfaceType);
			}
		}
		interfaceTypes = interfaces.toArray(new Class[interfaces.size()]);

		// Create the proxy
		Object proxy = Proxy.newProxyInstance(clientClassLoader, interfaceTypes,
				new TypeAdapter(implementation, clientClassLoader, implClassLoader, interfaceTypes));

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
	 * Interface types. Hold reference to make debugging easier.
	 */
	private final Class<?>[] interfaceTypes;

	/**
	 * Access via staic methods.
	 * 
	 * @param implementation    Type implementation.
	 * @param clientClassLoader {@link ClassLoader} of the client.
	 * @param implClassLoader   {@link ClassLoader} of the implementation.
	 * @param interfaceTypes    Interface types. Keep reference to make debugging
	 *                          easier.
	 */
	private TypeAdapter(Object implementation, ClassLoader clientClassLoader, ClassLoader implClassLoader,
			Class<?>[] interfaceTypes) {
		this.implementation = implementation;
		this.clientClassLoader = clientClassLoader;
		this.implClassLoader = implClassLoader;
		this.interfaceTypes = interfaceTypes;
	}

	/*
	 * ========================== Object ===============================
	 */

	@Override
	public String toString() {
		StringBuilder msg = new StringBuilder();
		msg.append(super.toString() + " [");
		for (Class<?> interfaceType : this.interfaceTypes) {
			msg.append(" " + interfaceType.getName());
		}
		msg.append(" ]");
		return msg.toString();
	}

	/*
	 * ====================== InvocationHandler ========================
	 */

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		return invokeMethod(this.implementation, method.getName(), args, method.getParameterTypes(),
				this.clientClassLoader, this.implClassLoader);
	}

}
