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

import net.officefloor.compile.managedfunction.ManagedFunctionEscalationType;
import net.officefloor.compile.managedfunction.ManagedFunctionFlowType;
import net.officefloor.compile.managedfunction.ManagedFunctionObjectType;
import net.officefloor.compile.managedfunction.ManagedFunctionType;
import net.officefloor.compile.impl.managedfunction.FunctionNamespaceTypeImpl;
import net.officefloor.compile.managedfunction.FunctionNamespaceType;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionEscalationTypeBuilder;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionFlowTypeBuilder;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionObjectTypeBuilder;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionTypeBuilder;
import net.officefloor.compile.spi.managedfunction.source.FunctionNamespaceBuilder;
import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.function.Work;
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
	 * Creates a {@link FunctionNamespaceType}.
	 * 
	 * @param constructor
	 *            {@link WorkTypeConstructor} to construct the {@link FunctionNamespaceType}.
	 * @return {@link FunctionNamespaceType}.
	 */
	protected FunctionNamespaceType<Work> constructWorkType(WorkTypeConstructor constructor) {

		// Create the work type builder
		FunctionNamespaceTypeImpl<Work> workTypeBuilder = new FunctionNamespaceTypeImpl<Work>();

		// Build the work type via the constructor
		WorkTypeContext context = new WorkTypeContextImpl(workTypeBuilder);
		constructor.construct(context);

		// Return the work type
		return workTypeBuilder;
	}

	/**
	 * {@link WorkTypeConstructor} to construct the {@link FunctionNamespaceType}.
	 */
	protected interface WorkTypeConstructor {

		/**
		 * Constructs the {@link FunctionNamespaceType}.
		 * 
		 * @param context
		 *            {@link WorkTypeContext}.
		 */
		void construct(WorkTypeContext context);
	}

	/**
	 * Context to construct the {@link FunctionNamespaceType}.
	 */
	protected interface WorkTypeContext {

		/**
		 * Adds a {@link ManagedFunctionType}.
		 * 
		 * @param taskName
		 *            Name of the {@link ManagedFunction}.
		 * @return {@link TaskTypeConstructor} to provide simplified
		 *         {@link ManagedFunctionType} construction.
		 */
		TaskTypeConstructor addTask(String taskName);

		/**
		 * Adds a {@link ManagedFunctionTypeBuilder}.
		 * 
		 * @param taskName
		 *            Name of the {@link ManagedFunction}.
		 * @param dependencyKeys
		 *            Dependency keys {@link Enum}.
		 * @param flowKeys
		 *            Flow keys {@link Enum}.
		 * @return {@link ManagedFunctionTypeBuilder}.
		 */
		<D extends Enum<D>, F extends Enum<F>> ManagedFunctionTypeBuilder<D, F> addTask(
				String taskName, Class<D> dependencyKeys, Class<F> flowKeys);
	}

	/**
	 * Provides simplified construction of a {@link ManagedFunctionType}.
	 */
	protected interface TaskTypeConstructor {

		/**
		 * Adds a {@link ManagedFunctionObjectType}.
		 * 
		 * @param objectType
		 *            {@link Object} type.
		 * @param key
		 *            Key identifying the {@link ManagedFunctionObjectType}.
		 * @return {@link ManagedFunctionObjectTypeBuilder} for the added
		 *         {@link ManagedFunctionObjectType}.
		 */
		ManagedFunctionObjectTypeBuilder<?> addObject(Class<?> objectType, Enum<?> key);

		/**
		 * Adds a {@link ManagedFunctionFlowType}.
		 * 
		 * @param argumentType
		 *            Argument type.
		 * @param key
		 *            Key identifying the {@link ManagedFunctionFlowType}.
		 * @return {@link ManagedFunctionFlowTypeBuilder} for the added
		 *         {@link ManagedFunctionObjectType}.
		 */
		ManagedFunctionFlowTypeBuilder<?> addFlow(Class<?> argumentType, Enum<?> key);

		/**
		 * Adds a {@link ManagedFunctionEscalationType}.
		 * 
		 * @param escalationType
		 *            Escalation type.
		 * @return {@link ManagedFunctionEscalationTypeBuilder} for the added
		 *         {@link ManagedFunctionEscalationType}.
		 */
		ManagedFunctionEscalationTypeBuilder addEscalation(
				Class<? extends Throwable> escalationType);

		/**
		 * Obtains the underlying {@link ManagedFunctionTypeBuilder}.
		 * 
		 * @return Underlying {@link ManagedFunctionTypeBuilder}.
		 */
		ManagedFunctionTypeBuilder<?, ?> getBuilder();
	}

	/**
	 * {@link WorkTypeContext} implementation.
	 */
	private class WorkTypeContextImpl implements WorkTypeContext {

		/**
		 * {@link FunctionNamespaceBuilder}.
		 */
		private final FunctionNamespaceBuilder<?> workTypeBuilder;

		/**
		 * Initiate.
		 * 
		 * @param workTypeBuilder
		 *            {@link FunctionNamespaceBuilder}.
		 */
		public WorkTypeContextImpl(FunctionNamespaceBuilder<?> workTypeBuilder) {
			this.workTypeBuilder = workTypeBuilder;
		}

		/*
		 * ================== WorkTypeContext ============================
		 */

		@Override
		@SuppressWarnings("rawtypes")
		public TaskTypeConstructor addTask(String taskName) {
			// Add the task
			ManagedFunctionTypeBuilder taskTypeBuilder = this.workTypeBuilder
					.addManagedFunctionType(taskName, null, (Class<Indexed>) null,
							(Class<Indexed>) null);

			// Return the task type constructor for the task type builder
			return new TaskTypeConstructorImpl(taskTypeBuilder);
		}

		@Override
		public <D extends Enum<D>, F extends Enum<F>> ManagedFunctionTypeBuilder<D, F> addTask(
				String taskName, Class<D> dependencyKeys, Class<F> flowKeys) {
			return this.workTypeBuilder.addManagedFunctionType(taskName, null,
					dependencyKeys, flowKeys);
		}
	}

	/**
	 * {@link TaskTypeConstructor} implementation.
	 */
	private class TaskTypeConstructorImpl implements TaskTypeConstructor {

		/**
		 * {@link ManagedFunctionTypeBuilder}.
		 */
		private final ManagedFunctionTypeBuilder<?, ?> taskTypeBuilder;

		/**
		 * Initiate.
		 * 
		 * @param taskTypeBuilder
		 *            {@link ManagedFunctionTypeBuilder}.
		 */
		public TaskTypeConstructorImpl(ManagedFunctionTypeBuilder<?, ?> taskTypeBuilder) {
			this.taskTypeBuilder = taskTypeBuilder;
		}

		/*
		 * ================= TaskTypeConstructor ===========================
		 */

		@Override
		@SuppressWarnings({ "unchecked", "rawtypes" })
		public ManagedFunctionObjectTypeBuilder<?> addObject(Class<?> objectType,
				Enum<?> key) {
			ManagedFunctionObjectTypeBuilder object = this.taskTypeBuilder
					.addObject(objectType);
			if (key != null) {
				object.setKey(key);
			}
			return object;
		}

		@Override
		@SuppressWarnings({ "unchecked", "rawtypes" })
		public ManagedFunctionFlowTypeBuilder<?> addFlow(Class<?> argumentType, Enum<?> key) {
			ManagedFunctionFlowTypeBuilder flow = this.taskTypeBuilder.addFlow();
			flow.setArgumentType(argumentType);
			if (key != null) {
				flow.setKey(key);
			}
			return flow;
		}

		@Override
		public ManagedFunctionEscalationTypeBuilder addEscalation(
				Class<? extends Throwable> escalationType) {
			ManagedFunctionEscalationTypeBuilder escalation = this.taskTypeBuilder
					.addEscalation(escalationType);
			return escalation;
		}

		@Override
		public ManagedFunctionTypeBuilder<?, ?> getBuilder() {
			return this.taskTypeBuilder;
		}
	}

	/**
	 * Constructor to construct the {@link ManagedFunctionType}.
	 */
	protected interface TaskConstructor {

		/**
		 * Constructs the {@link ManagedFunctionType}.
		 * 
		 * @param task
		 *            {@link ManagedFunctionType}.
		 */
		void construct(TaskTypeConstructor task);
	}

	/**
	 * Constructs the {@link ManagedFunctionType}.
	 * 
	 * @param taskName
	 *            Name of the {@link ManagedFunctionType}.
	 * @param constructor
	 *            {@link TaskConstructor}.
	 * @return {@link ManagedFunctionType}.
	 */
	protected ManagedFunctionType<?, ?, ?> constructTaskType(final String taskName,
			final TaskConstructor constructor) {

		// Construct the work
		FunctionNamespaceType<?> workType = this
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
		return workType.getManagedFunctionTypes()[0];
	}

}