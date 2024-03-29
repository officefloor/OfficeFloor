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

package net.officefloor.frame.impl.execute.clock;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;

import net.officefloor.frame.api.clock.Clock;
import net.officefloor.frame.api.clock.ClockFactory;
import net.officefloor.frame.api.executive.BackgroundScheduler;
import net.officefloor.frame.internal.structure.BackgroundScheduling;

/**
 * {@link ClockFactory} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class ClockFactoryImpl implements ClockFactory, BackgroundScheduling {

	/**
	 * {@link Clock} instances.
	 */
	private final List<ClockImpl<?>> clocks = new LinkedList<>();

	/**
	 * {@link BackgroundScheduler}.
	 */
	private volatile BackgroundScheduler scheduler;

	/**
	 * Obtains the current time in seconds since Epoch.
	 * 
	 * @return Current time in seconds since Epoch.
	 */
	protected long currentTimeSeconds() {
		return System.currentTimeMillis() / 1000;
	}

	/*
	 * ================= ClockFactory ======================
	 */

	@Override
	public <T> Clock<T> createClock(Function<Long, T> translator) {
		ClockImpl<T> clock = new ClockImpl<>(translator);
		this.clocks.add(clock);
		return clock;
	}

	/*
	 * ================== BackgroundScheduling =======================
	 */

	@Override
	public void startBackgroundScheduling(BackgroundScheduler scheduler) {

		// Capture scheduler for further time updates
		this.scheduler = scheduler;

		// Undertake time updates
		for (ClockImpl<?> clock : this.clocks) {

			// Set time immediately
			clock.run();

			// Start scheduling updates to time
			scheduler.schedule(1000, clock);
		}
	}

	/**
	 * {@link Clock} implementation.
	 */
	private class ClockImpl<T> implements Clock<T>, Runnable {

		/**
		 * Translator for time.
		 */
		private final Function<Long, T> translator;

		/**
		 * Time. Will be <code>null</code> if no background scheduling.
		 */
		private volatile T time = null;

		/**
		 * Instantiate.
		 * 
		 * @param translator Translator for time.
		 */
		private ClockImpl(Function<Long, T> translator) {
			this.translator = translator;
		}

		/*
		 * =================== Time =========================
		 */

		@Override
		public T getTime() {
			T time = this.time;
			return time != null ? time : this.translator.apply(ClockFactoryImpl.this.currentTimeSeconds());
		}

		/*
		 * ================== Runnable ======================
		 */

		@Override
		public void run() {

			// Update the time
			this.time = this.translator.apply(ClockFactoryImpl.this.currentTimeSeconds());

			// Register for another time update
			ClockFactoryImpl.this.scheduler.schedule(1000, this);
		}
	}

}
