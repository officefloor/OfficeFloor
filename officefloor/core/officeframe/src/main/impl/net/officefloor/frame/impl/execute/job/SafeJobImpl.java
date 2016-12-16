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
import net.officefloor.frame.spi.team.TeamIdentifier;

/**
 * {@link Thread} safe {@link Job} implementation.
 *
 * @author Daniel Sagenschneider
 */
public class SafeJobImpl extends UnsafeJobImpl {

	/**
	 * Instantiate.
	 * 
	 * @param initialJobNode
	 *            Initial {@link JobNode}.
	 */
	public SafeJobImpl(JobNode initialJobNode) {
		super(initialJobNode);
	}

	/*
	 * ====================== UnsafeJobImpl ======================
	 */

	@Override
	protected JobNode doThreadStateJobNodeLoop(JobNode head, JobContext context) {
		// Must now synchronise on thread state for thread safety
		synchronized (head.getThreadState()) {
			return super.doThreadStateJobNodeLoop(head, context);
		}
	}

	@Override
	protected void assignJob(JobNode jobNode, TeamIdentifier currentTeam) {
		// No need to synchronise on assigning jobs, as loop is thread safe
		jobNode.getResponsibleTeam().getTeam().assignJob(new SafeJobImpl(jobNode), currentTeam);
	}

}
