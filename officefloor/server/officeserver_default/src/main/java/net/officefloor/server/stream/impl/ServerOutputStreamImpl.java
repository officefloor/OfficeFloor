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
import java.io.Writer;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.util.LinkedList;
import java.util.Queue;

import net.officefloor.server.http.protocol.WriteBuffer;
import net.officefloor.server.http.protocol.WriteBufferEnum;
import net.officefloor.server.stream.ServerOutputStream;
import net.officefloor.server.stream.WriteBufferReceiver;

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
	 * Indicates if flushing the {@link Writer}.
	 */
	private boolean isFlushingWriter = false;

	/**
	 * Indicates if the data is flushed.
	 */
	private boolean isDataFlushed = false;

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
	 * Initiate.
	 * 
	 * @param receiver
	 *            {@link WriteBufferReceiver}.
	 * @param sendBufferSize
	 *            Send buffer size.
	 * @param stateMomento
	 *            Momento containing the state for this
	 *            {@link ServerOutputStream}.
	 */
	public ServerOutputStreamImpl(WriteBufferReceiver receiver,
			int sendBufferSize, Serializable stateMomento) {
		this(receiver, sendBufferSize);

		// Ensure valid momento
		if (!(stateMomento instanceof StateMomento)) {
			throw new IllegalArgumentException("Invalid momento for "
					+ ServerOutputStream.class.getSimpleName());
		}
		StateMomento state = (StateMomento) stateMomento;

		// Load the state
		this.currentData = state.data;
		this.nextCurrentDataIndex = this.currentData.length;
	}

	/**
	 * Exports a momento for the current state of this
	 * {@link ServerOutputStream}.
	 * 
	 * @param writer
	 *            Optional {@link Writer} to flush its contents before obtaining
	 *            the state for the momento.
	 * @return Momento for the current state of this {@link ServerOutputStream}.
	 * @throws DataWrittenException
	 *             Should data have already been written to the client.
	 * @throws IOException
	 *             If fails to flush data of {@link Writer}.
	 */
	public Serializable exportState(Writer writer) throws DataWrittenException,
			IOException {

		synchronized (this.receiver.getWriteLock()) {

			// Flush writer data for state
			if (writer != null) {
				try {
					this.isFlushingWriter = true;
					writer.flush();
				} finally {
					this.isFlushingWriter = false;
				}
			}

			// Ensure data has not yet been flushed
			if (this.isDataFlushed) {
				throw new DataWrittenException(
						ServerOutputStream.class.getSimpleName()
								+ " has written data to client.  Can not create State momento.");
			}

			// Calculate the amount of data
			int totalBytes = this.nextCurrentDataIndex;
			for (WriteBuffer buffer : this.dataToWrite) {
				WriteBufferEnum type = buffer.getType();
				switch (type) {
				case BYTE_ARRAY:
					totalBytes += buffer.length();
					break;
				case BYTE_BUFFER:
					totalBytes += buffer.getDataBuffer().remaining();
					break;
				default:
					throw new IllegalStateException(
							"Unknown write buffer type " + type);
				}
			}

			// Create the data buffer for state momento
			byte[] data = new byte[totalBytes];
			int nextPosition = 0;

			// Load the data to write
			int length;
			for (WriteBuffer buffer : this.dataToWrite) {
				WriteBufferEnum type = buffer.getType();
				switch (type) {
				case BYTE_ARRAY:
					length = buffer.length();
					System.arraycopy(buffer.getData(), 0, data, nextPosition,
							length);
					break;
				case BYTE_BUFFER:
					ByteBuffer duplicate = buffer.getDataBuffer().duplicate();
					length = duplicate.remaining();
					duplicate.get(data, nextPosition, length);
					break;
				default:
					throw new IllegalStateException(
							"Unknown write buffer type " + type);
				}
				nextPosition += length;
			}

			// Load the current data (if available)
			if ((this.currentData != null) && (this.nextCurrentDataIndex > 0)) {
				System.arraycopy(this.currentData, 0, data, nextPosition,
						this.nextCurrentDataIndex);
			}

			// Create and return the state momento
			return new StateMomento(data);
		}
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

		synchronized (this.receiver.getWriteLock()) {

			// Clear the data
			this.currentData = null;
			this.dataToWrite.clear();

		}
	}

	/*
	 * ==================== ByteOutputStream =========================
	 */

	@Override
	public void write(ByteBuffer cachedBuffer) throws IOException {

		synchronized (this.receiver.getWriteLock()) {

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

		synchronized (this.receiver.getWriteLock()) {

			// Ensure not closed
			this.ensureNotClosed();

			for (;;) {

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

				// Determine if space in current buffer
				if (this.nextCurrentDataIndex < this.currentData.length) {
					// Write to next index of current buffer
					this.currentData[this.nextCurrentDataIndex++] = (byte) b;
					return; // data written
				}

				// Write data to new buffer
				this.dataToWrite.add(this.receiver.createWriteBuffer(
						this.currentData, this.currentData.length));
				this.currentData = null; // clear to have new current data

			}
		}
	}

	@Override
	public void flush() throws IOException {

		synchronized (this.receiver.getWriteLock()) {

			// Do nothing if flushing writer
			if (this.isFlushingWriter) {
				return;
			}

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

			// Flag data is now flushed
			this.isDataFlushed = true;

			// Flush the data to the receiver
			this.receiver.writeData(buffers);
		}
	}

	@Override
	public void close() throws IOException {

		synchronized (this.receiver.getWriteLock()) {

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

	/**
	 * Momento to hold current state of this {@link ServerOutputStream}.
	 */
	private static class StateMomento implements Serializable {

		/**
		 * Data.
		 */
		private final byte[] data;

		/**
		 * Initiate.
		 * 
		 * @param data
		 *            Data written.
		 */
		public StateMomento(byte[] data) {
			this.data = data;
		}
	}

}