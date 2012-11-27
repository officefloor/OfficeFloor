/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2012 Daniel Sagenschneider
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

package net.officefloor.tutorial.woofservletwebapplication;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.junit.Ignore;

import junit.framework.TestCase;

/**
 * Tests the {@link ExampleFilter}.
 * 
 * @author Daniel Sagenschneider
 */
// START SNIPPET: example
@Ignore("TODO provide link suffix functionality to HttpTemplateSectionSource for Servlet mapping")
public class ExampleIT extends TestCase {

	private final HttpClient client = new DefaultHttpClient();

	public void testStaticResource() throws IOException {
		this.assertRequest("/resource.html",
				"<html><body>RESOURCE</body></html>");
	}

	public void testHttpTemplateAndLinkToJsp() throws IOException {
		this.assertRequest("/template",
				"<html><body><a href=\"/template-LINK\">TEMPLATE</a></body></html>");
	}

	public void testLinkToJsp() throws IOException {
		this.assertRequest("/template-LINK",
				"<html><body>Linked to JSP</body></html>");
	}

	public void testClass() throws IOException {
		this.assertRequest("/class", "<html><body>CLASS</body></html>");
	}

	private void assertRequest(String uri, String expectedContent)
			throws IOException {

		// Undertake the request
		HttpGet request = new HttpGet("http://localhost:18080" + uri);
		HttpResponse response = this.client.execute(request);
		assertEquals("Request should be successful: " + uri, 200, response
				.getStatusLine().getStatusCode());

		// Ensure content is as expected
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		response.getEntity().writeTo(buffer);
		assertEquals("Incorrect response entity", expectedContent,
				buffer.toString());
	}

}
// END SNIPPET: example