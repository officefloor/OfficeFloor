package net.officefloor.frame.api.managedobject.source;

/**
 * Individual property of the {@link ManagedObjectSourceSpecification}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ManagedObjectSourceProperty {

	/**
	 * Obtains name of property.
	 * 
	 * @return Name of property.
	 */
	String getName();

	/**
	 * Obtains the display name of the property. If this returns
	 * <code>null</code> then the return value of {@link #getName()} is used.
	 * 
	 * @return Display name of property.
	 */
	String getLabel();
}
