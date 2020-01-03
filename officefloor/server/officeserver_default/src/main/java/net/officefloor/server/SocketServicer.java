package net.officefloor.server;

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
	 */
	void service(StreamBuffer<ByteBuffer> readBuffer, long bytesRead, boolean isNewBuffer);

	/**
	 * Releases this {@link SocketServicer} from use.
	 */
	default void release() {
	}

}