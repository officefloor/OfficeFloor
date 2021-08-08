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

import java.net.Socket;
import java.nio.ByteBuffer;

import net.officefloor.server.stream.StreamBuffer;
import net.officefloor.server.stream.StreamBufferPool;

/**
 * Writes the response.
 * 
 * @author Daniel Sagenschneider
 */
public interface ResponseWriter {

	/**
	 * Obtains the {@link StreamBufferPool}.
	 * 
	 * @return {@link StreamBufferPool}.
	 */
	StreamBufferPool<ByteBuffer> getStreamBufferPool();

	/**
	 * Executes the {@link SocketRunnable} on the {@link Socket} {@link Thread}.
	 * 
	 * @param runnable {@link SocketRunnable}.
	 */
	void execute(SocketRunnable runnable);

	/**
	 * Writes the {@link StreamBuffer} instances as the response.
	 * 
	 * @param responseHeaderWriter {@link ResponseHeaderWriter}.
	 * @param headResponseBuffer   Head {@link StreamBuffer} for the linked list of
	 *                             {@link StreamBuffer} instances for the response.
	 *                             Once the {@link StreamBuffer} is written back to
	 *                             the {@link Socket}, it is released back to its
	 *                             {@link StreamBufferPool}.
	 */
	void write(ResponseHeaderWriter responseHeaderWriter, StreamBuffer<ByteBuffer> headResponseBuffer);

	/**
	 * <p>
	 * Indicates failure processing connection.
	 * <p>
	 * Should there be an unrecoverable failure of the connection, this enables
	 * closing the connection.
	 * 
	 * @param failure Cause of the failure.
	 */
	void closeConnection(Throwable failure);

}
