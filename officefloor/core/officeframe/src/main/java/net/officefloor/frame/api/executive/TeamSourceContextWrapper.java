/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
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
package net.officefloor.frame.api.executive;

import java.util.concurrent.ThreadFactory;

import net.officefloor.frame.api.team.Team;
import net.officefloor.frame.api.team.source.TeamSourceContext;
import net.officefloor.frame.impl.construct.source.SourceContextImpl;

/**
 * Wrapper of {@link TeamSourceContext} to enable customising the {@link Team}.
 * 
 * @author Daniel Sagenschneider
 */
public class TeamSourceContextWrapper extends SourceContextImpl implements TeamSourceContext {

	/**
	 * Provides a wrapper around the {@link Runnable} from the {@link Executive} for
	 * a {@link Thread}.
	 * 
	 * @author Daniel Sagenschneider
	 */
	@FunctionalInterface
	public interface WorkerEnvironment {

		/**
		 * Creates a {@link Runnable} to specify the environment around the worker
		 * {@link Runnable} for a {@link Thread}.
		 * 
		 * @param worker {@link Runnable} containing the worker logic.
		 * @return {@link Runnable} wrapping the {@link Runnable} providing the
		 *         environment for the worker.
		 */
		Runnable createWorkerEnvironment(Runnable worker);
	}

	/**
	 * {@link TeamSourceContext}.
	 */
	protected final TeamSourceContext context;

	/**
	 * Optional suffix for the {@link Team} name.
	 */
	private final String teamNameSuffix;

	/**
	 * Optional {@link WorkerEnvironment}.
	 */
	private final WorkerEnvironment workerEnvironment;

	/**
	 * Instantiate.
	 * 
	 * @param context           {@link TeamSourceContext}.
	 * @param teamNameSuffix    Optional suffix for the {@link Team} name. May be
	 *                          <code>null</code> for no suffix.
	 * @param workerEnvironment {@link WorkerEnvironment}. May be <code>null</code>.
	 */
	public TeamSourceContextWrapper(TeamSourceContext context, String teamNameSuffix,
			WorkerEnvironment workerEnvironment) {
		super(context.isLoadingType(), context, context);
		this.context = context;
		this.teamNameSuffix = teamNameSuffix;
		this.workerEnvironment = workerEnvironment;
	}

	/*
	 * =============== TeamSourceContext =================
	 */

	@Override
	public String getTeamName() {
		return this.context.getTeamName() + (this.teamNameSuffix == null ? "" : "-" + this.teamNameSuffix);
	}

	@Override
	public ThreadFactory getThreadFactory(int threadPriority) {

		// Determine if worker environment
		if (this.workerEnvironment == null) {
			return this.context.getThreadFactory(threadPriority);
		}

		// Provide worker wrapper around threads
		return (worker) -> this.context.getThreadFactory(threadPriority)
				.newThread(this.workerEnvironment.createWorkerEnvironment(worker));
	}

}