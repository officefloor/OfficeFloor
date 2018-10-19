/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package net.officefloor.server;

import java.io.IOException;
import java.net.BindException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.Pipe;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.officefloor.server.RequestHandler.Execution;
import net.officefloor.server.stream.StreamBuffer;
import net.officefloor.server.stream.StreamBuffer.FileBuffer;
import net.officefloor.server.stream.StreamBufferPool;

/**
 * Manages the {@link Socket} interaction.
 * 
 * @author Daniel Sagenschneider
 */
public class SocketManager {

	/**
	 * Default {@link ServerSocket} backlog size.
	 */
	public static final int DEFAULT_SERVER_SOCKET_BACKLOG_SIZE = 8192;

	/**
	 * {@link ThreadLocal} to determine if {@link SocketListener} {@link Thread}.
	 */
	private static final ThreadLocal<SocketListener> threadSocketLister = new ThreadLocal<>();

	/**
	 * {@link Logger}.
	 */
	private static final Logger LOGGER = Logger.getLogger(SocketManager.class.getName());

	/**
	 * {@link ByteBuffer} to send to on {@link Pipe} to notify.
	 */
	private static final ByteBuffer NOTIFY_BUFFER = ByteBuffer.wrap(new byte[] { 1 });

	/**
	 * {@link SocketListener} instances.
	 */
	private final SocketListener[] listeners;

	/**
	 * Index of the next {@link SocketListener} for handling the listening of a
	 * {@link ServerSocketChannel}.
	 */
	private int nextServerSocketListener = 0;

	/**
	 * Index of the next {@link SocketListener} for a new {@link Socket}.
	 */
	private final AtomicInteger nextSocketListener = new AtomicInteger(0);

	/**
	 * Bound {@link ServerSocket} instances.
	 */
	private final List<ServerSocket> boundServerSockets = new ArrayList<>();

	/**
	 * Instantiate.
	 * 
	 * @param listenerCount           Number of {@link SocketListener} instances.
	 * @param socketReceiveBufferSize Receive buffer size for the {@link Socket}.
	 * @param maxReadsOnSelect        Maximum number of reads per
	 *                                {@link SocketChannel} per select. The
	 *                                {@link Selector} has locking overheads that
	 *                                slow performance. By undertaking multiple
	 *                                reads on the {@link SocketChannel} it makes
	 *                                draining and servicing more efficient (and
	 *                                subsequently faster). This also allows the
	 *                                {@link StreamBuffer} sizes to be smaller than
	 *                                the receive {@link Socket} buffer size (but
	 *                                still maintain efficiency).
	 * @param bufferPool              {@link StreamBufferPool}.
	 * @param socketSendBufferSize    Send buffer size for the {@link Socket}.
	 * @throws IOException If fails to initialise {@link Socket} management.
	 */
	public SocketManager(int listenerCount, int socketReceiveBufferSize, int maxReadsOnSelect,
			StreamBufferPool<ByteBuffer> bufferPool, int socketSendBufferSize) throws IOException {
		this.listeners = new SocketListener[listenerCount];
		for (int i = 0; i < listeners.length; i++) {
			listeners[i] = new SocketListener(socketReceiveBufferSize, maxReadsOnSelect, bufferPool,
					socketSendBufferSize);
		}
	}

	/**
	 * Obtains the {@link StreamBufferPool} used by this {@link SocketManager}.
	 * 
	 * @return {@link StreamBufferPool} used by this {@link SocketManager}.
	 */
	public final StreamBufferPool<ByteBuffer> getStreamBufferPool() {
		return this.listeners[0].bufferPool;
	}

	/**
	 * Terminates the {@link SelectionKey}.
	 * 
	 * @param selectionKey   {@link SelectionKey}.
	 * @param socketListener {@link SocketListener}.
	 * @param cause          Possible cause of terminating the connection. May be
	 *                       <code>null</code>.
	 */
	private static final void terminteSelectionKey(SelectionKey selectionKey, SocketListener socketListener,
			Throwable cause) {

		// Determine (and possibly log) cause of termination
		if (cause != null) {
			try {
				throw cause;
			} catch (CancelledKeyException | ClosedChannelException ex) {
				// terminating, so ignore already closed
			} catch (IOException ex) {
				// Issue with connection, clean up and continue
				String message = ex.getMessage();
				switch (message == null ? "" : message) {
				case "Connection reset by peer":
				case "Broken pipe":
					break;
				default:
					LOGGER.log(Level.WARNING, "I/O failure with connection", ex);
				}
			} catch (Throwable ex) {
				LOGGER.log(Level.WARNING, "Failure with connection", ex);
			}
		}

		// Terminate the connection
		try {
			selectionKey.channel().close();
		} catch (IOException ex) {
			// consider already closed
		} finally {
			try {
				// Ensure cancel the key
				selectionKey.cancel();
			} finally {

				// Release the stream buffers (if safe, otherwise GC)
				if (socketListener.isSocketListenerThread()) {
					Object attachment = selectionKey.attachment();
					if (attachment instanceof AbstractReadHandler) {
						AbstractReadHandler handler = (AbstractReadHandler) attachment;
						handler.releaseStreamBuffers();
					}
				} else {
					// Log that unable to release buffers
					LOGGER.log(Level.WARNING, "Unable to release buffers as not on socket thread ("
							+ Thread.currentThread().getName() + ")");
				}
			}
		}
	}

	/**
	 * Obtains the {@link Runnable} instances to be executed for this
	 * {@link SocketManager}.
	 * 
	 * @return {@link Runnable} instances to be executed for this
	 *         {@link SocketManager}.
	 */
	public final Runnable[] getRunnables() {
		return this.listeners;
	}

	/**
	 * Binds a {@link ServerSocket} to be serviced.
	 *
	 * @param                         <R> Request type.
	 * @param port                    Port for the {@link ServerSocket}.
	 * @param serverSocketDecorator   Optional {@link ServerSocketDecorator}. May be
	 *                                <code>null</code>.
	 * @param acceptedSocketDecorator Optional {@link AcceptedSocketDecorator}. May
	 *                                be <code>null</code>.
	 * @param socketServicerFactory   {@link SocketServicerFactory} to service
	 *                                accepted connections.
	 * @param requestServicerFactory  {@link RequestServicerFactory} to service
	 *                                requests on the {@link Socket}.
	 * @throws IOException If fails to bind the {@link ServerSocket}.
	 */
	public synchronized final <R> void bindServerSocket(int port, ServerSocketDecorator serverSocketDecorator,
			AcceptedSocketDecorator acceptedSocketDecorator, SocketServicerFactory<R> socketServicerFactory,
			RequestServicerFactory<R> requestServicerFactory) throws IOException {

		// Spread acceptances listening across the listeners
		int next = this.nextServerSocketListener;
		this.nextServerSocketListener = (this.nextServerSocketListener + 1) % this.listeners.length;

		// Register server socket listening
		ServerSocket serverSocket = this.listeners[next].bindServerSocket(port, serverSocketDecorator,
				acceptedSocketDecorator, socketServicerFactory, requestServicerFactory);
		this.boundServerSockets.add(serverSocket);
	}

