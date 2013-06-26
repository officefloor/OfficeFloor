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
package net.officefloor.tutorial.woofservletjspintegration;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import junit.framework.TestCase;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

/**
 * Ensure obtains the response from integration of WoOF with JSP.
 * 
 * @author Daniel Sagenschneider
 */
// START SNIPPET: tutorial
public class JspIT extends TestCase {

	public void testTemplate() throws IOException {
		this.assertRequest(
				"/template.woof",
				"<html> <body> REQUEST SESSION APPLICATION <a href='/template-link.woof'>JSP</a> </body> </html>");
	}

	public void testLinkToJsp() throws IOException {
		this.assertRequest(
				"/template-link.woof",
				"<html> <body> REQUEST SESSION application <a href='/template.woof'>WoOF</a> </body> </html>");
	}

	private void assertRequest(String uri, String expectedContent)
			throws IOException {

		// Undertake the request
		HttpClient client = new DefaultHttpClient();
		try {
			HttpResponse response = client.execute(new HttpGet(
					"http://localhost:18180" + uri));
			assertEquals("Request should be successful: " + uri, 200, response
					.getStatusLine().getStatusCode());

			// Ensure content is as expected (removing additional white spacing)
			ByteArrayOutputStream buffer = new ByteArrayOutputStream();
			response.getEntity().writeTo(buffer);
			String html = buffer.toString();
			html = html.replace('\t', ' ');
			html = html.replace('\n', ' ');
			html = html.replace('\r', ' ');
			while (html.contains("  ")) {
				html = html.replace("  ", " ");
			}
			html = html.trim();

			// Ensure response is as expected
			assertEquals("Incorrect response entity", expectedContent, html);

		} finally {
			// Ensure stop client
			client.getConnectionManager().shutdown();
		}
	}
}
// END SNIPPET: tutorial