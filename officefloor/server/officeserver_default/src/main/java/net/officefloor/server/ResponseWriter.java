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