	/**
	 * Shuts down this {@link SocketManager}. This involves closing all
	 * {@link Socket} instances being managed and stopping the
	 * {@link SocketListener} instances.
	 * 
	 * @throws IOException If fails to shutdown this {@link SocketManager}.
	 */
	public synchronized final void shutdown() throws IOException {

		// Close the server sockets
		for (ServerSocket socket : this.boundServerSockets) {
			socket.close();
		}

		// Stop listening on sockets
		for (int i = 0; i < this.listeners.length; i++) {
			this.listeners[i].shutdown();
		}

		// Wait until all listeners are shutdown
		long startTime = System.currentTimeMillis();
		for (int i = 0; i < this.listeners.length; i++) {
			while (this.listeners[i].selector.isOpen()) {

				// Determine if timed out
				if (System.currentTimeMillis() > (startTime + 10000)) {
					throw new IOException(
							"Timed out waiting on " + SocketListener.class.getSimpleName() + " to shutdown");
				}

				// Allow some time to shutdown
				try {
					this.wait(10);
				} catch (InterruptedException ex) {
					LOGGER.log(Level.WARNING, "Thread interrupted waiting on listeners to shutdown");
				}
			}
		}
	}

	/**
	 * Manages an accepted {@link Socket}.
	 * 
	 * @param acceptedSocket {@link AcceptedSocketServicer}.
	 * @throws IOException If fails to managed the {@link AcceptedSocketServicer}.
	 */
	private final <R> void manageAcceptedSocket(AcceptedSocket<R> acceptedSocket) throws IOException {

		// Spread the connections across the listeners
		int next = this.nextSocketListener.getAndAccumulate(1,
				(prev, increment) -> (prev + increment) % this.listeners.length);

		// Register connection with socket listener
		this.listeners[next].acceptSocketHandler.serviceAcceptedSocket(acceptedSocket);
	}

	/**
	 * Services the {@link Socket}.
	 * 
	 * @author Daniel Sagenschneider
	 */
	private class SocketListener implements Runnable {

		/**
		 * {@link StreamBufferPool}.
		 */
		private final StreamBufferPool<ByteBuffer> bufferPool;

		/**
		 * {@link Socket} receive buffer size.
		 */
		private final int socketReceiveBufferSize;

		/**
		 * Maximum number of reads per {@link SocketChannel} per select.
		 */
		private final int maxReadsOnSelect;

		/**
		 * {@link Socket} send buffer size.
		 */
		private final int socketSendBufferSize;

		/**
		 * {@link Selector}.
		 */
		private final Selector selector;

		/**
		 * {@link AcceptSocketHandler}.
		 */
		private final AcceptSocketHandler acceptSocketHandler;

		/**
		 * {@link ExecutionHandler}.
		 */
		private final ExecutionHandler executionHandler;

		/**
		 * {@link SafeWriteSocketHandler}.
		 */
		private final SafeWriteSocketHandler safeWriteSocketHandler;

		/**
		 * {@link BulkFlushWritesHandler}.
		 */
		private final BulkFlushWritesHandler bulkFlushWritesHandler;

		/**
		 * {@link SafeCloseConnectionHandler}.
		 */
		private final SafeCloseConnectionHandler safeCloseConnectionHandler;

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
		 * @param socketReceiveBufferSize Receive buffer size for the {@link Socket}.
		 * @param maxReadsOnSelect        Maximum number of reads per
		 *                                {@link SocketChannel} per select.
		 * @param bufferPool              {@link StreamBufferPool}.
		 * @param socketSendBufferSize    Send buffer size for the {@link Socket}.
		 * @throws IOException If fails to establish necessary {@link Socket} and
		 *                     {@link Pipe} facilities.
		 */
		private SocketListener(int socketReceiveBufferSize, int maxReadsOnSelect,
				StreamBufferPool<ByteBuffer> bufferPool, int socketSendBufferSize) throws IOException {
			this.socketReceiveBufferSize = socketReceiveBufferSize;
			this.maxReadsOnSelect = maxReadsOnSelect;
			this.bufferPool = bufferPool;
			this.socketSendBufferSize = socketReceiveBufferSize;

			// Create the selector
			this.selector = Selector.open();

			// Create pipe to listen for accepted sockets
			Pipe acceptedSocketPipe = Pipe.open();
			acceptedSocketPipe.source().configureBlocking(false);
			this.acceptSocketHandler = new AcceptSocketHandler(acceptedSocketPipe, this);
			acceptedSocketPipe.source().register(this.selector, SelectionKey.OP_READ, this.acceptSocketHandler);

			// Create pipe to lister for executions
			Pipe executionPipe = Pipe.open();
			executionPipe.source().configureBlocking(false);
			this.executionHandler = new ExecutionHandler(executionPipe);
			executionPipe.source().register(this.selector, SelectionKey.OP_READ, this.executionHandler);

			// Create pipe to listen for safe socket writing
			Pipe safeWriteSocketPipe = Pipe.open();
			safeWriteSocketPipe.source().configureBlocking(false);
			this.safeWriteSocketHandler = new SafeWriteSocketHandler(safeWriteSocketPipe);
			safeWriteSocketPipe.source().register(this.selector, SelectionKey.OP_READ, this.safeWriteSocketHandler);

			// Create pipe to bulk flush writes
			Pipe bulkFlushWritesPipe = Pipe.open();
			bulkFlushWritesPipe.source().configureBlocking(false);
			this.bulkFlushWritesHandler = new BulkFlushWritesHandler(bulkFlushWritesPipe);
			bulkFlushWritesPipe.source().register(this.selector, SelectionKey.OP_READ, this.bulkFlushWritesHandler);

			// Create pipe to listen for connection close
			Pipe safeCloseConectionPipe = Pipe.open();
			safeCloseConectionPipe.source().configureBlocking(false);
			this.safeCloseConnectionHandler = new SafeCloseConnectionHandler(safeCloseConectionPipe);
			safeCloseConectionPipe.source().register(this.selector, SelectionKey.OP_READ,
					this.safeCloseConnectionHandler);

			// Create pipe to listen for shutdown
			this.shutdownPipe = Pipe.open();
			this.shutdownPipe.source().configureBlocking(false);
			this.shutdownPipe.source().register(this.selector, SelectionKey.OP_READ,
					new ShutdownReadHandler(this.shutdownPipe.source(), this));
		}

