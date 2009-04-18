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
import net.officefloor.compile.spi.officefloor.source.OfficeFloorSource;
import net.officefloor.frame.api.OfficeFrame;
import net.officefloor.frame.api.build.OfficeBuilder;
import net.officefloor.frame.api.build.OfficeFloorBuilder;
import net.officefloor.frame.api.build.OfficeFloorIssues;
import net.officefloor.frame.api.build.TaskBuilder;
import net.officefloor.frame.api.build.TeamBuilder;
import net.officefloor.frame.api.build.WorkBuilder;
import net.officefloor.frame.api.execute.Work;
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
	 * Creates a mock {@link TeamBuilder}.
	 * 
	 * @return Mock {@link TeamBuilder}.
	 */
	@SuppressWarnings("unchecked")
	protected TeamBuilder<MakerTeamSource> createMockTeamBuilder() {
		return this.createMock(TeamBuilder.class);
	}

	/**
	 * Creates a mock {@link OfficeBuilder}.
	 * 
	 * @return Mock {@link OfficeBuilder}.
	 */
	protected OfficeBuilder createMockOfficeBuilder() {
		return this.createMock(OfficeBuilder.class);
	}

	/**
	 * Creates a mock {@link WorkBuilder}.
	 * 
	 * @return Mock {@link WorkBuilder}.
	 */
	@SuppressWarnings("unchecked")
	protected WorkBuilder<Work> createMockWorkBuilder() {
		return this.createMock(WorkBuilder.class);
	}

	/**
	 * Creates the mock {@link TaskBuilder}.
	 * 
	 * @return Mock {@link TaskBuilder}.
	 */
	@SuppressWarnings("unchecked")
	protected TaskBuilder<Work, ?, ?> createMockTaskBuilder() {
		return this.createMock(TaskBuilder.class);
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
	 * @param maker
	 *            {@link OfficeFloorMaker}.
	 * @param propertyNameValuePairs
	 *            Name/value pairs for the necessary {@link Property} instances
	 *            to load the {@link OfficeFloor}.
	 */
	protected void loadOfficeFloor(boolean isExpectBuild,
			OfficeFloorMaker maker, String... propertyNameValuePairs) {

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

		// Register the office floor maker and add to property list
		PropertyList registerProperties = MakerOfficeFloorSource
				.register(maker);
		for (Property property : registerProperties) {
			propertyList.addProperty(property.getName()).setValue(
					property.getValue());
		}

		// Create the office loader and load the office floor
		this.replayMockObjects();
		OfficeFloorLoader officeFloorLoader = new OfficeFloorLoaderImpl(
				OFFICE_FLOOR_LOCATION);
		OfficeFloor loadedOfficeFloor = officeFloorLoader.loadOfficeFloor(
				MakerOfficeFloorSource.class, this.configurationContext,
				propertyList,
				LoadRequiredPropertiesTest.class.getClassLoader(), this.issues,
				officeFrame);
		this.verifyMockObjects();

		// Ensure the correct loaded office floor
		if (isExpectBuild) {
			assertEquals("Incorrect built office floor", officeFloor,
					loadedOfficeFloor);
		} else {
			assertNull("Should not build the office floor", officeFloor);
		}
	}

}