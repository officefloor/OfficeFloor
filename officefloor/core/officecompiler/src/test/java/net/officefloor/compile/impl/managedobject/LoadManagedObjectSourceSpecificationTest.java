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

package net.officefloor.compile.impl.managedobject;

import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.compile.issues.CompilerIssues;
import net.officefloor.compile.managedobject.ManagedObjectLoader;
import net.officefloor.compile.properties.Property;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.test.issues.MockCompilerIssues;
import net.officefloor.compile.test.properties.PropertyListUtil;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.managedobject.source.ManagedObjectExecuteContext;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSourceContext;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSourceMetaData;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSourceProperty;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSourceSpecification;
import net.officefloor.frame.api.managedobject.source.ManagedObjectUser;
import net.officefloor.frame.api.source.TestSource;
import net.officefloor.frame.test.OfficeFrameTestCase;

/**
 * Tests the {@link ManagedObjectLoader}.
 * 
 * @author Daniel Sagenschneider
 */
public class LoadManagedObjectSourceSpecificationTest extends OfficeFrameTestCase {

	/**
	 * {@link CompilerIssues}.
	 */
	private final MockCompilerIssues issues = new MockCompilerIssues(this);

	/**
	 * {@link ManagedObjectSourceSpecification}.
	 */
	private final ManagedObjectSourceSpecification specification = this
			.createMock(ManagedObjectSourceSpecification.class);

	/*
	 * (non-Javadoc)
	 * 
	 * @see junit.framework.TestCase#setUp()
	 */
	@Override
	protected void setUp() throws Exception {
		MockManagedObjectSource.reset(this.specification);
	}

	/**
	 * Ensures issue if fails to instantiate the {@link ManagedObjectSource}.
	 */
	public void testFailInstantiateForManagedObjectSourceSpecification() {

		final RuntimeException failure = new RuntimeException("instantiate failure");

		// Record failure to instantiate
		this.issues.recordIssue(
				"Failed to instantiate " + MockManagedObjectSource.class.getName() + " by default constructor",
				failure);

		// Attempt to obtain specification
		MockManagedObjectSource.instantiateFailure = failure;
		this.replayMockObjects();
		this.loadSpecification(false);
		this.verifyMockObjects();
	}

	/**
	 * Ensures issue if failure in obtaining the
	 * {@link ManagedObjectSourceSpecification}.
	 */
	public void testFailGetManagedObjectSourceSpecification() {

		final Error failure = new Error("specification failure");

		// Record failure to instantiate
		this.issues.recordIssue(
				"Failed to obtain ManagedObjectSourceSpecification from " + MockManagedObjectSource.class.getName(),
				failure);

		// Attempt to obtain specification
		MockManagedObjectSource.specificationFailure = failure;
		this.replayMockObjects();
		this.loadSpecification(false);
		this.verifyMockObjects();
	}

	/**
	 * Ensures issue if no {@link ManagedObjectSourceSpecification} obtained.
	 */
	public void testNoManagedObjectSourceSpecification() {

		// Record no specification returned
		this.issues.recordIssue(
				"No ManagedObjectSourceSpecification returned from " + MockManagedObjectSource.class.getName());

		// Attempt to obtain specification
		MockManagedObjectSource.specification = null;
		this.replayMockObjects();
		this.loadSpecification(false);
		this.verifyMockObjects();
	}

	/**
	 * Ensures issue if fails to obtain the {@link ManagedObjectSourceProperty}
	 * instances.
	 */
	public void testFailGetManagedObjectSourceProperties() {

		final NullPointerException failure = new NullPointerException("Fail to get managed object source properties");

		// Record null properties
		this.recordThrows(this.specification, this.specification.getProperties(), failure);
		this.issues.recordIssue(
				"Failed to obtain ManagedObjectSourceProperty instances from ManagedObjectSourceSpecification for "
						+ MockManagedObjectSource.class.getName(),
				failure);

		// Attempt to obtain specification
		this.replayMockObjects();
		this.loadSpecification(false);
		this.verifyMockObjects();
	}

	/**
	 * Ensures considers null {@link ManagedObjectSourceProperty} array as no
	 * properties.
	 */
	public void testNullManagedObjectSourcePropertiesArray() {

		// Record null properties
		this.recordReturn(this.specification, this.specification.getProperties(), null);

		// Attempt to obtain specification
		this.replayMockObjects();
		this.loadSpecification(true);
		this.verifyMockObjects();
	}

	/**
	 * Ensures issue if element in {@link ManagedObjectSourceProperty} array is
	 * null.
	 */
	public void testNullManagedObjectSourcePropertyElement() {

		// Record null properties
		this.recordReturn(this.specification, this.specification.getProperties(),
				new ManagedObjectSourceProperty[] { null });
		this.issues.recordIssue("ManagedObjectSourceProperty 0 is null from ManagedObjectSourceSpecification for "
				+ MockManagedObjectSource.class.getName());

		// Attempt to obtain specification
		this.replayMockObjects();
		this.loadSpecification(false);
		this.verifyMockObjects();
	}

	/**
	 * Ensures issue if <code>null</code> {@link ManagedObjectSourceProperty} name.
	 */
	public void testNullManagedObjectSourcePropertyName() {

		final ManagedObjectSourceProperty property = this.createMock(ManagedObjectSourceProperty.class);

		// Record obtaining properties
		this.recordReturn(this.specification, this.specification.getProperties(),
				new ManagedObjectSourceProperty[] { property });
		this.recordReturn(property, property.getName(), "");
		this.issues.recordIssue(
				"ManagedObjectSourceProperty 0 provided blank name from ManagedObjectSourceSpecification for "
						+ MockManagedObjectSource.class.getName());

		// Attempt to obtain specification
		this.replayMockObjects();
		this.loadSpecification(false);
		this.verifyMockObjects();
	}