		/**
		 * Binds the {@link SocketServicer} to the port.
		 * 
		 * @param port                    Port to bind the {@link SocketServicer}.
		 * @param serverSocketDecorator   Optional {@link ServerSocketDecorator}. May be
		 *                                <code>null</code>.
		 * @param acceptedSocketDecorator Optional {@link AcceptedSocketDecorator}. May
		 *                                be <code>null</code>.
		 * @param socketServicerFactory   {@link SocketServicerFactory}.
		 * @param requestServicerFactory  {@link RequestServicerFactory}.
		 * @return Bound {@link ServerSocket}.
		 * @throws IOException If fails to bind the {@link ServerSocket}.
		 */
		private final <R> ServerSocket bindServerSocket(int port, ServerSocketDecorator serverSocketDecorator,
				AcceptedSocketDecorator acceptedSocketDecorator, SocketServicerFactory<R> socketServicerFactory,
				RequestServicerFactory<R> requestServicerFactory) throws IOException {

			// Create the port socket address
			InetSocketAddress portAddress = new InetSocketAddress(port);

			// Quick start/stops can not clean socket so allow retry
			ServerSocketChannel channel = null;
			ServerSocket socket = null;
			BindException exception = null;
			int attempt = 0;
			do {
				// Create the Server Socket
				channel = ServerSocketChannel.open();
				try {
					channel.configureBlocking(false);
					socket = channel.socket();
					socket.setReuseAddress(true);
					socket.setReceiveBufferSize(this.socketReceiveBufferSize);
					int serverSocketBackLogSize = DEFAULT_SERVER_SOCKET_BACKLOG_SIZE;
					if (serverSocketDecorator != null) {
						// Override the defaults
						serverSocketBackLogSize = serverSocketDecorator.decorate(socket);
					}

					// Bind the Server Socket
					socket.bind(portAddress, serverSocketBackLogSize);

				} catch (BindException ex) {
					exception = ex;

					// Ensure clean up
					channel.close();
					socket.close();
					socket = null;

					// Allow some time for address to release
					try {
						Thread.sleep(10);
					} catch (InterruptedException interupted) {
						throw new IOException(interupted);
					}
				}
			} while ((socket == null) && (attempt < 3));

			// Ensure propagate failure
			if (exception != null) {
				throw exception;
			}

			// Ensure have accepted socket decorator
			if (acceptedSocketDecorator == null) {
				acceptedSocketDecorator = (undecorated) -> {
				};
			}

			// Register the channel with the selector
			channel.register(this.selector, SelectionKey.OP_ACCEPT, new AcceptHandler<R>(channel,
					acceptedSocketDecorator, socketServicerFactory, requestServicerFactory));

			// Return the server socket
			return socket;
		}

		/**
		 * Indicates if the current {@link Thread} is the {@link SocketListener}
		 * {@link Thread}.
		 * 
		 * @return <code>true</code> if current {@link Thread} is {@link SocketListener}
		 *         {@link Thread}.
		 */
		private final boolean isSocketListenerThread() {
			SocketListener threadSafeSocketListener = threadSocketLister.get();
			return (this == threadSafeSocketListener);
		}

		/**
		 * Ensures execution by {@link SocketListener} {@link Thread}.
		 * 
		 * @throws IllegalStateException If different {@link Thread}.
		 */
		private final void ensureSocketListenerThread() throws IllegalStateException {

			// Ensure only handle requests on socket listener thread
			if (!this.isSocketListenerThread()) {
				throw new IllegalStateException(
						"Attempting to handle request via alternate thread " + Thread.currentThread().getName());
			}
		}

		/**
		 * Shuts down this {@link SocketListener}.
		 * 
		 * @throws IOException If fails to notify of shutdown.
		 */
		private final void shutdown() throws IOException {
			try {
				// Send message to shutdown
				this.shutdownPipe.sink().write(NOTIFY_BUFFER.duplicate());
			} catch (IOException ex) {
				// Determine if already shutdown
				if ("Broken pipe".equals(ex.getMessage())) {
					return;
				}

				// Other failure, so propagate
				throw ex;
			}
		}

		/*
		 * =================== Runnable =========================
		 */

		@Override
		public final void run() {

			// Register this socket listener with the thread
			threadSocketLister.set(this);

			try {
				// Loop until shutdown
				while (!this.isShutdown) {

					// Select keys
					try {
						this.selector.select(50);
					} catch (IOException ex) {
						// Should not occur
						LOGGER.log(Level.SEVERE, "Selector failure", ex);
						return; // fatal error, so can not continue
					}

					// Obtain the selected keys
					Set<SelectionKey> selectedKeys = this.selector.selectedKeys();

					// Service the selected keys
					Iterator<SelectionKey> iterator = selectedKeys.iterator();
					NEXT_KEY: while (iterator.hasNext()) {
						SelectionKey selectedKey = iterator.next();
						iterator.remove();

						try {

							// Obtain ready operations
							int readyOps = selectedKey.readyOps();

							// Determine if accept
							if ((readyOps & SelectionKey.OP_ACCEPT) == SelectionKey.OP_ACCEPT) {

								// Obtain the accept handler
								@SuppressWarnings("unchecked")
								AcceptHandler<Object> handler = (AcceptHandler<Object>) selectedKey.attachment();

								try {
									// Accept the connection
									SocketChannel socketChannel = handler.channel.accept();

									// Flag socket as unblocking
									socketChannel.configureBlocking(false);

									// Configure the socket
									Socket socket = socketChannel.socket();
									socket.setTcpNoDelay(true);
									socket.setReceiveBufferSize(this.socketReceiveBufferSize);
									socket.setSendBufferSize(this.socketSendBufferSize);
									handler.acceptedSocketDecorator.decorate(socket);

									// Manage the accepted socket
									AcceptedSocket<Object> acceptedSocket = new AcceptedSocket<>(handler,
											socketChannel);
									SocketManager.this.manageAcceptedSocket(acceptedSocket);

								} catch (IOException ex) {
									// Should not fail to accept connection
									LOGGER.log(Level.WARNING, "Failed to accept socket connection", ex);
								}

								// Accepted the connection
								continue NEXT_KEY;
							}

							// Determine if read content
							if ((readyOps & SelectionKey.OP_READ) == SelectionKey.OP_READ) {

								// Obtain the read handler
								AbstractReadHandler handler = (AbstractReadHandler) selectedKey.attachment();

								// Drain the socket of data
								int readsOnSelect = 0;
								ByteBuffer buffer;
								do {
									// Ensure have buffer to handle read
									// Need to track if new (as pool buffers)
									boolean isNewBuffer;
									if ((handler.readBuffer == null)
											|| (handler.readBuffer.pooledBuffer.remaining() == 0)) {
										// Require a new buffer
										handler.readBuffer = this.bufferPool.getPooledStreamBuffer();
										buffer = handler.readBuffer.pooledBuffer;
										isNewBuffer = true;
									} else {
										/*
										 * Tests are finding that re-using the buffer with position != 0 causes invalid
										 * data. To overcome this, need to create duplicate with position 0. Note that
										 * for direct buffers slicing takes a new memory location (likely why has
										 * issue).
										 */
										buffer = handler.readBuffer.pooledBuffer.slice();
										isNewBuffer = false;
									}

									// Read content from channel
									int bytesRead = handler.channel.read(buffer);
									readsOnSelect++;

									// Determine if closed connection
									if (bytesRead < 0) {
										// Connection closed, so terminate
										SocketManager.terminteSelectionKey(selectedKey, this, null);
										continue NEXT_KEY;
									}

									// Must update position (if re-use buffer)
									if (!isNewBuffer) {
										handler.readBuffer.pooledBuffer
												.position(handler.readBuffer.pooledBuffer.position() + bytesRead);
									}

									// Handle the read
									handler.handleRead(bytesRead, isNewBuffer);

								} while ((buffer.remaining() == 0) && (readsOnSelect < this.maxReadsOnSelect));
							}

							// Determine if write content
							if ((readyOps & SelectionKey.OP_WRITE) == SelectionKey.OP_WRITE) {

								// Write further data to the socket
								AcceptedSocketServicer<?> acceptedSocket = (AcceptedSocketServicer<?>) selectedKey
										.attachment();
								if (acceptedSocket.unsafeSendWrites()) {
									// Content written, no longer write interest
									selectedKey.interestOps(SelectionKey.OP_READ);
								}
							}

						} catch (Throwable ex) {
							SocketManager.terminteSelectionKey(selectedKey, this, ex);
							continue NEXT_KEY;
						}
					}
				}

			} finally {

				// Clear socket listener from the thread
				threadSocketLister.set(null);

				try {
					// Shutting down, so close selector
					this.selector.close();
				} catch (IOException ex) {
					LOGGER.log(Level.WARNING, "Failed to close selector", ex);
				}
			}
		}
	}

