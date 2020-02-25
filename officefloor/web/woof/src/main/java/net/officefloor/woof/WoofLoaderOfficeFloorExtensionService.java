/*-
 * #%L
 * Web on OfficeFloor
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package net.officefloor.woof;

import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import net.officefloor.compile.impl.util.CompileUtil;
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

			// Load the optional properties for the application
			if (configuration.isLoadProperties()) {
				Properties overrideProperties = new Properties();

				// Obtain the profiles
				String profilesValue = System.getProperty(WoOF.OFFICEFLOOR_PROFILES, "");
				List<String> profiles = new LinkedList<>();
				profiles.add(null); // always default properties first
				for (String profile : profilesValue.split(",")) {
					if (!CompileUtil.isBlank(profile)) {
						profiles.add(profile.trim()); // include profile
					}
				}

				// Load configuration properties for all profiles
				for (String profile : profiles) {
					ConfigurationItem propertiesConfiguration = configuration.getPropertiesConfiguration(profile,
							context);
					if (propertiesConfiguration != null) {

						// Load the properties
						overrideProperties.load(propertiesConfiguration.getInputStream());
					}
				}

				// Load only system properties for this office
				// (takes precedence over configuration properties)
				String officePrefix = officeName + ".";
				Properties systemProperties = System.getProperties();
				for (String systemPropertyName : systemProperties.stringPropertyNames()) {
					if (systemPropertyName.startsWith(officePrefix)) {
						String overridePropertyName = systemPropertyName.substring(officePrefix.length());
						String systemPropertyValue = systemProperties.getProperty(systemPropertyName);
						overrideProperties.setProperty(overridePropertyName, systemPropertyValue);
					}
				}

				// Load the override properties
				for (String propertyName : overrideProperties.stringPropertyNames()) {
					String propertyValue = overrideProperties.getProperty(propertyName);
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