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
package net.officefloor.frame.impl.execute.managedfunction;

import net.officefloor.frame.api.execute.FlowCallback;
import net.officefloor.frame.api.execute.ManagedFunction;
import net.officefloor.frame.api.execute.ManagedFunctionContext;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.api.manage.InvalidParameterTypeException;
import net.officefloor.frame.api.manage.UnknownFunctionException;
import net.officefloor.frame.api.manage.UnknownWorkException;
import net.officefloor.frame.impl.execute.function.ManagedFunctionContainerImpl;
import net.officefloor.frame.impl.execute.managedobject.ManagedObjectIndexImpl;
import net.officefloor.frame.internal.structure.AssetManager;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.internal.structure.FlowInstigationStrategyEnum;
import net.officefloor.frame.internal.structure.FlowMetaData;
import net.officefloor.frame.internal.structure.GovernanceDeactivationStrategy;
import net.officefloor.frame.internal.structure.ManagedFunctionContainer;
import net.officefloor.frame.internal.structure.ManagedFunctionLogicContext;
import net.officefloor.frame.internal.structure.ManagedFunctionMetaData;
import net.officefloor.frame.internal.structure.ManagedObjectIndex;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.frame.internal.structure.ProcessState;
import net.officefloor.frame.internal.structure.WorkContainer;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.team.Job;

/**
 * {@link ManagedFunction} implementation of a {@link Job}.
 * 
 * @author Daniel Sagenschneider
 */
public class ManagedFunctionImpl<W extends Work, D extends Enum<D>, F extends Enum<F>>
		extends ManagedFunctionContainerImpl<W, ManagedFunctionMetaData<W, D, F>> {

	/**
	 * <p>
	 * Scope index of the {@link ManagedObjectIndex} indicating the parameter
	 * rather than the object of the {@link ManagedObject}.
	 * <p>
	 * As {@link ManagedObjectIndex} are indexes into arrays, the negative value
	 * is safe to use as a parameter index.
	 */
	public static final int PARAMETER_INDEX = -1;

	/**
	 * {@link ManagedObjectIndex} for the parameter.
	 */
	public static final ManagedObjectIndex PARAMETER_MANAGED_OBJECT_INDEX = new ManagedObjectIndexImpl(
			ManagedObjectScope.WORK, PARAMETER_INDEX);

	/**
	 * {@link ManagedFunctionContext} that exposes only the required
	 * functionality.
	 */
	private final ManagedFunctionContext<W, D, F> taskContextToken = new TaskContextToken();

	/**
	 * {@link ManagedFunction} to be done.
	 */
	private final ManagedFunction<W, D, F> function;

	/**
	 * Parameter of the current {@link ManagedFunction} being managed.
	 */
	private final Object parameter;

	/**
	 * Initiate.
	 * 
	 * @param flow
	 *            {@link Flow}.
	 * @param workContainer
	 *            {@link WorkContainer}.
	 * @param taskMetaData
	 *            {@link ManagedFunctionMetaData}.
	 * @param governanceDeactivationStrategy
	 *            {@link GovernanceDeactivationStrategy}.
	 * @param parallelOwner
	 *            Parallel owning {@link ManagedFunctionContainerImpl}.
	 * @param parameter
	 *            Parameter for the {@link ManagedFunction}.
	 */
	public ManagedFunctionImpl(Flow flow, WorkContainer<W> workContainer, ManagedFunctionMetaData<W, D, F> taskMetaData,
			GovernanceDeactivationStrategy governanceDeactivationStrategy, ManagedFunctionContainerImpl parallelOwner,
			Object parameter) {
		super(flow, workContainer, taskMetaData, parallelOwner, taskMetaData.getRequiredManagedObjects(),
				taskMetaData.getRequiredGovernance(), governanceDeactivationStrategy);
		this.parameter = parameter;

		// Create the function
		this.function = this.functionContainerMetaData.getManagedFunctionFactory()
				.createManagedFunction(this.workContainer.getWork(flow.getThreadState()));
	}

	/*
	 * ====================== ManagedFunction ==========================
	 */

	@Override
	protected Object executeFunction(ManagedFunctionLogicContext context) throws Throwable {
		return this.function.execute(this.taskContextToken);
	}

	/**
	 * <p>
	 * Token class given to the {@link ManagedFunction}.
	 * <p>
	 * As application code will be provided a {@link ManagedFunctionContext}
	 * this exposes just the necessary functionality and prevents access to
	 * internals of the framework.
	 */
	private final class TaskContextToken implements ManagedFunctionContext<W, D, F> {

		/*
		 * ====================== TaskContext ===========================
		 */

		@Override
		public W getWork() {
			return ManagedFunctionImpl.this.workContainer.getWork(ManagedFunctionImpl.this.flow.getThreadState());
		}

		@Override
		public Object getObject(D key) {
			return this.getObject(key.ordinal());
		}

		@Override
		public Object getObject(int managedObjectIndex) {

			// Obtain the work managed object index
			ManagedObjectIndex index = ManagedFunctionImpl.this.functionContainerMetaData
					.translateManagedObjectIndexForWork(managedObjectIndex);

			// Determine if a parameter
			if (index.getIndexOfManagedObjectWithinScope() == PARAMETER_INDEX) {
				return ManagedFunctionImpl.this.parameter; // parameter, so
															// return the
				// parameter
			}

			// Return the Object
			return ManagedFunctionImpl.this.workContainer.getObject(index);
		}

		@Override
		public void doFlow(F key, Object parameter, FlowCallback callback) {
			this.doFlow(key.ordinal(), parameter, callback);
		}

		@Override
		public void doFlow(int flowIndex, Object parameter, FlowCallback callback) {
			// Obtain the Flow meta-data and do the flow
			FlowMetaData<?> flowMetaData = ManagedFunctionImpl.this.functionContainerMetaData.getFlow(flowIndex);
			ManagedFunctionImpl.this.doFlow(flowMetaData, parameter, callback);
		}

		@Override
		@SuppressWarnings("rawtypes")
		public void doFlow(String workName, String taskName, Object parameter, FlowCallback callback)
				throws UnknownWorkException, UnknownFunctionException, InvalidParameterTypeException {

			// Obtain the Process State
			ProcessState processState = ManagedFunctionImpl.this.flow.getThreadState().getProcessState();

			// Obtain the Task meta-data
			final ManagedFunctionMetaData<?, ?, ?> taskMetaData = processState.getTaskMetaData(workName, taskName);

			// Invoke the Flow
			ManagedFunctionImpl.this.doFlow(new FlowMetaData() {
				@Override
				public ManagedFunctionMetaData<?, ?, ?> getInitialTaskMetaData() {
					return taskMetaData;
				}

				@Override
				public FlowInstigationStrategyEnum getInstigationStrategy() {
					// Always instigated in parallel
					return FlowInstigationStrategyEnum.PARALLEL;
				}

				@Override
				public AssetManager getFlowManager() {
					// Asset Manager not required
					return null;
				}
			}, parameter, callback);
		}
	}

}