package net.officefloor.tutorial.objectifyhttpserver;

import static org.junit.Assert.assertEquals;

import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

import com.googlecode.objectify.Objectify;

import net.officefloor.nosql.objectify.mock.ObjectifyRule;
import net.officefloor.server.http.HttpMethod;
import net.officefloor.test.SkipUtil;
import net.officefloor.test.skip.SkipRule;
import net.officefloor.woof.mock.MockWoofResponse;
import net.officefloor.woof.mock.MockWoofServer;
import net.officefloor.woof.mock.MockWoofServerRule;

/**
 * Tests the {@link Objectify} HTTP server.
 * 
 * @author Daniel Sagenschneider
 */
public class ObjectifyHttpServerJUnit4Test {

	@ClassRule
	public static SkipRule ensureGCloudAvailable = new SkipRule(SkipUtil.isSkipTestsUsingGCloud(),
			"GCloud not available");

	// START SNIPPET: tutorial
	private final ObjectifyRule objectify = new ObjectifyRule();

	private final MockWoofServerRule server = new MockWoofServerRule();

	@Rule
	public final RuleChain ordered = RuleChain.outerRule(this.objectify).around(this.server);

	@Test
	public void ensureCreatePost() throws Exception {

		// Have server create the post
		Post post = new Post(null, "TEST");
		MockWoofResponse response = this.server.send(MockWoofServer.mockJsonRequest(HttpMethod.POST, "/posts", post));
		response.assertResponse(204, "");

		// Ensure post created
		Post created = this.objectify.get(Post.class);
		assertEquals("Incorrect post", "TEST", created.getMessage());
	}
	// END SNIPPET: tutorial

}