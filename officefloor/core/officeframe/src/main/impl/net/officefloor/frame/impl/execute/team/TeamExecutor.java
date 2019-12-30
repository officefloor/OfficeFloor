/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2019 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package net.officefloor.frame.impl.execute.team;

import java.util.concurrent.Executor;

import net.officefloor.frame.api.executive.Executive;
import net.officefloor.frame.api.team.Job;
import net.officefloor.frame.api.team.Team;
import net.officefloor.frame.impl.execute.thread.ThreadStateImpl;

/**
 * {@link Executor} that is backed by a {@link Team}.
 * 
 * @author Daniel Sagenschneider
 */
public class TeamExecutor implements Executor {

	/**
	 * {@link Team} to execute the {@link Runnable} instances.
	 */
	private final Team team;

	/**
	 * {@link Executive}.
	 */
	private final Executive executive;

	/**
	 * Instantiate.
	 * 
	 * @param team      {@link Team}.
	 * @param executive {@link Executive}.
	 */
	public TeamExecutor(Team team, Executive executive) {
		this.team = team;
		this.executive = executive;
	}

	/*
	 * ================= Executor ========================
	 */

	@Override
	public void execute(Runnable command) {

		// Attempt to determine current thread state (to re-use process identifier)
		Object identifier = ThreadStateImpl.currentProcessIdentifier();
		if (identifier == null) {
			// Invoked outside management, so create new process to run
			identifier = this.executive.createProcessIdentifier();
		}

		// Execute the runnable
		try {
			final Object processIdentifier = identifier;
			this.team.assignJob(new Job() {

				@Override
				public Object getProcessIdentifier() {
					return processIdentifier;
				}

				@Override
				public void run() {
					command.run();
				}

				@Override
				public void cancel(Throwable cause) {
					// Propagate failure
					throw new TeamExecutorRuntimeException(cause);
				}
			});
		} catch (Exception ex) {
			// Propagate failure
			throw new TeamExecutorRuntimeException(ex);
		}
	}

}