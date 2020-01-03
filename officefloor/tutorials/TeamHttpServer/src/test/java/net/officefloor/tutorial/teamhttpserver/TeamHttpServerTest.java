package net.officefloor.tutorial.teamhttpserver;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.sql.Connection;
import java.sql.ResultSet;

import javax.sql.DataSource;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import net.officefloor.OfficeFloorMain;
import net.officefloor.jdbc.datasource.DefaultDataSourceFactory;
import net.officefloor.jdbc.test.DataSourceRule;
import net.officefloor.server.http.mock.MockHttpResponse;
import net.officefloor.server.http.mock.MockHttpServer;
import net.officefloor.woof.mock.MockWoofServerRule;

/**
 * Tests the {@link TeamHttpServer}.
 * 
 * @author Daniel Sagenschneider
 */
public class TeamHttpServerTest {

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

	@Before
	public void ensureData() throws Exception {

		// Request page to allow time for database setup
		MockHttpResponse response = this.server.send(MockHttpServer.mockRequest("/example"));
		assertEquals("Should be sucessful", 200, response.getStatus().getStatusCode());

		// Ensure can get initial row
		DataSourceRule.waitForDatabaseAvailable((context) -> {
			try (Connection connection = context.setConnection(dataSource.getConnection())) {
				ResultSet resultSet = connection.createStatement()
						.executeQuery("SELECT CODE FROM LETTER_CODE WHERE LETTER = 'A'");
				assertTrue("Ensure have result", resultSet.next());
				assertEquals("Incorrect code for letter", "Y", resultSet.getString("CODE"));
				assertFalse("Ensure no further results", resultSet.next());
			}
		});
	}

	// START SNIPPET: test
	@ClassRule
	public static DataSourceRule dataSource = new DataSourceRule("datasource.properties");

	@Rule
	public MockWoofServerRule server = new MockWoofServerRule();

	@Test
	public void retrieveEncryptions() throws Exception {

		// Retrieving from database (will have value cached)
		MockHttpResponse response = this.server.send(MockHttpServer.mockRequest("/example+encrypt?letter=A"));
		String responseBody = response.getEntity(null);
		assertEquals("Follow POST then GET pattern: " + responseBody, 303, response.getStatus().getStatusCode());
		assertFalse("Ensure not cached (obtain from database)", responseBody.contains("[cached]"));

		// Looking up within cache
		response = this.server.send(MockHttpServer.mockRequest("/example+encrypt?letter=A"));
		assertEquals("Follow POST then GET pattern", 303, response.getStatus().getStatusCode());
		assertFalse("Ensure cached", response.getEntity(null).contains("[cached]"));
	}
	// END SNIPPET: test

}