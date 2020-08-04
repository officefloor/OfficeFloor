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

package net.officefloor.compile.impl.supplier;

import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.compile.issues.CompilerIssues;
import net.officefloor.compile.properties.Property;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.supplier.source.SupplierSource;
import net.officefloor.compile.spi.supplier.source.SupplierSourceContext;
import net.officefloor.compile.spi.supplier.source.SupplierSourceProperty;
import net.officefloor.compile.spi.supplier.source.SupplierSourceSpecification;
import net.officefloor.compile.supplier.SupplierLoader;
import net.officefloor.compile.test.issues.MockCompilerIssues;
import net.officefloor.compile.test.properties.PropertyListUtil;
import net.officefloor.frame.api.source.TestSource;
import net.officefloor.frame.test.OfficeFrameTestCase;

/**
 * Tests the {@link SupplierLoader}.
 * 
 * @author Daniel Sagenschneider
 */
public class LoadSupplierSourceSpecificationTest extends OfficeFrameTestCase {

	/**
	 * {@link CompilerIssues}.
	 */
	private final MockCompilerIssues issues = new MockCompilerIssues(this);

	/**
	 * {@link SupplierSourceSpecification}.
	 */
	private final SupplierSourceSpecification specification = this.createMock(SupplierSourceSpecification.class);

	@Override
	protected void setUp() throws Exception {
		MockSupplierSource.reset(this.specification);
	}

	/**
	 * Ensures issue if fails to instantiate the {@link SupplierSource}.
	 */
	public void testFailInstantiateForSupplierSourceSpecification() {

		final RuntimeException failure = new RuntimeException("instantiate failure");

		// Record failure to instantiate
		this.issues.recordIssue(
				"Failed to instantiate " + MockSupplierSource.class.getName() + " by default constructor", failure);

		// Attempt to obtain specification
		MockSupplierSource.instantiateFailure = failure;
		this.replayMockObjects();
		this.loadSpecification(false);
		this.verifyMockObjects();
	}

	/**
	 * Ensures issue if failure in obtaining the
	 * {@link SupplierSourceSpecification}.
	 */
	public void testFailGetSupplierSourceSpecification() {

		final Error failure = new Error("specification failure");

		// Record failure to instantiate
		this.issues.recordIssue(
				"Failed to obtain SupplierSourceSpecification from " + MockSupplierSource.class.getName(), failure);

		// Attempt to obtain specification
		MockSupplierSource.specificationFailure = failure;
		this.replayMockObjects();
		this.loadSpecification(false);
		this.verifyMockObjects();
	}

	/**
	 * Ensures issue if no {@link SupplierSourceSpecification} obtained.
	 */
	public void testNoSupplierSourceSpecification() {

		// Record no specification returned
		this.issues.recordIssue("No SupplierSourceSpecification returned from " + MockSupplierSource.class.getName());

		// Attempt to obtain specification
		MockSupplierSource.specification = null;
		this.replayMockObjects();
		this.loadSpecification(false);
		this.verifyMockObjects();
	}

	/**
	 * Ensures issue if fails to obtain the {@link SupplierSourceProperty}
	 * instances.
	 */
	public void testFailGetSupplierSourceProperties() {

		final RuntimeException failure = new RuntimeException("Fail to get supplier source properties");

		// Record null properties
		this.recordThrows(this.specification, this.specification.getProperties(), failure);
		this.issues
				.recordIssue("Failed to obtain SupplierSourceProperty instances from SupplierSourceSpecification for "
						+ MockSupplierSource.class.getName(), failure);

		// Attempt to obtain specification
		this.replayMockObjects();
		this.loadSpecification(false);
		this.verifyMockObjects();
	}

	/**
	 * Ensures considers null {@link SupplierSourceProperty} array as no properties.
	 */
	public void testNullSupplierSourcePropertiesArray() {

		// Record null properties
		this.recordReturn(this.specification, this.specification.getProperties(), null);

		// Attempt to obtain specification
		this.replayMockObjects();
		this.loadSpecification(true);
		this.verifyMockObjects();
	}

	/**
	 * Ensures issue if element in {@link SupplierSourceProperty} array is null.
	 */
	public void testNullSupplierSourcePropertyElement() {

		// Record null properties
		this.recordReturn(this.specification, this.specification.getProperties(),
				new SupplierSourceProperty[] { null });
		this.issues.recordIssue("SupplierSourceProperty 0 is null from SupplierSourceSpecification for "
				+ MockSupplierSource.class.getName());

		// Attempt to obtain specification
		this.replayMockObjects();
		this.loadSpecification(false);
		this.verifyMockObjects();
	}

