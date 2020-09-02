package net.officefloor.tutorial.databasehttpserver;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

import javax.sql.DataSource;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import net.officefloor.OfficeFloorMain;
import net.officefloor.jdbc.datasource.DefaultDataSourceFactory;
import net.officefloor.jdbc.test.DatabaseTestUtil;
import net.officefloor.plugin.clazz.Dependency;
import net.officefloor.woof.mock.MockWoofResponse;
import net.officefloor.woof.mock.MockWoofServer;
import net.officefloor.woof.mock.MockWoofServerExtension;

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

	private Connection connection; // keep in memory database alive

	@BeforeEach
	public void setupDatabase() throws Exception {
		this.connection = DatabaseTestUtil.waitForAvailableConnection((context) -> this.dataSource, (connection) -> {
			try (Statement statement = connection.createStatement()) {
				statement.execute("DROP ALL OBJECTS");
				statement.execute(
						"CREATE TABLE EXAMPLE ( ID IDENTITY PRIMARY KEY, NAME VARCHAR(20), DESCRIPTION VARCHAR(256) )");
				statement.execute("INSERT INTO EXAMPLE ( NAME, DESCRIPTION ) VALUES ( 'WoOF', 'Web on OfficeFloor' )");
			}
		});
	}

	@AfterEach
	public void closeDatabase() throws Exception {
		this.connection.close();
	}

	/**
	 * Ensure able to connect to database with {@link DataSource}.
	 */
	@Test
	public void testConnection() throws Exception {
		try (Connection connection = this.dataSource.getConnection()) {

			// Undertake request to allow set-up
			this.server.send(MockWoofServer.mockRequest("/example"));

			// Ensure can get initial row
			ResultSet resultSet = connection.createStatement().executeQuery("SELECT * FROM EXAMPLE");
			assertTrue(resultSet.next(), "Ensure have result");
			assertEquals("WoOF", resultSet.getString("NAME"), "Incorrect name");
			assertEquals("Web on OfficeFloor", resultSet.getString("DESCRIPTION"), "Incorrect description");
			assertFalse(resultSet.next(), "Ensure no further results");
		}
	}

	@Test
	public void ensureFullPage() {
		MockWoofResponse response = this.server.send(MockWoofServer.mockRequest("/example"));
		assertEquals(200, response.getStatus().getStatusCode(), "Should be successful");
		String body = response.getEntity(null);
		assertTrue(body.contains("WoOF"), "Should have row from database: " + body);
		assertTrue(body.endsWith("</html>"), "Should have full request: " + body);
	}

	// START SNIPPET: test
	@RegisterExtension
	public MockWoofServerExtension server = new MockWoofServerExtension();

	private @Dependency DataSource dataSource;

	@Test
	public void testInteraction() throws Exception {
		try (Connection connection = this.dataSource.getConnection()) {

			// Request page
			this.server.send(MockWoofServer.mockRequest("/example"));

			// Add row (will pick up parameter values from URL)
			MockWoofResponse response = this.server
					.send(MockWoofServer.mockRequest("/example+addRow?name=Daniel&description=Founder"));
			assertEquals(303, response.getStatus().getStatusCode(), "Should follow POST then GET pattern");
			assertEquals("/example", response.getHeader("location").getValue(), "Ensure redirect to load page");

			// Ensure row in database
			PreparedStatement statement = connection.prepareStatement("SELECT * FROM EXAMPLE WHERE NAME = 'Daniel'");
			ResultSet resultSet = statement.executeQuery();
			assertTrue(resultSet.next(), "Should find row");
			assertEquals("Founder", resultSet.getString("DESCRIPTION"), "Ensure correct row");

			// Delete row
			this.server.send(MockWoofServer.mockRequest("/example+deleteRow?id=" + resultSet.getInt("ID")));

			// Ensure row is deleted
			resultSet = statement.executeQuery();
			assertFalse(resultSet.next(), "Row should be deleted");
		}
	}
	// END SNIPPET: test

}