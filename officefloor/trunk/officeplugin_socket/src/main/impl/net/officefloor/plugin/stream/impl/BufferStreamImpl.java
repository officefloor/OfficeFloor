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
package net.officefloor.plugin.stream.impl;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;

import net.officefloor.plugin.stream.BufferProcessException;
import net.officefloor.plugin.stream.BufferProcessor;
import net.officefloor.plugin.stream.BufferSquirt;
import net.officefloor.plugin.stream.BufferSquirtFactory;
import net.officefloor.plugin.stream.BufferStream;
import net.officefloor.plugin.stream.InputBufferStream;
import net.officefloor.plugin.stream.OutputBufferStream;

/**
 * {@link BufferStream} implementation.
 *
 * @author Daniel Sagenschneider
 */
public class BufferStreamImpl implements BufferStream {

	/**
	 * {@link BufferSquirtFactory}.
	 */
	private final BufferSquirtFactory squirtFactory;

	/**
	 * {@link InputBufferStream}.
	 */
	private final InputBufferStream input = new InputBufferStreamImpl(this);

	/**
	 * {@link OutputBufferStream}.
	 */
	private final OutputBufferStream output = new OutputBufferStreamImpl(this);

	/**
	 * Initiate.
	 *
	 * @param squirtFactory
	 *            {@link BufferSquirtFactory}.
	 */
	public BufferStreamImpl(BufferSquirtFactory squirtFactory) {
		this.squirtFactory = squirtFactory;
	}

	/**
	 * Head {@link BufferSquirtElement} in stream.
	 */
	private BufferSquirtElement head = null;

	/**
	 * Tail {@link BufferSquirtElement} in stream.
	 */
	private BufferSquirtElement tail = null;

	/**
	 * Flags if the input is closed.
	 */
	private boolean isInputClosed = false;

	/**
	 * Flags if the output is closed.
	 */
	private boolean isOutputClosed = false;

	/**
	 * Ensures the input is open.
	 *
	 * @throws IOException
	 *             If input is closed.
	 */
	private void ensureInputOpen() throws IOException {
		if (this.isInputClosed) {
			throw new ClosedChannelException();
		}
	}

	/**
	 * Ensures the output is open.
	 *
	 * @throws IOException
	 *             If output is closed.
	 */
	private void ensureOutputOpen() throws IOException {
		if (this.isOutputClosed) {
			throw new ClosedChannelException();
		}
	}

	/**
	 * Slices the write buffer to allow reading.
	 *
	 * @return {@link BufferSquirtElement} containing the read content.
	 */
	private BufferSquirtElement sliceWriteBufferForReading() {

		// Obtain the write buffer
		ByteBuffer buffer = this.tail.squirt.getBuffer();

		// Add remaining buffer content as squirt
		ByteBuffer appendBuffer = buffer.slice();
		BufferSquirtElement writeElement = new BufferSquirtElement(
				new BufferSquirtSlice(appendBuffer, this.tail.squirt));
		BufferSquirtElement readElement = this.tail;
		this.tail.next = writeElement;
		this.tail = writeElement;

		// Setup content for reading
		buffer.flip();
		readElement.isSliced = true;

		// Return the read element
		return readElement;
	}

	/*
	 * ================== BufferStream ==================================
	 */

	@Override
	public OutputBufferStream getOutputBufferStream() {
		return this.output;
	}

	@Override
	public void write(byte[] bytes) throws IOException {

		// Ensure the output is open
		this.ensureOutputOpen();

		// Obtain the content markers
		int contentPosition = 0;
		int contentRemaining = bytes.length;

		// Write content to stream until no further content
		while (contentRemaining > 0) {

			// Ensure element in stream for appending
			BufferSquirtElement element;
			if (this.head == null) {
				// Create first element in stream
				element = new BufferSquirtElement(this.squirtFactory
						.createBufferSquirt());
				this.head = element;
				this.tail = element;
			} else {
				// Append to last element
				element = this.tail;
			}

			// Ensure space in buffer to append content
			ByteBuffer buffer = element.squirt.getBuffer();
			if (buffer.remaining() == 0) {
				// Append new buffer and flip current last for reading
				element = new BufferSquirtElement(this.squirtFactory
						.createBufferSquirt());
				buffer = element.squirt.getBuffer();
				this.tail.next = element;
				this.tail.squirt.getBuffer().flip();
				this.tail = element;
			}

			// Write content to the buffer
			int bufferRemaining = buffer.remaining();
			int writeLength = Math.min(bufferRemaining, contentRemaining);
			buffer.put(bytes, contentPosition, writeLength);

			// Update content markers
			contentPosition += writeLength;
			contentRemaining -= writeLength;
		}
	}

