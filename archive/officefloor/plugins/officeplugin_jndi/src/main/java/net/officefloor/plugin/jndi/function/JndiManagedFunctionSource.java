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
package net.officefloor.plugin.jndi.function;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

import javax.naming.Context;

import net.officefloor.compile.managedfunction.ManagedFunctionObjectType;
import net.officefloor.compile.spi.managedfunction.source.FunctionNamespaceBuilder;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionSource;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionSourceContext;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionTypeBuilder;
import net.officefloor.compile.spi.managedfunction.source.impl.AbstractManagedFunctionSource;
import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.function.ManagedFunctionContext;
import net.officefloor.frame.api.function.ManagedFunctionFactory;
import net.officefloor.frame.api.manage.OfficeFloor;

/**
 * {@link ManagedFunctionSource} to execute {@link Method} instances on a JNDI
 * Object.
 * 
 * @author Daniel Sagenschneider
 */
public class JndiManagedFunctionSource extends AbstractManagedFunctionSource {

	/**
	 * Name of property containing the JNDI name of the Object that is to be the
	 * functionality of the {@link ManagedFunction}.
	 */
	public static final String PROPERTY_JNDI_NAME = "jndi.name";

	/**
	 * Name of property containing the fully qualified type of the expected JNDI
	 * Object.
	 */
	public static final String PROPERTY_OBJECT_TYPE = "object.type";

	/**
	 * <p>
	 * Name of property containing the fully qualified class of the facade.
	 * <p>
	 * To simplify using JNDI objects as {@link ManagedFunction}, a facade can
	 * be optionally used to simplify the JNDI object methods for configuring
	 * into {@link OfficeFloor}.
	 * <p>
	 * Only {@link Method} instances of the facade that have a parameter as per
	 * {@link #PROPERTY_OBJECT_TYPE} are included. The JNDI Object will however
	 * not appear as a {@link ManagedFunctionObjectType}, as it will be
	 * provided.
	 * <p>
	 * Should the facade have a {@link Method} of the same name as a
	 * {@link Method} of the JNDI Object, the facade method will override the
	 * JNDI Object {@link Method}.
	 * <p>
	 * The facade allows for example to:
	 * <ol>
	 * <li>Manipulating the parameter of the {@link ManagedFunction} to the JNDI
	 * Object {@link Method} parameters</li>
	 * <li>Changing the type/order of parameters to the JNDI Object
	 * {@link Method}</li>
	 * <li>Invoking the JNDI Object {@link Method} instances multiple times to
	 * process lists</li>
	 * </ol>
	 * <p>
	 * This also helps reduce the number of JNDI look ups when undertaking
	 * multiple operations on the JNDI object.
	 */
	public static final String PROPERTY_FACADE_CLASS = "facade.class";

	/*
	 * ===================== ManagedFunctionSource =====================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
		context.addProperty(PROPERTY_JNDI_NAME, "JNDI Name");
		context.addProperty(PROPERTY_OBJECT_TYPE, "Object Type");
	}

	@Override
	public void sourceManagedFunctions(FunctionNamespaceBuilder functionTypeBuilder,
			ManagedFunctionSourceContext context) throws Exception {

		// Obtain the JNDI name
		String jndiName = context.getProperty(PROPERTY_JNDI_NAME);

		// Obtain the object type
		String objectTypeName = context.getProperty(PROPERTY_OBJECT_TYPE);
		final Class<?> objectType = context.loadClass(objectTypeName);

		// Keep track of the registered functions
		final Set<String> registeredFunctions = new HashSet<String>();

		// Obtain the possible facade class name
		String facadeClassName = context.getProperty(PROPERTY_FACADE_CLASS, null);

		// Create the JNDI reference and possible facade class
		Class<?> facadeClass = (facadeClassName != null) ? context.loadClass(facadeClassName) : null;
		JndiReference reference = new JndiReference(jndiName, facadeClass);

		// Load the facade class methods
		if (facadeClass != null) {

			// Obtain listing of functions from methods of the facade class
			for (Method method : facadeClass.getMethods()) {

				// Potentially register the method
				this.registerMethodAsPotentialFunction(functionTypeBuilder, method, objectType,
						(functionName, functionMethod, isStatic, parameters) -> {

							// Indicate registered function
							registeredFunctions.add(functionName);

							// Create and return the facade function factory
							return new JndiFacadeManagedFunctionFactory(reference, functionMethod, isStatic,
									parameters);
						});
			}
		}

		// Obtain listing of functions from the methods of the namespace type
		for (Method method : objectType.getMethods()) {

			// Potentially register the method
			this.registerMethodAsPotentialFunction(functionTypeBuilder, method, null,
					(functionName, functionMethod, isStatic, parameters) -> {

						// Take facade method over JNDI Object method
						if (registeredFunctions.contains(functionName)) {
							return null; // have facade function
						}

						// Create and return JNDI object function factory
						return new JndiObjectManagedFunctionFactory(reference, functionMethod, parameters);
					});
		}
	}

	/**
	 * Manufacturer of a {@link ManagedFunctionFactory}.
	 */
	private interface ManagedFunctionFactoryManufacturer {

