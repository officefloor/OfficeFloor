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
import net.officefloor.plugin.socket.server.protocol.WriteBuffer;
import net.officefloor.plugin.stream.ByteOutputStream;
import net.officefloor.plugin.stream.WriteBufferReceiver;

/**
 * Tests the {@link ByteOutputStream}.
 * 
 * @author Daniel Sagenschneider
 */
public class ByteOutputStreamTest extends OfficeFrameTestCase implements
		WriteBufferReceiver {

	/**
	 * {@link ByteOutputStream} to test.
	 */
	private final ByteOutputStreamImpl stream = new ByteOutputStreamImpl(this,
			1024);

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
		// TODO implement WriteBufferReceiver.createWriteBuffer
		throw new UnsupportedOperationException(
				"TODO implement WriteBufferReceiver.createWriteBuffer");
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