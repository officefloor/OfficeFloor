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

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.function.Consumer;
import java.util.function.Function;

import org.junit.jupiter.api.Test;

import net.officefloor.frame.api.clock.Clock;
import net.officefloor.frame.api.clock.ClockFactory;

/**
 * Tests the default {@link ClockFactory}.
 * 
 * @author Daniel Sagenschneider
 */
public class ClockFactoryTest {

	/**
	 * Ensure appropriately updates time.
	 */
	@Test
	public void clockFactory() {

		// Create the clock
		MockClockFactory clockFactory = new MockClockFactory();

		// Translators
		Function<Long, ZonedDateTime> dateTimeTranslator = (time) -> Instant.ofEpochSecond(time)
				.atZone(ZoneId.of("UTC"));

		// Create some clocks
		Clock<Long> seconds = clockFactory.createClock((time) -> time);
		Clock<ZonedDateTime> dateTime = clockFactory.createClock(dateTimeTranslator);

		// Assert the time on the clocks
		Consumer<Long> assertTimes = (currentTimeSeconds) -> {

			// Trigger timer to update time
			clockFactory.currentTimeSeconds = currentTimeSeconds;

			// Ensure clock as expected
			assertEquals(Long.valueOf(currentTimeSeconds), seconds.getTime(),
					"Incorrect seconds - " + currentTimeSeconds);
			assertEquals(dateTimeTranslator.apply(currentTimeSeconds), dateTime.getTime(),
					"Incorrect date time - " + currentTimeSeconds);
		};

		// Validate the clock
		assertTimes.accept(1000L);
		assertTimes.accept(Instant.now().getEpochSecond());
	}

	/**
	 * {@link ClockFactory} mock time.
	 */
	private static class MockClockFactory extends ClockFactoryImpl {

		/**
		 * Current time in seconds since Epoch.
		 */
		private long currentTimeSeconds;

		/*
		 * ================ ClockFactory ===================
		 */

		@Override
		protected long currentTimeSeconds() {
			return this.currentTimeSeconds;
		}
	}

}
