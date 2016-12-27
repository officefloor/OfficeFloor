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
import net.officefloor.frame.api.build.ManagedFunctionBuilder;
import net.officefloor.frame.api.build.ManagedFunctionFactory;
import net.officefloor.frame.api.execute.ManagedFunction;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.impl.construct.util.ConstructUtil;
import net.officefloor.frame.internal.configuration.ManagedFunctionEscalationConfiguration;
import net.officefloor.frame.internal.configuration.ManagedFunctionFlowConfiguration;
import net.officefloor.frame.internal.configuration.ManagedFunctionConfiguration;
import net.officefloor.frame.internal.configuration.ManagedFunctionDutyConfiguration;
import net.officefloor.frame.internal.configuration.ManagedFunctionGovernanceConfiguration;
import net.officefloor.frame.internal.configuration.ManagedFunctionReference;
import net.officefloor.frame.internal.configuration.ManagedFunctionObjectConfiguration;
import net.officefloor.frame.internal.structure.EscalationProcedure;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.internal.structure.FlowInstigationStrategyEnum;
import net.officefloor.frame.internal.structure.FunctionState;
import net.officefloor.frame.spi.governance.Governance;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.spi.team.Team;

/**
 * Implementation of the {@link ManagedFunctionBuilder}.
 * 
 * @author Daniel Sagenschneider
 */
public class TaskBuilderImpl<W extends Work, D extends Enum<D>, F extends Enum<F>>
		implements ManagedFunctionBuilder<W, D, F>, ManagedFunctionConfiguration<W, D, F> {

	/**
	 * Name of this {@link ManagedFunction}.
	 */
	private final String taskName;

	/**
	 * {@link ManagedFunctionFactory}.
	 */
	private final ManagedFunctionFactory<W, D, F> taskFactory;

	/**
	 * {@link Object} instances to be linked to this {@link ManagedFunction}.
	 */
	private final Map<Integer, TaskObjectConfigurationImpl<D>> objects = new HashMap<Integer, TaskObjectConfigurationImpl<D>>();

	/**
	 * {@link Flow} instances to be linked to this {@link ManagedFunction}.
	 */
	private final Map<Integer, TaskFlowConfigurationImpl<F>> flows = new HashMap<Integer, TaskFlowConfigurationImpl<F>>();

	/**
	 * {@link Governance} instances to be active for this {@link ManagedFunction}.
	 */
	private final List<ManagedFunctionGovernanceConfiguration> governances = new LinkedList<ManagedFunctionGovernanceConfiguration>();

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
	private ManagedFunctionReference nextTaskInFlow;

	/**
	 * Listing of task administration duties to do before executing the
	 * {@link ManagedFunction}.
	 */
	private final List<TaskDutyConfigurationImpl<?>> preTaskDuties = new LinkedList<TaskDutyConfigurationImpl<?>>();

	/**
	 * Listing of task administration duties to do after executing the
	 * {@link ManagedFunction}.
	 */
	private final List<TaskDutyConfigurationImpl<?>> postTaskDuties = new LinkedList<TaskDutyConfigurationImpl<?>>();

	/**
	 * Listing of {@link ManagedFunctionEscalationConfiguration} instances to form the
	 * {@link EscalationProcedure} for the resulting {@link ManagedFunction} of this
	 * {@link ManagedFunctionBuilder}.
	 */
	private final List<ManagedFunctionEscalationConfiguration> escalations = new LinkedList<ManagedFunctionEscalationConfiguration>();

	/**
	 * Initiate.
	 * 
	 * @param taskName
	 *            Name of this {@link ManagedFunction}.
	 * @param taskFactory
	 *            {@link ManagedFunctionFactory}.
	 */
	public TaskBuilderImpl(String taskName, ManagedFunctionFactory<W, D, F> taskFactory) {
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
	 *            Name of the {@link ManagedFunction}.
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
	public ManagedFunctionFactory<W, D, F> getTaskFactory() {
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
	public ManagedFunctionObjectConfiguration<D>[] getObjectConfiguration() {
		return ConstructUtil.toArray(this.objects,
				new ManagedFunctionObjectConfiguration[0]);
	}

	@Override
	public ManagedFunctionGovernanceConfiguration[] getGovernanceConfiguration() {
		return this.governances
				.toArray(new ManagedFunctionGovernanceConfiguration[this.governances
						.size()]);
	}

	@Override
	public ManagedFunctionReference getNextTaskInFlow() {
		return this.nextTaskInFlow;
	}

	@Override
	public ManagedFunctionFlowConfiguration<F>[] getFlowConfiguration() {
		return ConstructUtil.toArray(this.flows, new ManagedFunctionFlowConfiguration[0]);
	}

	@Override
	public ManagedFunctionDutyConfiguration<?>[] getPreTaskAdministratorDutyConfiguration() {
		return this.preTaskDuties.toArray(new ManagedFunctionDutyConfiguration[0]);
	}

	@Override
	public ManagedFunctionDutyConfiguration<?>[] getPostTaskAdministratorDutyConfiguration() {
		return this.postTaskDuties.toArray(new ManagedFunctionDutyConfiguration[0]);
	}

	@Override
	public ManagedFunctionEscalationConfiguration[] getEscalations() {
		return this.escalations.toArray(new ManagedFunctionEscalationConfiguration[0]);
	}

}