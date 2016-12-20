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

import net.officefloor.frame.api.build.TaskFactory;
import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.internal.structure.EscalationProcedure;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.internal.structure.FlowMetaData;
import net.officefloor.frame.internal.structure.GovernanceDeactivationStrategy;
import net.officefloor.frame.internal.structure.FunctionLoop;
import net.officefloor.frame.internal.structure.ManagedFunction;
import net.officefloor.frame.internal.structure.ManagedObjectIndex;
import net.officefloor.frame.internal.structure.TaskDutyAssociation;
import net.officefloor.frame.internal.structure.TaskMetaData;
import net.officefloor.frame.internal.structure.TeamManagement;
import net.officefloor.frame.internal.structure.WorkContainer;
import net.officefloor.frame.internal.structure.WorkMetaData;
import net.officefloor.frame.spi.administration.Duty;
import net.officefloor.frame.spi.governance.Governance;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.team.Job;
import net.officefloor.frame.spi.team.Team;

/**
 * Meta-data of a {@link Task}.
 * 
 * @author Daniel Sagenschneider
 */
public class TaskMetaDataImpl<W extends Work, D extends Enum<D>, F extends Enum<F>> implements TaskMetaData<W, D, F> {

	/**
	 * Name of this {@link Job}.
	 */
	private final String jobName;

	/**
	 * Name of the {@link Task} within the {@link Work}.
	 */
	private final String taskName;

	/**
	 * {@link TaskFactory} to create the {@link Task} of the
	 * {@link TaskMetaData}.
	 */
	private final TaskFactory<W, D, F> taskFactory;

	/**
	 * Differentiator.
	 */
	private final Object differentiator;

	/**
	 * Parameter type of this {@link Task}.
	 */
	private final Class<?> parameterType;

	/**
	 * {@link TeamManagement} of the {@link Team} responsible for executing this
	 * {@link Task}.
	 */
	private final TeamManagement responsibleTeam;

	/**
	 * {@link ManagedObjectIndex} instances identifying the
	 * {@link ManagedObject} instances that must be loaded before the
	 * {@link Task} may be executed.
	 */
	private final ManagedObjectIndex[] requiredManagedObjects;

	/**
	 * Translations of the {@link Task} {@link ManagedObject} index to the
	 * {@link Work} {@link ManagedObjectIndex}.
	 */
	private final ManagedObjectIndex[] taskToWorkMoTranslations;

	/**
	 * Required {@link Governance}.
	 */
	private final boolean[] requiredGovernance;

	/**
	 * {@link TaskDutyAssociation} specifying the {@link Duty} instances to be
	 * completed before executing the {@link Task}.
	 */
	private final TaskDutyAssociation<?>[] preTaskDuties;

	/**
	 * {@link TaskDutyAssociation} specifying the {@link Duty} instances to be
	 * completed after executing the {@link Task}.
	 */
	private final TaskDutyAssociation<?>[] postTaskDuties;

	/**
	 * {@link FunctionLoop}.
	 */
	private final FunctionLoop jobNodeLoop;

	/**
	 * <p>
	 * {@link WorkMetaData} for this {@link Task}.
	 * <p>
	 * Acts as <code>final</code> but specified after constructor.
	 */
	private WorkMetaData<W> workMetaData;

	/**
	 * <p>
	 * Meta-data of the available {@link Flow} instances from this {@link Task}.
	 * <p>
	 * Acts as <code>final</code> but specified after constructor.
	 */
	private FlowMetaData<?>[] flowMetaData;

	/**
	 * <p>
	 * {@link TaskMetaData} of the next {@link Task} within the {@link Flow}.
	 * <p>
	 * Acts as <code>final</code> but specified after constructor.
	 */
	private TaskMetaData<?, ?, ?> nextTaskInFlow;

	/**
	 * {@link EscalationProcedure} for exceptions of the {@link Task} of this
	 * {@link TaskMetaData}.
	 */
	private EscalationProcedure escalationProcedure;

