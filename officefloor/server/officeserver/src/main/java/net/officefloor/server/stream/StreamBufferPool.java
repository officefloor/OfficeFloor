package net.officefloor.server.stream;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * Provides interface to wrap buffer pooling implementations.
 * 
 * @param <B>
 *            Type of buffer being pooled.
 * @author Daniel Sagenschneider
 */
public interface StreamBufferPool<B> {

	/**
	 * Obtains a {@link StreamBuffer}.
	 * 
	 * @return {@link StreamBuffer}.
	 */
	StreamBuffer<B> getPooledStreamBuffer();

	/**
	 * <p>
	 * Obtains an {@link StreamBuffer} that is not pooled. This is for
	 * {@link ByteBuffer} instances that are managed outside the BufferPool.
	 * <p>
	 * Typical use is to create {@link StreamBuffer} for some read-only cached
	 * content within a {@link ByteBuffer}.
	 * 
	 * @param buffer
	 *            {@link ByteBuffer}.
	 * @return {@link StreamBuffer} for the unpooled {@link ByteBuffer}.
	 */
	StreamBuffer<B> getUnpooledStreamBuffer(ByteBuffer buffer);

	/**
	 * <p>
	 * Obtains a {@link StreamBuffer} for the {@link FileChannel} content.
	 * <p>
	 * This enables efficient writing (ie DMA) of {@link FileChannel} content.
	 * <p>
	 * To write the entire {@link FileChannel} contents, invoke
	 * <code>write(file, 0, -1)</code>.
	 * <p>
	 * Note that the underlying implementation will need to support
	 * {@link FileChannel} efficiencies.
	 *
	 * @param file
	 *            {@link FileChannel}.
	 * @param position
	 *            Position within the {@link FileChannel} to start writing content.
	 *            Must be non-negative number.
	 * @param count
	 *            Count of bytes to write from the {@link FileChannel}. A negative
	 *            value (typically <code>-1</code>) indicates to write the remaining
	 *            {@link FileChannel} content from the position.
	 * @param callback
	 *            Optional {@link FileCompleteCallback}. May be <code>null</code>.
	 * @return {@link StreamBuffer} for the {@link FileChannel}.
	 * @throws IOException
	 *             If fails to create the {@link StreamBuffer} for the
	 *             {@link FileChannel}. Typically, this is because the underlying
	 *             implementation does not support DMA and copies the data from the
	 *             {@link FileChannel}.
	 */
	StreamBuffer<B> getFileStreamBuffer(FileChannel file, long position, long count, FileCompleteCallback callback)
			throws IOException;

}