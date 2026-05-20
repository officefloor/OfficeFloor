package net.officefloor.tutorial.environmenthttpserver;

import java.io.IOException;

import net.officefloor.plugin.clazz.Property;
import net.officefloor.server.http.ServerHttpConnection;

/**
 * Logic to indicate environment configuration.
 * 
 * @author Daniel Sagenschneider
 */
// START SNIPPET: tutorial
public class EnvironmentLogic {

	public void service(@Property("name") String value, ServerHttpConnection connection) throws IOException {
		connection.getResponse().getEntityWriter().write(value);
	}
}
// END SNIPPET: tutorial
