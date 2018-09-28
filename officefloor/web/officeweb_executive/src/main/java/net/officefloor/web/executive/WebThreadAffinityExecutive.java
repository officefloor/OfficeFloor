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
package net.officefloor.web.executive;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import net.officefloor.frame.api.executive.ExecutionStrategy;
import net.officefloor.frame.api.executive.Executive;
import net.officefloor.frame.api.executive.ExecutiveContext;
import net.officefloor.frame.api.executive.TeamOversight;
import net.officefloor.frame.api.executive.TeamSourceContextWrapper;
import net.officefloor.frame.api.executive.source.ExecutiveSourceContext;
import net.officefloor.frame.api.team.Job;
import net.officefloor.frame.api.team.Team;
import net.officefloor.frame.api.team.source.TeamSourceContext;
import net.officefloor.frame.internal.structure.Execution;
import net.officefloor.frame.internal.structure.ProcessState;
import net.officefloor.web.executive.CpuCore.LogicalCpu;
import net.openhft.affinity.Affinity;

/**
 * Web {@link Thread} affinity {@link Executive}.
 * 
 * @author Daniel Sagenschneider
 */
public class WebThreadAffinityExecutive implements Executive, ExecutionStrategy, TeamOversight {

	/**
	 * Bound {@link CpuAffinity}.
	 */
	private final ThreadLocal<CpuAffinity> boundCpuAffinity = new ThreadLocal<>();

	/**
	 * {@link CpuCore} instances.
	 */
	private final CpuCore[] cpuCores;

	/**
	 * {@link ExecutionStrategy} instances.
	 */
	private final ExecutionStrategy[] executionStrategies = new ExecutionStrategy[] { this };

	/**
	 * {@link ThreadFactory} instances for the {@link ExecutionStrategy}.
	 */
	private final ThreadFactory[] threadFactories;

	/**
	 * {@link TeamOversight} instances.
	 */
	private final TeamOversight[] teamOversights = new TeamOversight[] { this };

	/**
	 * {@link CpuAffinity} instances.
	 */
	private final CpuAffinity[] cpuAffinities;

	/**
	 * Next {@link CpuAffinity} index.
	 */
	private final AtomicInteger nextCpuAffinityIndex = new AtomicInteger(0);

	/**
	 * Instantiate.
	 * 
	 * @param cpuCores {@link CpuCore} instances.
	 * @param context  {@link ExecutiveSourceContext}.
	 */
	public WebThreadAffinityExecutive(CpuCore[] cpuCores, ExecutiveSourceContext context) {
		this.cpuCores = cpuCores;

		// Create a thread factory per logical CPU
		List<CpuAffinity> cpuAffinities = new LinkedList<>();
		List<ThreadFactory> threadFactories = new LinkedList<>();
		for (int coreIndex = 0; coreIndex < this.cpuCores.length; coreIndex++) {
			CpuCore cpuCore = this.cpuCores[coreIndex];

			// Load the logical CPU
			for (LogicalCpu logicalCpu : cpuCore.getCpus()) {

				// Create and register the CPU affinity
				CpuAffinity cpuAffinity = new CpuAffinity(coreIndex);
				cpuAffinities.add(cpuAffinity);

				// Create thread factory for logical CPU
				ThreadFactory rawThreadFactory = context.createThreadFactory(this.getExecutionStrategyName(), this);
				ThreadFactory boundThreadFactory = (runnable) -> rawThreadFactory.newThread(() -> {

					// Bind thread to logical CPU
					Affinity.setAffinity(logicalCpu.getCpuAffinity());
					this.boundCpuAffinity.set(cpuAffinity);

					// Run logic for thread
					runnable.run();
				});

				// Add the thread factory
				threadFactories.add(boundThreadFactory);
			}
		}
		this.cpuAffinities = cpuAffinities.toArray(new CpuAffinity[0]);
		this.threadFactories = threadFactories.toArray(new ThreadFactory[0]);
	}

	/**
	 * <p>
	 * Obtains the {@link CpuAffinity} for the current {@link Thread}.
	 * <p>
	 * Note that if the current {@link Thread} does not have a {@link CpuAffinity},
	 * one is assigned.
	 * 
	 * @return {@link CpuAffinity} for the current {@link Thread}.
	 */
	private CpuAffinity bindCurrentThreadCpuAffinity() {

		// Determine if already bound to CPU affinity
		CpuAffinity cpuAffinity = this.boundCpuAffinity.get();
		if (cpuAffinity != null) {
			return cpuAffinity;
		}

		// No bound CPU affinity, so round robin
		int cpuAffinityIndex = this.nextCpuAffinityIndex
				.getAndUpdate((value) -> (value + 1) % this.cpuAffinities.length);
		cpuAffinity = this.cpuAffinities[cpuAffinityIndex];

		// Bind current thread to the CPU core
		CpuCore cpuCore = this.cpuCores[cpuAffinity.coreIndex];
		Affinity.setAffinity(cpuCore.getCoreAffinity());
		this.boundCpuAffinity.set(cpuAffinity);

		// Return the affinity
		return cpuAffinity;
	}

