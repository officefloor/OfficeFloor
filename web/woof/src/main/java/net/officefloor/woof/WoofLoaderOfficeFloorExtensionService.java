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
import java.util.function.BiFunction;
import java.util.function.Function;

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
import net.officefloor.web.rest.build.RestEmployer;
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

			// Determine if load
			if (!configuration.isLoad()) {
				continue NEXT_OFFICE;
			}

			// Load the additional profiles for the application (needed before override properties)
			if (configuration.isLoadAdditionalProfiles()) {
				String[] additionalProfiles = configuration.getAdditionalProfiles(context);
				for (String additionalProfile : additionalProfiles) {
					office.addAdditionalProfile(additionalProfile);
				}
			}

			// Load the override properties for the application
			Properties overrideProperties = null;
			if (configuration.isLoadOverrideProperties()) {
				overrideProperties = configuration.getOverrideProperties(context, context);
				for (String propertyName : overrideProperties.stringPropertyNames()) {
					String propertyValue = overrideProperties.getProperty(propertyName);
					office.addOverrideProperty(propertyName, propertyValue);
				}
			}

			// Function to obtain office property
			final Properties finalOverrideProperties = overrideProperties;
			BiFunction<String, String, String> getOfficeProperty = (propertyName, defaultValue) -> {

				// Determine if override property
				if (finalOverrideProperties != null) {
					String propertyValue = finalOverrideProperties.getProperty(propertyName);
					if ((propertyValue != null) && (!propertyValue.isEmpty())) {
						return propertyValue;
					}
				}

				// Determine from Office prefixed property
				return context.getProperty(officeName + "." + propertyName, defaultValue);
			};

			// Obtain the REST directory
			String officeFloorDirectory = getOfficeProperty.apply(WoofLoaderOfficeExtensionService.OFFICE_FLOOR_DIRECTORY_PROPERTY, WoofLoaderOfficeExtensionService.OFFICE_FLOOR_DEFAULT_DIRECTORY);
			String restDirectory = WoofLoaderOfficeExtensionService.interpolateRestDirectory(officeFloorDirectory, getOfficeProperty.apply(WoofLoaderOfficeExtensionService.REST_DIRECTORY_PROPERTY, WoofLoaderOfficeExtensionService.REST_DEFAULT_DIRECTORY));

			// Determine if WoOF application
			if ((!configuration.isWoofApplication(context)) && (!RestEmployer.isRestAvailable(restDirectory))) {
				return; // not WoOF application
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
