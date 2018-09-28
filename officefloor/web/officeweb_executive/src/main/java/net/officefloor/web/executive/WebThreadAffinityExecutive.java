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

import net.officefloor.frame.api.executive.ExecutionStrategy;
import net.officefloor.frame.api.executive.Executive;
import net.officefloor.frame.api.executive.TeamOversight;
import net.officefloor.frame.api.executive.source.ExecutiveSourceContext;
import net.officefloor.frame.internal.structure.Execution;
import net.officefloor.web.executive.CpuCore.LogicalCpu;
import net.openhft.affinity.Affinity;

/**
 * Web {@link Thread} affinity {@link Executive}.
 * 
 * @author Daniel Sagenschneider
 */
public class WebThreadAffinityExecutive implements Executive, ExecutionStrategy, TeamOversight {

	/**
	 * Bound {@link LogicalCpu}.
	 */
	private final ThreadLocal<LogicalCpu> boundLogicalCpu = new ThreadLocal<>();

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
	 * Instantiate.
	 * 
	 * @param cpuCores {@link CpuCore} instances.
	 * @param context  {@link ExecutiveSourceContext}.
	 */
	public WebThreadAffinityExecutive(CpuCore[] cpuCores, ExecutiveSourceContext context) {

		// Create a thread factory per logical CPU
		List<ThreadFactory> threadFactories = new LinkedList<>();
		for (CpuCore cpuCore : cpuCores) {
			for (LogicalCpu logicalCpu : cpuCore.getCpus()) {

				// Create thread factory for logical CPU
				ThreadFactory rawThreadFactory = context.createThreadFactory(this.getExecutionStrategyName(), this);
				ThreadFactory boundThreadFactory = (runnable) -> rawThreadFactory.newThread(() -> {

					// Bind thread to logical CPU
					Affinity.setAffinity(logicalCpu.getCpuAffinity());

					// Run logic for thread
					runnable.run();
				});

				// Add the thread factory
				threadFactories.add(boundThreadFactory);
			}
		}
		this.threadFactories = threadFactories.toArray(new ThreadFactory[0]);
	}

	/*
	 * =================== Executive ======================
	 */

	@Override
	public Object createProcessIdentifier() {
		// TODO Auto-generated method stub
		return Executive.super.createProcessIdentifier();
	}

	@Override
	public <T extends Throwable> void manageExecution(Execution<T> execution) throws T {

		// TODO ensure associate thread to core

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
		return "LOGICAL_CPU_AFFINITY";
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
		return "THREAD_AFFINITY";
	}

}