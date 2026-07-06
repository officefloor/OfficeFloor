/*-
 * #%L
 * Web Plug-in
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

package net.officefloor.web.response;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import net.officefloor.compile.managedfunction.ManagedFunctionObjectType;
import net.officefloor.compile.managedfunction.ManagedFunctionType;
import net.officefloor.compile.test.managedobject.ManagedObjectLoaderUtil;
import net.officefloor.compile.test.managedobject.ManagedObjectTypeBuilder;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.test.MockTestSupport;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.frame.test.TestSupportExtension;
import net.officefloor.frame.util.ManagedObjectSourceStandAlone;
import net.officefloor.frame.util.ManagedObjectUserStandAlone;
import net.officefloor.server.http.HttpException;
import net.officefloor.server.http.HttpHeader;
import net.officefloor.server.http.HttpHeaderValue;
import net.officefloor.server.http.HttpResponse;
import net.officefloor.server.http.HttpStatus;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.server.http.mock.MockHttpRequestBuilder;
import net.officefloor.server.http.mock.MockHttpResponse;
import net.officefloor.server.http.mock.MockHttpServer;
import net.officefloor.server.http.mock.MockServerHttpConnection;
import net.officefloor.web.ObjectResponse;
import net.officefloor.web.build.HttpEscalationResponder;
import net.officefloor.web.build.HttpEscalationResponderContext;
import net.officefloor.web.build.HttpObjectResponder;
import net.officefloor.web.build.HttpObjectResponderContext;
import net.officefloor.web.build.HttpObjectResponderFactory;
import net.officefloor.web.response.ObjectResponseManagedObjectSource.ObjectResponseDependencies;
import net.officefloor.web.state.HttpObjectManagedObjectSource.HttpObjectDependencies;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Tests the {@link ObjectResponseManagedObjectSource}.
 * 
 * @author Daniel Sagenschneider
 */
@ExtendWith(TestSupportExtension.class)
public class ObjectResponseManagedObjectSourceTest {

	/**
	 * {@link MockTestSupport}.
	 */
	private final MockTestSupport mocks = new MockTestSupport();

	/**
	 * Default {@link HttpObjectResponderFactory}.
	 */
	private HttpObjectResponderFactory defaultHttpObjectResponderFactory = null;

	/**
	 * {@link HttpStatus} for response.
	 */
	private HttpStatus httpStatus = HttpStatus.OK;

	/**
	 * Mock {@link ManagedFunctionType}.
	 */
	private ManagedFunctionType<?, ?> managedFunctionType;

	/**
	 * Mock {@link ManagedFunctionObjectType}.
	 */
	private ManagedFunctionObjectType<?> managedFunctionObjectType;

	@BeforeEach
	public void setup() {
		this.managedFunctionType = this.mocks.createMock(ManagedFunctionType.class);
		this.managedFunctionObjectType = this.mocks.createMock(ManagedFunctionObjectType.class);
	}

	/**
	 * Validate specification.
	 */
	@Test
	public void specification() {
		HttpResponder httpResponder = new HttpResponder(List.of(new MockHttpObjectResponderFactory("application/json", 0)),
				() -> this.defaultHttpObjectResponderFactory);
		ManagedObjectLoaderUtil.validateSpecification(new ObjectResponseManagedObjectSource(httpResponder, HttpStatus.OK,
				this.managedFunctionType, this.managedFunctionObjectType));
	}

	/**
	 * Ensure correct type.
	 */
	@Test
	public void type() {

		// Obtain the type
		ManagedObjectTypeBuilder type = ManagedObjectLoaderUtil.createManagedObjectTypeBuilder();
		type.setObjectClass(ObjectResponse.class);
		type.addDependency(ObjectResponseDependencies.SERVER_HTTP_CONNECTION, ServerHttpConnection.class, null);

		// Validate the managed object type
		HttpResponder httpResponder = new HttpResponder(List.of(new MockHttpObjectResponderFactory("application/json", 0)),
				() -> this.defaultHttpObjectResponderFactory);
		ManagedObjectLoaderUtil.validateManagedObjectType(type, new ObjectResponseManagedObjectSource(httpResponder,
				HttpStatus.OK, this.managedFunctionType, this.managedFunctionObjectType));
	}

