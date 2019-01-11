/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package net.officefloor.frame.impl.execute.officefloor;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ThreadFactory;

import net.officefloor.frame.api.executive.ExecutionStrategy;
import net.officefloor.frame.api.function.FlowCallback;
import net.officefloor.frame.api.manage.InvalidParameterTypeException;
import net.officefloor.frame.api.manage.ProcessManager;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.source.ManagedObjectExecuteContext;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.internal.structure.Execution;
import net.officefloor.frame.internal.structure.FlowMetaData;
import net.officefloor.frame.internal.structure.ManagedObjectExecuteManager;
import net.officefloor.frame.internal.structure.ManagedObjectMetaData;
import net.officefloor.frame.internal.structure.OfficeMetaData;
import net.officefloor.frame.internal.structure.ProcessState;

/**
 * {@link ManagedObjectExecuteContext} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class ManagedObjectExecuteManagerImpl<F extends Enum<F>> implements ManagedObjectExecuteManager<F> {

	/**
	 * {@link ManagedObjectMetaData} of the {@link ManagedObject}.
	 */
	private final ManagedObjectMetaData<?> managedObjectMetaData;

	/**
	 * Index of the {@link ManagedObject} within the {@link ProcessState}.
	 */
	private final int processMoIndex;

	/**
	 * {@link FlowMetaData} in index order for the {@link ManagedObjectSource}.
	 */
	private final FlowMetaData[] processLinks;

	/**
	 * {@link ExecutionStrategy} instances in index order for the
	 * {@link ManagedObjectSource}.
	 */
	private final ThreadFactory[][] executionStrategies;

	/**
	 * {@link OfficeMetaData} to create {@link ProcessState} instances.
	 */
	private final OfficeMetaData officeMetaData;

	/**
	 * {@link ManagedObjectExecuteContext}.
	 */
	private final ManagedObjectExecuteContext<F> executeContext = new ManagedObjectExecuteContextImpl();

	/**
	 * Registered start up {@link ProcessState} instances.
	 */
	private final List<Runnable> startupProcesses = new LinkedList<>();

	/**
	 * Indicate if processing.
	 */
	private boolean isProcessing = false;

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
	 * @param officeMetaData        {@link OfficeMetaData} to create
	 *                              {@link ProcessState} instances.
	 */
	public ManagedObjectExecuteManagerImpl(ManagedObjectMetaData<?> managedObjectMetaData, int processMoIndex,
			FlowMetaData[] processLinks, ThreadFactory[][] executionStrategies, OfficeMetaData officeMetaData) {
		this.managedObjectMetaData = managedObjectMetaData;
		this.processMoIndex = processMoIndex;
		this.processLinks = processLinks;
		this.executionStrategies = executionStrategies;
		this.officeMetaData = officeMetaData;
	}

	/**
	 * Obtains the {@link FlowMetaData}.
	 * 
	 * @param flowIndex Index of the {@link FlowMetaData}.
	 * @return {@link FlowMetaData}.
	 */
	private FlowMetaData getFlowMetaData(int flowIndex) {

		// Ensure valid flow meta-data
		if ((flowIndex < 0) || (flowIndex >= this.processLinks.length)) {
			String validIndexes = (this.processLinks.length == 0 ? " [no processes linked]"
					: " [valid only 0 to " + (this.processLinks.length - 1) + "]");
			throw new IllegalArgumentException("Invalid process index " + flowIndex + validIndexes);
		}

		// Return the flow meta-data
		return this.processLinks[flowIndex];
	}

	/**
	 * Invokes the {@link ProcessState} for the {@link FlowMetaData}.
	 * 
	 * @param flowMetaData  {@link FlowMetaData}.
	 * @param parameter     Parameter.
	 * @param managedObject {@link ManagedObject}.
	 * @param delay         Possible delay.
	 * @param callback      {@link FlowCallback}.
	 * @return {@link ProcessManager}.
	 */
	private ProcessManager invokeProcess(FlowMetaData flowMetaData, Object parameter, ManagedObject managedObject,
			long delay, FlowCallback callback) {

		// Ensure execution is managed
		Execution<RuntimeException> execution = () -> {
			try {

				// Invoke the process
				return this.officeMetaData.invokeProcess(flowMetaData, parameter, delay, callback, null, managedObject,
						this.managedObjectMetaData, this.processMoIndex);
			} catch (InvalidParameterTypeException ex) {
				// Propagate (unlikely so no need for checked exception)
				throw new IllegalArgumentException(ex);
			}
		};
		return this.officeMetaData.getManagedExecutionFactory()
				.createManagedExecution(this.officeMetaData.getExecutive(), execution).managedExecute();
	}

	/*
	 * =================== ManagedObjectExecuteManager =====================
	 */

	@Override
	public ManagedObjectExecuteContext<F> getManagedObjectExecuteContext() {
		return this.executeContext;
	}

	@Override
	public Runnable[] startComplete() {

		// Indicate processing (as start complete)
		this.isProcessing = true;

		// Obtain and clear the start up processes
		Runnable[] startup = this.startupProcesses.toArray(new Runnable[0]);
		this.startupProcesses.clear();

		// Return the startup processes
		return startup;
	}

	/**
	 * {@link ManagedObjectExecuteContext} implementation.
	 */
	private class ManagedObjectExecuteContextImpl implements ManagedObjectExecuteContext<F> {

		/*
		 * =============== ManagedObjectExecuteContext =============================
		 */

		@Override
		public void registerStartupProcess(F key, Object parameter, ManagedObject managedObject, FlowCallback callback)
				throws IllegalArgumentException {
			this.registerStartupProcess(key.ordinal(), parameter, managedObject, callback);
		}

		@Override
		public void registerStartupProcess(int flowIndex, Object parameter, ManagedObject managedObject,
				FlowCallback callback) throws IllegalArgumentException {

			// Easy access to manager
			ManagedObjectExecuteManagerImpl<F> manager = ManagedObjectExecuteManagerImpl.this;

			// Ensure not processing
			if (manager.isProcessing) {
				throw new IllegalStateException("May only register start up processes during start(...) method");
			}

			// Obtain the flow meta-data
			FlowMetaData flowMetaData = manager.getFlowMetaData(flowIndex);

			// Register execution of start up process
			manager.startupProcesses.add(() -> {
				manager.invokeProcess(flowMetaData, parameter, managedObject, 0, callback);
			});
		}

		@Override
		public ProcessManager invokeProcess(F key, Object parameter, ManagedObject managedObject, long delay,
				FlowCallback callback) {
			return this.invokeProcess(key.ordinal(), parameter, managedObject, delay, callback);
		}

		@Override
		public ProcessManager invokeProcess(int flowIndex, Object parameter, ManagedObject managedObject, long delay,
				FlowCallback callback) {

			// Easy access to manager
			ManagedObjectExecuteManagerImpl<F> manager = ManagedObjectExecuteManagerImpl.this;

			// Ensure processing
			if (!manager.isProcessing) {
				throw new IllegalStateException("During start(...) method, may only register start up processes");
			}

			// Obtain the flow meta-data
			FlowMetaData flowMetaData = manager.getFlowMetaData(flowIndex);

			// Invoke the process
			return manager.invokeProcess(flowMetaData, parameter, managedObject, delay, callback);
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
	}

}