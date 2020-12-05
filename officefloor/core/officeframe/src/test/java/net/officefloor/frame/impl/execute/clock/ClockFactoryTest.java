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
