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
package net.officefloor.frame.impl.construct;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.officefloor.frame.api.build.TaskBuilder;
import net.officefloor.frame.api.build.TaskFactory;
import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.internal.configuration.ConfigurationException;
import net.officefloor.frame.internal.configuration.EscalationConfiguration;
import net.officefloor.frame.internal.configuration.FlowConfiguration;
import net.officefloor.frame.internal.configuration.TaskConfiguration;
import net.officefloor.frame.internal.configuration.TaskDutyConfiguration;
import net.officefloor.frame.internal.configuration.TaskManagedObjectConfiguration;
import net.officefloor.frame.internal.configuration.TaskNodeReference;
import net.officefloor.frame.internal.structure.EscalationProcedure;
import net.officefloor.frame.internal.structure.FlowInstigationStrategyEnum;
import net.officefloor.frame.internal.structure.ThreadState;

/**
 * Implementation of the {@link net.officefloor.frame.api.build.TaskBuilder}.
 * 
 * @author Daniel
 */
public class TaskBuilderImpl<P extends Object, W extends Work, M extends Enum<M>, F extends Enum<F>>
		implements TaskBuilder<P, W, M, F>, TaskConfiguration<P, W, M, F> {

	/**
	 * Name of this {@link net.officefloor.frame.api.execute.Task}.
	 */
	protected final String taskName;

	/**
	 * Parent {@link EscalationProcedure} for the resulting
	 * {@link net.officefloor.frame.api.execute.Task}.
	 */
	protected final EscalationProcedure parentEscalationProcedure;

	/**
	 * {@link net.officefloor.frame.spi.managedobject.ManagedObject} instances
	 * to be linked to this {@link net.officefloor.frame.api.execute.Task}.
	 */
	protected final Map<Integer, TaskManagedObjectConfigurationImpl> managedObjects;

	/**
	 * {@link net.officefloor.frame.internal.structure.Flow} instances to be
	 * linked to this {@link net.officefloor.frame.api.execute.Task}.
	 */
	protected final Map<Integer, FlowConfigurationImpl> flows;

	/**
	 * {@link TaskFactory}.
	 */
	protected TaskFactory<P, W, M, F> taskFactory;

	/**
	 * {@link net.officefloor.frame.spi.team.Team}.
	 */
	protected String teamName;

	/**
	 * Next {@link net.officefloor.frame.internal.structure.TaskNode} within the
	 * {@link net.officefloor.frame.internal.structure.Flow}.
	 */
	protected TaskNodeReference nextTaskInFlow;

	/**
	 * Listing of {@link EscalationConfiguration} instances to form the
	 * {@link EscalationProcedure} for the resulting {@link Task} of this
	 * {@link TaskBuilder}.
	 */
	protected List<EscalationConfiguration> escalations = new LinkedList<EscalationConfiguration>();

	/**
	 * Listing of task administration duties to do before executing the
	 * {@link net.officefloor.frame.api.execute.Task}.
	 */
	protected List<TaskDutyConfigurationImpl<?>> preTaskDuties = new LinkedList<TaskDutyConfigurationImpl<?>>();

	/**
	 * Listing of task administration duties to do after executing the
	 * {@link net.officefloor.frame.api.execute.Task}.
	 */
	protected List<TaskDutyConfigurationImpl<?>> postTaskDuties = new LinkedList<TaskDutyConfigurationImpl<?>>();

	/**
	 * Initiate.
	 * 
	 * @param taskName
	 *            Name of this {@link net.officefloor.frame.api.execute.Task}.
	 * @param parentEscalationProcedure
	 *            {@link EscalationProcedure} for the
	 *            {@link net.officefloor.frame.api.execute.Task}.
	 */
	public TaskBuilderImpl(String taskName,
			EscalationProcedure parentEscalationProcedure) {
		// Store state
		this.taskName = taskName;
		this.parentEscalationProcedure = parentEscalationProcedure;
		this.managedObjects = new HashMap<Integer, TaskManagedObjectConfigurationImpl>();
		this.flows = new HashMap<Integer, FlowConfigurationImpl>();
	}

	/*
	 * ====================================================================
	 * TaskBuilder
	 * ====================================================================
	 */

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.api.task.TaskMetaData#setTaskFactory(net.officefloor.frame.api.task.TaskFactory)
	 */
	public void setTaskFactory(TaskFactory<P, W, M, F> factory) {
		this.taskFactory = factory;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.api.build.TaskBuilder#setTeam(java.lang.String)
	 */
	public void setTeam(String teamId) {
		this.teamName = teamId;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.api.build.TaskBuilder#setNextTaskInFlow(java.lang.String)
	 */
	public void setNextTaskInFlow(String taskName) {
		this.nextTaskInFlow = new TaskNodeReferenceImpl(taskName);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.api.build.TaskBuilder#setNextTaskInFlow(java.lang.String,
	 *      java.lang.String)
	 */
	public void setNextTaskInFlow(String workName, String taskName) {
		this.nextTaskInFlow = new TaskNodeReferenceImpl(workName, taskName);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.api.build.TaskBuilder#linkManagedObject(M,
	 *      java.lang.String)
	 */
	public void linkManagedObject(M key, String workManagedObjectName) {
		this.linkManagedObject(key.ordinal(), workManagedObjectName);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.api.build.TaskBuilder#linkManagedObject(int,
	 *      java.lang.String)
	 */
	public void linkManagedObject(int managedObjectIndex,
			String workManagedObjectName) {
		this.managedObjects.put(new Integer(managedObjectIndex),
				new TaskManagedObjectConfigurationImpl(workManagedObjectName));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.api.build.TaskBuilder#linkPreTaskAdministration(java.lang.String,
	 *      A)
	 */
	public <A extends Enum<A>> void linkPreTaskAdministration(
			String workAdministratorName, A dutyKey) {
		this.preTaskDuties.add(new TaskDutyConfigurationImpl<A>(
				workAdministratorName, dutyKey));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.api.build.TaskBuilder#linkPostTaskAdministration(java.lang.String,
	 *      A)
	 */
	public <A extends Enum<A>> void linkPostTaskAdministration(
			String workAdministratorName, A dutyKey) {
		this.postTaskDuties.add(new TaskDutyConfigurationImpl<A>(
				workAdministratorName, dutyKey));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.api.build.FlowLinker#linkFlow(F,
	 *      java.lang.String)
	 */
	public void linkFlow(F key, String taskName,
			FlowInstigationStrategyEnum strategy) {
		this.linkFlow(key.ordinal(), null, taskName, strategy);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.api.build.FlowLinker#linkFlow(int,
	 *      java.lang.String)
	 */
	public void linkFlow(int flowIndex, String taskName,
			FlowInstigationStrategyEnum strategy) {
		this.linkFlow(flowIndex, null, taskName, strategy);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.api.build.FlowLinker#linkFlow(F,
	 *      java.lang.String, java.lang.String)
	 */
	public void linkFlow(F key, String workName, String taskName,
			FlowInstigationStrategyEnum strategy) {
		this.linkFlow(key.ordinal(), workName, taskName, strategy);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.api.build.FlowLinker#linkFlow(int,
	 *      java.lang.String, java.lang.String)
	 */
	public void linkFlow(int flowIndex, String workName, String taskName,
			FlowInstigationStrategyEnum strategy) {
		this.flows.put(new Integer(flowIndex), new FlowConfigurationImpl(
				strategy, new TaskNodeReferenceImpl(workName, taskName)));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.api.build.TaskBuilder#addEscalation(java.lang.Class,
	 *      boolean, java.lang.String)
	 */
	@Override
	public void addEscalation(Class<? extends Throwable> typeOfCause,
			boolean isResetThreadState, String taskName) {
		this.escalations.add(new EscalationConfigurationImpl(typeOfCause,
				isResetThreadState, new TaskNodeReferenceImpl(taskName)));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.api.build.TaskBuilder#addEscalation(java.lang.Class,
	 *      boolean, java.lang.String, java.lang.String)
	 */
	@Override
	public void addEscalation(Class<? extends Throwable> typeOfCause,
			boolean isResetThreadState, String workName, String taskName) {
		this.escalations.add(new EscalationConfigurationImpl(typeOfCause,
				isResetThreadState, new TaskNodeReferenceImpl(workName,
						taskName)));
	}

	/*
	 * ====================================================================
	 * TaskConfiguration
	 * ====================================================================
	 */

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.internal.configuration.TaskConfiguration#getTaskName()
	 */
	public String getTaskName() {
		return this.taskName;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.internal.configuration.TaskConfiguration#getTaskFactory()
	 */
	public TaskFactory<P, W, M, F> getTaskFactory() {
		return this.taskFactory;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.internal.configuration.TaskConfiguration#getTeamId()
	 */
	public String getTeamId() {
		return this.teamName;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.internal.configuration.TaskConfiguration#getManagedObjectConfiguration()
	 */
	public TaskManagedObjectConfiguration[] getManagedObjectConfiguration()
			throws ConfigurationException {

		// Create the listing of managed objects
		TaskManagedObjectConfiguration[] tmos = new TaskManagedObjectConfiguration[this.managedObjects
				.size()];
		for (int i = 0; i < tmos.length; i++) {
			tmos[i] = this.managedObjects.get(new Integer(i));
			if (tmos[i] == null) {
				throw new ConfigurationException(
						"Managed Object not linked for index " + i);
			}
		}

		// Return listing
		return tmos;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.internal.configuration.TaskConfiguration#getNextTaskInFlow()
	 */
	public TaskNodeReference getNextTaskInFlow() {
		return this.nextTaskInFlow;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.internal.configuration.TaskConfiguration#getFlowConfiguration()
	 */
	public FlowConfiguration[] getFlowConfiguration()
			throws ConfigurationException {

		// Create the listing of flows
		FlowConfiguration[] fs = new FlowConfiguration[this.flows.size()];
		for (int i = 0; i < fs.length; i++) {
			fs[i] = this.flows.get(new Integer(i));
			if (fs[i] == null) {
				throw new ConfigurationException("Flow not linked for index "
						+ i);
			}
		}

		// Return listing
		return fs;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.internal.configuration.TaskConfiguration#getParentEscalationProcedure()
	 */
	public EscalationProcedure getParentEscalationProcedure() {
		return this.parentEscalationProcedure;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.internal.configuration.TaskConfiguration#getPreTaskAdministratorDutyConfiguration()
	 */
	public TaskDutyConfiguration<?>[] getPreTaskAdministratorDutyConfiguration() {
		return this.preTaskDuties.toArray(new TaskDutyConfiguration[0]);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.internal.configuration.TaskConfiguration#getPostTaskAdministratorDutyConfiguration()
	 */
	public TaskDutyConfiguration<?>[] getPostTaskAdministratorDutyConfiguration() {
		return this.postTaskDuties.toArray(new TaskDutyConfiguration[0]);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.internal.configuration.TaskConfiguration#getEscalations()
	 */
	@Override
	public EscalationConfiguration[] getEscalations()
			throws ConfigurationException {
		return this.escalations.toArray(new EscalationConfiguration[0]);
	}

}

/**
 * Implementation of the
 * {@link net.officefloor.frame.internal.configuration.TaskManagedObjectConfiguration}.
 */
class TaskManagedObjectConfigurationImpl implements
		TaskManagedObjectConfiguration {

	/**
	 * Name of {@link net.officefloor.frame.spi.managedobject.ManagedObject}
	 * local to the {@link Work}.
	 */
	protected final String workManagedObjectName;

	/**
	 * Initiate.
	 * 
	 * @param workManagedObjectName
	 *            Name of
	 *            {@link net.officefloor.frame.spi.managedobject.ManagedObject}
	 *            local to the {@link Work}.
	 */
	public TaskManagedObjectConfigurationImpl(String workManagedObjectName) {
		this.workManagedObjectName = workManagedObjectName;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.internal.configuration.TaskManagedObjectConfiguration#getWorkManagedObjectName()
	 */
	public String getWorkManagedObjectName() {
		return this.workManagedObjectName;
	}

}

/**
 * Implementation of
 * {@link net.officefloor.frame.internal.configuration.TaskDutyConfiguration}.
 */
class TaskDutyConfigurationImpl<A extends Enum<A>> implements
		TaskDutyConfiguration<A> {

	/**
	 * Name of the
	 * {@link net.officefloor.frame.spi.administration.Administrator}.
	 */
	protected final String adminName;

	/**
	 * Key identifying the {@link net.officefloor.frame.spi.administration.Duty}.
	 */
	protected final A dutyKey;

	/**
	 * Initiate.
	 * 
	 * @param adminName
	 *            Name of the
	 *            {@link net.officefloor.frame.spi.administration.Administrator}.
	 * @param dutyKey
	 *            Key identifying the
	 *            {@link net.officefloor.frame.spi.administration.Duty}.
	 */
	public TaskDutyConfigurationImpl(String adminName, A dutyKey) {
		this.adminName = adminName;
		this.dutyKey = dutyKey;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.internal.configuration.TaskDutyConfiguration#getAdministratorName()
	 */
	public String getAdministratorName() {
		return this.adminName;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.internal.configuration.TaskDutyConfiguration#getDuty()
	 */
	public A getDuty() {
		return this.dutyKey;
	}

}

/**
 * Implementation of the
 * {@link net.officefloor.frame.internal.configuration.FlowConfiguration}.
 */
class FlowConfigurationImpl implements FlowConfiguration {

	/**
	 * {@link FlowInstigationStrategyEnum}.
	 */
	protected final FlowInstigationStrategyEnum strategy;

	/**
	 * Reference to the initial {@link net.officefloor.frame.api.execute.Task}
	 * of this {@link net.officefloor.frame.internal.structure.Flow}.
	 */
	protected final TaskNodeReference taskNodeRef;

	/**
	 * Initiate.
	 * 
	 * @param strategy
	 *            {@link FlowInstigationStrategyEnum}.
	 * @param taskNodeRef
	 *            Reference to the initial
	 *            {@link net.officefloor.frame.api.execute.Task} of this
	 *            {@link net.officefloor.frame.internal.structure.Flow}.
	 */
	public FlowConfigurationImpl(FlowInstigationStrategyEnum strategy,
			TaskNodeReference taskNodeRef) {
		this.strategy = strategy;
		this.taskNodeRef = taskNodeRef;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.internal.configuration.FlowConfiguration#getInstigationStrategy()
	 */
	public FlowInstigationStrategyEnum getInstigationStrategy()
			throws ConfigurationException {
		return this.strategy;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.internal.configuration.FlowConfiguration#getInitialTask()
	 */
	public TaskNodeReference getInitialTask() throws ConfigurationException {
		return this.taskNodeRef;
	}

}

/**
 * Implementation of the {@link EscalationConfiguration}.
 */
class EscalationConfigurationImpl implements EscalationConfiguration {

	/**
	 * Type of cause.
	 */
	private final Class<? extends Throwable> typeOfCause;

	/**
	 * Flag indicating to reset the {@link ThreadState}.
	 */
	private final boolean isResetThreadState;

	/**
	 * {@link TaskNodeReference}.
	 */
	private final TaskNodeReference taskNodeReference;

	/**
	 * Initiate.
	 * 
	 * @param typeOfCause
	 *            Type of cause.
	 * @param isResetThreadState
	 *            Flag indicating to reset the {@link ThreadState}.
	 * @param taskNodeReference
	 *            {@link TaskNodeReference}.
	 */
	public EscalationConfigurationImpl(Class<? extends Throwable> typeOfCause,
			boolean isResetThreadState, TaskNodeReference taskNodeReference) {
		this.typeOfCause = typeOfCause;
		this.isResetThreadState = isResetThreadState;
		this.taskNodeReference = taskNodeReference;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.internal.configuration.EscalationConfiguration#getTypeOfCause()
	 */
	@Override
	public Class<? extends Throwable> getTypeOfCause() {
		return this.typeOfCause;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.internal.configuration.EscalationConfiguration#isResetThreadState()
	 */
	@Override
	public boolean isResetThreadState() {
		return this.isResetThreadState;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.internal.configuration.EscalationConfiguration#getTaskNodeReference()
	 */
	@Override
	public TaskNodeReference getTaskNodeReference() {
		return this.taskNodeReference;
	}

}