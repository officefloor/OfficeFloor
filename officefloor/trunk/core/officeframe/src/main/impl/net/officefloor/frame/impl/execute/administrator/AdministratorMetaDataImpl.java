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

import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.impl.execute.duty.DutyJob;
import net.officefloor.frame.impl.execute.job.JobNodeActivatableSetImpl;
import net.officefloor.frame.internal.structure.AdministratorContainer;
import net.officefloor.frame.internal.structure.AdministratorMetaData;
import net.officefloor.frame.internal.structure.DutyMetaData;
import net.officefloor.frame.internal.structure.EscalationProcedure;
import net.officefloor.frame.internal.structure.ExtensionInterfaceMetaData;
import net.officefloor.frame.internal.structure.GovernanceActivity;
import net.officefloor.frame.internal.structure.JobNode;
import net.officefloor.frame.internal.structure.JobNodeActivatableSet;
import net.officefloor.frame.internal.structure.JobSequence;
import net.officefloor.frame.internal.structure.TaskDutyAssociation;
import net.officefloor.frame.internal.structure.TaskMetaData;
import net.officefloor.frame.internal.structure.TeamManagement;
import net.officefloor.frame.internal.structure.WorkContainer;
import net.officefloor.frame.spi.administration.Administrator;
import net.officefloor.frame.spi.administration.DutyKey;
import net.officefloor.frame.spi.administration.source.AdministratorSource;
import net.officefloor.frame.spi.team.Job;
import net.officefloor.frame.spi.team.Team;

/**
 * Implementation of the {@link AdministratorMetaData}.
 * 
 * @author Daniel Sagenschneider
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
	 * {@link TeamManagement} of {@link Team} responsible for the
	 * {@link GovernanceActivity}.
	 */
	private final TeamManagement responsibleTeam;

	/**
	 * {@link Team} to enable the worker ({@link Thread}) of the responsible
	 * {@link Team} to continue on to execute the next {@link Job}.
	 */
	private final Team continueTeam;

	/**
	 * {@link EscalationProcedure}.
	 */
	private final EscalationProcedure escalationProcedure;

	/**
	 * <p>
	 * Listing of {@link DutyMetaData} in order of {@link DutyKey} indexes.
	 * <p>
	 * This is treated as <code>final</code>.
	 */
	protected DutyMetaData[] dutyMetaData;

	/**
	 * Initiate with meta-data of the {@link Administrator} scope to the
	 * {@link Work}.
	 * 
	 * @param administratorSource
	 *            {@link AdministratorSource}.
	 * @param eiMetaData
	 *            {@link ExtensionInterfaceMetaData}.
	 * @param responsibleTeam
	 *            {@link TeamManagement} of {@link Team} responsible for the
	 *            {@link GovernanceActivity}.
	 * @param continueTeam
	 *            {@link Team} to enable the worker ({@link Thread}) of the
	 *            responsible {@link Team} to continue on to execute the next
	 *            {@link Job}.
	 * @param escalationProcedure
	 *            {@link EscalationProcedure}.
	 */
	public AdministratorMetaDataImpl(
			AdministratorSource<I, A> administratorSource,
			ExtensionInterfaceMetaData<I>[] eiMetaData,
			TeamManagement responsibleTeam, Team continueTeam,
			EscalationProcedure escalationProcedure) {
		this.eiMetaData = eiMetaData;
		this.administratorSource = administratorSource;
		this.responsibleTeam = responsibleTeam;
		this.continueTeam = continueTeam;
		this.escalationProcedure = escalationProcedure;
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
	 * ================= AdministratorMetaData ===============================
	 */

	@Override
	public String getJobName() {
		// TODO provide information of duty
		return Administrator.class.getSimpleName() + "-"
				+ this.administratorSource.getClass().getName();
	}

	@Override
	public AdministratorContainer<I, A> createAdministratorContainer() {
		return new AdministratorContainerImpl<I, A, None, None>(this);
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
	public DutyMetaData getDutyMetaData(DutyKey<A> dutyKey) {
		return this.dutyMetaData[dutyKey.getIndex()];
	}

	@Override
	public JobNodeActivatableSet createJobActivableSet() {
		return new JobNodeActivatableSetImpl();
	}

	@Override
	public TeamManagement getResponsibleTeam() {
		return this.responsibleTeam;
	}

	@Override
	public Team getContinueTeam() {
		return this.continueTeam;
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

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public JobNode createDutyNode(
			TaskMetaData<?, ?, ?> administeringTaskMetaData,
			WorkContainer<?> administeringWorkContainer, JobSequence flow,
			TaskDutyAssociation<?> taskDutyAssociation,
			JobNode parallelJobNodeOwner) {
		return new DutyJob(flow, administeringWorkContainer, this,
				taskDutyAssociation, parallelJobNodeOwner,
				administeringTaskMetaData);
	}

}