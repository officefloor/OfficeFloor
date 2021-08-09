/*-
 * #%L
 * Vertx HTTP Server
 * %%
 * Copyright (C) 2005 - 2021 Daniel Sagenschneider
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
