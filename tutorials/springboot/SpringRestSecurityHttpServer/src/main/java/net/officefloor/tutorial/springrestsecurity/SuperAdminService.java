package net.officefloor.tutorial.springrestsecurity;

import net.officefloor.web.ObjectResponse;

// START SNIPPET: tutorial
public class SuperAdminService {

	public void service(ObjectResponse<String> response) {
		response.send("Super Admin Only");
	}
}
// END SNIPPET: tutorial
