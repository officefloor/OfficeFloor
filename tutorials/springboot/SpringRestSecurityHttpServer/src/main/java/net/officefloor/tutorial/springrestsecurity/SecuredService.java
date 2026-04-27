package net.officefloor.tutorial.springrestsecurity;

import org.springframework.security.access.annotation.Secured;

import net.officefloor.web.ObjectResponse;

// START SNIPPET: tutorial
public class SecuredService {

	@Secured("ROLE_ADMIN")
	public void service(ObjectResponse<String> response) {
		response.send("Admin access via @Secured");
	}
}
// END SNIPPET: tutorial
