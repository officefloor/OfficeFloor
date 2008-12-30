/*
 * Created on Nov 30, 2005
 */
package net.officefloor.frame.spi.managedobject;

/**
 * <p>
 * Registry providing the dependent Object instances for a
 * {@link CoordinatingManagedObject} instance.
 * <p>
 * This is provided by the Office Floor implementation.
 * 
 * @author Daniel
 */
public interface ObjectRegistry<D extends Enum<D>> {

	/**
	 * Obtains the Object by the dependency key.
	 * 
	 * @param key
	 *            Dependency key of the Object to obtain.
	 * @return Object from the specified {@link ManagedObject}.
	 */
	Object getObject(D key);

}