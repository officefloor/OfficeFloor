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

import net.officefloor.compile.impl.properties.PropertyListImpl;
import net.officefloor.compile.impl.structure.AbstractStructureTestCase;
import net.officefloor.compile.issues.CompilerIssues;
import net.officefloor.compile.issues.CompilerIssues.LocationType;
import net.officefloor.compile.officefloor.OfficeFloorLoader;
import net.officefloor.compile.properties.Property;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.officefloor.OfficeFloorDeployer;
import net.officefloor.compile.spi.officefloor.source.OfficeFloorSource;
import net.officefloor.compile.spi.officefloor.source.OfficeFloorSourceContext;
import net.officefloor.compile.spi.officefloor.source.OfficeFloorSourceSpecification;
import net.officefloor.compile.spi.officefloor.source.RequiredProperties;
import net.officefloor.frame.api.OfficeFrame;
import net.officefloor.frame.api.build.OfficeFloorBuilder;
import net.officefloor.frame.api.build.OfficeFloorIssues;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.test.match.TypeMatcher;
import net.officefloor.model.repository.ConfigurationContext;

/**
 * Tests loading the {@link OfficeFloor} via the {@link OfficeFloorLoader} from
 * the {@link OfficeFloorSource}.
 * 
 * @author Daniel
 */
