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

package net.officefloor.frame.impl.execute.executive;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import net.officefloor.frame.api.executive.BackgroundScheduler;
import net.officefloor.frame.api.executive.ExecutionStrategy;
import net.officefloor.frame.api.executive.Executive;
import net.officefloor.frame.api.executive.ExecutiveStartContext;
import net.officefloor.frame.api.executive.ProcessIdentifier;
import net.officefloor.frame.api.executive.source.ExecutiveSource;
import net.officefloor.frame.api.executive.source.ExecutiveSourceContext;
import net.officefloor.frame.api.executive.source.impl.AbstractExecutiveSource;
import net.officefloor.frame.impl.execute.execution.ThreadFactoryManufacturer;
import net.officefloor.frame.internal.structure.OfficeManager;

/**
 * Default {@link Executive}.
 * 
 * @author Daniel Sagenschneider
 */
public class DefaultExecutive extends AbstractExecutiveSource
		implements Executive, ExecutionStrategy, BackgroundScheduler {

	/**
	 * Default {@link ExecutionStrategy} name.
	 */
	public static final String EXECUTION_STRATEGY_NAME = "default";

	/**
	 * {@link Executor} for servicing.
	 */
	private ExecutorService executor;

	/**
	 * {@link ScheduledExecutorService} for scheduling servicing.
	 */
	private ScheduledExecutorService scheduler;

	/**
	 * {@link ThreadFactory} instances.
	 */
	private ThreadFactory[] threadFactories;

	/**
	 * Default construct to be used as {@link ExecutiveSource}.
	 */
	public DefaultExecutive() {
	}

	/**
	 * Instantiate to use as {@link Executive}.
	 * 
	 * @param threadFactoryManufacturer {@link ThreadFactoryManufacturer}.
	 */
	public DefaultExecutive(ThreadFactoryManufacturer threadFactoryManufacturer) {
		this.threadFactories = new ThreadFactory[] {
				threadFactoryManufacturer.manufactureThreadFactory(this.getExecutionStrategyName(), this) };
	}

	/**
	 * Obtains the {@link ExecutionStrategy} instances by name.
	 * 
	 * @return {@link ExecutionStrategy} instances by name.
	 */
	public Map<String, ThreadFactory[]> getExecutionStrategyMap() {
		Map<String, ThreadFactory[]> executionStrategies = new HashMap<>();
		executionStrategies.put(this.getExecutionStrategyName(), this.threadFactories);
		return executionStrategies;
	}

	/*
	 * ================= ExecutiveSource =================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
	}

	@Override
	public Executive createExecutive(ExecutiveSourceContext context) throws Exception {
		int availableProcessors = Runtime.getRuntime().availableProcessors();
		this.threadFactories = new ThreadFactory[availableProcessors];
		for (int i = 0; i < availableProcessors; i++) {
			this.threadFactories[i] = context.createThreadFactory(this.getExecutionStrategyName() + "-" + i, this);
		}
		return this;
	}

	/*
	 * =================== Executive =====================
	 */

	@Override
	public ExecutionStrategy[] getExcutionStrategies() {
		return new ExecutionStrategy[] { this };
	}

	@Override
	public void startManaging(ExecutiveStartContext context) throws Exception {

		// Start the executor services
		this.executor = Executors.newCachedThreadPool();
		this.scheduler = Executors.newScheduledThreadPool(1);

		// Monitor all the default Offices
		for (OfficeManager officeManager : context.getDefaultOfficeManagers()) {
			final OfficeManager finalOfficeManager = officeManager;
			long monitorInterval = officeManager.getMonitorInterval();

			// Determine if monitor the office
			if (monitorInterval > 0) {
				this.scheduler.scheduleWithFixedDelay(() -> finalOfficeManager.runAssetChecks(), monitorInterval,
						monitorInterval, TimeUnit.MILLISECONDS);
			}
		}
	}

	@Override
	public Executor createExecutor(ProcessIdentifier processIdentifier) {
		return this.executor;
	}

	@Override
	public void schedule(ProcessIdentifier processIdentifier, long delay, Runnable runnable) {
		this.scheduler.schedule(runnable, delay, TimeUnit.MILLISECONDS);
	}

	@Override
	public void stopManaging() throws Exception {

		// Shutdown any servicing first
		this.executor.shutdown();
		this.executor.awaitTermination(10, TimeUnit.SECONDS);

		// Now servicing stopped, stop any monitoring/polling
		this.scheduler.shutdownNow();
		this.scheduler.awaitTermination(10, TimeUnit.SECONDS);
	}

	/*
	 * ================ BackgroundScheduler ==============
	 */

	@Override
	public void schedule(long delay, Runnable runnable) {
		this.scheduler.schedule(runnable, delay, TimeUnit.MILLISECONDS);
	}

	/*
	 * ================ ExecutionStrategy ================
	 */

	@Override
	public String getExecutionStrategyName() {
		return EXECUTION_STRATEGY_NAME;
	}

	@Override
	public ThreadFactory[] getThreadFactories() {
		return this.threadFactories;
	}

}
