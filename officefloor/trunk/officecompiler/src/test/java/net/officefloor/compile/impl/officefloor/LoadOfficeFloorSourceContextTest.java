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
package net.officefloor.compile.impl.officefloor;

import java.io.IOException;
import java.util.Properties;

import net.officefloor.compile.properties.Property;
import net.officefloor.compile.spi.officefloor.OfficeFloorDeployer;
import net.officefloor.compile.spi.officefloor.source.OfficeFloorSource;
import net.officefloor.compile.spi.officefloor.source.OfficeFloorSourceContext;
import net.officefloor.compile.spi.officefloor.source.RequiredProperties;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.model.repository.ConfigurationItem;

/**
 * Tests the {@link OfficeFloorSourceContext} when loading the
 * {@link OfficeFloor}.
 * 
 * @author Daniel
 */
public class LoadOfficeFloorSourceContextTest extends
		AbstractOfficeFloorTestCase {

	/**
	 * Ensure issue if fail to instantiate the {@link OfficeFloorSource}.
	 */
	public void testFailInstantiate() {

		final RuntimeException failure = new RuntimeException(
				"instantiate failure");

		// Record failure to instantiate
		this.record_officefloor_issue("Failed to instantiate "
				+ MockOfficeFloorSource.class.getName()
				+ " by default constructor", failure);

		// Attempt to instantiate
		MockOfficeFloorSource.instantiateFailure = failure;
		this.loadOfficeFloor(false, null);
	}

	/**
	 * Ensure obtain the correct {@link OfficeFloor} location.
	 */
	public void testOfficeFloorLocation() {
		this.loadOfficeFloor(true, new Loader() {
			@Override
			public void loadOfficeFloor(OfficeFloorDeployer deployer,
					OfficeFloorSourceContext context) throws Exception {
				assertEquals("Incorrect office location",
						OFFICE_FLOOR_LOCATION, context.getOfficeFloorLocation());
			}
		});
	}

	/**
	 * Ensures issue if fails to obtain the {@link ConfigurationItem}.
	 */
	public void testFailGetConfigurationItem() throws Exception {

		final String location = "LOCATION";
		final IOException failure = new IOException(
				"Configuration Item failure");

		// Record failing to obtain the configuration item
		this.control(this.configurationContext).expectAndThrow(
				this.configurationContext.getConfigurationItem(location),
				failure);
		this
				.record_officefloor_issue("Failure obtaining configuration 'LOCATION'",
						failure);

		// Attempt to obtain the configuration item
		this.loadOfficeFloor(false, new Loader() {
			@Override
			public void loadOfficeFloor(OfficeFloorDeployer deployer,
					OfficeFloorSourceContext context) throws Exception {
				context.getConfiguration(location);
			}
		});
	}

	/**
	 * Ensure able to obtain a {@link ConfigurationItem}.
	 */
	public void testGetConfigurationItem() throws Exception {

		final String location = "LOCATION";
		final ConfigurationItem item = this.createMock(ConfigurationItem.class);

		// Record obtaining the configuration item
		this.recordReturn(this.configurationContext, this.configurationContext
				.getConfigurationItem(location), item);

		// Obtain the configuration item
		this.loadOfficeFloor(true, new Loader() {
			@Override
			public void loadOfficeFloor(OfficeFloorDeployer deployer,
					OfficeFloorSourceContext context) throws Exception {
				assertEquals("Incorrect configuation item", item, context
						.getConfiguration(location));
			}
		});
	}

	/**
	 * Ensure issue if missing {@link Property}.
	 */
	public void testMissingProperty() {

		// Record missing property
		this.record_officefloor_issue("Missing property 'missing' for OfficeFloorSource "
				+ MockOfficeFloorSource.class.getName());

		// Attempt to load office floor
		this.loadOfficeFloor(false, new Loader() {
			@Override
			public void loadOfficeFloor(OfficeFloorDeployer deployer,
					OfficeFloorSourceContext context) throws Exception {
				context.getProperty("missing");
			}
		});
	}

	/**
	 * Ensure able to get properties.
	 */
	public void testGetProperties() {

		// Attempt to load office floor
		this.loadOfficeFloor(true, new Loader() {
			@Override
			public void loadOfficeFloor(
					OfficeFloorDeployer deployer,
					OfficeFloorSourceContext context) throws Exception {
				assertEquals("Ensure get defaulted property", "DEFAULT",
						context.getProperty("missing", "DEFAULT"));
				assertEquals("Ensure get property ONE", "1", context
						.getProperty("ONE"));
				assertEquals("Ensure get property TWO", "2", context
						.getProperty("TWO"));
				String[] names = context.getPropertyNames();
				assertEquals("Incorrect number of property names", 2,
						names.length);
				assertEquals("Incorrect property name 0", "ONE", names[0]);
				assertEquals("Incorrect property name 1", "TWO", names[1]);
				Properties properties = context.getProperties();
				assertEquals("Incorrect number of properties", 2, properties
						.size());
				assertEquals("Incorrect property ONE", "1", properties
						.get("ONE"));
				assertEquals("Incorrect property TWO", "2", properties
						.get("TWO"));
			}
		}, "ONE", "1", "TWO", "2");
	}

	/**
	 * Ensure able to get the {@link ClassLoader}.
	 */
	public void testGetClassLoader() {

		// Attempt to load office floor
		this.loadOfficeFloor(true, new Loader() {
			@Override
			public void loadOfficeFloor(OfficeFloorDeployer deployer,
					OfficeFloorSourceContext context) throws Exception {
				assertEquals("Incorrect class loader",
						LoadRequiredPropertiesTest.class.getClassLoader(),
						context.getClassLoader());
			}
		});
	}

	/**
	 * Ensure issue if fails to source the {@link RequiredProperties}.
	 */
	public void testFailSourceRequiredProperties() {

		final NullPointerException failure = new NullPointerException(
				"Fail source required properties");

		// Record failure to source the office floor
		this.record_officefloor_issue(
				"Failed to source required properties from OfficeFloorSource "
						+ MockOfficeFloorSource.class.getName(), failure);

		// Attempt to load office floor
		this.loadOfficeFloor(false, new Loader() {
			@Override
			public void loadOfficeFloor(OfficeFloorDeployer deployer,
					OfficeFloorSourceContext context) throws Exception {
				throw failure;
			}
		});
	}

}