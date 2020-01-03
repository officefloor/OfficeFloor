package net.officefloor.server;

import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.function.Function;

import net.officefloor.server.stream.StreamBuffer;

/**
 * Handles requests.
 * 
 * @author Daniel Sagenschneider
 */
public interface RequestHandler<R> {

	/**
	 * {@link Function} interface to run an execution on the {@link Socket}
	 * {@link Thread}.
	 */
	public static interface Execution {

		/**
		 * Runs the execution.
		 * 
		 * @throws Throwable
		 *             If execution fails.
		 */
		void run() throws Throwable;
	}

	/**
	 * Executes the {@link Execution} on the {@link Socket} {@link Thread}.
	 * 
	 * @param execution
	 *            {@link Execution}.
	 */
	void execute(Execution execution);

	/**
	 * <p>
	 * Handles a request.
	 * <p>
	 * This may only be invoked by the {@link Socket} {@link Thread}.
	 * 
	 * @param request
	 *            Request.
	 * @throws IllegalStateException
	 *             If invoked from another {@link Thread}.
	 */
	void handleRequest(R request) throws IllegalStateException;

	/**
	 * <p>
	 * Sends data immediately.
	 * <p>
	 * This may only be invoked by the {@link Socket} {@link Thread}.
	 * 
	 * @param immediateHead
	 *            Head {@link StreamBuffer} to linked list of
	 *            {@link StreamBuffer} instances of data to send immediately.
	 * @throws IllegalStateException
	 *             If invoked from another {@link Thread}.
	 */
	void sendImmediateData(StreamBuffer<ByteBuffer> immediateHead) throws IllegalStateException;

	/**
	 * Allows to close connection.
	 * 
	 * @param exception
	 *            Optional {@link Exception} for the cause of closing the
	 *            connection. <code>null</code> to indicate normal close.
	 */
	void closeConnection(Throwable exception);

}