/*-
 * #%L
 * Team HTTP Server Tutorial
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package net.officefloor.tutorial.teamhttpserver;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Sets up the database.
 * 
 * @author Daniel Sagenschneider
 */
// START SNIPPET: tutorial
public class Setup {

	public void setupDatabase(Connection connection) throws SQLException {
		try (Statement statement = connection.createStatement()) {
			try {

				// Determine if table exists
				statement.executeQuery("SELECT * FROM LETTER_CODE");

			} catch (SQLException ex) {

				// Create the table
				statement.execute("CREATE TABLE LETTER_CODE ( LETTER CHAR(1) PRIMARY KEY, CODE CHAR(1) )");

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
			}
		}
	}

}
// END SNIPPET: tutorial
