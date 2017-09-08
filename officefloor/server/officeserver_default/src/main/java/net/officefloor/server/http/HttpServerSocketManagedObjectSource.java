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
package net.officefloor.server.http;

import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.sql.Connection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.net.ssl.SSLContext;

import net.officefloor.compile.impl.util.CompileUtil;
import net.officefloor.compile.properties.Property;
import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.function.ManagedFunctionContext;
import net.officefloor.frame.api.function.ManagedFunctionFactory;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.recycle.RecycleManagedObjectParameter;
import net.officefloor.frame.api.managedobject.source.ManagedObjectExecuteContext;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSourceContext;
import net.officefloor.frame.api.managedobject.source.impl.AbstractManagedObjectSource;
import net.officefloor.server.AcceptedSocketDecorator;
import net.officefloor.server.RequestServicerFactory;
import net.officefloor.server.ServerSocketDecorator;
import net.officefloor.server.SocketManager;
import net.officefloor.server.SocketServicerFactory;
import net.officefloor.server.buffer.ThreadLocalStreamBufferPool;
import net.officefloor.server.http.impl.ProcessAwareServerHttpConnectionManagedObject;
import net.officefloor.server.http.parse.HttpRequestParser.HttpRequestParserMetaData;
import net.officefloor.server.ssl.OfficeFloorDefaultSslContextSource;
import net.officefloor.server.ssl.SslContextSource;
import net.officefloor.server.ssl.SslSocketServicerFactory;
import net.officefloor.server.stream.StreamBuffer;
import net.officefloor.server.stream.StreamBufferPool;

