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
import java.io.OutputStream;
import java.nio.ByteBuffer;

import net.officefloor.plugin.socket.server.spi.Connection;
import net.officefloor.plugin.socket.server.spi.Message;
import net.officefloor.plugin.socket.server.spi.WriteMessageListener;

/**
 * Stream of {@link Message} instances.
 * 
 * @author Daniel Sagenschneider
 */
public class Stream<T extends AbstractMessage<T>> {

	/**
	 * {@link Connection}.
	 */
	final ConnectionImpl<?> connection;

	/**
	 * Factory to create the {@link AbstractMessage} instances.
	 */
	private final MessageFactory<T> messageFactory;

	/**
	 * Head {@link AbstractMessage}.
	 */
	private T head = null;

	/**
	 * Tail {@link AbstractMessage}.
	 */
	private T tail = null;

	/**
	 * Initiate.
	 * 
	 * @param connection
	 *            {@link Connection}.
	 * @param messageFactory
	 *            {@link MessageFactory}.
	 */
	public Stream(ConnectionImpl<?> connection, MessageFactory<T> messageFactory) {
		this.connection = connection;
		this.messageFactory = messageFactory;
	}

	/**
	 * Creates a {@link AbstractMessage} and appends to listing of
	 * {@link AbstractMessage} instances of this {@link Stream}.
	 * 
	 * @param listener
	 *            {@link WriteMessageListener}.
	 * @return Newly created {@link AbstractMessage}.
	 * @throws IOException
	 *             If the {@link Connection} is closed.
	 */
	T appendMessage(WriteMessageListener listener) throws IOException {

		// Ensure connection is ok
		this.connection.checkIOState();

		// Create the message
		T message = this.messageFactory.createMessage(this, listener);

		// Append to end of message linked list
		if (this.head == null) {
			// Empty linked list (first entry)
			this.head = message;
		} else {
			// Add as last message
			this.tail.next = message;
			message.prev = this.tail;
		}
		this.tail = message;

		// Return the created message
		return message;
	}

	/**
	 * Obtains the first {@link AbstractMessage}.
	 * 
	 * @return First {@link AbstractMessage} or <code>null</code> if no
	 *         {@link AbstractMessage} instances for this {@link Stream}.
	 */
	T getFirstMessage() {
		return this.head;
	}

	/**
	 * Obtains the last {@link AbstractMessage}.
	 * 
	 * @return Last {@link AbstractMessage} or <code>null</code> if no
	 *         {@link AbstractMessage} instances for this {@link Stream}.
	 */
	T getLastMessage() {
		return this.tail;
	}

	/**
	 * Removes the input {@link AbstractMessage} and all {@link AbstractMessage}
	 * instances before it from the {@link Connection}.
	 * 
	 * @param message
	 *            {@link AbstractMessage} to remove from this {@link Connection}.
	 */
	void removeMessage(T message) {

		// Iterate up the current message removing it
		T current = this.head;
		while (current != null) {

			// Ensure the message is cleaned up
			current.cleanup();

			// Determine if the message
			if (current == message) {
				// Set head to next of message (effectively removing)
				this.head = message.next;
				message.prev = null;
				if (this.head == null) {
					// List empty so clear tail
					this.tail = null;
				}

				// Message removed
				return;
			}

			// Move to next message for next iteration
			current = current.next;
		}
	}

	/**
	 * Writes the data to this {@link Stream}.
	 * 
	 * @param data
	 *            Data to be written to this {@link Stream}.
	 * @param offset
	 *            Offset into data to start writing.
	 * @param length
	 *            Number of bytes to write.
	 * @throws IOException
	 *             If the {@link Connection} is closed.
	 */
	void write(byte[] data, int offset, int length) throws IOException {

		// Ensure connection is ok
		this.connection.checkIOState();

		// Always write to the last message
		T message = this.tail;

		// Obtain details to size the message
		int segmentCount = this.connection.recommendedSegmentCount;
		if (message != null) {
			if (message.isFilled()) {
				// Message filled, so create another
				message = null;
			} else {
				// Use the remaining of the message
				segmentCount -= message.getSegmentCount();
			}
		}
		if (segmentCount <= 0) {
			// Message is full, write to another
			segmentCount = this.connection.recommendedSegmentCount;
			if (message != null) {
				// Write the message and unset to create another
				message.write();
				message = null;
			}
		}

		// Obtain the segment buffer size
		int segmentBufferSize = this.connection.messageSegmentPool
				.getMessageSegmentBufferSize();

		// Loop writing data until all written
		while (length > 0) {

			// Ensure have a message to write data
			if (message == null) {
				message = this.appendMessage(null);
			}

			// Calculate the bytes to be written
			int msgSize = segmentCount * segmentBufferSize;
			msgSize = Math.min(msgSize, length);

			// Write the data to the message
			message.append(data, offset, msgSize);

			// Calculate details after write
			offset += msgSize;
			length -= msgSize;

			// Determine if more to write
			if (length > 0) {
				// More to write, therefore current message complete
				message.write();
			}

			// Set up to create another full message on next iteration
			message = null;
			segmentCount = this.connection.recommendedSegmentCount;
		}
	}

	/**
	 * Writes the {@link ByteBuffer} data to this {@link Stream}.
	 * 
	 * @param data
	 *            {@link ByteBuffer} of data.
	 * @throws IOException
	 *             If the {@link Connection} is closed.
	 */
	void write(ByteBuffer data) throws IOException {

		// Ensure connection is ok
		this.connection.checkIOState();

		// Obtain the message to append the data
		T message = this.tail;
		if (message != null) {
			// Determine if filled
			if (message.isFilled()) {
				// Unset message, to create another
				message = null;
			} else if (message.getSegmentCount() >= this.connection.recommendedSegmentCount) {
				// Message at recommended size
				message.write();
				message = null;
			}
		}
		if (message == null) {
			// Ensure a message is available
			message = this.appendMessage(null);
		}

		// Append the buffer in its own message segment
		message.appendSegment(data);
	}

	/**
	 * <p>
	 * Flags to flush the writes to the client.
	 * <p>
	 * Unlike {@link OutputStream#flush()} this method does not block, it only
	 * flags for the data written so far to be flushed to the client when
	 * possible.
	 */
	void flush() {
		// Flag to write the last message
		if (this.tail != null) {
			this.tail.write();
		}
	}

	/**
	 * Reads the data from this {@link Stream}.
	 * 
	 * @param buffer
	 *            Buffer to read data into.
	 * @param offset
	 *            Offset into buffer to start reading data.
	 * @param length
	 *            Number of bytes to read.
	 * @return Number of bytes read.
	 * @throws IOException
	 *             If the {@link Connection} is closed.
	 */
	int read(byte[] buffer, int offset, int length) throws IOException {

		// Ensure connection is ok
		this.connection.checkIOState();

		// Record the number of bytes read
		int bytesRead = 0;

		// Obtain the first message
		T message = this.head;
		while (message != null) {

			// Read the data from the message
			int bytesSize = message.read(buffer, offset, length);

			// Recalculate number of bytes to still obtain
			length -= bytesSize;
			bytesRead += bytesSize;
			if (length == 0) {
				// All bytes obtained
				return bytesRead;
			}

			// Calculate offset for next read
			offset += bytesSize;

			// Obtain the next message, removing current
			T next = message.next;
			message.remove();
			message = next;
		}

		// Return the bytes read
		return bytesRead;
	}

}
