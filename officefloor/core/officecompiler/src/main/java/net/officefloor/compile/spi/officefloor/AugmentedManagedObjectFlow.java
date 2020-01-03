package net.officefloor.compile.spi.officefloor;

import net.officefloor.compile.spi.managedobject.ManagedObjectFlow;

/**
 * Augmented {@link ManagedObjectFlow}.
 * 
 * @author Daniel Sagenschneider
 */
public interface AugmentedManagedObjectFlow {

	/**
	 * Obtains the name of this {@link ManagedObjectFlow}.
	 * 
	 * @return Name of this {@link ManagedObjectFlow}.
	 */
	String getManagedObjectFlowName();

	/**
	 * Indicates if the {@link ManagedObjectFlow} is already linked.
	 * 
	 * @return <code>true</code> if already linked.
	 */
	boolean isLinked();

}