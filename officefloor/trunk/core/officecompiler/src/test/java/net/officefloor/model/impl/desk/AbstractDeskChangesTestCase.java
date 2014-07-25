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
package net.officefloor.model.impl.desk;

import net.officefloor.compile.impl.work.WorkTypeImpl;
import net.officefloor.compile.spi.work.source.TaskEscalationTypeBuilder;
import net.officefloor.compile.spi.work.source.TaskFlowTypeBuilder;
import net.officefloor.compile.spi.work.source.TaskObjectTypeBuilder;
import net.officefloor.compile.spi.work.source.TaskTypeBuilder;
import net.officefloor.compile.spi.work.source.WorkTypeBuilder;
import net.officefloor.compile.work.TaskEscalationType;
import net.officefloor.compile.work.TaskFlowType;
import net.officefloor.compile.work.TaskObjectType;
import net.officefloor.compile.work.TaskType;
import net.officefloor.compile.work.WorkType;
import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.model.desk.DeskModel;
import net.officefloor.model.desk.DeskChanges;
import net.officefloor.model.impl.repository.ModelRepositoryImpl;
import net.officefloor.model.repository.ConfigurationItem;
import net.officefloor.model.test.changes.AbstractChangesTestCase;

/**
 * Provides abstract functionality for the {@link DeskChanges}.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class AbstractDeskChangesTestCase extends
		AbstractChangesTestCase<DeskModel, DeskChanges> {

	/**
	 * Initiate.
	 */
	public AbstractDeskChangesTestCase() {
	}

	/**
	 * Initiate.
	 * 
	 * @param isSpecificSetupFilePerTest
	 *            Flags if there is a specific setup file per test.
	 */
	public AbstractDeskChangesTestCase(boolean isSpecificSetupFilePerTest) {
		super(isSpecificSetupFilePerTest);
	}

	/*
	 * =================== AbstractOperationsTestCase ========================
	 */

	@Override
	protected DeskModel retrieveModel(ConfigurationItem configurationItem)
			throws Exception {
		return new DeskRepositoryImpl(new ModelRepositoryImpl())
				.retrieveDesk(configurationItem);
	}

	@Override
	protected DeskChanges createModelOperations(DeskModel model) {
		return new DeskChangesImpl(model);
	}

	@Override
	protected String getModelFileExtension() {
		return ".desk.xml";
	}

	/**
	 * Creates a {@link WorkType}.
	 * 
	 * @param constructor
	 *            {@link WorkTypeConstructor} to construct the {@link WorkType}.
	 * @return {@link WorkType}.
	 */
	protected WorkType<Work> constructWorkType(WorkTypeConstructor constructor) {

		// Create the work type builder
		WorkTypeImpl<Work> workTypeBuilder = new WorkTypeImpl<Work>();

		// Build the work type via the constructor
		WorkTypeContext context = new WorkTypeContextImpl(workTypeBuilder);
		constructor.construct(context);

		// Return the work type
		return workTypeBuilder;
	}

	/**
	 * {@link WorkTypeConstructor} to construct the {@link WorkType}.
	 */
	protected interface WorkTypeConstructor {

		/**
		 * Constructs the {@link WorkType}.
		 * 
		 * @param context
		 *            {@link WorkTypeContext}.
		 */
		void construct(WorkTypeContext context);
	}

	/**
	 * Context to construct the {@link WorkType}.
	 */
	protected interface WorkTypeContext {

		/**
		 * Adds a {@link TaskType}.
		 * 
		 * @param taskName
		 *            Name of the {@link Task}.
		 * @return {@link TaskTypeConstructor} to provide simplified
		 *         {@link TaskType} construction.
		 */
		TaskTypeConstructor addTask(String taskName);

		/**
		 * Adds a {@link TaskTypeBuilder}.
		 * 
		 * @param taskName
		 *            Name of the {@link Task}.
		 * @param dependencyKeys
		 *            Dependency keys {@link Enum}.
		 * @param flowKeys
		 *            Flow keys {@link Enum}.
		 * @return {@link TaskTypeBuilder}.
		 */
		<D extends Enum<D>, F extends Enum<F>> TaskTypeBuilder<D, F> addTask(
				String taskName, Class<D> dependencyKeys, Class<F> flowKeys);
	}

	/**
	 * Provides simplified construction of a {@link TaskType}.
	 */
	protected interface TaskTypeConstructor {

		/**
		 * Adds a {@link TaskObjectType}.
		 * 
		 * @param objectType
		 *            {@link Object} type.
		 * @param key
		 *            Key identifying the {@link TaskObjectType}.
		 * @return {@link TaskObjectTypeBuilder} for the added
		 *         {@link TaskObjectType}.
		 */
		TaskObjectTypeBuilder<?> addObject(Class<?> objectType, Enum<?> key);

		/**
		 * Adds a {@link TaskFlowType}.
		 * 
		 * @param argumentType
		 *            Argument type.
		 * @param key
		 *            Key identifying the {@link TaskFlowType}.
		 * @return {@link TaskFlowTypeBuilder} for the added
		 *         {@link TaskObjectType}.
		 */
		TaskFlowTypeBuilder<?> addFlow(Class<?> argumentType, Enum<?> key);

		/**
		 * Adds a {@link TaskEscalationType}.
		 * 
		 * @param escalationType
		 *            Escalation type.
		 * @return {@link TaskEscalationTypeBuilder} for the added
		 *         {@link TaskEscalationType}.
		 */
		TaskEscalationTypeBuilder addEscalation(
				Class<? extends Throwable> escalationType);

		/**
		 * Obtains the underlying {@link TaskTypeBuilder}.
		 * 
		 * @return Underlying {@link TaskTypeBuilder}.
		 */
		TaskTypeBuilder<?, ?> getBuilder();
	}

	/**
	 * {@link WorkTypeContext} implementation.
	 */
	private class WorkTypeContextImpl implements WorkTypeContext {

		/**
		 * {@link WorkTypeBuilder}.
		 */
		private final WorkTypeBuilder<?> workTypeBuilder;

		/**
		 * Initiate.
		 * 
		 * @param workTypeBuilder
		 *            {@link WorkTypeBuilder}.
		 */
		public WorkTypeContextImpl(WorkTypeBuilder<?> workTypeBuilder) {
			this.workTypeBuilder = workTypeBuilder;
		}

		/*
		 * ================== WorkTypeContext ============================
		 */

		@Override
		@SuppressWarnings("rawtypes")
		public TaskTypeConstructor addTask(String taskName) {
			// Add the task
			TaskTypeBuilder taskTypeBuilder = this.workTypeBuilder
					.addTaskType(taskName, null, (Class<Indexed>) null,
							(Class<Indexed>) null);

			// Return the task type constructor for the task type builder
			return new TaskTypeConstructorImpl(taskTypeBuilder);
		}

		@Override
		public <D extends Enum<D>, F extends Enum<F>> TaskTypeBuilder<D, F> addTask(
				String taskName, Class<D> dependencyKeys, Class<F> flowKeys) {
			return this.workTypeBuilder.addTaskType(taskName, null,
					dependencyKeys, flowKeys);
		}
	}

	/**
	 * {@link TaskTypeConstructor} implementation.
	 */
	private class TaskTypeConstructorImpl implements TaskTypeConstructor {

		/**
		 * {@link TaskTypeBuilder}.
		 */
		private final TaskTypeBuilder<?, ?> taskTypeBuilder;

		/**
		 * Initiate.
		 * 
		 * @param taskTypeBuilder
		 *            {@link TaskTypeBuilder}.
		 */
		public TaskTypeConstructorImpl(TaskTypeBuilder<?, ?> taskTypeBuilder) {
			this.taskTypeBuilder = taskTypeBuilder;
		}

		/*
		 * ================= TaskTypeConstructor ===========================
		 */

		@Override
		@SuppressWarnings({ "unchecked", "rawtypes" })
		public TaskObjectTypeBuilder<?> addObject(Class<?> objectType,
				Enum<?> key) {
			TaskObjectTypeBuilder object = this.taskTypeBuilder
					.addObject(objectType);
			if (key != null) {
				object.setKey(key);
			}
			return object;
		}

		@Override
		@SuppressWarnings({ "unchecked", "rawtypes" })
		public TaskFlowTypeBuilder<?> addFlow(Class<?> argumentType, Enum<?> key) {
			TaskFlowTypeBuilder flow = this.taskTypeBuilder.addFlow();
			flow.setArgumentType(argumentType);
			if (key != null) {
				flow.setKey(key);
			}
			return flow;
		}

		@Override
		public TaskEscalationTypeBuilder addEscalation(
				Class<? extends Throwable> escalationType) {
			TaskEscalationTypeBuilder escalation = this.taskTypeBuilder
					.addEscalation(escalationType);
			return escalation;
		}

		@Override
		public TaskTypeBuilder<?, ?> getBuilder() {
			return this.taskTypeBuilder;
		}
	}

	/**
	 * Constructor to construct the {@link TaskType}.
	 */
	protected interface TaskConstructor {

		/**
		 * Constructs the {@link TaskType}.
		 * 
		 * @param task
		 *            {@link TaskType}.
		 */
		void construct(TaskTypeConstructor task);
	}

	/**
	 * Constructs the {@link TaskType}.
	 * 
	 * @param taskName
	 *            Name of the {@link TaskType}.
	 * @param constructor
	 *            {@link TaskConstructor}.
	 * @return {@link TaskType}.
	 */
	protected TaskType<?, ?, ?> constructTaskType(final String taskName,
			final TaskConstructor constructor) {

		// Construct the work
		WorkType<?> workType = this
				.constructWorkType(new WorkTypeConstructor() {
					@Override
					public void construct(WorkTypeContext context) {
						// Construct the task
						TaskTypeConstructor task = context.addTask(taskName);
						if (constructor != null) {
							constructor.construct(task);
						}
					}
				});

		// Return the task from the work
		return workType.getTaskTypes()[0];
	}

}