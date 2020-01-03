package net.officefloor.server;

import java.nio.ByteBuffer;

import net.officefloor.server.http.mock.MockStreamBufferPool;
import net.officefloor.server.stream.StreamBuffer;
import net.officefloor.server.stream.StreamBufferPool;

/**
 * Ensures all {@link StreamBuffer} instances are released.
 * 
 * @author Daniel Sagenschneider
 */
public class BufferManagementSocketManagerTest extends AbstractSocketManagerTestCase {

	@Override
	protected int getBufferSize() {
		return 1024;
	}

	@Override
	protected StreamBufferPool<ByteBuffer> createStreamBufferPool(int bufferSize) {
		return new MockStreamBufferPool(() -> ByteBuffer.allocate(bufferSize));
	}

	@Override
	protected void handleCompletion(StreamBufferPool<ByteBuffer> bufferPool) {
		((MockStreamBufferPool) bufferPool).assertAllBuffersReturned();
	}

}