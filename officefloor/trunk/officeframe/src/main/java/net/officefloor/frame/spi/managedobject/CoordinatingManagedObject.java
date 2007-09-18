/*
 * Created on Jan 12, 2006
 */
package net.officefloor.frame.spi.managedobject;

/**
 * <p>
 * Provides the ability for the
 * {@link net.officefloor.frame.spi.managedobject.ManagedObject} to obtain
 * references to the Objects of other
 * {@link net.officefloor.frame.spi.managedobject.ManagedObject} instances.
 * <p>
 * Optionally implemented by the
 * {@link net.officefloor.frame.spi.managedobject.ManagedObject} provider.
 * 
 * @author Daniel
 */
public interface CoordinatingManagedObject<D extends Enum<D>> {

	/**
	 * <p>
	 * Loads the Objects of the {@link ManagedObject} instances to be referenced
	 * by this {@link CoordinatingManagedObject}.
	 * <p>
	 * References to the loaded Objects must be released on recycling the
	 * {@link ManagedObject}.
	 * 
	 * @param registry
	 *            Registry of the Objects for the {@link ManagedObject}
	 *            instances.
	 * @throws Exception
	 *             Should this {@link CoordinatingManagedObject} fail to load
	 *             the {@link ManagedObject}.
	 */
	void loadObjects(ObjectRegistry<D> registry) throws Exception;

}