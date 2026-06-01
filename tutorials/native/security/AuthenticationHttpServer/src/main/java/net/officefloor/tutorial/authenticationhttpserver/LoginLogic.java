package net.officefloor.tutorial.authenticationhttpserver;

import java.io.Serializable;

import lombok.Data;
import net.officefloor.web.HttpParameters;
import net.officefloor.web.security.HttpAuthentication;
import net.officefloor.web.security.HttpCredentials;
import net.officefloor.web.security.scheme.HttpCredentialsImpl;

/**
 * Logic for <code>login</code> page.
 *
 * @author Daniel Sagenschneider
 */
// START SNIPPET: tutorial
public class LoginLogic {

	@Data
	@HttpParameters
	public static class Form implements Serializable {
		private static final long serialVersionUID = 1L;

		private String username;

		private String password;
	}

	public void login(Form form, HttpAuthentication<HttpCredentials> authentication) {
		authentication.authenticate(new HttpCredentialsImpl(form.getUsername(), form.getPassword()), null);
	}

}
// END SNIPPET: tutorial
