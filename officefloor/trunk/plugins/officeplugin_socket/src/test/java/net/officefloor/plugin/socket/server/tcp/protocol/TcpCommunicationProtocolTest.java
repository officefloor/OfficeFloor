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

package net.officefloor.plugin.socket.server.tcp.protocol;

import java.io.IOException;
import java.io.OutputStream;

import net.officefloor.frame.api.escalate.EscalationHandler;
import net.officefloor.frame.internal.structure.ProcessState;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.plugin.socket.server.impl.AbstractClientServerTestCase;
import net.officefloor.plugin.socket.server.protocol.CommunicationProtocol;
import net.officefloor.plugin.socket.server.protocol.Connection;
import net.officefloor.plugin.socket.server.tcp.ServerTcpConnection;

/**
 * Tests the {@link TcpConnectionHandler}.
 * 
 * @author Daniel Sagenschneider
 */
public class TcpCommunicationProtocolTest extends AbstractClientServerTestCase {

	/**
	 * {@link ServerTcpConnection}.
	 */
	private ServerTcpConnection serverTcpConnection;

	/**
	 * {@link Connection}.
	 */
	private Connection rawConnection;

	/**
	 * Indicates if he {@link ProcessState} has be invoked to service the
	 * {@link ServerTcpConnection}.
	 */
	private boolean isProcessInvoked = false;

	/*
	 * ================== AbstractClientServerTestCase =========================
	 */

	@Override
	protected CommunicationProtocol createCommunicationProtocol() {
		return new TcpCommunicationProtocol() {
			@Override
			public TcpConnectionHandler createConnectionHandler(
					Connection connection) {
				// Create connection handler and allow access by test
				TcpConnectionHandler connectionHandler = super
						.createConnectionHandler(connection);
				TcpCommunicationProtocolTest.this.serverTcpConnection = connectionHandler;
				TcpCommunicationProtocolTest.this.rawConnection = connection;
				return connectionHandler;
			}
		};
	}

	@Override
	protected void handleInvokeProcess(ManagedObject managedObject,
			EscalationHandler escalationHandler) {
		assertSame("Incorrect managed object", this.serverTcpConnection,
				managedObject);
		this.isProcessInvoked = true;
	}

	/**
	 * Ensures can read from {@link ServerTcpConnection}.
	 */
	public void testReadFromConnection() throws Exception {
		final String TEXT = "test message";
		this.writeDataFromClientToServer(TEXT);
		this.runClientSelect();
		this.runServerSelect();
		assertTrue("Process should be invoked to service connection",
				this.isProcessInvoked);
		this.assertServerTcpConnectionRead(TEXT);
	}

	/**
	 * Ensures can write to {@link ServerTcpConnection}.
	 */
	public void testWriteToConnection() throws Exception {
		final String TEXT = "test message";
		this.writeToServerTcpConnection(TEXT);
		this.runServerSelect();
		this.assertClientReceivedData(TEXT);
	}

	/**
	 * Ensures can close {@link ServerTcpConnection}.
	 */
	public void testCloseConnection() throws Exception {
		this.serverTcpConnection.getOutputStream().close();
		this.runServerSelect();
		assertTrue("Connection should be closed", this.rawConnection.isClosed());
	}

	/**
	 * Validates request response.
	 */
	public void testRequestResponse() throws Exception {
		final String REQUEST = "request";
		final String RESPONSE = "response";

		// Send request
		this.writeDataFromClientToServer(REQUEST);
		this.runClientSelect();
		this.runServerSelect();
		this.assertServerTcpConnectionRead(REQUEST);

		// Send response
		this.writeToServerTcpConnection(RESPONSE);
		this.serverTcpConnection.getOutputStream().close();
		this.runServerSelect();
		this.assertClientReceivedData(RESPONSE);
	}

	/**
	 * Ensure appropriately waits on the client data.
	 */
	public void testWaitOnClientData() {
		fail("TODO implement test to wait on the client data");
	}

	/**
	 * Validates the read on the {@link ServerTcpConnection}.
	 * 
	 * @param text
	 *            Expected text.
	 */
	protected void assertServerTcpConnectionRead(String text)
			throws IOException {

		// Obtain the buffer to read data
		byte[] buffer = new byte[text.getBytes().length];

		// Read data from the connection
		int bytesRead = this.serverTcpConnection.getInputStream().read(buffer,
				0, buffer.length);

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
	protected void writeToServerTcpConnection(String text) throws IOException {

		// Obtain the data
		byte[] data = text.getBytes();

		// Write the data to the connection
		OutputStream outputStream = this.serverTcpConnection.getOutputStream();
		outputStream.write(data, 0, data.length);
		outputStream.flush();
	}

}