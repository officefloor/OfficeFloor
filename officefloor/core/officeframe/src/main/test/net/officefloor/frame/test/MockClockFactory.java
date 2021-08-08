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
