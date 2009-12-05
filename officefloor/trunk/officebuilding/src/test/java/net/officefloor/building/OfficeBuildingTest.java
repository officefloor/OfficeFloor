/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2009 Daniel Sagenschneider
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
package net.officefloor.building;

import java.io.File;

import junit.framework.TestCase;
import net.officefloor.building.OfficeBuilding;

/**
 * Tests the {@link OfficeBuilding}.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeBuildingTest extends TestCase {

	@Override
	protected void setUp() throws Exception {
		// Flag that testing
		OfficeBuilding.isTesting = true;
	}

	/**
	 * Ensure can start the {@link OfficeBuilding}.
	 */
	public void testStartOfficeBuilding() throws Throwable {

		// Provide Office Building Home
		File officeBuildingHomeDir = new File(".", "src/main/resources");
		System.setProperty(OfficeBuilding.OFFICE_BUILDING_HOME,
				officeBuildingHomeDir.getAbsolutePath());

		// Start the Office Building
		OfficeBuilding.main("start");
	}

}