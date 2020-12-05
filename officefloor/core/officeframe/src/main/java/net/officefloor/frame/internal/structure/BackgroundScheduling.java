package net.officefloor.frame.internal.structure;

import net.officefloor.frame.api.executive.BackgroundScheduler;

/**
 * <p>
 * Opportunity for background scheduling.
 * <p>
 * Background scheduling is not always available in some environments (typically
 * where {@link ProcessState} scoped {@link Thread} instances are only
 * available).
 * 
 * @author Daniel Sagenschneider
 */
public interface BackgroundScheduling {

	/**
	 * Starts the {@link BackgroundScheduling}.
	 * 
	 * @param scheduler {@link BackgroundScheduler}.
	 */
	void startBackgroundScheduling(BackgroundScheduler scheduler);

}