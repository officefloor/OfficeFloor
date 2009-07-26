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
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.util.concurrent.atomic.AtomicInteger;

import net.officefloor.plugin.stream.BufferPopulator;
import net.officefloor.plugin.stream.BufferProcessor;
import net.officefloor.plugin.stream.BufferSquirt;
import net.officefloor.plugin.stream.BufferSquirtFactory;
import net.officefloor.plugin.stream.BufferStream;
import net.officefloor.plugin.stream.InputBufferStream;
import net.officefloor.plugin.stream.OutputBufferStream;
import net.officefloor.plugin.stream.squirtfactory.NotCreateBufferSquirtFactory;

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
	 * Initiate to input data.
	 *
	 * @param data
	 *            Data.
	 * @throws IOException
	 *             If fails to initiate.
	 */
	public BufferStreamImpl(ByteBuffer data) throws IOException {
		this.squirtFactory = new NotCreateBufferSquirtFactory();
		this.append(data);
		this.output.close();
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
	 * Number of bytes available.
	 */
	private long availableSize = 0;

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
	 * Creates a new {@link BufferSquirt} from the {@link BufferSquirtFactory}
	 * wrapping it in a new {@link ManagedBufferSquirt}.
	 *
	 * @return New {@link ManagedBufferSquirt}.
	 */
	private ManagedBufferSquirt newBufferSquirt() {
		BufferSquirt squirt = this.squirtFactory.createBufferSquirt();
		BufferSquirtUsage usage = new BufferSquirtUsage(squirt);
		ManagedBufferSquirt managedSquirt = new ManagedBufferSquirt(usage,
				squirt.getBuffer());
		return managedSquirt;
	}

	/**
	 * Obtains the write {@link ByteBuffer} to write further content to this
	 * {@link BufferStream}.
	 *
	 * @return Write {@link ByteBuffer}.
	 */
	private ByteBuffer getWriteBuffer() {

		// Ensure element in stream for appending
		BufferSquirtElement element;
		if (this.head == null) {
			// Create first element in stream
			element = new BufferSquirtElement(this.newBufferSquirt());
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
			element = new BufferSquirtElement(this.newBufferSquirt());
			buffer = element.squirt.getBuffer();
			this.tail.next = element;
			this.tail.squirt.getBuffer().flip();
			this.tail = element;
		}

		// Return the buffer to write further content
		return buffer;
	}

	/**
	 * <p>
	 * Prepares for a read from the {@link ByteBuffer} returning a status on
	 * availability of data to read.
	 * <p>
	 * The status values are:<il>
	 * <li>{@link BufferStream#END_OF_STREAM} indicate end of stream</li>
	 * <li>0 indicating no further data to read (stream still open)</li>
	 * <li>1 indicating data available from head {@link BufferSquirt} to read</li>
	 * </il>
	 *
	 * @return Status value (as per above) indicating availability of data.
	 */
	private int prepareForRead() {

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

		// Return status indicating data available
		return 1;
	}

	/**
	 * Slices the write buffer to allow reading.
	 *
	 * @return {@link BufferSquirtElement} containing the read content.
	 */
	private BufferSquirtElement sliceWriteBufferForReading() {

		// Obtain the write buffer squirt usage and buffer
		BufferSquirtUsage usage = this.tail.squirt.usage;
		ByteBuffer buffer = this.tail.squirt.getBuffer();

		// Add remaining buffer content as squirt
		ByteBuffer appendBuffer = buffer.slice();
		BufferSquirtElement writeElement = new BufferSquirtElement(
				new ManagedBufferSquirt(usage, appendBuffer));
		BufferSquirtElement readElement = this.tail;
		this.tail.next = writeElement;
		this.tail = writeElement;

		// Setup content for reading
		buffer.flip();

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
		this.write(bytes, 0, bytes.length);
	}

	@Override
	public void write(byte[] bytes, int offset, int length) throws IOException {

		// Ensure the output is open
		this.ensureOutputOpen();

		// Obtain the content markers
		int contentPosition = offset;
		int contentRemaining = length;

		// Write content to stream until no further content
		while (contentRemaining > 0) {

			// Obtain the write buffer
			ByteBuffer buffer = this.getWriteBuffer();

			// Write content to the buffer
			int bufferRemaining = buffer.remaining();
			int writeLength = Math.min(bufferRemaining, contentRemaining);
			buffer.put(bytes, contentPosition, writeLength);

			// Indicate more bytes available
			this.availableSize += writeLength;

			// Update content markers
			contentPosition += writeLength;
			contentRemaining -= writeLength;
		}
	}

	@Override
	public void write(BufferPopulator populator) throws IOException {

		// Ensure the output is open
		this.ensureOutputOpen();

		// Obtain the write buffer
		ByteBuffer buffer = this.getWriteBuffer();

		// Keep track of data available
		int bufferRemaining = buffer.remaining();
		int bytesAdded;

		try {
			// Populate the buffer
			populator.populate(buffer);

		} finally {

			// Obtain the number of bytes added
			bytesAdded = bufferRemaining - buffer.remaining();

			// Adjust available bytes as possibly more bytes available
			this.availableSize += bytesAdded;
		}

		// Should not cause negative bytes added
		if (bytesAdded < 0) {
			throw new IOException(
					"Buffer populating resulting in negative write size");
		}
	}

	@Override
	public void append(ByteBuffer buffer) throws IOException {

		// Use duplicate to not change original buffer markers.
		// Also means not subject to external changes in buffer markers.
		// Note: Will still be subject to data changes.
		buffer = buffer.duplicate();

		// Add the appended buffer
		this.append(new AppendedBufferSquirt(buffer));
	}

	@Override
	public void append(BufferSquirt squirt) throws IOException {

		// Ensure the output is open
		this.ensureOutputOpen();

		// Obtain the buffer of the squirt
		ByteBuffer buffer = squirt.getBuffer();

		// Create the element and management for the squirt
		BufferSquirtElement element = new BufferSquirtElement(
				new ManagedBufferSquirt(new BufferSquirtUsage(squirt), buffer));

		// Determine if first buffer
		if (this.head == null) {
			// First buffer so append an empty write buffer.
			// Empty write squirt to save on memory if only appending buffers.
			EmptyBufferSquirt emptyBufferSquirt = new EmptyBufferSquirt();
			BufferSquirtElement writeElement = new BufferSquirtElement(
					new ManagedBufferSquirt(new BufferSquirtUsage(
							emptyBufferSquirt), emptyBufferSquirt.getBuffer()));
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

		// More bytes available
		this.availableSize += buffer.remaining();
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
	public InputStream getBrowseStream() {
		return new BrowseStream();
	}

	@Override
	public int read(byte[] readBuffer) throws IOException {
		return this.read(readBuffer, 0, readBuffer.length);
	}

	@Override
	public int read(byte[] readBuffer, int offset, int length)
			throws IOException {

		// Ensure the input is open
		this.ensureInputOpen();

		// Determine if content for reading
		if (this.head == null) {
			// No content, read size based on whether stream closed
			return (this.isOutputClosed ? BufferStream.END_OF_STREAM : 0);
		}

		// Obtain the read markers
		int readPosition = offset;
		int readRemaining = length;
		int readSize = 0; // return value

		// Read content until no data or read buffer full
		while (readRemaining > 0) {

			// Prepare for the read
			switch (this.prepareForRead()) {
			case BufferStream.END_OF_STREAM:
				// Previous loop may have read data
				return (readSize > 0 ? readSize : BufferStream.END_OF_STREAM);
			case 0:
				// No further data available
				return readSize;

				// Otherwise data available to read
			}

			// Read content from buffer
			BufferSquirtElement element = this.head;
			ByteBuffer buffer = element.squirt.getBuffer();
			int bufferRemaining = buffer.remaining();
			int readLength = Math.min(bufferRemaining, readRemaining);
			buffer.get(readBuffer, readPosition, readLength);

			// Indicate bytes read and no longer available
			this.availableSize -= readLength;

			// Update read markers
			readPosition += readLength;
			readRemaining -= readLength;
			readSize += readLength;

			// Determine if further content for buffer
			if (buffer.remaining() == 0) {
				// Buffer read so remove
				BufferSquirtElement removedElement = this.head;
				this.head = removedElement.next;
				removedElement.squirt.close();
			}
		}

		// Return the bytes read
		return readSize;
	}

	@Override
	public int read(BufferProcessor processor) throws IOException {

		// Ensure the input is open
		this.ensureInputOpen();

		// Prepare for the read
		switch (this.prepareForRead()) {
		case BufferStream.END_OF_STREAM:
			return BufferStream.END_OF_STREAM;
		case 0:
			return 0;
			// Otherwise data available to read
		}

		// Obtain the buffer to process
		ByteBuffer processBuffer = this.head.squirt.getBuffer();
		int readSize = processBuffer.remaining();

		try {
			// Process the buffer
			processor.process(processBuffer);

		} finally {
			// Determine number of bytes processed from buffer
			int bufferRemaining = processBuffer.remaining();
			if (bufferRemaining == 0) {
				// Buffer processed, therefore release squirt
				BufferSquirtElement element = this.head;
				this.head = element.next;
				element.squirt.close();

			} else {
				// Further data remaining in buffer, so adjust read size
				readSize -= bufferRemaining;
			}

			// Indicate bytes read and no longer available
			this.availableSize -= readSize;
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
	public int read(int numberOfBytes, OutputBufferStream outputBufferStream)
			throws IOException {

		// Ensure the input is open
		this.ensureInputOpen();

		// Determine if content for reading
		if (this.head == null) {
			// No content, read size based on whether stream closed
			return (this.isOutputClosed ? BufferStream.END_OF_STREAM : 0);
		}

		// Obtain the read markers
		int readRemaining = numberOfBytes;
		int readSize = 0; // return value

		// Read content until no data or read buffer full
		while (readRemaining > 0) {

			// Prepare for the read
			switch (this.prepareForRead()) {
			case BufferStream.END_OF_STREAM:
				// Previous loop may have read data
				return (readSize > 0 ? readSize : BufferStream.END_OF_STREAM);
			case 0:
				// No further data available
				return readSize;

				// Otherwise data available to read
			}

			// Determine content remaining in buffer to read
			BufferSquirtElement element = this.head;
			ByteBuffer buffer = element.squirt.getBuffer();
			int bufferRemaining = buffer.remaining();

			// Obtain the usage for the buffer squirt
			BufferSquirtUsage usage = element.squirt.usage;

			// Transfer the buffer
			int readLength;
			if (bufferRemaining > readRemaining) {
				// Only partial of buffer to transfer
				ByteBuffer transferBuffer = buffer.slice();
				transferBuffer.limit(readRemaining);
				outputBufferStream.append(new ManagedBufferSquirt(usage,
						transferBuffer));

				// Move position to after transferred content
				buffer.position(buffer.position() + readRemaining);
				readLength = readRemaining;

			} else if (buffer.position() == 0) {
				// Transfer the entire buffer
				this.head = element.next;
				outputBufferStream.append(element.squirt);
				readLength = bufferRemaining;

			} else {
				// Transfer the remaining of the buffer
				ByteBuffer transferBuffer = buffer.slice();
				outputBufferStream.append(new ManagedBufferSquirt(usage,
						transferBuffer));

				// Remove the buffer
				this.head = element.next;
				element.squirt.close();
				readLength = bufferRemaining;
			}

			// Indicate bytes read and no longer available
			this.availableSize -= readLength;

			// Update read markers
			readRemaining -= readLength;
			readSize += readLength;
		}

		// Return the bytes read
		return readSize;
	}

	@Override
	public long skip(long numberOfBytes) throws IOException {

		// Determine if content for reading
		if (this.head == null) {
			// No content, read size based on whether stream closed
			return (this.isOutputClosed ? BufferStream.END_OF_STREAM : 0);
		}

		// Obtain the read markers
		long readRemaining = numberOfBytes;
		long readSize = 0; // return value

		// Read content until no data or read buffer full
		while (readRemaining > 0) {

			// Prepare for the read
			switch (this.prepareForRead()) {
			case BufferStream.END_OF_STREAM:
				// Previous loop may have read data
				return (readSize > 0 ? readSize : BufferStream.END_OF_STREAM);
			case 0:
				// No further data available
				return readSize;

				// Otherwise data available to read
			}

			// Determine content remaining in buffer to skip
			BufferSquirtElement element = this.head;
			ByteBuffer buffer = element.squirt.getBuffer();
			int bufferRemaining = buffer.remaining();

			// Transfer the buffer
			long readLength;
			if (bufferRemaining > readRemaining) {
				// Move position to after transferred content
				buffer.position(buffer.position() + (int) readRemaining);
				readLength = readRemaining;

			} else {
				// Skip the remaining of the buffer
				this.head = element.next;
				element.squirt.close();
				readLength = bufferRemaining;
			}

			// Indicate bytes read and no longer available
			this.availableSize -= readLength;

			// Update read markers
			readRemaining -= readLength;
			readSize += readLength;
		}

		// Return the bytes read
		return readSize;
	}

	@Override
	public long available() {
		if (this.isOutputClosed && (this.availableSize == 0)) {
			// Ouput stream closed and no further data
			return BufferStream.END_OF_STREAM;
		} else if (this.isInputClosed) {
			// Input stream closed so end of stream
			return BufferStream.END_OF_STREAM;
		} else {
			// Data available until input/output closed
			return this.availableSize;
		}
	}

	@Override
	public void closeInput() {

		// Close all the buffer squirts
		BufferSquirtElement element = this.head;
		while (element != null) {
			element.squirt.close();
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
		 * {@link ManagedBufferSquirt}.
		 */
		public final ManagedBufferSquirt squirt;

		/**
		 * Next {@link BufferSquirtElement}.
		 */
		public BufferSquirtElement next = null;

		/**
		 * Initiate.
		 *
		 * @param squirt
		 *            {@link ManagedBufferSquirt}.
		 */
		public BufferSquirtElement(ManagedBufferSquirt squirt) {
			this.squirt = squirt;
		}
	}

	/**
	 * Provides usage of a {@link BufferSquirt}. This allows to track usage and
	 * close the {@link BufferSquirt} when no longer used.
	 */
	private static class BufferSquirtUsage {

		/**
		 * Count of uses of the {@link BufferSquirt}.
		 */
		public final AtomicInteger count = new AtomicInteger(0);

		/**
		 * {@link BufferSquirt} being used.
		 */
		public final BufferSquirt underlyingBufferSquirt;

		/**
		 * Initiate.
		 *
		 * @param bufferSquirt
		 *            {@link BufferSquirt} being used.
		 */
		public BufferSquirtUsage(BufferSquirt bufferSquirt) {
			this.underlyingBufferSquirt = bufferSquirt;
		}
	}

	/**
	 * {@link BufferSquirt} wrapper that provides management of the
	 * {@link BufferSquirt} to be closed when no longer being used.
	 */
	private static class ManagedBufferSquirt implements BufferSquirt {

		/**
		 * {@link BufferSquirtUsage}.
		 */
		private final BufferSquirtUsage usage;

		/**
		 * {@link ByteBuffer} over the {@link BufferSquirt} instances
		 * {@link ByteBuffer}.
		 */
		private final ByteBuffer buffer;

		/**
		 * Initiate.
		 *
		 * @param usage
		 *            {@link BufferSquirtUsage}.
		 * @param buffer
		 *            {@link ByteBuffer} over the {@link BufferSquirt} instances
		 *            {@link ByteBuffer}.
		 */
		public ManagedBufferSquirt(BufferSquirtUsage usage, ByteBuffer buffer) {
			this.usage = usage;
			this.buffer = buffer;

			// Increate usage for this managed buffer squirt instance
			this.usage.count.incrementAndGet();
		}

		/*
		 * ================ ManagedBufferSquirt ===========================
		 */

		@Override
		public ByteBuffer getBuffer() {
			return this.buffer;
		}

		@Override
		public void close() {

			// This instance no longer used, so remove from count
			int count = this.usage.count.decrementAndGet();

			// If no longer used, close the underlying buffer squirt
			if (count == 0) {
				this.usage.underlyingBufferSquirt.close();
			}
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
	 * {@link InputStream} to browse contents of this {@link BufferStream}.
	 */
	private class BrowseStream extends InputStream {

		/**
		 * Current {@link BufferSquirtElement} being browsed.
		 */
		private BufferSquirtElement element = BufferStreamImpl.this.head;

		/**
		 * Position of next read from the current {@link BufferSquirtElement}.
		 */
		private int position = 0;

		/*
		 * =============== InputStream ==========================
		 */

		@Override
		public int read() throws IOException {
			for (;;) {
				// Determine if data to read
				if (this.element == null) {
					// No data
					return BufferStream.END_OF_STREAM;
				}

				// Obtain the current buffer to browse
				ByteBuffer buffer = this.element.squirt.getBuffer();

				// Determine if last buffer (write buffer)
				if (this.element == BufferStreamImpl.this.tail) {
					// Last (write) buffer, browse to position (written bytes)
					if (this.position < buffer.position()) {
						// Read the byte and adjust for next read
						byte b = buffer.get(this.position);
						this.position++;
						return b;
					}

				} else {
					// Read buffer, so determine if further data to browse
					if (this.position < buffer.limit()) {
						// Read the byte and adjust for next read
						byte b = buffer.get(this.position);
						this.position++;
						return b;
					}
				}

				// Can not read from current buffer, so move to next
				this.element = this.element.next;
				this.position = 0;
			}
		}

		@Override
		public long skip(long n) throws IOException {
			// Implementation that does not require reading buffer contents.
			// Improves efficiency as no unnecessary buffer reading overhead.

			// Ensure do nothing if non-positive value
			if (n <= 0) {
				return 0;
			}

			// Loop skipping bytes
			long remaining = n;
			while (remaining > 0) {

				// Determine if data to skip
				if (this.element == null) {
					break; // no further data
				}

				// Determine the remaining bytes left in current buffer stream
				ByteBuffer buffer = this.element.squirt.getBuffer();
				int bufferRemaining;
				if (this.element == BufferStreamImpl.this.tail) {
					// Last (write) buffer so only written bytes
					bufferRemaining = (buffer.position() - this.position);
				} else {
					// Read buffer so full content of buffer
					bufferRemaining = (buffer.limit() - this.position);
				}

				// Determine if skip only in buffer or require to move to next
				if (remaining > bufferRemaining) {
					// Skip to next buffer
					remaining -= bufferRemaining;
					this.element = this.element.next;
					this.position = 0;
				} else {
					// Skip to position in buffer
					this.position += remaining;
					remaining = 0;
				}
			}

			// Return the number of bytes skipped
			return n - remaining;
		}
	}

}