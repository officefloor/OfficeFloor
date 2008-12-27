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

import net.officefloor.frame.api.build.TaskFactory;
import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.internal.structure.EscalationProcedure;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.internal.structure.FlowMetaData;
import net.officefloor.frame.internal.structure.JobActivatableSet;
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
	 * {@link TaskFactory} to create the {@link Task} of the
	 * {@link TaskMetaData}.
	 */
	protected final TaskFactory<P, W, M, F> taskFactory;

	/**
	 * {@link Team} responsible for executing this {@link Task}.
	 */
	protected final Team team;

	/**
	 * Indexes identifying the {@link ManagedObject} instances that must be
	 * ready before the {@link Task} can be executed.
	 */
	protected final int[] requiredManagedObjects;

	/**
	 * Translations of the {@link Task} {@link ManagedObject} index to the
	 * {@link Work} {@link ManagedObject} index.
	 */
	protected final int[] taskToWorkMoTranslations;

	/**
	 * {@link TaskDutyAssociation} specifying the {@link Duty} instances to be
	 * completed before executing the {@link Task}.
	 */
	protected final TaskDutyAssociation<?>[] preTaskDuties;

	/**
	 * {@link TaskDutyAssociation} specifying the {@link Duty} instances to be
	 * completed after executing the {@link Task}.
	 */
	protected final TaskDutyAssociation<?>[] postTaskDuties;

	/**
	 * <p>
	 * {@link WorkMetaData} for this {@link Task}.
	 * <p>
	 * Acts as <code>final</code> but specified after constructor.
	 */
	protected WorkMetaData<W> workMetaData;

	/**
	 * <p>
	 * Meta-data of the available {@link Flow} instances from this {@link Task}.
	 * <p>
	 * Acts as <code>final</code> but specified after constructor.
	 */
	protected FlowMetaData<?>[] flowMetaData;

	/**
	 * <p>
	 * {@link TaskMetaData} of the next {@link Task} within the {@link Flow}.
	 * <p>
	 * Acts as <code>final</code> but specified after constructor.
	 */
	protected TaskMetaData<?, ?, ?, ?> nextTaskInFlow;

	/**
	 * {@link EscalationProcedure} for exceptions of the {@link Task} of this
	 * {@link TaskMetaData}.
	 */
	protected EscalationProcedure escalationProcedure;

	/**
	 * Initiate with details of the meta-data for the {@link Task}.
	 * 
	 * @param taskFactory
	 *            {@link TaskFactory} to create the {@link Task} of the
	 *            {@link TaskMetaData}.
	 * @param team
	 *            {@link Team} responsible for executing this {@link Task}.
	 * @param requiredManagedObjects
	 *            Indexes identifying the {@link ManagedObject} instances that
	 *            must be ready before the {@link Task} can be executed.
	 * @param taskToWorkMoTranslations
	 *            Translations of the {@link Task} {@link ManagedObject} index
	 *            to the {@link Work} {@link ManagedObject} index.
	 * @param preTaskDuties
	 *            {@link TaskDutyAssociation} specifying the {@link Duty}
	 *            instances to be completed before executing the {@link Task}.
	 * @param postTaskDuties
	 *            {@link TaskDutyAssociation} specifying the {@link Duty}
	 *            instances to be completed after executing the {@link Task}.
	 */
	public TaskMetaDataImpl(TaskFactory<P, W, M, F> taskFactory, Team team,
			int[] requiredManagedObjects, int[] taskToWorkMoTranslations,
			TaskDutyAssociation<?>[] preTaskDuties,
			TaskDutyAssociation<?>[] postTaskDuties) {
		// Store state
		this.taskFactory = taskFactory;
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
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.officefloor.frame.internal.structure.TaskMetaData#getTaskFactory()
	 */
	@Override
	public TaskFactory<P, W, M, F> getTaskFactory() {
		return this.taskFactory;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.officefloor.frame.internal.structure.JobMetaData#createJobActivableSet
	 * ()
	 */
	@Override
	public JobActivatableSet createJobActivableSet() {
		return new JobActivatableSetImpl();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.internal.structure.TaskMetaData#getTeam()
	 */
	@Override
	public Team getTeam() {
		return this.team;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seenet.officefloor.frame.internal.structure.TaskMetaData#
	 * getRequiredManagedObjects()
	 */
	@Override
	public int[] getRequiredManagedObjects() {
		return this.requiredManagedObjects;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seenet.officefloor.frame.internal.structure.TaskMetaData#
	 * translateManagedObjectIndexForWork(int)
	 */
	@Override
	public int translateManagedObjectIndexForWork(int taskMoIndex) {
		return this.taskToWorkMoTranslations[taskMoIndex];
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.internal.structure.TaskMetaData#getFlow(int)
	 */
	@Override
	public FlowMetaData<?> getFlow(int flowIndex) {
		return this.flowMetaData[flowIndex];
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.officefloor.frame.internal.structure.TaskMetaData#getWorkMetaData()
	 */
	@Override
	public WorkMetaData<W> getWorkMetaData() {
		return this.workMetaData;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.officefloor.frame.internal.structure.TaskMetaData#getEscalationProcedure
	 * ()
	 */
	@Override
	public EscalationProcedure getEscalationProcedure() {
		return this.escalationProcedure;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.officefloor.frame.internal.structure.TaskMetaData#getNextTaskInFlow()
	 */
	@Override
	public TaskMetaData<?, ?, ?, ?> getNextTaskInFlow() {
		return this.nextTaskInFlow;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seenet.officefloor.frame.internal.structure.TaskMetaData#
	 * getPreAdministrationMetaData()
	 */
	@Override
	public TaskDutyAssociation<?>[] getPreAdministrationMetaData() {
		return this.preTaskDuties;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seenet.officefloor.frame.internal.structure.TaskMetaData#
	 * getPostAdministrationMetaData()
	 */
	@Override
	public TaskDutyAssociation<?>[] getPostAdministrationMetaData() {
		return this.postTaskDuties;
	}

}
