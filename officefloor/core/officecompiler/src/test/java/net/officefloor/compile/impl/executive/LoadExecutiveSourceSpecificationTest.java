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

package net.officefloor.compile.impl.executive;

import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.compile.issues.CompilerIssues;
import net.officefloor.compile.properties.Property;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.executive.ExecutiveLoader;
import net.officefloor.compile.test.issues.MockCompilerIssues;
import net.officefloor.compile.test.properties.PropertyListUtil;
import net.officefloor.frame.api.source.TestSource;
import net.officefloor.frame.api.executive.Executive;
import net.officefloor.frame.api.executive.source.ExecutiveSource;
import net.officefloor.frame.api.executive.source.ExecutiveSourceContext;
import net.officefloor.frame.api.executive.source.ExecutiveSourceProperty;
import net.officefloor.frame.api.executive.source.ExecutiveSourceSpecification;
import net.officefloor.frame.test.OfficeFrameTestCase;

/**
 * Tests the {@link ExecutiveLoader}.
 * 
 * @author Daniel Sagenschneider
 */
public class LoadExecutiveSourceSpecificationTest extends OfficeFrameTestCase {

	/**
	 * {@link CompilerIssues}.
	 */
	private final MockCompilerIssues issues = new MockCompilerIssues(this);

	/**
	 * {@link ExecutiveSourceSpecification}.
	 */
	private final ExecutiveSourceSpecification specification = this.createMock(ExecutiveSourceSpecification.class);

	@Override
	protected void setUp() throws Exception {
		MockLoadExecutiveSource.reset(this.specification);
	}

	/**
	 * Ensures issue if fails to instantiate the {@link ExecutiveSource}.
	 */
	public void testFailInstantiateForExecutiveSourceSpecification() {

		final RuntimeException failure = new RuntimeException("instantiate failure");

		// Record failure to instantiate
		this.issues.recordIssue(
				"Failed to instantiate " + MockLoadExecutiveSource.class.getName() + " by default constructor",
				failure);

		// Attempt to obtain specification
		MockLoadExecutiveSource.instantiateFailure = failure;
		this.replayMockObjects();
		this.loadSpecification(false);
		this.verifyMockObjects();
	}

	/**
	 * Ensures issue if failure in obtaining the
	 * {@link ExecutiveSourceSpecification} .
	 */
	public void testFailGetExecutiveSourceSpecification() {

		final Error failure = new Error("specification failure");

		// Record failure to instantiate
		this.issues.recordIssue(
				"Failed to obtain ExecutiveSourceSpecification from " + MockLoadExecutiveSource.class.getName(),
				failure);

		// Attempt to obtain specification
		MockLoadExecutiveSource.specificationFailure = failure;
		this.replayMockObjects();
		this.loadSpecification(false);
		this.verifyMockObjects();
	}

	/**
	 * Ensures issue if no {@link ExecutiveSourceSpecification} obtained.
	 */
	public void testNoExecutiveSourceSpecification() {

		// Record no specification returned
		this.issues.recordIssue(
				"No ExecutiveSourceSpecification returned from " + MockLoadExecutiveSource.class.getName());

		// Attempt to obtain specification
		MockLoadExecutiveSource.specification = null;
		this.replayMockObjects();
		this.loadSpecification(false);
		this.verifyMockObjects();
	}

	/**
	 * Ensures issue if fails to obtain the {@link ExecutiveSourceProperty}
	 * instances.
	 */
	public void testFailGetExecutiveSourceProperties() {

		final NullPointerException failure = new NullPointerException("Fail to get executive source properties");

		// Record null properties
		this.recordThrows(this.specification, this.specification.getProperties(), failure);
		this.issues
				.recordIssue("Failed to obtain ExecutiveSourceProperty instances from ExecutiveSourceSpecification for "
						+ MockLoadExecutiveSource.class.getName(), failure);

		// Attempt to obtain specification
		this.replayMockObjects();
		this.loadSpecification(false);
		this.verifyMockObjects();
	}

	/**
	 * Ensures considers null {@link ExecutiveSourceProperty} array as no
	 * properties.
	 */
	public void testNullExecutiveSourcePropertiesArray() {

		// Record null properties
		this.recordReturn(this.specification, this.specification.getProperties(), null);

		// Attempt to obtain specification
		this.replayMockObjects();
		this.loadSpecification(true);
		this.verifyMockObjects();
	}

	/**
	 * Ensures issue if element in {@link ExecutiveSourceProperty} array is null.
	 */
	public void testNullExecutiveSourcePropertyElement() {

		// Record null properties
		this.recordReturn(this.specification, this.specification.getProperties(),
				new ExecutiveSourceProperty[] { null });
		this.issues.recordIssue("ExecutiveSourceProperty 0 is null from ExecutiveSourceSpecification for "
				+ MockLoadExecutiveSource.class.getName());

		// Attempt to obtain specification
		this.replayMockObjects();
		this.loadSpecification(false);
		this.verifyMockObjects();
	}

