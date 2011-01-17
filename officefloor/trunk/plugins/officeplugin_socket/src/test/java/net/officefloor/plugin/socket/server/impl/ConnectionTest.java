/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2011 Daniel Sagenschneider
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

import net.officefloor.plugin.socket.server.Connection;
import net.officefloor.plugin.socket.server.impl.ConnectionImpl;

/**
 * Tests reading data from the {@link ConnectionImpl}.
 * 
 * @author Daniel Sagenschneider
 */
public class ConnectionTest extends AbstractWriteRead {

	/**
	 * Ensures that a plain connection is not secure.
	 */
	public void testPlainConnectionNotSecure() {
		assertFalse("Plain connection should not be secure", this.connection
				.isSecure());
	}

	/**
	 * Ensures able to read from the {@link Connection}.
	 */
	public void testReadFromConnection() throws Exception {
		final String TEXT = "test text";
		this.inputFromClient(TEXT);
		assertEquals("Incorrect bytes read", TEXT.getBytes().length,
				this.socketListener.readData(this.connection));
		this.validateConnectionRead(TEXT);
	}

	/**
	 * Ensures able to read multiple reads from the {@link Connection}.
	 */
	public void testMultipleReadFromConnection() throws Exception {
		final String TEXT = "test message";
		final int COUNT = 10000;

		// Input the data from client
		for (int i = 0; i < COUNT; i++) {
			String msg = TEXT + i;
			this.inputFromClient(msg);
		}

		// Read in data
		this.socketListener.readData(this.connection);

		// Validate read messages
		for (int i = 0; i < COUNT; i++) {
			String msg = TEXT + i;
			this.validateConnectionRead(msg);
		}
	}

	/**
	 * Ensures able to write the {@link Connection}.
	 */
	public void testWriteToConnection() throws Exception {
		final String TEXT = "test text";
		this.connectionWrite(TEXT);
		this.socketListener.writeData(this.connection);
		this.validateOutputToClient(TEXT);
	}

	/**
	 * Ensures able to write multiple write to the {@link Connection}.
	 */
	public void testMultipleWriteToConnection() throws Exception {
		final String TEXT = "test message";
		final int COUNT = 10000;

		// Write data to the client
		for (int i = 0; i < COUNT; i++) {
			String msg = TEXT + i;
			this.connectionWrite(msg);
		}

		// Write out data
		this.socketListener.writeData(this.connection);

		// Validate written data
		for (int i = 0; i < COUNT; i++) {
			String msg = TEXT + i;
			this.validateOutputToClient(msg);
		}
		this.socketChannel.validateNoOutput();
	}

}