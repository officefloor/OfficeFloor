/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2011 Daniel Sagenschneider
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
package net.officefloor.tutorial.sessionhttpserver;

import junit.framework.TestCase;
import net.officefloor.plugin.autowire.AutoWireAdministration;
import net.officefloor.plugin.woof.WoofOfficeFloorSource;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

/**
 * Tests the Session HTTP Server.
 * 
 * @author Daniel Sagenschneider
 */
public class SessionHttpServerTest extends TestCase {

	// START SNIPPET: pojo
	public void testTemplateLogic() {

		Posts session = new Posts();

		TemplateLogic logic = new TemplateLogic();

		// Add post to session via template logic
		Post post = new Post();
		post.setText("Test post");
		logic.post(post, session);
		assertEquals("Ensure post added", post, session.getPosts()[0]);

		// Ensure post provided from template logic
		assertEquals("Ensure post available", post, logic.getPosts(session)[0]);
	}
	// END SNIPPET: pojo

	private final HttpClient client = new DefaultHttpClient();

	public void testSessionPage() throws Exception {

		// Start server
		WoofOfficeFloorSource.main();

		// Send request for empty session
		this.doRequest("http://localhost:7878/post");

		// Add a post
		this.doRequest("http://localhost:7878/post.links-post.task?text=TEST");
	}

	private void doRequest(String url) throws Exception {
		HttpResponse response = this.client.execute(new HttpGet(url));
		assertEquals("Request should be successful", 200, response
				.getStatusLine().getStatusCode());
		response.getEntity().writeTo(System.out);
	}

	@Override
	protected void tearDown() throws Exception {
		// Stop server
		AutoWireAdministration.closeAllOfficeFloors();
	}

}