	/**
	 * Ensure have at least one {@link HttpObjectResponderFactory}.
	 */
	@Test
	@SuppressWarnings("unchecked")
	public void testNoFactory() {
		try {
			HttpResponder httpResponder = new HttpResponder(Collections.EMPTY_LIST, () -> null);
			httpResponder.build();
			fail("Should not be successful");
		} catch (Exception ex) {
			assertEquals("Must have at least one HttpObjectResponderFactory configured",
					ex.getMessage(), "Incorrect cause");
		}
	}

	/**
	 * Ensure can send based on input <code>content-type</code> as no accept type.
	 */
	@Test
	public void matchContentTypeAsNoAccept() throws Throwable {
		MockHttpResponse response = this.doObjectResponse(A(), "content/type", new MockObject("TEST"),
				A("application/not-match", "content/type"));
		assertResponse(response, "content/type", "{value: 'TEST-1'}");
	}

	/**
	 * Ensure can send based on input <code>content-type</code> as accept any type.
	 */
	@Test
	public void matchContentTypeAsWildcardAccept() throws Throwable {
		MockHttpResponse response = this.doObjectResponse(A("*/*"), "content/type", new MockObject("TEST"),
				A("application/not-match", "content/type"));
		assertResponse(response, "content/type", "{value: 'TEST-1'}");
	}

	/**
	 * Default to first sorted <code>content-type</code> if any.
	 */
	@Test
	public void defaultFirstAsWildcardAccept() throws Throwable {
		MockHttpResponse response = this.doObjectResponse(A("*/*"), "content/type", new MockObject("TEST"),
				A("application/first", "application/second"));
		assertResponse(response, "application/second", "{value: 'TEST-1'}");
	}

	/**
	 * Ensure 406 if no <code>content-type</code> acceptable.
	 */
	@Test
	public void notMatchAccept() throws Throwable {
		try {
			this.doObjectResponse("not/match", new MockObject("TEST"), "application/json", "application/xml");
			fail("Should not be successful");
		} catch (HttpException ex) {
			assertEquals(406, ex.getHttpStatus().getStatusCode(), "Should not accept");
			HttpHeader[] headers = ex.getHttpHeaders();
			assertEquals(1, headers.length, "Should have accept response header");
			assertEquals("accept", headers[0].getName(), "Not accept response hader");
			assertEquals("application/json, application/xml", headers[0].getValue(), "Should indicate what to accept");
		}
	}

	/**
	 * Ensure matches on specific sub-type first.
	 */
	@Test
	public void matchOnSubTypeFirst() throws Throwable {
		MockHttpResponse response = this.doObjectResponse("*/*,application/*,application/match", new MockObject("TEST"),
				"application/not-match", "application/match");
		assertResponse(response, "application/match", "{value: 'TEST-1'}");
	}

	/**
	 * Ensure matches on specific type first.
	 */
	@Test
	public void matchOnTypeFirst() throws Throwable {
		MockHttpResponse response = this.doObjectResponse("*/*,application/*", new MockObject("TEST"), "not/match",
				"application/match");
		assertResponse(response, "application/match", "{value: 'TEST-1'}");
	}

	/**
	 * Ensure matches as per q priority.
	 */
	@Test
	public void matchOnPriority() throws Throwable {
		MockHttpResponse response = this.doObjectResponse("application/not-match;q=0.8,application/match;q=1",
				new MockObject("TEST"), "application/not-match", "application/match");
		assertResponse(response, "application/match", "{value: 'TEST-1'}");
	}

	/**
	 * Ensure matches as per q priority.
	 */
	@Test
	public void matchOnPriorityBeforeWildcards() throws Throwable {
		MockHttpResponse response = this.doObjectResponse(
				"application/*,*/*,application/not-match;q=0.6,application/first;q=0.8", new MockObject("TEST"),
				"application/first", "application/not-match");
		assertResponse(response, "application/first", "{value: 'TEST-0'}");
	}