/**
 * {@link ManagedObjectSource} for a {@link ServerHttpConnection}.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpServerSocketManagedObjectSource extends AbstractManagedObjectSource<None, Indexed>
		implements ManagedFunction<Indexed, None> {

	/**
	 * {@link Logger}.
	 */
	private final Logger LOGGER = Logger.getLogger(this.getClass().getName());

	/**
	 * Name of {@link System} property to specify the number of
	 * {@link SocketListener} instances. If not specified, will default to
	 * {@link Runtime#availableProcessors()}.
	 */
	public static final String SYSTEM_PROPERTY_SOCKET_LISTENER_COUNT = "officefloor.socket.listener.count";

	/**
	 * Name of {@link System} property to obtain the pooled {@link StreamBuffer}
	 * size.
	 */
	public static final String SYSTEM_PROPERTY_BUFFER_SIZE = "officefloor.socket.buffer.size";

	/**
	 * Name of {@link System} property to obtain the maximum
	 * {@link ThreadLocalStreamBufferPool} {@link ThreadLocal} pool size.
	 */
	public static final String SYSTEM_PROPERTY_THREADLOCAL_BUFFER_POOL_MAX_SIZE = "officefloor.socket.threadlocal.buffer.pool.max.size";

	/**
	 * Name of {@link System} property to obtain the maximum
	 * {@link ThreadLocalStreamBufferPool} core pool size.
	 */
	public static final String SYSTEM_PROPERTY_CORE_BUFFER_POOL_MAX_SIZE = "officefloor.socket.core.buffer.pool.max.size";

	/**
	 * Name of {@link Property} for the port.
	 */
	public static final String PROPERTY_PORT = "port";

	/**
	 * Name of {@link Property} indicating if secure.
	 */
	public static final String PROPERTY_SECURE = "secure";

	/**
	 * Name of {@link Property} for the {@link SslContextSource}.
	 */
	public static final String PROPERTY_SSL_CONTEXT_SOURCE = "ssl.context.source";

	/**
	 * Value for {@link #PROPERTY_SSL_CONTEXT_SOURCE} to indicate that this
	 * server is behind a reverse proxy. This enables the reverse proxy to
	 * handle SSL and communicate with this server via insecure {@link Socket}
	 * (but stll appear as a secure {@link ServerHttpConnection}).
	 */
	public static final String SSL_REVERSE_PROXIED = "reverse-proxied";

	/**
	 * Name of {@link Property} for the maximum {@link HttpHeader} per
	 * {@link HttpRequest}.
	 */
	public static final String PROPERTY_MAX_HEADER_COUNT = "max.headers";

	/**
	 * Name of {@link Property} for the maximum text length for HTTP parsing.
	 */
	public static final String PROPERTY_MAX_TEXT_LENGTH = "max.text.length";

	/**
	 * Name of {@link Property} for the maximum entity length for HTTP parsing.
	 */
	public static final String PROPERTY_MAX_ENTITY_LENGTH = "max.entity.length";

	/**
	 * Name of {@link Property} for the size of the {@link StreamBuffer}
	 * instances for the service.
	 */
	public static final String PROPERTY_SERVICE_BUFFER_SIZE = "service.buffer.size";

	/**
	 * Singleton {@link SocketManager} for all {@link Connection} instances.
	 */
	private static SocketManager singletonSocketManager;

	/**
	 * {@link ExecutorService} for executing {@link SocketManager}.
	 */
	private static ExecutorService executor;

	/**
	 * Registered {@link HttpServerSocketManagedObjectSource} instances.
	 */
	private static Set<HttpServerSocketManagedObjectSource> registeredServerSocketManagedObjectSources = new HashSet<HttpServerSocketManagedObjectSource>();

	/**
	 * Obtains the {@link System} property value.
	 * 
	 * @param name
	 *            Name of the {@link System} property.
	 * @param defaultValue
	 *            Default value.
	 * @return {@link System} property value.
	 */
	private static int getSystemProperty(String name, int defaultValue) {
		String text = System.getProperty(name, null);
		if (CompileUtil.isBlank(text)) {
			// No value configured, so use default
			return defaultValue;

		} else {
			// Attempt to parse the configured value
			try {
				return Integer.parseInt(text);
			} catch (NumberFormatException ex) {
				// Invalid value
				throw new NumberFormatException(
						"Invalid system configured value for " + name + " '" + text + "'.  Must be an integer.");
			}
		}
	}

	/**
	 * Obtains the {@link SocketManager}.
	 * 
	 * @param mosContext
	 *            {@link ManagedObjectSourceContext}.
	 * @param instance
	 *            Instance of the {@link HttpServerSocketManagedObjectSource}
	 *            using the {@link SocketManager}.
	 * @return {@link SocketManager}.
	 */
	private static synchronized SocketManager getSocketManager(HttpServerSocketManagedObjectSource instance)
			throws IOException {

		// Lazy create the singleton socket manager
		if (singletonSocketManager == null) {

			// Obtain configuration of socket manager (with reasonable defaults)
			int numberOfSocketListeners = getSystemProperty(SYSTEM_PROPERTY_SOCKET_LISTENER_COUNT,
					Runtime.getRuntime().availableProcessors());
			int bufferSize = getSystemProperty(SYSTEM_PROPERTY_BUFFER_SIZE, 65536);
			int maxThreadLocalPoolSize = getSystemProperty(SYSTEM_PROPERTY_THREADLOCAL_BUFFER_POOL_MAX_SIZE,
					Integer.MAX_VALUE);
			int maxCorePoolSize = getSystemProperty(SYSTEM_PROPERTY_CORE_BUFFER_POOL_MAX_SIZE, Integer.MAX_VALUE);

			// Create the stream buffer pool
			StreamBufferPool<ByteBuffer> bufferPool = new ThreadLocalStreamBufferPool(
					() -> ByteBuffer.allocateDirect(bufferSize), maxThreadLocalPoolSize, maxCorePoolSize);

			// Create the singleton socket manager
			singletonSocketManager = new SocketManager(numberOfSocketListeners, bufferPool, bufferSize);

			// Create the executor service
			executor = Executors.newCachedThreadPool();

			// Run the socket listeners
			Runnable[] runnables = singletonSocketManager.getRunnables();
			for (int i = 0; i < runnables.length; i++) {
				executor.execute(runnables[i]);
			}
		}

		// Register the instance for use of the connection manager
		registeredServerSocketManagedObjectSources.add(instance);

		// Return the singleton connection manager
		return singletonSocketManager;
	}

	/**
	 * <p>
	 * Closes the possible open {@link SocketManager}.
	 * <p>
	 * Made public so that tests may use to close.
	 * 
	 * @throws IOException
	 *             If fails to close the {@link Old_SocketManager}.
	 */
	public static synchronized void closeSocketManager() throws IOException {

		// Clear all registered server sockets
		registeredServerSocketManagedObjectSources.clear();

		// Determine if active socket manager
		if (singletonSocketManager != null) {
			// Shutdown the socket manager
			singletonSocketManager.shutdown();
			executor.shutdown();
		}

		// Close (release) the socket manager to create again
		singletonSocketManager = null;
		executor = null;
	}

	/**
	 * <p>
	 * Releases the {@link HttpServerSocketManagedObjectSource} instance from
	 * the {@link SocketManager}.
	 * <p>
	 * Once all {@link HttpServerSocketManagedObjectSource} instances are
	 * release, the {@link SocketManager} itself is closed.
	 * 
	 * @param instance
	 *            {@link HttpServerSocketManagedObjectSource}.
	 * @throws IOException
	 *             If fails to close the {@link SocketManager}.
	 */
	private static synchronized void releaseFromSocketManager(HttpServerSocketManagedObjectSource instance)
			throws IOException {

		// Unregister from the connection manager
		registeredServerSocketManagedObjectSources.remove(instance);

		// Close connection manager if no further server sockets
		if (registeredServerSocketManagedObjectSources.size() == 0) {
			closeSocketManager();
		}
	}

	/**
	 * Port.
	 */
	private int port;

	/**
	 * {@link SSLContext}.
	 */
	private SSLContext sslContext;

	/**
	 * {@link ServerSocketDecorator}.
	 */
	private ServerSocketDecorator serverSocketDecorator;

	/**
	 * {@link AcceptedSocketDecorator}.
	 */
	private AcceptedSocketDecorator acceptedSocketDecorator;

	/**
	 * {@link ManagedObjectSourceHttpServicerFactory}.
	 */
	private ManagedObjectSourceHttpServicerFactory servicerFactory;

	/**
	 * Enables overriding to configure a {@link ServerSocketDecorator}.
	 * 
	 * @param context
	 *            {@link MetaDataContext}.
	 * @return {@link ServerSocketDecorator}.
	 */
	protected ServerSocketDecorator getServerSocketDecorator(MetaDataContext<None, Indexed> context) {
		return null;
	}

	/**
	 * Enables overriding to configure the {@link AcceptedSocketDecorator}.
	 * 
	 * @param context
	 *            {@link MetaDataContext}.
	 * @return {@link AcceptedSocketDecorator}.
	 */
	protected AcceptedSocketDecorator getAcceptedSocketDecorator(MetaDataContext<None, Indexed> context) {
		return null;
	}

	/*
	 * =================== ManagedObjectSource ==================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
		context.addProperty(PROPERTY_PORT, "Port");
	}

	@Override
	protected void loadMetaData(MetaDataContext<None, Indexed> context) throws Exception {

		// Configure meta-data
		context.setObjectClass(ServerHttpConnection.class);
		context.setManagedObjectClass(ProcessAwareServerHttpConnectionManagedObject.class);

		// Obtain the managed object source context
		ManagedObjectSourceContext<Indexed> mosContext = context.getManagedObjectSourceContext();

		// Load the configuration
		this.port = Integer.parseInt(mosContext.getProperty(PROPERTY_PORT));
		boolean isSecure = Boolean.parseBoolean(mosContext.getProperty(PROPERTY_SECURE, String.valueOf(false)));
		int maxHeaderCount = Integer.parseInt(mosContext.getProperty(PROPERTY_MAX_HEADER_COUNT, String.valueOf(50)));
		int maxTextLength = Integer.parseInt(mosContext.getProperty(PROPERTY_MAX_TEXT_LENGTH, String.valueOf(2048)));
		long maxEntityLength = Long
				.parseLong(mosContext.getProperty(PROPERTY_MAX_ENTITY_LENGTH, String.valueOf(20 * 1024 * 1024)));
		int serviceBufferSize = Integer
				.parseInt(mosContext.getProperty(PROPERTY_SERVICE_BUFFER_SIZE, String.valueOf(4096)));

		// Obtain the decorators
		this.serverSocketDecorator = this.getServerSocketDecorator(context);
		this.acceptedSocketDecorator = this.getAcceptedSocketDecorator(context);

		// Create the HTTP servicer factory
		ThreadLocalStreamBufferPool serviceBufferPool = new ThreadLocalStreamBufferPool(
				() -> ByteBuffer.allocate(serviceBufferSize), 3, 1000);
		this.servicerFactory = new ManagedObjectSourceHttpServicerFactory(isSecure,
				new HttpRequestParserMetaData(maxHeaderCount, maxTextLength, maxEntityLength), serviceBufferPool);

		// Obtain the SSL context
		if (isSecure) {
			String sslContextSourceClassName = mosContext.getProperty(PROPERTY_SSL_CONTEXT_SOURCE, null);
			if (sslContextSourceClassName != null) {
				sslContextSourceClassName = OfficeFloorDefaultSslContextSource.class.getName();
			}
			switch (sslContextSourceClassName) {
			case SSL_REVERSE_PROXIED:
				// Let be secure, without SSL
				break;
			default:
				// Create the SSL context
				Class<?> sslContextSourceClass = mosContext.loadClass(sslContextSourceClassName);
				SslContextSource sslContextSource = (SslContextSource) sslContextSourceClass.newInstance();
				this.sslContext = sslContextSource.createSslContext(mosContext);
				break;
			}
		}

		// Add recycle function (to capture clean up failures)
		mosContext.getRecycleFunction(new ManagedFunctionFactory<Indexed, None>() {
			@Override
			public ManagedFunction<Indexed, None> createManagedFunction() throws Throwable {
				return HttpServerSocketManagedObjectSource.this;
			}
		});
	}

	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void start(ManagedObjectExecuteContext<Indexed> context) throws Exception {

		// Obtain the socket manager
		SocketManager socketManager = getSocketManager(this);

		// Create the SSL servicer factory
		SocketServicerFactory socketServicerFactory = this.servicerFactory;
		RequestServicerFactory requestServicerFactory = this.servicerFactory;
		if (this.sslContext != null) {
			SslSocketServicerFactory<?> sslServicerFactory = new SslSocketServicerFactory<>(this.sslContext,
					this.servicerFactory, this.servicerFactory, socketManager.getStreamBufferPool(), executor);
			socketServicerFactory = sslServicerFactory;
			requestServicerFactory = sslServicerFactory;
		}

		// Bind server socket for this managed object source
		socketManager.bindServerSocket(this.port, this.serverSocketDecorator, this.acceptedSocketDecorator,
				socketServicerFactory, requestServicerFactory);
	}

	@Override
	protected ManagedObject getManagedObject() throws Throwable {
		// Should never be directly used by a function
		throw new IllegalStateException("Can not source managed object from a " + this.getClass().getSimpleName());
	}

	@Override
	public void stop() {

		// Release socket manager (closes socket listeners when appropriate)
		try {
			releaseFromSocketManager(this);

		} catch (IOException ex) {
			// Shutting down so just log issue
			if (LOGGER.isLoggable(Level.INFO)) {
				LOGGER.log(Level.INFO, "Failed to release " + SocketManager.class.getSimpleName(), ex);
			}
		}
	}

	/*
	 * ==================== ManagedFunction =====================
	 */

	@Override
	public Object execute(ManagedFunctionContext<Indexed, None> context) throws Throwable {

		// Obtain the parameter
		RecycleManagedObjectParameter<ProcessAwareServerHttpConnectionManagedObject<ByteBuffer>> parameter = RecycleManagedObjectParameter
				.getRecycleManagedObjectParameter(context);

		// Obtain the managed object
		ProcessAwareServerHttpConnectionManagedObject<ByteBuffer> managedObject = parameter.getManagedObject();

		// Load potential clean up escalations
		managedObject.setCleanupEscalations(parameter.getCleanupEscalations());

		// Nothing further
		return null;
	}

	/**
	 * {@link AbstractHttpServicerFactory}.
	 */
	private class ManagedObjectSourceHttpServicerFactory extends AbstractHttpServicerFactory {

		/**
		 * Instantiate.
		 * 
		 * @param isSecure
		 *            Indicates if a secure {@link ServerHttpConnection}.
		 * @param metaData
		 *            {@link HttpRequestParserMetaData}.
		 * @param serviceBufferPool
		 *            Service {@link StreamBufferPool}.
		 */
		public ManagedObjectSourceHttpServicerFactory(boolean isSecure, HttpRequestParserMetaData metaData,
				StreamBufferPool<ByteBuffer> serviceBufferPool) {
			super(isSecure, metaData, true, serviceBufferPool);
		}

		@Override
		protected void service(ProcessAwareServerHttpConnectionManagedObject<ByteBuffer> connection)
				throws IOException, HttpException {

			// Service request
		}
	}

}