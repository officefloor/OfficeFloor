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
package net.officefloor.tutorial.databasehttpserver;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;

import javax.sql.DataSource;

import junit.framework.TestCase;
import net.officefloor.plugin.woof.WoofOfficeFloorSource;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.hsqldb.jdbc.jdbcDataSource;

/**
 * Tests the {@link DatabaseHttpServer}.
 * 
 * @author Daniel Sagenschneider
 */
public class DatabaseHttpServerTest extends TestCase {

	/**
	 * URL for the database.
	 */
	private static final String DATABASE_URL = "jdbc:hsqldb:mem:exampleDb";

	/**
	 * User for the database.
	 */
	private static final String DATABASE_USER = "sa";

	@Override
	protected void setUp() throws Exception {
		// Start the database and HTTP Server
		WoofOfficeFloorSource.start();
	}

	@Override
	protected void tearDown() throws Exception {

		// Disconnect client
		this.client.getConnectionManager().shutdown();

		// Stop HTTP Server
		WoofOfficeFloorSource.stop();

		// Stop database for new instance each test
		DriverManager.getConnection(DATABASE_URL, DATABASE_USER, "")
				.createStatement().execute("SHUTDOWN IMMEDIATELY");
	}

	/**
	 * Ensure able to connect to database with {@link DataSource}.
	 */
	public void testConnection() throws Exception {

		// Undertake request to allow set-up
		this.doRequest("http://localhost:7878/example.woof");

		// Obtain connection via DataSource
		jdbcDataSource dataSource = new jdbcDataSource();
		dataSource.setDatabase(DATABASE_URL);
		dataSource.setUser(DATABASE_USER);
		Connection connection = dataSource.getConnection();

		// Ensure can get initial row
		ResultSet resultSet = connection.createStatement().executeQuery(
				"SELECT * FROM EXAMPLE");
		assertTrue("Ensure have result", resultSet.next());
		assertEquals("Incorrect name", "WoOF", resultSet.getString("NAME"));
		assertEquals("Incorrect description", "Web on OfficeFloor",
				resultSet.getString("DESCRIPTION"));
		assertFalse("Ensure no further results", resultSet.next());
		resultSet.close();
	}

	/**
	 * Requests page from HTTP Server.
	 */
	// START SNIPPET: test
	private final HttpClient client = new DefaultHttpClient();

	public void testInteraction() throws Exception {

		// Request page
		this.doRequest("http://localhost:7878/example.woof");

		// Add row (will pick up parameter values from URL)
		this.doRequest("http://localhost:7878/example-addRow.woof?name=Daniel&description=Founder");

		// Delete row
		this.doRequest("http://localhost:7878/example-deleteRow.woof?id=1");
	}

	private void doRequest(String url) throws Exception {
		HttpResponse response = this.client.execute(new HttpGet(url));
		assertEquals("Request should be successful", 200, response
				.getStatusLine().getStatusCode());
		response.getEntity().writeTo(System.out);
	}
	// END SNIPPET: test

}