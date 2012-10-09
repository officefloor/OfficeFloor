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
package net.officefloor.demo;

import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.woof.WoofOfficeFloorSource;

/**
 * Test to run the application.
 * 
 * @author Daniel Sagenschneider
 */
public class RunDemoAppTest extends OfficeFrameTestCase {

	/**
	 * Runs the application for manual testing.
	 * 
	 * @param args
	 *            Command line arguments.
	 */
	public static void main(String[] args) throws Exception {

		// Run from appropriate webapp directory
		System.setProperty(WoofOfficeFloorSource.PROPERTY_WEBAPP_LOCATION,
				ServletHostDemoAppTest.findWebApDirectory().getAbsolutePath());

		// Run
		WoofOfficeFloorSource.start(args);

		try {
			// Wait to stop
			System.out.print("Press enter to finish");
			System.out.flush();
			System.in.read();

		} finally {
			// Stop
			WoofOfficeFloorSource.stop();
		}
	}

	/**
	 * Runs the application.
	 */
	public void testRun() throws Exception {
		// Start (always from default webapp directory)
		WoofOfficeFloorSource.start();
	}

	@Override
	protected void tearDown() throws Exception {
		// Stop
		WoofOfficeFloorSource.stop();
	}

}