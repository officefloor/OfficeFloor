/*-
 * #%L
 * Web configuration
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

package net.officefloor.woof.template;

import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.compile.properties.Property;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.test.issues.MockCompilerIssues;
import net.officefloor.compile.test.properties.PropertyListUtil;
import net.officefloor.frame.api.source.TestSource;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.model.change.Change;
import net.officefloor.woof.template.impl.AbstractWoofTemplateExtensionSource;

/**
 * Tests the {@link WoofTemplateExtensionLoader}.
 * 
 * @author Daniel Sagenschneider
 */
public class LoadWoofTemplateExtensionSpecificationTest extends OfficeFrameTestCase {

	/**
	 * {@link ClassLoader}.
	 */
	private ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

	/**
	 * {@link MockCompilerIssues}.
	 */
	private final MockCompilerIssues issues = new MockCompilerIssues(this);

	/**
	 * {@link WoofTemplateExtensionSourceSpecification}.
	 */
	private final WoofTemplateExtensionSourceSpecification specification = this
			.createMock(WoofTemplateExtensionSourceSpecification.class);

	@Override
	protected void setUp() throws Exception {
		MockWoofTemplateExtensionSource.reset(this.specification);
	}

	/**
	 * Ensures issue if fails to instantiate the
	 * {@link WoofTemplateExtensionSource}.
	 */
	public void testFailInstantiateForSpecification() {

		final RuntimeException failure = new RuntimeException("instantiate failure");

		// Record failure to instantiate
		this.issues.recordIssue(
				"Failed to instantiate " + MockWoofTemplateExtensionSource.class.getName() + " by default constructor",
				failure);

		// Attempt to obtain specification
		MockWoofTemplateExtensionSource.instantiateFailure = failure;
		this.replayMockObjects();
		this.loadSpecification(false);
		this.verifyMockObjects();
	}

	/**
	 * Ensures issue if failure in obtaining the
	 * {@link WoofTemplateExtensionSourceSpecification}.
	 */
	public void testFailGetSpecification() {

		final Error failure = new Error("specification failure");

		// Record failure to instantiate
		this.issues.recordIssue("Failed to obtain WoofTemplateExtensionSourceSpecification from "
				+ MockWoofTemplateExtensionSource.class.getName(), failure);

		// Attempt to obtain specification
		MockWoofTemplateExtensionSource.specificationFailure = failure;
		this.replayMockObjects();
		this.loadSpecification(false);
		this.verifyMockObjects();
	}

	/**
	 * Ensures issue if no {@link WoofTemplateExtensionSourceSpecification}
	 * obtained.
	 */
	public void testNoSpecification() {

		// Record no specification returned
		this.issues.recordIssue("No WoofTemplateExtensionSourceSpecification returned from "
				+ MockWoofTemplateExtensionSource.class.getName());

		// Attempt to obtain specification
		MockWoofTemplateExtensionSource.specification = null;
		this.replayMockObjects();
		this.loadSpecification(false);
		this.verifyMockObjects();
	}

	/**
	 * Ensures issue if fails to obtain the
	 * {@link WoofTemplateExtensionSourceProperty} instances.
	 */
	public void testFailGetProperties() {

		final NullPointerException failure = new NullPointerException("Fail to get template extension properties");

		// Record null properties
		this.recordThrows(this.specification, this.specification.getProperties(), failure);
		this.issues.recordIssue(
				"Failed to obtain WoofTemplateExtensionSourceProperty instances from WoofTemplateExtensionSourceSpecification for "
						+ MockWoofTemplateExtensionSource.class.getName(),
				failure);

		// Attempt to obtain specification
		this.replayMockObjects();
		this.loadSpecification(false);
		this.verifyMockObjects();
	}

	/**
	 * Ensures considers null {@link WoofTemplateExtensionSourceProperty} array as
	 * no properties.
	 */
	public void testNullPropertiesArray() {

		// Record null properties
		this.recordReturn(this.specification, this.specification.getProperties(), null);

		// Attempt to obtain specification
		this.replayMockObjects();
		this.loadSpecification(true);
		this.verifyMockObjects();
	}

