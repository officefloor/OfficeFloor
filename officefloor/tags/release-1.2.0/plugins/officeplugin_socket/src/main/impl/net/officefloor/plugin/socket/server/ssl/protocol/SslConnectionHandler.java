/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2011 Daniel Sagenschneider
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

package net.officefloor.plugin.socket.server.ssl.protocol;

import java.io.IOException;

import javax.net.ssl.SSLEngine;

import net.officefloor.plugin.socket.server.Connection;
import net.officefloor.plugin.socket.server.ConnectionHandler;
import net.officefloor.plugin.socket.server.ConnectionHandlerContext;
import net.officefloor.plugin.socket.server.IdleContext;
import net.officefloor.plugin.socket.server.ReadContext;
import net.officefloor.plugin.socket.server.ServerSocketHandler;
import net.officefloor.plugin.socket.server.WriteContext;
import net.officefloor.plugin.socket.server.ssl.SslConnection;
import net.officefloor.plugin.socket.server.ssl.SslTaskExecutor;
import net.officefloor.plugin.socket.server.ssl.TemporaryByteArrayFactory;
import net.officefloor.plugin.stream.BufferSquirtFactory;
import net.officefloor.plugin.stream.InputBufferStream;

/**
 * SSL {@link ConnectionHandler}.
 * 
 * @author Daniel Sagenschneider
 */
public class SslConnectionHandler<CH extends ConnectionHandler> implements
		ConnectionHandler, TemporaryByteArrayFactory, ReadContext,
		WriteContext, IdleContext {

	/**
	 * {@link SslConnection}.
	 */
	private final SslConnection connection;

	/**
	 * Wrapped {@link ConnectionHandler}.
	 */
	private final CH wrappedConnectionHandler;

	/**
	 * <p>
	 * {@link SslContextObject}.
	 * <p>
	 * <code>volatile</code> as {@link TemporaryByteArrayFactory} will require
	 * to use it from another thread. As set only once on first read, it will
	 * always be available.
	 */
	private volatile SslContextObject contextObject;

	/**
	 * To enable wrapped {@link ConnectionHandler} to use context object, need
	 * to intercept this and delegate to the {@link SslContextObject} to
	 * provide.
	 */
	private ConnectionHandlerContext connectionHandlerContext;

	/**
	 * {@link ReadContext} for additional read methods.
	 */
	private ReadContext readContext;

	/**
	 * Initiate.
	 * 
	 * @param connection
	 *            {@link Connection}.
	 * @param engine
	 *            {@link SSLEngine}.
	 * @param bufferSquirtFactory
	 *            {@link BufferSquirtFactory}.
	 * @param taskExecutor
	 *            {@link SslTaskExecutor}.
	 * @param wrappedServerSocketHandler
	 *            Wrapped {@link ServerSocketHandler}.
	 */
	public SslConnectionHandler(Connection connection, SSLEngine engine,
			BufferSquirtFactory bufferSquirtFactory,
			SslTaskExecutor taskExecutor,
			ServerSocketHandler<CH> wrappedServerSocketHandler) {

		// Creates the SSL connection
		this.connection = new SslConnectionImpl(connection.getLock(),
				connection.getLocalAddress(), connection.getRemoteAddress(),
				connection.getInputBufferStream(),
				connection.getOutputBufferStream(), engine,
				bufferSquirtFactory, this, taskExecutor);

		// Create the connection handler to wrap
		this.wrappedConnectionHandler = wrappedServerSocketHandler
				.createConnectionHandler(this.connection);
	}

	/**
	 * Obtains the wrapped {@link ConnectionHandler}.
	 * 
	 * @return Wrapped {@link ConnectionHandler}.
	 */
	public CH getWrappedConnectionHandler() {
		return this.wrappedConnectionHandler;
	}

	/*
	 * ======================== ConnectionHandler ============================
	 */

	@Override
	@SuppressWarnings("unchecked")
	public void handleRead(ReadContext context) throws IOException {

		// Ensure have SSL context object
		if (this.contextObject == null) {
			// Attempt to obtain shared context object (via double check lock)
			this.contextObject = (SslContextObject) context.getContextObject();
			if (this.contextObject == null) {
				synchronized (context) {
					this.contextObject = (SslContextObject) context
							.getContextObject();
					if (this.contextObject == null) {
						// Create as first handle read for context
						this.contextObject = new SslContextObject();
						context.setContextObject(this.contextObject);
					}
				}
			}
		}

		// Process the data from peer
		this.connection.processDataFromPeer();

		// Always handle read even if no application data (stop timeouts)
		this.connectionHandlerContext = context;
		this.readContext = context;
		this.wrappedConnectionHandler.handleRead(this);
	}

	@Override
	public void handleWrite(WriteContext context) throws IOException {
		// Delegate to wrapped handler
		this.connectionHandlerContext = context;
		this.wrappedConnectionHandler.handleWrite(this);
	}

	@Override
	public void handleIdleConnection(IdleContext context) throws IOException {

		// Ensure no error in processing
		this.connection.validate();

		// Delegate to wrapped handler
		this.connectionHandlerContext = context;
		this.wrappedConnectionHandler.handleIdleConnection(this);
	}

	/*
	 * ================= TemporaryByteArrayFactory =============================
	 */

	@Override
	public byte[] createDestinationByteArray(int minimumSize) {

		// Obtain existing array (context will always be available)
		byte[] array = this.contextObject.destinationBytes;

		// Ensure have array and is big enough
		if ((array == null) || (array.length < minimumSize)) {
			// Increase array size to minimum size required
			array = new byte[minimumSize];

			// Make larger array available for further processing
			this.contextObject.destinationBytes = array;
		}

		// Return the array
		return array;
	}

	@Override
	public byte[] createSourceByteArray(int minimumSize) {

		// Obtain existing array (context will always be available)
		byte[] array = this.contextObject.sourceBytes;

		// Ensure have array and is big enough
		if ((array == null) || (array.length < minimumSize)) {
			// Increase array size to minimum size required
			array = new byte[minimumSize];

			// Make larger array available for further processing
			this.contextObject.sourceBytes = array;
		}

		// Return the array
		return array;
	}

	/*
	 * ===================== ConnectionHandlerContext =========================
	 */

	@Override
	public long getTime() {
		return this.connectionHandlerContext.getTime();
	}

	@Override
	public void setCloseConnection(boolean isClose) {
		this.connectionHandlerContext.setCloseConnection(isClose);
	}

	@Override
	public Object getContextObject() {
		// Use context object wrapped object
		return this.contextObject.wrappedContextObject;
	}

	@Override
	public void setContextObject(Object contextObject) {
		// Use context object wrapped object
		this.contextObject.wrappedContextObject = contextObject;
	}

	/*
	 * ==================== ReadContext =====================================
	 */

	@Override
	public InputBufferStream getInputBufferStream() {
		// Use the plain text input
		return this.connection.getInputBufferStream();
	}

	@Override
	public void processRequest(Object attachment) throws IOException {
		this.readContext.processRequest(attachment);
	}

	/**
	 * {@link ConnectionHandlerContext} context object for SSL.
	 */
	private class SslContextObject {

		/**
		 * Sources bytes for {@link TemporaryByteArrayFactory}.
		 */
		public volatile byte[] sourceBytes = null;

		/**
		 * Destination bytes for {@link TemporaryByteArrayFactory}.
		 */
		public volatile byte[] destinationBytes = null;

		/**
		 * Wrapped context object.
		 */
		public Object wrappedContextObject = null;

	}

}