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

import net.officefloor.compile.spi.work.source.TaskTypeBuilder;
import net.officefloor.compile.spi.work.source.WorkSource;
import net.officefloor.compile.spi.work.source.WorkTypeBuilder;
import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.build.TaskFactory;
import net.officefloor.frame.api.build.WorkFactory;
import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.execute.TaskContext;
import net.officefloor.plugin.work.clazz.ClassTask;
import net.officefloor.plugin.work.clazz.ClassWork;
import net.officefloor.plugin.work.clazz.ClassWorkSource;
import net.officefloor.plugin.work.clazz.ParameterFactory;
import net.officefloor.plugin.work.clazz.Sequence;

/**
 * {@link WorkSource} implementation to provide the {@link Task} instances for
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
	protected TaskFactory<ClassWork, Indexed, Indexed> createTaskFactory(
			Class<?> clazz, Method method, boolean isStatic,
			ParameterFactory[] parameters) {
		return new SectionTaskFactory(method, isStatic, parameters);
	}

	@Override
	protected TaskTypeBuilder<Indexed, Indexed> addTaskType(Class<?> clazz,
			WorkTypeBuilder<ClassWork> workTypeBuilder, String taskName,
			TaskFactory<ClassWork, Indexed, Indexed> taskFactory,
			Sequence objectSequence, Sequence flowSequence) {

		// Include method as task in type definition
		TaskTypeBuilder<Indexed, Indexed> taskTypeBuilder = workTypeBuilder
				.addTaskType(taskName, taskFactory, null, null);

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
	 * {@link TaskFactory} for overriding {@link ClassWorkSource} behaviour.
	 */
	public static class SectionTaskFactory implements
			TaskFactory<ClassWork, Indexed, Indexed>,
			Task<ClassWork, Indexed, Indexed> {

		/**
		 * {@link Method} for the {@link Task}.
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
		 *            {@link Method} for the {@link Task}.
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
		public Task<ClassWork, Indexed, Indexed> createTask(ClassWork work) {
			return this;
		}

		/*
		 * ===================== Task ===========================
		 */

		@Override
		public Object doTask(TaskContext<ClassWork, Indexed, Indexed> context)
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