package net.officefloor.tutorial.teamhttpserver;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Connection;
import java.sql.PreparedStatement;

import javax.sql.DataSource;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import net.officefloor.jdbc.test.DatabaseTestUtil;
import net.officefloor.plugin.clazz.Dependency;
import net.officefloor.server.http.HttpMethod;
import net.officefloor.server.http.mock.MockHttpResponse;
import net.officefloor.server.http.mock.MockHttpServer;
import net.officefloor.woof.mock.MockWoofServerExtension;

/**
 * Tests the {@link TeamHttpServer}.
 * 
 * @author Daniel Sagenschneider
 */
public class TeamHttpServerTest {

	private @Dependency Connection connection; // keep in memory database alive

	private @Dependency DataSource dataSource;

	@BeforeEach
	public void ensureDataSetup() throws Exception {
		DatabaseTestUtil.waitForAvailableDatabase((context) -> this.dataSource, (connection) -> {

			// Create the table
			connection.createStatement().execute("DROP TABLE IF EXISTS LETTER_CODE");
			connection.createStatement()
					.execute("CREATE TABLE LETTER_CODE ( LETTER CHAR(1) PRIMARY KEY, CODE CHAR(1) )");

			// Load the data
			try (PreparedStatement insert = connection
					.prepareStatement("INSERT INTO LETTER_CODE ( LETTER, CODE ) VALUES ( ?, ? )")) {
				for (char letter = ' '; letter <= 'z'; letter++) {
					char code = (char) ('z' - letter + ' '); // simple reverse order
					insert.setString(1, String.valueOf(letter));
					insert.setString(2, String.valueOf(code));
					insert.execute();
				}
			}
		});
	}

	// START SNIPPET: test
	@RegisterExtension
	public final MockWoofServerExtension server = new MockWoofServerExtension();

	@Test
	public void retrieveEncryptions() throws Exception {

		// Retrieving from database (will have value cached)
		MockHttpResponse response = this.server
				.sendFollowRedirect(MockHttpServer.mockRequest("/example+encrypt?letter=A").method(HttpMethod.POST));
		assertEquals(200, response.getStatus().getStatusCode(), "Should be successful after POST/GET pattern");
		assertFalse(response.getEntity(null).contains("[cached]"), "Ensure not cached (obtain from database)");

		// Looking up within cache (referencing session in cookies)
		response = this.server.sendFollowRedirect(
				MockHttpServer.mockRequest("/example+encrypt?letter=A").method(HttpMethod.POST).cookies(response));
		assertEquals(200, response.getStatus().getStatusCode(), "Should be successful after POST/GET pattern");
		assertTrue(response.getEntity(null).contains("[cached]"), "Ensure cached");
	}
	// END SNIPPET: test

}