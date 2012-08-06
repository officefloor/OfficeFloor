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
package net.officefloor.plugin.socket.server.impl;

import java.io.IOException;

import net.officefloor.plugin.socket.server.protocol.Connection;

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
		this.assertClientReceivedData(""); // Allow to be closed
		assertFalse("Client output should be shutdown", this.getClientChannel()
				.socket().isOutputShutdown());
		assertFalse("Client input should be shutdown", this.getClientChannel()
				.socket().isInputShutdown());
	}

	/**
	 * Ensure appropriately detects client closed {@link Connection}.
	 */
	public void testClientCloseConnection() throws IOException {
		this.getClientChannel().close();
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
	public void testReadFromServer() {
		this.writeDataFromServerToClient("TEST");
		this.runServerSelect();
		this.assertClientReceivedData("TEST");
	}

	/**
	 * Ensure server can echo data sent by the client back to the client.
	 */
	public void testEcho() {

		// Send to server
		this.writeDataFromClientToServer("TEST");
		this.runClientSelect();
		this.assertServerReceivedData("TEST");

		// Send to client
		this.writeDataFromServerToClient("TEST");
		this.runServerSelect();
		this.assertClientReceivedData("TEST");
	}

}