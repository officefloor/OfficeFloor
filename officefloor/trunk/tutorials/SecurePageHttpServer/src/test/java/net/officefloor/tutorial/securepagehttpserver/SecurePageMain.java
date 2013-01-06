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
package net.officefloor.tutorial.securepagehttpserver;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import net.officefloor.plugin.woof.WoofOfficeFloorSource;

/**
 * Main to run application.
 * 
 * @author Daniel Sagenschneider
 */
public class SecurePageMain {

	/**
	 * Enables running the application.
	 * 
	 * @param args
	 *            Command line arguments.
	 */
	public static void main(String... args) throws Exception {

		// Run
		WoofOfficeFloorSource.start(args);
		try {

			// Enter return to quit
			System.out.println("Press [enter] to exit");
			new BufferedReader(new InputStreamReader(System.in)).readLine();

		} finally {
			// Stop
			WoofOfficeFloorSource.stop();
		}
	}

}
