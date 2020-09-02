/*-
 * #%L
 * OfficeFrame
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package net.officefloor.frame.impl.execute.officefloor;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ThreadFactory;
import java.util.logging.Logger;

import net.officefloor.frame.api.executive.ExecutionStrategy;
import net.officefloor.frame.api.function.FlowCallback;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.source.ManagedObjectExecuteContext;
import net.officefloor.frame.api.managedobject.source.ManagedObjectService;
import net.officefloor.frame.api.managedobject.source.ManagedObjectServiceContext;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.api.managedobject.source.ManagedObjectStartupCompletion;
import net.officefloor.frame.api.managedobject.source.ManagedObjectStartupProcess;
import net.officefloor.frame.internal.structure.FlowMetaData;
import net.officefloor.frame.internal.structure.ManagedObjectExecuteManager;
import net.officefloor.frame.internal.structure.ManagedObjectExecuteStart;
import net.officefloor.frame.internal.structure.ManagedObjectMetaData;
import net.officefloor.frame.internal.structure.ManagedObjectServiceReady;
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
	 * {@link ExecutionStrategy} instances in index order for the
	 * {@link ManagedObjectSource}.
	 */
	private final ThreadFactory[][] executionStrategies;

	/**
	 * {@link Logger} for the {@link ManagedObjectExecuteContext}.
	 */
	private final Logger executeLogger;

	/**
	 * Object to notify on start up completion.
	 */
	private final Object startupNotify;

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
	 * Registered {@link ManagedObjectStartupRunnable} instances.
	 */
	private List<ManagedObjectStartupRunnable> startupProcesses = new LinkedList<>();

	/**
	 * {@link ManagedObjectServiceReady} instances.
	 */
	private List<ManagedObjectServiceReady> serviceReadiness = new LinkedList<>();

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
	 * @param startupNotify         Object to notify on start up completion.
	 */
	public ManagedObjectExecuteManagerImpl(ManagedObjectMetaData<?> managedObjectMetaData, int processMoIndex,
			FlowMetaData[] processLinks, ThreadFactory[][] executionStrategies, Logger executeLogger,
			OfficeMetaData officeMetaData, Object startupNotify) {
		this.executionStrategies = executionStrategies;
		this.executeLogger = executeLogger;
		this.startupNotify = startupNotify;

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

		// Obtain and clear the start up processes
		ManagedObjectStartupRunnable[] startup = this.startupProcesses
				.toArray(new ManagedObjectStartupRunnable[this.startupProcesses.size()]);
		this.startupProcesses = null; // indicate start complete

		// Obtain the service readiness
		ManagedObjectServiceReady[] serviceReadiness = this.serviceReadiness
				.toArray(new ManagedObjectServiceReady[this.serviceReadiness.size()]);

		// Obtain the services
		@SuppressWarnings("unchecked")
		ManagedObjectService<F>[] services = this.services.toArray(new ManagedObjectService[this.services.size()]);

		// Return the execute start up
		return new ManagedObjectExecuteStartImpl(startup, serviceReadiness, services);
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
		public ManagedObjectStartupProcess registerStartupProcess(F key, Object parameter, ManagedObject managedObject,
				FlowCallback callback) throws IllegalArgumentException {
			return this.registerStartupProcess(key.ordinal(), parameter, managedObject, callback);
		}

		@Override
		public ManagedObjectStartupProcess registerStartupProcess(int flowIndex, Object parameter,
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
			ManagedObjectStartupRunnableImpl startupProcess = new ManagedObjectStartupRunnableImpl(manager,
					flowMetaData, parameter, managedObject, callback);
			manager.startupProcesses.add(startupProcess);

			// Return the start up process
			return startupProcess;
		}

		@Override
		public ManagedObjectStartupCompletion createStartupCompletion() {
			ManagedObjectStartupCompletionImpl startupCompletion = new ManagedObjectStartupCompletionImpl();
			ManagedObjectExecuteManagerImpl.this.serviceReadiness.add(startupCompletion);
			return startupCompletion;
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
		 * {@link ManagedObjectServiceReady} instances.
		 */
		private final ManagedObjectServiceReady[] serviceReadiness;

		/**
		 * {@link ManagedObjectService} instances.
		 */
		private final ManagedObjectService<F>[] services;

		/**
		 * Instantiate.
		 * 
		 * @param startupRunnables {@link ManagedObjectStartupRunnable} instances.
		 * @param serviceReadiness {@link ManagedObjectServiceReady} instances.
		 * @param services         {@link ManagedObjectService} instances.
		 */
		private ManagedObjectExecuteStartImpl(ManagedObjectStartupRunnable[] startupRunnables,
				ManagedObjectServiceReady[] serviceReadiness, ManagedObjectService<F>[] services) {
			this.startupRunnables = startupRunnables;
			this.serviceReadiness = serviceReadiness;
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
		public ManagedObjectServiceReady[] getServiceReadiness() {
			return this.serviceReadiness;
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
	 * {@link ManagedObjectStartupRunnable} implementation.
	 */
	private static class ManagedObjectStartupRunnableImpl
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
		private ManagedObjectStartupRunnableImpl(ManagedObjectExecuteManagerImpl<?> manager, FlowMetaData flowMetaData,
				Object parameter, ManagedObject managedObject, FlowCallback callback) {
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

	/**
	 * {@link ManagedObjectStartupCompletion} implementation.
	 */
	private class ManagedObjectStartupCompletionImpl
			implements ManagedObjectStartupCompletion, ManagedObjectServiceReady {

		/**
		 * Indicates if complete.
		 */
		private boolean isComplete = false;

		/**
		 * Possible start up failure.
		 */
		private Exception startupFailure = null;

		/*
		 * ===================== ManagedObjectStartupCompletion ===================
		 */

		@Override
		public void complete() {
			synchronized (ManagedObjectExecuteManagerImpl.this.startupNotify) {

				// Flag complete
				this.isComplete = true;

				// Notify to continue start up
				ManagedObjectExecuteManagerImpl.this.startupNotify.notify();
			}
		}

		@Override
		public void failOpen(Exception cause) {
			synchronized (ManagedObjectExecuteManagerImpl.this.startupNotify) {

				// Flag failure
				this.startupFailure = cause;

				// Notify to fail start up
				ManagedObjectExecuteManagerImpl.this.startupNotify.notify();
			}
		}

		/*
		 * ======================== ManagedObjectServiceReady =====================
		 */

		@Override
		public boolean isServiceReady() throws Exception {
			synchronized (ManagedObjectExecuteManagerImpl.this.startupNotify) {

				// Propagate possible failure
				if (this.startupFailure != null) {
					throw this.startupFailure;
				}

				// Return whether complete
				return this.isComplete;
			}
		}
	}

}