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
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Sets up the database.
 * 
 * @author Daniel Sagenschneider
 */
// START SNIPPET: example
public class Setup {

	public void setupDatabase(Connection connection) throws Exception {
		try (Statement statement = connection.createStatement()) {
			try {
				// Determine if table exists
				statement.executeQuery("SELECT * FROM EXAMPLE");

			} catch (SQLException ex) {
				// Create the table and row
				statement.execute(
						"CREATE TABLE EXAMPLE ( ID IDENTITY PRIMARY KEY, NAME VARCHAR(20), DESCRIPTION VARCHAR(256) )");
				statement.execute("INSERT INTO EXAMPLE ( NAME, DESCRIPTION ) VALUES ( 'WoOF', 'Web on OfficeFloor' )");
			}
		}
	}

}
// END SNIPPET: example