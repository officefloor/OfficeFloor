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
package net.officefloor.plugin.stream.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

import junit.framework.TestCase;
import net.officefloor.plugin.socket.server.impl.ArrayWriteBuffer;
import net.officefloor.plugin.socket.server.impl.BufferWriteBuffer;
import net.officefloor.plugin.socket.server.protocol.WriteBuffer;
import net.officefloor.plugin.socket.server.protocol.WriteBufferEnum;
import net.officefloor.plugin.stream.ServerOutputStream;
import net.officefloor.plugin.stream.ServerWriter;
import net.officefloor.plugin.stream.WriteBufferReceiver;

/**
 * Mock {@link ServerOutputStream} for testing.
 * 
 * @author Daniel Sagenschneider
 */
public class MockServerOutputStream implements WriteBufferReceiver {

	/**
	 * Default send buffer size.
	 */
	private static final int SEND_BUFFER_SIZE = 1024;

	/**
	 * {@link ServerOutputStream}.
	 */
	private final ServerOutputStream serverOutputStream;

	/**
	 * {@link ServerWriter}.
	 */
	private final ServerWriter serverWriter;

	/**
	 * {@link ByteArrayOutputStream} containing the written bytes.
	 */
	private final ByteArrayOutputStream writtenBytes;

	/**
	 * Indicates if closed.
	 */
	private boolean isClosed = false;

	/**
	 * Initiate.
	 */
	public MockServerOutputStream() {
		this(SEND_BUFFER_SIZE);
	}

	/**
	 * Initiate.
	 * 
	 * @param sendBufferSize
	 *            Send buffer size.
	 */
	public MockServerOutputStream(int sendBufferSize) {
		this(sendBufferSize, Charset.defaultCharset());
	}

	/**
	 * Initiate.
	 * 
	 * @param charset
	 *            {@link Charset}.
	 */
	public MockServerOutputStream(Charset charset) {
		this(SEND_BUFFER_SIZE, charset);
	}

	/**
	 * Initiate.
	 * 
	 * @param sendBufferSize
	 *            Send buffer size.
	 * @param charset
	 *            {@link Charset}.
	 */
	public MockServerOutputStream(int sendBufferSize, Charset charset) {
		this.serverOutputStream = new ServerOutputStreamImpl(this,
				sendBufferSize);
		this.serverWriter = new ServerWriter(this.serverOutputStream, charset,
				this);
		this.writtenBytes = new ByteArrayOutputStream(sendBufferSize);
	}

	/**
	 * Obtains the {@link ServerOutputStream}.
	 * 
	 * @return {@link ServerOutputStream}.
	 */
	public ServerOutputStream getServerOutputStream() {
		return this.serverOutputStream;
	}

	/**
	 * Obtains the {@link ServerWriter}.
	 * 
	 * @return {@link ServerWriter}.
	 */
	public ServerWriter getServerWriter() {
		return this.serverWriter;
	}

	/**
	 * Flushes all content.
	 * 
	 * @throws IOException
	 *             If fails to flush content.
	 */
	public void flush() throws IOException {
		this.serverWriter.flush();
		this.serverOutputStream.flush();
	}

	/**
	 * Obtains the written bytes.
	 * 
	 * @return Written bytes.
	 */
	public byte[] getWrittenBytes() {
		return this.writtenBytes.toByteArray();
	}

	/*
	 * ========================== WriteBufferReceiver ==================
	 */

	@Override
	public Object getWriteLock() {
		return this;
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
		// Capture the data written
		for (WriteBuffer buffer : data) {

			WriteBufferEnum type = buffer.getType();
			switch (type) {
			case BYTE_ARRAY:
				this.writtenBytes.write(buffer.getData(), 0, buffer.length());
				break;

			case BYTE_BUFFER:
				ByteBuffer bytes = buffer.getDataBuffer();
				byte[] content = new byte[bytes.remaining()];
				bytes.get(content);
				try {
					this.writtenBytes.write(content);
				} catch (IOException ex) {
					throw new IllegalStateException("Should not occur", ex);
				}
				break;

			default:
				TestCase.fail("Unknown " + WriteBuffer.class.getSimpleName()
						+ " type: " + type);
			}

		}
	}

	@Override
	public boolean isClosed() {
		return this.isClosed;
	}

	@Override
	public void close() {
		this.isClosed = true;
	}

}