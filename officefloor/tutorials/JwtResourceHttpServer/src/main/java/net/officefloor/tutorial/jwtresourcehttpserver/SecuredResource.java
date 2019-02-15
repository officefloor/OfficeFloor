package net.officefloor.tutorial.jwtresourcehttpserver;

import java.io.IOException;

import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.web.security.HttpAccess;

/**
 * Secured resource.
 * 
 * @author Daniel Sagenschneider
 */
public class SecuredResource {

	@HttpAccess(ifRole = "tutorial")
	public void secure(ServerHttpConnection connection) throws IOException {
		connection.getResponse().getEntityWriter().write("Hello JWT secured World");
	}

}