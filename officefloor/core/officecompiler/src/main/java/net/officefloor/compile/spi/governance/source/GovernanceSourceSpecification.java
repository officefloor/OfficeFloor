package net.officefloor.compile.spi.governance.source;


/**
 * Specification of a {@link GovernanceSource}. This is different to the
 * {@link GovernanceSourceMetaData} as it specifies how to configure the
 * {@link GovernanceSource} to then obtain its
 * {@link GovernanceSource} based on the configuration.
 * 
 * @author Daniel Sagenschneider
 */
public interface GovernanceSourceSpecification {

	/**
	 * Obtains the specification of the properties for the
	 * {@link GovernanceSource}.
	 * 
	 * @return Property specification.
	 */
	GovernanceSourceProperty[] getProperties();

}