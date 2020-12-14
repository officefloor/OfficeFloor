package net.officefloor.server.http;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import net.officefloor.frame.api.manage.ProcessManager;
import net.officefloor.frame.api.managedobject.ManagedObjectContext;
import net.officefloor.frame.api.managedobject.ProcessSafeOperation;
import net.officefloor.server.SocketManager;
import net.officefloor.server.http.impl.HttpServerLocationImpl;
import net.officefloor.server.http.impl.ProcessAwareServerHttpConnectionManagedObject;
import net.officefloor.server.http.parse.HttpRequestParser.HttpRequestParserMetaData;

/**
 * Tests {@link SocketManager} handles being overloaded with requests.
 * 
 * @author Daniel Sagenschneider
 */
public class OverloadedSocketManagerTest {

	/**
	 * {@link Logger}.
	 */
	private static final Logger LOGGER = Logger.getLogger(OverloadedSocketManagerTest.class.getName());

	/**
	 * {@link HttpServerLocation}.
	 */
	private static final HttpServerLocation SERVER_LOCATION = new HttpServerLocationImpl();

	/**
	 * {@link SocketManager}.
	 */
	private SocketManager socketManager;

	/**
	 * {@link ExecutorService}.
	 */
	private ExecutorService executor;

	/**
	 * Expected response.
	 */
	private String expectedResponse;

	/**
	 * Count of successful responses.
	 */
	private final AtomicInteger successfulResponses = new AtomicInteger(0);

	/**
	 * Count of overload responses.
	 */
	private final AtomicInteger overloadResponses = new AtomicInteger(0);

	/**
	 * Failure in servicing resposne.
	 */
	private volatile Throwable failureInServicingResponse = null;

	/**
	 * Validates providing valid response.
	 */
	@Test
	public void validateServer() throws Exception {
		try (CloseableHttpClient client = HttpClientTestUtil.createHttpClient()) {
			org.apache.http.HttpResponse response = client
					.execute(new HttpGet("http://localhost:" + SERVER_LOCATION.getClusterHttpPort()));
			String entity = EntityUtils.toString(response.getEntity());
			assertEquals(200, response.getStatusLine().getStatusCode(), "Should be successful");
			assertEquals(this.expectedResponse, entity, "Incorrect response");
		}
	}

	/**
	 * Ensures the {@link SocketManager} handles being overloaded with requests.
	 */
	@Test
	public void overloadServer() throws Exception {

		// Create request
		final String GET = "GET / HTTP/1.1\r\nHost: localhost\r\n\r\n";
		final byte[] GET_BYTES = GET.getBytes(Charset.forName("UTF-8"));
		ByteBuffer buffer = ByteBuffer.allocateDirect(GET_BYTES.length);
		buffer.put(GET_BYTES);
		buffer.flip();

		// Open selector
		Selector selector = Selector.open();

		// Undertake connection for so may clients
		SocketChannel socket = SocketChannel.open();
		socket.configureBlocking(false);
		socket.connect(new InetSocketAddress("localhost", SERVER_LOCATION.getClusterHttpPort()));
		socket.register(selector, SelectionKey.OP_CONNECT).attach(buffer.duplicate());

		// Loop for connection
		int requestsCount = 0;
		int responseBuffersCount = 0;
		ByteBuffer readBuffer = ByteBuffer.allocateDirect(8192);
		long startTime = System.currentTimeMillis();
		try {
			for (;;) {

				// Undertake selection
				selector.select(100);

				// Service the keys
				NEXT_KEY: for (SelectionKey key : selector.selectedKeys()) {

					if (!key.isValid()) {
						continue NEXT_KEY;
					}

					// Complete the connection
					if (key.isConnectable()) {
						SocketChannel channel = (SocketChannel) key.channel();
						channel.finishConnect();
						key.interestOps(SelectionKey.OP_WRITE | SelectionKey.OP_READ);
					}

					// Undertake writing responses
					if (key.isWritable()) {
						SocketChannel channel = (SocketChannel) key.channel();
						ByteBuffer writeBuffer = (ByteBuffer) key.attachment();

						// Fill the write requests
						int bytesWritten;
						do {
							bytesWritten = channel.write(writeBuffer);
							if (writeBuffer.remaining() == 0) {
								writeBuffer.flip(); // to write again
								requestsCount++;
							}
						} while (bytesWritten > 0);
					}

					// Consume all return data
					if (key.isReadable()) {
						SocketChannel channel = (SocketChannel) key.channel();

						// Read all data
						int bytesRead;
						do {
							if (readBuffer.remaining() == 0) {
								readBuffer.flip();
								responseBuffersCount++;
							}
							bytesRead = channel.read(readBuffer);
						} while (bytesRead > 0);
					}
				}

				// Determine if time up
				if (System.currentTimeMillis() > (startTime + 10_000)) {
					if (selector.keys().size() == 0) {
						return; // no keys, so connection closed
					}

					// Close the connections
					for (SelectionKey key : selector.keys()) {
						SocketChannel channel = (SocketChannel) key.channel();
						key.cancel();
						channel.close();
					}
				}
			}
		} finally {
			// Indicate number of requests made
			System.out.println("Requests " + requestsCount);
			System.out.println("Responses " + this.successfulResponses.get());
			System.out.println("Overload " + this.overloadResponses.get());
			System.out.println("Response buffers " + responseBuffersCount);
		}
	}

