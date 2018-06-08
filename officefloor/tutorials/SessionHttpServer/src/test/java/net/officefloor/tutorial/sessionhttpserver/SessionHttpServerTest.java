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
package net.officefloor.tutorial.sessionhttpserver;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import org.junit.Rule;
import org.junit.Test;

import net.officefloor.OfficeFloorMain;
import net.officefloor.server.http.mock.MockHttpResponse;
import net.officefloor.server.http.mock.MockHttpServer;
import net.officefloor.tutorial.sessionhttpserver.TemplateLogic.Post;
import net.officefloor.tutorial.sessionhttpserver.TemplateLogic.Posts;
import net.officefloor.woof.mock.MockWoofServerRule;

/**
 * Tests the Session HTTP Server.
 * 
 * @author Daniel Sagenschneider
 */
public class SessionHttpServerTest {

	/**
	 * Run application.
	 */
	public static void main(String[] args) throws Exception {
		OfficeFloorMain.main(args);
	}

	// START SNIPPET: pojo
	@Test
	public void testTemplateLogic() {

		Posts session = new Posts();

		TemplateLogic logic = new TemplateLogic();

		// Add post to session via template logic
		Post post = new Post();
		post.setText("Test post");
		logic.post(post, session);
		assertSame("Ensure post added", post, session.getPosts()[0]);

		// Ensure post provided from template logic
		assertSame("Ensure post available", post, logic.getTemplateData(session).getPosts()[0]);
	}
	// END SNIPPET: pojo

	@Rule
	public MockWoofServerRule server = new MockWoofServerRule();

	public void testSessionPage() throws Exception {

		// Send request for empty session
		MockHttpResponse response = this.server.send(MockHttpServer.mockRequest("/post"));
		assertEquals("Should obtain page", 200, response.getStatus().getStatusCode());

		// Add a post
		response = this.server.send(MockHttpServer.mockRequest("/post+post?text=TEST"));
		assertEquals("Should add post", 200, response.getStatus().getStatusCode());
	}

}