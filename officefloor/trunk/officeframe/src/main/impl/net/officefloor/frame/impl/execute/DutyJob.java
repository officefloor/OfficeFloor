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
package net.officefloor.frame.impl.execute;

import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.internal.structure.AdministratorContext;
import net.officefloor.frame.internal.structure.AdministratorMetaData;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.internal.structure.FlowMetaData;
import net.officefloor.frame.internal.structure.JobNode;
import net.officefloor.frame.internal.structure.TaskDutyAssociation;
import net.officefloor.frame.internal.structure.ThreadState;
import net.officefloor.frame.internal.structure.WorkContainer;
import net.officefloor.frame.spi.administration.Duty;
import net.officefloor.frame.spi.team.Job;

/**
 * {@link Duty} implementation for a {@link Job}.
 * 
 * @author Daniel
 */
public class DutyJob<W extends Work, I, A extends Enum<A>> extends
		AbstractJobContainer<W, AdministratorMetaData<I, A>> {

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
	 */
	public DutyJob(Flow flow, WorkContainer<W> workContainer,
			AdministratorMetaData<I, A> adminMetaData,
			TaskDutyAssociation<A> taskDutyAssociation, JobNode parallelOwner) {
		super(flow, workContainer, adminMetaData, parallelOwner);
		this.taskDutyAssociation = taskDutyAssociation;
	}

	/*
	 * ======================= JobContainer ==========================
	 */

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.officefloor.frame.impl.execute.JobImpl#executeJob(net.officefloor
	 * .frame.impl.execute.JobExecuteContext)
	 */
	@Override
	protected Object executeJob(JobExecuteContext context) throws Throwable {

		// Administer the duty
		this.workContainer.administerManagedObjects(this.taskDutyAssociation,
				this.administratorContext);

		// Administration duties do not pass on parameters
		return null;
	}

	/**
	 * {@link AdministratorContext} implementations.
	 */
	private class AdministratorContextImpl implements AdministratorContext {

		/*
		 * (non-Javadoc)
		 * 
		 * @seenet.officefloor.frame.internal.structure.AdministratorContext#
		 * getThreadState()
		 */
		@Override
		public ThreadState getThreadState() {
			return DutyJob.this.flow.getThreadState();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * net.officefloor.frame.internal.structure.AdministratorContext#doFlow
		 * (net.officefloor.frame.internal.structure.FlowMetaData,
		 * java.lang.Object)
		 */
		@Override
		public void doFlow(FlowMetaData<?> flowMetaData, Object parameter) {
			DutyJob.this.doFlow(flowMetaData, parameter);
		}
	}

}
