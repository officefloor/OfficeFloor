package net.officefloor.tutorial.jwtresourcehttpserver;

import java.io.IOException;

import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.web.security.HttpAccess;

// START SNIPPET: tutorial
public class SecuredResource {

	@HttpAccess(ifRole = "tutorial")
	public void secure(Claims claims, ServerHttpConnection connection) throws IOException {
		connection.getResponse().getEntityWriter().write("Hello " + claims.getId());
	}

}
// END SNIPPET: tutorial