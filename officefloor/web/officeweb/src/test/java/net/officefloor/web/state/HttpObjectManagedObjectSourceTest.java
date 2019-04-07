/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package net.officefloor.web.state;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import net.officefloor.compile.test.managedobject.ManagedObjectLoaderUtil;
import net.officefloor.compile.test.managedobject.ManagedObjectTypeBuilder;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.frame.util.ManagedObjectSourceStandAlone;
import net.officefloor.frame.util.ManagedObjectUserStandAlone;
import net.officefloor.server.http.EntityUtil;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.server.http.mock.MockHttpServer;
import net.officefloor.server.http.mock.MockServerHttpConnection;
import net.officefloor.web.build.HttpObjectParser;
import net.officefloor.web.build.HttpObjectParserFactory;
import net.officefloor.web.state.HttpObjectManagedObjectSource.HttpObjectDependencies;

/**
 * Tests the {@link HttpObjectManagedObjectSource}.
 *
 * @author Daniel Sagenschneider
 */
public class HttpObjectManagedObjectSourceTest extends OfficeFrameTestCase {

	private static final String MOCK_CONTENT_TYPE = "application/mock";

	/**
	 * Default {@link HttpObjectParserFactory}.
	 */
	private HttpObjectParserFactory defaultHttpObjectParserFactory = null;

	/**
	 * Ensure correct specification.
	 */
	public void testSpecification() {
		ManagedObjectLoaderUtil.validateSpecification(new HttpObjectManagedObjectSource<>(MockObject.class, null,
				new ArrayList<>(), () -> this.defaultHttpObjectParserFactory));
	}

	/**
	 * Ensure correct type.
	 */
	public void testType() {

		// Obtain the type
		ManagedObjectTypeBuilder type = ManagedObjectLoaderUtil.createManagedObjectTypeBuilder();
		type.setObjectClass(MockObject.class);
		type.addDependency(HttpObjectDependencies.SERVER_HTTP_CONNECTION, ServerHttpConnection.class, null);

		// Validate the managed object type
		ManagedObjectLoaderUtil.validateManagedObjectType(type, new HttpObjectManagedObjectSource<>(MockObject.class,
				null, Arrays.asList(new MockHttpObjectParserFactory()), () -> this.defaultHttpObjectParserFactory));
	}

	/**
	 * Ensure can load the HTTP object.
	 */
	public void testLoadObject() throws Throwable {
		this.doLoadObjectTest(MockObject.class, MOCK_CONTENT_TYPE,
				(httpObject) -> assertEquals("Incorrect parsed content", "TEST", httpObject.value));
	}

	/**
	 * Ensure propagate issue with creating {@link HttpObjectParser}.
	 */
	public void testFailObject() throws Throwable {
		try {
			this.doLoadObjectTest(Exception.class, MOCK_CONTENT_TYPE, null);
			fail("Should not be successful");
		} catch (Exception ex) {
			assertEquals("Incorrect cause",
					"Failed to create HttpObjectParser for Content-Type application/mock for object "
							+ Exception.class.getName() + " (cause: TEST)",
					ex.getMessage());
		}
	}

	/**
	 * Ensure issue if no {@link HttpObjectParser} made available.
	 */
	public void testNoObjectParser() throws Throwable {
		try {
			this.doLoadObjectTest(Void.class, MOCK_CONTENT_TYPE, null);
			fail("Should not be successful");
		} catch (Exception ex) {
			assertEquals("Incorrect cause",
					"No HttpObjectParser available for object " + Void.class.getName() + " in any Content-Type",
					ex.getMessage());
		}
	}

	/**
	 * Ensure issue if the <code>content-type</code> required is not supported.
	 */
	public void testContentTypeNotSupported() throws Throwable {
		try {
			this.doLoadObjectTest(MockObject.class, MOCK_CONTENT_TYPE, null, "unsupported/content");
			fail("Should not be successful");
		} catch (Exception ex) {
			assertEquals("Incorrect cause", "No HttpObjectParser available for object " + MockObject.class.getName()
					+ " for accepting Content-Type unsupported/content", ex.getMessage());
		}
	}

