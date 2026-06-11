package net.officefloor.tutorial.springrestmanagedobject;

import net.officefloor.web.ObjectResponse;

// START SNIPPET: tutorial
public class SessionIdService {

	public void service(SessionId sessionId, ObjectResponse<String> response) {
		response.send(sessionId.getId());
	}
}
// END SNIPPET: tutorial
