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
package net.officefloor.frame.util;

import java.util.HashMap;
import java.util.Map;

import net.officefloor.frame.api.OfficeFrame;
import net.officefloor.frame.api.build.ManagingOfficeBuilder;
import net.officefloor.frame.api.build.OfficeBuilder;
import net.officefloor.frame.api.build.OfficeFloorBuilder;
import net.officefloor.frame.api.escalate.EscalationHandler;
import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.execute.TaskContext;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.manage.ProcessFuture;
import net.officefloor.frame.impl.construct.managedobjectsource.ManagedObjectSourceContextImpl;
import net.officefloor.frame.impl.construct.source.SourceContextImpl;
import net.officefloor.frame.impl.construct.source.SourcePropertiesImpl;
import net.officefloor.frame.internal.structure.ProcessState;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectExecuteContext;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.spi.source.SourceContext;
import net.officefloor.frame.spi.source.SourceProperties;

/**
 * Loads {@link ManagedObjectSource} for stand-alone use.
 * 
 * @author Daniel Sagenschneider
 */
public class ManagedObjectSourceStandAlone {

	/**
	 * Name of the {@link ManagedObjectSource} being loaded.
	 */
	public static final String STAND_ALONE_MANAGED_OBJECT_SOURCE_NAME = "managed.object.source";

	/**
	 * Name of the {@link Office} managing the {@link ManagedObjectSource} being
	 * loaded.
	 */
	public static final String STAND_ALONE_MANAGING_OFFICE_NAME = "office";

	/**
	 * {@link SourceProperties}.
	 */
	private final SourcePropertiesImpl properties = new SourcePropertiesImpl();

	/**
	 * {@link InvokedProcessServicer} instances by their {@link ProcessState}
	 * invocation index.
	 */
	private final Map<Integer, InvokedProcessServicer> processes = new HashMap<Integer, InvokedProcessServicer>();

	/**
	 * Adds a property for the {@link ManagedObjectSource}.
	 * 
	 * @param name
	 *            Name of the property.
	 * @param value
	 *            Value for the property.
	 */
	public void addProperty(String name, String value) {
		this.properties.addProperty(name, value);
	}

	/**
	 * Instantiates and initialises the {@link ManagedObjectSource}.
	 * 
	 * @param <D>
	 *            Dependency key type.
	 * @param <F>
	 *            Flow key type.
	 * @param <MS>
	 *            {@link ManagedObjectSource} type.
	 * @param managedObjectSourceClass
	 *            Class of the {@link ManagedObjectSource}.
	 * @return Initialised {@link ManagedObjectSource}.
	 * @throws Exception
	 *             If fails to initialise {@link ManagedObjectSource}.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public <D extends Enum<D>, F extends Enum<F>, MS extends ManagedObjectSource<D, F>> MS initManagedObjectSource(
			Class<MS> managedObjectSourceClass) throws Exception {

		// Create a new instance of the managed object source
		MS moSource = managedObjectSourceClass.newInstance();

		// Create necessary builders
		OfficeFloorBuilder officeFloorBuilder = OfficeFrame
				.createOfficeFloorBuilder();
		ManagingOfficeBuilder<F> managingOfficeBuilder = officeFloorBuilder
				.addManagedObject(STAND_ALONE_MANAGED_OBJECT_SOURCE_NAME,
						managedObjectSourceClass).setManagingOffice(
						"STAND ALONE");
		OfficeBuilder officeBuilder = officeFloorBuilder
				.addOffice(STAND_ALONE_MANAGING_OFFICE_NAME);

		// Create the delegate source context
		SourceContext context = new SourceContextImpl(false, Thread
				.currentThread().getContextClassLoader());

		// Initialise the managed object source
		ManagedObjectSourceContextImpl sourceContext = new ManagedObjectSourceContextImpl(
				false, STAND_ALONE_MANAGED_OBJECT_SOURCE_NAME, this.properties,
				context, managingOfficeBuilder, officeBuilder);
		moSource.init(sourceContext);

		// Return the initialised managed object source
		return moSource;
	}

	/**
	 * Starts the {@link ManagedObjectSource}.
	 *
	 * @param <D>
	 *            Dependency key type.
	 * @param <F>
	 *            Flow key type.
	 * @param <MS>
	 *            {@link ManagedObjectSource} type.
	 * @param managedObjectSource
	 *            {@link ManagedObjectSource}.
	 * @throws Exception
	 *             If fails to start the {@link ManagedObjectSource}.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public <D extends Enum<D>, F extends Enum<F>, MS extends ManagedObjectSource<D, F>> void startManagedObjectSource(
			MS managedObjectSource) throws Exception {
		// Start the managed object source
		managedObjectSource.start(new LoadExecuteContext());
	}

	/**
	 * Loads (init and start) the {@link ManagedObjectSource}.
	 *
	 * @param <D>
	 *            Dependency key type.
	 * @param <F>
	 *            Flow key type.
	 * @param <MS>
	 *            {@link ManagedObjectSource} type.
	 * @param managedObjectSourceClass
	 *            {@link ManagedObjectSource} class.
	 * @return Loaded {@link ManagedObjectSource}.
	 * @throws Exception
	 *             If fails to init and start the {@link ManagedObjectSource}.
	 */
	public <D extends Enum<D>, F extends Enum<F>, MS extends ManagedObjectSource<D, F>> MS loadManagedObjectSource(
			Class<MS> managedObjectSourceClass) throws Exception {

		// Initialise the managed object source
		MS moSource = this.initManagedObjectSource(managedObjectSourceClass);

		// Start the managed object source
		this.startManagedObjectSource(moSource);

		// Return the loaded managed object source
		return moSource;
	}

