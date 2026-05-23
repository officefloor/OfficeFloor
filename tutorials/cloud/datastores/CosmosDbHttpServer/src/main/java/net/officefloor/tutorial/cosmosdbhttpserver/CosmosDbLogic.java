package net.officefloor.tutorial.cosmosdbhttpserver;

import java.util.UUID;

import com.azure.cosmos.CosmosContainer;
import com.azure.cosmos.CosmosDatabase;
import com.azure.cosmos.models.PartitionKey;

import net.officefloor.nosql.cosmosdb.CosmosEntities;
import net.officefloor.web.HttpPathParameter;
import net.officefloor.web.ObjectResponse;

/**
 * {@link CosmosDatabase} logic.
 * 
 * @author Daniel Sagenschneider
 */
// START SNIPPET: tutorial
public class CosmosDbLogic {

	public void savePost(Post post, CosmosEntities entities, ObjectResponse<Post> response) {
		CosmosContainer container = entities.getContainer(Post.class);
		Post created = container.createItem(new Post(UUID.randomUUID().toString(), post.getMessage())).getItem();
		response.send(created);
	}

	public void retrievePost(@HttpPathParameter("id") String identifier, CosmosEntities entities,
			ObjectResponse<Post> response) {
		CosmosContainer container = entities.getContainer(Post.class);
		PartitionKey partitionKey = entities.createPartitionKey(new Post());
		Post post = container.readItem(identifier, partitionKey, Post.class).getItem();
		response.send(post);
	}

	public void retrieveAllPosts(CosmosEntities entities, ObjectResponse<Post[]> response) {
		CosmosContainer container = entities.getContainer(Post.class);
		PartitionKey partitionKey = entities.createPartitionKey(new Post());
		Post[] posts = container.readAllItems(partitionKey, Post.class).stream().toArray(Post[]::new);
		response.send(posts);
	}
}
// END SNIPPET: tutorial