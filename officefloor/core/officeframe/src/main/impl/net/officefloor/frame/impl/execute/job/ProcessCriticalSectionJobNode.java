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
package net.officefloor.frame.impl.execute.job;

import net.officefloor.frame.internal.structure.JobNode;
import net.officefloor.frame.internal.structure.JobNodeRunnable;
import net.officefloor.frame.internal.structure.ProcessState;
import net.officefloor.frame.internal.structure.TeamManagement;
import net.officefloor.frame.internal.structure.ThreadState;
import net.officefloor.frame.spi.team.JobContext;

/**
 * {@link ProcessState} critical section {@link JobNode}.
 *
 * @author Daniel Sagenschneider
 */
public class ProcessCriticalSectionJobNode implements JobNode {

	/**
	 * {@link JobNodeRunnable}.
	 */
	private final JobNodeRunnable runnable;

	/**
	 * Responsible {@link TeamManagement}.
	 */
	private final TeamManagement responsibleTeam;

	/**
	 * {@link ProcessState}.
	 */
	private final ProcessState processState;

	/**
	 * {@link JobNode} to continue once the {@link ProcessState} critical
	 * section is complete.
	 */
	private final JobNode continueJobNode;

	/**
	 * Instantiate.
	 * 
	 * @param runnable
	 *            {@link JobNodeRunnable}.
	 * @param responsibleTeam
	 *            Responsible {@link TeamManagement}.
	 * @param processState
	 *            {@link ProcessState}.
	 * @param continueJobNode
	 *            {@link JobNode} to continue once the {@link ProcessState}
	 *            critical section is complete.
	 */
	public ProcessCriticalSectionJobNode(JobNodeRunnable runnable, TeamManagement responsibleTeam,
			ProcessState processState, JobNode continueJobNode) {
		this.runnable = runnable;
		this.responsibleTeam = responsibleTeam;
		this.processState = processState;
		this.continueJobNode = continueJobNode;
	}

	/*
	 * ========================= JobNode =================================
	 */

	@Override
	public JobNode doJob(JobContext context) {
		return ContinueJobNode.continueWith(this.runnable.run(), this.continueJobNode);
	}

	@Override
	public TeamManagement getResponsibleTeam() {
		return this.responsibleTeam;
	}

	@Override
	public ThreadState getThreadState() {
		return this.processState.getMainThreadState();
	}

}