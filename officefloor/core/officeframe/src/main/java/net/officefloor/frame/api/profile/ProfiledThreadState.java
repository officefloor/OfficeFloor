package net.officefloor.frame.api.profile;

import java.util.List;

import net.officefloor.frame.internal.structure.ThreadState;

/**
 * Profiled {@link ThreadState}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ProfiledThreadState {

	/**
	 * Obtains the start time stamp.
	 * 
	 * @return Start time stamp in milliseconds.
	 */
	long getStartTimestampMilliseconds();

	/**
	 * Obtains the start time stamp.
	 * 
	 * @return Start time stamp in nanoseconds.
	 */
	long getStartTimestampNanoseconds();

	/**
	 * Obtains the {@link ProfiledManagedFunction} instances.
	 * 
	 * @return {@link ProfiledManagedFunction} instances.
	 */
	List<ProfiledManagedFunction> getProfiledManagedFunctions();

}
