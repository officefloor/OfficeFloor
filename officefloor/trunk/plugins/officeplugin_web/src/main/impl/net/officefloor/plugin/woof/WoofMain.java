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

import net.officefloor.model.impl.repository.ModelRepositoryImpl;
import net.officefloor.model.impl.repository.classloader.ClassLoaderConfigurationContext;
import net.officefloor.model.repository.ConfigurationContext;
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
public class WoofMain extends HttpServerAutoWireOfficeFloorSource {

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
		run(new WoofMain());
	}

	/**
	 * Configures and runs the {@link HttpServerAutoWireApplication}.
	 * 
	 * @param application
	 *            {@link WoofMain}.
	 * @throws Exception
	 *             If fails to run.
	 */
	public static void run(WoofMain application) throws Exception {

		// Obtain the WoOF location
		String woofLocation = System.getProperty(
				PROPERTY_WOOF_CONFIGURATION_LOCATION,
				DEFAULT_WOOF_CONFIGUARTION_LOCATION);

		// Obtain objects to load configuration
		ClassLoader classLoader = Thread.currentThread()
				.getContextClassLoader();
		ConfigurationContext context = new ClassLoaderConfigurationContext(
				classLoader);
		WoofRepository repository = new WoofRepositoryImpl(
				new ModelRepositoryImpl());

		// Load the WoOF configuration to the application
		new WoofLoaderImpl(classLoader, context, repository)
				.loadWoofConfiguration(woofLocation, application);

		// Providing additional configuration
		application.configure(application);

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

}