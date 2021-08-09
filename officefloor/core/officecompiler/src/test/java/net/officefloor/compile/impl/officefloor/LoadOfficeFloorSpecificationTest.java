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

package net.officefloor.compile.impl.officefloor;

import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.compile.issues.CompilerIssues;
import net.officefloor.compile.officefloor.OfficeFloorLoader;
import net.officefloor.compile.properties.Property;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.officefloor.OfficeFloorDeployer;
import net.officefloor.compile.spi.officefloor.source.OfficeFloorSource;
import net.officefloor.compile.spi.officefloor.source.OfficeFloorSourceContext;
import net.officefloor.compile.spi.officefloor.source.OfficeFloorSourceProperty;
import net.officefloor.compile.spi.officefloor.source.OfficeFloorSourceSpecification;
import net.officefloor.compile.spi.officefloor.source.RequiredProperties;
import net.officefloor.compile.test.issues.MockCompilerIssues;
import net.officefloor.compile.test.properties.PropertyListUtil;
import net.officefloor.frame.test.OfficeFrameTestCase;

/**
 * Tests the {@link OfficeFloorLoader}.
 * 
 * @author Daniel Sagenschneider
 */
public class LoadOfficeFloorSpecificationTest extends OfficeFrameTestCase {

	/**
	 * {@link CompilerIssues}.
	 */
	private final MockCompilerIssues issues = new MockCompilerIssues(this);

	/**
	 * {@link OfficeFloorSourceSpecification}.
	 */
	private final OfficeFloorSourceSpecification specification = this.createMock(OfficeFloorSourceSpecification.class);

	@Override
	protected void setUp() throws Exception {
		MockOfficeFloorSource.reset(this.specification);
	}

	/**
	 * Ensures issue if fails to instantiate the {@link OfficeFloorSource}.
	 */
	public void testFailInstantiateForOfficeFloorSpecification() {

		final RuntimeException failure = new RuntimeException("instantiate failure");

		// Record failure to instantiate
		this.issues.recordIssue(
				"Failed to instantiate " + MockOfficeFloorSource.class.getName() + " by default constructor", failure);

		// Attempt to obtain specification
		MockOfficeFloorSource.instantiateFailure = failure;
		this.replayMockObjects();
		this.loadSpecification(false);
		this.verifyMockObjects();
	}

	/**
	 * Ensures issue if failure in obtaining the
	 * {@link OfficeFloorSourceSpecification}.
	 */
	public void testFailGetOfficeFloorSpecification() {

		final Error failure = new Error("specification failure");

		// Record failure to instantiate
		this.issues.recordIssue(
				"Failed to obtain OfficeFloorSourceSpecification from " + MockOfficeFloorSource.class.getName(),
				failure);

		// Attempt to obtain specification
		MockOfficeFloorSource.specificationFailure = failure;
		this.replayMockObjects();
		this.loadSpecification(false);
		this.verifyMockObjects();
	}

	/**
	 * Ensures issue if no {@link OfficeFloorSourceSpecification} obtained.
	 */
	public void testNoOfficeFloorSpecification() {

		// Record no specification returned
		this.issues.recordIssue(
				"No OfficeFloorSourceSpecification returned from " + MockOfficeFloorSource.class.getName());

		// Attempt to obtain specification
		MockOfficeFloorSource.specification = null;
		this.replayMockObjects();
		this.loadSpecification(false);
		this.verifyMockObjects();
	}

	/**
	 * Ensures issue if fails to obtain the {@link OfficeFloorSourceProperty}
	 * instances.
	 */
	public void testFailGetOfficeFloorProperties() {

		final NullPointerException failure = new NullPointerException("Fail to get office floor properties");

		// Record null office floor properties
		this.recordThrows(this.specification, this.specification.getProperties(), failure);
		this.issues.recordIssue(
				"Failed to obtain OfficeFloorSourceProperty instances from OfficeFloorSourceSpecification for "
						+ MockOfficeFloorSource.class.getName(),
				failure);

		// Attempt to obtain specification
		this.replayMockObjects();
		this.loadSpecification(false);
		this.verifyMockObjects();
	}

	/**
	 * Ensures considers null {@link OfficeFloorSourceProperty} array as no
	 * properties.
	 */
	public void testNullOfficeFloorPropertiesArray() {

		// Record null office floor properties
		this.recordReturn(this.specification, this.specification.getProperties(), null);

		// Attempt to obtain specification
		this.replayMockObjects();
		this.loadSpecification(true);
		this.verifyMockObjects();
	}

	/**
	 * Ensures issue if element in {@link OfficeFloorSourceProperty} array is null.
	 */
	public void testNullOfficeFloorPropertyElement() {

		// Record null office floor properties
		this.recordReturn(this.specification, this.specification.getProperties(),
				new OfficeFloorSourceProperty[] { null });
		this.issues.recordIssue("OfficeFloorSourceProperty 0 is null from OfficeFloorSourceSpecification for "
				+ MockOfficeFloorSource.class.getName());

		// Attempt to obtain specification
		this.replayMockObjects();
		this.loadSpecification(false);
		this.verifyMockObjects();
	}

