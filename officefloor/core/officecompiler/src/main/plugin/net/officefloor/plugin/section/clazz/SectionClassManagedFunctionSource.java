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
package net.officefloor.plugin.section.clazz;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.List;

import net.officefloor.compile.spi.managedfunction.source.FunctionNamespaceBuilder;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionSource;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionTypeBuilder;
import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.function.ManagedFunctionContext;
import net.officefloor.frame.api.function.ManagedFunctionFactory;
import net.officefloor.frame.api.function.StaticManagedFunction;
import net.officefloor.plugin.clazz.Sequence;
import net.officefloor.plugin.managedfunction.clazz.ClassFunction;
import net.officefloor.plugin.managedfunction.clazz.ClassManagedFunctionSource;
import net.officefloor.plugin.managedfunction.clazz.ManagedFunctionParameterFactory;

/**
 * {@link ManagedFunctionSource} implementation to provide the
 * {@link ManagedFunction} instances for the {@link ClassSectionSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class SectionClassManagedFunctionSource extends ClassManagedFunctionSource {

	/*
	 * =================== ClassManagedFunctionSource ==========================
	 */

	@Override
	protected void loadParameterManufacturers(List<ParameterManufacturer> manufacturers) {
		manufacturers.add(new FlowParameterManufacturer<SectionInterface>(SectionInterface.class));
	}

	@Override
	protected ManagedFunctionFactory<Indexed, Indexed> createManagedFunctionFactory(Constructor<?> constructor,
			Method method, ManagedFunctionParameterFactory[] parameters) {
		boolean isStatic = (constructor == null);
		return new SectionManagedFunctionFactory(method, isStatic, parameters);
	}

	@Override
	protected ManagedFunctionTypeBuilder<Indexed, Indexed> addManagedFunctionType(Class<?> clazz,
			FunctionNamespaceBuilder namespaceBuilder, String functionName,
			ManagedFunctionFactory<Indexed, Indexed> functionFactory, Sequence objectSequence, Sequence flowSequence) {

		// Include method as function in type definition
		ManagedFunctionTypeBuilder<Indexed, Indexed> functionTypeBuilder = namespaceBuilder
				.addManagedFunctionType(functionName, functionFactory, Indexed.class, Indexed.class);

		// Add the section object always as first dependency
		functionTypeBuilder.addObject(clazz).setLabel("OBJECT");
		objectSequence.nextIndex(); // index for section object

		// Return the function type builder
		return functionTypeBuilder;
	}

	/**
	 * {@link ManagedFunctionFactory} for overriding
	 * {@link ClassManagedFunctionSource} behaviour.
	 */
	public static class SectionManagedFunctionFactory extends StaticManagedFunction<Indexed, Indexed> {

		/**
		 * {@link Method} for the {@link ManagedFunction}.
		 */
		private final Method method;

		/**
		 * Indicates if the {@link Method} is static.
		 */
		private final boolean isStatic;

		/**
		 * {@link ManagedFunctionParameterFactory} instances for the parameters
		 * of the {@link Method}.
		 */
		private final ManagedFunctionParameterFactory[] parameters;

		/**
		 * Initiate.
		 * 
		 * @param method
		 *            {@link Method} for the {@link ManagedFunction}.
		 * @param isStatic
		 *            Indicates if the {@link Method} is static.
		 * @param parameters
		 *            {@link ManagedFunctionParameterFactory} instances for the
		 *            parameters of the {@link Method}.
		 */
		public SectionManagedFunctionFactory(Method method, boolean isStatic,
				ManagedFunctionParameterFactory[] parameters) {
			this.method = method;
			this.isStatic = isStatic;
			this.parameters = parameters;
		}

		/**
		 * Obtains the {@link Method}.
		 * 
		 * @return {@link Method}.
		 */
		public Method getMethod() {
			return this.method;
		}

		/**
		 * Obtains the {@link ManagedFunctionParameterFactory} instances.
		 * 
		 * @return {@link ManagedFunctionParameterFactory} instances.
		 */
		public ManagedFunctionParameterFactory[] getParameterFactories() {
			return this.parameters;
		}

		/*
		 * ===================== ManagedFunction ===========================
		 */

		@Override
		public Object execute(ManagedFunctionContext<Indexed, Indexed> context) throws Throwable {

			// Obtain the section object
			Object sectionObject = context.getObject(0);

			// Obtain the instance to invoke the method on
			Object instance = (this.isStatic ? null : sectionObject);

			// Create the listing of parameters
			Object[] params = new Object[this.parameters.length];
			for (int i = 0; i < params.length; i++) {
				params[i] = this.parameters[i].createParameter(context);
			}

			// Invoke the method as the task
			return ClassFunction.invokeMethod(instance, this.method, params);
		}
	}

}