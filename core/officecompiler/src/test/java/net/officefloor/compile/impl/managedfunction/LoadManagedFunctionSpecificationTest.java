/*-
 * #%L
 * OfficeCompiler
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

package net.officefloor.compile.impl.managedfunction;

import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.compile.issues.CompilerIssues;
import net.officefloor.compile.managedfunction.ManagedFunctionLoader;
import net.officefloor.compile.properties.Property;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.managedfunction.source.FunctionNamespaceBuilder;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionSource;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionSourceContext;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionSourceProperty;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionSourceSpecification;
import net.officefloor.compile.test.issues.MockCompilerIssues;
import net.officefloor.compile.test.properties.PropertyListUtil;
import net.officefloor.frame.api.source.TestSource;
import net.officefloor.frame.test.OfficeFrameTestCase;

/**
 * Tests the {@link ManagedFunctionLoader}.
 * 
 * @author Daniel Sagenschneider
 */
public class LoadManagedFunctionSpecificationTest extends OfficeFrameTestCase {

	/**
	 * {@link CompilerIssues}.
	 */
	private final MockCompilerIssues issues = new MockCompilerIssues(this);

	/**
	 * {@link ManagedFunctionSourceSpecification}.
	 */
	private final ManagedFunctionSourceSpecification specification = this
			.createMock(ManagedFunctionSourceSpecification.class);

	@Override
	protected void setUp() throws Exception {
		MockManagedFunctionSource.reset(this.specification);
	}

	/**
	 * Ensures issue if fails to instantiate the {@link ManagedFunctionSource}.
	 */
	public void testFailInstantiateForManagedFunctionSpecification() {

		final RuntimeException failure = new RuntimeException("instantiate failure");

		// Record failure to instantiate
		this.issues.recordIssue(
				"Failed to instantiate " + MockManagedFunctionSource.class.getName() + " by default constructor",
				failure);

		// Attempt to obtain specification
		MockManagedFunctionSource.instantiateFailure = failure;
		this.replayMockObjects();
		this.loadSpecification(false);
		this.verifyMockObjects();
	}

	/**
	 * Ensures issue if failure in obtaining the
	 * {@link ManagedFunctionSourceSpecification} .
	 */
	public void testFailGetManagedFunctionSpecification() {

		final Error failure = new Error("specification failure");

		// Record failure to instantiate
		this.issues.recordIssue(
				"Failed to obtain ManagedFunctionSourceSpecification from " + MockManagedFunctionSource.class.getName(),
				failure);

		// Attempt to obtain specification
		MockManagedFunctionSource.specificationFailure = failure;
		this.replayMockObjects();
		this.loadSpecification(false);
		this.verifyMockObjects();
	}

	/**
	 * Ensures issue if no {@link ManagedFunctionSourceSpecification} obtained.
	 */
	public void testNoManagedFunctionSpecification() {

		// Record no specification returned
		this.issues.recordIssue(
				"No ManagedFunctionSourceSpecification returned from " + MockManagedFunctionSource.class.getName());

		// Attempt to obtain specification
		MockManagedFunctionSource.specification = null;
		this.replayMockObjects();
		this.loadSpecification(false);
		this.verifyMockObjects();
	}

	/**
	 * Ensures issue if fails to obtain the {@link ManagedFunctionSourceProperty}
	 * instances.
	 */
	public void testFailGetManagedFunctionProperties() {

		final NullPointerException failure = new NullPointerException("Fail to get work properties");

		// Record null work properties
		this.recordThrows(this.specification, this.specification.getProperties(), failure);
		this.issues.recordIssue(
				"Failed to obtain ManagedFunctionSourceProperty instances from ManagedFunctionSourceSpecification for "
						+ MockManagedFunctionSource.class.getName(),
				failure);

		// Attempt to obtain specification
		this.replayMockObjects();
		this.loadSpecification(false);
		this.verifyMockObjects();
	}

	/**
	 * Ensures considers null {@link ManagedFunctionSourceProperty} array as no
	 * properties.
	 */
	public void testNullManagedFunctionPropertiesArray() {

		// Record null work properties
		this.recordReturn(this.specification, this.specification.getProperties(), null);

		// Attempt to obtain specification
		this.replayMockObjects();
		this.loadSpecification(true);
		this.verifyMockObjects();
	}

	/**
	 * Ensures issue if element in {@link ManagedFunctionSourceProperty} array is
	 * null.
	 */
	public void testNullManagedFunctionPropertyElement() {

		// Record null work properties
		this.recordReturn(this.specification, this.specification.getProperties(),
				new ManagedFunctionSourceProperty[] { null });
		this.issues.recordIssue("ManagedFunctionSourceProperty 0 is null from ManagedFunctionSourceSpecification for "
				+ MockManagedFunctionSource.class.getName());

		// Attempt to obtain specification
		this.replayMockObjects();
		this.loadSpecification(false);
		this.verifyMockObjects();
	}

