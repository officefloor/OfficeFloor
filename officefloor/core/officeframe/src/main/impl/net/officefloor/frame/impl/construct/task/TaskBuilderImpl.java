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
package net.officefloor.frame.impl.construct.task;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.build.TaskBuilder;
import net.officefloor.frame.api.build.TaskFactory;
import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.impl.construct.util.ConstructUtil;
import net.officefloor.frame.internal.configuration.TaskEscalationConfiguration;
import net.officefloor.frame.internal.configuration.TaskFlowConfiguration;
import net.officefloor.frame.internal.configuration.TaskConfiguration;
import net.officefloor.frame.internal.configuration.TaskDutyConfiguration;
import net.officefloor.frame.internal.configuration.TaskGovernanceConfiguration;
import net.officefloor.frame.internal.configuration.TaskNodeReference;
import net.officefloor.frame.internal.configuration.TaskObjectConfiguration;
import net.officefloor.frame.internal.structure.EscalationProcedure;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.internal.structure.FlowInstigationStrategyEnum;
import net.officefloor.frame.internal.structure.FunctionState;
import net.officefloor.frame.spi.governance.Governance;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.spi.team.Team;

/**
 * Implementation of the {@link TaskBuilder}.
 * 
 * @author Daniel Sagenschneider
 */
