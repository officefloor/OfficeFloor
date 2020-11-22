/*-
 * #%L
 * Testing of HTTP Server
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