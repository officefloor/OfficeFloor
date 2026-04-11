package net.officefloor.tutorial.springrestsecurity;

import net.officefloor.web.ObjectResponse;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;

// START SNIPPET: tutorial
public class CurrentUserService {

	public void service(@AuthenticationPrincipal UserDetails user, ObjectResponse<String> response) {
		response.send("Hello, " + user.getUsername() + "!");
	}
}
// END SNIPPET: tutorial
