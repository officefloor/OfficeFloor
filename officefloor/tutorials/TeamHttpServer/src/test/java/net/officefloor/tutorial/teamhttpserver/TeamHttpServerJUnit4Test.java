package net.officefloor.tutorial.teamhttpserver;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.sql.Connection;
import java.sql.ResultSet;

import javax.sql.DataSource;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import net.officefloor.jdbc.test.DatabaseTestUtil;
import net.officefloor.plugin.clazz.Dependency;
import net.officefloor.server.http.HttpMethod;
import net.officefloor.server.http.mock.MockHttpResponse;
import net.officefloor.server.http.mock.MockHttpServer;
import net.officefloor.woof.mock.MockWoofServerRule;

/**
 * Tests the {@link TeamHttpServer}.
 * 
 * @author Daniel Sagenschneider
 */
public class TeamHttpServerJUnit4Test {

	private @Dependency Connection connection; // keep in memory database alive

	private @Dependency DataSource dataSource;

	@Before
	public void ensureDataSetup() throws Exception {
		DatabaseTestUtil.waitForAvailableDatabase((context) -> this.dataSource, (connection) -> {
			ResultSet resultSet = connection.createStatement()
					.executeQuery("SELECT CODE FROM LETTER_CODE WHERE LETTER = 'A'");
			assertTrue("Ensure have result", resultSet.next());
			assertEquals("Incorrect code for letter", "Y", resultSet.getString("CODE"));
			assertFalse("Ensure no further results", resultSet.next());
		});
	}

	// START SNIPPET: test
	@Rule
	public final MockWoofServerRule server = new MockWoofServerRule(this);

	@Test
	public void retrieveEncryptions() throws Exception {

		// Retrieving from database (will have value cached)
		MockHttpResponse response = this.server
				.sendFollowRedirect(MockHttpServer.mockRequest("/example+encrypt?letter=A").method(HttpMethod.POST));
		assertEquals("Should be successful after POST/GET pattern", 200, response.getStatus().getStatusCode());
		assertFalse("Ensure not cached (obtain from database)", response.getEntity(null).contains("[cached]"));

		// Looking up within cache (referencing session in cookies)
		response = this.server.sendFollowRedirect(
				MockHttpServer.mockRequest("/example+encrypt?letter=A").method(HttpMethod.POST).cookies(response));
		assertEquals("Should be successful after POST/GET pattern", 200, response.getStatus().getStatusCode());
		assertTrue("Ensure cached", response.getEntity(null).contains("[cached]"));
	}
	// END SNIPPET: test

}