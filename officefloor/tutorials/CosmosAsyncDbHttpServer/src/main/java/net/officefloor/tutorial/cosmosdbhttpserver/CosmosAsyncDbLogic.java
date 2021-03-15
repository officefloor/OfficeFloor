package net.officefloor.tutorial.cosmosdbhttpserver;

import java.util.UUID;

import com.azure.cosmos.CosmosDatabase;
import com.azure.cosmos.models.PartitionKey;

import net.officefloor.nosql.cosmosdb.CosmosAsyncEntities;
import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.web.HttpPathParameter;
import net.officefloor.web.ObjectResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * {@link CosmosDatabase} logic.
 * 
 * @author Daniel Sagenschneider
 */
// START SNIPPET: tutorial
public class CosmosAsyncDbLogic {

	public Mono<Post> savePost(Post post, CosmosAsyncEntities entities) {
		return entities.getContainer(Post.class).createItem(new Post(UUID.randomUUID().toString(), post.getMessage()))
				.map(response -> response.getItem());
	}

	public Mono<Post> retrievePost(@HttpPathParameter("id") String identifier, CosmosAsyncEntities entities) {
		PartitionKey partitionKey = entities.createPartitionKey(new Post());
		return entities.getContainer(Post.class).readItem(identifier, partitionKey, Post.class)
				.map(response -> response.getItem());
	}

	public void sendPost(@Parameter Post post, ObjectResponse<Post> response) {
		response.send(post);
	}

	public Flux<Post> retrieveAllPosts(CosmosAsyncEntities entities, ObjectResponse<Post[]> response) {
		PartitionKey partitionKey = entities.createPartitionKey(new Post());
		return entities.getContainer(Post.class).readAllItems(partitionKey, Post.class);
	}

	public void sendPosts(@Parameter Post[] posts, ObjectResponse<Post[]> response) {
		response.send(posts);
	}
}
// END SNIPPET: tutorial