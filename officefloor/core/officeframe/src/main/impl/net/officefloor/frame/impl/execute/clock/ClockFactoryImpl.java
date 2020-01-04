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

package net.officefloor.frame.impl.execute.clock;

import java.time.Instant;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.function.Function;

import net.officefloor.frame.api.build.OfficeFloorEvent;
import net.officefloor.frame.api.build.OfficeFloorListener;
import net.officefloor.frame.api.clock.Clock;
import net.officefloor.frame.api.clock.ClockFactory;
import net.officefloor.frame.api.manage.OfficeFloor;

/**
 * {@link ClockFactory} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class ClockFactoryImpl extends TimerTask implements ClockFactory, OfficeFloorListener {

	/**
	 * {@link Clock} instances.
	 */
	private final List<ClockImpl<?>> clocks = new LinkedList<>();

	/**
	 * {@link Timer}.
	 */
	private Timer timer;

	/**
	 * Obtains the current time in seconds since Epoch.
	 * 
	 * @return Current time in seconds since Epoch.
	 */
	protected long currentTimeSeconds() {
		return Instant.now().getEpochSecond();
	}

	/*
	 * ================= ClockFactory ======================
	 */

	@Override
	public <T> Clock<T> createClock(Function<Long, T> translator) {
		ClockImpl<T> clock = new ClockImpl<>(this.currentTimeSeconds(), translator);
		this.clocks.add(clock);
		return clock;
	}

	/*
	 * =============== OfficeFloorListener =================
	 */

	@Override
	public void officeFloorOpened(OfficeFloorEvent event) throws Exception {
		this.timer = new Timer(OfficeFloor.class.getSimpleName() + "_Clocks", true);
		this.timer.schedule(this, 0, 1000);

	}

	@Override
	public void officeFloorClosed(OfficeFloorEvent event) throws Exception {
		if (this.timer != null) {
			this.timer.cancel();
		}
	}

	/*
	 * ==================== TimerTask ======================
	 */

	@Override
	public void run() {

		// Obtain the current time
		long currentTimeSeconds = this.currentTimeSeconds();

		// Update the clocks
		for (ClockImpl<?> clock : this.clocks) {
			clock.updateTime(currentTimeSeconds);
		}
	}

	/**
	 * {@link Clock} implementation.
	 */
	private static class ClockImpl<T> implements Clock<T> {

		/**
		 * Translator for time.
		 */
		private final Function<Long, T> translator;

		/**
		 * Time.
		 */
		private volatile T time;

		/**
		 * Instantiate.
		 * 
		 * @param currentTimeSeconds Current time in seconds since Epoch.
		 * @param translator         Translator for time.
		 */
		private ClockImpl(long currentTimeSeconds, Function<Long, T> translator) {
			this.translator = translator;

			// Set initial time
			this.updateTime(currentTimeSeconds);
		}

		/**
		 * Updates the time.
		 * 
		 * @param currentTimeSeconds Current time in seconds.
		 */
		private void updateTime(long currentTimeSeconds) {
			this.time = translator.apply(currentTimeSeconds);
		}

		/*
		 * =================== Time =========================
		 */

		@Override
		public T getTime() {
			return this.time;
		}
	}

}
