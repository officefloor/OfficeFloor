package net.officefloor.server.http.vertx;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServerResponse;
import net.officefloor.server.http.AbstractHttpServerImplementationTestCase;
import net.officefloor.server.http.HttpHeader;
import net.officefloor.server.http.HttpServerImplementation;
import net.officefloor.server.http.HttpServerLocation;
import net.officefloor.vertx.OfficeFloorVertx;

/**
 * Tests the {@link VertxHttpServerImplementation}.
 * 
 * @author Daniel Sagenschneider
 */
public class VertxHttpServerImplementationTest extends AbstractHttpServerImplementationTestCase {

	@Override
	protected Class<? extends HttpServerImplementation> getHttpServerImplementationClass() {
		return VertxHttpServerImplementation.class;
	}

	@Override
	protected AutoCloseable startRawHttpServer(HttpServerLocation serverLocation) throws Exception {

		// Create Vertx
		Vertx vertx = Vertx.vertx();

		// Start HTTP server
		int httpPort = serverLocation.getHttpPort();
		OfficeFloorVertx.block(vertx.createHttpServer().requestHandler((request) -> {
			HttpServerResponse response = request.response();
			response.putHeader("Content-Length", "11");
			response.putHeader("Content-Type", "text/plain");
			response.end("hello world");
		}).listen(httpPort));

		// Return shutdown of Vertx
		return () -> OfficeFloorVertx.block(vertx.close());
	}

	@Override
	protected HttpHeader[] getServerResponseHeaderValues() {
		return new HttpHeader[] { newHttpHeader("Content-Length", "?"), newHttpHeader("Content-Type", "?") };
	}

	@Override
	protected String getServerNameSuffix() {
		return "Vertx";
	}

}
