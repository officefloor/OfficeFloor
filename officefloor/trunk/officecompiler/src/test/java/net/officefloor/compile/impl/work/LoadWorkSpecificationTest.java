/*
 *  Office Floor, Application Server
 *  Copyright (C) 2006 Daniel Sagenschneider
 *
 *  This program is free software; you can redistribute it and/or modify it under the terms 
 *  of the GNU General Public License as published by the Free Software Foundation; either 
 *  version 2 of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with this program; 
 *  if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, 
 *  MA 02111-1307 USA
 */
package net.officefloor.compile.impl.work;

import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.compile.issues.CompilerIssues;
import net.officefloor.compile.issues.CompilerIssues.LocationType;
import net.officefloor.compile.properties.Property;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.work.source.WorkSource;
import net.officefloor.compile.spi.work.source.WorkSourceContext;
import net.officefloor.compile.spi.work.source.WorkSourceProperty;
import net.officefloor.compile.spi.work.source.WorkSourceSpecification;
import net.officefloor.compile.spi.work.source.WorkTypeBuilder;
import net.officefloor.compile.test.properties.PropertyListUtil;
import net.officefloor.compile.work.WorkLoader;
import net.officefloor.frame.api.build.OfficeFloorIssues.AssetType;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.spi.TestSource;
import net.officefloor.frame.test.OfficeFrameTestCase;

/**
 * Tests the {@link WorkLoader}.
 * 
 * @author Daniel
 */
public class LoadWorkSpecificationTest extends OfficeFrameTestCase {

	/**
	 * {@link CompilerIssues}.
	 */
	private final CompilerIssues issues = this.createMock(CompilerIssues.class);

	/**
	 * {@link WorkSourceSpecification}.
	 */
	private final WorkSourceSpecification specification = this
			.createMock(WorkSourceSpecification.class);

	/*
	 * (non-Javadoc)
	 * 
	 * @see junit.framework.TestCase#setUp()
	 */
	@Override
	protected void setUp() throws Exception {
		MockWorkSource.reset(this.specification);
	}

	/**
	 * Ensures issue if fails to instantiate the {@link WorkSource}.
	 */
	public void testFailInstantiateForWorkSpecification() {

		final RuntimeException failure = new RuntimeException(
				"instantiate failure");

		// Record failure to instantiate
		this.record_issue("Failed to instantiate "
				+ MockWorkSource.class.getName() + " by default constructor",
				failure);

		// Attempt to obtain specification
		MockWorkSource.instantiateFailure = failure;
		this.replayMockObjects();
		this.loadSpecification(false);
		this.verifyMockObjects();
	}

	/**
	 * Ensures issue if failure in obtaining the {@link WorkSourceSpecification}
	 * .
	 */
	public void testFailGetWorkSpecification() {

		final Error failure = new Error("specification failure");

		// Record failure to instantiate
		this.record_issue("Failed to obtain WorkSourceSpecification from "
				+ MockWorkSource.class.getName(), failure);

		// Attempt to obtain specification
		MockWorkSource.specificationFailure = failure;
		this.replayMockObjects();
		this.loadSpecification(false);
		this.verifyMockObjects();
	}

	/**
	 * Ensures issue if no {@link WorkSourceSpecification} obtained.
	 */
	public void testNoWorkSpecification() {

		// Record no specification returned
		this.record_issue("No WorkSourceSpecification returned from "
				+ MockWorkSource.class.getName());

		// Attempt to obtain specification
		MockWorkSource.specification = null;
		this.replayMockObjects();
		this.loadSpecification(false);
		this.verifyMockObjects();
	}

	/**
	 * Ensures issue if fails to obtain the {@link WorkSourceProperty}
	 * instances.
	 */
	public void testFailGetWorkProperties() {

		final NullPointerException failure = new NullPointerException(
				"Fail to get work properties");

		// Record null work properties
		this.control(this.specification).expectAndThrow(
				this.specification.getProperties(), failure);
		this
				.record_issue(
						"Failed to obtain WorkSourceProperty instances from WorkSourceSpecification for "
								+ MockWorkSource.class.getName(), failure);

		// Attempt to obtain specification
		this.replayMockObjects();
		this.loadSpecification(false);
		this.verifyMockObjects();
	}

	/**
	 * Ensures considers null {@link WorkSourceProperty} array as no properties.
	 */
	public void testNullWorkPropertiesArray() {

		// Record null work properties
		this.recordReturn(this.specification, this.specification
				.getProperties(), null);

		// Attempt to obtain specification
		this.replayMockObjects();
		this.loadSpecification(true);
		this.verifyMockObjects();
	}

	/**
	 * Ensures issue if element in {@link WorkSourceProperty} array is null.
	 */
	public void testNullWorkPropertyElement() {

		// Record null work properties
		this.recordReturn(this.specification, this.specification
				.getProperties(), new WorkSourceProperty[] { null });
		this
				.record_issue("WorkSourceProperty 0 is null from WorkSourceSpecification for "
						+ MockWorkSource.class.getName());

		// Attempt to obtain specification
		this.replayMockObjects();
		this.loadSpecification(false);
		this.verifyMockObjects();
	}

