package net.officefloor.servlet;

import java.util.concurrent.Executor;

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
	 * @param executor         {@link Executor}.
	 * @throws Exception If fails to service.
	 */
	void service(ServerHttpConnection connection, AsynchronousFlow asynchronousFlow, Executor executor)
			throws Exception;

}