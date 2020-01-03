package net.officefloor.compile.spi.governance.source;

/**
 * Individual property of the {@link GovernanceSourceSpecification}.
 * 
 * @author Daniel Sagenschneider
 */
public interface GovernanceSourceProperty {

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