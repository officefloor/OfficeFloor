package net.officefloor.compile.spi.administration.source;


/**
 * Specification of a {@link AdministrationSource}. This is different to the
 * {@link AdministrationSourceMetaData} as it specifies how to configure the
 * {@link AdministrationSource} to then obtain its
 * {@link AdministrationSourceMetaData} based on the configuration.
 * 
 * @author Daniel Sagenschneider
 */
public interface AdministrationSourceSpecification {

	/**
	 * Obtains the specification of the properties for the
	 * {@link AdministrationSource}.
	 * 
	 * @return Property specification.
	 */
	AdministrationSourceProperty[] getProperties();
}
