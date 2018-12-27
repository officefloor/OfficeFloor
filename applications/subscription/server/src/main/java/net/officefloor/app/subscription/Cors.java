package net.officefloor.app.subscription;

import net.officefloor.server.http.ServerHttpConnection;

public class Cors {

	public void options(ServerHttpConnection connection) {
		System.out.println("Connection: " + connection.getRequest().getUri());
	}

}