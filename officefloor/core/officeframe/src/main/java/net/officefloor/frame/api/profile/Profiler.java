package net.officefloor.frame.api.profile;

import net.officefloor.frame.api.OfficeFrame;
import net.officefloor.frame.internal.structure.ProcessState;

/**
 * Profiler that receives profile information from {@link OfficeFrame} on each
 * {@link ProcessState} completion.
 * 
 * @author Daniel Sagenschneider
 */
public interface Profiler {

	/**
	 * <p>
	 * Enables the {@link Profiler} to profile the completed
	 * {@link ProcessState}.
	 * <p>
	 * 
	 * @param process
	 *            {@link ProfiledProcessState}.
	 */
	void profileProcessState(ProfiledProcessState process);

}