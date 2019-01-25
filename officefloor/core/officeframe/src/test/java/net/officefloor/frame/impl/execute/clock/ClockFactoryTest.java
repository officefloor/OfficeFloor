package net.officefloor.frame.impl.execute.clock;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.function.Consumer;
import java.util.function.Function;

import net.officefloor.frame.api.clock.Clock;
import net.officefloor.frame.api.clock.ClockFactory;
import net.officefloor.frame.test.OfficeFrameTestCase;

/**
 * Tests the default {@link ClockFactory}.
 * 
 * @author Daniel Sagenschneider
 */
public class ClockFactoryTest extends OfficeFrameTestCase {

	/**
	 * Ensure appropriately updates time.
	 */
	public void testClockFactory() {

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
			clockFactory.run();

			// Ensure clock as expected
			assertEquals("Incorrect seconds - " + currentTimeSeconds, Long.valueOf(currentTimeSeconds),
					seconds.getTime());
			assertEquals("Incorrect date time - " + currentTimeSeconds, dateTimeTranslator.apply(currentTimeSeconds),
					dateTime.getTime());
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