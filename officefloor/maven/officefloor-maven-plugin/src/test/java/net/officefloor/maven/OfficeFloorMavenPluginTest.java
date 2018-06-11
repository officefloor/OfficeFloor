/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
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
package net.officefloor.maven;

import java.io.File;

import org.apache.maven.plugin.testing.AbstractMojoTestCase;

import net.officefloor.frame.api.manage.OfficeFloor;

/**
 * Tests the {@link OpenOfficeFloorMojo} and {@link CloseOfficeFloorMojo}.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeFloorMavenPluginTest extends AbstractMojoTestCase {

	/**
	 * Ensure can open and close the {@link OfficeFloor}.
	 */
	public void testOpenCloseOfficeFloor() throws Exception {

		// Open the OfficeFloor
		OpenOfficeFloorMojo open = (OpenOfficeFloorMojo) this.lookupMojo("open",
				new File(getBasedir(), "src/test/resources/test-open-pom.xml"));
		open.execute();

		// Ensure can access OfficeFloor
		// TODO access OfficeFloor

		// Close the OfficeFloor
		CloseOfficeFloorMojo close = (CloseOfficeFloorMojo) this.lookupMojo("close",
				new File(getBasedir(), "src/test/resources/test-close-pom.xml"));
		close.execute();

		// Ensure no further access to OfficeFloor
		// TODO ensure no access to OfficeFloor
	}

}
