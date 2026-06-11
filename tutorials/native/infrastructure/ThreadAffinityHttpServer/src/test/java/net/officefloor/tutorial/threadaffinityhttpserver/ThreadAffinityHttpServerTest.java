package net.officefloor.tutorial.threadaffinityhttpserver;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.sql.DataSource;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import net.officefloor.OfficeFloorMain;
import net.officefloor.jdbc.datasource.DefaultDataSourceFactory;
import net.officefloor.jdbc.h2.test.H2Reset;
import net.officefloor.server.http.mock.MockHttpResponse;
import net.officefloor.server.http.mock.MockHttpServer;
import net.officefloor.test.skip.SkipExtension;
import net.officefloor.web.executive.WebThreadAffinityExecutiveSource;
import net.officefloor.woof.mock.MockWoofServerExtension;

/**
 * Tests the thread affinity.
 * 
 * @author Daniel Sagenschneider
 */
public class ThreadAffinityHttpServerTest {

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

	@RegisterExtension
	public static SkipExtension threadAfinityAvailable = new SkipExtension(
			!WebThreadAffinityExecutiveSource.isThreadAffinityAvailable());

	@BeforeEach
	public void setup(H2Reset reset, Connection connection) throws Exception {
		reset.clean();
		connection.createStatement().executeUpdate("CREATE TABLE CPU ( ID IDENTITY, CPU_NUMBER INT)");
		PreparedStatement insert = connection.prepareStatement("INSERT INTO CPU ( CPU_NUMBER ) VALUES ( ? )");
		for (int i = 0; i < Runtime.getRuntime().availableProcessors(); i++) {
			insert.setInt(1, i);
			insert.executeUpdate();
		}
	}

	// START SNIPPET: tutorial
	@RegisterExtension
	public final MockWoofServerExtension server = new MockWoofServerExtension();

	@Test
	public void sameThreadPoolDueToAffinity() throws Exception {

		// On multiple calls, should be same core (as locks affinity)
		String previousCore = null;
		for (int i = 0; i < 100; i++) {

			// GET entry
			MockHttpResponse response = this.server.send(MockHttpServer.mockRequest("/"));
			String html = response.getEntity(null);
			assertEquals(200, response.getStatus().getStatusCode(), "Should be successful: " + html);

			// Parse out the core
			Pattern pattern = Pattern.compile(".*CORE-(\\d+)-.*", Pattern.DOTALL);
			Matcher matcher = pattern.matcher(html);
			assertTrue(matcher.matches(), "Should be able to obtain thread affinity core");
			String core = matcher.group(1);

			// Ensure same as previous core (ignoring first call)
			if (previousCore != null) {
				assertEquals(previousCore, core, "Should be locked to same core");
			}

			// Set up for next call
			previousCore = core;
		}
	}
	// END SNIPPET: tutorial

}