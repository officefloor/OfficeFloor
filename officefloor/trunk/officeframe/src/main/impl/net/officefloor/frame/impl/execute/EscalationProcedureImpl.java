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

import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.build.TaskFactory;
import net.officefloor.frame.api.build.WorkFactory;
import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.execute.TaskContext;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.api.execute.WorkContext;
import net.officefloor.frame.internal.structure.AdministratorMetaData;
import net.officefloor.frame.internal.structure.Escalation;
import net.officefloor.frame.internal.structure.EscalationProcedure;
import net.officefloor.frame.internal.structure.FlowInstigationStrategyEnum;
import net.officefloor.frame.internal.structure.FlowMetaData;
import net.officefloor.frame.internal.structure.ManagedObjectMetaData;
import net.officefloor.frame.internal.structure.TaskDutyAssociation;
import net.officefloor.frame.internal.structure.WorkMetaData;
import net.officefloor.frame.spi.team.Team;

/**
 * Implementation of the
 * {@link net.officefloor.frame.internal.structure.EscalationProcedure}.
 * 
 * @author Daniel
 */
public class EscalationProcedureImpl implements EscalationProcedure {

	/**
	 * Parent {@link EscalationProcedure} to be taken if the provided
	 * {@link Escalation} instances for this {@link EscalationProcedure} do not
	 * handle the escalation.
	 */
	protected final EscalationProcedure parentEscalationProcedure;

	/**
	 * {@link Escalation} instances in order for this procedure.
	 */
	protected final Escalation[] escalations;

	/**
	 * Initiate with {@link Escalation} details.
	 * 
	 * @param parentEscalationProcedure
	 *            {@link EscalationProcedure} to be taken if the
	 *            {@link Escalation} instances for this
	 *            {@link EscalationProcedure} do not handle the escalation.
	 * @param escalations
	 *            {@link Escalation} instances in order to be taken for this
	 *            procedure.
	 */
	public EscalationProcedureImpl(
			EscalationProcedure parentEscalationProcedure,
			Escalation... escalations) {
		// Store state
		this.parentEscalationProcedure = parentEscalationProcedure;
		this.escalations = escalations;
	}

	/**
	 * Initiate top level {@link EscalationProcedure}.
	 * 
	 * @param team
	 *            {@link Team} to execute the catch all {@link Task}.
	 */
	public EscalationProcedureImpl(Team team) {
		// No parent as providing catch all escalation
		this.parentEscalationProcedure = null;

		// Create the catch all escalation
		TaskMetaDataImpl<Throwable, Work, None, None> catchAllTask = new TaskMetaDataImpl<Throwable, Work, None, None>(
				new CatchAllEscalationTaskFactory(), team, new int[0],
				new int[0], new int[0], new TaskDutyAssociation[0],
				new TaskDutyAssociation[0]);
		FlowMetaData<Work> catchAllFlow = new FlowMetaDataImpl<Work>(
				FlowInstigationStrategyEnum.SEQUENTIAL, catchAllTask, null);
		WorkMetaData<Work> workMetaData = new WorkMetaDataImpl<Work>(-1,
				new CatchAllEscalationWorkFactory(),
				new ManagedObjectMetaData[0], new AdministratorMetaData[0],
				catchAllFlow);
		catchAllTask.loadRemainingState(workMetaData, new FlowMetaData[0],
				null, this);
		Escalation catchAllEscalation = new EscalationImpl(Throwable.class,
				true, catchAllFlow);

		// Provide catch all escalation
		this.escalations = new Escalation[] { catchAllEscalation };
	}

	/**
	 * Invoked by the catch all {@link Escalation}.
	 * 
	 * @param exception
	 *            Cause of the escalation.
	 */
	protected void handleTopLevelEscalation(Throwable cause) {
		// Default implementation is to write exception to stderr
		System.err.println("Office Floor top level failure: ");
		cause.printStackTrace();
	}

	/*
	 * ====================================================================
	 * EscalationProcedure
	 * ====================================================================
	 */

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.internal.structure.EscalationProcedure#getEscalation(java.lang.Throwable)
	 */
	@Override
	public Escalation getEscalation(Throwable cause) {

		// Find the first matching escalation
		for (Escalation escalation : this.escalations) {
			if (escalation.getTypeOfCause().isInstance(cause)) {
				// Use first matching
				return escalation;
			}
		}

		// Not found so ask parent for escalation
		return this.parentEscalationProcedure.getEscalation(cause);
	}

	/**
	 * {@link TaskFactory} for the top level {@link Escalation}.
	 */
	private class CatchAllEscalationTaskFactory implements
			TaskFactory<Throwable, Work, None, None>,
			Task<Throwable, Work, None, None> {

		/*
		 * ===========================================================================
		 * TaskFactory
		 * ===========================================================================
		 */

		/*
		 * (non-Javadoc)
		 * 
		 * @see net.officefloor.frame.api.build.TaskFactory#createTask(net.officefloor.frame.api.execute.Work)
		 */
		@Override
		public Task<Throwable, Work, None, None> createTask(Work work) {
			return this;
		}

		/*
		 * ===========================================================================
		 * Task
		 * ===========================================================================
		 */

		/*
		 * (non-Javadoc)
		 * 
		 * @see net.officefloor.frame.api.execute.Task#doTask(net.officefloor.frame.api.execute.TaskContext)
		 */
		@Override
		public Object doTask(TaskContext<Throwable, Work, None, None> context) {

			// Handle exception
			EscalationProcedureImpl.this.handleTopLevelEscalation(context
					.getParameter());

			// No return
			return null;
		}
	}

	/**
	 * {@link WorkFactory} for the top level {@link Escalation}.
	 */
	private static class CatchAllEscalationWorkFactory implements
			WorkFactory<Work>, Work {

		/*
		 * ===========================================================================
		 * WorkFactory
		 * ===========================================================================
		 */

		/*
		 * (non-Javadoc)
		 * 
		 * @see net.officefloor.frame.api.build.WorkFactory#createWork()
		 */
		@Override
		public Work createWork() {
			return this;
		}

		/*
		 * ===========================================================================
		 * Work
		 * ===========================================================================
		 */

		/*
		 * (non-Javadoc)
		 * 
		 * @see net.officefloor.frame.api.execute.Work#setWorkContext(net.officefloor.frame.api.execute.WorkContext)
		 */
		@Override
		public void setWorkContext(WorkContext context) throws Exception {
			// Do nothing
		}
	}

}