	/**
	 * Ensures issue if <code>null</code> {@link ManagedFunctionSourceProperty}
	 * name.
	 */
	public void testNullManagedFunctionPropertyName() {

		final ManagedFunctionSourceProperty property = this.createMock(ManagedFunctionSourceProperty.class);

		// Record obtaining work properties
		this.recordReturn(this.specification, this.specification.getProperties(),
				new ManagedFunctionSourceProperty[] { property });
		this.recordReturn(property, property.getName(), "");
		this.issues.recordIssue(
				"ManagedFunctionSourceProperty 0 provided blank name from ManagedFunctionSourceSpecification for "
						+ MockManagedFunctionSource.class.getName());

		// Attempt to obtain specification
		this.replayMockObjects();
		this.loadSpecification(false);
		this.verifyMockObjects();
	}

	/**
	 * Ensures issue if fails to get the {@link ManagedFunctionSourceProperty} name.
	 */
	public void testFailGetManagedFunctionPropertyName() {

		final RuntimeException failure = new RuntimeException("Failed to get property name");
		final ManagedFunctionSourceProperty property = this.createMock(ManagedFunctionSourceProperty.class);

		// Record obtaining work properties
		this.recordReturn(this.specification, this.specification.getProperties(),
				new ManagedFunctionSourceProperty[] { property });
		this.recordThrows(property, property.getName(), failure);
		this.issues.recordIssue(
				"Failed to get name for ManagedFunctionSourceProperty 0 from ManagedFunctionSourceSpecification for "
						+ MockManagedFunctionSource.class.getName(),
				failure);

		// Attempt to obtain specification
		this.replayMockObjects();
		this.loadSpecification(false);
		this.verifyMockObjects();
	}

	/**
	 * Ensures issue if fails to get the {@link ManagedFunctionSourceProperty}
	 * label.
	 */
	public void testFailGetManagedFunctionPropertyLabel() {

		final RuntimeException failure = new RuntimeException("Failed to get property label");
		final ManagedFunctionSourceProperty property = this.createMock(ManagedFunctionSourceProperty.class);

		// Record obtaining work properties
		this.recordReturn(this.specification, this.specification.getProperties(),
				new ManagedFunctionSourceProperty[] { property });
		this.recordReturn(property, property.getName(), "NAME");
		this.recordThrows(property, property.getLabel(), failure);
		this.issues.recordIssue(
				"Failed to get label for ManagedFunctionSourceProperty 0 (NAME) from ManagedFunctionSourceSpecification for "
						+ MockManagedFunctionSource.class.getName(),
				failure);

		// Attempt to obtain specification
		this.replayMockObjects();
		this.loadSpecification(false);
		this.verifyMockObjects();
	}

	/**
	 * Ensures able to load the {@link ManagedFunctionSourceSpecification}.
	 */
	public void testLoadManagedFunctionSpecification() {

		final ManagedFunctionSourceProperty propertyWithLabel = this.createMock(ManagedFunctionSourceProperty.class);
		final ManagedFunctionSourceProperty propertyWithoutLabel = this.createMock(ManagedFunctionSourceProperty.class);

		// Record obtaining work properties
		this.recordReturn(this.specification, this.specification.getProperties(),
				new ManagedFunctionSourceProperty[] { propertyWithLabel, propertyWithoutLabel });
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
	 * Loads the {@link ManagedFunctionSourceSpecification}.
	 * 
	 * @param isExpectToLoad Flag indicating if expect to obtain the
	 *                       {@link ManagedFunctionSourceSpecification}.
	 * @param propertyNames  Expected {@link Property} names for being returned.
	 */
	private void loadSpecification(boolean isExpectToLoad, String... propertyNameLabelPairs) {

		// Load the work specification
		OfficeFloorCompiler compiler = OfficeFloorCompiler.newOfficeFloorCompiler(null);
		compiler.setCompilerIssues(this.issues);
		ManagedFunctionLoader functionLoader = compiler.getManagedFunctionLoader();
		PropertyList propertyList = functionLoader.loadSpecification(MockManagedFunctionSource.class);

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
	 * Mock {@link ManagedFunctionSource} for testing.
	 */
	@TestSource
	public static class MockManagedFunctionSource implements ManagedFunctionSource {

		/**
		 * Failure to instantiate an instance.
		 */
		public static RuntimeException instantiateFailure = null;

		/**
		 * Failure to obtain the {@link ManagedFunctionSourceSpecification}.
		 */
		public static Error specificationFailure = null;

		/**
		 * {@link ManagedFunctionSourceSpecification}.
		 */
		public static ManagedFunctionSourceSpecification specification;

		/**
		 * Resets the state for next test.
		 * 
		 * @param specification {@link ManagedFunctionSourceSpecification}.
		 */
		public static void reset(ManagedFunctionSourceSpecification specification) {
			instantiateFailure = null;
			specificationFailure = null;
			MockManagedFunctionSource.specification = specification;
		}

		/**
		 * Default constructor.
		 */
		public MockManagedFunctionSource() {
			// Determine if fail to instantiate
			if (instantiateFailure != null) {
				throw instantiateFailure;
			}
		}

		/*
		 * ================ ManagedFunctionSource ================
		 */

		@Override
		public ManagedFunctionSourceSpecification getSpecification() {

			// Determine if failure to obtain
			if (specificationFailure != null) {
				throw specificationFailure;
			}

			// Return the specification
			return specification;
		}

		@Override
		public void sourceManagedFunctions(FunctionNamespaceBuilder namespaceBuilder,
				ManagedFunctionSourceContext context) throws Exception {
			fail("Should not be invoked for obtaining specification");
		}
	}

}
