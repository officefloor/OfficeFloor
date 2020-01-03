package net.officefloor.spring;

import net.officefloor.frame.api.manage.OfficeFloor;

/**
 * Object supplied from {@link OfficeFloor} to Spring.
 * 
 * @author Daniel Sagenschneider
 */
public interface OfficeFloorManagedObject {

	String getValue();

}