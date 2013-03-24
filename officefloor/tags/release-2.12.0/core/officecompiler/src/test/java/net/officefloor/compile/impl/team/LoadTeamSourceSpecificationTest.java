/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2013 Daniel Sagenschneider
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
package net.officefloor.compile.impl.team;

import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.compile.issues.CompilerIssues;
import net.officefloor.compile.issues.CompilerIssues.LocationType;
import net.officefloor.compile.properties.Property;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.team.TeamLoader;
import net.officefloor.compile.test.properties.PropertyListUtil;
import net.officefloor.frame.api.build.OfficeFloorIssues.AssetType;
import net.officefloor.frame.spi.TestSource;
import net.officefloor.frame.spi.team.Team;
import net.officefloor.frame.spi.team.source.TeamSource;
import net.officefloor.frame.spi.team.source.TeamSourceContext;
import net.officefloor.frame.spi.team.source.TeamSourceProperty;
import net.officefloor.frame.spi.team.source.TeamSourceSpecification;
import net.officefloor.frame.test.OfficeFrameTestCase;

/**
 * Tests the {@link TeamLoader}.
 * 
 * @author Daniel Sagenschneider
 */
public class LoadTeamSourceSpecificationTest extends OfficeFrameTestCase {

	/**
	 * {@link CompilerIssues}.
	 */
	private final CompilerIssues issues = this.createMock(CompilerIssues.class);

	/**
	 * {@link TeamSourceSpecification}.
	 */
	private final TeamSourceSpecification specification = this
			.createMock(TeamSourceSpecification.class);

	@Override
	protected void setUp() throws Exception {
		MockLoadTeamSource.reset(this.specification);
	}

	/**
	 * Ensures issue if fails to instantiate the {@link TeamSource}.
	 */
	public void testFailInstantiateForTeamSourceSpecification() {

		final RuntimeException failure = new RuntimeException(
				"instantiate failure");

		// Record failure to instantiate
		this.record_issue(
				"Failed to instantiate " + MockLoadTeamSource.class.getName()
						+ " by default constructor", failure);

		// Attempt to obtain specification
		MockLoadTeamSource.instantiateFailure = failure;
		this.replayMockObjects();
		this.loadSpecification(false);
		this.verifyMockObjects();
	}

	/**
	 * Ensures issue if failure in obtaining the {@link TeamSourceSpecification}
	 * .
	 */
	public void testFailGetTeamSourceSpecification() {

		final Error failure = new Error("specification failure");

		// Record failure to instantiate
		this.record_issue("Failed to obtain TeamSourceSpecification from "
				+ MockLoadTeamSource.class.getName(), failure);

		// Attempt to obtain specification
		MockLoadTeamSource.specificationFailure = failure;
		this.replayMockObjects();
		this.loadSpecification(false);
		this.verifyMockObjects();
	}

	/**
	 * Ensures issue if no {@link TeamSourceSpecification} obtained.
	 */
	public void testNoTeamSourceSpecification() {

		// Record no specification returned
		this.record_issue("No TeamSourceSpecification returned from "
				+ MockLoadTeamSource.class.getName());

		// Attempt to obtain specification
		MockLoadTeamSource.specification = null;
		this.replayMockObjects();
		this.loadSpecification(false);
		this.verifyMockObjects();
	}

	/**
	 * Ensures issue if fails to obtain the {@link TeamSourceProperty}
	 * instances.
	 */
	public void testFailGetTeamSourceProperties() {

		final NullPointerException failure = new NullPointerException(
				"Fail to get managed object source properties");

		// Record null properties
		this.control(this.specification).expectAndThrow(
				this.specification.getProperties(), failure);
		this.record_issue(
				"Failed to obtain TeamSourceProperty instances from TeamSourceSpecification for "
						+ MockLoadTeamSource.class.getName(), failure);

		// Attempt to obtain specification
		this.replayMockObjects();
		this.loadSpecification(false);
		this.verifyMockObjects();
	}

