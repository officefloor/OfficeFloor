package net.officefloor.compile.spi.officefloor.source;

import net.officefloor.frame.api.manage.OfficeFloor;

/**
 * Provides the specification of the {@link OfficeFloor} to be deployed by the
 * particular {@link OfficeFloorSource}.
 * 
 * @author Daniel Sagenschneider
 */
public interface OfficeFloorSourceSpecification {

	/**
	 * Obtains the specification of the properties for the {@link OfficeFloor}.
	 * 
	 * @return Property specification.
	 */
	OfficeFloorSourceProperty[] getProperties();

}