package net.officefloor.server;

import java.net.Socket;

/**
 * {@link Runnable} to run on the {@link Socket} {@link Thread}.
 * 
 * @author Daniel Sagenschneider
 */
public interface SocketRunnable {

	/**
	 * Runs the execution.
	 * 
	 * @throws Throwable If execution fails.
	 */
	void run() throws Throwable;

}