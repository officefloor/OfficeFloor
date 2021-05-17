/*-
 * #%L
 * Vertx HTTP Server
 * %%
 * Copyright (C) 2005 - 2021 Daniel Sagenschneider
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

package net.officefloor.server.http.vertx;

import org.junit.jupiter.api.BeforeEach;

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

	@BeforeEach
	public void resetVertx() {
		// Require resetting Vertx to avoid overload the thread pool for fast tests
		OfficeFloorVertx.setVertx(null);
	}

	/*
	 * ============== AbstractHttpServerImplementationTestCase ================
	 */

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
