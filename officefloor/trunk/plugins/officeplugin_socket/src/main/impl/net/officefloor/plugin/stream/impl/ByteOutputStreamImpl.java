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
import java.util.LinkedList;
import java.util.Queue;

import net.officefloor.plugin.socket.server.protocol.WriteBuffer;
import net.officefloor.plugin.stream.ByteOutputStream;
import net.officefloor.plugin.stream.WriteBufferReceiver;

/**
 * {@link ByteOutputStream} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class ByteOutputStreamImpl extends ByteOutputStream {

	/**
	 * {@link WriteBufferReceiver}.
	 */
	private final WriteBufferReceiver receiver;

	/**
	 * Receiver buffer size.
	 */
	private final int receiverBufferSize;

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
	 * @param receiverBufferSize
	 *            Receiver buffer size.
	 */
	public ByteOutputStreamImpl(WriteBufferReceiver receiver,
			int receiverBufferSize) {
		this.receiver = receiver;
		this.receiverBufferSize = receiverBufferSize;
	}

	/*
	 * ==================== ByteOutputStream =========================
	 */

	@Override
	public void write(ByteBuffer cachedBuffer) throws IOException {
		// TODO implement ByteOutputStream.write
		throw new UnsupportedOperationException(
				"TODO implement ByteOutputStream.write");
	}

	@Override
	public synchronized void write(int b) throws IOException {

		// Ensure have current data
		if (this.currentData == null) {
			// Provide new data for writing
			this.currentData = new byte[this.receiverBufferSize];

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

	@Override
	public synchronized void flush() throws IOException {

		// Determine the number of buffers to write
		int buffersToWrite = this.dataToWrite.size()
				+ (this.currentData == null ? 0 : 1);

		// Ensure there is data to write
		if (buffersToWrite == 0) {
			return;
		}

		// Create the array of buffers to write
		WriteBuffer[] buffers = new WriteBuffer[buffersToWrite];

		// TODO provide buffers from queue

		// Provide last buffer
		buffers[buffers.length - 1] = this.receiver.createWriteBuffer(
				this.currentData, this.nextCurrentDataIndex);

		// Flush the data to the receiver
		this.receiver.writeData(buffers);
	}

}