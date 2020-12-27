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

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Iterator;
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
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import net.officefloor.frame.api.manage.ProcessManager;
import net.officefloor.frame.api.managedobject.ManagedObjectContext;
import net.officefloor.frame.api.managedobject.ProcessSafeOperation;
import net.officefloor.frame.api.managedobject.pool.ThreadCompletionListener;
import net.officefloor.server.SocketManager;
import net.officefloor.server.http.impl.HttpServerLocationImpl;
import net.officefloor.server.http.impl.ProcessAwareServerHttpConnectionManagedObject;
import net.officefloor.server.http.parse.HttpRequestParser.HttpRequestParserMetaData;
import net.officefloor.test.StressTest;

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
	@Disabled
	@StressTest
	public void overloadServer() throws Exception {

		// Create request
		final String GET = "GET / HTTP/1.1\r\nHost: localhost\r\n\r\n";
		final byte[] GET_BYTES = GET.getBytes(Charset.forName("UTF-8"));
		ByteBuffer buffer = ByteBuffer.allocateDirect(GET_BYTES.length);
		buffer.put(GET_BYTES);
		buffer.flip();

		// Open selector
		Selector selector = Selector.open();

		// Undertake connection for so many clients
		final int clientCount = 512;
		int invalidatedKeys = 0;
		for (int i = 0; i < clientCount; i++) {
			try {
				SocketChannel socket = SocketChannel.open();
				socket.configureBlocking(false);
				socket.connect(new InetSocketAddress("localhost", SERVER_LOCATION.getClusterHttpPort()));
				socket.register(selector, SelectionKey.OP_CONNECT).attach(buffer.duplicate());
			} catch (Exception ex) {
				invalidatedKeys++;
			}
		}

		// Loop for connection
		int requestsCount = 0;
		int responseBuffersCount = 0;
		int failures = 0;
		ByteBuffer readBuffer = ByteBuffer.allocateDirect(8192);
		long startTime = System.currentTimeMillis();
		try {
			for (;;) {

				// Undertake selection
				selector.select(100);

				// Service the keys
				NEXT_KEY: for (Iterator<SelectionKey> iterator = selector.selectedKeys().iterator(); iterator
						.hasNext();) {
					SelectionKey key = iterator.next();
					iterator.remove();

					if (!key.isValid()) {
						invalidatedKeys++;
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
						try {
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
						} catch (Exception ex) {
							failures++;
						}
					}

					// Consume all return data
					if (key.isReadable()) {
						try {
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
						} catch (Exception ex) {
							failures++;
						}
					}
				}

				// Determine if time up
				if (System.currentTimeMillis() > (startTime + (1 * 60 * 1000))) {
					if (selector.keys().size() == 0) {
						return; // no keys, so connection closed
					}

					// Close the connections
					for (SelectionKey key : selector.keys()) {
						try {
							SocketChannel channel = (SocketChannel) key.channel();
							key.cancel();
							channel.close();
						} catch (Exception ex) {
							// Ignore shutdown failures
						}
					}
				}
			}
		} finally {
			// Indicate number of requests made
			System.out.println("Clients " + clientCount);
			System.out.println("Requests " + requestsCount);
			System.out.println("Responses " + this.successfulResponses.get());
			System.out.println("Overload " + this.overloadResponses.get());
			System.out.println("Invalidated " + invalidatedKeys);
			System.out.println("Response buffers " + responseBuffersCount);
			System.out.println("Failures " + failures);
		}
	}

	@BeforeEach
	public void startServer() throws Exception {

		// Create socket manager (leaving thread for client and background processing)
		int servicerCount = Math.max(1, Runtime.getRuntime().availableProcessors() - 2);
		ThreadCompletionListener[] threadCompletionListenerCapture = new ThreadCompletionListener[] { null };
		this.socketManager = HttpServerSocketManagedObjectSource.createSocketManager(new ThreadFactory[servicerCount],
				(threadCompletionListener) -> {
					threadCompletionListenerCapture[0] = threadCompletionListener;
				});

		// Create the expected response
		StringBuilder responseBuilder = new StringBuilder();
		for (int i = 0; i < 10_000; i++) {
			responseBuilder.append((char) (i % 128));
		}
		this.expectedResponse = responseBuilder.toString();

		// Create the executor that can be overloaded
		this.executor = new ThreadPoolExecutor(1, 1, 0, TimeUnit.MILLISECONDS, new SynchronousQueue<Runnable>(),
				(runnable) -> new Thread(() -> {
					try {
						runnable.run();
					} finally {
						threadCompletionListenerCapture[0].threadComplete();
					}
				}));

		// Bind for servicing
		OverloadHttpServicerFactory servicerFactory = new OverloadHttpServicerFactory(SERVER_LOCATION, this.executor,
				this.expectedResponse);
		this.socketManager.bindServerSocket(SERVER_LOCATION.getClusterHttpPort(), null, null, servicerFactory,
				servicerFactory);

		// Start servicing
		for (Runnable runnable : this.socketManager.getRunnables()) {
			new Thread(() -> {
				try {
					runnable.run();
				} finally {
					threadCompletionListenerCapture[0].threadComplete();
				}
			}).start();
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
						} catch (IOException ex) {
							// ignore overload
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

			} catch (IOException ex) {
				throw ex; // propagate

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
