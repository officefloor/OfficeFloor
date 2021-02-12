package net.officefloor.tutorial.awssamhttpserver;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import net.officefloor.nosql.dynamodb.test.DynamoDbExtension;
import net.officefloor.server.http.HttpMethod;
import net.officefloor.test.UsesDockerTest;
import net.officefloor.tutorial.awssamhttpserver.SamLogic.Post;
import net.officefloor.woof.mock.MockWoofResponse;
import net.officefloor.woof.mock.MockWoofServer;
import net.officefloor.woof.mock.MockWoofServerExtension;

/**
 * Tests the AWS SAM HTTP Server.
 * 
 * @author Daniel Sagenschneider
 */
@UsesDockerTest
public class AwsSamHttpServerTest {

	// START SNIPPET: tutorial
	public static final @Order(1) @RegisterExtension DynamoDbExtension dynamo = new DynamoDbExtension();

	public static final @Order(2) @RegisterExtension MockWoofServerExtension server = new MockWoofServerExtension();

	@Test
	public void createPost() {

		// Create the entity
		MockWoofResponse response = server
				.send(MockWoofServer.mockJsonRequest(HttpMethod.POST, "/post", new Post("TEST")));
		PostEntity entity = response.getJson(200, PostEntity.class);

		// Ensure in store
		PostEntity stored = dynamo.getDynamoDbMapper().load(PostEntity.class, entity.getId());
		assertNotNull(stored, "Should find entity in DynamoDB " + entity.getId());
		assertEquals("TEST", stored.getMessage(), "Incorrent entity");
	}
	// END SNIPPET: tutorial

	@Test
	public void getPost() {

		// Create the entity
		PostEntity entity = new PostEntity(null, "TEST");
		dynamo.getDynamoDbMapper().save(entity);

		// Obtain the entity
		MockWoofResponse response = server.send(MockWoofServer.mockRequest("/post/" + entity.getId()));
		PostEntity retrieved = response.getJson(200, PostEntity.class);
		assertEquals("TEST", retrieved.getMessage(), "Incorrect entity");
	}

}