	/**
	 * Ensures issue if <code>null</code> {@link WorkSourceProperty} name.
	 */
	public void testNullWorkPropertyName() {

		final WorkSourceProperty property = this
				.createMock(WorkSourceProperty.class);

		// Record obtaining work properties
		this.recordReturn(this.specification, this.specification
				.getProperties(), new WorkSourceProperty[] { property });
		this.recordReturn(property, property.getName(), "");
		this
				.record_issue("WorkSourceProperty 0 provided blank name from WorkSourceSpecification for "
						+ MockWorkSource.class.getName());

		// Attempt to obtain specification
		this.replayMockObjects();
		this.loadSpecification(false);
		this.verifyMockObjects();
	}

	/**
	 * Ensures issue if fails to get the {@link WorkSourceProperty} name.
	 */
	public void testFailGetWorkPropertyName() {

		final RuntimeException failure = new RuntimeException(
				"Failed to get property name");
		final WorkSourceProperty property = this
				.createMock(WorkSourceProperty.class);

		// Record obtaining work properties
		this.recordReturn(this.specification, this.specification
				.getProperties(), new WorkSourceProperty[] { property });
		this.control(property).expectAndThrow(property.getName(), failure);
		this.record_issue(
				"Failed to get name for WorkSourceProperty 0 from WorkSourceSpecification for "
						+ MockWorkSource.class.getName(), failure);

		// Attempt to obtain specification
		this.replayMockObjects();
		this.loadSpecification(false);
		this.verifyMockObjects();
	}

	/**
	 * Ensures issue if fails to get the {@link WorkSourceProperty} label.
	 */
	public void testFailGetWorkPropertyLabel() {

		final RuntimeException failure = new RuntimeException(
				"Failed to get property label");
		final WorkSourceProperty property = this
				.createMock(WorkSourceProperty.class);

		// Record obtaining work properties
		this.recordReturn(this.specification, this.specification
				.getProperties(), new WorkSourceProperty[] { property });
		this.recordReturn(property, property.getName(), "NAME");
		this.control(property).expectAndThrow(property.getLabel(), failure);
		this
				.record_issue(
						"Failed to get label for WorkSourceProperty 0 (NAME) from WorkSourceSpecification for "
								+ MockWorkSource.class.getName(), failure);

		// Attempt to obtain specification
		this.replayMockObjects();
		this.loadSpecification(false);
		this.verifyMockObjects();
	}

	/**
	 * Ensures able to load the {@link WorkSourceSpecification}.
	 */
	public void testLoadWorkSpecification() {

		final WorkSourceProperty propertyWithLabel = this
				.createMock(WorkSourceProperty.class);
		final WorkSourceProperty propertyWithoutLabel = this
				.createMock(WorkSourceProperty.class);

		// Record obtaining work properties
		this.recordReturn(this.specification, this.specification
				.getProperties(), new WorkSourceProperty[] { propertyWithLabel,
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
		this.issues.addIssue(LocationType.SECTION, null, AssetType.WORK, null,
				issueDescription);
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
		this.issues.addIssue(LocationType.SECTION, null, AssetType.WORK, null,
				issueDescription, cause);
	}

	/**
	 * Loads the {@link WorkSourceSpecification}.
	 * 
	 * @param isExpectToLoad
	 *            Flag indicating if expect to obtain the
	 *            {@link WorkSourceSpecification}.
	 * @param propertyNames
	 *            Expected {@link Property} names for being returned.
	 */
	private void loadSpecification(boolean isExpectToLoad,
			String... propertyNameLabelPairs) {

		// Load the work specification
		OfficeFloorCompiler compiler = OfficeFloorCompiler
				.newOfficeFloorCompiler();
		compiler.setCompilerIssues(this.issues);
		WorkLoader workLoader = compiler.getWorkLoader();
		PropertyList propertyList = workLoader
				.loadSpecification(MockWorkSource.class);

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
	 * Mock {@link WorkSource} for testing.
	 */
	@TestSource
	public static class MockWorkSource implements WorkSource<Work> {

		/**
		 * Failure to instantiate an instance.
		 */
		public static RuntimeException instantiateFailure = null;

		/**
		 * Failure to obtain the {@link WorkSourceSpecification}.
		 */
		public static Error specificationFailure = null;

		/**
		 * {@link WorkSourceSpecification}.
		 */
		public static WorkSourceSpecification specification;

		/**
		 * Resets the state for next test.
		 * 
		 * @param specification
		 *            {@link WorkSourceSpecification}.
		 */
		public static void reset(WorkSourceSpecification specification) {
			instantiateFailure = null;
			specificationFailure = null;
			MockWorkSource.specification = specification;
		}

		/**
		 * Default constructor.
		 */
		public MockWorkSource() {
			// Determine if fail to instantiate
			if (instantiateFailure != null) {
				throw instantiateFailure;
			}
		}

		/*
		 * ================ WorkSource ================================
		 */

		@Override
		public WorkSourceSpecification getSpecification() {

			// Determine if failure to obtain
			if (specificationFailure != null) {
				throw specificationFailure;
			}

			// Return the specification
			return specification;
		}

		@Override
		public void sourceWork(WorkTypeBuilder<Work> workTypeBuilder,
				WorkSourceContext context) throws Exception {
			fail("Should not be invoked for obtaining specification");
		}
	}

}