package net.officefloor.tutorial.springrestsecurity;

import jakarta.annotation.security.RolesAllowed;

import net.officefloor.web.ObjectResponse;

// START SNIPPET: tutorial
public class RolesAllowedService {

	@RolesAllowed("ADMIN")
	public void service(ObjectResponse<String> response) {
		response.send("Admin access via @RolesAllowed");
	}
}
// END SNIPPET: tutorial
