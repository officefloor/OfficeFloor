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

package net.officefloor.frame.impl.spi.team;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import net.officefloor.frame.api.team.Job;
import net.officefloor.frame.api.team.Team;
import net.officefloor.frame.api.team.source.TeamSource;
import net.officefloor.frame.api.team.source.TeamSourceContext;
import net.officefloor.frame.api.team.source.impl.AbstractTeamSource;
import net.officefloor.frame.impl.execute.officefloor.OfficeFloorImpl;
import net.officefloor.frame.util.TeamSourceStandAlone;

/**
 * {@link TeamSource} based on the {@link Executors} cached thread pool.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class AbstractExecutorTeamSource extends AbstractTeamSource {

	/**
	 * Name of property to obtain the {@link Thread} priority.
	 */
	public static final String PROPERTY_THREAD_PRIORITY = "thread.priority";

	/**
	 * Maximum time to wait in seconds for the {@link ExecutorService} to shutdown.
	 */
	public static final String PROPERTY_SHUTDOWN_TIME_IN_SECONDS = "max.shutdown.time";

	/**
	 * Convenience method to create a {@link Team} from the implementation of this
	 * {@link AbstractExecutorTeamSource}.
	 *
	 * @param teamSize           {@link Team} size.
	 * @param propertyNameValues Property name/value pairs for the
	 *                           {@link TeamSource}.
	 * @return {@link Team}.
	 * @throws IllegalArgumentException If fails to provide correct information to
	 *                                  load the {@link Team}.
	 */
	public Team createTeam(int teamSize, String... propertyNameValues) throws IllegalArgumentException {
		TeamSourceStandAlone standAlone = new TeamSourceStandAlone();
		standAlone.setTeamSize(teamSize);
		for (int i = 0; i < propertyNameValues.length; i += 2) {
			String name = propertyNameValues[i];
			String value = propertyNameValues[i + 1];
			standAlone.addProperty(name, value);
		}
		try {
			Team team = standAlone.loadTeam(this.getClass());
			team.startWorking();
			return team;
		} catch (Exception ex) {
			// Propagate failure
			throw new IllegalArgumentException(ex);
		}
	}

	/**
	 * Obtains the factory to create {@link ExecutorService}.
	 * 
	 * @param context       {@link TeamSourceContext}.
	 * @param threadFactory {@link ThreadFactory} to use for the creation of the
	 *                      {@link Thread} instances.
	 * @return {@link ExecutorServiceFactory}.
	 * @throws Exception If fails to create the {@link ExecutorServiceFactory}.
	 */
	protected abstract ExecutorServiceFactory createExecutorServiceFactory(TeamSourceContext context,
			ThreadFactory threadFactory) throws Exception;

	/**
	 * Factory to create the {@link ExecutorService}.
	 */
	protected static interface ExecutorServiceFactory {

		/**
		 * Creates the {@link ExecutorService}.
		 * 
		 * @return {@link ExecutorService}.
		 */
		ExecutorService createExecutorService();

	}

	/*
	 * ======================= TeamSource ==================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
		// No required properties
	}

	@Override
	public Team createTeam(TeamSourceContext context) throws Exception {

		// Obtain the details of the team
		String teamName = context.getTeamName();
		int maxShutdownWaitTimeInSeconds = Integer
				.valueOf(context.getProperty(PROPERTY_SHUTDOWN_TIME_IN_SECONDS, String.valueOf(10)));
		int threadPriority = Integer
				.valueOf(context.getProperty(PROPERTY_THREAD_PRIORITY, String.valueOf(Thread.NORM_PRIORITY)));

		// Create the thread factory
		ThreadFactory threadFactory = context.getThreadFactory();
		if (threadPriority != Thread.NORM_PRIORITY) {
			ThreadFactory delegate = threadFactory;
			threadFactory = (runnable) -> {
				Thread thread = delegate.newThread(runnable);
				thread.setPriority(threadPriority);
				return thread;
			};
		}

		// Create and return the executor team
		ExecutorServiceFactory serviceFactory = this.createExecutorServiceFactory(context, threadFactory);
		return new ExecutorTeam(teamName, serviceFactory, maxShutdownWaitTimeInSeconds);
	}

	/**
	 * {@link Team} based on the {@link ExecutorService}.
	 */
	public static class ExecutorTeam implements Team {

		/**
		 * {@link ExecutorServiceFactory}.
		 */
		private final ExecutorServiceFactory factory;

		/**
		 * Name of the {@link Team}.
		 */
		private final String teamName;

		/**
		 * Maximum time in seconds to wait for shutdown.
		 */
		private final int maxShutdownWaitTimeInSeconds;

		/**
		 * {@link ExecutorService}.
		 */
		private ExecutorService servicer;

		/**
		 * Initiate.
		 * 
		 * @param teamName                     Name of the {@link Team}.
		 * @param factory                      {@link ExecutorServiceFactory}.
		 * @param maxShutdownWaitTimeInSeconds Maximum time in seconds to wait for
		 *                                     shutdown.
		 */
		public ExecutorTeam(String teamName, ExecutorServiceFactory factory, int maxShutdownWaitTimeInSeconds) {
			this.teamName = teamName;
			this.factory = factory;
			this.maxShutdownWaitTimeInSeconds = maxShutdownWaitTimeInSeconds;
		}

		/*
		 * ==================== Team ============================
		 */

		@Override
		public void startWorking() {
			this.servicer = this.factory.createExecutorService();

			// Determine if can handle rejected jobs
			if (this.servicer instanceof ThreadPoolExecutor) {
				ThreadPoolExecutor threadPool = (ThreadPoolExecutor) this.servicer;
				threadPool.setRejectedExecutionHandler((job, exception) -> {
					((Job) job).cancel(new RejectedExecutionException());
				});
			}
		}

		@Override
		public void assignJob(Job job) {
			this.servicer.execute(job);
		}

		@Override
		public void stopWorking() {

			// Do nothing if not started
			if (this.servicer == null) {
				return;
			}

			// Determine if can wait for thread pool to complete
			if (this.servicer instanceof ThreadPoolExecutor) {
				ThreadPoolExecutor threadPool = (ThreadPoolExecutor) this.servicer;
				BlockingQueue<Runnable> queue = threadPool.getQueue();

				// Wait some time until queue is empty
				long endWaitTime = System.currentTimeMillis() + (this.maxShutdownWaitTimeInSeconds * 1000);
				while ((!queue.isEmpty()) || (threadPool.getActiveCount() > 0)) {

					// Determine if still within time
					if (System.currentTimeMillis() <= endWaitTime) {

						// Still within time, so wait a little
						try {
							Thread.sleep(10);
						} catch (InterruptedException ex) {
						}
					}
				}
			}

			// Shutdown executor
			this.servicer.shutdown();

			// Await termination
			try {
				if (this.servicer.awaitTermination(this.maxShutdownWaitTimeInSeconds, TimeUnit.SECONDS)) {
					return; // successful shutdown
				}
			} catch (InterruptedException ex) {
			}

			// Failed shutdown within time period
			OfficeFloorImpl.getFrameworkLogger().log(Level.WARNING, "Team " + this.teamName + " failed to stop within "
					+ this.maxShutdownWaitTimeInSeconds + " seconds");
		}
	}

}
