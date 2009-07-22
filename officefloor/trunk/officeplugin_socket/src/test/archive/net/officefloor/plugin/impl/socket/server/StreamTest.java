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
package net.officefloor.plugin.impl.socket.server;

import java.io.IOException;

import net.officefloor.plugin.socket.server.impl.AbstractWriteRead;

/**
 * Tests the {@link Stream}.
 * 
 * @author Daniel Sagenschneider
 */
public class StreamTest extends AbstractWriteRead {

	/**
	 * Ensure able to write and read from the {@link Stream}.
	 */
	public void testWriteAndRead() throws IOException {

		// Obtain the write message stream
		Stream<WriteMessageImpl> writeStream = this.connection.writeStream;

		// Write data to the stream
		final String MESSAGE = "This is some data of a stream";
		final int SIZE = MESSAGE.getBytes().length;
		writeStream.write(MESSAGE.getBytes(), 0, SIZE);

		// Read the data back from the stream
		byte[] buffer = new byte[SIZE + 10];
		int readSize = writeStream.read(buffer, 0, buffer.length);

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

		// Obtain the write message stream
		Stream<WriteMessageImpl> writeStream = this.connection.writeStream;

		// Write the messages
		for (int i = 0; i < ITERATIONS; i++) {
			// Create the data to write
			byte[] data = (MESSAGE + i).getBytes();

			// Write the data
			writeStream.write(data, 0, data.length);
		}

		// Ensure all the messages are filled
		WriteMessageImpl writeMessage = writeStream.getFirstMessage();
		assertNotNull("Expecting at least one write message", writeMessage);
		while (writeMessage != null) {
			if (writeMessage.next != null) {
				// Not last, so should be written
				assertTrue("Write messages must be filled", writeMessage
						.isFilled());
			} else {
				// Last, so should NOT be written (buffering)
				assertFalse("Last write message should NOT be filled",
						writeMessage.isFilled());
			}
			writeMessage = writeMessage.next;
		}

		// Flush the stream (last message is written)
		writeStream.flush();

		// Last write message should now be filled
		assertTrue("Last write message should be filled on flush", writeStream
				.getLastMessage().isFilled());

		// Read back the data ensuring correct
		for (int i = 0; i < ITERATIONS; i++) {

			// Obtain the expected message
			String expectedMessage = MESSAGE + i;
			int expectedSize = expectedMessage.getBytes().length;

			// Read the data
			byte[] buffer = new byte[expectedSize];
			int readSize = writeStream.read(buffer, 0, buffer.length);

			// Ensure data read back correctly
			assertEquals("Incorrect read size", expectedSize, readSize);
			assertEquals("Incorrect data", expectedMessage, new String(buffer));
		}

		// Should be no further data to read
		byte[] buffer = new byte[1];
		assertEquals("No further data should be available", 0, writeStream
				.read(buffer, 0, buffer.length));

		// Should no longer have any messages (read beyond end above)
		assertNull("All messages should be read and cleaned up", writeStream
				.getFirstMessage());
		assertNull("If first null, last should also be null", writeStream
				.getLastMessage());
	}

}
