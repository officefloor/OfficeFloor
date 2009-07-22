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
package net.officefloor.plugin.socket.server.tcp;

import java.io.IOException;
import java.io.OutputStream;

import net.officefloor.plugin.socket.server.impl.AbstractWriteRead;
import net.officefloor.plugin.socket.server.tcp.api.ServerTcpConnection;

/**
 * Tests the {@link TcpConnectionHandler}.
 *
 * @author Daniel Sagenschneider
 */
public class TcpConnectionHandlerTest extends AbstractWriteRead {

	/**
	 * {@link TcpConnectionHandler}.
	 */
	private TcpConnectionHandler tcpConnectionHandler = new TcpConnectionHandler(
			this.connection);

	/**
	 * {@link ServerTcpConnection} which is implemented by the
	 * {@link TcpConnectionHandler}.
	 */
	private final ServerTcpConnection serverTcpConnection = this.tcpConnectionHandler;

	/**
	 * Initiate.
	 */
	public TcpConnectionHandlerTest() {
		this.connectionHandler
				.setDelegateConnectionHandler(this.tcpConnectionHandler);
	}

	/**
	 * Ensures can read from {@link ServerTcpConnection}.
	 */
	public void testReadFromConnection() throws Exception {
		final String TEXT = "test message";
		this.inputFromClient(TEXT);
		this.runSocketListener();
		this.validateConnectionRead(TEXT);
	}

	/**
	 * Ensures can write to {@link ServerTcpConnection}.
	 */
	public void testWriteToConnection() throws Exception {
		final String TEXT = "test message";
		this.connectionWrite(TEXT);
		this.runSocketListener(); // Flag to write
		this.runSocketListener(); // Writes out
		this.validateOutputToClient(TEXT);
	}

	/**
	 * Ensures can close {@link ServerTcpConnection}.
	 */
	public void testCloseConnection() throws Exception {
		this.serverTcpConnection.close();
		this.runSocketListener();
		assertFalse("Key should be cancelled", this.selectionKey.isValid());
		assertTrue("Channel should be closed", this.socketChannel.isClosed());
	}

	/**
	 * Validates request response.
	 */
	public void testRequestResponse() throws Exception {
		final String REQUEST = "request";
		final String RESPONSE = "response";
		this.inputFromClient(REQUEST);
		this.runSocketListener();
		this.validateConnectionRead(REQUEST);
		this.connectionWrite(RESPONSE);
		this.serverTcpConnection.close();
		this.runSocketListener(); // specifies writing
		this.runSocketListener(); // does the writing
		assertFalse("Key should be cancelled", this.selectionKey.isValid());
		assertTrue("Channel should be closed", this.socketChannel.isClosed());
	}

	/**
	 * Validates the read on the {@link ServerTcpConnection}.
	 *
	 * @param text
	 *            Expected text.
	 */
	@Override
	protected void validateConnectionRead(String text) throws IOException {

		// Obtain the buffer to read data
		byte[] buffer = new byte[text.getBytes().length];

		// Read data from the connection
		int bytesRead = this.serverTcpConnection.read(buffer, 0, buffer.length);

		// Obtain text of bytes read
		String actualText = new String(buffer, 0, bytesRead);

		// Validate
		assertEquals("Incorrect number of bytes read [expected: " + text
				+ ", actual: " + actualText + "]", buffer.length, bytesRead);
		assertEquals("Incorrect bytes read", text, actualText);
	}

	/**
	 * Writes to the {@link ServerTcpConnection}.
	 *
	 * @param text
	 *            Text to write.
	 */
	@Override
	protected void connectionWrite(String text) throws IOException {

		// Obtain the data
		byte[] data = text.getBytes();

		// Write the data to the connection
		OutputStream outputStream = this.serverTcpConnection.getOutputStream();
		outputStream.write(data, 0, data.length);
	}

}