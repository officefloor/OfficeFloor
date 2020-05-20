package net.officefloor.spring.jaxrs;

import net.officefloor.frame.api.manage.OfficeFloor;

/**
 * {@link OfficeFloor} dependency.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeFloorDependency {

	public String getMessage() {
		return OfficeFloor.class.getSimpleName();
	}
}