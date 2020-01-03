package net.officefloor.server;

import java.net.ServerSocket;

/**
 * Decorates the {@link ServerSocket}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ServerSocketDecorator {

	/**
	 * Decorates the {@link ServerSocket}.
	 * 
	 * @param serverSocket
	 *            {@link ServerSocket} to be decorated.
	 * @return {@link ServerSocket} back log size.
	 */
	int decorate(ServerSocket serverSocket);

}