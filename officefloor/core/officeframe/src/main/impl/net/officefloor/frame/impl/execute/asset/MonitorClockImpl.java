package net.officefloor.frame.impl.execute.asset;

import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.internal.structure.MonitorClock;

/**
 * {@link MonitorClock} implementation.
 *
 * @author Daniel Sagenschneider
 */
public class MonitorClockImpl implements MonitorClock {

	/**
	 * Keeps approximate time for monitoring the {@link Office}.
	 */
	private volatile long currentTime = System.currentTimeMillis();

	/**
	 * Updates the current time.
	 */
	public void updateTime() {
		this.currentTime = System.currentTimeMillis();
	}

	/*
	 * ======================== OfficeClock =====================
	 */

	@Override
	public long currentTimeMillis() {
		return this.currentTime;
	}

}
