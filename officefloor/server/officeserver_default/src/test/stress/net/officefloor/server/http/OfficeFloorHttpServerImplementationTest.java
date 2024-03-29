/*-
 * #%L
 * Default OfficeFloor HTTP Server
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

package net.officefloor.server.http;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.logging.Logger;

import org.junit.jupiter.api.BeforeEach;

import net.officefloor.frame.api.manage.ProcessManager;
import net.officefloor.frame.api.managedobject.ManagedObjectContext;
import net.officefloor.frame.api.managedobject.ProcessSafeOperation;
import net.officefloor.frame.api.managedobject.pool.ThreadCompletionListener;
import net.officefloor.server.SocketManager;
import net.officefloor.server.http.impl.ProcessAwareServerHttpConnectionManagedObject;
import net.officefloor.server.http.parse.HttpRequestParser.HttpRequestParserMetaData;

/**
 * Tests the {@link OfficeFloorHttpServerImplementation}.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeFloorHttpServerImplementationTest extends AbstractHttpServerImplementationTestCase {

	private static final byte[] helloWorld = "hello world".getBytes(ServerHttpConnection.DEFAULT_HTTP_ENTITY_CHARSET);
	private static final HttpHeaderValue textPlain = new HttpHeaderValue("text/plain");

	@BeforeEach
	protected void ensureCleanStart() throws Exception {

		// Ensure clean start of test
		assertFalse(HttpServerSocketManagedObjectSource.isSocketManagerActive(),
				"Should not have active socket manager");
	}

	@Override
	protected Class<? extends HttpServerImplementation> getHttpServerImplementationClass() {
		return OfficeFloorHttpServerImplementation.class;
	}

	@Override
	protected HttpHeader[] getServerResponseHeaderValues() {
		return new HttpHeader[] { newHttpHeader("content-type", "?"), newHttpHeader("content-length", "?") };
	}

	@Override
	protected String getServerNameSuffix() {
		return null;
	}

	@Override
	protected AutoCloseable startRawHttpServer(HttpServerLocation serverLocation) throws Exception {

		// Create thread affinity execution strategy
		ThreadFactory[] executionStrategy = new ThreadFactory[Runtime.getRuntime().availableProcessors()];

		// Create the socket manager
		ThreadCompletionListener[] threadCompletionListenerCapture = new ThreadCompletionListener[] { null };
		SocketManager manager = HttpServerSocketManagedObjectSource.createSocketManager(executionStrategy,
				(threadCompletionListener) -> threadCompletionListenerCapture[0] = threadCompletionListener);

		// Create raw HTTP servicing
		RawHttpServicerFactory serviceFactory = new RawHttpServicerFactory(serverLocation);
		manager.bindServerSocket(serverLocation.getClusterHttpPort(), null, null, serviceFactory, serviceFactory);

		// Start servicing
		ExecutorService executor = Executors.newCachedThreadPool();
		for (Runnable runnable : manager.getRunnables()) {
			executor.execute(() -> {
				try {
					runnable.run();
				} finally {
					threadCompletionListenerCapture[0].threadComplete();
				}
			});
		}

		// Return means to stop server
		return () -> {
			try {
				manager.shutdown();
			} finally {
				executor.shutdown();
			}
		};
	}

	/**
	 * Raw {@link AbstractHttpServicerFactory}.
	 */
	private static class RawHttpServicerFactory extends AbstractHttpServicerFactory {

		/**
		 * {@link ManagedObjectContext}.
		 */
		private static ManagedObjectContext managedObjectContext = new ManagedObjectContext() {

			@Override
			public String getBoundName() {
				fail("Should not require bound name");
				return null;
			}

			@Override
			public Logger getLogger() {
				fail("Should not require logger");
				return null;
			}

			@Override
			public <R, T extends Throwable> R run(ProcessSafeOperation<R, T> operation) throws T {
				return operation.run();
			}
		};

		/**
		 * Instantiate.
		 *
		 * @param serverLocation {@link HttpServerLocation}.
		 */
		public RawHttpServicerFactory(HttpServerLocation serverLocation) {
			super(serverLocation, false, new HttpRequestParserMetaData(100, 1000, 1000000), null, null, true);
		}

		/*
		 * ===================== HttpServicer ====================
		 */

		@Override
		protected ProcessManager service(ProcessAwareServerHttpConnectionManagedObject<ByteBuffer> connection)
				throws IOException {

			// Configure context awareness
			connection.setManagedObjectContext(managedObjectContext);

			// Service the connection
			HttpResponse response = connection.getResponse();
			response.getEntity().write(helloWorld);
			response.setContentType(textPlain, null);

			// Send response
			try {
				connection.getServiceFlowCallback().run(null);
			} catch (Throwable ex) {
				throw new IOException(ex);
			}

			// No process management
			return null;
		}
	}

}
