package net.officefloor.server.stream;

import java.nio.ByteBuffer;

/**
 * Factory to create {@link ByteBuffer}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ByteBufferFactory {

	/**
	 * Creates a new {@link ByteBuffer}.
	 * 
	 * @return {@link ByteBuffer}.
	 */
	ByteBuffer createByteBuffer();

}