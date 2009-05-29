/*
 * Office Floor, Application Server
 * Copyright (C) 2005-2009 Daniel Sagenschneider
 *
 * This program is free software; you can redistribute it and/or modify it under the terms
 * of the GNU General Public License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program;
 * if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston,
 * MA 02111-1307 USA
 */
package net.officefloor.plugin.work.http.html.form;

import java.net.URISyntaxException;
import java.util.List;

import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.work.http.html.form.HtmlForm;
import net.officefloor.plugin.work.http.html.form.HtmlFormParameter;

/**
 * Tests the {@link HtmlForm}.
 * 
 * @author Daniel Sagenschneider
 */
public class HtmlFormTest extends OfficeFrameTestCase {

	/**
	 * Ensure able to parse GET with no parameters.
	 */
	public void testGetNoParameters() throws Exception {
		this.doTest("/path", null, "/path", null);
	}

	/**
	 * Ensures able to parse GET with one parameter.
	 */
	public void testGetWithOneParameter() throws Exception {
		this.doTest("/path?name=value", null, "/path", null, "name", "value");
	}

	/**
	 * Ensures able to parse GET with many parameters.
	 */
	public void testGetWithManyParameters() throws Exception {
		this.doTest("/path?one=first&two=second&three=third", null, "/path",
				null, "one", "first", "two", "second", "three", "third");
	}

	/**
	 * Ensures able to parse GET with only a segment.
	 */
	public void testGetWithFragment() throws Exception {
		this.doTest("/path#fragment", null, "/path", "fragment");
	}

	/**
	 * Ensures able to parse GET with parameters and fragments.
	 */
	public void testGetWithParametersAndFragments() throws Exception {
		this.doTest("/path?one=first&two=second&three=third#fragment", null,
				"/path", "fragment", "one", "first", "two", "second", "three",
				"third");
	}

	/**
	 * Ensure able to parse POST with no parameters.
	 */
	public void testPostNoParameters() throws Exception {
		this.doTest("/path", "", "/path", null);
	}

	/**
	 * Ensures able to parse POST with one body parameter.
	 */
	public void testPostWithOneBodyParameter() throws Exception {
		this.doTest("/path", "name=value", "/path", null, "name", "value");
	}

	/**
	 * Ensures able to parse POST with many body parameters.
	 */
	public void testGetWithManyBodyParameters() throws Exception {
		this.doTest("/path", "one=first&two=second&three=third", "/path", null,
				"one", "first", "two", "second", "three", "third");
	}

	/**
	 * Ensures able to parse POST with only a segment.
	 */
	public void testPostWithFragment() throws Exception {
		this.doTest("/path#fragment", "", "/path", "fragment");
	}

	/**
	 * Ensures able to parse POST with URI and body parameters.
	 */
	public void testPostWithUriAndBodyParameters() throws Exception {
		this.doTest("/path?one=first&two=second", "three=third&four=fourth",
				"/path", null, "one", "first", "two", "second", "three",
				"third", "four", "fourth");
	}

	/**
	 * Ensures able to parse POST with URI and body parameters and fragments.
	 */
	public void testPostWithUriAndBodyParametersAndFragments() throws Exception {
		this.doTest("/path?one=first&two=second#fragment",
				"three=third&four=fourth", "/path", "fragment", "one", "first",
				"two", "second", "three", "third", "four", "fourth");
	}

	/**
	 * Does the test.
	 * 
	 * @param uriPath
	 *            HTTP URI path.
	 * @param postBody
	 *            Body of a post method. May be <code>null</code> to indicate
	 *            GET method.
	 * @param path
	 *            Expected path.
	 * @param fragment
	 *            Expected fragment.
	 * @param parameterNameValues
	 *            Expected parameter name/value pairs.
	 */
	public void doTest(String uriPath, String postBody, String path,
			String fragment, String... parameterNameValues)
			throws URISyntaxException {

		// Create the HTTP path
		HtmlForm httpPath = (postBody == null ? new HtmlForm(uriPath)
				: new HtmlForm(uriPath, postBody));

		// Validate the expected path
		assertEquals("Incorrect path", path, httpPath.getPath());
		assertEquals("Incorrect fragment", fragment, httpPath.getFragment());

		// Verify the parameters
		List<HtmlFormParameter> parameters = httpPath.getParameters();
		assertEquals("Incorrect number of parameters",
				(parameterNameValues.length / 2), parameters.size());
		for (int i = 0; i < parameterNameValues.length; i += 2) {
			String paramName = parameterNameValues[i];
			String paramValue = parameterNameValues[i + 1];

			int paramIndex = i / 2;
			HtmlFormParameter parameter = parameters.get(paramIndex);

			// Ensure details of parameter correct
			assertEquals("Incorrect name for parameter " + paramIndex,
					paramName, parameter.getName());
			assertEquals("Incorrect value for parameter " + paramIndex,
					paramValue, parameter.getValue());
		}
	}
}
