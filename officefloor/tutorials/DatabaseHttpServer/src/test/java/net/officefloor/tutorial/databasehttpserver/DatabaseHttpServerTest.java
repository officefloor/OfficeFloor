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
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.sql.DataSource;

import junit.framework.TestCase;
import net.officefloor.jdbc.datasource.DefaultDataSourceFactory;
import net.officefloor.server.http.mock.MockHttpResponse;
import net.officefloor.server.http.mock.MockHttpServer;
import net.officefloor.woof.mock.MockWoofServer;

/**
 * Tests the {@link DatabaseHttpServer}.
 * 
 * @author Daniel Sagenschneider
 */
public class DatabaseHttpServerTest extends TestCase {

	/**
	 * {@link MockWoofServer}.
	 */
	private MockWoofServer server;

	@Override
	protected void tearDown() throws Exception {
		if (this.server != null) {
			this.server.close();
		}
	}

	/**
	 * Ensure able to connect to database with {@link DataSource}.
	 */
	public void testConnection() throws Exception {

		// Obtain connection via DataSource
		// Need to keep connection open to keep database alive
		try (Connection connection = DefaultDataSourceFactory.createDataSource("datasource.properties")
				.getConnection()) {

			// Start test server
			this.server = MockWoofServer.open();

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

	/**
	 * Requests page from HTTP Server.
	 */
	// START SNIPPET: test

	public void testInteraction() throws Exception {

		// Need to keep connection open to keep in memory database alive
		try (Connection connection = DefaultDataSourceFactory.createDataSource("datasource.properties")
				.getConnection()) {

			// Start test server
			this.server = MockWoofServer.open();

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