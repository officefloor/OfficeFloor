package net.officefloor.servlet;

import javax.servlet.AsyncContext;
import javax.servlet.Servlet;

import net.officefloor.frame.api.function.AsynchronousFlow;
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
	 * @param connection       {@link ServerHttpConnection}.
	 * @param asynchronousFlow {@link AsynchronousFlow} to allow for
	 *                         {@link AsyncContext}.
	 * @throws Exception If fails to service.
	 */
	void service(ServerHttpConnection connection, AsynchronousFlow asynchronousFlow) throws Exception;
}