package net.officefloor.frame.api.executive;

import net.officefloor.frame.internal.structure.ProcessState;

/**
 * <p>
 * Optional interface for {@link Executive} to implement to indicate it supports
 * background scheduling of {@link Runnable} instances.
 * <p>
 * Some {@link Executive} implementations can only have {@link ProcessState}
 * scoped {@link Thread} instances that disallow long running background
 * {@link Thread}. However, long running background {@link Thread} allows more
 * efficiency in running scheduled {@link Runnable} instances.
 * 
 * @author Daniel Sagenschneider
 */
public interface BackgroundScheduler {

	/**
	 * Schedules the {@link Runnable} to be executed after the delay.
	 * 
	 * @param delay    Delay in milliseconds.
	 * @param runnable {@link Runnable}.
	 */
	void schedule(long delay, Runnable runnable);

}