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
package net.officefloor.plugin.stream.inputstream;

import java.io.IOException;
import java.io.InputStream;

/**
 * Provides ability to browse an {@link InputStream}.
 * 
 * @author Daniel Sagenschneider
 */
public class BrowsableInputStream extends InputStream {

	/**
	 * {@link InputStream}.
	 */
	private final InputStream input;

	/**
	 * Capacity for each {@link DataBuffer}.
	 */
	private final int dataBufferCapacity;

	/**
	 * Mutex to synchronise on.
	 */
	private final Object mutex;

	/**
	 * {@link InputStream} to utilise for consuming {@link InputStream}.
	 */
	private BrowseInputStream consume = null;

	/**
	 * Initiate.
	 * 
	 * @param input
	 *            {@link InputStream}.
	 * @param dataBufferCapacity
	 *            Capacity for each {@link DataBuffer}.
	 * @param mutex
	 *            Mutex to synchronise on.
	 */
	public BrowsableInputStream(InputStream input, int dataBufferCapacity,
			Object mutex) {
		this.input = input;
		this.dataBufferCapacity = dataBufferCapacity;
		this.mutex = mutex;
	}

	/**
	 * Creates browse {@link InputStream} to content.
	 * 
	 * @return Browse {@link InputStream}.
	 */
	public InputStream createBrowser() {
		synchronized (this.mutex) {
			// Lazy create the consume input stream
			if (this.consume == null) {
				this.consume = new BrowseInputStream();
			}

			// Return a new browse stream
			return this.consume.clone();
		}
	}

	/*
	 * =========================== InputStream =======================
	 */

	@Override
	public int available() throws IOException {
		synchronized (this.mutex) {
			if (this.consume == null) {
				// Not browsing so go direct to input
				return this.input.available();
			} else {
				// Browsing so rely on consumer
				return this.consume.available();
			}
		}
	}

	@Override
	public int read() throws IOException {
		synchronized (this.mutex) {
			if (this.consume == null) {
				// Not browsing so go direct to input
				return this.input.read();
			} else {
				// Browsing so rely on consumer
				return this.consume.read();
			}
		}
	}

	@Override
	public synchronized void close() throws IOException {
		this.input.close();
	}

	/**
	 * Buffer of data from {@link InputStream}.
	 */
	private class DataBuffer {

		/**
		 * Data for this {@link DataBuffer}.
		 */
		public final byte[] data = new byte[BrowsableInputStream.this.dataBufferCapacity];

		/**
		 * Position within the data.
		 */
		private int writePosition = 0;

		/**
		 * Reference to the next {@link DataBuffer}.
		 */
		public DataBuffer next = null;
	}

	/**
	 * Browse {@link InputStream}.
	 */
	private class BrowseInputStream extends InputStream {

		/**
		 * Current {@link DataBuffer}.
		 */
		private DataBuffer data;

		/**
		 * Read position within the current {@link DataBuffer}.
		 */
		private int readPosition;

		/**
		 * Initiate for consuming {@link BrowseInputStream}.
		 */
		public BrowseInputStream() {
			this(new DataBuffer(), 0);
		}

		/**
		 * Initiate.
		 * 
		 * @param data
		 *            {@link DataBuffer}.
		 * @param readPosition
		 *            Read position within the {@link DataBuffer}.
		 */
		private BrowseInputStream(DataBuffer data, int readPosition) {
			this.data = data;
			this.readPosition = readPosition;
		}

		/**
		 * Creates a {@link BrowseInputStream} to browse from current position
		 * of this {@link BrowseInputStream}.
		 * 
		 * @return {@link BrowseInputStream}.
		 */
		@Override
		public BrowseInputStream clone() {
			return new BrowseInputStream(this.data, this.readPosition);
		}

		/*
		 * ==================== InputStream ========================
		 */

		@Override
		public int available() throws IOException {
			synchronized (BrowsableInputStream.this.mutex) {

				// Determine available
				int available = 0;

				// Remove read of current buffer
				available -= this.readPosition;

				// Count remaining buffer data
				DataBuffer buffer = this.data;
				while (buffer != null) {

					// Append available data
					available += buffer.writePosition;

					// Move to next buffer
					buffer = buffer.next;
				}

				// Add remaining from input
				available += BrowsableInputStream.this.input.available();

				// Return content available
				return available;
			}
		}

		@Override
		public int read() throws IOException {
			synchronized (BrowsableInputStream.this.mutex) {

				// Determine if at end of buffer
				if (this.data.data.length == this.readPosition) {
					if (this.data.next != null) {
						// Move to next buffer
						this.data = this.data.next;
					}
				}

				// Determine if data from buffer
				if (this.data.writePosition > this.readPosition) {
					// Return value from buffer (incrementing position)
					return this.data.data[this.readPosition++];
				}

				// No data available in buffer, so must obtain from input
				int value = BrowsableInputStream.this.input.read();
				if (value == -1) {
					// End of stream, no further data
					return value;
				}

				// Load value for other browsing (ensuring space)
				if (this.data.data.length == this.data.writePosition) {
					// End of buffer so create new buffer for content
					DataBuffer buffer = new DataBuffer();
					this.data.next = buffer;
					this.data = buffer;
				}

				// Append value to buffer for other browsing
				this.data.data[this.data.writePosition++] = (byte) value;
				this.readPosition = this.data.writePosition; // so not re-read

				// Return the value
				return value;
			}
		}
	}

}