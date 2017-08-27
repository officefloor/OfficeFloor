/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2017 Daniel Sagenschneider
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
package net.officefloor.server;

import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.Pipe;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.officefloor.server.stream.BufferPool;
import net.officefloor.server.stream.StreamBuffer;

/**
 * Services the {@link Socket}.
 * 
 * @author Daniel Sagenschneider
 */
public class SocketServicer implements Runnable {

	/**
	 * {@link Logger}.
	 */
	private static final Logger LOGGER = Logger.getLogger(SocketServicer.class.getName());

	/**
	 * {@link BufferPool}.
	 */
	private final BufferPool<ByteBuffer> bufferPool;

	/**
	 * {@link Selector}.
	 */
	private final Selector selector;

	/**
	 * {@link Pipe} to invoke to shutdown servicing.
	 */
	private final Pipe shutdownPipe;

	/**
	 * Indicates whether to shutdown.
	 */
	private boolean isShutdown = false;

	/**
	 * Instantiate.
	 * 
	 * @param bufferPool
	 *            {@link BufferPool}.
	 * @throws IOException
	 *             If fails to establish necessary {@link Socket} and
	 *             {@link Pipe} facilities.
	 */
	public SocketServicer(BufferPool<ByteBuffer> bufferPool) throws IOException {
		this.bufferPool = bufferPool;

		// Create the selector
		this.selector = Selector.open();

		// Create pipe to listen for shutdown
		this.shutdownPipe = Pipe.open();
		this.shutdownPipe.source().configureBlocking(false);
		this.shutdownPipe.source().register(this.selector, SelectionKey.OP_READ,
				new ShutdownReadHandler(this.shutdownPipe.source()));
	}

	/**
	 * Shuts down this {@link SocketServicer}.
	 * 
	 * @throws IOException
	 *             If fails to notify of shutdown.
	 */
	public void shutdown() throws IOException {
		// Send message to shutdown
		this.shutdownPipe.sink().write(ByteBuffer.wrap(new byte[] { 1 }));
	}

	/*
	 * =================== Runnable =========================
	 */

	@Override
	public void run() {

		// Loop until shutdown
		while (!this.isShutdown) {

			// Select keys
			try {
				this.selector.select(1000); // 1 second
			} catch (IOException ex) {
				// Should not occur
				LOGGER.log(Level.SEVERE, "Selector failure", ex);
				return; // fatal error, so can not continue
			}

			// Obtain the selected keys
			Set<SelectionKey> selectedKeys = this.selector.selectedKeys();

			// Service the selected keys
			NEXT_KEY: for (SelectionKey selectedKey : selectedKeys) {

				// Obtain the ready operations
				int readyOps = selectedKey.readyOps();

				// Determine if read content
				if ((readyOps & SelectionKey.OP_READ) != 0) {

					// Obtain the read handler
					AbstractReadHandler handler = (AbstractReadHandler) selectedKey.attachment();

					// Keep reading data until empty socket buffer
					boolean isFurtherDataToRead = true;
					while (isFurtherDataToRead) {

						// Ensure have buffer to handle read
						if ((handler.readBuffer == null) || (handler.readBuffer.getPooledBuffer().remaining() == 0)) {
							// Require a new buffer
							handler.readBuffer = this.bufferPool.getPooledStreamBuffer();
						}

						// Obtain the byte buffer
						ByteBuffer buffer = handler.readBuffer.getPooledBuffer();

						// Read content from channel
						int bytesRead = 0;
						IOException readFailure = null;
						try {
							bytesRead = handler.channel.read(buffer);
						} catch (IOException ex) {
							readFailure = ex;
						}

						// Determine if closed connection or in error
						if ((bytesRead < 0) || (readFailure != null)) {
							// Connection failed, so terminate
							try {
								handler.channel.close();
							} catch (ClosedChannelException ex) {
								// Ignore, already closed
							} catch (IOException ex) {
								LOGGER.log(Level.WARNING, "Failed closing connection", ex);
							} finally {
								// Ensure cancel the key
								selectedKey.cancel();
							}
							continue NEXT_KEY;
						}

						// Handle the read
						handler.handleRead();

						// Determine if further data
						if (buffer.remaining() != 0) {
							// Buffer did not fill, so no further data
							isFurtherDataToRead = false;
						}
					}
				}
			}

			// Clear the selected keys as now serviced
			selectedKeys.clear();
		}
	}

	/**
	 * Abstract functionality for handling reads.
	 */
	public abstract class AbstractReadHandler {

		/**
		 * Current {@link StreamBuffer} for read content.
		 */
		public StreamBuffer<ByteBuffer> readBuffer;

		/**
		 * {@link ReadableByteChannel}.
		 */
		private final ReadableByteChannel channel;

		/**
		 * Instantiate.
		 * 
		 * @param channel
		 *            {@link ReadableByteChannel}.
		 */
		public AbstractReadHandler(ReadableByteChannel channel) {
			this.channel = channel;
		}

		/**
		 * Handles the read.
		 */
		public abstract void handleRead();

	}

	/**
	 * Shutdown handler.
	 */
	private class ShutdownReadHandler extends AbstractReadHandler {

		/**
		 * Instantiate.
		 * 
		 * @param channel
		 *            {@link ReadableByteChannel}.
		 */
		public ShutdownReadHandler(ReadableByteChannel channel) {
			super(channel);
		}

		/*
		 * ============== AbstractReadHandler ================
		 */

		@Override
		public void handleRead() {

			// Release buffer (as content not important, only notification)
			this.readBuffer.release();

			// Flag to shutdown
			SocketServicer.this.isShutdown = true;

			// TODO consider closing connections
		}
	}

}