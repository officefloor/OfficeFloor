/*-
 * #%L
 * Netty HTTP Server
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
