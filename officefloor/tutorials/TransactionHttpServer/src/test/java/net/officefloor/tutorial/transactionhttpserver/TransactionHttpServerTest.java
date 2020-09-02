package net.officefloor.tutorial.transactionhttpserver;

import java.sql.SQLException;
import java.util.Arrays;

import javax.sql.DataSource;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import net.officefloor.plugin.clazz.Dependency;
import net.officefloor.server.http.HttpMethod;
import net.officefloor.woof.mock.MockWoofResponse;
import net.officefloor.woof.mock.MockWoofServer;
import net.officefloor.woof.mock.MockWoofServerExtension;

/**
 * Tests the Transaction HTTP Server.
 * 
 * @author Daniel Sagenschneider
 */
public class TransactionHttpServerTest {

	private static final String POST_CONTENT = "Interesting post article";

	private @Dependency DataSource dataSource;

	@BeforeEach
	public void resetDatabase() throws SQLException {
		Flyway flyway = Flyway.configure().dataSource(this.dataSource).load();
		flyway.clean();
		flyway.migrate();
	}

	@Test
	public void createPost() throws Exception {

		// Ensure content not in database
		MockWoofResponse response = this.server.send(MockWoofServer.mockRequest("/posts"));
		response.assertJson(200, Arrays.asList());

		// Create the post entry
		response = this.server
				.send(MockWoofServer.mockJsonRequest(HttpMethod.POST, "/posts", new Post(null, POST_CONTENT)));
		response.assertJson(200, new Post(1, POST_CONTENT));

		// Ensure post persisted
		response = this.server.send(MockWoofServer.mockRequest("/posts"));
		response.assertJson(200, Arrays.asList(new Post(1, POST_CONTENT)));
	}

	// START SNIPPET: commit
	@RegisterExtension
	public final MockWoofServerExtension server = new MockWoofServerExtension();

	@Test
	public void commit() throws Exception {

		// Create, will handle exception and commit
		MockWoofResponse response = this.server
				.send(MockWoofServer.mockJsonRequest(HttpMethod.POST, "/commit", new Post(null, POST_CONTENT)));
		response.assertResponse(201, "committed");

		// Ensure persisted to database
		response = this.server.send(MockWoofServer.mockRequest("/posts"));
		response.assertJson(200, Arrays.asList(new Post(1, POST_CONTENT), new Post(2, "Additional")));
	}
	// END SNIPPET: commit

	// START SNIPPET: rollback
	@Test
	public void rollback() throws Exception {

		// Attempt to create (but should roll back)
		MockWoofResponse response = this.server
				.send(MockWoofServer.mockJsonRequest(HttpMethod.POST, "/rollback", new Post(null, POST_CONTENT)));
		response.assertResponse(500, "rolled back");

		// Ensure not persisted to database
		response = this.server.send(MockWoofServer.mockRequest("/posts"));
		response.assertJson(200, Arrays.asList());
	}
	// END SNIPPET: rollback
}