/*
 *  Office Floor, Application Server
 *  Copyright (C) 2006 Daniel Sagenschneider
 *
 *  This program is free software; you can redistribute it and/or modify it under the terms 
 *  of the GNU General Public License as published by the Free Software Foundation; either 
 *  version 2 of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with this program; 
 *  if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, 
 *  MA 02111-1307 USA
 */
package net.officefloor.frame.impl.spi.team;

import net.officefloor.frame.spi.team.ExecutionContext;
import net.officefloor.frame.spi.team.TaskContainer;
import net.officefloor.frame.spi.team.Team;

/**
 * <p>
 * Passive {@link net.officefloor.frame.spi.team.Team} which uses the invoking
 * {@link java.lang.Thread} to execute the
 * {@link net.officefloor.frame.api.execute.Task}.
 * </p>
 * <p>
 * Note that using this team will block the invoking {@link java.lang.Thread}
 * until the {@link net.officefloor.frame.api.execute.Task} is complete.
 * </p>
 * 
 * @author Daniel
 */
public class PassiveTeam implements Team, ExecutionContext {

	/**
	 * Current time for execution.
	 */
	protected long time;

	/**
	 * Indicates if should continue working.
	 */
	protected volatile boolean continueWorking = true;

	/*
	 * ========================================================================
	 * Team
	 * ========================================================================
	 */

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.spi.team.Team#startWorking()
	 */
	public void startWorking() {
		// No workers as passive
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.spi.team.Team#assignTask(net.officefloor.frame.spi.team.TaskContainer)
	 */
	public void assignTask(TaskContainer task) {
		// Loop executing the Task until it is complete or stop working
		do {

			// Specify the time
			this.time = System.currentTimeMillis();

			// Attempt to complete the Task
			if (task.doTask(this)) {
				// Task complete
				return;
			}

			// Allow other processing to take place
			Thread.yield();

		} while (this.continueWorking);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.spi.team.Team#stopWorking()
	 */
	public void stopWorking() {
		// Flag to stop working
		this.continueWorking = false;
	}

	/*
	 * ====================================================================
	 * ExecutionContext
	 * ====================================================================
	 */

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.spi.team.ExecutionContext#getTime()
	 */
	public long getTime() {
		return this.time;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.spi.team.ExecutionContext#continueExecution()
	 */
	public boolean continueExecution() {
		return this.continueWorking;
	}

}
