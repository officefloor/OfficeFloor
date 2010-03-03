/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2010 Daniel Sagenschneider
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

package net.officefloor.compile.impl.officefloor;

import java.io.IOException;
import java.util.Properties;

import net.officefloor.compile.properties.Property;
import net.officefloor.compile.spi.officefloor.source.OfficeFloorSource;
import net.officefloor.compile.spi.officefloor.source.OfficeFloorSourceContext;
import net.officefloor.compile.spi.officefloor.source.RequiredProperties;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.model.repository.ConfigurationItem;

/**
 * Tests the {@link OfficeFloorSourceContext} when loading the
 * {@link OfficeFloor}.
 * 
 * @author Daniel Sagenschneider
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
				+ MakerOfficeFloorSource.class.getName()
				+ " by default constructor", failure);

		// Attempt to instantiate
		MakerOfficeFloorSource.instantiateFailure = failure;
		this.loadOfficeFloor(false, null);
	}

	/**
	 * Ensure obtain the correct {@link OfficeFloor} location.
	 */
	public void testOfficeFloorLocation() {
		this.loadOfficeFloor(true, new OfficeFloorMaker() {
			@Override
			public void make(OfficeFloorMakerContext context) {
				assertEquals("Incorrect office location",
						OFFICE_FLOOR_LOCATION, context.getContext()
								.getOfficeFloorLocation());
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
		this.record_officefloor_issue(
				"Failure obtaining configuration 'LOCATION'", failure);

		// Attempt to obtain the configuration item
		this.loadOfficeFloor(false, new OfficeFloorMaker() {
			@Override
			public void make(OfficeFloorMakerContext context) {
				context.getContext().getConfiguration(location);
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
		this.loadOfficeFloor(true, new OfficeFloorMaker() {
			@Override
			public void make(OfficeFloorMakerContext context) {
				assertEquals("Incorrect configuation item", item, context
						.getContext().getConfiguration(location));
			}
		});
	}

	/**
	 * Ensure issue if missing {@link Property}.
	 */
	public void testMissingProperty() {

		// Record missing property
		this
				.record_officefloor_issue("Missing property 'missing' for OfficeFloorSource "
						+ MakerOfficeFloorSource.class.getName());

		// Attempt to load office floor
		this.loadOfficeFloor(false, new OfficeFloorMaker() {
			@Override
			public void make(OfficeFloorMakerContext context) {
				context.getContext().getProperty("missing");
			}
		});
	}

	/**
	 * Ensure able to get properties.
	 */
	public void testGetProperties() {

		// Attempt to load office floor
		this.loadOfficeFloor(true, new OfficeFloorMaker() {
			@Override
			public void make(OfficeFloorMakerContext makerContext) {
				OfficeFloorSourceContext context = makerContext.getContext();
				assertEquals("Ensure get defaulted property", "DEFAULT",
						context.getProperty("missing", "DEFAULT"));
				assertEquals("Ensure get property ONE", "1", context
						.getProperty("ONE"));
				assertEquals("Ensure get property TWO", "2", context
						.getProperty("TWO"));
				String[] names = context.getPropertyNames();
				assertEquals("Incorrect number of property names", 3,
						names.length);
				assertEquals("Incorrect property name 0", "ONE", names[0]);
				assertEquals("Incorrect property name 1", "TWO", names[1]);
				assertEquals("Incorrect identifier",
						MakerOfficeFloorSource.MAKER_IDENTIFIER_PROPERTY_NAME,
						names[2]);
				Properties properties = context.getProperties();
				assertEquals("Incorrect number of properties", 3, properties
						.size());
				assertEquals("Incorrect property ONE", "1", properties
						.get("ONE"));
				assertEquals("Incorrect property TWO", "2", properties
						.get("TWO"));
				assertNotNull(
						"Incorrect identifier",
						properties
								.get(MakerOfficeFloorSource.MAKER_IDENTIFIER_PROPERTY_NAME));
			}
		}, "ONE", "1", "TWO", "2");
	}

	/**
	 * Ensure able to get the {@link ClassLoader}.
	 */
	public void testGetClassLoader() {

		// Attempt to load office floor
		this.loadOfficeFloor(true, new OfficeFloorMaker() {
			@Override
			public void make(OfficeFloorMakerContext context) {
				assertEquals("Incorrect class loader",
						LoadRequiredPropertiesTest.class.getClassLoader(),
						context.getContext().getClassLoader());
			}
		});
	}

	/**
	 * Ensure issue if fails to source the {@link RequiredProperties}.
	 */
	public void testFailSourceOfficeFloor() {

		final NullPointerException failure = new NullPointerException(
				"Fail source office floor");

		// Record failure to source the office floor
		this.record_officefloor_issue(
				"Failed to source OfficeFloor from OfficeFloorSource (source="
						+ MakerOfficeFloorSource.class.getName()
						+ ", location=" + OFFICE_FLOOR_LOCATION + ")", failure);

		// Attempt to load office floor
		this.loadOfficeFloor(false, new OfficeFloorMaker() {
			@Override
			public void make(OfficeFloorMakerContext context) {
				throw failure;
			}
		});
	}

}