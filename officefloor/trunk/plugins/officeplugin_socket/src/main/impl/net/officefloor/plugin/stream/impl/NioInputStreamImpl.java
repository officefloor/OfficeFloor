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
import java.util.LinkedList;
import java.util.Queue;

import net.officefloor.plugin.stream.NioInputStream;
import net.officefloor.plugin.stream.NoAvailableInputException;

/**
 * {@link NioInputStream} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class NioInputStreamImpl extends NioInputStream {

	/**
	 * Object to <code>synchronize</code> on for operaterations.
	 */
	private final Object lock;

	/**
	 * {@link Queue} of data for reading.
	 */
	private final Queue<ReadBuffer> data = new LinkedList<ReadBuffer>();

	/**
	 * Current {@link ReadBuffer} being read.
	 */
	private ReadBuffer currentBuffer = null;

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
	 * Initiate.
	 * 
	 * @param lock
	 *            Object to <code>synchronize</code> on for operaterations.
	 */
	public NioInputStreamImpl(Object lock) {
		this.lock = lock;
	}

	/**
	 * Queues data for reading.
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
	public void queueData(byte[] data, int startIndex, int endIndex,
			boolean isFurtherData) {

		synchronized (this.lock) {

			// Ensure have data
			if (data != null) {

				// Create the read buffer
				ReadBuffer readBuffer = new ReadBuffer(data, startIndex,
						endIndex);

				// Queue further data
				if (this.currentBuffer == null) {
					this.currentBuffer = readBuffer;
					this.currentBufferIndex = this.currentBuffer.startIndex;
				} else {
					this.data.add(readBuffer);
				}

				// Increment available bytes (+1 for last byte indexed)
				this.available += ((endIndex - startIndex) + 1);
			}

			// Indicate if further data
			this.isFurtherData = isFurtherData;
		}
	}

	/*
	 * ====================== NioInputStream =======================
	 */

	@Override
	public int read() throws IOException, NoAvailableInputException {

		synchronized (this.lock) {

			for (;;) {

				// Ensure have data
				if (this.currentBuffer == null) {
					if (this.isFurtherData) {
						// Not yet end of stream
						throw new NoAvailableInputException();
					} else {
						// End of stream
						return -1;
					}
				}

				// Determine if data is available
				if (this.currentBufferIndex <= this.currentBuffer.endIndex) {
					// Return the byte (keeping available up to date)
					this.available--;
					return this.currentBuffer.data[this.currentBufferIndex++];
				}

				// Obtain the next buffer to start reading
				this.currentBuffer = this.data.poll();
				if (this.currentBuffer != null) {
					this.currentBufferIndex = this.currentBuffer.startIndex;
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

}