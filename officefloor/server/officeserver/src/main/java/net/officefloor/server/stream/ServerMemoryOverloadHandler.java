package net.officefloor.server.stream;

/**
 * Handler on server memory overload.
 * 
 * @author Daniel Sagenschneider
 */
public interface ServerMemoryOverloadHandler {

	/**
	 * Handles the server memory overload.
	 */
	void handleServerMemoryOverload();
}