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
package net.officefloor.frame.impl.execute.managedfunction;

import net.officefloor.frame.api.build.ManagedFunctionFactory;
import net.officefloor.frame.api.execute.ManagedFunction;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.internal.structure.EscalationProcedure;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.internal.structure.FlowMetaData;
import net.officefloor.frame.internal.structure.FunctionLoop;
import net.officefloor.frame.internal.structure.GovernanceDeactivationStrategy;
import net.officefloor.frame.internal.structure.ManagedFunctionContainer;
import net.officefloor.frame.internal.structure.ManagedFunctionMetaData;
import net.officefloor.frame.internal.structure.ManagedObjectIndex;
import net.officefloor.frame.internal.structure.ManagedFunctionDutyAssociation;
import net.officefloor.frame.internal.structure.TeamManagement;
import net.officefloor.frame.internal.structure.WorkContainer;
import net.officefloor.frame.internal.structure.WorkMetaData;
import net.officefloor.frame.spi.administration.Duty;
import net.officefloor.frame.spi.governance.Governance;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.team.Job;
import net.officefloor.frame.spi.team.Team;

/**
 * Meta-data of a {@link ManagedFunction}.
 * 
 * @author Daniel Sagenschneider
 */
public class ManagedFunctionMetaDataImpl<W extends Work, D extends Enum<D>, F extends Enum<F>>
		implements ManagedFunctionMetaData<W, D, F> {

	/**
	 * Name of the {@link ManagedFunction}.
	 */
	private final String functionName;

	/**
	 * {@link ManagedFunctionFactory} to create the {@link ManagedFunction} of
	 * the {@link ManagedFunctionMetaData}.
	 */
	private final ManagedFunctionFactory<W, D, F> functionFactory;

	/**
	 * Differentiator.
	 */
	private final Object differentiator;

	/**
	 * Parameter type of this {@link ManagedFunction}.
	 */
	private final Class<?> parameterType;

	/**
	 * {@link TeamManagement} of the {@link Team} responsible for executing this
	 * {@link ManagedFunction}.
	 */
	private final TeamManagement responsibleTeam;

	/**
	 * {@link ManagedObjectIndex} instances identifying the
	 * {@link ManagedObject} instances that must be loaded before the
	 * {@link ManagedFunction} may be executed.
	 */
	private final ManagedObjectIndex[] requiredManagedObjects;

	/**
	 * Translations of the {@link ManagedFunction} {@link ManagedObject} index
	 * to the {@link Work} {@link ManagedObjectIndex}.
	 */
	private final ManagedObjectIndex[] taskToWorkMoTranslations;

	/**
	 * Required {@link Governance}.
	 */
	private final boolean[] requiredGovernance;

	/**
	 * {@link ManagedFunctionDutyAssociation} specifying the {@link Duty} instances to be
	 * completed before executing the {@link ManagedFunction}.
	 */
	private final ManagedFunctionDutyAssociation<?>[] preTaskDuties;

	/**
	 * {@link ManagedFunctionDutyAssociation} specifying the {@link Duty} instances to be
	 * completed after executing the {@link ManagedFunction}.
	 */
	private final ManagedFunctionDutyAssociation<?>[] postTaskDuties;

	/**
	 * {@link FunctionLoop}.
	 */
	private final FunctionLoop functionLoop;

	/**
	 * <p>
	 * {@link WorkMetaData} for this {@link ManagedFunction}.
	 * <p>
	 * Acts as <code>final</code> but specified after constructor.
	 */
	private WorkMetaData<W> workMetaData;

	/**
	 * <p>
	 * Meta-data of the available {@link Flow} instances from this
	 * {@link ManagedFunction}.
	 * <p>
	 * Acts as <code>final</code> but specified after constructor.
	 */
	private FlowMetaData<?>[] flowMetaData;

	/**
	 * {@link ManagedFunctionMetaData} of the next {@link ManagedFunction}.
	 */
	private ManagedFunctionMetaData<?, ?, ?> nextFunctionMetaData;

	/**
	 * {@link EscalationProcedure} for exceptions of the {@link ManagedFunction}
	 * of this {@link ManagedFunctionMetaData}.
	 */
	private EscalationProcedure escalationProcedure;

	/**
	 * Initiate with details of the meta-data for the {@link ManagedFunction}.
	 * 
	 * @param functionName
	 *            Name of the {@link ManagedFunction}.
	 * @param functionFactory
	 *            {@link ManagedFunctionFactory} to create the
	 *            {@link ManagedFunction} of the
	 *            {@link ManagedFunctionMetaData}.
	 * @param differentiator
	 *            Differentiator. May be <code>null</code>.
	 * @param parameterType
	 *            Parameter type of this {@link ManagedFunction}.
	 * @param responsibleTeam
	 *            {@link TeamManagement} of the {@link Team} responsible for
	 *            executing this {@link ManagedFunction}. May be
	 *            <code>null</code>.
	 * @param continueTeam
	 *            {@link Team} to enable the worker ({@link Thread}) of the
	 *            responsible {@link Team} to continue on to execute the next
	 *            {@link Job}.
	 * @param requiredManagedObjects
	 *            {@link ManagedObjectIndex} instances identifying the
	 *            {@link ManagedObject} instances that must be loaded before the
	 *            {@link ManagedFunction} may be executed.
	 * @param requiredGovernance
	 *            Required {@link Governance}.
	 * @param taskToWorkMoTranslations
	 *            Translations of the {@link ManagedFunction}
	 *            {@link ManagedObject} index to the {@link Work}
	 *            {@link ManagedObjectIndex}.
	 * @param preTaskDuties
	 *            {@link ManagedFunctionDutyAssociation} specifying the {@link Duty}
	 *            instances to be completed before executing the
	 *            {@link ManagedFunction}.
	 * @param postTaskDuties
	 *            {@link ManagedFunctionDutyAssociation} specifying the {@link Duty}
	 *            instances to be completed after executing the
	 *            {@link ManagedFunction}.
	 * @param functionLoop
	 *            {@link FunctionLoop}.
	 */
	public ManagedFunctionMetaDataImpl(String functionName, ManagedFunctionFactory<W, D, F> functionFactory,
			Object differentiator, Class<?> parameterType, TeamManagement responsibleTeam,
			ManagedObjectIndex[] requiredManagedObjects, ManagedObjectIndex[] taskToWorkMoTranslations,
			boolean[] requiredGovernance, ManagedFunctionDutyAssociation<?>[] preTaskDuties,
			ManagedFunctionDutyAssociation<?>[] postTaskDuties, FunctionLoop functionLoop) {
		this.functionName = functionName;
		this.functionFactory = functionFactory;
		this.differentiator = differentiator;
		this.parameterType = parameterType;
		this.responsibleTeam = responsibleTeam;
		this.requiredManagedObjects = requiredManagedObjects;
		this.taskToWorkMoTranslations = taskToWorkMoTranslations;
		this.requiredGovernance = requiredGovernance;
		this.preTaskDuties = preTaskDuties;
		this.postTaskDuties = postTaskDuties;
		this.functionLoop = functionLoop;
	}

	/**
	 * Loads the remaining state of this {@link ManagedFunctionMetaData}.
	 * 
	 * @param workMetaData
	 *            {@link WorkMetaData} for this {@link ManagedFunction}.
	 * @param flowMetaData
	 *            Meta-data of the available {@link Flow} instances from this
	 *            {@link ManagedFunction}.
	 * @param nextFunctionMetaData
	 *            {@link ManagedFunctionMetaData} of the next
	 *            {@link ManagedFunction}.
	 * @param escalationProcedure
	 *            {@link EscalationProcedure} for exceptions of the
	 *            {@link ManagedFunction} of this
	 *            {@link ManagedFunctionMetaData}.
	 */
	public void loadRemainingState(WorkMetaData<W> workMetaData, FlowMetaData<?>[] flowMetaData,
			ManagedFunctionMetaData<?, ?, ?> nextFunctionMetaData, EscalationProcedure escalationProcedure) {
		this.workMetaData = workMetaData;
		this.flowMetaData = flowMetaData;
		this.nextFunctionMetaData = nextFunctionMetaData;
		this.escalationProcedure = escalationProcedure;
	}

	/*
	 * =============== ManagedFunctionMetaData ========================
	 */

	@Override
	public String getFunctionName() {
		return this.functionName;
	}

	@Override
	public ManagedFunctionFactory<W, D, F> getManagedFunctionFactory() {
		return this.functionFactory;
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
	public ManagedFunctionMetaData<?, ?, ?> getNextManagedFunctionMetaData() {
		return this.nextFunctionMetaData;
	}

	@Override
	public ManagedFunctionDutyAssociation<?>[] getPreAdministrationMetaData() {
		return this.preTaskDuties;
	}

	@Override
	public ManagedFunctionDutyAssociation<?>[] getPostAdministrationMetaData() {
		return this.postTaskDuties;
	}

	@Override
	public FunctionLoop getFunctionLoop() {
		return this.functionLoop;
	}

	@Override
	public ManagedFunctionContainer createManagedFunctionContainer(Flow flow, WorkContainer<W> workContainer,
			ManagedFunctionContainer parallelFunctionOwner, Object parameter,
			GovernanceDeactivationStrategy governanceDeactivationStrategy) {
		return new ManagedFunctionImpl<W, D, F>(flow, workContainer, this, governanceDeactivationStrategy,
				parallelFunctionOwner, parameter);
	}

}