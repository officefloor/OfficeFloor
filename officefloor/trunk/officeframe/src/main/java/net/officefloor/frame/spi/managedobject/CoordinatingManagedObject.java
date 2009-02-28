/*
 * Created on Jan 12, 2006
 */
package net.officefloor.frame.spi.managedobject;

/**
 * <p>
 * Provides the ability for the {@link ManagedObject} to obtain references to
 * the Objects of other {@link ManagedObject} instances.
 * <p>
 * Optionally implemented by the {@link ManagedObject} provider.
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
	 * @throws Throwable
	 *             Should this {@link CoordinatingManagedObject} fail to load
	 *             the {@link ManagedObject}.
	 */
	void loadObjects(ObjectRegistry<D> registry) throws Throwable;

}