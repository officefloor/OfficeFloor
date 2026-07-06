package net.officefloor.tutorial.springhttpserver;

import net.officefloor.frame.api.manage.OfficeFloor;

// START SNIPPET: tutorial
public class World implements Other {

	@Override
	public String getName() {
		return "OfficeFloor";
	}
}
// END SNIPPET: tutorial