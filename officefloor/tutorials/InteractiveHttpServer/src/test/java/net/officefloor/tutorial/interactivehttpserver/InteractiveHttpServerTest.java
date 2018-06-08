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
package net.officefloor.tutorial.interactivehttpserver;

import static org.junit.Assert.assertEquals;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.junit.Rule;
import org.junit.Test;

import net.officefloor.OfficeFloorMain;
import net.officefloor.server.http.HttpClientRule;
import net.officefloor.test.OfficeFloorRule;
import net.officefloor.woof.mock.MockWoofServerRule;

/**
 * Tests the {@link PageFlowHttpServer}.
 * 
 * @author Daniel Sagenschneider
 */
public class InteractiveHttpServerTest {

	/**
	 * Run application.
	 */
	public static void main(String[] args) throws Exception {
		OfficeFloorMain.main(args);
	}

	// START SNIPPET: test
	/**
	 * See {@link MockWoofServerRule} for faster tests that avoid sending requests
	 * over sockets. However, for this tutorial we are demonstrating running the
	 * full application for testing.
	 */
	@Rule
	public OfficeFloorRule officeFloor = new OfficeFloorRule();

	@Rule
	public HttpClientRule client = new HttpClientRule();

	@Test
	public void pageInteraction() throws Exception {

		// Request the initial page
		HttpResponse response = this.client.execute(new HttpGet(this.client.url("/example")));
		assertEquals("Request should be successful", 200, response.getStatusLine().getStatusCode());

		// Send form submission
		response = this.client
				.execute(new HttpPost(this.client.url("/example+handleSubmission?name=Daniel&description=founder")));
		assertEquals("Should submit successfully", 200, response.getStatusLine().getStatusCode());
	}
	// END SNIPPET: test

}