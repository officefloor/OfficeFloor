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
package net.officefloor.plugin.stream.impl;

import java.io.IOException;

import net.officefloor.plugin.stream.BrowseInputStream;
import net.officefloor.plugin.stream.NoAvailableInputException;
import net.officefloor.plugin.stream.ServerInputStream;

/**
 * {@link ServerInputStream} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class ServerInputStreamImpl extends ServerInputStream {

	/**
	 * Object to <code>synchronize</code> on for operations.
	 */
	private final Object lock;

	/**
	 * Head {@link ReadBuffer}.
	 */
	private ReadBuffer headBuffer = null;

	/**
	 * Tail {@link ReadBuffer}.
	 */
	private ReadBuffer tailBuffer = null;

	/**
	 * Current {@link ReadBuffer} index.
	 */
	private int currentBufferIndex = 0;

	/**
	 * Number of bytes currently available.
	 */
	private int available = 0;

	/**
	 * Indicates if further data.
	 */
	private boolean isFurtherData = true;

	/**
	 * Head of {@link BrowseInputStreamImpl} waiting data.
	 */
	private BrowseInputStreamImpl browseWaitingDataHead = null;

	/**
	 * Initiate.
	 * 
	 * @param lock
	 *            Object to <code>synchronize</code> on for operaterations.
	 */
	public ServerInputStreamImpl(Object lock) {
		this.lock = lock;
	}

	/**
	 * Inputs data for reading.
	 * 
	 * @param data
	 *            Data for reading.
	 * @param startIndex
	 *            Start index within the data.
	 * @param endIndex
	 *            End index within the data.
	 * @param isFurtherData
	 *            Indicates if further data is going to be made available.
	 */
	public void inputData(byte[] data, int startIndex, int endIndex,
			boolean isFurtherData) {

		synchronized (this.lock) {

			// Ensure have data
			if (data != null) {

				// Create the read buffer
				ReadBuffer readBuffer = new ReadBuffer(data, startIndex,
						endIndex);

				// Queue the read buffer
				if (this.tailBuffer == null) {
					// Provide as first buffer
					this.headBuffer = readBuffer;
					this.tailBuffer = readBuffer;
					this.currentBufferIndex = readBuffer.startIndex;

				} else {
					// Append to tail
					this.tailBuffer.next = readBuffer;
					this.tailBuffer = readBuffer;
				}

				// Notify waiting browsers
				BrowseInputStreamImpl browse = this.browseWaitingDataHead;
				while (browse != null) {
					browse.currentBuffer = readBuffer;
					browse.currentBufferIndex = readBuffer.startIndex;
					browse = browse.next;
				}
				this.browseWaitingDataHead = null;

				// Increment available bytes (+1 for last byte indexed)
				this.available += ((endIndex - startIndex) + 1);
			}

			// Indicate if further data
			this.isFurtherData = isFurtherData;
		}
	}

	/*
	 * ====================== ServerInputStream =======================
	 */

	@Override
	public BrowseInputStream createBrowseInputStream() {

		// Create the browse input stream (ensuring constructor invoked)
		BrowseInputStreamImpl browse = new BrowseInputStreamImpl();
		if (browse.currentBuffer == null) {
			browse.registerForInputData();
		}

		// Return the browse input stream
		return browse;
	}

	@Override
	public int read() throws IOException, NoAvailableInputException {

		synchronized (this.lock) {

			for (;;) {

				// Ensure have data
				if (this.headBuffer == null) {
					if (this.isFurtherData) {
						// Not yet end of stream
						throw new NoAvailableInputException();
					} else {
						// End of stream
						return -1;
					}
				}

				// Determine if data is available
				if (this.currentBufferIndex <= this.headBuffer.endIndex) {
					// Return the byte (keeping available up to date)
					this.available--;
					return this.headBuffer.data[this.currentBufferIndex++];
				}

				// Obtain the next buffer to start reading
				this.headBuffer = this.headBuffer.next;
				if (this.headBuffer != null) {
					// Provide current buffer index
					this.currentBufferIndex = this.headBuffer.startIndex;
				} else {
					// No further data, update tail to reflect
					this.tailBuffer = null;
				}
			}
		}
	}

	@Override
	public int available() throws IOException {

		synchronized (this.lock) {

			// -1 if no available and no further data
			return ((this.available == 0) && (!this.isFurtherData)) ? -1
					: this.available;

		}
	}

	/**
	 * Read buffer.
	 */
	private static class ReadBuffer {

		/**
		 * Data.
		 */
		public final byte[] data;

		/**
		 * Start index within the data.
		 */
		public final int startIndex;

		/**
		 * End index within the data.
		 */
		public final int endIndex;

		/**
		 * Next {@link ReadBuffer}.
		 */
		public ReadBuffer next = null;

		/**
		 * Initiate.
		 * 
		 * @param data
		 *            Data.
		 * @param startIndex
		 *            Start index within the data.
		 * @param endIndex
		 *            End index within the data.
		 */
		public ReadBuffer(byte[] data, int startIndex, int endIndex) {
			this.data = data;
			this.startIndex = startIndex;
			this.endIndex = endIndex;
		}
	}

	/**
	 * {@link BrowseInputStream} implementation.
	 */
	private class BrowseInputStreamImpl extends BrowseInputStream {

		/**
		 * Current {@link ReadBuffer}.
		 */
		public ReadBuffer currentBuffer;

		/**
		 * Current {@link ReadBuffer} index.
		 */
		public int currentBufferIndex = 0;

		/**
		 * {@link BrowseInputStream}.
		 */
		public BrowseInputStreamImpl next = null;

		/**
		 * Initiate.
		 */
		public BrowseInputStreamImpl() {
			this.currentBuffer = ServerInputStreamImpl.this.headBuffer;
			this.currentBufferIndex = ServerInputStreamImpl.this.currentBufferIndex;
		}

		/**
		 * Registers for notification of input data.
		 */
		private void registerForInputData() {

			// Register to wait on data
			BrowseInputStreamImpl currentWait = ServerInputStreamImpl.this.browseWaitingDataHead;
			if (currentWait == null) {
				// Only browser input stream waiting
				ServerInputStreamImpl.this.browseWaitingDataHead = this;

			} else {
				// Append to end of waiting browse input streams
				BrowseInputStreamImpl previousWait = currentWait;
				currentWait = previousWait.next;
				while (currentWait != null) {
					previousWait = currentWait;
					currentWait = currentWait.next;
				}
				previousWait.next = this;
			}
		}

		/*
		 * =================== BrowseInputStream ===========================
		 */

		@Override
		public int read() throws IOException, NoAvailableInputException {

			synchronized (ServerInputStreamImpl.this.lock) {

				for (;;) {

					// Ensure have data
					if (this.currentBuffer == null) {
						if (ServerInputStreamImpl.this.isFurtherData) {
							// Not yet end of stream
							throw new NoAvailableInputException();
						} else {
							// End of stream
							return -1;
						}
					}

					// Determine if data is available
					if (this.currentBufferIndex <= this.currentBuffer.endIndex) {
						// Return the byte
						return this.currentBuffer.data[this.currentBufferIndex++];
					}

					// Obtain the next buffer to start reading
					this.currentBuffer = this.currentBuffer.next;
					if (this.currentBuffer != null) {
						// Provide current buffer index
						this.currentBufferIndex = this.currentBuffer.startIndex;

					} else {
						// Register for notification of input data
						this.registerForInputData();
					}
				}
			}
		}

		@Override
		public int available() throws IOException {

			synchronized (ServerInputStreamImpl.this.lock) {

				// Determine if data is available on current buffer
				if ((this.currentBuffer != null)
						&& (this.currentBufferIndex > this.currentBuffer.endIndex)) {
					// No data available, so move to next buffer
					this.currentBuffer = this.currentBuffer.next;
					if (this.currentBuffer != null) {
						this.currentBufferIndex = this.currentBuffer.startIndex;
					}
				}

				// Calculate the available
				int available = 0;
				ReadBuffer buffer = this.currentBuffer;
				while (buffer != null) {
					// +1 for including end index
					available += ((buffer.endIndex - buffer.startIndex) + 1);
					buffer = buffer.next;
				}

				// Determine if further data
				if ((!ServerInputStreamImpl.this.isFurtherData)
						&& (available == 0)) {
					available = -1; // no further data
				}

				// Return the available
				return available;
			}
		}
	}

}