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

	/*
	 * ================== BufferStream ==================================
	 */

	@Override
	public OutputBufferStream getOutputBufferStream() {
		return this.output;
	}

	@Override
	public void write(byte b) throws IOException {

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

		// Write the content to the buffer
		buffer.put(b);
	}

	@Override
	public InputBufferStream getInputBufferStream() {
		return this.input;
	}

	@Override
	public int read(byte[] readBuffer) {

		// Obtain the content for reading
		if (this.head == null) {
			// No content, so nothing read
			return 0;
		} else if (this.head == this.tail) {
			// Set up single buffer for reading
			ByteBuffer buffer = this.head.squirt.getBuffer();

			// Add remaining buffer content as squirt
			ByteBuffer appendBuffer = buffer.slice();
			BufferSquirt appendSquirt = new BufferSquirtSlice(appendBuffer,
					this.head.squirt);
			BufferSquirtElement appendElement = new BufferSquirtElement(
					appendSquirt);
			this.head.next = appendElement;
			this.tail = appendElement;

			// Setup content for reading
			buffer.flip();
			this.head.isSliced = true;
		}

		// Obtain the number of bytes read
		int readSize = 0;

		// Obtain the first buffer
		BufferSquirtElement element = this.head;
		ByteBuffer buffer = element.squirt.getBuffer();
		if (buffer.remaining() >= readBuffer.length) {
			// Fill the buffer
			buffer.get(readBuffer);
			readSize += readBuffer.length;

			// Buffer read so remove
			BufferSquirtElement removedElement = this.head;
			this.head = removedElement.next;
			if (!removedElement.isSliced) {
				removedElement.squirt.close();
			}

		} else {
			// Load remaining content of buffer
			readSize += buffer.remaining();
			buffer.get(readBuffer, 0, buffer.remaining());
		}

		// Return the bytes read
		return readSize;
	}

	/**
	 * Element in the stream containing the {@link BufferSquirt}.
	 */
	private class BufferSquirtElement {

		/**
		 * {@link BufferSquirt}.
		 */
		public final BufferSquirt squirt;

		/**
		 * Previous {@link BufferSquirtElement}.
		 */
		public BufferSquirtElement prev = null;

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
	 * Slice of a {@link BufferSquirt}.
	 */
	private class BufferSquirtSlice implements BufferSquirt {

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