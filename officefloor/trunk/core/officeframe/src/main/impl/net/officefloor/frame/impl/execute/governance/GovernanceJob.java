/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2011 Daniel Sagenschneider
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

/**
 * {@link Governance} {@link Job}.
 * 
 * @author Daniel Sagenschneider
 */
public class GovernanceJob<I, F extends Enum<F>, W extends Work> extends
		AbstractJobContainer<W, GovernanceMetaData<I, F>> implements
		GovernanceContext<F> {

	/**
	 * No {@link ManagedObject} instances required for {@link Governance}.
	 */
	private static final ManagedObjectIndex[] REQUIRED_MANAGED_OBJECTS = new ManagedObjectIndex[0];

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
	 * @param parallelOwner
	 */
	public GovernanceJob(JobSequence flow, WorkContainer<W> workContainer,
			GovernanceMetaData<I, F> nodeMetaData, JobNode parallelOwner,
			GovernanceActivity<I, F> governanceActivity) {
		super(flow, workContainer, nodeMetaData, parallelOwner,
				REQUIRED_MANAGED_OBJECTS);
		this.governanceActivity = governanceActivity;
	}

	/*
	 * ========================= Job =================================
	 */

	@Override
	protected Object executeJob(JobExecuteContext context,
			JobContext jobContext, JobNodeActivateSet activateSet)
			throws Throwable {

		// Execute the governance activity
		this.governanceActivity.doActivity(this, jobContext, this, activateSet,
				this);

		// No further tasks expected after activity
		return null;
	}

	/*
	 * ======================== GovernanceContext ======================
	 */

	@Override
	public void doFlow(F key, Object parameter) {
		// TODO implement GovernanceContext<F>.doFlow
		throw new UnsupportedOperationException(
				"TODO implement GovernanceContext<F>.doFlow");
	}

	@Override
	public void doFlow(int flowIndex, Object parameter) {
		// TODO implement GovernanceContext<F>.doFlow
		throw new UnsupportedOperationException(
				"TODO implement GovernanceContext<F>.doFlow");
	}

}