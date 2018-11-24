/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package net.officefloor.server.http;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import net.officefloor.server.http.impl.HttpServerLocationImpl;

/**
 * {@link TestRule} for a {@link HttpClient} to {@link HttpServer}.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpClientRule implements TestRule {

	/**
	 * Indicates if secure.
	 */
	private final boolean isSecure;

	/**
	 * Port for connection.
	 */
	private final int port;

	/**
	 * {@link CloseableHttpClient}.
	 */
	private CloseableHttpClient client;

	/**
	 * Instantiate for non-secure (HTTP) connection to {@link HttpServer}.
	 */
	public HttpClientRule() {
		this(false);
	}

	/**
	 * Instantiate with flagging if secure (HTTPS) connection to {@link HttpServer}.
	 * 
	 * @param isSecure
	 *            Indicates if secure (HTTPS).
	 */
	public HttpClientRule(boolean isSecure) {
		this(isSecure, isSecure ? HttpServerLocationImpl.DEFAULT_HTTPS_PORT : HttpServerLocationImpl.DEFAULT_HTTP_PORT);
	}

	/**
	 * Instantiate indicating the server port.
	 * 
	 * @param isSecure
	 *            Indicates if secure (HTTPS).
	 * @param port
	 *            Server port to connect.
	 */
	public HttpClientRule(boolean isSecure, int port) {
		this.isSecure = isSecure;
		this.port = port;
	}

	/**
	 * Obtains the {@link HttpClient}.
	 * 
	 * @return {@link HttpClient}.
	 */
	public HttpClient getHttpClient() {
		if (this.client == null) {
			throw new IllegalStateException(HttpClient.class.getSimpleName() + " only available within test");
		}
		return this.client;
	}

	/**
	 * Creates URL to the server.
	 * 
	 * @param path
	 *            Path on the server.
	 * @return URL to the server.
	 */
	public String url(String path) {
		return (this.isSecure ? "https" : "http") + "://localhost:" + this.port + path;
	}

	/**
	 * Convenience method to execute the {@link HttpUriRequest}.
	 * 
	 * @param request
	 *            {@link HttpUriRequest}.
	 * @return {@link HttpResponse}.
	 * @throws IOException
	 *             If failure in communication with server.
	 * @throws ClientProtocolException
	 *             If client protocol issue.
	 */
	public HttpResponse execute(HttpUriRequest request) throws IOException, ClientProtocolException {
		return this.getHttpClient().execute(request);
	}

	/*
	 * =============== TestRule ======================
	 */

	@Override
	public Statement apply(Statement base, Description description) {
		return new Statement() {

			@Override
			public void evaluate() throws Throwable {
				try (CloseableHttpClient client = HttpClientTestUtil.createHttpClient(HttpClientRule.this.isSecure)) {
					HttpClientRule.this.client = client;

					// Undertake test
					base.evaluate();

				} finally {
					HttpClientRule.this.client = null;
				}
			}
		};
	}

}