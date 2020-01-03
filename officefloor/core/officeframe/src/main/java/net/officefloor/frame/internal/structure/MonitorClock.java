package net.officefloor.frame.internal.structure;

import net.officefloor.frame.api.manage.Office;

/**
 * Clock for monitoring the {@link Office}.
 *
 * @author Daniel Sagenschneider
 */
public interface MonitorClock {

	/**
	 * <p>
	 * Obtains the approximate current time.
	 * <p>
	 * This is more efficient means to obtain {@link System#currentTimeMillis()} as
	 * complete millisecond accuracy is not required.
	 * 
	 * @return Approximate {@link System#currentTimeMillis()}.
	 */
	long currentTimeMillis();

}