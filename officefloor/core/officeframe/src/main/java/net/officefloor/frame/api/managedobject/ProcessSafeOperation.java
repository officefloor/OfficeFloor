package net.officefloor.frame.api.managedobject;

import net.officefloor.frame.api.escalate.Escalation;
import net.officefloor.frame.internal.structure.ProcessState;

/**
 * {@link ProcessState} safe operation.
 *
 * @author Daniel Sagenschneider
 */
public interface ProcessSafeOperation<R, T extends Throwable> {

	/**
	 * Contains the logic requiring {@link ProcessState} safety.
	 * 
	 * @return Optional return value from operation.
	 * @throws T
	 *             Possible {@link Escalation}.
	 */
	R run() throws T;

}