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
package net.officefloor.managedobjects;

import java.io.File;

import net.officefloor.LoaderContext;
import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.frame.api.OfficeFrame;
import net.officefloor.frame.api.build.BuilderFactory;
import net.officefloor.frame.api.execute.Handler;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.impl.OfficeFrameImpl;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.repository.ConfigurationItem;
import net.officefloor.repository.filesystem.FileSystemConfigurationContext;

/**
 * Ensure able to configure a
 * {@link net.officefloor.frame.spi.managedobject.source.ManagedObjectSource}.
 * 
 * @author Daniel
 */
public class ManagedObjectSourceTest extends OfficeFrameTestCase {

	/**
	 * {@link OfficeFloor}.
	 */
	private OfficeFloor officeFloor;

	/**
	 * {@link TestManagedObjectSource} for verifying loaded correctly.
	 */
	private TestManagedObjectSource mos;

	/*
	 * (non-Javadoc)
	 * 
	 * @see junit.framework.TestCase#setUp()
	 */
	@Override
	protected void setUp() throws Exception {

		// Clear the previous office floors
		((OfficeFrameImpl) OfficeFrame.getInstance()).clearOfficeFloors();

		// Obtain the office floor configuration file
		File officeFloorConfigFile = this.findFile(this.getClass(),
				"TestManagedObjects.officefloor.xml");

		// Create the configuration context
		FileSystemConfigurationContext context = new FileSystemConfigurationContext(
				officeFloorConfigFile.getParentFile());

		// Obtain the configuration item for the office floor
		ConfigurationItem configuration = context
				.getConfigurationItem(officeFloorConfigFile.getName());

		// Create the Office Floor
		BuilderFactory builderFactory = OfficeFrame.getInstance()
				.getBuilderFactory();
		LoaderContext loaderContext = new LoaderContext(this.getClass()
				.getClassLoader());
		this.officeFloor = new OfficeFloorCompiler().compileOfficeFloor(
				configuration, builderFactory, loaderContext);

		// Open the Office Floor
		this.officeFloor.openOfficeFloor();

		// Obtain the Managed Object
		// TODO obtain managed object externally by name rather than Id
		ManagedObject managedObject = this.officeFloor.getOffice("office")
				.getManagedObject("MO-ID");

		// Obtain the object (downcast for use)
		this.mos = (TestManagedObjectSource) managedObject.getObject();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see junit.framework.TestCase#tearDown()
	 */
	@Override
	protected void tearDown() throws Exception {
		// Close the office floor if created
		if (this.officeFloor != null) {
			this.officeFloor.closeOfficeFloor();
		}
	}

	/**
	 * Ensures properties are made available.
	 */
	public void testProperties() throws Exception {
		// Validate the properties
		assertEquals("Incorrect property", "TEST PROPERTY", this.mos.properties
				.getProperty("PROPERTY"));
	}

	/**
	 * Ensure correctly invokes {@link ManagedObjectSource} added
	 * {@link Handler}.
	 */
	public void testManagedObjectSourceAddedHandler() throws Exception {
		// Ensure added handler is available
		assertEquals("Incorrect handler", "ADDED", this.mos.addedHandler
				.getHandlerId());
	}
}
