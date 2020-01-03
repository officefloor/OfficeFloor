package net.officefloor.web.session;

import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.managedobject.AsynchronousManagedObject;

/**
 * Administration interface for the {@link HttpSession}.
 *
 * @author Daniel Sagenschneider
 */
public interface HttpSessionAdministration {

	/**
	 * Triggers invalidating the {@link HttpSession}.
	 *
	 * @param isRequireNewSession
	 *            <code>true</code> to have a new {@link HttpSession} created.
	 * @throws Throwable
	 *             If immediate failure in invalidating the {@link HttpSession}.
	 */
	void invalidate(boolean isRequireNewSession) throws Throwable;

	/**
	 * Triggers storing the {@link HttpSession}.
	 *
	 * @throws Throwable
	 *             If immediate failure in storing the {@link HttpSession}.
	 */
	void store() throws Throwable;

	/**
	 * <p>
	 * Indicates if the invalidate or store operation are complete.
	 * <p>
	 * As is an {@link AsynchronousManagedObject}, the next time a new
	 * {@link ManagedFunction} is run the operation should be complete. This method enables
	 * determining if completed immediately and there were no failures of the
	 * operation.
	 *
	 * @return <code>true</code> if the invalidate or store operation is
	 *         complete.
	 * @throws Throwable
	 *             Possible failure in invalidating or storing the
	 *             {@link HttpSession}.
	 */
	boolean isOperationComplete() throws Throwable;

}