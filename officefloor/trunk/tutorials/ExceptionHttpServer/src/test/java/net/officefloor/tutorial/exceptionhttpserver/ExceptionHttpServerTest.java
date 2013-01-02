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
package net.officefloor.tutorial.exceptionhttpserver;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import junit.framework.TestCase;
import net.officefloor.plugin.woof.WoofOfficeFloorSource;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

/**
 * Ensure appropriately handling exception.
 * 
 * @author Daniel Sagenschneider
 */
public class ExceptionHttpServerTest extends TestCase {

	// START SNIPPET: handle
	public void testExceptionHandling() throws Exception {

		// Override stderr
		ByteArrayOutputStream error = new ByteArrayOutputStream();
		System.setErr(new PrintStream(error, true));

		// Start server
		WoofOfficeFloorSource.start();

		// Submit to trigger the exception
		this.client.execute(new HttpGet(
				"http://localhost:7878/template-submit.woof"));

		// Ensure handling by logging the failure
		String log = new String(error.toByteArray()).trim();
		assertEquals("Should log error", "Test", log);
	}
	// END SNIPPET: handle

	private PrintStream stderr;

	private HttpClient client = new DefaultHttpClient();

	@Override
	protected void setUp() throws Exception {
		// Maintain stderr to reinstate
		this.stderr = System.err;
	}

	@Override
	protected void tearDown() throws Exception {

		// Reinstate stderr
		System.setErr(this.stderr);

		// Shutdown client
		this.client.getConnectionManager().shutdown();

		// Stop server
		WoofOfficeFloorSource.stop();
	}

}