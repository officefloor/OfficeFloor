/*
 *  Office Floor, Application Server
 *  Copyright (C) 2006 Daniel Sagenschneider
 *
 *  This program is free software; you can redistribute it and/or modify it under the terms 
 *  of the GNU General Public License as published by the Free Software Foundation; either 
 *  version 2 of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with this program; 
 *  if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, 
 *  MA 02111-1307 USA
 */
package net.officefloor.plugin.impl.socket.server;

import java.nio.ByteBuffer;

import net.officefloor.plugin.socket.server.spi.Message;
import net.officefloor.plugin.socket.server.spi.MessageSegment;
import net.officefloor.plugin.socket.server.spi.WriteMessage;
import net.officefloor.plugin.socket.server.spi.WriteMessageListener;

/**
 * Abstract {@link Message}.
 * 
 * @author Daniel
 */
abstract class AbstractMessage<T extends AbstractMessage<T>> implements Message {

	/**
	 * {@link MessageSegment} count indicating that this {@link Message} is
	 * cleaned up.
	 */
	public static final int MESSAGE_SEGMENT_CLEANED_UP_COUNT = -1;

	/**
	 * {@link Stream} that this message is participating within.
	 */
	final Stream<T> stream;

	/**
	 * Previous {@link AbstractMessage}.
	 */
	T prev = null;

	/**
	 * Next {@link AbstractMessage}.
	 */
	T next = null;

	/**
	 * Head of the {@link MessageSegment} list.
	 */
	PooledMessageSegment head = null;

	/**
	 * Tail of the {@link MessageSegment} list.
	 */
	PooledMessageSegment tail = null;

	/**
	 * Number of {@link MessageSegment} instances on this {@link Message}.
	 */
	int segmentCount = 0;

	/**
	 * Current {@link MessageSegment} being written to the client.
	 */
	MessageSegment currentMessageSegment = null;

	/**
	 * Offset into the current {@link MessageSegment}.
	 */
	int currentMessageSegmentOffset = 0;

	/**
	 * Initiate.
	 * 
	 * @param stream
	 *            {@link Stream} that this message is participating within.
	 * @param listener
	 *            {@link WriteMessageListener}.
	 */
	AbstractMessage(Stream<T> stream, WriteMessageListener listener) {
		this.stream = stream;
		this.listener = listener;
	}

	/**
	 * Removes this {@link AbstractMessage}.
	 */
	@SuppressWarnings("unchecked")
	void remove() {
		// Remove, which in turn invokes cleanup
		this.stream.removeMessage((T) this);
	}

	/**
	 * <p>
	 * Cleans up this {@link AbstractMessage}.
	 * <p>
	 * This should only be called from the {@link Stream}.
	 */
	void cleanup() {
		// Return the message segments to pool
		if (this.head != null) {
			this.stream.connection.messageSegmentPool
					.returnMessageSegments(this.head);

			// Unlink message segments
			this.head = null;
			this.tail = null;
		}

		// Flag that the message is cleaned up
		this.segmentCount = MESSAGE_SEGMENT_CLEANED_UP_COUNT;
	}

	/**
	 * Checks state of this {@link Message} to ensure it may still perform I/O
	 * operations
	 * 
	 * @throws IllegalStateException
	 *             If {@link Message} is cleaned up.
	 */
	void checkIOState() throws IllegalStateException {
		if (this.segmentCount == MESSAGE_SEGMENT_CLEANED_UP_COUNT) {
			throw new IllegalStateException("Message is cleaned up");
		}
	}

	/**
	 * Appends a {@link MessageSegment}.
	 * 
	 * @param byteBuffer
	 *            {@link ByteBuffer} for the segment. Provide <code>null</code>
	 *            to use a {@link MessageSegment} from the
	 *            {@link MessageSegmentPool}.
	 * @return {@link PooledMessageSegment}.
	 */
	PooledMessageSegment appendMessageSegment(ByteBuffer byteBuffer) {

		// Obtain the message segment
		PooledMessageSegment messageSegment;
		if (byteBuffer == null) {
			messageSegment = this.stream.connection.messageSegmentPool
					.getMessageSegment();
		} else {
			messageSegment = this.stream.connection.messageSegmentPool
					.getMessageSegment(byteBuffer);
		}

		// Append to end of linked list
		if (this.head == null) {
			// Empty linked list (first entry)
			this.head = messageSegment;
		} else {
			this.tail.setNextSegment(messageSegment);
			messageSegment.setPreviousSegment(this.tail);
		}
		this.tail = messageSegment;

		// Increment the number of segments
		this.segmentCount++;

		// Return the message segment
		return messageSegment;
	}

