/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2017 Daniel Sagenschneider
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

import javax.net.ssl.SSLContext;

import net.officefloor.compile.spi.officefloor.DeployedOfficeInput;
import net.officefloor.compile.spi.officefloor.ExternalServiceCleanupEscalationHandler;
import net.officefloor.compile.spi.officefloor.ExternalServiceInput;
import net.officefloor.compile.spi.officefloor.OfficeFloorDeployer;
import net.officefloor.compile.spi.officefloor.source.OfficeFloorSourceContext;
import net.officefloor.frame.api.managedobject.ManagedObject;

/**
 * HTTP Server.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpServer {

	/**
	 * Configures the {@link HttpServer}.
	 * 
	 * @param officeFloorDeployer
	 *            {@link OfficeFloorDeployer}.
	 * @param context
	 *            {@link OfficeFloorSourceContext}.
	 */
	public static void configureHttpServer(OfficeFloorDeployer officeFloorDeployer, OfficeFloorSourceContext context) {
		// TODO configure HTTP Server from properties
	}

	/**
	 * Configures the {@link HttpServer}.
	 * 
	 * @param httpPort
	 *            Port for HTTP traffic.
	 * @param httpsPort
	 *            Port for HTTPS traffic.
	 * @param implementation
	 *            {@link HttpServerImplementation}.
	 * @param sslContext
	 *            {@link SSLContext}.
	 * @param serviceInput
	 *            {@link DeployedOfficeInput} servicing the
	 *            {@link ServerHttpConnection}.
	 * @param officeFloorDeployer
	 *            {@link OfficeFloorDeployer}.
	 * @param context
	 *            {@link OfficeFloorSourceContext}.
	 */
	public static void configureHttpServer(int httpPort, int httpsPort, HttpServerImplementation implementation,
			SSLContext sslContext, DeployedOfficeInput serviceInput, OfficeFloorDeployer officeFloorDeployer,
			OfficeFloorSourceContext context) {

		// Configure the HTTP server
		implementation.configureHttpServer(new HttpServerImplementationContext() {

			/*
			 * ================= HttpServerImplementationContext ==============
			 */

			@Override
			public int getHttpPort() {
				return httpPort;
			}

			@Override
			public int getHttpsPort() {
				return httpsPort;
			}

			@Override
			public SSLContext getSslContext() {
				return sslContext;
			}

			@Override
			public DeployedOfficeInput getInternalServiceInput() {
				return serviceInput;
			}

			@Override
			public <M extends ManagedObject> ExternalServiceInput<ServerHttpConnection, M> getExternalServiceInput(
					Class<? extends M> managedObjectType,
					ExternalServiceCleanupEscalationHandler<? super M> cleanupEscalationHandler) {
				return serviceInput.addExternalServiceInput(ServerHttpConnection.class, managedObjectType,
						cleanupEscalationHandler);
			}

			@Override
			public OfficeFloorDeployer getOfficeFloorDeployer() {
				return officeFloorDeployer;
			}

			@Override
			public OfficeFloorSourceContext getOfficeFloorSourceContext() {
				return context;
			}
		});
	}

}