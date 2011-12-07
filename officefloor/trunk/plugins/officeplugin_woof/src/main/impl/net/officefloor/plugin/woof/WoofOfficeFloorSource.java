/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2011 Daniel Sagenschneider
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

import net.officefloor.compile.spi.officefloor.OfficeFloorDeployer;
import net.officefloor.compile.spi.officefloor.source.OfficeFloorSourceContext;
import net.officefloor.frame.api.build.OfficeFloorIssues.AssetType;
import net.officefloor.model.impl.repository.ModelRepositoryImpl;
import net.officefloor.model.impl.repository.classloader.ClassLoaderConfigurationContext;
import net.officefloor.model.repository.ConfigurationContext;
import net.officefloor.model.repository.ConfigurationItem;
import net.officefloor.model.woof.WoofModel;
import net.officefloor.model.woof.WoofRepository;
import net.officefloor.model.woof.WoofRepositoryImpl;
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

		// Obtain the WoOF location
		String woofLocation = context.getProperty(
				PROPERTY_WOOF_CONFIGURATION_LOCATION,
				DEFAULT_WOOF_CONFIGUARTION_LOCATION);

		// Obtain the woof configuration (ensuring exists)
		ClassLoader classLoader = this.getOfficeFloorCompiler()
				.getClassLoader();
		ConfigurationContext configurationContext = new ClassLoaderConfigurationContext(
				classLoader);
		ConfigurationItem configuration = configurationContext
				.getConfigurationItem(woofLocation);
		if (configuration == null) {
			deployer.addIssue("Can not find WoOF configuration file '"
					+ woofLocation + "'", AssetType.OFFICE_FLOOR, "WoOF");
			return; // must have WoOF configuration
		}

		// Load the WoOF configuration to the application
		WoofRepository repository = new WoofRepositoryImpl(
				new ModelRepositoryImpl());
		new WoofLoaderImpl(repository).loadWoofConfiguration(configuration,
				this);

		// Providing additional configuration
		this.configure(this);

		// Initialise parent
		super.initOfficeFloor(deployer, context);
	}

}