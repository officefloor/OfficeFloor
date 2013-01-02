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
import net.officefloor.building.command.officefloor.OpenOfficeFloorCommand;
import net.officefloor.building.console.OfficeFloorConsole;
import net.officefloor.building.console.OfficeFloorConsoleFactory;
import net.officefloor.building.console.OfficeFloorConsoleImpl;
import net.officefloor.building.console.OfficeFloorConsoleMain;
import net.officefloor.building.decorate.OfficeFloorDecorator;
import net.officefloor.building.decorate.OfficeFloorDecoratorServiceLoader;
import net.officefloor.frame.api.manage.OfficeFloor;

/**
 * {@link OfficeFloorConsoleFactory} to open the {@link OfficeFloor}.
 * 
 * @author Daniel Sagenschneider
 */
public final class OpenOfficeFloor implements OfficeFloorConsoleFactory {

	/**
	 * <p>
	 * Convenience entrance point for just opening an {@link OfficeFloor}.
	 * <p>
	 * This does not configure the environment from
	 * {@link OfficeFloorConsoleMain#OFFICE_FLOOR_HOME}.
	 * 
	 * @param arguments
	 *            Arguments to open the {@link OfficeFloor}.
	 * @throws Throwable
	 *             If fails to open the {@link OfficeFloor}.
	 */
	public static void main(String[] arguments) throws Throwable {

		// Obtain the environment
		Properties environment = new Properties();
		environment.putAll(System.getProperties());

		// Create the console
		OfficeFloorConsole console = new OpenOfficeFloor()
				.createOfficeFloorConsole(
						OpenOfficeFloor.class.getSimpleName(), environment);

		// Open the OfficeFloor
		console.run(System.out, System.err, null, null, arguments);
	}

	/**
	 * Indicates if to open the {@link OfficeFloor} within a spawned
	 * {@link Process}.
	 */
	private final boolean isOpenInSpawnedProcess;

	/**
	 * <p>
	 * Default constructor necessary for using as an
	 * {@link OfficeFloorConsoleFactory}.
	 * <p>
	 * Using this construction will not have the {@link OfficeFloor} open within
	 * a spawned {@link Process}.
	 */
	public OpenOfficeFloor() {
		this(false);
	}

	/**
	 * Initiate.
	 * 
	 * @param isOpenInSpawnedProcess
	 *            <code>true</code> if to open the {@link OfficeFloor} within a
	 *            spawned {@link Process}.
	 */
	public OpenOfficeFloor(boolean isOpenInSpawnedProcess) {
		this.isOpenInSpawnedProcess = isOpenInSpawnedProcess;
	}

	/*
	 * ==================== OfficeFloorConsoleFactory ===================
	 */

	@Override
	public OfficeFloorConsole createOfficeFloorConsole(String scriptName,
			Properties environment) throws Exception {

		// Only able to open the OfficeFloor
		OfficeFloorCommandFactory[] commandFactories = new OfficeFloorCommandFactory[] { new OpenOfficeFloorCommand(
				this.isOpenInSpawnedProcess, !this.isOpenInSpawnedProcess) };

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