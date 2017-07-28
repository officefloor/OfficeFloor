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
package net.officefloor.tutorial.inherithttpserver;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;

import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;

import junit.framework.TestCase;
import net.officefloor.OfficeFloorMain;
import net.officefloor.plugin.socket.server.http.HttpTestUtil;

/**
 * Ensure appropriately inherit content.
 * 
 * @author Daniel Sagenschneider
 */
public class InheritHttpServerTest extends TestCase {

	/**
	 * {@link CloseableHttpClient}.
	 */
	private final CloseableHttpClient client = HttpTestUtil.createHttpClient();

	@Override
	protected void setUp() throws Exception {
		OfficeFloorMain.open();
	}

	@Override
	protected void tearDown() throws Exception {
		try {
			// Stop the client
			this.client.close();
		} finally {
			// Stop the server
			OfficeFloorMain.close();
		}
	}

	/**
	 * Ensure able to obtain parent template.
	 */
	public void testParent() throws IOException {
		this.doTest("parent.woof", "parent-expected.html");
	}

	/**
	 * Ensure able to obtain child template.
	 */
	public void testChild() throws IOException {
		this.doTest("child.woof", "child-expected.html");
	}

	/**
	 * Ensure able to obtain grand child template.
	 */
	public void testGrandChild() throws IOException {
		this.doTest("grandchild.woof", "grandchild-expected.html");
	}

	/**
	 * Undertakes the {@link HttpRequest} and ensures the responding page is as
	 * expected.
	 * 
	 * @param url
	 *            URL.
	 * @param fileNameContainingExpectedContent
	 *            Name of file containing the expected content.
	 */
	private void doTest(String url, String fileNameContainingExpectedContent) throws IOException {

		// Undertake the request
		HttpResponse response = this.client.execute(new HttpGet("http://localhost:7878/" + url));
		assertEquals("Incorrect response status for URL " + url, 200, response.getStatusLine().getStatusCode());
		String content = EntityUtils.toString(response.getEntity());

		// Obtain the expected content
		InputStream contentInputStream = Thread.currentThread().getContextClassLoader()
				.getResourceAsStream(fileNameContainingExpectedContent);
		assertNotNull("Can not find file " + fileNameContainingExpectedContent, contentInputStream);
		Reader reader = new InputStreamReader(contentInputStream);
		StringWriter expected = new StringWriter();
		for (int character = reader.read(); character != -1; character = reader.read()) {
			expected.append((char) character);
		}
		reader.close();

		// Ensure the context is as expected
		assertEquals("Incorrect content for URL " + url, expected.toString(), content);
	}

}