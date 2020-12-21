/*-
 * #%L
 * Default OfficeFloor HTTP Server
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
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
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.net.ssl.SSLContext;

import net.officefloor.compile.impl.util.CompileUtil;
import net.officefloor.compile.properties.Property;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.escalate.Escalation;
import net.officefloor.frame.api.executive.ExecutionStrategy;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.function.ManagedFunctionContext;
import net.officefloor.frame.api.manage.ProcessManager;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.executor.ManagedObjectExecutorFactory;
import net.officefloor.frame.api.managedobject.recycle.RecycleManagedObjectParameter;
import net.officefloor.frame.api.managedobject.source.ManagedObjectExecuteContext;
import net.officefloor.frame.api.managedobject.source.ManagedObjectService;
import net.officefloor.frame.api.managedobject.source.ManagedObjectServiceContext;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSourceContext;
import net.officefloor.frame.api.managedobject.source.impl.AbstractManagedObjectSource;
import net.officefloor.frame.api.source.PrivateSource;
import net.officefloor.frame.api.team.Team;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.server.AcceptedSocketDecorator;
import net.officefloor.server.RequestServicerFactory;
import net.officefloor.server.ServerSocketDecorator;
import net.officefloor.server.SocketManager;
import net.officefloor.server.SocketServicer;
import net.officefloor.server.SocketServicerFactory;
import net.officefloor.server.http.impl.HttpServerLocationImpl;
import net.officefloor.server.http.impl.ProcessAwareServerHttpConnectionManagedObject;
import net.officefloor.server.http.parse.HttpRequestParser.HttpRequestParserMetaData;
import net.officefloor.server.ssl.SslSocketServicerFactory;
import net.officefloor.server.stream.StreamBuffer;
import net.officefloor.server.stream.StreamBufferPool;
import net.officefloor.server.stream.impl.ThreadLocalStreamBufferPool;

/**
 * {@link ManagedObjectSource} for a {@link ServerHttpConnection}.
 * 
 * @author Daniel Sagenschneider
 */
