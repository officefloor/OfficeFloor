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
 * Context for the {@link HttpServerImplementation}.
 * 
 * @author Daniel Sagenschneider
 */
public interface HttpServerImplementationContext {

	/**
	 * Obtains the HTTP port.
	 * 
	 * @return HTTP port.
	 */
	int getHttpPort();

	/**
	 * Obtains the HTTPS port.
	 * 
	 * @return HTTPS port. May be <code>-1</code> if separate HTTPS port is not
	 *         required.
	 */
	int getHttpsPort();

	/**
	 * <p>
	 * Obtains the {@link SSLContext} to use for HTTPS.
	 * <p>
	 * Should the {@link HttpServer} be behind a Reverse Proxy providing SSL,
	 * then this may be <code>null</code> if the Reverse Proxy is communicating
	 * via non-secure HTTP.
	 * 
	 * @return {@link SSLContext} to use for HTTPS. May be <code>null</code> if
	 *         behind Reverse Proxy handling SSL (with communication to Reverse
	 *         Proxy over non-secure HTTP).
	 */
	SSLContext getSslContext();

	/**
	 * Obtains the {@link DeployedOfficeInput} for internal invoked servicing.
	 * 
	 * @return {@link DeployedOfficeInput} for internal invoked servicing.
	 */
	DeployedOfficeInput getInternalServiceInput();

	/**
	 * Obtains the {@link ExternalServiceInput}.
	 * 
	 * @param managedObjectType
	 *            Type of the {@link ManagedObject}.
	 * @param cleanupEscalationHandler
	 *            {@link ExternalServiceCleanupEscalationHandler}.
	 * @return {@link ExternalServiceInput}.
	 */
	<M extends ManagedObject> ExternalServiceInput<ServerHttpConnection, M> getExternalServiceInput(
			Class<M> managedObjectType, ExternalServiceCleanupEscalationHandler<? super M> cleanupEscalationHandler);

	/**
	 * Obtains the {@link OfficeFloorDeployer} to configure the
	 * {@link HttpServer}.
	 * 
	 * @return {@link OfficeFloorDeployer} to configure the {@link HttpServer}.
	 */
	OfficeFloorDeployer getOfficeFloorDeployer();

	/**
	 * Obtains the {@link OfficeFloorSourceContext} to obtain details to
	 * configure the {@link HttpServerImplementation}.
	 * 
	 * @return {@link OfficeFloorSourceContext} to obtain details to configure
	 *         the {@link HttpServerImplementation}.
	 */
	OfficeFloorSourceContext getOfficeFloorSourceContext();

}