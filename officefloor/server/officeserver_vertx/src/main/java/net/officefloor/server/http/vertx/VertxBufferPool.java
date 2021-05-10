package net.officefloor.server.http.vertx;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import io.netty.buffer.ByteBuf;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.buffer.impl.BufferImpl;
import net.officefloor.server.stream.FileCompleteCallback;
import net.officefloor.server.stream.StreamBuffer;
import net.officefloor.server.stream.StreamBufferPool;

/**
 * {@link Vertx} {@link StreamBufferPool}.
 * 
 * @author Daniel Sagenschneider
 */
public class VertxBufferPool extends StreamBuffer<Buffer> implements StreamBufferPool<Buffer> {

	/**
	 * {@link ByteBuf} {@link Field} within {@link Buffer}.
	 */
	private static final Field bufferField;

	static {
		final String bufferFieldName = "buffer";
		try {
			bufferField = BufferImpl.class.getDeclaredField(bufferFieldName);
			bufferField.setAccessible(true);
		} catch (NoSuchFieldException | SecurityException ex) {
			throw new IllegalStateException(
					"Can not find field " + bufferFieldName + " on " + BufferImpl.class.getName(), ex);
		}
	}

	/**
	 * Obtains the {@link ByteBuf} backing the {@link Buffer}.
	 * 
	 * @param buffer {@link Buffer}.
	 * @return Backing {@link ByteBuf}.
	 */
	private static ByteBuf getByteBuf(Buffer buffer) {
		try {
			return (ByteBuf) bufferField.get(buffer);
		} catch (IllegalArgumentException | IllegalAccessException ex) {
			throw new IllegalStateException(
					"Failed to obtain " + ByteBuf.class.getSimpleName() + " from " + buffer.getClass().getName(), ex);
		}
	}

	/**
	 * Instantiate.
	 */
	public VertxBufferPool() {
		super(Buffer.buffer(), null, null);
	}

	/*
	 * ================== StreamBufferPool ===================
	 */

	@Override
	public StreamBuffer<Buffer> getPooledStreamBuffer() {
		return this;
	}

	@Override
	public StreamBuffer<Buffer> getUnpooledStreamBuffer(ByteBuffer buffer) {
		getByteBuf(this.pooledBuffer).writeBytes(buffer);
		return null;
	}

	@Override
	public StreamBuffer<Buffer> getFileStreamBuffer(FileChannel file, long position, long count,
			FileCompleteCallback callback) throws IOException {
		int length = (int) (count < 0 ? file.size() - position : count);
		getByteBuf(this.pooledBuffer).writeBytes(file, position, length);
		if (callback != null) {
			callback.complete(file, true);
		}
		return this;
	}

	@Override
	public void close() {
		// Nothing to close
	}

	/*
	 * ==================== StreamBuffer =====================
	 */

	@Override
	public boolean write(byte datum) {
		this.pooledBuffer.appendByte(datum);
		return true;
	}

	@Override
	public int write(byte[] data, int offset, int length) {
		this.pooledBuffer.appendBytes(data, offset, length);
		return length;
	}

	@Override
	public void release() {
		// Called on response reset, so clear content
		getByteBuf(this.pooledBuffer).clear();
	}

}