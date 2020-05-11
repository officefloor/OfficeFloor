package net.officefloor.spring;

import net.officefloor.frame.api.manage.OfficeFloor;

/**
 * Object dependency supplied from {@link OfficeFloor} to Spring.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeFloorObjectDependency {

	public String getMessage() {
		return OfficeFloor.class.getSimpleName() + Object.class.getSimpleName();
	}
}