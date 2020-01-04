/*-
 * #%L
 * Web Plug-in
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

package net.officefloor.web.state;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.test.issues.MockCompilerIssues;
import net.officefloor.compile.test.managedobject.ManagedObjectLoaderUtil;
import net.officefloor.compile.test.managedobject.ManagedObjectTypeBuilder;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.frame.util.ManagedObjectSourceStandAlone;
import net.officefloor.frame.util.ManagedObjectUserStandAlone;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.server.http.mock.MockHttpServer;
import net.officefloor.web.build.HttpArgumentParser;
import net.officefloor.web.state.HttpRequestObjectManagedObjectSource.HttpRequestObjectDependencies;
import net.officefloor.web.state.HttpRequestStateManagedObjectSource.HttpRequestStateDependencies;

/**
 * Tests the {@link HttpRequestObjectManagedObjectSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpRequestObjectManagedObjectSourceTest extends OfficeFrameTestCase {

	/**
	 * Ensure correct specification.
	 */
	public void testSpecification() {
		ManagedObjectLoaderUtil.validateSpecification(HttpRequestObjectManagedObjectSource.class,
				HttpRequestObjectManagedObjectSource.PROPERTY_CLASS_NAME, "Class");
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
	 * @param isLoadParameters <code>true</code> to load parameters.
	 */
	public void doTypeTest(boolean isLoadParameters) {

		// Obtain the type
		ManagedObjectTypeBuilder type = ManagedObjectLoaderUtil.createManagedObjectTypeBuilder();
		type.setObjectClass(MockObject.class);
		type.addDependency(HttpRequestObjectDependencies.HTTP_REQUEST_STATE, HttpRequestState.class, null);

		// Validate the managed object type
		List<String> properties = new ArrayList<String>(4);
		properties.addAll(
				Arrays.asList(HttpRequestObjectManagedObjectSource.PROPERTY_CLASS_NAME, MockObject.class.getName()));
		if (isLoadParameters) {
			properties.addAll(Arrays.asList(HttpRequestObjectManagedObjectSource.PROPERTY_IS_LOAD_HTTP_PARAMETERS,
					String.valueOf(true)));
		}
		ManagedObjectLoaderUtil.validateManagedObjectType(type, HttpRequestObjectManagedObjectSource.class,
				properties.toArray(new String[properties.size()]));
	}

	/**
	 * Ensures the object is {@link Serializable}.
	 */
	public void testInvalidObjectAsNotSerializable() {

		final MockCompilerIssues issues = new MockCompilerIssues(this);

		// Record issue as not serializable object
		issues.recordIssue("Failed to init", new Exception(
				"HttpRequestState object " + MockInvalidObject.class.getName() + " must be Serializable"));

		// Test
		this.replayMockObjects();
		OfficeFloorCompiler compiler = OfficeFloorCompiler.newOfficeFloorCompiler(null);
		compiler.setCompilerIssues(issues);
		PropertyList properties = compiler.createPropertyList();
		properties.addProperty(HttpRequestObjectManagedObjectSource.PROPERTY_CLASS_NAME)
				.setValue(MockInvalidObject.class.getName());
		compiler.getManagedObjectLoader().loadManagedObjectType(HttpRequestObjectManagedObjectSource.class, properties);
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
		this.doTest(false, null, true, "/path?VALUE=TEST", "TEST",
				HttpRequestObjectManagedObjectSource.PROPERTY_IS_LOAD_HTTP_PARAMETERS, String.valueOf(true));
	}

	/**
	 * Ensure can non load case sensitive parameter.
	 */
	public void testNonLoadCaseSensitiveParameter() throws Throwable {
		this.doTest(false, null, true, "/path?VALUE=TEST", null,
				HttpRequestObjectManagedObjectSource.PROPERTY_IS_LOAD_HTTP_PARAMETERS, String.valueOf(true),
				HttpRequestObjectManagedObjectSource.PROPERTY_CASE_INSENSITIVE, String.valueOf(false));
	}

	/**
	 * Ensure load parameter via alias.
	 */
	public void testLoadAliasParameter() throws Throwable {
		this.doTest(false, null, true, "/path?alias=TEST", "TEST",
				HttpRequestObjectManagedObjectSource.PROPERTY_IS_LOAD_HTTP_PARAMETERS, String.valueOf(true),
				HttpRequestObjectManagedObjectSource.PROPERTY_PREFIX_ALIAS + "alias", "value");
	}

	/**
	 * Undertakes the test to use the {@link HttpRequestState}.
	 * 
	 * @param isAlreadyRegistered    Indicates if object is already registered with
	 *                               the {@link HttpRequestState}.
	 * @param boundName              Name to bind object within
	 *                               {@link HttpRequestState}. <code>null</code> to
	 *                               use {@link ManagedObject} name.
	 * @param isLoadParameters       Indicates if loading parameters.
	 * @param requestUri             Request URI.
	 * @param expectedValue          Expected value to be loaded.
	 * @param propertyNameValuePairs Additional property name/value pairs.
	 */
	public void doTest(boolean isAlreadyRegistered, String boundName, boolean isLoadParameters, String requestUri,
			String expectedValue, String... propertyNameValuePairs) throws Throwable {

		// Create the server HTTP connection
		ServerHttpConnection connection = MockHttpServer.mockConnection(MockHttpServer.mockRequest(requestUri));

		// Create the request state
		HttpRequestState requestState = this.createHttpRequestState(connection);

		// Determine the name to retrieve object from request state
		final String MO_NAME = "MO";
		final String RETRIEVE_NAME = (boundName == null ? MO_NAME : boundName);

		// Obtain the object
		MockObject object = new MockObject();
		if (isAlreadyRegistered) {
			// Record obtain registered object
			requestState.setAttribute(RETRIEVE_NAME, object);
		}

		// Load the managed object source
		ManagedObjectSourceStandAlone loader = new ManagedObjectSourceStandAlone();
		loader.addProperty(HttpRequestObjectManagedObjectSource.PROPERTY_CLASS_NAME, MockObject.class.getName());
		if (boundName != null) {
			loader.addProperty(HttpRequestObjectManagedObjectSource.PROPERTY_BIND_NAME, boundName);
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
		user.mapDependency(HttpRequestObjectDependencies.HTTP_REQUEST_STATE, requestState);
		ManagedObject managedObject = user.sourceManagedObject(source);
		if (isAlreadyRegistered) {
			assertSame("Incorrect instantiated object", object, managedObject.getObject());
			assertNull("Should not load value to existing object", object.value);
		} else {
			object = (MockObject) managedObject.getObject();
			assertEquals("Incorrect loaded value", expectedValue, object.value);
		}
	}

	/**
	 * Creates the {@link HttpRequestState}.
	 * 
	 * @param connection {@link ServerHttpConnection}.
	 * @return {@link HttpRequestState}.
	 */
	private HttpRequestState createHttpRequestState(ServerHttpConnection connection) throws Throwable {
		HttpRequestStateManagedObjectSource mos = new HttpRequestStateManagedObjectSource(new HttpArgumentParser[0]);
		ManagedObjectUserStandAlone user = new ManagedObjectUserStandAlone();
		user.mapDependency(HttpRequestStateDependencies.SERVER_HTTP_CONNECTION, connection);
		return (HttpRequestState) user.sourceManagedObject(mos);
	}

	/**
	 * Mock object.
	 */
	public static class MockObject implements Serializable {
		private static final long serialVersionUID = 1L;

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
