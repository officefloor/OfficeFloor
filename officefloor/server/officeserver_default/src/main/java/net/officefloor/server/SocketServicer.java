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
