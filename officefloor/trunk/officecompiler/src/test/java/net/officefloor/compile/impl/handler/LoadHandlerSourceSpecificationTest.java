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
package net.officefloor.compile.impl.handler;

import java.util.List;

import net.officefloor.compile.issues.CompilerIssues;
import net.officefloor.compile.issues.CompilerIssues.LocationType;
import net.officefloor.compile.properties.Property;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.handler.HandlerLoader;
import net.officefloor.compile.spi.handler.source.HandlerSource;
import net.officefloor.compile.spi.handler.source.HandlerSourceContext;
import net.officefloor.compile.spi.handler.source.HandlerSourceProperty;
import net.officefloor.compile.spi.handler.source.HandlerSourceSpecification;
import net.officefloor.compile.spi.handler.source.HandlerTypeBuilder;
import net.officefloor.frame.api.build.OfficeFloorIssues.AssetType;
import net.officefloor.frame.api.execute.Handler;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.model.officefloor.OfficeFloorModel;

/**
 * Tests the {@link HandlerLoader}.
 * 
 * @author Daniel
 */
public class LoadHandlerSourceSpecificationTest extends OfficeFrameTestCase {

	/**
	 * Location of the {@link OfficeFloorModel} as {@link HandlerSource}
	 * typically used in an {@link OfficeFloorModel}.
	 */
	private final String OFFICE_FLOOR_LOCATION = "OFFICE_FLOOR";

	/**
	 * Name of the {@link Handler}.
	 */
	private final String HANDLER_NAME = "HANDLER";

	/**
	 * {@link CompilerIssues}.
	 */
	private final CompilerIssues issues = this.createMock(CompilerIssues.class);

	/**
	 * {@link HandlerSourceSpecification}.
	 */
	private final HandlerSourceSpecification specification = this
			.createMock(HandlerSourceSpecification.class);

	/*
	 * (non-Javadoc)
	 * 
	 * @see junit.framework.TestCase#setUp()
	 */
	@Override
	protected void setUp() throws Exception {
		MockHandlerSource.reset(this.specification);
	}

	/**
	 * Ensures issue if fails to instantiate the {@link HandlerSource}.
	 */
	public void testFailInstantiateForHandlerSourceSpecification() {

		final RuntimeException failure = new RuntimeException(
				"instantiate failure");

		// Record failure to instantiate
		this.record_issue(
				"Failed to instantiate " + MockHandlerSource.class.getName()
						+ " by default constructor", failure);

		// Attempt to obtain specification
		MockHandlerSource.instantiateFailure = failure;
		this.replayMockObjects();
		this.loadSpecification(false);
		this.verifyMockObjects();
	}

	/**
	 * Ensures issue if failure in obtaining the
	 * {@link HandlerSourceSpecification}.
	 */
	public void testFailGetHandlerSourceSpecification() {

		final Error failure = new Error("specification failure");

		// Record failure to instantiate
		this.record_issue("Failed to obtain HandlerSourceSpecification from "
				+ MockHandlerSource.class.getName(), failure);

		// Attempt to obtain specification
		MockHandlerSource.specificationFailure = failure;
		this.replayMockObjects();
		this.loadSpecification(false);
		this.verifyMockObjects();
	}

	/**
	 * Ensures issue if no {@link HandlerSourceSpecification} obtained.
	 */
	public void testNoHandlerSourceSpecification() {

		// Record no specification returned
		this.record_issue("No HandlerSourceSpecification returned from "
				+ MockHandlerSource.class.getName());

		// Attempt to obtain specification
		MockHandlerSource.specification = null;
		this.replayMockObjects();
		this.loadSpecification(false);
		this.verifyMockObjects();
	}

	/**
	 * Ensures issue if fails to obtain the {@link HandlerSourceProperty}
	 * instances.
	 */
	public void testFailGetHandlerSourceProperties() {

		final NullPointerException failure = new NullPointerException(
				"Fail to get managed object source properties");

		// Record null properties
		this.control(this.specification).expectAndThrow(
				this.specification.getProperties(), failure);
		this
				.record_issue(
						"Failed to obtain HandlerSourceProperty instances from HandlerSourceSpecification for "
								+ MockHandlerSource.class.getName(), failure);

		// Attempt to obtain specification
		this.replayMockObjects();
		this.loadSpecification(false);
		this.verifyMockObjects();
	}

	/**
	 * Ensures considers null {@link HandlerSourceProperty} array as no
	 * properties.
	 */
	public void testNullHandlerSourcePropertiesArray() {

		// Record null properties
		this.recordReturn(this.specification, this.specification
				.getProperties(), null);

		// Attempt to obtain specification
		this.replayMockObjects();
		this.loadSpecification(true);
		this.verifyMockObjects();
	}

	/**
	 * Ensures issue if element in {@link HandlerSourceProperty} array is null.
	 */
	public void testNullHandlerSourcePropertyElement() {

		// Record null properties
		this.recordReturn(this.specification, this.specification
				.getProperties(), new HandlerSourceProperty[] { null });
		this
				.record_issue("HandlerSourceProperty 0 is null from HandlerSourceSpecification for "
						+ MockHandlerSource.class.getName());

		// Attempt to obtain specification
		this.replayMockObjects();
		this.loadSpecification(false);
		this.verifyMockObjects();
	}

