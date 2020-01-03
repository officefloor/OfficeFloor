package net.officefloor.frame.api.managedobject;

/**
 * <p>
 * Core interface of a Managed Object.
 * <p>
 * Additional managed functionality is available by implementing the following
 * interfaces:
 * <ol>
 * <li>{@link ContextAwareManagedObject}</li>
 * <li>{@link AsynchronousManagedObject}</li>
 * <li>{@link CoordinatingManagedObject}</li>
 * </ol>
 * 
 * @author Daniel Sagenschneider
 */
public interface ManagedObject {

	/**
	 * Obtains the object being managed.
	 * 
	 * @return Object being managed.
	 * @throws Throwable
	 *             Indicating failed to obtain the object for use.
	 */
	Object getObject() throws Throwable;

}