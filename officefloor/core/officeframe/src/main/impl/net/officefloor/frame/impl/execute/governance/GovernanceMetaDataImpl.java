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
package net.officefloor.frame.impl.execute.governance;

import net.officefloor.frame.api.build.GovernanceFactory;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.impl.execute.job.JobNodeActivatableSetImpl;
import net.officefloor.frame.internal.structure.ActiveGovernanceControl;
import net.officefloor.frame.internal.structure.ActiveGovernanceManager;
import net.officefloor.frame.internal.structure.EscalationProcedure;
import net.officefloor.frame.internal.structure.FlowMetaData;
import net.officefloor.frame.internal.structure.GovernanceActivity;
import net.officefloor.frame.internal.structure.GovernanceContainer;
import net.officefloor.frame.internal.structure.GovernanceControl;
import net.officefloor.frame.internal.structure.GovernanceMetaData;
import net.officefloor.frame.internal.structure.JobNode;
import net.officefloor.frame.internal.structure.JobNodeActivatableSet;
import net.officefloor.frame.internal.structure.JobSequence;
import net.officefloor.frame.internal.structure.ManagedObjectContainer;
import net.officefloor.frame.internal.structure.ProcessState;
import net.officefloor.frame.internal.structure.TaskMetaData;
import net.officefloor.frame.internal.structure.TeamManagement;
import net.officefloor.frame.internal.structure.ThreadState;
import net.officefloor.frame.internal.structure.WorkContainer;
import net.officefloor.frame.internal.structure.WorkMetaData;
import net.officefloor.frame.spi.governance.Governance;
import net.officefloor.frame.spi.team.Job;
import net.officefloor.frame.spi.team.Team;

/**
 * {@link GovernanceMetaData} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class GovernanceMetaDataImpl<I, F extends Enum<F>> implements
		GovernanceMetaData<I, F> {

	/**
	 * {@link GovernanceWork} as {@link WorkMetaData}.
	 */
	private static final WorkMetaData<Work> governanceWorkMetaData = new GovernanceWork();

	/**
	 * Name of the {@link Governance}.
	 */
	private final String governanceName;

	/**
	 * {@link GovernanceFactory}.
	 */
	private final GovernanceFactory<? super I, F> governanceFactory;

	/**
	 * {@link TeamManagement} of {@link Team} responsible for the
	 * {@link GovernanceActivity} instances.
	 */
	private final TeamManagement responsibleTeam;

	/**
	 * {@link Team} to enable the worker ({@link Thread}) of the responsible
	 * {@link Team} to continue on to execute the next {@link Job}.
	 */
	private final Team continueTeam;

	/**
	 * {@link FlowMetaData} instances.
	 */
	private FlowMetaData<?>[] flowMetaData;

	/**
	 * {@link EscalationProcedure} for the {@link GovernanceActivity} failures.
	 */
	private EscalationProcedure escalationProcedure;

	/**
	 * Initiate.
	 * 
	 * @param governanceName
	 *            Name of the {@link Governance}.
	 * @param governanceFactory
	 *            {@link GovernanceFactory}.
	 * @param responsibleTeam
	 *            {@link TeamManagement} of {@link Team} responsible for the
	 *            {@link GovernanceActivity} instances.
	 * @param continueTeam
	 *            {@link Team} to enable the worker ({@link Thread}) of the
	 *            responsible {@link Team} to continue on to execute the next
	 *            {@link Job}.
	 */
	public GovernanceMetaDataImpl(String governanceName,
			GovernanceFactory<? super I, F> governanceFactory,
			TeamManagement responsibleTeam, Team continueTeam) {
		this.governanceName = governanceName;
		this.governanceFactory = governanceFactory;
		this.responsibleTeam = responsibleTeam;
		this.continueTeam = continueTeam;
	}

	/**
	 * Loads the remaining state.
	 * 
	 * @param flowMetaData
	 *            {@link FlowMetaData} instances.
	 * @param escalationProcedure
	 *            {@link EscalationProcedure}.
	 */
	public void loadRemainingState(FlowMetaData<?>[] flowMetaData,
			EscalationProcedure escalationProcedure) {
		this.flowMetaData = flowMetaData;
		this.escalationProcedure = escalationProcedure;
	}

	/*
	 * ======================= JobMetaData =============================
	 */

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
	public TaskMetaData<?, ?, ?> getNextTaskInFlow() {
		// Never a next task for governance activity
		return null;
	}

	@Override
	public EscalationProcedure getEscalationProcedure() {
		return this.escalationProcedure;
	}

	/*
	 * ================== GovernanceMetaData ==========================
	 */

	@Override
	public String getJobName() {
		// TODO indicate type of governance action being undertaken
		return Governance.class.getSimpleName() + "-" + this.governanceName;
	}

	@Override
	public String getGovernanceName() {
		return this.governanceName;
	}

	@Override
	public GovernanceFactory<? super I, F> getGovernanceFactory() {
		return this.governanceFactory;
	}

	@Override
	public GovernanceContainer<I, F> createGovernanceContainer(
			ThreadState threadState, int processRegisteredIndex) {
		return new GovernanceContainerImpl<I, F>(this, threadState,
				processRegisteredIndex);
	}

	@Override
	public ActiveGovernanceManager<I, F> createActiveGovernance(
			GovernanceContainer<I, F> governanceContainer,
			GovernanceControl<I, F> governanceControl, I extensionInterface,
			ManagedObjectContainer managedobjectContainer,
			WorkContainer<?> workContainer,
			int managedObjectContainerRegisteredIndex) {
		return new ActiveGovernanceImpl<I, F>(governanceContainer, this,
				governanceControl, extensionInterface, managedobjectContainer,
				workContainer, managedObjectContainerRegisteredIndex);
	}

	@Override
	public GovernanceActivity<I, F> createActivateActivity(
			GovernanceControl<I, F> governanceControl) {
		return new ActivateGovernanceActivity<I, F>(this, governanceControl);
	}

	@Override
	public GovernanceActivity<I, F> createGovernActivity(
			ActiveGovernanceControl<F> activeGovernanceControl) {
		return new GovernGovernanceActivity<I, F>(this, activeGovernanceControl);
	}

	@Override
	public GovernanceActivity<I, F> createEnforceActivity(
			GovernanceControl<I, F> governanceControl) {
		return new EnforceGovernanceActivity<I, F>(this, governanceControl);
	}

	@Override
	public GovernanceActivity<I, F> createDisregardActivity(
			GovernanceControl<I, F> governanceControl) {
		return new DisregardGovernanceActivity<I, F>(this, governanceControl);
	}

	@Override
	public FlowMetaData<?> getFlow(int flowIndex) {
		return this.flowMetaData[flowIndex];
	}

	@Override
	public JobNode createGovernanceJob(JobSequence flow,
			GovernanceActivity<I, F> governanceActivity,
			JobNode parallelJobNodeOwner) {

		// Obtain the process state
		ProcessState processState = flow.getThreadState().getProcessState();

		// Create the work container
		WorkContainer<Work> workContainer = governanceWorkMetaData
				.createWorkContainer(processState);

		// Create and return the job
		return new GovernanceJob<I, F, Work>(flow, workContainer, this,
				parallelJobNodeOwner, governanceActivity);
	}

}