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
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;

import junit.framework.TestCase;

/**
 * Validates that the WoOF is running.
 * 
 * @author Daniel Sagenschneider
 */
public class RunWoofGoalTest extends TestCase {

	/**
	 * Ensure able to obtain class path resource.
	 */
	public void testClassPathResource() throws Exception {
		this.doTest("/classpath.html",
				"<html><body>CLASS PATH RESOURCE</body></html>");
	}

	/**
	 * Ensure able to obtain web app resource.
	 */
	public void testWebappResource() throws Exception {
		this.doTest("/webapp.html",
				"<html><body>WEB APP RESOURCE</body></html>");
	}

	/**
	 * Ensure appropriately handles WoOF resources.
	 */
	public void testWoofResource() throws Exception {
		this.doTest("/woof.woof", "<html><body>WOOF RESOURCE</body></html>");
	}

	/**
	 * Ensure appropriate handles WoOF template. Also tests using property files
	 * for production environment.
	 */
	public void testWoofTemplate() throws Exception {
		this.doTest("/template.woof", "<html><body>TEMPLATE</body></html>");
	}

	/**
	 * Undertakes the tests.
	 * 
	 * @param uri
	 *            URI.
	 * @param expectedResponse
	 *            Expected response.
	 */
	private void doTest(String uri, String expectedResponse) throws Exception {

		// Connect
		URL url = new URL("http", "localhost", 7878, uri);
		InputStream input = url.openConnection().getInputStream();

		// Ensure provides expected response
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		for (int character = input.read(); character != -1; character = input
				.read()) {
			buffer.write(character);
		}
		assertEquals("Incorrect content", expectedResponse,
				new String(buffer.toByteArray()));
	}

}