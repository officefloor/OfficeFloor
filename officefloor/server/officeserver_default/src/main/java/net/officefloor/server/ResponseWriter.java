package net.officefloor.server;

import java.net.Socket;
import java.nio.ByteBuffer;

import net.officefloor.server.stream.StreamBufferPool;
import net.officefloor.server.stream.StreamBuffer;

/**
 * Writes the response.
 * 
 * @author Daniel Sagenschneider
 */
public interface ResponseWriter {

	/**
	 * Writes the {@link StreamBuffer} instances as the response.
	 * 
	 * @param responseHeaderWriter
	 *            {@link ResponseHeaderWriter}.
	 * @param headResponseBuffer
	 *            Head {@link StreamBuffer} for the linked list of
	 *            {@link StreamBuffer} instances for the response. Once the
	 *            {@link StreamBuffer} is written back to the {@link Socket}, it
	 *            is released back to its {@link StreamBufferPool}.
	 */
	void write(ResponseHeaderWriter responseHeaderWriter, StreamBuffer<ByteBuffer> headResponseBuffer);

}