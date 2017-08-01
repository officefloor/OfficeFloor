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
package net.officefloor.plugin.json.write;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;

import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.test.managedobject.ManagedObjectLoaderUtil;
import net.officefloor.compile.test.managedobject.ManagedObjectTypeBuilder;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.json.JsonResponseWriter;
import net.officefloor.plugin.json.write.JsonResponseWriterManagedObjectSource.Dependencies;
import net.officefloor.plugin.web.http.test.WebCompileOfficeFloor;
import net.officefloor.server.http.HttpClientTestUtil;
import net.officefloor.server.http.HttpServerTestUtil;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.server.impl.AbstractServerSocketManagedObjectSource;

/**
 * Tests the {@link JsonResponseWriterManagedObjectSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class JsonResponseWriterManagedObjectSourceTest extends OfficeFrameTestCase {

	/**
	 * Validate specification.
	 */
	public void testSpecification() {
		ManagedObjectLoaderUtil.validateSpecification(JsonResponseWriterManagedObjectSource.class);
	}

	/**
	 * Validate type.
	 */
	public void testType() {

		// Create the expected type
		ManagedObjectTypeBuilder type = ManagedObjectLoaderUtil.createManagedObjectTypeBuilder();
		type.setObjectClass(JsonResponseWriter.class);
		type.addDependency(Dependencies.SERVER_HTTP_CONNECTION, ServerHttpConnection.class, null);

		// Validate type
		ManagedObjectLoaderUtil.validateManagedObjectType(type, JsonResponseWriterManagedObjectSource.class);
	}

	/**
	 * Validate <code>null</code> JSON object.
	 */
	public void testNullObject() throws Exception {
		this.doWriteObjectTest(null, "null");
	}

	/**
	 * Validate <code>null</code> null values for JSON object properties.
	 */
	public void testNullValues() throws Exception {
		this.doWriteObjectTest(new MockJsonObject(null, null, null, (String[]) null),
				"{\"name\":null,\"array\":null,\"subObject\":null,\"empty\":null}");
	}

	/**
	 * Validate will provide values.
	 */
	public void testValues() throws Exception {
		this.doWriteObjectTest(
				new MockJsonObject("Daniel", new MockJsonSubObject("SUB"), new Object(), "ONE", "two", "Three"),
				"{\"name\":\"Daniel\",\"array\":[\"ONE\",\"two\",\"Three\"],\"subObject\":{\"text\":\"SUB\"},\"empty\":{}}");
	}

	/**
	 * Undertakes writing a JSON object to the response.
	 */
	private void doWriteObjectTest(MockJsonObject jsonObject, String expectedJsonEntity) throws Exception {

		// Start the application
		WebCompileOfficeFloor compiler = new WebCompileOfficeFloor();
		compiler.officeFloor((context) -> {
			HttpServerTestUtil.configureTestHttpServer(context, 7878, "ROUTE", "route");
		});
		compiler.web((context) -> {
			OfficeSection servicer = context.addSection("SECTION", MockService.class);
			context.getWebArchitect().linkUri("service", servicer.getOfficeSectionInput("service"));
			context.getOfficeArchitect()
					.addOfficeManagedObjectSource("JSON_RESPONSE",
							JsonResponseWriterManagedObjectSource.class.getName())
					.addOfficeManagedObject("JSON_RESPONSE", ManagedObjectScope.PROCESS);
		});
		OfficeFloor officeFloor = compiler.compileAndOpenOfficeFloor();
		try (CloseableHttpClient client = HttpClientTestUtil.createHttpClient()) {

			// Specify the JSON object
			MockService.object = jsonObject;

			// Undertake request to obtain the object
			HttpGet request = new HttpGet("http://localhost:7878/service");
			HttpResponse response = client.execute(request);
			assertEquals("Incorrect response entity", expectedJsonEntity, EntityUtils.toString(response.getEntity()));
			assertEquals("Request should be successful", 200, response.getStatusLine().getStatusCode());
			assertEquals("Must specify content type",
					"application/json; charset=" + AbstractServerSocketManagedObjectSource.getCharset(null).name(),
					response.getFirstHeader("Content-Type").getValue());

		} finally {
			// Ensure stop server (client already closed)
			officeFloor.closeOfficeFloor();
		}
	}

	/**
	 * Mock servicing.
	 */
	public static class MockService {

		private static volatile MockJsonObject object = null;

		public void service(JsonResponseWriter writer) throws IOException {
			writer.writeResponse(object);
		}
	}

	/**
	 * Mock JSON object for testing.
	 */
	public static class MockJsonObject {

		private final String name;

		private final String[] array;

		private final MockJsonSubObject subObject;

		private final Object empty;

		public MockJsonObject(String name, MockJsonSubObject subObject, Object empty, String... arrayValues) {
			this.name = name;
			this.array = arrayValues;
			this.subObject = subObject;
			this.empty = empty;
		}

		public String getName() {
			return this.name;
		}

		public String[] getArray() {
			return this.array;
		}

		public MockJsonSubObject getSubObject() {
			return this.subObject;
		}

		public Object getEmpty() {
			return this.empty;
		}
	}

	/**
	 * Mock JSON sub object for testing.
	 */
	public static class MockJsonSubObject {

		private final String text;

		public MockJsonSubObject(String text) {
			this.text = text;
		}

		public String getText() {
			return this.text;
		}
	}

}