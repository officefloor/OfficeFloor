/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2013 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.officefloor.server.http;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.util.LinkedList;
import java.util.List;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;

import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.ProtocolException;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.RedirectStrategy;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.socket.LayeredConnectionSocketFactory;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.junit.Assert;

import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.spi.office.OfficeSectionInput;
import net.officefloor.compile.spi.officefloor.OfficeFloorInputManagedObject;
import net.officefloor.compile.test.officefloor.CompileOfficeFloorContext;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.server.http.conversation.HttpEntity;
import net.officefloor.server.http.conversation.impl.HttpEntityImpl;
import net.officefloor.server.http.conversation.impl.HttpRequestImpl;
import net.officefloor.server.http.parse.impl.HttpHeaderImpl;
import net.officefloor.server.http.source.HttpServerSocketManagedObjectSource;
import net.officefloor.server.http.source.HttpsServerSocketManagedObjectSource;
import net.officefloor.server.impl.AbstractServerSocketManagedObjectSource;
import net.officefloor.server.ssl.OfficeFloorDefaultSslContextSource;
import net.officefloor.server.stream.impl.ServerInputStreamImpl;

/**
 * Utility class aiding in testing HTTP functionality.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpTestUtil {

	/**
	 * Starting port number.
	 */
	private static int portStart = 12643;

	/**
	 * Obtains the next port number for testing.
	 * 
	 * @return Next port number for testing.
	 */
	public static int getAvailablePort() {
		int port = portStart;
		portStart++; // increment port for next test
		return port;
	}

	/**
	 * Convenience method to configure {@link CompileOfficeFloorContext} with a
	 * test HTTP server.
	 * 
	 * @param context
	 *            {@link CompileOfficeFloorContext}.
	 * @param httpPort
	 *            Port to listen for HTTP requests.
	 * @param httpsPort
	 *            Port to listen for HTTPS requests.
	 * @param sectionName
	 *            Name of the {@link OfficeSection} servicing the requests.
	 * @param sectionInputName
	 *            Name of the {@link OfficeSectionInput} on the
	 *            {@link OfficeSection} servicing the requests.
	 * @return {@link OfficeFloorInputManagedObject}.
	 */
	public static OfficeFloorInputManagedObject configureTestHttpServer(CompileOfficeFloorContext context, int port,
			String sectionName, String sectionInputName) {
		return HttpServerSocketManagedObjectSource.configure(context.getOfficeFloorDeployer(), port,
				context.getDeployedOffice(), sectionName, sectionInputName);
	}

	/**
	 * Convenience method to configure {@link CompileOfficeFloorContext} with a
	 * test HTTP server.
	 * 
	 * @param context
	 *            {@link CompileOfficeFloorContext}.
	 * @param httpPort
	 *            Port to listen for HTTP requests.
	 * @param httpsPort
	 *            Port to listen for HTTPS requests.
	 * @param sectionName
	 *            Name of the {@link OfficeSection} servicing the requests.
	 * @param sectionInputName
	 *            Name of the {@link OfficeSectionInput} on the
	 *            {@link OfficeSection} servicing the requests.
	 * @return {@link OfficeFloorInputManagedObject}.
	 */
	public static OfficeFloorInputManagedObject configureTestHttpServer(CompileOfficeFloorContext context, int port,
			int httpsPort, String sectionName, String sectionInputName) {
		return HttpsServerSocketManagedObjectSource.configure(context.getOfficeFloorDeployer(), port, httpsPort,
				createTestServerSslContext(),
				context.getDeployedOffice().getDeployedOfficeInput(sectionName, sectionInputName));
	}

	/**
	 * Obtains the {@link HttpEntity} content.
	 * 
	 * @param response
	 *            {@link HttpResponse}.
	 * @return Content of {@link HttpEntity}.
	 * @throws IOException
	 *             If fails to obtain content.
	 */
	public static String getEntityBody(HttpResponse response) throws IOException {
		return getEntityBody(response, Charset.defaultCharset());
	}

	/**
	 * Obtains the {@link HttpEntity} content.
	 * 
	 * @param response
	 *            {@link HttpResponse}.
	 * @param charset
	 *            {@link Charset}.
	 * @return Content of {@link HttpEntity}.
	 * @throws IOException
	 *             If fails to obtain content.
	 */
	public static String getEntityBody(HttpResponse response, Charset charset) throws IOException {

		// Obtain the entity
		org.apache.http.HttpEntity entity = response.getEntity();
		if (entity == null) {
			return null; // no entity so no content
		}

		// Return the entity body contents
		return EntityUtils.toString(entity, charset);
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
	 * @param isSecure
	 *            Indicate if require secure connection.
	 * @return {@link CloseableHttpClient}.
	 */
	public static CloseableHttpClient createHttpClient(boolean isSecure) {

		// Create the HTTP client
		HttpClientBuilder builder = HttpClientBuilder.create();

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
	 * @param builder
	 *            {@link HttpClientBuilder}.
	 * 
	 * @see #createTestServerSslContext()
	 */
	public static void configureHttps(HttpClientBuilder builder) {
		// Provide SSL Socket Factory
		builder.setSSLSocketFactory(new OfficeFloorDefaultSocketFactory());
	}

	/**
	 * Configures no redirects for the {@link HttpClientBuilder}.
	 * 
	 * @param builder
	 *            {@link HttpClientBuilder}.
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
	 * @param builder
	 *            {@link HttpClientBuilder}.
	 * @param realm
	 *            Security realm.
	 * @param scheme
	 *            Security scheme.
	 * @param username
	 *            User name.
	 * @param password
	 *            Password.
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
	 * Creates the {@link SSLContext}.
	 * 
	 * @return {@link SSLContext}.
	 */
	public static SSLContext createTestServerSslContext() {
		try {
			return OfficeFloorDefaultSslContextSource.createServerSslContext(null);
		} catch (Exception ex) {
			// Should always create, otherwise fail test
			throw OfficeFrameTestCase.fail(ex);
		}
	}

	/**
	 * Creates a {@link net.officefloor.server.http.HttpRequest} for testing.
	 * 
	 * @param method
	 *            HTTP method (GET, POST).
	 * @param requestUri
	 *            Request URI.
	 * @param entity
	 *            Contents of the
	 *            {@link net.officefloor.server.http.HttpRequest} entity.
	 * @param headerNameValues
	 *            {@link HttpHeader} name values.
	 * @return {@link net.officefloor.server.http.HttpRequest}.
	 * @throws Exception
	 *             If fails to create the
	 *             {@link net.officefloor.server.http.HttpRequest} .
	 */
	public static net.officefloor.server.http.HttpRequest createHttpRequest(String method, String requestUri,
			String entity, String... headerNameValues) throws Exception {

		// Obtain the entity data
		final Charset charset = AbstractServerSocketManagedObjectSource.getCharset(null);
		byte[] entityData = (entity == null ? new byte[0] : entity.getBytes(charset));

		// Create the headers
		List<HttpHeader> headers = new LinkedList<HttpHeader>();
		if (entity != null) {
			// Include content type and content length if entity
			headers.add(new HttpHeaderImpl("content-type", "text/plain; charset=" + charset.name()));
			headers.add(new HttpHeaderImpl("content-length", String.valueOf(entityData.length)));
		}
		for (int i = 0; i < headerNameValues.length; i += 2) {
			String name = headerNameValues[i];
			String value = headerNameValues[i + 1];
			headers.add(new HttpHeaderImpl(name, value));
		}

		// Create the entity input stream
		ServerInputStreamImpl inputStream = new ServerInputStreamImpl(new Object());
		inputStream.inputData(entityData, 0, (entityData.length - 1), false);
		HttpEntity httpEntity = new HttpEntityImpl(inputStream);

		// Return the HTTP request
		return new HttpRequestImpl(method, requestUri, "HTTP/1.1", headers, httpEntity);
	}

	/**
	 * All access via static methods.
	 */
	private HttpTestUtil() {
	}

	/**
	 * <p>
	 * {@link LayeredConnectionSocketFactory} to connect to the
	 * {@link MockHttpServer} over secure connection.
	 * <p>
	 * This allows working with a {@link OfficeFloorDefaultSslContextSource}.
	 */
	private static class OfficeFloorDefaultSocketFactory implements LayeredConnectionSocketFactory {

		/*
		 * ============== ConnectionSocketFactory ==================
		 */

		@Override
		public Socket createSocket(HttpContext context) throws IOException {

			// Create the secure connected socket
			SSLContext sslContext;
			try {
				sslContext = OfficeFloorDefaultSslContextSource.createClientSslContext(null);

			} catch (Exception ex) {
				// Propagate failure in configuring OfficeFloor default key
				throw new IOException(ex);
			}

			// Create the socket
			SSLSocketFactory socketFactory = sslContext.getSocketFactory();
			Socket socket = socketFactory.createSocket();
			Assert.assertFalse("Socket should not be connected", socket.isConnected());

			// Return the socket
			return socket;
		}

		@Override
		public Socket connectSocket(int connectTimeout, Socket socket, HttpHost host, InetSocketAddress remoteAddress,
				InetSocketAddress localAddress, HttpContext context) throws IOException {

			// Connect the socket
			socket.connect(new InetSocketAddress(host.getAddress(), host.getPort()));
			Assert.assertTrue("Socket should now be connected", socket.isConnected());

			// Return the connected socket
			return socket;
		}

		@Override
		public Socket createLayeredSocket(Socket socket, String target, int port, HttpContext context)
				throws IOException, UnknownHostException {
			// Should be already secure socket
			return socket;
		}
	}

}