public abstract class AbstractOfficeFloorTestCase extends
		AbstractStructureTestCase {

	/**
	 * Location of the {@link OfficeFloor}.
	 */
	protected final String OFFICE_FLOOR_LOCATION = "OFFICE_FLOOR";

	/**
	 * {@link ConfigurationContext}.
	 */
	protected final ConfigurationContext configurationContext = this
			.createMock(ConfigurationContext.class);

	/**
	 * {@link CompilerIssues}.
	 */
	protected final CompilerIssues issues = this
			.createMock(CompilerIssues.class);

	/**
	 * {@link OfficeFloorBuilder}.
	 */
	protected final OfficeFloorBuilder officeFloorBuilder = this
			.createMock(OfficeFloorBuilder.class);

	/**
	 * Initiate this test.
	 */
	public AbstractOfficeFloorTestCase() {
		MockOfficeFloorSource.reset();
	}

	/**
	 * Records an issue.
	 * 
	 * @param issueDescription
	 *            Description of the issue.
	 */
	protected void record_officefloor_issue(String issueDescription) {
		this.issues.addIssue(LocationType.OFFICE_FLOOR, OFFICE_FLOOR_LOCATION,
				null, null, issueDescription);
	}

	/**
	 * Records an issue.
	 * 
	 * @param issueDescription
	 *            Description of the issue.
	 * @param cause
	 *            Cause of the issue.
	 */
	protected void record_officefloor_issue(String issueDescription,
			Throwable cause) {
		this.issues.addIssue(LocationType.OFFICE_FLOOR, OFFICE_FLOOR_LOCATION,
				null, null, issueDescription, cause);
	}

	/**
	 * Records an issue.
	 * 
	 * @param issueDescription
	 *            Description of the issue.
	 * @param officeLocation
	 *            Location of the {@link Office}.
	 */
	protected void record_office_issue(String issueDescription,
			String officeLocation) {
		this.issues.addIssue(LocationType.OFFICE, officeLocation, null, null,
				issueDescription);
	}

	/**
	 * Records an issue.
	 * 
	 * @param issueDescription
	 *            Description of the issue.
	 * @param cause
	 *            Cause of the issue.
	 * @param officeLocation
	 *            Location of the {@link Office}.
	 */
	protected void record_office_issue(String issueDescription,
			Throwable cause, String officeLocation) {
		this.issues.addIssue(LocationType.OFFICE, officeLocation, null, null,
				issueDescription, cause);
	}

	/**
	 * Loads the {@link OfficeFloor}.
	 * 
	 * @param isExpectBuild
	 *            If expected to build the {@link OfficeFloor}.
	 * @param loader
	 *            {@link Loader}.
	 * @param propertyNameValuePairs
	 *            Name/value pairs for the necessary {@link Property} instances
	 *            to load the {@link OfficeFloor}.
	 */
	protected void loadOfficeFloor(boolean isExpectBuild, Loader loader,
			String... propertyNameValuePairs) {

		// Office floor potentially built
		OfficeFloor officeFloor = null;

		// Record building if expected to build office floor
		if (isExpectBuild) {
			// Create the mock office floor built
			officeFloor = this.createMock(OfficeFloor.class);

			// Record successfully building the office floor
			this.recordReturn(this.officeFloorBuilder, this.officeFloorBuilder
					.buildOfficeFloor(null), officeFloor, new TypeMatcher(
					OfficeFloorIssues.class));
		}

		// Replay mock objects
		this.replayMockObjects();

		// Create the property list
		PropertyList propertyList = new PropertyListImpl();
		for (int i = 0; i < propertyNameValuePairs.length; i += 2) {
			String name = propertyNameValuePairs[i];
			String value = propertyNameValuePairs[i + 1];
			propertyList.addProperty(name).setValue(value);
		}

		// Create the office frame to return mock office floor builder
		OfficeFrame officeFrame = new OfficeFrame() {
			@Override
			public OfficeFloorBuilder createOfficeFloorBuilder(
					String officeFloorName) {
				return AbstractOfficeFloorTestCase.this.officeFloorBuilder;
			}
		};

		// Create the office loader and load the office floor
		OfficeFloorLoader officeFloorLoader = new OfficeFloorLoaderImpl(
				OFFICE_FLOOR_LOCATION);
		MockOfficeFloorSource.loader = loader;
		OfficeFloor loadedOfficeFloor = officeFloorLoader.loadOfficeFloor(
				MockOfficeFloorSource.class, this.configurationContext,
				propertyList,
				LoadRequiredPropertiesTest.class.getClassLoader(), this.issues,
				officeFrame);

		// Verify the mock objects
		this.verifyMockObjects();

		// Ensure the correct loaded office floor
		if (isExpectBuild) {
			assertEquals("Incorrect built office floor", officeFloor,
					loadedOfficeFloor);
		} else {
			assertNull("Should not build the office floor", officeFloor);
		}
	}

	/**
	 * Implemented to load the {@link OfficeFloor}.
	 */
	protected interface Loader {

		/**
		 * Implemented to load the {@link OfficeFloor}.
		 * 
		 * @param deployer
		 *            {@link OfficeFloorDeployer}.
		 * @param context
		 *            {@link OfficeFloorSourceContext}.
		 * @throws Exception
		 *             If fails to load the {@link OfficeFloor}.
		 */
		void loadOfficeFloor(OfficeFloorDeployer deployer,
				OfficeFloorSourceContext context) throws Exception;
	}

	/**
	 * Mock {@link OfficeFloorSource} for testing.
	 */
	public static class MockOfficeFloorSource implements OfficeFloorSource {

		/**
		 * {@link Loader} to load the {@link RequiredProperties}.
		 */
		public static Loader loader;

		/**
		 * Failure in instantiating an instance.
		 */
		public static RuntimeException instantiateFailure;

		/**
		 * Resets the state for the next test.
		 */
		public static void reset() {
			loader = null;
			instantiateFailure = null;
		}

		/**
		 * Default constructor.
		 */
		public MockOfficeFloorSource() {
			if (instantiateFailure != null) {
				throw instantiateFailure;
			}
		}

		/*
		 * ================ OfficeFloorSource ==============================
		 */

		@Override
		public OfficeFloorSourceSpecification getSpecification() {
			fail("Should not required specification");
			return null;
		}

		@Override
		public void specifyConfigurationProperties(
				RequiredProperties requiredProperties,
				OfficeFloorSourceContext context) throws Exception {
			fail("Should not require initialising");
		}

		@Override
		public void sourceOfficeFloor(OfficeFloorDeployer deployer,
				OfficeFloorSourceContext context) throws Exception {
			// Load the office floor
			loader.loadOfficeFloor(deployer, context);
		}
	}

}