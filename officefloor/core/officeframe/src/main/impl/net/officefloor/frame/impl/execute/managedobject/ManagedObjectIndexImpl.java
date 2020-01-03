package net.officefloor.frame.impl.execute.managedobject;

import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.internal.structure.ManagedObjectIndex;
import net.officefloor.frame.internal.structure.ManagedObjectScope;

/**
 * {@link ManagedObjectIndex} implementation.
 *
 * @author Daniel Sagenschneider
 */
public class ManagedObjectIndexImpl implements ManagedObjectIndex {

	/**
	 * {@link ManagedObjectScope}.
	 */
	private final ManagedObjectScope managedObjectScope;

	/**
	 * Index of the {@link ManagedObject} within the {@link ManagedObjectScope}.
	 */
	private final int indexOfManagedObjectWithinScope;

	/**
	 * Initiate.
	 *
	 * @param managedObjectScope
	 *            {@link ManagedObjectScope}.
	 * @param indexOfManagedObjectWithinScope
	 *            Index of the {@link ManagedObject} within the
	 *            {@link ManagedObjectScope}.
	 */
	public ManagedObjectIndexImpl(ManagedObjectScope managedObjectScope,
			int indexOfManagedObjectWithinScope) {
		this.managedObjectScope = managedObjectScope;
		this.indexOfManagedObjectWithinScope = indexOfManagedObjectWithinScope;
	}

	/*
	 * ========================= ManagedObjectIndex ===========================
	 */

	@Override
	public ManagedObjectScope getManagedObjectScope() {
		return this.managedObjectScope;
	}

	@Override
	public int getIndexOfManagedObjectWithinScope() {
		return this.indexOfManagedObjectWithinScope;
	}

	/*
	 * ========================= Object ===========================
	 */

	@Override
	public String toString() {
		// Provide details of index
		StringBuilder text = new StringBuilder();
		text.append(this.getClass().getSimpleName());
		text.append("[");
		text.append(this.managedObjectScope.toString());
		text.append(":");
		text.append(this.indexOfManagedObjectWithinScope);
		text.append("]");
		return text.toString();
	}

}