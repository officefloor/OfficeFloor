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
package net.officefloor.frame.impl;

import java.util.Properties;

import net.officefloor.frame.api.OfficeFrame;
import net.officefloor.frame.api.build.BuildException;
import net.officefloor.frame.api.build.HandlerBuilder;
import net.officefloor.frame.api.build.HandlerFactory;
import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.build.ManagedObjectBuilder;
import net.officefloor.frame.api.build.ManagedObjectHandlerBuilder;
import net.officefloor.frame.api.build.ManagedObjectHandlersBuilder;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.build.OfficeBuilder;
import net.officefloor.frame.api.build.TaskBuilder;
import net.officefloor.frame.api.build.TaskFactory;
import net.officefloor.frame.api.build.WorkBuilder;
import net.officefloor.frame.api.build.WorkFactory;
import net.officefloor.frame.api.execute.Handler;
import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.impl.construct.OfficeBuilderImpl;
import net.officefloor.frame.internal.structure.FlowInstigationStrategyEnum;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSourceContext;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSourceUnknownPropertyError;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectTaskBuilder;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectWorkBuilder;
import net.officefloor.frame.spi.managedobject.source.ResourceLocator;

/**
 * Implementation of the
 * {@link net.officefloor.frame.spi.managedobject.source.ManagedObjectSourceContext}.
 * 
 * @author Daniel
 */
