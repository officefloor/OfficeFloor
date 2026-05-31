package net.officefloor.tutorial.azurewebappshttpserver;

import java.util.UUID;

import com.azure.cosmos.CosmosContainer;
import com.azure.cosmos.models.PartitionKey;

import net.officefloor.nosql.cosmosdb.CosmosAsyncEntities;
import net.officefloor.nosql.cosmosdb.CosmosEntities;
import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.web.HttpPathParameter;
import net.officefloor.web.ObjectResponse;
import reactor.core.publisher.Flux;

/**
 * Azure logic.
 * 
 * @author Daniel Sagenschneider
 */
public class AzureLogic {

	/** Saves a {@link Post}. */
	public void savePost(Post post, CosmosEntities entities, ObjectResponse<Post> response) {
		CosmosContainer container = entities.getContainer(Post.class);
		Post created = container.createItem(new Post(UUID.randomUUID().toString(), post.getMessage())).getItem();
		response.send(created);
	}

	/** Retrieves a {@link Post} by identifier. */
	public void retrievePost(@HttpPathParameter("id") String identifier, CosmosEntities entities,
			ObjectResponse<Post> response) {
		CosmosContainer container = entities.getContainer(Post.class);
		PartitionKey partitionKey = entities.createPartitionKey(new Post());
		Post post = container.readItem(identifier, partitionKey, Post.class).getItem();
		response.send(post);
	}

	/** Retrieves all {@link Post} instances asynchronously. */
	public Flux<Post> retrieveAllPosts(CosmosAsyncEntities entities, ObjectResponse<Post[]> response) {
		PartitionKey partitionKey = entities.createPartitionKey(new Post());
		return entities.getContainer(Post.class).readAllItems(partitionKey, Post.class);
	}

	/** Sends the {@link Post} array response. */
	public void sendPosts(@Parameter Post[] posts, ObjectResponse<Post[]> response) {
		response.send(posts);
	}

}
