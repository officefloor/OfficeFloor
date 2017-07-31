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
package net.officefloor.server.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import net.officefloor.server.http.protocol.Connection;

/**
 * Provides simple client server communication tests.
 * 
 * @author Daniel Sagenschneider
 */
public class SimpleClientServerTest extends SocketAccepterListenerTestCase {

	/**
	 * Ensure can close the {@link Connection}.
	 */
	public void testServerCloseConnection() throws IOException {
		this.getServerSideConnection().close();
		this.runServerSelect(); // Do close action
		this.assertClientReceivedData((byte[]) null); // Allow to be closed
		assertFalse("Client output should be shutdown", this.getClientChannel()
				.socket().isOutputShutdown());
		assertFalse("Client input should be shutdown", this.getClientChannel()
				.socket().isInputShutdown());
	}

	/**
	 * Ensure appropriately detects client closed {@link Connection}.
	 */
	public void testClientCloseConnection() throws IOException {
		this.closeClient();
		this.runServerSelect();
		assertTrue("Server connection should be closed", this
				.getServerSideConnection().isClosed());
	}

	/**
	 * Ensure can write data to the server.
	 */
	public void testWriteToServer() {
		this.writeDataFromClientToServer("TEST");
		this.runClientSelect();
		this.assertServerReceivedData("TEST");
	}

	/**
	 * Ensure can read data from the server.
	 */
	public void testReadFromServer() throws IOException {
		this.writeDataFromServerToClient("TEST");
		this.runServerSelect();
		this.assertClientReceivedData("TEST");
	}

	/**
	 * Ensure can read cached {@link ByteBuffer} from server.
	 */
	public void testReadDirectBufferFromServer() throws IOException {
		ByteBuffer buffer = ByteBuffer.wrap("TEST".getBytes());
		this.writeDataFromServerToClient(buffer);
		this.runServerSelect();
		this.assertClientReceivedData("TEST");
	}

	/**
	 * Ensure server can echo data sent by the client back to the client.
	 */
	public void testEcho() throws IOException {

		// Send to server
		this.writeDataFromClientToServer("TEST");
		this.runClientSelect();
		this.assertServerReceivedData("TEST");

		// Send to client
		this.writeDataFromServerToClient("TEST");
		this.runServerSelect();
		this.assertClientReceivedData("TEST");
	}

	/**
	 * Read content from server larger than socket buffer size.
	 */
	public void testReadLargerThanBufferSizeFromServer() throws Exception {
		this.doReadLargerThanBufferSizeTest(false);
	}

	/**
	 * Read content from server larger than socket buffer size which is followed
	 * immediately by a server close of the connection.
	 */
	public void testReadLargerThanBufferSizeFromServerFollowedByClose()
			throws Exception {
		this.doReadLargerThanBufferSizeTest(true);
	}

	/**
	 * Undertakes the read larger than buffer size test.
	 * 
	 * @param isCloseConnection
	 *            If should close {@link Connection} after sending the data.
	 */
	private void doReadLargerThanBufferSizeTest(boolean isCloseConnection)
			throws Exception {

		final int ITERATION_COUNT = 100;

		// Provide more data than available socket buffer size
		int sendBufferSize = this.getSendBufferSize();
		byte[] data = new byte[sendBufferSize];
		for (int i = 0; i < data.length; i++) {
			data[i] = 1;
		}
		int writtenDataSize = 0;
		for (int i = 0; i < ITERATION_COUNT; i++) {
			this.writeDataFromServerToClient(data);
			writtenDataSize += data.length;
		}

		// Close connection immediately
		if (isCloseConnection) {
			this.getServerSideConnection().close();
		}

		// Send and receive all the data
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		long startTime = System.currentTimeMillis();
		while (outputStream.size() < writtenDataSize) {

			// Attempt to read data
			int currentSize = outputStream.size();
			this.writeClientReceivedData(outputStream);
			if (currentSize == outputStream.size()) {
				// Need to run server select to send further data
				this.runServerSelect();
			}

			// Ensure has not taken too long
			if ((System.currentTimeMillis() - startTime) > 30000) {
				fail("Receive should not take more than 30 seconds");
			}
		}

		// Ensure all data is correct
		byte[] receivedData = outputStream.toByteArray();
		assertEquals("Incorrect amount of received data",
				(sendBufferSize * ITERATION_COUNT), receivedData.length);
		for (int i = 0; i < receivedData.length; i++) {
			assertEquals("Incorrect data value for index " + i, 1,
					receivedData[i]);
		}

		// Run select to close connection
		if (isCloseConnection) {
			// Ensure end of stream for channel
			this.assertClientReceivedData((byte[]) null);

		} else {
			// Should still be open channel
			this.assertClientReceivedData(new byte[0]);
		}
	}

}