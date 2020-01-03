package net.officefloor.web.spi.security;

/**
 * Specification of a {@link HttpSecuritySource}. This is different to the
 * {@link HttpSecuritySourceMetaData} as it specifies how to configure the
 * {@link HttpSecuritySource} to then obtain its
 * {@link HttpSecuritySourceMetaData} based on the configuration.
 * 
 * @author Daniel Sagenschneider
 */
public interface HttpSecuritySourceSpecification {

	/**
	 * Obtains the specification of the properties for the
	 * {@link HttpSecuritySource}.
	 * 
	 * @return Property specification.
	 */
	HttpSecuritySourceProperty[] getProperties();

}