	/*
	 * ====================================================================
	 * Message
	 * ====================================================================
	 */

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.plugin.socket.server.spi.Message#getConnection()
	 */
	public ConnectionImpl<?> getConnection() {
		return this.stream.connection;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.plugin.socket.server.spi.Message#getSegmentCount()
	 */
	public int getSegmentCount() {
		return this.segmentCount;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.plugin.socket.server.spi.Message#getFirstSegment()
	 */
	public MessageSegment getFirstSegment() {
		return this.head;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.plugin.socket.server.spi.Message#getLastSegment()
	 */
	public MessageSegment getLastSegment() {
		return this.tail;
	}

	/*
	 * ====================================================================
	 * WriteMessage
	 * ====================================================================
	 */

	/**
	 * {@link WriteMessageListener}.
	 */
	private final WriteMessageListener listener;

	/**
	 * Flag indicating if this {@link WriteMessage} is filled.
	 */
	private boolean isFilled = false;

	/**
	 * Flag indicating if this {@link WriteMessage} is written to the client.
	 */
	private boolean isWritten = false;

	/**
	 * Obtains if this {@link WriteMessage} is filled.
	 * 
	 * @return <code>true</code> if this {@link WriteMessage} is filled and
	 *         can be sent.
	 */
	boolean isFilled() {
		return this.isFilled;
	}

	/**
	 * Flags that this {@link WriteMessage} has been written to the client.
	 */
	void written() {

		// Unset the current message segment (allows garbage collection)
		this.currentMessageSegment = null;

		// Flag written
		this.isWritten = true;

		// Notify message written
		if (this.listener != null) {
			this.listener.messageWritten((WriteMessage) this);
		}

		// Remove this message as written
		this.remove();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.plugin.socket.server.spi.WriteMessage#send()
	 */
	public void write() {
		// Flag the message is filled
		this.isFilled = true;

		// Wake up the socket listener to process immediately
		this.stream.connection.wakeupSocketListener();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.plugin.socket.server.spi.WriteMessage#isWritten()
	 */
	public boolean isWritten() {
		return this.isWritten;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.plugin.socket.server.spi.WriteMessage#append(byte[])
	 */
	public void append(byte[] data) {
		this.append(data, 0, data.length);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.plugin.socket.server.spi.WriteMessage#append(byte[],
	 *      int, int)
	 */
	public void append(byte[] data, int offset, int length) {

		// Ensure valid state
		this.checkIOState();

		// Obtain the last segment
		PooledMessageSegment messageSegment = this.tail;

		// Determine if space is remaining in buffer
		while (length > 0) {

			// Obtain the byte buffer to load data
			if (messageSegment == null) {
				// No segment, so append one
				messageSegment = this.appendMessageSegment(null);
			}
			ByteBuffer buffer = messageSegment.getBuffer();

			// Load the buffer with the data
			int loadSize = Math.min(length, buffer.remaining());
			if (loadSize > 0) {
				buffer.put(data, offset, loadSize);
			}

			// Alter offset and length for next iteration
			offset += loadSize; // move to next data to load
			length -= loadSize; // decrease data to load

			// Unset message segment to append in next iteration
			messageSegment = null;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.plugin.socket.server.spi.WriteMessage#appendSegment()
	 */
	public MessageSegment appendSegment() {
		// Ensure valid state
		this.checkIOState();

		// Return an appended pooled segment
		return this.appendMessageSegment(null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.plugin.socket.server.spi.WriteMessage#appendSegment(java.nio.ByteBuffer)
	 */
	public MessageSegment appendSegment(ByteBuffer byteBuffer) {
		// Ensure valid state
		this.checkIOState();

		// Return an appended message segment
		return this.appendMessageSegment(byteBuffer);
	}

	/*
	 * ====================================================================
	 * ReadMessage
	 * ====================================================================
	 */

	/**
	 * Indicates the current {@link MessageSegment} being read from.
	 */
	MessageSegment currentReadSegment = null;

	/**
	 * Offset into the {@link #currentReadSegment} to start reading from.
	 */
	int currentReadOffset = 0;

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.plugin.socket.server.spi.ReadMessage#read(byte[])
	 */
	public int read(byte[] buffer) {
		return this.read(buffer, 0, buffer.length);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.plugin.socket.server.spi.ReadMessage#read(byte[],
	 *      int, int)
	 */
	public int read(byte[] buffer, int offset, int length) {

		// Ensure valid state
		this.checkIOState();

		// Remaining bytes for complete read
		int remaining = length;

		// Find the segment to start from
		MessageSegment segment = this.currentReadSegment;
		if (segment == null) {
			// Use the first segment
			segment = this.head;
		}

		// Read the bytes until either no further segments or length
		// required is obtained.
		while (segment != null) {

			// Obtain the buffer for reading
			ByteBuffer data = segment.getBuffer().duplicate();
			data.flip();
			data.position(this.currentReadOffset);

			// Determine amount of data to read
			int readSize = Math.min(data.remaining(), remaining);

			// Only read if data available to read
			if (readSize > 0) {

				// Read the data
				data.get(buffer, offset, readSize);

				// Calculate remaining and offsets after read
				remaining -= readSize;
				offset += readSize;
				this.currentReadOffset += readSize;

				// Determine if all data read
				if (remaining == 0) {
					return length;
				}
			}

			// Further data to read, try for next segment.
			// Note: do not automatically move current segment as may still
			// be writing to the segment.
			segment = segment.getNextSegment();
			if (segment != null) {
				// Another segment, so set-up for next segment
				this.currentReadSegment = segment;
				this.currentReadOffset = 0;
			}
		}

		// Not all data was read (ran out of segments)
		return (length - remaining);
	}

}
