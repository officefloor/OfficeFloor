package net.officefloor.server.servlet.test;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.function.Consumer;

import javax.net.ssl.SSLContext;
import javax.servlet.DispatcherType;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpFilter;
import javax.servlet.http.HttpServlet;
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
import org.eclipse.jetty.servlet.FilterHolder;
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
import net.officefloor.server.http.HttpServerLocation;
import net.officefloor.server.http.impl.HttpServerLocationImpl;

/**
 * Provide abstract test functionality for testing with {@link HttpServlet}.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class AbstractServletHttpServerImplementationTest extends AbstractHttpServerImplementationTest<Server> {

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

	/**
	 * Context for configuring the {@link Server}.
	 */
	protected static class ServerContext {

		/**
		 * {@link Server}.
		 */
		private final Server server;

		/**
		 * {@link ServletContextHandler}.
		 */
		private final ServletContextHandler handler;

		/**
		 * Instantiate.
		 * 
		 * @param server  {@link Server}.
		 * @param handler {@link ServletContextHandler}.
		 */
		private ServerContext(Server server, ServletContextHandler handler) {
			this.server = server;
			this.handler = handler;
		}

		/**
		 * Obtains the {@link Server}.
		 * 
		 * @return {@link Server}.
		 */
		public Server getServer() {
			return this.server;
		}

		/**
		 * Obtains the {@link ServletContextHandler}.
		 * 
		 * @return {@link ServletContextHandler}.
		 */
		public ServletContextHandler getHandler() {
			return this.handler;
		}
	}

	/**
	 * Configures the {@link Server}.
	 * 
	 * @param context {@link ServerContext}.
	 * @throws Exception If fails to configure {@link Server}.
	 */
	protected abstract void configureServer(ServerContext context) throws Exception;

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
		handler.addFilter(new FilterHolder(new FixHeadersFilter(this.getServerName())), "/*",
				EnumSet.of(DispatcherType.REQUEST));
		this.configureServer(new ServerContext(server, handler));
		server.setHandler(handler);

		// Provide wrapping OfficeFloor (to manage server)
		CompileOfficeFloor serverCompiler = new CompileOfficeFloor();
		serverCompiler.officeFloor(
				(context) -> context.getOfficeFloorDeployer().addOfficeFloorListener(new OfficeFloorListener() {
					@Override
					public void officeFloorOpened(OfficeFloorEvent event) throws Exception {
						MockServerSettings.runWithinContext(officeFloorExtension, officeExtension,
								() -> server.start());
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
	protected Server startRawHttpServer(HttpServerLocation serverLocation) throws Exception {
		Server server = createServer(serverLocation, null);
		FixHeadersFilter fixHeaders = new FixHeadersFilter(this.getServerName());
		byte[] helloWorld = "hello world".getBytes(Charset.forName("UTF-8"));
		server.setHandler(new AbstractHandler() {
			@Override
			public void handle(String target, Request baseRequest, HttpServletRequest request,
					HttpServletResponse response) throws IOException, ServletException {
				response.setStatus(HttpServletResponse.SC_OK);
				fixHeaders.loadFixHeaders(response);
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
		 * Name of the server.
		 */
		private final String serverName;

		/**
		 * Initiate.
		 * 
		 * @param serverName Name of the server.
		 */
		private FixHeadersFilter(String serverName) {
			this.serverName = serverName;
		}

		/**
		 * Loads the fix {@link org.eclipse.jetty.http.HttpHeader} instances.
		 * 
		 * @param response {@link HttpServletResponse}.
		 */
		protected void loadFixHeaders(HttpServletResponse response) {
			response.setHeader("Date", "NOW");
			response.setHeader("Server", this.serverName);
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
