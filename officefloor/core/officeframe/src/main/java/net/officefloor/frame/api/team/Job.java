package net.officefloor.frame.api.team;

import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.internal.structure.ProcessState;

/**
 * {@link Job} executed by a {@link Team}.
 * 
 * @author Daniel Sagenschneider
 */
public interface Job extends Runnable {

	/**
	 * <p>
	 * Obtains the identifier for the {@link ProcessState} containing this
	 * {@link Job}.
	 * <p>
	 * This allows the {@link Team} executing the {@link Job} to be aware of the
	 * {@link ProcessState} context in which the {@link Job} is to be executed.
	 * <p>
	 * An example use would be embedding {@link OfficeFloor} within an
	 * Application Server and using this identifier and a
	 * {@link ThreadLocalAwareTeam} to know the invoking {@link Thread} for
	 * interaction with {@link ThreadLocal} instances of the Application Server.
	 * 
	 * @return Identifier for the {@link ProcessState} containing this
	 *         {@link Job}
	 * 
	 * @see ThreadLocalAwareTeam
	 */
	Object getProcessIdentifier();

	/**
	 * Enables a {@link Team} to cancel the {@link Job} should it be overloaded
	 * with {@link Job} instances.
	 * 
	 * @param cause
	 *            Reason by {@link Team} for canceling the {@link Job}.
	 */
	void cancel(Throwable cause);

}