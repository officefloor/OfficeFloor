package net.officefloor.tutorial.springrestsecurity;

import net.officefloor.web.ObjectResponse;
import org.springframework.security.access.prepost.PreAuthorize;

// START SNIPPET: tutorial
public class RolesBeanService {

	@PreAuthorize("hasRole(@roles.ADMIN)")
	public void service(ObjectResponse<String> response) {
		response.send("Admin access via @PreAuthorize SpEL bean reference");
	}
}
// END SNIPPET: tutorial
