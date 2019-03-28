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
package net.officefloor.plugin.managedfunction.clazz;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

import net.officefloor.compile.ManagedFunctionSourceService;
import net.officefloor.compile.spi.managedfunction.source.FunctionNamespaceBuilder;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionSource;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionSourceContext;
import net.officefloor.compile.spi.managedfunction.source.impl.AbstractManagedFunctionSource;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.plugin.managedfunction.clazz.MethodManagedFunctionBuilder.MethodObjectInstanceManufacturer;

/**
 * {@link ManagedFunctionSource} for a {@link Class} having the {@link Method}
 * instances as the {@link ManagedFunction} instances.
 * 
 * @author Daniel Sagenschneider
 */
public class ClassManagedFunctionSource extends AbstractManagedFunctionSource
		implements ManagedFunctionSourceService<ClassManagedFunctionSource> {

	/**
	 * Property name providing the {@link Class} name.
	 */
	public static final String CLASS_NAME_PROPERTY_NAME = "class.name";

	/**
	 * Creates the {@link MethodManagedFunctionBuilder}.
	 * 
	 * @param namespaceBuilder {@link FunctionNamespaceBuilder}.
	 * @param context          {@link ManagedFunctionSourceContext}.
	 * @return {@link MethodManagedFunctionBuilder}.
	 * @throws Exception If fails to create {@link MethodManagedFunctionBuilder}.
	 */
	protected MethodManagedFunctionBuilder createMethodManagedFunctionBuilder(FunctionNamespaceBuilder namespaceBuilder,
			ManagedFunctionSourceContext context) throws Exception {
		return new MethodManagedFunctionBuilder();
	}

	/*
	 * =================== ManagedFunctionSourceService ===================
	 */

	@Override
	public String getManagedFunctionSourceAlias() {
		return "CLASS";
	}

	@Override
	public Class<ClassManagedFunctionSource> getManagedFunctionSourceClass() {
		return ClassManagedFunctionSource.class;
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
		Class<?> clazz = context.loadClass(className);

		// Create the method object instance manufacturer
		MethodObjectInstanceFactory instanceFactory = new DefaultConstructorMethodObjectInstanceFactory(clazz);
		MethodObjectInstanceManufacturer instanceManufacturer = () -> {
			return instanceFactory;
		};

		// Create the method managed function builder
		MethodManagedFunctionBuilder methodBuilder = this.createMethodManagedFunctionBuilder(namespaceBuilder, context);

		// Work up the hierarchy of classes to inherit methods by name
		Set<String> includedMethodNames = new HashSet<String>();
		while ((clazz != null) && (!(Object.class.equals(clazz)))) {

			// Obtain the listing of functions from the methods of the class
			Set<String> currentClassMethods = new HashSet<String>();
			NEXT_METHOD: for (Method method : clazz.getDeclaredMethods()) {

				// Ignore non-function methods
				if (!methodBuilder.isCandidateFunctionMethod(method)) {
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
				methodBuilder.buildMethod(method, clazz, instanceManufacturer, namespaceBuilder, context);
			}

			// Add methods from the parent class on next iteration
			clazz = clazz.getSuperclass();
		}
	}

}