	/**
	 * Ensures issue if <code>null</code> {@link ExecutiveSourceProperty} name.
	 */
	public void testNullExecutiveSourcePropertyName() {

		final ExecutiveSourceProperty property = this.createMock(ExecutiveSourceProperty.class);

		// Record obtaining properties
		this.recordReturn(this.specification, this.specification.getProperties(),
				new ExecutiveSourceProperty[] { property });
		this.recordReturn(property, property.getName(), "");
		this.issues.recordIssue("ExecutiveSourceProperty 0 provided blank name from ExecutiveSourceSpecification for "
				+ MockLoadExecutiveSource.class.getName());

		// Attempt to obtain specification
		this.replayMockObjects();
		this.loadSpecification(false);
		this.verifyMockObjects();
	}

	/**
	 * Ensures issue if fails to get the {@link ExecutiveSourceProperty} name.
	 */
	public void testFailGetExecutiveSourcePropertyName() {

		final RuntimeException failure = new RuntimeException("Failed to get property name");
		final ExecutiveSourceProperty property = this.createMock(ExecutiveSourceProperty.class);

		// Record obtaining properties
		this.recordReturn(this.specification, this.specification.getProperties(),
				new ExecutiveSourceProperty[] { property });
		this.recordThrows(property, property.getName(), failure);
		this.issues
				.recordIssue("Failed to get name for ExecutiveSourceProperty 0 from ExecutiveSourceSpecification for "
						+ MockLoadExecutiveSource.class.getName(), failure);

		// Attempt to obtain specification
		this.replayMockObjects();
		this.loadSpecification(false);
		this.verifyMockObjects();
	}

	/**
	 * Ensures issue if fails to get the {@link ExecutiveSourceProperty} label.
	 */
	public void testFailGetExecutiveSourcePropertyLabel() {

		final RuntimeException failure = new RuntimeException("Failed to get property label");
		final ExecutiveSourceProperty property = this.createMock(ExecutiveSourceProperty.class);

		// Record obtaining properties
		this.recordReturn(this.specification, this.specification.getProperties(),
				new ExecutiveSourceProperty[] { property });
		this.recordReturn(property, property.getName(), "NAME");
		this.recordThrows(property, property.getLabel(), failure);
		this.issues.recordIssue(
				"Failed to get label for ExecutiveSourceProperty 0 (NAME) from ExecutiveSourceSpecification for "
						+ MockLoadExecutiveSource.class.getName(),
				failure);

		// Attempt to obtain specification
		this.replayMockObjects();
		this.loadSpecification(false);
		this.verifyMockObjects();
	}

	/**
	 * Ensures able to load the {@link ExecutiveSourceSpecification}.
	 */
	public void testLoadExecutiveSourceSpecification() {

		final ExecutiveSourceProperty propertyWithLabel = this.createMock(ExecutiveSourceProperty.class);
		final ExecutiveSourceProperty propertyWithoutLabel = this.createMock(ExecutiveSourceProperty.class);

		// Record obtaining properties
		this.recordReturn(this.specification, this.specification.getProperties(),
				new ExecutiveSourceProperty[] { propertyWithLabel, propertyWithoutLabel });
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
	 * Loads the {@link ExecutiveSourceSpecification}.
	 * 
	 * @param isExpectToLoad Flag indicating if expect to obtain the
	 *                       {@link ExecutiveSourceSpecification}.
	 * @param propertyNames  Expected {@link Property} names for being returned.
	 */
	private void loadSpecification(boolean isExpectToLoad, String... propertyNameLabelPairs) {

		// Load the managed object specification specification
		OfficeFloorCompiler compiler = OfficeFloorCompiler.newOfficeFloorCompiler(null);
		compiler.setCompilerIssues(this.issues);
		ExecutiveLoader executiveLoader = compiler.getExecutiveLoader();
		PropertyList propertyList = executiveLoader.loadSpecification(MockLoadExecutiveSource.class);

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
	 * Mock {@link ExecutiveSource} for testing.
	 */
	@TestSource
	public static class MockLoadExecutiveSource implements ExecutiveSource {

		/**
		 * Failure to instantiate an instance.
		 */
		public static RuntimeException instantiateFailure = null;

		/**
		 * Failure to obtain the {@link ExecutiveSourceSpecification}.
		 */
		public static Error specificationFailure = null;

		/**
		 * {@link ExecutiveSourceSpecification}.
		 */
		public static ExecutiveSourceSpecification specification;

		/**
		 * Resets the state for next test.
		 * 
		 * @param specification {@link ExecutiveSourceSpecification}.
		 */
		public static void reset(ExecutiveSourceSpecification specification) {
			instantiateFailure = null;
			specificationFailure = null;
			MockLoadExecutiveSource.specification = specification;
		}

		/**
		 * Default constructor.
		 */
		public MockLoadExecutiveSource() {
			// Determine if fail to instantiate
			if (instantiateFailure != null) {
				throw instantiateFailure;
			}
		}

		/*
		 * ================ ExecutiveSource ================================
		 */

		@Override
		public ExecutiveSourceSpecification getSpecification() {
			// Determine if failure to obtain
			if (specificationFailure != null) {
				throw specificationFailure;
			}

			// Return the specification
			return specification;
		}

		@Override
		public Executive createExecutive(ExecutiveSourceContext context) throws Exception {
			fail("Should not be invoked for obtaining specification");
			return null;
		}
	}

}
