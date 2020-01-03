package net.officefloor.frame.api.managedobject;

import net.officefloor.frame.api.escalate.Escalation;

/**
 * Asynchronous operation.
 * 
 * @author Daniel Sagenschneider
 */
public interface AsynchronousOperation<T extends Throwable> {

	/**
	 * Undertakes the asynchronous operation.
	 * 
	 * @throws T
	 *             Possible {@link Escalation}.
	 */
	void run() throws T;

}