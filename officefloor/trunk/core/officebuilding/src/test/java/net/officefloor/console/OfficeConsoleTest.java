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

import net.officefloor.building.manager.OfficeBuildingManager;
import net.officefloor.building.manager.OfficeBuildingManagerMBean;
import net.officefloor.building.util.OfficeBuildingTestUtil;
import net.officefloor.frame.test.OfficeFrameTestCase;

import org.junit.Ignore;

import sun.tools.jconsole.OfficeConsole;

/**
 * Ensure able to connect to {@link OfficeBuildingManager} with similar
 * mechanism to JConsole.
 * 
 * @author Daniel Sagenschneider
 */
@Ignore("TODO tidy up OfficeConsole and provide automated test")
public class OfficeConsoleTest extends OfficeFrameTestCase {

	/**
	 * {@link OfficeBuildingManagerMBean}.
	 */
	private OfficeBuildingManagerMBean manager;

	@Override
	protected void tearDown() throws Exception {
		// Stop the office building
		if (this.manager != null) {
			this.manager.stopOfficeBuilding(1000);
		}
	}

	/**
	 * Ensure {@link OfficeConsole} can connect.
	 */
	public void _testOfficeConsole() throws Exception {

		// Start the Office Building
		this.manager = OfficeBuildingTestUtil.startOfficeBuilding(13778);

		// Start the console connected to the Office Building
		OfficeConsole.main(null);

		// TODO validate connected
	}

}