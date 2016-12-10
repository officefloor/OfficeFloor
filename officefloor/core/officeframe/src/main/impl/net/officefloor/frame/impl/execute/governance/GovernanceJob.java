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

import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.impl.execute.job.AbstractJobContainer;
import net.officefloor.frame.impl.execute.job.JobExecuteContext;
import net.officefloor.frame.internal.structure.FlowMetaData;
import net.officefloor.frame.internal.structure.GovernanceActivity;
import net.officefloor.frame.internal.structure.GovernanceMetaData;
import net.officefloor.frame.internal.structure.JobNode;
import net.officefloor.frame.internal.structure.JobNodeActivateSet;
import net.officefloor.frame.internal.structure.JobSequence;
import net.officefloor.frame.internal.structure.ManagedObjectIndex;
import net.officefloor.frame.internal.structure.WorkContainer;
import net.officefloor.frame.spi.governance.Governance;
import net.officefloor.frame.spi.governance.GovernanceContext;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.team.Job;
import net.officefloor.frame.spi.team.JobContext;
import net.officefloor.frame.spi.team.TeamIdentifier;

/**
 * {@link Governance} {@link Job}.
 * 
 * @author Daniel Sagenschneider
 */
public class GovernanceJob<I, F extends Enum<F>, W extends Work> extends
		AbstractJobContainer<W, GovernanceMetaData<I, F>> {

	/**
	 * No {@link ManagedObject} instances required for {@link Governance}.
	 */
	private static final ManagedObjectIndex[] REQUIRED_MANAGED_OBJECTS = new ManagedObjectIndex[0];

	/**
	 * {@link GovernanceContext} to disallow downcast to {@link GovernanceJob}.
	 */
	private final GovernanceContext<F> governanceContext = new GovernanceContextToken();

	/**
	 * {@link GovernanceActivity}.
	 */
	private final GovernanceActivity<I, F> governanceActivity;

	/**
	 * Initiate.
	 * 
	 * @param flow
	 *            {@link JobSequence}.
	 * @param workContainer
	 *            {@link WorkContainer}.
	 * @param nodeMetaData
	 *            {@link GovernanceMetaData}.
	 * @param parallelOwner
	 *            Parallel owner {@link JobNode}.
	 * @param governanceActivity
	 *            {@link GovernanceActivity}.
	 */
	public GovernanceJob(JobSequence flow, WorkContainer<W> workContainer,
			GovernanceMetaData<I, F> nodeMetaData, JobNode parallelOwner,
			GovernanceActivity<I, F> governanceActivity) {
		super(flow, workContainer, nodeMetaData, parallelOwner,
				REQUIRED_MANAGED_OBJECTS, null, null);
		this.governanceActivity = governanceActivity;
	}

	/*
	 * ========================= Job =================================
	 */

	@Override
	protected void loadJobName(StringBuilder message) {
		message.append("Governance ");
		message.append(this.governanceActivity.getGovernanceMetaData()
				.getGovernanceName());
	}

	@Override
	protected Object executeJob(JobExecuteContext context,
			JobContext jobContext, JobNodeActivateSet activateSet)
			throws Throwable {

		// Obtain the current team
		TeamIdentifier currentTeam = jobContext.getCurrentTeam();

		// Execute the governance activity
		boolean isComplete = this.governanceActivity.doActivity(
				this.governanceContext, jobContext, this, activateSet,
				currentTeam, this);

		// Flag whether activity is complete
		this.setJobComplete(isComplete);

		// No further tasks expected after activity
		return null;
	}

	/**
	 * Provide token as {@link GovernanceContext} so can not downcast to obtain
	 * additional functionality of {@link GovernanceJob}.
	 */
	private class GovernanceContextToken implements GovernanceContext<F> {

		/*
		 * ======================== GovernanceContext ======================
		 */

		@Override
		public void doFlow(F key, Object parameter) {
			this.doFlow(key.ordinal(), parameter);
		}

		@Override
		public void doFlow(int flowIndex, Object parameter) {

			// Obtain the flow meta-data
			FlowMetaData<?> flowMetaData = GovernanceJob.this.nodeMetaData
					.getFlow(flowIndex);

			// Do the flow
			GovernanceJob.this.doFlow(flowMetaData, parameter);
		}
	}

}