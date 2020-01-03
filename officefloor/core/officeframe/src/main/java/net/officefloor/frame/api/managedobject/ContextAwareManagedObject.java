package net.officefloor.frame.api.managedobject;

/**
 * Context aware {@link ManagedObject}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ContextAwareManagedObject extends ManagedObject {

	/**
	 * Provides the {@link ManagedObjectContext} to the {@link ManagedObject}.
	 * 
	 * @param context {@link ManagedObjectContext}.
	 */
	void setManagedObjectContext(ManagedObjectContext context);
}