/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package net.officefloor.server.http;

import javax.net.ssl.SSLContext;

import net.officefloor.compile.spi.officefloor.DeployedOfficeInput;
import net.officefloor.compile.spi.officefloor.ExternalServiceCleanupEscalationHandler;
import net.officefloor.compile.spi.officefloor.ExternalServiceInput;
import net.officefloor.compile.spi.officefloor.OfficeFloorDeployer;
import net.officefloor.compile.spi.officefloor.source.OfficeFloorSourceContext;
import net.officefloor.frame.api.escalate.Escalation;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.server.http.impl.DateHttpHeaderClock;

/**
 * Context for the {@link HttpServerImplementation}.
 * 
 * @author Daniel Sagenschneider
 */
public interface HttpServerImplementationContext {

	/**
	 * Obtains the {@link HttpServerLocation}.
	 * 
	 * @return {@link HttpServerLocation}.
	 */
	HttpServerLocation getHttpServerLocation();

	/**
	 * <p>
	 * Obtains the <code>Server</code> {@link HttpHeader} name.
	 * <p>
	 * {@link HttpServerImplementation} implementations may append to this name to
	 * indicate their use.
	 * 
	 * @return <code>Server</code> {@link HttpHeader} name. May be <code>null</code>
	 *         to not send {@link HttpHeader}.
	 */
	String getServerName();

	/**
	 * Obtains the {@link DateHttpHeaderClock} for the <code>Date</code>
	 * {@link HttpHeader}.
	 * 
	 * @return {@link DateHttpHeaderClock}. May be <code>null</code> to not send
	 *         {@link HttpHeader}.
	 */
	DateHttpHeaderClock getDateHttpHeaderClock();

	/**
	 * <p>
	 * Indicates whether the {@link HttpServerImplementation} should include the
	 * stack trace in {@link Escalation} responses.
	 * <p>
	 * For development, stack traces should be enabled for easier debugging of
	 * issues. However, in production, they should be hidden.
	 * 
	 * @return <code>true</code> to include the stack traces. <code>false</code>
	 *         should hide stack traces.
	 */
	boolean isIncludeEscalationStackTrace();

	/**
	 * <p>
	 * Obtains the {@link SSLContext} to use for HTTPS.
	 * <p>
	 * Should the {@link HttpServer} be behind a Reverse Proxy providing SSL, then
	 * this may be <code>null</code> if the Reverse Proxy is communicating via
	 * non-secure HTTP.
	 * 
	 * @return {@link SSLContext} to use for HTTPS. May be <code>null</code> if
	 *         behind Reverse Proxy handling SSL (with communication from Reverse
	 *         Proxy over non-secure HTTP).
	 * @throws Exception If fails to create the {@link SSLContext}.
	 */
	SSLContext getSslContext() throws Exception;

	/**
	 * Obtains the {@link DeployedOfficeInput} for internal invoked servicing.
	 * 
	 * @return {@link DeployedOfficeInput} for internal invoked servicing.
	 */
	DeployedOfficeInput getInternalServiceInput();

	/**
	 * Obtains the {@link ExternalServiceInput}.
	 * 
	 * @param                          <M> {@link ManagedObject} type.
	 * @param managedObjectType        Type of the {@link ManagedObject}.
	 * @param cleanupEscalationHandler {@link ExternalServiceCleanupEscalationHandler}.
	 * @return {@link ExternalServiceInput}.
	 */
	<M extends ManagedObject> ExternalServiceInput<ServerHttpConnection, M> getExternalServiceInput(
			Class<M> managedObjectType, ExternalServiceCleanupEscalationHandler<? super M> cleanupEscalationHandler);

	/**
	 * Obtains the {@link OfficeFloorDeployer} to configure the {@link HttpServer}.
	 * 
	 * @return {@link OfficeFloorDeployer} to configure the {@link HttpServer}.
	 */
	OfficeFloorDeployer getOfficeFloorDeployer();

	/**
	 * Obtains the {@link OfficeFloorSourceContext} to obtain details to configure
	 * the {@link HttpServerImplementation}.
	 * 
	 * @return {@link OfficeFloorSourceContext} to obtain details to configure the
	 *         {@link HttpServerImplementation}.
	 */
	OfficeFloorSourceContext getOfficeFloorSourceContext();

}