package net.officefloor.frame.api.managedobject.source;

/**
 * Specification of a {@link ManagedObjectSource}. This is different to the
 * {@link ManagedObjectSourceMetaData} as it specifies how to configure the
 * {@link ManagedObjectSource} to then obtain its
 * {@link ManagedObjectSourceMetaData} based on the configuration.
 * 
 * @author Daniel Sagenschneider
 */
public interface ManagedObjectSourceSpecification {

	/**
	 * Obtains the specification of the properties for the
	 * {@link ManagedObjectSource}.
	 * 
	 * @return Property specification.
	 */
	ManagedObjectSourceProperty[] getProperties();
}
