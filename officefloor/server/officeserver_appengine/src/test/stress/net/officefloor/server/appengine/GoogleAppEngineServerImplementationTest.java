/*-
 * #%L
 * Google AppEngine OfficeFloor Server
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

package net.officefloor.server.appengine;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.function.Consumer;

import javax.net.ssl.SSLContext;
import javax.servlet.DispatcherType;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.SecureRequestCustomizer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.SslConnectionFactory;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.util.ssl.SslContextFactory;

import net.officefloor.compile.spi.office.extension.OfficeExtensionService;
import net.officefloor.compile.spi.officefloor.extension.OfficeFloorExtensionService;
import net.officefloor.compile.test.officefloor.CompileOfficeFloor;
import net.officefloor.frame.api.build.OfficeFloorEvent;
import net.officefloor.frame.api.build.OfficeFloorListener;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.test.Closure;
import net.officefloor.server.http.AbstractHttpServerImplementationTest;
import net.officefloor.server.http.HttpHeader;
import net.officefloor.server.http.HttpServer;
import net.officefloor.server.http.HttpServerImplementation;
import net.officefloor.server.http.HttpServerLocation;
import net.officefloor.server.http.impl.HttpServerLocationImpl;
import net.officefloor.server.http.servlet.HttpServletHttpServerImplementation;
import net.officefloor.server.http.servlet.OfficeFloorFilter;

/**
 * Tests the Google AppEngine integration.
 * 
 * @author Daniel Sagenschneider
 */
public class GoogleAppEngineServerImplementationTest extends AbstractHttpServerImplementationTest<Server> {

	@Override
	protected int getRequestCount() {
		return 10000; // request threads, so avoid overload
	}

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
	 * =================== HttpServerImplementationTest =====================
	 */

	@Override
	protected void startHttpServer(OfficeFloorExtensionService officeFloorExtension,
			OfficeExtensionService officeExtension, Consumer<OfficeFloor> officeFloorListener) throws Exception {

		// Obtain the HTTP server location and SSL Context
		Closure<HttpServerLocation> location = new Closure<>();
		Closure<SSLContext> sslContext = new Closure<>();
		CompileOfficeFloor locationCompiler = new CompileOfficeFloor();
		locationCompiler.officeFloor((context) -> {
			location.value = new HttpServerLocationImpl(context.getOfficeFloorSourceContext());
			sslContext.value = HttpServer.getSslContext(context.getOfficeFloorSourceContext());
		});
		locationCompiler.compileAndOpenOfficeFloor().close();

		// Create the server
		Server server = createServer(location.value, sslContext.value);
		ServletContextHandler handler = new ServletContextHandler();
		handler.addFilter(FixHeadersFilter.class, "/*", EnumSet.of(DispatcherType.REQUEST));
		handler.addFilter(OfficeFloorFilter.class, "/*", EnumSet.of(DispatcherType.REQUEST));
		server.setHandler(handler);

		// Provide wrapping OfficeFloor (to manage server)
		CompileOfficeFloor serverCompiler = new CompileOfficeFloor();
		serverCompiler.officeFloor(
				(context) -> context.getOfficeFloorDeployer().addOfficeFloorListener(new OfficeFloorListener() {
					@Override
					public void officeFloorOpened(OfficeFloorEvent event) throws Exception {
						try {
							HttpServletOfficeFloorExtensionService.officeFloorExtensionService = officeFloorExtension;
							HttpServletOfficeExtensionService.officeExtensionService = officeExtension;
							server.start();
						} finally {
							HttpServletOfficeFloorExtensionService.officeFloorExtensionService = null;
							HttpServletOfficeExtensionService.officeExtensionService = null;

						}
					}

					@Override
					public void officeFloorClosed(OfficeFloorEvent event) throws Exception {
						server.stop();
					}
				}));
		OfficeFloor officeFloor = serverCompiler.compileAndOpenOfficeFloor();
		officeFloorListener.accept(officeFloor);
	}

	@Override
	protected Class<? extends HttpServerImplementation> getHttpServerImplementationClass() {
		return HttpServletHttpServerImplementation.class;
	}

	@Override
	protected Server startRawHttpServer(HttpServerLocation serverLocation) throws Exception {
		Server server = createServer(serverLocation, null);
		byte[] helloWorld = "hello world".getBytes(Charset.forName("UTF-8"));
		server.setHandler(new AbstractHandler() {

			@Override
			public void handle(String target, Request baseRequest, HttpServletRequest request,
					HttpServletResponse response) throws IOException, ServletException {
				response.setStatus(HttpServletResponse.SC_OK);
				FixHeadersFilter.loadFixHeaders(response);
				response.setContentType("text/plain");
				response.getOutputStream().write(helloWorld);
				baseRequest.setHandled(true);
			}
		});
		server.start();
		return server;
	}

	@Override
	protected void stopRawHttpServer(Server server) throws Exception {
		server.stop();
	}

	@Override
	protected HttpHeader[] getServerResponseHeaderValues() {
		return new HttpHeader[] { newHttpHeader("Date", "NOW"), newHttpHeader("Server", this.getServerName()),
				newHttpHeader("Expires", "Thu, 01 Jan 1970 00:00:00 GMT"), newHttpHeader("Content-Type", "text/plain"),
				newHttpHeader("Content-Length", "?") };
	}

	@Override
	protected String getServerNameSuffix() {
		return "Jetty";
	}

	@Override
	protected boolean isHandleCancel() {
		return false;
	}

	/**
	 * {@link Filter} to fix the {@link org.eclipse.jetty.http.HttpHeader}
	 * instances.
	 */
	public static class FixHeadersFilter extends HttpFilter {

		/**
		 * Serial version UID.
		 */
		private static final long serialVersionUID = 1L;

		/**
		 * Loads the fix {@link org.eclipse.jetty.http.HttpHeader} instances.
		 * 
		 * @param response {@link HttpServletResponse}.
		 */
		protected static void loadFixHeaders(HttpServletResponse response) {
			response.setHeader("Date", "NOW");
			response.setHeader("Server", "OfficeFloorServer Jetty");
			response.setHeader("Expires", "Thu, 01 Jan 1970 00:00:00 GMT");
		}

		/*
		 * ================ HttpFilter ======================
		 */

		@Override
		protected void doFilter(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
				throws IOException, ServletException {

			// Fix the headers
			loadFixHeaders(res);

			// Continue chain
			chain.doFilter(req, res);
		}
	}

}
