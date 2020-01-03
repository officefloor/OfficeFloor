package net.officefloor.frame.api.team.source;

/**
 * Specification of a {@link TeamSource}.
 * 
 * @author Daniel Sagenschneider
 */
public interface TeamSourceSpecification {

	/**
	 * Obtains the specification of the properties for the {@link TeamSource}.
	 * 
	 * @return Property specification.
	 */
	TeamSourceProperty[] getProperties();
}