	/*
	 * =================== Executive ======================
	 */

	@Override
	public Object createProcessIdentifier() {

		// Obtain the CPU affinity
		CpuAffinity cpuAffinity = this.bindCurrentThreadCpuAffinity();

		// Return the new process identifier
		return new ProcessIdentifier(cpuAffinity);
	}

	@Override
	public <T extends Throwable> void manageExecution(Execution<T> execution) throws T {

		// Ensure associate thread to CPU
		this.bindCurrentThreadCpuAffinity();

		// Execute the execution
		execution.execute();
	}

	@Override
	public ExecutionStrategy[] getExcutionStrategies() {
		return this.executionStrategies;
	}

	@Override
	public TeamOversight[] getTeamOversights() {
		return this.teamOversights;
	}

	/*
	 * ================= ExecutionStrategy ==================
	 */

	@Override
	public String getExecutionStrategyName() {
		return "CPU_AFFINITY";
	}

	@Override
	public ThreadFactory[] getThreadFactories() {
		return this.threadFactories;
	}

	/*
	 * =================== TeamOversight =====================
	 */

	@Override
	public String getTeamOversightName() {
		return "CORE_AFFINITY";
	}

	@Override
	public Team createTeam(ExecutiveContext context) throws Exception {

		// Determine the split of threads
		int teamSize = Math.max(1, context.getTeamSize() / this.cpuCores.length);

		// Create a team for each CPU core
		Team[] teams = new Team[this.cpuCores.length];
		for (int coreIndex = 0; coreIndex < teams.length; coreIndex++) {
			CpuCore core = this.cpuCores[coreIndex];

			// Create the team source context
			TeamSourceContext teamSourceContext = new TeamSourceContextWrapper(context, teamSize,
					"CORE-" + core.getCoreId(), (runnable) -> () -> {
						Affinity.setAffinity(core.getCoreAffinity());
						runnable.run();
					});

			// Create the team
			teams[coreIndex] = context.getTeamSource().createTeam(teamSourceContext);
		}

		// Return the thread affinity team
		return new ThreadAffinityTeam(teams);
	}

	/**
	 * CPU affinity.
	 */
	private static class CpuAffinity {

		/**
		 * Index of the core for this CPU.
		 */
		private final int coreIndex;

		/**
		 * Instantiate.
		 * 
		 * @param coreIndex Index of the core for this CPU.
		 */
		private CpuAffinity(int coreIndex) {
			this.coreIndex = coreIndex;
		}
	}

	/**
	 * {@link ProcessState} identifier.
	 */
	private static class ProcessIdentifier {

		/**
		 * {@link CpuAffinity} for the {@link ProcessState}.
		 */
		private final CpuAffinity cpuAffinity;

		/**
		 * Instantiate.
		 * 
		 * @param cpuAffinity {@link CpuAffinity} for the {@link ProcessState}.
		 */
		private ProcessIdentifier(CpuAffinity cpuAffinity) {
			this.cpuAffinity = cpuAffinity;
		}
	}

	/**
	 * {@link Team} delegating to appropriate {@link CpuCore} {@link Team}.
	 */
	private class ThreadAffinityTeam implements Team {

		/**
		 * {@link Team} instances for each {@link CpuCore}.
		 */
		private final Team[] teams;

		/**
		 * Instantiate.
		 * 
		 * @param teams {@link Team} instances for each {@link CpuCore}.
		 */
		private ThreadAffinityTeam(Team[] teams) {
			this.teams = teams;
		}

		/*
		 * ================ Team ======================
		 */

		@Override
		public void startWorking() {
			for (Team team : this.teams) {
				team.startWorking();
			}
		}

		@Override
		public void assignJob(Job job) {

			// Obtain the CPU affinity
			ProcessIdentifier identifier = (ProcessIdentifier) job.getProcessIdentifier();

			// Assign job to appropriate team
			int coreIndex = identifier.cpuAffinity.coreIndex;
			this.teams[coreIndex].assignJob(job);
		}

		@Override
		public void stopWorking() {
			for (Team team : this.teams) {
				team.stopWorking();
			}
		}
	}

}