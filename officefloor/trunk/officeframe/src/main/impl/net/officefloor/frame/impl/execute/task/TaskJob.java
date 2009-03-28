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
package net.officefloor.frame.impl.execute.task;

import net.officefloor.frame.api.execute.FlowFuture;
import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.execute.TaskContext;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.impl.execute.job.AbstractJobContainer;
import net.officefloor.frame.impl.execute.job.JobExecuteContext;
import net.officefloor.frame.impl.execute.managedobject.ManagedObjectIndexImpl;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.internal.structure.FlowMetaData;
import net.officefloor.frame.internal.structure.JobNode;
import net.officefloor.frame.internal.structure.ManagedObjectIndex;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.frame.internal.structure.TaskMetaData;
import net.officefloor.frame.internal.structure.WorkContainer;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.team.Job;

/**
 * {@link Task} implementation of a {@link Job}.
 * 
 * @author Daniel
 */
public class TaskJob<W extends Work, D extends Enum<D>, F extends Enum<F>>
		extends AbstractJobContainer<W, TaskMetaData<W, D, F>> {

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
	 * @param parallelOwner
	 *            Parallel owning {@link JobNode}.
	 * @param parameter
	 *            Parameter for the {@link Task}.
	 */
	public TaskJob(Flow flow, WorkContainer<W> workContainer,
			TaskMetaData<W, D, F> taskMetaData, JobNode parallelOwner,
			Object parameter) {
		super(flow, workContainer, taskMetaData, parallelOwner, taskMetaData
				.getRequiredManagedObjects());
		this.parameter = parameter;

		// Create the task
		this.task = this.nodeMetaData.getTaskFactory().createTask(
				this.workContainer.getWork(flow.getThreadState()));
	}

	/*
	 * ====================== JobContainer ==========================
	 */

	@Override
	protected Object executeJob(JobExecuteContext context) throws Throwable {
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
			return TaskJob.this.workContainer.getWork(TaskJob.this.flow
					.getThreadState());
		}

		@Override
		public Object getObject(D key) {
			return this.getObject(key.ordinal());
		}

		@Override
		public Object getObject(int managedObjectIndex) {

			// Obtain the work managed object index
			ManagedObjectIndex index = TaskJob.this.nodeMetaData
					.translateManagedObjectIndexForWork(managedObjectIndex);

			// Determine if a parameter
			if (index.getIndexOfManagedObjectWithinScope() == PARAMETER_INDEX) {
				return TaskJob.this.parameter; // parameter, so return the
				// parameter
			}

			// Return the Object
			return TaskJob.this.workContainer.getObject(index,
					TaskJob.this.flow.getThreadState());
		}

		@Override
		public FlowFuture doFlow(F key, Object parameter) {
			return this.doFlow(key.ordinal(), parameter);
		}

		@Override
		public FlowFuture doFlow(int flowIndex, Object parameter) {
			// Obtain the Flow meta-data and do the flow
			FlowMetaData<?> flowMetaData = TaskJob.this.nodeMetaData
					.getFlow(flowIndex);
			return TaskJob.this.doFlow(flowMetaData, parameter);
		}

		@Override
		public void join(FlowFuture flowFuture) {
			TaskJob.this.joinFlow(flowFuture);
		}

		@Override
		public Object getProcessLock() {
			return TaskJob.this.flow.getThreadState().getProcessState()
					.getProcessLock();
		}

		@Override
		public void setComplete(boolean isComplete) {
			TaskJob.this.setJobComplete(isComplete);
		}
	}

}