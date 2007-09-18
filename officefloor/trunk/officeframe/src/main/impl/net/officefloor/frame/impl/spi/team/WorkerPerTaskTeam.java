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
 * {@link net.officefloor.frame.spi.team.Team} that hires a specific new worker
 * dedicated each new {@link net.officefloor.frame.api.execute.Task}.
 * 
 * @author Daniel
 */
public class WorkerPerTaskTeam extends ThreadGroup implements Team {

	/**
	 * Indicates to continue working.
	 */
	protected volatile boolean continueWorking = true;

	/**
	 * Initiate team.
	 * 
	 * @param teamName
	 *            Name of this team.
	 */
	public WorkerPerTaskTeam(String teamName) {
		super(teamName);
	}

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
		// No initial workers as hired when required
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.spi.team.Team#assignTask(net.officefloor.frame.spi.team.TaskContainer)
	 */
	public void assignTask(TaskContainer task) {
		// Hire worker to execute the task
		new Thread(this, new DedicatedWorker(task)).start();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.spi.team.Team#stopWorking()
	 */
	public void stopWorking() {
		// Flag to workers to stop working
		this.continueWorking = false;
	}

	/**
	 * Worker dedicated to executing a
	 * {@link net.officefloor.frame.spi.team.TaskContainer}.
	 */
	protected class DedicatedWorker implements Runnable, ExecutionContext {

		/**
		 * {@link TaskContainer} to execute.
		 */
		protected final TaskContainer taskContainer;

		/**
		 * Current time for execution.
		 */
		protected long time;

		/**
		 * Initiate worker.
		 * 
		 * @param taskContainer
		 *            {@link TaskContainer} to execute.
		 */
		public DedicatedWorker(TaskContainer taskContainer) {
			// Store state
			this.taskContainer = taskContainer;
		}

		/*
		 * ========================================================================
		 * Runnable
		 * ========================================================================
		 */

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Runnable#run()
		 */
		public void run() {
			// Loop until task is complete or stop executing
			do {

				// Obtain current time
				this.time = System.currentTimeMillis();

				// Attempt to complete task
				if (this.taskContainer.doTask(this)) {
					// Task complete
					return;
				}

				// Allow other processing
				Thread.yield();

			} while (continueWorking);
		}

		/*
		 * ========================================================================
		 * ExecutionContext
		 * ========================================================================
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
			return continueWorking;
		}

	}

}