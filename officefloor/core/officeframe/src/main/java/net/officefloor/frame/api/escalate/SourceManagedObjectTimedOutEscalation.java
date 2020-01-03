package net.officefloor.frame.api.escalate;

import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;

/**
 * {@link Escalation} indicating that the {@link ManagedObjectSource} was timed
 * out in providing a {@link ManagedObject}.
 * 
 * @author Daniel Sagenschneider
 */
public class SourceManagedObjectTimedOutEscalation extends ManagedObjectEscalation {

	/**
	 * Serial version UID.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Initiate.
	 * 
	 * @param objectType {@link Class} of the {@link Object} returned from the timed
	 *                   out {@link ManagedObject}.
	 */
	public SourceManagedObjectTimedOutEscalation(Class<?> objectType) {
		super(objectType);
	}

}