package net.officefloor.frame.internal.structure;

/**
 * Factory for the creation of the {@link ManagedObjectExecuteManager}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ManagedObjectExecuteManagerFactory<F extends Enum<F>> {

	/**
	 * Creates the {@link ManagedObjectExecuteManager}.
	 * 
	 * @return {@link ManagedObjectExecuteManager}.
	 */
	ManagedObjectExecuteManager<F> createManagedObjectExecuteManager();

}