/*-
 * #%L
 * OfficeCompiler
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package net.officefloor.compile.impl.administration;

import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.compile.administration.AdministrationLoader;
import net.officefloor.compile.issues.CompilerIssues;
import net.officefloor.compile.properties.Property;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.administration.source.AdministrationSource;
import net.officefloor.compile.spi.administration.source.AdministrationSourceContext;
import net.officefloor.compile.spi.administration.source.AdministrationSourceMetaData;
import net.officefloor.compile.spi.administration.source.AdministrationSourceProperty;
import net.officefloor.compile.spi.administration.source.AdministrationSourceSpecification;
import net.officefloor.compile.test.issues.MockCompilerIssues;
import net.officefloor.compile.test.properties.PropertyListUtil;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.source.TestSource;
import net.officefloor.frame.test.OfficeFrameTestCase;

/**
 * Tests the {@link AdministrationLoader}.
 * 
 * @author Daniel Sagenschneider
 */
public class LoadAdministrationSourceSpecificationTest extends OfficeFrameTestCase {

	/**
	 * {@link CompilerIssues}.
	 */
	private final MockCompilerIssues issues = new MockCompilerIssues(this);

	/**
	 * {@link AdministrationSourceSpecification}.
	 */
	private final AdministrationSourceSpecification specification = this
			.createMock(AdministrationSourceSpecification.class);

	@Override
	protected void setUp() throws Exception {
		MockAdministrationSource.reset(this.specification);
	}

	/**
	 * Ensures issue if fails to instantiate the {@link AdministrationSource}.
	 */
	public void testFailInstantiateForAdministrationSourceSpecification() {

		final RuntimeException failure = new RuntimeException("instantiate failure");

		// Record failure to instantiate
		this.issues.recordIssue(
				"Failed to instantiate " + MockAdministrationSource.class.getName() + " by default constructor",
				failure);

		// Attempt to obtain specification
		MockAdministrationSource.instantiateFailure = failure;
		this.replayMockObjects();
		this.loadSpecification(false);
		this.verifyMockObjects();
	}

	/**
	 * Ensures issue if failure in obtaining the
	 * {@link AdministrationSourceSpecification}.
	 */
	public void testFailGetAdministrationSourceSpecification() {

		final Error failure = new Error("specification failure");

		// Record failure to instantiate
		this.issues.recordIssue(
				"Failed to obtain AdministrationSourceSpecification from " + MockAdministrationSource.class.getName(),
				failure);

		// Attempt to obtain specification
		MockAdministrationSource.specificationFailure = failure;
		this.replayMockObjects();
		this.loadSpecification(false);
		this.verifyMockObjects();
	}

	/**
	 * Ensures issue if no {@link AdministrationSourceSpecification} obtained.
	 */
	public void testNoAdministrationSourceSpecification() {

		// Record no specification returned
		this.issues.recordIssue(
				"No AdministrationSourceSpecification returned from " + MockAdministrationSource.class.getName());

		// Attempt to obtain specification
		MockAdministrationSource.specification = null;
		this.replayMockObjects();
		this.loadSpecification(false);
		this.verifyMockObjects();
	}

	/**
	 * Ensures issue if fails to obtain the {@link AdministrationSourceProperty}
	 * instances.
	 */
	public void testFailGetAdministrationSourceProperties() {

		final NullPointerException failure = new NullPointerException("Fail to get managed object source properties");

		// Record null properties
		this.recordThrows(this.specification, this.specification.getProperties(), failure);
		this.issues.recordIssue(
				"Failed to obtain AdministrationSourceProperty instances from AdministrationSourceSpecification for "
						+ MockAdministrationSource.class.getName(),
				failure);

		// Attempt to obtain specification
		this.replayMockObjects();
		this.loadSpecification(false);
		this.verifyMockObjects();
	}

	/**
	 * Ensures considers null {@link AdministrationSourceProperty} array as no
	 * properties.
	 */
	public void testNullAdministrationSourcePropertiesArray() {

		// Record null properties
		this.recordReturn(this.specification, this.specification.getProperties(), null);

		// Attempt to obtain specification
		this.replayMockObjects();
		this.loadSpecification(true);
		this.verifyMockObjects();
	}

	/**
	 * Ensures issue if element in {@link AdministrationSourceProperty} array is
	 * null.
	 */
	public void testNullAdministrationSourcePropertyElement() {

		// Record null properties
		this.recordReturn(this.specification, this.specification.getProperties(),
				new AdministrationSourceProperty[] { null });
		this.issues.recordIssue("AdministrationSourceProperty 0 is null from AdministrationSourceSpecification for "
				+ MockAdministrationSource.class.getName());

		// Attempt to obtain specification
		this.replayMockObjects();
		this.loadSpecification(false);
		this.verifyMockObjects();
	}

	/**
	 * Ensures issue if <code>null</code> {@link AdministrationSourceProperty} name.
	 */
	public void testNullAdministrationSourcePropertyName() {

		final AdministrationSourceProperty property = this.createMock(AdministrationSourceProperty.class);

		// Record obtaining properties
		this.recordReturn(this.specification, this.specification.getProperties(),
				new AdministrationSourceProperty[] { property });
		this.recordReturn(property, property.getName(), "");
		this.issues.recordIssue(
				"AdministrationSourceProperty 0 provided blank name from AdministrationSourceSpecification for "
						+ MockAdministrationSource.class.getName());

		// Attempt to obtain specification
		this.replayMockObjects();
		this.loadSpecification(false);
		this.verifyMockObjects();
	}

