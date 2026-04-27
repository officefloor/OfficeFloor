package net.officefloor.tutorial.springrestsecurity;

import org.springframework.security.access.prepost.PreAuthorize;

import net.officefloor.web.ObjectResponse;

// START SNIPPET: tutorial
public class PreAuthorizeService {

	@PreAuthorize("hasRole('ADMIN')")
	public void service(ObjectResponse<String> response) {
		response.send("Admin access via @PreAuthorize");
	}
}
// END SNIPPET: tutorial
