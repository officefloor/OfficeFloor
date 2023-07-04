/*-
 * #%L
 * Vertx HTTP Server
 * %%
 * Copyright (C) 2005 - 2021 Daniel Sagenschneider
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

package net.officefloor.server.http.vertx;

import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.SSLContext;

import io.netty.handler.ssl.ClientAuth;
import io.netty.handler.ssl.JdkSslContext;
import io.netty.handler.ssl.SslContext;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.net.JdkSSLEngineOptions;
import io.vertx.core.net.JksOptions;
import io.vertx.core.spi.tls.SslContextFactory;
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

			// Create the SSL Context
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

			// Create the HTTP Server options to use the SSL Context
			JdkSSLEngineOptions engineOptions = new JdkSSLEngineOptions() {
				
				@Override
				public JdkSSLEngineOptions copy() {
					// Avoid copy to use SSL Context override
					return this;
				}

				@Override
				public SslContextFactory sslContextFactory() {
					// Supply the SSL Context
					return () -> nettyContext;
				}
			};
			HttpServerOptions serverOptions = new HttpServerOptions().setSsl(true).setSslEngineOptions(engineOptions);
			serverOptions.getSslOptions().setKeyCertOptions(new JksOptions());

			// Create the server
			HttpServer httpsServer = this.vertx.createHttpServer(serverOptions);

			// Configure handler
			httpsServer = httpsServer.requestHandler(handler);

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
