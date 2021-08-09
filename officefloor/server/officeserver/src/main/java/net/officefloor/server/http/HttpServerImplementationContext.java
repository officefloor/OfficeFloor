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

package net.officefloor.server.http;

import javax.net.ssl.SSLContext;

import net.officefloor.compile.spi.officefloor.DeployedOfficeInput;
import net.officefloor.compile.spi.officefloor.ExternalServiceCleanupEscalationHandler;
import net.officefloor.compile.spi.officefloor.ExternalServiceInput;
import net.officefloor.compile.spi.officefloor.OfficeFloorDeployer;
import net.officefloor.compile.spi.officefloor.source.OfficeFloorSourceContext;
import net.officefloor.frame.api.escalate.Escalation;
import net.officefloor.frame.api.managedobject.ManagedObject;

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
