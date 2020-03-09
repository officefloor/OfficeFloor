package net.officefloor.tutorial.environmenthttpserver;

import java.io.IOException;

import net.officefloor.server.http.ServerHttpConnection;

/**
 * Logic to indicate environment configuration.
 * 
 * @author Daniel Sagenschneider
 */
public class EnvironmentLogic {

	public void service(String value, ServerHttpConnection connection) throws IOException {
		connection.getResponse().getEntityWriter().write(value);
	}

}