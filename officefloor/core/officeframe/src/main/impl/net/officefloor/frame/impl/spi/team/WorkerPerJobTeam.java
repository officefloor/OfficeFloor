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
package net.officefloor.frame.impl.spi.team;

import java.util.concurrent.atomic.AtomicLong;

import net.officefloor.frame.api.team.Job;
import net.officefloor.frame.api.team.Team;

/**
 * {@link Team} that uses a specific new worker ({@link Thread}) dedicated to
 * each new {@link Job}.
 * 
 * @author Daniel Sagenschneider
 */
public class WorkerPerJobTeam extends ThreadGroup implements Team {

	/**
	 * Priority for the worker {@link Thread} instances.
	 */
	private final int threadPriority;

	/**
	 * Count of the {@link Thread} instances created to obtain next index.
	 */
	private AtomicLong threadIndex = new AtomicLong(0);

	/**
	 * Initiate {@link Team}.
	 * 
	 * @param teamName
	 *            Name of this team.
	 * @param threadPriority
	 *            Priority for the worker {@link Thread} instances.
	 */
	public WorkerPerJobTeam(String teamName, int threadPriority) {
		super(teamName);
		this.threadPriority = threadPriority;
	}

	/**
	 * Initiate {@link Team} with normal priority.
	 * 
	 * @param teamName
	 *            Name of this team.
	 */
	public WorkerPerJobTeam(String teamName) {
		this(teamName, Thread.NORM_PRIORITY);
	}

	/*
	 * ======================== Team ==========================================
	 */

	@Override
	public void startWorking() {
		// No initial workers as hired when required
	}

	@Override
	public void assignJob(Job job) {

		// Create name for the worker
		long threadIndex = this.threadIndex.getAndIncrement();
		String threadName = this.getClass().getSimpleName() + "_" + this.getName() + "_" + String.valueOf(threadIndex);

		// Hire worker to execute the job
		Thread thread = new Thread(this, job, threadName);
		if (thread.getPriority() != this.threadPriority) {
			thread.setPriority(this.threadPriority);
		}
		if (!(thread.isDaemon())) {
			thread.setDaemon(true);
		}
		thread.start();
	}

	@Override
	public void stopWorking() {
	}

}