	/**
	 * Ensure can default the {@link HttpObjectParser}.
	 */
	public void testDefaultObjectParser() throws Throwable {
		this.defaultHttpObjectParserFactory = new MockHttpObjectParserFactory();
		this.doLoadObjectTest(MockObject.class, MOCK_CONTENT_TYPE, false,
				(httpObject) -> assertEquals("Incorrect parsed content", "TEST", httpObject.value));
	}

	/**
	 * Undertakes the load HTTP object test.
	 */
	private <T> void doLoadObjectTest(Class<T> objectType, String contentType, Consumer<T> validator,
			String... acceptedContentTypes) throws Throwable {
		this.doLoadObjectTest(objectType, contentType, true, validator, acceptedContentTypes);
	}

	/**
	 * Undertakes the load HTTP object test.
	 */
	@SuppressWarnings("unchecked")
	private <T> void doLoadObjectTest(Class<T> objectType, String contentType, boolean isAddParserFactory,
			Consumer<T> validator, String... acceptedContentTypes) throws Throwable {

		// Create the connection
		MockServerHttpConnection connection = MockHttpServer
				.mockConnection(MockHttpServer.mockRequest().header("content-type", contentType).entity("TEST"));

		// Create the list of object parsers
		List<HttpObjectParserFactory> objectParserFactories = new ArrayList<>(1);
		if (isAddParserFactory) {
			objectParserFactories.add(new MockHttpObjectParserFactory());
		} else {
			objectParserFactories.add(new NoHttpObjectParserFactory());
		}

		// Source the managed object source
		ManagedObjectSourceStandAlone loader = new ManagedObjectSourceStandAlone();
		HttpObjectManagedObjectSource<T> mos = loader.loadManagedObjectSource(new HttpObjectManagedObjectSource<>(
				objectType, acceptedContentTypes, objectParserFactories, () -> this.defaultHttpObjectParserFactory));

		// Load the HTTP Object
		ManagedObjectUserStandAlone user = new ManagedObjectUserStandAlone();
		user.mapDependency(HttpObjectDependencies.SERVER_HTTP_CONNECTION, connection);
		ManagedObject mo = user.sourceManagedObject(mos);
		Object object = mo.getObject();

		// Ensure correctly parsed out object
		assertEquals("Incorrect object class", objectType, object.getClass());
		validator.accept((T) object);
	}

	private static class NoHttpObjectParserFactory implements HttpObjectParserFactory {

		@Override
		public String getContentType() {
			return MOCK_CONTENT_TYPE;
		}

		@Override
		public <T> HttpObjectParser<T> createHttpObjectParser(Class<T> objectClass) throws Exception {
			return null;
		}
	}

	private static class MockHttpObjectParserFactory implements HttpObjectParserFactory {

		@Override
		public String getContentType() {
			return MOCK_CONTENT_TYPE;
		}

		@Override
		@SuppressWarnings("unchecked")
		public <T> HttpObjectParser<T> createHttpObjectParser(Class<T> objectType) throws Exception {
			if (objectType == MockObject.class) {
				return (HttpObjectParser<T>) new MockHttpObjectParser();
			} else if (objectType == Exception.class) {
				throw new Exception("TEST");
			} else {
				return null;
			}
		}
	}

	private static class MockHttpObjectParser implements HttpObjectParser<MockObject> {

		@Override
		public String getContentType() {
			return "application/mock";
		}

		@Override
		public Class<MockObject> getObjectType() {
			return MockObject.class;
		}

		@Override
		public MockObject parse(ServerHttpConnection connection) {
			String content = EntityUtil.toString(connection.getRequest(), null);
			return new MockObject(content);
		}
	}

	private static class MockObject {

		private final String value;

		private MockObject(String value) {
			this.value = value;
		}
	}

}