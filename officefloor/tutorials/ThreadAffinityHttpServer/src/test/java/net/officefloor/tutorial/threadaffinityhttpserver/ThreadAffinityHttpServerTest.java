package net.officefloor.tutorial.threadaffinityhttpserver;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.persistence.EntityManager;
import javax.sql.DataSource;

import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import net.officefloor.OfficeFloorMain;
import net.officefloor.jdbc.datasource.DefaultDataSourceFactory;
import net.officefloor.jdbc.test.DatabaseTestUtil;
import net.officefloor.plugin.clazz.Dependency;
import net.officefloor.server.http.mock.MockHttpResponse;
import net.officefloor.server.http.mock.MockHttpServer;
import net.officefloor.test.skip.SkipRule;
import net.officefloor.web.executive.WebThreadAffinityExecutiveSource;
import net.officefloor.woof.mock.MockWoofServerRule;

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

	@ClassRule
	public static SkipRule threadAfinityAvailable = new SkipRule(
			!WebThreadAffinityExecutiveSource.isThreadAffinityAvailable());

	private Connection connection; // keep in memory database alive

	@Before
	public void setup() throws Exception {
		this.connection = DatabaseTestUtil.waitForAvailableConnection((context) -> this.dataSource, (connection) -> {
			connection.createStatement().executeUpdate("CREATE TABLE CPU ( ID IDENTITY, CPU_NUMBER INT)");
			PreparedStatement insert = connection.prepareStatement("INSERT INTO CPU ( CPU_NUMBER ) VALUES ( ? )");
			for (int i = 0; i < Runtime.getRuntime().availableProcessors(); i++) {
				insert.setInt(1, i);
				insert.executeUpdate();
			}
		});
	}

	@After
	public void tearDown() throws Exception {
		this.connection.close();
	}

	// START SNIPPET: tutorial
	@Rule
	public MockWoofServerRule server = new MockWoofServerRule(this);

	private @Dependency DataSource dataSource;

	private @Dependency EntityManager entityManager;

	@Test
	public void sameThreadPoolDueToAffinity() throws Exception {

		// On multiple calls, should be same core (as locks affinity)
		String previousCore = null;
		for (int i = 0; i < 100; i++) {

			// GET entry
			MockHttpResponse response = this.server.send(MockHttpServer.mockRequest("/"));
			String html = response.getEntity(null);
			assertEquals("Should be successful: " + html, 200, response.getStatus().getStatusCode());

			// Parse out the core
			Pattern pattern = Pattern.compile(".*CORE-(\\d+)-.*", Pattern.DOTALL);
			Matcher matcher = pattern.matcher(html);
			assertTrue("Should be able to obtain thread affinity core", matcher.matches());
			String core = matcher.group(1);

			// Ensure same as previous core (ignoring first call)
			if (previousCore != null) {
				assertEquals("Should be locked to same core", previousCore, core);
			}

			// Set up for next call
			previousCore = core;
		}
	}
	// END SNIPPET: tutorial

}