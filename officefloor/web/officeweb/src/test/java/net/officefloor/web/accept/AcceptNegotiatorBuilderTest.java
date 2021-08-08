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

package net.officefloor.web.accept;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;

import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.server.http.HttpHeader;
import net.officefloor.server.http.mock.MockHttpRequestBuilder;
import net.officefloor.server.http.mock.MockHttpServer;
import net.officefloor.web.build.AcceptNegotiatorBuilder;
import net.officefloor.web.build.NoAcceptHandlersException;

/**
 * Tests the {@link AcceptNegotiatorBuilder} and it's built
 * {@link AcceptNegotiator}.
 * 
 * @author Daniel Sagenschneider
 */
public class AcceptNegotiatorBuilderTest extends OfficeFrameTestCase {

	/**
	 * Ensure have at least one handler.
	 */
	public void testNoFactory() throws Exception {
		try {
			this.doTest(null, this.request(null, null));
			fail("Should not be successful");
		} catch (NoAcceptHandlersException ex) {
			assertEquals("Incorrect cause", "Must have at least one AcceptHandler configured for the AcceptNegotiator",
					ex.getMessage());
		}
	}

	/**
	 * Ensure can negotiate based on input <code>content-type</code> as no
	 * accept type.
	 */
	public void testMatchContentTypeAsNoAccept() throws Exception {
		this.doSpecificAndGenericTest("content/type", this.request(null, "content/type"), "application/not-match",
				"content/type");
	}

	/**
	 * Ensure can negotiate based on input <code>content-type</code> as accept
	 * any type.
	 */
	public void testMatchContentTypeAsWildcardAccept() throws Exception {
		this.doSpecificAndGenericTest("content/type", this.request("*/*", "content/type"), "application/not-match",
				"content/type");
	}

	/**
	 * Default to first <code>content-type</code> if any.
	 */
	public void testDefaultFirstAsWildcardAccept() throws Exception {
		this.doTest("application/second", this.request("*/*"), "application/first", "application/second");
	}

	/**
	 * Ensure 406 if no <code>content-type</code> acceptable.
	 */
	public void testNotMatchAccept() throws Exception {
		this.doSpecificAndGenericTest(null, this.request("not/match"), "application/json", "application/xml");
	}

	/**
	 * Ensure matches on specific sub-type first.
	 */
	public void testMatchOnSubTypeFirst() throws Exception {
		this.doSpecificAndGenericTest("application/match", this.request("*/*,application/*,application/match"),
				"application/not-match", "application/match");
	}

	/**
	 * Ensure matches on specific type first.
	 */
	public void testMatchOnTypeFirst() throws Exception {
		this.doSpecificAndGenericTest("application/match", this.request("*/*,application/*"), "not/match",
				"application/match");
	}

	/**
	 * Ensure matches as per q priority.
	 */
	public void testMatchOnPriority() throws Throwable {
		this.doSpecificAndGenericTest("application/match",
				this.request("application/not-match;q=0.8,application/match;q=1"), "application/not-match",
				"application/match");
	}

	/**
	 * Ensure matches as per q priority.
	 */
	public void testMatchOnPriorityBeforeWildcards() throws Throwable {
		this.doSpecificAndGenericTest("application/first",
				this.request("application/*,*/*,application/not-match;q=0.6,application/first;q=0.8"),
				"application/first", "application/not-match");
	}

	/**
	 * Ensure ordering by most specific.
	 */
	public void testMatchKeepingOrderOfSameQValues() throws Exception {
		// Match becoming more specific
		List<String> availableContentTypes = new LinkedList<>();
		for (String availableContentType : new String[] { "wildcard/wildcard", "specific/wildcard", "specific/type",
				"parameter/type", "multiple/parameters" }) {
			availableContentTypes.add(availableContentType);

			this.doSpecificAndGenericTest(availableContentType,
					this.request(
							"*/*,specific/*,specific/type,parameter/type;one=parameter,multiple/parameters;has=two;parameters"),
					availableContentTypes.toArray(new String[0]));
		}
	}

