package net.officefloor.tutorial.springrestsecurity;

import org.springframework.stereotype.Component;

// START SNIPPET: tutorial
@Component("roles")
public class Roles {
	public final String ADMIN = "ADMIN";
	public final String USER  = "USER";
}
// END SNIPPET: tutorial
