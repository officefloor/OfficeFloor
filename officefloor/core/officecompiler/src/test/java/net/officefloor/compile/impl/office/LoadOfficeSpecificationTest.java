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

package net.officefloor.compile.impl.office;

import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.compile.issues.CompilerIssues;
import net.officefloor.compile.office.OfficeLoader;
import net.officefloor.compile.properties.Property;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.compile.spi.office.source.OfficeSource;
import net.officefloor.compile.spi.office.source.OfficeSourceContext;
import net.officefloor.compile.spi.office.source.OfficeSourceProperty;
import net.officefloor.compile.spi.office.source.OfficeSourceSpecification;
import net.officefloor.compile.test.issues.MockCompilerIssues;
import net.officefloor.compile.test.properties.PropertyListUtil;
import net.officefloor.frame.api.source.TestSource;
import net.officefloor.frame.test.OfficeFrameTestCase;

/**
 * Tests the {@link OfficeLoader}.
 * 
 * @author Daniel Sagenschneider
 */
public class LoadOfficeSpecificationTest extends OfficeFrameTestCase {

	/**
	 * {@link CompilerIssues}.
	 */
	private final MockCompilerIssues issues = new MockCompilerIssues(this);

	/**
	 * {@link OfficeSourceSpecification}.
	 */
	private final OfficeSourceSpecification specification = this.createMock(OfficeSourceSpecification.class);

	@Override
	protected void setUp() throws Exception {
		MockOfficeSource.reset(this.specification);
	}

	/**
	 * Ensures issue if fails to instantiate the {@link OfficeSource}.
	 */
	public void testFailInstantiateForOfficeSpecification() {

		final RuntimeException failure = new RuntimeException("instantiate failure");

		// Record failure to instantiate
		this.issues.recordIssue("Failed to instantiate " + MockOfficeSource.class.getName() + " by default constructor",
				failure);

		// Attempt to obtain specification
		MockOfficeSource.instantiateFailure = failure;
		this.replayMockObjects();
		this.loadSpecification(false);
		this.verifyMockObjects();
	}

	/**
	 * Ensures issue if failure in obtaining the {@link OfficeSourceSpecification}.
	 */
	public void testFailGetOfficeSpecification() {

		final Error failure = new Error("specification failure");

		// Record failure to instantiate
		this.issues.recordIssue("Failed to obtain OfficeSourceSpecification from " + MockOfficeSource.class.getName(),
				failure);

		// Attempt to obtain specification
		MockOfficeSource.specificationFailure = failure;
		this.replayMockObjects();
		this.loadSpecification(false);
		this.verifyMockObjects();
	}

	/**
	 * Ensures issue if no {@link OfficeSourceSpecification} obtained.
	 */
	public void testNoOfficeSpecification() {

		// Record no specification returned
		this.issues.recordIssue("No OfficeSourceSpecification returned from " + MockOfficeSource.class.getName());

		// Attempt to obtain specification
		MockOfficeSource.specification = null;
		this.replayMockObjects();
		this.loadSpecification(false);
		this.verifyMockObjects();
	}

	/**
	 * Ensures issue if fails to obtain the {@link OfficeSourceProperty} instances.
	 */
	public void testFailGetOfficeProperties() {

		final NullPointerException failure = new NullPointerException("Fail to get office properties");

		// Record null office properties
		this.recordThrows(this.specification, this.specification.getProperties(), failure);
		this.issues.recordIssue("Failed to obtain OfficeSourceProperty instances from OfficeSourceSpecification for "
				+ MockOfficeSource.class.getName(), failure);

		// Attempt to obtain specification
		this.replayMockObjects();
		this.loadSpecification(false);
		this.verifyMockObjects();
	}

	/**
	 * Ensures considers null {@link OfficeSourceProperty} array as no properties.
	 */
	public void testNullOfficePropertiesArray() {

		// Record null office properties
		this.recordReturn(this.specification, this.specification.getProperties(), null);

		// Attempt to obtain specification
		this.replayMockObjects();
		this.loadSpecification(true);
		this.verifyMockObjects();
	}

	/**
	 * Ensures issue if element in {@link OfficeSourceProperty} array is null.
	 */
	public void testNullOfficePropertyElement() {

		// Record null office properties
		this.recordReturn(this.specification, this.specification.getProperties(), new OfficeSourceProperty[] { null });
		this.issues.recordIssue("OfficeSourceProperty 0 is null from OfficeSourceSpecification for "
				+ MockOfficeSource.class.getName());

		// Attempt to obtain specification
		this.replayMockObjects();
		this.loadSpecification(false);
		this.verifyMockObjects();
	}