	/**
	 * Initiate with details of the meta-data for the {@link Task}.
	 * 
	 * @param jobName
	 *            Name of the {@link Job}.
	 * @param taskName
	 *            Name of the {@link Task} within the {@link Work}.
	 * @param taskFactory
	 *            {@link TaskFactory} to create the {@link Task} of the
	 *            {@link TaskMetaData}.
	 * @param differentiator
	 *            Differentiator. May be <code>null</code>.
	 * @param parameterType
	 *            Parameter type of this {@link Task}.
	 * @param responsibleTeam
	 *            {@link TeamManagement} of the {@link Team} responsible for
	 *            executing this {@link Task}. May be <code>null</code>.
	 * @param continueTeam
	 *            {@link Team} to enable the worker ({@link Thread}) of the
	 *            responsible {@link Team} to continue on to execute the next
	 *            {@link Job}.
	 * @param requiredManagedObjects
	 *            {@link ManagedObjectIndex} instances identifying the
	 *            {@link ManagedObject} instances that must be loaded before the
	 *            {@link Task} may be executed.
	 * @param requiredGovernance
	 *            Required {@link Governance}.
	 * @param taskToWorkMoTranslations
	 *            Translations of the {@link Task} {@link ManagedObject} index
	 *            to the {@link Work} {@link ManagedObjectIndex}.
	 * @param preTaskDuties
	 *            {@link TaskDutyAssociation} specifying the {@link Duty}
	 *            instances to be completed before executing the {@link Task}.
	 * @param postTaskDuties
	 *            {@link TaskDutyAssociation} specifying the {@link Duty}
	 *            instances to be completed after executing the {@link Task}.
	 * @param jobNodeLoop
	 *            {@link FunctionLoop}.
	 */
	public TaskMetaDataImpl(String jobName, String taskName, TaskFactory<W, D, F> taskFactory, Object differentiator,
			Class<?> parameterType, TeamManagement responsibleTeam, ManagedObjectIndex[] requiredManagedObjects,
			ManagedObjectIndex[] taskToWorkMoTranslations, boolean[] requiredGovernance,
			TaskDutyAssociation<?>[] preTaskDuties, TaskDutyAssociation<?>[] postTaskDuties, FunctionLoop jobNodeLoop) {
		this.jobName = jobName;
		this.taskName = taskName;
		this.taskFactory = taskFactory;
		this.differentiator = differentiator;
		this.parameterType = parameterType;
		this.responsibleTeam = responsibleTeam;
		this.requiredManagedObjects = requiredManagedObjects;
		this.taskToWorkMoTranslations = taskToWorkMoTranslations;
		this.requiredGovernance = requiredGovernance;
		this.preTaskDuties = preTaskDuties;
		this.postTaskDuties = postTaskDuties;
		this.jobNodeLoop = jobNodeLoop;
	}

	/**
	 * Loads the remaining state of this {@link TaskMetaData}.
	 * 
	 * @param workMetaData
	 *            {@link WorkMetaData} for this {@link Task}.
	 * @param flowMetaData
	 *            Meta-data of the available {@link Flow} instances from this
	 *            {@link Task}.
	 * @param nextTaskInFlow
	 *            {@link TaskMetaData} of the next {@link Task} within the
	 *            {@link Flow}.
	 * @param escalationProcedure
	 *            {@link EscalationProcedure} for exceptions of the {@link Task}
	 *            of this {@link TaskMetaData}.
	 */
	public void loadRemainingState(WorkMetaData<W> workMetaData, FlowMetaData<?>[] flowMetaData,
			TaskMetaData<?, ?, ?> nextTaskInFlow, EscalationProcedure escalationProcedure) {
		this.workMetaData = workMetaData;
		this.flowMetaData = flowMetaData;
		this.nextTaskInFlow = nextTaskInFlow;
		this.escalationProcedure = escalationProcedure;
	}

	/*
	 * ================= TaskMetaData ===================================
	 */

	@Override
	public String getJobName() {
		return this.jobName;
	}

	@Override
	public String getTaskName() {
		return this.taskName;
	}

	@Override
	public TaskFactory<W, D, F> getTaskFactory() {
		return this.taskFactory;
	}

	@Override
	public Object getDifferentiator() {
		return this.differentiator;
	}

	@Override
	public Class<?> getParameterType() {
		return this.parameterType;
	}

	@Override
	public TeamManagement getResponsibleTeam() {
		return this.responsibleTeam;
	}

	@Override
	public ManagedObjectIndex[] getRequiredManagedObjects() {
		return this.requiredManagedObjects;
	}

	@Override
	public boolean[] getRequiredGovernance() {
		return this.requiredGovernance;
	}

	@Override
	public ManagedObjectIndex translateManagedObjectIndexForWork(int taskMoIndex) {
		return this.taskToWorkMoTranslations[taskMoIndex];
	}

	@Override
	public FlowMetaData<?> getFlow(int flowIndex) {
		return this.flowMetaData[flowIndex];
	}

	@Override
	public WorkMetaData<W> getWorkMetaData() {
		return this.workMetaData;
	}

	@Override
	public EscalationProcedure getEscalationProcedure() {
		return this.escalationProcedure;
	}

	@Override
	public TaskMetaData<?, ?, ?> getNextTaskInFlow() {
		return this.nextTaskInFlow;
	}

	@Override
	public TaskDutyAssociation<?>[] getPreAdministrationMetaData() {
		return this.preTaskDuties;
	}

	@Override
	public TaskDutyAssociation<?>[] getPostAdministrationMetaData() {
		return this.postTaskDuties;
	}

	@Override
	public FunctionLoop getJobNodeLoop() {
		return this.jobNodeLoop;
	}

	@Override
	public ManagedFunction createTaskNode(Flow flow, WorkContainer<W> workContainer, ManagedFunction parallelJobNodeOwner,
			Object parameter, GovernanceDeactivationStrategy governanceDeactivationStrategy) {
		return new TaskJobNode<W, D, F>(flow, workContainer, this, governanceDeactivationStrategy, parallelJobNodeOwner,
				parameter);
	}

}