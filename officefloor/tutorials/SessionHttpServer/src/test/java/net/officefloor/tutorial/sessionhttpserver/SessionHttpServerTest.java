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