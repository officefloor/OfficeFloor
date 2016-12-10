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
package net.officefloor.frame.impl.construct.managedobjectsource;

import net.officefloor.frame.api.build.ManagingOfficeBuilder;
import net.officefloor.frame.api.build.OfficeBuilder;
import net.officefloor.frame.api.build.TaskBuilder;
import net.officefloor.frame.api.build.TaskFactory;
import net.officefloor.frame.api.build.WorkBuilder;
import net.officefloor.frame.api.build.WorkFactory;
import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.impl.construct.office.OfficeBuilderImpl;
import net.officefloor.frame.impl.construct.source.SourceContextImpl;
import net.officefloor.frame.internal.structure.FlowInstigationStrategyEnum;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSourceContext;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectTaskBuilder;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectWorkBuilder;
import net.officefloor.frame.spi.source.SourceContext;
import net.officefloor.frame.spi.source.SourceProperties;

/**
 * Implementation of the {@link ManagedObjectSourceContext}.
 * 
 * @author Daniel Sagenschneider
 */
public class ManagedObjectSourceContextImpl<F extends Enum<F>> extends
		SourceContextImpl implements ManagedObjectSourceContext<F> {

	/**
	 * Name of the {@link Work} to recycle the {@link ManagedObject}.
	 */
	public static final String MANAGED_OBJECT_RECYCLE_WORK_NAME = "#recycle#";

	/**
	 * Name of the {@link ManagedObject}.
	 */
	private final String managedObjectName;

	/**
	 * {@link ManagingOfficeBuilder}.
	 */
	private ManagingOfficeBuilder<F> managingOfficeBuilder;

	/**
	 * {@link OfficeBuilder} for the office using the
	 * {@link ManagedObjectSource}.
	 */
	private OfficeBuilder officeBuilder;

	/**
	 * Name of the {@link Work} to clean up this {@link ManagedObject}.
	 */
	private String recycleWorkName = null;

	/**
	 * Initiate.
	 * 
	 * @param isLoadingType
	 *            Indicates if loading type.
	 * @param managedObjectName
	 *            Name of the {@link ManagedObject}.
	 * @param properties
	 *            Properties.
	 * @param sourceContext
	 *            Delegate {@link SourceContext}.
	 * @param managingOfficeBuilder
	 *            {@link ManagingOfficeBuilder}.
	 * @param officeBuilder
	 *            {@link OfficeBuilder} for the office using the
	 *            {@link ManagedObjectSource}.
	 */
	public ManagedObjectSourceContextImpl(boolean isLoadingType,
			String managedObjectName, SourceProperties properties,
			SourceContext sourceContext,
			ManagingOfficeBuilder<F> managingOfficeBuilder,
			OfficeBuilder officeBuilder) {
		super(isLoadingType, sourceContext, properties);
		this.managedObjectName = managedObjectName;
		this.managingOfficeBuilder = managingOfficeBuilder;
		this.officeBuilder = officeBuilder;
	}

	/**
	 * Indicates that the
	 * {@link ManagedObjectSource#init(ManagedObjectSourceContext)} method has
	 * completed.
	 */
	public void flagInitOver() {
		// Disallow further configuration
		this.managingOfficeBuilder = null;
		this.officeBuilder = null;
	}

	/**
	 * Obtains the name of the {@link Work} to recycle this
	 * {@link ManagedObject}.
	 * 
	 * @return Name of the {@link Work} to recycle this {@link ManagedObject} or
	 *         <code>null</code> if no recycling of this {@link ManagedObject}.
	 */
	public String getRecycleWorkName() {
		return this.recycleWorkName;
	}

	/*
	 * =============== ManagedObjectSourceContext =====================
	 */

	@Override
	public <W extends Work> ManagedObjectWorkBuilder<W> addWork(
			String workName, WorkFactory<W> workFactory) {

		// Obtain the name of work
		String namespacedWorkName = this.getNamespacedName(workName);

		// Create the work
		WorkBuilder<W> workBuilder = this.officeBuilder.addWork(
				namespacedWorkName, workFactory);

		// Return the managed object work builder
		return new ManagedObjectWorkBuilderImpl<W>(workBuilder);
	}

	@Override
	public <W extends Work> ManagedObjectWorkBuilder<W> getRecycleWork(
			WorkFactory<W> workFactory) {

		// Ensure not already created
		if (this.recycleWorkName != null) {
			throw new IllegalStateException(
					"Only one clean up per Managed Object");
		}

		// Name the recycle work
		this.recycleWorkName = this
				.getNamespacedName(MANAGED_OBJECT_RECYCLE_WORK_NAME);

		// Add and return the recycle work
		return this.addWork(MANAGED_OBJECT_RECYCLE_WORK_NAME, workFactory);
	}

	@Override
	public void linkProcess(F key, String workName, String taskName) {
		this.managingOfficeBuilder.linkProcess(key,
				this.getNamespacedName(workName), taskName);
	}

	@Override
	public void linkProcess(int flowIndex, String workName, String taskName) {
		this.managingOfficeBuilder.linkProcess(flowIndex,
				this.getNamespacedName(workName), taskName);
	}

	@Override
	public void addStartupTask(String workName, String taskName) {
		this.officeBuilder.addStartupTask(this.getNamespacedName(workName),
				taskName);
	}

	/**
	 * Obtains the name including the name space for this
	 * {@link ManagedObjectSource}.
	 * 
	 * @param name
	 *            Name to add name space.
	 * @return Name including the name space.
	 */
	private String getNamespacedName(String name) {
		return OfficeBuilderImpl
				.getNamespacedName(this.managedObjectName, name);
	}

	/**
	 * {@link ManagedObjectWorkBuilder} implementation.
	 */
	private class ManagedObjectWorkBuilderImpl<W extends Work> implements
			ManagedObjectWorkBuilder<W> {

		/**
		 * {@link WorkBuilder}.
		 */
		private final WorkBuilder<W> workBuilder;

		/**
		 * Flag indicating if first {@link Task} being added.
		 */
		private boolean isFirstTask = true;

		/**
		 * Initiate.
		 * 
		 * @param workBuilder
		 *            Underlying {@link WorkBuilder}.
		 */
		public ManagedObjectWorkBuilderImpl(WorkBuilder<W> workBuilder) {
			this.workBuilder = workBuilder;
		}

		/*
		 * ============ ManagedObjectWorkBuilder =====================
		 */

		@Override
		public <d extends Enum<d>, f extends Enum<f>> ManagedObjectTaskBuilder<d, f> addTask(
				String taskName, TaskFactory<W, d, f> taskFactory) {

			// Create and initialise the task
			TaskBuilder<W, d, f> taskBuilder = this.workBuilder.addTask(
					taskName, taskFactory);

			// Register as initial task of work if first task
			if (this.isFirstTask) {
				this.workBuilder.setInitialTask(taskName);
				this.isFirstTask = false;
			}

			// Return the task builder for the managed object source
			return new ManagedObjectTaskBuilderImpl<d, f>(taskBuilder);
		}
	}

	/**
	 * {@link ManagedObjectTaskBuilder} implementation.
	 */
	private class ManagedObjectTaskBuilderImpl<d extends Enum<d>, f extends Enum<f>>
			implements ManagedObjectTaskBuilder<d, f> {

		/**
		 * {@link TaskBuilder}.
		 */
		private final TaskBuilder<?, d, f> taskBuilder;

		/**
		 * Initiate.
		 * 
		 * @param taskBuilder
		 *            {@link TaskBuilder}.
		 */
		public ManagedObjectTaskBuilderImpl(TaskBuilder<?, d, f> taskBuilder) {
			this.taskBuilder = taskBuilder;
		}

		/*
		 * ============== ManagedObjectTaskBuilder =====================
		 */

		@Override
		public void linkParameter(d key, Class<?> parameterType) {
			this.taskBuilder.linkParameter(key, parameterType);
		}

		@Override
		public void linkParameter(int index, Class<?> parameterType) {
			this.taskBuilder.linkParameter(index, parameterType);
		}

		@Override
		public void setTeam(String teamName) {
			this.taskBuilder.setTeam(ManagedObjectSourceContextImpl.this
					.getNamespacedName(teamName));
		}

		@Override
		public void setNextTaskInFlow(String taskName, Class<?> argumentType) {
			this.taskBuilder.setNextTaskInFlow(taskName, argumentType);
		}

		@Override
		public void setNextTaskInFlow(String workName, String taskName,
				Class<?> argumentType) {
			this.taskBuilder.setNextTaskInFlow(
					ManagedObjectSourceContextImpl.this
							.getNamespacedName(workName), taskName,
					argumentType);
		}

		@Override
		public void linkFlow(f key, String taskName,
				FlowInstigationStrategyEnum strategy, Class<?> argumentType) {
			this.taskBuilder.linkFlow(key, taskName, strategy, argumentType);
		}

		@Override
		public void linkFlow(int flowIndex, String taskName,
				FlowInstigationStrategyEnum strategy, Class<?> argumentType) {
			this.taskBuilder.linkFlow(flowIndex, taskName, strategy,
					argumentType);
		}

		@Override
		public void linkFlow(f key, String workName, String taskName,
				FlowInstigationStrategyEnum strategy, Class<?> argumentType) {
			this.taskBuilder.linkFlow(key, ManagedObjectSourceContextImpl.this
					.getNamespacedName(workName), taskName, strategy,
					argumentType);
		}

		@Override
		public void linkFlow(int flowIndex, String workName, String taskName,
				FlowInstigationStrategyEnum strategy, Class<?> argumentType) {
			this.taskBuilder.linkFlow(flowIndex,
					ManagedObjectSourceContextImpl.this
							.getNamespacedName(workName), taskName, strategy,
					argumentType);
		}

		@Override
		public void addEscalation(Class<? extends Throwable> typeOfCause,
				String taskName) {
			this.taskBuilder.addEscalation(typeOfCause, taskName);
		}

		@Override
		public void addEscalation(Class<? extends Throwable> typeOfCause,
				String workName, String taskName) {
			this.taskBuilder.addEscalation(typeOfCause,
					ManagedObjectSourceContextImpl.this
							.getNamespacedName(workName), taskName);
		}
	}

}