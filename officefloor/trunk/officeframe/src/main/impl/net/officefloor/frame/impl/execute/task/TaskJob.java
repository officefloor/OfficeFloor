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
public class TaskJob<P, W extends Work, M extends Enum<M>, F extends Enum<F>>
		extends AbstractJobContainer<W, TaskMetaData<P, W, M, F>> implements
		TaskContext<P, W, M, F> {

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
	 * {@link Task} to be done.
	 */
	private final Task<P, W, M, F> task;

	/**
	 * Parameter of the current {@link Task} being managed.
	 */
	private final P parameter;

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
			TaskMetaData<P, W, M, F> taskMetaData, JobNode parallelOwner,
			P parameter) {
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
		return this.task.doTask(this);
	}

	/*
	 * ====================== TaskContext ===========================
	 */

	@Override
	public W getWork() {
		return this.workContainer.getWork(this.flow.getThreadState());
	}

	@Override
	public P getParameter() {
		return this.parameter;
	}

	@Override
	public Object getObject(M key) {
		return this.getObject(key.ordinal());
	}

	@Override
	public Object getObject(int managedObjectIndex) {

		// Obtain the work managed object index
		ManagedObjectIndex index = this.nodeMetaData
				.translateManagedObjectIndexForWork(managedObjectIndex);

		// Determine if a parameter
		if (index.getIndexOfManagedObjectWithinScope() == PARAMETER_INDEX) {
			return this.parameter; // parameter, so return the parameter
		}

		// Return the Object
		return this.workContainer.getObject(index, this.flow.getThreadState());
	}

	@Override
	public FlowFuture doFlow(F key, Object parameter) {
		return this.doFlow(key.ordinal(), parameter);
	}

	@Override
	public FlowFuture doFlow(int flowIndex, Object parameter) {
		// Obtain the Flow meta-data and do the flow
		FlowMetaData<?> flowMetaData = this.nodeMetaData.getFlow(flowIndex);
		return this.doFlow(flowMetaData, parameter);
	}

	@Override
	public void join(FlowFuture flowFuture) {
		this.joinFlow(flowFuture);
	}

	@Override
	public Object getProcessLock() {
		return this.flow.getThreadState().getProcessState().getProcessLock();
	}

	@Override
	public void setComplete(boolean isComplete) {
		this.setJobComplete(isComplete);
	}

}