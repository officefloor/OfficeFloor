package net.officefloor.server;

/**
 * Factory to create a {@link SocketServicer}.
 * 
 * @author Daniel Sagenschneider
 */
public interface SocketServicerFactory<R> {

	/**
	 * Creates the {@link SocketServicer}.
	 * 
	 * @param requestHandler
	 *            {@link RequestHandler}.
	 * @return {@link SocketServicer}.
	 */
	SocketServicer<R> createSocketServicer(RequestHandler<R> requestHandler);

}