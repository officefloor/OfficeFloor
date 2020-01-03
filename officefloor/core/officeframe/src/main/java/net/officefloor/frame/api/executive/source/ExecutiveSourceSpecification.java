package net.officefloor.frame.api.executive.source;

/**
 * Specification of a {@link ExecutiveSource}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ExecutiveSourceSpecification {

	/**
	 * Obtains the specification of the properties for the {@link ExecutiveSource}.
	 * 
	 * @return Property specification.
	 */
	ExecutiveSourceProperty[] getProperties();
}
