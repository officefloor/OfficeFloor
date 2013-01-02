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
package net.officefloor.compile.impl.administrator;

import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.compile.administrator.AdministratorLoader;
import net.officefloor.compile.issues.CompilerIssues;
import net.officefloor.compile.issues.CompilerIssues.LocationType;
import net.officefloor.compile.properties.Property;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.test.properties.PropertyListUtil;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.build.OfficeFloorIssues.AssetType;
import net.officefloor.frame.spi.TestSource;
import net.officefloor.frame.spi.administration.Administrator;
import net.officefloor.frame.spi.administration.source.AdministratorSource;
import net.officefloor.frame.spi.administration.source.AdministratorSourceContext;
import net.officefloor.frame.spi.administration.source.AdministratorSourceMetaData;
import net.officefloor.frame.spi.administration.source.AdministratorSourceProperty;
import net.officefloor.frame.spi.administration.source.AdministratorSourceSpecification;
import net.officefloor.frame.test.OfficeFrameTestCase;

/**
 * Tests the {@link AdministratorLoader}.
 * 
 * @author Daniel Sagenschneider
 */
public class LoadAdministratorSourceSpecificationTest extends
		OfficeFrameTestCase {

	/**
	 * {@link CompilerIssues}.
	 */
	private final CompilerIssues issues = this.createMock(CompilerIssues.class);

	/**
	 * {@link AdministratorSourceSpecification}.
	 */
	private final AdministratorSourceSpecification specification = this
			.createMock(AdministratorSourceSpecification.class);

	/*
	 * (non-Javadoc)
	 * 
	 * @see junit.framework.TestCase#setUp()
	 */
	@Override
	protected void setUp() throws Exception {
		MockAdministratorSource.reset(this.specification);
	}

	/**
	 * Ensures issue if fails to instantiate the {@link AdministratorSource}.
	 */
	public void testFailInstantiateForAdministratorSourceSpecification() {

		final RuntimeException failure = new RuntimeException(
				"instantiate failure");

		// Record failure to instantiate
		this.record_issue("Failed to instantiate "
				+ MockAdministratorSource.class.getName()
				+ " by default constructor", failure);

		// Attempt to obtain specification
		MockAdministratorSource.instantiateFailure = failure;
		this.replayMockObjects();
		this.loadSpecification(false);
		this.verifyMockObjects();
	}

	/**
	 * Ensures issue if failure in obtaining the
	 * {@link AdministratorSourceSpecification}.
	 */
	public void testFailGetAdministratorSourceSpecification() {

		final Error failure = new Error("specification failure");

		// Record failure to instantiate
		this.record_issue(
				"Failed to obtain AdministratorSourceSpecification from "
						+ MockAdministratorSource.class.getName(), failure);

		// Attempt to obtain specification
		MockAdministratorSource.specificationFailure = failure;
		this.replayMockObjects();
		this.loadSpecification(false);
		this.verifyMockObjects();
	}

	/**
	 * Ensures issue if no {@link AdministratorSourceSpecification} obtained.
	 */
	public void testNoAdministratorSourceSpecification() {

		// Record no specification returned
		this.record_issue("No AdministratorSourceSpecification returned from "
				+ MockAdministratorSource.class.getName());

		// Attempt to obtain specification
		MockAdministratorSource.specification = null;
		this.replayMockObjects();
		this.loadSpecification(false);
		this.verifyMockObjects();
	}

	/**
	 * Ensures issue if fails to obtain the {@link AdministratorSourceProperty}
	 * instances.
	 */
	public void testFailGetAdministratorSourceProperties() {

		final NullPointerException failure = new NullPointerException(
				"Fail to get managed object source properties");

		// Record null properties
		this.control(this.specification).expectAndThrow(
				this.specification.getProperties(), failure);
		this.record_issue(
				"Failed to obtain AdministratorSourceProperty instances from AdministratorSourceSpecification for "
						+ MockAdministratorSource.class.getName(), failure);

		// Attempt to obtain specification
		this.replayMockObjects();
		this.loadSpecification(false);
		this.verifyMockObjects();
	}

	/**
	 * Ensures considers null {@link AdministratorSourceProperty} array as no
	 * properties.
	 */
	public void testNullAdministratorSourcePropertiesArray() {

		// Record null properties
		this.recordReturn(this.specification,
				this.specification.getProperties(), null);

		// Attempt to obtain specification
		this.replayMockObjects();
		this.loadSpecification(true);
		this.verifyMockObjects();
	}

	/**
	 * Ensures issue if element in {@link AdministratorSourceProperty} array is
	 * null.
	 */
	public void testNullAdministratorSourcePropertyElement() {

		// Record null properties
		this.recordReturn(this.specification,
				this.specification.getProperties(),
				new AdministratorSourceProperty[] { null });
		this.record_issue("AdministratorSourceProperty 0 is null from AdministratorSourceSpecification for "
				+ MockAdministratorSource.class.getName());

		// Attempt to obtain specification
		this.replayMockObjects();
		this.loadSpecification(false);
		this.verifyMockObjects();
	}

	/**
	 * Ensures issue if <code>null</code> {@link AdministratorSourceProperty}
	 * name.
	 */
	public void testNullAdministratorSourcePropertyName() {

		final AdministratorSourceProperty property = this
				.createMock(AdministratorSourceProperty.class);

		// Record obtaining properties
		this.recordReturn(this.specification,
				this.specification.getProperties(),
				new AdministratorSourceProperty[] { property });
		this.recordReturn(property, property.getName(), "");
		this.record_issue("AdministratorSourceProperty 0 provided blank name from AdministratorSourceSpecification for "
				+ MockAdministratorSource.class.getName());

		// Attempt to obtain specification
		this.replayMockObjects();
		this.loadSpecification(false);
		this.verifyMockObjects();
	}

	/**
	 * Ensures issue if fails to get the {@link AdministratorSourceProperty}
	 * name.
	 */
	public void testFailGetAdministratorSourcePropertyName() {

		final RuntimeException failure = new RuntimeException(
				"Failed to get property name");
		final AdministratorSourceProperty property = this
				.createMock(AdministratorSourceProperty.class);

		// Record obtaining properties
		this.recordReturn(this.specification,
				this.specification.getProperties(),
				new AdministratorSourceProperty[] { property });
		this.control(property).expectAndThrow(property.getName(), failure);
		this.record_issue(
				"Failed to get name for AdministratorSourceProperty 0 from AdministratorSourceSpecification for "
						+ MockAdministratorSource.class.getName(), failure);

		// Attempt to obtain specification
		this.replayMockObjects();
		this.loadSpecification(false);
		this.verifyMockObjects();
	}

	/**
	 * Ensures issue if fails to get the {@link AdministratorSourceProperty}
	 * label.
	 */
	public void testFailGetAdministratorSourcePropertyLabel() {

		final RuntimeException failure = new RuntimeException(
				"Failed to get property label");
		final AdministratorSourceProperty property = this
				.createMock(AdministratorSourceProperty.class);

		// Record obtaining properties
		this.recordReturn(this.specification,
				this.specification.getProperties(),
				new AdministratorSourceProperty[] { property });
		this.recordReturn(property, property.getName(), "NAME");
		this.control(property).expectAndThrow(property.getLabel(), failure);
		this.record_issue(
				"Failed to get label for AdministratorSourceProperty 0 (NAME) from AdministratorSourceSpecification for "
						+ MockAdministratorSource.class.getName(), failure);

		// Attempt to obtain specification
		this.replayMockObjects();
		this.loadSpecification(false);
		this.verifyMockObjects();
	}

	/**
	 * Ensures able to load the {@link AdministratorSourceSpecification}.
	 */
	public void testLoadAdministratorSourceSpecification() {

		final AdministratorSourceProperty propertyWithLabel = this
				.createMock(AdministratorSourceProperty.class);
		final AdministratorSourceProperty propertyWithoutLabel = this
				.createMock(AdministratorSourceProperty.class);

		// Record obtaining properties
		this.recordReturn(this.specification,
				this.specification.getProperties(),
				new AdministratorSourceProperty[] { propertyWithLabel,
						propertyWithoutLabel });
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
		this.issues.addIssue(LocationType.OFFICE, null,
				AssetType.ADMINISTRATOR, null, issueDescription);
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
		this.issues.addIssue(LocationType.OFFICE, null,
				AssetType.ADMINISTRATOR, null, issueDescription, cause);
	}

	/**
	 * Loads the {@link AdministratorSourceSpecification}.
	 * 
	 * @param isExpectToLoad
	 *            Flag indicating if expect to obtain the
	 *            {@link AdministratorSourceSpecification}.
	 * @param propertyNames
	 *            Expected {@link Property} names for being returned.
	 */
	private void loadSpecification(boolean isExpectToLoad,
			String... propertyNameLabelPairs) {

		// Load the managed object specification specification
		OfficeFloorCompiler compiler = OfficeFloorCompiler
				.newOfficeFloorCompiler(null);
		compiler.setCompilerIssues(this.issues);
		AdministratorLoader administratorLoader = compiler
				.getAdministratorLoader();
		PropertyList propertyList = administratorLoader
				.loadSpecification(MockAdministratorSource.class);

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
	 * Mock {@link AdministratorSource} for testing.
	 */
	@TestSource
	public static class MockAdministratorSource implements
			AdministratorSource<None, None> {

		/**
		 * Failure to instantiate an instance.
		 */
		public static RuntimeException instantiateFailure = null;

		/**
		 * Failure to obtain the {@link AdministratorSourceSpecification}.
		 */
		public static Error specificationFailure = null;

		/**
		 * {@link AdministratorSourceSpecification}.
		 */
		public static AdministratorSourceSpecification specification;

		/**
		 * Resets the state for next test.
		 * 
		 * @param specification
		 *            {@link AdministratorSourceSpecification}.
		 */
		public static void reset(AdministratorSourceSpecification specification) {
			instantiateFailure = null;
			specificationFailure = null;
			MockAdministratorSource.specification = specification;
		}

		/**
		 * Default constructor.
		 */
		public MockAdministratorSource() {
			// Determine if fail to instantiate
			if (instantiateFailure != null) {
				throw instantiateFailure;
			}
		}

		/*
		 * ================ AdministratorSource ================================
		 */

		@Override
		public AdministratorSourceSpecification getSpecification() {
			// Determine if failure to obtain
			if (specificationFailure != null) {
				throw specificationFailure;
			}

			// Return the specification
			return specification;
		}

		@Override
		public void init(AdministratorSourceContext context) throws Exception {
			fail("Should not be invoked for obtaining specification");
		}

		@Override
		public AdministratorSourceMetaData<None, None> getMetaData() {
			fail("Should not be invoked for obtaining specification");
			return null;
		}

		@Override
		public Administrator<None, None> createAdministrator() {
			fail("Should not be invoked for obtaining specification");
			return null;
		}
	}

}