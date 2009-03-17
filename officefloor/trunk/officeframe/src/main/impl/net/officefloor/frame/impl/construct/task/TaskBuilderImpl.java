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
package net.officefloor.frame.impl.construct.task;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.officefloor.frame.api.build.TaskBuilder;
import net.officefloor.frame.api.build.TaskFactory;
import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.impl.construct.util.ConstructUtil;
import net.officefloor.frame.internal.configuration.EscalationConfiguration;
import net.officefloor.frame.internal.configuration.FlowConfiguration;
import net.officefloor.frame.internal.configuration.TaskConfiguration;
import net.officefloor.frame.internal.configuration.TaskDutyConfiguration;
import net.officefloor.frame.internal.configuration.TaskManagedObjectConfiguration;
import net.officefloor.frame.internal.configuration.TaskNodeReference;
import net.officefloor.frame.internal.structure.EscalationProcedure;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.internal.structure.FlowInstigationStrategyEnum;
import net.officefloor.frame.internal.structure.JobNode;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.team.Team;

/**
 * Implementation of the {@link TaskBuilder}.
 * 
 * @author Daniel
 */
public class TaskBuilderImpl<P extends Object, W extends Work, M extends Enum<M>, F extends Enum<F>>
		implements TaskBuilder<P, W, M, F>, TaskConfiguration<P, W, M, F> {

	/**
	 * Name of this {@link Task}.
	 */
	private final String taskName;

	/**
	 * {@link TaskFactory}.
	 */
	private final TaskFactory<P, W, M, F> taskFactory;

	/**
	 * {@link ManagedObject} instances to be linked to this {@link Task}.
	 */
	private final Map<Integer, TaskManagedObjectConfigurationImpl> managedObjects = new HashMap<Integer, TaskManagedObjectConfigurationImpl>();

	/**
	 * {@link Flow} instances to be linked to this {@link Task}.
	 */
	private final Map<Integer, FlowConfigurationImpl> flows = new HashMap<Integer, FlowConfigurationImpl>();

	/**
	 * {@link Team}.
	 */
	private String teamName;

	/**
	 * Next {@link JobNode} within the {@link Flow}.
	 */
	private TaskNodeReference nextTaskInFlow;

	/**
	 * Listing of task administration duties to do before executing the
	 * {@link Task}.
	 */
	private final List<TaskDutyConfigurationImpl<?>> preTaskDuties = new LinkedList<TaskDutyConfigurationImpl<?>>();

	/**
	 * Listing of task administration duties to do after executing the
	 * {@link Task}.
	 */
	private final List<TaskDutyConfigurationImpl<?>> postTaskDuties = new LinkedList<TaskDutyConfigurationImpl<?>>();

	/**
	 * Listing of {@link EscalationConfiguration} instances to form the
	 * {@link EscalationProcedure} for the resulting {@link Task} of this
	 * {@link TaskBuilder}.
	 */
	private final List<EscalationConfiguration> escalations = new LinkedList<EscalationConfiguration>();

	/**
	 * Initiate.
	 * 
	 * @param taskName
	 *            Name of this {@link Task}.
	 * @param taskFactory
	 *            {@link TaskFactory}.
	 */
	public TaskBuilderImpl(String taskName, TaskFactory<P, W, M, F> taskFactory) {
		this.taskName = taskName;
		this.taskFactory = taskFactory;
	}

	/*
	 * ================ TaskBuilder =======================================
	 */

	@Override
	public void setTeam(String officeTeamName) {
		this.teamName = officeTeamName;
	}

	@Override
	public void setNextTaskInFlow(String taskName) {
		this.nextTaskInFlow = new TaskNodeReferenceImpl(taskName);
	}

	@Override
	public void setNextTaskInFlow(String workName, String taskName) {
		this.nextTaskInFlow = new TaskNodeReferenceImpl(workName, taskName);
	}

	@Override
	public void linkManagedObject(M key, String scopeManagedObjectName) {
		this.linkManagedObject(key.ordinal(), scopeManagedObjectName);
	}

	@Override
	public void linkManagedObject(int managedObjectIndex,
			String scopeManagedObjectName) {
		this.managedObjects.put(new Integer(managedObjectIndex),
				new TaskManagedObjectConfigurationImpl(scopeManagedObjectName));
	}

	@Override
	public <A extends Enum<A>> void linkPreTaskAdministration(
			String scopeAdministratorName, A dutyKey) {
		this.preTaskDuties.add(new TaskDutyConfigurationImpl<A>(
				scopeAdministratorName, dutyKey));
	}

	@Override
	public <A extends Enum<A>> void linkPostTaskAdministration(
			String scopeAdministratorName, A dutyKey) {
		this.postTaskDuties.add(new TaskDutyConfigurationImpl<A>(
				scopeAdministratorName, dutyKey));
	}

	@Override
	public void linkFlow(F key, String taskName,
			FlowInstigationStrategyEnum strategy) {
		this.linkFlow(key.ordinal(), key, null, taskName, strategy);
	}

	@Override
	public void linkFlow(int flowIndex, String taskName,
			FlowInstigationStrategyEnum strategy) {
		this.linkFlow(flowIndex, null, null, taskName, strategy);
	}

	@Override
	public void linkFlow(F key, String workName, String taskName,
			FlowInstigationStrategyEnum strategy) {
		this.linkFlow(key.ordinal(), key, workName, taskName, strategy);
	}

	@Override
	public void linkFlow(int flowIndex, String workName, String taskName,
			FlowInstigationStrategyEnum strategy) {
		this.linkFlow(flowIndex, null, workName, taskName, strategy);
	}

	/**
	 * Links in a {@link Flow}.
	 * 
	 * @param flowIndex
	 *            Index of the {@link Flow}.
	 * @param flowKey
	 *            Key of the {@link Flow}. Should be <code>null</code> if
	 *            indexed.
	 * @param workName
	 *            Name of the {@link Work}. May be <code>null</code> if same
	 *            {@link Work}.
	 * @param taskName
	 *            Name of the {@link Task}.
	 * @param strategy
	 *            {@link FlowInstigationStrategyEnum}.
	 */
	private void linkFlow(int flowIndex, F flowKey, String workName,
			String taskName, FlowInstigationStrategyEnum strategy) {

		// Determine the flow name
		String flowName = (flowKey != null ? flowKey.name() : String
				.valueOf(flowIndex));

		// Create the task node reference (no task name, no reference)
		TaskNodeReferenceImpl taskNode = (taskName == null ? null
				: new TaskNodeReferenceImpl(workName, taskName));

		// Create the flow configuration
		FlowConfigurationImpl flow = new FlowConfigurationImpl(flowName,
				strategy, taskNode);

		// Register the flow
		this.flows.put(new Integer(flowIndex), flow);
	}

	@Override
	public void addEscalation(Class<? extends Throwable> typeOfCause,
			String taskName) {
		this.escalations.add(new EscalationConfigurationImpl(typeOfCause,
				new TaskNodeReferenceImpl(taskName)));
	}

	@Override
	public void addEscalation(Class<? extends Throwable> typeOfCause,
			String workName, String taskName) {
		this.escalations.add(new EscalationConfigurationImpl(typeOfCause,
				new TaskNodeReferenceImpl(workName, taskName)));
	}

	/*
	 * ============ TaskConfiguration =====================================
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
	public String getOfficeTeamName() {
		return this.teamName;
	}

	@Override
	public TaskManagedObjectConfiguration[] getManagedObjectConfiguration() {
		return ConstructUtil.toArray(this.managedObjects,
				new TaskManagedObjectConfiguration[0]);
	}

	@Override
	public TaskNodeReference getNextTaskInFlow() {
		return this.nextTaskInFlow;
	}

	@Override
	public FlowConfiguration[] getFlowConfiguration() {
		return ConstructUtil.toArray(this.flows, new FlowConfiguration[0]);
	}

	@Override
	public TaskDutyConfiguration<?>[] getPreTaskAdministratorDutyConfiguration() {
		return this.preTaskDuties.toArray(new TaskDutyConfiguration[0]);
	}

	@Override
	public TaskDutyConfiguration<?>[] getPostTaskAdministratorDutyConfiguration() {
		return this.postTaskDuties.toArray(new TaskDutyConfiguration[0]);
	}

	@Override
	public EscalationConfiguration[] getEscalations() {
		return this.escalations.toArray(new EscalationConfiguration[0]);
	}

}