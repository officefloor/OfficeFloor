package net.officefloor.frame.api.escalate;

import net.officefloor.frame.api.managedobject.ManagedObject;

/**
 * {@link Escalation} indicating that an operation by the {@link ManagedObject}
 * was timed out.
 * 
 * @author Daniel Sagenschneider
 */
public class ManagedObjectOperationTimedOutEscalation extends ManagedObjectEscalation {

	/**
	 * Serial version UID.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Initiate.
	 * 
	 * @param objectType {@link Class} of the {@link Object} returned from the
	 *                   {@link ManagedObject} which had its asynchronous operation
	 *                   timeout.
	 */
	public ManagedObjectOperationTimedOutEscalation(Class<?> objectType) {
		super(objectType);
	}

}