/*-
 * #%L
 * Default OfficeFloor HTTP Server
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

package net.officefloor.server;

import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;

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
