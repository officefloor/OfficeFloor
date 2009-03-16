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

import net.officefloor.frame.api.build.HandlerBuilder;
import net.officefloor.frame.api.build.HandlerFactory;
import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.build.ManagedObjectBuilder;
import net.officefloor.frame.api.build.ManagedObjectHandlerBuilder;
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
public class ManagedObjectSourceContextImpl<H extends Enum<H>> implements
		ManagedObjectSourceContext<H> {

	/**
	 * Name of the {@link Work} to clean up the {@link ManagedObject}.
	 */
	public static final String MANAGED_OBJECT_CLEAN_UP_WORK_NAME = "#managed.object.cleanup#";

	/**
	 * Name of the {@link ManagedObject}.
	 */
	protected final String managedObjectName;

	/**
	 * Properties.
	 */
	protected final Properties properties;

	/**
	 * Resource locator.
	 */
	protected final ResourceLocator resourceLocator;

	/**
	 * {@link ManagedObjectBuilder}.
	 */
	protected ManagedObjectBuilder<H> managedObjectBuilder;

	/**
	 * {@link OfficeBuilder} for the office using the
	 * {@link ManagedObjectSource}.
	 */
	protected OfficeBuilder officeBuilder;

	/**
	 * Name of the {@link Work} to clean up this {@link ManagedObject}.
	 */
	protected String recycleWorkName = null;

	/**
	 * Initiate.
	 * 
	 * @param managedObjectName
	 *            Name of the {@link ManagedObject}.
	 * @param properties
	 *            Properties.
	 * @param resourceLocator
	 *            {@link ResourceLocator}.
	 * @param managedObjectBuilder
	 *            {@link ManagedObjectBuilder}.
	 * @param officeBuilder
	 *            {@link OfficeBuilder} for the office using the
	 *            {@link ManagedObjectSource}.
	 */
	public ManagedObjectSourceContextImpl(String managedObjectName,
			Properties properties, ResourceLocator resourceLocator,
			ManagedObjectBuilder<H> managedObjectBuilder,
			OfficeBuilder officeBuilder) {
		this.managedObjectName = managedObjectName;
		this.properties = properties;
		this.resourceLocator = resourceLocator;
		this.managedObjectBuilder = managedObjectBuilder;
		this.officeBuilder = officeBuilder;
	}

	/**
	 * Indicates that the
	 * {@link ManagedObjectSource#init(ManagedObjectSourceContext)} method has
	 * completed.
	 */
	public void flagInitOver() {
		// Disallow further configuration
		this.managedObjectBuilder = null;
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
	public ManagedObjectHandlerBuilder<H> getHandlerBuilder() {
		// Return the wrapped managed object hander builder
		ManagedObjectHandlerBuilder<H> handlerBuilder = this.managedObjectBuilder
				.getManagedObjectHandlerBuilder();
		ManagedObjectHandlerBuilderWrapper wrapper = new ManagedObjectHandlerBuilderWrapper(
				handlerBuilder);
		return wrapper;
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
	public void addStartupTask(String workName, String taskName) {
		this.officeBuilder.addStartupTask(this.getNamespacedName(workName),
				this.getNamespacedName(taskName));
	}

	/**
	 * Obtains the name including the namespace for this
	 * {@link ManagedObjectSource}.
	 * 
	 * @param name
	 *            Name to add namespace.
	 * @return Name including the namespace.
	 */
	protected String getNamespacedName(String name) {
		return OfficeBuilderImpl
				.getNamespacedName(this.managedObjectName, name);
	}

	/**
	 * Wrapper for a delegate {@link ManagedObjectHandlerBuilder} that applies
	 * the {@link ManagedObjectSource} instance's namespace.
	 */
	protected class ManagedObjectHandlerBuilderWrapper implements
			ManagedObjectHandlerBuilder<H> {

		/**
		 * Delegate {@link ManagedObjectHandlerBuilder}.
		 */
		private final ManagedObjectHandlerBuilder<H> delegate;

		/**
		 * Initiate.
		 * 
		 * @param delegate
		 *            Delegate {@link ManagedObjectHandlerBuilder}.
		 */
		public ManagedObjectHandlerBuilderWrapper(
				ManagedObjectHandlerBuilder<H> delegate) {
			this.delegate = delegate;
		}

		/*
		 * ============= ManagedObjectHandlerBuilder ==================
		 */

		@Override
		public <F extends Enum<F>> HandlerBuilder<F> registerHandler(
				H handlerKey, Class<F> processListingEnum) {
			return new HandlerBuilderWrapper<F>(this.delegate.registerHandler(
					handlerKey, processListingEnum));
		}

		@Override
		public HandlerBuilder<Indexed> registerHandler(H handlerKey) {
			return new HandlerBuilderWrapper<Indexed>(this.delegate
					.registerHandler(handlerKey));
		}
	}

	/**
	 * Wrapper for a delegate {@link HandlerBuilder} that applies the
	 * {@link ManagedObjectSource} instance's namespace.
	 */
	public class HandlerBuilderWrapper<F extends Enum<F>> implements
			HandlerBuilder<F> {

		/**
		 * Delegate {@link HandlerBuilder}.
		 */
		private final HandlerBuilder<F> delegate;

		/**
		 * Initiate.
		 * 
		 * @param delegate
		 *            Delegate {@link HandlerBuilder}.
		 */
		public HandlerBuilderWrapper(HandlerBuilder<F> delegate) {
			this.delegate = delegate;
		}

		/*
		 * =============== HandlerBuilder ===========================
		 */

		@Override
		public void setHandlerFactory(HandlerFactory<F> factory) {
			this.delegate.setHandlerFactory(factory);
		}

		@Override
		public void linkProcess(F key, String workName, String taskName) {

			// Obtain the namespaced names
			String namespacedWorkName = ManagedObjectSourceContextImpl.this
					.getNamespacedName(workName);
			String namespacedTaskName = ManagedObjectSourceContextImpl.this
					.getNamespacedName(taskName);

			// Link the process
			this.delegate.linkProcess(key, namespacedWorkName,
					namespacedTaskName);
		}

		@Override
		public void linkProcess(int processIndex, String workName,
				String taskName) {

			// Obtain the namespaced names
			String namespacedWorkName = ManagedObjectSourceContextImpl.this
					.getNamespacedName(workName);
			String namespacedTaskName = ManagedObjectSourceContextImpl.this
					.getNamespacedName(taskName);

			// Link the process
			this.delegate.linkProcess(processIndex, namespacedWorkName,
					namespacedTaskName);
		}
	}

	/**
	 * {@link ManagedObjectWorkBuilder} implementation.
	 */
	protected class ManagedObjectWorkBuilderImpl<W extends Work> implements
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
		public <P, F extends Enum<F>> ManagedObjectTaskBuilder<F> addTask(
				String taskName, TaskFactory<P, W, None, F> taskFactory) {

			// Obtain the namespaced task name
			String namespacedTaskName = ManagedObjectSourceContextImpl.this
					.getNamespacedName(taskName);

			// Create and initialise the task
			TaskBuilder<P, W, None, F> taskBuilder = this.workBuilder.addTask(
					namespacedTaskName, taskFactory);

			// Register as initial task of work if first task
			if (this.isFirstTask) {
				this.workBuilder.setInitialTask(namespacedTaskName);
				this.isFirstTask = false;
			}

			// Return the task builder for the managed object source
			return new ManagedObjectTaskBuilderImpl<F>(taskBuilder);
		}
	}

	/**
	 * {@link ManagedObjectTaskBuilder} implementation.
	 */
	public class ManagedObjectTaskBuilderImpl<F extends Enum<F>> implements
			ManagedObjectTaskBuilder<F> {

		/**
		 * {@link TaskBuilder}.
		 */
		private final TaskBuilder<?, ?, None, F> taskBuilder;

		/**
		 * Initiate.
		 * 
		 * @param taskBuilder
		 *            {@link TaskBuilder}.
		 */
		public ManagedObjectTaskBuilderImpl(
				TaskBuilder<?, ?, None, F> taskBuilder) {
			this.taskBuilder = taskBuilder;
		}

		/*
		 * ============== ManagedObjectTaskBuilder =====================
		 */

		@Override
		public void setNextTaskInFlow(String taskName) {
			this.taskBuilder
					.setNextTaskInFlow(ManagedObjectSourceContextImpl.this
							.getNamespacedName(taskName));
		}

		@Override
		public void setNextTaskInFlow(String workName, String taskName) {
			this.taskBuilder.setNextTaskInFlow(
					ManagedObjectSourceContextImpl.this
							.getNamespacedName(workName),
					ManagedObjectSourceContextImpl.this
							.getNamespacedName(taskName));
		}

		@Override
		public void setTeam(String teamName) {
			this.taskBuilder.setTeam(ManagedObjectSourceContextImpl.this
					.getNamespacedName(teamName));
		}

		@Override
		public void linkFlow(F key, String taskName,
				FlowInstigationStrategyEnum strategy) {
			this.taskBuilder.linkFlow(key, ManagedObjectSourceContextImpl.this
					.getNamespacedName(taskName), strategy);
		}

		@Override
		public void linkFlow(int flowIndex, String taskName,
				FlowInstigationStrategyEnum strategy) {
			this.taskBuilder.linkFlow(flowIndex,
					ManagedObjectSourceContextImpl.this
							.getNamespacedName(taskName), strategy);
		}

		@Override
		public void linkFlow(F key, String workName, String taskName,
				FlowInstigationStrategyEnum strategy) {
			this.taskBuilder.linkFlow(key, ManagedObjectSourceContextImpl.this
					.getNamespacedName(workName),
					ManagedObjectSourceContextImpl.this
							.getNamespacedName(taskName), strategy);
		}

		@Override
		public void linkFlow(int flowIndex, String workName, String taskName,
				FlowInstigationStrategyEnum strategy) {
			this.taskBuilder.linkFlow(flowIndex,
					ManagedObjectSourceContextImpl.this
							.getNamespacedName(workName),
					ManagedObjectSourceContextImpl.this
							.getNamespacedName(taskName), strategy);
		}

		@Override
		public void addEscalation(Class<? extends Throwable> typeOfCause,
				String taskName) {
			this.taskBuilder.addEscalation(typeOfCause,
					ManagedObjectSourceContextImpl.this
							.getNamespacedName(taskName));
		}

		@Override
		public void addEscalation(Class<? extends Throwable> typeOfCause,
				String workName, String taskName) {
			this.taskBuilder.addEscalation(typeOfCause,
					ManagedObjectSourceContextImpl.this
							.getNamespacedName(workName),
					ManagedObjectSourceContextImpl.this
							.getNamespacedName(taskName));
		}
	}
}
