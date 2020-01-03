package net.officefloor.compile.spi.managedfunction.source;

/**
 * Provides the specification of the {@link ManagedFunctionSource}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ManagedFunctionSourceSpecification {

	/**
	 * Obtains the specification of the properties for the
	 * {@link ManagedFunctionSource}.
	 * 
	 * @return Property specification.
	 */
	ManagedFunctionSourceProperty[] getProperties();

}