	/**
	 * Ensure ordering by most specific.
	 */
	@Test
	public void matchKeepingOrderOfSameQValues() throws Throwable {
		// Match becoming more specific
		List<String> availableContentTypes = new LinkedList<>();
		for (String availableContentType : new String[] { "wildcard/wildcard", "specific/wildcard", "specific/type",
				"parameter/type", "multiple/parameters" }) {
			availableContentTypes.add(availableContentType);

			MockHttpResponse response = this.doObjectResponse(
					"*/*,specific/*,specific/type,parameter/type;one=parameter,multiple/parameters;has=two;parameters",
					new MockObject("TEST"), availableContentTypes.toArray(new String[0]));
			assertResponse(response, availableContentType,
					"{value: 'TEST-" + (availableContentTypes.size() - 1) + "'}");
		}
	}

	/**
	 * Ensure use {@link HttpResponder.DefaultHttpObjectResponder}.
	 */
	@Test
	public void matchWithDefaultHttpObjectResponderFactory() throws Throwable {
		this.defaultHttpObjectResponderFactory = new MockHttpObjectResponderFactory("application/default", -1);
		MockHttpResponse response = this.doObjectResponse("application/default", new MockObject("TEST"));
		assertResponse(response, "application/default", "{value: 'TEST--1'}");
	}

	/**
	 * Ensure can parse out values with spacing.
	 */
	@Test
	public void parsingWithSpacing() throws Throwable {
		MockHttpResponse response = this.doObjectResponse(
				"\t application/* \t , \t */* \t , application/match \t ; \t q=0.6 ; \t another = value \t ; param",
				new MockObject("TEST"), "application/not-match", "application/match");
		assertResponse(response, "application/match", "{value: 'TEST-1'}");
	}

	/**
	 * Ensure can provide different response status.
	 */
	@Test
	public void differentResponseStatus() throws Throwable {
		this.httpStatus = HttpStatus.CREATED;
		MockHttpResponse response = this.doObjectResponse("application/match", new MockObject("TEST"),
				"application/match");
		assertEquals(201, response.getStatus().getStatusCode(), "Incorrect status");
		assertResponse(response, "application/match", "{value: 'TEST-0'}");
	}

	private static String[] A(String... values) {
		return values;
	}

	/**
	 * Convenience method to do the object response test.
	 */
	private MockHttpResponse doObjectResponse(String requestAcceptedContentType, Object responseObject,
			String... objectResponderContentTypes) throws Throwable {
		return this.doObjectResponse(requestAcceptedContentType == null ? new String[0] : A(requestAcceptedContentType),
				null, responseObject, objectResponderContentTypes);
	}

	/**
	 * Undertakes the object response test.
	 */
	@SuppressWarnings("unchecked")
	private MockHttpResponse doObjectResponse(String[] requestAcceptedContentTypes, String inputContentType,
			Object responseObject, String[] objectResponderContentTypes) throws Throwable {

		// Create the connection
		MockHttpRequestBuilder request = MockHttpServer.mockRequest();
		for (String acceptedContentType : requestAcceptedContentTypes) {
			request.header("accept", acceptedContentType);
		}
		if (inputContentType != null) {
			request.header("content-type", inputContentType);
			request.entity("TEST");
		}
		MockServerHttpConnection connection = MockHttpServer.mockConnection(request);

		// Create the object responders for each content type
		List<HttpObjectResponderFactory> factories = new ArrayList<>(objectResponderContentTypes.length);
		int index = 0;
		for (String responderContentType : objectResponderContentTypes) {
			factories.add(new MockHttpObjectResponderFactory(responderContentType, index++));
		}

		// Create the HTTP responder
		HttpResponder httpResponder = new HttpResponder(factories, () -> this.defaultHttpObjectResponderFactory);

		// Source the managed object source
		ManagedObjectSourceStandAlone loader = new ManagedObjectSourceStandAlone();
		ObjectResponseManagedObjectSource mos = loader.loadManagedObjectSource(new ObjectResponseManagedObjectSource(
				httpResponder, this.httpStatus, this.managedFunctionType, this.managedFunctionObjectType));

		// Obtain the object response
		ManagedObjectUserStandAlone user = new ManagedObjectUserStandAlone();
		user.mapDependency(HttpObjectDependencies.SERVER_HTTP_CONNECTION, connection);
		ManagedObject mo = user.sourceManagedObject(mos);
		ObjectResponse<Object> objectResponse = (ObjectResponse<Object>) mo.getObject();

		// Send the response
		objectResponse.send(responseObject);
		return connection.send(null);
	}

