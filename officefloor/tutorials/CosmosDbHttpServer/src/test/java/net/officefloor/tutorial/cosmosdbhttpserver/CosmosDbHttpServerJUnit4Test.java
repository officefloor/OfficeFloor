package net.officefloor.tutorial.cosmosdbhttpserver;

import static org.junit.Assert.assertEquals;

import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

import com.azure.cosmos.CosmosContainer;
import com.azure.cosmos.CosmosDatabase;
import com.azure.cosmos.models.PartitionKey;

import net.officefloor.nosql.cosmosdb.CosmosEntities;
import net.officefloor.nosql.cosmosdb.test.CosmosDbRule;
import net.officefloor.plugin.clazz.Dependency;
import net.officefloor.server.http.HttpMethod;
import net.officefloor.test.SkipUtil;
import net.officefloor.test.skip.SkipRule;
import net.officefloor.woof.mock.MockWoofResponse;
import net.officefloor.woof.mock.MockWoofServer;
import net.officefloor.woof.mock.MockWoofServerRule;

/**
 * Tests the {@link CosmosDatabase} HTTP server.
 * 
 * @author Daniel Sagenschneider
 */
public class CosmosDbHttpServerJUnit4Test {

	@ClassRule
	public static SkipRule ensureDockerAvailable = new SkipRule(SkipUtil.isSkipTestsUsingDocker(),
			"Docker not available");

	// START SNIPPET: tutorial
	private final CosmosDbRule dynamoDb = new CosmosDbRule();

	private final MockWoofServerRule server = new MockWoofServerRule(this);

	@Rule
	public final RuleChain ordered = RuleChain.outerRule(this.dynamoDb).around(this.server);

	private @Dependency CosmosEntities entities;

	@Test
	public void ensureCreatePost() throws Exception {

		// Have server create the post
		Post post = new Post(null, "TEST");
		MockWoofResponse response = this.server.send(MockWoofServer.mockJsonRequest(HttpMethod.POST, "/posts", post));
		response.assertResponse(204, "");

		// Ensure post created
		CosmosContainer container = this.entities.getContainer(Post.class);
		PartitionKey partitionKey = this.entities.createPartitionKey(post);
		Post[] created = container.readAllItems(partitionKey, Post.class).stream().toArray(Post[]::new);
		assertEquals("Should only be one created post", 1, created.length);
		assertEquals("Incorrect post", "TEST", created[0].getMessage());
	}
	// END SNIPPET: tutorial

}