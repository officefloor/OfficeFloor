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

package net.officefloor.compile.impl.pool;

import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.compile.issues.CompilerIssues;
import net.officefloor.compile.pool.ManagedObjectPoolLoader;
import net.officefloor.compile.properties.Property;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.pool.source.ManagedObjectPoolSource;
import net.officefloor.compile.spi.pool.source.ManagedObjectPoolSourceContext;
import net.officefloor.compile.spi.pool.source.ManagedObjectPoolSourceMetaData;
import net.officefloor.compile.spi.pool.source.ManagedObjectPoolSourceProperty;
import net.officefloor.compile.spi.pool.source.ManagedObjectPoolSourceSpecification;
import net.officefloor.compile.test.issues.MockCompilerIssues;
import net.officefloor.compile.test.properties.PropertyListUtil;
import net.officefloor.frame.test.OfficeFrameTestCase;

/**
 * Tests the {@link ManagedObjectPoolLoader}.
 * 
 * @author Daniel Sagenschneider
 */
public class LoadManagedObjectPoolSourceSpecificationTest extends OfficeFrameTestCase {

	/**
	 * {@link CompilerIssues}.
	 */
	private final MockCompilerIssues issues = new MockCompilerIssues(this);

	/**
	 * {@link ManagedObjectPoolSourceSpecification}.
	 */
	private final ManagedObjectPoolSourceSpecification specification = this
			.createMock(ManagedObjectPoolSourceSpecification.class);

	@Override
	protected void setUp() throws Exception {
		MockManagedObjectPoolSource.reset(this.specification);
	}

	/**
	 * Ensures issue if fails to instantiate the {@link ManagedObjectPoolSource} .
	 */
	public void testFailInstantiateForManagedObjectPoolSourceSpecification() {

		final RuntimeException failure = new RuntimeException("instantiate failure");

		// Record failure to instantiate
		this.issues.recordIssue(
				"Failed to instantiate " + MockManagedObjectPoolSource.class.getName() + " by default constructor",
				failure);

		// Attempt to obtain specification
		MockManagedObjectPoolSource.instantiateFailure = failure;
		this.replayMockObjects();
		this.loadSpecification(false);
		this.verifyMockObjects();
	}

	/**
	 * Ensures issue if failure in obtaining the
	 * {@link ManagedObjectPoolSourceSpecification}.
	 */
	public void testFailGetManagedObjectPoolSourceSpecification() {

		final Error failure = new Error("specification failure");

		// Record failure to instantiate
		this.issues.recordIssue("Failed to obtain ManagedObjectPoolSourceSpecification from "
				+ MockManagedObjectPoolSource.class.getName(), failure);

		// Attempt to obtain specification
		MockManagedObjectPoolSource.specificationFailure = failure;
		this.replayMockObjects();
		this.loadSpecification(false);
		this.verifyMockObjects();
	}

	/**
	 * Ensures issue if no {@link ManagedObjectPoolSourceSpecification} obtained.
	 */
	public void testNoManagedObjectPoolSourceSpecification() {

		// Record no specification returned
		this.issues.recordIssue(
				"No ManagedObjectPoolSourceSpecification returned from " + MockManagedObjectPoolSource.class.getName());

		// Attempt to obtain specification
		MockManagedObjectPoolSource.specification = null;
		this.replayMockObjects();
		this.loadSpecification(false);
		this.verifyMockObjects();
	}

	/**
	 * Ensures issue if fails to obtain the {@link ManagedObjectPoolSourceProperty}
	 * instances.
	 */
	public void testFailGetManagedObjectPoolSourceProperties() {

		final NullPointerException failure = new NullPointerException("Fail to get managed object source properties");

		// Record null properties
		this.recordThrows(this.specification, this.specification.getProperties(), failure);
		this.issues.recordIssue(
				"Failed to obtain ManagedObjectPoolSourceProperty instances from ManagedObjectPoolSourceSpecification for "
						+ MockManagedObjectPoolSource.class.getName(),
				failure);

		// Attempt to obtain specification
		this.replayMockObjects();
		this.loadSpecification(false);
		this.verifyMockObjects();
	}

	/**
	 * Ensures considers null {@link ManagedObjectPoolSourceProperty} array as no
	 * properties.
	 */
	public void testNullManagedObjectPoolSourcePropertiesArray() {

		// Record null properties
		this.recordReturn(this.specification, this.specification.getProperties(), null);

		// Attempt to obtain specification
		this.replayMockObjects();
		this.loadSpecification(true);
		this.verifyMockObjects();
	}

	/**
	 * Ensures issue if element in {@link ManagedObjectPoolSourceProperty} array is
	 * null.
	 */
	public void testNullManagedObjectPoolSourcePropertyElement() {

		// Record null properties
		this.recordReturn(this.specification, this.specification.getProperties(),
				new ManagedObjectPoolSourceProperty[] { null });
		this.issues
				.recordIssue("ManagedObjectPoolSourceProperty 0 is null from ManagedObjectPoolSourceSpecification for "
						+ MockManagedObjectPoolSource.class.getName());

		// Attempt to obtain specification
		this.replayMockObjects();
		this.loadSpecification(false);
		this.verifyMockObjects();
	}

