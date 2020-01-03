package net.officefloor.compile.spi.officefloor;

import net.officefloor.compile.spi.managedobject.ManagedObjectExecutionStrategy;

/**
 * Augmented {@link ManagedObjectExecutionStrategy}.
 * 
 * @author Daniel Sagenschneider
 */
public interface AugmentedManagedObjectExecutionStrategy {

	/**
	 * Obtains the name of this {@link ManagedObjectExecutionStrategy}.
	 * 
	 * @return Name of this {@link ManagedObjectExecutionStrategy}.
	 */
	String getManagedObjectExecutionStrategyName();

	/**
	 * Indicates if the {@link ManagedObjectExecutionStrategy} is already linked.
	 * 
	 * @return <code>true</code> if already linked.
	 */
	boolean isLinked();

}