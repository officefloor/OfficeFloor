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

package net.officefloor.compile.impl.governance;

import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.compile.governance.GovernanceLoader;
import net.officefloor.compile.issues.CompilerIssues;
import net.officefloor.compile.properties.Property;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.governance.source.GovernanceSource;
import net.officefloor.compile.spi.governance.source.GovernanceSourceContext;
import net.officefloor.compile.spi.governance.source.GovernanceSourceMetaData;
import net.officefloor.compile.spi.governance.source.GovernanceSourceProperty;
import net.officefloor.compile.spi.governance.source.GovernanceSourceSpecification;
import net.officefloor.compile.test.issues.MockCompilerIssues;
import net.officefloor.compile.test.properties.PropertyListUtil;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.source.TestSource;
import net.officefloor.frame.test.OfficeFrameTestCase;

/**
 * Tests the {@link GovernanceLoader}.
 * 
 * @author Daniel Sagenschneider
 */
public class LoadGovernanceSourceSpecificationTest extends OfficeFrameTestCase {

	/**
	 * {@link CompilerIssues}.
	 */
	private final MockCompilerIssues issues = new MockCompilerIssues(this);

	/**
	 * {@link GovernanceSourceSpecification}.
	 */
	private final GovernanceSourceSpecification specification = this.createMock(GovernanceSourceSpecification.class);

	@Override
	protected void setUp() throws Exception {
		MockGovernanceSource.reset(this.specification);
	}

	/**
	 * Ensures issue if fails to instantiate the {@link GovernanceSource}.
	 */
	public void testFailInstantiateForGovernanceSourceSpecification() {

		final RuntimeException failure = new RuntimeException("instantiate failure");

		// Record failure to instantiate
		this.issues.recordIssue(
				"Failed to instantiate " + MockGovernanceSource.class.getName() + " by default constructor", failure);

		// Attempt to obtain specification
		MockGovernanceSource.instantiateFailure = failure;
		this.replayMockObjects();
		this.loadSpecification(false);
		this.verifyMockObjects();
	}

	/**
	 * Ensures issue if failure in obtaining the
	 * {@link GovernanceSourceSpecification}.
	 */
	public void testFailGetGovernanceSourceSpecification() {

		final Error failure = new Error("specification failure");

		// Record failure to instantiate
		this.issues.recordIssue(
				"Failed to obtain GovernanceSourceSpecification from " + MockGovernanceSource.class.getName(), failure);

		// Attempt to obtain specification
		MockGovernanceSource.specificationFailure = failure;
		this.replayMockObjects();
		this.loadSpecification(false);
		this.verifyMockObjects();
	}

	/**
	 * Ensures issue if no {@link GovernanceSourceSpecification} obtained.
	 */
	public void testNoGovernanceSourceSpecification() {

		// Record no specification returned
		this.issues
				.recordIssue("No GovernanceSourceSpecification returned from " + MockGovernanceSource.class.getName());

		// Attempt to obtain specification
		MockGovernanceSource.specification = null;
		this.replayMockObjects();
		this.loadSpecification(false);
		this.verifyMockObjects();
	}

	/**
	 * Ensures issue if fails to obtain the {@link GovernanceSourceProperty}
	 * instances.
	 */
	public void testFailGetGovernanceSourceProperties() {

		final NullPointerException failure = new NullPointerException("Fail to get managed object source properties");

		// Record null properties
		this.recordThrows(this.specification, this.specification.getProperties(), failure);
		this.issues.recordIssue(
				"Failed to obtain GovernanceSourceProperty instances from GovernanceSourceSpecification for "
						+ MockGovernanceSource.class.getName(),
				failure);

		// Attempt to obtain specification
		this.replayMockObjects();
		this.loadSpecification(false);
		this.verifyMockObjects();
	}

	/**
	 * Ensures considers null {@link GovernanceSourceProperty} array as no
	 * properties.
	 */
	public void testNullGovernanceSourcePropertiesArray() {

		// Record null properties
		this.recordReturn(this.specification, this.specification.getProperties(), null);

		// Attempt to obtain specification
		this.replayMockObjects();
		this.loadSpecification(true);
		this.verifyMockObjects();
	}

	/**
	 * Ensures issue if element in {@link GovernanceSourceProperty} array is null.
	 */
	public void testNullGovernanceSourcePropertyElement() {

		// Record null properties
		this.recordReturn(this.specification, this.specification.getProperties(),
				new GovernanceSourceProperty[] { null });
		this.issues.recordIssue("GovernanceSourceProperty 0 is null from GovernanceSourceSpecification for "
				+ MockGovernanceSource.class.getName());

		// Attempt to obtain specification
		this.replayMockObjects();
		this.loadSpecification(false);
		this.verifyMockObjects();
	}

