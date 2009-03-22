/*
 *  Office Floor, Application Server
 *  Copyright (C) 2006 Daniel Sagenschneider
 *
 *  This program is free software; you can redistribute it and/or modify it under the terms 
 *  of the GNU General Public License as published by the Free Software Foundation; either 
 *  version 2 of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with this program; 
 *  if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, 
 *  MA 02111-1307 USA
 */
package net.officefloor.frame.impl.construct.managedobjectsource;

import java.util.Properties;

import net.officefloor.frame.api.build.ManagingOfficeBuilder;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.build.OfficeBuilder;
import net.officefloor.frame.api.build.TaskBuilder;
import net.officefloor.frame.api.build.TaskFactory;
import net.officefloor.frame.api.build.WorkBuilder;
import net.officefloor.frame.api.build.WorkFactory;
import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.impl.construct.office.OfficeBuilderImpl;
import net.officefloor.frame.internal.structure.FlowInstigationStrategyEnum;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSourceContext;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSourceUnknownPropertyError;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectTaskBuilder;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectWorkBuilder;
import net.officefloor.frame.spi.managedobject.source.ResourceLocator;

/**
 * Implementation of the {@link ManagedObjectSourceContext}.
 * 
 * @author Daniel
 */
public class ManagedObjectSourceContextImpl<F extends Enum<F>> implements
		ManagedObjectSourceContext<F> {

	/**
	 * Name of the {@link Work} to clean up the {@link ManagedObject}.
	 */
	public static final String MANAGED_OBJECT_CLEAN_UP_WORK_NAME = "#managed.object.cleanup#";

	/**
	 * Name of the {@link ManagedObject}.
	 */
	private final String managedObjectName;

	/**
	 * Properties.
	 */
	private final Properties properties;

	/**
	 * Resource locator.
	 */
	private final ResourceLocator resourceLocator;

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
	 * @param managedObjectName
	 *            Name of the {@link ManagedObject}.
	 * @param properties
	 *            Properties.
	 * @param resourceLocator
	 *            {@link ResourceLocator}.
	 * @param managingOfficeBuilder
	 *            {@link ManagingOfficeBuilder}.
	 * @param officeBuilder
	 *            {@link OfficeBuilder} for the office using the
	 *            {@link ManagedObjectSource}.
	 */
	public ManagedObjectSourceContextImpl(String managedObjectName,
			Properties properties, ResourceLocator resourceLocator,
			ManagingOfficeBuilder<F> managingOfficeBuilder,
			OfficeBuilder officeBuilder) {
		this.managedObjectName = managedObjectName;
		this.properties = properties;
		this.resourceLocator = resourceLocator;
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
	public Properties getProperties() {
		return this.properties;
	}

	@Override
	public String getProperty(String name)
			throws ManagedObjectSourceUnknownPropertyError {
		// Obtain the value
		String value = this.getProperty(name, null);

		// Ensure have a value
		if (value == null) {
			throw new ManagedObjectSourceUnknownPropertyError(
					"Unknown property '" + name + "'", name);
		}

		// Return the value
		return value;
	}

	@Override
	public String getProperty(String name, String defaultValue) {
		// Obtain the value
		String value = this.properties.getProperty(name);

		// Default value if not specified
		if (value == null) {
			value = defaultValue;
		}

		// Return the value
		return value;
	}

	@Override
	public ResourceLocator getResourceLocator() {
		return this.resourceLocator;
	}

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
				.getNamespacedName(MANAGED_OBJECT_CLEAN_UP_WORK_NAME);

		// Add and return the recycle work
		return this.addWork(MANAGED_OBJECT_CLEAN_UP_WORK_NAME, workFactory);
	}

	@Override
	public void linkProcess(F key, String workName, String taskName) {
		this.managingOfficeBuilder
				.linkProcess(key, ManagedObjectSourceContextImpl.this
						.getNamespacedName(workName), taskName);
	}

	@Override
	public void linkProcess(int flowIndex, String workName, String taskName) {
		this.managingOfficeBuilder
				.linkProcess(flowIndex, ManagedObjectSourceContextImpl.this
						.getNamespacedName(workName), taskName);
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
	protected String getNamespacedName(String name) {
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
		public <P, f extends Enum<f>> ManagedObjectTaskBuilder<f> addTask(
				String taskName, TaskFactory<P, W, None, f> taskFactory) {

			// Create and initialise the task
			TaskBuilder<P, W, None, f> taskBuilder = this.workBuilder.addTask(
					taskName, taskFactory);

			// Register as initial task of work if first task
			if (this.isFirstTask) {
				this.workBuilder.setInitialTask(taskName);
				this.isFirstTask = false;
			}

			// Return the task builder for the managed object source
			return new ManagedObjectTaskBuilderImpl<f>(taskBuilder);
		}
	}

	/**
	 * {@link ManagedObjectTaskBuilder} implementation.
	 */
	public class ManagedObjectTaskBuilderImpl<f extends Enum<f>> implements
			ManagedObjectTaskBuilder<f> {

		/**
		 * {@link TaskBuilder}.
		 */
		private final TaskBuilder<?, ?, None, f> taskBuilder;

		/**
		 * Initiate.
		 * 
		 * @param taskBuilder
		 *            {@link TaskBuilder}.
		 */
		public ManagedObjectTaskBuilderImpl(
				TaskBuilder<?, ?, None, f> taskBuilder) {
			this.taskBuilder = taskBuilder;
		}

		/*
		 * ============== ManagedObjectTaskBuilder =====================
		 */

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