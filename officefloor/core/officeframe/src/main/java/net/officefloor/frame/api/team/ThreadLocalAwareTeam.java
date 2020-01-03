package net.officefloor.frame.api.team;

import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.internal.structure.ProcessState;

/**
 * <p>
 * Enables a {@link Team} to execute the {@link Job} with the {@link Thread}
 * invoking the {@link ProcessState}.
 * <p>
 * An example use is for embedding {@link OfficeFloor} within an Application
 * Server and associating the {@link Thread} invoking the {@link ProcessState}
 * for {@link ThreadLocal} instances of the Application Server.
 * 
 * @author Daniel Sagenschneider
 */
public interface ThreadLocalAwareTeam extends Team {

	/**
	 * <p>
	 * Indicates if {@link ThreadLocalAwareTeam}.
	 * <p>
	 * Allows for implementing the interface without being thread-local aware.
	 * 
	 * @return <code>true</code> if {@link ThreadLocalAwareTeam}.
	 */
	default boolean isThreadLocalAware() {
		return true;
	}

	/**
	 * Sets the {@link ThreadLocalAwareContext} for the {@link Team}.
	 * 
	 * @param context {@link ThreadLocalAwareContext} for the {@link Team}.
	 */
	void setThreadLocalAwareness(ThreadLocalAwareContext context);

}