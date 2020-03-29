package net.officefloor.servlet;

import java.util.concurrent.Executor;

import javax.servlet.AsyncContext;
import javax.servlet.Filter;
import javax.servlet.FilterChain;

import net.officefloor.frame.api.function.AsynchronousFlow;
import net.officefloor.server.http.ServerHttpConnection;

/**
 * Services {@link ServerHttpConnection} via {@link Filter}.
 * 
 * @author Daniel Sagenschneider
 */
public interface FilterServicer {

	/**
	 * Services the {@link ServerHttpConnection}.
	 * 
	 * @param connection       {@link ServerHttpConnection}.
	 * @param asynchronousFlow {@link AsynchronousFlow} to allow for
	 *                         {@link AsyncContext}.
	 * @param executor         {@link Executor}.
	 * @param chain            {@link FilterChain}.
	 * @throws Exception If fails to service.
	 */
	void service(ServerHttpConnection connection, AsynchronousFlow asynchronousFlow, Executor executor,
			FilterChain chain) throws Exception;

}