	/**
	 * Ensures issue if <code>null</code> {@link ManagedObjectPoolSourceProperty}
	 * name.
	 */
	public void testNullManagedObjectPoolSourcePropertyName() {

		final ManagedObjectPoolSourceProperty property = this.createMock(ManagedObjectPoolSourceProperty.class);

		// Record obtaining properties
		this.recordReturn(this.specification, this.specification.getProperties(),
				new ManagedObjectPoolSourceProperty[] { property });
		this.recordReturn(property, property.getName(), "");
		this.issues.recordIssue(
				"ManagedObjectPoolSourceProperty 0 provided blank name from ManagedObjectPoolSourceSpecification for "
						+ MockManagedObjectPoolSource.class.getName());

		// Attempt to obtain specification
		this.replayMockObjects();
		this.loadSpecification(false);
		this.verifyMockObjects();
	}

	/**
	 * Ensures issue if fails to get the {@link ManagedObjectPoolSourceProperty}
	 * name.
	 */
	public void testFailGetManagedObjectPoolSourcePropertyName() {

		final RuntimeException failure = new RuntimeException("Failed to get property name");
		final ManagedObjectPoolSourceProperty property = this.createMock(ManagedObjectPoolSourceProperty.class);

		// Record obtaining properties
		this.recordReturn(this.specification, this.specification.getProperties(),
				new ManagedObjectPoolSourceProperty[] { property });
		this.recordThrows(property, property.getName(), failure);
		this.issues.recordIssue(
				"Failed to get name for ManagedObjectPoolSourceProperty 0 from ManagedObjectPoolSourceSpecification for "
						+ MockManagedObjectPoolSource.class.getName(),
				failure);

		// Attempt to obtain specification
		this.replayMockObjects();
		this.loadSpecification(false);
		this.verifyMockObjects();
	}

	/**
	 * Ensures issue if fails to get the {@link ManagedObjectPoolSourceProperty}
	 * label.
	 */
	public void testFailGetManagedObjectPoolSourcePropertyLabel() {

		final RuntimeException failure = new RuntimeException("Failed to get property label");
		final ManagedObjectPoolSourceProperty property = this.createMock(ManagedObjectPoolSourceProperty.class);

		// Record obtaining properties
		this.recordReturn(this.specification, this.specification.getProperties(),
				new ManagedObjectPoolSourceProperty[] { property });
		this.recordReturn(property, property.getName(), "NAME");
		this.recordThrows(property, property.getLabel(), failure);
		this.issues.recordIssue(
				"Failed to get label for ManagedObjectPoolSourceProperty 0 (NAME) from ManagedObjectPoolSourceSpecification for "
						+ MockManagedObjectPoolSource.class.getName(),
				failure);

		// Attempt to obtain specification
		this.replayMockObjects();
		this.loadSpecification(false);
		this.verifyMockObjects();
	}

	/**
	 * Ensures able to load the {@link ManagedObjectPoolSourceSpecification}.
	 */
	public void testLoadManagedObjectPoolSourceSpecification() {

		final ManagedObjectPoolSourceProperty propertyWithLabel = this
				.createMock(ManagedObjectPoolSourceProperty.class);
		final ManagedObjectPoolSourceProperty propertyWithoutLabel = this
				.createMock(ManagedObjectPoolSourceProperty.class);

		// Record obtaining properties
		this.recordReturn(this.specification, this.specification.getProperties(),
				new ManagedObjectPoolSourceProperty[] { propertyWithLabel, propertyWithoutLabel });
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
	 * Loads the {@link ManagedObjectPoolSourceSpecification}.
	 * 
	 * @param isExpectToLoad Flag indicating if expect to obtain the
	 *                       {@link ManagedObjectPoolSourceSpecification}.
	 * @param propertyNames  Expected {@link Property} names for being returned.
	 */
	private void loadSpecification(boolean isExpectToLoad, String... propertyNameLabelPairs) {

		// Load the managed object pool specification specification
		OfficeFloorCompiler compiler = OfficeFloorCompiler.newOfficeFloorCompiler(null);
		compiler.setCompilerIssues(this.issues);
		ManagedObjectPoolLoader managedObjectPoolLoader = compiler.getManagedObjectPoolLoader();
		PropertyList propertyList = managedObjectPoolLoader.loadSpecification(MockManagedObjectPoolSource.class);

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
	 * Mock {@link ManagedObjectPoolSource} for testing.
	 */
	public static class MockManagedObjectPoolSource implements ManagedObjectPoolSource {

		/**
		 * Failure to instantiate an instance.
		 */
		public static RuntimeException instantiateFailure = null;

		/**
		 * Failure to obtain the {@link ManagedObjectPoolSourceSpecification}.
		 */
		public static Error specificationFailure = null;

		/**
		 * {@link ManagedObjectPoolSourceSpecification}.
		 */
		public static ManagedObjectPoolSourceSpecification specification;

		/**
		 * Resets the state for next test.
		 * 
		 * @param specification {@link ManagedObjectPoolSourceSpecification}.
		 */
		public static void reset(ManagedObjectPoolSourceSpecification specification) {
			instantiateFailure = null;
			specificationFailure = null;
			MockManagedObjectPoolSource.specification = specification;
		}

		/**
		 * Default constructor.
		 */
		public MockManagedObjectPoolSource() {
			// Determine if fail to instantiate
			if (instantiateFailure != null) {
				throw instantiateFailure;
			}
		}

		/*
		 * ================ ManagedObjectPoolSource ============================
		 */

		@Override
		public ManagedObjectPoolSourceSpecification getSpecification() {
			// Determine if failure to obtain
			if (specificationFailure != null) {
				throw specificationFailure;
			}

			// Return the specification
			return specification;
		}

		@Override
		public ManagedObjectPoolSourceMetaData init(ManagedObjectPoolSourceContext context) throws Exception {
			fail("Should not be invoked for obtaining specification");
			return null;
		}
	}

}
