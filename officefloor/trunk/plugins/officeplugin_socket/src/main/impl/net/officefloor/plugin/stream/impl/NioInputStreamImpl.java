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
	private final Queue<byte[]> data = new LinkedList<byte[]>();

	/**
	 * Current data being read.
	 */
	private byte[] currentData = null;

	/**
	 * Current data index.
	 */
	private int currentDataIndex = 0;

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
	 * @param isFurtherData
	 *            Indicates if further data is going to be made available.
	 */
	public void queueData(byte[] data, boolean isFurtherData) {

		synchronized (this.lock) {

			// Ensure have data
			if (data != null) {

				// Queue further data
				if (this.currentData == null) {
					this.currentData = data;
				} else {
					this.data.add(data);
				}

				// Increment the number of bytes available
				this.available += data.length;
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
				if (this.currentData == null) {
					if (this.isFurtherData) {
						// Not yet end of stream
						throw new NoAvailableInputException();
					} else {
						// End of stream
						return -1;
					}
				}

				// Determine if data is available
				if (this.currentDataIndex < this.currentData.length) {
					// Return the byte (keeping available up to date)
					this.available--;
					return this.currentData[this.currentDataIndex++];
				}

				// Obtain the next data to start reading
				this.currentData = this.data.poll();
				this.currentDataIndex = 0;
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

}