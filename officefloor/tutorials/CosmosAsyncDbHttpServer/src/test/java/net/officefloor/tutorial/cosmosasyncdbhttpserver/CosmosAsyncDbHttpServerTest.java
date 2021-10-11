package net.officefloor.tutorial.cosmosasyncdbhttpserver;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.azure.cosmos.CosmosAsyncDatabase;
import com.azure.cosmos.models.PartitionKey;

import net.officefloor.nosql.cosmosdb.CosmosAsyncEntities;
import net.officefloor.nosql.cosmosdb.test.CosmosDbExtension;
import net.officefloor.plugin.clazz.Dependency;
import net.officefloor.server.http.HttpMethod;
import net.officefloor.server.http.mock.MockHttpServer;
import net.officefloor.test.UsesDockerTest;
import net.officefloor.woof.mock.MockWoofResponse;
import net.officefloor.woof.mock.MockWoofServer;
import net.officefloor.woof.mock.MockWoofServerExtension;
import reactor.core.publisher.Mono;

/**
 * Tests the {@link CosmosAsyncDatabase} HTTP server.
 * 
 * @author Daniel Sagenschneider
 */
@UsesDockerTest
public class CosmosAsyncDbHttpServerTest {

	// START SNIPPET: tutorial
	@Order(1)
	@RegisterExtension
	public final CosmosDbExtension cosmosDb = new CosmosDbExtension();

	@Order(2)
	@RegisterExtension
	public final MockWoofServerExtension server = new MockWoofServerExtension();

	private @Dependency CosmosAsyncEntities entities;

	@Test
	public void ensureCreatePost() throws Exception {

		// Have server create the post
		Post post = new Post(null, "TEST");
		MockWoofResponse response = this.server.send(MockWoofServer.mockJsonRequest(HttpMethod.POST, "/posts", post));
		response.assertStatus(200);

		// Ensure post created
		PartitionKey partitionKey = this.entities.createPartitionKey(new Post());
		Mono<List<Post>> monoCreated = this.entities.getContainer(Post.class).readAllItems(partitionKey, Post.class)
				.collectList();
		List<Post> created = monoCreated.block();
		assertEquals(1, created.size(), "Should only be one created post");
		assertEquals("TEST", created.get(0).getMessage(), "Incorrect post");
	}
	// END SNIPPET: tutorial

	@Test
	public void ensureRetrievePost() throws Exception {

		// Create the post
		Post post = new Post(UUID.randomUUID().toString(), "TEST");
		Mono<Post> monoCreated = this.entities.getContainer(Post.class).createItem(post)
				.map(response -> response.getItem());
		Post created = monoCreated.block();

		// Ensure retrieve the post
		MockWoofResponse response = this.server.send(MockHttpServer.mockRequest("/posts/" + created.getId()));
		response.assertJson(200, post);
	}

	@Test
	public void ensureRetrieveAllPosts() throws Exception {

		// Create the post
		Post post = new Post(UUID.randomUUID().toString(), "TEST");
		Mono<Post> monoCreated = this.entities.getContainer(Post.class).createItem(post)
				.map(response -> response.getItem());
		Post created = monoCreated.block();

		// Ensure retrieve the post
		MockWoofResponse response = this.server.send(MockHttpServer.mockRequest("/posts/"));
		response.assertJson(200, new Post[] { created });
	}

}