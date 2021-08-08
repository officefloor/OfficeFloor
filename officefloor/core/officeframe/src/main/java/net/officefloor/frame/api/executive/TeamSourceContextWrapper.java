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

package net.officefloor.frame.api.executive;

import java.util.concurrent.ThreadFactory;
import java.util.function.Function;

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
	 * {@link ExecutiveContext}.
	 */
	private final ExecutiveContext executiveContext;

	/**
	 * Calculates the {@link Team} size from the configured {@link Team} size.
	 */
	private final Function<Integer, Integer> teamSizeCalculator;

	/**
	 * {@link Team} name.
	 */
	private final String teamName;

	/**
	 * {@link ThreadFactory}.
	 */
	private final ThreadFactory threadFactory;

	/**
	 * Instantiate.
	 * 
	 * @param context            {@link ExecutiveContext}.
	 * @param teamSizeCalculator Calculates the {@link Team} size from the
	 *                           configured {@link Team} size.
	 * @param teamNameSuffix     Optional suffix for the {@link Team} name. May be
	 *                           <code>null</code> for no suffix.
	 * @param workerEnvironment  {@link WorkerEnvironment}. May be
	 *                           <code>null</code>.
	 */
	public TeamSourceContextWrapper(ExecutiveContext context, Function<Integer, Integer> teamSizeCalculator,
			String teamNameSuffix, WorkerEnvironment workerEnvironment) {
		super(getTeamName(context, teamNameSuffix), context.isLoadingType(), new String[0], context, context);
		this.executiveContext = context;
		this.teamSizeCalculator = teamSizeCalculator;

		// Specify the team name
		this.teamName = getTeamName(context, teamNameSuffix);

		// Obtain the thread factory
		ThreadFactory threadFactory = context.createThreadFactory(this.teamName);
		if (workerEnvironment != null) {
			// Provide worker wrapper
			ThreadFactory delegate = threadFactory;
			threadFactory = (worker) -> delegate.newThread(workerEnvironment.createWorkerEnvironment(worker));
		}
		this.threadFactory = threadFactory;
	}

	/**
	 * Determines the {@link Team} name.
	 * 
	 * @param context        {@link ExecutiveContext}.
	 * @param teamNameSuffix {@link Team} name suffix.
	 * @return {@link Team} name.
	 */
	private static String getTeamName(ExecutiveContext context, String teamNameSuffix) {
		return context.getTeamName() + (teamNameSuffix == null ? "" : "-" + teamNameSuffix);
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
		return this.teamSizeCalculator.apply(this.executiveContext.getTeamSize());
	}

	@Override
	public int getTeamSize(int defaultSize) {
		return this.teamSizeCalculator.apply(this.executiveContext.getTeamSize(defaultSize));
	}

	@Override
	public ThreadFactory getThreadFactory() {
		return this.threadFactory;
	}

}
