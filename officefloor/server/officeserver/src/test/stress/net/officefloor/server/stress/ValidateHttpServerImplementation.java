/*-
 * #%L
 * HTTP Server
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

			// Use the provided SSL context
			SslContextFactory sslContextFactory = new SslContextFactory.Server();
			sslContextFactory.setSslContext(sslContext);
			sslContextFactory.setExcludeCipherSuites();
			sslContextFactory.setExcludeProtocols();

			// Provide HTTPS connector
			ServerConnector sslConnector = new ServerConnector(server,
					new SslConnectionFactory(sslContextFactory, "http/1.1"), new HttpConnectionFactory(https));
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
