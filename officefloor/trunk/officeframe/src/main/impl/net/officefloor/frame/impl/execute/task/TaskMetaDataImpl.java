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

import net.officefloor.frame.api.build.TaskFactory;
import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.impl.execute.job.JobActivatableSetImpl;
import net.officefloor.frame.internal.structure.EscalationProcedure;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.internal.structure.FlowMetaData;
import net.officefloor.frame.internal.structure.JobActivatableSet;
import net.officefloor.frame.internal.structure.ManagedObjectIndex;
import net.officefloor.frame.internal.structure.TaskDutyAssociation;
import net.officefloor.frame.internal.structure.TaskMetaData;
import net.officefloor.frame.internal.structure.WorkMetaData;
import net.officefloor.frame.spi.administration.Duty;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.team.Team;

/**
 * Meta-data of a {@link Task}.
 * 
 * @author Daniel
 */
public class TaskMetaDataImpl<P extends Object, W extends Work, M extends Enum<M>, F extends Enum<F>>
		implements TaskMetaData<P, W, M, F> {

	/**
	 * Name of the {@link Task}.
	 */
	private final String taskName;

	/**
	 * {@link TaskFactory} to create the {@link Task} of the
	 * {@link TaskMetaData}.
	 */
	private final TaskFactory<P, W, M, F> taskFactory;

	/**
	 * Parameter type of this {@link Task}.
	 */
	private final Class<?> parameterType;

	/**
	 * {@link Team} responsible for executing this {@link Task}.
	 */
	private final Team team;

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
	private TaskMetaData<?, ?, ?, ?> nextTaskInFlow;

	/**
	 * {@link EscalationProcedure} for exceptions of the {@link Task} of this
	 * {@link TaskMetaData}.
	 */
	private EscalationProcedure escalationProcedure;

	/**
	 * Initiate with details of the meta-data for the {@link Task}.
	 * 
	 * @param taskName
	 *            Name of the {@link Task}.
	 * @param taskFactory
	 *            {@link TaskFactory} to create the {@link Task} of the
	 *            {@link TaskMetaData}.
	 * @param parameterType
	 *            Parameter type of this {@link Task}.
	 * @param team
	 *            {@link Team} responsible for executing this {@link Task}.
	 * @param requiredManagedObjects
	 *            {@link ManagedObjectIndex} instances identifying the
	 *            {@link ManagedObject} instances that must be loaded before the
	 *            {@link Task} may be executed.
	 * @param taskToWorkMoTranslations
	 *            Translations of the {@link Task} {@link ManagedObject} index
	 *            to the {@link Work} {@link ManagedObjectIndex}.
	 * @param preTaskDuties
	 *            {@link TaskDutyAssociation} specifying the {@link Duty}
	 *            instances to be completed before executing the {@link Task}.
	 * @param postTaskDuties
	 *            {@link TaskDutyAssociation} specifying the {@link Duty}
	 *            instances to be completed after executing the {@link Task}.
	 */
	public TaskMetaDataImpl(String taskName,
			TaskFactory<P, W, M, F> taskFactory, Class<?> parameterType,
			Team team, ManagedObjectIndex[] requiredManagedObjects,
			ManagedObjectIndex[] taskToWorkMoTranslations,
			TaskDutyAssociation<?>[] preTaskDuties,
			TaskDutyAssociation<?>[] postTaskDuties) {
		this.taskName = taskName;
		this.taskFactory = taskFactory;
		this.parameterType = parameterType;
		this.team = team;
		this.requiredManagedObjects = requiredManagedObjects;
		this.taskToWorkMoTranslations = taskToWorkMoTranslations;
		this.preTaskDuties = preTaskDuties;
		this.postTaskDuties = postTaskDuties;
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
	public void loadRemainingState(WorkMetaData<W> workMetaData,
			FlowMetaData<?>[] flowMetaData,
			TaskMetaData<?, ?, ?, ?> nextTaskInFlow,
			EscalationProcedure escalationProcedure) {
		this.workMetaData = workMetaData;
		this.flowMetaData = flowMetaData;
		this.nextTaskInFlow = nextTaskInFlow;
		this.escalationProcedure = escalationProcedure;
	}

	/*
	 * ================= TaskMetaData ===================================
	 */

	@Override
	public String getTaskName() {
		return this.taskName;
	}

	@Override
	public TaskFactory<P, W, M, F> getTaskFactory() {
		return this.taskFactory;
	}

	@Override
	public Class<?> getParameterType() {
		return this.parameterType;
	}

	@Override
	public JobActivatableSet createJobActivableSet() {
		return new JobActivatableSetImpl();
	}

	@Override
	public Team getTeam() {
		return this.team;
	}

	@Override
	public ManagedObjectIndex[] getRequiredManagedObjects() {
		return this.requiredManagedObjects;
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
	public TaskMetaData<?, ?, ?, ?> getNextTaskInFlow() {
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

}