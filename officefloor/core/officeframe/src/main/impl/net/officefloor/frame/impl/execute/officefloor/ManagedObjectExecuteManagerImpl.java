/*-
 * #%L
 * OfficeFrame
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

package net.officefloor.frame.impl.execute.officefloor;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ThreadFactory;
import java.util.logging.Logger;

import net.officefloor.frame.api.executive.ExecutionStrategy;
import net.officefloor.frame.api.function.FlowCallback;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.source.ManagedObjectExecuteContext;
import net.officefloor.frame.api.managedobject.source.ManagedObjectService;
import net.officefloor.frame.api.managedobject.source.ManagedObjectServiceContext;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.api.managedobject.source.ManagedObjectStartupProcess;
import net.officefloor.frame.internal.structure.FlowMetaData;
import net.officefloor.frame.internal.structure.FunctionState;
import net.officefloor.frame.internal.structure.ManagedObjectExecuteManager;
import net.officefloor.frame.internal.structure.ManagedObjectExecuteStart;
import net.officefloor.frame.internal.structure.ManagedObjectMetaData;
import net.officefloor.frame.internal.structure.ManagedObjectStartupFunction;
import net.officefloor.frame.internal.structure.ManagedObjectStartupRunnable;
import net.officefloor.frame.internal.structure.OfficeMetaData;
import net.officefloor.frame.internal.structure.ProcessState;

/**
 * {@link ManagedObjectExecuteContext} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class ManagedObjectExecuteManagerImpl<F extends Enum<F>> implements ManagedObjectExecuteManager<F> {

	/**
	 * {@link ManagedObjectMetaData}.
	 */
	private final ManagedObjectMetaData<?> managedObjectMetaData;

	/**
	 * {@link ExecutionStrategy} instances in index order for the
	 * {@link ManagedObjectSource}.
	 */
	private final ThreadFactory[][] executionStrategies;

	/**
	 * {@link Logger} for the {@link ManagedObjectExecuteContext}.
	 */
	private final Logger executeLogger;

	/**
	 * {@link OfficeMetaData}.
	 */
	private final OfficeMetaData officeMetaData;

	/**
	 * {@link ManagedObjectExecuteContext}.
	 */
	private final ManagedObjectExecuteContext<F> executeContext = new ManagedObjectExecuteContextImpl();

	/**
	 * {@link ManagedObjectServiceContextImpl}.
	 */
	private final ManagedObjectServiceContextImpl<F> serviceContext;

	/**
	 * {@link ManagedObjectService} instances.
	 */
	private final List<ManagedObjectService<F>> services = new LinkedList<>();

	/**
	 * {@link ManagedObjectStartupRunnable} instances for start up
	 * {@link ProcessState} instances.
	 */
	private List<ManagedObjectStartupRunnable> startupProcesses = new LinkedList<>();

	/**
	 * Initiate.
	 * 
	 * @param managedObjectMetaData {@link ManagedObjectMetaData} of the
	 *                              {@link ManagedObject}.
	 * @param processMoIndex        Index of the {@link ManagedObject} within the
	 *                              {@link ProcessState}.
	 * @param processLinks          {@link FlowMetaData} in index order for the
	 *                              {@link ManagedObjectSource}.
	 * @param executionStrategies   {@link ExecutionStrategy} instances in index
	 *                              order for the {@link ManagedObjectSource}.
	 * @param executeLogger         {@link Logger} for the
	 *                              {@link ManagedObjectExecuteContext}.
	 * @param officeMetaData        {@link OfficeMetaData} to create
	 *                              {@link ProcessState} instances.
	 */
	public ManagedObjectExecuteManagerImpl(ManagedObjectMetaData<?> managedObjectMetaData, int processMoIndex,
			FlowMetaData[] processLinks, ThreadFactory[][] executionStrategies, Logger executeLogger,
			OfficeMetaData officeMetaData) {
		this.managedObjectMetaData = managedObjectMetaData;
		this.executionStrategies = executionStrategies;
		this.executeLogger = executeLogger;
		this.officeMetaData = officeMetaData;

		// Create the service context
		this.serviceContext = new ManagedObjectServiceContextImpl<>(managedObjectMetaData, processMoIndex, processLinks,
				officeMetaData);
	}

	/*
	 * =================== ManagedObjectExecuteManager =====================
	 */

	@Override
	public ManagedObjectExecuteContext<F> getManagedObjectExecuteContext() {
		return this.executeContext;
	}

	@Override
	public ManagedObjectExecuteStart<F> startComplete() {

		// Create the start ups
		ManagedObjectStartupFunction[] startupFunctions = this.managedObjectMetaData != null
				? this.managedObjectMetaData.getStartupFunctions()
				: new ManagedObjectStartupFunction[0];
		List<ManagedObjectStartupRunnable> startupRunnables = new ArrayList<>(
				startupFunctions.length + this.startupProcesses.size());
		for (ManagedObjectStartupFunction startupFunction : startupFunctions) {
			startupRunnables.add(new ManagedFunctionManagedObjectStartupRunnable(startupFunction, this.officeMetaData));
		}
		startupRunnables.addAll(this.startupProcesses);

		// Obtain and clear the start up processes
		ManagedObjectStartupRunnable[] startup = startupRunnables
				.toArray(new ManagedObjectStartupRunnable[startupRunnables.size()]);
		this.startupProcesses = null; // indicate start complete

		// Obtain the services
		@SuppressWarnings("unchecked")
		ManagedObjectService<F>[] services = this.services.toArray(new ManagedObjectService[this.services.size()]);

		// Return the execute start up
		return new ManagedObjectExecuteStartImpl(startup, services);
	}

	/**
	 * {@link ManagedObjectExecuteContext} implementation.
	 */
	private class ManagedObjectExecuteContextImpl implements ManagedObjectExecuteContext<F> {

		/*
		 * =============== ManagedObjectExecuteContext =============================
		 */

		@Override
		public Logger getLogger() {
			return ManagedObjectExecuteManagerImpl.this.executeLogger;
		}

		@Override
		public ThreadFactory[] getExecutionStrategy(int executionStrategyIndex) {

			// Easy access to manager
			ManagedObjectExecuteManagerImpl<F> manager = ManagedObjectExecuteManagerImpl.this;

			// Ensure valid execution strategy index
			if ((executionStrategyIndex < 0) || (executionStrategyIndex >= manager.executionStrategies.length)) {
				String validIndexes = (manager.executionStrategies.length == 0 ? " [no execution strategies linked]"
						: " [valid only 0 to " + (manager.executionStrategies.length - 1) + "]");
				throw new IllegalArgumentException(
						"Invalid execution strategy index " + executionStrategyIndex + validIndexes);
			}

			// Return the execution strategy
			return manager.executionStrategies[executionStrategyIndex];
		}

		@Override
		public ManagedObjectStartupProcess invokeStartupProcess(F key, Object parameter, ManagedObject managedObject,
				FlowCallback callback) throws IllegalArgumentException {
			return this.invokeStartupProcess(key.ordinal(), parameter, managedObject, callback);
		}

		@Override
		public ManagedObjectStartupProcess invokeStartupProcess(int flowIndex, Object parameter,
				ManagedObject managedObject, FlowCallback callback) throws IllegalArgumentException {

			// Easy access to manager
			ManagedObjectExecuteManagerImpl<F> manager = ManagedObjectExecuteManagerImpl.this;

			// Ensure not after start up
			if (manager.startupProcesses == null) {
				throw new IllegalStateException("May only register start up processes during start(...) method");
			}

			// Obtain the flow meta-data
			FlowMetaData flowMetaData = manager.serviceContext.getFlowMetaData(flowIndex);

			// Register execution of start up process
			ProcessStateManagedObjectStartupRunnable startupProcess = new ProcessStateManagedObjectStartupRunnable(
					manager, flowMetaData, parameter, managedObject, callback);
			manager.startupProcesses.add(startupProcess);

			// Return the start up process
			return startupProcess;
		}

		@Override
		public void addService(ManagedObjectService<F> service) {
			ManagedObjectExecuteManagerImpl.this.services.add(service);
		}
	}

	/**
	 * {@link ManagedObjectExecuteStart} implementation.
	 */
	private class ManagedObjectExecuteStartImpl implements ManagedObjectExecuteStart<F> {

		/**
		 * {@link ManagedObjectStartupRunnable} instances.
		 */
		private final ManagedObjectStartupRunnable[] startupRunnables;

		/**
		 * {@link ManagedObjectService} instances.
		 */
		private final ManagedObjectService<F>[] services;

		/**
		 * Instantiate.
		 * 
		 * @param startupRunnables {@link ManagedObjectStartupRunnable} instances.
		 * @param services         {@link ManagedObjectService} instances.
		 */
		private ManagedObjectExecuteStartImpl(ManagedObjectStartupRunnable[] startupRunnables,
				ManagedObjectService<F>[] services) {
			this.startupRunnables = startupRunnables;
			this.services = services;
		}

		/*
		 * ================== ManagedObjectExecuteStart ================
		 */

		@Override
		public ManagedObjectStartupRunnable[] getStartups() {
			return this.startupRunnables;
		}

		@Override
		public ManagedObjectService<F>[] getServices() {
			return this.services;
		}

		@Override
		public ManagedObjectServiceContext<F> getManagedObjectServiceContext() {
			return ManagedObjectExecuteManagerImpl.this.serviceContext;
		}
	}

	/**
	 * {@link ManagedObjectStartupRunnable} implementation for
	 * {@link ManagedFunction}.
	 */
	private static class ManagedFunctionManagedObjectStartupRunnable implements ManagedObjectStartupRunnable {

		/**
		 * {@link ManagedObjectStartupFunction}.
		 */
		private final ManagedObjectStartupFunction startupFunction;

		/**
		 * {@link OfficeMetaData}.
		 */
		private final OfficeMetaData officeMetaData;

		/**
		 * Instantiate.
		 * 
		 * @param startupFunction {@link ManagedObjectStartupFunction}.
		 * @param officeMetaData  {@link OfficeMetaData}.
		 */
		private ManagedFunctionManagedObjectStartupRunnable(ManagedObjectStartupFunction startupFunction,
				OfficeMetaData officeMetaData) {
			this.startupFunction = startupFunction;
			this.officeMetaData = officeMetaData;
		}

		/*
		 * ===================== ManagedObjectStartupRunnable ==================
		 */

		@Override
		public void run() {

			// Create and activate the startup function
			FunctionState startup = officeMetaData.createProcess(this.startupFunction.getFlowMetaData(),
					this.startupFunction.getParameter(), null, null);
			officeMetaData.getFunctionLoop().delegateFunction(startup);
		}

		@Override
		public boolean isConcurrent() {
			return false;
		}
	}

	/**
	 * {@link ManagedObjectStartupRunnable} implementation for {@link ProcessState}.
	 */
	private static class ProcessStateManagedObjectStartupRunnable
			implements ManagedObjectStartupProcess, ManagedObjectStartupRunnable {

		/**
		 * {@link ManagedObjectExecuteManagerImpl}.
		 */
		private final ManagedObjectExecuteManagerImpl<?> manager;

		/**
		 * {@link FlowMetaData} for the {@link ProcessState}.
		 */
		private FlowMetaData flowMetaData;

		/**
		 * Parameter. May be <code>null</code>.
		 */
		private final Object parameter;

		/**
		 * {@link ManagedObject} for the {@link ProcessState}.
		 */
		private final ManagedObject managedObject;

		/**
		 * {@link FlowCallback}. May be <code>null</code>.
		 */
		private final FlowCallback callback;

		/**
		 * Indicates if to undertake concurrently.
		 */
		private boolean isConcurrent = false;

		/**
		 * Instantiate.
		 * 
		 * @param manager       {@link ManagedObjectExecuteManagerImpl}.
		 * @param flowMetaData  {@link FlowMetaData} for the {@link ProcessState}.
		 * @param parameter     Parameter. May be <code>null</code>.
		 * @param managedObject {@link ManagedObject} for the {@link ProcessState}.
		 * @param callback      {@link FlowCallback}. May be <code>null</code>.
		 */
		private ProcessStateManagedObjectStartupRunnable(ManagedObjectExecuteManagerImpl<?> manager,
				FlowMetaData flowMetaData, Object parameter, ManagedObject managedObject, FlowCallback callback) {
			this.manager = manager;
			this.flowMetaData = flowMetaData;
			this.parameter = parameter;
			this.managedObject = managedObject;
			this.callback = callback;
		}

		/*
		 * ================= ManagedObjectStartupProcess =====================
		 */

		@Override
		public void setConcurrent(boolean isConcurrent) {
			this.isConcurrent = isConcurrent;
		}

		/*
		 * ================= ManagedObjectStartupRunnable ====================
		 */

		@Override
		public boolean isConcurrent() {
			return this.isConcurrent;
		}

		@Override
		public void run() {
			this.manager.serviceContext.invokeProcess(this.flowMetaData, this.parameter, this.managedObject, 0,
					this.callback);
		}
	}

}
