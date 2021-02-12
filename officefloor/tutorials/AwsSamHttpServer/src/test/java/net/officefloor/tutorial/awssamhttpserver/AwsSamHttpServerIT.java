package net.officefloor.tutorial.awssamhttpserver;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

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
	public static final @RegisterExtension HttpClientExtension serverClient = new HttpClientExtension(false, 8181)
			.timeout(30_000);

	public static final @RegisterExtension DynamoDbConnectExtension dynamoClient = new DynamoDbConnectExtension(
			new Configuration().port(8282));

	private static final ObjectMapper mapper = new ObjectMapper();

	@Test
	public void createPost() throws IOException {

		// Create the entity
		HttpPost request = new HttpPost(serverClient.url("/post"));
		request.setHeader("Accept", "application/json");
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
	public void getPost() throws IOException {

		// Create the entity
		PostEntity entity = new PostEntity(null, "TEST");
		dynamoClient.getDynamoDbMapper().save(entity);

		// Obtain the entity
		HttpGet request = new HttpGet(serverClient.url("/post/" + entity.getId()));
		request.setHeader("Accept", "application/json");
		HttpResponse response = serverClient.execute(request);
		String responseBody = EntityUtils.toString(response.getEntity());
		assertEquals(200, response.getStatusLine().getStatusCode(), "Should be successful: " + responseBody);
		PostEntity retrieved = mapper.readValue(responseBody, PostEntity.class);

		// Ensure correct
		assertEquals("TEST", retrieved.getMessage(), "Incorrect entity");
	}

}