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
package net.officefloor.server.stream.impl;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.io.Writer;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.util.LinkedList;
import java.util.List;

import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.server.impl.ArrayWriteBuffer;
import net.officefloor.server.impl.BufferWriteBuffer;
import net.officefloor.server.protocol.WriteBuffer;
import net.officefloor.server.protocol.WriteBufferEnum;
import net.officefloor.server.stream.ServerInputStream;
import net.officefloor.server.stream.ServerOutputStream;
import net.officefloor.server.stream.WriteBufferReceiver;
import net.officefloor.server.stream.impl.DataWrittenException;
import net.officefloor.server.stream.impl.ServerOutputStreamImpl;

/**
 * Tests the {@link ServerOutputStream}.
 * 
 * @author Daniel Sagenschneider
 */
public class ServerOutputStreamTest extends OfficeFrameTestCase implements WriteBufferReceiver {

	/**
	 * Send buffer size.
	 */
	private final int sendBufferSize = 1024;

	/**
	 * {@link ServerOutputStream} to test.
	 */
	private final ServerOutputStreamImpl stream = new ServerOutputStreamImpl(this, this.sendBufferSize);

	/**
	 * Written {@link WriteBuffer} instances.
	 */
	private final List<WriteBuffer> writtenData = new LinkedList<WriteBuffer>();

	/**
	 * Indicates if closed {@link WriteBufferReceiver}.
	 */
	private boolean isClosed = false;

	/**
	 * Test do nothing if no data to flush.
	 */
	public void testFlushNoData() throws IOException {
		this.stream.flush();
		assertEquals("Should not write data if none", 0, this.writtenData.size());
	}

	/**
	 * Tests flushing some data.
	 */
	public void testFlushData() throws IOException {
		this.stream.write("TEST".getBytes());
		this.stream.flush();
		assertEquals("Should just be the one write buffer", 1, this.writtenData.size());
		this.assertWrittenData("TEST");
	}

	/**
	 * Ensure able to write more data than the send buffer size.
	 */
	public void testMoreWrittenDataThanSendBufferSize() throws IOException {

		final int numberOfBuffers = 10;

		// Provide send data (use bytes to validate buffer allocation)
		byte[] data = new byte[this.sendBufferSize * numberOfBuffers];
		for (int i = 0; i < data.length; i++) {
			data[i] = (byte) (i / this.sendBufferSize);
		}

		// Write the data
		this.stream.write(data);
		this.stream.flush();

		// Validate that data written
		assertEquals("Incorrect number of write buffers", numberOfBuffers, this.writtenData.size());

		// Validate the data is correct
		for (int i = 0; i < this.writtenData.size(); i++) {
			byte[] writeData = this.writtenData.get(i).getData();

			// Ensure data is correct
			for (int j = 0; j < writeData.length; j++) {
				assertEquals("Incorrect value for write buffer " + i + " (index " + j + ")", i, writeData[j]);
			}
		}
	}

