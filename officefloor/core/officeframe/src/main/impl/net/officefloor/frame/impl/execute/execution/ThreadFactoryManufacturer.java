/*-
 * #%L
 * OfficeFrame
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

package net.officefloor.frame.impl.execute.execution;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import net.officefloor.frame.api.executive.Executive;
import net.officefloor.frame.api.team.Team;
import net.officefloor.frame.internal.structure.ManagedExecution;
import net.officefloor.frame.internal.structure.ManagedExecutionFactory;

/**
 * Manufactures {@link ThreadFactory}.
 * 
 * @author Daniel Sagenschneider
 */
public class ThreadFactoryManufacturer {

	/**
	 * {@link ManagedExecutionFactory}.
	 */
	private final ManagedExecutionFactory managedExecutionFactory;

	/**
	 * Decorator of the created {@link Thread} instances. May be <code>null</code>.
	 */
	private final Consumer<Thread> decorator;

	/**
	 * Instantiate.
	 *
	 * @param executionFactory {@link ManagedExecutionFactory}.
	 * @param decorator        Decorator of the created {@link Thread} instances.
	 *                         May be <code>null</code>.
	 */
	public ThreadFactoryManufacturer(ManagedExecutionFactory executionFactory, Consumer<Thread> decorator) {
		this.managedExecutionFactory = executionFactory;
		this.decorator = decorator;
	}

	/**
	 * Manufactures a new {@link ThreadFactory}.
	 * 
	 * @param name      Name for the {@link Thread} instances created from the
	 *                  {@link ThreadFactory}.
	 * @param executive {@link Executive}.
	 * @return {@link ThreadFactory}.
	 */
	public ThreadFactory manufactureThreadFactory(String name, Executive executive) {
		return new OfficeFloorThreadFactory(name, executive);
	}

	/**
	 * {@link ThreadFactory} for the {@link Team}.
	 */
	private class OfficeFloorThreadFactory implements ThreadFactory {

		/**
		 * {@link ThreadGroup}.
		 */
		private final ThreadGroup group;

		/**
		 * Prefix of {@link Thread} name.
		 */
		private final String threadNamePrefix;

		/**
		 * {@link Executive}.
		 */
		private final Executive executive;

		/**
		 * Index of the next {@link Thread}.
		 */
		private final AtomicInteger nextThreadIndex = new AtomicInteger(1);

		/**
		 * Initiate.
		 * 
		 * @param name Name for the {@link Thread} instances created from the
		 *             {@link ThreadFactory}.
		 */
		private OfficeFloorThreadFactory(String name, Executive executive) {
			this.group = new ThreadGroup(name);
			this.threadNamePrefix = name + "-";
			this.executive = executive;
		}

		/*
		 * ==================== ThreadFactory =======================
		 */

		@Override
		public Thread newThread(final Runnable r) {

			// Create the managed execution
			ManagedExecution<RuntimeException> managedExecution = ThreadFactoryManufacturer.this.managedExecutionFactory
					.createManagedExecution(this.executive, () -> {
						r.run();
						return null;
					});

			// Create and configure the thread
			String threadName = this.threadNamePrefix + this.nextThreadIndex.getAndIncrement();
			Runnable runnable = () -> managedExecution.managedExecute();
			Thread thread = this.executive.createThread(threadName, this.group, runnable);
			if (ThreadFactoryManufacturer.this.decorator != null) {
				ThreadFactoryManufacturer.this.decorator.accept(thread);
			}

			// Return the thread
			return thread;
		}
	}

}
