package net.officefloor.frame.api.profile;

import net.officefloor.frame.api.function.ManagedFunction;

/**
 * Profiled execution of a {@link ManagedFunction}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ProfiledManagedFunction {

	/**
	 * Obtains the name of the {@link ManagedFunction}.
	 * 
	 * @return Name of the {@link ManagedFunction}.
	 */
	String getFunctionName();

	/**
	 * Obtains the time stamp in milliseconds when the {@link ManagedFunction}
	 * was started.
	 * 
	 * @return Time stamp in milliseconds when the {@link ManagedFunction} was
	 *         started.
	 */
	long getStartTimestampMilliseconds();

	/**
	 * Obtains the time stamp in nanoseconds when the {@link ManagedFunction}
	 * was started.
	 * 
	 * @return Time stamp in nanoseconds when the {@link ManagedFunction} was
	 *         started.
	 */
	long getStartTimestampNanoseconds();

	/**
	 * Obtains the name of the executing {@link Thread}.
	 * 
	 * @return Name of the executing {@link Thread}.
	 */
	String getExecutingThreadName();

}