public class TaskBuilderImpl<W extends Work, D extends Enum<D>, F extends Enum<F>>
		implements TaskBuilder<W, D, F>, TaskConfiguration<W, D, F> {

	/**
	 * Name of this {@link Task}.
	 */
	private final String taskName;

	/**
	 * {@link TaskFactory}.
	 */
	private final TaskFactory<W, D, F> taskFactory;

	/**
	 * {@link Object} instances to be linked to this {@link Task}.
	 */
	private final Map<Integer, TaskObjectConfigurationImpl<D>> objects = new HashMap<Integer, TaskObjectConfigurationImpl<D>>();

	/**
	 * {@link Flow} instances to be linked to this {@link Task}.
	 */
	private final Map<Integer, TaskFlowConfigurationImpl<F>> flows = new HashMap<Integer, TaskFlowConfigurationImpl<F>>();

	/**
	 * {@link Governance} instances to be active for this {@link Task}.
	 */
	private final List<TaskGovernanceConfiguration> governances = new LinkedList<TaskGovernanceConfiguration>();

	/**
	 * Differentiator.
	 */
	private Object differentiator = null;

	/**
	 * {@link Team}.
	 */
	private String teamName;

	/**
	 * Next {@link FunctionState} within the {@link Flow}.
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
	 * Listing of {@link TaskEscalationConfiguration} instances to form the
	 * {@link EscalationProcedure} for the resulting {@link Task} of this
	 * {@link TaskBuilder}.
	 */
	private final List<TaskEscalationConfiguration> escalations = new LinkedList<TaskEscalationConfiguration>();

	/**
	 * Initiate.
	 * 
	 * @param taskName
	 *            Name of this {@link Task}.
	 * @param taskFactory
	 *            {@link TaskFactory}.
	 */
	public TaskBuilderImpl(String taskName, TaskFactory<W, D, F> taskFactory) {
		this.taskName = taskName;
		this.taskFactory = taskFactory;
	}

	/*
	 * ================ TaskBuilder =======================================
	 */

	@Override
	public void setDifferentiator(Object differentiator) {
		this.differentiator = differentiator;
	}

	@Override
	public void setTeam(String officeTeamName) {
		this.teamName = officeTeamName;
	}

	@Override
	public void setNextTaskInFlow(String taskName, Class<?> argumentType) {
		this.nextTaskInFlow = new TaskNodeReferenceImpl(taskName, argumentType);
	}

	@Override
	public void setNextTaskInFlow(String workName, String taskName,
			Class<?> argumentType) {
		this.nextTaskInFlow = new TaskNodeReferenceImpl(workName, taskName,
				argumentType);
	}

	@Override
	public void linkParameter(D key, Class<?> parameterType) {
		this.linkObject(key.ordinal(), key, true, null, parameterType);
	}

	@Override
	public void linkParameter(int index, Class<?> parameterType) {
		this.linkObject(index, null, true, null, parameterType);
	}

	@Override
	public void linkManagedObject(D key, String scopeManagedObjectName,
			Class<?> objectType) {
		this.linkObject(key.ordinal(), key, false, scopeManagedObjectName,
				objectType);
	}

	@Override
	public void linkManagedObject(int index, String scopeManagedObjectName,
			Class<?> objectType) {
		this.linkObject(index, null, false, scopeManagedObjectName, objectType);
	}

	/**
	 * Links in a dependent {@link Object}.
	 * 
	 * @param objectIndex
	 *            Index of the {@link Object}.
	 * @param objectKey
	 *            Key of the {@link Object}. Should be <code>null</code> if
	 *            indexed.
	 * @param isParameter
	 *            <code>true</code> if the {@link Object} is a parameter.
	 * @param scopeManagedObjectName
	 *            Name of the {@link ManagedObject} within the
	 *            {@link ManagedObjectSource}. Should be <code>null</code> if a
	 *            parameter.
	 * @param objectType
	 *            Type of {@link Object} required.
	 */
	private void linkObject(int objectIndex, D objectKey, boolean isParameter,
			String scopeManagedObjectName, Class<?> objectType) {
		this.objects.put(new Integer(objectIndex),
				new TaskObjectConfigurationImpl<D>(isParameter,
						scopeManagedObjectName, objectType, objectIndex,
						objectKey));
	}

	@Override
	public void linkPreTaskAdministration(String scopeAdministratorName,
			String dutyName) {
		this.preTaskDuties.add(new TaskDutyConfigurationImpl<None>(
				scopeAdministratorName, dutyName));
	}

	@Override
	public <A extends Enum<A>> void linkPreTaskAdministration(
			String scopeAdministratorName, A dutyKey) {
		this.preTaskDuties.add(new TaskDutyConfigurationImpl<A>(
				scopeAdministratorName, dutyKey));
	}

	@Override
	public void linkPostTaskAdministration(String scopeAdministratorName,
			String dutyName) {
		this.postTaskDuties.add(new TaskDutyConfigurationImpl<None>(
				scopeAdministratorName, dutyName));
	}

	@Override
	public <A extends Enum<A>> void linkPostTaskAdministration(
			String scopeAdministratorName, A dutyKey) {
		this.postTaskDuties.add(new TaskDutyConfigurationImpl<A>(
				scopeAdministratorName, dutyKey));
	}

	@Override
	public void linkFlow(F key, String taskName,
			FlowInstigationStrategyEnum strategy, Class<?> argumentType) {
		this.linkFlow(key.ordinal(), key, null, taskName, strategy,
				argumentType);
	}

	@Override
	public void linkFlow(int flowIndex, String taskName,
			FlowInstigationStrategyEnum strategy, Class<?> argumentType) {
		this.linkFlow(flowIndex, null, null, taskName, strategy, argumentType);
	}

	@Override
	public void linkFlow(F key, String workName, String taskName,
			FlowInstigationStrategyEnum strategy, Class<?> argumentType) {
		this.linkFlow(key.ordinal(), key, workName, taskName, strategy,
				argumentType);
	}

	@Override
	public void linkFlow(int flowIndex, String workName, String taskName,
			FlowInstigationStrategyEnum strategy, Class<?> argumentType) {
		this.linkFlow(flowIndex, null, workName, taskName, strategy,
				argumentType);
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
	 * @param argumentType
	 *            Type of argument passed to the instigated {@link Flow}.
	 */
	private void linkFlow(int flowIndex, F flowKey, String workName,
			String taskName, FlowInstigationStrategyEnum strategy,
			Class<?> argumentType) {

		// Determine the flow name
		String flowName = (flowKey != null ? flowKey.name() : String
				.valueOf(flowIndex));

		// Create the task node reference
		TaskNodeReferenceImpl taskNode = new TaskNodeReferenceImpl(workName,
				taskName, argumentType);

		// Create the flow configuration
		TaskFlowConfigurationImpl<F> flow = new TaskFlowConfigurationImpl<F>(
				flowName, strategy, taskNode, flowIndex, flowKey);

		// Register the flow
		this.flows.put(new Integer(flowIndex), flow);
	}

	@Override
	public void addEscalation(Class<? extends Throwable> typeOfCause,
			String taskName) {
		this.escalations.add(new TaskEscalationConfigurationImpl(typeOfCause,
				new TaskNodeReferenceImpl(taskName, typeOfCause)));
	}

	@Override
	public void addEscalation(Class<? extends Throwable> typeOfCause,
			String workName, String taskName) {
		this.escalations.add(new TaskEscalationConfigurationImpl(typeOfCause,
				new TaskNodeReferenceImpl(workName, taskName, typeOfCause)));
	}

	@Override
	public void addGovernance(String governanceName) {
		this.governances
				.add(new TaskGovernanceConfigurationImpl(governanceName));
	}

	/*
	 * ============ TaskConfiguration =====================================
	 */

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
	public String getOfficeTeamName() {
		return this.teamName;
	}

	@Override
	public TaskObjectConfiguration<D>[] getObjectConfiguration() {
		return ConstructUtil.toArray(this.objects,
				new TaskObjectConfiguration[0]);
	}

	@Override
	public TaskGovernanceConfiguration[] getGovernanceConfiguration() {
		return this.governances
				.toArray(new TaskGovernanceConfiguration[this.governances
						.size()]);
	}

	@Override
	public TaskNodeReference getNextTaskInFlow() {
		return this.nextTaskInFlow;
	}

	@Override
	public TaskFlowConfiguration<F>[] getFlowConfiguration() {
		return ConstructUtil.toArray(this.flows, new TaskFlowConfiguration[0]);
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
	public TaskEscalationConfiguration[] getEscalations() {
		return this.escalations.toArray(new TaskEscalationConfiguration[0]);
	}

}