	/**
	 * Asserts the {@link MockHttpResponse} to be correct.
	 */
	private void assertResponse(MockHttpResponse response, String contentType, String entity) {
		assertEquals(this.httpStatus.getStatusCode(), response.getStatus().getStatusCode(), "Incorrect response status");
		assertEquals(contentType, response.getHeader("content-type").getValue(), "Incorrect response content");
		assertEquals(entity, response.getEntity(null), "Incorrect response");
	}

	private class MockHttpObjectResponderFactory implements HttpObjectResponderFactory {

		private final String contentType;

		private final int index;

		private final HttpHeaderValue headerValue;

		private MockHttpObjectResponderFactory(String contentType, int index) {
			this.contentType = contentType;
			this.index = index;
			this.headerValue = new HttpHeaderValue(contentType);
		}

		@Override
		public String getContentType() {
			return this.contentType;
		}

		@Override
		@SuppressWarnings("unchecked")
		public <T> HttpObjectResponder<T> createHttpObjectResponder(Class<T> objectType) {
			if (objectType == MockObject.class) {
				return (HttpObjectResponder<T>) new MockHttpObjectResponder();
			} else {
				return null;
			}
		}

		@Override
		public <E extends Throwable> HttpEscalationResponder<E> createHttpEscalationResponder(Class<E> escalationType, boolean isOfficeFloorEscalation) {
			return new MockHttpEscalationResponder<>(escalationType, isOfficeFloorEscalation);
		}

		private class MockHttpObjectResponder implements HttpObjectResponder<MockObject> {

			@Override
			public String getContentType() {
				return MockHttpObjectResponderFactory.this.contentType;
			}

			@Override
			public void send(HttpObjectResponderContext<MockObject> context) throws IOException {

				// Ensure correct details of object sending the response
				assertSame(ObjectResponseManagedObjectSourceTest.this.managedFunctionType, context.getManagedFunctionType(), "Incorrect function type");
				assertSame(ObjectResponseManagedObjectSourceTest.this.managedFunctionObjectType, context.getManagedFunctionObjectType(), "Incorrect function object type");

				// Send the response
				HttpResponse response = context.getServerHttpConnection().getResponse();
				response.setContentType(MockHttpObjectResponderFactory.this.headerValue, null);
				response.getEntityWriter()
						.write("{value: '" + context.getResponseObject().value + "-" + MockHttpObjectResponderFactory.this.index + "'}");
			}
		}

		public class MockHttpEscalationResponder<E extends Throwable> implements HttpEscalationResponder<E> {

			private final Class<E> escalationType;

			private boolean isOfficeFloorEscalation;

			public MockHttpEscalationResponder(Class<E> escalationType, boolean isOfficeFloorEscalation) {
				this.escalationType = escalationType;
				this.isOfficeFloorEscalation = isOfficeFloorEscalation;
			}

			@Override
			public String getContentType() {
				return MockHttpObjectResponderFactory.this.contentType;
			}

			@Override
			public void send(HttpEscalationResponderContext<E> context) throws IOException {
				assertEquals(this.isOfficeFloorEscalation, context.isOfficeFloorEscalation(), "Should pass through same value indicating if handled");
				HttpResponse response = context.getServerHttpConnection().getResponse();
				response.setContentType(MockHttpObjectResponderFactory.this.headerValue, null);
				response.getEntityWriter().write(
						"{error: '" + context.getEscalation().getMessage() + "-" + MockHttpObjectResponderFactory.this.index + "'}");
			}
		}
	}

	private static class MockObject {

		private final String value;

		private MockObject(String value) {
			this.value = value;
		}
	}

}
