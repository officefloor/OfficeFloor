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
	 * Obtains the dependency {@link Object} for the dependency key.
	 * 
	 * @param key
	 *            Key identifying the dependency {@link Object}.
	 * @return Dependency {@link Object} for the key.
	 */
	Object getObject(D key);

	/**
	 * <p>
	 * Obtains the dependency {@link Object} by its index.
	 * <p>
	 * This enables a dynamic number of dependencies for the
	 * {@link ManagedObject}.
	 * 
	 * @param index
	 *            Index identifying the dependency {@link Object}.
	 * @return Dependency {@link Object} for the index.
	 */
	Object getObject(int index);

}