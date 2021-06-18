package net.officefloor.tutorial.awssamhttpserver;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.fasterxml.jackson.databind.ObjectMapper;

import net.officefloor.nosql.dynamodb.test.AbstractDynamoDbConnectJunit.Configuration;
import net.officefloor.nosql.dynamodb.test.DynamoDbConnectExtension;
import net.officefloor.server.http.HttpClientExtension;
import net.officefloor.test.UsesAwsTest;
import net.officefloor.test.UsesDockerTest;
import net.officefloor.tutorial.awssamhttpserver.SamLogic.Post;

/**
 * Integration tests the AWS SAM HTTP Server.
 * 
 * @author Daniel Sagenschneider
 */
@UsesDockerTest
@UsesAwsTest
public class AwsSamHttpServerIT {

	// START SNIPPET: tutorial
	public static final @RegisterExtension HttpClientExtension serverClient = new HttpClientExtension(false, 8381)
			.timeout(30_000);

	public static final @RegisterExtension DynamoDbConnectExtension dynamoClient = new DynamoDbConnectExtension(
			new Configuration().port(8382));

	private static final ObjectMapper mapper = new ObjectMapper();

	@Test
	public void createPost() throws IOException {

		// Create the entity
		HttpPost request = new HttpPost(serverClient.url("/posts"));
		request.setHeader("Accept", "application/json"); // Provide Accept header due to AWS SAM requiring it
		request.setHeader("Content-Type", "application/json");
		request.setEntity(new StringEntity(mapper.writeValueAsString(new Post("TEST"))));
		HttpResponse response = serverClient.execute(request);
		String responseBody = EntityUtils.toString(response.getEntity());
		assertEquals(200, response.getStatusLine().getStatusCode(), "Should be successful: " + responseBody);
		PostEntity entity = mapper.readValue(responseBody, PostEntity.class);

		// Ensure in store
		PostEntity stored = dynamoClient.getDynamoDbMapper().load(PostEntity.class, entity.getId());
		assertNotNull(stored, "Should find entity in DynamoDB " + entity.getId());
		assertEquals("TEST", stored.getMessage(), "Incorrent entity");
	}
	// END SNIPPET: tutorial

	@Test
	public void getPosts() throws IOException {

		// Create the entity
		PostEntity entity = new PostEntity(null, "LIST");
		dynamoClient.getDynamoDbMapper().save(entity);

		// Obtain the entities
		HttpGet request = new HttpGet(serverClient.url("/posts"));
		request.setHeader("Accept", "application/json");
		HttpResponse response = serverClient.execute(request);
		String responseBody = EntityUtils.toString(response.getEntity());
		assertEquals(200, response.getStatusLine().getStatusCode(), "Should be successful: " + responseBody);
		PostEntity[] retrieved = mapper.readValue(responseBody, PostEntity[].class);

		// Ensure correct
		assertTrue(retrieved.length > 0, "Should have posts");
		PostEntity list = null;
		for (PostEntity post : retrieved) {
			if ("LIST".equals(post.getMessage())) {
				list = post;
			}
		}
		assertNotNull(list, "Should find LIST post");
	}

	@Test
	public void getPost() throws IOException {

		// Create the entity
		PostEntity entity = new PostEntity(null, "TEST");
		dynamoClient.getDynamoDbMapper().save(entity);

		// Obtain the entity
		HttpGet request = new HttpGet(serverClient.url("/posts/" + entity.getId()));
		request.setHeader("Accept", "application/json");
		HttpResponse response = serverClient.execute(request);
		String responseBody = EntityUtils.toString(response.getEntity());
		assertEquals(200, response.getStatusLine().getStatusCode(), "Should be successful: " + responseBody);
		PostEntity retrieved = mapper.readValue(responseBody, PostEntity.class);

		// Ensure correct
		assertEquals("TEST", retrieved.getMessage(), "Incorrect entity");
	}

	@Test
	public void index() throws IOException {
		HttpGet request = new HttpGet(serverClient.url("/"));
		request.addHeader("Accept", "text/html");
		HttpResponse response = serverClient.execute(request);
		String html = EntityUtils.toString(response.getEntity());
		assertEquals(200, response.getStatusLine().getStatusCode(), "Should be successful: " + html);
		assertTrue(html.contains("<title>AwsSamHttpServer</title>"), "Should get index.html page");
	}

}