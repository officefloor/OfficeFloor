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
package net.officefloor.frame.impl.execute.duty;

import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.impl.execute.jobnode.AbstractManagedJobNodeContainer;
import net.officefloor.frame.internal.structure.AdministratorContext;
import net.officefloor.frame.internal.structure.AdministratorMetaData;
import net.officefloor.frame.internal.structure.JobNodeActivateSet;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.internal.structure.ManagedJobNodeContext;
import net.officefloor.frame.internal.structure.FlowMetaData;
import net.officefloor.frame.internal.structure.JobNode;
import net.officefloor.frame.internal.structure.TaskDutyAssociation;
import net.officefloor.frame.internal.structure.TaskMetaData;
import net.officefloor.frame.internal.structure.ThreadState;
import net.officefloor.frame.internal.structure.WorkContainer;
import net.officefloor.frame.spi.administration.Duty;
import net.officefloor.frame.spi.team.Job;
import net.officefloor.frame.spi.team.JobContext;

/**
 * {@link Duty} implementation for a {@link Job}.
 * 
 * @author Daniel Sagenschneider
 */
public class DutyJob<W extends Work, I, A extends Enum<A>> extends
		AbstractManagedJobNodeContainer<W, AdministratorMetaData<I, A>> {

	/**
	 * {@link TaskDutyAssociation}.
	 */
	private final TaskDutyAssociation<A> taskDutyAssociation;

	/**
	 * {@link AdministratorContext}.
	 */
	private final AdministratorContext administratorContext = new AdministratorContextImpl();

	/**
	 * Initiate.
	 * 
	 * @param flow
	 *            {@link Flow}.
	 * @param workContainer
	 *            {@link WorkContainer}.
	 * @param adminMetaData
	 *            {@link AdministratorMetaData}.
	 * @param taskDutyAssociation
	 *            {@link TaskDutyAssociation}.
	 * @param parallelOwner
	 *            Parallel owning {@link JobNode}.
	 * @param administeringTaskMetaData
	 *            {@link TaskMetaData} of the {@link Task} being administered.
	 */
	public DutyJob(Flow flow, WorkContainer<W> workContainer,
			AdministratorMetaData<I, A> adminMetaData,
			TaskDutyAssociation<A> taskDutyAssociation, JobNode parallelOwner,
			TaskMetaData<?, ?, ?> administeringTaskMetaData) {
		super(flow, workContainer, adminMetaData, parallelOwner,
				administeringTaskMetaData.getRequiredManagedObjects(), null,
				null);
		this.taskDutyAssociation = taskDutyAssociation;
	}

	/*
	 * ======================= JobContainer ==========================
	 */

	@Override
	protected void loadJobName(StringBuilder message) {
		message.append("Duty");
	}

	@Override
	protected Object executeJob(ManagedJobNodeContext context,
			JobContext jobContext, JobNodeActivateSet activateSet)
			throws Throwable {

		// Administer the duty
		this.workContainer.administerManagedObjects(this.taskDutyAssociation,
				this.administratorContext, this);

		// Administration duties do not pass on parameters
		return null;
	}

	/**
	 * {@link AdministratorContext} implementations.
	 */
	private class AdministratorContextImpl implements AdministratorContext {

		/*
		 * ======================= AdministratorContext =======================
		 */

		@Override
		public ThreadState getThreadState() {
			return DutyJob.this.flow.getThreadState();
		}

		@Override
		public void doFlow(FlowMetaData<?> flowMetaData, Object parameter) {
			DutyJob.this.doFlow(flowMetaData, parameter);
		}
	}

}