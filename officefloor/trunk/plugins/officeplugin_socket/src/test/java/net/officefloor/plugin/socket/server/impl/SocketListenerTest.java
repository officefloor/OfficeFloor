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

import java.nio.channels.Selector;

/**
 * Tests the {@link SocketListener}.
 * 
 * @author Daniel Sagenschneider
 */
public class SocketListenerTest extends AbstractWriteRead {

	/**
	 * Validates read.
	 */
	public void testRead() throws Exception {
		final String TEXT = "test message";
		this.inputFromClient(TEXT);
		this.runSocketListener();
		this.validateConnectionRead(TEXT);
	}

	/**
	 * Validates writing.
	 */
	public void testWrite() throws Exception {
		final String TEXT = "test message";
		this.connectionWrite(TEXT);
		this.runSocketListener(); // specifies writing
		this.runSocketListener(); // does the writing
		this.validateOutputToClient(TEXT);
	}

	/**
	 * Validates handles closing connection.
	 */
	public void testCloseConnection() throws Exception {
		this.flagCloseConnection();
		this.runSocketListener();
		assertFalse("Key should be cancelled", this.selectionKey.isValid());
		assertTrue("Channel should be closed", this.socketChannel.isClosed());
		assertFalse("Selector should still be open", this.selector.isClosed());
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
		this.flagCloseConnection();
		this.runSocketListener(); // specifies writing
		this.runSocketListener(); // does the writing
		this.validateOutputToClient(RESPONSE);
		assertFalse("Key should be cancelled", this.selectionKey.isValid());
		assertTrue("Channel should be closed", this.socketChannel.isClosed());
		assertFalse("Selector should still be open", this.selector.isClosed());
	}

	/**
	 * Ensure appropriately closes the {@link Selector}.
	 */
	public void testCloseSelector() {
		fail("TODO handle closing the selector");
	}

}