package net.officefloor.tutorial.authenticationhttpserver;

import java.io.IOException;

import lombok.Data;
import net.officefloor.server.http.HttpStatus;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.web.security.HttpAccess;
import net.officefloor.web.security.HttpAccessControl;
import net.officefloor.web.security.HttpAuthentication;

/**
 * Logic for <code>hello</code> page.
 *
 * @author Daniel Sagenschneider
 */
// START SNIPPET: tutorial
public class HelloLogic {

	@Data
	public static class TemplateData {

		private final String username;

	}

	@HttpAccess
	public TemplateData getTemplateData(HttpAccessControl accessControl) {
		String username = accessControl.getPrincipal().getName();
		return new TemplateData(username);
	}

	public void logout(HttpAuthentication<?> authentication, ServerHttpConnection connection) throws IOException {
		authentication.logout(null);
		connection.getResponse().setStatus(HttpStatus.SEE_OTHER);
		connection.getResponse().getHeaders().addHeader("location", "/logout");
	}

}
// END SNIPPET: tutorial
