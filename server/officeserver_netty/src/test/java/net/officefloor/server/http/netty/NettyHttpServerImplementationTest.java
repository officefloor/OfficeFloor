/*-
 * #%L
 * Netty HTTP Server
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

package net.officefloor.server.http.netty;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.util.AsciiString;
import io.netty.util.CharsetUtil;
import net.officefloor.frame.api.manage.ProcessManager;
import net.officefloor.server.http.AbstractHttpServerImplementationTestCase;
import net.officefloor.server.http.HttpHeader;
import net.officefloor.server.http.HttpServerImplementation;
import net.officefloor.server.http.HttpServerLocation;

/**
 * Tests the {@link NettyHttpServerImplementation}.
 * 
 * @author Daniel Sagenschneider
 */
public class NettyHttpServerImplementationTest extends AbstractHttpServerImplementationTestCase {

	@Override
	protected Class<? extends HttpServerImplementation> getHttpServerImplementationClass() {
		return NettyHttpServerImplementation.class;
	}

	@Override
	protected HttpHeader[] getServerResponseHeaderValues() {
		return new HttpHeader[] { newHttpHeader("Content-Length", "?"), newHttpHeader("Content-Type", "?") };
	}

	@Override
	protected String getServerNameSuffix() {
		return "Netty";
	}

	@Override
	protected AutoCloseable startRawHttpServer(HttpServerLocation serverLocation) throws Exception {
		RawNettyHttpServer server = new RawNettyHttpServer();
		server.startHttpServer(serverLocation.getClusterHttpPort(), -1, null);
		return () -> server.stopHttpServer();
	}

	/**
	 * Raw Netty HTTP Server.
	 */
	public static class RawNettyHttpServer extends AbstractNettyHttpServer {

		private static final byte[] HELLO_WORLD = "hello world".getBytes(CharsetUtil.UTF_8);
		private static final CharSequence CONTENT_LENGTH = new AsciiString("Content-Length");
		private static final int HELLO_WORLD_LENGTH = HELLO_WORLD.length;
		private static final ByteBuf HELLO_WORLD_BUFFER = Unpooled
				.unreleasableBuffer(Unpooled.directBuffer().writeBytes(HELLO_WORLD));
		private static final CharSequence CONTENT_TYPE = new AsciiString("Content-Type");
		private static final CharSequence TYPE_PLAIN = new AsciiString("text/plain");

		public RawNettyHttpServer() {
			super(1024);
		}

		@Override
		protected ProcessManager service(ChannelHandlerContext context, HttpRequest request) throws Exception {
			FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK,
					HELLO_WORLD_BUFFER.duplicate(), false);
			response.headers().set(CONTENT_LENGTH, HELLO_WORLD_LENGTH).set(CONTENT_TYPE, TYPE_PLAIN);
			context.write(response);
			return () -> {
				// no cancel handling
			};
		}
	}

}