	/**
	 * Ensures considers null {@link TeamSourceProperty} array as no properties.
	 */
	public void testNullTeamSourcePropertiesArray() {

		// Record null properties
		this.recordReturn(this.specification,
				this.specification.getProperties(), null);

		// Attempt to obtain specification
		this.replayMockObjects();
		this.loadSpecification(true);
		this.verifyMockObjects();
	}

	/**
	 * Ensures issue if element in {@link TeamSourceProperty} array is null.
	 */
	public void testNullTeamSourcePropertyElement() {

		// Record null properties
		this.recordReturn(this.specification,
				this.specification.getProperties(),
				new TeamSourceProperty[] { null });
		this.record_issue("TeamSourceProperty 0 is null from TeamSourceSpecification for "
				+ MockLoadTeamSource.class.getName());

		// Attempt to obtain specification
		this.replayMockObjects();
		this.loadSpecification(false);
		this.verifyMockObjects();
	}

	/**
	 * Ensures issue if <code>null</code> {@link TeamSourceProperty} name.
	 */
	public void testNullTeamSourcePropertyName() {

		final TeamSourceProperty property = this
				.createMock(TeamSourceProperty.class);

		// Record obtaining properties
		this.recordReturn(this.specification,
				this.specification.getProperties(),
				new TeamSourceProperty[] { property });
		this.recordReturn(property, property.getName(), "");
		this.record_issue("TeamSourceProperty 0 provided blank name from TeamSourceSpecification for "
				+ MockLoadTeamSource.class.getName());

		// Attempt to obtain specification
		this.replayMockObjects();
		this.loadSpecification(false);
		this.verifyMockObjects();
	}

	/**
	 * Ensures issue if fails to get the {@link TeamSourceProperty} name.
	 */
	public void testFailGetTeamSourcePropertyName() {

		final RuntimeException failure = new RuntimeException(
				"Failed to get property name");
		final TeamSourceProperty property = this
				.createMock(TeamSourceProperty.class);

		// Record obtaining properties
		this.recordReturn(this.specification,
				this.specification.getProperties(),
				new TeamSourceProperty[] { property });
		this.control(property).expectAndThrow(property.getName(), failure);
		this.record_issue(
				"Failed to get name for TeamSourceProperty 0 from TeamSourceSpecification for "
						+ MockLoadTeamSource.class.getName(), failure);

		// Attempt to obtain specification
		this.replayMockObjects();
		this.loadSpecification(false);
		this.verifyMockObjects();
	}

	/**
	 * Ensures issue if fails to get the {@link TeamSourceProperty} label.
	 */
	public void testFailGetTeamSourcePropertyLabel() {

		final RuntimeException failure = new RuntimeException(
				"Failed to get property label");
		final TeamSourceProperty property = this
				.createMock(TeamSourceProperty.class);

		// Record obtaining properties
		this.recordReturn(this.specification,
				this.specification.getProperties(),
				new TeamSourceProperty[] { property });
		this.recordReturn(property, property.getName(), "NAME");
		this.control(property).expectAndThrow(property.getLabel(), failure);
		this.record_issue(
				"Failed to get label for TeamSourceProperty 0 (NAME) from TeamSourceSpecification for "
						+ MockLoadTeamSource.class.getName(), failure);

		// Attempt to obtain specification
		this.replayMockObjects();
		this.loadSpecification(false);
		this.verifyMockObjects();
	}

