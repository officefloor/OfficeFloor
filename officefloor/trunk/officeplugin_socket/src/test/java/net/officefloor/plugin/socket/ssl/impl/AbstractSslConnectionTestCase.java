/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2009 Daniel Sagenschneider
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
package net.officefloor.plugin.socket.ssl.impl;

import java.io.IOException;

import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.socket.server.http.HttpRequest;
import net.officefloor.plugin.socket.server.ssl.ByteArrayFactory;
import net.officefloor.plugin.socket.server.ssl.SslConnection;
import net.officefloor.plugin.socket.server.ssl.SslTaskExecutor;
import net.officefloor.plugin.stream.InputBufferStream;

/**
 * Abstract test for {@link SslConnection}.
 *
 * @author Daniel Sagenschneider
 */
public abstract class AbstractSslConnectionTestCase extends OfficeFrameTestCase
		implements ByteArrayFactory, SslTaskExecutor {

	/**
	 * Server side of the {@link SslConnection}.
	 */
	protected SslConnection server;

	/**
	 * Client side of the {@link SslConnection}.
	 */
	protected SslConnection client;

	/**
	 * Transfers the data from the client to the server and notifies the server
	 * that the data is available.
	 */
	protected abstract void transferDataFromClientToServer() throws IOException;

	/**
	 * Transfers the data from the server to the client and notifies the client
	 * that the data is available.
	 */
	protected abstract void transferDataFromServerToClient() throws IOException;

	/**
	 * Ensure able to send and receive data.
	 */
	public void testSendReceiveData() throws Exception {

		// Client sends data to the server
		final byte[] message = "TEST".getBytes();
		this.client.getOutputBufferStream().write(message);

		// Handshake
		this.handshake();

		// Ensure server received the message
		this.transferDataFromClientToServer();
		assertReceivedData("TEST", this.server.getInputBufferStream());
	}

	/**
	 * Ensure able to send request and response.
	 */
	public void testRequestResponse() throws Exception {

		// Send request triggering handshake
		this.client.getOutputBufferStream().write("REQUEST".getBytes());

		// Handshake
		this.handshake();

		// Ensure server received request
		this.transferDataFromClientToServer();
		assertReceivedData("REQUEST", this.server.getInputBufferStream());

		// Send server response
		this.server.getOutputBufferStream().write("RESPONSE".getBytes());
		this.transferDataFromServerToClient();
		assertReceivedData("RESPONSE", this.client.getInputBufferStream());
	}

	/**
	 * Ensure able to send a batch of requests and respond with batch of
	 * responses. Practical use of this test ensures that can handle multiple
	 * {@link HttpRequest} instances fired one after another on the same
	 * connection.
	 */
	public void testBatchRequestResonses() throws Exception {

		final int BATCH_REQUEST_COUNT = 10;

		// Send a batch number of requests (triggers handshake)
		for (int i = 0; i < BATCH_REQUEST_COUNT; i++) {
			this.client.getOutputBufferStream().write(
					("REQUEST-" + String.valueOf(i)).getBytes());
		}

		// Handshake
		this.handshake();

		// Server receives batch number of requests
		this.transferDataFromClientToServer();

		// Ensure receive batch requests (sending responses)
		for (int i = 0; i < BATCH_REQUEST_COUNT; i++) {
			// Ensure receive request
			assertReceivedData("REQUEST-" + String.valueOf(i), this.server
					.getInputBufferStream());

			// Send response for request
			this.server.getOutputBufferStream().write(
					("RESPONSE-" + String.valueOf(i)).getBytes());
		}

		// Client receives batch number of responses
		this.transferDataFromServerToClient();
		for (int i = 0; i < BATCH_REQUEST_COUNT; i++) {
			// Ensure receive response
			assertReceivedData("RESPONSE-" + String.valueOf(i), this.client
					.getInputBufferStream());
		}
	}

	/*
	 * ======================= Helper Methods =============================
	 */

	/**
	 * Does the handshake interaction between the client and server.
	 */
	private void handshake() throws IOException {
		this.transferDataFromClientToServer();
		this.transferDataFromServerToClient();
		this.transferDataFromClientToServer();
		this.transferDataFromServerToClient();
	}

	/**
	 * Asserts the expected data was received.
	 *
	 * @param expectedData
	 *            Expected data.
	 * @param inputBufferStream
	 *            {@link InputBufferStream} that should contain the expected
	 *            data.
	 */
	private static void assertReceivedData(String expectedData,
			InputBufferStream inputBufferStream) throws IOException {
		byte[] expectedBytes = expectedData.getBytes();
		byte[] receivedBytes = new byte[expectedBytes.length];
		assertEquals("Incorrect number of bytes received",
				expectedBytes.length, inputBufferStream.read(receivedBytes));
		String receivedData = new String(receivedBytes);
		assertEquals("Incorrect data received", expectedData, receivedData);
	}

	/*
	 * ====================== ByteArrayFactory ==============================
	 */

	/**
	 * Allow reuse of byte array.
	 */
	private byte[] byteArray = null;

	@Override
	public byte[] createByteArray(int minimumSize) {

		// Ensure have byte array of minimum size
		if ((this.byteArray == null) || (this.byteArray.length < minimumSize)) {
			this.byteArray = new byte[minimumSize];
		}

		// Return the byte array
		return this.byteArray;
	}

	/*
	 * ====================== SslTaskExecutor ===============================
	 */

	@Override
	public void beginTask(Runnable task) {
		task.run();
	}

}