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