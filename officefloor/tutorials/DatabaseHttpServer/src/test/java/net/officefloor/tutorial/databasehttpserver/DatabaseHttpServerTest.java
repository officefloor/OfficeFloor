package net.officefloor.tutorial.databasehttpserver;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.sql.DataSource;

import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import net.officefloor.OfficeFloorMain;
import net.officefloor.jdbc.datasource.DefaultDataSourceFactory;
import net.officefloor.jdbc.test.DataSourceRule;
import net.officefloor.server.http.mock.MockHttpResponse;
import net.officefloor.server.http.mock.MockHttpServer;
import net.officefloor.woof.mock.MockWoofServer;
import net.officefloor.woof.mock.MockWoofServerRule;

/**
 * Tests the {@link DatabaseHttpServer}.
 * 
 * @author Daniel Sagenschneider
 */
public class DatabaseHttpServerTest {

	/**
	 * Run application.
	 */
	public static void main(String[] args) throws Exception {

		// Keep database alive by keeping connection
		DataSource dataSource = DefaultDataSourceFactory.createDataSource("datasource.properties");
		try (Connection connection = dataSource.getConnection()) {
			OfficeFloorMain.main(args);
		}
	}

	/**
	 * Ensure able to connect to database with {@link DataSource}.
	 */
	@Test
	public void testConnection() throws Exception {
		try (Connection connection = dataSource.getConnection()) {

			// Undertake request to allow set-up
			this.server.send(MockWoofServer.mockRequest("/example"));

			// Ensure can get initial row
			ResultSet resultSet = connection.createStatement().executeQuery("SELECT * FROM EXAMPLE");
			assertTrue("Ensure have result", resultSet.next());
			assertEquals("Incorrect name", "WoOF", resultSet.getString("NAME"));
			assertEquals("Incorrect description", "Web on OfficeFloor", resultSet.getString("DESCRIPTION"));
			assertFalse("Ensure no further results", resultSet.next());
		}
	}

	@Test
	public void ensureFullPage() {
		MockHttpResponse response = this.server.send(MockHttpServer.mockRequest("/example"));
		assertEquals("Should be successful", 200, response.getStatus().getStatusCode());
		String body = response.getEntity(null);
		assertTrue("Should have row from database: " + body, body.contains("WoOF"));
		assertTrue("Should have full request: " + body, body.endsWith("</html>"));

	}

	// START SNIPPET: test
	@ClassRule
	public static DataSourceRule dataSource = new DataSourceRule("datasource.properties");

	@Rule
	public MockWoofServerRule server = new MockWoofServerRule();

	@Test
	public void testInteraction() throws Exception {
		try (Connection connection = dataSource.getConnection()) {

			// Request page
			this.server.send(MockHttpServer.mockRequest("/example"));

			// Add row (will pick up parameter values from URL)
			MockHttpResponse response = this.server
					.send(MockHttpServer.mockRequest("/example+addRow?name=Daniel&description=Founder"));
			assertEquals("Should follow POST then GET pattern", 303, response.getStatus().getStatusCode());
			assertEquals("Ensure redirect to load page", "/example", response.getHeader("location").getValue());

			// Ensure row in database
			PreparedStatement statement = connection.prepareStatement("SELECT * FROM EXAMPLE WHERE NAME = 'Daniel'");
			ResultSet resultSet = statement.executeQuery();
			assertTrue("Should find row", resultSet.next());
			assertEquals("Ensure correct row", "Founder", resultSet.getString("DESCRIPTION"));

			// Delete row
			this.server.send(MockHttpServer.mockRequest("/example+deleteRow?id=" + resultSet.getInt("ID")));

			// Ensure row is deleted
			resultSet = statement.executeQuery();
			assertFalse("Row should be deleted", resultSet.next());
		}
	}
	// END SNIPPET: test

}