	/**
	 * Registers the initial {@link Task} for the invoked {@link ProcessState}.
	 * 
	 * @param processKey
	 *            Key of the {@link ProcessState}.
	 * @param task
	 *            Initial {@link Task} for the {@link ProcessState}.
	 * @param taskContext
	 *            {@link TaskContext} for the {@link Task}. Allows for mocking
	 *            the {@link TaskContext} to validate functionality for the
	 *            {@link Task}.
	 */
	public void registerInvokeProcessTask(Enum<?> processKey,
			Task<?, ?, ?> task, TaskContext<?, ?, ?> taskContext) {
		this.registerInvokeProcessTask(processKey.ordinal(), task, taskContext);
	}

	/**
	 * Registers the initial {@link Task} for the invoked {@link ProcessState}.
	 * 
	 * @param processIndex
	 *            Index of the {@link ProcessState}.
	 * @param task
	 *            Initial {@link Task} for the {@link ProcessState}.
	 * @param taskContext
	 *            {@link TaskContext} for the {@link Task}. Allows for mocking
	 *            the {@link TaskContext} to validate functionality for the
	 *            {@link Task}.
	 */
	public void registerInvokeProcessTask(int processIndex, Task<?, ?, ?> task,
			TaskContext<?, ?, ?> taskContext) {
		this.registerInvokeProcessServicer(processIndex,
				new TaskInvokedProcessServicer(task, taskContext));
	}

	/**
	 * Registers an {@link InvokedProcessServicer} for the invoked
	 * {@link ProcessState}.
	 * 
	 * @param processKey
	 *            Key of the {@link ProcessState}.
	 * @param servicer
	 *            {@link InvokedProcessServicer}.
	 */
	public void registerInvokeProcessServicer(Enum<?> processKey,
			InvokedProcessServicer servicer) {
		this.registerInvokeProcessServicer(processKey.ordinal(), servicer);
	}

	/**
	 * Registers an {@link InvokedProcessServicer} for the invoked
	 * {@link ProcessState}.
	 * 
	 * @param processIndex
	 *            Index of the {@link ProcessState}.
	 * @param servicer
	 *            {@link InvokedProcessServicer}.
	 */
	public void registerInvokeProcessServicer(int processIndex,
			InvokedProcessServicer servicer) {
		this.processes.put(new Integer(processIndex), servicer);
	}

	/**
	 * {@link InvokedProcessServicer} containing the details for the initial
	 * {@link Task} to be executed for the invoked {@link ProcessState}.
	 */
	private class TaskInvokedProcessServicer implements InvokedProcessServicer {

