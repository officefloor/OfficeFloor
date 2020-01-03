package net.officefloor.server;

import java.net.Socket;

/**
 * Decorates the accepted {@link Socket} connections.
 * 
 * @author Daniel Sagenschneider
 */
public interface AcceptedSocketDecorator {

	/**
	 * Decorates the accepted {@link Socket} connections.
	 * 
	 * @param socket
	 *            Accepted {@link Socket}.
	 */
	void decorate(Socket socket);

}