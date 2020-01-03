package net.officefloor.server;

import java.nio.ByteBuffer;

import net.officefloor.server.stream.StreamBufferPool;
import net.officefloor.server.stream.impl.ThreadLocalStreamBufferPool;

/**
 * Tests the {@link SocketManager} with {@link ThreadLocalStreamBufferPool}.
 * 
 * @author Daniel Sagenschneider
 */
public class ThreadLocalPoolSocketManagerTest extends AbstractSocketManagerTestCase {

	@Override
	protected int getBufferSize() {
		return 4096;
	}

	@Override
	protected StreamBufferPool<ByteBuffer> createStreamBufferPool(int bufferSize) {
		return new ThreadLocalStreamBufferPool(() -> ByteBuffer.allocateDirect(bufferSize), 10, 10);
	}

}