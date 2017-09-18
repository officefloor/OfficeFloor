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
import java.io.PrintStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.sql.Connection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.net.ssl.SSLContext;

import net.officefloor.compile.impl.util.CompileUtil;
import net.officefloor.compile.properties.Property;
import net.officefloor.compile.properties.PropertyList;
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
import net.officefloor.server.http.impl.ProcessAwareServerHttpConnectionManagedObject;
import net.officefloor.server.http.parse.HttpRequestParser.HttpRequestParserMetaData;
import net.officefloor.server.ssl.OfficeFloorDefaultSslContextSource;
import net.officefloor.server.ssl.SslContextSource;
import net.officefloor.server.ssl.SslSocketServicerFactory;
import net.officefloor.server.stream.StreamBuffer;
import net.officefloor.server.stream.StreamBufferPool;
import net.officefloor.server.stream.impl.ThreadLocalStreamBufferPool;

/**
 * {@link ManagedObjectSource} for a {@link ServerHttpConnection}.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpServerSocketManagedObjectSource
		extends AbstractManagedObjectSource<None, HttpServerSocketManagedObjectSource.Flows>
		implements ManagedFunction<Indexed, None> {

	/**
	 * Flows for this {@link HttpServerSocketManagedObjectSource}.
	 */
	public static enum Flows {
		HANDLE_REQUEST
	}

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
	public static final String SYSTEM_PROPERTY_STREAM_BUFFER_SIZE = "officefloor.socket.stream.buffer.size";

	/**
	 * Name of {@link System} property to obtain the receive buffer size of the
	 * {@link Socket}.
	 */
	public static final String SYSTEM_PROPERTY_RECEIVE_BUFFER_SIZE = "officefloor.socket.receive.buffer.size";

	/**
	 * Name of {@link System} property to obtain the maximum number of reads for
	 * a {@link SocketChannel} per select.
	 */
	public static final String SYSTEM_PROPERTY_MAX_READS_ON_SELECT = "officefloor.socket.max.reads.on.select";

	/**
	 * Name of {@link System} property to obtain the send buffer size of the
	 * {@link Socket}.
	 */
	public static final String SYSTEM_PROPERTY_SEND_BUFFER_SIZE = "officefloor.socket.send.buffer.size";

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
	 * Name of {@link Property} for the maximum number of {@link StreamBuffer}
	 * instances cached in the {@link Thread}.
	 */
	public static final String PROPERTY_SERVICE_MAX_THREAD_POOL_SIZE = "service.buffer.max.thread.pool.size";

	/**
	 * Name of {@link Property} for the maximum number of {@link StreamBuffer}
	 * instances cached in a core pool.
	 */
	private static final String PROPERTY_SERVICE_MAX_CORE_POOL_SIZE = "service.buffer.max.core.pool.size";

	/**
	 * Singleton {@link SocketManager} for all {@link Connection} instances.
	 */
	private static SocketManager singletonSocketManager;

	/**
	 * {@link ExecutorService} for executing {@link SocketManager}.
	 */
	private static ExecutorService executorService;

	/**
	 * {@link ServiceExecutor}.
	 */
	private static ServiceExecutor executor;

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
	 * Creates the {@link SocketManager} configured from {@link System}
	 * properties.
	 * 
	 * @return {@link SocketManager} configured from {@link System} properties.
	 * @throws IOException
	 *             If fails to create the {@link SocketManager}.
	 */
	public static SocketManager createSocketManager() throws IOException {

		/*
		 * Obtain configuration of socket manager.
		 * 
		 * Defaults are based on following:
		 * 
		 * - thread per CPU (leaving non-used CPU utilisation for additional
		 * threads servicing requests)
		 * 
		 * - 8192 to fill a Jumbo Ethernet frame (i.e. 9000)
		 * 
		 * - 8 * 8192 = 65536 to fill a TCP packet, with additional 4 packet TCP
		 * buffer.
		 */
		int numberOfSocketListeners = getSystemProperty(SYSTEM_PROPERTY_SOCKET_LISTENER_COUNT,
				Runtime.getRuntime().availableProcessors());
		int streamBufferSize = getSystemProperty(SYSTEM_PROPERTY_STREAM_BUFFER_SIZE, 8192);
		int maxReadsOnSelect = getSystemProperty(SYSTEM_PROPERTY_MAX_READS_ON_SELECT, 8 * 4);
		int receiveBufferSize = getSystemProperty(SYSTEM_PROPERTY_RECEIVE_BUFFER_SIZE,
				streamBufferSize * maxReadsOnSelect);
		int sendBufferSize = getSystemProperty(SYSTEM_PROPERTY_SEND_BUFFER_SIZE, receiveBufferSize);
		int maxThreadLocalPoolSize = getSystemProperty(SYSTEM_PROPERTY_THREADLOCAL_BUFFER_POOL_MAX_SIZE,
				Integer.MAX_VALUE);
		int maxCorePoolSize = getSystemProperty(SYSTEM_PROPERTY_CORE_BUFFER_POOL_MAX_SIZE, Integer.MAX_VALUE);

		// Create the stream buffer pool
		StreamBufferPool<ByteBuffer> bufferPool = new ThreadLocalStreamBufferPool(
				() -> ByteBuffer.allocateDirect(streamBufferSize), maxThreadLocalPoolSize, maxCorePoolSize);

		// Create and return the socket manager
		return new SocketManager(numberOfSocketListeners, receiveBufferSize, maxReadsOnSelect, bufferPool,
				sendBufferSize);
	}

	/**
	 * Obtains the {@link SocketManager} for use by
	 * {@link HttpServerSocketManagedObjectSource} instances.
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

			// Create the singleton socket manager
			singletonSocketManager = createSocketManager();

			// Create the executor service
			executorService = Executors.newCachedThreadPool();
			executor = new ServiceExecutor();

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
	 * Keeps track of active {@link Thread} instances.
	 */
	private static class ServiceExecutor implements Executor {

		/**
		 * Active {@link Thread}.
		 */
		private List<Thread> activeThreads = new LinkedList<>();

		/**
		 * Indicates whether to dump the active {@link Thread} instances.
		 */
		private boolean isDump = true;

		/**
		 * Obtains the active {@link Thread} instances and their current
		 * {@link StackTraceElement} instances.
		 */
		private Map<Thread, StackTraceElement[]> getActiveThreads() {
			synchronized (this.activeThreads) {

				// Obtain the stack traces of active threads
				Map<Thread, StackTraceElement[]> threads = new HashMap<>();
				for (Thread activeThread : this.activeThreads) {
					threads.put(activeThread, activeThread.getStackTrace());
				}

				// Return the active threads
				return threads;
			}
		}

		/**
		 * Dumps the active {@link Thread} instances after the specified time.
		 * 
		 * @param timeInMilliseconds
		 *            Time in milliseconds to dump the active {@link Thread}
		 *            instances.
		 */
		private void dumpActiveThreadsAfter(long timeInMilliseconds) {
			long startTime = System.currentTimeMillis();
			new Thread(() -> {
				synchronized (this.activeThreads) {

					// Determine if should dump
					if (!this.isDump) {
						return;
					}

					// Wait the specified period of time
					try {
						this.activeThreads.wait(timeInMilliseconds);
					} catch (InterruptedException ex) {
						// ignore, and carry on
					}

					// Determine if should dump
					if (!this.isDump) {
						return;
					}

					// Obtain the active threads
					Map<Thread, StackTraceElement[]> activeThreads = this.getActiveThreads();

					// Max stack trace depth
					final int maxDepth = 15;

					// Dump the active threads
					PrintStream writer = System.err;
					writer.println(SocketManager.class.getSimpleName() + " waited "
							+ (System.currentTimeMillis() - startTime) + " milliseconds for shutdown");
					writer.println();
					writer.println("Waiting on active threads:");
					for (Thread thread : activeThreads.keySet()) {
						StackTraceElement[] stackTrace = activeThreads.get(thread);
						writer.println();
						writer.println("Thread: " + thread.getName());
						for (int i = 0; i < Math.min(maxDepth, stackTrace.length); i++) {
							StackTraceElement element = stackTrace[i];
							writer.print("\t");
							writer.println(element.toString());
						}
						if (stackTrace.length > maxDepth) {
							writer.println("\t... (" + (stackTrace.length - maxDepth) + " more)");
						}
						writer.println();
					}
				}
			}).start();
		}

		/**
		 * Stops the dump.
		 */
		private void stopDump() {
			synchronized (this.activeThreads) {

				// Flag not to dump
				this.isDump = false;

				// Notify, so don't wait
				this.activeThreads.notify();
			}
		}

		/**
		 * ================= Executor =======================
		 */

		@Override
		public void execute(Runnable command) {
			executorService.execute(() -> {
				Thread currentThread = Thread.currentThread();
				try {
					// Register this thread
					synchronized (this.activeThreads) {
						this.activeThreads.add(currentThread);
					}

					// Undertake the functionality
					command.run();

				} finally {
					// Unregister this thread
					synchronized (this.activeThreads) {
						this.activeThreads.remove(currentThread);
					}
				}
			});
		}
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
	private static synchronized void closeSocketManager() throws IOException {

		// Clear all registered server sockets
		registeredServerSocketManagedObjectSources.clear();

		// Determine if active socket manager
		if (singletonSocketManager != null) {
			// Shutdown the socket manager
			singletonSocketManager.shutdown();

			// Wait on shut down
			executorService.shutdown();
			try {
				int waitTime = 10; // 10 seconds
				executor.dumpActiveThreadsAfter((waitTime - 1) * 1000);
				executorService.awaitTermination(10, TimeUnit.SECONDS);
			} catch (InterruptedException ex) {
				throw new IOException("Interrupted waiting on Socket Manager shut down", ex);
			} finally {
				executor.stopDump();
			}
		}

		// Close (release) the socket manager to create again
		singletonSocketManager = null;
		executorService = null;
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
	private int port = -1;

	/**
	 * {@link HttpRequestParserMetaData}.
	 */
	private HttpRequestParserMetaData httpRequestParserMetaData;

	/**
	 * {@link StreamBuffer} size of pooled {@link ByteBuffer} for servicing.
	 */
	private int serviceBufferSize;

	/**
	 * Maximum pool size of {@link StreamBuffer} instances cached on the
	 * {@link Thread}.
	 */
	private int serviceBufferMaxThreadPoolSize;

	/**
	 * Maximum pool size of {@link StreamBuffer} instances within the core pool.
	 */
	private int serviceBufferMaxCorePoolSize;

	/**
	 * Indicates if secure HTTP connection.
	 */
	private boolean isSecure;

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
	 * Default constructor to configure from {@link PropertyList}.
	 */
	public HttpServerSocketManagedObjectSource() {
	}

	/**
	 * Instantiate for non-secure servicing.
	 * 
	 * @param port
	 *            Port.
	 */
	public HttpServerSocketManagedObjectSource(int port) {
		this.port = port;
		this.isSecure = false;
		this.sslContext = null;
	}

	/**
	 * Instantiate for secure servicing.
	 * 
	 * @param port
	 *            Port.
	 * @param sslContext
	 *            {@link SSLContext}. May be <code>null</code> if behind reverse
	 *            proxy handling secure communication.
	 */
	public HttpServerSocketManagedObjectSource(int port, SSLContext sslContext) {
		this.port = port;
		this.isSecure = true;
		this.sslContext = sslContext;
	}

	/**
	 * Enables overriding to configure a {@link ServerSocketDecorator}.
	 * 
	 * @param context
	 *            {@link MetaDataContext}.
	 * @return {@link ServerSocketDecorator}.
	 */
	protected ServerSocketDecorator getServerSocketDecorator(MetaDataContext<None, Flows> context) {
		return null;
	}

	/**
	 * Enables overriding to configure the {@link AcceptedSocketDecorator}.
	 * 
	 * @param context
	 *            {@link MetaDataContext}.
	 * @return {@link AcceptedSocketDecorator}.
	 */
	protected AcceptedSocketDecorator getAcceptedSocketDecorator(MetaDataContext<None, Flows> context) {
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
	protected void loadMetaData(MetaDataContext<None, Flows> context) throws Exception {

		// Configure meta-data
		context.setObjectClass(ServerHttpConnection.class);
		context.setManagedObjectClass(ProcessAwareServerHttpConnectionManagedObject.class);

		// Create the flow meta-data
		context.addFlow(Flows.HANDLE_REQUEST, null);

		// Obtain the managed object source context
		ManagedObjectSourceContext<Flows> mosContext = context.getManagedObjectSourceContext();

		// Load configuration
		int maxHeaderCount = Integer.parseInt(mosContext.getProperty(PROPERTY_MAX_HEADER_COUNT, String.valueOf(50)));
		int maxTextLength = Integer.parseInt(mosContext.getProperty(PROPERTY_MAX_TEXT_LENGTH, String.valueOf(2048)));
		long maxEntityLength = Long
				.parseLong(mosContext.getProperty(PROPERTY_MAX_ENTITY_LENGTH, String.valueOf(1 * 1024 * 1024)));
		this.serviceBufferSize = Integer
				.parseInt(mosContext.getProperty(PROPERTY_SERVICE_BUFFER_SIZE, String.valueOf(256)));
		this.serviceBufferMaxThreadPoolSize = Integer
				.parseInt(mosContext.getProperty(PROPERTY_SERVICE_MAX_THREAD_POOL_SIZE, String.valueOf(10000)));
		this.serviceBufferMaxCorePoolSize = Integer
				.parseInt(mosContext.getProperty(PROPERTY_SERVICE_MAX_CORE_POOL_SIZE, String.valueOf(10000000)));

		// Create the request parser meta-data
		this.httpRequestParserMetaData = new HttpRequestParserMetaData(maxHeaderCount, maxTextLength, maxEntityLength);

		// Obtain the decorators
		this.serverSocketDecorator = this.getServerSocketDecorator(context);
		this.acceptedSocketDecorator = this.getAcceptedSocketDecorator(context);

		// Determine if need to configure from properties
		if (this.port == -1) {

			// Default constructor, so load the configuration
			this.port = Integer.parseInt(mosContext.getProperty(PROPERTY_PORT));
			this.isSecure = Boolean.parseBoolean(mosContext.getProperty(PROPERTY_SECURE, String.valueOf(false)));

			// Obtain the SSL context
			if (this.isSecure) {
				String sslContextSourceClassName = mosContext.getProperty(PROPERTY_SSL_CONTEXT_SOURCE, null);
				if (sslContextSourceClassName == null) {
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
	public void start(ManagedObjectExecuteContext<Flows> context) throws Exception {

		// Obtain the socket manager
		SocketManager socketManager = getSocketManager(this);

		// Create the HTTP servicer factory
		ThreadLocalStreamBufferPool serviceBufferPool = new ThreadLocalStreamBufferPool(
				() -> ByteBuffer.allocateDirect(this.serviceBufferSize), this.serviceBufferMaxThreadPoolSize,
				this.serviceBufferMaxCorePoolSize);
		ManagedObjectSourceHttpServicerFactory servicerFactory = new ManagedObjectSourceHttpServicerFactory(context,
				this.isSecure, this.httpRequestParserMetaData, serviceBufferPool);

		// Create the SSL servicer factory
		SocketServicerFactory socketServicerFactory = servicerFactory;
		RequestServicerFactory requestServicerFactory = servicerFactory;
		if (this.sslContext != null) {
			SslSocketServicerFactory<?> sslServicerFactory = new SslSocketServicerFactory<>(this.sslContext,
					servicerFactory, servicerFactory, socketManager.getStreamBufferPool(), executor);
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
		 * {@link ManagedObjectExecuteContext} to service the
		 * {@link ServerHttpConnection}.
		 */
		private final ManagedObjectExecuteContext<Flows> context;

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
		public ManagedObjectSourceHttpServicerFactory(ManagedObjectExecuteContext<Flows> context, boolean isSecure,
				HttpRequestParserMetaData metaData, StreamBufferPool<ByteBuffer> serviceBufferPool) {
			super(isSecure, metaData, serviceBufferPool);
			this.context = context;
		}

		@Override
		protected void service(ProcessAwareServerHttpConnectionManagedObject<ByteBuffer> connection)
				throws IOException, HttpException {

			// Service request
			this.context.invokeProcess(Flows.HANDLE_REQUEST, null, connection, 0, connection.getServiceFlowCallback());
		}
	}

}