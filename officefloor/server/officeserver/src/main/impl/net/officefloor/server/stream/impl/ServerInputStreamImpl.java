/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2013 Daniel Sagenschneider
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
package net.officefloor.server.stream.impl;

import java.io.IOException;
import java.io.Serializable;

import net.officefloor.server.stream.BrowseInputStream;
import net.officefloor.server.stream.NoAvailableInputException;
import net.officefloor.server.stream.ServerInputStream;

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
	 *            Object to <code>synchronize</code> on for operations.
	 */
	public ServerInputStreamImpl(Object lock) {
		this.lock = lock;
	}

	/**
	 * Initiate.
	 * 
	 * @param lock
	 *            Object to <code>synchronize</code> on for operations.
	 * @param stateMomento
	 *            Momento containing the state for this
	 *            {@link ServerInputStream}.
	 * @throws IllegalArgumentException
	 *             Should the momento be invalid.
	 */
	public ServerInputStreamImpl(Object lock, Serializable stateMomento)
			throws IllegalArgumentException {
		this(lock);

		// Ensure state momento is valid
		if (!(stateMomento instanceof StateMomento)) {
			throw new IllegalArgumentException("Invalid momento for "
					+ ServerInputStream.class.getSimpleName());
		}
		StateMomento state = (StateMomento) stateMomento;

		// Load the state
		this.headBuffer = state.headBuffer;
		this.currentBufferIndex = state.headBufferIndex;
		this.isFurtherData = false;

		// Provide the available
		this.available = calculateAvailable(this.headBuffer,
				this.currentBufferIndex, this.isFurtherData);
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

			// Ensure not previously flagged that no further data
			if (!this.isFurtherData) {
				throw new IllegalStateException(
						"May not input further data as flagged previously that no further data");
			}

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

	/**
	 * Exports the momento containing the current state of the
	 * {@link ServerInputStream}.
	 * 
	 * @return Momento containing the current state of the
	 *         {@link ServerInputStream}.
	 * @throws NotAllDataAvailableException
	 *             Should all input data not be available.
	 */
	public Serializable exportState() throws NotAllDataAvailableException {

		synchronized (this.lock) {

			// Ensure state complete by no further data
			if (this.isFurtherData) {
				throw new NotAllDataAvailableException(
						ServerInputStream.class.getSimpleName()
								+ " has not finished receiving data.  Can not obtain complete state momento.");
			}

			// Return the state momento
			return new StateMomento(this.headBuffer, this.currentBufferIndex);
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
	 * Momento for the state of this {@link ServerInputStream}.
	 */
	private static class StateMomento implements Serializable {

		/**
		 * Head {@link ReadBuffer}.
		 */
		private final ReadBuffer headBuffer;

		/**
		 * Head {@link ReadBuffer} index.
		 */
		private final int headBufferIndex;

		/**
		 * Initiate.
		 * 
		 * @param headBuffer
		 *            Head {@link ReadBuffer}.
		 * @param headBufferIndex
		 *            Head {@link ReadBuffer} inde.
		 */
		public StateMomento(ReadBuffer headBuffer, int headBufferIndex) {
			this.headBuffer = headBuffer;
			this.headBufferIndex = headBufferIndex;
		}
	}

	/**
	 * Read buffer.
	 */
	private static class ReadBuffer implements Serializable {

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

				// Return the available
				return calculateAvailable(this.currentBuffer,
						this.currentBufferIndex,
						ServerInputStreamImpl.this.isFurtherData);
			}
		}
	}

	/**
	 * Calculates available.
	 * 
	 * @param currentBuffer
	 *            Current {@link ReadBuffer}.
	 * @param currentBufferIndex
	 *            Current {@link ReadBuffer} index.
	 * @param isFurtherData
	 *            Indicates if further data.
	 * @return Available data.
	 */
	private static int calculateAvailable(ReadBuffer currentBuffer,
			int currentBufferIndex, boolean isFurtherData) {

		// Determine if data is available on current buffer
		if ((currentBuffer != null)
				&& (currentBufferIndex > currentBuffer.endIndex)) {
			// No data available, so move to next buffer
			currentBuffer = currentBuffer.next;
			if (currentBuffer != null) {
				currentBufferIndex = currentBuffer.startIndex;
			}
		}

		// Calculate the available
		int available = 0;
		ReadBuffer buffer = currentBuffer;
		while (buffer != null) {
			// +1 for including end index
			available += ((buffer.endIndex - buffer.startIndex) + 1);
			buffer = buffer.next;
		}

		// Determine if further data
		if ((!isFurtherData) && (available == 0)) {
			available = -1; // no further data
		}

		// Return the available
		return available;
	}

}