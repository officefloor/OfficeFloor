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
package net.officefloor.server.http;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import net.officefloor.frame.api.managedobject.ProcessAwareContext;
import net.officefloor.frame.api.managedobject.ProcessSafeOperation;
import net.officefloor.server.SocketManager;
import net.officefloor.server.http.impl.ProcessAwareServerHttpConnectionManagedObject;
import net.officefloor.server.http.parse.HttpRequestParser.HttpRequestParserMetaData;
import net.officefloor.server.stream.StreamBufferPool;
import net.officefloor.server.stream.impl.ThreadLocalStreamBufferPool;

/**
 * Tests the {@link OfficeFloorHttpServerImplementation}.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeFloorHttpServerImplementationTest extends AbstractHttpServerImplementationTest<SocketManager> {

	private static final byte[] helloWorld = "hello world".getBytes(ServerHttpConnection.DEFAULT_HTTP_ENTITY_CHARSET);
	private static final HttpHeaderValue textPlain = new HttpHeaderValue("text/plain");

	@Override
	protected HttpServerImplementation createHttpServerImplementation() {
		return new OfficeFloorHttpServerImplementation();
	}

	@Override
	protected HttpHeader[] getServerResponseHeaderValues() {
		return new HttpHeader[] { newHttpHeader("content-type", "?"), newHttpHeader("content-length", "?") };
	}

	@Override
	protected SocketManager startRawHttpServer(int httpPort) throws Exception {

		// Create the socket manager
		SocketManager manager = HttpServerSocketManagedObjectSource.createSocketManager();

		// Create raw HTTP servicing
		final int serviceBufferSize = 256;
		StreamBufferPool<ByteBuffer> serviceBufferPool = new ThreadLocalStreamBufferPool(
				() -> ByteBuffer.allocateDirect(serviceBufferSize), Integer.MAX_VALUE, Integer.MAX_VALUE);
		RawHttpServicerFactory serviceFactory = new RawHttpServicerFactory(serviceBufferPool);
		manager.bindServerSocket(httpPort, null, null, serviceFactory, serviceFactory);

		// Start servicing
		Executor executor = Executors.newCachedThreadPool();
		for (Runnable runnable : manager.getRunnables()) {
			executor.execute(runnable);
		}

		// Return the socket manager
		return manager;
	}

	@Override
	protected void stopRawHttpServer(SocketManager momento) throws Exception {
		momento.shutdown();
	}

	/**
	 * Raw {@link AbstractHttpServicerFactory}.
	 */
	private static class RawHttpServicerFactory extends AbstractHttpServicerFactory {

		/**
		 * {@link ProcessAwareContext}.
		 */
		private static ProcessAwareContext processAwareContext = new ProcessAwareContext() {
			@Override
			public <R, T extends Throwable> R run(ProcessSafeOperation<R, T> operation) throws T {
				return operation.run();
			}
		};

		/**
		 * Instantiate.
		 *
		 * @param serviceBufferPool
		 *            {@link StreamBufferPool}.
		 */
		public RawHttpServicerFactory(StreamBufferPool<ByteBuffer> serviceBufferPool) {
			super(false, new HttpRequestParserMetaData(100, 1000, 1000000), serviceBufferPool);
		}

		/*
		 * ===================== HttpServicer ====================
		 */

		@Override
		protected void service(ProcessAwareServerHttpConnectionManagedObject<ByteBuffer> connection)
				throws IOException {

			// Configure process awareness
			connection.setProcessAwareContext(processAwareContext);

			// Service the connection
			HttpResponse response = connection.getHttpResponse();
			response.getEntity().write(helloWorld);
			response.setContentType(textPlain, null);

			// Send response
			try {
				connection.getServiceFlowCallback().run(null);
			} catch (Throwable ex) {
				throw new IOException(ex);
			}
		}
	}

}