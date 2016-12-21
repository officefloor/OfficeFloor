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
package net.officefloor.frame.impl.execute.process;

import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.build.TaskFactory;
import net.officefloor.frame.api.escalate.Escalation;
import net.officefloor.frame.api.escalate.EscalationHandler;
import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.execute.TaskContext;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.impl.execute.escalation.EscalationProcedureImpl;
import net.officefloor.frame.impl.execute.task.TaskJobNode;
import net.officefloor.frame.impl.execute.task.TaskMetaDataImpl;
import net.officefloor.frame.impl.execute.work.WorkMetaDataImpl;
import net.officefloor.frame.internal.structure.AdministratorMetaData;
import net.officefloor.frame.internal.structure.EscalationFlow;
import net.officefloor.frame.internal.structure.EscalationProcedure;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.internal.structure.FlowMetaData;
import net.officefloor.frame.internal.structure.FunctionLoop;
import net.officefloor.frame.internal.structure.GovernanceDeactivationStrategy;
import net.officefloor.frame.internal.structure.ManagedFunctionContainer;
import net.officefloor.frame.internal.structure.ManagedObjectIndex;
import net.officefloor.frame.internal.structure.ManagedObjectMetaData;
import net.officefloor.frame.internal.structure.TaskDutyAssociation;
import net.officefloor.frame.internal.structure.TaskMetaData;
import net.officefloor.frame.internal.structure.TeamManagement;
import net.officefloor.frame.internal.structure.WorkContainer;
import net.officefloor.frame.internal.structure.WorkMetaData;
import net.officefloor.frame.spi.governance.Governance;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.team.Team;
import net.officefloor.frame.util.AbstractSingleTask;

/**
 * {@link EscalationFlow} for an {@link EscalationHandler}.
 * 
 * @author Daniel Sagenschneider
 */
public class EscalationHandlerEscalation implements EscalationFlow {

	/**
	 * No required {@link ManagedObject} instances for
	 * {@link EscalationHandlerTask}.
	 */
	private static final ManagedObjectIndex[] REQUIRED_MANAGED_OBJECTS = new ManagedObjectIndex[0];

	/**
	 * NO {@link TaskDutyAssociation} instances for
	 * {@link EscalationHandlerTask}.
	 */
	private static final TaskDutyAssociation<?>[] TASK_DUTY_ASSOCIATIONS = new TaskDutyAssociation[0];

	/**
	 * No {@link FlowMetaData} instnaces for {@link EscalationHandlerTask}.
	 */
	private static final FlowMetaData<?>[] FLOW_META_DATA = new FlowMetaData<?>[0];

	/**
	 * No {@link Escalation} handling from {@link EscalationHandlerTask}.
	 */
	private static final EscalationProcedure FURTHER_ESCALATION_PROCEDURE = new EscalationProcedureImpl();

	/**
	 * {@link ManagedObjectIndex} instances for dependencies.
	 */
	private static final ManagedObjectIndex[] MANGED_OBJECT_DEPENDENCIES = new ManagedObjectIndex[1];

	/**
	 * <p>
	 * {@link WorkMetaData} for the {@link EscalationHandlerTask}.
	 * <p>
	 * The {@link EscalationHandlerTask} does not use state from the
	 * {@link Work} so single instance across all {@link EscalationHandlerTask}
	 * instances to reduce overheads.
	 */
	@SuppressWarnings("unchecked")
	private static final WorkMetaData<EscalationHandlerTask> WORK_META_DATA = new WorkMetaDataImpl<EscalationHandlerTask>(
			"Escalation Handler Work", new EscalationHandlerTask(null), new ManagedObjectMetaData[0],
			new AdministratorMetaData[0], null, new TaskMetaData[0]);

	/**
	 * Initiate static state.
	 */
	static {
		// Specify dependency on parameter for escalation to handle
		MANGED_OBJECT_DEPENDENCIES[EscalationKey.EXCEPTION.ordinal()] = TaskJobNode.PARAMETER_MANAGED_OBJECT_INDEX;
	}

	/**
	 * {@link EscalationHandler}.
	 */
	private final EscalationHandler escalationHandler;

	/**
	 * {@link TeamManagement} responsible to undertake the
	 * {@link EscalationFlow}.
	 */
	private final TeamManagement responsibleTeam;

	/**
	 * {@link TaskMetaData} for the {@link EscalationHandler} {@link Task}.
	 */
	private final TaskMetaData<EscalationHandlerTask, EscalationKey, None> taskMetaData;

	/**
	 * Initiate.
	 * 
	 * @param escalationHandler
	 *            {@link EscalationHandler}.
	 * @param responsibleTeam
	 *            {@link TeamManagement} responsible to undertake the
	 *            {@link EscalationFlow}.
	 * @param requiredGovernance
	 *            Required {@link Governance}.
	 * @param functionLoop
	 *            {@link FunctionLoop}.
	 */
	public EscalationHandlerEscalation(EscalationHandler escalationHandler, TeamManagement responsibleTeam,
			boolean[] requiredGovernance, FunctionLoop functionLoop) {
		this.escalationHandler = escalationHandler;
		this.responsibleTeam = responsibleTeam;

		// Specify the basic escalation task meta-data
		this.taskMetaData = this.createTaskMetaData(null, functionLoop);
	}

