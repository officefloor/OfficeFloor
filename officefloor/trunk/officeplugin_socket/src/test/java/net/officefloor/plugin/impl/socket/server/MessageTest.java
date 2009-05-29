/*
 * Office Floor, Application Server
 * Copyright (C) 2005-2009 Daniel Sagenschneider
 *
 * This program is free software; you can redistribute it and/or modify it under the terms
 * of the GNU General Public License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program;
 * if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston,
 * MA 02111-1307 USA
 */
package net.officefloor.plugin.impl.socket.server;

import net.officefloor.plugin.socket.server.spi.Message;

/**
 * Tests the {@link Message} implementations.
 * 
 * @author Daniel Sagenschneider
 */
public class MessageTest extends AbstractWriteRead {

	/**
	 * Ensures able to append data to the {@link Message} and read it back.
	 */
	public void testWriteAndRead() throws Exception {

		// Create the write message
		WriteMessageImpl msg = this.connection.writeStream.appendMessage(null);

		// Append data to the message
		final String MESSAGE = "This is some data of a message";
		final int SIZE = MESSAGE.getBytes().length;
		msg.append(MESSAGE.getBytes());

		// Read the data back from the message
		byte[] buffer = new byte[SIZE + 10];
		int readSize = msg.read(buffer);

		// Ensure data read back correctly
		assertEquals("Incorrect read size", SIZE, readSize);
		assertEquals("Incorrect data", MESSAGE, new String(buffer, 0, SIZE));
	}

	/**
	 * Ensures able to write a significant amount of data and read it back.
	 */
	public void testMultipleWriteAndRead() throws Exception {

		final String MESSAGE = "Data for message ";
		final int ITERATIONS = 10000;

		// Create the write message
		WriteMessageImpl msg = this.connection.writeStream.appendMessage(null);

		// Write the messages
		for (int i = 0; i < ITERATIONS; i++) {
			// Create the data to write
			byte[] data = (MESSAGE + i).getBytes();

			// Append the data
			msg.append(data);
		}

		// Read back the data ensuring correct
		for (int i = 0; i < ITERATIONS; i++) {
			// Obtain the expected message
			String expectedMessage = MESSAGE + i;
			int expectedSize = expectedMessage.getBytes().length;

			// Read the data
			byte[] buffer = new byte[expectedSize];
			int readSize = msg.read(buffer);

			// Ensure data read back correctly
			assertEquals("Incorrect read size", expectedSize, readSize);
			assertEquals("Incorrect data", expectedMessage, new String(buffer));
		}

		// Should have no further data available
		byte[] buffer = new byte[1];
		assertEquals("No further data expected", 0, msg.read(buffer));
	}

}