	@Override
	public void append(ByteBuffer buffer) throws IOException {

		// Use duplicate to not change original buffer markers.
		// Also means not subject to external changes in buffer markers.
		buffer = buffer.duplicate();

		// Create the squirt element for the buffer
		BufferSquirtElement element = new BufferSquirtElement(
				new AppendedBufferSquirt(buffer));

		// Determine if first buffer
		if (this.head == null) {
			// First buffer so append an empty write buffer.
			// Empty write squirt to save on memory if only appending buffers.
			BufferSquirtElement writeElement = new BufferSquirtElement(
					new EmptyBufferSquirt());
			this.head = element;
			this.head.next = writeElement;
			this.tail = writeElement;

		} else {
			// Slice write buffer to append after read content
			BufferSquirtElement secondLastElement = this
					.sliceWriteBufferForReading();

			// Append buffer after read content (before write)
			secondLastElement.next = element;
			element.next = this.tail;
		}
	}

	@Override
	public void closeOutput() {
		// Flag the output as closed
		this.isOutputClosed = true;
	}

	@Override
	public InputBufferStream getInputBufferStream() {
		return this.input;
	}

	@Override
	public int read(byte[] readBuffer) throws IOException {

		// Ensure the input is open
		this.ensureInputOpen();

		// Determine if content for reading
		if (this.head == null) {
			// No content, read size based on whether stream closed
			return (this.isOutputClosed ? BufferStream.END_OF_STREAM : 0);
		}

		// Obtain the read markers
		int readPosition = 0;
		int readRemaining = readBuffer.length;
		int readSize = 0; // return value

		// Read content until no data or read buffer full
		while (readRemaining > 0) {

			// Ensure if last buffer that setup for reading
			if (this.head == this.tail) {
				// Set up single buffer for reading
				ByteBuffer buffer = this.head.squirt.getBuffer();

				// Determine if data available to read in last buffer
				if (buffer.position() == 0) {
					// No data, so determine if output stream closed
					if (this.isOutputClosed) {
						// Clean up as output stream closed
						this.head.squirt.close();
						this.head = null;
						this.tail = null;

						// Indicate bytes read or end of stream
						return (readSize > 0 ? readSize
								: BufferStream.END_OF_STREAM);
					}

					// Output stream open but no further data to read
					return readSize;
				}

				// Slice the write buffer to read content so far
				this.sliceWriteBufferForReading();
			}

			// Read content from buffer
			BufferSquirtElement element = this.head;
			ByteBuffer buffer = element.squirt.getBuffer();
			int bufferRemaining = buffer.remaining();
			int readLength = Math.min(bufferRemaining, readRemaining);
			buffer.get(readBuffer, readPosition, readLength);

			// Update read markers
			readPosition += readLength;
			readRemaining -= readLength;
			readSize += readLength;

			// Determine if further content for buffer
			if (buffer.remaining() == 0) {
				// Buffer read so remove
				BufferSquirtElement removedElement = this.head;
				this.head = removedElement.next;
				if (!removedElement.isSliced) {
					removedElement.squirt.close();
				}
			}
		}

		// Return the bytes read
		return readSize;
	}

	@Override
	public int read(BufferProcessor processor) throws IOException,
			BufferProcessException {

		// Ensure the input is open
		this.ensureInputOpen();

		// Determine if buffer data to process
		if (this.head == null) {
			// No data, return based on whether output closed
			return (this.isOutputClosed ? BufferStream.END_OF_STREAM : 0);

		} else if (this.head == this.tail) {
			// Set up single buffer for reading
			ByteBuffer buffer = this.head.squirt.getBuffer();

			// Determine if data available to read in last buffer
			if (buffer.position() == 0) {
				// No data, so determine if output stream closed
				if (this.isOutputClosed) {
					// Clean up as output stream closed
					this.head.squirt.close();
					this.head = null;
					this.tail = null;

					// No data and output closed, therefore end of stream
					return BufferStream.END_OF_STREAM;
				}

				// Output stream open but no further data to read
				return 0;
			}

			// Slice the write buffer to read content so far
			this.sliceWriteBufferForReading();
		}

		// Obtain the buffer to process
		ByteBuffer processBuffer = this.head.squirt.getBuffer();
		int readSize = processBuffer.remaining();

		try {
			// Process the buffer
			processor.process(processBuffer);

		} catch (Exception ex) {
			// Propagate process failure
			throw new BufferProcessException(ex);

		} finally {
			// Determine number of bytes processed from buffer
			int bufferRemaining = processBuffer.remaining();
			if (bufferRemaining == 0) {
				// Buffer processed, therefore release squirt
				BufferSquirtElement element = this.head;
				if (!element.isSliced) {
					element.squirt.close();
				}
				this.head = element.next;

			} else {
				// Further data remaining in buffer, so adjust read size
				readSize -= bufferRemaining;
			}
		}

		// Must have non-negative read size
		if (readSize < 0) {
			throw new IOException(
					"Buffer processing resulting in negative read size");
		}

		// Return the bytes read in processing the buffer
		return readSize;
	}

