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
package net.officefloor.tutorial.featureapp;

import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import junit.framework.TestCase;
import net.officefloor.autowire.AutoWireManagement;
import net.officefloor.frame.api.OfficeFrame;
import net.officefloor.plugin.woof.WoofOfficeFloorSource;

/**
 * Test to run within Eclipse IDE.
 * 
 * @author Daniel Sagenschneider
 */
public class RunFeatureAppTest extends TestCase {

	public void testRun() throws Exception {

		// Stop all other offices
		AutoWireManagement.closeAllOfficeFloors();

		// Determine if waiting
		boolean isWait = ("wait".equals(System.getProperty("block.test")));

		// Provide logging of jobs
		Logger logger = Logger.getLogger(OfficeFrame.class.getName());
		Level logLevel = logger.getLevel();
		Handler logHandler = new ConsoleHandler();
		logHandler.setLevel(Level.FINER);
		try {
			if (isWait) {
				logger.setLevel(Level.FINER);
				logger.addHandler(logHandler);
			}
			
			// Start
			WoofOfficeFloorSource.main();

			// Wait to stop
			if (isWait) {
				System.out.print("Press enter to finish");
				System.out.flush();
				System.in.read();
			}

		} finally {
			// Reset logging level
			if (isWait) {
				logger.setLevel(logLevel);
				logger.removeHandler(logHandler);
			}
		}
	}

	@Override
	protected void tearDown() throws Exception {
		// Stop the office
		AutoWireManagement.closeAllOfficeFloors();
	}

}