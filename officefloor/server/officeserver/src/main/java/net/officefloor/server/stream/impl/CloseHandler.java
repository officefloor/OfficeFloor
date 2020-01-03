package net.officefloor.server.stream.impl;

import java.io.IOException;

import net.officefloor.server.stream.ServerOutputStream;

/**
 * Handles closing the {@link ServerOutputStream}.
 * 
 * @author Daniel Sagenschneider
 */
public interface CloseHandler {

	/**
	 * Indicates if closed.
	 * 
	 * @return <code>true</code> if closed.
	 */
	boolean isClosed();

	/**
	 * Handles the close.
	 * 
	 * @throws IOException
	 *             If fails to close.
	 */
	void close() throws IOException;

}