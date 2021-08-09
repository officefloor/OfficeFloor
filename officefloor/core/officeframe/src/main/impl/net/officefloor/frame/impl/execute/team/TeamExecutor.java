/*-
 * #%L
 * OfficeFrame
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

package net.officefloor.frame.impl.execute.team;

import java.util.concurrent.Executor;

import net.officefloor.frame.api.executive.Executive;
import net.officefloor.frame.api.executive.ProcessIdentifier;
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
		ProcessIdentifier identifier = ThreadStateImpl.currentProcessIdentifier();
		if (identifier == null) {
			// Invoked outside management, so create new process to run
			identifier = this.executive.createProcessIdentifier(null);
		}

		// Execute the runnable
		try {
			final ProcessIdentifier processIdentifier = identifier;
			this.team.assignJob(new Job() {

				@Override
				public ProcessIdentifier getProcessIdentifier() {
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
