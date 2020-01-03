package net.officefloor.compile.spi.pool.source;

/**
 * Specification of a {@link ManagedObjectPoolSource}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ManagedObjectPoolSourceSpecification {

	/**
	 * Obtains the specification of the properties for the
	 * {@link ManagedObjectPoolSource}.
	 * 
	 * @return Property specification.
	 */
	ManagedObjectPoolSourceProperty[] getProperties();

}