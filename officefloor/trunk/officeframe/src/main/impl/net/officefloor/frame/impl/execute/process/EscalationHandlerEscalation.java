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
package net.officefloor.frame.impl.execute.process;

import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.execute.EscalationHandler;
import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.execute.TaskContext;
import net.officefloor.frame.impl.execute.flow.FlowMetaDataImpl;
import net.officefloor.frame.impl.execute.task.TaskMetaDataImpl;
import net.officefloor.frame.impl.execute.work.WorkMetaDataImpl;
import net.officefloor.frame.internal.structure.AdministratorMetaData;
import net.officefloor.frame.internal.structure.Escalation;
import net.officefloor.frame.internal.structure.FlowInstigationStrategyEnum;
import net.officefloor.frame.internal.structure.FlowMetaData;
import net.officefloor.frame.internal.structure.ManagedObjectIndex;
import net.officefloor.frame.internal.structure.ManagedObjectMetaData;
import net.officefloor.frame.internal.structure.TaskDutyAssociation;
import net.officefloor.frame.internal.structure.TaskMetaData;
import net.officefloor.frame.internal.structure.WorkMetaData;
import net.officefloor.frame.spi.team.Team;
import net.officefloor.frame.util.AbstractSingleTask;

/**
 * {@link Escalation} for an {@link EscalationHandler}.
 * 
 * @author Daniel
 */
public class EscalationHandlerEscalation implements Escalation {

	/**
	 * {@link EscalationHandler}.
	 */
	private final EscalationHandler escalationHandler;

	/**
	 * {@link FlowMetaData} for the {@link EscalationHandler} {@link Task}.
	 */
	private final FlowMetaData<EscalationHandlerTask> flowMetaData;

	/**
	 * Initiate.
	 * 
	 * @param escalationHandler
	 *            {@link EscalationHandler}.
	 * @param team
	 *            {@link Team} responsible to undertake the {@link Escalation}.
	 */
	@SuppressWarnings("unchecked")
	public EscalationHandlerEscalation(EscalationHandler escalationHandler,
			Team team) {
		this.escalationHandler = escalationHandler;

		// Create the escalation task
		EscalationHandlerTask task = new EscalationHandlerTask();

		// Create the escalation task meta-data
		TaskMetaDataImpl<Throwable, EscalationHandlerTask, None, None> taskMetaData = new TaskMetaDataImpl<Throwable, EscalationHandlerTask, None, None>(
				"Escalation Handler Task", task, Throwable.class, team,
				new ManagedObjectIndex[0], new ManagedObjectIndex[0],
				new TaskDutyAssociation[0], new TaskDutyAssociation[0]);

		// Create the escalation work meta-data (and load to task meta-data)
		WorkMetaData<EscalationHandlerTask> workMetaData = new WorkMetaDataImpl<EscalationHandlerTask>(
				"Escalation Handler Work", task, new ManagedObjectMetaData[0],
				new AdministratorMetaData[0], null,
				new TaskMetaData[] { taskMetaData });
		taskMetaData.loadRemainingState(workMetaData, new FlowMetaData[0],
				null, null);

		// Create the escalation flow meta-data
		this.flowMetaData = new FlowMetaDataImpl<EscalationHandlerTask>(
				FlowInstigationStrategyEnum.PARALLEL, taskMetaData, null);
	}

	/**
	 * Obtains the {@link EscalationHandler}.
	 * 
	 * @return {@link EscalationHandler}.
	 */
	public EscalationHandler getEscalationHandler() {
		return this.escalationHandler;
	}

	/*
	 * ==================== Escalation =====================================
	 */

	@Override
	public Class<? extends Throwable> getTypeOfCause() {
		return Throwable.class;
	}

	@Override
	public FlowMetaData<?> getFlowMetaData() {
		return this.flowMetaData;
	}

	/**
	 * {@link Task} to execute the {@link EscalationHandler}.
	 */
	private class EscalationHandlerTask extends
			AbstractSingleTask<Throwable, EscalationHandlerTask, None, None> {

		/*
		 * ================== Task ============================================
		 */

		@Override
		public Object doTask(
				TaskContext<Throwable, EscalationHandlerTask, None, None> context)
				throws Throwable {

			// Obtain the exception
			Throwable exception = context.getParameter();

			// Handle the exception
			EscalationHandlerEscalation.this.escalationHandler
					.handleEscalation(exception);

			// Nothing to return
			return null;
		}
	}

}