	/**
	 * Accept handler.
	 */
	private static class AcceptHandler<R> {

		/**
		 * {@link ServerSocketChannel}.
		 */
		private final ServerSocketChannel channel;

		/**
		 * {@link AcceptedSocketDecorator}.
		 */
		private final AcceptedSocketDecorator acceptedSocketDecorator;

		/**
		 * {@link SocketServicerFactory}.
		 */
		private final SocketServicerFactory<R> socketServicerFactory;

		/**
		 * {@link RequestServicerFactory}.
		 */
		private final RequestServicerFactory<R> requestServicerFactory;

		/**
		 * Instantiate.
		 * 
		 * @param channel                 {@link ServerSocketChannel}.
		 * @param acceptedSocketDecorator {@link AcceptedSocketDecorator}.
		 * @param socketServicerFactory   {@link SocketServicer}.
		 * @param requestServicerFactory  {@link RequestServicer}.
		 */
		private AcceptHandler(ServerSocketChannel channel, AcceptedSocketDecorator acceptedSocketDecorator,
				SocketServicerFactory<R> socketServicerFactory, RequestServicerFactory<R> requestServicerFactory) {
			this.channel = channel;
			this.acceptedSocketDecorator = acceptedSocketDecorator;
			this.socketServicerFactory = socketServicerFactory;
			this.requestServicerFactory = requestServicerFactory;
		}
	}

	/**
	 * Abstract functionality for handling reads.
	 */
	private static abstract class AbstractReadHandler {

		/**
		 * {@link ReadableByteChannel}.
		 */
		private final ReadableByteChannel channel;

		/**
		 * Current {@link StreamBuffer} for read content.
		 */
		protected StreamBuffer<ByteBuffer> readBuffer = null;

		/**
		 * Instantiate.
		 * 
		 * @param channel {@link ReadableByteChannel}.
		 */
		public AbstractReadHandler(ReadableByteChannel channel) {
			this.channel = channel;
		}

		/**
		 * Handles the read.
		 * 
		 * @param bytesRead   Number of bytes read.
		 * @param isNewBuffer Indicates if new {@link StreamBuffer}.
		 * @throws Throwable If fails to handle read.
		 */
		public abstract void handleRead(long bytesRead, boolean isNewBuffer) throws Throwable;

		/**
		 * Releases the {@link StreamBuffer} instances used by this
		 * {@link AbstractReadHandler}.
		 */
		public void releaseStreamBuffers() {
			if (this.readBuffer != null) {
				this.readBuffer.release();
				this.readBuffer = null;
			}
		}
	}

	/**
	 * Safe {@link AbstractReadHandler}.
	 */
	private static abstract class AbstractSafeReadHandler<E> extends AbstractReadHandler {

		/**
		 * {@link Pipe} to send unsafe operations.
		 */
		private final Pipe pipe;

		/**
		 * Unsafe events to be run in safe context.
		 */
		private final List<E> events = new ArrayList<>();

		/**
		 * Indicates if notified.
		 */
		private boolean isNotified = false;

		/**
		 * Instantiate.
		 * 
		 * @param pipe {@link Pipe} to send unsafe events to be handled safely.
		 */
		public AbstractSafeReadHandler(Pipe pipe) {
			super(pipe.source());
			this.pipe = pipe;
		}

		/**
		 * Sends the unsafe event to be handled safely.
		 * 
		 * @param event Unsafe event.
		 */
		protected synchronized final void sendUnsafeEvent(E event) {

			// Queue the event
			this.events.add(event);

			// Notify queue accepted sockets
			if (!this.isNotified) {
				try {
					this.pipe.sink().write(NOTIFY_BUFFER.duplicate());
					this.isNotified = true;
				} catch (IOException ex) {
					// Error on pipe means shutting down
					// Shutdown will close connections
					LOGGER.log(Level.FINEST,
							"Failed to send unsafe event " + event + " [" + event.getClass().getName() + "]", ex);
				}
			}
		}

		/**
		 * Safely handles the event.
		 * 
		 * @param event Event.
		 */
		protected abstract void safelyHandleEvent(E event);

		/*
		 * ============== AbstractReadHandler ================
		 */

		@Override
		@SuppressWarnings("unchecked")
		public final void handleRead(long bytesRead, boolean isNewBuffer) throws Throwable {

			// Release the read buffer (as just notification)
			this.releaseStreamBuffers();

			// Obtain copy of events (reduces time for lock contention)
			E[] safeEvents;
			synchronized (this) {

				// No longer notified (repeatable, so ensure unset)
				this.isNotified = false;

				// Copy the events
				safeEvents = (E[]) this.events.toArray();

				// Events will be handled
				this.events.clear();
			}

			// Safely handle the events
			for (int i = 0; i < safeEvents.length; i++) {
				this.safelyHandleEvent(safeEvents[i]);
			}
		}
	}

	/**
	 * Accept {@link Socket} handler.
	 */
	private static class AcceptSocketHandler extends AbstractSafeReadHandler<AcceptedSocket<?>> {

		/**
		 * {@link SocketListener}.
		 */
		private final SocketListener socketListener;

		/**
		 * Instantiate.
		 * 
		 * @param acceptedSocketPipe {@link AcceptedSocketServicer} {@link Pipe}.
		 * @param listener           {@link SocketListener}.
		 */
		private AcceptSocketHandler(Pipe acceptedSocketPipe, SocketListener listener) {
			super(acceptedSocketPipe);
			this.socketListener = listener;
		}

		/**
		 * Services an {@link AcceptedSocketServicer}.
		 * 
		 * @param accceptedSocket {@link AcceptedSocketServicer}.
		 * @throws IOException If fails to service the {@link AcceptedSocketServicer}.
		 */
		private final <R> void serviceAcceptedSocket(AcceptedSocket<R> accceptedSocket) {
			this.sendUnsafeEvent(accceptedSocket);
		}

		/*
		 * ============== AbstractReadHandler ================
		 */

		@Override
		@SuppressWarnings({ "rawtypes", "unchecked" })
		protected void safelyHandleEvent(AcceptedSocket<?> acceptedSocket) {

			// Accept the socket (registers itself for servicing)
			try {
				new AcceptedSocketServicer(acceptedSocket, this.socketListener);
			} catch (IOException ex) {
				LOGGER.log(Level.WARNING, "Failed to register accepted socket", ex);
			}
		}
	}

	/**
	 * Accepted {@link Socket}.
	 */
	private static class AcceptedSocket<R> {

		/**
		 * {@link AcceptHandler}.
		 */
		private final AcceptHandler<R> acceptHandler;

		/**
		 * {@link SocketChannel} for the accepted {@link Socket}.
		 */
		protected final SocketChannel socketChannel;

		/**
		 * Instantiate.
		 * 
		 * @param socketChannel          {@link SocketChannel}.
		 * @param socketServicerFactory  {@link SocketServicerFactory}.
		 * @param requestServicerFactory {@link RequestServicerFactory}.
		 */
		private AcceptedSocket(AcceptHandler<R> acceptHandler, SocketChannel socketChannel) {
			this.acceptHandler = acceptHandler;
			this.socketChannel = socketChannel;
		}
	}

	/**
	 * Accepted {@link Socket} servicer.
	 */
	private static class AcceptedSocketServicer<R> extends AbstractReadHandler implements RequestHandler<R> {