		/**
		 * {@link Task}.
		 */
		public final Task<?, ?, ?> task;

		/**
		 * {@link TaskContext}.
		 */
		public final TaskContext<?, ?, ?> taskContext;

		/**
		 * Initiate.
		 * 
		 * @param task
		 *            {@link Task}.
		 * @param taskContext
		 *            {@link TaskContext}.
		 */
		public TaskInvokedProcessServicer(Task<?, ?, ?> task,
				TaskContext<?, ?, ?> taskContext) {
			this.task = task;
			this.taskContext = taskContext;
		}

		/*
		 * =============== InvokedProcessServicer ==================
		 */

		@Override
		@SuppressWarnings({ "unchecked", "rawtypes" })
		public void service(int processIndex, Object parameter,
				ManagedObject managedObject) throws Throwable {
			this.task.doTask((TaskContext) this.taskContext);
		}
	}

	/**
	 * {@link ManagedObjectExecuteContext}.
	 */
	private class LoadExecuteContext<F extends Enum<F>> implements
			ManagedObjectExecuteContext<F>, ProcessFuture {

		/**
		 * Processes the {@link Task} for the invoked {@link ProcessState}.
		 * 
		 * @param processIndex
		 *            Index of the {@link ProcessState} to invoke.
		 * @param escalationHandler
		 *            {@link EscalationHandler}. May be <code>null</code>.
		 * @param delay
		 *            Delay to invoke {@link ProcessState}.
		 * @param parameter
		 *            Parameter to initial {@link Task} of the invoked
		 *            {@link ProcessState}.
		 * @param managedObject
		 *            {@link ManagedObject} for the {@link ProcessState}.
		 */
		private ProcessFuture process(int processIndex,
				EscalationHandler escalationHandler, long delay,
				Object parameter, ManagedObject managedObject) {

			// Ignore delay and execute immediately

			// Obtain the details for invoking process
			InvokedProcessServicer servicer = ManagedObjectSourceStandAlone.this.processes
					.get(new Integer(processIndex));
			if (servicer == null) {
				throw new UnsupportedOperationException(
						"No task configured for process invocation index "
								+ processIndex);
			}

			try {
				try {
					// Service the invoked process
					servicer.service(processIndex, parameter, managedObject);

				} catch (Throwable ex) {
					// Determine if handle
					if (escalationHandler != null) {
						escalationHandler.handleEscalation(ex);
					} else {
						throw ex; // not handled so propagate
					}
				}
			} catch (Throwable ex) {
				// Handle failure
				if (ex instanceof Error) {
					throw (Error) ex;
				} else if (ex instanceof RuntimeException) {
					throw (RuntimeException) ex;
				} else {
					// Propagate failure
					throw new Error(ex);
				}
			}

			// Return this as the process future
			return this;
		}

		/*
		 * ================ ManagedObjectExecuteContext =====================
		 */

		@Override
		public ProcessFuture invokeProcess(F key, Object parameter,
				ManagedObject managedObject, long delay) {
			return this.process(key.ordinal(), null, delay, parameter,
					managedObject);
		}

		@Override
		public ProcessFuture invokeProcess(int flowIndex, Object parameter,
				ManagedObject managedObject, long delay) {
			return this.process(flowIndex, null, delay, parameter,
					managedObject);
		}

		@Override
		public ProcessFuture invokeProcess(F key, Object parameter,
				ManagedObject managedObject, long delay,
				EscalationHandler escalationHandler) {
			return this.process(key.ordinal(), escalationHandler, delay,
					parameter, managedObject);
		}

		@Override
		public ProcessFuture invokeProcess(int flowIndex, Object parameter,
				ManagedObject managedObject, long delay,
				EscalationHandler escalationHandler) {
			return this.process(flowIndex, escalationHandler, delay, parameter,
					managedObject);
		}

		/*
		 * ==================== ProcessFuture =============================
		 */

		@Override
		public boolean isComplete() {
			// Always complete
			return true;
		}
	}

}