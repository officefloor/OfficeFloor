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
	 * {@link ThreadLocal} to determine if {@link SocketListener}
	 * {@link Thread}.
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
	 * @param listenerCount
	 *            Number of {@link SocketListener} instances.
	 * @param bufferPool
	 *            {@link StreamBufferPool}.
	 * @param socketBufferSize
	 *            Buffer size for the {@link Socket}. This should match the
	 *            {@link StreamBuffer} size.
	 * @throws IOException
	 *             If fails to initialise {@link Socket} management.
	 */
	public SocketManager(int listenerCount, StreamBufferPool<ByteBuffer> bufferPool, int socketBufferSize)
			throws IOException {
		this.listeners = new SocketListener[listenerCount];
		for (int i = 0; i < listeners.length; i++) {
			listeners[i] = new SocketListener(bufferPool, socketBufferSize);
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
	 * @param selectionKey
	 *            {@link SelectionKey}.
	 */
	private static final void terminteSelectionKey(SelectionKey selectionKey, SocketListener socketListener) {
		try {
			selectionKey.channel().close();
		} catch (ClosedChannelException ex) {
			// Ignore, already closed
		} catch (IOException ex) {
			LOGGER.log(Level.WARNING, "Failed closing connection", ex);
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
	 * @param port
	 *            Port for the {@link ServerSocket}.
	 * @param serverSocketDecorator
	 *            Optional {@link ServerSocketDecorator}. May be
	 *            <code>null</code>.
	 * @param acceptedSocketDecorator
	 *            Optional {@link AcceptedSocketDecorator}. May be
	 *            <code>null</code>.
	 * @param socketServicerFactory
	 *            {@link SocketServicerFactory} to service accepted connections.
	 * @param requestServicerFactory
	 *            {@link RequestServicerFactory} to service requests on the
	 *            {@link Socket}.
	 * @throws IOException
	 *             If fails to bind the {@link ServerSocket}.
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
	 * @throws IOException
	 *             If fails to shutdown this {@link SocketManager}.
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
	}

	/**
	 * Manages an accepted {@link Socket}.
	 * 
	 * @param acceptedSocket
	 *            {@link AcceptedSocketServicer}.
	 * @throws IOException
	 *             If fails to managed the {@link AcceptedSocketServicer}.
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
		 * {@link Socket} buffer size.
		 */
		private final int socketBufferSize;

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
		 * @param bufferPool
		 *            {@link StreamBufferPool}.
		 * @param socketBufferSize
		 *            {@link Socket} buffer size.
		 * @throws IOException
		 *             If fails to establish necessary {@link Socket} and
		 *             {@link Pipe} facilities.
		 */
		private SocketListener(StreamBufferPool<ByteBuffer> bufferPool, int socketBufferSize) throws IOException {
			this.bufferPool = bufferPool;
			this.socketBufferSize = socketBufferSize;

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
		 * @param port
		 *            Port to bind the {@link SocketServicer}.
		 * @param serverSocketDecorator
		 *            Optional {@link ServerSocketDecorator}. May be
		 *            <code>null</code>.
		 * @param acceptedSocketDecorator
		 *            Optional {@link AcceptedSocketDecorator}. May be
		 *            <code>null</code>.
		 * @param socketServicerFactory
		 *            {@link SocketServicerFactory}.
		 * @param requestServicerFactory
		 *            {@link RequestServicerFactory}.
		 * @return Bound {@link ServerSocket}.
		 * @throws IOException
		 *             If fails to bind the {@link ServerSocket}.
		 */
		private final <R> ServerSocket bindServerSocket(int port, ServerSocketDecorator serverSocketDecorator,
				AcceptedSocketDecorator acceptedSocketDecorator, SocketServicerFactory<R> socketServicerFactory,
				RequestServicerFactory<R> requestServicerFactory) throws IOException {

			// Create the port socket address
			InetSocketAddress portAddress = new InetSocketAddress(port);

			// Create the Server Socket
			ServerSocketChannel channel = ServerSocketChannel.open();
			channel.configureBlocking(false);
			ServerSocket socket = channel.socket();
			socket.setReuseAddress(true);
			int serverSocketBackLogSize = DEFAULT_SERVER_SOCKET_BACKLOG_SIZE;
			if (serverSocketDecorator != null) {
				// Override the defaults
				serverSocketBackLogSize = serverSocketDecorator.decorate(socket);
			}

			// Bind the Server Socket
			socket.bind(portAddress, serverSocketBackLogSize);

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
		 * @return <code>true</code> if current {@link Thread} is
		 *         {@link SocketListener} {@link Thread}.
		 */
		private final boolean isSocketListenerThread() {
			SocketListener threadSafeSocketListener = threadSocketLister.get();
			return (this == threadSafeSocketListener);
		}

		/**
		 * Ensures execution by {@link SocketListener} {@link Thread}.
		 * 
		 * @throws IllegalStateException
		 *             If different {@link Thread}.
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
		 * @throws IOException
		 *             If fails to notify of shutdown.
		 */
		private final void shutdown() throws IOException {
			// Send message to shutdown
			this.shutdownPipe.sink().write(NOTIFY_BUFFER.duplicate());
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
									socket.setReuseAddress(true);
									socket.setReceiveBufferSize(this.socketBufferSize);
									socket.setSendBufferSize(this.socketBufferSize);
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

								// Ensure have buffer to handle read
								// Need to track if new (as pool buffers)
								boolean isNewBuffer = false;
								if ((handler.readBuffer == null)
										|| (handler.readBuffer.pooledBuffer.remaining() == 0)) {
									// Require a new buffer
									handler.readBuffer = this.bufferPool.getPooledStreamBuffer();
									isNewBuffer = true;
								}

								// Obtain the byte buffer
								ByteBuffer buffer = handler.readBuffer.pooledBuffer;

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
									// Connection closed/failed, so terminate
									SocketManager.terminteSelectionKey(selectedKey, this);
									continue NEXT_KEY;
								}

								// Handle the read
								handler.handleRead(isNewBuffer);
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

						} catch (CancelledKeyException ex) {
							// Already closed, so just continue on
							SocketManager.terminteSelectionKey(selectedKey, this);
							continue NEXT_KEY;

						} catch (Throwable ex) {
							LOGGER.log(Level.WARNING, "Failure with connection", ex);
							SocketManager.terminteSelectionKey(selectedKey, this);
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
		 * @param channel
		 *            {@link ServerSocketChannel}.
		 * @param acceptedSocketDecorator
		 *            {@link AcceptedSocketDecorator}.
		 * @param socketServicerFactory
		 *            {@link SocketServicer}.
		 * @param requestServicerFactory
		 *            {@link RequestServicer}.
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
		 * @param channel
		 *            {@link ReadableByteChannel}.
		 */
		public AbstractReadHandler(ReadableByteChannel channel) {
			this.channel = channel;
		}

		/**
		 * Handles the read.
		 * 
		 * @param isNewBuffer
		 *            Indicates if new {@link StreamBuffer}.
		 * @throws Throwable
		 *             If fails to handle read.
		 */
		public abstract void handleRead(boolean isNewBuffer) throws Throwable;

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
	 * Accept {@link Socket} handler.
	 */
	private static class AcceptSocketHandler extends AbstractReadHandler {

		/**
		 * {@link AcceptedSocketServicer} {@link Pipe}.
		 */
		private final Pipe acceptedSocketPipe;

		/**
		 * {@link SocketListener}.
		 */
		private final SocketListener socketListener;

		/**
		 * {@link List} of {@link AcceptedSocket} instances.
		 */
		private final List<AcceptedSocket<?>> acceptedSockets = new ArrayList<>();

		/**
		 * Indicates if notified.
		 */
		private boolean isNotified = false;

		/**
		 * Instantiate.
		 * 
		 * @param acceptedSocketPipe
		 *            {@link AcceptedSocketServicer} {@link Pipe}.
		 * @param listener
		 *            {@link SocketListener}.
		 */
		private AcceptSocketHandler(Pipe acceptedSocketPipe, SocketListener listener) {
			super(acceptedSocketPipe.source());
			this.acceptedSocketPipe = acceptedSocketPipe;
			this.socketListener = listener;
		}

		/**
		 * Services an {@link AcceptedSocketServicer}.
		 * 
		 * @param accceptedSocket
		 *            {@link AcceptedSocketServicer}.
		 * @throws IOException
		 *             If fails to service the {@link AcceptedSocketServicer}.
		 */
		private synchronized final <R> void serviceAcceptedSocket(AcceptedSocket<R> accceptedSocket)
				throws IOException {

			// Queue for adding
			this.acceptedSockets.add(accceptedSocket);

			// Notify queue accepted sockets
			if (!this.isNotified) {
				this.acceptedSocketPipe.sink().write(NOTIFY_BUFFER.duplicate());
				this.isNotified = true;
			}
		}

		/*
		 * ============== AbstractReadHandler ================
		 */

		@Override
		@SuppressWarnings({ "rawtypes", "unchecked" })
		public synchronized final void handleRead(boolean isNewBuffer) {

			// No longer notified
			this.isNotified = false;

			// Only notification, content not important
			this.readBuffer.pooledBuffer.clear();

			// Register the accepted sockets
			for (int i = 0; i < this.acceptedSockets.size(); i++) {
				AcceptedSocket<?> acceptedSocket = this.acceptedSockets.get(i);

				// Accept the socket (registers itself for servicing)
				try {
					new AcceptedSocketServicer(acceptedSocket, this.socketListener);
				} catch (IOException ex) {
					LOGGER.log(Level.WARNING, "Failed to register accepted socket", ex);
				}
			}

			// All sockets accepted
			this.acceptedSockets.clear();
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
		 * @param socketChannel
		 *            {@link SocketChannel}.
		 * @param socketServicerFactory
		 *            {@link SocketServicerFactory}.
		 * @param requestServicerFactory
		 *            {@link RequestServicerFactory}.
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
		 * Request {@link StreamBuffer} instances to be released with the
		 * current request.
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
		 * To avoid TCP overheads, response {@link StreamBuffer} instances are
		 * compacted into a new single {@link StreamBuffer} linked list to fill
		 * {@link Socket} buffers. This allows disabling Nagle's algorithm.
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
		 * @param acceptedSocket
		 *            {@link AcceptedSocket}.
		 * @param socketListener
		 *            {@link SocketListener} servicing the
		 *            {@link AcceptedSocket}.
		 * @throws IOException
		 *             If fails to set up servicing the {@link AcceptedSocket}.
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
		 * @param execution
		 *            {@link Execution}.
		 */
		private final void unsafeExecute(Execution execution) {
			try {
				execution.run();
			} catch (Throwable ex) {
				this.closeConnection(ex);
			}
		}

		/**
		 * {@link Thread} unsafe Writes the response.
		 * 
		 * @param socketRequest
		 *            {@link SocketRequest}.
		 * @param responseHeaderWriter
		 *            {@link ResponseHeaderWriter}.
		 * @param headResponseBuffer
		 *            Head response {@link StreamBuffer} of the linked list of
		 *            {@link StreamBuffer} instances for the
		 *            {@link SocketRequest}.
		 */
		private final void unsafeWriteResponse(SocketRequest<R> socketRequest,
				ResponseHeaderWriter responseHeaderWriter, StreamBuffer<ByteBuffer> headResponseBuffer) {

			// Provide the response for the request
			synchronized (this.socketListener.safeWriteSocketHandler) {
				socketRequest.responseHeaderWriter = responseHeaderWriter;
				socketRequest.headResponseBuffer = headResponseBuffer;
			}

			// Response written (so release all request buffers)
			StreamBuffer<ByteBuffer> requestBuffer = this.head.headRequestBuffer;
			while (requestBuffer != null) {
				StreamBuffer<ByteBuffer> release = requestBuffer;
				requestBuffer = requestBuffer.next;

				// Must release buffer after released from chain
				release.release();
			}

			// Prepare the pooled response buffers
			StreamBuffer<ByteBuffer> prepareBuffer = headResponseBuffer;
			while (prepareBuffer != null) {
				if (prepareBuffer.isPooled) {
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
			if ((!writeBuffer.isPooled) || (writeBuffer.pooledBuffer.remaining() == 0)) {
				// Require new write buffer
				writeBuffer.next = this.socketListener.bufferPool.getPooledStreamBuffer();
				writeBuffer = writeBuffer.next;
			}

			// Compact the stream buffers for writing
			while (this.head != null) {

				// Ensure a response for request
				if ((this.head.responseHeaderWriter == null) && (this.head.headResponseBuffer == null)) {
					return; // no response yet
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
					ByteBuffer buffer = (streamBuffer.isPooled ? streamBuffer.pooledBuffer
							: streamBuffer.unpooledByteBuffer);

					// Compact the data into the write buffer
					int writeBufferRemaining = writeBuffer.pooledBuffer.remaining();
					int bytesToWrite = buffer.remaining();
					if (writeBufferRemaining > bytesToWrite) {
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

					// Buffer compacted, move to next (releasing written buffer)
					this.head.headResponseBuffer = streamBuffer.next;

					// Must release buffer (after released from chain)
					streamBuffer.release();
				}

				// Compacted head response, so move onto next request
				this.head = this.head.next;
			}
		}

		/**
		 * Undertakes flushing the response data to the {@link SocketChannel}.
		 */
		private final void unsafeFlushWrites() {

			// Do nothing if no compact responses to flush
			if (this.compactedResponseHead == null) {
				return;
			}

			// Capture head for sending
			StreamBuffer<ByteBuffer> response = this.compactedResponseHead;

			// Prepare compacted buffers for writing
			while (this.compactedResponseHead != null) {
				this.compactedResponseHead.pooledBuffer.flip();
				this.compactedResponseHead = this.compactedResponseHead.next;
			}
			// compact response head should now be null

			// Append response for writing
			this.unsafeAppendWrite(response);

			// Send the data
			this.unsafeSendWrites();
		}

		/**
		 * Undertakes appending {@link StreamBuffer} instances for writing to
		 * the {@link SocketChannel}.
		 * 
		 * @param writeHead
		 *            Head {@link StreamBuffer} to linked list of
		 *            {@link StreamBuffer} instances to write to the
		 *            {@link SocketChannel}.
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
		 *         <code>false</code> indicating the {@link Socket} buffer
		 *         filled.
		 */
		private final boolean unsafeSendWrites() {

			// Flush the compacted buffers to the socket
			while (this.writeResponseHead != null) {

				// Obtain the write buffer
				ByteBuffer writeBuffer = this.writeResponseHead.isPooled ? this.writeResponseHead.pooledBuffer
						: this.writeResponseHead.unpooledByteBuffer;

				// Write the buffer to the socket
				try {
					this.socketChannel.write(writeBuffer);
				} catch (IOException ex) {
					// Failed to write to channel, so close
					this.closeConnection(ex);
					return false; // terminating
				}

				// Determine if written all bytes
				if (writeBuffer.remaining() != 0) {
					// Not all bytes written, so write when buffer emptied

					// Flag interest in write (as buffer full)
					this.selectionKey.interestOps(SelectionKey.OP_READ | SelectionKey.OP_WRITE);

					// Can not write anything further
					return false; // require further writes
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
		 * @param exception
		 *            Possible {@link Throwable} being the cause of the close of
		 *            the connection. <code>null</code> if normal close.
		 */
		private final void unsafeCloseConnection(Throwable exception) {
			SocketManager.terminteSelectionKey(this.selectionKey, this.socketListener);

			// Release the stream buffers
			this.releaseStreamBuffers();
		}

		/*
		 * ============== AbstractReadHandler ================
		 */

		@Override
		public final void handleRead(boolean isNewBuffer) {

			// Only invoked by Socket Listener thread

			// Keep track of buffers (to enable releasing)
			if ((this.previousRequestBuffer != null) && (this.readBuffer != this.previousRequestBuffer)) {
				// New buffer (release previous on servicing request)
				this.previousRequestBuffer.next = this.releaseRequestBuffers;
				this.releaseRequestBuffers = this.previousRequestBuffer;
			}
			this.previousRequestBuffer = this.readBuffer;

			// Service the socket
			this.socketServicer.service(this.readBuffer, isNewBuffer);

			// Ensure flush writes
			this.unsafeFlushWrites();
		}

		@Override
		public final void releaseStreamBuffers() {

			// Only invoked by Socket Listener thread

			// Release socket read buffer
			if (this.previousRequestBuffer != null) {
				if (this.readBuffer != this.previousRequestBuffer) {
					// Fail on read, so no handle
					this.readBuffer.release();
				}
				this.previousRequestBuffer.release();
				this.previousRequestBuffer = null;
			}

			// Release the request buffers
			while (this.releaseRequestBuffers != null) {
				StreamBuffer<ByteBuffer> release = this.releaseRequestBuffers;
				this.releaseRequestBuffers = this.releaseRequestBuffers.next;
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
				try {
					// Trigger to undertake execution on socket thread
					this.socketListener.executionHandler.safeExecute(this, execution);
				} catch (IOException ex) {
					// Failed, so close connection
					this.closeConnection(ex);
				}
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
				if (streamBuffer.isPooled) {
					streamBuffer.pooledBuffer.flip();
				}
				streamBuffer = streamBuffer.next;
			}

			// Append to write
			this.unsafeAppendWrite(immediateHead);

			// Immediately flush data
			this.unsafeSendWrites();
		}

		@Override
		public final void closeConnection(Throwable exception) {
			// Appropriately execute based on thread safety
			if (this.socketListener.isSocketListenerThread()) {
				this.unsafeCloseConnection(exception);

			} else {
				try {
					// Trigger to close the connection
					this.socketListener.safeCloseConnectionHandler.safeCloseConnection(this, exception);
				} catch (IOException ex) {
					// Failed, so forcefully terminate connection
					LOGGER.log(Level.WARNING, "Failed to safely close connectin", ex);
					SocketManager.terminteSelectionKey(this.selectionKey, this.socketListener);
				}
			}
		}
	}

	/**
	 * Handles safely executing the {@link Execution}.
	 */
	private static class ExecutionHandler extends AbstractReadHandler {

		/**
		 * Notify execution {@link Pipe}.
		 */
		private final Pipe executionPipe;

		/**
		 * {@link Execution} instances.
		 */
		private final List<AcceptedSocketExecution> executions = new ArrayList<>();

		/**
		 * Indicates if notified.
		 */
		private boolean isNotified = false;

		/**
		 * Instantiate.
		 * 
		 * @param executionPipe
		 *            Notify execution {@link Pipe}.
		 */
		private ExecutionHandler(Pipe executionPipe) {
			super(executionPipe.source());
			this.executionPipe = executionPipe;
		}

		/**
		 * <p>
		 * Safely executes the {@link Execution}.
		 * <p>
		 * Method is synchronised to ensure data is safe across the
		 * {@link Thread}, when {@link #handleRead()} is invoked by the
		 * {@link SocketListener} {@link Thread}.
		 * 
		 * @param acceptedSocket
		 *            {@link AcceptedSocketServicer}.
		 * @param execution
		 *            {@link Execution}.
		 * @throws IOException
		 *             If fails to execute the {@link Execution}.
		 */
		private synchronized final <R> void safeExecute(AcceptedSocketServicer<R> acceptedSocket, Execution execution)
				throws IOException {

			// Capture the execution
			this.executions.add(new AcceptedSocketExecution(acceptedSocket, execution));

			// Notify to execute
			if (!this.isNotified) {
				this.executionPipe.sink().write(NOTIFY_BUFFER.duplicate());
				this.isNotified = true;
			}
		}

		/*
		 * ============== AbstractReadHandler ================
		 */

		@Override
		public final void handleRead(boolean isNewBuffer) {

			// Safely within the SocketListener thread

			// Only notification, content not important
			this.readBuffer.pooledBuffer.clear();

			// Take copy to minimise service threads blocking to send
			AcceptedSocketExecution[] executions;
			synchronized (this) {
				this.isNotified = false; // no longer notified
				executions = this.executions.toArray(new AcceptedSocketExecution[this.executions.size()]);
				this.executions.clear();
			}

			// Execute each execution
			for (int i = 0; i < executions.length; i++) {
				AcceptedSocketExecution execution = executions[i];
				execution.acceptedSocket.unsafeExecute(execution.execution);
			}
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
		 * @param acceptedSocket
		 *            {@link AcceptedSocketServicer}.
		 * @param execution
		 *            {@link Execution}.
		 */
		public AcceptedSocketExecution(AcceptedSocketServicer<?> acceptedSocket, Execution execution) {
			this.acceptedSocket = acceptedSocket;
			this.execution = execution;
		}
	}

	/**
	 * Handles safely writing the {@link Socket} data (from another
	 * {@link Thread} than the {@link SocketListener} {@link Thread}).
	 */
	private static class SafeWriteSocketHandler extends AbstractReadHandler {

		/**
		 * Notify write {@link Pipe}.
		 */
		private final Pipe writeSocketPipe;

		/**
		 * {@link SafeWriteResponse} instances.
		 */
		private final List<SafeWriteResponse<?>> responses = new ArrayList<>();

		/**
		 * Indicates if notified.
		 */
		private boolean isNotified = false;

		/**
		 * Instantiate.
		 * 
		 * @param writeSocketPipe
		 *            Notify write {@link Pipe}.
		 */
		private SafeWriteSocketHandler(Pipe writeSocketPipe) {
			super(writeSocketPipe.source());
			this.writeSocketPipe = writeSocketPipe;
		}

		/**
		 * <p>
		 * Safely writes the response.
		 * <p>
		 * Method is synchronised to ensure data is safe across the
		 * {@link Thread}, when {@link #handleRead()} is invoked by the
		 * {@link SocketListener} {@link Thread}.
		 * 
		 * @param acceptedSocket
		 *            {@link AcceptedSocketServicer}.
		 * @param socketRequest
		 *            {@link SocketRequest}.
		 * @param responseHeaderWriter
		 *            {@link ResponseHeaderWriter}.
		 * @param headResponseBuffer
		 *            Head {@link StreamBuffer} to the linked list of
		 *            {@link StreamBuffer} instances for the response.
		 * @throws IOException
		 *             If fails to write the response.
		 */
		private synchronized final <R> void safeWriteResponse(AcceptedSocketServicer<R> acceptedSocket,
				SocketRequest<R> socketRequest, ResponseHeaderWriter responseHeaderWriter,
				StreamBuffer<ByteBuffer> headResponseBuffer) throws IOException {

			// Capture the response
			// Note does not write as socket listener thread may be accessing
			this.responses.add(
					new SafeWriteResponse<>(acceptedSocket, socketRequest, responseHeaderWriter, headResponseBuffer));

			// Notify to write the response
			if (!this.isNotified) {
				this.writeSocketPipe.sink().write(NOTIFY_BUFFER.duplicate());
				this.isNotified = true;
			}
		}

		/*
		 * ============== AbstractReadHandler ================
		 */

		@Override
		@SuppressWarnings("unchecked")
		public final void handleRead(boolean isNewBuffer) {

			// Safely within the SocketListener thread

			// Only notification, content not important
			this.readBuffer.pooledBuffer.clear();

			// Take copy to minimise service threads blocking to send
			SafeWriteResponse<?>[] responses;
			synchronized (this) {
				this.isNotified = false; // no longer notified
				responses = this.responses.toArray(new SafeWriteResponse[this.responses.size()]);
				this.responses.clear();
			}

			// Write the responses
			// Separate to flush, to enable multiple response writing in packets
			for (int i = 0; i < responses.length; i++) {
				@SuppressWarnings("rawtypes")
				SafeWriteResponse response = responses[i];
				response.acceptedSocket.unsafeWriteResponse(response.socketRequest, response.responseHeaderWriter,
						response.headResponseBuffer);
			}

			// Flush the writes
			for (int i = 0; i < responses.length; i++) {
				responses[i].acceptedSocket.unsafeFlushWrites();
			}
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
		 * @param acceptedSocket
		 *            {@link AcceptedSocketServicer}.
		 * @param socketRequest
		 *            {@link SocketRequest}.
		 * @param responseHeaderWriter
		 *            {@link ResponseHeaderWriter}.
		 * @param headResponseBuffer
		 *            Head {@link StreamBuffer} to the linked list of
		 *            {@link StreamBuffer} instances for the response.
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
		 * Head request {@link StreamBuffer} of the linked list of
		 * {@link StreamBuffer} instances.
		 */
		private final StreamBuffer<ByteBuffer> headRequestBuffer;

		/**
		 * {@link ResponseHeaderWriter}.
		 */
		private ResponseHeaderWriter responseHeaderWriter;

		/**
		 * Head response {@link StreamBuffer} of the linked list of
		 * {@link StreamBuffer} instances.
		 */
		private StreamBuffer<ByteBuffer> headResponseBuffer = null;

		/**
		 * Next {@link SocketRequest}.
		 */
		private SocketRequest<R> next = null;

		/**
		 * Instantiate.
		 * 
		 * @param acceptedSocket
		 *            {@link AcceptedSocketServicer}.
		 * @param headRequestBuffer
		 *            Request {@link StreamBuffer} instances.
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
				try {
					// Writes to the request, so may happen in any order
					this.acceptedSocket.socketListener.safeWriteSocketHandler.safeWriteResponse(this.acceptedSocket,
							this, responseHeaderWriter, headResponseBuffers);
				} catch (IOException ex) {
					// Failed, so close connection
					this.acceptedSocket.closeConnection(ex);
				}
			}
		}
	}

	/**
	 * Safely closes connection handler.
	 */
	private static class SafeCloseConnectionHandler extends AbstractReadHandler {

		/**
		 * Notify close connection {@link Pipe}.
		 */
		private final Pipe closeConnectionPipe;

		/**
		 * {@link SafeCloseConnection} instances to close.
		 */
		private final List<SafeCloseConnection> connectionsToClose = new ArrayList<>();

		/**
		 * Indicates if notified.
		 */
		private boolean isNotified = false;

		/**
		 * Instantiate.
		 * 
		 * @param closeConnectionPipe
		 *            Close connection {@link Pipe}.
		 */
		private SafeCloseConnectionHandler(Pipe closeConnectionPipe) {
			super(closeConnectionPipe.source());
			this.closeConnectionPipe = closeConnectionPipe;
		}

		/**
		 * Safely closes the connection.
		 * 
		 * @param acceptedSocket
		 *            {@link AcceptedSocketServicer} to close.
		 * @param exception
		 *            Possible {@link Throwable} for cause of closing
		 *            connection.
		 * @throws IOException
		 *             If fails to close the connection.
		 */
		private synchronized final void safeCloseConnection(AcceptedSocketServicer<?> acceptedSocket,
				Throwable exception) throws IOException {

			// Add to sockets to close
			this.connectionsToClose.add(new SafeCloseConnection(acceptedSocket, exception));

			// Notify to close the socket
			if (!this.isNotified) {
				this.closeConnectionPipe.sink().write(NOTIFY_BUFFER.duplicate());
				this.isNotified = true;
			}
		}

		/*
		 * ============== AbstractReadHandler ================
		 */

		@Override
		public final void handleRead(boolean isNewBuffer) {

			// Safely within the SocketListener thread

			// Only notification, content not important
			this.readBuffer.pooledBuffer.clear();

			// Take copy to minimise service threads blocking to send
			SafeCloseConnection[] connectionsToClose;
			synchronized (this) {
				this.isNotified = false; // no longer notified
				connectionsToClose = this.connectionsToClose
						.toArray(new SafeCloseConnection[this.connectionsToClose.size()]);
				this.connectionsToClose.clear();
			}

			// Close the accepted sockets
			for (int i = 0; i < connectionsToClose.length; i++) {
				SafeCloseConnection connection = connectionsToClose[i];
				connection.acceptedSocket.unsafeCloseConnection(connection.exception);
			}
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
		 * @param acceptedSocket
		 *            {@link AcceptedSocketServicer} to close.
		 * @param exception
		 *            Possible {@link Throwable} for cuase of closing the
		 *            connection. <code>null</code> if normal close.
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
		 * @param channel
		 *            {@link ReadableByteChannel}.
		 * @param listener
		 *            {@link SocketListener} to shut down.
		 */
		private ShutdownReadHandler(ReadableByteChannel channel, SocketListener listener) {
			super(channel);
			this.listener = listener;
		}

		/*
		 * ============== AbstractReadHandler ================
		 */

		@Override
		public final void handleRead(boolean isNewBuffer) {

			// Release buffer (as content not important, only notification)
			this.readBuffer.pooledBuffer.clear();

			// Terminate all keys
			for (SelectionKey key : this.listener.selector.keys()) {
				SocketManager.terminteSelectionKey(key, this.listener);
			}

			// Flag to shutdown
			this.listener.isShutdown = true;
		}
	}

}