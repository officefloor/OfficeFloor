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
package net.officefloor.compile.impl.section;

import java.util.List;

import net.officefloor.compile.issues.CompilerIssues;
import net.officefloor.compile.issues.CompilerIssues.LocationType;
import net.officefloor.compile.properties.Property;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.section.SectionLoader;
import net.officefloor.compile.spi.section.Section;
import net.officefloor.compile.spi.section.source.SectionSource;
import net.officefloor.compile.spi.section.source.SectionSourceContext;
import net.officefloor.compile.spi.section.source.SectionSourceProperty;
import net.officefloor.compile.spi.section.source.SectionSourceSpecification;
import net.officefloor.compile.spi.section.source.SectionTypeBuilder;
import net.officefloor.frame.test.OfficeFrameTestCase;

/**
 * Tests the {@link SectionLoader}.
 * 
 * @author Daniel
 */
public class LoadSectionSpecificationTest extends OfficeFrameTestCase {

	/**
	 * Location of the {@link Section}.
	 */
	private final String SECTION_LOCATION = "SECTION";

	/**
	 * {@link CompilerIssues}.
	 */
	private final CompilerIssues issues = this.createMock(CompilerIssues.class);

	/**
	 * {@link SectionSourceSpecification}.
	 */
	private final SectionSourceSpecification specification = this
			.createMock(SectionSourceSpecification.class);

	/*
	 * (non-Javadoc)
	 * 
	 * @see junit.framework.TestCase#setUp()
	 */
	@Override
	protected void setUp() throws Exception {
		MockSectionSource.reset(this.specification);
	}

	/**
	 * Ensures issue if fails to instantiate the {@link SectionSource}.
	 */
	public void testFailInstantiateForSectionSpecification() {

		final RuntimeException failure = new RuntimeException(
				"instantiate failure");

		// Record failure to instantiate
		this.record_issue(
				"Failed to instantiate " + MockSectionSource.class.getName()
						+ " by default constructor", failure);

		// Attempt to obtain specification
		MockSectionSource.instantiateFailure = failure;
		this.replayMockObjects();
		this.loadSpecification(false);
		this.verifyMockObjects();
	}

	/**
	 * Ensures issue if failure in obtaining the
	 * {@link SectionSourceSpecification}.
	 */
	public void testFailGetSectionSpecification() {

		final Error failure = new Error("specification failure");

		// Record failure to instantiate
		this.record_issue("Failed to obtain SectionSourceSpecification from "
				+ MockSectionSource.class.getName(), failure);

		// Attempt to obtain specification
		MockSectionSource.specificationFailure = failure;
		this.replayMockObjects();
		this.loadSpecification(false);
		this.verifyMockObjects();
	}

	/**
	 * Ensures issue if no {@link SectionSourceSpecification} obtained.
	 */
	public void testNoSectionSpecification() {

		// Record no specification returned
		this.record_issue("No SectionSourceSpecification returned from "
				+ MockSectionSource.class.getName());

		// Attempt to obtain specification
		MockSectionSource.specification = null;
		this.replayMockObjects();
		this.loadSpecification(false);
		this.verifyMockObjects();
	}

	/**
	 * Ensures issue if fails to obtain the {@link SectionSourceProperty}
	 * instances.
	 */
	public void testFailGetSectionProperties() {

		final NullPointerException failure = new NullPointerException(
				"Fail to get section properties");

		// Record null section properties
		this.control(this.specification).expectAndThrow(
				this.specification.getProperties(), failure);
		this
				.record_issue(
						"Failed to obtain SectionSourceProperty instances from SectionSourceSpecification for "
								+ MockSectionSource.class.getName(), failure);

		// Attempt to obtain specification
		this.replayMockObjects();
		this.loadSpecification(false);
		this.verifyMockObjects();
	}

	/**
	 * Ensures considers null {@link SectionSourceProperty} array as no
	 * properties.
	 */
	public void testNullSectionPropertiesArray() {

		// Record null section properties
		this.recordReturn(this.specification, this.specification
				.getProperties(), null);

		// Attempt to obtain specification
		this.replayMockObjects();
		this.loadSpecification(true);
		this.verifyMockObjects();
	}

	/**
	 * Ensures issue if element in {@link SectionSourceProperty} array is null.
	 */
	public void testNullSectionPropertyElement() {

		// Record null section properties
		this.recordReturn(this.specification, this.specification
				.getProperties(), new SectionSourceProperty[] { null });
		this
				.record_issue("SectionSourceProperty 0 is null from SectionSourceSpecification for "
						+ MockSectionSource.class.getName());

		// Attempt to obtain specification
		this.replayMockObjects();
		this.loadSpecification(false);
		this.verifyMockObjects();
	}

	/**
	 * Ensures issue if <code>null</code> {@link SectionSourceProperty} name.
	 */
	public void testNullSectionPropertyName() {

		final SectionSourceProperty property = this
				.createMock(SectionSourceProperty.class);

		// Record obtaining section properties
		this.recordReturn(this.specification, this.specification
				.getProperties(), new SectionSourceProperty[] { property });
		this.recordReturn(property, property.getName(), "");
		this
				.record_issue("SectionSourceProperty 0 provided blank name from SectionSourceSpecification for "
						+ MockSectionSource.class.getName());

		// Attempt to obtain specification
		this.replayMockObjects();
		this.loadSpecification(false);
		this.verifyMockObjects();
	}

