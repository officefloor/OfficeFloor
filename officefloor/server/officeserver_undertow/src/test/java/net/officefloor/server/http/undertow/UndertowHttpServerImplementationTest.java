package net.officefloor.server.http.undertow;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

import io.undertow.server.HttpServerExchange;
import io.undertow.util.HttpString;
import net.officefloor.frame.api.manage.ProcessManager;
import net.officefloor.server.http.AbstractHttpServerImplementationTest;
import net.officefloor.server.http.HttpHeader;
import net.officefloor.server.http.HttpServerImplementation;
import net.officefloor.server.http.HttpServerLocation;

/**
 * Tests the {@link UndertowHttpServerImplementation}.
 * 
 * @author Daniel Sagenschneider
 */
public class UndertowHttpServerImplementationTest
		extends AbstractHttpServerImplementationTest<UndertowHttpServerImplementationTest.RawUndertowHttpServer> {

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
	protected RawUndertowHttpServer startRawHttpServer(HttpServerLocation serverLocation) throws Exception {
		RawUndertowHttpServer server = new RawUndertowHttpServer();
		server.startHttpServer(serverLocation.getClusterHttpPort(), -1, null);
		return server;
	}

	@Override
	protected void stopRawHttpServer(RawUndertowHttpServer server) throws Exception {
		server.stopHttpServer();
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