	/**
	 * Ensure can clear content.
	 */
	public void testClear() throws IOException {

		// Write data (spanning multiple write buffers)
		for (int i = 0; i < this.sendBufferSize; i++) {
			this.stream.write("IGNORE".getBytes());
		}

		// Clear the data
		this.stream.clear();

		// No data should be written
		assertEquals("No data should be written", 0, this.writtenData.size());

		// Write other data and flush
		this.stream.write("OTHER".getBytes());
		this.stream.flush();

		// Confirm only the other data written
		this.assertWrittenData("OTHER");
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
		assertEquals("Should be no data received", 0, this.writtenData.size());

		// Flush data and confirm as expected
		this.stream.flush();
		assertEquals("Incorrect number of received buffers", 4, this.writtenData.size());
		WriteBuffer one = this.writtenData.get(0);
		assertEquals("Incorrect first write buffer", "ONE", new String(one.getData(), 0, one.length()));
		assertSame("Incorrect second write buffer", two, this.writtenData.get(1).getDataBuffer());
		assertSame("Incorrect third write buffer", three, this.writtenData.get(2).getDataBuffer());
		WriteBuffer four = this.writtenData.get(3);
		assertEquals("Incorrect fourth write buffer", "FOUR", new String(four.getData(), 0, four.length()));
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

	/**
	 * Ensure only allow appropriate momento.
	 */
	public void testInvalidMomento() throws IOException {

		// Ensure fails if invalid momento
		try (Closeable closeable = new ServerOutputStreamImpl(this, this.sendBufferSize,
				this.createMock(Serializable.class))) {
			fail("Should not be successful");
		} catch (IllegalArgumentException ex) {
			assertEquals("Incorrect cause", "Invalid momento for ServerOutputStream", ex.getMessage());
		}
	}

	/**
	 * Can not create state momento after flush as client now aware of state.
	 */
	public void testNoStateMomentoAfterFlush() throws IOException {

		// Flush some data
		this.stream.write("some data".getBytes());
		this.stream.flush();

		// Ensure can not export state after flush
		try {
			this.stream.exportState(null);
			fail("Should not be successful");
		} catch (DataWrittenException ex) {
			assertEquals("Incorrect cause",
					"ServerOutputStream has written data to client.  Can not create State momento.", ex.getMessage());
		}
	}

	/**
	 * Ensure can have no data for momento state.
	 */
	public void testNoDataStateMomento() {
		this.assertMomentoClonedStream(null, null);
	}

	/**
	 * Ensure can have data for momento state.
	 */
	public void testSingleBufferStateMomento() throws IOException {
		final String CONTENT = "TEST";
		this.stream.write(CONTENT.getBytes());
		this.assertMomentoClonedStream(null, CONTENT);
	}

	/**
	 * Ensure can have data for momento state.
	 */
	public void testMultipleBufferStateMomento() throws IOException {
		final String CONTENT = "TEST_";
		StringBuilder expectedContent = new StringBuilder();
		for (int i = 0; i < this.sendBufferSize; i++) {
			expectedContent.append(CONTENT);
			this.stream.write(CONTENT.getBytes());
		}
		this.assertMomentoClonedStream(null, expectedContent.toString());
	}

	/**
	 * Ensure {@link ByteBuffer} appropriately serialized for momento state.
	 */
	public void testByteBufferStateMomento() throws IOException {
		this.stream.write("ONE_".getBytes());
		this.stream.write(ByteBuffer.wrap("TWO_".getBytes()));
		this.stream.write(ByteBuffer.wrap("THREE_".getBytes()));
		this.stream.write("FOUR".getBytes());
		this.assertMomentoClonedStream(null, "ONE_TWO_THREE_FOUR");
	}

	/**
	 * Ensure can flush contents from {@link Writer} for momento state.
	 */
	public void testWriterStateMomento() throws IOException {
		Writer writer = new BufferedWriter(new OutputStreamWriter(this.stream));
		writer.write("TEST");
		this.assertMomentoClonedStream(writer, "TEST");
	}

	/**
	 * Asserts the cloning with momento.
	 * 
	 * @param writer
	 *            {@link Writer} to flush its content. May be <code>null</code>.
	 * @param expectedContent
	 *            Expected content of the cloned {@link ServerInputStream}.
	 */
	private void assertMomentoClonedStream(Writer writer, String expectedContent) {
		try {

			// Export the state (ensuring serialises)
			Serializable momento = this.stream.exportState(writer);

			// Serialise the momento
			ByteArrayOutputStream outputBuffer = new ByteArrayOutputStream();
			ObjectOutputStream output = new ObjectOutputStream(outputBuffer);
			output.writeObject(momento);
			output.flush();

			// Unserialise the momento
			ByteArrayInputStream inputBuffer = new ByteArrayInputStream(outputBuffer.toByteArray());
			ObjectInputStream input = new ObjectInputStream(inputBuffer);
			Serializable unserialisedMomento = (Serializable) input.readObject();

			// Create new output stream from momento
			ServerOutputStream clonedStream = new ServerOutputStreamImpl(this, this.sendBufferSize,
					unserialisedMomento);

			// Ensure can continue to write further data
			final String furtherContent = "_FURTHER_CONTENT";
			clonedStream.write(furtherContent.getBytes());

			// Flush contents and validate has expected data
			clonedStream.flush();

			// Validate expected data
			this.assertWrittenData((expectedContent == null ? "" : expectedContent) + furtherContent);

			// Close the cloned stream
			clonedStream.close();

		} catch (Exception ex) {
			// Should not occur so do not impose exception on tests
			throw fail(ex);
		}
	}

	/**
	 * Ensures the written data is as expected.
	 * 
	 * @param expectedData
	 *            Expected written data.
	 */
	private void assertWrittenData(String expectedData) throws IOException {

		// Extract all the bytes written
		ByteArrayOutputStream allData = new ByteArrayOutputStream();
		for (WriteBuffer buffer : this.writtenData) {
			WriteBufferEnum type = buffer.getType();
			switch (type) {
			case BYTE_ARRAY:
				allData.write(buffer.getData(), 0, buffer.length());
				break;
			case BYTE_BUFFER:
				ByteBuffer dataBuffer = buffer.getDataBuffer();
				byte[] bufferData = new byte[dataBuffer.remaining()];
				dataBuffer.get(bufferData);
				allData.write(bufferData);
				break;
			default:
				fail("Unknown buffer type " + type);
				break;
			}
		}

		// Determine whether should be data
		if (expectedData == null) {
			// Ensure no data
			assertEquals("Should be no written data", 0, allData.size());

		} else {
			// Ensure expected written data
			String actualData = new String(allData.toByteArray());
			assertEquals("Incorrect written data", expectedData, actualData);
		}
	}

	/*
	 * ========================= WriteBufferReceiver ========================
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
		for (WriteBuffer buffer : data) {
			this.writtenData.add(buffer);
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