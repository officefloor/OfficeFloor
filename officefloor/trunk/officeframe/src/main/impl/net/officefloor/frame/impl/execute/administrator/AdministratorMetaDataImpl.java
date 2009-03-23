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
package net.officefloor.frame.impl.execute.administrator;

import java.util.Map;

import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.impl.execute.job.JobActivatableSetImpl;
import net.officefloor.frame.internal.structure.AdministratorContainer;
import net.officefloor.frame.internal.structure.AdministratorMetaData;
import net.officefloor.frame.internal.structure.DutyMetaData;
import net.officefloor.frame.internal.structure.EscalationProcedure;
import net.officefloor.frame.internal.structure.ExtensionInterfaceMetaData;
import net.officefloor.frame.internal.structure.JobActivatableSet;
import net.officefloor.frame.internal.structure.TaskMetaData;
import net.officefloor.frame.spi.administration.Administrator;
import net.officefloor.frame.spi.administration.Duty;
import net.officefloor.frame.spi.administration.source.AdministratorSource;
import net.officefloor.frame.spi.team.Team;

/**
 * Implementation of the {@link AdministratorMetaData}.
 * 
 * @author Daniel
 */
public class AdministratorMetaDataImpl<I extends Object, A extends Enum<A>>
		implements AdministratorMetaData<I, A> {

	/**
	 * {@link AdministratorSource}.
	 */
	private final AdministratorSource<I, A> administratorSource;

	/**
	 * {@link ExtensionInterfaceMetaData}.
	 */
	private final ExtensionInterfaceMetaData<I>[] eiMetaData;

	/**
	 * {@link Team}.
	 */
	private final Team team;

	/**
	 * {@link EscalationProcedure}.
	 */
	private final EscalationProcedure escalationProcedure;

	/**
	 * <p>
	 * Registry of {@link DutyMetaData} by its {@link Duty} key.
	 * <p>
	 * This is treated as <code>final</code>.
	 */
	protected Map<A, DutyMetaData> dutyMetaData;

	/**
	 * Initiate with meta-data of the {@link Administrator} scope to the
	 * {@link Work}.
	 * 
	 * @param administratorSource
	 *            {@link AdministratorSource}.
	 * @param eiMetaData
	 *            {@link ExtensionInterfaceMetaData}.
	 * @param team
	 *            {@link Team}.
	 * @param escalationProcedure
	 *            {@link EscalationProcedure}.
	 */
	public AdministratorMetaDataImpl(
			AdministratorSource<I, A> administratorSource,
			ExtensionInterfaceMetaData<I>[] eiMetaData, Team team,
			EscalationProcedure escalationProcedure) {
		this.eiMetaData = eiMetaData;
		this.administratorSource = administratorSource;
		this.team = team;
		this.escalationProcedure = escalationProcedure;
	}

	/**
	 * Loads the remaining state.
	 * 
	 * @param dutyMetaData
	 *            {@link DutyMetaData} for each {@link Duty} of the
	 *            {@link Administrator}.
	 */
	public void loadRemainingState(Map<A, DutyMetaData> dutyMetaData) {
		this.dutyMetaData = dutyMetaData;
	}

	/*
	 * ================= AdministratorMetaData ===============================
	 */

	@Override
	public AdministratorContainer<I, A> createAdministratorContainer() {
		return new AdministratorContainerImpl<I, A, None>(this);
	}

	@Override
	public AdministratorSource<I, A> getAdministratorSource() {
		return this.administratorSource;
	}

	@Override
	public ExtensionInterfaceMetaData<I>[] getExtensionInterfaceMetaData() {
		return this.eiMetaData;
	}

	@Override
	public DutyMetaData getDutyMetaData(A key) {
		return this.dutyMetaData.get(key);
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
	public EscalationProcedure getEscalationProcedure() {
		return this.escalationProcedure;
	}

	@Override
	public TaskMetaData<?, ?, ?> getNextTaskInFlow() {
		// Never a next task for an administrator duty
		return null;
	}

}