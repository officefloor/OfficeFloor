package net.officefloor.jaxrs;

import org.jvnet.hk2.annotations.Contract;

import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.managedobject.ManagedObject;

/**
 * This enables {@link OfficeFloor} to provide {@link ManagedObject} instances
 * to HK2.
 * 
 * @author Daniel Sagenschneider
 */
@Contract
public interface OfficeFloorIntoHk2Bridge {

	/**
	 * Bridges {@link OfficeFloor} into HK2.
	 * 
	 * @param dependencies {@link OfficeFloorDependencies}.
	 */
	void bridgeOfficeFloor(OfficeFloorDependencies dependencies);
}