	/**
	 * Ensures issue if <code>null</code> {@link OfficeFloorSourceProperty} name.
	 */
	public void testNullOfficeFloorPropertyName() {

		final OfficeFloorSourceProperty property = this.createMock(OfficeFloorSourceProperty.class);

		// Record obtaining office floor properties
		this.recordReturn(this.specification, this.specification.getProperties(),
				new OfficeFloorSourceProperty[] { property });
		this.recordReturn(property, property.getName(), "");
		this.issues
				.recordIssue("OfficeFloorSourceProperty 0 provided blank name from OfficeFloorSourceSpecification for "
						+ MockOfficeFloorSource.class.getName());

		// Attempt to obtain specification
		this.replayMockObjects();
		this.loadSpecification(false);
		this.verifyMockObjects();
	}

	/**
	 * Ensures issue if fails to get the {@link OfficeFloorSourceProperty} name.
	 */
	public void testFailGetOfficeFloorPropertyName() {

		final RuntimeException failure = new RuntimeException("Failed to get property name");
		final OfficeFloorSourceProperty property = this.createMock(OfficeFloorSourceProperty.class);

		// Record obtaining office floor properties
		this.recordReturn(this.specification, this.specification.getProperties(),
				new OfficeFloorSourceProperty[] { property });
		this.recordThrows(property, property.getName(), failure);
		this.issues.recordIssue(
				"Failed to get name for OfficeFloorSourceProperty 0 from OfficeFloorSourceSpecification for "
						+ MockOfficeFloorSource.class.getName(),
				failure);

		// Attempt to obtain specification
		this.replayMockObjects();
		this.loadSpecification(false);
		this.verifyMockObjects();
	}

	/**
	 * Ensures issue if fails to get the {@link OfficeFloorSourceProperty} label.
	 */
	public void testFailGetOfficeFloorPropertyLabel() {

		final RuntimeException failure = new RuntimeException("Failed to get property label");
		final OfficeFloorSourceProperty property = this.createMock(OfficeFloorSourceProperty.class);

		// Record obtaining office floor properties
		this.recordReturn(this.specification, this.specification.getProperties(),
				new OfficeFloorSourceProperty[] { property });
		this.recordReturn(property, property.getName(), "NAME");
		this.recordThrows(property, property.getLabel(), failure);
		this.issues.recordIssue(
				"Failed to get label for OfficeFloorSourceProperty 0 (NAME) from OfficeFloorSourceSpecification for "
						+ MockOfficeFloorSource.class.getName(),
				failure);

		// Attempt to obtain specification
		this.replayMockObjects();
		this.loadSpecification(false);
		this.verifyMockObjects();
	}

	/**
	 * Ensures able to load the {@link OfficeFloorSourceSpecification}.
	 */
	public void testLoadOfficeFloorSpecification() {

		final OfficeFloorSourceProperty propertyWithLabel = this.createMock(OfficeFloorSourceProperty.class);
		final OfficeFloorSourceProperty propertyWithoutLabel = this.createMock(OfficeFloorSourceProperty.class);

		// Record obtaining office floor properties
		this.recordReturn(this.specification, this.specification.getProperties(),
				new OfficeFloorSourceProperty[] { propertyWithLabel, propertyWithoutLabel });
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
	 * Loads the {@link OfficeFloorSourceSpecification}.
	 * 
	 * @param isExpectToLoad Flag indicating if expect to obtain the
	 *                       {@link OfficeFloorSourceSpecification}.
	 * @param propertyNames  Expected {@link Property} names for being returned.
	 */
	private void loadSpecification(boolean isExpectToLoad, String... propertyNameLabelPairs) {

		// Load the office floor specification
		OfficeFloorCompiler compiler = OfficeFloorCompiler.newOfficeFloorCompiler(null);
		compiler.setCompilerIssues(this.issues);
		OfficeFloorLoader officeFloorLoader = compiler.getOfficeFloorLoader();
		PropertyList propertyList = officeFloorLoader.loadSpecification(MockOfficeFloorSource.class);

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
	 * Mock {@link OfficeFloorSource} for testing.
	 */
	public static class MockOfficeFloorSource implements OfficeFloorSource {

		/**
		 * Failure to instantiate an instance.
		 */
		public static RuntimeException instantiateFailure = null;

		/**
		 * Failure to obtain the {@link OfficeFloorSourceSpecification}.
		 */
		public static Error specificationFailure = null;

		/**
		 * {@link OfficeFloorSourceSpecification}.
		 */
		public static OfficeFloorSourceSpecification specification;

		/**
		 * Resets the state for next test.
		 * 
		 * @param specification {@link OfficeFloorSourceSpecification}.
		 */
		public static void reset(OfficeFloorSourceSpecification specification) {
			instantiateFailure = null;
			specificationFailure = null;
			MockOfficeFloorSource.specification = specification;
		}

		/**
		 * Default constructor.
		 */
		public MockOfficeFloorSource() {
			// Determine if fail to instantiate
			if (instantiateFailure != null) {
				throw instantiateFailure;
			}
		}

		/*
		 * ================ OfficeFloorSource ================================
		 */

		@Override
		public OfficeFloorSourceSpecification getSpecification() {

			// Determine if failure to obtain
			if (specificationFailure != null) {
				throw specificationFailure;
			}

			// Return the specification
			return specification;
		}

		@Override
		public void specifyConfigurationProperties(RequiredProperties requiredProperties,
				OfficeFloorSourceContext context) throws Exception {
			fail("Should not be invoked for obtaining specification");
		}

		@Override
		public void sourceOfficeFloor(OfficeFloorDeployer officeFloorDeployer, OfficeFloorSourceContext context)
				throws Exception {
			fail("Should not be invoked for obtaining specification");
		}
	}

}