@PrivateSource
public class HttpServerSocketManagedObjectSource extends AbstractManagedObjectSource<None, Indexed>
		implements ManagedFunction<Indexed, None> {

	/**
	 * Name of {@link System} property to specify the number of
	 * {@link SocketServicer} instances. If not specified, will default to
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
	 * Name of {@link System} property to obtain the maximum number of reads for a
	 * {@link SocketChannel} per select.
	 */
	public static final String SYSTEM_PROPERTY_MAX_READS_ON_SELECT = "officefloor.socket.max.reads.on.select";

	/**
	 * Name of {@link System} property to obtain the maximum number of active
	 * requests on the socket.
	 */
	public static final String SYSTEM_PROPERTY_MAX_ACTIVE_SOCKET_REQUESTS = "officefloor.socket.max.active.socket.requests";

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
	 * Name of {@link System} property to obtain the memory threshold percentage.
	 */
	public static final String SYSTEM_PROPERTY_MEMORY_THRESHOLD_PERCENTAGE = "officefloor.socket.memory.threshold.percentage";

	/**
	 * Name of {@link Property} indicating if secure.
	 */
	public static final String PROPERTY_SECURE = "secure";

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
	 * Name of the {@link Flow} to handle the request.
	 */
	public static final String HANDLE_REQUEST_FLOW_NAME = "HANDLE_REQUEST";

	/**
	 * Name of the {@link Team} to execute the SSL tasks.
	 */
	public static final String SSL_TEAM_NAME = "SSL_TEAM";

	/**
	 * Singleton {@link SocketManager} for all {@link Connection} instances.
	 */
	private static SocketManager singletonSocketManager;

	/**
	 * {@link ServiceExecutor} for executing {@link SocketManager}.
	 */
	private static ServiceExecutor serviceExecutor;

	/**
	 * Registered {@link HttpServerSocketManagedObjectSource} instances.
	 */
	private static Set<HttpServerSocketManagedObjectSource> registeredServerSocketManagedObjectSources = new HashSet<HttpServerSocketManagedObjectSource>();

	/**
	 * Obtains the {@link System} integer property value.
	 * 
	 * @param name         Name of the {@link System} property.
	 * @param defaultValue Default value.
	 * @return {@link System} property value.
	 */
	private static int getIntegerSystemProperty(String name, int defaultValue) {
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
	 * Obtains the {@link System} float property value.
	 * 
	 * @param name         Name of the {@link System} property.
	 * @param defaultValue Default value.
	 * @return {@link System} property value.
	 */
	private static float getFloatSystemProperty(String name, float defaultValue) {
		String text = System.getProperty(name, null);
		if (CompileUtil.isBlank(text)) {
			// No value configured, so use default
			return defaultValue;

		} else {
			// Attempt to parse the configured value
			try {
				return Float.parseFloat(text);
			} catch (NumberFormatException ex) {
				// Invalid value
				throw new NumberFormatException(
						"Invalid system configured value for " + name + " '" + text + "'.  Must be a float.");
			}
		}
	}

	/**
	 * Creates the {@link SocketManager} configured from {@link System} properties.
	 * 
	 * @param executionStrategy {@link ThreadFactory} instances for the
	 *                          {@link ExecutionStrategy} for the
	 *                          {@link SocketManager}.
	 * @return {@link SocketManager} configured from {@link System} properties.
	 * @throws IOException If fails to create the {@link SocketManager}.
	 */
	public static SocketManager createSocketManager(ThreadFactory[] executionStrategy) throws IOException {

		/*
		 * Obtain configuration of socket manager.
		 * 
		 * Defaults are based on following:
		 * 
		 * - thread per CPU (leaving non-used CPU utilisation for additional threads
		 * servicing requests)
		 * 
		 * - 8192 to fill a Jumbo Ethernet frame (i.e. 9000)
		 * 
		 * - 8 * 8192 = 65536 to fill a TCP packet, with 4 TCP packet buffer.
		 */
		int numberOfSocketListeners = getIntegerSystemProperty(SYSTEM_PROPERTY_SOCKET_LISTENER_COUNT,
				executionStrategy.length);
		int streamBufferSize = getIntegerSystemProperty(SYSTEM_PROPERTY_STREAM_BUFFER_SIZE, 8192);
		int maxReadsOnSelect = getIntegerSystemProperty(SYSTEM_PROPERTY_MAX_READS_ON_SELECT, 8 * 4);
		int maxActiveSocketRequests = getIntegerSystemProperty(SYSTEM_PROPERTY_MAX_ACTIVE_SOCKET_REQUESTS, 10);
		int receiveBufferSize = getIntegerSystemProperty(SYSTEM_PROPERTY_RECEIVE_BUFFER_SIZE,
				streamBufferSize * maxReadsOnSelect);
		int sendBufferSize = getIntegerSystemProperty(SYSTEM_PROPERTY_SEND_BUFFER_SIZE, receiveBufferSize);
		int maxThreadLocalPoolSize = getIntegerSystemProperty(SYSTEM_PROPERTY_THREADLOCAL_BUFFER_POOL_MAX_SIZE,
				Integer.MAX_VALUE);
		int maxCorePoolSize = getIntegerSystemProperty(SYSTEM_PROPERTY_CORE_BUFFER_POOL_MAX_SIZE, Integer.MAX_VALUE);
		float memoryThresholdPercentage = getFloatSystemProperty(SYSTEM_PROPERTY_MEMORY_THRESHOLD_PERCENTAGE, 0.4f);

		// Create the stream buffer pool
		StreamBufferPool<ByteBuffer> bufferPool = new ThreadLocalStreamBufferPool(
				() -> ByteBuffer.allocateDirect(streamBufferSize), maxThreadLocalPoolSize, maxCorePoolSize);

		// Determine the maximum direct memory
		long maxDirectMemory = SocketManager.getMaxDirectMemory();
		long socketListenerMemoryThreshold = (long) ((maxDirectMemory * memoryThresholdPercentage)
				/ numberOfSocketListeners);

		// Create and return the socket manager
		return new SocketManager(numberOfSocketListeners, receiveBufferSize, maxReadsOnSelect, maxActiveSocketRequests,
				bufferPool, sendBufferSize, socketListenerMemoryThreshold);
	}

	/**
	 * Indicates if there is an active {@link SocketManager} for
	 * {@link HttpServerSocketManagedObjectSource} instances.
	 * 
	 * @return <code>true</code> if active {@link SocketManager}.
	 */
	public static synchronized boolean isSocketManagerActive() {
		return (singletonSocketManager != null);
	}

	/**
	 * Obtains the {@link SocketManager} for use by
	 * {@link HttpServerSocketManagedObjectSource} instances.
	 * 
	 * @param mosContext        {@link ManagedObjectSourceContext}.
	 * @param instance          Instance of the
	 *                          {@link HttpServerSocketManagedObjectSource} using
	 *                          the {@link SocketManager}.
	 * @param executionStrategy {@link ThreadFactory} instances for the
	 *                          {@link ExecutionStrategy} for the
	 *                          {@link SocketManager}.
	 * @return {@link SocketManager}.
	 */
	private static synchronized SocketManager getSocketManager(HttpServerSocketManagedObjectSource instance,
			ThreadFactory[] executionStrategy) throws IOException {

		// Lazy create the singleton socket manager
		if (singletonSocketManager == null) {

			// Create the singleton socket manager
			singletonSocketManager = createSocketManager(executionStrategy);

			// Create the service executor
			serviceExecutor = new ServiceExecutor();

			// Run the socket listeners
			Runnable[] runnables = singletonSocketManager.getRunnables();
			for (int i = 0; i < runnables.length; i++) {
				serviceExecutor.execute(runnables[i], executionStrategy[i]);
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
	private static class ServiceExecutor {

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
		 * @param timeInMilliseconds Time in milliseconds to dump the active
		 *                           {@link Thread} instances.
		 */
		private void dumpActiveThreadsAfter(long timeInMilliseconds) {
			long startTime = System.currentTimeMillis();
			long endTime = startTime + timeInMilliseconds;
			new Thread(() -> {
				synchronized (this.activeThreads) {

					// Wait the specified period of time
					while ((this.isDump) && (System.currentTimeMillis() < endTime)) {
						try {
							this.activeThreads.wait(100);
						} catch (InterruptedException ex) {
							return;
						}
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
				this.activeThreads.notifyAll();
			}
		}

		/**
		 * Awaits the termination of the {@link Thread} instances.
		 * 
		 * @param waitTimeInMilliseconds Wait time in milliseconds.
		 * @throws InterruptedException If fails to wait on termination.
		 * @throws TimeoutException     If termination taken too long.
		 */
		public void awaitTermination(int waitTimeInMilliseconds) throws InterruptedException, TimeoutException {

			// Capture the start time for time out
			long endTime = System.currentTimeMillis() + waitTimeInMilliseconds;

			// Loop until times out or no active threads
			synchronized (this.activeThreads) {
				while (this.activeThreads.size() > 0) {

					// Determine if timed out
					if (System.currentTimeMillis() > endTime) {
						throw new TimeoutException("Waited more than " + waitTimeInMilliseconds
								+ " milliseconds for HTTP socket servicing to terminate");
					}

					// Wait some time for termination
					this.activeThreads.wait(10);
				}
			}
		}

		/**
		 * Executes the {@link Runnable}.
		 * 
		 * @param command       {@link Runnable} command to be executed.
		 * @param threadFactory {@link ThreadFactory} to create the {@link Thread} for
		 *                      the {@link Runnable}.
		 */
		public void execute(Runnable command, ThreadFactory threadFactory) {
			threadFactory.newThread(() -> {
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

						// Notify immediately, to allow shutdown
						this.activeThreads.notifyAll();
					}
				}
			}).start();
		}

	}

	/**
	 * <p>
	 * Closes the possible open {@link SocketManager}.
	 * <p>
	 * Made public so that tests may use to close.
	 * 
	 * @throws IOException      If fails to close the {@link SocketManager}.
	 * @throws TimeoutException If taking too long to close the
	 *                          {@link SocketManager}.
	 */
	private static synchronized void closeSocketManager() throws TimeoutException, IOException {
		try {
			// Clear all registered server sockets
			registeredServerSocketManagedObjectSources.clear();

			// Determine if active socket manager
			if (singletonSocketManager != null) {
				// Shutdown the socket manager
				singletonSocketManager.shutdown();

				// Wait on shut down
				try {
					int waitTime = 10; // 10 seconds
					serviceExecutor.dumpActiveThreadsAfter((waitTime - 1) * 1000);
					serviceExecutor.awaitTermination(waitTime * 1000);
				} catch (InterruptedException ex) {
					throw new IOException("Interrupted waiting on Socket Manager shut down", ex);
				} finally {
					serviceExecutor.stopDump();
				}
			}

		} finally {
			// Close (release) the socket manager to create again
			singletonSocketManager = null;
			serviceExecutor = null;
		}
	}

	/**
	 * <p>
	 * Releases the {@link HttpServerSocketManagedObjectSource} instance from the
	 * {@link SocketManager}.
	 * <p>
	 * Once all {@link HttpServerSocketManagedObjectSource} instances are release,
	 * the {@link SocketManager} itself is closed.
	 * 
	 * @param instance {@link HttpServerSocketManagedObjectSource}.
	 * @throws IOException      If fails to close the {@link SocketManager}.
	 * @throws TimeoutException If taking too long to close the
	 *                          {@link SocketManager}.
	 */
	private static synchronized void releaseFromSocketManager(HttpServerSocketManagedObjectSource instance)
			throws IOException, TimeoutException {

		// Unregister from the connection manager
		registeredServerSocketManagedObjectSources.remove(instance);

		// Close connection manager if no further server sockets
		if (registeredServerSocketManagedObjectSources.size() == 0) {
			closeSocketManager();
		}
	}

	/**
	 * {@link HttpServerLocation}.
	 */
	private HttpServerLocation serverLocation;

	/**
	 * <code>Server</code> {@link HttpHeaderValue}.
	 */
	private HttpHeaderValue serverName;

	/**
	 * {@link DateHttpHeaderClock}.
	 */
	private DateHttpHeaderClock dateHttpHeaderClock;

	/**
	 * Indicates whether to include the {@link Escalation} stack trace in the
	 * {@link HttpResponse}.
	 */
	private boolean isIncludeEscalationStackTrace = true;

	/**
	 * {@link HttpRequestParserMetaData}.
	 */
	private HttpRequestParserMetaData httpRequestParserMetaData;

	/**
	 * Indicates if secure HTTP connection.
	 */
	private boolean isSecure;

	/**
	 * {@link SSLContext}.
	 */
	private SSLContext sslContext;

	/**
	 * {@link Flow} index for the handle request.
	 */
	private int handleRequestFlowIndex;

	/**
	 * {@link ManagedObjectExecutorFactory}.
	 */
	private ManagedObjectExecutorFactory<Indexed> executorFactory;

	/**
	 * {@link ServerSocketDecorator}.
	 */
	private ServerSocketDecorator serverSocketDecorator;

	/**
	 * {@link AcceptedSocketDecorator}.
	 */
	private AcceptedSocketDecorator acceptedSocketDecorator;

	/**
	 * {@link Logger}.
	 */
	private Logger logger;

	/**
	 * Default constructor to configure from {@link PropertyList}.
	 */
	public HttpServerSocketManagedObjectSource() {
	}

	/**
	 * Instantiate for non-secure servicing.
	 * 
	 * @param serverLocation                {@link HttpServerLocation}.
	 * @param serverName                    <code>Server</code>
	 *                                      {@link HttpHeaderValue}.
	 * @param dateHttpHeaderClock           {@link DateHttpHeaderClock}.
	 * @param isIncludeEscalationStackTrace Indicates if include the
	 *                                      {@link Escalation} stack trace on the
	 *                                      {@link HttpResponse}.
	 */
	public HttpServerSocketManagedObjectSource(HttpServerLocation serverLocation, HttpHeaderValue serverName,
			DateHttpHeaderClock dateHttpHeaderClock, boolean isIncludeEscalationStackTrace) {
		this.serverLocation = serverLocation;
		this.serverName = serverName;
		this.dateHttpHeaderClock = dateHttpHeaderClock;
		this.isIncludeEscalationStackTrace = isIncludeEscalationStackTrace;
		this.isSecure = false;
		this.sslContext = null;
	}

	/**
	 * Instantiate for secure servicing.
	 * 
	 * @param serverLocation                {@link HttpServerLocation}.
	 * @param serverName                    <code>Server</code>
	 *                                      {@link HttpHeaderValue}.
	 * @param dateHttpHeaderClock           {@link DateHttpHeaderClock}.
	 * @param isIncludeEscalationStackTrace Indicates if include the
	 *                                      {@link Escalation} stack trace on the
	 *                                      {@link HttpResponse}.
	 * @param sslContext                    {@link SSLContext}. May be
	 *                                      <code>null</code> if behind reverse
	 *                                      proxy handling secure communication.
	 */
	public HttpServerSocketManagedObjectSource(HttpServerLocation serverLocation, HttpHeaderValue serverName,
			DateHttpHeaderClock dateHttpHeaderClock, boolean isIncludeEscalationStackTrace, SSLContext sslContext) {
		this.serverLocation = serverLocation;
		this.serverName = serverName;
		this.dateHttpHeaderClock = dateHttpHeaderClock;
		this.isIncludeEscalationStackTrace = isIncludeEscalationStackTrace;
		this.isSecure = true;
		this.sslContext = sslContext;
	}

	/**
	 * Enables overriding to configure a {@link ServerSocketDecorator}.
	 * 
	 * @param context {@link MetaDataContext}.
	 * @return {@link ServerSocketDecorator}.
	 */
	protected ServerSocketDecorator getServerSocketDecorator(MetaDataContext<None, Indexed> context) {
		return null;
	}

	/**
	 * Enables overriding to configure the {@link AcceptedSocketDecorator}.
	 * 
	 * @param context {@link MetaDataContext}.
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
		// All properties have reasonable defaults
	}

	@Override
	protected void loadMetaData(MetaDataContext<None, Indexed> context) throws Exception {

		// Configure meta-data
		context.setObjectClass(ServerHttpConnection.class);
		context.setManagedObjectClass(ProcessAwareServerHttpConnectionManagedObject.class);

		// Create the flow meta-data
		Labeller<Indexed> handleRequestFlow = context.addFlow(null);
		handleRequestFlow.setLabel(HANDLE_REQUEST_FLOW_NAME);
		this.handleRequestFlowIndex = handleRequestFlow.getIndex();

		// Create the execution meta-data (for servicing HTTP sockets)
		context.addExecutionStrategy().setLabel("HTTP_SOCKET_SERVICING");

		// Obtain the managed object source context
		ManagedObjectSourceContext<Indexed> mosContext = context.getManagedObjectSourceContext();

		// Load configuration
		int maxHeaderCount = Integer.parseInt(mosContext.getProperty(PROPERTY_MAX_HEADER_COUNT, String.valueOf(50)));
		int maxTextLength = Integer.parseInt(mosContext.getProperty(PROPERTY_MAX_TEXT_LENGTH, String.valueOf(2048)));
		long maxEntityLength = Long
				.parseLong(mosContext.getProperty(PROPERTY_MAX_ENTITY_LENGTH, String.valueOf(1 * 1024 * 1024)));

		// Create the request parser meta-data
		this.httpRequestParserMetaData = new HttpRequestParserMetaData(maxHeaderCount, maxTextLength, maxEntityLength);

		// Obtain the decorators
		this.serverSocketDecorator = this.getServerSocketDecorator(context);
		this.acceptedSocketDecorator = this.getAcceptedSocketDecorator(context);

		// Determine if configured via default constructor
		if (this.serverLocation == null) {

			// Default constructor so load configuration
			this.serverLocation = new HttpServerLocationImpl(mosContext);
			this.isIncludeEscalationStackTrace = Boolean.parseBoolean(mosContext.getProperty(
					HttpServer.PROPERTY_INCLUDE_STACK_TRACE, String.valueOf(this.isIncludeEscalationStackTrace)));

			// Determine if secure and set up security
			this.isSecure = Boolean.parseBoolean(mosContext.getProperty(PROPERTY_SECURE, String.valueOf(false)));
			this.sslContext = this.isSecure ? HttpServer.getSslContext(mosContext) : null;
		}

		// Add the executor factory if SSL context
		if (this.sslContext != null) {
			this.executorFactory = new ManagedObjectExecutorFactory<>(context, SSL_TEAM_NAME);
		}

		// Add recycle function (to capture clean up failures)
		mosContext.getRecycleFunction(() -> HttpServerSocketManagedObjectSource.this).linkParameter(0,
				RecycleManagedObjectParameter.class);
	}

	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void start(ManagedObjectExecuteContext<Indexed> context) throws Exception {

		// Capture the logger
		this.logger = context.getLogger();

		// Obtain the execution strategy
		ThreadFactory[] executionStrategy = context.getExecutionStrategy(0);

		// Easy access to source
		HttpServerSocketManagedObjectSource source = this;

		// Provide HTTP service
		context.addService(new ManagedObjectService<Indexed>() {

			@Override
			public void startServicing(ManagedObjectServiceContext<Indexed> serviceContext) throws Exception {

				// Obtain the socket manager
				SocketManager socketManager = getSocketManager(source, executionStrategy);

				// Create the HTTP servicer factory
				ManagedObjectSourceHttpServicerFactory servicerFactory = new ManagedObjectSourceHttpServicerFactory(
						serviceContext, source.serverLocation, source.isSecure, source.httpRequestParserMetaData,
						source.serverName, source.dateHttpHeaderClock, source.isIncludeEscalationStackTrace);

				// Create the SSL servicer factory
				SocketServicerFactory socketServicerFactory = servicerFactory;
				RequestServicerFactory requestServicerFactory = servicerFactory;
				if (source.sslContext != null) {

					// Create the executor for the SSL tasks
					ProcessAwareServerHttpConnectionManagedObject executorManagedObject = new ProcessAwareServerHttpConnectionManagedObject<>(
							source.serverLocation, true, () -> HttpMethod.GET, () -> "SSL TASK", HttpVersion.HTTP_1_1,
							null, null, source.serverName, source.dateHttpHeaderClock, false, null, null);
					Executor executor = source.executorFactory.createExecutor(serviceContext, executorManagedObject);

					// Register SSL servicing
					SslSocketServicerFactory<?> sslServicerFactory = new SslSocketServicerFactory<>(source.sslContext,
							servicerFactory, servicerFactory, executor);
					socketServicerFactory = sslServicerFactory;
					requestServicerFactory = sslServicerFactory;
				}

				// Bind server socket for this managed object source
				int port = (source.isSecure ? source.serverLocation.getClusterHttpsPort()
						: source.serverLocation.getClusterHttpPort());
				socketManager.bindServerSocket(port, source.serverSocketDecorator, source.acceptedSocketDecorator,
						socketServicerFactory, requestServicerFactory);
			}

			@Override
			public void stopServicing() {

				// Release socket manager (closes socket listeners when appropriate)
				try {
					releaseFromSocketManager(source);

				} catch (IOException | TimeoutException ex) {
					// Shutting down so just log issue
					if (source.logger.isLoggable(Level.INFO)) {
						source.logger.log(Level.INFO, "Failed to release " + SocketManager.class.getSimpleName(), ex);
					}
				}
			}
		});
	}

	@Override
	protected ManagedObject getManagedObject() throws Throwable {
		// Should never be directly used by a function
		throw new IllegalStateException("Can not source managed object from a " + this.getClass().getSimpleName());
	}

	/*
	 * ==================== ManagedFunction =====================
	 */

	@Override
	public void execute(ManagedFunctionContext<Indexed, None> context) throws Throwable {

		// Obtain the parameter
		RecycleManagedObjectParameter<ProcessAwareServerHttpConnectionManagedObject<ByteBuffer>> parameter = RecycleManagedObjectParameter
				.getRecycleManagedObjectParameter(context);

		// Obtain the managed object
		ProcessAwareServerHttpConnectionManagedObject<ByteBuffer> managedObject = parameter.getManagedObject();

		// Load potential clean up escalations
		managedObject.setCleanupEscalations(parameter.getCleanupEscalations());
	}

	/**
	 * {@link AbstractHttpServicerFactory}.
	 */
	private class ManagedObjectSourceHttpServicerFactory extends AbstractHttpServicerFactory {

		/**
		 * {@link ManagedObjectServiceContext} to service the
		 * {@link ServerHttpConnection}.
		 */
		private final ManagedObjectServiceContext<Indexed> context;

		/**
		 * Instantiate.
		 * 
		 * @param context                       {@link ManagedObjectServiceContext}.
		 * @param serverLocation                {@link HttpServerLocation}.
		 * @param isSecure                      Indicates if a secure
		 *                                      {@link ServerHttpConnection}.
		 * @param metaData                      {@link HttpRequestParserMetaData}.
		 * @param serverName                    <code>Server</code>
		 *                                      {@link HttpHeaderValue}.
		 * @param dateHttpHeaderClock           {@link DateHttpHeaderClock}.
		 * @param isIncludeEscalationStackTrace Indicates whether to include the
		 *                                      {@link Escalation} stack trace in the
		 *                                      {@link HttpResponse}.
		 */
		public ManagedObjectSourceHttpServicerFactory(ManagedObjectServiceContext<Indexed> context,
				HttpServerLocation serverLocation, boolean isSecure, HttpRequestParserMetaData metaData,
				HttpHeaderValue serverName, DateHttpHeaderClock dateHttpHeaderClock,
				boolean isIncludeEscalationStackTrace) {
			super(serverLocation, isSecure, metaData, serverName, dateHttpHeaderClock, isIncludeEscalationStackTrace);
			this.context = context;
		}

		@Override
		protected ProcessManager service(ProcessAwareServerHttpConnectionManagedObject<ByteBuffer> connection)
				throws IOException, HttpException {

			// Service request
			return this.context.invokeProcess(HttpServerSocketManagedObjectSource.this.handleRequestFlowIndex, null,
					connection, 0, connection.getServiceFlowCallback());
		}
	}

}
