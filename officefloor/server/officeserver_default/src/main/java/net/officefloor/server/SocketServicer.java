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

import net.officefloor.server.stream.StreamBuffer;
import net.officefloor.server.stream.StreamBufferPool;

/**
 * Services the {@link Socket}.
 * 
 * @author Daniel Sagenschneider
 */
public interface SocketServicer<R> {

	/**
	 * Services the {@link Socket}.
	 * 
	 * @param readBuffer  {@link StreamBuffer} containing the just read bytes. Note
	 *                    that this could be the same {@link StreamBuffer} as
	 *                    previous, with just further bytes written.
	 * @param bytesRead   Number of bytes read.
	 * @param isNewBuffer Indicates if the {@link StreamBuffer} is a new
	 *                    {@link StreamBuffer}. Due to {@link StreamBufferPool}, the
	 *                    same {@link StreamBuffer} may be re-used. This flag
	 *                    indicates if a new {@link StreamBuffer} with new data,
	 *                    even if same {@link StreamBuffer} instance.
	 * @throws IOException If fails to service the {@link Socket}. This indicates
	 *                     failure in servicing the connection and hence will close
	 *                     the connection.
	 */
	void service(StreamBuffer<ByteBuffer> readBuffer, long bytesRead, boolean isNewBuffer) throws IOException;

	/**
	 * Releases this {@link SocketServicer} from use.
	 */
	default void release() {
	}

}
