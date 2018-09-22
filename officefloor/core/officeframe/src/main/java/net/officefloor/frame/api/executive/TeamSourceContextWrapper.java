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
	 * {@link Team} name.
	 */
	private final String teamName;

	/**
	 * {@link Team} size.
	 */
	private final int teamSize;

	/**
	 * {@link ThreadFactory}.
	 */
	private final ThreadFactory threadFactory;

	/**
	 * Instantiate.
	 * 
	 * @param context           {@link ExecutiveContext}.
	 * @param teamSize          {@link Team} size.
	 * @param teamNameSuffix    Optional suffix for the {@link Team} name. May be
	 *                          <code>null</code> for no suffix.
	 * @param workerEnvironment {@link WorkerEnvironment}. May be <code>null</code>.
	 */
	public TeamSourceContextWrapper(ExecutiveContext context, int teamSize, String teamNameSuffix,
			WorkerEnvironment workerEnvironment) {
		super(context.isLoadingType(), context, context);
		this.teamSize = teamSize;

		// Specify the team name
		this.teamName = context.getTeamName() + (teamNameSuffix == null ? "" : "-" + teamNameSuffix);

		// Obtain the thread factory
		ThreadFactory threadFactory = context.createThreadFactory(this.teamName);
		if (workerEnvironment != null) {
			// Provide worker wrapper
			ThreadFactory delegate = threadFactory;
			threadFactory = (worker) -> delegate.newThread(workerEnvironment.createWorkerEnvironment(worker));
		}
		this.threadFactory = threadFactory;
	}

	/*
	 * =============== TeamSourceContext =================
	 */

	@Override
	public String getTeamName() {
		return this.teamName;
	}

	@Override
	public int getTeamSize() {
		return this.teamSize;
	}

	@Override
	public ThreadFactory getThreadFactory() {
		return this.threadFactory;
	}

}