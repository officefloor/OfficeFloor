package net.officefloor.compile.spi.section.source;

/**
 * Property of the {@link SectionSourceSpecification}.
 * 
 * @author Daniel Sagenschneider
 */
public interface SectionSourceProperty {

	/**
	 * Obtains the name of the property.
	 * 
	 * @return Name of the property.
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