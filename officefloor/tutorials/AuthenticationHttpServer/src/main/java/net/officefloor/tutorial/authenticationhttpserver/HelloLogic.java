package net.officefloor.tutorial.authenticationhttpserver;

import lombok.Data;
import net.officefloor.plugin.section.clazz.Next;
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

	@Next("LoggedOut")
	public void logout(HttpAuthentication<?> authentication) {
		authentication.logout(null);
	}

}
// END SNIPPET: tutorial