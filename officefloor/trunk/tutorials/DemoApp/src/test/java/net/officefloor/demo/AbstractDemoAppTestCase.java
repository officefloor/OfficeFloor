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
package net.officefloor.demo;

import java.io.ByteArrayOutputStream;
import java.io.File;

import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.socket.server.http.HttpTestUtil;
import net.officefloor.plugin.woof.WoofOfficeFloorSource;

import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.impl.client.CloseableHttpClient;

/**
 * Provides abstract tests to validate each means of running the application.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class AbstractDemoAppTestCase extends OfficeFrameTestCase {

	/**
	 * Starts the server.
	 * 
	 * @return Port the server is running on.
	 */
	protected abstract int startServer() throws Exception;

	/**
	 * Stops the server.
	 */
	protected abstract void stopServer() throws Exception;

	/**
	 * <p>
	 * Finds the <code>webapp</code> directory.
	 * <p>
	 * It first attempts to look for a target directory containing the compiled
	 * GWT content and if not available falls back to the
	 * <code>src/main/webapp</code>.
	 * 
	 * @return {@link File} location of the <code>webapp</code> directory.
	 */
	public static File findWebApDirectory() {

		// Find target directory (as has incrementing version)
		File targetDir = new File(".", "target");
		File webAppDir = null;
		if (targetDir.exists()) {
			for (File dir : targetDir.listFiles()) {

				// Ignore if not directory (ignores packaged content)
				if (!(dir.isDirectory())) {
					continue;
				}

				// Determine if webap directory
				if (dir.getName().startsWith("DemoApp-")) {
					// Found the webapp directory
					webAppDir = dir;
				}
			}
		}

		// Determine if target webapp directory is suitable to use
		if ((webAppDir != null)
				&& (webAppDir.exists())
				&& (new File(webAppDir, WoofOfficeFloorSource.WEBXML_FILE_PATH)
						.exists())) {
			// Use target compiled directory
			return webAppDir;
		}

		// Use source webapp directory
		System.err.println("WARNING: using "
				+ WoofOfficeFloorSource.WEBAPP_PATH
				+ " so GWT functionality will NOT be available");
		return new File(".", WoofOfficeFloorSource.WEBAPP_PATH);
	}

	/**
	 * Server port.
	 */
	private int serverPort;

	/**
	 * {@link CloseableHttpClient}.
	 */
	protected final CloseableHttpClient client = HttpTestUtil.createHttpClient();

	/**
	 * Ensure able to obtain resource file.
	 */
	public void testResourceFile() {
		this.doRequest("/css/Stocks.css");
	}

	/**
	 * Ensure WoOF services for URI (rather than DefaultServlet).
	 */
	public void testWoOF() {
		String response = this.doRequest("/chat.woof");
		assertTrue("Should be serviced by WoOF",
				response.contains("<h2>Chat Example</h2>"));
	}

	@Override
	protected void setUp() throws Exception {
		// Start the server
		this.serverPort = this.startServer();
	}

	@Override
	protected void tearDown() throws Exception {
		try {
			// Stop the client
			this.client.close();

		} finally {
			// Stop the server
			this.stopServer();
		}
	}

	/**
	 * Undertakes the {@link HttpRequest}.
	 * 
	 * @param uri
	 *            URI.
	 * @return {@link HttpRequest}.
	 */
	protected String doRequest(String uri) {

		HttpResponse response = null;
		try {
			// Try a few times (server may take a moment to come up)
			CONNECTING: for (int i = 0; i < 3; i++) {
				try {
					// Ensure able to obtain css file (via war plugin)
					response = this.client.execute(new HttpGet(
							"http://localhost:" + this.serverPort + uri));
					break CONNECTING; // connected
				} catch (HttpHostConnectException ex) {
					// Try again, after a moment
					Thread.sleep(100);
					continue;
				}
			}
		} catch (Exception ex) {
			throw fail(ex);
		}

		// Validate successfully obtained response
		assertNotNull("Did not succeed in contacting application", response);
		assertEquals("Should be successful", 200, response.getStatusLine()
				.getStatusCode());

		// Return the response
		try {
			ByteArrayOutputStream buffer = new ByteArrayOutputStream();
			response.getEntity().writeTo(buffer);
			return new String(buffer.toByteArray());
		} catch (Exception ex) {
			throw fail(ex);
		}
	}

}