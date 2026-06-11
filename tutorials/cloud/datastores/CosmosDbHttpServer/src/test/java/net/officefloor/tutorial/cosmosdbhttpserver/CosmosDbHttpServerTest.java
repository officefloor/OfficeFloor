package net.officefloor.tutorial.cosmosdbhttpserver;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.UUID;

import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.azure.cosmos.CosmosDatabase;
import com.azure.cosmos.models.PartitionKey;

import net.officefloor.nosql.cosmosdb.CosmosEntities;
import net.officefloor.nosql.cosmosdb.test.CosmosDbExtension;
import net.officefloor.plugin.clazz.Dependency;
import net.officefloor.server.http.HttpMethod;
import net.officefloor.server.http.mock.MockHttpServer;
import net.officefloor.test.UsesDockerTest;
import net.officefloor.woof.mock.MockWoofResponse;
import net.officefloor.woof.mock.MockWoofServer;
import net.officefloor.woof.mock.MockWoofServerExtension;

/**
 * Tests the {@link CosmosDatabase} HTTP server.
 * 
 * @author Daniel Sagenschneider
 */
@UsesDockerTest
public class CosmosDbHttpServerTest {

	// START SNIPPET: tutorial
	@Order(1)
	@RegisterExtension
	public final CosmosDbExtension cosmosDb = new CosmosDbExtension();

	@Order(2)
	@RegisterExtension
	public final MockWoofServerExtension server = new MockWoofServerExtension();

	private @Dependency CosmosEntities entities;

	@Test
	public void ensureCreatePost() throws Exception {

		// Have server create the post
		Post post = new Post(null, "TEST");
		MockWoofResponse response = this.server.send(MockWoofServer.mockJsonRequest(HttpMethod.POST, "/posts", post));
		response.assertStatus(200);

		// Ensure post created
		PartitionKey partitionKey = this.entities.createPartitionKey(new Post());
		Post[] created = this.entities.getContainer(Post.class).readAllItems(partitionKey, Post.class).stream()
				.toArray(Post[]::new);
		assertEquals(1, created.length, "Should only be one created post");
		assertEquals("TEST", created[0].getMessage(), "Incorrect post");
	}
	// END SNIPPET: tutorial

	@Test
	public void ensureRetrievePost() throws Exception {

		// Create the post
		Post post = new Post(UUID.randomUUID().toString(), "TEST");
		Post created = this.entities.getContainer(Post.class).createItem(post).getItem();

		// Ensure retrieve the post
		MockWoofResponse response = this.server.send(MockHttpServer.mockRequest("/posts/" + created.getId()));
		response.assertJson(200, post);
	}

	@Test
	public void ensureRetrieveAllPosts() throws Exception {

		// Create the post
		Post post = new Post(UUID.randomUUID().toString(), "TEST");
		Post created = this.entities.getContainer(Post.class).createItem(post).getItem();

		// Ensure retrieve the post
		MockWoofResponse response = this.server.send(MockHttpServer.mockRequest("/posts/"));
		response.assertJson(200, new Post[] { created });
	}

}