	/**
	 * Ensures issue if fails to get the {@link SectionSourceProperty} name.
	 */
	public void testFailGetSectionPropertyName() {

		final RuntimeException failure = new RuntimeException(
				"Failed to get property name");
		final SectionSourceProperty property = this
				.createMock(SectionSourceProperty.class);

		// Record obtaining section properties
		this.recordReturn(this.specification, this.specification
				.getProperties(), new SectionSourceProperty[] { property });
		this.control(property).expectAndThrow(property.getName(), failure);
		this
				.record_issue(
						"Failed to get name for SectionSourceProperty 0 from SectionSourceSpecification for "
								+ MockSectionSource.class.getName(), failure);

		// Attempt to obtain specification
		this.replayMockObjects();
		this.loadSpecification(false);
		this.verifyMockObjects();
	}

	/**
	 * Ensures issue if fails to get the {@link SectionSourceProperty} label.
	 */
	public void testFailGetSectionPropertyLabel() {

		final RuntimeException failure = new RuntimeException(
				"Failed to get property label");
		final SectionSourceProperty property = this
				.createMock(SectionSourceProperty.class);

		// Record obtaining section properties
		this.recordReturn(this.specification, this.specification
				.getProperties(), new SectionSourceProperty[] { property });
		this.recordReturn(property, property.getName(), "NAME");
		this.control(property).expectAndThrow(property.getLabel(), failure);
		this
				.record_issue(
						"Failed to get label for SectionSourceProperty 0 (NAME) from SectionSourceSpecification for "
								+ MockSectionSource.class.getName(), failure);

		// Attempt to obtain specification
		this.replayMockObjects();
		this.loadSpecification(false);
		this.verifyMockObjects();
	}

	/**
	 * Ensures able to load the {@link SectionSourceSpecification}.
	 */
	public void testLoadSectionSpecification() {

		final SectionSourceProperty propertyWithLabel = this
				.createMock(SectionSourceProperty.class);
		final SectionSourceProperty propertyWithoutLabel = this
				.createMock(SectionSourceProperty.class);

		// Record obtaining section properties
		this.recordReturn(this.specification, this.specification
				.getProperties(), new SectionSourceProperty[] {
				propertyWithLabel, propertyWithoutLabel });
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
		this.issues.addIssue(LocationType.SECTION, SECTION_LOCATION, null,
				null, issueDescription);
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
		this.issues.addIssue(LocationType.SECTION, SECTION_LOCATION, null,
				null, issueDescription, cause);
	}

	/**
	 * Loads the {@link SectionSourceSpecification}.
	 * 
	 * @param isExpectToLoad
	 *            Flag indicating if expect to obtain the
	 *            {@link SectionSourceSpecification}.
	 * @param propertyNames
	 *            Expected {@link Property} names for being returned.
	 */
	private void loadSpecification(boolean isExpectToLoad,
			String... propertyNameLabelPairs) {

		// Load the section specification
		SectionLoader sectionLoader = new SectionLoaderImpl(SECTION_LOCATION);
		PropertyList propertyList = sectionLoader.loadSpecification(
				MockSectionSource.class, this.issues);

		// Determine if expected to load
		if (isExpectToLoad) {
			assertNotNull("Expected to load specification", propertyList);

			// Ensure the properties are as expected
			List<Property> properties = propertyList.getPropertyList();
			assertEquals("Incorrect number of properties",
					(propertyNameLabelPairs.length / 2), properties.size());
			for (int i = 0; i < propertyNameLabelPairs.length; i += 2) {
				String propertyName = propertyNameLabelPairs[i];
				String propertyLabel = propertyNameLabelPairs[i + 1];
				Property property = properties.get(i / 2);
				assertEquals("Incorrect name for property " + (i / 2),
						propertyName, property.getName());
				assertEquals("Incorrect label for property " + (i / 2),
						propertyLabel, property.getLabel());
				assertEquals("Should be blank value for property " + (i / 2),
						null, property.getValue());
			}

		} else {
			assertNull("Should not load specification", propertyList);
		}
	}

	/**
	 * Mock {@link SectionSource} for testing.
	 */
	public static class MockSectionSource implements SectionSource {

		/**
		 * Failure to instantiate an instance.
		 */
		public static RuntimeException instantiateFailure = null;

		/**
		 * Failure to obtain the {@link SectionSourceSpecification}.
		 */
		public static Error specificationFailure = null;

		/**
		 * {@link SectionSourceSpecification}.
		 */
		public static SectionSourceSpecification specification;

		/**
		 * Resets the state for next test.
		 * 
		 * @param specification
		 *            {@link SectionSourceSpecification}.
		 */
		public static void reset(SectionSourceSpecification specification) {
			instantiateFailure = null;
			specificationFailure = null;
			MockSectionSource.specification = specification;
		}

		/**
		 * Default constructor.
		 */
		public MockSectionSource() {
			// Determine if fail to instantiate
			if (instantiateFailure != null) {
				throw instantiateFailure;
			}
		}

		/*
		 * ================ SectionSource ================================
		 */

		@Override
		public SectionSourceSpecification getSpecification() {

			// Determine if failure to obtain
			if (specificationFailure != null) {
				throw specificationFailure;
			}

			// Return the specification
			return specification;
		}

		@Override
		public void sourceSectionType(SectionTypeBuilder sectionTypeBuilder,
				SectionSourceContext context) throws Exception {
			fail("Should not be invoked for obtaining specification");
		}

		@Override
		public Section sourceSection() {
			fail("Should not be invoked for obtaining specification");
			return null;
		}
	}

}