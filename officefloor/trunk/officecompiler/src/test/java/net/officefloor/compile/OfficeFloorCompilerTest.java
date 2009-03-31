/*
 *  Office Floor, Application Server
 *  Copyright (C) 2006 Daniel Sagenschneider
 *
 *  This program is free software; you can redistribute it and/or modify it under the terms 
 *  of the GNU General Public License as published by the Free Software Foundation; either 
 *  version 2 of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with this program; 
 *  if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, 
 *  MA 02111-1307 USA
 */
package net.officefloor.compile;

import java.io.File;

import net.officefloor.frame.api.OfficeFrame;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.manage.WorkManager;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.model.impl.repository.filesystem.FileSystemConfigurationContext;
import net.officefloor.model.repository.ConfigurationItem;

/**
 * Tests the building and running of an
 * {@link net.officefloor.frame.api.manage.OfficeFloor}.
 * 
 * @author Daniel
 */
public class OfficeFloorCompilerTest extends OfficeFrameTestCase {

	/**
	 * Ensure able to build and run an
	 * {@link net.officefloor.frame.api.manage.OfficeFloor}.
	 */
	public void testBuildingAndRunningAnOfficeFloor() throws Exception {

		// Obtain the office floor configuration file
		File officeFloorConfigFile = this.findFile(this.getClass(),
				"TestOfficeFloor.officefloor.xml");

		// Create the configuration context
		FileSystemConfigurationContext configContext = new FileSystemConfigurationContext(
				officeFloorConfigFile.getParentFile());

		// Obtain the configuration item for the office floor
		ConfigurationItem configItem = configContext
				.getConfigurationItem(officeFloorConfigFile.getName());

		// Compile the Office Floor
		OfficeFloorCompiler compiler = new OfficeFloorCompiler();
		OfficeFloor officeFloor = compiler.compileOfficeFloor(configItem,
				OfficeFrame.getInstance(), new LoaderContext(this.getClass()
						.getClassLoader()));

		// Open the office floor
		officeFloor.openOfficeFloor();

		// Invoke the work
		Office office = officeFloor.getOffice("office");
		WorkManager workManager = office.getWorkManager("1.work");
		workManager.invokeWork(null);

		// Close the office floor
		officeFloor.closeOfficeFloor();

		// Validate task one run
		assertTrue("taskOne not run", MockWorkFunctionality.isTaskOneInvoked);
	}

}