	@Override
	public int read(OutputBufferStream outputBufferStream) throws IOException {

		// Ensure the input is open
		this.ensureInputOpen();

		// Determine if buffer data to process
		if (this.head == null) {
			// No data, return based on whether output closed
			return (this.isOutputClosed ? BufferStream.END_OF_STREAM : 0);

		} else if (this.head == this.tail) {
			// Set up single buffer for reading
			ByteBuffer buffer = this.head.squirt.getBuffer();

			// Determine if data available to read in last buffer
			if (buffer.position() == 0) {
				// No data, so determine if output stream closed
				if (this.isOutputClosed) {
					// Clean up as output stream closed
					this.head.squirt.close();
					this.head = null;
					this.tail = null;

					// No data and output closed, therefore end of stream
					return BufferStream.END_OF_STREAM;
				}

				// Output stream open but no further data to read
				return 0;
			}

			// Slice the write buffer to read content so far
			this.sliceWriteBufferForReading();
		}

		// Obtain the buffer to process
		ByteBuffer processBuffer = this.head.squirt.getBuffer();

		// TODO pass squirt rather than copy data
		byte[] transfer = new byte[processBuffer.remaining()];
		processBuffer.get(transfer);
		outputBufferStream.write(transfer);

		// Buffer read so remove
		BufferSquirtElement removedElement = this.head;
		this.head = removedElement.next;
		if (!removedElement.isSliced) {
			removedElement.squirt.close();
		}

		// Return the bytes transferred
		return transfer.length;
	}

	@Override
	public void closeInput() {

		// Close all the buffer squirts
		BufferSquirtElement element = this.head;
		while (element != null) {

			// Close the squirt if not a slice
			if (!element.isSliced) {
				element.squirt.close();
			}

			// Move to next element
			element = element.next;
		}

		// Release the elements (buffers)
		this.head = null;
		this.tail = null;

		// Flag the input is closed
		this.isInputClosed = true;
	}

	/**
	 * Element in the stream containing the {@link BufferSquirt}.
	 */
	private static class BufferSquirtElement {

		/**
		 * {@link BufferSquirt}.
		 */
		public final BufferSquirt squirt;

		/**
		 * Next {@link BufferSquirtElement}.
		 */
		public BufferSquirtElement next = null;

		/**
		 * Flags indicating if the {@link ByteBuffer} of the
		 * {@link BufferSquirt} was sliced.
		 */
		public boolean isSliced = false;

		/**
		 * Initiate.
		 *
		 * @param squirt
		 *            {@link BufferSquirt}.
		 */
		public BufferSquirtElement(BufferSquirt squirt) {
			this.squirt = squirt;
		}
	}

	/**
	 * {@link BufferSquirt} containing an empty {@link ByteBuffer}.
	 */
	private static class EmptyBufferSquirt implements BufferSquirt {

		/**
		 * Empty {@link ByteBuffer}.
		 */
		private static final ByteBuffer BUFFER = ByteBuffer.wrap(new byte[0]);

		/*
		 * ================= BufferSquirt ===========================
		 */

		@Override
		public ByteBuffer getBuffer() {
			return BUFFER;
		}

		@Override
		public void close() {
			// Do nothing
		}
	}

	/**
	 * {@link BufferSquirt} for an appended {@link ByteBuffer}.
	 */
	private static class AppendedBufferSquirt implements BufferSquirt {

		/**
		 * Appended {@link ByteBuffer}.
		 */
		private final ByteBuffer buffer;

		/**
		 * Initiate.
		 *
		 * @param buffer
		 *            Appended {@link ByteBuffer}.
		 */
		public AppendedBufferSquirt(ByteBuffer buffer) {
			this.buffer = buffer;
		}

		/*
		 * ================= BufferSquirt ===========================
		 */

		@Override
		public ByteBuffer getBuffer() {
			return this.buffer;
		}

		@Override
		public void close() {
			// Do nothing
		}
	}

	/**
	 * Slice of a {@link BufferSquirt}.
	 */
	private static class BufferSquirtSlice implements BufferSquirt {

		/**
		 * {@link ByteBuffer} slice.
		 */
		private final ByteBuffer slice;

		/**
		 * Delegate {@link BufferSquirt} being wrapped.
		 */
		private final BufferSquirt delegate;

		/**
		 * Initiate.
		 *
		 * @param slice
		 *            {@link ByteBuffer} slice.
		 * @param delegate
		 *            Delegate {@link BufferSquirt} being wrapped.
		 */
		public BufferSquirtSlice(ByteBuffer slice, BufferSquirt delegate) {
			this.slice = slice;
			this.delegate = delegate;
		}

		/*
		 * ================= BufferSquirt ===========================
		 */

		@Override
		public ByteBuffer getBuffer() {
			return this.slice;
		}

		@Override
		public void close() {
			this.delegate.close();
		}
	}

}