public class ManagedObjectSourceContextImpl implements
		ManagedObjectSourceContext {

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
	protected ManagedObjectBuilder managedObjectBuilder;

	/**
	 * {@link OfficeBuilder} for the office using the
	 * {@link ManagedObjectSource}.
	 */
	protected OfficeBuilder officeBuilder;

	/**
	 * {@link OfficeFrame}.
	 */
	protected OfficeFrame officeFrame;

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
	 * @param officeFrame
	 *            {@link OfficeFrame}.
	 */
	public ManagedObjectSourceContextImpl(String managedObjectName,
			Properties properties, ResourceLocator resourceLocator,
			ManagedObjectBuilder managedObjectBuilder,
			OfficeBuilder officeBuilder, OfficeFrame officeFrame) {
		this.managedObjectName = managedObjectName;
		this.properties = properties;
		this.resourceLocator = resourceLocator;
		this.managedObjectBuilder = managedObjectBuilder;
		this.officeBuilder = officeBuilder;
		this.officeFrame = officeFrame;
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
		this.officeFrame = null;
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
	 * ================================================================
	 * ManagedObjectSourceContext
	 * ================================================================
	 */

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.spi.managedobject.source.ManagedObjectSourceContext#getProperties()
	 */
	public Properties getProperties() {
		return this.properties;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.spi.managedobject.source.ManagedObjectSourceContext#getProperty(java.lang.String)
	 */
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.spi.managedobject.source.ManagedObjectSourceContext#getProperty(java.lang.String,
	 *      java.lang.String)
	 */
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.spi.managedobject.source.ManagedObjectSourceContext#getResourceLocator()
	 */
	public ResourceLocator getResourceLocator() {
		return this.resourceLocator;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.spi.managedobject.source.ManagedObjectSourceContext#getHandlerBuilder(java.lang.Class)
	 */
	@Override
	public <H extends Enum<H>> ManagedObjectHandlersBuilder<H> getHandlerBuilder(
			Class<H> handlerKeys) throws BuildException {
		// Return the wrapped managed object hander builder
		return new ManagedObjectHandlersBuilderWrapper<H>(
				this.managedObjectBuilder
						.getManagedObjectHandlerBuilder(handlerKeys));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.spi.managedobject.source.ManagedObjectSourceContext#addWork(java.lang.String,
	 *      java.lang.Class)
	 */
	@Override
	public <W extends Work> ManagedObjectWorkBuilder<W> addWork(
			String workName, Class<W> typeOfWork) throws BuildException {

		// Create the work
		WorkBuilder<W> workBuilder = this.officeFrame.getBuilderFactory()
				.createWorkBuilder(typeOfWork);

		// Register the work with the office
		this.officeBuilder.addWork(this.getNamespacedName(workName),
				workBuilder);

		// Return the managed object work builder
		return new ManagedObjectWorkBuilderImpl<W>(workBuilder);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.spi.managedobject.source.ManagedObjectSourceContext#getRecycleWork(java.lang.Class)
	 */
	@Override
	public <W extends Work> ManagedObjectWorkBuilder<W> getRecycleWork(
			Class<W> typeOfWork) throws BuildException {

		// Ensure not already created
		if (this.recycleWorkName != null) {
			throw new IllegalStateException(
					"Only one clean up per Managed Object");
		}

		// Name the recycle work
		this.recycleWorkName = this.managedObjectName + "."
				+ RawManagedObjectMetaData.MANAGED_OBJECT_CLEAN_UP_WORK_NAME;

		// Add and return the recycle work
		return this.addWork(
				RawManagedObjectMetaData.MANAGED_OBJECT_CLEAN_UP_WORK_NAME,
				typeOfWork);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.spi.managedobject.source.ManagedObjectSourceContext#addStartupTask(java.lang.String,
	 *      java.lang.String)
	 */
	@Override
	public void addStartupTask(String workName, String taskName)
			throws BuildException {
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
	 * Wrapper for a delegate {@link ManagedObjectHandlersBuilder} that applies
	 * the {@link ManagedObjectSource} instance's namespace.
	 */
	protected class ManagedObjectHandlersBuilderWrapper<H extends Enum<H>>
			implements ManagedObjectHandlersBuilder<H> {

		/**
		 * Delegate {@link ManagedObjectHandlersBuilder}.
		 */
		private final ManagedObjectHandlersBuilder<H> delegate;

		/**
		 * Initiate.
		 * 
		 * @param delegate
		 *            Delegate {@link ManagedObjectHandlersBuilder}.
		 */
		public ManagedObjectHandlersBuilderWrapper(
				ManagedObjectHandlersBuilder<H> delegate) {
			this.delegate = delegate;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see net.officefloor.frame.api.build.ManagedObjectHandlersBuilder#registerHandler(java.lang.Enum)
		 */
		@Override
		public ManagedObjectHandlerBuilder registerHandler(H key)
				throws BuildException {
			return new ManagedObjectHandlerBuilderWrapper(this.delegate
					.registerHandler(key));
		}

	}

	/**
	 * Wrapper for a delegate {@link ManagedObjectHandlerBuilder} that applies
	 * the {@link ManagedObjectSource} instance's namespace.
	 */
	protected class ManagedObjectHandlerBuilderWrapper implements
			ManagedObjectHandlerBuilder {

		/**
		 * Delegate {@link ManagedObjectHandlerBuilder}.
		 */
		private final ManagedObjectHandlerBuilder delegate;

		/**
		 * Initiate.
		 * 
		 * @param delegate
		 *            Delegate {@link ManagedObjectHandlerBuilder}.
		 */
		public ManagedObjectHandlerBuilderWrapper(
				ManagedObjectHandlerBuilder delegate) {
			this.delegate = delegate;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see net.officefloor.frame.api.build.ManagedObjectHandlerBuilder#setHandlerType(java.lang.Class)
		 */
		@Override
		public <H extends Handler<?>> void setHandlerType(Class<H> handlerType)
				throws BuildException {
			this.delegate.setHandlerType(handlerType);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see net.officefloor.frame.api.build.ManagedObjectHandlerBuilder#getHandlerBuilder(java.lang.Class)
		 */
		@Override
		public <F extends Enum<F>> HandlerBuilder<F> getHandlerBuilder(
				Class<F> processListingEnum) throws BuildException {
			return new HandlerBuilderWrapper<F>(this.delegate
					.getHandlerBuilder(processListingEnum));
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see net.officefloor.frame.api.build.ManagedObjectHandlerBuilder#getHandlerBuilder()
		 */
		@Override
		public HandlerBuilder<Indexed> getHandlerBuilder()
				throws BuildException {
			return new HandlerBuilderWrapper<Indexed>(this.delegate
					.getHandlerBuilder());
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
		 * (non-Javadoc)
		 * 
		 * @see net.officefloor.frame.api.build.HandlerBuilder#setHandlerFactory(net.officefloor.frame.api.build.HandlerFactory)
		 */
		@Override
		public void setHandlerFactory(HandlerFactory<F> factory)
				throws BuildException {
			this.delegate.setHandlerFactory(factory);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see net.officefloor.frame.api.build.HandlerBuilder#linkProcess(java.lang.Enum,
		 *      java.lang.String, java.lang.String)
		 */
		@Override
		public void linkProcess(F key, String workName, String taskName)
				throws BuildException {

			// Obtain the namespaced names
			String namespacedWorkName = ManagedObjectSourceContextImpl.this
					.getNamespacedName(workName);
			String namespacedTaskName = ManagedObjectSourceContextImpl.this
					.getNamespacedName(taskName);

			// Link the process
			this.delegate.linkProcess(key, namespacedWorkName,
					namespacedTaskName);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see net.officefloor.frame.api.build.HandlerBuilder#linkProcess(int,
		 *      java.lang.String, java.lang.String)
		 */
		@Override
		public void linkProcess(int processIndex, String workName,
				String taskName) throws BuildException {

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
		 * (non-Javadoc)
		 * 
		 * @see net.officefloor.frame.spi.managedobject.source.ManagedObjectWorkBuilder#setWorkFactory(net.officefloor.frame.api.build.WorkFactory)
		 */
		@Override
		public void setWorkFactory(WorkFactory<W> factory)
				throws BuildException {
			this.workBuilder.setWorkFactory(factory);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see net.officefloor.frame.spi.managedobject.source.ManagedObjectWorkBuilder#addTask(java.lang.String,
		 *      java.lang.Class, net.officefloor.frame.api.build.TaskFactory,
		 *      java.lang.Class)
		 */
		@Override
		public <P, F extends Enum<F>> ManagedObjectTaskBuilder<F> addTask(
				String taskName, Class<P> parameterType,
				TaskFactory<P, W, None, F> factory, Class<F> flowListingEnum)
				throws BuildException {

			// Obtain the namespaced task name
			String namespacedTaskName = ManagedObjectSourceContextImpl.this
					.getNamespacedName(taskName);

			// Create and initialise the task
			TaskBuilder<P, W, None, F> taskBuilder = this.workBuilder.addTask(
					namespacedTaskName, parameterType, None.class,
					flowListingEnum);
			taskBuilder.setTaskFactory(factory);

			// Register as initial task of work if first task
			if (this.isFirstTask) {
				this.workBuilder.setInitialTask(namespacedTaskName);
				this.isFirstTask = false;
			}

			// Return the task builder for the managed object source
			return new ManagedObjectTaskBuilderImpl<F>(taskBuilder);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see net.officefloor.frame.spi.managedobject.source.ManagedObjectWorkBuilder#addTask(java.lang.String,
		 *      java.lang.Class, net.officefloor.frame.api.build.TaskFactory)
		 */
		@Override
		public <P> ManagedObjectTaskBuilder<Indexed> addTask(String taskName,
				Class<P> parameterType, TaskFactory<P, W, None, Indexed> factory)
				throws BuildException {
			return this
					.addTask(taskName, parameterType, factory, Indexed.class);
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
		 * (non-Javadoc)
		 * 
		 * @see net.officefloor.frame.api.build.FlowNodeBuilder#setNextTaskInFlow(java.lang.String)
		 */
		@Override
		public void setNextTaskInFlow(String taskName) {
			this.taskBuilder
					.setNextTaskInFlow(ManagedObjectSourceContextImpl.this
							.getNamespacedName(taskName));
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see net.officefloor.frame.api.build.FlowNodeBuilder#setNextTaskInFlow(java.lang.String,
		 *      java.lang.String)
		 */
		@Override
		public void setNextTaskInFlow(String workName, String taskName) {
			this.taskBuilder.setNextTaskInFlow(
					ManagedObjectSourceContextImpl.this
							.getNamespacedName(workName),
					ManagedObjectSourceContextImpl.this
							.getNamespacedName(taskName));
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see net.officefloor.frame.spi.managedobject.source.ManagedObjectTaskBuilder#setTeam(java.lang.String)
		 */
		@Override
		public void setTeam(String teamName) {
			this.taskBuilder.setTeam(ManagedObjectSourceContextImpl.this
					.getNamespacedName(teamName));
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see net.officefloor.frame.api.build.FlowNodeBuilder#linkFlow(java.lang.Enum,
		 *      java.lang.String,
		 *      net.officefloor.frame.internal.structure.FlowInstigationStrategyEnum)
		 */
		@Override
		public void linkFlow(F key, String taskName,
				FlowInstigationStrategyEnum strategy) throws BuildException {
			this.taskBuilder.linkFlow(key, ManagedObjectSourceContextImpl.this
					.getNamespacedName(taskName), strategy);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see net.officefloor.frame.api.build.FlowNodeBuilder#linkFlow(int,
		 *      java.lang.String,
		 *      net.officefloor.frame.internal.structure.FlowInstigationStrategyEnum)
		 */
		@Override
		public void linkFlow(int flowIndex, String taskName,
				FlowInstigationStrategyEnum strategy) throws BuildException {
			this.taskBuilder.linkFlow(flowIndex,
					ManagedObjectSourceContextImpl.this
							.getNamespacedName(taskName), strategy);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see net.officefloor.frame.api.build.FlowNodeBuilder#linkFlow(java.lang.Enum,
		 *      java.lang.String, java.lang.String,
		 *      net.officefloor.frame.internal.structure.FlowInstigationStrategyEnum)
		 */
		@Override
		public void linkFlow(F key, String workName, String taskName,
				FlowInstigationStrategyEnum strategy) throws BuildException {
			this.taskBuilder.linkFlow(key, ManagedObjectSourceContextImpl.this
					.getNamespacedName(workName),
					ManagedObjectSourceContextImpl.this
							.getNamespacedName(taskName), strategy);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see net.officefloor.frame.api.build.FlowNodeBuilder#linkFlow(int,
		 *      java.lang.String, java.lang.String,
		 *      net.officefloor.frame.internal.structure.FlowInstigationStrategyEnum)
		 */
		@Override
		public void linkFlow(int flowIndex, String workName, String taskName,
				FlowInstigationStrategyEnum strategy) throws BuildException {
			this.taskBuilder.linkFlow(flowIndex,
					ManagedObjectSourceContextImpl.this
							.getNamespacedName(workName),
					ManagedObjectSourceContextImpl.this
							.getNamespacedName(taskName), strategy);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see net.officefloor.frame.api.build.FlowNodeBuilder#addEscalation(java.lang.Class,
		 *      boolean, java.lang.String)
		 */
		@Override
		public void addEscalation(Class<? extends Throwable> typeOfCause,
				boolean isResetThreadState, String taskName) {
			this.taskBuilder.addEscalation(typeOfCause, isResetThreadState,
					ManagedObjectSourceContextImpl.this
							.getNamespacedName(taskName));
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see net.officefloor.frame.api.build.FlowNodeBuilder#addEscalation(java.lang.Class,
		 *      boolean, java.lang.String, java.lang.String)
		 */
		@Override
		public void addEscalation(Class<? extends Throwable> typeOfCause,
				boolean isResetThreadState, String workName, String taskName) {
			this.taskBuilder.addEscalation(typeOfCause, isResetThreadState,
					ManagedObjectSourceContextImpl.this
							.getNamespacedName(workName),
					ManagedObjectSourceContextImpl.this
							.getNamespacedName(taskName));
		}
	}
}