		/**
		 * {@link SocketChannel}.
		 */
		private final SocketChannel socketChannel;

		/**
		 * {@link SocketServicer}.
		 */
		private final SocketServicer<R> socketServicer;

		/**
		 * {@link RequestServicer}.
		 */
		private final RequestServicer<R> requestServicer;

		/**
		 * {@link SocketListener}.
		 */
		private final SocketListener socketListener;

		/**
		 * {@link SelectionKey}.
		 */
		private final SelectionKey selectionKey;

		/**
		 * Indicates if within an active read, so that a flush will be triggered at the
		 * end. If not going to flush, then must flush on send. This is typically
		 * because another {@link Thread} has triggered the write.
		 */
		private boolean isGoingToFlush = false;

		/**
		 * Request {@link StreamBuffer} instances to be released with the current
		 * request.
		 */
		private StreamBuffer<ByteBuffer> releaseRequestBuffers = null;

		/**
		 * Previous request {@link StreamBuffer}.
		 */
		private StreamBuffer<ByteBuffer> previousRequestBuffer = null;

		/**
		 * Head {@link SocketRequest}.
		 */
		private SocketRequest<R> head = null;

		/**
		 * Tail {@link SocketRequest}.
		 */
		private SocketRequest<R> tail = null;

		/**
		 * To avoid TCP overheads, response {@link StreamBuffer} instances are compacted
		 * into a new single {@link StreamBuffer} linked list to fill {@link Socket}
		 * buffers. This allows disabling Nagle's algorithm.
		 */
		private StreamBuffer<ByteBuffer> compactedResponseHead = null;

		/**
		 * Head {@link StreamBuffer} to the linked list of {@link StreamBuffer}
		 * instances for writing to the {@link Socket}.
		 */
		private StreamBuffer<ByteBuffer> writeResponseHead = null;

		/**
		 * Instantiate.
		 * 
		 * @param acceptedSocket {@link AcceptedSocket}.
		 * @param socketListener {@link SocketListener} servicing the
		 *                       {@link AcceptedSocket}.
		 * @throws IOException If fails to set up servicing the {@link AcceptedSocket}.
		 */
		private AcceptedSocketServicer(AcceptedSocket<R> acceptedSocket, SocketListener socketListener)
				throws IOException {
			super(acceptedSocket.socketChannel);
			this.socketChannel = acceptedSocket.socketChannel;
			this.socketListener = socketListener;

			// Create the socket servicer
			this.socketServicer = acceptedSocket.acceptHandler.socketServicerFactory.createSocketServicer(this);

			// Create the request servicer
			this.requestServicer = acceptedSocket.acceptHandler.requestServicerFactory
					.createRequestServicer(this.socketServicer);

			// Register for servicing
			this.selectionKey = acceptedSocket.socketChannel.register(this.socketListener.selector,
					SelectionKey.OP_READ, this);
		}

		/**
		 * Undertakes executing the {@link Execution}.
		 * 
		 * @param execution {@link Execution}.
		 */
		private final void unsafeExecute(Execution execution) {
			try {
				execution.run();
			} catch (Throwable ex) {
				this.unsafeCloseConnection(ex);
			}
		}

		/**
		 * {@link Thread} unsafe Writes the response.
		 * 
		 * @param socketRequest        {@link SocketRequest}.
		 * @param responseHeaderWriter {@link ResponseHeaderWriter}.
		 * @param headResponseBuffer   Head response {@link StreamBuffer} of the linked
		 *                             list of {@link StreamBuffer} instances for the
		 *                             {@link SocketRequest}.
		 */
		private final void unsafeWriteResponse(SocketRequest<R> socketRequest,
				ResponseHeaderWriter responseHeaderWriter, StreamBuffer<ByteBuffer> headResponseBuffer) {

			// Provide the response for the request
			socketRequest.responseHeaderWriter = responseHeaderWriter;
			socketRequest.headResponseBuffer = headResponseBuffer;

			// Response written (so release all request buffers)
			StreamBuffer<ByteBuffer> requestBuffer = socketRequest.headRequestBuffer;
			while (requestBuffer != null) {
				StreamBuffer<ByteBuffer> release = requestBuffer;
				requestBuffer = requestBuffer.next;

				// Must release buffer after released from chain
				release.release();
			}

			// Prepare the pooled response buffers
			StreamBuffer<ByteBuffer> prepareBuffer = headResponseBuffer;
			while (prepareBuffer != null) {
				if (prepareBuffer.pooledBuffer != null) {
					prepareBuffer.pooledBuffer.flip();
				}
				prepareBuffer = prepareBuffer.next;
			}

			// Ensure have a compact response head
			if (this.compactedResponseHead == null) {
				this.compactedResponseHead = this.socketListener.bufferPool.getPooledStreamBuffer();
			}

			// Obtain the unfilled write buffer
			StreamBuffer<ByteBuffer> writeBuffer = this.compactedResponseHead;
			while (writeBuffer.next != null) {
				writeBuffer = writeBuffer.next;
			}

			// Compact the stream buffers for writing
			while (this.head != null) {

				// Ensure a response for request
				if ((this.head.responseHeaderWriter == null) && (this.head.headResponseBuffer == null)) {
					return; // no response yet
				}

				// Ensure have space to write content
				if ((writeBuffer.pooledBuffer == null) || (writeBuffer.pooledBuffer.remaining() == 0)) {
					// Require new write buffer
					writeBuffer.next = this.socketListener.bufferPool.getPooledStreamBuffer();
					writeBuffer = writeBuffer.next;
				}

				// Determine if write the header
				if (this.head.responseHeaderWriter != null) {

					// Write the header
					this.head.responseHeaderWriter.write(writeBuffer, this.socketListener.bufferPool);

					// Clear writer (so only writes once)
					this.head.responseHeaderWriter = null;
				}

				// Obtain the unfilled write buffer
				// (may not be writer buffer after header written)
				while (writeBuffer.next != null) {
					writeBuffer = writeBuffer.next;
				}

				// Compact the content
				while (this.head.headResponseBuffer != null) {

					// Obtain the next buffer
					StreamBuffer<ByteBuffer> streamBuffer = this.head.headResponseBuffer;
					boolean isReleaseStreamBuffer;
					if (streamBuffer.fileBuffer != null) {
						// Append the file buffer
						// (avoids copying data into user space for DMA)
						writeBuffer.next = streamBuffer;
						isReleaseStreamBuffer = false;
						writeBuffer = writeBuffer.next;

					} else {
						// Pooled / Unpooled buffer
						ByteBuffer buffer = (streamBuffer.pooledBuffer != null) ? streamBuffer.pooledBuffer
								: streamBuffer.unpooledByteBuffer;
						isReleaseStreamBuffer = true;

						// Ensure have pooled buffer for writing
						if (writeBuffer.pooledBuffer == null) {
							writeBuffer.next = this.socketListener.bufferPool.getPooledStreamBuffer();
							writeBuffer = writeBuffer.next;
						}

						// Compact the data into the write buffer
						int writeBufferRemaining = writeBuffer.pooledBuffer.remaining();
						int bytesToWrite = buffer.remaining();
						if (writeBufferRemaining >= bytesToWrite) {
							// Space available to write the entire buffer
							writeBuffer.pooledBuffer.put(buffer);

						} else {
							// Must slice up buffer to write
							ByteBuffer slice = buffer.duplicate();
							int slicePosition = slice.position();
							do {
								// Write the slice
								slice.limit(slicePosition + writeBufferRemaining);
								writeBuffer.pooledBuffer.put(slice);

								// Decrement bytes to write
								slicePosition += writeBufferRemaining;
								bytesToWrite -= writeBufferRemaining;

								// Setup for next write
								writeBuffer.next = this.socketListener.bufferPool.getPooledStreamBuffer();
								writeBuffer = writeBuffer.next;

								// Setup slice for next write
								writeBufferRemaining = Math.min(writeBuffer.pooledBuffer.remaining(), bytesToWrite);
								slice.position(slicePosition);

							} while (bytesToWrite > 0);
						}
					}

					// Buffer compacted, move next (releasing written)
					this.head.headResponseBuffer = streamBuffer.next;

					// Must release buffer (after released from chain)
					if (isReleaseStreamBuffer) {
						streamBuffer.release();
					}
				}

				// Compacted head response, so move onto next request
				this.head = this.head.next;
			}

			// If not going to flush, must flush immediately
			if (!this.isGoingToFlush) {
				// Flush the writes in the future
				// (allows multiple writes to be flush on same packets)
				this.socketListener.bulkFlushWritesHandler.bulkFlushWrites(this);
			}
		}

