package net.officefloor.tutorial.sessionhttpserver;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import net.officefloor.OfficeFloorMain;
import net.officefloor.server.http.mock.MockHttpResponse;
import net.officefloor.server.http.mock.MockHttpServer;
import net.officefloor.tutorial.sessionhttpserver.TemplateLogic.Post;
import net.officefloor.tutorial.sessionhttpserver.TemplateLogic.Posts;
import net.officefloor.woof.mock.MockWoofServerExtension;

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
		assertSame(post, session.getPosts()[0], "Ensure post added");

		// Ensure post provided from template logic
		assertSame(post, logic.getTemplateData(session).getPosts()[0], "Ensure post available");
	}
	// END SNIPPET: pojo

	@RegisterExtension
	public MockWoofServerExtension server = new MockWoofServerExtension();

	public void testSessionPage() throws Exception {

		// Send request for empty session
		MockHttpResponse response = this.server.send(MockHttpServer.mockRequest("/post"));
		assertEquals(200, response.getStatus().getStatusCode(), "Should obtain page");

		// Add a post
		response = this.server.send(MockHttpServer.mockRequest("/post+post?text=TEST"));
		assertEquals(200, response.getStatus().getStatusCode(), "Should add post");
	}

}