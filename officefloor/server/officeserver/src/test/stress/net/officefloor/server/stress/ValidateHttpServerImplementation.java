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

package net.officefloor.server.stress;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.SSLContext;

import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.SecureRequestCustomizer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.SslConnectionFactory;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.ssl.SslContextFactory;

import net.officefloor.compile.spi.officefloor.ExternalServiceInput;
import net.officefloor.frame.api.build.OfficeFloorEvent;
import net.officefloor.frame.api.build.OfficeFloorListener;
import net.officefloor.frame.api.source.ServiceContext;
import net.officefloor.server.http.DateHttpHeaderClock;
import net.officefloor.server.http.HttpHeaderValue;
import net.officefloor.server.http.HttpServerImplementation;
import net.officefloor.server.http.HttpServerImplementationContext;
import net.officefloor.server.http.HttpServerImplementationFactory;
import net.officefloor.server.http.HttpServerLocation;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.server.http.impl.ProcessAwareServerHttpConnectionManagedObject;
import net.officefloor.server.stream.StreamBufferPool;
import net.officefloor.server.stream.impl.ThreadLocalStreamBufferPool;

/**
 * {@link HttpServerImplementation} to validate tests.
 *
 * @author Daniel Sagenschneider
 */
public class ValidateHttpServerImplementation implements HttpServerImplementation, HttpServerImplementationFactory {

	/**
	 * {@link StreamBufferPool}.
	 */
	private static final StreamBufferPool<ByteBuffer> bufferPool = new ThreadLocalStreamBufferPool(
			() -> ByteBuffer.allocate(1024), 10, 1000);

	/**
	 * Creates the {@link Server} for the {@link HttpServerLocation}.
	 * 
	 * @param serverLocation {@link HttpServerLocation}.
	 * @param sslContext     {@link SSLContext}. May be <code>null</code> if no
	 *                       HTTPS required.
	 * @return Configured but not started {@link Server}.
	 */
	public static Server createServer(HttpServerLocation serverLocation, SSLContext sslContext) {

		// Create the server
		Server server = new Server();
		List<ServerConnector> connectors = new ArrayList<>(2);

		// Provide HTTP connector
		ServerConnector connector = new ServerConnector(server);
		connector.setPort(serverLocation.getHttpPort());
		connectors.add(connector);

		// Provide HTTPS connector (if required)
		int httpsPort = serverLocation.getHttpsPort();
		if (httpsPort > 0) {

			// Create HTTPS connection
			HttpConfiguration https = new HttpConfiguration();
			https.addCustomizer(new SecureRequestCustomizer());

			// Connection factory for HTTP/1.1
			HttpConnectionFactory http11 = new HttpConnectionFactory(https);

			// Use the provided SSL context
			SslContextFactory.Server sslContextFactory = new SslContextFactory.Server();
			sslContextFactory.setSslContext(sslContext);
			SslConnectionFactory tls = new SslConnectionFactory(sslContextFactory, http11.getProtocol());

			// Provide HTTPS connector
			ServerConnector sslConnector = new ServerConnector(server, tls, http11);
			sslConnector.setPort(httpsPort);
			connectors.add(sslConnector);
		}

		// Configure the connectors
		server.setConnectors(connectors.toArray(new ServerConnector[connectors.size()]));

		// Return the server
		return server;
	}

	/*
	 * ===================== HttpServerImplementation ====================
	 */

	@Override
	public HttpServerImplementation createService(ServiceContext context) throws Throwable {
		return this;
	}

	@Override
	@SuppressWarnings("rawtypes")
	public void configureHttpServer(HttpServerImplementationContext context) throws Exception {

		// Provide server
		Server server = createServer(context.getHttpServerLocation(), context.getSslContext());

		// Create the input
		ExternalServiceInput<ServerHttpConnection, ProcessAwareServerHttpConnectionManagedObject> input = context
				.getExternalServiceInput(ProcessAwareServerHttpConnectionManagedObject.class,
						ProcessAwareServerHttpConnectionManagedObject.getCleanupEscalationHandler());

		// Obtain details for servicing
		HttpHeaderValue serverName = new HttpHeaderValue("OfficeFloorServer Jetty");
		HttpServerLocation serverLocation = context.getHttpServerLocation();
		DateHttpHeaderClock dateHttpHeaderClock = context.getDateHttpHeaderClock();
		boolean isIncludeEscalationStackTrace = context.isIncludeEscalationStackTrace();

		// Provide servicing via using input
		ServletHolder servlet = new ServletHolder("handler", new OfficeFloorHttpServlet(input, serverLocation,
				serverName, dateHttpHeaderClock, isIncludeEscalationStackTrace, bufferPool));
		servlet.setAsyncSupported(true);
		ServletHandler handler = new ServletHandler();
		handler.addServletWithMapping(servlet, "/");
		server.setHandler(handler);

		// Configure starting stopping server
		context.getOfficeFloorDeployer().addOfficeFloorListener(new OfficeFloorListener() {

			@Override
			public void officeFloorOpened(OfficeFloorEvent event) throws Exception {
				server.start();
			}

			@Override
			public void officeFloorClosed(OfficeFloorEvent event) throws Exception {
				server.stop();
			}
		});
	}

}
