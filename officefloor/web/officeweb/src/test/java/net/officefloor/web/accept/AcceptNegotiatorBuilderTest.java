/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2017 Daniel Sagenschneider
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
package net.officefloor.web.accept;

import java.util.LinkedList;
import java.util.List;

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
		this.doTest("content/type", this.request(null, "content/type"), "application/not-match", "content/type");
	}

	/**
	 * Ensure can negotiate based on input <code>content-type</code> as accept
	 * any type.
	 */
	public void testMatchContentTypeAsWildcardAccept() throws Exception {
		this.doTest("content/type", this.request("*/*", "content/type"), "application/not-match", "content/type");
	}

	/**
	 * Default to first <code>content-type</code> if any.
	 */
	public void testDefaultFirstAsWildcardAccept() throws Exception {
		this.doTest("application/first", this.request("*/*"), "application/first", "application/second");
	}

	/**
	 * Ensure 406 if no <code>content-type</code> acceptable.
	 */
	public void testNotMatchAccept() throws Exception {
		this.doTest(null, this.request("not/match"), "application/json", "application/xml");
	}

	/**
	 * Ensure matches on specific sub-type first.
	 */
	public void testMatchOnSubTypeFirst() throws Exception {
		this.doTest("application/match", this.request("*/*,application/*,application/match"), "application/not-match",
				"application/match");
	}

	/**
	 * Ensure matches on specific type first.
	 */
	public void testMatchOnTypeFirst() throws Exception {
		this.doTest("application/match", this.request("*/*,application/*"), "not/match", "application/match");
	}

	/**
	 * Ensure matches as per q priority.
	 */
	public void testMatchOnPriority() throws Throwable {
		this.doTest("application/match", this.request("application/not-match;q=0.8,application/match;q=1"),
				"application/not-match", "application/match");
	}

	/**
	 * Ensure matches as per q priority.
	 */
	public void testMatchOnPriorityBeforeWildcards() throws Throwable {
		this.doTest("application/first",
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

			this.doTest(availableContentType,
					this.request(
							"*/*,specific/*,specific/type,parameter/type;one=parameter,multiple/parameters;has=two;parameters"),
					availableContentTypes.toArray(new String[0]));
		}
	}

	/**
	 * Ensure can parse out values with spacing.
	 */
	public void testParsingWithSpacing() throws Exception {
		this.doTest("application/match",
				this.request(
						"\t application/* \t , \t */* \t , application/match \t ; \t q=0.6 ; \t another = value \t ; param"),
				"application/not-match", "application/match");
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