		/**
		 * Creates the {@link ManagedFunctionFactory}.
		 * 
		 * @param functionName
		 *            Name of the {@link ManagedFunction}.
		 * @param method
		 *            {@link Method} for the {@link ManagedFunction}.
		 * @param isStatic
		 *            <code>true</code> if the {@link Method} is static.
		 * @param parameters
		 *            {@link ParameterFactory} instances for the
		 *            {@link ManagedFunction}.
		 * @return {@link ManagedFunctionFactory}.
		 */
		@SuppressWarnings("rawtypes")
		ManagedFunctionFactory createManagedFunctionFactory(String functionName, Method method, boolean isStatic,
				ParameterFactory[] parameters);
	}

	/**
	 * Registers the {@link ManagedFunction}.
	 * 
	 * @param namespaceTypeBuilder
	 *            {@link FunctionNamespaceBuilder}.
	 * @param method
	 *            {@link Method} to potentially create the
	 *            {@link ManagedFunction} from.
	 * @param objectType
	 *            Type of JNDI object. May be <code>null</code> but if provided
	 *            must be one of the parameter types of the {@link Method}.
	 * @param manufacturer
	 *            {@link ManagedFunctionFactoryManufacturer}.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void registerMethodAsPotentialFunction(FunctionNamespaceBuilder namespaceTypeBuilder, Method method,
			Class<?> objectType, ManagedFunctionFactoryManufacturer manufacturer) {

		// Ignore non-public methods
		if (!Modifier.isPublic(method.getModifiers())) {
			return;
		}

		// Ignore Object methods
		if (Object.class.equals(method.getDeclaringClass())) {
			return;
		}

		// Obtain details of the method
		String methodName = method.getName();
		Class<?>[] paramTypes = method.getParameterTypes();

		// Determine if include based on object type provided
		if (objectType != null) {
			boolean hasObjectTypeParameter = false;
			for (Class<?> paramType : paramTypes) {
				// (qualified name match due to class loader issues)
				if (objectType.equals(paramType) || (objectType.getName().equals(paramType.getName()))) {
					hasObjectTypeParameter = true;
				}
			}
			if (!hasObjectTypeParameter) {
				// Does not have object type so do not include method
				return;
			}
		}

		// Create to parameters to method to be populated.
		ParameterFactory[] parameters = new ParameterFactory[paramTypes.length];

		// Determine if the method is static
		boolean isStatic = Modifier.isStatic(method.getModifiers());

		// Create the TaskFactory
		ManagedFunctionFactory taskFactory = manufacturer.createManagedFunctionFactory(methodName, method, isStatic,
				parameters);
		if (taskFactory == null) {
			return; // no task factory, so do not register
		}

		// Include method as task in type definition
		ManagedFunctionTypeBuilder<Indexed, None> taskTypeBuilder = namespaceTypeBuilder
				.addManagedFunctionType(methodName, taskFactory, Indexed.class, None.class);

		// Define the return type (it not void)
		Class<?> returnType = method.getReturnType();
		if ((returnType != null) && (!Void.TYPE.equals(returnType))) {
			taskTypeBuilder.setReturnType(returnType);
		}

		// Add the Context dependency
		taskTypeBuilder.addObject(Context.class).setLabel(Context.class.getName());

		// Define the listing of task objects
		int objectIndex = 1; // Index after Context dependency
		for (int i = 0; i < paramTypes.length; i++) {
			// Obtain the parameter type
			Class<?> paramType = paramTypes[i];

			// Determine if task context
			if (ManagedFunctionContext.class.equals(paramType)) {
				// Parameter is a task context.
				parameters[i] = new ManagedFunctionContextParameterFactory();
				continue;
			}

			// Determine if object type.
			// (qualified name match due to class loader issues)
			if (objectType != null) {
				if (objectType.equals(paramType) || (objectType.getName().equals(paramType.getName()))) {
					// Parameter is the object type.
					parameters[i] = new JndiObjectParameterFactory();
					continue;
				}
			}

			// Otherwise must be an object.
			parameters[i] = new ObjectParameterFactory(objectIndex++);
			taskTypeBuilder.addObject(paramType).setLabel(paramType.getName());
		}

		// Define the escalation listing
		for (Class<?> escalationType : method.getExceptionTypes()) {
			taskTypeBuilder.addEscalation((Class<Throwable>) escalationType);
		}
	}

}