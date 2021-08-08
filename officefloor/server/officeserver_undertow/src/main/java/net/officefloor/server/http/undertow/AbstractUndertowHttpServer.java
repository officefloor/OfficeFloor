/*-
 * #%L
 * Undertow HTTP Server
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

package net.officefloor.server.http.undertow;

import javax.net.ssl.SSLContext;

import io.undertow.Undertow;
import io.undertow.UndertowOptions;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import net.officefloor.frame.api.manage.ProcessManager;

/**
 * Abstract Undertow HTTP server.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class AbstractUndertowHttpServer {

	/**
	 * {@link Undertow} server.
	 */
	private Undertow server;

	/**
	 * Starts the HTTP Server.
	 * 
	 * @param httpPort   HTTP port.
	 * @param httpsPort  HTTPS secure port.
	 * @param sslContext {@link SSLContext}. May be <code>null</code>.
	 * @throws Exception If fails to start the HTTP Server.
	 */
	public void startHttpServer(int httpPort, int httpsPort, SSLContext sslContext) throws Exception {

		// Bind to all addresses
		String hostName = "0.0.0.0";

		// Start the Undertow server
		Undertow.Builder builder = Undertow.builder().addHttpListener(httpPort, hostName);

		// Add possible HTTPS listener
		if (httpsPort > 0) {
			builder = sslContext != null ? builder.addHttpsListener(httpsPort, hostName, sslContext)
					: builder.addHttpListener(httpPort, hostName);
		}

		// Do not always set values
		builder.setServerOption(UndertowOptions.ALWAYS_SET_KEEP_ALIVE, false);
		builder.setServerOption(UndertowOptions.ALWAYS_SET_DATE, false);

		// Add the handler and build the server
		this.server = builder.setHandler(new HttpHandler() {
			@Override
			public void handleRequest(HttpServerExchange exchange) throws Exception {
				AbstractUndertowHttpServer.this.service(exchange);
			}
		}).build();
		this.server.start();
	}

	/**
	 * Stops the HTTP Server.
	 */
	public void stopHttpServer() {
		if (this.server != null) {
			this.server.stop();
		}
	}

	/**
	 * Services the {@link HttpServerExchange}.
	 * 
	 * @param exchange {@link HttpServerExchange}.
	 * @return {@link ProcessManager}.
	 * @throws Exception If fails to service the {@link HttpServerExchange}.
	 */
	protected abstract ProcessManager service(HttpServerExchange exchange) throws Exception;

}
