package net.officefloor.server;

import java.nio.ByteBuffer;

import net.officefloor.server.stream.StreamBuffer;
import net.officefloor.server.stream.StreamBufferPool;

/**
 * Provides means to write header content before the response
 * {@link StreamBuffer} instances.
 * 
 * @author Daniel Sagenschneider
 */
public interface ResponseHeaderWriter {

	/**
	 * Writes the header content.
	 * 
	 * @param head
	 *            Head {@link StreamBuffer} to the linked list of
	 *            {@link StreamBuffer} instances to write the response.
	 * @param bufferPool
	 *            {@link StreamBufferPool}.
	 */
	void write(StreamBuffer<ByteBuffer> head, StreamBufferPool<ByteBuffer> bufferPool);

}