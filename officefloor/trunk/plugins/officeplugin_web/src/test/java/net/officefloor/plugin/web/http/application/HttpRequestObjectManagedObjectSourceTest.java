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
package net.officefloor.plugin.web.http.application;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.compile.issues.CompilerIssues;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.test.managedobject.ManagedObjectLoaderUtil;
import net.officefloor.compile.test.managedobject.ManagedObjectTypeBuilder;
import net.officefloor.frame.api.build.OfficeFloorIssues.AssetType;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.frame.util.ManagedObjectSourceStandAlone;
import net.officefloor.frame.util.ManagedObjectUserStandAlone;
import net.officefloor.plugin.socket.server.http.HttpRequest;
import net.officefloor.plugin.socket.server.http.ServerHttpConnection;
import net.officefloor.plugin.socket.server.http.conversation.impl.HttpRequestImpl;

import org.easymock.AbstractMatcher;

/**
 * Tests the {@link HttpRequestObjectManagedObjectSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpRequestObjectManagedObjectSourceTest extends
		OfficeFrameTestCase {

	/**
	 * Ensure correct specification.
	 */
	public void testSpecification() {
		ManagedObjectLoaderUtil.validateSpecification(
				HttpRequestObjectManagedObjectSource.class,
				HttpRequestObjectManagedObjectSource.PROPERTY_CLASS_NAME,
				"Class");
	}

	/**
	 * Ensure valid type if NOT loading parameters.
	 */
	public void testTypeNotLoadingParameters() {
		this.doTypeTest(false);
	}

	/**
	 * Ensure valid type on loading parameters.
	 */
	public void testTypeLoadingParameters() {
		this.doTypeTest(true);
	}

	/**
	 * Ensure correct type.
	 * 
	 * @param isLoadParameters
	 *            <code>true</code> to load parameters.
	 */
	public void doTypeTest(boolean isLoadParameters) {

		// Obtain the type
		ManagedObjectTypeBuilder type = ManagedObjectLoaderUtil
				.createManagedObjectTypeBuilder();
		type.setObjectClass(MockObject.class);
		type.addDependency("REQUEST_STATE", HttpRequestState.class, null, 0,
				null);
		if (isLoadParameters) {
			type.addDependency("SERVER_HTTP_CONNECTION",
					ServerHttpConnection.class, null, 1, null);
		}

		// Validate the managed object type
		List<String> properties = new ArrayList<String>(4);
		properties.addAll(Arrays.asList(
				HttpRequestObjectManagedObjectSource.PROPERTY_CLASS_NAME,
				MockObject.class.getName()));
		if (isLoadParameters) {
			properties
					.addAll(Arrays
							.asList(HttpRequestObjectManagedObjectSource.PROPERTY_IS_LOAD_HTTP_PARAMETERS,
									String.valueOf(true)));
		}
		ManagedObjectLoaderUtil.validateManagedObjectType(type,
				HttpRequestObjectManagedObjectSource.class,
				properties.toArray(new String[properties.size()]));
	}

	/**
	 * Ensures the object is {@link Serializable}.
	 */
	public void testInvalidObjectAsNotSerializable() {

		final CompilerIssues issues = this.createMock(CompilerIssues.class);

		// Record issue as not serializable object
		issues.addIssue(null, null, AssetType.MANAGED_OBJECT, null,
				"Failed to init", null);
		this.control(issues).setMatcher(new AbstractMatcher() {
			@Override
			public boolean matches(Object[] expected, Object[] actual) {
				for (int i = 0; i < (expected.length - 1); i++) {
					assertEquals("Invalid parameter " + i, expected[i],
							actual[i]);
				}
				Exception cause = (Exception) actual[expected.length - 1];
				assertEquals("Incorrect cause", "HttpRequestState object "
						+ MockInvalidObject.class.getName()
						+ " must be Serializable", cause.getMessage());
				return true;
			}
		});

		// Test
		this.replayMockObjects();
		OfficeFloorCompiler compiler = OfficeFloorCompiler
				.newOfficeFloorCompiler(null);
		compiler.setCompilerIssues(issues);
		PropertyList properties = compiler.createPropertyList();
		properties.addProperty(
				HttpRequestObjectManagedObjectSource.PROPERTY_CLASS_NAME)
				.setValue(MockInvalidObject.class.getName());
		compiler.getManagedObjectLoader().loadManagedObjectType(
				HttpRequestObjectManagedObjectSource.class, properties);
		this.verifyMockObjects();
	}

	/**
	 * Ensure use {@link ManagedObject} name for already registered.
	 */
	public void testAlreadyRegisteredByManagedObjectName() throws Throwable {
		this.doTest(true, null, false, "/path?value=TEST", null);
	}

	/**
	 * Ensure can override binding name for already registered.
	 */
	public void testAlreadyRegisteredOverrideBindingName() throws Throwable {
		this.doTest(true, "OVERRIDDEN", false, "/path?value=TEST", null);
	}

	/**
	 * Ensure use {@link ManagedObject} name for instantiated object.
	 */
	public void testInstantiateByManagedObjectName() throws Throwable {
		this.doTest(false, null, false, "/path?value=TEST", null);
	}

	/**
	 * Ensure can override binding name for instantiated object.
	 */
	public void testInstantiateOverrideBindingName() throws Throwable {
		this.doTest(false, "OVERRIDDEN", false, "/path?value=TEST", null);
	}

	/**
	 * Ensure can load parameter.
	 */
	public void testLoadParameter() throws Throwable {
		this.doTest(
				false,
				null,
				true,
				"/path?VALUE=TEST",
				"TEST",
				HttpRequestObjectManagedObjectSource.PROPERTY_IS_LOAD_HTTP_PARAMETERS,
				String.valueOf(true));
	}

	/**
	 * Ensure can non load case sensitive parameter.
	 */
	public void testNonLoadCaseSensitiveParameter() throws Throwable {
		this.doTest(
				false,
				null,
				true,
				"/path?VALUE=TEST",
				null,
				HttpRequestObjectManagedObjectSource.PROPERTY_IS_LOAD_HTTP_PARAMETERS,
				String.valueOf(true),
				HttpRequestObjectManagedObjectSource.PROPERTY_CASE_INSENSITIVE,
				String.valueOf(false));
	}

	/**
	 * Ensure load parameter via alias.
	 */
	public void testLoadAliasParameter() throws Throwable {
		this.doTest(
				false,
				null,
				true,
				"/path?alias=TEST",
				"TEST",
				HttpRequestObjectManagedObjectSource.PROPERTY_IS_LOAD_HTTP_PARAMETERS,
				String.valueOf(true),
				HttpRequestObjectManagedObjectSource.PROPERTY_PREFIX_ALIAS
						+ "alias", "value");
	}

	/**
	 * Undertakes the test to use the {@link HttpRequestState}.
	 * 
	 * @param isAlreadyRegistered
	 *            Indicates if object is already registered with the
	 *            {@link HttpRequestState}.
	 * @param boundName
	 *            Name to bind object within {@link HttpRequestState}.
	 *            <code>null</code> to use {@link ManagedObject} name.
	 * @param isLoadParameters
	 *            Indicates if loading parameters.
	 * @param requestUri
	 *            Request URI.
	 * @param expectedValue
	 *            Expected value to be loaded.
	 * @param propertyNameValuePairs
	 *            Additional property name/value pairs.
	 */
	public void doTest(boolean isAlreadyRegistered, String boundName,
			boolean isLoadParameters, String requestUri, String expectedValue,
			String... propertyNameValuePairs) throws Throwable {

		final HttpRequestState state = this.createMock(HttpRequestState.class);
		final ServerHttpConnection connection = this
				.createMock(ServerHttpConnection.class);

		// Determine the name to retrieve object from request state
		final String MO_NAME = "MO";
		final String RETRIEVE_NAME = (boundName == null ? MO_NAME : boundName);

		// Obtain the object
		final MockObject[] object = new MockObject[1];
		if (isAlreadyRegistered) {
			// Record obtain registered object
			object[0] = new MockObject();
			this.recordReturn(state, state.getAttribute(RETRIEVE_NAME),
					object[0]);

		} else {
			// Record instantiate and register in request state
			this.recordReturn(state, state.getAttribute(RETRIEVE_NAME), null);
			state.setAttribute(RETRIEVE_NAME, null);
			this.control(state).setMatcher(new AbstractMatcher() {
				@Override
				public boolean matches(Object[] expected, Object[] actual) {
					assertEquals("Incorrect bound name", RETRIEVE_NAME,
							actual[0]);
					object[0] = (MockObject) actual[1];
					assertNotNull("Expecting instantiated object", object[0]);
					return true;
				}
			});

			// Load parameters
			if (isLoadParameters) {
				HttpRequest request = new HttpRequestImpl("GET", requestUri,
						"HTTP/1.1", null, null);
				this.recordReturn(connection, connection.getHttpRequest(),
						request);
			}
		}

		// Test
		this.replayMockObjects();

		// Load the managed object source
		ManagedObjectSourceStandAlone loader = new ManagedObjectSourceStandAlone();
		loader.addProperty(
				HttpRequestObjectManagedObjectSource.PROPERTY_CLASS_NAME,
				MockObject.class.getName());
		if (boundName != null) {
			loader.addProperty(
					HttpRequestObjectManagedObjectSource.PROPERTY_BIND_NAME,
					boundName);
		}
		for (int i = 0; i < propertyNameValuePairs.length; i += 2) {
			String name = propertyNameValuePairs[i];
			String value = propertyNameValuePairs[i + 1];
			loader.addProperty(name, value);
		}
		HttpRequestObjectManagedObjectSource source = loader
				.loadManagedObjectSource(HttpRequestObjectManagedObjectSource.class);

		// Instantiate and obtain the object
		ManagedObjectUserStandAlone user = new ManagedObjectUserStandAlone();
		user.setBoundManagedObjectName(MO_NAME);
		user.mapDependency(0, state);
		if (isLoadParameters) {
			user.mapDependency(1, connection);
		}
		ManagedObject managedObject = user.sourceManagedObject(source);
		assertSame("Incorrect instantiated object", object[0],
				managedObject.getObject());

		// Verify
		this.verifyMockObjects();

		// Ensure correct value
		assertEquals("Incorrect value", expectedValue, object[0].value);
	}

	/**
	 * Mock object.
	 */
	public static class MockObject implements Serializable {

		private String value = null;

		public void setValue(String value) {
			this.value = value;
		}
	}

	/**
	 * Mock invalid object.
	 */
	public static class MockInvalidObject {
	}

}