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
package net.officefloor.plugin.json.read;

import java.io.IOException;
import java.io.Serializable;

import net.officefloor.autowire.AutoWire;
import net.officefloor.autowire.AutoWireObject;
import net.officefloor.autowire.AutoWireOfficeFloor;
import net.officefloor.autowire.AutoWireSection;
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
import net.officefloor.plugin.json.read.JsonRequestReaderManagedObjectSource.Dependencies;
import net.officefloor.plugin.section.clazz.ClassSectionSource;
import net.officefloor.plugin.section.clazz.NextTask;
import net.officefloor.plugin.socket.server.http.HttpRequest;
import net.officefloor.plugin.socket.server.http.ServerHttpConnection;
import net.officefloor.plugin.socket.server.http.parse.impl.HttpRequestParserImpl;
import net.officefloor.plugin.stream.impl.ServerInputStreamImpl;
import net.officefloor.plugin.web.http.application.HttpRequestState;
import net.officefloor.plugin.web.http.application.WebAutoWireApplication;
import net.officefloor.plugin.web.http.server.HttpServerAutoWireOfficeFloorSource;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.easymock.AbstractMatcher;

/**
 * Tests the {@link JsonRequestReaderManagedObjectSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class JsonRequestReaderManagedObjectSourceTest extends
		OfficeFrameTestCase {

	/**
	 * Validate specification.
	 */
	public void testSpecification() {
		ManagedObjectLoaderUtil
				.validateSpecification(
						JsonRequestReaderManagedObjectSource.class,
						JsonRequestReaderManagedObjectSource.PROPERTY_JSON_OBJECT_CLASS,
						"Class");
	}

	/**
	 * Validate the type.
	 */
	public void testType() {

		// Create the expected type
		ManagedObjectTypeBuilder type = ManagedObjectLoaderUtil
				.createManagedObjectTypeBuilder();
		type.setObjectClass(MockJsonObject.class);
		type.addDependency(Dependencies.SERVER_HTTP_CONNECTION,
				ServerHttpConnection.class, null);
		type.addDependency(Dependencies.HTTP_REQUEST_STATE,
				HttpRequestState.class, null);

		// Validate type
		ManagedObjectLoaderUtil
				.validateManagedObjectType(
						type,
						JsonRequestReaderManagedObjectSource.class,
						JsonRequestReaderManagedObjectSource.PROPERTY_JSON_OBJECT_CLASS,
						MockJsonObject.class.getName());
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
				assertEquals(
						"Incorrect cause",
						"JSON object "
								+ MockInvalidObject.class.getName()
								+ " must be Serializable as stored in HttpRequestState",
						cause.getMessage());
				return true;
			}
		});

		// Test
		this.replayMockObjects();
		OfficeFloorCompiler compiler = OfficeFloorCompiler
				.newOfficeFloorCompiler(null);
		compiler.setCompilerIssues(issues);
		PropertyList properties = compiler.createPropertyList();
		properties
				.addProperty(
						JsonRequestReaderManagedObjectSource.PROPERTY_JSON_OBJECT_CLASS)
				.setValue(MockInvalidObject.class.getName());
		compiler.getManagedObjectLoader().loadManagedObjectType(
				JsonRequestReaderManagedObjectSource.class, properties);
		this.verifyMockObjects();
	}

	/**
	 * Invalid {@link Object} as not {@link Serializable}.
	 */
	public static class MockInvalidObject {
	}

	/**
	 * Reads the object from the {@link HttpRequestState}.
	 */
	public void testReadObjectFromHttpRequestState() throws Throwable {
		this.doReadObjectTest(null, "STAND_ALONE", true);
	}

	/**
	 * Reads the object from the {@link HttpRequestState} specifying the bound
	 * name.
	 */
	public void testReadObjectFromBoundHttpRequestState() throws Throwable {
		this.doReadObjectTest("BIND", "BIND", true);
	}

	/**
	 * Ensure able to read object from {@link HttpRequest} pay load.
	 */
	public void testReadObjectFromPayload() throws Throwable {
		this.doReadObjectTest(null, "STAND_ALONE", false);
	}

	/**
	 * Ensure able to read object from {@link HttpRequest} pay load specifying
	 * the bound name.
	 */
	public void testReadObjectFromPayloadRegisteringWithBoundName()
			throws Throwable {
		this.doReadObjectTest("BIND", "BIND", false);
	}

	/**
	 * Undertakes the read object test.
	 * 
	 * @param bindName
	 *            Name to bind the object. May be <code>null</code>.
	 * @param boundName
	 *            Name used to identify object within {@link HttpRequestState}.
	 * @param isAvailableInHttpRequestState
	 *            <code>true</code> should the object be available from the
	 *            {@link HttpRequestState}.
	 */
	private void doReadObjectTest(String bindName, String boundName,
			boolean isAvailableInHttpRequestState) throws Throwable {

		final ServerHttpConnection connection = this
				.createMock(ServerHttpConnection.class);
		final HttpRequestState requestState = this
				.createMock(HttpRequestState.class);
		final HttpRequest request = this.createMock(HttpRequest.class);

		ServerInputStreamImpl entity = new ServerInputStreamImpl(new Object());
		byte[] data = "{\"name\":\"Daniel\"}"
				.getBytes(HttpRequestParserImpl.US_ASCII);
		entity.inputData(data, 0, (data.length - 1), false);

		// Provide object
		final MockJsonObject[] jsonObject = new MockJsonObject[] { new MockJsonObject() };
		jsonObject[0].setName("Daniel");

		// Record reading the object
		if (isAvailableInHttpRequestState) {
			// Obtain available from HTTP request state
			this.recordReturn(requestState,
					requestState.getAttribute(boundName), jsonObject[0]);

		} else {
			// Obtain from HTTP request pay load
			this.recordReturn(requestState,
					requestState.getAttribute(boundName), null);
			this.recordReturn(connection, connection.getHttpRequest(), request);
			this.recordReturn(request, request.getEntity(), entity);
			requestState.setAttribute(boundName, null);
			this.control(requestState).setMatcher(new AbstractMatcher() {
				@Override
				public boolean matches(Object[] expected, Object[] actual) {
					assertEquals("Incorrect bound name", expected[0], actual[0]);

					// Validate and register the JSON object
					MockJsonObject object = (MockJsonObject) actual[1];
					assertEquals("Invalid JSON object", "Daniel", object.name);
					jsonObject[0] = object;

					// As here, match
					return true;
				}
			});
		}

		// Test
		this.replayMockObjects();

		// Load the source
		ManagedObjectSourceStandAlone loader = new ManagedObjectSourceStandAlone();
		loader.addProperty(
				JsonRequestReaderManagedObjectSource.PROPERTY_JSON_OBJECT_CLASS,
				MockJsonObject.class.getName());
		if (bindName != null) {
			loader.addProperty(
					JsonRequestReaderManagedObjectSource.PROPERTY_BIND_NAME,
					bindName);
		}
		JsonRequestReaderManagedObjectSource source = loader
				.loadManagedObjectSource(JsonRequestReaderManagedObjectSource.class);

		// Load the managed object
		ManagedObjectUserStandAlone user = new ManagedObjectUserStandAlone();
		user.mapDependency(Dependencies.SERVER_HTTP_CONNECTION, connection);
		user.mapDependency(Dependencies.HTTP_REQUEST_STATE, requestState);
		ManagedObject managedObject = user.sourceManagedObject(source);

		// Obtain the object
		Object object = managedObject.getObject();
		assertSame("Incorrect JSON object", jsonObject[0], object);

		// Verify
		this.verifyMockObjects();
	}

	/**
	 * Ensure able to handle no JSON (pay load).
	 */
	public void testNoJson() throws Exception {
		this.doReadObjectTest(null, null, null, false, (String[]) null);
	}

	/**
	 * Ensure able to handle null JSON values.
	 */
	public void testLoadNullValues() throws Exception {
		this.doReadObjectTest(
				"{ \"name\" : null, \"array\" : null, \"subObject\" : null, \"empty\" : null }",
				null, null, false, (String[]) null);
	}

	/**
	 * Ensure able to load JSON (pay load).
	 */
	public void testLoadJson() throws Exception {
		this.doReadObjectTest(
				"{ \"name\" : \"Daniel\", \"array\" : [ \"ONE\", \"two\", \"Three\" ], \"subObject\" : { \"text\" : \"SUB\" }, \"empty\" : {} }",
				"Daniel", "SUB", true, "ONE", "two", "Three");
	}

	/**
	 * Ensure can read and load JSON contents from pay load.
	 */
	private void doReadObjectTest(String jsonPayLoad, String expectedName,
			String expectedSubObjectText, boolean isExpectedEmpty,
			String... expectedArrayValues) throws Exception {

		// Start the application
		WebAutoWireApplication app = new HttpServerAutoWireOfficeFloorSource();
		AutoWireSection servicer = app
				.addSection("SECTION", ClassSectionSource.class.getName(),
						MockService.class.getName());
		app.linkUri("service", servicer, "service");
		AutoWireObject json = app.addManagedObject(
				JsonRequestReaderManagedObjectSource.class.getName(), null,
				new AutoWire(MockJsonObject.class));
		json.addProperty(
				JsonRequestReaderManagedObjectSource.PROPERTY_JSON_OBJECT_CLASS,
				MockJsonObject.class.getName());
		AutoWireOfficeFloor officeFloor = app.openOfficeFloor();
		HttpClient client = new DefaultHttpClient();
		try {

			// Reset service to obtain the JSON object
			MockService.object = null;

			// Undertake request to obtain the object
			HttpPost post = new HttpPost("http://localhost:7878/service");
			if (jsonPayLoad != null) {
				post.setEntity(new StringEntity(jsonPayLoad));
			}
			HttpResponse response = client.execute(post);
			assertEquals("Incorrect response entity", "SERVICED",
					EntityUtils.toString(response.getEntity()));
			assertEquals("Request should be successful", 200, response
					.getStatusLine().getStatusCode());

		} finally {
			// Ensure stop client and server
			try {
				client.getConnectionManager().shutdown();
			} finally {
				officeFloor.closeOfficeFloor();
			}
		}

		// Ensure correctly loaded JSON object
		MockJsonObject jsonObject = MockService.object;
		assertNotNull("Should always have object", jsonObject);

		// Validate object loaded
		assertEquals("Incorrect name", expectedName, jsonObject.name);
		if (expectedArrayValues == null) {
			assertNull("Should not have array", jsonObject.array);
		} else {
			assertEquals("Incorrect number of array items",
					expectedArrayValues.length, jsonObject.array.length);
			int index = 0;
			for (String value : expectedArrayValues) {
				assertEquals("Incorrect value", value,
						jsonObject.array[index++]);
			}
		}
		MockJsonSubObject subObject = jsonObject.subObject;
		if (expectedSubObjectText == null) {
			assertNull("Should not have sub object", subObject);
		} else {
			assertNotNull("Should have sub object", subObject);
			assertEquals("Incorrect text for sub object",
					expectedSubObjectText, subObject.text);
		}
		if (isExpectedEmpty) {
			assertNotNull("Should have empty object", jsonObject.empty);
		} else {
			assertNull("Should be no empty object", jsonObject.empty);
		}
	}

	/**
	 * Mock servicing.
	 */
	public static class MockService {

		private MockJsonObject check = null;

		private static volatile MockJsonObject object = null;

		@NextTask("same")
		public void service(MockJsonObject object,
				ServerHttpConnection connection) throws IOException {

			// Specify the object to check same on another request task
			this.check = object;

			// Provide a response
			connection.getHttpResponse().getEntityWriter().write("SERVICED");
		}

		public void same(MockJsonObject object) {

			// Ensure same object (cached in HttpRequestState)
			assertSame("Object to be cached in HttpRequestState", this.check,
					object);

			// Specify the object for testing JSON
			MockService.object = object;
		}
	}

	/**
	 * Mock JSON object for testing.
	 */
	public static class MockJsonObject implements Serializable {

		private String name;

		private String[] array;

		private MockJsonSubObject subObject;

		private Object empty;

		public void setName(String name) {
			this.name = name;
		}

		public void setArray(String[] values) {
			this.array = values;
		}

		public void setSubObject(MockJsonSubObject subObject) {
			this.subObject = subObject;
		}

		public void setEmpty(Object empty) {
			this.empty = empty;
		}
	}

	/**
	 * Mock JSON sub object for testing.
	 */
	public static class MockJsonSubObject implements Serializable {

		private String text;

		public void setText(String text) {
			this.text = text;
		}
	}

}