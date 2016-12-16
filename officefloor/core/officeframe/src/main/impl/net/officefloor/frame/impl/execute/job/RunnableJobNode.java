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
import net.officefloor.frame.internal.structure.TeamManagement;
import net.officefloor.frame.internal.structure.ThreadState;
import net.officefloor.frame.spi.team.JobContext;

/**
 * {@link JobNode} to execute a {@link Runnable}.
 *
 * @author Daniel Sagenschneider
 */
public class RunnableJobNode implements JobNode {

	/**
	 * {@link Runnable}.
	 */
	private final JobNodeRunnable runnable;

	/**
	 * Responsible {@link TeamManagement}.
	 */
	private final TeamManagement responsibleTeam;

	/**
	 * {@link ThreadState}.
	 */
	private ThreadState threadState;

	/**
	 * Instantiate.
	 * 
	 * @param runnable
	 *            {@link JobNodeRunnable}.
	 * @param responsibleTeam
	 *            Responsible {@link TeamManagement}.
	 * @param threadState
	 *            {@link ThreadState}.
	 */
	public RunnableJobNode(JobNodeRunnable runnable, TeamManagement responsibleTeam, ThreadState threadState) {
		this.runnable = runnable;
		this.responsibleTeam = responsibleTeam;
		this.threadState = threadState;
	}

	/*
	 * ==================== JobNode ===========================
	 */

	@Override
	public JobNode doJob(JobContext context) {
		return this.runnable.run();
	}

	@Override
	public TeamManagement getResponsibleTeam() {
		return this.responsibleTeam;
	}

	@Override
	public ThreadState getThreadState() {
		return this.threadState;
	}

}