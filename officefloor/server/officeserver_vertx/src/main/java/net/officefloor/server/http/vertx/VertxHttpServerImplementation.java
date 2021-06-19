/*-
 * #%L
 * Vertx HTTP Server
 * %%
 * Copyright (C) 2005 - 2021 Daniel Sagenschneider
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

package net.officefloor.server.http.vertx;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.SSLContext;

import io.netty.handler.ssl.ClientAuth;
import io.netty.handler.ssl.JdkSslContext;
import io.netty.handler.ssl.SslContext;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.net.impl.SSLHelper;
import io.vertx.core.net.impl.TCPServerBase;
import net.officefloor.compile.spi.officefloor.ExternalServiceInput;
import net.officefloor.frame.api.build.OfficeFloorEvent;
import net.officefloor.frame.api.build.OfficeFloorListener;
import net.officefloor.frame.api.source.ServiceContext;
import net.officefloor.server.http.HttpHeaderValue;
import net.officefloor.server.http.HttpServerImplementation;
import net.officefloor.server.http.HttpServerImplementationContext;
import net.officefloor.server.http.HttpServerImplementationFactory;
import net.officefloor.server.http.HttpServerLocation;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.server.http.impl.ProcessAwareServerHttpConnectionManagedObject;
import net.officefloor.vertx.OfficeFloorVertx;

/**
 * {@link Vertx} {@link HttpServerImplementation}.
 * 
 * @author Daniel Sagenschneider
 */
public class VertxHttpServerImplementation
		implements HttpServerImplementation, HttpServerImplementationFactory, OfficeFloorListener {

	/**
	 * {@link HttpServerImplementationContext}.
	 */
	private HttpServerImplementationContext context;

	/**
	 * {@link ExternalServiceInput}.
	 */
	@SuppressWarnings("rawtypes")
	private ExternalServiceInput<ServerHttpConnection, ProcessAwareServerHttpConnectionManagedObject> serviceInput;

	/**
	 * {@link Vertx}.
	 */
	private Vertx vertx;

	/*
	 * ===================== HttpServerImplementationFactory =====================
	 */

	@Override
	public HttpServerImplementation createService(ServiceContext context) throws Throwable {
		return this;
	}

	/*
	 * ======================== HttpServerImplementation =========================
	 */

	@Override
	public void configureHttpServer(HttpServerImplementationContext context) throws Exception {
		this.context = context;

		// Obtain the service input for handling requests
		this.serviceInput = context.getExternalServiceInput(ProcessAwareServerHttpConnectionManagedObject.class,
				ProcessAwareServerHttpConnectionManagedObject.getCleanupEscalationHandler());

		// Register to start Vertx on OfficeFloor start
		context.getOfficeFloorDeployer().addOfficeFloorListener(this);
	}

	/*
	 * =========================== OfficeFloorListener =============================
	 */

	@Override
	public void officeFloorOpened(OfficeFloorEvent event) throws Exception {

		// Establish vertx server
		this.vertx = OfficeFloorVertx.getVertx();

		// Obtain the server location
		HttpServerLocation serverLocation = this.context.getHttpServerLocation();

		// Create the handler
		HttpHeaderValue serverName = net.officefloor.server.http.HttpServer.getServerHttpHeaderValue(context, "Vertx");
		OfficeFloorVertxHandler handler = new OfficeFloorVertxHandler(serverLocation, serverName,
				this.context.getDateHttpHeaderClock(), this.context.isIncludeEscalationStackTrace(), this.serviceInput);

		// Start the HTTP server
		int httpPort = serverLocation.getHttpPort();
		OfficeFloorVertx.block(this.vertx.createHttpServer().requestHandler(handler).listen(httpPort));

		// Determine if start HTTPS server
		int httpsPort = serverLocation.getHttpsPort();
		if (httpsPort > 0) {

			// Start the HTTPS server
			SSLContext sslContext = this.context.getSslContext();
			SslContext nettyContext = new JdkSslContext(sslContext, false, null,
					(requestedCiphers, defaultCiphers, supportedCiphers) -> {
						if (requestedCiphers == null) {
							return defaultCiphers.toArray(String[]::new);
						} else {
							List<String> ciphers = new ArrayList<>(defaultCiphers.size());
							requestedCiphers.forEach((cipher) -> {
								if (defaultCiphers.contains(cipher)) {
									ciphers.add(cipher);
								}
							});
							return ciphers.toArray(String[]::new);
						}
					}, null, ClientAuth.NONE, new String[] { sslContext.getProtocol() }, true);

			// Create the server
			HttpServer httpsServer = this.vertx.createHttpServer(new HttpServerOptions().setSsl(true))
					.requestHandler(handler);

			// Specify the SSL context
			Field sslHelperField = TCPServerBase.class.getDeclaredField("sslHelper");
			sslHelperField.setAccessible(true);
			SSLHelper sslHelper = (SSLHelper) sslHelperField.get(httpsServer);
			Field sslContextField = SSLHelper.class.getDeclaredField("sslContext");
			sslContextField.setAccessible(true);
			sslContextField.set(sslHelper, nettyContext);

			// Start the HTTPS server
			OfficeFloorVertx.block(httpsServer.listen(httpsPort));
		}
	}

	@Override
	public void officeFloorClosed(OfficeFloorEvent event) throws Exception {
		if (this.vertx != null) {
			OfficeFloorVertx.block(this.vertx.close());
		}
	}

}
