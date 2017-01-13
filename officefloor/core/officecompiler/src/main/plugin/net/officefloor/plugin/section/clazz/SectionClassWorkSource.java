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

import java.lang.reflect.Method;
import java.util.List;

import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionSource;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionTypeBuilder;
import net.officefloor.compile.spi.managedfunction.source.FunctionNamespaceBuilder;
import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.build.WorkFactory;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.function.ManagedFunctionContext;
import net.officefloor.frame.api.function.ManagedFunctionFactory;
import net.officefloor.plugin.work.clazz.ClassTask;
import net.officefloor.plugin.work.clazz.ClassWork;
import net.officefloor.plugin.work.clazz.ClassWorkSource;
import net.officefloor.plugin.work.clazz.ParameterFactory;
import net.officefloor.plugin.work.clazz.Sequence;

/**
 * {@link ManagedFunctionSource} implementation to provide the {@link ManagedFunction} instances for
 * the {@link ClassSectionSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class SectionClassWorkSource extends ClassWorkSource {

	/*
	 * =================== ClassWorkSource ==========================
	 */

	@Override
	protected void loadParameterManufacturers(
			List<ParameterManufacturer> manufacturers) {
		manufacturers
				.add(new FlowInterfaceParameterManufacturer<SectionInterface>(
						SectionInterface.class));
	}

	@Override
	protected WorkFactory<ClassWork> createWorkFactory(Class<?> clazz) {
		return new SectionWorkFactory();
	}

	@Override
	protected ManagedFunctionFactory<ClassWork, Indexed, Indexed> createTaskFactory(
			Class<?> clazz, Method method, boolean isStatic,
			ParameterFactory[] parameters) {
		return new SectionTaskFactory(method, isStatic, parameters);
	}

	@Override
	protected ManagedFunctionTypeBuilder<Indexed, Indexed> addTaskType(Class<?> clazz,
			FunctionNamespaceBuilder<ClassWork> workTypeBuilder, String taskName,
			ManagedFunctionFactory<ClassWork, Indexed, Indexed> taskFactory,
			Sequence objectSequence, Sequence flowSequence) {

		// Include method as task in type definition
		ManagedFunctionTypeBuilder<Indexed, Indexed> taskTypeBuilder = workTypeBuilder
				.addManagedFunctionType(taskName, taskFactory, null, null);

		// Add the section object always as first dependency
		taskTypeBuilder.addObject(clazz).setLabel("OBJECT");
		objectSequence.nextIndex(); // index for section object

		// Return the task type builder
		return taskTypeBuilder;
	}

	/**
	 * <p>
	 * {@link WorkFactory} for overriding {@link ClassWorkSource} behaviour.
	 * <p>
	 * The object is a dependency rather than being instantiated.
	 */
	private static class SectionWorkFactory extends ClassWork implements
			WorkFactory<ClassWork> {

		/**
		 * Initiate.
		 */
		public SectionWorkFactory() {
			super(null);
		}

		@Override
		public ClassWork createWork() {
			return this;
		}
	}

	/**
	 * {@link ManagedFunctionFactory} for overriding {@link ClassWorkSource} behaviour.
	 */
	public static class SectionTaskFactory implements
			ManagedFunctionFactory<ClassWork, Indexed, Indexed>,
			ManagedFunction<ClassWork, Indexed, Indexed> {

		/**
		 * {@link Method} for the {@link ManagedFunction}.
		 */
		private final Method method;

		/**
		 * Indicates if the {@link Method} is static.
		 */
		private final boolean isStatic;

		/**
		 * {@link ParameterFactory} instances for the parameters of the
		 * {@link Method}.
		 */
		private final ParameterFactory[] parameters;

		/**
		 * Initiate.
		 * 
		 * @param method
		 *            {@link Method} for the {@link ManagedFunction}.
		 * @param isStatic
		 *            Indicates if the {@link Method} is static.
		 * @param parameters
		 *            {@link ParameterFactory} instances for the parameters of
		 *            the {@link Method}.
		 */
		public SectionTaskFactory(Method method, boolean isStatic,
				ParameterFactory[] parameters) {
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
		 * Obtains the {@link ParameterFactory} instances.
		 * 
		 * @return {@link ParameterFactory} instances.
		 */
		public ParameterFactory[] getParameterFactories() {
			return this.parameters;
		}

		/*
		 * ================= TaskFactory ========================
		 */

		@Override
		public ManagedFunction<ClassWork, Indexed, Indexed> createManagedFunction(ClassWork work) {
			return this;
		}

		/*
		 * ===================== Task ===========================
		 */

		@Override
		public Object execute(ManagedFunctionContext<ClassWork, Indexed, Indexed> context)
				throws Throwable {

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
			return ClassTask.invokeMethod(instance, this.method, params);
		}
	}

}