	/**
	 * Ensures issue if <code>null</code> {@link HandlerSourceProperty} name.
	 */
	public void testNullHandlerSourcePropertyName() {

		final HandlerSourceProperty property = this
				.createMock(HandlerSourceProperty.class);

		// Record obtaining properties
		this.recordReturn(this.specification, this.specification
				.getProperties(), new HandlerSourceProperty[] { property });
		this.recordReturn(property, property.getName(), "");
		this
				.record_issue("HandlerSourceProperty 0 provided blank name from HandlerSourceSpecification for "
						+ MockHandlerSource.class.getName());

		// Attempt to obtain specification
		this.replayMockObjects();
		this.loadSpecification(false);
		this.verifyMockObjects();
	}

	/**
	 * Ensures issue if fails to get the {@link HandlerSourceProperty} name.
	 */
	public void testFailGetHandlerSourcePropertyName() {

		final RuntimeException failure = new RuntimeException(
				"Failed to get property name");
		final HandlerSourceProperty property = this
				.createMock(HandlerSourceProperty.class);

		// Record obtaining properties
		this.recordReturn(this.specification, this.specification
				.getProperties(), new HandlerSourceProperty[] { property });
		this.control(property).expectAndThrow(property.getName(), failure);
		this
				.record_issue(
						"Failed to get name for HandlerSourceProperty 0 from HandlerSourceSpecification for "
								+ MockHandlerSource.class.getName(), failure);

		// Attempt to obtain specification
		this.replayMockObjects();
		this.loadSpecification(false);
		this.verifyMockObjects();
	}

	/**
	 * Ensures issue if fails to get the {@link HandlerSourceProperty} label.
	 */
	public void testFailGetHandlerSourcePropertyLabel() {

		final RuntimeException failure = new RuntimeException(
				"Failed to get property label");
		final HandlerSourceProperty property = this
				.createMock(HandlerSourceProperty.class);

		// Record obtaining properties
		this.recordReturn(this.specification, this.specification
				.getProperties(), new HandlerSourceProperty[] { property });
		this.recordReturn(property, property.getName(), "NAME");
		this.control(property).expectAndThrow(property.getLabel(), failure);
		this
				.record_issue(
						"Failed to get label for HandlerSourceProperty 0 (NAME) from HandlerSourceSpecification for "
								+ MockHandlerSource.class.getName(), failure);

		// Attempt to obtain specification
		this.replayMockObjects();
		this.loadSpecification(false);
		this.verifyMockObjects();
	}

	/**
	 * Ensures able to load the {@link HandlerSourceSpecification}.
	 */
	public void testLoadHandlerSourceSpecification() {

		final HandlerSourceProperty propertyWithLabel = this
				.createMock(HandlerSourceProperty.class);
		final HandlerSourceProperty propertyWithoutLabel = this
				.createMock(HandlerSourceProperty.class);

		// Record obtaining properties
		this.recordReturn(this.specification, this.specification
				.getProperties(), new HandlerSourceProperty[] {
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
		this.issues.addIssue(LocationType.OFFICE_FLOOR, OFFICE_FLOOR_LOCATION,
				AssetType.HANDLER, HANDLER_NAME, issueDescription);
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
		this.issues.addIssue(LocationType.OFFICE_FLOOR, OFFICE_FLOOR_LOCATION,
				AssetType.HANDLER, HANDLER_NAME, issueDescription,
				cause);
	}

	/**
	 * Loads the {@link HandlerSourceSpecification}.
	 * 
	 * @param isExpectToLoad
	 *            Flag indicating if expect to obtain the
	 *            {@link HandlerSourceSpecification}.
	 * @param propertyNames
	 *            Expected {@link Property} names for being returned.
	 */
	private void loadSpecification(boolean isExpectToLoad,
			String... propertyNameLabelPairs) {

		// Load the managed object specification specification
		HandlerLoader handlerLoader = new HandlerLoaderImpl(
				LocationType.OFFICE_FLOOR, OFFICE_FLOOR_LOCATION,
				HANDLER_NAME);
		PropertyList propertyList = handlerLoader.loadSpecification(
				MockHandlerSource.class, this.issues);

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
	 * Mock {@link HandlerSource} for testing.
	 */
	public static class MockHandlerSource implements HandlerSource {

		/**
		 * Failure to instantiate an instance.
		 */
		public static RuntimeException instantiateFailure = null;

		/**
		 * Failure to obtain the {@link HandlerSourceSpecification}.
		 */
		public static Error specificationFailure = null;

		/**
		 * {@link HandlerSourceSpecification}.
		 */
		public static HandlerSourceSpecification specification;

		/**
		 * Resets the state for next test.
		 * 
		 * @param specification
		 *            {@link HandlerSourceSpecification}.
		 */
		public static void reset(HandlerSourceSpecification specification) {
			instantiateFailure = null;
			specificationFailure = null;
			MockHandlerSource.specification = specification;
		}

		/**
		 * Default constructor.
		 */
		public MockHandlerSource() {
			// Determine if fail to instantiate
			if (instantiateFailure != null) {
				throw instantiateFailure;
			}
		}

		/*
		 * ================ HandlerSource ================================
		 */

		@Override
		public HandlerSourceSpecification getSpecification() {
			// Determine if failure to obtain
			if (specificationFailure != null) {
				throw specificationFailure;
			}

			// Return the specification
			return specification;
		}

		@Override
		public void sourceHandler(HandlerTypeBuilder handlerTypeBuilder,
				HandlerSourceContext context) throws Exception {
			fail("Should not be invoked for obtaining specification");
		}
	}

}