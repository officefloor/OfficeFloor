package net.officefloor.servlet;

import javax.servlet.Servlet;

import net.officefloor.server.http.ServerHttpConnection;

/**
 * Services {@link ServerHttpConnection} via {@link Servlet}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ServletServicer {

	/**
	 * Services the {@link ServerHttpConnection}.
	 * 
	 * @param connection {@link ServerHttpConnection}.
	 * @throws Exception If fails to service.
	 */
	void service(ServerHttpConnection connection) throws Exception;
}