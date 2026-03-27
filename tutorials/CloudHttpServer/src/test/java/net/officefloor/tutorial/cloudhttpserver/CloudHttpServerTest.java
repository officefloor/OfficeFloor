package net.officefloor.tutorial.cloudhttpserver;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.extension.ExtendWith;

import net.officefloor.cabinet.spi.CabinetManager;
import net.officefloor.cloud.test.CloudTest;
import net.officefloor.cloud.test.CloudTestService;
import net.officefloor.cloud.test.OfficeFloorCloudProviders;
import net.officefloor.plugin.clazz.Dependency;
import net.officefloor.server.http.HttpMethod;
import net.officefloor.test.UsesDockerTest;
import net.officefloor.woof.mock.MockWoofResponse;
import net.officefloor.woof.mock.MockWoofServer;

/**
 * Tests the {@link CloudLogic}.
 * 
 * @author Daniel Sagenschneider
 */
@ExtendWith(OfficeFloorCloudProviders.class)
@UsesDockerTest
public class CloudHttpServerTest {

	private @Dependency MockWoofServer server;

	private @Dependency PostRepository repository;

	private @Dependency CabinetManager cabinetManager;

	@CloudTest
	public void repository() {
		Post post = new Post("Test");
		this.repository.store(post);
		assertNotNull(post.getKey(), "Should assign key on store");
		Post retrieved = this.repository.getPostByKey(post.getKey());
		assertEquals(post.getMessage(), retrieved.getMessage(), "Incorrect post");
	}

	@CloudTest
	public void store() {
		Post post = new Post("Test");
		MockWoofResponse response = this.server.send(MockWoofServer.mockJsonRequest(HttpMethod.POST, "/store", post));
		Post stored = response.getJson(204, Post.class);
		assertNotNull(stored.getKey(), "Should return key to stored post");
		Post retrieved = this.repository.getPostByKey(stored.getKey());
		assertEquals(post.getMessage(), retrieved.getMessage(), "Incorrect post");
	}

	@CloudTest
	public void retrieve(MockWoofServer server) throws Exception {
		Post post = new Post("Test");
		this.repository.store(post);
		this.cabinetManager.flush();
		MockWoofResponse response = this.server.send(MockWoofServer.mockRequest("/retrieve/" + post.getKey()));
		Post retrieved = response.getJson(200, Post.class);
		assertEquals(post.getMessage(), retrieved.getMessage(), "Incorrect post");
	}

	private static final List<String> cloudProviders = new ArrayList<>(2);

	@CloudTest
	public void registerCloudProviders(CloudTestService service) {
		cloudProviders.add(service.getCloudServiceName());
	}

	@AfterAll
	public static void ensureCloudProvidersRegistered() {
		final String[] EXPECTED = new String[] { "AWS", "Google" };
		assertEquals(EXPECTED.length, cloudProviders.size(), "Incorrect number of cloud providers " + cloudProviders);
		for (int i = 0; i < EXPECTED.length; i++) {
			assertEquals(EXPECTED[i], cloudProviders.get(i), "Incorrect cloud provider " + i);
		}
	}

}
