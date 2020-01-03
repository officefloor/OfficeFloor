package net.officefloor.frame.api.managedobject.source.impl;

import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.api.managedobject.source.ManagedObjectUser;

/**
 * <p>
 * Abstract {@link ManagedObjectSource} that allows to synchronously source the
 * {@link ManagedObject}.
 * <p>
 * For asynchronous sourcing of a {@link ManagedObject} use
 * {@link AbstractAsyncManagedObjectSource}.
 * 
 * @see AbstractAsyncManagedObjectSource
 * 
 * @author Daniel Sagenschneider
 */
public abstract class AbstractManagedObjectSource<O extends Enum<O>, F extends Enum<F>>
		extends AbstractAsyncManagedObjectSource<O, F> {

	/*
	 * ============= ManagedObjectSource ===================================
	 */

	@Override
	public void sourceManagedObject(ManagedObjectUser user) {
		try {
			// Obtain the managed object
			ManagedObject managedObject = this.getManagedObject();

			// Provide the managed object to the user
			user.setManagedObject(managedObject);

		} catch (Throwable ex) {
			// Flag error in retrieving
			user.setFailure(ex);
		}
	}

	/**
	 * Synchronously obtains the {@link ManagedObject}.
	 * 
	 * @return {@link ManagedObject}.
	 * @throws Throwable
	 *             If fails to obtain the {@link ManagedObject}.
	 */
	protected abstract ManagedObject getManagedObject() throws Throwable;

}