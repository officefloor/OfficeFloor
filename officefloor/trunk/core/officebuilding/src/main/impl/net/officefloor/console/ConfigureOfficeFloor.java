/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2014 Daniel Sagenschneider
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
package net.officefloor.console;

import java.util.Properties;

import net.officefloor.building.command.OfficeFloorCommandFactory;
import net.officefloor.building.command.officefloor.OfficeBuildingOpenOfficeFloorCommand;
import net.officefloor.building.console.OfficeFloorConsole;
import net.officefloor.building.console.OfficeFloorConsoleFactory;
import net.officefloor.building.console.OfficeFloorConsoleImpl;
import net.officefloor.building.console.OfficeFloorConsoleMain;
import net.officefloor.building.console.OfficeFloorConsoleMain.OfficeFloorConsoleMainErrorHandler;
import net.officefloor.building.decorate.OfficeFloorDecorator;
import net.officefloor.building.decorate.OfficeFloorDecoratorServiceLoader;
import net.officefloor.building.manager.OpenOfficeFloorConfiguration;

/**
 * ConfigureOfficeFloor.java.
 * 
 * @author Daniel Sagenschneider
 */
public class ConfigureOfficeFloor implements OfficeFloorConsoleFactory {

	/**
	 * Convenience method to create a new {@link OpenOfficeFloorConfiguration}
	 * loaded with default configuration.
	 * 
	 * @param errorHandler
	 *            {@link OfficeFloorConsoleMainErrorHandler}.
	 * @return New {@link OpenOfficeFloorConfiguration}.
	 * @throws Exception
	 *             If fails to load the new {@link OpenOfficeFloorConfiguration}
	 *             with default configuration.
	 */
	public static OpenOfficeFloorConfiguration newOpenOfficeFloorConfiguration(
			OfficeFloorConsoleMainErrorHandler errorHandler) throws Exception {

		// Provides means to obtain the open OfficeFloor configuration
		ConfigureOfficeFloor configure = new ConfigureOfficeFloor();

		// Run the OfficeFloor console
		OfficeFloorConsoleMain.run(ConfigureOfficeFloor.class.getSimpleName(),
				new String[0], configure, errorHandler);

		// Return the open OfficeFloor configuration
		return configure.getOpenOfficeFloorConfiguration();
	}

	/**
	 * {@link OfficeBuildingOpenOfficeFloorCommand}.
	 */
	private OfficeBuildingOpenOfficeFloorCommand command = null;

	/**
	 * Obtains the {@link OpenOfficeFloorConfiguration}.
	 * 
	 * @return {@link OpenOfficeFloorConfiguration}.
	 * @throws Exception
	 *             If fails to obtain the {@link OpenOfficeFloorConfiguration}.
	 */
	public OpenOfficeFloorConfiguration getOpenOfficeFloorConfiguration()
			throws Exception {
		// Provide configuration from configured command
		return this.command.getOpenOfficeFloorConfiguration();
	}

	/*
	 * ===================== OfficeFloorConsoleFactory =========================
	 */

	@Override
	public OfficeFloorConsole createOfficeFloorConsole(String scriptName,
			Properties environment) throws Exception {

		// Create the command to be configured
		this.command = new OfficeBuildingOpenOfficeFloorCommand(false, true);

		// Only able to open the OfficeFloor
		OfficeFloorCommandFactory[] commandFactories = new OfficeFloorCommandFactory[] { this.command };

		// Obtain the decorators
		OfficeFloorDecorator[] decorators = OfficeFloorDecoratorServiceLoader
				.loadOfficeFloorDecorators(null);

		// Create the OfficeFloorConsole
		OfficeFloorConsole console = new OfficeFloorConsoleImpl(scriptName,
				commandFactories, environment, decorators);

		// Return the OfficeFloorConsole
		return console;
	}

}