	/**
	 * Ensures issue if element in {@link WoofTemplateExtensionSourceProperty} array
	 * is null.
	 */
	public void testNullPropertyElement() {

		// Record null properties
		this.recordReturn(this.specification, this.specification.getProperties(),
				new WoofTemplateExtensionSourceProperty[] { null });
		this.issues.recordIssue(
				"WoofTemplateExtensionSourceProperty 0 is null from WoofTemplateExtensionSourceSpecification for "
						+ MockWoofTemplateExtensionSource.class.getName());

		// Attempt to obtain specification
		this.replayMockObjects();
		this.loadSpecification(false);
		this.verifyMockObjects();
	}

	/**
	 * Ensures issue if <code>null</code>
	 * {@link WoofTemplateExtensionSourceProperty} name.
	 */
	public void testNullPropertyName() {

		final WoofTemplateExtensionSourceProperty property = this.createMock(WoofTemplateExtensionSourceProperty.class);

		// Record obtaining properties
		this.recordReturn(this.specification, this.specification.getProperties(),
				new WoofTemplateExtensionSourceProperty[] { property });
		this.recordReturn(property, property.getName(), "");
		this.issues.recordIssue(
				"WoofTemplateExtensionSourceProperty 0 provided blank name from WoofTemplateExtensionSourceSpecification for "
						+ MockWoofTemplateExtensionSource.class.getName());

		// Attempt to obtain specification
		this.replayMockObjects();
		this.loadSpecification(false);
		this.verifyMockObjects();
	}

	/**
	 * Ensures issue if fails to get the {@link WoofTemplateExtensionSourceProperty}
	 * name.
	 */
	public void testFailPropertyName() {

		final RuntimeException failure = new RuntimeException("Failed to get property name");
		final WoofTemplateExtensionSourceProperty property = this.createMock(WoofTemplateExtensionSourceProperty.class);

		// Record obtaining properties
		this.recordReturn(this.specification, this.specification.getProperties(),
				new WoofTemplateExtensionSourceProperty[] { property });
		this.recordThrows(property, property.getName(), failure);
		this.issues.recordIssue(
				"Failed to get name for WoofTemplateExtensionSourceProperty 0 from WoofTemplateExtensionSourceSpecification for "
						+ MockWoofTemplateExtensionSource.class.getName(),
				failure);

		// Attempt to obtain specification
		this.replayMockObjects();
		this.loadSpecification(false);
		this.verifyMockObjects();
	}

	/**
	 * Ensures issue if fails to get the {@link WoofTemplateExtensionSourceProperty}
	 * label.
	 */
	public void testFailGetWorkPropertyLabel() {

		final RuntimeException failure = new RuntimeException("Failed to get property label");
		final WoofTemplateExtensionSourceProperty property = this.createMock(WoofTemplateExtensionSourceProperty.class);

		// Record obtaining properties
		this.recordReturn(this.specification, this.specification.getProperties(),
				new WoofTemplateExtensionSourceProperty[] { property });
		this.recordReturn(property, property.getName(), "NAME");
		this.recordThrows(property, property.getLabel(), failure);
		this.issues.recordIssue(
				"Failed to get label for WoofTemplateExtensionSourceProperty 0 (NAME) from WoofTemplateExtensionSourceSpecification for "
						+ MockWoofTemplateExtensionSource.class.getName(),
				failure);

		// Attempt to obtain specification
		this.replayMockObjects();
		this.loadSpecification(false);
		this.verifyMockObjects();
	}

