package net.officefloor.tutorial.dynamodbhttpserver;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBScanExpression;

import net.officefloor.nosql.dynamodb.test.DynamoDbExtension;
import net.officefloor.server.http.HttpMethod;
import net.officefloor.server.http.mock.MockHttpServer;
import net.officefloor.test.UsesDockerTest;
import net.officefloor.woof.mock.MockWoofResponse;
import net.officefloor.woof.mock.MockWoofServer;
import net.officefloor.woof.mock.MockWoofServerExtension;

/**
 * Tests the {@link DynamoDBMapper} HTTP server.
 * 
 * @author Daniel Sagenschneider
 */
@UsesDockerTest
public class DynamoDbHttpServerTest {

	// START SNIPPET: tutorial
	@Order(1)
	@RegisterExtension
	public final DynamoDbExtension dynamoDb = new DynamoDbExtension();

	@Order(2)
	@RegisterExtension
	public final MockWoofServerExtension server = new MockWoofServerExtension();

	@Test
	public void ensureCreatePost() throws Exception {

		// Have server create the post
		Post post = new Post(null, "TEST");
		MockWoofResponse response = this.server.send(MockWoofServer.mockJsonRequest(HttpMethod.POST, "/posts", post));
		response.assertResponse(204, "");

		// Ensure post created
		Post[] created = this.dynamoDb.getDynamoDbMapper().scan(Post.class, new DynamoDBScanExpression())
				.toArray(new Post[1]);
		assertEquals(1, created.length, "Should only be one created post");
		assertEquals("TEST", created[0].getMessage(), "Incorrect post");
	}
	// END SNIPPET: tutorial

	@Test
	public void ensureRetrievePost() throws Exception {

		// Obtain the mapper
		DynamoDBMapper mapper = this.dynamoDb.getDynamoDbMapper();

		// Create the post
		Post post = new Post(null, "TEST");
		mapper.save(post);

		// Ensure retrieve the post
		MockWoofResponse response = this.server.send(MockHttpServer.mockRequest("/posts/" + post.getId()));
		response.assertJson(200, post);
	}

	@Test
	public void ensureRetrieveAllPosts() throws Exception {

		// Obtain the mapper
		DynamoDBMapper mapper = this.dynamoDb.getDynamoDbMapper();

		// Create the post
		Post post = new Post();
		mapper.save(post);

		// Ensure retrieve the post
		MockWoofResponse response = this.server.send(MockHttpServer.mockRequest("/posts/"));
		response.assertJson(200, new Post[] { post });
	}

}