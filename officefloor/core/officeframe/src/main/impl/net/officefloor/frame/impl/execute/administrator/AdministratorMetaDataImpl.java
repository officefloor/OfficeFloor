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

import net.officefloor.compile.spi.administration.source.AdministratorSource;
import net.officefloor.frame.api.administration.Administration;
import net.officefloor.frame.api.administration.DutyKey;
import net.officefloor.frame.api.team.Team;
import net.officefloor.frame.internal.structure.AdministrationMetaData;
import net.officefloor.frame.internal.structure.DutyMetaData;
import net.officefloor.frame.internal.structure.EscalationProcedure;
import net.officefloor.frame.internal.structure.ExtensionInterfaceMetaData;
import net.officefloor.frame.internal.structure.FunctionLoop;
import net.officefloor.frame.internal.structure.GovernanceActivity;
import net.officefloor.frame.internal.structure.ManagedFunctionMetaData;
import net.officefloor.frame.internal.structure.TeamManagement;

/**
 * Implementation of the {@link AdministrationMetaData}.
 * 
 * @author Daniel Sagenschneider
 */
public class AdministratorMetaDataImpl<E extends Object, A extends Enum<A>> implements AdministrationMetaData<E, A> {

	/**
	 * {@link AdministratorSource}.
	 */
	private final AdministratorSource<E, A> administratorSource;

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
	 * <p>
	 * Listing of {@link DutyMetaData} in order of {@link DutyKey} indexes.
	 * <p>
	 * This is treated as <code>final</code>.
	 */
	protected DutyMetaData[] dutyMetaData;

	/**
	 * Instantiate.
	 * 
	 * @param administratorSource
	 *            {@link AdministratorSource}.
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
	public AdministratorMetaDataImpl(AdministratorSource<E, A> administratorSource,
			ExtensionInterfaceMetaData<E>[] eiMetaData, TeamManagement responsibleTeam,
			EscalationProcedure escalationProcedure, FunctionLoop functionLoop) {
		this.eiMetaData = eiMetaData;
		this.administratorSource = administratorSource;
		this.responsibleTeam = responsibleTeam;
		this.escalationProcedure = escalationProcedure;
		this.functionLoop = functionLoop;
	}

	/**
	 * Loads the remaining state.
	 * 
	 * @param dutyMetaData
	 *            Listing of {@link DutyMetaData} in order of {@link DutyKey}
	 *            indexes.
	 */
	public void loadRemainingState(DutyMetaData[] dutyMetaData) {
		this.dutyMetaData = dutyMetaData;
	}

	/*
	 * ================= ManagedFunctionContainerMetaData =================
	 */

	@Override
	public String getFunctionName() {
		return Administration.class.getSimpleName() + "-" + this.administratorSource.getClass().getName();
	}

	@Override
	public TeamManagement getResponsibleTeam() {
		return this.responsibleTeam;
	}

	@Override
	public FunctionLoop getFunctionLoop() {
		return this.functionLoop;
	}

	@Override
	public ManagedFunctionMetaData<?, ?> getNextManagedFunctionMetaData() {
		return null; // no next function
	}

	@Override
	public EscalationProcedure getEscalationProcedure() {
		return this.escalationProcedure;
	}

	/*
	 * ================= AdministratorMetaData ============================
	 */

	@Override
	public AdministratorSource<E, A> getAdministratorSource() {
		return this.administratorSource;
	}

	@Override
	public ExtensionInterfaceMetaData<E>[] getExtensionInterfaceMetaData() {
		return this.eiMetaData;
	}

	@Override
	public DutyMetaData getDutyMetaData(DutyKey<A> dutyKey) {
		return this.dutyMetaData[dutyKey.getIndex()];
	}

}