	/**
	 * Ensures issue if <code>null</code> {@link OfficeSourceProperty} name.
	 */
	public void testNullOfficePropertyName() {

		final OfficeSourceProperty property = this.createMock(OfficeSourceProperty.class);

		// Record obtaining office properties
		this.recordReturn(this.specification, this.specification.getProperties(),
				new OfficeSourceProperty[] { property });
		this.recordReturn(property, property.getName(), "");
		this.issues.recordIssue("OfficeSourceProperty 0 provided blank name from OfficeSourceSpecification for "
				+ MockOfficeSource.class.getName());

		// Attempt to obtain specification
		this.replayMockObjects();
		this.loadSpecification(false);
		this.verifyMockObjects();
	}

	/**
	 * Ensures issue if fails to get the {@link OfficeSourceProperty} name.
	 */
	public void testFailGetOfficePropertyName() {

		final RuntimeException failure = new RuntimeException("Failed to get property name");
		final OfficeSourceProperty property = this.createMock(OfficeSourceProperty.class);

		// Record obtaining office properties
		this.recordReturn(this.specification, this.specification.getProperties(),
				new OfficeSourceProperty[] { property });
		this.recordThrows(property, property.getName(), failure);
		this.issues.recordIssue("Failed to get name for OfficeSourceProperty 0 from OfficeSourceSpecification for "
				+ MockOfficeSource.class.getName(), failure);

		// Attempt to obtain specification
		this.replayMockObjects();
		this.loadSpecification(false);
		this.verifyMockObjects();
	}

	/**
	 * Ensures issue if fails to get the {@link OfficeSourceProperty} label.
	 */
	public void testFailGetOfficePropertyLabel() {

		final RuntimeException failure = new RuntimeException("Failed to get property label");
		final OfficeSourceProperty property = this.createMock(OfficeSourceProperty.class);

		// Record obtaining office properties
		this.recordReturn(this.specification, this.specification.getProperties(),
				new OfficeSourceProperty[] { property });
		this.recordReturn(property, property.getName(), "NAME");
		this.recordThrows(property, property.getLabel(), failure);
		this.issues
				.recordIssue("Failed to get label for OfficeSourceProperty 0 (NAME) from OfficeSourceSpecification for "
						+ MockOfficeSource.class.getName(), failure);

		// Attempt to obtain specification
		this.replayMockObjects();
		this.loadSpecification(false);
		this.verifyMockObjects();
	}

	/**
	 * Ensures able to load the {@link OfficeSourceSpecification}.
	 */
	public void testLoadOfficeSpecification() {

		final OfficeSourceProperty propertyWithLabel = this.createMock(OfficeSourceProperty.class);
		final OfficeSourceProperty propertyWithoutLabel = this.createMock(OfficeSourceProperty.class);

		// Record obtaining office properties
		this.recordReturn(this.specification, this.specification.getProperties(),
				new OfficeSourceProperty[] { propertyWithLabel, propertyWithoutLabel });
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
	 * Loads the {@link OfficeSourceSpecification}.
	 * 
	 * @param isExpectToLoad Flag indicating if expect to obtain the
	 *                       {@link OfficeSourceSpecification}.
	 * @param propertyNames  Expected {@link Property} names for being returned.
	 */
	private void loadSpecification(boolean isExpectToLoad, String... propertyNameLabelPairs) {

		// Load the office specification
		OfficeFloorCompiler compiler = OfficeFloorCompiler.newOfficeFloorCompiler(null);
		compiler.setCompilerIssues(this.issues);
		OfficeLoader officeLoader = compiler.getOfficeLoader();
		PropertyList propertyList = officeLoader.loadSpecification(MockOfficeSource.class);

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
	 * Mock {@link OfficeSource} for testing.
	 */
	@TestSource
	public static class MockOfficeSource implements OfficeSource {

		/**
		 * Failure to instantiate an instance.
		 */
		public static RuntimeException instantiateFailure = null;

		/**
		 * Failure to obtain the {@link OfficeSourceSpecification}.
		 */
		public static Error specificationFailure = null;

		/**
		 * {@link OfficeSourceSpecification}.
		 */
		public static OfficeSourceSpecification specification;

		/**
		 * Resets the state for next test.
		 * 
		 * @param specification {@link OfficeSourceSpecification}.
		 */
		public static void reset(OfficeSourceSpecification specification) {
			instantiateFailure = null;
			specificationFailure = null;
			MockOfficeSource.specification = specification;
		}

		/**
		 * Default constructor.
		 */
		public MockOfficeSource() {
			// Determine if fail to instantiate
			if (instantiateFailure != null) {
				throw instantiateFailure;
			}
		}

		/*
		 * ================ OfficeSource ================================
		 */

		@Override
		public OfficeSourceSpecification getSpecification() {

			// Determine if failure to obtain
			if (specificationFailure != null) {
				throw specificationFailure;
			}

			// Return the specification
			return specification;
		}

		@Override
		public void sourceOffice(OfficeArchitect officeArchitect, OfficeSourceContext context) throws Exception {
			fail("Should not be invoked for obtaining specification");
		}
	}

}
