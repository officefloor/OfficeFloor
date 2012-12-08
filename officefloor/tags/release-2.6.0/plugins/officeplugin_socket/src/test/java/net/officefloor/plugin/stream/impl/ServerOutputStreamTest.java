/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2012 Daniel Sagenschneider
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

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;

import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.socket.server.impl.ArrayWriteBuffer;
import net.officefloor.plugin.socket.server.impl.BufferWriteBuffer;
import net.officefloor.plugin.socket.server.protocol.WriteBuffer;
import net.officefloor.plugin.stream.ServerOutputStream;
import net.officefloor.plugin.stream.WriteBufferReceiver;

/**
 * Tests the {@link ServerOutputStream}.
 * 
 * @author Daniel Sagenschneider
 */
public class ServerOutputStreamTest extends OfficeFrameTestCase implements
		WriteBufferReceiver {

	/**
	 * Send buffer size.
	 */
	private final int sendBufferSize = 1024;

	/**
	 * {@link ServerOutputStream} to test.
	 */
	private final ServerOutputStreamImpl stream = new ServerOutputStreamImpl(
			this, this.sendBufferSize);

	/**
	 * Written {@link WriteBuffer} instances.
	 */
	private WriteBuffer[] writtenData = null;

	/**
	 * Indicates if closed {@link WriteBufferReceiver}.
	 */
	private boolean isClosed = false;

	/**
	 * Test do nothing if no data to flush.
	 */
	public void testFlushNoData() throws IOException {
		this.stream.flush();
		assertNull("Should not write data if none", this.writtenData);
	}

	/**
	 * Tests flushing some data.
	 */
	public void testFlushData() throws IOException {
		this.stream.write(10);
		this.stream.flush();
		assertNotNull("Should have written data", this.writtenData);
		assertEquals("Should just be the one write buffer", 1,
				this.writtenData.length);
		WriteBuffer buffer = this.writtenData[0];
		assertEquals("Should just be one byte written", 1, buffer.length());
		assertEquals("Incorrect written byte", 10, buffer.getData()[0]);
	}

	/**
	 * Ensure able to write more data than the send buffer size.
	 */
	public void testMoreWrittenDataThanSendBufferSize() throws IOException {

		final int numberOfBuffers = 10;

		// Provide send data
		byte[] data = new byte[this.sendBufferSize * numberOfBuffers];
		for (int i = 0; i < data.length; i++) {
			data[i] = (byte) (i / this.sendBufferSize);
		}

		// Write the data
		this.stream.write(data);
		this.stream.flush();

		// Validate that data written
		assertNotNull("Data should be written", this.writtenData);
		assertEquals("Incorrect number of write buffers", numberOfBuffers,
				this.writtenData.length);

		// Validate the data is correct
		for (int i = 0; i < this.writtenData.length; i++) {
			byte[] writeData = this.writtenData[i].getData();

			// Ensure data is correct
			for (int j = 0; j < writeData.length; j++) {
				assertEquals("Incorrect value for write buffer " + i
						+ " (index " + j + ")", i, writeData[j]);
			}
		}
	}

	/**
	 * Ensure can clear content.
	 */
	public void testClear() throws IOException {

		// Write some data (spanning multiple write buffers)
		byte[] data = new byte[this.sendBufferSize * 10];
		this.stream.write(data);

		// Clear the data
		this.stream.clear();

		// No data should be written
		assertNull("No data should be written", this.writtenData);

		// Write other data and flush
		this.stream.write("TEST".getBytes());
		this.stream.flush();

		// Confirm only the other data written
		assertNotNull("Data should be written", this.writtenData);
		assertEquals("Should only be the one write buffer", 1,
				this.writtenData.length);
		WriteBuffer buffer = this.writtenData[0];
		assertEquals("Only uncleared data should be written", "TEST",
				new String(buffer.getData(), 0, buffer.length()));
	}

	/**
	 * Ensure able to write the cached {@link ByteBuffer}.
	 */
	public void testWriteCachedByteBuffer() throws IOException {

		// Write some text
		this.stream.write("ONE".getBytes());

		// Write cached buffer
		final ByteBuffer two = ByteBuffer.allocateDirect(20);
		this.stream.write(two);

		// Write another cached buffer
		final ByteBuffer three = ByteBuffer.allocateDirect(30);
		this.stream.write(three);

		// Write some further text
		this.stream.write("FOUR".getBytes());

		// Should be no data received so far
		assertNull("Should be no data received", this.writtenData);

		// Flush data and confirm as expected
		this.stream.flush();
		assertNotNull("Should have received data", this.writtenData);
		assertEquals("Incorrect number of received buffers", 4,
				this.writtenData.length);
		assertEquals("Incorrect first write buffer", "ONE", new String(
				this.writtenData[0].getData(), 0, this.writtenData[0].length()));
		assertSame("Incorrect second write buffer", two,
				this.writtenData[1].getDataBuffer());
		assertSame("Incorrect third write buffer", three,
				this.writtenData[2].getDataBuffer());
		assertEquals("Incorrect fourth write buffer", "FOUR", new String(
				this.writtenData[3].getData(), 0, this.writtenData[3].length()));
	}

	/**
	 * Tests closing.
	 */
	public void testClose() throws IOException {

		// Close
		this.stream.close();
		assertTrue("Receiver should be closed", this.isClosed);

		// Attempting to write should fail
		try {
			this.stream.write(1);
			fail("Should not successfully write");
		} catch (ClosedChannelException ex) {
			// Correct notified that closed
		}
	}

	/*
	 * ========================= WriteBufferReceiver ========================
	 */

	@Override
	public Object getLock() {
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
		this.writtenData = data;
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