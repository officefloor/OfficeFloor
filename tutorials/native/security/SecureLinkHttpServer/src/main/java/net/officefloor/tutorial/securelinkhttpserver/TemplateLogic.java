package net.officefloor.tutorial.securelinkhttpserver;

import java.io.Serializable;

import lombok.Data;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.web.HttpParameters;

/**
 * Logic for the template.
 * 
 * @author Daniel Sagenschneider
 */
// START SNIPPET: tutorial
public class TemplateLogic {

	@Data
	@HttpParameters
	public static class LoginParameters implements Serializable {
		private static final long serialVersionUID = 1L;

		private String username;

		private String password;
	}

	public void login(LoginParameters credentials, ServerHttpConnection connection) {

		// Confirm a secure connection (not needed but included for tutorial)
		if (!connection.isSecure()) {
			throw new IllegalStateException();
		}

		// Logic for login
	}

}
// END SNIPPET: tutorial