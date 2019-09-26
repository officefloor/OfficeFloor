/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2019 Daniel Sagenschneider
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
package net.officefloor.polyglot.scala;

import java.lang.reflect.Method;

import net.officefloor.compile.spi.managedfunction.source.FunctionNamespaceBuilder;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionSource;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionSourceContext;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionTypeBuilder;
import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.function.ManagedFunctionContext;
import net.officefloor.frame.api.function.ManagedFunctionFactory;
import net.officefloor.frame.api.function.StaticManagedFunction;
import net.officefloor.plugin.managedfunction.clazz.ClassManagedFunctionSource;
import net.officefloor.plugin.managedfunction.method.MethodFunction;
import net.officefloor.plugin.managedfunction.method.MethodManagedFunctionBuilder;
import net.officefloor.plugin.managedfunction.method.MethodObjectInstanceManufacturer;
import net.officefloor.plugin.managedfunction.method.MethodParameterFactory;
import net.officefloor.plugin.section.clazz.SectionClassManagedFunctionSource;

/**
 * Scala {@link ManagedFunctionSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class ScalaManagedFunctionSource extends SectionClassManagedFunctionSource {

	/*
	 * =================== ClassManagedFunctionSource ==========================
	 */

	@Override
	protected MethodObjectInstanceManufacturer createMethodObjectInstanceManufacturer(Class<?> clazz) throws Exception {

		// Return the Scala module
		Object module = clazz.getField("MODULE$").get(null);

		// Return manufacturer for module
		return () -> () -> module;
	}

	@Override
	protected MethodManagedFunctionBuilder createMethodManagedFunctionBuilder(FunctionNamespaceBuilder namespaceBuilder,
																			  ManagedFunctionSourceContext context) throws Exception {
		return new ScalaMethodManagedFunctionBuilder();
	}

	/**
	 * {@link MethodManagedFunctionBuilder} for the
	 * {@link ScalaFunctionSectionSource}.
	 */
	protected class ScalaMethodManagedFunctionBuilder extends SectionMethodManagedFunctionBuilder {

		@Override
		protected ManagedFunctionFactory<Indexed, Indexed> createManagedFunctionFactory(
				MethodManagedFunctionFactoryContext context) throws Exception {

			// Obtain the module
			Object module = context.getMethodObjectInstanceFactory().createInstance();

			// Return the Scala function factory
			return new ScalaManagedFunctionFactory(module, context.getMethod(), context.getParameters());
		}

		@Override
		protected ManagedFunctionTypeBuilder<Indexed, Indexed> addManagedFunctionType(
				MethodManagedFunctionTypeContext context) {
			return context.getNamespaceBuilder().addManagedFunctionType(context.getFunctionName(),
					context.getFunctionFactory(), Indexed.class, Indexed.class);
		}
	}

	/**
	 * {@link ManagedFunctionFactory} for overriding
	 * {@link ClassManagedFunctionSource} behaviour.
	 */
	public static class ScalaManagedFunctionFactory extends StaticManagedFunction<Indexed, Indexed> {

		/**
		 * Scala module.
		 */
		private final Object module;

		/**
		 * {@link Method} for the {@link ManagedFunction}.
		 */
		private final Method method;

		/**
		 * {@link MethodParameterFactory} instances for the parameters of the
		 * {@link Method}.
		 */
		private final MethodParameterFactory[] parameters;

		/**
		 * Initiate.
		 * 
		 * @param module     Scala module.
		 * @param method     {@link Method} for the {@link ManagedFunction}.
		 * @param parameters {@link MethodParameterFactory} instances for the
		 *                   parameters of the {@link Method}.
		 */
		public ScalaManagedFunctionFactory(Object module, Method method, MethodParameterFactory[] parameters) {
			this.module = module;
			this.method = method;
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
		 * Obtains the {@link MethodParameterFactory} instances.
		 * 
		 * @return {@link MethodParameterFactory} instances.
		 */
		public MethodParameterFactory[] getParameterFactories() {
			return this.parameters;
		}

		/*
		 * ===================== ManagedFunction ===========================
		 */

		@Override
		public Object execute(ManagedFunctionContext<Indexed, Indexed> context) throws Throwable {

			// Create the listing of parameters
			Object[] params = new Object[this.parameters.length];
			for (int i = 0; i < params.length; i++) {
				params[i] = this.parameters[i].createParameter(context);
			}

			// Invoke the method as the task
			return MethodFunction.invokeMethod(this.module, this.method, params);
		}
	}

}