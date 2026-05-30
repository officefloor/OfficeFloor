package net.officefloor.tutorial.springhttpserver;

import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.managedobject.ManagedObject;

/**
 * {@link OfficeFloor} {@link ManagedObject} wired into Spring.
 * 
 * @author Daniel Sagenschneider
 */
// START SNIPPET: tutorial
public class World implements Other {

	@Override
	public String getName() {
		return "OfficeFloor";
	}
}
// END SNIPPET: tutorial