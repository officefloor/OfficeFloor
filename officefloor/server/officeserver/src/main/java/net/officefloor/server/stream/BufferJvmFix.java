package net.officefloor.server.stream;

import java.nio.Buffer;
import java.nio.ByteBuffer;

/**
 * Fix for compatibility issue between JDK8 and JDK9.
 * 
 * @author Daniel Sagenschneider
 */
public class BufferJvmFix {

	/**
	 * Handles difference in flip.
	 * 
	 * @param buffer {@link ByteBuffer}.
	 * @return Result of flip.
	 */
	public static Buffer flip(Buffer buffer) {
		return buffer.flip();
	}

	/**
	 * Handle difference in clear.
	 * 
	 * @param buffer {@link ByteBuffer}.
	 * @return Result of clear.
	 */
	public static Buffer clear(Buffer buffer) {
		return buffer.clear();
	}

	/**
	 * All access via static methods.
	 */
	private BufferJvmFix() {
	}
}