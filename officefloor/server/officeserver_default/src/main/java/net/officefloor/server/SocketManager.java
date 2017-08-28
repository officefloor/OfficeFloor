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
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.officefloor.server.stream.BufferPool;
import net.officefloor.server.stream.StreamBuffer;

/**
 * Manages the {@link Socket} interaction.
 * 
 * @author Daniel Sagenschneider
 */
public class SocketManager {

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
	 *            {@link BufferPool}.
	 * @throws IOException
	 *             If fails to initialise {@link Socket} management.
	 */
	public SocketManager(int listenerCount, BufferPool<ByteBuffer> bufferPool) throws IOException {
		this.listeners = new SocketListener[listenerCount];
		for (int i = 0; i < listeners.length; i++) {
			listeners[i] = new SocketListener(bufferPool);
		}
	}

	/**
	 * Obtains the {@link Runnable} instances to be executed for this
	 * {@link SocketManager}.
	 * 
	 * @return {@link Runnable} instances to be executed for this
	 *         {@link SocketManager}.
	 */
	public Runnable[] getRunnables() {
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
	 * @param socketServicer
	 *            {@link SocketServicer} to service accepted connections.
	 * @param requestServicer
	 *            {@link RequestServicer} to service requests on the
	 *            {@link Socket}.
	 * @throws IOException
	 *             If fails to bind the {@link ServerSocket}.
	 */
	public synchronized <R> void bindServerSocket(int port, ServerSocketDecorator serverSocketDecorator,
			AcceptedSocketDecorator acceptedSocketDecorator, SocketServicer<R> socketServicer,
			RequestServicer<R> requestServicer) throws IOException {

		// Spread acceptances listening across the listeners
		int next = this.nextServerSocketListener;
		this.nextServerSocketListener = (this.nextServerSocketListener + 1) % this.listeners.length;

		// Register server socket listening
		ServerSocket serverSocket = this.listeners[next].bindServerSocket(port, serverSocketDecorator,
				acceptedSocketDecorator, socketServicer, requestServicer);
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
	public synchronized void shutdown() throws IOException {

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
	 *            {@link AcceptedSocket}.
	 * @throws IOException
	 *             If fails to managed the {@link AcceptedSocket}.
	 */
	private <R> void manageAcceptedSocket(AcceptedSocket<R> acceptedSocket) throws IOException {

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
		 * {@link BufferPool}.
		 */
		private final BufferPool<ByteBuffer> bufferPool;

		/**
		 * {@link Selector}.
		 */
		private final Selector selector;

		/**
		 * {@link AcceptSocketHandler}.
		 */
		private final AcceptSocketHandler acceptSocketHandler;

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
		private SocketListener(BufferPool<ByteBuffer> bufferPool) throws IOException {
			this.bufferPool = bufferPool;

			// Create the selector
			this.selector = Selector.open();

			// Create pipe to listen for accepted sockets
			Pipe acceptedSocketPipe = Pipe.open();
			acceptedSocketPipe.source().configureBlocking(false);
			this.acceptSocketHandler = new AcceptSocketHandler(acceptedSocketPipe, this);
			acceptedSocketPipe.source().register(this.selector, SelectionKey.OP_READ, this.acceptSocketHandler);

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
		 * @param socketServicer
		 *            {@link SocketServicer}.
		 * @param requestServicer
		 *            {@link RequestServicer}.
		 * @return Bound {@link ServerSocket}.
		 * @throws IOException
		 *             If fails to bind the {@link ServerSocket}.
		 */
		private <R> ServerSocket bindServerSocket(int port, ServerSocketDecorator serverSocketDecorator,
				AcceptedSocketDecorator acceptedSocketDecorator, SocketServicer<R> socketServicer,
				RequestServicer<R> requestServicer) throws IOException {

			// Create the port socket address
			InetSocketAddress portAddress = new InetSocketAddress(port);

			// Create the Server Socket
			ServerSocketChannel channel = ServerSocketChannel.open();
			channel.configureBlocking(false);
			ServerSocket socket = channel.socket();
			socket.setReuseAddress(true);
			int serverSocketBackLogSize = 25000;
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
			channel.register(this.selector, SelectionKey.OP_ACCEPT,
					new AcceptHandler<R>(channel, acceptedSocketDecorator, socketServicer, requestServicer));

			// Return the server socket
			return socket;
		}

		/**
		 * Shuts down this {@link SocketListener}.
		 * 
		 * @throws IOException
		 *             If fails to notify of shutdown.
		 */
		private void shutdown() throws IOException {
			// Send message to shutdown
			this.shutdownPipe.sink().write(NOTIFY_BUFFER.duplicate());
		}

		/*
		 * =================== Runnable =========================
		 */

		@Override
		public void run() {

			try {
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
								handler.acceptedSocketDecorator.decorate(socket);

								// Manage the accepted socket
								AcceptedSocket<Object> acceptedSocket = new AcceptedSocket<>(socketChannel,
										handler.socketServicer, handler.requestServicer);
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

							// Keep reading data until empty socket buffer
							boolean isFurtherDataToRead = true;
							while (isFurtherDataToRead) {

								// Ensure have buffer to handle read
								if ((handler.readBuffer == null)
										|| (handler.readBuffer.getPooledBuffer().remaining() == 0)) {
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

			} finally {
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
	 * Abstract functionality for handling reads.
	 */
	private static abstract class AbstractReadHandler {

		/**
		 * Current {@link StreamBuffer} for read content.
		 */
		protected StreamBuffer<ByteBuffer> readBuffer;

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
	 * Accepted {@link Socket}.
	 */
	private static class AcceptedSocket<R> extends AbstractReadHandler implements RequestHandler<R> {

		/**
		 * {@link SocketChannel} for the accepted {@link Socket}.
		 */
		protected final SocketChannel socketChannel;

		/**
		 * {@link SocketServicer}.
		 */
		private final SocketServicer<R> socketServicer;

		/**
		 * {@link RequestServicer}.
		 */
		private final RequestServicer<R> requestServicer;

		/**
		 * Head {@link SocketRequest}.
		 */
		private SocketRequest<R> head = null;

		/**
		 * Tail {@link SocketRequest}.
		 */
		private SocketRequest<R> tail = null;

		/**
		 * Instantiate.
		 * 
		 * @param socketChannel
		 *            {@link SocketChannel}.
		 * @param socketServicer
		 *            {@link SocketServicer}.
		 * @param requestServicer
		 *            {@link RequestServicer}.
		 */
		public AcceptedSocket(SocketChannel socketChannel, SocketServicer<R> socketServicer,
				RequestServicer<R> requestServicer) {
			super(socketChannel);
			this.socketChannel = socketChannel;
			this.socketServicer = socketServicer;
			this.requestServicer = requestServicer;
		}

		/**
		 * Writes the response.
		 * 
		 * @param socketRequest
		 *            {@link SocketRequest}.
		 * @param responseBuffers
		 *            Response {@link StreamBuffer} instances for the
		 *            {@link SocketRequest}.
		 */
		private void writeResponse(SocketRequest<R> socketRequest, Iterable<StreamBuffer<ByteBuffer>> responseBuffers) {

			// TODO determine if thread safe

			// Provide the response buffers for the request
			socketRequest.responseBuffers = responseBuffers;

			// Write buffers in order (and as available)
			while ((this.head != null) && (this.head.responseBuffers != null)) {

				// Write the response
				Iterator<StreamBuffer<ByteBuffer>> iterator = this.head.responseBuffers.iterator();
				while (iterator.hasNext()) {

					// Obtain the next buffer
					StreamBuffer<ByteBuffer> streamBuffer = iterator.next();
					ByteBuffer buffer = (streamBuffer.isPooled() ? streamBuffer.getPooledBuffer()
							: streamBuffer.getUnpooledByteBuffer());

					// Prepare the buffer
					buffer.flip();

					// Write the buffer
					try {
						this.socketChannel.write(buffer);
					} catch (IOException ex) {
						// Failed to write to channel, so close
						try {
							this.socketChannel.socket().close();
						} catch (IOException closeException) {
							LOGGER.log(Level.WARNING, "Failed to close connection", closeException);
						}
					}

					// Determine if written all bytes
					if (buffer.remaining() != 0) {
						// Not all bytes written, so write when buffer emptied

						// TODO handle take interest in write

						// Can not write anything further
						return;
					}

					// Data written, so remove buffer and release it
					iterator.remove();
					streamBuffer.release();
				}

				// Write head response, so move onto next request
				this.head = this.head.next;
			}
		}

		/*
		 * ============== AbstractReadHandler ================
		 */

		@Override
		public void handleRead() {

			// TODO keep track of buffers (to enable releasing)

			// Service the socket
			this.socketServicer.service(this.readBuffer, this);
		}

		/*
		 * ================ RequestHandler ===================
		 */

		@Override
		public void handleRequest(R request) {

			// Create the socket request
			SocketRequest<R> socketRequest = new SocketRequest<>(this);

			// TODO determine if thread safe

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
	}

	/**
	 * Request on the {@link Socket}.
	 */
	private static class SocketRequest<R> implements ResponseWriter {

		/**
		 * {@link AcceptedSocket}.
		 */
		private final AcceptedSocket<R> acceptedSocket;

		/**
		 * Next {@link SocketRequest}.
		 */
		private SocketRequest<R> next = null;

		/**
		 * Response {@link StreamBuffer} instances.
		 */
		private Iterable<StreamBuffer<ByteBuffer>> responseBuffers = null;

		/**
		 * Instantiate.
		 * 
		 * @param acceptedSocket
		 *            {@link AcceptedSocket}.
		 */
		private SocketRequest(AcceptedSocket<R> acceptedSocket) {
			this.acceptedSocket = acceptedSocket;
		}

		/*
		 * ================== ResponseWriter ==================
		 */

		@Override
		public void write(Iterable<StreamBuffer<ByteBuffer>> responseBuffers) {
			this.acceptedSocket.writeResponse(this, responseBuffers);
		}
	}

	/**
	 * Accept {@link Socket} handler.
	 */
	private static class AcceptSocketHandler extends AbstractReadHandler {

		/**
		 * {@link AcceptedSocket} {@link Pipe}.
		 */
		private final Pipe acceptedSocketPipe;

		/**
		 * {@link SocketListener}.
		 */
		private final SocketListener listener;

		/**
		 * {@link Queue} of {@link AcceptedSocket} instances.
		 */
		private Queue<AcceptedSocket<?>> acceptedSockets = new ConcurrentLinkedQueue<>();

		/**
		 * Instantiate.
		 * 
		 * @param acceptedSocketPipe
		 *            {@link AcceptedSocket} {@link Pipe}.
		 * @param listener
		 *            {@link SocketListener}.
		 */
		private AcceptSocketHandler(Pipe acceptedSocketPipe, SocketListener listener) {
			super(acceptedSocketPipe.source());
			this.acceptedSocketPipe = acceptedSocketPipe;
			this.listener = listener;
		}

		/**
		 * Services an {@link AcceptedSocket}.
		 * 
		 * @param accceptedSocket
		 *            {@link AcceptedSocket}.
		 * @throws IOException
		 *             If fails to service the {@link AcceptedSocket}.
		 */
		private <R> void serviceAcceptedSocket(AcceptedSocket<R> accceptedSocket) throws IOException {

			// Queue for adding
			this.acceptedSockets.add(accceptedSocket);

			// Notify queue accepted sockets
			this.acceptedSocketPipe.sink().write(NOTIFY_BUFFER.duplicate());
		}

		/*
		 * ============== AbstractReadHandler ================
		 */

		@Override
		public void handleRead() {

			// Release buffer (as content not important, only notification)
			this.readBuffer.release();
			this.readBuffer = null;

			// Register the accepted sockets
			Iterator<AcceptedSocket<?>> iterator = this.acceptedSockets.iterator();
			while (iterator.hasNext()) {

				// Obtain the accepted socket and register
				AcceptedSocket<?> acceptedSocket = iterator.next();
				try {
					acceptedSocket.socketChannel.register(this.listener.selector, SelectionKey.OP_READ, acceptedSocket);
				} catch (IOException ex) {
					LOGGER.log(Level.WARNING, "Failed to register accepted socket", ex);
				}

				// Socket registered, so remove
				iterator.remove();
			}
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
		public void handleRead() {

			// Release buffer (as content not important, only notification)
			this.readBuffer.release();
			this.readBuffer = null;

			// Cancel all keys
			for (SelectionKey key : this.listener.selector.keys()) {
				key.cancel();
			}

			// Flag to shutdown
			this.listener.isShutdown = true;
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
		 * {@link SocketServicer}.
		 */
		private final SocketServicer<R> socketServicer;

		/**
		 * {@link RequestServicer}.
		 */
		private final RequestServicer<R> requestServicer;

		/**
		 * Instantiate.
		 * 
		 * @param channel
		 *            {@link ServerSocketChannel}.
		 * @param acceptedSocketDecorator
		 *            {@link AcceptedSocketDecorator}.
		 * @param socketServicer
		 *            {@link SocketServicer}.
		 * @param requestServicer
		 *            {@link RequestServicer}.
		 */
		private AcceptHandler(ServerSocketChannel channel, AcceptedSocketDecorator acceptedSocketDecorator,
				SocketServicer<R> socketServicer, RequestServicer<R> requestServicer) {
			this.channel = channel;
			this.acceptedSocketDecorator = acceptedSocketDecorator;
			this.socketServicer = socketServicer;
			this.requestServicer = requestServicer;
		}
	}

}