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
package net.officefloor.frame.impl.execute.jobnode;

import net.officefloor.frame.internal.structure.JobNode;
import net.officefloor.frame.internal.structure.TeamManagement;
import net.officefloor.frame.internal.structure.ThreadState;
import net.officefloor.frame.spi.team.JobContext;

/**
 * {@link JobNode} to fail the {@link ThreadState}.
 *
 * @author Daniel Sagenschneider
 */
public class FailThreadStateJobNode implements JobNode {

	/**
	 * Failure for the {@link ThreadState}.
	 */
	private final Throwable failure;

	/**
	 * Continue {@link JobNode}.
	 */
	private final JobNode continueJobNode;

	/**
	 * Instantiate.
	 * 
	 * @param failure
	 *            Failure for the {@link ThreadState}.
	 * @param continueJobNode
	 *            Continue {@link JobNode}.
	 */
	public FailThreadStateJobNode(Throwable failure, JobNode continueJobNode) {
		this.failure = failure;
		this.continueJobNode = continueJobNode;
	}

	/*
	 * ======================== JobNode ==========================
	 */

	@Override
	public JobNode doJob(JobContext context) {

		// Flag thread state as failed
		this.continueJobNode.getThreadState().setFailure(this.failure);

		// Continue on to fail the thread
		return this.continueJobNode;
	}

	@Override
	public TeamManagement getResponsibleTeam() {
		return this.continueJobNode.getResponsibleTeam();
	}

	@Override
	public ThreadState getThreadState() {
		return this.continueJobNode.getThreadState();
	}

}