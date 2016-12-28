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
import net.officefloor.frame.internal.structure.AdministratorMetaData;
import net.officefloor.frame.internal.structure.EscalationProcedure;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.internal.structure.FlowMetaData;
import net.officefloor.frame.internal.structure.FunctionLoop;
import net.officefloor.frame.internal.structure.ManagedFunctionDutyAssociation;
import net.officefloor.frame.internal.structure.ManagedFunctionMetaData;
import net.officefloor.frame.internal.structure.ManagedObjectIndex;
import net.officefloor.frame.internal.structure.ManagedObjectMetaData;
import net.officefloor.frame.internal.structure.TeamManagement;
import net.officefloor.frame.spi.administration.Administrator;
import net.officefloor.frame.spi.administration.Duty;
import net.officefloor.frame.spi.governance.Governance;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.team.Team;

/**
 * Meta-data of a {@link ManagedFunction}.
 * 
 * @author Daniel Sagenschneider
 */
public class ManagedFunctionMetaDataImpl<O extends Enum<O>, F extends Enum<F>>
		implements ManagedFunctionMetaData<O, F> {

	/**
	 * Name of the {@link ManagedFunction}.
	 */
	private final String functionName;

	/**
	 * {@link ManagedFunctionFactory} to create the {@link ManagedFunction} of
	 * the {@link ManagedFunctionMetaData}.
	 */
	private final ManagedFunctionFactory<O, F> functionFactory;

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
	 * Required {@link Governance}.
	 */
	private final boolean[] requiredGovernance;

	/**
	 * Translates the {@link ManagedFunction} index to the
	 * {@link ManagedObjectIndex} to obtain the {@link ManagedObject} for the
	 * {@link ManagedFunction}.
	 */
	private final ManagedObjectIndex[] functionIndexedManagedObjects;

	/**
	 * {@link ManagedFunctionDutyAssociation} specifying the {@link Duty}
	 * instances to be completed before executing the {@link ManagedFunction}.
	 */
	private final ManagedFunctionDutyAssociation<?>[] preFunctionDuties;

	/**
	 * {@link ManagedFunctionDutyAssociation} specifying the {@link Duty}
	 * instances to be completed after executing the {@link ManagedFunction}.
	 */
	private final ManagedFunctionDutyAssociation<?>[] postFunctionDuties;

	/**
	 * {@link ManagedObjectMetaData} for the {@link ManagedObject} instances
	 * bound to this {@link ManagedFunction}.
	 */
	private final ManagedObjectMetaData<?>[] functionBoundManagedObjects;

	/**
	 * {@link AdministratorMetaData} for the {@link Administrator} instances
	 * bound to this {@link ManagedFunction}.
	 */
	private final AdministratorMetaData<?, ?>[] functionBoundAdministrators;

	/**
	 * {@link FunctionLoop}.
	 */
	private final FunctionLoop functionLoop;

	/**
	 * <p>
	 * Meta-data of the available {@link Flow} instances from this
	 * {@link ManagedFunction}.
	 * <p>
	 * Acts as <code>final</code> but specified after constructor.
	 */
	private FlowMetaData[] flowMetaData;

	/**
	 * {@link ManagedFunctionMetaData} of the next {@link ManagedFunction}.
	 */
	private ManagedFunctionMetaData<?, ?> nextFunctionMetaData;

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
	 * @param functionIndexedManagedObjects
	 *            Translates the {@link ManagedFunction} index to the
	 *            {@link ManagedObjectIndex} to obtain the {@link ManagedObject}
	 *            for the {@link ManagedFunction}.
	 * @param requiredManagedObjects
	 *            {@link ManagedObjectIndex} instances identifying the
	 *            {@link ManagedObject} instances that must be loaded before the
	 *            {@link ManagedFunction} may be executed.
	 * @param requiredGovernance
	 *            Required {@link Governance}.
	 * @param preFunctionDuties
	 *            {@link ManagedFunctionDutyAssociation} specifying the
	 *            {@link Duty} instances to be completed before executing the
	 *            {@link ManagedFunction}.
	 * @param postFunctionDuties
	 *            {@link ManagedFunctionDutyAssociation} specifying the
	 *            {@link Duty} instances to be completed after executing the
	 *            {@link ManagedFunction}.
	 * @param functionLoop
	 *            {@link FunctionLoop}.
	 */
	public ManagedFunctionMetaDataImpl(String functionName, ManagedFunctionFactory<O, F> functionFactory,
			Object differentiator, Class<?> parameterType, TeamManagement responsibleTeam,
			ManagedObjectIndex[] functionIndexedManagedObjects, ManagedObjectMetaData<?>[] functionBoundManagedObjects,
			ManagedObjectIndex[] requiredManagedObjects, boolean[] requiredGovernance,
			AdministratorMetaData<?, ?>[] functionBoundAdministrators,
			ManagedFunctionDutyAssociation<?>[] preFunctionDuties,
			ManagedFunctionDutyAssociation<?>[] postFunctionDuties, FunctionLoop functionLoop) {
		this.functionName = functionName;
		this.functionFactory = functionFactory;
		this.differentiator = differentiator;
		this.parameterType = parameterType;
		this.responsibleTeam = responsibleTeam;
		this.functionIndexedManagedObjects = functionIndexedManagedObjects;
		this.functionBoundManagedObjects = functionBoundManagedObjects;
		this.requiredManagedObjects = requiredManagedObjects;
		this.requiredGovernance = requiredGovernance;
		this.functionBoundAdministrators = functionBoundAdministrators;
		this.preFunctionDuties = preFunctionDuties;
		this.postFunctionDuties = postFunctionDuties;
		this.functionLoop = functionLoop;
	}

	/**
	 * Loads the remaining state of this {@link ManagedFunctionMetaData}.
	 * 
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
	public void loadRemainingState(FlowMetaData[] flowMetaData, ManagedFunctionMetaData<?, ?> nextFunctionMetaData,
			EscalationProcedure escalationProcedure) {
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
	public ManagedFunctionFactory<O, F> getManagedFunctionFactory() {
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
	public ManagedObjectIndex getManagedObject(int managedObjectIndex) {
		return this.functionIndexedManagedObjects[managedObjectIndex];
	}

	@Override
	public FlowMetaData getFlow(int flowIndex) {
		return this.flowMetaData[flowIndex];
	}

	@Override
	public EscalationProcedure getEscalationProcedure() {
		return this.escalationProcedure;
	}

	@Override
	public ManagedFunctionMetaData<?, ?> getNextManagedFunctionMetaData() {
		return this.nextFunctionMetaData;
	}

	@Override
	public ManagedFunctionDutyAssociation<?>[] getPreAdministrationMetaData() {
		return this.preFunctionDuties;
	}

	@Override
	public ManagedFunctionDutyAssociation<?>[] getPostAdministrationMetaData() {
		return this.postFunctionDuties;
	}

	@Override
	public ManagedObjectMetaData<?>[] getManagedObjectMetaData() {
		return this.functionBoundManagedObjects;
	}

	@Override
	public AdministratorMetaData<?, ?>[] getAdministratorMetaData() {
		return this.functionBoundAdministrators;
	}

	@Override
	public FunctionLoop getFunctionLoop() {
		return this.functionLoop;
	}

}