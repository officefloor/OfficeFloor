package net.officefloor.tutorial.springrestsecurity;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import net.officefloor.web.ObjectResponse;

// START SNIPPET: tutorial
public class SecurityBeanService {

	public void service(UserDetailsService userDetailsService, ObjectResponse<String> response) {
		UserDetails user = userDetailsService.loadUserByUsername("user");
		response.send("Loaded: " + user.getUsername());
	}
}
// END SNIPPET: tutorial
