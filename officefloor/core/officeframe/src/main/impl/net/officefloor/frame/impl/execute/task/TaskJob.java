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
package net.officefloor.frame.impl.execute.task;

import net.officefloor.frame.api.execute.FlowFuture;
import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.execute.TaskContext;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.api.manage.InvalidParameterTypeException;
import net.officefloor.frame.api.manage.UnknownTaskException;
import net.officefloor.frame.api.manage.UnknownWorkException;
import net.officefloor.frame.impl.execute.job.AbstractManagedJobNodeContainer;
import net.officefloor.frame.impl.execute.managedobject.ManagedObjectIndexImpl;
import net.officefloor.frame.internal.structure.AssetManager;
import net.officefloor.frame.internal.structure.GovernanceDeactivationStrategy;
import net.officefloor.frame.internal.structure.JobNodeActivateSet;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.internal.structure.ManagedJobNodeContext;
import net.officefloor.frame.internal.structure.FlowInstigationStrategyEnum;
import net.officefloor.frame.internal.structure.FlowMetaData;
import net.officefloor.frame.internal.structure.JobNode;
import net.officefloor.frame.internal.structure.ManagedObjectIndex;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.frame.internal.structure.ProcessState;
import net.officefloor.frame.internal.structure.TaskMetaData;
import net.officefloor.frame.internal.structure.WorkContainer;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.managedobject.source.CriticalSection;
import net.officefloor.frame.spi.team.Job;
import net.officefloor.frame.spi.team.JobContext;

/**
 * {@link Task} implementation of a {@link Job}.
 * 
 * @author Daniel Sagenschneider
 */
public class TaskJob<W extends Work, D extends Enum<D>, F extends Enum<F>>
		extends AbstractManagedJobNodeContainer<W, TaskMetaData<W, D, F>> {

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
	 * {@link TaskContext} that exposes only the required functionality.
	 */
	private final TaskContext<W, D, F> taskContextToken = new TaskContextToken();

	/**
	 * {@link Task} to be done.
	 */
	private final Task<W, D, F> task;

	/**
	 * Parameter of the current {@link Task} being managed.
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
	 *            {@link TaskMetaData}.
	 * @param governanceDeactivationStrategy
	 *            {@link GovernanceDeactivationStrategy}.
	 * @param parallelOwner
	 *            Parallel owning {@link JobNode}.
	 * @param parameter
	 *            Parameter for the {@link Task}.
	 */
	public TaskJob(Flow flow, WorkContainer<W> workContainer, TaskMetaData<W, D, F> taskMetaData,
			GovernanceDeactivationStrategy governanceDeactivationStrategy, JobNode parallelOwner, Object parameter) {
		super(flow, workContainer, taskMetaData, parallelOwner, taskMetaData.getRequiredManagedObjects(),
				taskMetaData.getRequiredGovernance(), governanceDeactivationStrategy);
		this.parameter = parameter;

		// Create the task
		this.task = this.nodeMetaData.getTaskFactory().createTask(this.workContainer.getWork(flow.getThreadState()));
	}

	/*
	 * ====================== JobContainer ==========================
	 */

	@Override
	protected void loadJobName(StringBuilder message) {
		message.append("Task ");
		message.append(this.nodeMetaData.getTaskName());
		message.append("(");
		message.append(this.parameter);
		message.append(")");
		message.append(" of Work ");
		message.append(this.nodeMetaData.getWorkMetaData().getWorkName());
	}

	@Override
	protected Object executeJob(ManagedJobNodeContext context, JobContext jobContext, JobNodeActivateSet activateSet)
			throws Throwable {
		// Execute the task
		return this.task.doTask(this.taskContextToken);
	}

	/**
	 * <p>
	 * Token class given to the {@link Task}.
	 * <p>
	 * As application code will be provided a {@link TaskContext} this exposes
	 * just the necessary functionality and prevents access to internals of the
	 * framework.
	 */
	private final class TaskContextToken implements TaskContext<W, D, F> {

		/*
		 * ====================== TaskContext ===========================
		 */

		@Override
		public W getWork() {
			return TaskJob.this.workContainer.getWork(TaskJob.this.flow.getThreadState());
		}

		@Override
		public Object getObject(D key) {
			return this.getObject(key.ordinal());
		}

		@Override
		public Object getObject(int managedObjectIndex) {

			// Obtain the work managed object index
			ManagedObjectIndex index = TaskJob.this.nodeMetaData.translateManagedObjectIndexForWork(managedObjectIndex);

			// Determine if a parameter
			if (index.getIndexOfManagedObjectWithinScope() == PARAMETER_INDEX) {
				return TaskJob.this.parameter; // parameter, so return the
				// parameter
			}

			// Return the Object
			return TaskJob.this.workContainer.getObject(index, TaskJob.this.flow.getThreadState());
		}

		@Override
		public FlowFuture doFlow(F key, Object parameter) {
			return this.doFlow(key.ordinal(), parameter);
		}

		@Override
		public FlowFuture doFlow(int flowIndex, Object parameter) {
			// Obtain the Flow meta-data and do the flow
			FlowMetaData<?> flowMetaData = TaskJob.this.nodeMetaData.getFlow(flowIndex);
			return TaskJob.this.doFlow(flowMetaData, parameter);
		}

		@Override
		@SuppressWarnings("rawtypes")
		public void doFlow(String workName, String taskName, Object parameter)
				throws UnknownWorkException, UnknownTaskException, InvalidParameterTypeException {

			// Obtain the Process State
			ProcessState processState = TaskJob.this.flow.getThreadState().getProcessState();

			// Obtain the Task meta-data
			final TaskMetaData<?, ?, ?> taskMetaData = processState.getTaskMetaData(workName, taskName);

			// Invoke the Flow
			TaskJob.this.doFlow(new FlowMetaData() {
				@Override
				public TaskMetaData<?, ?, ?> getInitialTaskMetaData() {
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
			}, parameter);
		}

		@Override
		public void join(FlowFuture flowFuture, long timeout, Object token) {
			TaskJob.this.joinFlow(flowFuture, timeout, token);
		}

		@Override
		public void setComplete(boolean isComplete) {
			TaskJob.this.setJobComplete(isComplete);
		}
	}

}