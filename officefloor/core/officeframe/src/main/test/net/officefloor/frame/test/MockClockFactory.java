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

package net.officefloor.frame.test;

import java.time.Instant;
import java.util.function.Function;
import java.util.function.Supplier;

import net.officefloor.frame.api.clock.Clock;
import net.officefloor.frame.api.clock.ClockFactory;

/**
 * Mock {@link ClockFactory}.
 * 
 * @author Daniel Sagenschneider
 */
public class MockClockFactory implements ClockFactory {

	/**
	 * Current time in seconds since Epoch.
	 */
	private Supplier<Long> currentTimeSeconds;

	/**
	 * Instantiate with current time.
	 */
	public MockClockFactory() {
		this.currentTimeSeconds = () -> Instant.now().getEpochSecond();
	}

	/**
	 * Instantiate.
	 * 
	 * @param currentTimeSeconds Current time in seconds since Epoch.
	 */
	public MockClockFactory(long currentTimeSeconds) {
		this.currentTimeSeconds = () -> currentTimeSeconds;
	}

	/**
	 * Specifies the current time.
	 * 
	 * @param currentTimeSeconds Current time in seconds since Epoch.
	 */
	public void setCurrentTimeSeconds(long currentTimeSeconds) {
		this.currentTimeSeconds = () -> currentTimeSeconds;
	}

	/*
	 * ==================== ClockFactory =======================
	 */

	@Override
	public <T> Clock<T> createClock(Function<Long, T> translator) {
		return () -> translator.apply(this.currentTimeSeconds.get());
	}

}
