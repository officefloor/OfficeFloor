/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2013 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.officefloor.tutorial.teamhttpserver;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;

import javax.sql.DataSource;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.hsqldb.jdbc.jdbcDataSource;

import junit.framework.TestCase;
import net.officefloor.OfficeFloorMain;
import net.officefloor.server.http.HttpTestUtil;

/**
 * Tests the {@link TeamHttpServer}.
 * 
 * @author Daniel Sagenschneider
 */
public class TeamHttpServerTest extends TestCase {

	/**
	 * URL for the database.
	 */
	private static final String DATABASE_URL = "jdbc:hsqldb:mem:exampleDb";

	/**
	 * User for the database.
	 */
	private static final String DATABASE_USER = "sa";

	/**
	 * {@link CloseableHttpClient}.
	 */
	private final CloseableHttpClient client = HttpTestUtil.createHttpClient();

	@Override
	protected void setUp() throws Exception {
		// Start the database and HTTP Server
		OfficeFloorMain.open();

		// Request page to allow time for database setup
		this.doRequest("http://localhost:7878/example.woof");
	}

	@Override
	protected void tearDown() throws Exception {

		try {
			// Disconnect client
			this.client.close();
		} finally {
			try {
				// Stop HTTP Server
				OfficeFloorMain.close();
			} finally {
				// Stop database for new instance each test
				DriverManager.getConnection(DATABASE_URL, DATABASE_USER, "").createStatement()
						.execute("SHUTDOWN IMMEDIATELY");
			}
		}
	}

	/**
	 * Ensure able to connect to database with {@link DataSource}.
	 */
	public void testConnection() throws Exception {

		// Request page to allow time for database setup
		this.doRequest("http://localhost:7878/example.woof");

		// Obtain connection via DataSource
		jdbcDataSource dataSource = new jdbcDataSource();
		dataSource.setDatabase(DATABASE_URL);
		dataSource.setUser(DATABASE_USER);
		Connection connection = dataSource.getConnection();

		// Ensure can get initial row
		Thread.sleep(100); // allow time for database setup
		ResultSet resultSet = connection.createStatement()
				.executeQuery("SELECT CODE FROM LETTER_CODE WHERE LETTER = 'A'");
		assertTrue("Ensure have result", resultSet.next());
		assertEquals("Incorrect code for letter", "Y", resultSet.getString("CODE"));
		assertFalse("Ensure no further results", resultSet.next());
		resultSet.close();
	}

	/**
	 * Requests page from HTTP Server.
	 */
	// START SNIPPET: test
	public void testRetrieveEncryptions() throws Exception {

		// Request page to allow time for database setup
		this.doRequest("http://localhost:7878/example.woof");

		// Retrieving from database
		this.doRequest("http://localhost:7878/example-encrypt.woof?letter=A");

		// Looking up within cache
		this.doRequest("http://localhost:7878/example-encrypt.woof?letter=A");
	}

	private void doRequest(String url) throws Exception {
		HttpResponse response = this.client.execute(new HttpGet(url));
		response.getEntity().writeTo(System.out);
		assertEquals("Request should be successful", 200, response.getStatusLine().getStatusCode());
	}
	// END SNIPPET: test

}