package net.officefloor.server.stream;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

/**
 * Utility functionality for a {@link StreamBuffer}.
 * 
 * @author Daniel Sagenschneider
 */
public class StreamBufferUtil {

	/**
	 * Writes the chain of {@link StreamBuffer} content to the {@link OutputStream}.
	 * 
	 * @param headBuffer   Head {@link StreamBuffer}.
	 * @param outputStream Target {@link OutputStream}.
	 * @param bufferPool   {@link StreamBufferPool}.
	 */
	public static void write(StreamBuffer<ByteBuffer> headBuffer, OutputStream outputStream,
			StreamBufferPool<ByteBuffer> bufferPool) throws IOException {

		// Write the stream of content
		StreamBuffer<ByteBuffer> stream = headBuffer;
		while (stream != null) {
			if (stream.pooledBuffer != null) {
				// Write the pooled byte buffer
				BufferJvmFix.flip(stream.pooledBuffer);
				write(stream.pooledBuffer, outputStream);

			} else if (stream.unpooledByteBuffer != null) {
				// Write the unpooled byte buffer
				write(stream.unpooledByteBuffer, outputStream);

			} else {
				// Write the file content
				StreamBuffer<ByteBuffer> streamBuffer = bufferPool.getPooledStreamBuffer();
				boolean isWritten = false;
				try {
					ByteBuffer buffer = streamBuffer.pooledBuffer;
					long position = stream.fileBuffer.position;
					long count = stream.fileBuffer.count;
					int bytesRead;
					do {
						BufferJvmFix.clear(buffer);

						// Read bytes
						bytesRead = stream.fileBuffer.file.read(buffer, position);
						position += bytesRead;

						// Setup for bytes
						BufferJvmFix.flip(buffer);
						if (count >= 0) {
							count -= bytesRead;

							// Determine read further than necessary
							if (count < 0) {
								BufferJvmFix.limit(buffer, BufferJvmFix.limit(buffer) - (int) Math.abs(count));
								bytesRead = 0;
							}
						}

						// Write the buffer
						write(buffer, outputStream);
					} while (bytesRead > 0);

					// As here, written file
					isWritten = true;

				} finally {
					streamBuffer.release();

					// Close the file
					if (stream.fileBuffer.callback != null) {
						stream.fileBuffer.callback.complete(stream.fileBuffer.file, isWritten);
					}
				}
			}
			stream = stream.next;
		}
	}

	/**
	 * Writes the {@link ByteBuffer} to the {@link OutputStream}.
	 * 
	 * @param buffer       {@link ByteBuffer}.
	 * @param outputStream {@link OutputStream}.
	 * @throws IOException If fails to write {@link ByteBuffer} content to the
	 *                     {@link OutputStream}.
	 */
	private static final void write(ByteBuffer buffer, OutputStream outputStream) throws IOException {
		for (int position = BufferJvmFix.position(buffer); position < BufferJvmFix.limit(buffer); position++) {
			outputStream.write(buffer.get());
		}
	};

	/**
	 * All access via static methods.
	 */
	private StreamBufferUtil() {
	}

}
