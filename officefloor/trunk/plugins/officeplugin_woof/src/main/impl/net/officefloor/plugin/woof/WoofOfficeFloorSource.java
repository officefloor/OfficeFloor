/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2012 Daniel Sagenschneider
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

package net.officefloor.plugin.woof;

import net.officefloor.autowire.AutoWireApplication;
import net.officefloor.compile.spi.officefloor.OfficeFloorDeployer;
import net.officefloor.compile.spi.officefloor.source.OfficeFloorSourceContext;
import net.officefloor.frame.api.build.OfficeFloorIssues.AssetType;
import net.officefloor.model.impl.repository.ModelRepositoryImpl;
import net.officefloor.model.impl.repository.classloader.ClassLoaderConfigurationContext;
import net.officefloor.model.objects.AutoWireObjectsRepositoryImpl;
import net.officefloor.model.repository.ConfigurationContext;
import net.officefloor.model.repository.ConfigurationItem;
import net.officefloor.model.teams.AutoWireTeamsRepositoryImpl;
import net.officefloor.model.woof.WoofModel;
import net.officefloor.model.woof.WoofRepositoryImpl;
import net.officefloor.plugin.objects.AutoWireObjectsLoader;
import net.officefloor.plugin.objects.AutoWireObjectsLoaderImpl;
import net.officefloor.plugin.teams.AutoWireTeamsLoader;
import net.officefloor.plugin.teams.AutoWireTeamsLoaderImpl;
import net.officefloor.plugin.web.http.server.HttpServerAutoWireApplication;
import net.officefloor.plugin.web.http.server.HttpServerAutoWireOfficeFloorSource;

/**
 * <code>main</code> class to run a {@link WoofModel} on a
 * {@link HttpServerAutoWireApplication}.
 * 
 * @author Daniel Sagenschneider
 */
public class WoofOfficeFloorSource extends HttpServerAutoWireOfficeFloorSource {

	/**
	 * Property for the location of the WoOF configuration for the application.
	 */
	public static final String PROPERTY_WOOF_CONFIGURATION_LOCATION = "woof.configuration.location";

	/**
	 * Default WoOF configuration location.
	 */
	public static final String DEFAULT_WOOF_CONFIGUARTION_LOCATION = "application.woof";

	/**
	 * Property for the location of the Objects configuration for the
	 * application.
	 */
	public static final String PROPERTY_OBJECTS_CONFIGURATION_LOCATION = "objects.configuration.location";

	/**
	 * Default Objects configuration location.
	 */
	public static final String DEFAULT_OBJECTS_CONFIGURATION_LOCATION = "application.objects";

	/**
	 * Property for the location of the Teams configuration for the application.
	 */
	public static final String PROPERTY_TEAMS_CONFIGURATION_LOCATION = "teams.configuration.location";

	/**
	 * Default Teams configuration location.
	 */
	public static final String DEFAULT_TEAMS_CONFIGURATION_LOCATION = "application.teams";

	/**
	 * <code>main</code> to run the {@link WoofModel}.
	 * 
	 * @param args
	 *            Command line arguments.
	 * @throws Exception
	 *             If fails to run.
	 */
	public static void main(String... args) throws Exception {
		run(new WoofOfficeFloorSource());
	}

	/**
	 * Configures and runs the {@link HttpServerAutoWireApplication}.
	 * 
	 * @param application
	 *            {@link WoofOfficeFloorSource}.
	 * @throws Exception
	 *             If fails to run.
	 */
	public static void run(WoofOfficeFloorSource application) throws Exception {
		// Start the application
		application.openOfficeFloor();
	}

	/**
	 * Loads the optional configuration.
	 * 
	 * @param application
	 *            {@link AutoWireApplication}.
	 * @param objectsLocation
	 *            Location of the Objects {@link ConfigurationItem}.
	 * @param teamsLocation
	 *            Location of the Teams {@link ConfigurationItem}.
	 * @param configurationContext
	 *            {@link ConfigurationContext}.
	 * @throws Exception
	 *             If fails to load the optional configuration.
	 */
	public static void loadOptionalConfiguration(
			AutoWireApplication application, String objectsLocation,
			String teamsLocation, ConfigurationContext configurationContext)
			throws Exception {

		// Load the optional objects configuration to the application
		ConfigurationItem objectsConfiguration = configurationContext
				.getConfigurationItem(objectsLocation);
		if (objectsConfiguration != null) {
			// Load the objects configuration
			AutoWireObjectsLoader objectsLoader = new AutoWireObjectsLoaderImpl(
					new AutoWireObjectsRepositoryImpl(new ModelRepositoryImpl()));
			objectsLoader.loadAutoWireObjectsConfiguration(
					objectsConfiguration, application);
		}

		// Load the optional teams configuration to the application
		ConfigurationItem teamsConfiguration = configurationContext
				.getConfigurationItem(teamsLocation);
		if (teamsConfiguration != null) {
			// Load the teams configuration
			AutoWireTeamsLoader teamsLoader = new AutoWireTeamsLoaderImpl(
					new AutoWireTeamsRepositoryImpl(new ModelRepositoryImpl()));
			teamsLoader.loadAutoWireTeamsConfiguration(teamsConfiguration,
					application);
		}
	}

	/**
	 * Allows overriding to provide additional configuration.
	 * 
	 * @param application
	 *            {@link HttpServerAutoWireOfficeFloorSource}.
	 */
	protected void configure(HttpServerAutoWireOfficeFloorSource application) {
		// No additional configuration by default
	}

	/*
	 * =================== AutoWireOfficeFloorSource ======================
	 */

	@Override
	protected void initOfficeFloor(OfficeFloorDeployer deployer,
			OfficeFloorSourceContext context) throws Exception {

		// Obtain the configuration context
		ClassLoader classLoader = this.getOfficeFloorCompiler()
				.getClassLoader();
		ConfigurationContext configurationContext = new ClassLoaderConfigurationContext(
				classLoader);

		// Obtain the woof configuration (ensuring exists)
		String woofLocation = context.getProperty(
				PROPERTY_WOOF_CONFIGURATION_LOCATION,
				DEFAULT_WOOF_CONFIGUARTION_LOCATION);
		ConfigurationItem woofConfiguration = configurationContext
				.getConfigurationItem(woofLocation);
		if (woofConfiguration == null) {
			deployer.addIssue("Can not find WoOF configuration file '"
					+ woofLocation + "'", AssetType.OFFICE_FLOOR, "WoOF");
			return; // must have WoOF configuration
		}

		// Load the WoOF configuration to the application
		WoofLoader woofLoader = new WoofLoaderImpl(new WoofRepositoryImpl(
				new ModelRepositoryImpl()));
		woofLoader.loadWoofConfiguration(woofConfiguration, this);

		// Load the optional configuration to the application
		String objectsLocation = context.getProperty(
				PROPERTY_OBJECTS_CONFIGURATION_LOCATION,
				DEFAULT_OBJECTS_CONFIGURATION_LOCATION);
		String teamsLocation = context.getProperty(
				PROPERTY_TEAMS_CONFIGURATION_LOCATION,
				DEFAULT_TEAMS_CONFIGURATION_LOCATION);
		loadOptionalConfiguration(this, objectsLocation, teamsLocation,
				configurationContext);

		// Providing additional configuration
		this.configure(this);

		// Initialise parent
		super.initOfficeFloor(deployer, context);
	}

}