package net.officefloor.frame.api.profile;

import java.util.List;

import net.officefloor.frame.internal.structure.ProcessState;

/**
 * Profiled {@link ProcessState}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ProfiledProcessState {

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
	 * Obtains the {@link ProfiledThreadState} instances.
	 * 
	 * @return {@link ProfiledThreadState} instances.
	 */
	List<ProfiledThreadState> getProfiledThreadStates();

}