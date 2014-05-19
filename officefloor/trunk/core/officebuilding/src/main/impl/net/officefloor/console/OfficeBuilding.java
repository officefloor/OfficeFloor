/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2013 Daniel Sagenschneider
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
import net.officefloor.building.command.officefloor.OfficeBuildingCloseOfficeFloorCommand;
import net.officefloor.building.command.officefloor.OfficeBuildingInvokeOfficeFloorCommand;
import net.officefloor.building.command.officefloor.OfficeBuildingListOfficeFloorCommand;
import net.officefloor.building.command.officefloor.OfficeBuildingOpenOfficeFloorCommand;
import net.officefloor.building.command.officefloor.OfficeBuildingUrlCommand;
import net.officefloor.building.command.officefloor.StartOfficeBuildingCommand;
import net.officefloor.building.command.officefloor.StopOfficeBuildingCommand;
import net.officefloor.building.console.OfficeFloorConsole;
import net.officefloor.building.console.OfficeFloorConsoleFactory;
import net.officefloor.building.console.OfficeFloorConsoleImpl;
import net.officefloor.building.decorate.OfficeFloorDecorator;
import net.officefloor.building.decorate.OfficeFloorDecoratorServiceLoader;
import net.officefloor.frame.api.manage.Office;

/**
 * {@link OfficeFloorConsoleFactory} for {@link Office} Building.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeBuilding implements OfficeFloorConsoleFactory {

	/*
	 * =================== OfficeFloorConsoleFactory ================
	 */

	@Override
	public OfficeFloorConsole createOfficeFloorConsole(String scriptName,
			Properties environment) throws Exception {

		// Create the listing of commands
		OfficeFloorCommandFactory[] commandFactories = new OfficeFloorCommandFactory[] {
				new StartOfficeBuildingCommand(environment),
				new OfficeBuildingUrlCommand(),
				new OfficeBuildingOpenOfficeFloorCommand(true, false),
				new OfficeBuildingListOfficeFloorCommand(),
				new OfficeBuildingInvokeOfficeFloorCommand(),
				new OfficeBuildingCloseOfficeFloorCommand(),
				new StopOfficeBuildingCommand() };

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