/*-
 * #%L
 * AWS SAM HTTP Server
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

package net.officefloor.server.aws.sam;

import java.util.logging.Logger;

import com.amazonaws.services.lambda.runtime.RequestHandler;

import net.officefloor.compile.impl.ApplicationOfficeFloorSource;
import net.officefloor.compile.spi.officefloor.DeployedOffice;
import net.officefloor.compile.spi.officefloor.ExternalServiceInput;
import net.officefloor.compile.spi.officefloor.OfficeFloorDeployer;
import net.officefloor.compile.spi.officefloor.OfficeFloorTeam;
import net.officefloor.frame.api.source.ServiceContext;
import net.officefloor.frame.impl.spi.team.ThreadLocalAwareTeamSource;
import net.officefloor.server.http.HttpServerImplementation;
import net.officefloor.server.http.HttpServerImplementationContext;
import net.officefloor.server.http.HttpServerImplementationFactory;
import net.officefloor.server.http.HttpServerLocation;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.server.http.impl.ProcessAwareServerHttpConnectionManagedObject;

/**
 * AWS SAM {@link HttpServerImplementation}.
 * 
 * @author Daniel Sagenschneider
 */
public class SamHttpServerImplementation implements HttpServerImplementation, HttpServerImplementationFactory {

	/**
	 * Name of the {@link ThreadLocalAwareTeamSource} to provide synchronous
	 * blocking servicing to work within {@link RequestHandler}.
	 */
	public static final String SYNC_TEAM_NAME = "_sam_sync_team_";

	/**
	 * {@link ThreadLocal} capture of {@link SamHttpServerImplementation}.
	 */
	private static final ThreadLocal<SamHttpServerImplementation> captureSamHttpServerImplementation = new ThreadLocal<>();

	/**
	 * Obtains the {@link SamHttpServerImplementation}.
	 * 
	 * @return {@link SamHttpServerImplementation}.
	 */
	public static SamHttpServerImplementation getSamHttpServerImplementation() {
		try {
			return captureSamHttpServerImplementation.get();
		} finally {
			// Ensure remove from thread context
			captureSamHttpServerImplementation.remove();
		}
	}

	/**
	 * {@link HttpServerLocation}.
	 */
	private HttpServerLocation location;

	/**
	 * {@link ExternalServiceInput} to service the AWS API event.
	 */
	@SuppressWarnings("rawtypes")
	private ExternalServiceInput<ServerHttpConnection, ProcessAwareServerHttpConnectionManagedObject> input;

	/**
	 * Indicates if include stack trace in HTTP responses.
	 */
	private boolean isIncludeEscalationStackTrace;

	/**
	 * {@link Logger}.
	 */
	private Logger logger;

	/**
	 * Obtains the {@link HttpServerLocation}.
	 * 
	 * @return {@link HttpServerLocation}.
	 */
	public HttpServerLocation getHttpServerLocation() {
		return this.location;
	}

	/**
	 * Obtains the {@link ExternalServiceInput} to service the AWS API event.
	 * 
	 * @return {@link ExternalServiceInput} to service the AWS API event.
	 */
	@SuppressWarnings("rawtypes")
	public ExternalServiceInput<ServerHttpConnection, ProcessAwareServerHttpConnectionManagedObject> getInput() {
		return this.input;
	}

	/**
	 * Indicates if include stack trace in HTTP responses.
	 * 
	 * @return <code>true</code> to include stack trace.
	 */
	public boolean isIncludeEscalationStackTrace() {
		return this.isIncludeEscalationStackTrace;
	}

	/**
	 * Obtains the {@link Logger}.
	 * 
	 * @return {@link Logger}.
	 */
	public Logger getLogger() {
		return this.logger;
	}

	/*
	 * ==================== HttpServerImplementation ===================
	 */

	@Override
	public HttpServerImplementation createService(ServiceContext context) throws Throwable {
		return this;
	}

	@Override
	public void configureHttpServer(HttpServerImplementationContext context) throws Exception {

		// Obtain the server details
		this.location = context.getHttpServerLocation();
		this.input = context.getExternalServiceInput(ProcessAwareServerHttpConnectionManagedObject.class,
				ProcessAwareServerHttpConnectionManagedObject.getCleanupEscalationHandler());
		this.isIncludeEscalationStackTrace = context.isIncludeEscalationStackTrace();
		this.logger = context.getOfficeFloorSourceContext().getLogger();

		// Capture this
		captureSamHttpServerImplementation.set(this);

		// Register thread local aware team (to block invoking thread until serviced)
		OfficeFloorDeployer deployer = context.getOfficeFloorDeployer();
		OfficeFloorTeam team = deployer.addTeam(SYNC_TEAM_NAME, new ThreadLocalAwareTeamSource());
		team.requestNoTeamOversight();

		// Register team to the office
		DeployedOffice office = deployer.getDeployedOffice(ApplicationOfficeFloorSource.OFFICE_NAME);
		deployer.link(office.getDeployedOfficeTeam(SYNC_TEAM_NAME), team);
	}

}
