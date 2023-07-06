/*-
 * #%L
 * Provides testing using HttpServlet
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

package net.officefloor.server.http.test;

import net.officefloor.compile.impl.ApplicationOfficeFloorSource;
import net.officefloor.compile.spi.office.extension.OfficeExtensionService;
import net.officefloor.compile.spi.officefloor.DeployedOffice;
import net.officefloor.compile.spi.officefloor.DeployedOfficeInput;
import net.officefloor.compile.spi.officefloor.extension.OfficeFloorExtensionService;
import net.officefloor.compile.spi.section.SectionInput;
import net.officefloor.server.http.HttpServer;
import net.officefloor.server.http.ServerHttpConnection;

/**
 * Enables running an external server.
 * 
 * @author Daniel Sagenschneider
 */
public class ExternalServerRunner {

	/**
	 * {@link OfficeFloorExtensionService}.
	 */
	static OfficeFloorExtensionService officeFloorExtensionService = null;

	/**
	 * {@link OfficeExtensionService}.
	 */
	static OfficeExtensionService officeExtensionService = null;

	/**
	 * Logic to run within context.
	 */
	@FunctionalInterface
	public static interface StartExternalServer {
		void startExternalServer() throws Exception;
	}

	/**
	 * All access via static methods.
	 */
	private ExternalServerRunner() {
	}

	/**
	 * Runs {@link StartExternalServer} logic.
	 * 
	 * @param sectionName          Name of section handling
	 *                             {@link ServerHttpConnection}.
	 * @param sectionInputName     Name of {@link SectionInput} handling
	 *                             {@link ServerHttpConnection}.
	 * @param officeFloorExtension {@link OfficeFloorExtensionService}.
	 * @param officeExtension      {@link OfficeExtensionService}.
	 * @param logic                {@link StartExternalServer} logic.
	 * @return {@link HttpServer}.
	 * @throws Exception If failure with logic.
	 */
	public static HttpServer startExternalServer(String sectionName, String sectionInputName,
			OfficeFloorExtensionService officeFloorExtension, OfficeExtensionService officeExtension,
			StartExternalServer logic) throws Exception {

		// Capture the HttpServer
		HttpServer[] httpServer = new HttpServer[1];

		// Create extension to load in external server
		OfficeFloorExtensionService createHttpServer = (officeFloorDeployer, context) -> {

			// Obtain the input to service the HTTP requests
			DeployedOffice office = officeFloorDeployer.getDeployedOffice(ApplicationOfficeFloorSource.OFFICE_NAME);
			DeployedOfficeInput officeInput = office.getDeployedOfficeInput(sectionName, sectionInputName);

			// Load the HTTP server
			httpServer[0] = new HttpServer(officeInput, officeFloorDeployer, context);

			// Undertake further configuration
			if (officeFloorExtension != null) {
				officeFloorExtension.extendOfficeFloor(officeFloorDeployer, context);
			}
		};

		// Start external server
		startExternalServer(createHttpServer, officeExtension, logic);

		// Return the HttpServer
		return httpServer[0];
	}

	/**
	 * Runs {@link StartExternalServer} logic.
	 * 
	 * @param officeFloorExtension {@link OfficeFloorExtensionService}.
	 * @param officeExtension      {@link OfficeExtensionService}.
	 * @param logic                {@link StartExternalServer} logic.
	 * @throws Exception If failure with logic.
	 */
	public static void startExternalServer(OfficeFloorExtensionService officeFloorExtension,
			OfficeExtensionService officeExtension, StartExternalServer logic) throws Exception {

		// Start the external server
		officeFloorExtensionService = officeFloorExtension;
		officeExtensionService = officeExtension;
		try {
			logic.startExternalServer();
		} finally {
			officeFloorExtensionService = null;
			officeExtensionService = null;
		}
	}

}
