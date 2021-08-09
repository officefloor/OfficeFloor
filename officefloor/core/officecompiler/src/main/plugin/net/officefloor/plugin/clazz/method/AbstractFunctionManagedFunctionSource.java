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

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

import net.officefloor.compile.properties.Property;
import net.officefloor.compile.spi.managedfunction.source.FunctionNamespaceBuilder;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionSource;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionSourceContext;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionTypeBuilder;
import net.officefloor.compile.spi.managedfunction.source.impl.AbstractManagedFunctionSource;
import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.plugin.clazz.NonFunctionMethod;

/**
 * {@link ManagedFunctionSource} for a {@link Class} having the {@link Method}
 * instances as the {@link ManagedFunction} instances.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class AbstractFunctionManagedFunctionSource extends AbstractManagedFunctionSource {

	/**
	 * {@link Property} name providing the {@link Class} name.
	 */
	public static final String CLASS_NAME_PROPERTY_NAME = "class.name";

	/**
	 * {@link Property} name specifying a single {@link Method} to use for the
	 * {@link ManagedFunction}.
	 */
	public static final String PROPERTY_FUNCTION_NAME = "function.name";

	/**
	 * Builds the {@link Method}.
	 * 
	 * @param clazz                  {@link Class} of object to invoke
	 *                               {@link Method} against.
	 * @param method                 {@link Method}.
	 * @param managedFunctionBuilder {@link MethodManagedFunctionBuilder}.
	 * @return {@link ManagedFunctionTypeBuilder}.
	 * @throws Exception If fails to build the {@link Method}.
	 */
	protected ManagedFunctionTypeBuilder<Indexed, Indexed> buildMethod(Class<?> clazz, Method method,
			MethodManagedFunctionBuilder managedFunctionBuilder) throws Exception {
		return managedFunctionBuilder.buildMethod(method);
	}

	/*
	 * =================== AbstractManagedFunctionSource ===================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
		context.addProperty(CLASS_NAME_PROPERTY_NAME, "Class");
	}

	public void sourceManagedFunctions(FunctionNamespaceBuilder namespaceBuilder, ManagedFunctionSourceContext context)
			throws Exception {

		// Obtain the class
		String className = context.getProperty(CLASS_NAME_PROPERTY_NAME);
		Class<?> objectClass = context.loadClass(className);

		// Create the method managed function builder
		MethodManagedFunctionBuilder methodBuilder = new MethodManagedFunctionBuilder(objectClass, namespaceBuilder,
				context);

		// Determine if only single method
		String singleMethodName = context.getProperty(PROPERTY_FUNCTION_NAME, null);

		// Work up the hierarchy of classes to inherit methods by name
		Set<String> includedMethodNames = new HashSet<String>();
		Class<?> clazz = objectClass;
		while ((clazz != null) && (!(Object.class.equals(clazz)))) {

			// Obtain the listing of functions from the methods of the class
			Set<String> currentClassMethods = new HashSet<String>();
			NEXT_METHOD: for (Method method : clazz.getDeclaredMethods()) {

				// Determine if include method
				if ((singleMethodName != null) && (!singleMethodName.equals(method.getName()))) {
					continue NEXT_METHOD;
				}

				// Ignore non-public methods
				if (!Modifier.isPublic(method.getModifiers())) {
					continue NEXT_METHOD;
				}

				// Ignore non-function methods
				if (method.isAnnotationPresent(NonFunctionMethod.class)) {
					continue NEXT_METHOD;
				}

				// Determine if method already exists on the current class
				String methodName = method.getName();
				if (currentClassMethods.contains(methodName)) {
					throw new IllegalStateException("Two methods by the same name '" + methodName + "' in class "
							+ clazz.getName() + ".  Either rename one of the methods or annotate one with @"
							+ NonFunctionMethod.class.getSimpleName());
				}
				currentClassMethods.add(methodName);

				// Ignore if already included method
				if (includedMethodNames.contains(methodName)) {
					continue NEXT_METHOD;
				}
				includedMethodNames.add(methodName);

				// Build the managed function for the method
				this.buildMethod(objectClass, method, methodBuilder);
			}

			// Add methods from the parent class on next iteration
			clazz = clazz.getSuperclass();
		}
	}

}
