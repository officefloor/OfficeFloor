package net.officefloor.tutorial.springrestsecurity;

import org.springframework.security.core.Authentication;

import net.officefloor.web.ObjectResponse;

// START SNIPPET: tutorial
public class AuthenticationService {

	public void service(Authentication authentication, ObjectResponse<String> response) {
		response.send("Authenticated as: " + authentication.getName());
	}
}
// END SNIPPET: tutorial
