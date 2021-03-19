/*-
 * #%L
 * Web Executive
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package net.officefloor.web.executive;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import net.officefloor.frame.api.executive.BackgroundScheduler;
import net.officefloor.frame.api.executive.ExecutionStrategy;
import net.officefloor.frame.api.executive.Executive;
import net.officefloor.frame.api.executive.ExecutiveContext;
import net.officefloor.frame.api.executive.ExecutiveOfficeContext;
import net.officefloor.frame.api.executive.ExecutiveStartContext;
import net.officefloor.frame.api.executive.ProcessIdentifier;
import net.officefloor.frame.api.executive.TeamOversight;
import net.officefloor.frame.api.executive.TeamSourceContextWrapper;
import net.officefloor.frame.api.executive.source.ExecutiveSourceContext;
import net.officefloor.frame.api.manage.ProcessManager;
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
public class WebThreadAffinityExecutive implements Executive, BackgroundScheduler, ExecutionStrategy, TeamOversight {

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
	 * {@link CpuAffinity} instances.
	 */
	private final CpuAffinity[] cpuAffinities;

	/**
	 * Thread affinity {@link ExecutorService} instances.
	 */
	private ExecutorService[] executors;

	/**
	 * Thread affinity {@link ScheduledExecutorService} instances.
	 */
	private ScheduledExecutorService[] scheduledExecutors;

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
	public ExecutionStrategy[] getExcutionStrategies() {
		return this.executionStrategies;
	}

	@Override
	public TeamOversight getTeamOversight() {
		return this;
	}

	@Override
	public void startManaging(ExecutiveStartContext context) throws Exception {

		// Create executors for each core
		this.executors = new ExecutorService[this.threadFactories.length];
		this.scheduledExecutors = new ScheduledExecutorService[this.threadFactories.length];
		for (int i = 0; i < this.threadFactories.length; i++) {
			this.executors[i] = Executors.newCachedThreadPool(this.threadFactories[i]);
			this.scheduledExecutors[i] = Executors.newScheduledThreadPool(1, this.threadFactories[i]);
		}
	}

	@Override
	public <T extends Throwable> ProcessManager manageExecution(Execution<T> execution) throws T {

		// Ensure associate thread to CPU
		this.bindCurrentThreadCpuAffinity();

		// Execute the execution
		return execution.execute();
	}

	@Override
	public ProcessIdentifier createProcessIdentifier(ExecutiveOfficeContext officeContext) {

		// Obtain the CPU affinity
		CpuAffinity cpuAffinity = this.bindCurrentThreadCpuAffinity();

		// Return the new process identifier
		return new ThreadAffinityProcessIdentifier(cpuAffinity);
	}

	@Override
	public Executor createExecutor(ProcessIdentifier processIdentifier) {

		// Obtain core for process
		ThreadAffinityProcessIdentifier threadAffinityIdentifier = (ThreadAffinityProcessIdentifier) processIdentifier;
		int coreIndex = threadAffinityIdentifier.cpuAffinity.coreIndex;

		// Return executor for the core
		return this.executors[coreIndex];
	}

	@Override
	public void schedule(ProcessIdentifier processIdentifier, long delay, Runnable runnable) {

		// Obtain core for process
		ThreadAffinityProcessIdentifier threadAffinityIdentifier = (ThreadAffinityProcessIdentifier) processIdentifier;
		int coreIndex = threadAffinityIdentifier.cpuAffinity.coreIndex;

		// Schedule on core scheduler
		ScheduledExecutorService scheduler = this.scheduledExecutors[coreIndex];
		scheduler.schedule(runnable, delay, TimeUnit.MILLISECONDS);
	}

	@Override
	public void stopManaging() throws Exception {

		// Stop the executors
		for (ExecutorService executor : this.executors) {
			executor.shutdown();
		}
		for (ScheduledExecutorService scheduler : this.scheduledExecutors) {
			scheduler.shutdown();
		}
		for (ExecutorService executor : this.executors) {
			executor.awaitTermination(10, TimeUnit.SECONDS);
		}
		for (ScheduledExecutorService scheduler : this.scheduledExecutors) {
			scheduler.awaitTermination(10, TimeUnit.SECONDS);
		}
	}

	/*
	 * =================== BackgroundScheduler ==================
	 */

	@Override
	public void schedule(long delay, Runnable runnable) {
		this.scheduledExecutors[0].schedule(runnable, delay, TimeUnit.MILLISECONDS);
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
	public Team createTeam(ExecutiveContext context) throws Exception {

		// Determine if opt out of thread affinity
		if (context.isRequestNoTeamOversight()) {
			return context.getTeamSource().createTeam(context);
		}

		// Create a team for each CPU core
		Team[] teams = new Team[this.cpuCores.length];
		for (int coreIndex = 0; coreIndex < teams.length; coreIndex++) {
			CpuCore core = this.cpuCores[coreIndex];

			// Create the team source context
			TeamSourceContext teamSourceContext = new TeamSourceContextWrapper(context, (teamSize) -> {
				return Math.max(1, teamSize / this.cpuCores.length);
			}, "CORE-" + core.getCoreId(), (runnable) -> () -> {
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
	 * Thread affinity {@link ProcessIdentifier}.
	 */
	private static class ThreadAffinityProcessIdentifier implements ProcessIdentifier {

		/**
		 * {@link CpuAffinity} for the {@link ProcessState}.
		 */
		private final CpuAffinity cpuAffinity;

		/**
		 * Instantiate.
		 * 
		 * @param cpuAffinity {@link CpuAffinity} for the {@link ProcessState}.
		 */
		private ThreadAffinityProcessIdentifier(CpuAffinity cpuAffinity) {
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
		public void assignJob(Job job) throws Exception {

			// Obtain the CPU affinity
			ThreadAffinityProcessIdentifier identifier = (ThreadAffinityProcessIdentifier) job.getProcessIdentifier();
			int coreIndex = identifier.cpuAffinity.coreIndex;

			// Assign job to appropriate team
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
