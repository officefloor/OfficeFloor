package net.officefloor.server;

/**
 * Factory to create the {@link RequestServicer}.
 * 
 * @author Daniel Sagenschneider
 */
public interface RequestServicerFactory<R> {

	/**
	 * Creates the {@link RequestServicer} for the {@link SocketServicer}.
	 * 
	 * @param socketServicer
	 *            {@link SocketServicer}.
	 * @return {@link RequestServicer}.
	 */
	RequestServicer<R> createRequestServicer(SocketServicer<R> socketServicer);

}