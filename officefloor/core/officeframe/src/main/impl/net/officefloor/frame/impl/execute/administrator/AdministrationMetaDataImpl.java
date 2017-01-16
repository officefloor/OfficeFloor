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
package net.officefloor.frame.impl.execute.administrator;

import net.officefloor.frame.api.administration.Administration;
import net.officefloor.frame.api.administration.AdministrationFactory;
import net.officefloor.frame.api.governance.Governance;
import net.officefloor.frame.api.team.Team;
import net.officefloor.frame.internal.structure.AdministrationMetaData;
import net.officefloor.frame.internal.structure.EscalationProcedure;
import net.officefloor.frame.internal.structure.ExtensionInterfaceMetaData;
import net.officefloor.frame.internal.structure.FlowMetaData;
import net.officefloor.frame.internal.structure.FunctionLoop;
import net.officefloor.frame.internal.structure.GovernanceActivity;
import net.officefloor.frame.internal.structure.ManagedFunctionMetaData;
import net.officefloor.frame.internal.structure.TeamManagement;
import net.officefloor.frame.internal.structure.ThreadState;

/**
 * Implementation of the {@link AdministrationMetaData}.
 * 
 * @author Daniel Sagenschneider
 */
public class AdministrationMetaDataImpl<E, F extends Enum<F>, G extends Enum<G>>
		implements AdministrationMetaData<E, F, G> {

	/**
	 * Bound name of this {@link Administration}.
	 */
	private final String administrationName;

	/**
	 * {@link AdministrationFactory}.
	 */
	private final AdministrationFactory<E, F, G> administrationFactory;

	/**
	 * {@link ExtensionInterfaceMetaData}.
	 */
	private final ExtensionInterfaceMetaData<E>[] eiMetaData;

	/**
	 * {@link TeamManagement} of {@link Team} responsible for the
	 * {@link GovernanceActivity}.
	 */
	private final TeamManagement responsibleTeam;

	/**
	 * {@link EscalationProcedure}.
	 */
	private final EscalationProcedure escalationProcedure;

	/**
	 * {@link FunctionLoop}.
	 */
	private final FunctionLoop functionLoop;

	/**
	 * {@link FlowMetaData} instances for this {@link Administration}.
	 */
	private FlowMetaData[] flowMetaData;

	/**
	 * Translates the index to a {@link ThreadState} {@link Governance} index.
	 */
	private int[] governanceIndexes;

	/**
	 * Instantiate.
	 * 
	 * @param administrationName
	 *            Bound name of this {@link Administration}.
	 * @param administrationFactory
	 *            {@link AdministrationFactory}.
	 * @param eiMetaData
	 *            {@link ExtensionInterfaceMetaData}.
	 * @param responsibleTeam
	 *            {@link TeamManagement} of {@link Team} responsible for the
	 *            {@link GovernanceActivity}.
	 * @param escalationProcedure
	 *            {@link EscalationProcedure}.
	 * @param functionLoop
	 *            {@link FunctionLoop}.
	 */
	public AdministrationMetaDataImpl(String administrationName, AdministrationFactory<E, F, G> administrationFactory,
			ExtensionInterfaceMetaData<E>[] eiMetaData, TeamManagement responsibleTeam,
			EscalationProcedure escalationProcedure, FunctionLoop functionLoop) {
		this.administrationName = administrationName;
		this.administrationFactory = administrationFactory;
		this.eiMetaData = eiMetaData;
		this.responsibleTeam = responsibleTeam;
		this.escalationProcedure = escalationProcedure;
		this.functionLoop = functionLoop;
	}

	/**
	 * Loads the remaining state.
	 * 
	 * @param flowMetaData
	 *            {@link FlowMetaData} instances for this
	 *            {@link Administration}.
	 * @param governanceIndexes
	 *            Translates the index to a {@link ThreadState}
	 *            {@link Governance} index.
	 */
	public void loadRemainingState(FlowMetaData[] flowMetaData, int[] governanceIndexes) {
		this.flowMetaData = flowMetaData;
		this.governanceIndexes = governanceIndexes;
	}

	/*
	 * ================= ManagedFunctionContainerMetaData =================
	 */

	@Override
	public String getFunctionName() {
		return Administration.class.getSimpleName() + "-" + this.administrationName;
	}

	@Override
	public TeamManagement getResponsibleTeam() {
		return this.responsibleTeam;
	}

	@Override
	public FlowMetaData getFlow(int flowIndex) {
		return this.flowMetaData[flowIndex];
	}

	@Override
	public ManagedFunctionMetaData<?, ?> getNextManagedFunctionMetaData() {
		return null; // no next function
	}

	@Override
	public EscalationProcedure getEscalationProcedure() {
		return this.escalationProcedure;
	}

	@Override
	public FunctionLoop getFunctionLoop() {
		return this.functionLoop;
	}

	/*
	 * ================= AdministratorMetaData ============================
	 */

	@Override
	public ExtensionInterfaceMetaData<E>[] getExtensionInterfaceMetaData() {
		return this.eiMetaData;
	}

	@Override
	public String getAdministrationName() {
		return this.administrationName;
	}

	@Override
	public AdministrationFactory<E, F, G> getAdministrationFactory() {
		return this.administrationFactory;
	}

	@Override
	public int translateGovernanceIndexToThreadIndex(int governanceIndex) {
		return this.governanceIndexes[governanceIndex];
	}

}