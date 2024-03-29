/*-
 * #%L
 * HttpServlet adapter for OfficeFloor HTTP Server
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

package net.officefloor.server.http.servlet;

import jakarta.servlet.Filter;
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
 * {@link HttpServerImplementation} to validate tests.
 *
 * @author Daniel Sagenschneider
 */
public class HttpServletHttpServerImplementation implements HttpServerImplementation, HttpServerImplementationFactory {

	/**
	 * Name of the {@link ThreadLocalAwareTeamSource} to provide synchronous
	 * blocking servicing to work within {@link Filter} chain.
	 */
	public static final String SYNC_TEAM_NAME = "_servlet_sync_team_";

	/**
	 * {@link ThreadLocal} {@link Bridge}.
	 */
	private static final ThreadLocal<Bridge> threadLocalBridge = new ThreadLocal<>();

	/**
	 * Provides context to load the {@link HttpServletHttpServerImplementation}.
	 */
	@FunctionalInterface
	public static interface HttpServletOfficeFloorLoader {

		/**
		 * Loads.
		 * 
		 * @throws Exception If fails to load.
		 */
		void load() throws Exception;
	}

	/**
	 * Bridge.
	 */
	private static class Bridge {

		/**
		 * {@link HttpServletOfficeFloorBridge} to return.
		 */
		private HttpServletOfficeFloorBridge bridge = null;
	}

	/**
	 * Loads the {@link HttpServletHttpServerImplementation}.
	 * 
	 * @param loader {@link HttpServletOfficeFloorLoader}.
	 * @return {@link HttpServletOfficeFloorBridge}.
	 * @throws Exception If fails to load.
	 */
	public static HttpServletOfficeFloorBridge load(HttpServletOfficeFloorLoader loader) throws Exception {
		Bridge bridge = new Bridge();
		try {
			threadLocalBridge.set(bridge);
			loader.load();
			return bridge.bridge;
		} finally {
			threadLocalBridge.remove();
		}
	}

	/**
	 * Indicates if running within the {@link OfficeFloorFilter}.
	 * 
	 * @return <code>true</code> if running within {@link OfficeFloorFilter}.
	 */
	public static boolean isWithinOfficeFloorFilter() {
		return threadLocalBridge.get() != null;
	}

	/*
	 * ===================== HttpServerImplementation ====================
	 */

	@Override
	public HttpServerImplementation createService(ServiceContext context) throws Throwable {
		return this;
	}

	@Override
	@SuppressWarnings("rawtypes")
	public void configureHttpServer(HttpServerImplementationContext context) {

		// Ensure within context
		Bridge bridge = threadLocalBridge.get();
		if (bridge == null) {
			throw new IllegalStateException("Must load " + this.getClass().getSimpleName() + " within load context");
		}

		// Obtain the required details
		HttpServerLocation location = context.getHttpServerLocation();
		boolean isIncludeEscalationStackTrace = context.isIncludeEscalationStackTrace();

		// Create the input
		ExternalServiceInput<ServerHttpConnection, ProcessAwareServerHttpConnectionManagedObject> input = context
				.getExternalServiceInput(ProcessAwareServerHttpConnectionManagedObject.class,
						ProcessAwareServerHttpConnectionManagedObject.getCleanupEscalationHandler());

		// Load the bridge
		bridge.bridge = new HttpServletOfficeFloorBridge(location, isIncludeEscalationStackTrace, input);

		// Register thread local aware team (to block invoking thread until serviced)
		OfficeFloorDeployer deployer = context.getOfficeFloorDeployer();
		OfficeFloorTeam team = deployer.addTeam(SYNC_TEAM_NAME, new ThreadLocalAwareTeamSource());
		team.requestNoTeamOversight();

		// Register team to the office
		DeployedOffice office = deployer.getDeployedOffice(ApplicationOfficeFloorSource.OFFICE_NAME);
		deployer.link(office.getDeployedOfficeTeam(SYNC_TEAM_NAME), team);
	}

}