	/**
	 * Ensures issue if <code>null</code> {@link SupplierSourceProperty} name.
	 */
	public void testNullSupplierSourcePropertyName() {

		final SupplierSourceProperty property = this.createMock(SupplierSourceProperty.class);

		// Record obtaining properties
		this.recordReturn(this.specification, this.specification.getProperties(),
				new SupplierSourceProperty[] { property });
		this.recordReturn(property, property.getName(), "");
		this.issues.recordIssue("SupplierSourceProperty 0 provided blank name from SupplierSourceSpecification for "
				+ MockSupplierSource.class.getName());

		// Attempt to obtain specification
		this.replayMockObjects();
		this.loadSpecification(false);
		this.verifyMockObjects();
	}

	/**
	 * Ensures issue if fails to get the {@link SupplierSourceProperty} name.
	 */
	public void testFailGetSupplierSourcePropertyName() {

		final RuntimeException failure = new RuntimeException("Failed to get property name");
		final SupplierSourceProperty property = this.createMock(SupplierSourceProperty.class);

		// Record obtaining properties
		this.recordReturn(this.specification, this.specification.getProperties(),
				new SupplierSourceProperty[] { property });
		this.recordThrows(property, property.getName(), failure);
		this.issues.recordIssue("Failed to get name for SupplierSourceProperty 0 from SupplierSourceSpecification for "
				+ MockSupplierSource.class.getName(), failure);

		// Attempt to obtain specification
		this.replayMockObjects();
		this.loadSpecification(false);
		this.verifyMockObjects();
	}

	/**
	 * Ensures issue if fails to get the {@link SupplierSourceProperty} label.
	 */
	public void testFailGetSupplierSourcePropertyLabel() {

		final RuntimeException failure = new RuntimeException("Failed to get property label");
		final SupplierSourceProperty property = this.createMock(SupplierSourceProperty.class);

		// Record obtaining properties
		this.recordReturn(this.specification, this.specification.getProperties(),
				new SupplierSourceProperty[] { property });
		this.recordReturn(property, property.getName(), "NAME");
		this.recordThrows(property, property.getLabel(), failure);
		this.issues.recordIssue(
				"Failed to get label for SupplierSourceProperty 0 (NAME) from SupplierSourceSpecification for "
						+ MockSupplierSource.class.getName(),
				failure);

		// Attempt to obtain specification
		this.replayMockObjects();
		this.loadSpecification(false);
		this.verifyMockObjects();
	}

	/**
	 * Ensures able to load the {@link SupplierSourceSpecification}.
	 */
	public void testLoadSupplierSourceSpecification() {

		final SupplierSourceProperty propertyWithLabel = this.createMock(SupplierSourceProperty.class);
		final SupplierSourceProperty propertyWithoutLabel = this.createMock(SupplierSourceProperty.class);

		// Record obtaining properties
		this.recordReturn(this.specification, this.specification.getProperties(),
				new SupplierSourceProperty[] { propertyWithLabel, propertyWithoutLabel });
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
	 * Loads the {@link SupplierSourceSpecification}.
	 * 
	 * @param isExpectToLoad Flag indicating if expect to obtain the
	 *                       {@link SupplierSourceSpecification}.
	 * @param propertyNames  Expected {@link Property} names for being returned.
	 */
	private void loadSpecification(boolean isExpectToLoad, String... propertyNameLabelPairs) {

		// Load the supplier specification specification
		OfficeFloorCompiler compiler = OfficeFloorCompiler.newOfficeFloorCompiler(null);
		compiler.setCompilerIssues(this.issues);
		SupplierLoader supplierLoader = compiler.getSupplierLoader();
		PropertyList propertyList = supplierLoader.loadSpecification(MockSupplierSource.class);

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
	 * Mock {@link SupplierSource} for testing.
	 */
	@TestSource
	public static class MockSupplierSource implements SupplierSource {

		/**
		 * Failure to instantiate an instance.
		 */
		public static RuntimeException instantiateFailure = null;

		/**
		 * Failure to obtain the {@link SupplierSourceSpecification}.
		 */
		public static Error specificationFailure = null;

		/**
		 * {@link SupplierSourceSpecification}.
		 */
		public static SupplierSourceSpecification specification;

		/**
		 * Resets the state for next test.
		 * 
		 * @param specification {@link SupplierSourceSpecification}.
		 */
		public static void reset(SupplierSourceSpecification specification) {
			instantiateFailure = null;
			specificationFailure = null;
			MockSupplierSource.specification = specification;
		}

		/**
		 * Default constructor.
		 */
		public MockSupplierSource() {
			// Determine if fail to instantiate
			if (instantiateFailure != null) {
				throw instantiateFailure;
			}
		}

		/*
		 * ================ SupplierSource ================================
		 */

		@Override
		public SupplierSourceSpecification getSpecification() {
			// Determine if failure to obtain
			if (specificationFailure != null) {
				throw specificationFailure;
			}

			// Return the specification
			return specification;
		}

		@Override
		public void supply(SupplierSourceContext context) {
			fail("Should not be invoked for obtaining specification");
		}

		@Override
		public void terminate() {
			// nothing to clean up
		}
	}

}
