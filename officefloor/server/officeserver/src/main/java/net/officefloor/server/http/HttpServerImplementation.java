package net.officefloor.server.http;

import java.util.ServiceLoader;

/**
 * {@link HttpServer} implementation provided from the {@link ServiceLoader}.
 * 
 * @author Daniel Sagenschneider
 */
public interface HttpServerImplementation {

	/**
	 * Configures the {@link HttpServer}.
	 * 
	 * @param context {@link HttpServerImplementationContext}.
	 * @throws Exception If fails to configure the {@link HttpServerImplementation}.
	 */
	void configureHttpServer(HttpServerImplementationContext context) throws Exception;

}