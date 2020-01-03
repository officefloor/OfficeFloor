package net.officefloor.compile.spi.section.source;

import net.officefloor.compile.section.SectionType;

/**
 * Provides the specification of the {@link SectionType} to be loaded by the
 * particular {@link SectionSource}.
 * 
 * @author Daniel Sagenschneider
 */
public interface SectionSourceSpecification {

	/**
	 * Obtains the specification of the properties for the {@link SectionType}.
	 * 
	 * @return Property specification.
	 */
	SectionSourceProperty[] getProperties();

}