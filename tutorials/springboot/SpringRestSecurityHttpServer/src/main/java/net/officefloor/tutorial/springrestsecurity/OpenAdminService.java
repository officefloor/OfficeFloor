package net.officefloor.tutorial.springrestsecurity;

import net.officefloor.web.ObjectResponse;

// START SNIPPET: tutorial
public class OpenAdminService {

	public void service(ObjectResponse<String> response) {
		response.send("Open under Admin");
	}
}
// END SNIPPET: tutorial
