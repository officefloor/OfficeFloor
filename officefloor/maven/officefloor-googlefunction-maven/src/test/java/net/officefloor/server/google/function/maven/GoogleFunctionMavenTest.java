package net.officefloor.server.google.function.maven;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.Firestore;

import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.nosql.firestore.test.AbstractFirestoreConnectJunit.Configuration;
import net.officefloor.nosql.firestore.test.FirestoreConnectExtension;
import net.officefloor.server.google.function.OfficeFloorHttpFunction;
import net.officefloor.server.http.HttpClientExtension;
import net.officefloor.test.UsesDockerTest;
import net.officefloor.test.system.SystemPropertiesExtension;

/**
 * Ensure able to run {@link OfficeFloorHttpFunction}.
 * 
 * @author Daniel Sagenschneider
 */
public class GoogleFunctionMavenTest {

	/**
	 * HTTP port.
	 */
	private final int HTTP_PORT = 8181;

	/**
	 * HTTPS port.
	 */
	private final int HTTPS_PORT = HTTP_PORT + 1;

	/**
	 * {@link Firestore} port.
	 */
	private final int FIRESTORE_PORT = HTTPS_PORT + 1;

	/**
	 * {@link ObjectMapper}.
	 */
	private static final ObjectMapper mapper = new ObjectMapper();

	/**
	 * {@link SystemPropertiesExtension}.
	 */
	private final @RegisterExtension @Order(1) SystemPropertiesExtension systemProperties = new SystemPropertiesExtension(
			MavenGoogleFunctionOfficeFloorExtensionService.HTTP_PORT_NAME, String.valueOf(HTTP_PORT),
			MavenGoogleFunctionOfficeFloorExtensionService.HTTPS_PORT_NAME, String.valueOf(HTTPS_PORT),
			OfficeFloorHttpFunctionMain.FIRESTORE_PORT_NAME, String.valueOf(FIRESTORE_PORT));

	/**
	 * Insecure {@link HttpClientExtension}.
	 */
	private final @RegisterExtension @Order(2) HttpClientExtension insecureClient = new HttpClientExtension();

	/**
	 * Secure {@link HttpClientExtension}.
	 */
	private final @RegisterExtension @Order(3) HttpClientExtension secureClient = new HttpClientExtension(true);

	/**
	 * {@link Firestore}.
	 */
	private final @RegisterExtension @Order(4) FirestoreConnectExtension firestore = new FirestoreConnectExtension(
			new Configuration().port(FIRESTORE_PORT));

	/**
	 * {@link OfficeFloor}.
	 */
	private OfficeFloor officeFloor;

	@BeforeEach
	public void open() throws Exception {
		OfficeFloorHttpFunctionMain main = new OfficeFloorHttpFunctionMain();
		this.officeFloor = main.open();
	}

	@AfterEach
	public void close() throws Exception {
		if (this.officeFloor != null) {
			this.officeFloor.close();
		}
	}

	/**
	 * Ensure can service via HTTP.
	 */
	@UsesDockerTest
	public void serviceViaHttp() throws Exception {
		HttpResponse response = insecureClient.execute(new HttpGet("http://localhost:" + HTTP_PORT));
		String entity = EntityUtils.toString(response.getEntity());
		assertEquals(200, response.getStatusLine().getStatusCode(), "Should be successful: " + entity);
		assertEquals("SERVICED", entity, "Incorrect response");
	}

	/**
	 * Ensure can service via HTTPS.
	 */
	@UsesDockerTest
	public void serviceViaHttps() throws Exception {
		HttpResponse response = secureClient.execute(new HttpGet("https://localhost:" + HTTPS_PORT));
		String entity = EntityUtils.toString(response.getEntity());
		assertEquals(200, response.getStatusLine().getStatusCode(), "Should be successful: " + entity);
		assertEquals("SERVICED", entity, "Incorrect response");
	}

	/**
	 * Ensure create entry with {@link Firestore}.
	 */
	@UsesDockerTest
	public void firestoreCreate() throws Exception {

		// Send request to create entity
		final String MESSAGE = "CREATE";
		HttpPost request = new HttpPost("http://localhost:" + HTTP_PORT + "/create");
		request.setHeader("Content-Type", "application/json");
		request.setEntity(new StringEntity(mapper.writeValueAsString(new Post(null, MESSAGE))));
		HttpResponse response = insecureClient.execute(request);
		String entity = EntityUtils.toString(response.getEntity());
		assertEquals(200, response.getStatusLine().getStatusCode(), "Should be successful: " + entity);

		// Ensure entity created
		Post identifier = mapper.readValue(entity, Post.class);
		Post post = firestore.getFirestore().collection(Post.class.getSimpleName()).document(identifier.getId()).get()
				.get().toObject(Post.class);
		assertEquals(MESSAGE, post.getMessage(), "Incorrect entry");
	}

	/**
	 * Ensure can retrieve {@link Firestore} entry.
	 */
	@UsesDockerTest
	public void firestoreRetrieve() throws Exception {

		// Create entry
		final String MESSAGE = "RETRIEVE";
		DocumentReference docRef = firestore.getFirestore().collection(Post.class.getSimpleName()).document();
		Post entity = new Post(docRef.getId(), MESSAGE);
		docRef.create(entity).get();

		// Retrieve entry
		HttpResponse response = insecureClient
				.execute(new HttpGet("http://localhost:" + HTTP_PORT + "/retrieve/" + entity.getId()));
		String responseEntity = EntityUtils.toString(response.getEntity());
		assertEquals(200, response.getStatusLine().getStatusCode(), "Should be successful: " + responseEntity);

		// Ensure correct entity
		Post post = mapper.readValue(responseEntity, Post.class);
		assertEquals(MESSAGE, post.getMessage(), "Incorrect entry");
	}

}
