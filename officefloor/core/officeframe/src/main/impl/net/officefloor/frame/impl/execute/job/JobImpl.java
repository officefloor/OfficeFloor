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
import net.officefloor.frame.spi.team.Job;
import net.officefloor.frame.spi.team.JobContext;
import net.officefloor.frame.spi.team.Team;

/**
 * {@link Job} implementation.
 *
 * @author Daniel Sagenschneider
 */
public class JobImpl implements Job {

	/**
	 * Initial {@link JobNode}.
	 */
	private final JobNode initialJobNode;

	/**
	 * Next {@link Job} that is managed by the {@link Team}.
	 */
	private Job nextJob = null;

	/**
	 * Instantiate.
	 * 
	 * @param initialJobNode
	 *            Initial {@link JobNode}.
	 */
	public JobImpl(JobNode initialJobNode) {
		this.initialJobNode = initialJobNode;
	}

	/*
	 * ========================= Job ========================================
	 */

	@Override
	public void doJob(JobContext context) {
		this.initialJobNode.getThreadState().doJobNodeLoop(this.initialJobNode, context);
	}

	@Override
	public Object getProcessIdentifier() {
		return this.initialJobNode.getThreadState().getProcessState().getProcessIdentifier();
	}

	@Override
	public void setNextJob(Job job) {
		this.nextJob = job;
	}

	@Override
	public Job getNextJob() {
		return this.nextJob;
	}

}