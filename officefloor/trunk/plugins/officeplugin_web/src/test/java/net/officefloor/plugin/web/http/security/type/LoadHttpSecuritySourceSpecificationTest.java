/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2013 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.officefloor.plugin.web.http.security.type;

import net.officefloor.compile.issues.CompilerIssues;
import net.officefloor.compile.properties.Property;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.test.properties.PropertyListUtil;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.spi.TestSource;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.web.http.security.HttpAuthenticateContext;
import net.officefloor.plugin.web.http.security.HttpCredentials;
import net.officefloor.plugin.web.http.security.HttpSecurity;
import net.officefloor.plugin.web.http.security.HttpSecuritySource;
import net.officefloor.plugin.web.http.security.HttpSecuritySourceContext;
import net.officefloor.plugin.web.http.security.HttpSecuritySourceMetaData;
import net.officefloor.plugin.web.http.security.HttpSecuritySourceProperty;
import net.officefloor.plugin.web.http.security.HttpSecuritySourceSpecification;
import net.officefloor.plugin.web.http.session.HttpSession;

/**
 * Tests the {@link HttpSecurityLoader}.
 * 
 * @author Daniel Sagenschneider
 */
public class LoadHttpSecuritySourceSpecificationTest extends
		OfficeFrameTestCase {

	/**
	 * {@link CompilerIssues}.
	 */
	private final CompilerIssues issues = this.createMock(CompilerIssues.class);

	/**
	 * {@link HttpSecuritySourceSpecification}.
	 */
	private final HttpSecuritySourceSpecification specification = this
			.createMock(HttpSecuritySourceSpecification.class);

	@Override
	protected void setUp() throws Exception {
		MockHttpSecuritySource.reset(this.specification);
	}

	/**
	 * Ensures issue if fails to instantiate the {@link HttpSecuritySource}.
	 */
	public void testFailInstantiateForHttpSecuritySourceSpecification() {

		final RuntimeException failure = new RuntimeException(
				"instantiate failure");

		// Record failure to instantiate
		this.record_issue("Failed to instantiate "
				+ MockHttpSecuritySource.class.getName()
				+ " by default constructor", failure);

		// Attempt to obtain specification
		MockHttpSecuritySource.instantiateFailure = failure;
		this.replayMockObjects();
		this.loadSpecification(false);
		this.verifyMockObjects();
	}

	/**
	 * Ensures issue if failure in obtaining the
	 * {@link HttpSecuritySourceSpecification}.
	 */
	public void testFailGetHttpSecuritySourceSpecification() {

		final Error failure = new Error("specification failure");

		// Record failure to instantiate
		this.record_issue(
				"Failed to obtain HttpSecuritySourceSpecification from "
						+ MockHttpSecuritySource.class.getName(), failure);

		// Attempt to obtain specification
		MockHttpSecuritySource.specificationFailure = failure;
		this.replayMockObjects();
		this.loadSpecification(false);
		this.verifyMockObjects();
	}

	/**
	 * Ensures issue if no {@link HttpSecuritySourceSpecification} obtained.
	 */
	public void testNoHttpSecuritySourceSpecification() {

		// Record no specification returned
		this.record_issue("No HttpSecuritySourceSpecification returned from "
				+ MockHttpSecuritySource.class.getName());

		// Attempt to obtain specification
		MockHttpSecuritySource.specification = null;
		this.replayMockObjects();
		this.loadSpecification(false);
		this.verifyMockObjects();
	}

	/**
	 * Ensures issue if fails to obtain the {@link HttpSecuritySourceProperty}
	 * instances.
	 */
	public void testFailGetHttpSecuritySourceProperties() {

		final NullPointerException failure = new NullPointerException(
				"Fail to get HTTP security source properties");

		// Record null properties
		this.control(this.specification).expectAndThrow(
				this.specification.getProperties(), failure);
		this.record_issue(
				"Failed to obtain HttpSecuritySourceProperty instances from HttpSecuritySourceSpecification for "
						+ MockHttpSecuritySource.class.getName(), failure);

		// Attempt to obtain specification
		this.replayMockObjects();
		this.loadSpecification(false);
		this.verifyMockObjects();
	}

	/**
	 * Ensures considers null {@link HttpSecuritySourceProperty} array as no
	 * properties.
	 */
	public void testNullHttpSecuritySourcePropertiesArray() {

		// Record null properties
		this.recordReturn(this.specification,
				this.specification.getProperties(), null);

		// Attempt to obtain specification
		this.replayMockObjects();
		this.loadSpecification(true);
		this.verifyMockObjects();
	}

	/**
	 * Ensures issue if element in {@link HttpSecuritySourceProperty} array is
	 * null.
	 */
	public void testNullHttpSecuritySourcePropertyElement() {

		// Record null properties
		this.recordReturn(this.specification,
				this.specification.getProperties(),
				new HttpSecuritySourceProperty[] { null });
		this.record_issue("HttpSecuritySourceProperty 0 is null from HttpSecuritySourceSpecification for "
				+ MockHttpSecuritySource.class.getName());

		// Attempt to obtain specification
		this.replayMockObjects();
		this.loadSpecification(false);
		this.verifyMockObjects();
	}

	/**
	 * Ensures issue if <code>null</code> {@link HttpSecuritySourceProperty}
	 * name.
	 */
	public void testNullHttpSecuritySourcePropertyName() {

		final HttpSecuritySourceProperty property = this
				.createMock(HttpSecuritySourceProperty.class);

		// Record obtaining properties
		this.recordReturn(this.specification,
				this.specification.getProperties(),
				new HttpSecuritySourceProperty[] { property });
		this.recordReturn(property, property.getName(), "");
		this.record_issue("HttpSecuritySourceProperty 0 provided blank name from HttpSecuritySourceSpecification for "
				+ MockHttpSecuritySource.class.getName());

		// Attempt to obtain specification
		this.replayMockObjects();
		this.loadSpecification(false);
		this.verifyMockObjects();
	}

	/**
	 * Ensures issue if fails to get the {@link HttpSecuritySourceProperty}
	 * name.
	 */
	public void testFailGetHttpSecuritySourcePropertyName() {

		final RuntimeException failure = new RuntimeException(
				"Failed to get property name");
		final HttpSecuritySourceProperty property = this
				.createMock(HttpSecuritySourceProperty.class);

		// Record obtaining properties
		this.recordReturn(this.specification,
				this.specification.getProperties(),
				new HttpSecuritySourceProperty[] { property });
		this.control(property).expectAndThrow(property.getName(), failure);
		this.record_issue(
				"Failed to get name for HttpSecuritySourceProperty 0 from HttpSecuritySourceSpecification for "
						+ MockHttpSecuritySource.class.getName(), failure);

		// Attempt to obtain specification
		this.replayMockObjects();
		this.loadSpecification(false);
		this.verifyMockObjects();
	}

	/**
	 * Ensures issue if fails to get the {@link HttpSecuritySourceProperty}
	 * label.
	 */
	public void testFailGetHttpSecuritySourcePropertyLabel() {

		final RuntimeException failure = new RuntimeException(
				"Failed to get property label");
		final HttpSecuritySourceProperty property = this
				.createMock(HttpSecuritySourceProperty.class);

		// Record obtaining properties
		this.recordReturn(this.specification,
				this.specification.getProperties(),
				new HttpSecuritySourceProperty[] { property });
		this.recordReturn(property, property.getName(), "NAME");
		this.control(property).expectAndThrow(property.getLabel(), failure);
		this.record_issue(
				"Failed to get label for HttpSecuritySourceProperty 0 (NAME) from HttpSecuritySourceSpecification for "
						+ MockHttpSecuritySource.class.getName(), failure);

		// Attempt to obtain specification
		this.replayMockObjects();
		this.loadSpecification(false);
		this.verifyMockObjects();
	}

	/**
	 * Ensures able to load the {@link HttpSecuritySourceSpecification}.
	 */
	public void testLoadHttpSecuritySourceSpecification() {

		final HttpSecuritySourceProperty propertyWithLabel = this
				.createMock(HttpSecuritySourceProperty.class);
		final HttpSecuritySourceProperty propertyWithoutLabel = this
				.createMock(HttpSecuritySourceProperty.class);

		// Record obtaining properties
		this.recordReturn(this.specification,
				this.specification.getProperties(),
				new HttpSecuritySourceProperty[] { propertyWithLabel,
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
		this.issues.addIssue(null, null, null, null, issueDescription);
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
		this.issues.addIssue(null, null, null, null, issueDescription, cause);
	}

	/**
	 * Loads the {@link HttpSecuritySourceSpecification}.
	 * 
	 * @param isExpectToLoad
	 *            Flag indicating if expect to obtain the
	 *            {@link HttpSecuritySourceSpecification}.
	 * @param propertyNames
	 *            Expected {@link Property} names for being returned.
	 */
	private void loadSpecification(boolean isExpectToLoad,
			String... propertyNameLabelPairs) {

		// Load the HTTP security specification specification
		HttpSecurityLoader httpSecurityLoader = null; // HttpSecurityLoaderImpl(this.issues);
		PropertyList propertyList = httpSecurityLoader
				.loadSpecification(MockHttpSecuritySource.class);

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
	 * Mock {@link HttpSecuritySource} for testing.
	 */
	@TestSource
	public static class MockHttpSecuritySource implements
			HttpSecuritySource<HttpSecurity, HttpCredentials, None, None> {

		/**
		 * Failure to instantiate an instance.
		 */
		public static RuntimeException instantiateFailure = null;

		/**
		 * Failure to obtain the {@link HttpSecuritySourceSpecification}.
		 */
		public static Error specificationFailure = null;

		/**
		 * {@link HttpSecuritySourceSpecification}.
		 */
		public static HttpSecuritySourceSpecification specification;

		/**
		 * Resets the state for next test.
		 * 
		 * @param specification
		 *            {@link HttpSecuritySourceSpecification}.
		 */
		public static void reset(HttpSecuritySourceSpecification specification) {
			instantiateFailure = null;
			specificationFailure = null;
			MockHttpSecuritySource.specification = specification;
		}

		/**
		 * Default constructor.
		 */
		public MockHttpSecuritySource() {
			// Determine if fail to instantiate
			if (instantiateFailure != null) {
				throw instantiateFailure;
			}
		}

		/*
		 * ================ HttpSecuritySource ================================
		 */

		@Override
		public HttpSecuritySourceSpecification getSpecification() {
			// Determine if failure to obtain
			if (specificationFailure != null) {
				throw specificationFailure;
			}

			// Return the specification
			return specification;
		}

		@Override
		public void init(HttpSecuritySourceContext context) throws Exception {
			fail("Should not be invoked for obtaining specification");
		}

		@Override
		public HttpSecuritySourceMetaData<HttpSecurity, HttpCredentials, None, None> getMetaData() {
			fail("Should not be invoked for obtaining specification");
			return null;
		}

		@Override
		public HttpSecurity retrieveCached(HttpSession session) {
			fail("Should not be invoked for obtaining specification");
			return null;
		}

		@Override
		public void authenticate(
				HttpAuthenticateContext<HttpSecurity, HttpCredentials, None, None> context) {
			fail("Should not be invoked for obtaining specification");
		}
	}

}