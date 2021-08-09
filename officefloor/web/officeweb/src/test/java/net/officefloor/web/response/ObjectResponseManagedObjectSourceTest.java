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

import net.officefloor.compile.test.managedobject.ManagedObjectLoaderUtil;
import net.officefloor.compile.test.managedobject.ManagedObjectTypeBuilder;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.test.OfficeFrameTestCase;
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
import net.officefloor.web.build.HttpObjectResponder;
import net.officefloor.web.build.HttpObjectResponderFactory;
import net.officefloor.web.response.ObjectResponseManagedObjectSource.DefaultHttpObjectResponder;
import net.officefloor.web.response.ObjectResponseManagedObjectSource.ObjectResponseDependencies;
import net.officefloor.web.state.HttpObjectManagedObjectSource.HttpObjectDependencies;

/**
 * Tests the {@link ObjectResponseManagedObjectSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class ObjectResponseManagedObjectSourceTest extends OfficeFrameTestCase {

	/**
	 * Default {@link HttpObjectResponderFactory}.
	 */
	private HttpObjectResponderFactory defaultHttpObjectResponderFactory = null;

	/**
	 * {@link HttpStatus} for response.
	 */
	private HttpStatus httpStatus = HttpStatus.OK;

	/**
	 * Validate specification.
	 */
	public void testSpecification() {
		ManagedObjectLoaderUtil.validateSpecification(new ObjectResponseManagedObjectSource(HttpStatus.OK,
				Arrays.asList(new MockHttpObjectResponderFactory("application/json", 0)),
				() -> this.defaultHttpObjectResponderFactory));
	}

	/**
	 * Ensure correct type.
	 */
	public void testType() {
		// Obtain the type
		ManagedObjectTypeBuilder type = ManagedObjectLoaderUtil.createManagedObjectTypeBuilder();
		type.setObjectClass(ObjectResponse.class);
		type.addDependency(ObjectResponseDependencies.SERVER_HTTP_CONNECTION, ServerHttpConnection.class, null);

		// Validate the managed object type
		ManagedObjectLoaderUtil.validateManagedObjectType(type,
				new ObjectResponseManagedObjectSource(HttpStatus.OK,
						Arrays.asList(new MockHttpObjectResponderFactory("application/json", 0)),
						() -> this.defaultHttpObjectResponderFactory));
	}

	/**
	 * Ensure have at least one {@link HttpObjectResponderFactory}.
	 */
	@SuppressWarnings("unchecked")
	public void testNoFactory() {
		try {
			new ManagedObjectSourceStandAlone().initManagedObjectSource(
					new ObjectResponseManagedObjectSource(HttpStatus.OK, Collections.EMPTY_LIST, () -> null));
			fail("Should not be successful");
		} catch (Exception ex) {
			assertEquals("Incorrect cause", "Must have at least one HttpObjectResponderFactory configured",
					ex.getMessage());
		}
	}

	/**
	 * Ensure can send based on input <code>content-type</code> as no accept type.
	 */
	public void testMatchContentTypeAsNoAccept() throws Throwable {
		MockHttpResponse response = this.doObjectResponse(A(), "content/type", new MockObject("TEST"),
				A("application/not-match", "content/type"));
		assertResponse(response, "content/type", "{value: 'TEST-1'}");
	}

	/**
	 * Ensure can send based on input <code>content-type</code> as accept any type.
	 */
	public void testMatchContentTypeAsWildcardAccept() throws Throwable {
		MockHttpResponse response = this.doObjectResponse(A("*/*"), "content/type", new MockObject("TEST"),
				A("application/not-match", "content/type"));
		assertResponse(response, "content/type", "{value: 'TEST-1'}");
	}

	/**
	 * Default to first sorted <code>content-type</code> if any.
	 */
	public void testDefaultFirstAsWildcardAccept() throws Throwable {
		MockHttpResponse response = this.doObjectResponse(A("*/*"), "content/type", new MockObject("TEST"),
				A("application/first", "application/second"));
		assertResponse(response, "application/second", "{value: 'TEST-1'}");
	}

	/**
	 * Ensure 406 if no <code>content-type</code> acceptable.
	 */
	public void testNotMatchAccept() throws Throwable {
		try {
			this.doObjectResponse("not/match", new MockObject("TEST"), "application/json", "application/xml");
			fail("Should not be successful");
		} catch (HttpException ex) {
			assertEquals("Should not accept", 406, ex.getHttpStatus().getStatusCode());
			HttpHeader[] headers = ex.getHttpHeaders();
			assertEquals("Should have accept response header", 1, headers.length);
			assertEquals("Not accept response hader", "accept", headers[0].getName());
			assertEquals("Should indicate what to accept", "application/json, application/xml", headers[0].getValue());
		}
	}

	/**
	 * Ensure matches on specific sub-type first.
	 */
	public void testMatchOnSubTypeFirst() throws Throwable {
		MockHttpResponse response = this.doObjectResponse("*/*,application/*,application/match", new MockObject("TEST"),
				"application/not-match", "application/match");
		assertResponse(response, "application/match", "{value: 'TEST-1'}");
	}

	/**
	 * Ensure matches on specific type first.
	 */
	public void testMatchOnTypeFirst() throws Throwable {
		MockHttpResponse response = this.doObjectResponse("*/*,application/*", new MockObject("TEST"), "not/match",
				"application/match");
		assertResponse(response, "application/match", "{value: 'TEST-1'}");
	}

	/**
	 * Ensure matches as per q priority.
	 */
	public void testMatchOnPriority() throws Throwable {
		MockHttpResponse response = this.doObjectResponse("application/not-match;q=0.8,application/match;q=1",
				new MockObject("TEST"), "application/not-match", "application/match");
		assertResponse(response, "application/match", "{value: 'TEST-1'}");
	}

	/**
	 * Ensure matches as per q priority.
	 */
	public void testMatchOnPriorityBeforeWildcards() throws Throwable {
		MockHttpResponse response = this.doObjectResponse(
				"application/*,*/*,application/not-match;q=0.6,application/first;q=0.8", new MockObject("TEST"),
				"application/first", "application/not-match");
		assertResponse(response, "application/first", "{value: 'TEST-0'}");
	}

	/**
	 * Ensure ordering by most specific.
	 */
	public void testMatchKeepingOrderOfSameQValues() throws Throwable {
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
	 * Ensure use {@link DefaultHttpObjectResponder}.
	 */
	public void testMatchWithDefaultHttpObjectResponderFactory() throws Throwable {
		this.defaultHttpObjectResponderFactory = new MockHttpObjectResponderFactory("application/default", -1);
		MockHttpResponse response = this.doObjectResponse("application/default", new MockObject("TEST"));
		assertResponse(response, "application/default", "{value: 'TEST--1'}");
	}

	/**
	 * Ensure can parse out values with spacing.
	 */
	public void testParsingWithSpacing() throws Throwable {
		MockHttpResponse response = this.doObjectResponse(
				"\t application/* \t , \t */* \t , application/match \t ; \t q=0.6 ; \t another = value \t ; param",
				new MockObject("TEST"), "application/not-match", "application/match");
		assertResponse(response, "application/match", "{value: 'TEST-1'}");
	}

	/**
	 * Ensure can provide different response status.
	 */
	public void testDifferentResponseStatus() throws Throwable {
		this.httpStatus = HttpStatus.CREATED;
		MockHttpResponse response = this.doObjectResponse("application/match", new MockObject("TEST"),
				"application/match");
		assertEquals("Incorrect status", 201, response.getStatus().getStatusCode());
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

		// Source the managed object source
		ManagedObjectSourceStandAlone loader = new ManagedObjectSourceStandAlone();
		ObjectResponseManagedObjectSource mos = loader.loadManagedObjectSource(new ObjectResponseManagedObjectSource(
				this.httpStatus, factories, () -> this.defaultHttpObjectResponderFactory));

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
		assertEquals("Incorrect response status", this.httpStatus.getStatusCode(),
				response.getStatus().getStatusCode());
		assertEquals("Incorrect response content", contentType, response.getHeader("content-type").getValue());
		assertEquals("Incorrect response", entity, response.getEntity(null));
	}

	private static class MockHttpObjectResponderFactory implements HttpObjectResponderFactory {

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
		public <E extends Throwable> HttpObjectResponder<E> createHttpEscalationResponder(Class<E> escalationType) {
			return new MockHttpEscalationResponder<>(escalationType);
		}

		private class MockHttpObjectResponder implements HttpObjectResponder<MockObject> {

			@Override
			public String getContentType() {
				return MockHttpObjectResponderFactory.this.contentType;
			}

			@Override
			public Class<MockObject> getObjectType() {
				return MockObject.class;
			}

			@Override
			public void send(MockObject object, ServerHttpConnection connection) throws IOException {
				HttpResponse response = connection.getResponse();
				response.setContentType(MockHttpObjectResponderFactory.this.headerValue, null);
				response.getEntityWriter()
						.write("{value: '" + object.value + "-" + MockHttpObjectResponderFactory.this.index + "'}");
			}
		}

		public class MockHttpEscalationResponder<E extends Throwable> implements HttpObjectResponder<E> {

			private final Class<E> escalationType;

			public MockHttpEscalationResponder(Class<E> escalationType) {
				this.escalationType = escalationType;
			}

			@Override
			public String getContentType() {
				return MockHttpObjectResponderFactory.this.contentType;
			}

			@Override
			public Class<E> getObjectType() {
				return this.escalationType;
			}

			@Override
			public void send(E escalation, ServerHttpConnection connection) throws IOException {
				HttpResponse response = connection.getResponse();
				response.setContentType(MockHttpObjectResponderFactory.this.headerValue, null);
				response.getEntityWriter().write(
						"{error: '" + escalation.getMessage() + "-" + MockHttpObjectResponderFactory.this.index + "'}");
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
