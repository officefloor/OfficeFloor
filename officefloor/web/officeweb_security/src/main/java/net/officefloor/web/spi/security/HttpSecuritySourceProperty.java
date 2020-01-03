package net.officefloor.web.spi.security;

/**
 * Individual property of the {@link HttpSecuritySourceSpecification}.
 * 
 * @author Daniel Sagenschneider
 */
public interface HttpSecuritySourceProperty {

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