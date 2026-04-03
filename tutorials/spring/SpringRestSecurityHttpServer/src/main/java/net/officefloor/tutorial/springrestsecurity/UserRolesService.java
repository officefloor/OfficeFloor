package net.officefloor.tutorial.springrestsecurity;

import net.officefloor.web.ObjectResponse;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.stream.Collectors;

// START SNIPPET: tutorial
public class UserRolesService {

	public void service(@AuthenticationPrincipal UserDetails user, ObjectResponse<String> response) {
		String roles = user.getAuthorities().stream()
			.map(GrantedAuthority::getAuthority)
			.sorted()
			.collect(Collectors.joining(", "));
		response.send(roles);
	}
}
// END SNIPPET: tutorial
