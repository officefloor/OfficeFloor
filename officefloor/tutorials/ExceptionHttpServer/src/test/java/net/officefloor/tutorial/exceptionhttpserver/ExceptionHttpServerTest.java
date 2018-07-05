/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package net.officefloor.tutorial.exceptionhttpserver;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import net.officefloor.OfficeFloorMain;
import net.officefloor.server.http.HttpClientRule;
import net.officefloor.test.OfficeFloorRule;

/**
 * Ensure appropriately handling exception.
 * 
 * @author Daniel Sagenschneider
 */
public class ExceptionHttpServerTest {

	/**
	 * Run application.
	 */
	public static void main(String[] args) throws Exception {
		OfficeFloorMain.main(args);
	}

	// START SNIPPET: handle
	@Rule
	public OfficeFloorRule officeFloor = new OfficeFloorRule();

	@Rule
	public HttpClientRule client = new HttpClientRule();

	private PrintStream stderr;

	@Before
	public void setup() {
		// Maintain stderr to reinstate
		this.stderr = System.err;
	}

	@Test
	public void ensureHandleException() throws Exception {

		// Override stderr
		ByteArrayOutputStream error = new ByteArrayOutputStream();
		System.setErr(new PrintStream(error, true));

		// Submit to trigger the exception
		HttpResponse response = this.client.execute(new HttpGet(this.client.url("/template+submit")));
		assertEquals("Should be successful", 200, response.getStatusLine().getStatusCode());

		// Ensure handling by logging the failure
		String log = new String(error.toByteArray()).trim();
		assertEquals("Should log error", "Test", log);
	}

	@After
	public void reinstate() {
		// Reinstate stderr
		System.setErr(this.stderr);
	}
	// END SNIPPET: handle

}