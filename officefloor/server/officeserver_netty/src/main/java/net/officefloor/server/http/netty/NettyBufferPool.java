/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2017 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.officefloor.server.http.netty;

import java.nio.ByteBuffer;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.FullHttpResponse;
import net.officefloor.server.stream.BufferPool;
import net.officefloor.server.stream.PooledBuffer;

/**
 * Netty {@link BufferPool}.
 * 
 * @author Daniel Sagenschneider
 */
public class NettyBufferPool implements BufferPool<ByteBuf>, PooledBuffer<ByteBuf> {

	/**
	 * {@link FullHttpResponse}.
	 */
	private final FullHttpResponse response;

	/**
	 * Instantiate.
	 * 
	 * @param response
	 *            {@link FullHttpResponse}.
	 */
	public NettyBufferPool(FullHttpResponse response) {
		this.response = response;
	}

	/*
	 * ================= BufferPool =================
	 */

	@Override
	public PooledBuffer<ByteBuf> getPooledBuffer() {
		return this;
	}

	@Override
	public PooledBuffer<ByteBuf> getReadOnlyBuffer(ByteBuffer buffer) {
		this.response.content().writeBytes(buffer);
		return this;
	}

	/*
	 * ================== PooledBuffer ==============
	 */

	@Override
	public boolean isReadOnly() {
		return false;
	}

	@Override
	public ByteBuf getBuffer() {
		return this.response.content();
	}

	@Override
	public ByteBuffer getReadOnlyByteBuffer() {
		throw new IllegalStateException(this.getClass().getSimpleName() + " is always writable");
	}

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
		// Let Netty manage
	}

}