	/**
	 * Ensure can parse out values with spacing.
	 */
	public void testParsingWithSpacing() throws Exception {
		this.doSpecificAndGenericTest("application/match",
				this.request(
						"\t application/* \t , \t */* \t , application/match \t ; \t q=0.6 ; \t another = value \t ; param"),
				"application/not-match", "application/match");
	}

	/**
	 * Ensure default matcher is used last.
	 */
	public void testDefaultContentTypeMatchedLast() throws Exception {
		String[] contentTypes = new String[] { "*/*", "type/*", "type/subtype" };
		this.doTest("type/subtype", this.request("type/subtype"), contentTypes);
		this.doTest("type/*", this.request("type/not"), contentTypes);
		this.doTest("*/*", this.request("not/not"), contentTypes);
	}

	/**
	 * Creates the {@link MockHttpRequestBuilder}.
	 * 
	 * @param acceptHeaderValue
	 *            <code>Accept</code> {@link HttpHeader} value.
	 * @return {@link MockHttpRequestBuilder}.
	 */
	private MockHttpRequestBuilder request(String acceptHeaderValue) {
		return this.request(acceptHeaderValue, null);
	}

	/**
	 * Create the {@link MockHttpRequestBuilder}.
	 * 
	 * @param acceptHeaderValue
	 *            <code>Accept</code> {@link HttpHeader} value.
	 * @param contentTypeHeaderValue
	 *            <code>Content-Type</code> {@link HttpHeader} value.
	 * @return {@link MockHttpRequestBuilder}.
	 */
	private MockHttpRequestBuilder request(String acceptHeaderValue, String contentTypeHeaderValue) {
		MockHttpRequestBuilder request = MockHttpServer.mockRequest();
		if (acceptHeaderValue != null) {
			request.header("accept", acceptHeaderValue);
		}
		if (contentTypeHeaderValue != null) {
			request.header("content-type", contentTypeHeaderValue);
		}
		return request;
	}

	/**
	 * Undertakes the test and the generic test.
	 * 
	 * @param expectedResult
	 *            Expected result (<code>Content-Type</code> used as handler).
	 * @param request
	 *            {@link MockHttpRequestBuilder}.
	 * @param contentTypes
	 *            <code>Content-Type</code> instances to create handlers.
	 */
	private void doSpecificAndGenericTest(String expectedResult, MockHttpRequestBuilder request, String... contentTypes)
			throws NoAcceptHandlersException {

		// Specific type test
		this.doTest(expectedResult, request, contentTypes);

		// Transform type to generic
		Function<String, String> transform = (type) -> type.split("/")[0] + "/*";

		// Setup for generic test
		if (expectedResult != null) {
			// Expected result, so transform only the result
			String genericType = transform.apply(expectedResult);
			for (int i = 0; i < contentTypes.length; i++) {
				if (expectedResult.equals(contentTypes[i])) {
					contentTypes[i] = genericType;
				}
			}
			expectedResult = genericType;

		} else {
			// No expected, result so transform all types
			for (int i = 0; i < contentTypes.length; i++) {
				contentTypes[i] = transform.apply(contentTypes[i]);
			}
		}

		// Undertake generic type test
		this.doTest(expectedResult, request, contentTypes);
	}

	/**
	 * Undertakes the test.
	 * 
	 * @param expectedResult
	 *            Expected result (<code>Content-Type</code> used as handler).
	 * @param request
	 *            {@link MockHttpRequestBuilder}.
	 * @param contentTypes
	 *            <code>Content-Type</code> instances to create handlers.
	 */
	private void doTest(String expectedResult, MockHttpRequestBuilder request, String... contentTypes)
			throws NoAcceptHandlersException {
		AcceptNegotiatorBuilder<String> builder = new AcceptNegotiatorBuilderImpl<>();
		for (String contentType : contentTypes) {
			builder.addHandler(contentType, contentType);
		}
		AcceptNegotiator<String> negotiator = builder.build();
		String result = negotiator.getHandler(request.build());
		assertEquals("Incorrect result", expectedResult, result);
	}

}