		/**
		 * Undertakes flushing the response data to the {@link SocketChannel}.
		 */
		private final void unsafeFlushWrites() throws IOException {

			// Do nothing if no compact responses to flush
			if (this.compactedResponseHead == null) {
				return;
			}

			// Capture head for sending
			StreamBuffer<ByteBuffer> response = this.compactedResponseHead;

			// Prepare compacted buffers for writing
			while (this.compactedResponseHead != null) {
				if (this.compactedResponseHead.pooledBuffer != null) {
					this.compactedResponseHead.pooledBuffer.flip();
				}
				this.compactedResponseHead = this.compactedResponseHead.next;
			}
			// compact response head should now be null

			// Append response for writing
			this.unsafeAppendWrite(response);

			// Send the data
			this.unsafeSendWrites();
		}

		/**
		 * Undertakes appending {@link StreamBuffer} instances for writing to the
		 * {@link SocketChannel}.
		 * 
		 * @param writeHead Head {@link StreamBuffer} to linked list of
		 *                  {@link StreamBuffer} instances to write to the
		 *                  {@link SocketChannel}.
		 */
		private final void unsafeAppendWrite(StreamBuffer<ByteBuffer> writeHead) {

			// Obtain the tail write buffer
			StreamBuffer<ByteBuffer> tailWriteBuffer = this.writeResponseHead;
			if (tailWriteBuffer != null) {
				while (tailWriteBuffer.next != null) {
					tailWriteBuffer = tailWriteBuffer.next;
				}
			}

			// Join the send buffers for writing
			if (tailWriteBuffer == null) {
				this.writeResponseHead = writeHead;
			} else {
				tailWriteBuffer.next = writeHead;
			}
		}

		/**
		 * Undertakes sending the response data.
		 * 
		 * @return <code>true</code> if all response data written. Otherwise,
		 *         <code>false</code> indicating the {@link Socket} buffer filled.
		 */
		private final boolean unsafeSendWrites() throws IOException {

			// Flush the compacted buffers to the socket
			while (this.writeResponseHead != null) {

				// Determine if file buffer
				if (this.writeResponseHead.fileBuffer != null) {
					// Write the file content to the socket
					FileBuffer writeBuffer = this.writeResponseHead.fileBuffer;

					// Determine the position and count
					long position = writeBuffer.position + writeBuffer.bytesWritten;
					long count = (writeBuffer.count < 0 ? writeBuffer.file.size() - writeBuffer.position
							: writeBuffer.count) - writeBuffer.bytesWritten;

					// Write the file content to the socket
					long bytesWritten = writeBuffer.file.transferTo(position, count, this.socketChannel);

					// Increment the number of bytes written
					writeBuffer.bytesWritten += bytesWritten;

					// Determine if written all bytes
					if (bytesWritten < count) {
						// Not all bytes written, so write when emptied

						// Flag interest in write (as buffer full)
						this.selectionKey.interestOps(SelectionKey.OP_READ | SelectionKey.OP_WRITE);

						// Can not write anything further
						return false; // require further writes
					}

					// As here, file complete
					if (writeBuffer.callback != null) {
						writeBuffer.callback.complete(writeBuffer.file, true);
					}

				} else {
					// Pooled / Unpooled buffer
					ByteBuffer writeBuffer = (this.writeResponseHead.pooledBuffer != null)
							? this.writeResponseHead.pooledBuffer
							: this.writeResponseHead.unpooledByteBuffer;

					// Write the buffer to the socket
					this.socketChannel.write(writeBuffer);

					// Determine if written all bytes
					if (writeBuffer.remaining() != 0) {
						// Not all bytes written, so write when buffer emptied

						// Flag interest in write (as buffer full)
						this.selectionKey.interestOps(SelectionKey.OP_READ | SelectionKey.OP_WRITE);

						// Can not write anything further
						return false; // require further writes
					}
				}

				// Capture buffer for release, and move to next buffer
				StreamBuffer<ByteBuffer> release = this.writeResponseHead;
				this.writeResponseHead = this.writeResponseHead.next;

				// Release the written buffer
				release.release();
			}

			// As here, all data written
			return true;
		}

		/**
		 * Closes the connection.
		 * 
		 * @param exception Possible {@link Throwable} being the cause of the close of
		 *                  the connection. <code>null</code> if normal close.
		 */
		private final void unsafeCloseConnection(Throwable exception) {
			SocketManager.terminteSelectionKey(this.selectionKey, this.socketListener, exception);
		}

		/*
		 * ============== AbstractReadHandler ================
		 */

		@Override
		public final void handleRead(long bytesRead, boolean isNewBuffer) throws IOException {

			// Only invoked by Socket Listener thread

			// Going to flush at end of read
			this.isGoingToFlush = true;

			// Keep track of buffers (to enable releasing)
			if (isNewBuffer && (this.previousRequestBuffer != null)) {
				// New buffer (release previous on servicing request)
				this.previousRequestBuffer.next = this.releaseRequestBuffers;
				this.releaseRequestBuffers = this.previousRequestBuffer;
			}
			this.previousRequestBuffer = this.readBuffer;

			// Service the socket (if have bytes)
			this.socketServicer.service(this.readBuffer, bytesRead, isNewBuffer);

			// Ensure flush writes
			this.isGoingToFlush = false;
			this.unsafeFlushWrites();
		}

