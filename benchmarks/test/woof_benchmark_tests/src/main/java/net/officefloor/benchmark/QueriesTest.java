package net.officefloor.benchmark;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.concurrent.ThreadLocalRandom;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.Data;
import net.officefloor.jdbc.postgresql.test.PostgreSqlRule;
import net.officefloor.server.http.HttpClientRule;
import net.officefloor.server.http.HttpServer;
import net.officefloor.server.http.HttpServerLocation;
import net.officefloor.server.http.SystemPropertiesRule;
import net.officefloor.test.OfficeFloorRule;

/**
 * Tests multiple queries.
 */
public class QueriesTest {

	@ClassRule
	public static PostgreSqlRule dataSource = BenchmarkEnvironment.createPostgreSqlRule();

	@ClassRule
	public static SystemPropertiesRule systemProperties = new SystemPropertiesRule(HttpServer.PROPERTY_HTTP_SERVER_NAME,
			"OF", HttpServer.PROPERTY_HTTP_DATE_HEADER, "true", HttpServerLocation.PROPERTY_HTTP_PORT, "8181");

	@Rule
	public OfficeFloorRule server = new OfficeFloorRule();

	@Rule
	public HttpClientRule client = new HttpClientRule();

	@Before
	public void setupDatabase() throws Exception {
		try (Connection connection = dataSource.getConnection()) {
			try {
				connection.createStatement().executeQuery("SELECT * FROM World");
			} catch (SQLException ex) {
				connection.createStatement()
						.executeUpdate("CREATE TABLE World ( id INT PRIMARY KEY, randomNumber INT)");
				PreparedStatement insert = connection
						.prepareStatement("INSERT INTO World (id, randomNumber) VALUES (?, ?)");
				for (int i = 0; i < 10000; i++) {
					insert.setInt(1, i + 1);
					insert.setInt(2, ThreadLocalRandom.current().nextInt(1, 10000));
					insert.executeUpdate();
				}
			}
		}
	}

	protected String getServerName() {
		return "OF";
	}

	@Test
	public void ensureHandleZero() throws Exception {
		this.doTest("0", 1);
	}

	@Test
	public void ensureHandleBlank() throws Exception {
		this.doTest("", 1);
	}

	@Test
	public void ensureHandleFoo() throws Exception {
		this.doTest("foo", 1);
	}

	@Test
	public void ensureHandleLarge() throws Exception {
		this.doTest("501", 500);
	}

	@Test
	public void ensureMultiple() throws Exception {
		this.doTest("20", 20);
	}

	private void doTest(String queriesValue, int expectedRows) throws Exception {
		HttpResponse response = this.client
				.execute(new HttpGet("http://localhost:8181/queries?queries=" + queriesValue));
		String entity = EntityUtils.toString(response.getEntity());
		assertEquals("Should be successful:\n\n" + entity, 200, response.getStatusLine().getStatusCode());
		assertEquals("Incorrect content-type", "application/json", response.getFirstHeader("content-type").getValue());
		assertEquals("Incorrect server", this.getServerName(), response.getFirstHeader("Server").getValue());
		assertNotNull("Should have date", response.getFirstHeader("date"));
		WorldResponse[] worlds = new ObjectMapper().readValue(entity, WorldResponse[].class);
		assertEquals("Incorrect number of worlds", expectedRows, worlds.length);
		for (WorldResponse world : worlds) {
			assertTrue("Invalid id: " + world.id, (world.id >= 1) && (world.id <= 10000));
		}
	}

	@Test
	public void stress() throws Exception {
		BenchmarkEnvironment.doStressTest("http://localhost:8181/queries?queries=10", 4, 10, 25);
	}

	@Data
	public static class WorldResponse {
		private int id;
		private int randomNumber;
	}

}
