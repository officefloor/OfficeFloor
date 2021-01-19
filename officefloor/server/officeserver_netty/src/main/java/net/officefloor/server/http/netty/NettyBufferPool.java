/*-
 * #%L
 * Netty HTTP Server
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package net.officefloor.server.http.netty;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.FullHttpResponse;
import net.officefloor.server.stream.FileCompleteCallback;
import net.officefloor.server.stream.StreamBuffer;
import net.officefloor.server.stream.StreamBufferPool;

/**
 * Netty {@link StreamBufferPool}.
 * 
 * @author Daniel Sagenschneider
 */
public class NettyBufferPool extends StreamBuffer<ByteBuf> implements StreamBufferPool<ByteBuf> {

	/**
	 * {@link FullHttpResponse}.
	 */
	private final FullHttpResponse response;

	/**
	 * Instantiate.
	 * 
	 * @param response {@link FullHttpResponse}.
	 */
	public NettyBufferPool(FullHttpResponse response) {
		super(response.content(), null, null);
		this.response = response;
	}

	/*
	 * ============== StreamBufferPool =============
	 */

	@Override
	public StreamBuffer<ByteBuf> getPooledStreamBuffer() {
		return this;
	}

	@Override
	public StreamBuffer<ByteBuf> getUnpooledStreamBuffer(ByteBuffer buffer) {
		this.response.content().writeBytes(buffer);
		return this;
	}

	@Override
	public StreamBuffer<ByteBuf> getFileStreamBuffer(FileChannel file, long position, long count,
			FileCompleteCallback callback) throws IOException {
		int length = (int) (count < 0 ? file.size() - position : count);
		this.response.content().writeBytes(file, position, length);
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
	 * ================== StreamBuffer ==============
	 */

	@Override
	public boolean write(byte datum) {
		this.response.content().writeByte(datum);
		return true;
	}

	@Override
	public int write(byte[] data, int offset, int length) {
		this.response.content().writeBytes(data, offset, length);
		return length;
	}

	@Override
	public void release() {
		// Called on response reset, so clear content
		this.response.content().clear();
	}

}
