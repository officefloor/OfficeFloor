package net.officefloor.frame.api.manage;

import net.officefloor.frame.api.managedobject.ManagedObject;

/**
 * <p>
 * Manages state (multiple {@link ManagedObject} instances used externally).
 * <p>
 * {@link ManagedObject} instances are kept alive until the {@link StateManager}
 * is closed.
 * 
 * @author Daniel Sagenschneider
 */
public interface StateManager extends AutoCloseable {

	/**
	 * Loads the object from the {@link ManagedObject} asynchronously.
	 * 
	 * @param boundObjectName Bound name of the {@link ManagedObject}.
	 * @param user            {@link ObjectUser} to receive the loaded object (or
	 *                        possible failure).
	 * @throws UnknownObjectException If unknown bound object name.
	 */
	<O> void load(String boundObjectName, ObjectUser<O> user) throws UnknownObjectException;

	/**
	 * Obtains the object for the {@link ManagedObject} synchronously.
	 * 
	 * @param boundObjectName       Bound name of the {@link ManagedObject}.
	 * @param timeoutInMilliseconds Time out in milliseconds to wait for the
	 *                              {@link ManagedObject} creation.
	 * @return Object.
	 * @throws UnknownObjectException If unknown bound object name.
	 * @throws Throwable              If failure in obtaining the bound object.
	 */
	<O> O getObject(String boundObjectName, long timeoutInMilliseconds) throws UnknownObjectException, Throwable;

}