	/**
	 * Ensures issue if <code>null</code> {@link GovernanceSourceProperty} name.
	 */
	public void testNullGovernanceSourcePropertyName() {

		final GovernanceSourceProperty property = this.createMock(GovernanceSourceProperty.class);

		// Record obtaining properties
		this.recordReturn(this.specification, this.specification.getProperties(),
				new GovernanceSourceProperty[] { property });
		this.recordReturn(property, property.getName(), "");
		this.issues.recordIssue("GovernanceSourceProperty 0 provided blank name from GovernanceSourceSpecification for "
				+ MockGovernanceSource.class.getName());

		// Attempt to obtain specification
		this.replayMockObjects();
		this.loadSpecification(false);
		this.verifyMockObjects();
	}

	/**
	 * Ensures issue if fails to get the {@link GovernanceSourceProperty} name.
	 */
	public void testFailGetGovernanceSourcePropertyName() {

		final RuntimeException failure = new RuntimeException("Failed to get property name");
		final GovernanceSourceProperty property = this.createMock(GovernanceSourceProperty.class);

		// Record obtaining properties
		this.recordReturn(this.specification, this.specification.getProperties(),
				new GovernanceSourceProperty[] { property });
		this.recordThrows(property, property.getName(), failure);
		this.issues
				.recordIssue("Failed to get name for GovernanceSourceProperty 0 from GovernanceSourceSpecification for "
						+ MockGovernanceSource.class.getName(), failure);

		// Attempt to obtain specification
		this.replayMockObjects();
		this.loadSpecification(false);
		this.verifyMockObjects();
	}

	/**
	 * Ensures issue if fails to get the {@link GovernanceSourceProperty} label.
	 */
	public void testFailGetGovernanceSourcePropertyLabel() {

		final RuntimeException failure = new RuntimeException("Failed to get property label");
		final GovernanceSourceProperty property = this.createMock(GovernanceSourceProperty.class);

		// Record obtaining properties
		this.recordReturn(this.specification, this.specification.getProperties(),
				new GovernanceSourceProperty[] { property });
		this.recordReturn(property, property.getName(), "NAME");
		this.recordThrows(property, property.getLabel(), failure);
		this.issues.recordIssue(
				"Failed to get label for GovernanceSourceProperty 0 (NAME) from GovernanceSourceSpecification for "
						+ MockGovernanceSource.class.getName(),
				failure);

		// Attempt to obtain specification
		this.replayMockObjects();
		this.loadSpecification(false);
		this.verifyMockObjects();
	}

	/**
	 * Ensures able to load the {@link GovernanceSourceSpecification}.
	 */
	public void testLoadGovernanceSourceSpecification() {

		final GovernanceSourceProperty propertyWithLabel = this.createMock(GovernanceSourceProperty.class);
		final GovernanceSourceProperty propertyWithoutLabel = this.createMock(GovernanceSourceProperty.class);

		// Record obtaining properties
		this.recordReturn(this.specification, this.specification.getProperties(),
				new GovernanceSourceProperty[] { propertyWithLabel, propertyWithoutLabel });
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
	 * Loads the {@link GovernanceSourceSpecification}.
	 * 
	 * @param isExpectToLoad Flag indicating if expect to obtain the
	 *                       {@link GovernanceSourceSpecification}.
	 * @param propertyNames  Expected {@link Property} names for being returned.
	 */
	private void loadSpecification(boolean isExpectToLoad, String... propertyNameLabelPairs) {

		// Load the managed object specification specification
		OfficeFloorCompiler compiler = OfficeFloorCompiler.newOfficeFloorCompiler(null);
		compiler.setCompilerIssues(this.issues);
		GovernanceLoader governanceLoader = compiler.getGovernanceLoader();
		PropertyList propertyList = governanceLoader.loadSpecification(MockGovernanceSource.class);

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
	 * Mock {@link GovernanceSource} for testing.
	 */
	@TestSource
	public static class MockGovernanceSource implements GovernanceSource<Object, None> {

		/**
		 * Failure to instantiate an instance.
		 */
		public static RuntimeException instantiateFailure = null;

		/**
		 * Failure to obtain the {@link GovernanceSourceSpecification}.
		 */
		public static Error specificationFailure = null;

		/**
		 * {@link GovernanceSourceSpecification}.
		 */
		public static GovernanceSourceSpecification specification;

		/**
		 * Resets the state for next test.
		 * 
		 * @param specification {@link GovernanceSourceSpecification}.
		 */
		public static void reset(GovernanceSourceSpecification specification) {
			instantiateFailure = null;
			specificationFailure = null;
			MockGovernanceSource.specification = specification;
		}

		/**
		 * Default constructor.
		 */
		public MockGovernanceSource() {
			// Determine if fail to instantiate
			if (instantiateFailure != null) {
				throw instantiateFailure;
			}
		}

		/*
		 * ================ GovernanceSource ================================
		 */

		@Override
		public GovernanceSourceSpecification getSpecification() {
			// Determine if failure to obtain
			if (specificationFailure != null) {
				throw specificationFailure;
			}

			// Return the specification
			return specification;
		}

		@Override
		public GovernanceSourceMetaData<Object, None> init(GovernanceSourceContext context) throws Exception {
			fail("Should not be invoked for obtaining specification");
			return null;
		}
	}

}