	/**
	 * Ensures able to load the {@link TeamSourceSpecification}.
	 */
	public void testLoadTeamSourceSpecification() {

		final TeamSourceProperty propertyWithLabel = this
				.createMock(TeamSourceProperty.class);
		final TeamSourceProperty propertyWithoutLabel = this
				.createMock(TeamSourceProperty.class);

		// Record obtaining properties
		this.recordReturn(this.specification,
				this.specification.getProperties(), new TeamSourceProperty[] {
						propertyWithLabel, propertyWithoutLabel });
		this.recordReturn(propertyWithLabel, propertyWithLabel.getName(),
				"NAME");
		this.recordReturn(propertyWithLabel, propertyWithLabel.getLabel(),
				"LABEL");
		this.recordReturn(propertyWithoutLabel, propertyWithoutLabel.getName(),
				"NO LABEL");
		this.recordReturn(propertyWithoutLabel,
				propertyWithoutLabel.getLabel(), null);

		// Attempt to obtain specification
		this.replayMockObjects();
		this.loadSpecification(true, "NAME", "LABEL", "NO LABEL", "NO LABEL");
		this.verifyMockObjects();
	}

	/**
	 * Records an issue.
	 * 
	 * @param issueDescription
	 *            Description of the issue.
	 */
	private void record_issue(String issueDescription) {
		this.issues.addIssue(LocationType.OFFICE_FLOOR, null, AssetType.TEAM,
				null, issueDescription);
	}

	/**
	 * Records an issue.
	 * 
	 * @param issueDescription
	 *            Description of the issue.
	 * @param cause
	 *            Cause of the issue.
	 */
	private void record_issue(String issueDescription, Throwable cause) {
		this.issues.addIssue(LocationType.OFFICE_FLOOR, null, AssetType.TEAM,
				null, issueDescription, cause);
	}

	/**
	 * Loads the {@link TeamSourceSpecification}.
	 * 
	 * @param isExpectToLoad
	 *            Flag indicating if expect to obtain the
	 *            {@link TeamSourceSpecification}.
	 * @param propertyNames
	 *            Expected {@link Property} names for being returned.
	 */
	private void loadSpecification(boolean isExpectToLoad,
			String... propertyNameLabelPairs) {

		// Load the managed object specification specification
		OfficeFloorCompiler compiler = OfficeFloorCompiler
				.newOfficeFloorCompiler(null);
		compiler.setCompilerIssues(this.issues);
		TeamLoader teamLoader = compiler.getTeamLoader();
		PropertyList propertyList = teamLoader
				.loadSpecification(MockLoadTeamSource.class);

		// Determine if expected to load
		if (isExpectToLoad) {
			assertNotNull("Expected to load specification", propertyList);

			// Ensure the properties are as expected
			PropertyListUtil.validatePropertyNameLabels(propertyList,
					propertyNameLabelPairs);

		} else {
			assertNull("Should not load specification", propertyList);
		}
	}

	/**
	 * Mock {@link TeamSource} for testing.
	 */
	@TestSource
	public static class MockLoadTeamSource implements TeamSource {

		/**
		 * Failure to instantiate an instance.
		 */
		public static RuntimeException instantiateFailure = null;

		/**
		 * Failure to obtain the {@link TeamSourceSpecification}.
		 */
		public static Error specificationFailure = null;

		/**
		 * {@link TeamSourceSpecification}.
		 */
		public static TeamSourceSpecification specification;

		/**
		 * Resets the state for next test.
		 * 
		 * @param specification
		 *            {@link TeamSourceSpecification}.
		 */
		public static void reset(TeamSourceSpecification specification) {
			instantiateFailure = null;
			specificationFailure = null;
			MockLoadTeamSource.specification = specification;
		}

		/**
		 * Default constructor.
		 */
		public MockLoadTeamSource() {
			// Determine if fail to instantiate
			if (instantiateFailure != null) {
				throw instantiateFailure;
			}
		}

		/*
		 * ================ TeamSource ================================
		 */

		@Override
		public TeamSourceSpecification getSpecification() {
			// Determine if failure to obtain
			if (specificationFailure != null) {
				throw specificationFailure;
			}

			// Return the specification
			return specification;
		}

		@Override
		public Team createTeam(TeamSourceContext context) throws Exception {
			fail("Should not be invoked for obtaining specification");
			return null;
		}
	}

}