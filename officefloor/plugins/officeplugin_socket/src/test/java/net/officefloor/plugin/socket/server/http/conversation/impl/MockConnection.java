/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2013 Daniel Sagenschneider
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
package net.officefloor.plugin.socket.server.http.conversation.impl;

import java.io.ByteArrayOutputStream;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

import junit.framework.TestCase;

import net.officefloor.plugin.socket.server.impl.ArrayWriteBuffer;
import net.officefloor.plugin.socket.server.impl.BufferWriteBuffer;
import net.officefloor.plugin.socket.server.protocol.Connection;
import net.officefloor.plugin.socket.server.protocol.WriteBuffer;
import net.officefloor.plugin.socket.server.protocol.WriteBufferEnum;

/**
 * Mock {@link Connection} for testing.
 * 
 * @author Daniel Sagenschneider
 */
public class MockConnection implements Connection {

	/**
	 * Written bytes.
	 */
	private final ByteArrayOutputStream wire = new ByteArrayOutputStream();

	/**
	 * Indicates if closed.
	 */
	private boolean isClosed = false;

	/**
	 * Obtains the bytes written.
	 * 
	 * @return Bytes written.
	 */
	public byte[] getWrittenBytes() {
		return this.wire.toByteArray();
	}

	/**
	 * <p>
	 * Consumes the bytes written.
	 * <p>
	 * Allows checking for further data written.
	 */
	public void consumeWrittenBytes() {
		this.wire.reset();
	}

	/*
	 * ================== Connection =================================
	 */

	@Override
	public Object getWriteLock() {
		return this.wire;
	}

	@Override
	public InetSocketAddress getLocalAddress() {
		TestCase.fail("Local InetSocketAddress should not be required for conversation testing");
		return null;
	}

	@Override
	public InetSocketAddress getRemoteAddress() {
		TestCase.fail("Remote InetSocketAddress should not be required for conversation testing");
		return null;
	}

	@Override
	public boolean isSecure() {
		TestCase.fail("Determining if secure should not be required for conversation testing");
		return false;
	}

	@Override
	public WriteBuffer createWriteBuffer(byte[] data, int length) {
		return new ArrayWriteBuffer(data, length);
	}

	@Override
	public WriteBuffer createWriteBuffer(ByteBuffer buffer) {
		return new BufferWriteBuffer(buffer);
	}

	@Override
	public void writeData(WriteBuffer[] data) {

		// Write out the data
		for (WriteBuffer buffer : data) {
			WriteBufferEnum type = buffer.getType();
			switch (type) {

			case BYTE_ARRAY:
				this.wire.write(buffer.getData(), 0, buffer.length());
				break;

			case BYTE_BUFFER:
				ByteBuffer byteBuffer = buffer.getDataBuffer();
				byte[] writeData = new byte[byteBuffer.remaining()];
				byteBuffer.get(writeData);
				this.wire.write(writeData, 0, writeData.length);
				break;

			default:
				TestCase.fail("Unknown type: " + type);
			}
		}
	}

	@Override
	public void close() {
		this.isClosed = true;
	}

	@Override
	public boolean isClosed() {
		return this.isClosed;
	}

}