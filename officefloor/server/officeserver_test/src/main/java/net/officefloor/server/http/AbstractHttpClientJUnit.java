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

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import net.officefloor.server.http.impl.HttpServerLocationImpl;
import net.officefloor.test.JUnitAgnosticAssert;

/**
 * Abstract {@link HttpClient} to {@link HttpServer}.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class AbstractHttpClientJUnit<T> {

	/**
	 * Indicates if secure.
	 */
	private final boolean isSecure;

	/**
	 * Port for connection.
	 */
	private final int port;

	/**
	 * Indicates to whether to follow redirects.
	 */
	private boolean isFollowRedirects = true;

	/**
	 * {@link CloseableHttpClient}.
	 */
	private CloseableHttpClient client;

	/**
	 * Instantiate for non-secure (HTTP) connection to {@link HttpServer}.
	 */
	public AbstractHttpClientJUnit() {
		this(false);
	}

	/**
	 * Instantiate with flagging if secure (HTTPS) connection to {@link HttpServer}.
	 * 
	 * @param isSecure Indicates if secure (HTTPS).
	 */
	public AbstractHttpClientJUnit(boolean isSecure) {
		this(isSecure, isSecure ? HttpServerLocationImpl.DEFAULT_HTTPS_PORT : HttpServerLocationImpl.DEFAULT_HTTP_PORT);
	}

	/**
	 * Instantiate indicating the server port.
	 * 
	 * @param isSecure Indicates if secure (HTTPS).
	 * @param port     Server port to connect.
	 */
	public AbstractHttpClientJUnit(boolean isSecure, int port) {
		this.isSecure = isSecure;
		this.port = port;
	}

	/**
	 * Flags whether to follow redirects.
	 * 
	 * @param isFollowRedirects Indicates if follow redirects.
	 * @return <code>this</code>.
	 */
	@SuppressWarnings("unchecked")
	public T followRedirects(boolean isFollowRedirects) {
		if (this.client != null) {
			throw new IllegalStateException("Can only configure following redirects at test creation time");
		}
		this.isFollowRedirects = isFollowRedirects;
		return (T) this;
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
	 * @param path Path on the server.
	 * @return URL to the server.
	 */
	public String url(String path) {
		return (this.isSecure ? "https" : "http") + "://localhost:" + this.port + path;
	}

	/**
	 * Convenience method to execute the {@link HttpUriRequest}.
	 * 
	 * @param request {@link HttpUriRequest}.
	 * @return {@link HttpResponse}.
	 * @throws IOException             If failure in communication with server.
	 * @throws ClientProtocolException If client protocol issue.
	 */
	public HttpResponse execute(HttpUriRequest request) throws IOException, ClientProtocolException {
		return this.getHttpClient().execute(request);
	}

	/**
	 * Opens the {@link CloseableHttpClient}.
	 */
	protected void openHttpClient() {
		JUnitAgnosticAssert.assertNull(this.client, HttpClient.class.getSimpleName() + " already created");
		HttpClientBuilder builder = HttpClientTestUtil.createHttpClientBuilder();
		if (this.isSecure) {
			HttpClientTestUtil.configureHttps(builder);
		}
		if (!this.isFollowRedirects) {
			HttpClientTestUtil.configureNoRedirects(builder);
		}
		this.client = builder.build();
	}

	/**
	 * Closes the {@link HttpClient}.
	 * 
	 * @throws IOException If fails to close the {@link HttpClient}.
	 */
	protected void closeHttpClient() throws IOException {
		try {
			if (this.client != null) {
				this.client.close();
			}
		} finally {
			this.client = null;
		}
	}

}