package net.officefloor.app.subscription;

import lombok.Data;
import net.officefloor.web.HttpObject;

/**
 * Provides authentication.
 */
public class Authenticate {

	@Data
	@HttpObject
	public static class Authentication {
		String idToken;
	}

	public void service(Authentication authentication) {
		System.out.println("Authentication: " + authentication.getIdToken());
	}

}