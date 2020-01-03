package net.officefloor.frame.api.managedobject.recycle;

import net.officefloor.frame.api.escalate.Escalation;
import net.officefloor.frame.api.managedobject.ManagedObject;

/**
 * {@link Escalation} occurring on cleanup of a {@link ManagedObject}.
 *
 * @author Daniel Sagenschneider
 */
public interface CleanupEscalation {

	/**
	 * Obtains the object type of the {@link ManagedObject}.
	 * 
	 * @return Object type of the {@link ManagedObject}.
	 */
	Class<?> getObjectType();

	/**
	 * Obtains the {@link Escalation}.
	 * 
	 * @return {@link Escalation}.
	 */
	Throwable getEscalation();

}