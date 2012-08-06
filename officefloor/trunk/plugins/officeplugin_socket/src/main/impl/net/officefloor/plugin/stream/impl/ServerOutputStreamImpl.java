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
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.util.LinkedList;
import java.util.Queue;

import net.officefloor.plugin.socket.server.protocol.WriteBuffer;
import net.officefloor.plugin.stream.ServerOutputStream;
import net.officefloor.plugin.stream.WriteBufferReceiver;

/**
 * {@link ServerOutputStream} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class ServerOutputStreamImpl extends ServerOutputStream {

	/**
	 * {@link WriteBufferReceiver}.
	 */
	private final WriteBufferReceiver receiver;

	/**
	 * Send buffer size.
	 */
	private final int sendBufferSize;

	/**
	 * Current data.
	 */
	private byte[] currentData;

	/**
	 * Index to write next byte to current data.
	 */
	private int nextCurrentDataIndex = 0;

	/**
	 * {@link Queue} of {@link WriteBuffer} instances to be written.
	 */
	private final Queue<WriteBuffer> dataToWrite = new LinkedList<WriteBuffer>();

	/**
	 * Initiate.
	 * 
	 * @param receiver
	 *            {@link WriteBufferReceiver}.
	 * @param sendBufferSize
	 *            Send buffer size.
	 */
	public ServerOutputStreamImpl(WriteBufferReceiver receiver,
			int sendBufferSize) {
		this.receiver = receiver;
		this.sendBufferSize = sendBufferSize;
	}

	/**
	 * Ensures that not closed.
	 * 
	 * @throws ClosedChannelException
	 *             If closed.
	 */
	private void ensureNotClosed() throws ClosedChannelException {
		if (this.receiver.isClosed()) {
			throw new ClosedChannelException();
		}
	}

	/**
	 * Clears the content.
	 * 
	 * @throws IOException
	 *             If content already provided to {@link WriteBufferReceiver}.
	 */
	public void clear() throws IOException {
		// TODO implement ServerOutputStreamImpl.clear()
		System.err.println("TODO implement ServerOutputStreamImpl.clear()");
	}

	/*
	 * ==================== ByteOutputStream =========================
	 */

	@Override
	public void write(ByteBuffer cachedBuffer) throws IOException {

		synchronized (this.receiver.getLock()) {

			// Move current data for writing
			if (this.currentData != null) {

				// Append the current data for writing
				WriteBuffer buffer = this.receiver.createWriteBuffer(
						this.currentData, this.nextCurrentDataIndex);
				this.dataToWrite.add(buffer);
				this.currentData = null; // now no current data
			}

			// Append the cached buffer
			WriteBuffer buffer = this.receiver.createWriteBuffer(cachedBuffer);
			this.dataToWrite.add(buffer);
		}
	}

	@Override
	public void write(int b) throws IOException {

		synchronized (this.receiver.getLock()) {

			// Ensure not closed
			this.ensureNotClosed();

			// Ensure have current data
			if (this.currentData == null) {
				// Provide new data for writing
				this.currentData = new byte[this.sendBufferSize];

				// Write byte immediately
				this.currentData[0] = (byte) b;
				this.nextCurrentDataIndex = 1; // after first byte

				// Data written
				return;
			}

			// TODO handle current data full

			// Write to next index
			this.currentData[this.nextCurrentDataIndex++] = (byte) b;
		}
	}

	@Override
	public void flush() throws IOException {

		synchronized (this.receiver.getLock()) {

			// Only check if closed
			this.ensureNotClosed();

			// Determine the number of buffers to write
			int dataToWriteSize = this.dataToWrite.size();
			boolean isCurrentData = (this.currentData != null);
			int buffersToWrite = dataToWriteSize + (isCurrentData ? 1 : 0);

			// Ensure there is data to write
			if (buffersToWrite == 0) {
				return;
			}

			// Create the array of buffers to write
			WriteBuffer[] buffers = new WriteBuffer[buffersToWrite];

			// Provide buffers from queue
			for (int i = 0; i < dataToWriteSize; i++) {
				buffers[i] = this.dataToWrite.poll();
			}

			// Provide current buffer
			if (isCurrentData) {
				buffers[buffers.length - 1] = this.receiver.createWriteBuffer(
						this.currentData, this.nextCurrentDataIndex);
			}

			// Clear data as about to be written
			this.currentData = null;
			this.dataToWrite.clear();

			// Flush the data to the receiver
			this.receiver.writeData(buffers);
		}
	}

	@Override
	public void close() throws IOException {

		synchronized (this.receiver.getLock()) {

			// Do nothing if already closed
			if (this.receiver.isClosed()) {
				return;
			}

			// Flush any data to receiver
			this.flush();

			// Close the receiver
			this.receiver.close();
		}
	}

}