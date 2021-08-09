/*-
 * #%L
 * Testing of HTTP Server
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

import org.apache.http.client.HttpClient;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.Extension;
import org.junit.jupiter.api.extension.ExtensionContext;

/**
 * {@link Extension} for a {@link HttpClient} to {@link HttpServer}.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpClientExtension extends AbstractHttpClientJUnit<HttpClientExtension>
		implements BeforeAllCallback, BeforeEachCallback, AfterEachCallback, AfterAllCallback {

	/**
	 * Indicates whether to start/stop {@link HttpClient} for each test.
	 */
	private boolean isEach = true;

	/**
	 * Instantiate for non-secure (HTTP) connection to {@link HttpServer}.
	 */
	public HttpClientExtension() {
	}

	/**
	 * Instantiate with flagging if secure (HTTPS) connection to {@link HttpServer}.
	 * 
	 * @param isSecure Indicates if secure (HTTPS).
	 */
	public HttpClientExtension(boolean isSecure) {
		super(isSecure);
	}

	/**
	 * Instantiate indicating the server port.
	 * 
	 * @param isSecure Indicates if secure (HTTPS).
	 * @param port     Server port to connect.
	 */
	public HttpClientExtension(boolean isSecure, int port) {
		super(isSecure, port);
	}

	/*
	 * ======================== Extension =============================
	 */

	@Override
	public void beforeAll(ExtensionContext context) throws Exception {

		// Start only once for all tests
		this.isEach = false;

		// Start the client
		this.openHttpClient();
	}

	@Override
	public void beforeEach(ExtensionContext context) throws Exception {

		// Start the client if for each
		if (this.isEach) {
			this.openHttpClient();
		}
	}

	@Override
	public void afterEach(ExtensionContext context) throws Exception {

		// Stop the client if for each
		if (this.isEach) {
			this.closeHttpClient();
		}
	}

	@Override
	public void afterAll(ExtensionContext context) throws Exception {

		// Stop client if all
		if (!this.isEach) {
			this.closeHttpClient();
		}
	}

}
