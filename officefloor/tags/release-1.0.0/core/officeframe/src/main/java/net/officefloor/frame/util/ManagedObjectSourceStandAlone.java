/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2011 Daniel Sagenschneider
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
import java.util.Properties;

import net.officefloor.frame.api.OfficeFrame;
import net.officefloor.frame.api.build.ManagingOfficeBuilder;
import net.officefloor.frame.api.build.OfficeBuilder;
import net.officefloor.frame.api.build.OfficeFloorBuilder;
import net.officefloor.frame.api.escalate.EscalationHandler;
import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.execute.TaskContext;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.impl.construct.managedobjectsource.ManagedObjectSourceContextImpl;
import net.officefloor.frame.internal.structure.ProcessState;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectExecuteContext;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;

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
	 * {@link Properties}.
	 */
	private final Properties properties = new Properties();

	/**
	 * {@link InvokeProcessTaskStruct} instances by their {@link ProcessState}
	 * invocation index.
	 */
	private final Map<Integer, InvokeProcessTaskStruct> processes = new HashMap<Integer, InvokeProcessTaskStruct>();

	/**
	 * Adds a property for the {@link ManagedObjectSource}.
	 * 
	 * @param name
	 *            Name of the property.
	 * @param value
	 *            Value for the property.
	 */
	public void addProperty(String name, String value) {
		this.properties.setProperty(name, value);
	}

	/**
	 * Instantiates and initialises the {@link ManagedObjectSource}.
	 * 
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

		// Initialise the managed object source
		ManagedObjectSourceContextImpl sourceContext = new ManagedObjectSourceContextImpl(
				STAND_ALONE_MANAGED_OBJECT_SOURCE_NAME, this.properties,
				managedObjectSourceClass.getClassLoader(),
				managingOfficeBuilder, officeBuilder);
		moSource.init(sourceContext);

		// Return the initialised managed object source
		return moSource;
	}

	/**
	 * Starts the {@link ManagedObjectSource}.
	 * 
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
		this.processes.put(new Integer(processIndex),
				new InvokeProcessTaskStruct(task, taskContext));
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
	 * Struct containing the details for the initial {@link Task} to be executed
	 * for the invoked {@link ProcessState}.
	 */
	private class InvokeProcessTaskStruct {

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
		public InvokeProcessTaskStruct(Task<?, ?, ?> task,
				TaskContext<?, ?, ?> taskContext) {
			this.task = task;
			this.taskContext = taskContext;
		}
	}

	/**
	 * {@link ManagedObjectExecuteContext}.
	 */
	private class LoadExecuteContext<F extends Enum<F>> implements
			ManagedObjectExecuteContext<F> {

		/**
		 * Processes the {@link Task} for the invoked {@link ProcessState}.
		 * 
		 * @param processIndex
		 *            Index of the {@link ProcessState} to invoke.
		 * @param escalationHandler
		 *            {@link EscalationHandler}. May be <code>null</code>.
		 */
		@SuppressWarnings({ "unchecked", "rawtypes" })
		private void process(int processIndex,
				EscalationHandler escalationHandler) {

			// Obtain the details for invoking process
			InvokeProcessTaskStruct struct = ManagedObjectSourceStandAlone.this.processes
					.get(new Integer(processIndex));
			if (struct == null) {
				throw new UnsupportedOperationException(
						"No task configured for process invocation index "
								+ processIndex);
			}

			try {
				try {
					// Invoke the task for the process
					struct.task.doTask((TaskContext) struct.taskContext);

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
		}

		/*
		 * ================ ManagedObjectExecuteContext =====================
		 */

		@Override
		public void invokeProcess(F key, Object parameter,
				ManagedObject managedObject) {
			this.process(key.ordinal(), null);
		}

		@Override
		public void invokeProcess(int flowIndex, Object parameter,
				ManagedObject managedObject) {
			this.process(flowIndex, null);
		}

		@Override
		public void invokeProcess(F key, Object parameter,
				ManagedObject managedObject, EscalationHandler escalationHandler) {
			this.process(key.ordinal(), escalationHandler);
		}

		@Override
		public void invokeProcess(int flowIndex, Object parameter,
				ManagedObject managedObject, EscalationHandler escalationHandler) {
			this.process(flowIndex, escalationHandler);
		}
	}

}