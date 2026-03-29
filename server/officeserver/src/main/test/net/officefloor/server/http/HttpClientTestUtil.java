/*-
 * #%L
 * HTTP Server
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

import java.io.IOException;
import java.nio.charset.Charset;

import org.apache.http.HttpEntity;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.ProtocolException;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.RedirectStrategy;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.cookie.CookieSpecProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.cookie.RFC6265CookieSpecProvider;
import org.apache.http.impl.cookie.RFC6265CookieSpecProvider.CompatibilityLevel;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.junit.Assert;

import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.server.ssl.OfficeFloorDefaultSslContextSource;

/**
 * Utility class aiding in testing HTTP functionality.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpClientTestUtil {

	/**
	 * {@link System} property configured to <code>true</code> to not time out
	 * {@link HttpClient} instances. This is useful for debugging server handling of
	 * requests.
	 */
	public static final String PROPERTY_NO_TIMEOUT = "http.client.debug.no.timeout";

	/**
	 * Indicates whether to timeout the {@link HttpClient}.
	 * 
	 * @return <code>true</code> to allow timing out the {@link HttpClient}.
	 */
	private static boolean isTimeoutClient() {
		String value = System.getProperty(PROPERTY_NO_TIMEOUT);
		return !("true".equalsIgnoreCase(value));
	}

	/**
	 * Obtains the client time out.
	 * 
	 * @return Client time out.
	 */
	public static int getClientTimeout() {
		return isTimeoutClient() ? 10000 : 0;
	}

	/**
	 * Obtains the {@link HttpEntity} content.
	 * 
	 * @param response {@link HttpResponse}.
	 * @return Content of {@link HttpEntity}.
	 * @throws IOException If fails to obtain content.
	 */
	public static String entityToString(HttpResponse response) throws IOException {
		return entityToString(response, ServerHttpConnection.DEFAULT_HTTP_ENTITY_CHARSET);
	}

	/**
	 * Obtains the {@link HttpEntity} content.
	 * 
	 * @param response {@link HttpResponse}.
	 * @param charset  {@link Charset}.
	 * @return Content of {@link HttpEntity}.
	 * @throws IOException If fails to obtain content.
	 */
	public static String entityToString(HttpResponse response, Charset charset) throws IOException {

		// Obtain the entity
		org.apache.http.HttpEntity entity = response.getEntity();
		if (entity == null) {
			return null; // no entity so no content
		}

		// Return the entity body contents
		return EntityUtils.toString(entity, charset);
	}

	/**
	 * Create the {@link HttpClientBuilder}.
	 * 
	 * @return {@link HttpClientBuilder}.
	 */
	public static HttpClientBuilder createHttpClientBuilder() {
		return createHttpClientBuilder(getClientTimeout());
	}

	/**
	 * Create the {@link HttpClientBuilder}.
	 * 
	 * @return {@link HttpClientBuilder}.
	 */
	public static HttpClientBuilder createHttpClientBuilder(int timeout) {

		// Create the HTTP client
		HttpClientBuilder builder = HttpClientBuilder.create();

		// Provide timeout of requests
		RequestConfig.Builder requestConfig = RequestConfig.custom();
		requestConfig.setSocketTimeout(timeout);
		requestConfig.setConnectTimeout(timeout);
		requestConfig.setConnectionRequestTimeout(timeout);

		// Provide cookie handling similar to browser
		final String OVERRIDE_COOKIE_SPEC = "OverrideCookieSpec";
		builder.setDefaultCookieSpecRegistry(RegistryBuilder.<CookieSpecProvider>create().register(OVERRIDE_COOKIE_SPEC,
				new RFC6265CookieSpecProvider(CompatibilityLevel.IE_MEDIUM_SECURITY, null)).build());
		requestConfig.setCookieSpec(OVERRIDE_COOKIE_SPEC);

		builder.setDefaultRequestConfig(requestConfig.build());

		// Return the builder
		return builder;
	}

	/**
	 * Creates a {@link CloseableHttpClient} ready for use with default values.
	 * 
	 * @return {@link CloseableHttpClient}.
	 */
	public static CloseableHttpClient createHttpClient() {
		return createHttpClient(false);
	}

	/**
	 * Creates a {@link CloseableHttpClient} ready for use.
	 * 
	 * @param isSecure Indicate if require secure connection.
	 * @return {@link CloseableHttpClient}.
	 */
	public static CloseableHttpClient createHttpClient(boolean isSecure) {

		// Create the HTTP client builder
		HttpClientBuilder builder = createHttpClientBuilder();

		// Configure to be secure client
		if (isSecure) {
			configureHttps(builder);
		}

		// Create the client
		CloseableHttpClient client = builder.build();

		// Return the client
		return client;
	}

	/**
	 * Configures the {@link HttpClientBuilder} for HTTPS.
	 * 
	 * @param builder {@link HttpClientBuilder}.
	 */
	public static void configureHttps(HttpClientBuilder builder) {
		try {
			// Provide SSL Context
			builder.setSSLContext(OfficeFloorDefaultSslContextSource.createClientSslContext(null));
			builder.setSSLHostnameVerifier(new NoopHostnameVerifier());
		} catch (Exception ex) {
			throw OfficeFrameTestCase.fail(ex);
		}
	}

	/**
	 * Configures no redirects for the {@link HttpClientBuilder}.
	 * 
	 * @param builder {@link HttpClientBuilder}.
	 */
	public static void configureNoRedirects(HttpClientBuilder builder) {
		builder.setRedirectStrategy(new RedirectStrategy() {
			@Override
			public boolean isRedirected(HttpRequest request, HttpResponse response, HttpContext context)
					throws ProtocolException {
				// No redirection
				return false;
			}

			@Override
			public HttpUriRequest getRedirect(HttpRequest request, HttpResponse response, HttpContext context)
					throws ProtocolException {
				Assert.fail("Should not need redirect request");
				return null;
			}
		});
	}

	/**
	 * Configures {@link CredentialsProvider} for the {@link HttpClientBuilder}.
	 * 
	 * @param builder  {@link HttpClientBuilder}.
	 * @param realm    Security realm.
	 * @param scheme   Security scheme.
	 * @param username User name.
	 * @param password Password.
	 * @return {@link CredentialsProvider}.
	 */
	public static CredentialsProvider configureCredentials(HttpClientBuilder builder, String realm, String scheme,
			String username, String password) {

		// Provide credentials
		BasicCredentialsProvider provider = new BasicCredentialsProvider();
		provider.setCredentials(new AuthScope(null, -1, realm, scheme),
				new UsernamePasswordCredentials(username, password));
		builder.setDefaultCredentialsProvider(provider);

		// Return the credentials provider
		return provider;
	}

	/**
	 * All access via static methods.
	 */
	private HttpClientTestUtil() {
	}

}
