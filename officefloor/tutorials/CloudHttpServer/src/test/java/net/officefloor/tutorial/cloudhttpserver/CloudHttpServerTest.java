package net.officefloor.tutorial.cloudhttpserver;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.extension.ExtendWith;

import net.officefloor.cabinet.spi.CabinetManager;
import net.officefloor.cloud.test.CloudTest;
import net.officefloor.cloud.test.OfficeFloorCloudProviders;
import net.officefloor.plugin.clazz.Dependency;
import net.officefloor.server.http.HttpMethod;
import net.officefloor.woof.mock.MockWoofResponse;
import net.officefloor.woof.mock.MockWoofServer;

/**
 * Tests the {@link CloudLogic}.
 * 
 * @author Daniel Sagenschneider
 */
@ExtendWith(OfficeFloorCloudProviders.class)
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

}