	/**
	 * Ensures issue if fails to get the {@link ManagedObjectSourceProperty} name.
	 */
	public void testFailGetManagedObjectSourcePropertyName() {

		final RuntimeException failure = new RuntimeException("Failed to get property name");
		final ManagedObjectSourceProperty property = this.createMock(ManagedObjectSourceProperty.class);

		// Record obtaining properties
		this.recordReturn(this.specification, this.specification.getProperties(),
				new ManagedObjectSourceProperty[] { property });
		this.recordThrows(property, property.getName(), failure);
		this.issues.recordIssue(
				"Failed to get name for ManagedObjectSourceProperty 0 from ManagedObjectSourceSpecification for "
						+ MockManagedObjectSource.class.getName(),
				failure);

		// Attempt to obtain specification
		this.replayMockObjects();
		this.loadSpecification(false);
		this.verifyMockObjects();
	}

	/**
	 * Ensures issue if fails to get the {@link ManagedObjectSourceProperty} label.
	 */
	public void testFailGetManagedObjectSourcePropertyLabel() {

		final RuntimeException failure = new RuntimeException("Failed to get property label");
		final ManagedObjectSourceProperty property = this.createMock(ManagedObjectSourceProperty.class);

		// Record obtaining properties
		this.recordReturn(this.specification, this.specification.getProperties(),
				new ManagedObjectSourceProperty[] { property });
		this.recordReturn(property, property.getName(), "NAME");
		this.recordThrows(property, property.getLabel(), failure);
		this.issues.recordIssue(
				"Failed to get label for ManagedObjectSourceProperty 0 (NAME) from ManagedObjectSourceSpecification for "
						+ MockManagedObjectSource.class.getName(),
				failure);

		// Attempt to obtain specification
		this.replayMockObjects();
		this.loadSpecification(false);
		this.verifyMockObjects();
	}

	/**
	 * Ensures able to load the {@link ManagedObjectSourceSpecification}.
	 */
	public void testLoadManagedObjectSourceSpecification() {

		final ManagedObjectSourceProperty propertyWithLabel = this.createMock(ManagedObjectSourceProperty.class);
		final ManagedObjectSourceProperty propertyWithoutLabel = this.createMock(ManagedObjectSourceProperty.class);

		// Record obtaining properties
		this.recordReturn(this.specification, this.specification.getProperties(),
				new ManagedObjectSourceProperty[] { propertyWithLabel, propertyWithoutLabel });
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
	 * Loads the {@link ManagedObjectSourceSpecification}.
	 * 
	 * @param isExpectToLoad Flag indicating if expect to obtain the
	 *                       {@link ManagedObjectSourceSpecification}.
	 * @param propertyNames  Expected {@link Property} names for being returned.
	 */
	private void loadSpecification(boolean isExpectToLoad, String... propertyNameLabelPairs) {

		// Load the managed object specification specification
		OfficeFloorCompiler compiler = OfficeFloorCompiler.newOfficeFloorCompiler(null);
		compiler.setCompilerIssues(this.issues);
		ManagedObjectLoader managedObjectLoader = compiler.getManagedObjectLoader();
		PropertyList propertyList = managedObjectLoader.loadSpecification(MockManagedObjectSource.class);

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
	 * Mock {@link ManagedObjectSource} for testing.
	 */
	@TestSource
	public static class MockManagedObjectSource implements ManagedObjectSource<None, None> {

		/**
		 * Failure to instantiate an instance.
		 */
		public static RuntimeException instantiateFailure = null;

		/**
		 * Failure to obtain the {@link ManagedObjectSourceSpecification}.
		 */
		public static Error specificationFailure = null;

		/**
		 * {@link ManagedObjectSourceSpecification}.
		 */
		public static ManagedObjectSourceSpecification specification;

		/**
		 * Resets the state for next test.
		 * 
		 * @param specification {@link ManagedObjectSourceSpecification}.
		 */
		public static void reset(ManagedObjectSourceSpecification specification) {
			instantiateFailure = null;
			specificationFailure = null;
			MockManagedObjectSource.specification = specification;
		}

		/**
		 * Default constructor.
		 */
		public MockManagedObjectSource() {
			// Determine if fail to instantiate
			if (instantiateFailure != null) {
				throw instantiateFailure;
			}
		}

		/*
		 * ================ ManagedObjectSource ================================
		 */

		@Override
		public ManagedObjectSourceSpecification getSpecification() {
			// Determine if failure to obtain
			if (specificationFailure != null) {
				throw specificationFailure;
			}

			// Return the specification
			return specification;
		}

		@Override
		public ManagedObjectSourceMetaData<None, None> init(ManagedObjectSourceContext<None> context) throws Exception {
			fail("Should not be invoked for obtaining specification");
			return null;
		}

		@Override
		public void start(ManagedObjectExecuteContext<None> context) throws Exception {
			fail("Should not be invoked for obtaining specification");
		}

		@Override
		public void sourceManagedObject(ManagedObjectUser user) {
			fail("Should not be invoked for obtaining specification");
		}

		@Override
		public void stop() {
			fail("Should not be invoked for obtaining specification");
		}
	}

}
