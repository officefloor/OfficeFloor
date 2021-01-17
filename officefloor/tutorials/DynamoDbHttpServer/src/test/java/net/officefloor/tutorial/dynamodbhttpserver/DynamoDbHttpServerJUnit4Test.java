package net.officefloor.tutorial.dynamodbhttpserver;

import static org.junit.Assert.assertEquals;

import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBScanExpression;

import net.officefloor.nosql.dynamodb.test.DynamoDbRule;
import net.officefloor.server.http.HttpMethod;
import net.officefloor.test.SkipUtil;
import net.officefloor.test.skip.SkipRule;
import net.officefloor.woof.mock.MockWoofResponse;
import net.officefloor.woof.mock.MockWoofServer;
import net.officefloor.woof.mock.MockWoofServerRule;

/**
 * Tests the {@link DynamoDBMapper} HTTP server.
 * 
 * @author Daniel Sagenschneider
 */
public class DynamoDbHttpServerJUnit4Test {

	@ClassRule
	public static SkipRule ensureDockerAvailable = new SkipRule(SkipUtil.isSkipTestsUsingDocker(),
			"Docker not available");

	// START SNIPPET: tutorial
	private final DynamoDbRule dynamoDb = new DynamoDbRule();

	private final MockWoofServerRule server = new MockWoofServerRule();

	@Rule
	public final RuleChain ordered = RuleChain.outerRule(this.dynamoDb).around(this.server);

	@Test
	public void ensureCreatePost() throws Exception {

		// Have server create the post
		Post post = new Post(null, "TEST");
		MockWoofResponse response = this.server.send(MockWoofServer.mockJsonRequest(HttpMethod.POST, "/posts", post));
		response.assertResponse(204, "");

		// Ensure post created
		Post[] created = this.dynamoDb.getDynamoDbMapper().scan(Post.class, new DynamoDBScanExpression())
				.toArray(new Post[1]);
		assertEquals("Should only be one created post", 1, created.length);
		assertEquals("Incorrect post", "TEST", created[0].getMessage());
	}
	// END SNIPPET: tutorial

}