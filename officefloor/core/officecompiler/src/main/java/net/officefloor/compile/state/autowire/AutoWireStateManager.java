package net.officefloor.compile.state.autowire;

import net.officefloor.frame.api.manage.ObjectUser;
import net.officefloor.frame.api.manage.StateManager;
import net.officefloor.frame.api.manage.UnknownObjectException;
import net.officefloor.frame.api.managedobject.ManagedObject;

/**
 * {@link StateManager} that enables obtaining {@link ManagedObject} objects by
 * their qualifications and types.
 * 
 * @author Daniel Sagenschneider
 */
public interface AutoWireStateManager extends AutoCloseable {

	/**
	 * Loads the object from the {@link ManagedObject} asynchronously.
	 * 
	 * @param qualifier  Qualifier. May be <code>null</code>.
	 * @param objectType Required object type.
	 * @param user       {@link ObjectUser} to receive the loaded object (or
	 *                   possible failure).
	 * @throws UnknownObjectException If unknown bound object name.
	 */
	<O> void load(String qualifier, Class<? extends O> objectType, ObjectUser<O> user) throws UnknownObjectException;

	/**
	 * Obtains the object for the {@link ManagedObject} synchronously.
	 * 
	 * @param qualifier             Qualifier. May be <code>null</code>.
	 * @param objectType            Required object type.
	 * @param timeoutInMilliseconds Time out in milliseconds to wait for the
	 *                              {@link ManagedObject} creation.
	 * @return Object.
	 * @throws UnknownObjectException If unknown bound object name.
	 * @throws Throwable              If failure in obtaining the bound object.
	 */
	<O> O getObject(String qualifier, Class<? extends O> objectType, long timeoutInMilliseconds)
			throws UnknownObjectException, Throwable;

}