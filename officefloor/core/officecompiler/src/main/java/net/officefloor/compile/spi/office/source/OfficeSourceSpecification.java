package net.officefloor.compile.spi.office.source;

import net.officefloor.compile.office.OfficeType;

/**
 * Provides the specification of the {@link OfficeType} to be loaded by the
 * particular {@link OfficeSource}.
 * 
 * @author Daniel Sagenschneider
 */
public interface OfficeSourceSpecification {

	/**
	 * Obtains the specification of the properties for the {@link OfficeType}.
	 * 
	 * @return Property specification.
	 */
	OfficeSourceProperty[] getProperties();

}