package net.officefloor.tutorial.springrestmanagedobject;

import java.util.UUID;

// START SNIPPET: tutorial
public class SessionId {

	private final String id = UUID.randomUUID().toString();

	public String getId() {
		return id;
	}
}
// END SNIPPET: tutorial