	/**
	 * Ensures issue if fails to get the {@link AdministrationSourceProperty} name.
	 */
	public void testFailGetAdministrationSourcePropertyName() {

		final RuntimeException failure = new RuntimeException("Failed to get property name");
		final AdministrationSourceProperty property = this.createMock(AdministrationSourceProperty.class);

		// Record obtaining properties
		this.recordReturn(this.specification, this.specification.getProperties(),
				new AdministrationSourceProperty[] { property });
		this.recordThrows(property, property.getName(), failure);
		this.issues.recordIssue(
				"Failed to get name for AdministrationSourceProperty 0 from AdministrationSourceSpecification for "
						+ MockAdministrationSource.class.getName(),
				failure);

		// Attempt to obtain specification
		this.replayMockObjects();
		this.loadSpecification(false);
		this.verifyMockObjects();
	}

	/**
	 * Ensures issue if fails to get the {@link AdministrationSourceProperty} label.
	 */
	public void testFailGetAdministrationSourcePropertyLabel() {

		final RuntimeException failure = new RuntimeException("Failed to get property label");
		final AdministrationSourceProperty property = this.createMock(AdministrationSourceProperty.class);

		// Record obtaining properties
		this.recordReturn(this.specification, this.specification.getProperties(),
				new AdministrationSourceProperty[] { property });
		this.recordReturn(property, property.getName(), "NAME");
		this.recordThrows(property, property.getLabel(), failure);
		this.issues.recordIssue(
				"Failed to get label for AdministrationSourceProperty 0 (NAME) from AdministrationSourceSpecification for "
						+ MockAdministrationSource.class.getName(),
				failure);

		// Attempt to obtain specification
		this.replayMockObjects();
		this.loadSpecification(false);
		this.verifyMockObjects();
	}

	/**
	 * Ensures able to load the {@link AdministrationSourceSpecification}.
	 */
	public void testLoadAdministrationSourceSpecification() {

		final AdministrationSourceProperty propertyWithLabel = this.createMock(AdministrationSourceProperty.class);
		final AdministrationSourceProperty propertyWithoutLabel = this.createMock(AdministrationSourceProperty.class);

		// Record obtaining properties
		this.recordReturn(this.specification, this.specification.getProperties(),
				new AdministrationSourceProperty[] { propertyWithLabel, propertyWithoutLabel });
		this.recordReturn(propertyWithLabel, propertyWithLabel.getName(), "NAME");
		this.recordReturn(propertyWithLabel, propertyWithLabel.getLabel(), "LABEL");
		this.recordReturn(propertyWithoutLabel, propertyWithoutLabel.getName(), "NO LABEL");
		this.recordReturn(propertyWithoutLabel, propertyWithoutLabel.getLabel(), null);

		// Attempt to obtain specification
		this.replayMockObjects();
		this.loadSpecification(true, "NAME", "LABEL", "NO LABEL", "NO LABEL");
		this.verifyMockObjects();
	}

	/**
	 * Loads the {@link AdministrationSourceSpecification}.
	 * 
	 * @param isExpectToLoad Flag indicating if expect to obtain the
	 *                       {@link AdministrationSourceSpecification}.
	 * @param propertyNames  Expected {@link Property} names for being returned.
	 */
	private void loadSpecification(boolean isExpectToLoad, String... propertyNameLabelPairs) {

		// Load the managed object specification specification
		OfficeFloorCompiler compiler = OfficeFloorCompiler.newOfficeFloorCompiler(null);
		compiler.setCompilerIssues(this.issues);
		AdministrationLoader administrationLoader = compiler.getAdministrationLoader();
		PropertyList propertyList = administrationLoader.loadSpecification(MockAdministrationSource.class);

		// Determine if expected to load
		if (isExpectToLoad) {
			assertNotNull("Expected to load specification", propertyList);

			// Ensure the properties are as expected
			PropertyListUtil.validatePropertyNameLabels(propertyList, propertyNameLabelPairs);

		} else {
			assertNull("Should not load specification", propertyList);
		}
	}

	/**
	 * Mock {@link AdministrationSource} for testing.
	 */
	@TestSource
	public static class MockAdministrationSource implements AdministrationSource<Object, None, None> {

		/**
		 * Failure to instantiate an instance.
		 */
		public static RuntimeException instantiateFailure = null;

		/**
		 * Failure to obtain the {@link AdministrationSourceSpecification}.
		 */
		public static Error specificationFailure = null;

		/**
		 * {@link AdministrationSourceSpecification}.
		 */
		public static AdministrationSourceSpecification specification;

		/**
		 * Resets the state for next test.
		 * 
		 * @param specification {@link AdministrationSourceSpecification}.
		 */
		public static void reset(AdministrationSourceSpecification specification) {
			instantiateFailure = null;
			specificationFailure = null;
			MockAdministrationSource.specification = specification;
		}

		/**
		 * Default constructor.
		 */
		public MockAdministrationSource() {
			// Determine if fail to instantiate
			if (instantiateFailure != null) {
				throw instantiateFailure;
			}
		}

		/*
		 * ================ AdministrationSource ================
		 */

		@Override
		public AdministrationSourceSpecification getSpecification() {
			// Determine if failure to obtain
			if (specificationFailure != null) {
				throw specificationFailure;
			}

			// Return the specification
			return specification;
		}

		@Override
		public AdministrationSourceMetaData<Object, None, None> init(AdministrationSourceContext context)
				throws Exception {
			fail("Should not be invoked for obtaining specification");
			return null;
		}
	}

}
