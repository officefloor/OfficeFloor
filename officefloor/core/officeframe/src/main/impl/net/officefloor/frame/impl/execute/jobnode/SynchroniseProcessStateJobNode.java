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
import net.officefloor.frame.internal.structure.ProcessState;
import net.officefloor.frame.internal.structure.TeamManagement;
import net.officefloor.frame.internal.structure.ThreadState;
import net.officefloor.frame.spi.team.JobContext;

/**
 * {@link JobNode} to synchronise the {@link ProcessState} with the current
 * {@link ThreadState}.
 *
 * @author Daniel Sagenschneider
 */
public class SynchroniseProcessStateJobNode implements JobNode {

	/**
	 * {@link JobNode} to continue after synchronising the {@link ProcessState}.
	 */
	private final JobNode continueJobNode;

	/**
	 * Instantiate.
	 * 
	 * @param continueJobNode
	 *            {@link JobNode} to continue after synchronising the
	 *            {@link ProcessState}.
	 */
	public SynchroniseProcessStateJobNode(JobNode continueJobNode) {
		this.continueJobNode = continueJobNode;
	}

	/*
	 * ======================== JobNode =================================
	 */

	@Override
	public JobNode doJob(JobContext context) {

		// Synchronise the process state (always undertaken on main thread)
		synchronized (this.continueJobNode.getThreadState().getProcessState().getMainThreadState()) {
		}

		// Continue executing with synchronised process state
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