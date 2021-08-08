/*-
 * #%L
 * Undertow HTTP Server
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

package net.officefloor.server.http.undertow;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

import io.undertow.server.HttpServerExchange;
import io.undertow.util.HttpString;
import net.officefloor.frame.api.manage.ProcessManager;
import net.officefloor.server.http.AbstractHttpServerImplementationTestCase;
import net.officefloor.server.http.HttpHeader;
import net.officefloor.server.http.HttpServerImplementation;
import net.officefloor.server.http.HttpServerLocation;

/**
 * Tests the {@link UndertowHttpServerImplementation}.
 * 
 * @author Daniel Sagenschneider
 */
public class UndertowHttpServerImplementationTest extends AbstractHttpServerImplementationTestCase {

	@Override
	protected Class<? extends HttpServerImplementation> getHttpServerImplementationClass() {
		return UndertowHttpServerImplementation.class;
	}

	@Override
	protected HttpHeader[] getServerResponseHeaderValues() {
		return new HttpHeader[] { newHttpHeader("Content-Length", "?"), newHttpHeader("Content-Type", "?") };
	}

	@Override
	protected String getServerNameSuffix() {
		return "Undertow";
	}

	@Override
	protected boolean isHandleCancel() {
		return false;
	}

	@Override
	protected AutoCloseable startRawHttpServer(HttpServerLocation serverLocation) throws Exception {
		RawUndertowHttpServer server = new RawUndertowHttpServer();
		server.startHttpServer(serverLocation.getClusterHttpPort(), -1, null);
		return () -> server.stopHttpServer();
	}

	/**
	 * Raw Undertow HTTP Server.
	 */
	public static class RawUndertowHttpServer extends AbstractUndertowHttpServer {

		private static final byte[] HELLO_WORLD = "hello world".getBytes(Charset.forName("UTF-8"));
		private static final ByteBuffer HELLO_WORLD_BUFFER = ByteBuffer.wrap(HELLO_WORLD);
		private static final HttpString CONTENT_LENGTH = new HttpString("Content-Length");
		private static final HttpString CONTENT_TYPE = new HttpString("Content-Type");
		private static final String TYPE_PLAIN = "text/plain";

		@Override
		protected ProcessManager service(HttpServerExchange exchange) throws Exception {
			exchange.getResponseHeaders().put(CONTENT_LENGTH, HELLO_WORLD.length);
			exchange.getResponseHeaders().put(CONTENT_TYPE, TYPE_PLAIN);
			exchange.getResponseChannel().write(HELLO_WORLD_BUFFER.duplicate());
			exchange.endExchange();
			return () -> {
				// no cancel handling
			};
		}
	}

}