	/**
	 * Obtains the {@link EscalationHandler}.
	 * 
	 * @return {@link EscalationHandler}.
	 */
	public EscalationHandler getEscalationHandler() {
		return this.escalationHandler;
	}

	/**
	 * Creates the {@link TaskMetaData} for the {@link EscalationHandlerTask}.
	 * 
	 * @param requiredGovernance
	 *            Required {@link Governance} to execute the
	 *            {@link EscalationHandlerTask}. This enables deactivating all
	 *            {@link Governance} for the particular {@link Office} before
	 *            executing the {@link Task}.
	 * @param functionLoop
	 *            {@link FunctionLoop}.
	 * @return {@link TaskMetaData} for the {@link EscalationHandlerTask}.
	 */
	private TaskMetaData<EscalationHandlerTask, EscalationKey, None> createTaskMetaData(boolean[] requiredGovernance,
			FunctionLoop functionLoop) {

		// Create the escalation task
		EscalationHandlerTask task = new EscalationHandlerTask(this.escalationHandler);

		// Create the escalation task meta-data
		EscalationTaskMetaData taskMetaData = new EscalationTaskMetaData(task, this.responsibleTeam, requiredGovernance,
				functionLoop);

		// Return the task meta-data
		return taskMetaData;
	}

	/*
	 * ==================== Escalation =====================================
	 */

	@Override
	public Class<? extends Throwable> getTypeOfCause() {
		return Throwable.class;
	}

	@Override
	public TaskMetaData<?, ?, ?> getTaskMetaData() {
		return this.taskMetaData;
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
	private static class EscalationHandlerTask extends AbstractSingleTask<EscalationHandlerTask, EscalationKey, None> {

		/**
		 * {@link EscalationHandler}.
		 */
		private final EscalationHandler escalationHandler;

		/**
		 * Initiate.
		 * 
		 * @param escalationHandler
		 *            {@link EscalationHandler}.
		 */
		public EscalationHandlerTask(EscalationHandler escalationHandler) {
			this.escalationHandler = escalationHandler;
		}

		/*
		 * ================== Task ============================================
		 */

		@Override
		public Object doTask(TaskContext<EscalationHandlerTask, EscalationKey, None> context) throws Throwable {

			// Obtain the exception
			Throwable exception = (Throwable) context.getObject(EscalationKey.EXCEPTION);

			// Handle the exception
			this.escalationHandler.handleEscalation(exception);

			// Nothing to return
			return null;
		}
	}

	/**
	 * <p>
	 * {@link TaskMetaData} for the {@link EscalationHandlerTask} to ensure
	 * deactivate any active {@link Governance} before execution.
	 * <p>
	 * The {@link TaskMetaData} used by the actual executing
	 * {@link EscalationHandlerTask} is dynamic to the {@link Governance} for
	 * the particular {@link Office}.
	 */
	private static class EscalationTaskMetaData extends TaskMetaDataImpl<EscalationHandlerTask, EscalationKey, None> {

		/**
		 * Initiate.
		 * 
		 * @param taskFactory
		 *            {@link TaskFactory}.
		 * @param responsibleTeam
		 *            {@link TeamManagement} of {@link Team} responsible for the
		 *            {@link Escalation} {@link Task}.
		 * @param requiredGovernance
		 *            Required {@link Governance}.
		 * @param functionLoop
		 *            {@link FunctionLoop}.
		 */
		public EscalationTaskMetaData(TaskFactory<EscalationHandlerTask, EscalationKey, None> taskFactory,
				TeamManagement responsibleTeam, boolean[] requiredGovernance, FunctionLoop functionLoop) {
			super("Escalation Handler Task", "Escalation Handler Task", taskFactory, null, Throwable.class,
					responsibleTeam, REQUIRED_MANAGED_OBJECTS, MANGED_OBJECT_DEPENDENCIES, requiredGovernance,
					TASK_DUTY_ASSOCIATIONS, TASK_DUTY_ASSOCIATIONS, functionLoop);
			this.loadRemainingState(WORK_META_DATA, FLOW_META_DATA, null, FURTHER_ESCALATION_PROCEDURE);
		}

		/*
		 * ======================= TaskMetaData ==========================
		 */

		@Override
		public ManagedFunctionContainer createTaskNode(Flow flow, WorkContainer<EscalationHandlerTask> workContainer,
				ManagedFunctionContainer parallelJobNodeOwner, Object parameter,
				GovernanceDeactivationStrategy governanceDeactivationStrategy) {

			// Create required Governance to deactivate all governance
			int requiredGovernanceCount = flow.getThreadState().getThreadMetaData().getGovernanceMetaData().length;
			boolean[] requiredGovernance = new boolean[requiredGovernanceCount];
			for (int i = 0; i < requiredGovernance.length; i++) {
				requiredGovernance[i] = false;
			}

			// Create task meta-data for escalation
			EscalationTaskMetaData taskMetaData = new EscalationTaskMetaData(this.getTaskFactory(),
					this.getResponsibleTeam(), requiredGovernance, this.getFunctionLoop());

			// Create and return the job node
			return new TaskJobNode<EscalationHandlerTask, EscalationKey, None>(flow, workContainer, taskMetaData,
					governanceDeactivationStrategy, parallelJobNodeOwner, parameter);
		}
	}

}