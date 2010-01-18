/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2010 Daniel Sagenschneider
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

package net.officefloor.frame.impl.execute.process;

import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.escalate.EscalationHandler;
import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.execute.TaskContext;
import net.officefloor.frame.impl.execute.escalation.EscalationProcedureImpl;
import net.officefloor.frame.impl.execute.flow.FlowMetaDataImpl;
import net.officefloor.frame.impl.execute.task.TaskJob;
import net.officefloor.frame.impl.execute.task.TaskMetaDataImpl;
import net.officefloor.frame.impl.execute.work.WorkMetaDataImpl;
import net.officefloor.frame.internal.structure.AdministratorMetaData;
import net.officefloor.frame.internal.structure.EscalationFlow;
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
 * {@link EscalationFlow} for an {@link EscalationHandler}.
 *
 * @author Daniel Sagenschneider
 */
public class EscalationHandlerEscalation implements EscalationFlow {

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
	 *            {@link Team} responsible to undertake the
	 *            {@link EscalationFlow}.
	 */
	@SuppressWarnings("unchecked")
	public EscalationHandlerEscalation(EscalationHandler escalationHandler,
			Team team) {
		this.escalationHandler = escalationHandler;

		// Create the escalation task
		EscalationHandlerTask task = new EscalationHandlerTask();

		// Create the dependencies to obtain exception as parameter
		ManagedObjectIndex[] dependencies = new ManagedObjectIndex[1];
		dependencies[EscalationKey.EXCEPTION.ordinal()] = TaskJob.PARAMETER_MANAGED_OBJECT_INDEX;

		// Create the escalation task meta-data
		TaskMetaDataImpl<EscalationHandlerTask, EscalationKey, None> taskMetaData = new TaskMetaDataImpl<EscalationHandlerTask, EscalationKey, None>(
				"Escalation Handler Task", task, Throwable.class, team,
				new ManagedObjectIndex[0], dependencies,
				new TaskDutyAssociation[0], new TaskDutyAssociation[0]);

		// Create the escalation work meta-data (and load to task meta-data)
		WorkMetaData<EscalationHandlerTask> workMetaData = new WorkMetaDataImpl<EscalationHandlerTask>(
				"Escalation Handler Work", task, new ManagedObjectMetaData[0],
				new AdministratorMetaData[0], null,
				new TaskMetaData[] { taskMetaData });
		taskMetaData.loadRemainingState(workMetaData, new FlowMetaData[0],
				null, new EscalationProcedureImpl());

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
	 * Key identifying the {@link Exception} for the {@link EscalationFlow}.
	 */
	private enum EscalationKey {
		EXCEPTION
	}

	/**
	 * {@link Task} to execute the {@link EscalationHandler}.
	 */
	private class EscalationHandlerTask extends
			AbstractSingleTask<EscalationHandlerTask, EscalationKey, None> {

		/*
		 * ================== Task ============================================
		 */

		@Override
		public Object doTask(
				TaskContext<EscalationHandlerTask, EscalationKey, None> context)
				throws Throwable {

			// Obtain the exception
			Throwable exception = (Throwable) context
					.getObject(EscalationKey.EXCEPTION);

			// Handle the exception
			EscalationHandlerEscalation.this.escalationHandler
					.handleEscalation(exception);

			// Nothing to return
			return null;
		}
	}

}