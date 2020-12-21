/*-
 * #%L
 * Default OfficeFloor HTTP Server
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package net.officefloor.server;

import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;

import net.officefloor.server.stream.ServerMemoryOverloadHandler;
import net.officefloor.server.stream.StreamBuffer;
import net.officefloor.server.stream.StreamBufferPool;

/**
 * Handles requests.
 * 
 * @author Daniel Sagenschneider
 */
public interface RequestHandler<R> {

	/**
	 * <p>
	 * Indicates if reading {@link Socket} input from client.
	 * <p>
	 * To avoid out of memory, the reading from the {@link Socket} may be halted
	 * temporarily. This indicates if actively reading input from the
	 * {@link Socket}.
	 * 
	 * @return <code>true</code> if reading {@link Socket} input from client.
	 */
	boolean isReadingInput();

	/**
	 * Obtains the {@link StreamBufferPool} for thsi {@link RequestHandler}.
	 * 
	 * @return {@link StreamBufferPool} for thsi {@link RequestHandler}.
	 */
	StreamBufferPool<ByteBuffer> getStreamBufferPool();

	/**
	 * Obtains the {@link ServerMemoryOverloadHandler}.
	 * 
	 * @return {@link ServerMemoryOverloadHandler}.
	 */
	ServerMemoryOverloadHandler getServerMemoryOverloadHandler();

	/**
	 * Executes the {@link SocketRunnable} on the {@link Socket} {@link Thread}.
	 * 
	 * @param runnable {@link SocketRunnable}.
	 */
	void execute(SocketRunnable runnable);

	/**
	 * <p>
	 * Handles a request.
	 * <p>
	 * This may only be invoked by the {@link Socket} {@link Thread}.
	 * 
	 * @param request Request.
	 * @throws IOException           If fails to handle the request.
	 * @throws IllegalStateException If invoked from another {@link Thread}.
	 */
	void handleRequest(R request) throws IOException, IllegalStateException;

	/**
	 * <p>
	 * Sends data immediately.
	 * <p>
	 * This may only be invoked by the {@link Socket} {@link Thread}.
	 * 
	 * @param immediateHead Head {@link StreamBuffer} to linked list of
	 *                      {@link StreamBuffer} instances of data to send
	 *                      immediately.
	 * @throws IllegalStateException If invoked from another {@link Thread}.
	 */
	void sendImmediateData(StreamBuffer<ByteBuffer> immediateHead) throws IllegalStateException;

	/**
	 * Allows to close connection.
	 * 
	 * @param exception Optional {@link Exception} for the cause of closing the
	 *                  connection. <code>null</code> to indicate normal close.
	 */
	void closeConnection(Throwable exception);

}
