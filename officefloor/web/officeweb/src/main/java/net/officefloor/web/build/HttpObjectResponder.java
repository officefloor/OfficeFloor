package net.officefloor.web.build;

import java.io.IOException;

import net.officefloor.server.http.ServerHttpConnection;

/**
 * Provides ability to send an {@link Object} response.
 * 
 * @author Daniel Sagenschneider
 */
public interface HttpObjectResponder<T> {

	/**
	 * Obtains the <code>Content-Type</code> provided by this
	 * {@link HttpObjectResponder}.
	 * 
	 * @return <code>Content-Type</code> provided by this
	 *         {@link HttpObjectResponder}.
	 */
	String getContentType();

	/**
	 * Obtains the object type expected for this {@link HttpObjectResponder}.
	 * 
	 * @return Type of object expected for this {@link HttpObjectResponder}.
	 */
	Class<T> getObjectType();

	/**
	 * Sends the object.
	 * 
	 * @param object
	 *            Object to send.
	 * @param connection
	 *            {@link ServerHttpConnection}.
	 * @throws IOException
	 *             If fails to send the object.
	 */
	void send(T object, ServerHttpConnection connection) throws IOException;

}