		@Override
		public final void releaseStreamBuffers() {

			// Only invoked by Socket Listener thread

			// Release socket read buffer
			if (this.previousRequestBuffer != null) {
				if (this.readBuffer == this.previousRequestBuffer) {
					this.readBuffer = null; // released as previous
				}
				this.previousRequestBuffer.release();
				this.previousRequestBuffer = null;
			}
			if (this.readBuffer != null) {
				this.readBuffer.release();
				this.readBuffer = null;
			}

			// Release the request buffers
			while (this.releaseRequestBuffers != null) {
				StreamBuffer<ByteBuffer> release = this.releaseRequestBuffers;
				this.releaseRequestBuffers = this.releaseRequestBuffers.next;
				release.release();
			}

			// Release the compact buffers
			while (this.compactedResponseHead != null) {
				StreamBuffer<ByteBuffer> release = this.compactedResponseHead;
				this.compactedResponseHead = this.compactedResponseHead.next;
				release.release();
			}

			// Release buffers for requests
			while (this.head != null) {
				StreamBuffer<ByteBuffer> headRequest = this.head.headRequestBuffer;
				while (headRequest != null) {
					StreamBuffer<ByteBuffer> release = headRequest;
					headRequest = headRequest.next;
					release.release();
				}
				StreamBuffer<ByteBuffer> headResponse = this.head.headResponseBuffer;
				while (headResponse != null) {
					StreamBuffer<ByteBuffer> release = headResponse;
					headResponse = headResponse.next;
					release.release();
				}
				this.head = this.head.next;
			}

			// Release the write buffers
			while (this.writeResponseHead != null) {
				StreamBuffer<ByteBuffer> release = this.writeResponseHead;
				this.writeResponseHead = this.writeResponseHead.next;
				release.release();
			}

			// Allow socket servicer to release buffers
			this.socketServicer.release();
		}

		/*
		 * ================ RequestHandler ===================
		 */

		@Override
		public final void execute(Execution execution) {
			// Appropriately execute based on thread safety
			if (this.socketListener.isSocketListenerThread()) {
				this.unsafeExecute(execution);

			} else {
				// Trigger to undertake execution on socket thread
				this.socketListener.executionHandler.safeExecute(this, execution);
			}
		}

		@Override
		public final void handleRequest(R request) {

			// Ensure only handle requests on socket listener thread
			this.socketListener.ensureSocketListenerThread();

			// Create the socket request
			SocketRequest<R> socketRequest = new SocketRequest<>(this, this.releaseRequestBuffers);
			this.releaseRequestBuffers = null;

			// Add to pipeline of requests
			if (this.head == null) {
				// First request
				this.head = socketRequest;
				this.tail = socketRequest;
			} else {
				// Append additional request
				this.tail.next = socketRequest;
				this.tail = socketRequest;
			}

			// Service the request
			this.requestServicer.service(request, socketRequest);
		}

		@Override
		public final void sendImmediateData(StreamBuffer<ByteBuffer> immediateHead) {

			// Ensure only send data on socket listener thread
			this.socketListener.ensureSocketListenerThread();

			// Prepare the pooled response buffers
			StreamBuffer<ByteBuffer> streamBuffer = immediateHead;
			while (streamBuffer != null) {
				if (streamBuffer.pooledBuffer != null) {
					streamBuffer.pooledBuffer.flip();
				}
				streamBuffer = streamBuffer.next;
			}

			// Append to write
			this.unsafeAppendWrite(immediateHead);

			// Immediately flush data
			try {
				this.unsafeSendWrites();
			} catch (IOException ex) {
				this.unsafeCloseConnection(ex);
			}
		}

		@Override
		public final void closeConnection(Throwable exception) {
			// Appropriately execute based on thread safety
			if (this.socketListener.isSocketListenerThread()) {
				this.unsafeCloseConnection(exception);

			} else {
				// Trigger to close the connection
				this.socketListener.safeCloseConnectionHandler.safeCloseConnection(this, exception);
			}
		}
	}

	/**
	 * Handles safely executing the {@link Execution}.
	 */
	private static class ExecutionHandler extends AbstractSafeReadHandler<AcceptedSocketExecution> {

		/**
		 * Instantiate.
		 * 
		 * @param executionPipe Notify execution {@link Pipe}.
		 */
		private ExecutionHandler(Pipe executionPipe) {
			super(executionPipe);
		}

		/**
		 * <p>
		 * Safely executes the {@link Execution}.
		 * <p>
		 * Method is synchronised to ensure data is safe across the {@link Thread}, when
		 * {@link #handleRead()} is invoked by the {@link SocketListener}
		 * {@link Thread}.
		 * 
		 * @param acceptedSocket {@link AcceptedSocketServicer}.
		 * @param execution      {@link Execution}.
		 */
		private final <R> void safeExecute(AcceptedSocketServicer<R> acceptedSocket, Execution execution) {
			this.sendUnsafeEvent(new AcceptedSocketExecution(acceptedSocket, execution));
		}

		/*
		 * ============== AbstractSafeReadHandler ================
		 */

		@Override
		protected void safelyHandleEvent(AcceptedSocketExecution execution) {
			execution.acceptedSocket.unsafeExecute(execution.execution);
		}
	}

	/**
	 * {@link AcceptedSocketServicer} {@link Execution}.
	 */
	private static class AcceptedSocketExecution {

		/**
		 * {@link AcceptedSocketServicer}.
		 */
		private final AcceptedSocketServicer<?> acceptedSocket;

		/**
		 * {@link Execution}.
		 */
		private final Execution execution;

		/**
		 * Instantiate.
		 * 
		 * @param acceptedSocket {@link AcceptedSocketServicer}.
		 * @param execution      {@link Execution}.
		 */
		public AcceptedSocketExecution(AcceptedSocketServicer<?> acceptedSocket, Execution execution) {
			this.acceptedSocket = acceptedSocket;
			this.execution = execution;
		}
	}

	/**
	 * Handles safely writing the {@link Socket} data (from another {@link Thread}
	 * than the {@link SocketListener} {@link Thread}).
	 */
	private static class SafeWriteSocketHandler extends AbstractSafeReadHandler<SafeWriteResponse<?>> {

		/**
		 * Instantiate.
		 * 
		 * @param writeSocketPipe Notify write {@link Pipe}.
		 */
		private SafeWriteSocketHandler(Pipe writeSocketPipe) {
			super(writeSocketPipe);
		}

		/**
		 * <p>
		 * Safely writes the response.
		 * <p>
		 * Method is synchronised to ensure data is safe across the {@link Thread}, when
		 * {@link #handleRead()} is invoked by the {@link SocketListener}
		 * {@link Thread}.
		 * 
		 * @param acceptedSocket       {@link AcceptedSocketServicer}.
		 * @param socketRequest        {@link SocketRequest}.
		 * @param responseHeaderWriter {@link ResponseHeaderWriter}.
		 * @param headResponseBuffer   Head {@link StreamBuffer} to the linked list of
		 *                             {@link StreamBuffer} instances for the response.
		 */
		private final <R> void safeWriteResponse(AcceptedSocketServicer<R> acceptedSocket,
				SocketRequest<R> socketRequest, ResponseHeaderWriter responseHeaderWriter,
				StreamBuffer<ByteBuffer> headResponseBuffer) {
			this.sendUnsafeEvent(
					new SafeWriteResponse<>(acceptedSocket, socketRequest, responseHeaderWriter, headResponseBuffer));
		}

		/*
		 * ============== AbstractSafeReadHandler ================
		 */

		@Override
		@SuppressWarnings({ "unchecked", "rawtypes" })
		protected void safelyHandleEvent(SafeWriteResponse response) {
			response.acceptedSocket.unsafeWriteResponse(response.socketRequest, response.responseHeaderWriter,
					response.headResponseBuffer);
		}
	}

	/**
	 * Safe write response.
	 */
	private static class SafeWriteResponse<R> {

		/**
		 * {@link AcceptedSocketServicer}.
		 */
		private final AcceptedSocketServicer<R> acceptedSocket;

		/**
		 * {@link SocketRequest}.
		 */
		private final SocketRequest<R> socketRequest;

		/**
		 * {@link ResponseHeaderWriter}.
		 */
		private final ResponseHeaderWriter responseHeaderWriter;

