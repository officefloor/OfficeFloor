package net.officefloor.frame.api.clock;

import java.util.function.Function;

/**
 * Factory to create a {@link Clock}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ClockFactory {

	/**
	 * Creates a {@link Clock}.
	 * 
	 * @param translator Translate the seconds since Epoch to {@link Clock} time.
	 * @return {@link Clock}.
	 */
	<T> Clock<T> createClock(Function<Long, T> translator);

}