/*-
 * #%L
 * Web on OfficeFloor
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

package net.officefloor.woof;

import java.util.Properties;

import net.officefloor.compile.spi.officefloor.DeployedOffice;
import net.officefloor.compile.spi.officefloor.DeployedOfficeInput;
import net.officefloor.compile.spi.officefloor.OfficeFloorDeployer;
import net.officefloor.compile.spi.officefloor.extension.OfficeFloorExtensionContext;
import net.officefloor.compile.spi.officefloor.extension.OfficeFloorExtensionService;
import net.officefloor.compile.spi.officefloor.extension.OfficeFloorExtensionServiceFactory;
import net.officefloor.configuration.ConfigurationItem;
import net.officefloor.frame.api.source.ServiceContext;
import net.officefloor.model.impl.repository.ModelRepositoryImpl;
import net.officefloor.server.http.HttpServer;
import net.officefloor.web.build.WebArchitect;
import net.officefloor.woof.WoofLoaderSettings.WoofLoaderConfiguration;
import net.officefloor.woof.model.teams.WoofTeamsRepositoryImpl;
import net.officefloor.woof.teams.WoofTeamsLoader;
import net.officefloor.woof.teams.WoofTeamsLoaderContext;
import net.officefloor.woof.teams.WoofTeamsLoaderImpl;

/**
 * {@link OfficeFloorExtensionService} for the {@link WoofLoader}.
 * 
 * @author Daniel Sagenschneider
 */
public class WoofLoaderOfficeFloorExtensionService
		implements OfficeFloorExtensionService, OfficeFloorExtensionServiceFactory {

	/*
	 * ============= OfficeFloorExtensionServiceFactory =================
	 */

	@Override
	public OfficeFloorExtensionService createService(ServiceContext context) throws Throwable {
		return this;
	}

	/*
	 * ================= OfficeFloorExtensionService ===================
	 */

	@Override
	public void extendOfficeFloor(OfficeFloorDeployer officeFloorDeployer, OfficeFloorExtensionContext context)
			throws Exception {

		// Configure each Office
		NEXT_OFFICE: for (DeployedOffice office : officeFloorDeployer.getDeployedOffices()) {

			// Obtain the WoOF loader configuration
			String officeName = office.getDeployedOfficeName();
			WoofLoaderConfiguration configuration = WoofLoaderSettings.getWoofLoaderConfiguration(officeName);

			// Determine if WoOF application
			if (!configuration.isWoofApplication(context)) {
				continue NEXT_OFFICE; // not WoOF application
			}

			// Load the HTTP Server
			if (configuration.isLoadHttpServer()) {

				// Obtain the input to service the HTTP requests
				DeployedOfficeInput officeInput = office.getDeployedOfficeInput(WebArchitect.HANDLER_SECTION_NAME,
						WebArchitect.HANDLER_INPUT_NAME);

				// Load the HTTP server
				HttpServer server = new HttpServer(officeInput, officeFloorDeployer, context);

				// Indicate the implementation of HTTP server
				context.getLogger().info("HTTP server implementation "
						+ server.getHttpServerImplementation().getClass().getSimpleName());
			}

			// Indicate loading WoOF
			if (!configuration.isContextualLoad()) {
				context.getLogger().info("Extending Office " + officeName + " with WoOF");
			}

			// Load the additional profiles for the application
			if (configuration.isLoadAdditionalProfiles()) {

				// Load the additional profiles
				String[] additionalProfiles = configuration.getAdditionalProfiles(context);
				for (String additionalProfile : additionalProfiles) {
					office.addAdditionalProfile(additionalProfile);
				}
			}

			// Load the override properties for the application
			if (configuration.isLoadOverrideProperties()) {

				// Load the override properties
				Properties properties = configuration.getOverrideProperties(context, context);
				for (String propertyName : properties.stringPropertyNames()) {
					String propertyValue = properties.getProperty(propertyName);
					office.addOverrideProperty(propertyName, propertyValue);
				}
			}

			// Load the optional teams configuration for the application
			if (configuration.isLoadTeams()) {
				ConfigurationItem teamsConfiguration = configuration.getTeamsConfiguration(context);
				if (teamsConfiguration != null) {

					// Indicate loading teams
					if (!configuration.isContextualLoad()) {
						context.getLogger().info("Loading WoOF teams");
					}

					// Load the teams configuration
					WoofTeamsLoader teamsLoader = new WoofTeamsLoaderImpl(
							new WoofTeamsRepositoryImpl(new ModelRepositoryImpl()));
					teamsLoader.loadWoofTeamsConfiguration(new WoofTeamsLoaderContext() {

						@Override
						public OfficeFloorExtensionContext getOfficeFloorExtensionContext() {
							return context;
						}

						@Override
						public OfficeFloorDeployer getOfficeFloorDeployer() {
							return officeFloorDeployer;
						}

						@Override
						public ConfigurationItem getConfiguration() {
							return teamsConfiguration;
						}

						@Override
						public DeployedOffice getDeployedOffice() {
							return office;
						}
					});
				}
			}
		}
	}

}
