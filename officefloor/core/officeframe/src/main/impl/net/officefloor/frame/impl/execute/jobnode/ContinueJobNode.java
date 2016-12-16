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
 * {@link JobNode} that enables a {@link JobNode} to continue once all
 * {@link JobNode} instances of the a {@link JobNode} is complete.
 *
 * @author Daniel Sagenschneider
 */
public class ContinueJobNode implements JobNode {

	/**
	 * Convenience method to enable continue.
	 * 
	 * @param delegateJobNode
	 *            Delegate {@link JobNode}.
	 * @param continueJobNode
	 *            Continue {@link JobNode}. May be <code>null</code>.
	 * @return Next {@link JobNode}.
	 */
	public static JobNode continueWith(JobNode delegateJobNode, JobNode continueJobNode) {
		return (continueJobNode == null) ? delegateJobNode : new ContinueJobNode(delegateJobNode, continueJobNode);
	}

	/**
	 * Delegate {@link JobNode}.
	 */
	private final JobNode delegate;

	/**
	 * Continue {@link JobNode}.
	 */
	private final JobNode continueJobNode;

	/**
	 * Instantiate.
	 * 
	 * @param delegate
	 *            Delegate {@link JobNode} to complete it and all produced
	 *            {@link JobNode} instances before continuing.
	 * @param continueJobNode
	 *            Continue {@link JobNode}.
	 */
	public ContinueJobNode(JobNode delegate, JobNode continueJobNode) {
		this.delegate = delegate;
		this.continueJobNode = continueJobNode;
	}

	/*
	 * =================== JobNode ==============================
	 */

	@Override
	public JobNode doJob(JobContext context) {
		return continueWith(this.delegate.doJob(context), this.continueJobNode);
	}

	@Override
	public TeamManagement getResponsibleTeam() {
		return this.delegate.getResponsibleTeam();
	}

	@Override
	public ThreadState getThreadState() {
		return this.getThreadState();
	}

}