	@BeforeEach
	public void startServer() throws Exception {

		// Create socket manager (leaving thread for client and background processing)
		int servicerCount = Math.max(1, Runtime.getRuntime().availableProcessors() - 2);
		this.socketManager = HttpServerSocketManagedObjectSource.createSocketManager(new ThreadFactory[servicerCount]);

		// Create the expected response
		StringBuilder responseBuilder = new StringBuilder();
		for (int i = 0; i < 10_000; i++) {
			responseBuilder.append((char) (i % 128));
		}
		this.expectedResponse = responseBuilder.toString();

		// Create the executor that can be overloaded
		this.executor = new ThreadPoolExecutor(1, 1, 0, TimeUnit.MILLISECONDS, new SynchronousQueue<Runnable>());

		// Bind for servicing
		OverloadHttpServicerFactory servicerFactory = new OverloadHttpServicerFactory(SERVER_LOCATION, this.executor,
				this.expectedResponse);
		this.socketManager.bindServerSocket(SERVER_LOCATION.getClusterHttpPort(), null, null, servicerFactory,
				servicerFactory);

		// Start servicing
		for (Runnable runnable : this.socketManager.getRunnables()) {
			new Thread(runnable).start();
		}
	}

	@AfterEach
	public void stopServer() throws Throwable {
		try {
			this.socketManager.shutdown();
		} finally {
			this.executor.shutdown();
		}
		if (this.failureInServicingResponse != null) {
			throw this.failureInServicingResponse;
		}
	}

	/**
	 * {@link AbstractHttpServicerFactory} for overloaded testing.
	 */
	private class OverloadHttpServicerFactory extends AbstractHttpServicerFactory implements ManagedObjectContext {

		/**
		 * {@link Executor}.
		 */
		private final Executor executor;

		/**
		 * Response entity.
		 */
		private final String responseEntity;

		/**
		 * Instantiate.
		 * 
		 * @param serverLocation {@link HttpServerLocation}.
		 * @param executor       {@link Executor}.
		 * @param responseEntity Response entity.
		 */
		public OverloadHttpServicerFactory(HttpServerLocation serverLocation, Executor executor,
				String responseEntity) {
			super(serverLocation, false, new HttpRequestParserMetaData(100, 1000, 1000000),
					new HttpHeaderValue("OVERLOAD"), null, false);
			this.executor = executor;
			this.responseEntity = responseEntity;
		}

		/*
		 * ===================== AbstractHttpServicerFactory ==================
		 */

		@Override
		protected ProcessManager service(ProcessAwareServerHttpConnectionManagedObject<ByteBuffer> connection)
				throws IOException, HttpException {

			// Undertake request
			connection.setManagedObjectContext(this);
			try {
				try {

					// Attempt to undertake on thread pool
					this.executor.execute(() -> {

						// Send overload response
						HttpResponse response = connection.getResponse();
						try {
							response.getEntityWriter().write(this.responseEntity);
							this.sendResponse(connection);
							OverloadedSocketManagerTest.this.successfulResponses.incrementAndGet();
						} catch (Throwable ex) {
							OverloadedSocketManagerTest.this.failureInServicingResponse = ex;
						}
					});

				} catch (RejectedExecutionException ex) {
					// Overloaded threading, so indicate overloaded
					HttpResponse response = connection.getResponse();
					response.setStatus(HttpStatus.SERVICE_UNAVAILABLE);
					this.sendResponse(connection);
					OverloadedSocketManagerTest.this.overloadResponses.incrementAndGet();
				}

			} catch (Throwable ex) {
				OverloadedSocketManagerTest.this.failureInServicingResponse = ex;
			}

			// No process management
			return null;
		}

		/**
		 * Sends the response.
		 * 
		 * @param connection {@link ProcessAwareServerHttpConnectionManagedObject}.
		 */
		private void sendResponse(ProcessAwareServerHttpConnectionManagedObject<ByteBuffer> connection)
				throws IOException {
			try {
				connection.getServiceFlowCallback().run(null);
			} catch (Throwable ex) {
				throw new IOException(ex);
			}
		}

		/*
		 * ========================== ManagedObjectContext ======================
		 */

		@Override
		public String getBoundName() {
			return "OVERLOAD";
		}

		@Override
		public Logger getLogger() {
			return LOGGER;
		}

		@Override
		public <R, T extends Throwable> R run(ProcessSafeOperation<R, T> operation) throws T {
			return operation.run();
		}
	}

}