package net.officefloor.frame.api.build;

import net.officefloor.frame.api.manage.OfficeFloor;

/**
 * {@link OfficeFloor} event.
 * 
 * @author Daniel Sagenschneider
 */
public interface OfficeFloorEvent {

	/**
	 * Obtains the {@link OfficeFloor}.
	 * 
	 * @return {@link OfficeFloor}.
	 */
	OfficeFloor getOfficeFloor();

}