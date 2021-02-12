package net.officefloor.tutorial.awssamhttpserver;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.amazonaws.client.builder.AwsClientBuilder.EndpointConfiguration;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.fasterxml.jackson.databind.ObjectMapper;

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

	public final @RegisterExtension HttpClientExtension client = new HttpClientExtension(false, 8181).timeout(30_000);

	private static DynamoDBMapper dynamo;

	private static final ObjectMapper mapper = new ObjectMapper();

	@BeforeAll
	public static void connect() {
		dynamo = new DynamoDBMapper(
				AmazonDynamoDBClientBuilder.standard()
						.withEndpointConfiguration(
								new EndpointConfiguration("http://localhost:8282", Regions.DEFAULT_REGION.getName()))
						.build());
	}

	@Test
	public void createPost() throws IOException {

		// Create the entity
		HttpPost request = new HttpPost(client.url("/post"));
		request.setHeader("Accept", "application/json");
		request.setEntity(new StringEntity(mapper.writeValueAsString(new Post("TEST"))));
		HttpResponse response = client.execute(request);
		String responseBody = EntityUtils.toString(response.getEntity());
		assertEquals(200, response.getStatusLine().getStatusCode(), "Should be successful: " + responseBody);
		PostEntity entity = mapper.readValue(responseBody, PostEntity.class);

		// Ensure in store
		PostEntity stored = dynamo.load(PostEntity.class, entity.getId());
		assertNotNull(stored, "Should find entity in DynamoDB " + entity.getId());
		assertEquals("TEST", stored.getMessage(), "Incorrent entity");
	}

	@Test
	public void getPost() throws IOException {

		// Create the entity
		PostEntity entity = new PostEntity(null, "TEST");
		dynamo.save(entity);

		// Obtain the entity
		HttpGet request = new HttpGet(client.url("/post/" + entity.getId()));
		request.setHeader("Accept", "application/json");
		HttpResponse response = client.execute(request);
		String responseBody = EntityUtils.toString(response.getEntity());
		assertEquals(200, response.getStatusLine().getStatusCode(), "Should be successful: " + responseBody);
		PostEntity retrieved = mapper.readValue(responseBody, PostEntity.class);

		// Ensure correct
		assertEquals("TEST", retrieved.getMessage(), "Incorrect entity");
	}

}