	/**
	 * Ensures able to load the {@link WoofTemplateExtensionSourceSpecification} .
	 */
	public void testLoadSpecification() {

		final WoofTemplateExtensionSourceProperty propertyWithLabel = this
				.createMock(WoofTemplateExtensionSourceProperty.class);
		final WoofTemplateExtensionSourceProperty propertyWithoutLabel = this
				.createMock(WoofTemplateExtensionSourceProperty.class);

		// Record obtaining properties
		this.recordReturn(this.specification, this.specification.getProperties(),
				new WoofTemplateExtensionSourceProperty[] { propertyWithLabel, propertyWithoutLabel });
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
	 * Ensure can load the adapted specification.
	 */
	public void testAdaptedLoadSpecification() {
		this.loadSpecification(AdaptedWoofTemplateExtensionSource.class.getName(), true, "NAME", "LABEL");
	}

	/**
	 * Adapted {@link WoofTemplateExtensionSource}.
	 */
	public static class AdaptedWoofTemplateExtensionSource extends AbstractWoofTemplateExtensionSource {

		@Override
		protected void loadSpecification(SpecificationContext context) {
			context.addProperty("NAME", "LABEL");
		}

		@Override
		public void extendTemplate(WoofTemplateExtensionSourceContext context) throws Exception {
			fail("Should not be invoked for obtaining specification");
		}
	}

	/**
	 * Loads the {@link WoofTemplateExtensionSourceSpecification}.
	 * 
	 * @param isExpectToLoad Flag indicating if expect to obtain the
	 *                       {@link WoofTemplateExtensionSourceSpecification}.
	 * @param propertyNames  Expected {@link Property} names for being returned.
	 */
	private void loadSpecification(boolean isExpectToLoad, String... propertyNameLabelPairs) {
		this.loadSpecification(MockWoofTemplateExtensionSource.class.getName(), isExpectToLoad, propertyNameLabelPairs);
	}

	/**
	 * Loads the {@link WoofTemplateExtensionSourceSpecification}.
	 * 
	 * @param woofTemplateExtensionSourceClassName {@link WoofTemplateExtensionSource}
	 *                                             class name.
	 * @param isExpectToLoad                       Flag indicating if expect to
	 *                                             obtain the
	 *                                             {@link WoofTemplateExtensionSourceSpecification}.
	 * @param propertyNames                        Expected {@link Property} names
	 *                                             for being returned.
	 */
	private void loadSpecification(String woofTemplateExtensionSourceClassName, boolean isExpectToLoad,
			String... propertyNameLabelPairs) {

		// Load the specification
		OfficeFloorCompiler compiler = OfficeFloorCompiler.newOfficeFloorCompiler(null);
		compiler.setCompilerIssues(this.issues);
		WoofTemplateExtensionLoader loader = new WoofTemplateExtensionLoaderImpl();
		PropertyList propertyList = loader.loadSpecification(woofTemplateExtensionSourceClassName, this.classLoader,
				this.issues);

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
	 * Mock {@link WoofTemplateExtensionSourceSpecification} for testing.
	 */
	@TestSource
	public static class MockWoofTemplateExtensionSource implements WoofTemplateExtensionSource {

		/**
		 * Failure to instantiate an instance.
		 */
		public static RuntimeException instantiateFailure = null;

		/**
		 * Failure to obtain the {@link WoofTemplateExtensionSourceSpecification}.
		 */
		public static Error specificationFailure = null;

		/**
		 * {@link WoofTemplateExtensionSourceSpecification}.
		 */
		public static WoofTemplateExtensionSourceSpecification specification;

		/**
		 * Resets the state for next test.
		 * 
		 * @param specification {@link WoofTemplateExtensionSourceSpecification}.
		 */
		public static void reset(WoofTemplateExtensionSourceSpecification specification) {
			instantiateFailure = null;
			specificationFailure = null;
			MockWoofTemplateExtensionSource.specification = specification;
		}

		/**
		 * Default constructor.
		 */
		public MockWoofTemplateExtensionSource() {
			// Determine if fail to instantiate
			if (instantiateFailure != null) {
				throw instantiateFailure;
			}
		}

		/*
		 * ============ WoofTemplateExtensionSource ===========================
		 */

		@Override
		public WoofTemplateExtensionSourceSpecification getSpecification() {

			// Determine if failure to obtain
			if (specificationFailure != null) {
				throw specificationFailure;
			}

			// Return the specification
			return specification;
		}

		@Override
		public Change<?> createConfigurationChange(WoofTemplateExtensionChangeContext context) {
			fail("Should not be invoked for obtaining specification");
			return null;
		}

		@Override
		public void extendTemplate(WoofTemplateExtensionSourceContext context) throws Exception {
			fail("Should not be invoked for obtaining specification");
		}
	}

}