		/**
		 * Head {@link StreamBuffer} to the linked list of {@link StreamBuffer}
		 * instances for the response.
		 */
		private final StreamBuffer<ByteBuffer> headResponseBuffer;

		/**
		 * Instantiate.
		 * 
		 * @param acceptedSocket       {@link AcceptedSocketServicer}.
		 * @param socketRequest        {@link SocketRequest}.
		 * @param responseHeaderWriter {@link ResponseHeaderWriter}.
		 * @param headResponseBuffer   Head {@link StreamBuffer} to the linked list of
		 *                             {@link StreamBuffer} instances for the response.
		 */
		public SafeWriteResponse(AcceptedSocketServicer<R> acceptedSocket, SocketRequest<R> socketRequest,
				ResponseHeaderWriter responseHeaderWriter, StreamBuffer<ByteBuffer> headResponseBuffer) {
			this.acceptedSocket = acceptedSocket;
			this.socketRequest = socketRequest;
			this.responseHeaderWriter = responseHeaderWriter;
			this.headResponseBuffer = headResponseBuffer;
		}
	}

	/**
	 * Request on the {@link Socket}.
	 */
	private static class SocketRequest<R> implements ResponseWriter {

		/**
		 * {@link AcceptedSocketServicer}.
		 */
		private final AcceptedSocketServicer<R> acceptedSocket;

		/**
		 * Head request {@link StreamBuffer} of the linked list of {@link StreamBuffer}
		 * instances.
		 */
		private final StreamBuffer<ByteBuffer> headRequestBuffer;

		/**
		 * {@link ResponseHeaderWriter}.
		 */
		private ResponseHeaderWriter responseHeaderWriter;

		/**
		 * Head response {@link StreamBuffer} of the linked list of {@link StreamBuffer}
		 * instances.
		 */
		private StreamBuffer<ByteBuffer> headResponseBuffer = null;

		/**
		 * Next {@link SocketRequest}.
		 */
		private SocketRequest<R> next = null;

		/**
		 * Instantiate.
		 * 
		 * @param acceptedSocket    {@link AcceptedSocketServicer}.
		 * @param headRequestBuffer Request {@link StreamBuffer} instances.
		 */
		private SocketRequest(AcceptedSocketServicer<R> acceptedSocket, StreamBuffer<ByteBuffer> headRequestBuffer) {
			this.acceptedSocket = acceptedSocket;
			this.headRequestBuffer = headRequestBuffer;
		}

		/*
		 * ================== ResponseWriter ==================
		 */

		@Override
		public final void write(ResponseHeaderWriter responseHeaderWriter,
				StreamBuffer<ByteBuffer> headResponseBuffers) {

			// Appropriately write the response based on thread safety
			if (this.acceptedSocket.socketListener.isSocketListenerThread()) {
				this.acceptedSocket.unsafeWriteResponse(this, responseHeaderWriter, headResponseBuffers);

			} else {
				// Writes to the request, so may happen in any order
				this.acceptedSocket.socketListener.safeWriteSocketHandler.safeWriteResponse(this.acceptedSocket, this,
						responseHeaderWriter, headResponseBuffers);
			}
		}
	}

	/**
	 * Handles safely flushing the write {@link StreamBuffer} instances to the
	 * {@link Socket}.
	 */
	private static class BulkFlushWritesHandler extends AbstractSafeReadHandler<AcceptedSocketServicer<?>> {

		/**
		 * Instantiate.
		 * 
		 * @param flushWritesPipe Notify flush writes {@link Pipe}.
		 */
		private BulkFlushWritesHandler(Pipe flushWritesPipe) {
			super(flushWritesPipe);
		}

		/**
		 * <p>
		 * Safely writes the response.
		 * <p>
		 * Method is synchronised to ensure data is safe across the {@link Thread}, when
		 * {@link #handleRead()} is invoked by the {@link SocketListener}
		 * {@link Thread}.
		 * 
		 * @param acceptedSocket {@link AcceptedSocketServicer}.
		 */
		private final <R> void bulkFlushWrites(AcceptedSocketServicer<R> acceptedSocket) {
			this.sendUnsafeEvent(acceptedSocket);
		}

		/*
		 * ============== AbstractSafeReadHandler ================
		 */

		@Override
		protected void safelyHandleEvent(AcceptedSocketServicer<?> servicer) {
			try {
				servicer.unsafeFlushWrites();
			} catch (IOException ex) {
				servicer.unsafeCloseConnection(ex);
			}
		}
	}

	/**
	 * Safely closes connection handler.
	 */
	private static class SafeCloseConnectionHandler extends AbstractSafeReadHandler<SafeCloseConnection> {

		/**
		 * Instantiate.
		 * 
		 * @param closeConnectionPipe Close connection {@link Pipe}.
		 */
		private SafeCloseConnectionHandler(Pipe closeConnectionPipe) {
			super(closeConnectionPipe);
		}

		/**
		 * Safely closes the connection.
		 * 
		 * @param acceptedSocket {@link AcceptedSocketServicer} to close.
		 * @param exception      Possible {@link Throwable} for cause of closing
		 *                       connection.
		 */
		private final void safeCloseConnection(AcceptedSocketServicer<?> acceptedSocket, Throwable exception) {
			this.sendUnsafeEvent(new SafeCloseConnection(acceptedSocket, exception));
		}

		/*
		 * ============== AbstractSafeReadHandler ================
		 */

		@Override
		protected void safelyHandleEvent(SafeCloseConnection connection) {
			connection.acceptedSocket.unsafeCloseConnection(connection.exception);
		}
	}

	/**
	 * Safe close connection.
	 */
	private static class SafeCloseConnection {

		/**
		 * {@link AcceptedSocketServicer} to close.
		 */
		private final AcceptedSocketServicer<?> acceptedSocket;

		/**
		 * Possible {@link Throwable} for cuase of closing the connection.
		 * <code>null</code> if normal close.
		 */
		private final Throwable exception;

		/**
		 * Instantiate.
		 * 
		 * @param acceptedSocket {@link AcceptedSocketServicer} to close.
		 * @param exception      Possible {@link Throwable} for cuase of closing the
		 *                       connection. <code>null</code> if normal close.
		 */
		public SafeCloseConnection(AcceptedSocketServicer<?> acceptedSocket, Throwable exception) {
			this.acceptedSocket = acceptedSocket;
			this.exception = exception;
		}
	}

	/**
	 * Shutdown handler.
	 */
	private static class ShutdownReadHandler extends AbstractReadHandler {

		/**
		 * Listener to shut down.
		 */
		private final SocketListener listener;

		/**
		 * Instantiate.
		 * 
		 * @param channel  {@link ReadableByteChannel}.
		 * @param listener {@link SocketListener} to shut down.
		 */
		private ShutdownReadHandler(ReadableByteChannel channel, SocketListener listener) {
			super(channel);
			this.listener = listener;
		}

		/*
		 * ============== AbstractReadHandler ================
		 */

		@Override
		public final void handleRead(long bytesRead, boolean isNewBuffer) {

			// Release buffer (as content not important, only notification)
			this.readBuffer.release();
			this.readBuffer = null;

			// Terminate all keys
			for (SelectionKey key : this.listener.selector.keys()) {
				SocketManager.terminteSelectionKey(key, this.listener, null);
			}

			// Flag to shutdown
			this.listener.isShutdown = true;
		}
	}

}