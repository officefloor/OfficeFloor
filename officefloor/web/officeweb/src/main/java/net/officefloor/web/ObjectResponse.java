package net.officefloor.web;

import net.officefloor.server.http.HttpException;

/**
 * Dependency injected interface to send the {@link Object} response.
 * 
 * @author Daniel Sagenschneider
 */
public interface ObjectResponse<T> {

	/**
	 * Sends the {@link Object}.
	 * 
	 * @param object
	 *            {@link Object} to send as response.
	 * @throws HttpException
	 *             If fails to send the {@link Object}.
	 */
	void send(T object) throws HttpException;

}